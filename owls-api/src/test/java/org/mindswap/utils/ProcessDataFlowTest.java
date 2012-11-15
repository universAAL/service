/*
 * Created 29.10.2009
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
package org.mindswap.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNoException;

import java.io.InputStream;
import java.net.URI;
import java.util.StringTokenizer;

import org.junit.Test;
import org.mindswap.owl.OWLClass;
import org.mindswap.owl.OWLFactory;
import org.mindswap.owl.OWLIndividualList;
import org.mindswap.owl.OWLKnowledgeBase;
import org.mindswap.owl.OWLOntology;
import org.mindswap.owl.OWLType;
import org.mindswap.owl.vocabulary.RDFS;
import org.mindswap.owls.process.AtomicProcess;
import org.mindswap.owls.process.CompositeProcess;
import org.mindswap.owls.process.Perform;
import org.mindswap.owls.process.Process;
import org.mindswap.owls.process.Result;
import org.mindswap.owls.process.Sequence;
import org.mindswap.owls.process.variable.Input;
import org.mindswap.owls.process.variable.Loc;
import org.mindswap.owls.process.variable.Output;
import org.mindswap.owls.process.variable.Parameter;
import org.mindswap.owls.profile.Profile;
import org.mindswap.owls.service.Service;
import org.mindswap.owls.vocabulary.OWLS;

import examples.ExampleURIs;

/**
 * Note that this test depends on the <tt>RepeatUntil.owl</tt> OWL-S service
 * description. If its data flow specification is changed this test is likely
 * to fail then.
 *
 * @author unascribed
 * @version $Rev: 2350 $; $Author: thorsten $; $Date: 2009-11-18 17:44:31 +0200 (Wed, 18 Nov 2009) $
 */
public class ProcessDataFlowTest
{
	private static final URI baseURI = URI.create("http://example.org");

	private OWLClass[] classes;

	private Input p0i1, p0i2, p0i3;
	private Loc p0l1;
	private Output p0o1, p0o2, p0o3, p0o4;

	private Input p1i1;
	private Output p1o1, p1o2;

	private Input p2i1, p2i2;
	private Output p2o1;

	private Input p3i1;
	private Output p3o1;

	private Input p4i1, p4i2, p4i3;

	private Input p5i1, p5i2, p5i3;
	private Output p5o1;

	/**
	 * Test method for {@link org.mindswap.utils.ProcessDataFlow#toString()}.
	 */
	@Test
	public void testToString()
	{
		OWLKnowledgeBase kb = OWLFactory.createKB();
		Service service = null;
		try
		{
			final InputStream inpStr = ClassLoader.getSystemResourceAsStream("owls/1.2/RepeatUntil.owl");
			service = kb.readService(inpStr, URIUtils.createURI(
				ExampleURIs.REPEAT_UNTIL_OWLS12, "RepeatUntilService")); // service name required because file specifies two services
		}
		catch (Exception e)
		{
			assumeNoException(e);
		}
		Process p = service.getProcess();
		String s = ProcessDataFlow.index(p.getOntology(), p).toString();
		assertNotNull(s);

		// Not the best test. At least, it does verify that expected number of
		// data flow bindings were indexed and that (source, sink) pairs are as defined.
		StringTokenizer st = new StringTokenizer(s, Utils.LINE_SEPARATOR);
		int tokens = 0, idx = -1;
		while (st.hasMoreTokens())
		{
			String t = st.nextToken();
			if ((idx = t.indexOf("currentWeight")) > -1 && (idx = t.indexOf("personWeight", idx)) > -1) tokens++;
			if ((idx = t.indexOf("newWeight")) > -1 && (idx = t.indexOf("currentWeight", idx)) > -1) tokens++;
			if ((idx = t.indexOf("person")) > -1 && (idx = t.indexOf("dietPerson", idx)) > -1) tokens++;
		}
		assertEquals(3, tokens);
	}

