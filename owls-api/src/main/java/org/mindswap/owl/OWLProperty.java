/*
 * Created on Nov 20, 2004
 */
package org.mindswap.owl;


/**
 *
 * @author unascribed
 * @version $Rev: 2269 $; $Author: thorsten $; $Date: 2009-08-19 18:21:09 +0300 (Wed, 19 Aug 2009) $
 */
public interface OWLProperty extends OWLEntity
{
	/**
	 * @return <code>true</code> if this this property has an
	 * 	<code>rdf:type</code> that defines it as an annotation property.
	 */
	public boolean isAnnotationProperty();

	/**
	 * @return <code>true</code> if this this property has an
	 * 	<code>rdf:type</code> that defines it as a datatype property.
	 */
	public boolean isDatatypeProperty();

	/**
	 * @return <code>true</code> if this this property has an
	 * 	<code>rdf:type</code> that defines it as an object property.
	 */
	public boolean isObjectProperty();
}
