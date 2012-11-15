/*
 * Created 11.07.2010
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
package org.mindswap.owl;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNoException;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.net.URI;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mindswap.owls.grounding.WSDLOperationRef;
import org.mindswap.owls.process.Process;
import org.mindswap.owls.profile.Actor;
import org.mindswap.owls.profile.ServiceCategory;
import org.mindswap.owls.service.Service;
import org.mindswap.owls.vocabulary.OWLS;
import org.mindswap.utils.ServiceGenerator;
import org.mindswap.utils.URIUtils;


/**
 *
 * @author unascribed
 * @version $Rev: 2544 $; $Author: thorsten $; $Date: 2010-09-30 16:30:42 +0200 (Thu, 30 Sep 2010) $
 */
public class OWLEntityTest
{
	private static OWLKnowledgeBase kb;
	private static URI baseURI;
	private static URI baseURIren;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		kb = OWLFactory.createKB();
		kb.load(OWLS.Process.AnyOrder.getOntology(), true); // tests require knowledge in Process ontology
		kb.setReasoner("Pellet");
		baseURI = URI.create("http://www.example.org/test.owl");
		baseURIren = URI.create("http://www.example.org/renamed.owl");

		// let's generate a synthetic service and some statements about it; subsequently we will rename them
		ServiceGenerator sg;
		try
		{
			sg = new ServiceGenerator(kb);
		}
		catch (final IOException e)
		{
			assumeNoException(e);
			return; // test returns on previous line already, this line is just to make the compiler happy
		}
		sg.generateStatements(baseURI, 5000, false);
	}

	@Test
	public void testRenameInd()
	{
		// rename using a newly created individual
		final OWLIndividual ind = kb.createInstance(OWLS.Actor.Actor, URIUtils.createURI(baseURI, "SomeActor"));
		final URI old = ind.getURI();
		final URI ren = URIUtils.createURI(baseURIren, old.getFragment());
		final OWLEntity renInd = ind.rename(ren);
		assertNotNull(renInd);
		assertFalse(renInd.isAnon());
		assertEquals(ren, renInd.getURI());
		assertNull(kb.getEntity(old));
		assertEquals(renInd, kb.getEntity(ren));
		assertTrue(renInd.canCastTo(Actor.class));
	}

	@Test
	public void testRenameIndReflexiveProp()
	{
		// rename using a newly created individual and reflexive property assertion
		final OWLIndividual ind = kb.createInstance(OWLS.ServiceCategory.ServiceCategory,
			URIUtils.createURI(baseURI, "MyCategory"));
		final OWLObjectProperty prop = kb.createObjectProperty(URIUtils.createURI(baseURI, "myProp"));
		kb.setProperty(ind, prop, ind);
		final URI old = ind.getURI();
		final URI ren = URIUtils.createURI(baseURIren, old.getFragment());
		final OWLEntity renInd = ind.rename(ren);
		assertNotNull(renInd);
		assertFalse(renInd.isAnon());
		assertEquals(ren, renInd.getURI());
		assertNull(kb.getEntity(old));
		assertEquals(renInd, kb.getEntity(ren));
		assertTrue(renInd.canCastTo(ServiceCategory.class));
		assertEquals(renInd, kb.getProperty(renInd.castTo(OWLIndividual.class), prop));
	}

	@Test
	public void testRenameService()
	{
		// rename existing individual
		final OWLIndividualList<Service> services = kb.getServices(true);
		assumeTrue(services.size() > 0);
		final Service s = services.get(0);
		final URI old = s.getURI();
		final URI ren = URIUtils.createURI(baseURIren, old.getFragment());
		assumeTrue(!old.equals(ren));

		final OWLEntity sren = s.rename(ren);
		assertNotNull(sren);
		assertEquals(ren, sren.getURI());
		assertNull(kb.getEntity(old));
		assertEquals(sren, kb.getEntity(ren));
		assertTrue(sren.canCastTo(Service.class));
	}

	@Test
	public void testMakeAnonymous()
	{
		// make anon
		final OWLIndividualList<? extends Process> processes = kb.getProcesses(Process.ANY, true);
		assumeTrue(processes.size() > 0);
		final Process p = processes.get(0);
		final URI old = p.getURI();

		final OWLEntity pren = p.rename(null);
		assertNotNull(pren);
		assertTrue(pren.isAnon());
		assertNull(kb.getEntity(old));
		assertTrue(pren.canCastTo(Process.class));
	}

	@Test
	public void testMakeAnonymousNamed()
	{
		// make anon named
		final OWLIndividualList<?> operations = kb.getInstances(OWLS.Grounding.WsdlOperationRef, true);
		assumeTrue(operations.size() > 0);
		final OWLIndividual op = operations.get(0);
		assumeTrue(op.isAnon());
		assumeTrue(op.canCastTo(WSDLOperationRef.class));

		final URI ren = URIUtils.createURI(baseURIren, "WSDLOperation");
		final OWLEntity opren = op.rename(ren);
		assertNotNull(opren);
		assertFalse(opren.isAnon());
		assertEquals(ren, opren.getURI());
		assertEquals(opren, kb.getEntity(ren));
		assertTrue(opren.canCastTo(WSDLOperationRef.class));
		// unfortunately, we have no means to check if the previous anon entity is no longer in the KB
	}

}
