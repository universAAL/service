/*
 * Created 10.07.2009
 *
 * (c) 2009 Thorsten Möller - University of Basel Switzerland
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
package org.mindswap.query;

import static org.junit.Assert.assertEquals;

import java.net.URI;

import org.junit.Test;
import org.mindswap.owl.OWLDataValue;
import org.mindswap.owl.OWLFactory;
import org.mindswap.owl.OWLIndividual;
import org.mindswap.owl.OWLKnowledgeBase;
import org.mindswap.owl.OWLOntology;
import org.mindswap.owl.OWLValue;
import org.mindswap.owl.vocabulary.OWL;
import org.mindswap.owls.process.execution.BaseExecutionContext;
import org.mindswap.owls.process.variable.Input;
import org.mindswap.owls.process.variable.Loc;
import org.mindswap.owls.process.variable.Output;
import org.mindswap.owls.process.variable.ProcessVar;


/**
 * JUnit test cases for {@link ValueMap} and {@link BaseExecutionContext} indirectly.
 *
 * @author unascribed
 * @version $Rev: 2269 $; $Author: thorsten $; $Date: 2009-08-19 18:21:09 +0300 (Wed, 19 Aug 2009) $
 */
public class ValueMapTest
{
	@Test
	public void testValueMap()
	{
		OWLKnowledgeBase kb = OWLFactory.createKB();
		OWLOntology ont = kb.createOntology(URI.create("http://www.example.org/ont"));
		Input input = ont.createInput(null);
		Output output = ont.createOutput(null);
		Loc loc = ont.createLoc(null);
		OWLDataValue inputValue = ont.createDataValue("input data");
		OWLDataValue outputValue = ont.createDataValue("output data");
		OWLIndividual locValue = ont.createInstance(OWL.Thing, null);
		ValueMap<ProcessVar, OWLValue> vm = new ValueMap<ProcessVar, OWLValue>();
		vm.setValue(input, inputValue);
		vm.setValue(output, outputValue);
		vm.setValue(loc, locValue);
		BaseExecutionContext ec = new BaseExecutionContext(vm);
		assertEquals(ec.getValues().size(), 3);
		assertEquals(ec.getInputs().size(), 1);
		assertEquals(ec.getOutputs().size(), 1);
		assertEquals(ec.getLocals().size(), 1);
		assertEquals(ec.getInputs().getValue(input), inputValue);
		assertEquals(ec.getOutputs().getValue(output), outputValue);
		assertEquals(ec.getLocals().getValue(loc), locValue);
	}

}
