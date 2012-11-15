/*
 * Created 03.05.2009
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
package impl.jena;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.junit.Test;
import org.mindswap.owl.OWLDataValue;
import org.mindswap.owl.OWLFactory;
import org.mindswap.owl.OWLIndividual;
import org.mindswap.owl.OWLOntology;
import org.mindswap.owl.OWLValue;
import org.mindswap.owl.list.OWLList;
import org.mindswap.owl.vocabulary.OWL;
import org.mindswap.owl.vocabulary.RDF;
import org.mindswap.owl.vocabulary.SWRL;
import org.mindswap.owls.process.AnyOrder;
import org.mindswap.owls.process.Choice;
import org.mindswap.owls.process.ControlConstruct;
import org.mindswap.owls.process.variable.Input;
import org.mindswap.owls.vocabulary.OWLS;
import org.mindswap.swrl.Atom;
import org.mindswap.swrl.DifferentIndividualsAtom;
import org.mindswap.swrl.SWRLFactory;
import org.mindswap.swrl.SWRLFactory.ISWRLFactory;

/**
 * JUnit test of classes {@link impl.jena.OWLListImpl}, using different
 * list item types, such as {@link OWLValue}, {@link OWLIndividual},
 * {@link Atom}, and {@link ControlConstruct}.
 *
 * @author unascribed
 * @version $Rev: 2269 $; $Author: thorsten $; $Date: 2009-08-19 18:21:09 +0300 (Wed, 19 Aug 2009) $
 */
public class OWLListTest
{
	private static final String baseURI = "http://on.cs.unibas.ch/owl-s/ontologies/";

	/**
	 * Tests case for various factory creation methods for lists.
	 */
	@Test
	public void testTrivialThings()
	{
		OWLOntology ont = OWLFactory.createKB().createOntology(URI.create(baseURI + System.nanoTime()));
		OWLList<OWLValue> list = ont.createList(RDF.ListVocabulary);
		assertEquals(0, list.size());
		assertEquals(RDF.ListVocabulary, list.getVocabulary());

		OWLList<OWLIndividual> olist = ont.createList(OWLS.ObjectList.List);
		assertEquals(0, olist.size());
		assertEquals(OWLS.ObjectList.List, olist.getVocabulary());

		OWLList<Atom> alist = SWRLFactory.createFactory(ont).createList();
		assertEquals(0, alist.size());
		assertEquals(SWRL.AtomListVocabulary, alist.getVocabulary());

		OWLList<ControlConstruct> clist = ont.createControlConstructList(null);
		assertEquals(0, clist.size());
		assertEquals(OWLS.Process.CCList, clist.getVocabulary());

		OWLList<ControlConstruct> cbag = ont.createControlConstructBag(null);
		assertEquals(0, cbag.size());
		assertEquals(OWLS.Process.CCBag, cbag.getVocabulary());
	}

	/**
	 * Tests cases for {@link OWLListImpl#clear()} and {@link OWLListImpl#delete()}.
	 */
	@Test
	public void testClearAndDelete()
	{
		int j = 300;
		OWLList<OWLValue> list = createSampleOWLValueList(j, false);
		OWLOntology ont = list.getOntology();
		assertTrue(ont.size() > 0);
		assertEquals(j, list.size());

		list = list.clear();
		assertEquals(0, list.size());
		assertEquals(0, ont.size()); // empty list is just a resource, thus, no statement remains!


		list = createSampleOWLValueList(j, false);
		ont = list.getOntology();
		list.delete();
		assertEquals(0, ont.size()); // list is entirely deleted!
	}

	/**
	 * Tests cases for {@link OWLListImpl#cons(OWLValue)}.
	 */
	@Test
	public void testConsOWLList()
	{
		int j = 1000;
		OWLList<OWLValue> list = createSampleOWLValueList(j, false);

		for (Iterator<OWLValue> it = list.iterator(); it.hasNext(); )
		{
			OWLValue v = it.next();
			assertTrue(v instanceof OWLDataValue);
			Integer vi = (Integer) ((OWLDataValue ) v).getValue();
			assertEquals(--j, vi.intValue());
		}
	}

