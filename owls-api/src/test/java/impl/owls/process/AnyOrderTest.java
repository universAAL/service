/*
 * Created 01.06.2009
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
import impl.owls.grounding.JavaAtomicGroundingImpl;
import impl.owls.process.constructs.AnyOrderImpl;

import java.io.InputStream;

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
 * JUnit integration test for {@link AnyOrderImpl}. Example process is loaded
 * and executed. Also tests parts of {@link JavaAtomicGroundingImpl} because
 * the example process uses a Java atomic process grounding.
 *
 * @author unascribed
 * @version $Rev: 2269 $; $Author: thorsten $; $Date: 2009-08-19 18:21:09 +0300 (Wed, 19 Aug 2009) $
 */
public class AnyOrderTest
{
	/**
	 * Used by the AnyOrder.owl OWL-S test service. This service uses a
	 * JavaGrounding that refers to this method.
	 */
	@Ignore("Not a test case.")
	public static final void log(final String text)
	{
		System.out.println(text);
	}

	@Test
	public void testAnyOrder() throws Exception
	{
		long read = System.nanoTime();
		final OWLKnowledgeBase kb = OWLFactory.createKB();
		final InputStream inpStr = ClassLoader.getSystemResourceAsStream("owls/1.2/AnyOrder.owl");
		final Service service = kb.readService(inpStr, URIUtils.createURI(
			ExampleURIs.ANYORDER_OWLS12, "AnyOrderService")); // service name required because file specifies two services
		read = (System.nanoTime() - read) / 1000000;
		final Process process = service.getProcess();

		final ProcessExecutionEngine exec = OWLSFactory.createExecutionEngine();
		long exet = System.nanoTime();
		ValueMap<Output, OWLValue> outputs = exec.execute(process, new ValueMap<Input, OWLValue>(), kb);
		exet = (System.nanoTime() - exet) / 1000000;
		System.out.printf("Read took %5dms, Execution took %4dms%n", read, exet);

		assertNotNull(outputs);
		assertEquals(0, outputs.size());
	}

}
