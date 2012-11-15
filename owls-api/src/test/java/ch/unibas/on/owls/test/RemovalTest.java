/*
 * (c) Michael Daenzer - University of Zürich Switzerland
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
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.mindswap.owl.OWLFactory;
import org.mindswap.owl.OWLIndividualList;
import org.mindswap.owl.OWLKnowledgeBase;
import org.mindswap.owls.process.CompositeProcess;
import org.mindswap.owls.process.ControlConstruct;
import org.mindswap.owls.process.Perform;
import org.mindswap.owls.process.Sequence;
import org.mindswap.owls.process.variable.Input;
import org.mindswap.owls.process.variable.Output;
import org.mindswap.owls.service.Service;
import org.mindswap.utils.URIUtils;

/**
 *
 * @author unascribed
 * @version $Rev: 2319 $; $Author: thorsten $; $Date: 2009-09-23 18:30:19 +0300 (Wed, 23 Sep 2009) $
 */
public class RemovalTest
{
	private static final String BASE_URI = "http://example.com/";
	private OWLKnowledgeBase kb;
	private Perform p1;
	private Perform p2;
	private Perform p3;
	private Perform p4;

	@Before public void initialize()
	{
		kb = OWLFactory.createKB();

		// Actually, the tests in this class do not depend on reasoning, thus, we wouldn't
		// have to set a reasoner. However, we set it to make sure that rebinding is done
		// correctly (when the backing models are modified by the tests).
		kb.setReasoner("Pellet");
	}

	@Test public void removeCCTest()
	{
		p1 = kb.createPerform(URIUtils.createURI(BASE_URI + "p1"));
		p2 = kb.createPerform(URIUtils.createURI(BASE_URI + "p2"));
		p3 = kb.createPerform(URIUtils.createURI(BASE_URI + "p3"));
		p4 = kb.createPerform(URIUtils.createURI(BASE_URI + "p4"));

		final Sequence sequence = kb.createSequence(URIUtils.createURI(BASE_URI + "sequence"));
		sequence.addComponent(p4);
		sequence.addComponent(p3);
		sequence.addComponent(p2);
		sequence.addComponent(p1);
		assertEquals(sequence.getConstructs().get(0), p4);
		assertEquals(sequence.getConstructs().get(1), p3);
		assertEquals(sequence.getConstructs().get(2), p2);
		assertEquals(sequence.getConstructs().get(3), p1);

		final Input in1 = kb.createInput(URIUtils.createURI(BASE_URI + "in1"));
		final Input in2 = kb.createInput(URIUtils.createURI(BASE_URI + "in2"));
		final Output out1 = kb.createOutput(URIUtils.createURI(BASE_URI + "out1"));

		final CompositeProcess process = kb.createCompositeProcess(URIUtils.createURI(BASE_URI + "process"));
		process.setComposedOf(sequence);
		process.addInput(in1);
		process.addInput(in2);
		process.addOutput(out1);
		assertEquals(process.getComposedOf(), sequence);
		assertEquals(process.getInput("in1"), in1);
		assertEquals(process.getInput("in2"), in2);
		assertEquals(process.getOutput("out1"), out1);

		final Service service = kb.createService(URIUtils.createURI(BASE_URI + "service"));
		service.setProcess(process);
		assertEquals(service.getProcess(), process);

		service.getProcess().delete();
		assertNull(service.getProcess());
	}

	@Test public void removeList()
	{
		p1 = kb.createPerform(URIUtils.createURI(BASE_URI + "p1"));
		p2 = kb.createPerform(URIUtils.createURI(BASE_URI + "p2"));
		p3 = kb.createPerform(URIUtils.createURI(BASE_URI + "p3"));
		p4 = kb.createPerform(URIUtils.createURI(BASE_URI + "p4"));

		final Sequence sequence = kb.createSequence(URIUtils.createURI(BASE_URI + "sequence"));
		sequence.addComponent(p4);
		sequence.addComponent(p3);
		sequence.addComponent(p2);
		sequence.addComponent(p1);
		assertEquals(sequence.getConstructs().size(), 4);

		sequence.removeConstruct(p4);
		sequence.removeConstruct(p2);

		final OWLIndividualList<ControlConstruct> list = sequence.getConstructs();
		assertEquals(list.size(), 2);
		assertEquals(list.get(0), p3);
		assertEquals(list.get(1), p1);
	}
}
