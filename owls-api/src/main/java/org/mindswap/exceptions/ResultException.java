/*
 * Created on 18.08.2010
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
package org.mindswap.exceptions;

import org.mindswap.owls.process.Result;

/**
 * Thrown when applying the effect(s) of some {@link Result} caused the
 * backing KB to become inconsistent.
 *
 * @author unascribed
 * @version $Rev: 2536 $; $Author: thorsten $; $Date: 2010-08-18 15:56:09 +0300 (Wed, 18 Aug 2010) $
 */
public class ResultException extends ExecutionException
{
	private static final long serialVersionUID = -5767219672148315851L;

	private final transient Result result;

	/**
	 * @param message
	 * @param result
	 */
	public ResultException(final String message, final Result result)
	{
		super(message, result.getProcess());
		this.result = result;
	}

	/**
	 * @param cause
	 * @param result
	 */
	public ResultException(final Throwable cause, final Result result)
	{
		super(cause, result.getProcess());
		this.result = result;
	}

	/**
	 * @return The result whose evaluation caused an exception.
	 */
	public Result getResult()
	{
		return result;
	}

}
