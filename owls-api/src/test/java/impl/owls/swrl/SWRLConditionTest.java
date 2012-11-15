/*
 * Created 15.06.2009
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
package impl.owls.swrl;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.net.URI;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mindswap.common.Variable;
import org.mindswap.owl.OWLDataProperty;
import org.mindswap.owl.OWLDataValue;
import org.mindswap.owl.OWLFactory;
import org.mindswap.owl.OWLIndividual;
import org.mindswap.owl.OWLKnowledgeBase;
import org.mindswap.owl.OWLOntology;
import org.mindswap.owl.OWLValue;
import org.mindswap.owl.list.OWLList;
import org.mindswap.owl.vocabulary.SWRL;
import org.mindswap.owl.vocabulary.XSD;
import org.mindswap.owls.expression.Condition;
import org.mindswap.owls.process.variable.Existential;
import org.mindswap.owls.process.variable.Input;
import org.mindswap.owls.process.variable.Loc;
import org.mindswap.owls.process.variable.Output;
import org.mindswap.owls.vocabulary.OWLS;
import org.mindswap.query.ValueMap;
import org.mindswap.swrl.Atom;
import org.mindswap.swrl.SWRLDataVariable;
import org.mindswap.swrl.SWRLFactory;
import org.mindswap.swrl.SWRLFactory.ISWRLFactory;
import org.mindswap.utils.URIUtils;


/**
 *
 * @author unascribed
 * @version $Rev: 2530 $; $Author: thorsten $; $Date: 2010-07-30 20:46:00 +0300 (Fri, 30 Jul 2010) $
 */
public class SWRLConditionTest
{
	private static final String base = "http://example.org";
	private static OWLKnowledgeBase kb;

	@BeforeClass
	public static void before()
	{
		kb = OWLFactory.createKB();

		// reasoner is required to infer trivialities such as sameAs(ind1, ind1)
		// and to verify that kb is still consistent after all tests
		kb.setReasoner("Pellet");
	}

	@AfterClass
	public static void after()
	{
		System.out.println(
			kb.size() + " statements in KB after all tests finished; consistent " + kb.isConsistent());
//		kb.write(System.out, null);
	}

	@Test
	public void testSameAndDifferentIndividualAtomUsingIndividuals()
	{
		final OWLOntology ont = kb.createOntology(URI.create(base + "/1"));
		OWLS.addOWLSImports(ont);

		// create some instances of owl:Thing
		final OWLIndividual i1 = createThing(ont, "i1");
		final OWLIndividual i2 = createThing(ont, "i2");
		final OWLIndividual i3 = createThing(ont, "i3");
		final OWLIndividual i4 = createThing(ont, "i4");

		final ISWRLFactory sfa = SWRLFactory.createFactory(ont);
		OWLList<Atom> atoms = sfa.createList();
		Condition.SWRL swrlCondition = ont.createSWRLCondition(null);

		swrlCondition.setBody(atoms);
		assertTrue(swrlCondition.isTrue(kb, null)); // trivially true

		atoms = atoms.with(sfa.createDifferentIndividualsAtom(i1, i2));
		atoms.with(sfa.createDifferentIndividualsAtom(i2, i1));
		atoms.with(sfa.createSameIndividualAtom(i1, i1));
		atoms.with(sfa.createSameIndividualAtom(i2, i2));
		swrlCondition.setBody(atoms);
		assertFalse(swrlCondition.isTrue(kb, null)); // no knowledge if i1, i2 are different


		atoms = sfa.createList();
		atoms = atoms.with(sfa.createSameIndividualAtom(i1, i1));
		atoms.with(sfa.createSameIndividualAtom(i2, i2));
		swrlCondition = ont.createSWRLCondition(null);
		swrlCondition.setBody(atoms);
		assertTrue(swrlCondition.isTrue(kb, null)); // i1 = i1, i2 = i2 trivially true

		i3.addDifferentFrom(i4);
		atoms = sfa.createList(sfa.createDifferentIndividualsAtom(i3, i4));
		swrlCondition = ont.createSWRLCondition(null);
		swrlCondition.setBody(atoms);
		assertTrue(swrlCondition.isTrue(kb, null));

		atoms = sfa.createList(sfa.createSameIndividualAtom(i3, i4));
		swrlCondition = ont.createSWRLCondition(null);
		swrlCondition.setBody(atoms);
		assertFalse(swrlCondition.isTrue(kb, null));

		i1.addSameAs(i2);
		atoms = sfa.createList(sfa.createSameIndividualAtom(i1, i2));
		swrlCondition = ont.createSWRLCondition(null);
		swrlCondition.setBody(atoms);
		assertTrue(swrlCondition.isTrue(kb, null));

		atoms = sfa.createList(sfa.createDifferentIndividualsAtom(i1, i2));
		swrlCondition = ont.createSWRLCondition(null);
		swrlCondition.setBody(atoms);
		assertFalse(swrlCondition.isTrue(kb, null));

	}