	/**
	 * Test method for {@link org.mindswap.utils.ProcessDataFlow#index(OWLOntology, Process)}
	 * using a atomic process.
	 */
	@Test
	public void testIndexAtomicProcess()
	{
		OWLKnowledgeBase kb = OWLFactory.createKB();
		kb.setReasoner("Transitive"); // sufficient as we only need reflexive-transitive subClassOf closure
		OWLOntology ont = kb.createOntology(baseURI);
		createClasses(ont);
		AtomicProcess aProcess = createAtomicProcess(ont);
		ProcessDataFlow pdf = ProcessDataFlow.index(null, aProcess);
		// atomic process do not have a data flow --> expected to be empty
		String s = pdf.toString();
		assertEquals(0, s.length());

		OWLOntology pdfAsOnt = pdf.asOntology();
		assertEquals(0, pdfAsOnt.size());

		// most specific sink type always equal to the parameter type itself
		assertEquals(p0i1.getParamType(), pdf.getMostSpecificSinkType(p0i1));
		assertEquals(p0i2.getParamType(), pdf.getMostSpecificSinkType(p0i2));
		assertEquals(p0i3.getParamType(), pdf.getMostSpecificSinkType(p0i3));
	}

	/**
	 * Test method for {@link org.mindswap.utils.ProcessDataFlow#index(OWLOntology, Process)}
	 * using a composite process.
	 */
	@Test
	public void testIndexCompositeProcess()
	{
		OWLKnowledgeBase kb = OWLFactory.createKB();
		kb.setReasoner("Transitive"); // sufficient as we only need reflexive-transitive subClassOf closure
		OWLOntology ont = kb.createOntology(baseURI);
		createClasses(ont);
		CompositeProcess cProcess = createCompositeProcess(ont);
		ProcessDataFlow pdf = ProcessDataFlow.index(null, cProcess);
		System.out.println(pdf.toString());

		OWLIndividualList<Parameter> sinks = pdf.getSinkParameters(p0i1);
		assertNotNull(sinks);
		assertTrue(sinks.size() == 1);
		assertEquals(p0o1, sinks.get(0));
		OWLType msst = pdf.getMostSpecificSinkType(p0i1);
		assertNotNull(msst);
		assertEquals(classes[0], msst);

		sinks = pdf.getSinkParameters(p0i2);
		assertNotNull(sinks);
		assertTrue(sinks.size() == 1);
		assertEquals(p1i1, sinks.get(0));
		msst = pdf.getMostSpecificSinkType(p0i2);
		assertNotNull(msst);
		assertEquals(classes[1], msst);

		sinks = pdf.getSinkParameters(p0i3);
		assertNotNull(sinks);
		assertTrue(sinks.size() == 3);
		assertTrue(sinks.contains(p3i1));
		assertTrue(sinks.contains(p4i1));
		assertTrue(sinks.contains(p5i3));
		msst = pdf.getMostSpecificSinkType(p0i3);
		assertNotNull(msst);
		assertTrue(classes[4].equals(msst) || classes[6].equals(msst)); // incompatible consumers

		sinks = pdf.getSinkParameters(p0l1);
		assertNotNull(sinks);
		assertTrue(sinks.size() == 1);
		assertEquals(p0o4, sinks.get(0));
		msst = pdf.getMostSpecificSinkType(p0l1);
		assertNotNull(msst);
		assertEquals(classes[3], msst);

		sinks = pdf.getSinkParameters(p1o1);
		assertNotNull(sinks);
		assertTrue(sinks.size() == 2);
		assertTrue(sinks.contains(p0o2));
		assertTrue(sinks.contains(p2i2));
		msst = pdf.getMostSpecificSinkType(p1o1);
		assertNotNull(msst);
		assertTrue(classes[0].equals(msst));

		sinks = pdf.getSinkParameters(p1o2);
		assertNotNull(sinks);
		assertTrue(sinks.size() == 1);
		assertEquals(p2i1, sinks.get(0));
		msst = pdf.getMostSpecificSinkType(p1o2);
		assertNotNull(msst);
		assertEquals(classes[3], msst);

		sinks = pdf.getSinkParameters(p2o1);
		assertNotNull(sinks);
		assertTrue(sinks.size() == 2);
		assertTrue(sinks.contains(p4i2));
		assertTrue(sinks.contains(p5i1));
		msst = pdf.getMostSpecificSinkType(p2o1);
		assertNotNull(msst);
		assertTrue(classes[4].equals(msst));

		sinks = pdf.getSinkParameters(p3o1);
		assertNotNull(sinks);
		assertTrue(sinks.size() == 2);
		assertTrue(sinks.contains(p4i3));
		assertTrue(sinks.contains(p5i2));
		msst = pdf.getMostSpecificSinkType(p3o1);
		assertNotNull(msst);
		assertTrue(classes[6].equals(msst));

		sinks = pdf.getSinkParameters(p5o1);
		assertNotNull(sinks);
		assertTrue(sinks.size() == 1);
		assertEquals(p0o3, sinks.get(0));
		msst = pdf.getMostSpecificSinkType(p5o1);
		assertNotNull(msst);
		assertEquals(classes[5], msst);
	}

