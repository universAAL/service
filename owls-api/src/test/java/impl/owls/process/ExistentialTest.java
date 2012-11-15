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
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.junit.Assume.assumeNoException;
import static org.junit.Assume.assumeNotNull;
import impl.owls.process.variable.ExistentialImpl;

import java.io.InputStream;
import java.net.URI;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mindswap.exceptions.ExecutionException;
import org.mindswap.exceptions.MultipleSatisfiedPreconditionException;
import org.mindswap.exceptions.UnsatisfiedPreconditionException;
import org.mindswap.owl.OWLFactory;
import org.mindswap.owl.OWLIndividual;
import org.mindswap.owl.OWLKnowledgeBase;
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
 * JUnit integration test for {@link ExistentialImpl}. Also causes
 * {@link UnsatisfiedPreconditionException}, and
 * {@link MultipleSatisfiedPreconditionException} to be thrown, thus,
 * testing corresponding parts in the execution engine.
 * Example process is loaded and executed.
 *
 * @author unascribed
 * @version $Rev: 2478 $; $Author: thorsten $; $Date: 2010-06-10 15:49:46 +0300 (Thu, 10 Jun 2010) $
 */
public class ExistentialTest
{
	static final URI URI_C1Ind1 = URIUtils.createURI(ExampleURIs.EXISTENTIAL_OWLS12, "C1Ind1");
	static final URI URI_C1Ind2 = URIUtils.createURI(ExampleURIs.EXISTENTIAL_OWLS12, "C1Ind2");
	static final URI URI_C1Ind3 = URIUtils.createURI(ExampleURIs.EXISTENTIAL_OWLS12, "C1Ind3");
	static final URI URI_C2Ind1 = URIUtils.createURI(ExampleURIs.EXISTENTIAL_OWLS12, "C2Ind1");
	static final URI URI_C2Ind2 = URIUtils.createURI(ExampleURIs.EXISTENTIAL_OWLS12, "C2Ind2");
	static final URI URI_C2Ind3 = URIUtils.createURI(ExampleURIs.EXISTENTIAL_OWLS12, "C2Ind3");

	static OWLKnowledgeBase kb;
	static Service service;
	static Process process;

	static OWLIndividual C1Ind1, C1Ind2, C1Ind3;
	static OWLIndividual C2Ind1, C2Ind2, C2Ind3;

	@BeforeClass
	public static void init()
	{
		try
		{
			long read = System.nanoTime();
			kb = OWLFactory.createKB();
			final InputStream inpStr = ClassLoader.getSystemResourceAsStream("owls/1.2/Existential.owl");
			service = kb.readService(inpStr, ExampleURIs.EXISTENTIAL_OWLS12);
			read = (System.nanoTime() - read) / 1000000;
			System.out.printf("Reading service %s took %4dms\n", ExampleURIs.EXISTENTIAL_OWLS12, read);
			process = service.getProcess();
//			System.out.println(process.toRDF(false, false));
		}
		catch (final Exception e)
		{
			assumeNoException(e);
		}

		C1Ind1 = kb.getIndividual(URI_C1Ind1);
		C1Ind2 = kb.getIndividual(URI_C1Ind2);
		C1Ind3 = kb.getIndividual(URI_C1Ind3);
		C2Ind1 = kb.getIndividual(URI_C2Ind1);
		C2Ind2 = kb.getIndividual(URI_C2Ind2);
		C2Ind3 = kb.getIndividual(URI_C2Ind3);

		assumeNotNull(C1Ind1, C1Ind2, C1Ind3, C2Ind1, C2Ind2, C2Ind3);

		kb.setReasoner("Pellet"); // attach a reasoner as test is designed to require inferencing
		kb.prepare();
	}

