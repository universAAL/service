/*
 * Created on 19.03.2009
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
package impl.owls.process;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import impl.owls.process.constructs.IfThenElseImpl;

import java.io.InputStream;

import org.junit.Test;
import org.mindswap.owl.OWLClass;
import org.mindswap.owl.OWLDataValue;
import org.mindswap.owl.OWLFactory;
import org.mindswap.owl.OWLIndividual;
import org.mindswap.owl.OWLKnowledgeBase;
import org.mindswap.owl.OWLOntology;
import org.mindswap.owl.OWLValue;
import org.mindswap.owls.OWLSFactory;
import org.mindswap.owls.process.Process;
import org.mindswap.owls.process.execution.ProcessExecutionEngine;
import org.mindswap.owls.process.variable.Input;
import org.mindswap.owls.process.variable.Output;
import org.mindswap.owls.service.Service;
import org.mindswap.query.ValueMap;
import org.mindswap.utils.URIUtils;

import examples.ExampleURIs;

/**
 * JUnit integration test for {@link IfThenElseImpl}. Example processes are
 * loaded and executed. Indirectly tests (i) {@link impl.swrl.BuiltinAtomImpl}
 * (less-than) and (ii) {@link impl.swrl.ClassAtomImpl} because they
 * are used to express the If condition.
 *
 * @author unascribed
 * @version $Rev: 2269 $; $Author: thorsten $; $Date: 2009-08-19 18:21:09 +0300 (Wed, 19 Aug 2009) $
 */
public class IfThenElseTest
{
	@Test
	public void testIfThenElse1() throws Exception
	{
		long read = System.nanoTime();
		final OWLKnowledgeBase kb = OWLFactory.createKB();
		final InputStream inpStr = ClassLoader.getSystemResourceAsStream("owls/1.2/IfThenElse-1.owl");
		final Service service = kb.readService(inpStr, ExampleURIs.IF_THEN_ELSE1_OWLS12);
		read = (System.nanoTime() - read) / 1000000;
		final Process process = service.getProcess();
//		System.out.println(process.toRDF(false, false));

		// 9 < 7 == false --> Else branch is executed (outputNumber will be assigned to inputNumber2)
		ValueMap<Input, OWLValue> inputs = new ValueMap<Input, OWLValue>();
		Float inValue1 = Float.valueOf(9.0f);
		Float inValue2 = Float.valueOf(7.0f);
		inputs.setValue(process.getInput("inputNumber1"), kb.createDataValue(inValue1));
		inputs.setValue(process.getInput("inputNumber2"), kb.createDataValue(inValue2));

		final ProcessExecutionEngine exec = OWLSFactory.createExecutionEngine();
		long exet = System.nanoTime();
		ValueMap<Output, OWLValue> outputs = exec.execute(process, inputs, kb);
		exet = (System.nanoTime() - exet) / 1000000;
		System.out.printf("Read took %5dms, Execution took %4dms\n", read, exet);

		assertNotNull(outputs);
		assertEquals(1, outputs.size());
		OWLValue outputValue = outputs.getValue(process.getOutput("outputNumber"));
		assertTrue(outputValue.isDataValue());
		OWLDataValue outputDataValue = outputValue.castTo(OWLDataValue.class);
		assertEquals(outputDataValue.getValue(), inValue2);

		System.out.println(outputs);

		// -9 < -7 == true --> Then branch is executed (outputNumber will be assigned to inputNumber1)
		inputs = new ValueMap<Input, OWLValue>();
		inValue1 = Float.valueOf(-9.0f);
		inValue2 = Float.valueOf(-7.0f);
		inputs.setValue(process.getInput("inputNumber1"), kb.createDataValue(inValue1));
		inputs.setValue(process.getInput("inputNumber2"), kb.createDataValue(inValue2));

		outputs = exec.execute(process, inputs, kb);

		assertNotNull(outputs);
		assertEquals(1, outputs.size());
		outputValue = outputs.getValue(process.getOutput("outputNumber"));
		assertTrue(outputValue.isDataValue());
		outputDataValue = outputValue.castTo(OWLDataValue.class);
		assertEquals(outputDataValue.getValue(), inValue1);

		System.out.println(outputs);
	}