	/**
	 * Tests cases for {@link OWLListImpl#cons(OWLValue)} using {@link OWLIndividual}.
	 */
	@Test
	public void testConsOWLIndList()
	{
		int j = 1000;
		OWLList<OWLIndividual> list = createSampleOWLIndList(j, false);

		for (Iterator<OWLIndividual> it = list.iterator(); it.hasNext(); )
		{
			OWLIndividual v = it.next();
			assertTrue(v.canCastTo(Input.class));
			Input inp = v.castTo(Input.class);
			assertEquals(Integer.toString(--j), inp.getLabel(null));
		}
	}

	/**
	 * Tests cases for {@link OWLListImpl#cons(OWLValue)} using {@link Atom}.
	 */
	@Test
	public void testConsOWLAtomList()
	{
		int j = 1000;
		OWLList<Atom> list = createSampleAtomList(j, false);

		for (Iterator<Atom> it = list.iterator(); it.hasNext(); )
		{
			Atom atom = it.next();
			assertTrue(atom.canCastTo(DifferentIndividualsAtom.class));
			DifferentIndividualsAtom dia = atom.castTo(DifferentIndividualsAtom.class);
			assertEquals(Integer.toString(--j), dia.getLabel(null));
		}
	}

	/**
	 * Tests cases for {@link OWLListImpl#toList()}
	 */
	@Test
	public void testToList()
	{
		int j = 1000;
		OWLList<OWLValue> list = createSampleOWLValueList(j, false);

		List<OWLValue> asJavaList = list.toList();
		assertEquals(j, asJavaList.size());
		for (Iterator<OWLValue> it = list.iterator(), iit = asJavaList.iterator(); it.hasNext() && iit.hasNext(); )
		{
			OWLValue v = it.next();
			OWLValue v1 = iit.next();
			assertTrue(v.equals(v1));
			Integer vi = (Integer) ((OWLDataValue ) v).getValue();
			Integer v1i = (Integer) ((OWLDataValue ) v1).getValue();
			assertEquals(vi.intValue(), v1i.intValue());
		}
	}

	/**
	 * Tests cases for {@link OWLListImpl#with(OWLValue)}.
	 */
	@Test
	public void testWithOWLList()
	{
		int j = 1000;
		OWLList<OWLValue> list = createSampleOWLValueList(j, true);
		j = 0;
		for (Iterator<OWLValue> it = list.iterator(); it.hasNext(); )
		{
			OWLValue v = it.next();
			assertTrue(v instanceof OWLDataValue);
			Integer vi = (Integer) ((OWLDataValue) v).getValue();
			assertEquals(j++, vi.intValue());
		}
	}

	/**
	 * Tests cases for {@link OWLListImpl#cons(OWLValue)} using {@link OWLIndividual}
	 */
	@Test
	public void testWithOWLSObjList()
	{
		int j = 1000;
		OWLList<OWLIndividual> list = createSampleOWLIndList(j, true);

		j = 0;
		for (Iterator<OWLIndividual> it = list.iterator(); it.hasNext(); )
		{
			OWLValue v = it.next();
			assertTrue(v instanceof OWLIndividual);
			assertTrue(v.canCastTo(Input.class));
			Input inp = v.castTo(Input.class);
			assertEquals(Integer.toString(j++), inp.getLabel(null));
		}
	}

	/**
	 * Tests cases for {@link OWLListImpl#cons(OWLValue)} using {@link Atom}
	 */
	@Test
	public void testWithOWLAtomList()
	{
		int j = 1000;
		OWLList<Atom> list = createSampleAtomList(j, true);

		j = 0;
		for (Iterator<Atom> it = list.iterator(); it.hasNext(); )
		{
			OWLValue v = it.next();
			assertTrue(v instanceof OWLIndividual);
			assertTrue(v.canCastTo(Atom.class));
			Atom atom = v.castTo(Atom.class);
			assertTrue(atom.canCastTo(DifferentIndividualsAtom.class));
			DifferentIndividualsAtom dia = atom.castTo(DifferentIndividualsAtom.class);
			assertEquals(Integer.toString(j++), dia.getLabel(null));
		}
	}