	private AtomicProcess createAtomicProcess(final OWLOntology ont)
	{
		// profile and service are actually not required for the test, but it should not harm, of course
		Profile aProfile = ont.createProfile(URIUtils.createURI(baseURI, "profile0"));
		Service aService = ont.createService(URIUtils.createURI(baseURI, "service0"));
		AtomicProcess aProcess = ont.createAtomicProcess(URIUtils.createURI(baseURI, "ap0"));
		aService.addProfile(aProfile);
		aService.setProcess(aProcess);

		// create Inputs, Outputs
		p0i1 = aProcess.createInput(URIUtils.createURI(baseURI, "p0.i1"), classes[0]);
		p0i2 = aProcess.createInput(URIUtils.createURI(baseURI, "p0.i2"), classes[1]);
		p0i3 = aProcess.createInput(URIUtils.createURI(baseURI, "p0.i3"), classes[2]);
		p0o1 = aProcess.createOutput(URIUtils.createURI(baseURI, "p0.o1"), classes[0]);
		p0o2 = aProcess.createOutput(URIUtils.createURI(baseURI, "p0.o2"), classes[0]);
		p0o3 = aProcess.createOutput(URIUtils.createURI(baseURI, "p0.o3"), classes[5]);
		aProfile.addInput(p0i1);
		aProfile.addInput(p0i2);
		aProfile.addInput(p0i3);
		aProfile.addOutput(p0o1);
		aProfile.addOutput(p0o2);
		aProfile.addOutput(p0o3);

		return aProcess;
	}

	private void createClasses(final OWLOntology ont)
	{
		classes = new OWLClass[7];
		for (int i = 0; i < classes.length; i++)
		{
			classes[i] = ont.createClass(URIUtils.createURI(baseURI, "C" + i));
		}

		// add some subclass axioms: C3 subclassOf C1; C4, C5 subclassOf C2; C6 subclassOf C5
		classes[3].addProperty(RDFS.subClassOf, classes[1]);
		classes[4].addProperty(RDFS.subClassOf, classes[2]);
		classes[5].addProperty(RDFS.subClassOf, classes[2]);
		classes[6].addProperty(RDFS.subClassOf, classes[5]);
	}

