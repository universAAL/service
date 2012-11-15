/*
 * Created 29.08.2008
 *
 * (c) 2008 Thorsten Möller - University of Basel Switzerland
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
package ch.unibas.on.owls.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNoException;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.junit.Test;
import org.mindswap.owl.OWLFactory;
import org.mindswap.owl.OWLIndividualList;
import org.mindswap.owl.OWLKnowledgeBase;
import org.mindswap.owls.expression.Condition;
import org.mindswap.owls.process.AtomicProcess;
import org.mindswap.owls.process.CompositeProcess;
import org.mindswap.owls.process.ControlConstruct;
import org.mindswap.owls.process.IfThenElse;
import org.mindswap.owls.process.Perform;
import org.mindswap.owls.process.Process;
import org.mindswap.owls.process.Sequence;
import org.mindswap.owls.service.Service;
import org.mindswap.utils.URIUtils;

import examples.ExampleURIs;

/**
 * Simple test that parses some composite process and asserts that its
 * control flow structure is represented in memory as defined by the
 * OWL-S document.
 * <p>
 * Note that this test depends on the existence of the exemplary OWL-S
 * process {@value ExampleURIs#BRAVO_AIR_SERVICE_OWLS12}. Furthermore, it also
 * depends on the control flow structure of this process as it was
 * defined at the time when this test was written.
 *
 * @author unascribed
 * @version $Rev: 2319 $; $Author: thorsten $; $Date: 2009-09-23 18:30:19 +0300 (Wed, 23 Sep 2009) $
 */
public class ProcessTest
{
	@Test public void testProcess()
	{
		final OWLKnowledgeBase kb = OWLFactory.createKB();

//		// required because BravoAirProcess.owl imports ontology that is no
//		// longer available (http://www.isi.edu/~pan/damltime/time-entry.owl)
//		// luckily, this ontology is irrelevant throughout the test
		kb.getReader().setIgnoreFailedImport(true);

		try
		{
			long time = System.currentTimeMillis();
			final Service s = kb.readService(ExampleURIs.BRAVO_AIR_SERVICE_OWLS12);
			System.out.printf("Reading %s took %4dms %n", ExampleURIs.BRAVO_AIR_SERVICE_OWLS12,
				(System.currentTimeMillis() - time));

			final OWLIndividualList<ControlConstruct> ccl = checkSequence(s.getProcess());
			for (int i = 0; i < ccl.size(); i++)
			{
				final ControlConstruct cc = ccl.get(i);
				assertTrue(cc instanceof Perform);
				final Perform perf = (Perform) cc;
				final Process p = perf.getProcess();
				assertNotNull(p);
				URI processURI;
				switch (i)
				{
					case 0:
						processURI = URIUtils.createURI(ExampleURIs.BRAVO_AIR_PROCESS_OWLS12, "GetDesiredFlightDetails");
						assertTrue(p instanceof AtomicProcess);
						break;
					case 1:
						processURI = URIUtils.createURI(ExampleURIs.BRAVO_AIR_PROCESS_OWLS12, "SelectAvailableFlight");
						assertTrue(p instanceof AtomicProcess);
						break;
					case 2:
						processURI = URIUtils.createURI(ExampleURIs.BRAVO_AIR_PROCESS_OWLS12, "BookFlight");
						checkBookFlightProcess(p);
						break;
					default:
						processURI = null;
						break;
				}
				assertEquals(processURI, p.getURI());
			}
		}
		catch (final IOException e)
		{
			assumeNoException(e);
		}
	}

	private OWLIndividualList<ControlConstruct> checkSequence(final Process p)
	{
		assertTrue(p instanceof CompositeProcess);
		final CompositeProcess cp = (CompositeProcess) p;

		final ControlConstruct cc = cp.getComposedOf();
		assertNotNull(cc);
		assertTrue(cc instanceof Sequence);
		final Sequence seq = (Sequence) cc;

		final List<ControlConstruct> ccs = cc.getConstructs();
		assertNotNull(ccs);

		final OWLIndividualList<ControlConstruct> ccl = seq.getConstructs();
		assertNotNull(ccl);
		return ccl;
	}

	private void checkBookFlightProcess(Process p)
	{
		final OWLIndividualList<ControlConstruct> ccl = checkSequence(p);
		for (int i = 0; i < ccl.size(); i++)
		{
			final ControlConstruct cc = ccl.get(i);
			assertTrue(cc instanceof Perform);
			final Perform perf = (Perform) cc;
			p = perf.getProcess();
			assertNotNull(p);
			URI processURI;
			switch (i)
			{
				case 0:
					processURI = URIUtils.createURI(ExampleURIs.BRAVO_AIR_PROCESS_OWLS12, "LogIn");
					assertTrue(p instanceof AtomicProcess);
					break;
				case 1:
					processURI = URIUtils.createURI(ExampleURIs.BRAVO_AIR_PROCESS_OWLS12, "CompleteReservation");
					checkCompleteReservation(p);
					break;
				default:
					processURI = null;
					break;
			}
			assertEquals(processURI, p.getURI());
		}

	}

	private void checkCompleteReservation(Process p)
	{

		assertTrue(p instanceof CompositeProcess);
		final CompositeProcess cp = (CompositeProcess) p;

		ControlConstruct cc = cp.getComposedOf();
		assertNotNull(cc);
		assertTrue(cc instanceof IfThenElse);
		final IfThenElse ite = (IfThenElse) cc;

		final Condition<?> c = ite.getCondition();
		assertNotNull(c);
		p.getKB().setReasoner("Pellet");
		assertFalse(c.isTrue(null)); // false because evaluated individuals are missing
		p.getKB().setReasoner(null);

		System.out.printf("Number of statements in KB %5d%n", p.getKB().size());

		cc = ite.getThen();
		assertTrue(cc instanceof Perform);
		p = ((Perform) cc).getProcess();
		assertTrue(p instanceof AtomicProcess);
		assertEquals(p.getURI(), URIUtils.createURI(ExampleURIs.BRAVO_AIR_PROCESS_OWLS12, "ConfirmReservation"));

		cc = ite.getElse();
		assertNull(cc);

	}

}