	@Test
	public void testSameAndDifferentIndividualAtomUsingProcVariable()
	{
		final OWLOntology ont = kb.createOntology(URI.create(base + "/2"));
		OWLS.addOWLSImports(ont);

		// create some instances of owl:Thing
		final OWLIndividual i1 = createThing(ont, "i1");
		final OWLIndividual i2 = createThing(ont, "i2");
		final OWLIndividual i3 = createThing(ont, "i3");
		final OWLIndividual i4 = createThing(ont, "i4");

		// create some process variables which will be bound to i1 ... i4
		final String input = "input", output = "output", local = "local", existential = "existential";
		final Input inp1 = ont.createInput(URIUtils.createURI(ont.getURI(), input));
		final Output out2 = ont.createOutput(URIUtils.createURI(ont.getURI(), output));
		final Loc loc3 = ont.createLoc(URIUtils.createURI(ont.getURI(), local));
		final Existential exi4 = ont.createExistential(URIUtils.createURI(ont.getURI(), existential));

		// create a map that binds each variable to a value/individual
		final ValueMap<Variable, OWLValue> binding = new ValueMap<Variable, OWLValue>();
		binding.setValue(inp1, i1);
		binding.setValue(out2, i2);
		binding.setValue(loc3, i3);
		binding.setValue(exi4, i4);

		final ISWRLFactory sfa = SWRLFactory.createFactory(ont);
		OWLList<Atom> atoms = ont.createList(SWRL.AtomListVocabulary);
		Condition.SWRL swrlCondition = ont.createSWRLCondition(null);

		atoms = atoms.with(sfa.createDifferentIndividualsAtom(inp1, inp1));
		atoms.with(sfa.createDifferentIndividualsAtom(inp1, out2));
		atoms.with(sfa.createSameIndividualAtom(out2, out2));
		atoms.with(sfa.createSameIndividualAtom(loc3, loc3));
		swrlCondition.setBody(atoms);

		assertFalse(swrlCondition.isTrue(kb, binding)); // no knowledge if i1, i2 are different

		atoms = sfa.createList();
		atoms = atoms.with(sfa.createSameIndividualAtom(inp1, inp1));
		atoms.with(sfa.createSameIndividualAtom(inp1, inp1));
		swrlCondition = ont.createSWRLCondition(null);
		swrlCondition.setBody(atoms);
		assertTrue(swrlCondition.isTrue(kb, binding)); // i1 = i1, i2 = i2 trivially true

		i3.addDifferentFrom(i4);
		atoms = sfa.createList(sfa.createDifferentIndividualsAtom(loc3, exi4));
		swrlCondition = ont.createSWRLCondition(null);
		swrlCondition.setBody(atoms);
		assertTrue(swrlCondition.isTrue(kb, binding));

		atoms = sfa.createList(sfa.createSameIndividualAtom(loc3, exi4));
		swrlCondition = ont.createSWRLCondition(null);
		swrlCondition.setBody(atoms);
		assertFalse(swrlCondition.isTrue(kb, binding));

		i1.addSameAs(i2);
		atoms = sfa.createList(sfa.createSameIndividualAtom(inp1, out2));
		swrlCondition = ont.createSWRLCondition(null);
		swrlCondition.setBody(atoms);
		assertTrue(swrlCondition.isTrue(kb, binding));

		atoms = sfa.createList(sfa.createDifferentIndividualsAtom(inp1, out2));
		swrlCondition = ont.createSWRLCondition(null);
		swrlCondition.setBody(atoms);
		assertFalse(swrlCondition.isTrue(kb, binding));

	}