	private CompositeProcess createCompositeProcess(final OWLOntology ont)
	{
		CompositeProcess cProcess = ont.createCompositeProcess(URIUtils.createURI(baseURI, "p0"));
		Sequence sequence = ont.createSequence(null);
		cProcess.setComposedOf(sequence);

		// create five atomic processes in the sequence each having some inputs
		// and/or outputs and wire them to create a data flow (by means of bindings)

		// create Inputs, Outputs, and Loc
		p0i1 = cProcess.createInput(URIUtils.createURI(baseURI, "p0.i1"), classes[0]);
		p0i2 = cProcess.createInput(URIUtils.createURI(baseURI, "p0.i2"), classes[1]);
		p0i3 = cProcess.createInput(URIUtils.createURI(baseURI, "p0.i3"), classes[2]);
		p0l1 = cProcess.createLoc(URIUtils.createURI(baseURI, "p0.l1"), classes[1]);
		p0o1 = cProcess.createOutput(URIUtils.createURI(baseURI, "p0.o1"), classes[0]);
		p0o2 = cProcess.createOutput(URIUtils.createURI(baseURI, "p0.o2"), classes[0]);
		p0o3 = cProcess.createOutput(URIUtils.createURI(baseURI, "p0.o3"), classes[5]);
		p0o4 = cProcess.createOutput(URIUtils.createURI(baseURI, "p0.o4"), classes[3]);

		// first element in sequence
		AtomicProcess ap = ont.createAtomicProcess(URIUtils.createURI(baseURI, "p1"));
		p1i1 = ap.createInput(URIUtils.createURI(baseURI, "p1.i1"), classes[1]);
		p1o1 = ap.createOutput(URIUtils.createURI(baseURI, "p1.o1"), classes[0]);
		p1o2 = ap.createOutput(URIUtils.createURI(baseURI, "p1.o2"), classes[1]);

		Perform perf1 = ont.createPerform(URIUtils.createURI(baseURI, "perf1"));
		perf1.addBinding(p1i1, OWLS.Process.ThisPerform, p0i2);
		perf1.setProcess(ap);
		sequence.addComponent(perf1);

		// second element in sequence
		ap = ont.createAtomicProcess(URIUtils.createURI(baseURI, "p2"));
		p2i1 = ap.createInput(URIUtils.createURI(baseURI, "p2.i1"), classes[3]);
		p2i2 = ap.createInput(URIUtils.createURI(baseURI, "p2.i2"), classes[0]);
		p2o1 = ap.createOutput(URIUtils.createURI(baseURI, "p2.o1"), classes[4]);

		Perform perf2 = ont.createPerform(URIUtils.createURI(baseURI, "perf2"));
		perf2.addBinding(p2i1, perf1, p1o2);
		perf2.addBinding(p2i2, perf1, p1o1);
		perf2.setProcess(ap);
		sequence.addComponent(perf2);

		// third element in sequence
		ap = ont.createAtomicProcess(URIUtils.createURI(baseURI, "p3"));
		p3i1 = ap.createInput(URIUtils.createURI(baseURI, "p3.i1"), classes[4]);
		p3o1 = ap.createOutput(URIUtils.createURI(baseURI, "p3.o1"), classes[5]);

		Perform perf3 = ont.createPerform(URIUtils.createURI(baseURI, "perf3"));
		perf3.addBinding(p3i1, OWLS.Process.ThisPerform, p0i3);
		perf3.setProcess(ap);
		sequence.addComponent(perf3);

		// fourth element in sequence
		ap = ont.createAtomicProcess(URIUtils.createURI(baseURI, "p4"));
		p4i1 = ap.createInput(URIUtils.createURI(baseURI, "p4.i1"), classes[5]);
		p4i2 = ap.createInput(URIUtils.createURI(baseURI, "p4.i2"), classes[2]);
		p4i3 = ap.createInput(URIUtils.createURI(baseURI, "p4.i3"), classes[2]);

		Perform perf4 = ont.createPerform(URIUtils.createURI(baseURI, "perf4"));
		perf4.addBinding(p4i1, OWLS.Process.ThisPerform, p0i3);
		perf4.addBinding(p4i2, perf2, p2o1);
		perf4.addBinding(p4i3, perf3, p3o1);
		perf4.setProcess(ap);
		sequence.addComponent(perf4);

		// fifth element in sequence
		ap = ont.createAtomicProcess(URIUtils.createURI(baseURI, "p5"));
		p5i1 = ap.createInput(URIUtils.createURI(baseURI, "p5.i1"), classes[4]);
		p5i2 = ap.createInput(URIUtils.createURI(baseURI, "p5.i2"), classes[6]);
		p5i3 = ap.createInput(URIUtils.createURI(baseURI, "p5.i3"), classes[6]);
		p5o1 = ap.createOutput(URIUtils.createURI(baseURI, "p5.o1"), classes[6]);

		Perform perf5 = ont.createPerform(URIUtils.createURI(baseURI, "perf5"));
		perf5.addBinding(p5i1, perf2, p2o1);
		perf5.addBinding(p5i2, perf3, p3o1);
		perf5.addBinding(p5i3, OWLS.Process.ThisPerform, p0i3);
		perf5.setProcess(ap);
		sequence.addComponent(perf5);

		Result result = cProcess.createResult(null);
		result.addBinding(p0o1, OWLS.Process.ThisPerform, p0i1);
		result.addBinding(p0o2, perf1, p1o1);
		result.addBinding(p0o3, perf5, p5o1);
		result.addBinding(p0o4, p0l1);

		return cProcess;
	}

}
