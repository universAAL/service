/*
 * Created 05.07.2010
 *
 * (c) 2010 Thorsten M?ller - University of Basel Switzerland
 *
 * The MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */
package impl.jena;

import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.mindswap.owl.OWLKnowledgeBaseManager;
import org.mindswap.utils.URIUtils;

import com.hp.hpl.jena.graph.GraphMaker;
import com.hp.hpl.jena.ontology.OntModelSpec;

/**
 * @author unascribed
 * @version $Rev: 2530 $; $Author: thorsten $; $Date: 2010-07-30 20:46:00 +0300 (Fri, 30 Jul 2010) $
 */
public class OWLKnowledgeBaseManagerImpl implements OWLKnowledgeBaseManager
{
	private final OntModelSpec spec;

	// Stores error messages resulting from failed attempts to load/read some ontology by OWLReaderImpl
	// Access to this map may happen concurrently by different threads, thus, we need to be thread safe.
	private ConcurrentHashMap<String, String> errorMsgMap;

	public OWLKnowledgeBaseManagerImpl(final OntModelSpec spec)
	{
		this.spec = spec;
	}

	/* @see java.lang.Object#equals(java.lang.Object) */
	@Override
	public boolean equals(final Object obj)
	{
		// result is determined by the OntDocumentManager as the same default instance
		// is used whenever global load and caching behavior is used

		if ((obj == null) || (obj.getClass() != this.getClass())) return false;
		final OWLKnowledgeBaseManagerImpl other = (OWLKnowledgeBaseManagerImpl) obj;
		return spec.getDocumentManager().equals(other.spec.getDocumentManager());
	}

	/* @see java.lang.Object#hashCode() */
	@Override
	public int hashCode()
	{
		return spec.getDocumentManager().hashCode();
	}

	/* @see org.mindswap.owl.OWLKnowledgeBaseManager#addIgnoredOntology(java.net.URI) */
	public void addIgnoredOntology(final URI uri)
	{
		if (uri == null) return;
		spec.getDocumentManager().addIgnoreImport(URIUtils.standardURI(uri).toString());
	}

	/* @see org.mindswap.owl.OWLKnowledgeBaseManager#clear(java.net.URI) */
	public void clear(final URI uri)
	{
		if (uri == null)
		{
			if (errorMsgMap != null) errorMsgMap.clear();
			for (Iterator<String> it = spec.getImportModelMaker().getGraphMaker().listGraphs(); it.hasNext(); it.remove())
			{
				it.next();
			}
			spec.getDocumentManager().clearCache();
		}
		else
		{
			final String ontURI = uri.toString();

			// Following three operations should actually be atomic to make it thread-safe.
			// If we remove from errorMsgMap first and then clear the caches, a load in between
			// the two operations would still get a reference from the cache, hence, a
			// load/read failure can not occur.
			if (errorMsgMap != null) errorMsgMap.remove(ontURI);
			GraphMaker gm = spec.getImportModelMaker().getGraphMaker(); 
			if (gm.hasGraph(ontURI)) gm.removeGraph(ontURI); // false if ontURI is never imported
			spec.getDocumentManager().getFileManager().removeCacheModel(ontURI);
		}
	}

	/* @see org.mindswap.owl.OWLKnowledgeBaseManager#clearAlternativeLocation(java.net.URI) */
	public void clearAlternativeLocation(final URI uri)
	{
		if (uri == null) return;
		final String ontURI = uri.toString();
		spec.getDocumentManager().getFileManager().getLocationMapper().removeAltEntry(ontURI);
	}

	/* @see org.mindswap.owl.OWLKnowledgeBaseManager#getIgnoredOntologies() */
	public Set<URI> getIgnoredOntologies()
	{
		final Set<URI> ignores = new HashSet<URI>();
		for (Iterator<String> it = spec.getDocumentManager().listIgnoredImports(); it.hasNext(); )
		{
			ignores.add(URIUtils.createURI(it.next()));
		}
		return ignores;
	}

	/* @see org.mindswap.owl.OWLKnowledgeBaseManager#removeIgnoredOntology(java.net.URI) */
	public boolean removeIgnoredOntology(final URI uri)
	{
		if (uri == null) return false;
		final String ontURI = uri.toString();
		if (spec.getDocumentManager().ignoringImport(ontURI))
		{
			spec.getDocumentManager().removeIgnoreImport(ontURI);
			return true;
		}
		return false;
	}

	/* @see org.mindswap.owl.OWLKnowledgeBaseManager#setAlternativeLocation(java.net.URI, java.net.URI) */
	public URI setAlternativeLocation(final URI original, final URI copy)
	{
		if (original == null || copy == null) return null;
		final String originalAsString = original.toString();
		final String prevAltnURI = spec.getDocumentManager().doAltURLMapping(originalAsString);
		spec.getDocumentManager().addAltEntry(originalAsString, copy.toString());
		return URIUtils.createURI(prevAltnURI);
	}

	/* @see org.mindswap.owl.OWLKnowledgeBaseManager#setCaching(boolean) */
	public void setCaching(final boolean useCache)
	{
		final boolean current = spec.getDocumentManager().getCacheModels();
		spec.getDocumentManager().setCacheModels(useCache);
		if (current && !useCache) clear(null); // clear cache if we switch from true to false
	}

	/* @see org.mindswap.owl.OWLKnowledgeBaseManager#usesCache() */
	public boolean usesCache()
	{
		return spec.getDocumentManager().getCacheModels();
	}

	OntModelSpec getOntModelSpec()
	{
		return spec;
	}

	void setErrorMsgMap(final ConcurrentHashMap<String, String> errorMsgMap)
	{
		this.errorMsgMap = errorMsgMap;
	}

}