	@Test
	public void testExecOK() throws Exception
	{
		final ValueMap<Input, OWLValue> inputs = new ValueMap<Input, OWLValue>();
		inputs.setValue(process.getInput("subject"), C1Ind1);

		final ProcessExecutionEngine exec = OWLSFactory.createExecutionEngine();
		exec.setCaching(false); // it doesn't matter if we use caching or not; disabled just to differ from default
		exec.getExecutionValidator().setPreconditionCheck(true);
		exec.getExecutionValidator().setAllowMultipleSatisfiedPreconditions(false);
		long exet = System.nanoTime();
		final ValueMap<Output, OWLValue> outputs = exec.execute(process, inputs, kb);
		exet = (System.nanoTime() - exet) / 1000000;
		System.out.printf("Execution took %4dms\n", exet);

		assertNotNull(outputs);
		assertEquals(1, outputs.size());
		final OWLValue outputValue = outputs.getValue(process.getOutput("object"));
		assertTrue(outputValue.isIndividual());
		final OWLIndividual outputInd = outputValue.castTo(OWLIndividual.class);
		assertEquals(C2Ind1, outputInd);
	}

	@Test
	public void testPreconditionException() throws Exception
	{
		final ValueMap<Input, OWLValue> inputs = new ValueMap<Input, OWLValue>();
		inputs.setValue(process.getInput("subject"), C1Ind2);

		final ProcessExecutionEngine exec = OWLSFactory.createExecutionEngine();
		exec.setCaching(false); // it doesn't matter if we use caching or not; disabled just to differ from default
		exec.getExecutionValidator().setPreconditionCheck(true);
		exec.getExecutionValidator().setAllowMultipleSatisfiedPreconditions(false);
		long exet = System.nanoTime();
		try
		{
			exec.execute(process, inputs, kb);
			fail(UnsatisfiedPreconditionException.class.getSimpleName() + " expected.");
		}
		catch (final ExecutionException e)
		{
			assertTrue(e instanceof UnsatisfiedPreconditionException);
			exet = (System.nanoTime() - exet) / 1000000;
			System.out.printf("Execution took %4dms\n", exet);
		}
	}

	@Test
	public void testExecOKMultipleBindingsAllowed() throws Exception
	{
		final ValueMap<Input, OWLValue> inputs = new ValueMap<Input, OWLValue>();
		inputs.setValue(process.getInput("subject"), C1Ind3);

		final ProcessExecutionEngine exec = OWLSFactory.createExecutionEngine();
		exec.setCaching(false); // it doesn't matter if we use caching or not; disabled just to differ from default
		exec.getExecutionValidator().setPreconditionCheck(true);
		exec.getExecutionValidator().setAllowMultipleSatisfiedPreconditions(true);
		long exet = System.nanoTime();
		final ValueMap<Output, OWLValue> outputs = exec.execute(process, inputs, kb);
		exet = (System.nanoTime() - exet) / 1000000;
		System.out.printf("Execution took %4dms\n", exet);

		assertNotNull(outputs);
		assertEquals(1, outputs.size());
		final OWLValue outputValue = outputs.getValue(process.getOutput("object"));
		assertTrue(outputValue.isIndividual());
		final OWLIndividual outputInd = outputValue.castTo(OWLIndividual.class);
		assertTrue(C2Ind2.equals(outputInd) || C2Ind3.equals(outputInd));
	}

	@Test
	public void testMultipleSatisfiedPreconditionException() throws Exception
	{
		final ValueMap<Input, OWLValue> inputs = new ValueMap<Input, OWLValue>();
		inputs.setValue(process.getInput("subject"), C1Ind3);

		final ProcessExecutionEngine exec = OWLSFactory.createExecutionEngine();
		exec.setCaching(false); // it doesn't matter if we use caching or not; disabled just to differ from default
		exec.getExecutionValidator().setPreconditionCheck(true);
		exec.getExecutionValidator().setAllowMultipleSatisfiedPreconditions(false);
		long exet = System.nanoTime();
		try
		{
			exec.execute(process, inputs, kb);
			fail(MultipleSatisfiedPreconditionException.class.getSimpleName() + " expected.");
		}
		catch (final ExecutionException e)
		{
			assertTrue(e instanceof MultipleSatisfiedPreconditionException);
			exet = (System.nanoTime() - exet) / 1000000;
			System.out.printf("Execution took %4dms\n", exet);
		}
	}

}
