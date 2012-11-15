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
import static junit.framework.Assert.fail;
import static org.junit.Assume.assumeNoException;
import impl.owls.process.constructs.SplitJoinImpl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
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
 * JUnit integration test for {@link SplitJoinImpl}. Example process is
 * loaded and executed. The process does also make use of Sequence and
 * IfThenElse constructs.
 *
 * @author unascribed
 * @version $Rev: 2389 $; $Author: thorsten $; $Date: 2010-01-14 18:54:16 +0200 (Thu, 14 Jan 2010) $
 */
public class SplitJoinTest
{
	private static final int MAX = 20; // Repeat-While current value is less than specified

	private static OWLKnowledgeBase kb;
	private static Service service;

	private static List<String> serialized;

	@BeforeClass
	public static void init()
	{
		serialized = Collections.synchronizedList(new ArrayList<String>());

		try // load service document and get Service, Process
		{
			kb = OWLFactory.createKB();
			long read = System.nanoTime();
			final InputStream inpStr = ClassLoader.getSystemResourceAsStream("owls/1.2/SplitJoin.owl");
			service = kb.readService(inpStr, URIUtils.createURI(
				ExampleURIs.SPLIT_JOIN_OWLS12, "SplitService")); // service name required because file specifies several services
			read = (System.nanoTime() - read) / 1000000;

			System.out.printf("Read took %5dms%n", read);
		}
		catch (final Exception e)
		{
			assumeNoException(e);
		}
	}

	@Test
	public void testUncached() throws Exception
	{
		kb.setReasoner(null);
		runTest(false);
		runTest(false); // do it once again, almost just for fun
	}

	@Test
	public void testCachedThenBranch() throws Exception
	{
		kb.setReasoner("Pellet"); // reasoner can infer Alice to be a Person
		runTest(true);
	}

	@Test
	public void testCachedElseBranch() throws Exception
	{
		kb.setReasoner(null); // without a reasoner Alice is not inferred to be a Person
		runTest(true);
	}

	private void runTest(final boolean useCaching) throws Exception
	{
		long exet = System.nanoTime();

		serialized.clear();

		final Process process = service.getProcess();
		final ValueMap<Input, OWLValue> inputs = new ValueMap<Input, OWLValue>();
		final ProcessExecutionEngine exec = OWLSFactory.createExecutionEngine();
		exec.getExecutionValidator().setPreconditionCheck(false);
		exec.setCaching(useCaching);

//		System.out.printf("Number of statements in KB before execution: %d%n", kb.size());

		final ValueMap<Output, OWLValue> outputs = exec.execute(process, inputs, kb);
		exet = (System.nanoTime() - exet);

		System.out.println(serialized);
		System.out.printf("Execution took (caching=%s, reasoner=%s) %dms%n",
			useCaching, (kb.getReasoner() != null), (exet / 1000000));
//		System.out.printf("Number of statements in KB after execution : %d%n", kb.size());

		assertNotNull(outputs);
		assertEquals(MAX + 2, serialized.size()); // exactly MAX invocations of inc(..) and two of log(..)
		int previous = -1, logs = 0;
		for (final String s : serialized) // do not need to synchronize as only this thread left at this point
		{
			int i;
			try
			{
				i = Integer.parseInt(s);
				assertTrue(previous < i);
				previous = i;
			}
			catch (final NumberFormatException e) // we must find exactly one non-numeric from log(..) at some index
			{
				if (logs < 2) logs++;
				else fail("log(..) invoked more than two times");
			}
		}
	}

	@Ignore // this is a target method of the Java grounding in the test process
	public static final int inc(int value)
	{
		value++;
		serialized.add(Integer.toString(value));
		return value;
	}

	@Ignore // this is a target method of the Java grounding in the test process
	public static final void log(final String s)
	{
		serialized.add(s);
	}

}
