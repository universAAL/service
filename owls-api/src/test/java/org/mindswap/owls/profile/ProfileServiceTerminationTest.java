/*
 * Created 25.11.2008
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
package org.mindswap.owls.profile;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mindswap.owl.OWLClass;
import org.mindswap.owl.OWLFactory;
import org.mindswap.owl.OWLIndividual;
import org.mindswap.owl.OWLKnowledgeBase;
import org.mindswap.owl.OWLObjectProperty;
import org.mindswap.owl.OWLOntology;
import org.mindswap.owls.vocabulary.OWLS;
import org.mindswap.owls.vocabulary.OWLS_Extensions;

/**
 *
 * @author unascribed
 * @version $Rev: 2269 $; $Author: thorsten $; $Date: 2009-08-19 18:21:09 +0300 (Wed, 19 Aug 2009) $
 */
public class ProfileServiceTerminationTest
{
	private static final String TEST_PROFILE_SERVICE_TERMINATION_ONT = "TestProfileServiceTermination.owl";
	private static OWLKnowledgeBase kb;
	private static OWLOntology ont;

	@BeforeClass
	public static void setUp() throws Exception
	{
		kb = OWLFactory.createKB();
		ont = kb.read(ClassLoader.getSystemClassLoader().getResource(
			"owls/1.2/" + TEST_PROFILE_SERVICE_TERMINATION_ONT).toURI());
		kb.setReasoner("Pellet");
		kb.prepare();
		if (!ont.isConsistent())
			fail("Ontology " + TEST_PROFILE_SERVICE_TERMINATION_ONT + " is not consistent.");
	}

	@Test
	public void testProfileServiceTerminationOntology()
	{
		OWLClass Compensatable = kb.getClass(OWLS_Extensions.Termination.Compensatable.getURI());
		OWLClass CompensationServiceParameter = kb.getClass(OWLS_Extensions.Termination.CompensationServiceParameter.getURI());
		OWLClass Compensator = kb.getClass(OWLS_Extensions.Termination.Compensator.getURI());
		OWLClass NonRetriable = kb.getClass(OWLS_Extensions.Termination.NonRetriable.getURI());
		OWLClass Pivot = kb.getClass(OWLS_Extensions.Termination.Pivot.getURI());
		OWLClass RetriabilityServiceParameter = kb.getClass(OWLS_Extensions.Termination.RetriabilityServiceParameter.getURI());
		OWLClass Retriable = kb.getClass(OWLS_Extensions.Termination.Retriable.getURI());
		assertNotNull(Compensatable);
		assertNotNull(Compensator);
		assertNotNull(NonRetriable);
		assertNotNull(Pivot);
		assertNotNull(Retriable);
		assertTrue(Compensatable.isSubTypeOf(OWLS.Profile.Profile));
		assertTrue(NonRetriable.isSubTypeOf(OWLS.Profile.Profile));
		assertTrue(Pivot.isSubTypeOf(OWLS.Profile.Profile));
		assertTrue(Retriable.isSubTypeOf(OWLS.Profile.Profile));
		assertTrue(kb.isComplement(Retriable, NonRetriable));
		assertTrue(kb.isComplement(Compensatable, Pivot));

		assertNotNull(CompensationServiceParameter);
		assertNotNull(RetriabilityServiceParameter);
		assertTrue(CompensationServiceParameter.isSubTypeOf(OWLS.ServiceParameter.ServiceParameter));
		assertTrue(RetriabilityServiceParameter.isSubTypeOf(OWLS.ServiceParameter.ServiceParameter));

		OWLObjectProperty retriable = kb.getObjectProperty(OWLS_Extensions.Termination.retriable.getURI());
		OWLObjectProperty compensator = kb.getObjectProperty(OWLS_Extensions.Termination.compensator.getURI());
		assertNotNull(retriable);
		assertNotNull(compensator);
		assertTrue(retriable.isSubPropertyOf(OWLS.ServiceParameter.serviceParameter));
		assertTrue(compensator.isSubPropertyOf(OWLS.ServiceParameter.serviceParameter));

		OWLIndividual False = kb.getIndividual(OWLS_Extensions.Termination.False.getURI());
		OWLIndividual True = kb.getIndividual(OWLS_Extensions.Termination.True.getURI());
		assertNotNull(False);
		assertNotNull(True);
		assertTrue(kb.isType(False, OWLS_Extensions.Termination.Boolean));
		assertTrue(kb.isType(True, OWLS_Extensions.Termination.Boolean));
		assertTrue(False.isDifferentFrom(True));
		assertTrue(True.isDifferentFrom(False));
	}

}
