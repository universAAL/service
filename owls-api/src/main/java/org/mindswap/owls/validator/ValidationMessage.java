// The MIT License
//
// Copyright (c) 2009 Thorsten Möller - University of Basel Switzerland
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

import java.io.Serializable;

/**
 * A message or result describing some particular (syntactical or semantical)
 * problem found by an {@link OWLSValidator} when validating some OWL-S service
 * description.
 *
 * @author unascribed
 * @version $Rev: 2396 $; $Author: thorsten $; $Date: 2010-01-15 12:34:01 +0200 (Fri, 15 Jan 2010) $
 */
public class ValidationMessage implements Serializable
{
	private static final long serialVersionUID = 772042063457599137L;

	private final int code;
	private final String message;

	public ValidationMessage(final int theCode, final String theMsg)
	{
		code = theCode;
		message = theMsg;
	}

	public int getCode()
	{
		return code;
	}

	public String getMessage()
	{
		return message;
	}

	/* @see java.lang.Object#toString() */
	@Override
	public String toString()
	{
		return getMessage();
	}

	// public Service getService() {
	// return mService;
	// }
}
