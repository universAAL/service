//The MIT License
//
// Copyright 2004 Evren Sirin
// Copyright 2009 Thorsten Möller - University of Basel Switzerland
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to
// deal in the Software without restriction, including without limitation the
// rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
// sell copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
// IN THE SOFTWARE.
package impl.jena;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.mindswap.owl.OWLErrorHandler;
import org.mindswap.owl.OWLOntology;
import org.mindswap.owl.OWLReader;
import org.mindswap.owl.OWLSyntax;
import org.mindswap.utils.URIUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntDocumentManager.ReadFailureHandler;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.shared.JenaException;

/**
 *
 * @author unascribed
 * @version $Rev: 2530 $; $Author: thorsten $; $Date: 2010-07-30 20:46:00 +0300 (Fri, 30 Jul 2010) $
 */
public class OWLReaderImpl implements OWLReader
{
	static final Logger logger = LoggerFactory.getLogger(OWLReaderImpl.class);

	OWLErrorHandler errHandler;
	private final ConcurrentHashMap<String, String> errorMsgMap;
	private final OWLKnowledgeBaseImpl kb;
	private boolean ignoreFailedImport;
	private OWLSyntax syntax;

	public OWLReaderImpl(final OWLKnowledgeBaseImpl kb)
	{
		super();
		this.kb = kb;
		ignoreFailedImport = false;
		syntax = OWLSyntax.RDFXML;

		// Create and set a new failure handler if not yet set for the OntDocumentManager
		final OntDocumentManager odm = kb.manager.getOntModelSpec().getDocumentManager();
		final FailureHandler fh;
		final ReadFailureHandler rfh;
		synchronized (odm) // odm may be shared by multiple threads (KBs) --> prevent inferences w.r.t. rfh
		{
			rfh = odm.getReadFailureHandler();
			if (rfh == null)
			{
				fh = new FailureHandler();
				odm.setReadFailureHandler(fh);
			}
			else if (rfh instanceof FailureHandler) fh = (FailureHandler) rfh;
			else // rfh is a instance which was set not by us, hence, we should not overwrite it
			{
				logger.warn("{} has another {} already set. Failed attempts to read an ontology will not be reported!",
					OntDocumentManager.class.getSimpleName(),	ReadFailureHandler.class.getSimpleName());
				fh = null;
			}

			errorMsgMap = (fh != null)? fh.errorMessageMap : null;
			kb.manager.setErrorMsgMap(errorMsgMap);
		}
	}

	/* @see org.mindswap.owl.OWLReader#setErrorHandler(org.mindswap.owl.OWLErrorHandler) */
	public OWLErrorHandler setErrorHandler(final OWLErrorHandler newErrHandler)
	{
		OWLErrorHandler old = this.errHandler;
		this.errHandler = newErrHandler;
		return old;
	}

	/* @see org.mindswap.owl.OWLReader#read(java.net.URI) */
	public OWLOntologyImpl read(final URI url) throws IOException
	{
		return readInternal(null, null, url);
	}

	/* @see org.mindswap.owl.OWLReader#read(java.io.Reader, java.net.URI) */
	public OWLOntologyImpl read(final Reader in, final URI baseURI) throws IOException
	{
		return readInternal(null, in, baseURI);
	}

	/* @see org.mindswap.owl.OWLReader#read(java.io.InputStream, java.net.URI) */
	public OWLOntologyImpl read(final InputStream in, final URI baseURI) throws IOException
	{
		return readInternal(in, null, baseURI);
	}

	/* @see org.mindswap.owl.OWLReader#setIgnoreFailedImport(boolean) */
	public void setIgnoreFailedImport(final boolean ignore)
	{
		ignoreFailedImport = ignore;
	}

	/* @see org.mindswap.owl.OWLReader#isIgnoreFailedImport() */
	public boolean isIgnoreFailedImport()
	{
		return ignoreFailedImport;
	}
	
	public void setSyntax(OWLSyntax syntax)
	{
		if (OWLSyntax.RDFXML.equals(syntax) || OWLSyntax.TURTLE.equals(syntax))
			this.syntax = syntax;
		else throw new IllegalArgumentException("This read does not support " + syntax + " syntax.");
	}

	/* @see org.mindswap.owl.OWLReader#supports(org.mindswap.owl.OWLSyntax) */
	public boolean supports(final OWLSyntax stx)
	{
		switch (stx)
		{
			case RDFXML:
			case TURTLE:
				return true;
			default:
				return false;
		}
	}

