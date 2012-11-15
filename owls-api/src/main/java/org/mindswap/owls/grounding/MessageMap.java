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
package org.mindswap.owls.grounding;

import java.net.URI;

import org.mindswap.exceptions.InvalidURIException;
import org.mindswap.owl.OWLIndividual;
import org.mindswap.owls.process.variable.Parameter;

/**
 * A mapping of some {@link Parameter} (defined by a service profile) to a
 * parameter in the grounded operation. Message maps can be part of
 * {@link AtomicGrounding atomic groundings}. Optionally, a
 * {@link #getTransformation() data transformation} may be specified
 *
 * @author unascribed
 * @version $Rev: 2423 $; $Author: thorsten $; $Date: 2010-03-15 09:57:50 +0200 (Mon, 15 Mar 2010) $
 */
public interface MessageMap<T> extends OWLIndividual
{
	/**
	 * @return The parameter in the service profile (to which the enclosed atomic
	 * 	grounding relates).
	 */
	public Parameter getOWLSParameter();

	/**
	 * @return The parameter in the grounded operation. (Syntax and semantics
	 * 	of this string is up to be defined by actual implementations.)
	 */
	public String getGroundingParameter();

	/**
	 * Utility method. Assumes that the {@link #getGroundingParameter()
	 * grounding parameter} string is syntactically a URI and tries to parse
	 * and return it as a {@link URI}.
	 *
	 * @return The grounding parameter converted to a URI.
	 * @throws InvalidURIException If the grounding parameter violated URI
	 * 	syntax specification.
	 */
	public URI getGroundingParameterAsURI();

	/**
	 * @return Optional data transformation, or <code>null</code> if not specified.
	 */
	public T getTransformation();

	/**
	 * @param param
	 */
	public void setOWLSParameter(Parameter param);

	/**
	 * @param param
	 */
	public void setGroundingParameter(String param);

	/**
	 * @param transform
	 */
	public void setTransformation(T transform);

	/**
	 * Special kind of message map where the data transformation is represented
	 * by a string. There is no further semantics imposed by this interface on
	 * such strings, i.e., subclasses define the semantics and hot to interpret
	 * them.
	 */
	public interface StringMessageMap extends MessageMap<String> {}
}
