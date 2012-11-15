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
 * Created on Nov 20, 2004
 */
package org.mindswap.owl;

import java.net.URI;
import java.util.List;

/**
 * Generic interface for OWL {@link OWLClass classes}, {@link OWLProperty properties},
 * and {@link OWLIndividual individuals}.
 *
 * @author unascribed
 * @version $Rev: 2530 $; $Author: thorsten $; $Date: 2010-07-30 20:46:00 +0300 (Fri, 30 Jul 2010) $
 */
public interface OWLEntity extends OWLObject
{
	/**
	 * @return The ontology attached to this OWL entity. Every OWL entity
	 * 	instance has a ontology attached. Usually, this ontology is the
	 * 	model in which it was created, i.e., the model that was subject to
	 * 	be queried when this instance was created.
	 * 	Note that "instance" here refers to the Java object instance. As
	 * 	such, it should not be mixed up with the term individual in the
	 * 	OWL terminology (or resource in RDF) because multiple ontologies
	 * 	may "talk about" the same OWL individual (resource), hence, it may
	 * 	be part of multiple ontologies.
	 */
	public OWLOntology getOntology();

	/**
	 * @return The knowledge base model that contains the
	 * 	{@link #getOntology() ontology model} of this OWL entity. Every
	 * 	ontology model is part of a knowledge base model.
	 */
	public OWLKnowledgeBase getKB();

	/**
	 * @return <code>true</code> if this entity is anonymous (a blank node in
	 * 	RDF terminology).
	 */
	public boolean isAnon();

	/**
	 * @return The URI of this resource. Returns <code>null</code> if this
	 * 	entity {@link #isAnon() is anonymous}.
	 */
	public URI getURI();

	/**
	 * @return The local name part within its {@link #getURI() full name},
	 * 	which is basically the fragment identifier of the URI. Returns
	 * 	<code>null</code> if this entity {@link #isAnon() is anonymous}.
	 */
	public String getLocalName();

	/**
	 * @return
	 */
	public String getQName();

	/**
	 * @return The namespace part within its {@link #getURI() full name}.
	 */
	public String getNamespace();

	/**
	 * @return A label string if this is an anonymous (blank) entity. Usually,
	 * 	labels are unique for the lifetime of the backing {@link #getKB() KB}.
	 * 	Details may vary depending on which RDF/OWL framework is used.
	 */
	public String getAnonID();

	/**
	 * Get the <tt>rdfs:label</tt> for this resource. This function will look
	 * for labels with different language codes according to the parameter. If
	 * there is more than one such resource, an arbitrary selection is made.
	 *
	 * @param lang The language attribute for the desired label (EN, FR, etc) or
	 * 	<code>null</code> for don't care. Will attempt to retrieve the most
	 * 	specific label matching the given language.
	 * @return A label string matching the given language, or <code>null</code>
	 * 	if there is no matching label, i.e., if the label for the given
	 * 	language does not exist return <code>null</code> even if a label is
	 * 	found for another language.
	 */
	public String getLabel(String lang);

	/**
	 * Return all labels written in any language.
	 *
	 * @return List of label strings or an empty list if non exist.
	 */
	public List<String> getLabels();

	/**
	 * Set the value of <tt>rdfs:label</tt> for this resource. Any existing
	 * statements for <tt>rdfs:label</tt> will be removed.
	 *
	 * @param label The label for this entity.
	 * @param lang The language attribute for this label (EN, FR, etc) or
	 * <code>null</code> if not specified.
	 */
	public void setLabel(String label, String lang);

	/**
	 * @param prop
	 * @return
	 */
	public OWLIndividual getPropertyAsIndividual(URI prop);

	/**
	 * @param prop
	 * @param lang
	 * @return
	 */
	public OWLDataValue getPropertyAsDataValue(URI prop, String lang);

	/**
	 * @param prop
	 * @return
	 */
	public OWLIndividualList<?> getPropertiesAsIndividual(URI prop);

	/**
	 * @param prop
	 * @return
	 */
	public List<OWLDataValue> getPropertiesAsDataValue(URI prop);

	/**
	 * @param p
	 * @param o
	 */
	public void addProperty(URI p, OWLDataValue o);

	/**
	 * @param p
	 * @param o
	 */
	public void addProperty(URI p, OWLEntity o);

	/**
	 * @param p
	 * @param o
	 * @param lang
	 */
	public void addProperty(URI p, String o, String lang);

	/**
	 * @param p
	 * @param o
	 */
	public void setProperty(URI p, OWLDataValue o);

	/**
	 * @param p
	 * @param o
	 */
	public void setProperty(URI p, OWLEntity o);

	/**
	 * @param p
	 * @param o
	 * @param lang
	 */
	public void setProperty(URI p, String o, String lang);

	/**
	 * @param prop
	 */
	public void removeProperties(URI prop);

	/**
	 * Rename this entity throughout the entire {@link #getKB() KB} to which it
	 * "belongs". This method supports making named entities anonymous and vice
	 * versa. However, one should not rename a named entity to the same name nor
	 * does it make sense to make a anonymous entity again anonymous.
	 * <p>
	 * ATTENTION! One should continue working with the renamed/returned Java
	 * object rather than with the Java object instance on which this method
	 * was invoked!
	 *
	 * @param newURI The new identifier for this entity or <code>null</code> to
	 * 	make it a anonymous entity.
	 * @return The renamed entity, which may be another Java object instance!
	 * @throws UnsupportedOperationException If this entity is a {@link OWLProperty}.
	 */
	public OWLEntity rename(URI newURI);

	/**
	 * Get a (more or less) user-friendly string representation of this entity.
	 * The following ordered procedure is used to create the string:
	 * <ol>
	 * 	<li>{@link #getLabel(String) getLabel(null)}. If this returns <code>null</code> then</li>
	 * 	<li>check if this entity is {@link #isAnon() anonymous} and return its
	 * 	anon identifier, otherwise</li>
	 * 	<li>return the <em>short form</em> of this' entities {@link #getURI() URI} using
	 * 	the QName provider of the backing {@link #getKB() KB} (see also
	 * 	{@link org.mindswap.utils.QNameProvider#shortForm(URI)}.</li>
	 * </ol>
	 * @return A "pretty" string representation.
	 */
	public String toPrettyString();
}