	/**
	 * Test cases for {@link OWLListImpl#get(int)}.
	 */
	@Test
	public void testGet1()
	{
		int size = 500;
		OWLList<OWLValue> list = createSampleOWLValueList(size, true);

		try
		{
			list.get(-1);
			fail(IndexOutOfBoundsException.class.getSimpleName() + " expected.");
		}
		catch (IndexOutOfBoundsException e)	{ /* expected */ }

		try
		{
			list.get(size);
			fail(IndexOutOfBoundsException.class.getSimpleName() + " expected.");
		}
		catch (IndexOutOfBoundsException e)	{ /* expected */ }

		try
		{
			list.get(size + size);
			fail(IndexOutOfBoundsException.class.getSimpleName() + " expected.");
		}
		catch (IndexOutOfBoundsException e)	{ /* expected */ }

		OWLValue v = list.get(0);
		Integer vi = (Integer) ((OWLDataValue) v).getValue();
		assertEquals(0, vi.intValue());

		v = list.get(size - 1);
		assertTrue(v.isDataValue());
		vi = (Integer) ((OWLDataValue) v).getValue();
		assertEquals(size - 1, vi.intValue());

		Random r = new Random();
		int rv;
		for (int i = 0; i < 100; i++)
		{
			rv = r.nextInt(size);
			v = list.get(rv);
			assertTrue(v.isDataValue());
			vi = (Integer) ((OWLDataValue) v).getValue();
			assertEquals(rv, vi.intValue());
		}
	}

	/**
	 * Test cases for {@link OWLListImpl#get(int)}.
	 */
	@Test
	public void testGet2() throws URISyntaxException
	{
		// most of the things that should be tested are already done in test case testGetOWLList()
		// just do some more tests using various list item types

		OWLList<OWLIndividual> olist = createSampleOWLIndList(500, true);
		OWLIndividual ind = olist.get(333);
		assertTrue(ind.canCastTo(Input.class));
		Input inp = ind.castTo(Input.class);
		assertEquals(Integer.toString(333), inp.getLabel(null));

		OWLList<Atom> alist = createSampleAtomList(500, true);
		Atom atom = alist.get(166);
		assertTrue(atom.canCastTo(DifferentIndividualsAtom.class));
		DifferentIndividualsAtom dia = atom.castTo(DifferentIndividualsAtom.class);
		assertEquals(Integer.toString(166), dia.getLabel(null));

		final OWLOntology ont = OWLFactory.createKB().createOntology(URI.create(baseURI + System.nanoTime()));
		final URI choiceURI = new URI("blah", "choice.blah.blah", "frablah");
		final Choice choice = ont.createChoice(choiceURI);
		final OWLList<ControlConstruct> cclist = ont.createControlConstructList(choice);
		assertEquals(1, cclist.size());
		ControlConstruct cc = cclist.get(0);
		assertTrue(cc.canCastTo(Choice.class));
		assertEquals(choiceURI, cc.getURI());

		final URI anyOrderURI = new URI("blah", "any.order.blah.blah", "frablah");
		final AnyOrder anyOrder = ont.createAnyOrder(anyOrderURI);
		final OWLList<ControlConstruct> ccbag = ont.createControlConstructBag(anyOrder);
		assertEquals(1, ccbag.size());
		cc = ccbag.get(0);
		assertTrue(cc.canCastTo(AnyOrder.class));
		assertEquals(anyOrderURI, cc.getURI());
	}