	@Test
	public void testIfThenElse2() throws Exception
	{
		long read = System.nanoTime();
		final OWLKnowledgeBase kb = OWLFactory.createKB();
		final InputStream inpStr = ClassLoader.getSystemResourceAsStream("owls/1.2/IfThenElse-2.owl");
		final Service service = kb.readService(inpStr, ExampleURIs.IF_THEN_ELSE2_OWLS12);
		read = (System.nanoTime() - read) / 1000000;
		final Process process = service.getProcess();
		final OWLOntology ont = service.getOntology();
//		System.out.println(process.toRDF(false, false));

		OWLClass A = ont.getClass(URIUtils.createURI(ExampleURIs.IF_THEN_ELSE2_OWLS12, "A"));
		OWLClass B = ont.getClass(URIUtils.createURI(ExampleURIs.IF_THEN_ELSE2_OWLS12, "B"));

		// not(inputA instanceof B) --> Else branch is executed (output will be individual assigned to inputB)
		ValueMap<Input, OWLValue> inputs = new ValueMap<Input, OWLValue>();
		OWLIndividual someA = ont.createInstance(A, URIUtils.createURI(ExampleURIs.IF_THEN_ELSE2_OWLS12, "someA"));
		OWLIndividual someB = ont.createInstance(B, URIUtils.createURI(ExampleURIs.IF_THEN_ELSE2_OWLS12, "someB"));
		inputs.setValue(process.getInput("inputA"), someA);
		inputs.setValue(process.getInput("inputB"), someB);

		// various sanity checks
		assertFalse(someA.equals(someB)); // even anon individuals of the same type are not equal
		assertFalse(someA.getType().equals(someB.getType())); // A and B are different classes, obviously
		assertFalse(someA.getType().isEquivalentTo(someB.getType())); // must return same result

		final ProcessExecutionEngine exec = OWLSFactory.createExecutionEngine();
		long exet = System.nanoTime();
		ValueMap<Output, OWLValue> outputs = exec.execute(process, inputs, kb);
		exet = (System.nanoTime() - exet) / 1000000;
		System.out.printf("Read took %5dms, Execution took %4dms\n", read, exet);

		assertNotNull(outputs);
		assertEquals(1, outputs.size());
		OWLValue outputValue = outputs.getValue(process.getOutput("outputA"));
		assertTrue(outputValue.isIndividual());
		OWLIndividual outputInd = outputValue.castTo(OWLIndividual.class);
		assertEquals(outputInd, someB);

		System.out.println(outputs);

		// (inputA instanceof B) --> Then branch is executed (output will be individual assigned to inputA)
		inputs = new ValueMap<Input, OWLValue>();
		OWLIndividual anotherB = ont.createInstance(B, URIUtils.createURI(ExampleURIs.IF_THEN_ELSE2_OWLS12, "anotherB"));
		inputs.setValue(process.getInput("inputA"), anotherB);
		inputs.setValue(process.getInput("inputB"), someB);

		// various sanity checks
		assertFalse(someB.equals(anotherB)); // even anon individuals of the same type are not equal
		assertTrue(someB.getType().equals(anotherB.getType())); // B equals B, obviously
		assertTrue(someB.getType().isEquivalentTo(someB.getType())); // must return same result

		outputs = exec.execute(process, inputs, kb);

		assertNotNull(outputs);
		assertEquals(1, outputs.size());
		outputValue = outputs.getValue(process.getOutput("outputA"));
		assertTrue(outputValue.isIndividual());
		outputInd = outputValue.castTo(OWLIndividual.class);
		assertEquals(outputInd, anotherB);

		System.out.println(outputs);
	}

}
