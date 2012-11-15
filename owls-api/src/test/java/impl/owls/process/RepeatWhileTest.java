/*
 * Created 23.08.2009
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
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assume.assumeNoException;
import impl.owls.process.constructs.RepeatWhileImpl;

import java.io.InputStream;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mindswap.exceptions.ExecutionException;
import org.mindswap.owl.OWLFactory;
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
 * JUnit integration test for {@link RepeatWhileImpl}. Example process is
 * loaded and executed. The process does nothing else more than incrementing
 * a number unless a limit is reached. This is also a performance test to see
 * how fast we can iterate.
 *
 * @author unascribed
 * @version $Rev: 2334 $; $Author: thorsten $; $Date: 2009-09-28 13:04:37 +0200 (Mon, 28 Sep 2009) $
 */
public class RepeatWhileTest
{
	static OWLKnowledgeBase kb;
	static Service service;

	@BeforeClass
	public static void init()
	{
		try // load service document and get Service, Process
		{
			kb = OWLFactory.createKB();
			long read = System.nanoTime();
			final InputStream inpStr = ClassLoader.getSystemResourceAsStream("owls/1.2/RepeatWhile.owl");
			service = kb.readService(inpStr, URIUtils.createURI(
				ExampleURIs.REPEAT_WHILE_OWLS12, "RepeatWhileService")); // service name required because file specifies two services
			read = (System.nanoTime() - read) / 1000000;

			System.out.printf("Read took %5dms%n", read);
		}
		catch (Exception e)
		{
			assumeNoException(e);
		}
	}

	@Test
	public void testRepeatWhileCached() throws Exception
	{
		runTest(true);
	}

	@Test
	public void testRepeatWhileUncached() throws Exception
	{
		runTest(false);
	}

	private void runTest(final boolean useCaching) throws Exception
	{
		int targetValue = 100;
		System.out.printf("Warmup run with %d iterations%n", targetValue);
		long javaTime = testRunJava(targetValue);
		long owlsTime = testRunOWLS(targetValue, useCaching);
		System.out.printf("Java execution took  %dns%n", javaTime);
		System.out.printf("OWL-S execution took %dns, %s%n", owlsTime, useCaching? "cached" : "uncached");


		targetValue = 3000;

//		ProcessExecutionEngineImpl.exeTimeSum = 0;
//		ProcessExecutionEngineImpl.checkTimeSum = 0;

		System.out.printf("Test run with %d iterations%n", targetValue);
		javaTime = testRunJava(targetValue);
		owlsTime = testRunOWLS(targetValue, useCaching);
		System.out.printf("Java execution took  %dns%n", javaTime);
		System.out.printf("OWL-S execution took %dns, %s%n", owlsTime, useCaching? "cached" : "uncached");
		System.out.printf("Average time for one iteration %dns%n", owlsTime / targetValue);
		System.out.printf("Java is %d times faster than OWL-S %n%n", (owlsTime / javaTime));

//		System.out.printf("Total time spent to check   %d%n", ProcessExecutionEngineImpl.checkTimeSum);
//		System.out.printf("Total time spent to execute %d%n", ProcessExecutionEngineImpl.exeTimeSum);
	}

	private long testRunJava(final int iterations)
	{
		int loopVar = 0;
		long exet = System.nanoTime();
		while (loopVar < iterations)
		{
			loopVar = inc(loopVar);
		}
		return System.nanoTime() - exet;
	}

	private long testRunOWLS(final int iterations, final boolean useCaching) throws ExecutionException
	{
		final Process process = service.getProcess();

		// prepare input
		final ValueMap<Input, OWLValue> inputs = new ValueMap<Input, OWLValue>();
		inputs.setValue(process.getInput("targetValue"), kb.createDataValue(Integer.valueOf(iterations)));

		final ProcessExecutionEngine exec = OWLSFactory.createExecutionEngine();
		exec.getExecutionValidator().setPreconditionCheck(false);
		exec.setCaching(useCaching);

		System.out.printf("Number of statements in KB before execution: %d", kb.size());

		long exet = System.nanoTime();
		ValueMap<Output, OWLValue> outputs = exec.execute(process, inputs, kb);
		exet = (System.nanoTime() - exet);

		System.out.printf(", after execution: %d%n", kb.size());

		assertNotNull(outputs);
		assertEquals(1, outputs.size());

		Integer actualValue = (Integer) outputs.getDataValue("actualValue").getValue();
		assertNotNull(actualValue);
		assertTrue(actualValue.intValue() == iterations); // values must be equal

		return exet;
	}

	@Ignore // this is the target method of the Java grounding in the test process
	public static final int inc(int value)
	{
		return ++value;
	}

}
