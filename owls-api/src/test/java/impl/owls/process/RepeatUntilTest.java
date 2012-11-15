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
package impl.owls.process;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.junit.Assume.assumeNoException;
import static org.junit.Assume.assumeNotNull;
import impl.owls.process.constructs.RepeatUntilImpl;

import java.io.InputStream;
import java.net.URI;
import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mindswap.exceptions.ExecutionException;
import org.mindswap.exceptions.UnsatisfiedPreconditionException;
import org.mindswap.owl.OWLClass;
import org.mindswap.owl.OWLDataProperty;
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
import org.mindswap.query.ValueMap;
import org.mindswap.utils.URIUtils;

import examples.ExampleURIs;


/**
 * JUnit integration test for {@link RepeatUntilImpl}. Example process is
 * loaded and executed. Indirectly tests (i) {@link impl.jena.SPARQLValueFunction},
 * (ii) {@link impl.swrl.BuiltinAtomImpl}, and {@link impl.swrl.ClassAtomImpl}
 * because they are used by the process. In addition, this test requires a
 * reasoner to be attached.
 *
 * @author unascribed
 * @version $Rev: 2269 $; $Author: thorsten $; $Date: 2009-08-19 18:21:09 +0300 (Wed, 19 Aug 2009) $
 */
public class RepeatUntilTest
{
	static final URI URI_weight = URIUtils.createURI(ExampleURIs.REPEAT_UNTIL_OWLS12, "weight");
	static final URI URI_insuranceNo = URIUtils.createURI(ExampleURIs.REPEAT_UNTIL_OWLS12, "insuranceNo");
	static final URI URI_PhysicalObject = URIUtils.createURI(ExampleURIs.REPEAT_UNTIL_OWLS12, "PhysicalObject");
	static final URI URI_Person = URIUtils.createURI(ExampleURIs.REPEAT_UNTIL_OWLS12, "Person");
	static final URI URI_InsuredPerson = URIUtils.createURI(ExampleURIs.REPEAT_UNTIL_OWLS12, "InsuredPerson");
	static final URI URI_Woman = URIUtils.createURI(ExampleURIs.REPEAT_UNTIL_OWLS12, "Woman");
	static final URI URI_Man = URIUtils.createURI(ExampleURIs.REPEAT_UNTIL_OWLS12, "Man");

	static final Random random = new Random();

	static OWLClass CLASS_PhysicalObject;
	static OWLClass CLASS_Person;
	static OWLClass CLASS_InsuredPerson;
	static OWLDataProperty PROP_weight;
	static OWLDataProperty PROP_insuranceNo;
	static OWLClass CLASS_Woman;
	static OWLClass CLASS_Man;

	static OWLKnowledgeBase kb;
	static Service service;
	static Process process;

	static OWLIndividual modelXYZ;
	static OWLIndividual sumotoriXYZ;

	@BeforeClass
	public static void init()
	{
		try // load service document and get Service, Process
		{
			kb = OWLFactory.createKB();
			long read = System.nanoTime();
			final InputStream inpStr = ClassLoader.getSystemResourceAsStream("owls/1.2/RepeatUntil.owl");
			service = kb.readService(inpStr, URIUtils.createURI(
				ExampleURIs.REPEAT_UNTIL_OWLS12, "RepeatUntilService")); // service name required because file specifies two services
			read = (System.nanoTime() - read) / 1000000;
			process = service.getProcess();

			System.out.printf("Read took %5dms%n", read);
		}
		catch (Exception e)
		{
			assumeNoException(e);
		}

		CLASS_PhysicalObject = kb.getClass(URI_PhysicalObject);
		CLASS_Person = kb.getClass(URI_Person);
		CLASS_InsuredPerson = kb.getClass(URI_InsuredPerson);
		CLASS_Woman = kb.getClass(URI_Woman);
		CLASS_Man = kb.getClass(URI_Man);

		PROP_weight = kb.getDataProperty(URI_weight);
		PROP_insuranceNo = kb.getDataProperty(URI_insuranceNo);
		assumeNotNull(CLASS_PhysicalObject, CLASS_InsuredPerson, CLASS_Person, CLASS_Woman,
			CLASS_Man, PROP_weight, PROP_insuranceNo);

		// reasoning is required throughout this test, so we need to set a reasoner
		kb.setReasoner("Pellet");
		kb.prepare();

//		((OntModel) kb.getImplementation()).register(new StatementListener() {
//			@Override
//			public void addedStatement(final Statement s)
//			{
//				System.out.println("added   " + s.toString());
//			}
//
//			@Override
//			public void removedStatement(final Statement s)
//			{
//				System.out.println("removed " + s.toString());
//			}
//		});
	}