	@Test
	public void testDataPropertyAtom()
	{
		final OWLOntology ont = kb.createOntology(URI.create(base + "/3"));
		OWLS.addOWLSImports(ont);

		// create some instances of owl:Thing and some assertions about them
		final OWLIndividual i1 = createThing(ont, "i1");
		final OWLIndividual i2 = createThing(ont, "i2");
		final OWLIndividual i3 = createThing(ont, "i3");
		final OWLIndividual i4 = createThing(ont, "i4");
		final OWLDataValue dv1 = ont.createDataValue("dv1", XSD.string);
		final OWLDataValue dv2 = ont.createDataValue("dv2", XSD.string);
		final OWLDataValue dv3 = ont.createDataValue("dv3", XSD.string);
		final OWLDataValue dv4 = ont.createDataValue("dv4", XSD.string);
		final OWLDataProperty dp = ont.createDataProperty(URIUtils.createURI(ont.getURI(), "aDataProperty"));
		ont.addProperty(i1, dp, dv1);
		ont.addProperty(i2, dp, dv2);
		ont.addProperty(i3, dp, dv3);
		ont.addProperty(i4, dp, dv4);

		// create some process variables which will be bound to i1 ... i4
		final String input = "input", output = "output", local = "local", existential = "existential";
		final Input inp1 = ont.createInput(URIUtils.createURI(ont.getURI(), input));
		final Output out2 = ont.createOutput(URIUtils.createURI(ont.getURI(), output));
		final Loc loc3 = ont.createLoc(URIUtils.createURI(ont.getURI(), local));
		final Existential exi4 = ont.createExistential(URIUtils.createURI(ont.getURI(), existential));

		// create a map that binds each variable to a value/individual
		final ValueMap<Variable, OWLValue> binding = new ValueMap<Variable, OWLValue>();

		final ISWRLFactory sfa = SWRLFactory.createFactory(ont);
		OWLList<Atom> atoms = ont.createList(SWRL.AtomListVocabulary);
		Condition.SWRL swrlCondition = ont.createSWRLCondition(null);

		// 1.) only constant values; all atoms satisfied
		atoms = atoms.with(sfa.createDataPropertyAtom(dp, i1, dv1));
		atoms.with(sfa.createDataPropertyAtom(dp, i2, dv2));
		atoms.with(sfa.createDataPropertyAtom(dp, i3, dv3));
		atoms.with(sfa.createDataPropertyAtom(dp, i4, dv4));
		swrlCondition.setBody(atoms);
		assertTrue(swrlCondition.isTrue(kb, binding));

		// 2.) only constant values; third atom unsatisfied
		atoms = sfa.createList();
		swrlCondition = ont.createSWRLCondition(null);
		atoms = atoms.with(sfa.createDataPropertyAtom(dp, i1, dv1));
		atoms.with(sfa.createDataPropertyAtom(dp, i2, dv2));
		atoms.with(sfa.createDataPropertyAtom(dp, i3, dv2));
		atoms.with(sfa.createDataPropertyAtom(dp, i4, dv4));
		swrlCondition.setBody(atoms);
		assertFalse(swrlCondition.isTrue(kb, binding));

		// 3.) variables used for first argument; all atoms satisfied
		binding.setValue(inp1, i1);
		binding.setValue(out2, i2);
		binding.setValue(loc3, i3);
		binding.setValue(exi4, i4);

		atoms = sfa.createList();
		swrlCondition = ont.createSWRLCondition(null);
		atoms = atoms.with(sfa.createDataPropertyAtom(dp, inp1, dv1));
		atoms.with(sfa.createDataPropertyAtom(dp, out2, dv2));
		atoms.with(sfa.createDataPropertyAtom(dp, loc3, dv3));
		atoms.with(sfa.createDataPropertyAtom(dp, exi4, dv4));
		swrlCondition.setBody(atoms);
		assertTrue(swrlCondition.isTrue(kb, binding));

		// 4.) variables used for first argument; second atom unsatisfied
		atoms = sfa.createList();
		swrlCondition = ont.createSWRLCondition(null);
		atoms = atoms.with(sfa.createDataPropertyAtom(dp, inp1, dv1));
		atoms.with(sfa.createDataPropertyAtom(dp, out2, dv1));
		atoms.with(sfa.createDataPropertyAtom(dp, loc3, dv3));
		atoms.with(sfa.createDataPropertyAtom(dp, exi4, dv4));
		swrlCondition.setBody(atoms);
		assertFalse(swrlCondition.isTrue(kb, binding));

		// 5.) variables used for both arguments; all atoms satisfied
		final SWRLDataVariable v1 = sfa.createDataVariable(URIUtils.createURI(ont.getURI(), "v1")),
			v2 = sfa.createDataVariable(URIUtils.createURI(ont.getURI(), "v2")),
			v3 = sfa.createDataVariable(URIUtils.createURI(ont.getURI(), "v3")),
			v4 = sfa.createDataVariable(URIUtils.createURI(ont.getURI(), "v4"));
		binding.setValue(inp1, i1);
		binding.setValue(out2, i2);
		binding.setValue(loc3, i3);
		binding.setValue(exi4, i4);
		binding.setValue(v1, dv1);
		binding.setValue(v2, dv2);
		binding.setValue(v3, dv3);
		binding.setValue(v4, dv4);

		atoms = sfa.createList();
		swrlCondition = ont.createSWRLCondition(null);
		atoms = atoms.with(sfa.createDataPropertyAtom(dp, inp1, v1));
		atoms.with(sfa.createDataPropertyAtom(dp, out2, v2));
		atoms.with(sfa.createDataPropertyAtom(dp, loc3, v3));
		atoms.with(sfa.createDataPropertyAtom(dp, exi4, v4));
		swrlCondition.setBody(atoms);
		assertTrue(swrlCondition.isTrue(kb, binding));

		// 6.) variables used for both arguments; fourth atom unsatisfied
		atoms = sfa.createList();
		swrlCondition = ont.createSWRLCondition(null);
		atoms = atoms.with(sfa.createDataPropertyAtom(dp, inp1, v1));
		atoms.with(sfa.createDataPropertyAtom(dp, out2, v2));
		atoms.with(sfa.createDataPropertyAtom(dp, loc3, v3));
		atoms.with(sfa.createDataPropertyAtom(dp, exi4, v3));
		swrlCondition.setBody(atoms);
		assertFalse(swrlCondition.isTrue(kb, binding));
	}


	private OWLIndividual createThing(final OWLOntology ont, final String localName)
	{
		return ont.createInstance(null, URIUtils.createURI(ont.getURI(), localName));
	}

//	@Test
//	public void test2()
//	{
//		String base = "http://example.org#";
//		OntModel model = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);
//
//		Individual i1 = model.createIndividual(base + "i1", OWL.Thing);
//		String query =
//			"PREFIX : <" + base + ">" +
//			"PREFIX owl: <" + OWL.getURI() + ">" +
//			"SELECT *" +
//			"WHERE  { :i1 owl:sameAs :i1 . }";
////			"WHERE  { ?x owl:sameAs ?x . }";
//
//		// ARQ: works
//		Query q = QueryFactory.create(query);
//		QueryExecution qe = QueryExecutionFactory.create(q, model);
//
//		ResultSet rs = qe.execSelect();
//		System.out.println(rs.next());
//
//		// Pellet: throws exception
//		qe = SparqlDLExecutionFactory.create(q, model);
//		rs = qe.execSelect();
//		System.out.println(rs.next());
//
//	}
}
