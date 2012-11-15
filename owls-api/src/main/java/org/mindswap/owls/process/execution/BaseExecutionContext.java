/*
 * Created on 26.04.2008
 *
 * The MIT License
 *
 * (c) 2008 Thorsten Möller - University of Basel Switzerland
 *
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
package org.mindswap.owls.process.execution;

import java.util.Map.Entry;

import org.mindswap.owl.OWLValue;
import org.mindswap.owls.process.ControlConstruct;
import org.mindswap.owls.process.variable.Input;
import org.mindswap.owls.process.variable.Local;
import org.mindswap.owls.process.variable.Output;
import org.mindswap.owls.process.variable.ProcessVar;
import org.mindswap.query.ValueMap;

/**
 * This class stores the (current) values of process variables in the scope
 * of a process instance that is currently being executed. It is passed to
 * enclosed {@link ControlConstruct control constructs} in the course of
 * execution. Another synonymous name for this class could be <em>process
 * whiteboard</em>.
 * <p>
 * Instances of this class are immutable after they have been created, but its
 * property {@link #getValues()} remains modifiable!
 *
 * @author unascribed
 * @version $Rev: 2574 $; $Author: thorsten $; $Date: 2011-02-03 14:51:12 +0200 (Thu, 03 Feb 2011) $
 */
public class BaseExecutionContext implements ExecutionContext
{
	private final boolean cachingPermitted;
	private final ValueMap<ProcessVar, OWLValue> values;

	/**
	 * Creates a new context initially having no {@link #getValues() values}.
	 * Caching is initially permitted, i.e., {@link #isCachingPermitted()}
	 * returns <code>true</code>.
	 */
	public BaseExecutionContext()
	{
		this(new ValueMap<ProcessVar, OWLValue>(), true);
	}

	/**
	 * Creates a new context initially having no {@link #getValues() values}.
	 * Caching will be set according to the parameter.
	 *
	 * @param cachingPermitted Whether the execution engine may use caching in
	 * 	order to boost performance. The details when caching can be used and
	 * 	what is actually cached are up to be defined by process execution
	 * 	engine implementations.
	 */
	public BaseExecutionContext(final boolean cachingPermitted)
	{
		this(new ValueMap<ProcessVar, OWLValue>(), cachingPermitted);
	}

	/**
	 * Creates a new context initially containing the given process variable
	 * mappings. Caching is initially permitted, i.e., {@link #isCachingPermitted()}
	 * returns <code>true</code>.
	 *
	 * @param values A container of initial process variable mappings;
	 * 	usually inputs supposed to be	consumed (outputs produced, and locals).
	 * @throws IllegalArgumentException In case <code>values</code> is <code>null</code>.
	 */
	public BaseExecutionContext(final ValueMap<ProcessVar, OWLValue> values)
	{
		this(values, true);
	}

	/**
	 * Creates a new context initially containing the given process variable
	 * mappings. Caching will be set according to the second parameter.
	 *
	 * @param values A container of initial process variable mappings;
	 * 	usually inputs supposed to be	consumed (outputs produced, and locals).
	 * @param cachingPermitted Whether the execution engine may use caching in
	 * 	order to boost performance. The details when caching can be used and
	 * 	what is actually cached are up to be defined by process execution
	 * 	engine implementations.
	 * @throws IllegalArgumentException In case <code>values</code> is <code>null</code>.
	 */
	public BaseExecutionContext(final ValueMap<ProcessVar, OWLValue> values, final boolean cachingPermitted)
	{
		if (values == null) throw new IllegalArgumentException("Process variable map was null.");
		this.cachingPermitted = cachingPermitted;
		this.values = values;
	}

	/* @see org.mindswap.owls.process.execution.ExecutionContext#addInputs(org.mindswap.query.ValueMap, boolean) */
	public ValueMap<Input, OWLValue> addInputs(final ValueMap<Input, OWLValue> inputs, final boolean returnPreviousValues)
	{
		return addValues(inputs, returnPreviousValues);
	}

	/* @see org.mindswap.owls.process.execution.ExecutionContext#addLocals(org.mindswap.query.ValueMap, boolean) */
	public ValueMap<Local, OWLValue> addLocals(final ValueMap<Local, OWLValue> locals, final boolean returnPreviousValues)
	{
		return addValues(locals, returnPreviousValues);
	}

	/* @see org.mindswap.owls.process.execution.ExecutionContext#addOutputs(org.mindswap.query.ValueMap, boolean) */
	public ValueMap<Output, OWLValue> addOutputs(final ValueMap<Output, OWLValue> inputs, final boolean returnPreviousValues)
	{
		return addValues(inputs, returnPreviousValues);
	}

	/* @see org.mindswap.owls.process.execution.ExecutionContext#getInputs() */
	public ValueMap<Input, OWLValue> getInputs()
	{
		return getValues(Input.class);
	}

	/* @see org.mindswap.owls.process.execution.ExecutionContext#getLocals() */
	public ValueMap<Local, OWLValue> getLocals()
	{
		return getValues(Local.class);
	}

	/* @see org.mindswap.owls.process.execution.ExecutionContext#getOutputs() */
	public ValueMap<Output, OWLValue> getOutputs()
	{
		return getValues(Output.class);
	}

	/* @see org.mindswap.owls.process.execution.ExecutionContext#getValues() */
	public ValueMap<ProcessVar, OWLValue> getValues()
	{
		return values;
	}

	/* @see org.mindswap.owls.process.execution.ExecutionContext#isCachingPermitted() */
	public boolean isCachingPermitted()
	{
		return cachingPermitted;
	}

	private <V extends ProcessVar> ValueMap<V, OWLValue> addValues(
		final ValueMap<V, OWLValue> valueMappings, final boolean returnPreviousValues)
	{
		if (returnPreviousValues)
		{
			final ValueMap<V, OWLValue> previousMappings = new ValueMap<V, OWLValue>();
			for (final Entry<V, OWLValue> entry : valueMappings)
			{
				previousMappings.setValue(entry.getKey(), values.setValue(entry.getKey(), entry.getValue()));
			}
			return previousMappings;
		}

		for (final Entry<V, OWLValue> entry : valueMappings)
		{
			values.setValue(entry.getKey(), entry.getValue());
		}
		return null;
	}

	private <V extends ProcessVar> ValueMap<V, OWLValue> getValues(final Class<V> varType)
	{
		final ValueMap<V, OWLValue> result = new ValueMap<V, OWLValue>();
		ProcessVar p;
		for (final Entry<ProcessVar, OWLValue> entry : values)
		{
			p = entry.getKey();
			if (varType.isInstance(p)) result.setValue(varType.cast(p), entry.getValue());
		}
		return result;
	}
}