	@Test
	public void testWorld()
	{
		// ontology contains those axioms: Woman, Man subclassOf Person; Woman disjointWith Man
		// actually not really required for this test, but it doesn't harm to test this as well
		assertTrue(CLASS_Woman.isSubTypeOf(CLASS_Person));
		assertTrue(CLASS_Man.isSubTypeOf(CLASS_Person));
		assertTrue(CLASS_Woman.isDisjointWith(CLASS_Man));
		assertTrue(CLASS_Man.isDisjointWith(CLASS_Woman));

		modelXYZ = kb.getIndividual(URIUtils.createURI(ExampleURIs.REPEAT_UNTIL_OWLS12, "ModelXYZ"));
		assumeNotNull(modelXYZ);

		// explicit axioms and assertions
		assertTrue(modelXYZ.isType(CLASS_Woman));
		assertTrue(modelXYZ.hasProperty(PROP_weight));
		assertTrue(modelXYZ.hasProperty(PROP_insuranceNo));

		// inferred axioms
		assertTrue(!modelXYZ.isType(CLASS_Man));
		assertTrue(modelXYZ.isType(CLASS_PhysicalObject));
		assertTrue(modelXYZ.isType(CLASS_InsuredPerson));

		System.out.println(modelXYZ.toRDF(false, true));


		sumotoriXYZ = kb.getIndividual(URIUtils.createURI(ExampleURIs.REPEAT_UNTIL_OWLS12, "SumotoriXYZ"));
		assumeNotNull(sumotoriXYZ);

		// explicit axioms and assertions
		assertTrue(sumotoriXYZ.isType(CLASS_Man));
		assertTrue(sumotoriXYZ.hasProperty(PROP_weight));
		assertTrue(!sumotoriXYZ.hasProperty(PROP_insuranceNo));

		// inferred axioms
		assertTrue(!sumotoriXYZ.isType(CLASS_Woman));
		assertTrue(sumotoriXYZ.isType(CLASS_PhysicalObject));
		assertTrue(!sumotoriXYZ.isType(CLASS_InsuredPerson));

		System.out.println(sumotoriXYZ.toRDF(false, true));
	}

	@Test
	public void testRepeatUntil1() throws Exception
	{
		int goalWeight = 60;

		// prepare input
		final ValueMap<Input, OWLValue> inputs = new ValueMap<Input, OWLValue>();
		inputs.setValue(process.getInput("person"), modelXYZ);
		inputs.setValue(process.getInput("goalWeight"), kb.createDataValue(Integer.valueOf(goalWeight)));

		final ProcessExecutionEngine exec = OWLSFactory.createExecutionEngine();
		exec.getExecutionValidator().setPreconditionCheck(true);

		System.out.printf("Number of statements in KB before execution: %d%n", kb.size());

		long exet = System.nanoTime();
		ValueMap<Output, OWLValue> outputs = exec.execute(process, inputs, kb);
		exet = (System.nanoTime() - exet) / 1000000;

		System.out.printf("Execution took %4dms%n", exet);
		System.out.printf("Number of statements in KB after execution: %d%n", kb.size());

		assertNotNull(outputs);
		assertEquals(1, outputs.size());

		Integer personWeight = (Integer) outputs.getDataValue("personWeight").getValue();
		assertNotNull(personWeight);
		assertTrue(personWeight.intValue() <= goalWeight); // weight must be less than or equal to goal weight

		// our Java grounding (see method makeDiet(...)) operates directly on the
		// person input of the process, thus, the weight property of modelXYZ must
		// be equal to the output value of the process
		Integer modelWeight = (Integer) modelXYZ.getProperty(PROP_weight).getValue();
		assertEquals(personWeight, modelWeight);
	}

	@Test
	public void testRepeatUntil2() throws Exception
	{
		int goalWeight = 100;

		// prepare input
		final ValueMap<Input, OWLValue> inputs = new ValueMap<Input, OWLValue>();
		inputs.setValue(process.getInput("person"), sumotoriXYZ);
		inputs.setValue(process.getInput("goalWeight"), kb.createDataValue(Integer.valueOf(goalWeight)));

		final ProcessExecutionEngine exec = OWLSFactory.createExecutionEngine();
		exec.getExecutionValidator().setPreconditionCheck(true);

		System.out.printf("Number of statements in KB before execution: %d%n", kb.size());

		long exet = System.nanoTime();
		try
		{
			exec.execute(process, inputs, kb);
			fail(UnsatisfiedPreconditionException.class + " expected");
		}
		catch (ExecutionException e) // sumotoriXYZ is not a insured person
		{
			System.out.println(e.getMessage());
			assertTrue(e instanceof UnsatisfiedPreconditionException);
			exet = (System.nanoTime() - exet) / 1000000;
		}

		System.out.printf("Execution took %4dms%n", exet);
		System.out.printf("Number of statements in KB after execution: %d%n", kb.size());
	}

	@Ignore // this is the target method of the Java grounding in the test process
	public static final int makeDiet(final OWLIndividual person)
	{
		int weight = ((Integer) person.getProperty(PROP_weight).getValue()).intValue();
		weight -= random.nextInt(3); // for one diet round a person loses at most 2kg :-)
		person.setProperty(PROP_weight, Integer.valueOf(weight));
		return weight;
	}

}
