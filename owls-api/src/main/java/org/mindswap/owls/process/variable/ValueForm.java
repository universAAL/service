/*
 * Created on Aug 26, 2004
 */
package org.mindswap.owls.process.variable;

import org.mindswap.owl.OWLIndividual;

/**
 * @author unascribed
 * @version $Rev: 2269 $; $Author: thorsten $; $Date: 2009-08-19 18:21:09 +0300 (Wed, 19 Aug 2009) $
 */
public interface ValueForm extends ParameterValue
{
	public String getForm();

	public OWLIndividual getFormAsIndividual();

	public void setForm(String formLiteral);

	public void removeForm();
}
