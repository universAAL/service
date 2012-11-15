/*
 * Created on 23.04.2010
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
import impl.owls.process.constructs.ProduceImpl;

import java.io.InputStream;
import java.net.URI;
import java.util.Set;

import org.junit.Test;
import org.mindswap.owl.OWLClass;
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
import org.mindswap.owls.vocabulary.OWLS;
import org.mindswap.query.ValueMap;

import examples.ExampleURIs;

/**
 * JUnit integration test for {@link ProduceImpl}. Example process is
 * loaded and executed.
 *
 * @author unascribed
 * @version $Rev: 2474 $; $Author: thorsten $; $Date: 2010-06-10 12:23:39 +0300 (Thu, 10 Jun 2010) $
 */
public class ProduceTest
{
	@Test
	public void testProduce() throws Exception
	{
		long read = System.nanoTime();
		final OWLKnowledgeBase kb = OWLFactory.createKB();
		kb.setReasoner("Pellet"); // attach a reasoner so that we get concept hierarchy w.r.t. input/OutputInd
		final InputStream inpStr = ClassLoader.getSystemResourceAsStream("owls/1.2/Produce.owl");
		final Service service = kb.readService(inpStr, ExampleURIs.PRODUCE_OWLS12);
		read = (System.nanoTime() - read) / 1000000;
		final Process process = service.getProcess();
//		System.out.println(process.toRDF(false, false));

		// the service we are executing realizes the identity function (using a Produce control construct)
		final ValueMap<Input, OWLValue> inputs = new ValueMap<Input, OWLValue>();
		final OWLIndividual inputInd = kb.createIndividual(OWLS.Process.AtomicProcess.getURI(),
			URI.create("http://dbis.cs.unibas.ch/SomeAtomicProcessJustForTestingPurposes" + System.nanoTime()));
		inputs.setValue(process.getInput("input"), inputInd);

		final ProcessExecutionEngine exec = OWLSFactory.createExecutionEngine();
		exec.setCaching(false); // it doesn't matter if we use caching or not; disabled just to differ from default
		long exet = System.nanoTime();
		final ValueMap<Output, OWLValue> outputs = exec.execute(process, inputs, kb);
		exet = (System.nanoTime() - exet) / 1000000;
		System.out.printf("Read took %5dms, Execution took %4dms\n", read, exet);

		assertNotNull(outputs);
		assertEquals(1, outputs.size());
		final OWLValue outputValue = outputs.getValue(process.getOutput("output"));
		assertTrue(outputValue.isIndividual());
		final OWLIndividual outputInd = outputValue.castTo(OWLIndividual.class);
		assertEquals(inputInd, outputInd);

		// check that input and output do have the same types <-- not ultimately required but does not harm
		final Set<OWLClass> outputIndTypes = outputInd.getTypes();
		for (final OWLClass c : inputInd.getTypes())
		{
			assertTrue(outputIndTypes.remove(c));
		}
		assertTrue(outputIndTypes.isEmpty());
	}

}
