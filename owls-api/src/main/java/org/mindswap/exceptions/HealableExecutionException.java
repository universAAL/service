/*
 * Created 31.12.2008
 *
 * (c) 2008 Thorsten Möller - University of Basel Switzerland
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

import java.util.Set;

import org.mindswap.owls.process.Process;

/**
 * A special kind of execution exception used to indicate that the corresponding
 * composite process (in which this exception arise) can possibly recover from
 * this exception, thus, enabling resumption of execution. <em>Healing</em> this
 * exception requires replacing the failed atomic process, i.e., a grounded
 * atomic service whose invocation failed, (and possibly remaining parts of the
 * composite process as well) by a replacement process that yields a functional
 * semantically equivalent execution.
 *
 * @author unascribed
 * @version $Rev:$; $Author:$; $Date:$
 */
public class HealableExecutionException extends ExecutionException
{
	private static final long serialVersionUID = -6206464410262350108L;

	/**
	 * @see ExecutionException#ExecutionException()
	 */
	public HealableExecutionException()
	{
		super();
	}

	/**
	 * @see ExecutionException#ExecutionException(String)
	 */
	public HealableExecutionException(final String message)
	{
		super(message);
	}

	/**
	 * @see ExecutionException#ExecutionException(String, Process)
	 */
	public HealableExecutionException(final String message, final Process process)
	{
		super(message, process);
	}

	/**
	 * @see ExecutionException#ExecutionException(Throwable)
	 */
	public HealableExecutionException(final Throwable t)
	{
		super(t);
	}

	/**
	 * @see ExecutionException#ExecutionException(Throwable, Process)
	 */
	public HealableExecutionException(final Throwable cause, final Process process)
	{
		super(cause, process);
	}

	/**
	 * @see ExecutionException#ExecutionException(Set)
	 */
	public HealableExecutionException(final Set<Exception> causes)
	{
		super(causes);
	}

}