	/**
	 * Test cases for {@link OWLListImpl#getFirst()}, {@link OWLListImpl#getRest()}.
	 */
	@Test
	public void testGetFirstRest() throws URISyntaxException
	{
		int j = 50;
		OWLList<OWLValue> list = createSampleOWLValueList(j, false);
		OWLList<OWLValue> listRest = list;
		while (!listRest.isEmpty())
		{
			OWLValue v = listRest.getFirst();
			assertTrue(v instanceof OWLDataValue);
			Integer vi = (Integer) ((OWLDataValue ) v).getValue();
			assertEquals(--j, vi.intValue());

			listRest = listRest.getRest();
		}


		// repeat using different parameterized lists

		j = 50;
		OWLList<Atom> alist = createSampleAtomList(j, false);
		OWLList<Atom> alistRest = alist;
		while (!alistRest.isEmpty())
		{
			Atom atom = alistRest.getFirst();
			assertTrue(atom.canCastTo(DifferentIndividualsAtom.class));
			DifferentIndividualsAtom dia = atom.castTo(DifferentIndividualsAtom.class);
			assertEquals(Integer.toString(--j), dia.getLabel(null));

			alistRest = alistRest.getRest();
		}


		final OWLOntology ont = OWLFactory.createKB().createOntology(URI.create(baseURI + System.nanoTime()));
		final URI choiceURI = new URI("blah", "choice.blah.blah", "frablah");
		final Choice choice = ont.createChoice(choiceURI);
		final OWLList<ControlConstruct> cclist = ont.createControlConstructList(choice);

		OWLIndividual first = cclist.getFirst();
		assertNotNull(first);
		assertTrue(first.canCastTo(Choice.class));
		Choice choiceTmp = first.castTo(Choice.class);
		assertEquals(choice, choiceTmp);

		final OWLList<ControlConstruct> restlist = cclist.getRest();
		assertNotNull(restlist);
		assertTrue(restlist.getURI().equals(cclist.getVocabulary().nil().getURI()));


		final URI anyOrderURI = new URI("blah", "any.order.blah.blah", "frablah");
		final AnyOrder anyOrder = ont.createAnyOrder(anyOrderURI);
		final OWLList<ControlConstruct> ccbag = ont.createControlConstructBag(anyOrder);

		first = ccbag.getFirst();
		assertNotNull(first);
		assertTrue(first.canCastTo(AnyOrder.class));
		AnyOrder anyOrderTmp = first.castTo(AnyOrder.class);
		assertEquals(anyOrder, anyOrderTmp);

		final OWLList<ControlConstruct> restbag = ccbag.getRest();
		assertNotNull(restbag);
		assertTrue(restbag.getURI().equals(ccbag.getVocabulary().nil().getURI()));
	}

	/**
	 * Test cases for {@link OWLListImpl#indexOf(OWLValue)}
	 */
	@Test
	public void testIndexOf()
	{
		int size = 300;
		OWLList<OWLValue> list = createSampleOWLValueList(size, true);
		final Random r = new Random();
		for (int i = 0; i < 100; i++)
		{
			int value = r.nextInt(size * 2);
			if (value >= size)
			{
				assertEquals(-1, list.indexOf(list.getOntology().createDataValue(Integer.valueOf(value))));
			}
			else
			{
				assertEquals(value, list.indexOf(list.getOntology().createDataValue(Integer.valueOf(value))));
			}
		}
	}

	/**
	 * Test cases for {@link OWLListImpl#insert(int, OWLValue)}.
	 */
	@Test
	public void testInsert()
	{
		int size = 50;
		OWLList<OWLValue> list = createSampleOWLValueList(size, true);

		try
		{
			list.insert(-1, list.getOntology().createDataValue(Integer.valueOf(0)));
			fail(IndexOutOfBoundsException.class.getSimpleName() + " expected.");
		}
		catch (IndexOutOfBoundsException e)	{ /* expected */ }

		try
		{
			// index == size is possible (add to end)
			list.insert(size + 1, list.getOntology().createDataValue(Integer.valueOf(0)));
			fail(IndexOutOfBoundsException.class.getSimpleName() + " expected.");
		}
		catch (IndexOutOfBoundsException e)	{ /* expected */ }

		try
		{
			list.insert(size + size, list.getOntology().createDataValue(Integer.valueOf(0)));
			fail(IndexOutOfBoundsException.class.getSimpleName() + " expected.");
		}
		catch (IndexOutOfBoundsException e)	{ /* expected */ }

		// insert at the beginning
		list = list.insert(0, list.getOntology().createDataValue(Integer.valueOf(-10)));
		assertEquals(++size, list.size());
		OWLValue v = list.get(0);
		Integer vi = (Integer) ((OWLDataValue) v).getValue();
		assertEquals(-10, vi.intValue());

		// insert at one before end
		list = list.insert(size - 1, list.getOntology().createDataValue(Integer.valueOf(-100)));
		assertEquals(++size, list.size());
		v = list.get(size - 2);
		assertTrue(v.isDataValue());
		vi = (Integer) ((OWLDataValue) v).getValue();
		assertEquals(-100, vi.intValue());

		// insert at end (append)
		list = list.insert(size, list.getOntology().createDataValue(Integer.valueOf(-1000)));
		assertEquals(++size, list.size());
		v = list.get(size - 1);
		assertTrue(v.isDataValue());
		vi = (Integer) ((OWLDataValue) v).getValue();
		assertEquals(-1000, vi.intValue());

		// insert some values at random position
		final Random r = new Random();
		final List<OWLValue> copy = list.toList();
		int index;
		for (int i = 0; i < 30; i++)
		{
			index = r.nextInt(size);
			v = list.getOntology().createDataValue(Integer.valueOf(index * 100000));
			list = list.insert(index, v);
			copy.add(index, v);
		}

		OWLValue cv;
		for(Iterator<OWLValue> lit = list.iterator(), cit = copy.iterator(); lit.hasNext() && cit.hasNext(); )
		{
			v = lit.next();
			cv = cit.next();
			assertEquals(cv, v);
		}
	}

