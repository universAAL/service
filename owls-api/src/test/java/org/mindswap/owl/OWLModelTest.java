/*
 * Created on 25.05.2010
 *
 * (c) 2010 Thorsten Möller - University of Basel Switzerland
 *
 * This file is part of OSIRIS Next.
 *
 * OSIRIS Next is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OSIRIS Next is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OSIRIS Next.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mindswap.owl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNoException;
import impl.jena.OWLModelImpl;

import java.io.IOException;
import java.util.Iterator;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mindswap.owl.vocabulary.OWL;
import org.mindswap.owl.vocabulary.XSD;
import org.mindswap.owls.profile.Profile;
import org.mindswap.owls.service.Service;

import examples.ExampleURIs;

/**
 * JUnit test cases for {@link OWLModelImpl}. Agnostic to the backing RDF/OWL
 * framework used (e.g. Jena).
 *
 * @author unascribed
 * @version $Rev: 2522 $; $Author: thorsten $; $Date: 2010-07-19 16:43:13 +0300 (Mon, 19 Jul 2010) $
 */
public class OWLModelTest
{
	private static OWLKnowledgeBase kb;
	private static OWLDataType dateTime;
	private static OWLDataType anyURI;
	private static OWLDataType xfloat;
	private static OWLDataType xdouble;
	private static OWLDataType integer;
	private static OWLDataType decimal;
	private static OWLDataType xlong;
	private static OWLDataType xint;
	private static OWLDataType xnnint;
	private static OWLDataType xnpint;
	private static OWLDataType xnint;
	private static OWLDataType xpint;
	private static OWLDataType xbyte;
	private static OWLDataType xshort;
	private static OWLDataType xboolean;
	private static OWLDataType string;


	@BeforeClass
	public static void setUp() throws Exception
	{
		kb = OWLFactory.createKB();
		kb.setReasoner("Pellet"); // we need a reasoner in order to compute DL based type relations

		try
		{
			kb.read(ExampleURIs.BABELFISH_TRANSLATOR_OWLS12);
			kb.read(ExampleURIs.BN_BOOK_PRICE_OWLS12);
			kb.read(ExampleURIs.BOOK_FINDER_OWLS12);
			kb.read(ExampleURIs.FRENCH_DICTIONARY_OWLS12);
		}
		catch (final IOException e)
		{
			assumeNoException(e);
		}

		dateTime = kb.createDataType(XSD.dateTime);
		anyURI   = kb.createDataType(XSD.anyURI);
		integer  = kb.createDataType(XSD.integer);
		decimal  = kb.createDataType(XSD.decimal);
		xfloat   = kb.createDataType(XSD.xsdFloat);
		xdouble  = kb.createDataType(XSD.xsdDouble);
		xint     = kb.createDataType(XSD.xsdInt);
		xlong    = kb.createDataType(XSD.xsdLong);
		xnnint   = kb.createDataType(XSD.nonNegativeInteger);
		xnpint   = kb.createDataType(XSD.nonPositiveInteger);
		xnint    = kb.createDataType(XSD.negativeInteger);
		xpint    = kb.createDataType(XSD.positiveInteger);
		xshort   = kb.createDataType(XSD.xsdShort);
		xbyte    = kb.createDataType(XSD.xsdByte);
		xboolean = kb.createDataType(XSD.xsdBoolean);
		string   = kb.createDataType(XSD.xsdString);
	}

	/**
	 * Test method for {@link org.mindswap.owl.OWLModel#isSubTypeOf(org.mindswap.owl.OWLType, org.mindswap.owl.OWLType)}.
	 */
	@Test
	public final void testIsSubTypeOfDatatypes()
	{
		/* Simple datatypes (only a selection of subsumptions satsfied by the XSD specification) */

		// test reflexivity of subsumption relation for a random selection of types
		assertTrue(kb.isSubTypeOf(xint, xint));
		assertTrue(kb.isSubTypeOf(string, string));
		assertTrue(kb.isSubTypeOf(dateTime, dateTime));

		assertTrue(kb.isSubTypeOf(xint, integer));
		assertTrue(kb.isSubTypeOf(xlong, integer));
		assertTrue(kb.isSubTypeOf(xnnint, integer));
		assertTrue(kb.isSubTypeOf(xnpint, integer));
		assertTrue(kb.isSubTypeOf(xshort, integer));
		assertTrue(kb.isSubTypeOf(xbyte, xshort));
		assertTrue(kb.isSubTypeOf(xint, xlong));
		assertTrue(kb.isSubTypeOf(xshort, xlong));

		assertFalse(kb.isSubTypeOf(xnnint, xint));
		assertFalse(kb.isSubTypeOf(xnpint, xint));
		assertFalse(kb.isSubTypeOf(xnnint, xlong));
		assertFalse(kb.isSubTypeOf(xlong, xint));
		assertFalse(kb.isSubTypeOf(xlong, xshort));
		assertFalse(kb.isSubTypeOf(xint, xnnint));
		assertFalse(kb.isSubTypeOf(decimal, integer));
		assertFalse(kb.isSubTypeOf(decimal, xdouble));


		/* classes */
		assertTrue(kb.isSubTypeOf(OWL.Thing, OWL.Thing));
		assertTrue(kb.isSubTypeOf(OWL.Nothing, OWL.Thing));

		assertFalse(kb.isSubTypeOf(OWL.Thing, OWL.Nothing));
	}

