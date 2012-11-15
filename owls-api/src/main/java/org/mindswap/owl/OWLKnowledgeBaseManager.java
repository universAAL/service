/*
 * Created 04.07.2010
 *
 * (c) 2010 Thorsten Möller - University of Basel Switzerland
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
package org.mindswap.owl;

import java.net.URI;
import java.util.Set;

/**
 * Knowledge base managers are used to control behavior of loading ontologies
 * (via {@link OWLReader readers}) into {@link OWLKnowledgeBase knowledge bases}.
 * More precisely, one can specify whether certain ontologies should be ignored
 * - not loaded - if they are (transitively) imported. Second, KB manager provide
 * methods to (partially) dispose cached ontologies. Previously loaded ontologies
 * can be kept (in memory) beyond the lifecycle of a KB and among KBs so that
 * they can be efficiently reused. Third, one can specify alternative locations
 * for ontologies which forces load/read from the alternative location instead
 * of the default.
 * <p>
 * Instances may be shared among multiple {@link OWLKnowledgeBase knowledge bases}.
 *
 * @author unascribed
 * @version $Rev: 2572 $; $Author: thorsten $; $Date: 2011-02-03 14:39:03 +0200 (Thu, 03 Feb 2011) $
 */
public interface OWLKnowledgeBaseManager
{
	/**
	 * Add the ontology identified by the given URI to the set of ontologies
	 * we ignore in import statements.
	 *
	 * @param uri The URI of an OWL ontology to ignore when importing.
	 */
	public void addIgnoredOntology(URI uri);

	/**
	 * Clear internal cache partially or completely, that is, dispose one (all)
	 * cached reference(s) that is (are) kept of previously loaded ontologies.
	 *
	 * @param uri The URI of an OWL ontology to remove from the cache. Completely
	 * 	clears the cache if value is <code>null</code>.
	 */
	public void clear(URI uri);

	/**
	 * Clear the alternative location for the ontology identified by the given
	 * URI, i.e., force that it will be loaded from this URI (i.e. it has to be
	 * a URL, in fact, that the system is able to resolve) when it is loaded/read
	 * the next time.
	 *
	 * @param uri The URI of an OWL ontology for which to clear the alternative
	 * 	location.
	 */
	public void clearAlternativeLocation(URI uri);

	/**
	 * The set of ontologies which will be ignored (i.e. not loaded) if they
	 * transitively occur as imports in a ontology to be loaded into a KB.
	 *
	 * @return The set of OWL ontologies we ignore when importing. Empty set
	 * 	if none was {@link #addIgnoredOntology(URI) added}.
	 */
	public Set<URI> getIgnoredOntologies();

	/**
	 * Remove the ontology identified by the given URI from the set of ontologies
	 * ignored in import statements.
	 *
	 * @param uri The URI of an OWL ontology to take into account when importing.
	 * @return <code>true</code> If the set of ignored ontologies changed as a
	 * 	result of this method invocation, <code>false</code> otherwise.
	 */
	public boolean removeIgnoredOntology(URI uri);

	/**
	 * Set an entry for a location where the ontology identified by the given URI
	 * can also be loaded/read from. This method needs to be invoked <b>before</b>
	 * the ontology is read/loaded to the KB that uses this manager.
	 *
	 * @param original The default or main URL of the ontology document.
	 * @param copy URL representing an alternative location where a copy of the
	 * 	ontology document can be found.
	 * @return The previous alternative location, if any, or
	 * 	<code>original</code> otherwise.
	 */
	public URI setAlternativeLocation(URI original, URI copy);

	/**
	 * <strong>Attention:</strong> When Jena is used as the backing RDF framework
	 * turning of caching only affects load/read of top-level ontologies and not
	 * imported ontologies. Imagine this property is switched to <code>false</code>.
	 * Thus, the internal cache will be entirely cleared. If a ontology that imports
	 * another ontology <tt>A</tt> is read/loaded afterwards, then <tt>A</tt> will
	 * be cached and any subsequent attempt to read/load another ontology that also
	 * imports <tt>A</tt> will get the cached reference (not accessing the source).
	 * This can only be circumvented by invoking {@link #clear(URI)} with the URI
	 * of <tt>A</tt> as the parameter.
	 *
	 * @param useCache If <code>true</code> references of already loaded ontologies
	 * 	will be kept in a internal (main memory) cache to efficiently reuse them.
	 * 	Note that switching from <code>true</code> to <code>false</code> involves
	 * 	clearing the entire cache, see {@link #clear(URI)}.
	 */
	public void setCaching(boolean useCache);

	/**
	 * @return <code>true</code> if a cache is used which keeps previously loaded
	 * 	ontologies for reuse on repeated load, <code>false</code> otherwise.
	 */
	public boolean usesCache();

}