	/**
	 * Test cases for {@link OWLListImpl#set(int, OWLValue)}.
	 */
	@Test
	public void testSet()
	{
		final int size = 50;
		OWLList<OWLValue> list = createSampleOWLValueList(size, true);

		try
		{
			list.set(-1, list.getOntology().createDataValue(Integer.valueOf(0)));
			fail(IndexOutOfBoundsException.class.getSimpleName() + " expected.");
		}
		catch (IndexOutOfBoundsException e)	{ /* expected */ }

		try
		{
			list.set(size, list.getOntology().createDataValue(Integer.valueOf(0)));
			fail(IndexOutOfBoundsException.class.getSimpleName() + " expected.");
		}
		catch (IndexOutOfBoundsException e)	{ /* expected */ }

		try
		{
			list.insert(size + size, list.getOntology().createDataValue(Integer.valueOf(0)));
			fail(IndexOutOfBoundsException.class.getSimpleName() + " expected.");
		}
		catch (IndexOutOfBoundsException e)	{ /* expected */ }

		// set first in list
		OWLValue old = list.set(0, list.getOntology().createDataValue(Integer.valueOf(-10)));
		assertEquals(size, list.size());
		assertEquals(0, ((Integer) ((OWLDataValue) old).getValue()).intValue());
		OWLValue v = list.get(0);
		Integer vi = (Integer) ((OWLDataValue) v).getValue();
		assertEquals(-10, vi.intValue());

		// set last in list
		old = list.set(size - 1, list.getOntology().createDataValue(Integer.valueOf(-100)));
		assertEquals(size, list.size());
		assertEquals(size - 1, ((Integer) ((OWLDataValue) old).getValue()).intValue());
		v = list.get(size - 1);
		assertTrue(v.isDataValue());
		vi = (Integer) ((OWLDataValue) v).getValue();
		assertEquals(-100, vi.intValue());

		// set some values at random position

		final Random r = new Random();
		list = createSampleOWLValueList(size, true); // get fresh list without replaced values
		final List<OWLValue> copy = list.toList();
		int index;
		for (int i = 0; i < 30; i++)
		{
			index = r.nextInt(size);
			v = list.getOntology().createDataValue(Integer.valueOf(index * 100000));
			old = list.set(index, v);
			assertEquals(copy.get(index), old);
			copy.set(index, v);
		}

		OWLValue cv;
		for(Iterator<OWLValue> lit = list.iterator(), cit = copy.iterator(); lit.hasNext() && cit.hasNext(); )
		{
			v = lit.next();
			cv = cit.next();
			assertEquals(cv, v);
		}
	}

	/**
	 * JUnit test cases for {@link OWLListImpl#remove(int)}.
	 */
	@Test
	public void testRemoveAtIndex()
	{
		int size = 300;
		OWLList<OWLValue> list = createSampleOWLValueList(size, true);
		final Random r = new Random();

		try
		{
			list.remove(-1);
			fail(IndexOutOfBoundsException.class + " expected.");
		}
		catch (IndexOutOfBoundsException e) { /* expected */ }

		try
		{
			list.remove(size);
			fail(IndexOutOfBoundsException.class + " expected.");
		}
		catch (IndexOutOfBoundsException e) { /* expected */ }

		try
		{
			list.remove(size + 1);
			fail(IndexOutOfBoundsException.class + " expected.");
		}
		catch (IndexOutOfBoundsException e) { /* expected */ }

		// random removal of elements
		while (!list.isEmpty())
		{
			int index = r.nextInt(size--);
			list = list.remove(index);

			assertEquals(size, list.size());
		}
		assertTrue(list.isEmpty());

		// always remove the last element
		size = 30;
		list = createSampleOWLValueList(size, true);
		while (!list.isEmpty())
		{
			list = list.remove(--size);
			assertEquals(-1, list.indexOf(list.getOntology().createDataValue(Integer.valueOf(size))));
		}
		assertTrue(list.isEmpty());
	}


