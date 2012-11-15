/*
 * Created 11.07.2009
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
package impl.owls.grounding;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import org.junit.Test;
import org.mindswap.owl.OWLDataValue;
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

/**
 * JUnit integration test for {@link JavaAtomicGroundingImpl}. Example process
 * is loaded and executed multiple times to also measure average execution time.
 *
 * @author unascribed
 * @version $Rev: 2334 $; $Author: thorsten $; $Date: 2009-09-28 13:04:37 +0200 (Mon, 28 Sep 2009) $
 */
public class JavaGroundingTest
{
	@Test
	public void testJavaGroundingCached() throws Exception
	{
		runTest(true);
	}

	@Test
	public void testJavaGroundingUncached() throws Exception
	{
		runTest(false);
	}

	private void runTest(final boolean useCaching) throws Exception
	{
		final OWLKnowledgeBase kb = OWLFactory.createKB();
		final Service service = kb.readService(
			ClassLoader.getSystemResource("owls/1.2/JGroundingTest.owl").toURI());
		final Process process = service.getProcess();

//		kb.addAlternativeLocation(
//			URI.create(ExampleURIs.ONT_LUBM), URI.create("file:/Volumes/Daten/development/lubm/univ-bench.owl"));
//		final ServiceGenerator g = new ServiceGenerator(kb);
//		g.generateAtomicServices(service.getOntology().getURI(), 4999, false, true);

		ValueMap<Input, OWLValue> inputs = new ValueMap<Input, OWLValue>();
		Integer inValue1 = Integer.valueOf(72);
		Float inValue2 = Float.valueOf(80.0f);

		Double expectedResult;

		final ProcessExecutionEngine exec = OWLSFactory.createExecutionEngine();
		exec.setCaching(useCaching);

		final Input input1 = process.getInput("FirstParam"), input2 = process.getInput("SecParam");
		final Output output = process.getOutput("OutParam");

		long totalExecTime = 0, numberOfIterations = 3000;
		for (int i = 0; i < numberOfIterations; i++)
		{
			expectedResult = pow(inValue1, inValue2);

			inputs.setValue(input1, kb.createDataValue(inValue1));
			inputs.setValue(input2, kb.createDataValue(inValue2));

			long exet = System.nanoTime();
			ValueMap<Output, OWLValue> outputs = exec.execute(process, inputs, kb);
			totalExecTime += (System.nanoTime() - exet) / 1000;

			assertNotNull(outputs);
			assertEquals(1, outputs.size());
			OWLValue outputValue = outputs.getValue(output);
			assertTrue(outputValue.isDataValue());
			OWLDataValue outputDataValue = outputValue.castTo(OWLDataValue.class);
			assertEquals(outputDataValue.getValue(), expectedResult);

			inValue2 = Float.valueOf(inValue2 - 0.01f);
		}

		System.out.printf("Average execution time for one execution %dµs, total %4d executions, %s%n",
			(totalExecTime / numberOfIterations), numberOfIterations, useCaching? "cached" : "uncached");

//		totalExecTime = 0;
//		for (int i = 0; i < numberOfIterations; i++)
//		{
//			expectedResult = pow(inValue1, inValue2);
//
//			inputs.setValue(input1, kb.createDataValue(inValue1));
//			inputs.setValue(input2, kb.createDataValue(inValue2));
//
//			long exet = System.nanoTime();
//			ValueMap<Output, OWLValue> outputs = exec.execute(process, inputs, kb);
//			totalExecTime += (System.nanoTime() - exet) / 1000;
//
//			assertNotNull(outputs);
//			assertEquals(1, outputs.size());
//			OWLValue outputValue = outputs.getValue(output);
//			assertTrue(outputValue.isDataValue());
//			OWLDataValue outputDataValue = outputValue.castTo(OWLDataValue.class);
//			assertEquals(outputDataValue.getValue(), expectedResult);
//
//			inValue2 = Float.valueOf(inValue2 - 0.01f);
//		}
//
//		System.out.printf("Average execution time for one execution %dµs, total %4d executions, %s%n",
//			(totalExecTime / numberOfIterations), numberOfIterations, useCaching? "cached" : "uncached");
	}

	/**
	 * This method will be invoked by the Java Grounding specified by the example
	 * service. Since it is not static a instance of this class is created prior
	 * to invocation, using the default no-args constructor.
	 */
	public Double pow(final int x, final Float y)
	{
		return Math.pow(x, y); // quite some implicit autoboxing and casting here :-p
	}

//	public static void main(final String[] args) throws Exception
//	{
//		JavaGroundingTest t = new JavaGroundingTest();
//		long time = System.nanoTime();
//		for (int i = 0; i < 5000; i++)
//		{
//			t.pow(72, 80.0f);
//		}
//		System.out.println((System.nanoTime() - time) / 5000);
//	}

}