	/**
	 * Test method for {@link org.mindswap.owl.OWLModel#isEquivalent(org.mindswap.owl.OWLType, org.mindswap.owl.OWLType)}.
	 */
	@Test
	public final void testIsEquivalent()
	{
		/* datatypes (only a selection of satisfied equivalences) */

		// an type is trivially a equivalent to itself (reflexivity)
		assertTrue(kb.isSubTypeOf(xint, xint));
		assertTrue(kb.isSubTypeOf(string, string));
		assertTrue(kb.isSubTypeOf(dateTime, dateTime));


		/* classes */

		// reflexivity
		assertTrue(kb.isEquivalent(OWL.Thing, OWL.Thing));
		assertTrue(kb.isEquivalent(OWL.Nothing, OWL.Nothing));

		// commutativity
		assertFalse(kb.isEquivalent(OWL.Thing, OWL.Nothing));
		assertFalse(kb.isEquivalent(OWL.Nothing, OWL.Thing));
	}

	/**
	 * Test method for {@link org.mindswap.owl.OWLModel#isDisjoint(org.mindswap.owl.OWLType, org.mindswap.owl.OWLType)}.
	 */
	@Test
	public final void testIsDisjoint()
	{
		/* Simple datatypes (only a selection of disjoints according to the XSD specification) */

		// 1. disjoint datatypes
		assertTrue(kb.isDisjoint(xnint, xpint));
		assertTrue(kb.isDisjoint(xnnint, xfloat));
		assertTrue(kb.isDisjoint(xlong, string));
		assertTrue(kb.isDisjoint(xint, anyURI));
		assertTrue(kb.isDisjoint(decimal, xdouble));
		assertTrue(kb.isDisjoint(decimal, xboolean));
		assertTrue(kb.isDisjoint(string, anyURI));

		// 2. reflexivity
		assertFalse(kb.isDisjoint(xint, xint));
		assertFalse(kb.isDisjoint(anyURI, anyURI));

		// 3. overlapping datatypes
		assertFalse(kb.isDisjoint(xnpint, xint));
		assertFalse(kb.isDisjoint(xlong, xnnint));
		assertFalse(kb.isDisjoint(xnint, xbyte));
		assertFalse(kb.isDisjoint(xpint, xbyte));

		// 4. commutativity to 3.
		assertFalse(kb.isDisjoint(xint, xnpint));
		assertFalse(kb.isDisjoint(xnnint, xlong));
		assertFalse(kb.isDisjoint(xbyte, xnint));
		assertFalse(kb.isDisjoint(xbyte, xpint));
	}

	/**
	 * Test method for {@link impl.jena.OWLModelImpl#getProfiles(boolean)}.
	 */
	@Test
	public final void testGetProfiles()
	{
		kb.setReasoner((Object) null);

		OWLIndividualList<Profile> profiles = kb.getProfiles(true);
		assertEquals(0, profiles.size()); // all profiles are in sub ontologies of the KB

		kb.setReasoner("Pellet");
		profiles = kb.getProfiles(true);
		assertEquals(0, profiles.size()); // even if reasoner is used we still don't get them


		// now we want to get all services that exist

		kb.setReasoner((Object) null);
		profiles = kb.getProfiles(false);

		// without a reasoner attached we only get direct instances of Profile,
		// and there is only one w.r.t. the loaded services
		assertEquals(1, profiles.size());
		assertNoDuplicates(profiles);

		kb.setReasoner("Pellet");
		profiles = kb.getProfiles(false);

		// using a reasoner we get instances of subclasses of Profile as well,
		// which amount to 5 in total w.r.t. the loaded services
		assertEquals(5, profiles.size());
		assertNoDuplicates(profiles);
	}

	/**
	 * Test method for {@link impl.jena.OWLModelImpl#getServices(boolean)}.
	 */
	@Test
	public final void testGetServices()
	{
		kb.setReasoner((Object) null);

		OWLIndividualList<Service> services = kb.getServices(true);
		assertEquals(0, services.size()); // all services are in sub ontologies of the KB

		kb.setReasoner("Pellet");
		services = kb.getServices(true);
		assertEquals(0, services.size()); // even if reasoner is used we still don't get them

		kb.setReasoner((Object) null);

		// now we want to get all services that exist
		services = kb.getServices(false);
		assertEquals(5, services.size());
		assertNoDuplicates(services);

		kb.setReasoner("Pellet");
		services = kb.getServices(false);
		assertEquals(5, services.size());
		assertNoDuplicates(services);
	}

	private <T extends OWLIndividual> void assertNoDuplicates(final OWLIndividualList<T> originals)
	{
		for (final Iterator<T> it = originals.iterator(); it.hasNext(); )
		{
			final T original = it.next();
			it.remove();

			final OWLIndividualList<T> clones = OWLFactory.createIndividualList();
			clones.addAll(originals);
			for (final Iterator<T> cit = clones.iterator(); cit.hasNext(); )
			{
				final T clone = cit.next();
				assertNotSame(original, clone);
			}
		}
	}

}
