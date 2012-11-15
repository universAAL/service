// The MIT License
//
// Copyright (c) 2004 Evren Sirin
// Copyright (c) 2009 Thorsten M?ller - University of Basel Switzerland
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
 * Created on Dec 29, 2003
 */
package org.mindswap.owl;

import impl.owl.CastingList;

import java.util.List;

import org.mindswap.common.Parser;
import org.mindswap.common.ServiceFinder;
import org.mindswap.common.Variable;
import org.mindswap.exceptions.CastingException;
import org.mindswap.query.ABoxQuery;
import org.mindswap.query.QueryLanguage;

/**
 *
 * @author unascribed
 * @version $Rev: 2530 $; $Author: thorsten $; $Date: 2010-07-30 20:46:00 +0300 (Fri, 30 Jul 2010) $
 */
public class OWLFactory
{
	private static final OWLProvider factory = ServiceFinder.instance().loadImplementation(
		OWLProvider.class, OWLProvider.DEFAULT_OWL_PROVIDER);

	/**
	 * Create a new and initially empty knowledge base. This method is functionally
	 * equivalent to invoking <code>createKB(null)</code>.
	 *
	 * @return A new initially empty knowledge base.
	 * @see #createKB(OWLKnowledgeBaseManager)
	 */
	public static final OWLKnowledgeBase createKB()
	{
		return factory.createKB();
	}

	/**
	 * Create a new knowledge base. Initially, its properties are set as follows
	 * <ul>
	 * 	<li>no reasoner is attached, i.e, {@link OWLKnowledgeBase#getReasoner()}
	 * 	returns <code>null</code>,</li>
	 * 	<li>auto consistency is disabled, i.e, {@link OWLKnowledgeBase#isAutoConsistency()}
	 * 	returns <code>false</code>,</li>
	 * 	<li>auto translation is disabled, i.e., {@link OWLKnowledgeBase#isAutoTranslate()}
	 * 	returns <code>false</code>, and</li>
	 * 	<li>strict conversion is enabled, i.e., {@link OWLKnowledgeBase#isStrictConversion()},
	 * 	returns <code>true</code>.</li>
	 * </ul>
	 * @param manager If <code>null</code> the KB uses the global/default KB
	 * 	manager, i.e., load and caching properties of the associated
	 * 	{@link OWLKnowledgeBase#getManager() KB manager} are shared by all
	 * 	KB created this way. Otherwise, the created KB will have the behavior
	 * 	as specified by the given KB manager (which may also be shared by
	 * 	multiple KBs).
	 *
	 * @return A new initially empty knowledge base.
	 */
	public static final OWLKnowledgeBase createKB(final OWLKnowledgeBaseManager manager)
	{
		return factory.createKB(manager);
	}

	/**
	 * Create a new knowledge base manager (that can be used by multiple KBs) to
	 * adjust load and caching behavior independently from the default/global
	 * KB manager.
	 *
	 * @return A new knowledge base manager.
	 */
	public static final OWLKnowledgeBaseManager createKBManager()
	{
		return factory.createKBManager();
	}

	/**
	 * @param <T> The type of OWL individuals the list is supposed to contain.
	 * @return A new and empty individuals list.
	 */
	public static final <T extends OWLIndividual> OWLIndividualList<T> createIndividualList() {
		return factory.createIndividualList();
	}

	/**
	 * @param <T> The type of OWL individuals the list is supposed to contain.
	 * @param capacity The initial capacity of the list.
	 * @return A new and empty individuals list with the specified initial capacity.
	 * @exception IllegalArgumentException if the specified initial capacity
	 *            is negative.
	 */
	public static final <T extends OWLIndividual> OWLIndividualList<T> createIndividualList(final int capacity)
	{
		return factory.createIndividualList(capacity);
	}

	/**
	 * @return A list of unique names of reasoners provided by the
	 * 	backing implementation.
	 */
	public static final List<String> getReasonerNames()
	{
		return factory.getReasonerNames();
	}

	/**
	 * Create a parser that can convert queries of the given query language into
	 * ABox queries. The details (formal semantics) of such conversions are up
	 * to be defined elsewhere.
	 *
	 * @param model The model to be queried by the returned ABox query.
	 * @param lang The language/syntax of the query.
	 * @return A new ABox query parser.
	 */
	public static final Parser<String, ABoxQuery<Variable>> createABoxQueryParser(
		final OWLModel model, final QueryLanguage lang)
	{
		return factory.createABoxQueryParser(model, lang);
	}

	/**
	 * Cast all elements of the given list to the target OWL concept and
	 * return them in a new list. It can be used whenever it is known that
	 * all source elements also represent the target OWL concept.
	 * <p>
	 * The order of elements is preserved.
	 * <p>
	 * In contrast to {@link #wrapList(List, Class)} this method
	 * reports failed attempts to cast some element into the target concept
	 * immediately by throwing a {@link CastingException}. For the former
	 * throwing this exception is delayed until the first access of some
	 * element in the list.
	 *
	 * @param list The list of elements to be casted.
	 * @param castTarget The target OWL concept into which to cast each element.
	 * @return The list of elements, all casted to the target OWL concept.
	 * @throws AssertionError If <tt>list</tt> and/or <tt>castTarget</tt>
	 * 	is <code>null</code>.
	 * @throws CastingException If some element does not represent an OWL
	 * 	individual (implicitly or explicitly) of the target concept.
	 */
	public static final <T extends OWLIndividual> OWLIndividualList<T> castList(
		final List<? extends OWLIndividual> list, final Class<T> castTarget)
	{
		return factory.castList(list, castTarget);
	}

	/**
	 * Wrap all elements of the given list by a list that allows dynamic casting
	 * of elements into the target OWL concept, only if they are accessed in the
	 * returned list. It can be used whenever it is known that all source elements
	 * also represent the target OWL concept.
	 * <p>
	 * The order of elements is preserved.
	 * <p>
	 * In contrast to {@link #castList(List, Class)} this method
	 * reports failed attempts to cast some element into the target concept
	 * only on first access from the returned list (by throwing a
	 * {@link CastingException}), whereas the former would throw this
	 * exception immediately when elements in the source list are casted and
	 * added to the parameterized list.
	 *
	 * @param list The list of elements to be wrapped.
	 * @param castTarget The target OWL concept into which to cast each element.
	 * @return A list of all elements that will be casted dynamically into the
	 * 	target OWL concept whenever accessed.
	 * @throws AssertionError If <tt>list</tt> and/or <tt>castTarget</tt> is
	 * 	<code>null</code>.
	 */
	public static final <T extends OWLIndividual> CastingList<T> wrapList(
		final List<? extends OWLIndividual> list, final Class<T> castTarget)
	{
		return factory.wrapList(list, castTarget);
	}

	/**
	 * @return The empty OWL individual list (immutable).
	 */
	public static final <T extends OWLIndividual> OWLIndividualList<T> emptyIndividualList()
	{
		return factory.emptyIndividualList();
	}

	/**
    * Returns an unmodifiable view of the specified list.  This method allows
    * to provide users with "read-only" access to lists.  Query operations on
    * the returned list "read through" to the specified list, and attempts to
    * modify the returned list, whether direct or via its iterator, result in
    * an <tt>UnsupportedOperationException</tt>.<p>
    *
    * @param  list The list for which an unmodifiable view is to be returned.
    * @return An unmodifiable view of the specified list.
    */
	public static final <T extends OWLIndividual> OWLIndividualList<T> unmodifiableIndividualList(
		final List<T> list)
	{
		return factory.unmodifiableIndividualList(list);
	}

}
