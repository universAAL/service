/*
 * Created on 04.02.2011
 * 
 * (c) 2011 Thorsten Möller - University of Basel Switzerland
 *
 * This file is part of OSIRIS Next.
 * 
 * OSIRIS Next is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * OSIRIS Next is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with OSIRIS Next.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mindswap.common;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * A simple Future implementation which is completed as of instantiation
 * because the result or an exception is injected at instantiation time.
 * 
 * @author unascribed
 * @version $Rev: 2579 $; $Author: thorsten $; $Date: 2011-02-04 19:11:24 +0200 (Fri, 04 Feb 2011) $
 */
public abstract class CompletedFuture<V> implements Future<V>
{
	/**
	 * Factory method for convenience.
	 */
	public static final <V> Success<V> createSuccessCompletionFuture(final V result)
	{
		return new Success<V>(result);
	}
	
	/**
	 * Factory method for convenience.
	 */
	public static final <V> Exception<V> createExceptionCompletionFuture(final Throwable cause)
	{
		return new Exception<V>(cause);
	}
	
	/* @see java.util.concurrent.Future#cancel(boolean) */
	public final boolean cancel(boolean mayInterruptIfRunning)
	{
		return false; // false as per method contract for already completed Futures
	}

	/* @see java.util.concurrent.Future#isCancelled() */
	public final boolean isCancelled()
	{
		return false;
	}

	/* @see java.util.concurrent.Future#isDone() */
	public final boolean isDone()
	{
		return true;
	}
	
	/**
	 * To be used for successfull completion.
	 */
	public static final class Success<V> extends CompletedFuture<V>
	{
		private final V result;
		
		/**
		 * @param result The result object.
		 */
		public Success(final V result)
		{
			this.result = result;
		}

		/* @see java.util.concurrent.Future#get() */
		public V get()
		{
			return result;
		}

		/* @see java.util.concurrent.Future#get(long, java.util.concurrent.TimeUnit) */
		public V get(long timeout, TimeUnit unit)
		{
			return result;
		}
	}
	
	/**
	 * To be used for unsuccessfull completion.
	 */
	public static final class Exception<V> extends CompletedFuture<V>
	{
		private final ExecutionException ee;
		
		/**
		 * @param cause The error causing exception.
		 */
		public Exception(final Throwable cause)
		{
			this.ee = new ExecutionException(cause);
		}

		/* @see java.util.concurrent.Future#get() */
		public V get() throws ExecutionException
		{
			throw ee;
		}

		/* @see java.util.concurrent.Future#get(long, java.util.concurrent.TimeUnit) */
		public V get(long timeout, TimeUnit unit) throws ExecutionException
		{
			throw ee;
		}
	}

}
