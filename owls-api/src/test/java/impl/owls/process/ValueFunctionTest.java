/*
 * Created on 10.06.2010
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
package impl.owls.process;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assume.assumeNoException;
import static org.junit.Assume.assumeNotNull;

import java.io.InputStream;
import java.net.URI;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mindswap.owl.OWLFactory;
import org.mindswap.owl.OWLIndividual;
import org.mindswap.owl.OWLKnowledgeBase;
import org.mindswap.owl.OWLObjectProperty;
import org.mindswap.owl.OWLValue;
import org.mindswap.owls.OWLSFactory;
import org.mindswap.owls.process.Process;
import org.mindswap.owls.process.execution.ProcessExecutionEngine;
import org.mindswap.owls.process.variable.Input;
import org.mindswap.owls.process.variable.Output;
import org.mindswap.owls.process.variable.ValueFunction;
import org.mindswap.owls.service.Service;
import org.mindswap.owls.vocabulary.OWLS;
import org.mindswap.query.ValueMap;
import org.mindswap.utils.URIUtils;

import examples.ExampleURIs;

/**
 * JUnit integration test for SPARQL {@link ValueFunction}.
 * Example process is loaded and executed.
 *
 * @author unascribed
 * @version $Rev: 2481 $; $Author: thorsten $; $Date: 2010-06-10 17:38:28 +0300 (Thu, 10 Jun 2010) $
 */
public class ValueFunctionTest
{
	static final URI URI_C1Ind1 = URIUtils.createURI(ExampleURIs.VALUEFUNCTION_OWLS12, "C1Ind1");
	static final URI URI_C2Ind1 = URIUtils.createURI(ExampleURIs.VALUEFUNCTION_OWLS12, "C2Ind1");
	static final URI URI_C2Ind2 = URIUtils.createURI(ExampleURIs.VALUEFUNCTION_OWLS12, "C2Ind2");
	static final URI URI_op1    = URIUtils.createURI(ExampleURIs.VALUEFUNCTION_OWLS12, "op1");
	static final URI URI_op1sub = URIUtils.createURI(ExampleURIs.VALUEFUNCTION_OWLS12, "op1sub");
	static final URI URI_op2    = URIUtils.createURI(ExampleURIs.VALUEFUNCTION_OWLS12, "op2");

	static OWLKnowledgeBase kb;
	static Service service;
	static Process process;

	static OWLObjectProperty PROP_op1, PROP_op2;
	static OWLIndividual C1Ind1;
	static OWLIndividual C2Ind1, C2Ind2;

	@BeforeClass
	public static void init()
	{
		try
		{
			long read = System.nanoTime();
			kb = OWLFactory.createKB();
			final InputStream inpStr = ClassLoader.getSystemResourceAsStream("owls/1.2/ValueFunction.owl");
			service = kb.readService(inpStr, ExampleURIs.VALUEFUNCTION_OWLS12);
			read = (System.nanoTime() - read) / 1000000;
			System.out.printf("Reading service %s took %4dms\n", ExampleURIs.VALUEFUNCTION_OWLS12, read);
			process = service.getProcess();
//			System.out.println(process.toRDF(false, false));
		}
		catch (final Exception e)
		{
			assumeNoException(e);
		}

		C1Ind1 = kb.getIndividual(URI_C1Ind1);
		C2Ind1 = kb.getIndividual(URI_C2Ind1);
		C2Ind2 = kb.getIndividual(URI_C2Ind2);
		PROP_op1 = kb.getObjectProperty(URI_op1);
		PROP_op2 = kb.getObjectProperty(URI_op2);

		assumeNotNull(C1Ind1, C2Ind1, C2Ind2, PROP_op1, PROP_op2);

		kb.setReasoner("Pellet"); // attach a reasoner as test is designed to require inferencing
		kb.prepare();
	}

	@Test
	public void testExecOK() throws Exception
	{
		ValueMap<Input, OWLValue> inputs = new ValueMap<Input, OWLValue>();
		inputs.setValue(process.getInput("subject"), C1Ind1);
		inputs.setValue(process.getInput("predicate"), PROP_op1.castTo(OWLIndividual.class));

		final ProcessExecutionEngine exec = OWLSFactory.createExecutionEngine();
		long exet = System.nanoTime();
		ValueMap<Output, OWLValue> outputs = exec.execute(process, inputs, kb);
		exet = (System.nanoTime() - exet) / 1000000;
		System.out.printf("Execution took %4dms\n", exet);

		assertNotNull(outputs);
		assertEquals(1, outputs.size());
		OWLValue outputValue = outputs.getValue(process.getOutput("object"));
		assertTrue(outputValue.isIndividual());
		OWLIndividual outputInd = outputValue.castTo(OWLIndividual.class);
		assertEquals(C2Ind1, outputInd);


		// second round using another property
		inputs = new ValueMap<Input, OWLValue>();
		inputs.setValue(process.getInput("subject"), C1Ind1);
		inputs.setValue(process.getInput("predicate"), PROP_op2.castTo(OWLIndividual.class));

		exet = System.nanoTime();
		outputs = exec.execute(process, inputs, kb);
		exet = (System.nanoTime() - exet) / 1000000;
		System.out.printf("Execution took %4dms\n", exet);

		assertNotNull(outputs);
		assertEquals(1, outputs.size());
		outputValue = outputs.getValue(process.getOutput("object"));
		assertTrue(outputValue.isIndividual());
		outputInd = outputValue.castTo(OWLIndividual.class);
		assertTrue(outputInd.isType(OWLS.Service.Service));
	}

	@Test
	public void testMissingTripleObject() throws Exception
	{
		final ValueMap<Input, OWLValue> inputs = new ValueMap<Input, OWLValue>();
		inputs.setValue(process.getInput("subject"), C2Ind1);
		inputs.setValue(process.getInput("predicate"), PROP_op2.castTo(OWLIndividual.class));

		final ProcessExecutionEngine exec = OWLSFactory.createExecutionEngine();
		long exet = System.nanoTime();
		final ValueMap<Output, OWLValue> outputs = exec.execute(process, inputs, kb);
		exet = (System.nanoTime() - exet) / 1000000;
		System.out.printf("Execution took %4dms\n", exet);

		assertNotNull(outputs);
		assertEquals(1, outputs.size());
		final OWLValue outputValue = outputs.getValue(process.getOutput("object"));
		assertNull(outputValue);
	}

	@Test
	public void testMissingTriple() throws Exception
	{
		final ValueMap<Input, OWLValue> inputs = new ValueMap<Input, OWLValue>();
		inputs.setValue(process.getInput("subject"), C2Ind2);
		inputs.setValue(process.getInput("predicate"), PROP_op1.castTo(OWLIndividual.class));

		final ProcessExecutionEngine exec = OWLSFactory.createExecutionEngine();
		long exet = System.nanoTime();
		final ValueMap<Output, OWLValue> outputs = exec.execute(process, inputs, kb);
		exet = (System.nanoTime() - exet) / 1000000;
		System.out.printf("Execution took %4dms\n", exet);

		assertNotNull(outputs);
		assertEquals(1, outputs.size());
		final OWLValue outputValue = outputs.getValue(process.getOutput("object"));
		assertNull(outputValue);
	}
}
