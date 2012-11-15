/*
 * Created 10.09.2009
 *
 * The MIT License
 *
 * (c) 2009 Thorsten Möller - University of Basel Switzerland
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

import org.mindswap.owl.OWLValue;
import org.mindswap.owls.process.variable.Input;
import org.mindswap.owls.process.variable.Local;
import org.mindswap.owls.process.variable.Output;
import org.mindswap.owls.process.variable.ProcessVar;
import org.mindswap.query.ValueMap;

/**
 * Execution contexts provide information used by {@link ProcessExecutionEngine
 * execution engines} to drive execution of {@link org.mindswap.owls.process.Process
 * processes}.
 * <p>
 * This interface defines basic methods required in any case to maintain a current
 * state of process variables during execution (sometimes also referred to as the
 * whiteboard). Sub interfaces may freely extend it to manage additional context
 * information as used by custom execution engines. 
 *
 * @author unascribed
 * @version $Rev: 2574 $; $Author: thorsten $; $Date: 2011-02-03 14:51:12 +0200 (Thu, 03 Feb 2011) $
 */
public interface ExecutionContext
{
	/**
	 * Add the given collection of input variable bindings to this context. Input
	 * variable bindings already contained in this context will be updated as
	 * given by the value mapping (whereby the value may be <code>null</code>,
	 * thus, leaving the input unbound) and the previous value will be included in
	 * the returned mapping.
	 *
	 * @param inputs The collection of input variable bindings to add.
	 * @param returnPreviousValues Determines whether the previous value bindings
	 * 	should be returned (<code>true</code>) or not (<code>false</code>).
	 * @return <code>null</code> if <code>returnPreviousValues == true</code>.
	 * 	Map containing the previous value bound to a variable if the value was
	 * 	re-assigned as a result of this method invocation.
	 */
	public ValueMap<Input, OWLValue> addInputs(ValueMap<Input, OWLValue> inputs, boolean returnPreviousValues);
	
	/**
	 * Add the given collection of local variable bindings to this context. Local
	 * variable bindings already contained in this context will be updated as
	 * given by the value mapping (whereby the value may be <code>null</code>,
	 * thus, leaving the local unbound) and the previous value will be included in
	 * the returned mapping.
	 *
	 * @param locals The collection of local variable bindings to add.
	 * @param returnPreviousValues Determines whether the previous value bindings
	 * 	should be returned (<code>true</code>) or not (<code>false</code>).
	 * @return <code>null</code> if <code>returnPreviousValues == true</code>.
	 * 	Map containing the previous value bound to a variable if the value was
	 * 	re-assigned as a result of this method invocation.
	 */
	public ValueMap<Local, OWLValue> addLocals(ValueMap<Local, OWLValue> locals, boolean returnPreviousValues);
	
	/**
	 * Add the given collection of output variable bindings to this context. Output
	 * variable bindings already contained in this context will be updated as
	 * given by the value mapping (whereby the value may be <code>null</code>,
	 * thus, leaving the output unbound) and the previous value will be included in
	 * the returned mapping.
	 *
	 * @param outputs The collection of output variable bindings to add.
	 * @param returnPreviousValues Determines whether the previous value bindings
	 * 	should be returned (<code>true</code>) or not (<code>false</code>).
	 * @return <code>null</code> if <code>returnPreviousValues == true</code>.
	 * 	Map containing the previous value bound to a variable if the value was
	 * 	re-assigned as a result of this method invocation.
	 */
	public ValueMap<Output, OWLValue> addOutputs(ValueMap<Output, OWLValue> outputs, boolean returnPreviousValues);
	
	/**
	 * @return A filtered view of all input variable bindings in this context.
	 */
	public ValueMap<Input, OWLValue> getInputs();
	
	/**
	 * @return A filtered view of all local variable bindings in this context.
	 */
	public ValueMap<Local, OWLValue> getLocals();
	
	/**
	 * @return A filtered view of all output variable bindings in this context.
	 */
	public ValueMap<Output, OWLValue> getOutputs();
	
	/**
	 * @return The union of {@link #getInputs()}, {@link #getLocals()}, and
	 * 	{@link #getOutputs()}.
	 */
	public ValueMap<ProcessVar, OWLValue> getValues();
	
	/**
	 * @return <code>true</code> if execution can use caching in order to boost
	 * 	performance.
	 */
	public boolean isCachingPermitted();

//	/**
//    * Get a parameter that was explicitly set with setParameter.
//    *
//    * @param name of <code>Object</code> to get
//    * @return A parameter that has been set with {@link #setParameter(String, Object)}.
//    */
//   public abstract Object getParameter(String name);
//
//	/**
//    * Add a parameter supposed to be used by the execution engine.
//    *
//    * @param name The name of the parameter.
//    * @param value The value object.  This can be any valid Java object.
//    * @throws NullPointerException If value is null.
//    */
//    public abstract void setParameter(String name, Object value);

}
