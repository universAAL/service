/*
 * Created on Jul 7, 2004
 */
package org.mindswap.owls.expression;

import java.net.URI;

import org.mindswap.owl.OWLIndividual;
import org.mindswap.owls.vocabulary.OWLS;

/**
 * An instance of this interface represents a particular logical formalism,
 * such as KIF, SWRL, or DRS. There are several instances already provided in
 * {@link OWLS.Expression}.
 *
 * @author unascribed
 * @version $Rev: 2269 $; $Author: thorsten $; $Date: 2009-08-19 18:21:09 +0300 (Wed, 19 Aug 2009) $
 */
public interface LogicLanguage extends OWLIndividual
{
	public URI getRefURI();

	// Deactivated for the moment since we want to prevent modifying
	// this property for constants declared in OWLS.Expression.
//	public void setRefURI(URI uri);
}