	/**
	 * JUnit test cases for {@link OWLListImpl#remove(OWLValue)}.
	 */
	@Test
	public void testRemoveValue()
	{
		int size = 300;
		OWLList<OWLValue> list = createSampleOWLValueList(size, true);
		Random r = new Random();

		// try to remove some elements that are not in the list --> should have no effect
		list = list.remove(null);
		assertEquals(size, list.size());

		list = list.remove(list.getOntology().createDataValue(Integer.valueOf(-1)));
		assertEquals(size, list.size());

		list = list.remove(list.getOntology().createDataValue(Integer.valueOf(size)));
		assertEquals(size, list.size());

		list = list.remove(list.getOntology().createDataValue(Integer.valueOf(size + 1)));
		assertEquals(size, list.size());

		// random removal of elements
		List<Integer> toRemove = new ArrayList<Integer>(size);
		for (int i = 0; i < size; i++) toRemove.add(Integer.valueOf(i));
		while (!list.isEmpty())
		{
			Integer i = toRemove.get(r.nextInt(size--));

			list = list.remove(list.getOntology().createDataValue(i));
			assertEquals(size, list.size());

			toRemove.remove(i);
		}
		assertTrue(list.isEmpty());

		// always remove the last element
		size = 30;
		list = createSampleOWLValueList(size, true);
		while (!list.isEmpty())
		{
			OWLDataValue integer = list.getOntology().createDataValue(Integer.valueOf(--size));
			list = list.remove(integer);
			assertEquals(-1, list.indexOf(integer));
		}
		assertTrue(list.isEmpty());
	}

	/**
	 * JUnit test cases for {@link OWLListImpl#specialize(Class)}.
	 */
	@Test
	public void testSpecialize()
	{
		final OWLList<Atom> list = createSampleAtomList(100, false);
		final OWLList<DifferentIndividualsAtom> specList = list.specialize(DifferentIndividualsAtom.class);

		final Iterator<Atom> lit = list.iterator();
		final Iterator<DifferentIndividualsAtom> slit = specList.iterator();
		while (lit.hasNext() && slit.hasNext())
		{
			Atom atom = lit.next();
			DifferentIndividualsAtom dia = slit.next();
			assertEquals(atom, dia);
		}
	}

	private OWLList<Atom> createSampleAtomList(final int numberOfElements, final boolean with)
	{
		final OWLOntology ont = OWLFactory.createKB().createOntology(URI.create(baseURI + System.nanoTime()));
		final ISWRLFactory swrlFa = SWRLFactory.createFactory(ont);
		OWLList<Atom> list = swrlFa.createList();
		for (int i = 0; i < numberOfElements; i++)
		{
			Atom atom = swrlFa.createDifferentIndividualsAtom(
				ont.createInstance(OWL.Thing, null), ont.createInstance(OWL.Thing, null));
			atom.setLabel(Integer.toString(i), null);
			if (with) list = list.with(atom);
			else list = list.cons(atom);
		}
		assertEquals(numberOfElements, list.size());
		return list;
	}

	private OWLList<OWLValue> createSampleOWLValueList(final int numberOfElements, final boolean with)
	{
		final OWLOntology ont = OWLFactory.createKB().createOntology(URI.create(baseURI + System.nanoTime()));
		OWLList<OWLValue> list = ont.createList(RDF.ListVocabulary);
		for (int i = 0; i < numberOfElements; i++)
		{
			if (with) list = list.with(ont.createDataValue(Integer.valueOf(i)));
			else list = list.cons(ont.createDataValue(Integer.valueOf(i)));
		}
		assertEquals(numberOfElements, list.size());
		return list;
	}

	private OWLList<OWLIndividual> createSampleOWLIndList(final int numberOfElements, final boolean with)
	{
		final OWLOntology ont = OWLFactory.createKB().createOntology(URI.create(baseURI + System.nanoTime()));
		OWLList<OWLIndividual> list = ont.createList(OWLS.ObjectList.List);
		for (int i = 0; i < numberOfElements; i++)
		{
			Input inp = ont.createInput(null);
			inp.setLabel(Integer.toString(i), null);
			if (with) list = list.with(inp);
			else list = list.cons(inp);
		}
		assertEquals(numberOfElements, list.size());
		return list;
	}
}