	private OWLOntologyImpl readInternal(final InputStream stream, final Reader reader, final URI uri)
		throws IOException
	{
		final URI ontURI = (uri != null)? URIUtils.standardURI(uri) :
			URI.create(OWLOntology.SYNTHETIC_ONT_PREFIX + "reader:Ontology" + System.nanoTime());

		OWLOntologyImpl ont = kb.getOntology(ontURI);
		if (ont != null)
		{
			logger.debug("Use already loaded ontology {} from given knowledge base.", ontURI);
		}
		else
		{
			final OntModel jenaModel;
			String base;
			final OntModelSpec spec = JenaOWLProvider.createOntSpec(kb.manager.getOntModelSpec());

			if (stream == null && reader == null) // read from URL --> uri should be != null
			{
				if (uri == null) throw new IOException("URL of ontology to read required (URL null).");
				base = ontURI.toString();
				logger.debug("Reading ontology {} via Jena's OntDocumentManager ...", base);
				jenaModel = spec.getDocumentManager().getOntology(base, spec);
			}
			else
			{
				jenaModel = ModelFactory.createOntologyModel(spec);
				base = (uri != null)? ontURI.toString() : null;
				final RDFReader jenaReader =
					(OWLSyntax.TURTLE.equals(syntax))? jenaModel.getReader("TURTLE") : jenaModel.getReader();
				if (errHandler != null) jenaReader.setErrorHandler(new DefaultErrorHandler());

				try
				{
					if (stream != null)
					{
						logger.debug("Reading ontology from InputStream, using base URI {}  ...", base);
						jenaReader.read(jenaModel, stream, base);
					}
					else if (reader != null)
					{
						logger.debug("Reading ontology from Reader, using base URI {}  ...", base);
						jenaReader.read(jenaModel, reader, base);
					}
					spec.getDocumentManager().loadImports(jenaModel);
				}
				catch (JenaException e)
				{
					throw new IOException("Failed to read resource. Details: " + e);
				}
				finally
				{
					try
					{
						if (stream != null) stream.close();
						else if (reader != null) reader.close();
					}
					catch (IOException e) { /* fall through --> we can't do anything useful to recover anyway */ }
				}
			}

			// Finally, check if reading of ontology or one of its imports failed and throw IOE if necessary.
			// It needs to be done this way because Jena's OntDocumentManager reports failures only to its
			// ReadFailureHandler
			final Set<String> ontsToCheck = new HashSet<String>();
			if (base != null) ontsToCheck.add(base);
			if (!ignoreFailedImport) ontsToCheck.addAll(jenaModel.listImportedOntologyURIs(true));
			final String failures = hasReportedFailure(ontsToCheck);
			if (failures != null)
				throw new IOException("Failed to read ontology. Details: " + failures);

			ont = kb.createOntology(ontURI, jenaModel);
		}

		return ont;
	}

	/**
	 * @param ontologyURIs The set of ontology URIs to check.
	 * @return Concatenation of all failure messages reported, or <code>null</code>
	 * 	if none were reported for the given URIs.
	 */
	private String hasReportedFailure(final Set<String> ontologyURIs)
	{
		final StringBuilder failures = new StringBuilder();
		for (String ontURI : ontologyURIs)
		{
			final String excMsg = errorMsgMap.get(ontURI);
			if (excMsg != null)
			{
				failures.append(excMsg).append(" ");
			}
		}
		return (failures.length() > 0)? failures.toString() : null;
	}

	private final class DefaultErrorHandler implements RDFErrorHandler
	{
		DefaultErrorHandler()
		{
			super();
		}

		/* @see com.hp.hpl.jena.rdf.model.RDFErrorHandler#warning(java.lang.Exception) */
		public void warning(final Exception e)
		{
			errHandler.warning(e);
		}

		/* @see com.hp.hpl.jena.rdf.model.RDFErrorHandler#error(java.lang.Exception) */
		public void error(final Exception e)
		{
			errHandler.error(e);
		}

		/* @see com.hp.hpl.jena.rdf.model.RDFErrorHandler#fatalError(java.lang.Exception) */
		public void fatalError(final Exception e)
		{
			errHandler.fatal(e);
		}
	}

	private static final class FailureHandler implements ReadFailureHandler
	{
		final ConcurrentHashMap<String, String> errorMessageMap;

		FailureHandler()
		{
			super();
			errorMessageMap = new ConcurrentHashMap<String, String>(8);
		}

		/* @see com.hp.hpl.jena.ontology.OntDocumentManager.ReadFailureHandler#handleFailedRead(java.lang.String, com.hp.hpl.jena.rdf.model.Model, java.lang.Exception) */
		public void handleFailedRead(final String url, final Model model, final Exception e)
		{
			final String previous = errorMessageMap.put(url, e.toString() + " " + url);
			if (previous != null) // if Jena uses model caching a failure may occur once only for every URL
				logger.debug("Bug indication: map already contained value for key. Jena cache seems to have changed.");
		}
	}
}
