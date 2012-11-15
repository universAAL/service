// The MIT License
//
// Copyright (c) 2004 Evren Sirin
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

/*
 * Created on Dec 27, 2003
 */
package org.mindswap.owl;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;

/**
 * OWL reader realize parsing and loading serialized OWL ontologies into
 * (in-memory) {@link OWLKnowledgeBase KBs}.
 * <p>
 * If Jena is used as the backing RDF library then repeated reads of the same
 * ontology (including imported ontologies) may bypass the read and use cached
 * in memory instances. This has an effect then in situations when read of some
 * (imported) ontology initially failed (for some I/O related or syntactical
 * reason): Only the first attempt will end by throwing an {@link IOException}
 * then. Subsequent attempts to read the same ontology (even if it is an
 * imported one) will end without an exception and its model will be empty.
 * However, it is possible to (partially) clear the cache by invoking
 * {@link OWLKnowledgeBaseManager#clear(URI)} on the KB manager associated to
 * the KB. After doing so, the next attempt to read the same ontology will try
 * to read the resource from its original location, thus, throwing an exception
 * in case of failures.
 *
 * @author unascribed
 * @version $Rev: 2530 $; $Author: thorsten $; $Date: 2010-07-30 20:46:00 +0300 (Fri, 30 Jul 2010) $
 */
public interface OWLReader
{

	/**
	 * Reads a ontology from the given URL (<tt>file:</tt> and <tt>http:</tt>)
	 * and loads it into the {@link OWLKnowledgeBase KB} to which this reader
	 * is associated. If the ontology imports other ontologies they will be
	 * read into the KB as well, provided that they can be located and accessed
	 * given their import URI.
	 *
	 * @param url The URL of the document to read (provided as a URI).
	 * @return The Ontology model.
	 * @throws IOException In case reading of the referenced ontology failed
	 * 	or was interrupted for some I/O related reason. If
	 * 	{@link #isIgnoreFailedImport()} returns <code>false</code>, this
	 * 	exception may also be thrown if reading of some imported ontology
	 * 	(transitively) failed. However, there is an Jena specific detail
	 * 	caused by the its internal caching mechanism, see class JavaDoc.
	 */
	public OWLOntology read(URI url) throws IOException;

	/**
	 * Reads a ontology from the given {@link Reader}
	 * and loads it into the {@link OWLKnowledgeBase KB} to which this reader
	 * is associated. If the ontology imports other ontologies they will be
	 * read into the KB as well, provided that they can be located and accessed
	 * given their import URI.
	 *
	 * @param in The reader representing the ontology to read.
	 * @param baseURI Logical base URI to resolve relative URIs. This URI will
	 * 	also be assigned to the ontology. If <code>null</code>, a synthetic
	 * 	URI will be created and assigned.
	 * @return The Ontology model.
	 * @throws IOException In case reading of the referenced ontology failed
	 * 	or was interrupted for some I/O related reason. If
	 * 	{@link #isIgnoreFailedImport()} returns <code>false</code>, this
	 * 	exception may also be thrown if reading of some imported ontology
	 * 	(transitively) failed. However, there is an Jena specific detail
	 * 	caused by the its internal caching mechanism, see class JavaDoc.
	 */
	public OWLOntology read(Reader in, URI baseURI) throws IOException;

	/**
	 * Reads a ontology from the given {@link InputStream}
	 * and loads it into the {@link OWLKnowledgeBase KB} to which this reader
	 * is associated. If the ontology imports other ontologies they will be
	 * read into the KB as well, provided that they can be located and accessed
	 * given their import URI.
	 *
	 * @param in The input stream representing the ontology to read.
	 * @param baseURI Logical base URI to to resolve relative URIs. This URI will
	 * 	also be assigned to the ontology. If <code>null</code>, a synthetic
	 * 	URI will be created and assigned.
	 * @return The Ontology model.
	 * @throws IOException In case reading of the referenced ontology failed
	 * 	or was interrupted for some I/O related reason. If
	 * 	{@link #isIgnoreFailedImport()} returns <code>false</code>, this
	 * 	exception may also be thrown if reading of some imported ontology
	 * 	(transitively) failed. However, there is an Jena specific detail
	 * 	caused by the its internal caching mechanism, see class JavaDoc.
	 */
	public OWLOntology read(InputStream in, URI baseURI) throws IOException;

	/**
	 * Set an error handler for this reader.
	 *
	 * @param newErrHandler The new error handler.
	 * @return The previous error handler (if any).
	 */
	public OWLErrorHandler setErrorHandler(OWLErrorHandler newErrHandler);


	/**
	 * Set the way this reader handles the event if some imported ontology can
	 * not be found when reading the ontology that specifies the import.
	 * (Ontologies to import are specified in OWL using the
	 * <code>owl:imports</code> property).
	 *
	 * @param ignore If <code>true</code> the reader will ignore the event when
	 * 	some imported ontology could not be found (for whatever reason). The
	 * 	corresponding read method will then not throw an {@link IOException}
	 * 	for failed attempts to read an imported ontology.
	 */
	public void setIgnoreFailedImport(boolean ignore);

	/**
	 * The way this reader handles the event if some imported ontology can not be
	 * found when reading the ontology that specifies the import. (Ontologies to
	 * import are specified in OWL using the <code>owl:imports</code> property).
	 * <p>
	 * The default value of implementations should be <code>false</code>.
	 *
	 * @return If <code>true</code> the reader will ignore the event when some
	 * 	imported ontology could not be found (for whatever reason). The
	 * 	corresponding read method will then not throw an {@link IOException}
	 * 	for failed attempts to read an imported ontology.
	 */
	public boolean isIgnoreFailedImport();
	
	/**
	 * Set the format of documents to be subsequently read. Each implementation
	 * must use {@link OWLSyntax#RDFXML} by default, i.e., a reader assumes
	 * documents to have this format unless set to another supported format.
	 * Furthermore, implementations must support switching formats between
	 * consecutive reads, i.e., it is not a set once only property.
	 * 
	 * @param syntax The format of ontology documents to be subsequently read.
	 * @throws IllegalArgumentException If the format is not supported.
	 */
	public void setSyntax(OWLSyntax syntax);

	/**
	 * Check if documents of the given format can be read.
	 *
	 * @param syntax The source format.
	 * @return <code>true</code> if the given format is supported by this reader,
	 * 	<code>false</code> otherwise.
	 */
	public boolean supports(OWLSyntax syntax);

}
