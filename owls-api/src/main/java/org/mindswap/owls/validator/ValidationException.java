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
package org.mindswap.owls.validator;

/**
 * Indicates a condition that caused validation of some service to fail, more
 * precisely, validation was not even started because of the error. See
 * {@link Error} for the different kinds of exceptional events.
 *
 * @author unascribed
 * @version $Rev: 2396 $; $Author: thorsten $; $Date: 2010-01-15 12:34:01 +0200 (Fri, 15 Jan 2010) $
 */
public class ValidationException extends Exception
{
	private static final long serialVersionUID = 77924528831606623L;

	private final Error mId;

	public ValidationException(final Error theId, final String theMsg)
	{
		super(theMsg);
		mId = theId;
	}

	public ValidationException(final Error theId, final String theMsg, final Throwable cause)
	{
		super(theMsg, cause);
		mId = theId;
	}

	public Error getId()
	{
		return mId;
	}

	public static final ValidationException createParameterException(final String theMsg, final Throwable cause)
	{
		return create(Error.PARAMETER, theMsg, cause);
	}

	public static final ValidationException createParseException(final String theMsg, final Throwable cause)
	{
		return create(Error.PARSE, theMsg, cause);
	}

	public static final ValidationException createIOException(final String theMsg, final Throwable cause)
	{
		return create(Error.IO, theMsg, cause);
	}

	public static final ValidationException createInconsistencyException(final String theMsg)
	{
		return create(Error.INCONSISTENCY, theMsg, null);
	}

	private static ValidationException create(final Error code, final String msg, final Throwable cause)
	{
		return (cause == null)? new ValidationException(code, msg) : new ValidationException(code, msg, cause);
	}

	public enum Error
	{
		/** Indicates an error related to a syntactical problem in the OWL-S document causing the parse process to fail. */
		PARSE,
		/** Indicates an error related to I/O problems occurred on (remote) loading of the OWL-S document. */
		IO,
		/** Indicates an error related to an invalid invocation parameter for the {@link OWLSValidator}. */
		PARAMETER,
		/** Indicates that the ontology/knowledge base containing the service is inconsistent. */
		INCONSISTENCY
	}

}
