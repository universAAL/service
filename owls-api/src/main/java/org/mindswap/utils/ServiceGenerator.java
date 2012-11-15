/*
 * Created on 24.09.2009
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

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

import org.mindswap.owl.OWLClass;
import org.mindswap.owl.OWLFactory;
import org.mindswap.owl.OWLIndividualList;
import org.mindswap.owl.OWLKnowledgeBase;
import org.mindswap.owl.OWLObjectProperty;
import org.mindswap.owl.OWLOntology;
import org.mindswap.owls.grounding.AtomicGrounding;
import org.mindswap.owls.grounding.JavaAtomicGrounding;
import org.mindswap.owls.grounding.JavaParameter;
import org.mindswap.owls.grounding.JavaVariable;
import org.mindswap.owls.grounding.MessageMap;
import org.mindswap.owls.grounding.WSDLAtomicGrounding;
import org.mindswap.owls.grounding.WSDLGrounding;
import org.mindswap.owls.grounding.WSDLOperationRef;
import org.mindswap.owls.process.AtomicProcess;
import org.mindswap.owls.process.Process;
import org.mindswap.owls.process.variable.Input;
import org.mindswap.owls.process.variable.Output;
import org.mindswap.owls.profile.Profile;
import org.mindswap.owls.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import examples.ExampleURIs;

/**
 * This class can be used for automated generation of (large amounts of)
 * synthetic (i) OWL-S service descriptions or (ii) additional statements
 * about an existing service description. Generated services and statements
 * respectively will be added to a user provided {@link OWLKnowledgeBase}.
 * In both cases only ABox assertions will be created. One or more ontologies
 * can be loaded to the KB at instantiation time which will be used when
 * synthetic individuals need to be created, with each new individual being an
 * instance of a randomly selected named concept out of the loaded ontologies.
 * By default, the {@link ExampleURIs#ONT_LUBM LUBM} ontology is used.
 *
 * @author unascribed
 * @version $Rev: 2545 $; $Author: thorsten $; $Date: 2010-09-30 18:15:31 +0200 (Thu, 30 Sep 2010) $
 */
public class ServiceGenerator
{
	/** The value of this constant is <tt>http://www.example.org/ont</tt> */
	public static final URI DEFAULT_BASE_URI = URI.create("http://www.example.org/ont");

	private static final Logger logger = LoggerFactory.getLogger(ServiceGenerator.class);

	URI base;
	final Random r;
	final OWLKnowledgeBase kb;
	final List<OWLClass> owlClasses;

	/**
	 * Create a new service generator that adds generated services to the given
	 * KB and also load all ontologies into the KB.
	 *
	 * @param target The target knowledge base to which all generated services
	 * 	will be added. If <code>null</code> a new one will be created.
	 * @param ontologies One or more URLs referring to ontologies to be loaded into
	 * 	the target KB. By default, if none is given (<code>null</code>), the
	 * 	{@link ExampleURIs#ONT_LUBM} ontology will be loaded into the KB.
	 * @throws IOException In case loading of some ontology failed for I/O
	 * 	related reasons.
	 */
	public ServiceGenerator(final OWLKnowledgeBase target, URI... ontologies) throws IOException
	{
		r = new Random();
		kb = (target != null)? target : OWLFactory.createKB();
		ontologies = (ontologies == null || ontologies.length == 0)?
			new URI[] {URI.create(ExampleURIs.ONT_LUBM)} : ontologies;
		owlClasses = new ArrayList<OWLClass>();

		for (final URI ont : ontologies)
		{
			final OWLOntology lubmOnt = kb.read(ont);
			owlClasses.addAll(lubmOnt.getClasses(true));
		}
	}

	/**
	 * This method is functionally equivalent to invoking
	 * <pre>
	 * generateAtomicServices(baseURI, numberOfServices, 2, 2, useSubModels, logProgress);
	 * </pre>
	 *
	 * @see #generateAtomicServices(URI, int, int, int, boolean, boolean)
	 */
	public void generateAtomicServices(final URI baseURI,
		final int numberOfServices, final boolean useSubModels, final boolean logProgress)
	{
		generateAtomicServices(baseURI, numberOfServices, 2, 2, useSubModels, logProgress);
	}

	/**
	 * Generate synthetic OWL-S service descriptions. More precisely, a Service
	 * that presents a Profile, described by a AtomicProcess, and that supports
	 * a WSDLAtomicGrounding.
	 * <p>
	 * Each generated service will have two inputs and two outputs, each
	 * typed to a randomly selected OWL class from the ontologies that have been
	 * loaded at instantiation time.
	 * Furthermore, each service has a complete but synthetic WSDL grounding.
	 * As such, generated services can be regarded "complete" exemplary atomic
	 * services, which can't be invoked, however, because the actual Web service
	 * does not exist.
	 *
	 * @param baseURI Base URI used as a prefix for named individuals (Service,
	 * 	Process, Profile, Grounding, etc). Defaults to {@link #DEFAULT_BASE_URI}
	 * 	if <code>null</code>.
	 * @param numberOfServices The number of services to generate. The upper
	 * 	limit mostly depends on available resources. For 32-bit machines,
	 * 	however, the upper limit is about 10^5 because of the 1,5GB heap
	 * 	memory boundary of most Java VMs.
	 * @param numberOfInputs Number of inputs to create for each generated service.
	 * 	For instance, a value of <tt>5</tt> results in a service having five
	 * 	inputs. Negative values can be used to create a random amount of inputs,
	 * 	uniformly distributed between <tt>0</tt> and the absolute value given.
	 * 	For instance, a value of <tt>-3</tt> results in service profiles having
	 * 	<tt>n</tt> inputs, <tt>0 &le; n &le; 3</tt>.
	 * @param numberOfOutputs Number of outputs to create for each generated service.
	 * 	For instance, a value of <tt>5</tt> results in a service having five
	 * 	outputs. Negative values can be used to create a random amount of outputs,
	 * 	uniformly distributed between <tt>0</tt> and the absolute value given.
	 * 	For instance, a value of <tt>-3</tt> results in service profiles having
	 * 	<tt>n</tt> outputs, <tt>0 &le; n &le; 3</tt>.
	 * @param useSubModels If <code>true</code> each service will generated in
	 * 	its own ontology, which will be added to the {@link #getKnowledgeBase()
	 * 	target KB}. If <code>false</code> all services will be added to the
	 * 	{@link OWLKnowledgeBase#getBaseModel() base model} of the target KB.
	 * @param logProgress If <code>true</code> generation progress messages will
	 * 	be logged, whenever <tt>progress = n * 10%, 1 < n &le; 10</tt>.
	 * @throws AssertionError If <code>numberOfServices &le; 0</code>.
	 */
	public void generateAtomicServices(final URI baseURI, final int numberOfServices,
		final int numberOfInputs, final int numberOfOutputs, final boolean useSubModels, final boolean logProgress)
	{
		assert numberOfServices > 0 : "Number of services must be larger than zero";
		this.base = (baseURI != null)? baseURI : DEFAULT_BASE_URI;
		final float logThreshold = numberOfServices / 10.0f; // we want progress messages for n * 10%, 1<n<=10

		final long time = System.currentTimeMillis();
		OWLOntology ont;
		for (int i = 1; i <= numberOfServices; i++)
		{
			ont = getTargetOntology(useSubModels, Integer.toString(i));
			generate(i, numberOfInputs, numberOfOutputs, ont);
			logProgress(logProgress, "services", logThreshold, i, numberOfServices);
		}
		logNumbers(System.currentTimeMillis() - time);
//		kb.write(System.out, base);
	}

	/**
	 * Generate synthetic ABox assertions about the Service, Profile, Process,
	 * and Grounding of an atomic service. The KB is searched if it already
	 * contains an atomic service. If none was found a synthetic service will
	 * be created first using {@link #generateAtomicServices(URI, int, boolean, boolean)}.
	 * <p>
	 * Generated assertions are of the form <tt>(s,p,o)</tt>, where <tt>p</tt>
	 * is randomly chosen from a set of 20 newly created synthetic predicates;
	 * <tt>o</tt> is a new synthetic named individual which is an instance of a
	 * randomly selected class out of the named classes in the ontologies that
	 * have been loaded at instantiation time (see {@link #getOWLClasses()}; and
	 * <tt>s</tt> is the Service, Profile, AtomicProcess, the AtomicGrounding
	 * (WSDL or Java depending on what it actually is), and the MessageMap
	 * elements if it is a WSDL grounding, or the JavaParameter elements if it
	 * is a Java grounding.
	 *
	 * @param baseURI Base URI used as a prefix for named individuals. Defaults
	 * 	to {@link #DEFAULT_BASE_URI} if <code>null</code>.
	 * @param numberOfStatements The number of statements to generate.
	 * @param logProgress If <code>true</code> generation progress messages will
	 * 	be logged, whenever <tt>progress = n * 10%, 1 < n <= 10</tt>.
	 * @throws AssertionError If <code>numberOfStatements < 1</code>.
	 */
	public void generateStatements(final URI baseURI, final int numberOfStatements, final boolean logProgress)
	{
		assert numberOfStatements >= 1 : "Minimum number of statements is 1";
		this.base = (baseURI != null)? baseURI : DEFAULT_BASE_URI;
		final float logThreshold = numberOfStatements / 10.0f; // we want progress messages for n * 10%, 1<n<=10

		final long time = System.currentTimeMillis();

		// check if KB already contains a Process; we assume that it is atomic and has Service, Profile, Grounding
		OWLIndividualList<? extends Process> processes = kb.getProcesses(Process.ATOMIC, false);
		if (processes.size() < 1) // if no Service/Process exists yet, we simply generate a new synthetic one
		{
			generateAtomicServices(baseURI, 1, false, false);
			processes = kb.getProcesses(Process.ATOMIC, false);
		}

		final long initialNumberOfStatements = kb.size();
		if (numberOfStatements <= initialNumberOfStatements)
		{
			logger.info("No statements generated: Initial size of KB {} >= {}",
				initialNumberOfStatements, numberOfStatements);
			return;
		}

		// statements will be added about the following resources; selection of Process, Service, Profile,
		// and Grounding is deliberate since they are queried, e.g., in the course of execution
		final AtomicProcess process = (AtomicProcess) processes.get(0);
		final Service service = process.getService();
		final Profile profile = service.getProfile();
		final AtomicGrounding<?> grounding = process.getGrounding();

		// first, let's create some synthetic properties we will use as predicates when adding statements
		final OWLObjectProperty[] properties = new OWLObjectProperty[20];
		for (int i = 0; i < properties.length; i++)
		{
			properties[i] = kb.createObjectProperty(URIUtils.createURI(base, "objProp" + i));
		}

		// second, create a function like callable that will add statements to the grounding
		final Callable<Integer> callable;
		if (grounding instanceof JavaAtomicGrounding)
			callable = new JavaGroundingExtender((JavaAtomicGrounding) grounding, properties);
		else
			callable = new MessageMapBasedGroundingExtender(grounding, properties);


		// let's now create synthetic statements about existing resources, thus, enlarging the ABox
		long i = kb.size();
		while (i < numberOfStatements)
		{
			process.addProperty(properties[r.nextInt(properties.length)], kb.createIndividual(
				owlClasses.get(r.nextInt(owlClasses.size())).getURI(), URIUtils.createURI(base, "ind" + i++)));
			service.addProperty(properties[r.nextInt(properties.length)], kb.createIndividual(
				owlClasses.get(r.nextInt(owlClasses.size())).getURI(), URIUtils.createURI(base, "ind" + i++)));
			profile.addProperty(properties[r.nextInt(properties.length)], kb.createIndividual(
				owlClasses.get(r.nextInt(owlClasses.size())).getURI(), URIUtils.createURI(base, "ind" + i++)));
			grounding.addProperty(properties[r.nextInt(properties.length)], kb.createIndividual(
				owlClasses.get(r.nextInt(owlClasses.size())).getURI(), URIUtils.createURI(base, "ind" + i++)));

			// factor 2: one statement for property and one for rdf:type on newly created individual
			i += 4;

			try
			{
				i += callable.call();
			}
			catch (final Exception e) { /* ignore */ }

			logProgress(logProgress, "statements", logThreshold, i, numberOfStatements);
		}

		logNumbers(System.currentTimeMillis() - time);
//		kb.write(System.out, base);
	}

	/**
	 * @return Returns an unmodifiable view of all named OWL classes defined by
	 * 	the ontologies that have been loaded at instantiation time. The order
	 * 	of this list is undefined.
	 */
	public List<OWLClass> getOWLClasses()
	{
		return Collections.unmodifiableList(owlClasses);
	}

	/**
	 * @return The target KB to which generated services will be added.
	 */
	public OWLKnowledgeBase getKnowledgeBase()
	{
		return kb;
	}

	/**
	 * Demonstrates how to this class is to be used.
	 */
	public static void main(final String[] args) throws IOException
	{
		final ServiceGenerator gen = new ServiceGenerator(OWLFactory.createKB());

		final int numberOfServices = (args != null && args.length > 0)? Integer.parseInt(args[0]) : 1;
		logger.info("Generating {} service(s)", numberOfServices);

		gen.generateAtomicServices(null, numberOfServices, -3, -3, false, true);

		// optional: attach a reasoner and let it classify the entire KB (would fail if KB would be inconsistent)
		gen.getKnowledgeBase().setReasoner("Pellet");
		gen.getKnowledgeBase().classify();

		// let's inspect the KB (should contain LUBM, OWL-S ontologies, and the generated service)
		gen.getKnowledgeBase().write(System.out, null);
	}

	private void generate(final int i, final int in, final int out, final OWLOntology ont)
	{
		final Service service = ont.createService(URIUtils.createURI(base, "Service" + i));
		final Profile profile = ont.createProfile(URIUtils.createURI(base, "Profile" + i));
		final AtomicProcess process = ont.createAtomicProcess(URIUtils.createURI(base, "Process" + i));
		final WSDLGrounding groundg = ont.createWSDLGrounding(URIUtils.createURI(base, "Grounding" + i));
		final WSDLAtomicGrounding atgrdg = ont.createWSDLAtomicGrounding(
			URIUtils.createURI(base, "AtomicGrounding" + i));

		// service
		service.setName("Service" + i);
		service.addProfile(profile);
		service.setProcess(process);
		service.addGrounding(groundg);

		// grounding, atomic grounding
		groundg.addGrounding(atgrdg);
		atgrdg.setProcess(process);
		final URI wsdlBase = URI.create(base + "?wsdl");
		atgrdg.setWSDL(wsdlBase);

		final WSDLOperationRef operation = ont.createWSDLOperationRef(null); // anon
		operation.setOperation(URIUtils.createURI(wsdlBase, "operation" + i));
		operation.setPortType(URIUtils.createURI(wsdlBase, "port" + i));
		atgrdg.setOperationRef(operation);
		atgrdg.setInputMessage(URIUtils.createURI(wsdlBase, "request" + i));
		atgrdg.setOutputMessage(URIUtils.createURI(wsdlBase, "reply" + i));

		// textual description
		profile.setTextDescription("Profile" + i + " description");
		process.setLabel("Process" + i + " description", null);

		// inputs - added to profile, process, and grounding
		int n = (in < 0)? r.nextInt(Math.abs(in) + 1) : in, j;
		for (j = 1; j <= n; j++)
		{
			final Input input = ont.createInput(URIUtils.createURI(base, "Input" + j + "." + i));
			input.setParamType(owlClasses.get(r.nextInt(owlClasses.size())));
			profile.addInput(input);
			process.addInput(input);
			final MessageMap<String> msgMap = ont.createWSDLInputMessageMap(null); // anon
			msgMap.setGroundingParameter(wsdlBase + "#Input" + j + "." + i);
			msgMap.setOWLSParameter(input);
			atgrdg.addInputMap(msgMap);
		}


		// outputs - added to profile, process, and create a corresponding parameter in grounding
		n = (out < 0)? r.nextInt(Math.abs(out) + 1) : out;
		for (j = 1; j <= n; j++)
		{
			final Output output = ont.createOutput(URIUtils.createURI(base, "Output" + j + "." + i));
			output.setParamType(owlClasses.get(r.nextInt(owlClasses.size())));
			profile.addOutput(output);
			process.addOutput(output);
			final MessageMap<String> msgMap = ont.createWSDLOutputMessageMap(null); // anon
			msgMap.setGroundingParameter(wsdlBase + "#Output" + j + "." + i);
			msgMap.setOWLSParameter(output);
			atgrdg.addOutputMap(msgMap);
		}
	}

	private OWLOntology getTargetOntology(final boolean useSubModels, final String suffix)
	{
		if (useSubModels) return kb.createOntology(URI.create(base + "/" + suffix));
		return (OWLOntology) kb.getBaseModel();
	}

	private void logNumbers(final long time)
	{
		logger.info(new Formatter().format("Generation took %.1fs", time / 1000f).toString());
		logger.info(new Formatter().format("Final KB size   %d statements", kb.size()).toString());
		System.gc();
		logger.info(new Formatter().format(
			"Total memory    %.2fMB", Runtime.getRuntime().totalMemory() / 1024f / 1024f).toString());
	}

	private void logProgress(final boolean logProgress, final String what, final float threshold,
		final long current, final int maximum)
	{
		if (logProgress && (current % threshold < 1))
		{
			logger.info("{} {} generated; number of statements in KB {}", new Object[] {current, what,
				(maximum > 10000)? "-" : kb.size()}); // skip progress size counting - takes too much time
		}
	}

	private final class JavaGroundingExtender implements Callable<Integer>
	{
		private final OWLIndividualList<JavaParameter> inputs;
		private final JavaVariable output;
		private final OWLObjectProperty[] properties;
		private int i;

		JavaGroundingExtender(final JavaAtomicGrounding grounding, final OWLObjectProperty[] properties)
		{
			i = 0;
			this.properties = properties;
			this.inputs = grounding.getInputParameters();
			this.output = grounding.getOutput();
		}

		/* @see java.util.concurrent.Callable#call() */
		public Integer call()
		{
			for (final JavaParameter input : inputs)
			{
				input.addProperty(properties[r.nextInt(properties.length)], kb.createIndividual(
					owlClasses.get(r.nextInt(owlClasses.size())).getURI(), URIUtils.createURI(base, "ind" + i++)));
			}

			// factor 2: one statement for property and one for rdf:type on newly created individual
			int generatedStatements = 2 * inputs.size();

			if (output != null)
			{
				output.addProperty(properties[r.nextInt(properties.length)], kb.createIndividual(
					owlClasses.get(r.nextInt(owlClasses.size())).getURI(), URIUtils.createURI(base, "ind" + i++)));
				generatedStatements += 2;
			}

			return generatedStatements;
		}

	}

	private final class MessageMapBasedGroundingExtender implements Callable<Integer>
	{
		private final OWLIndividualList<? extends MessageMap<?>> inputMappings;
		private final OWLIndividualList<? extends MessageMap<?>> outputMappings;
		private final OWLObjectProperty[] properties;
		private int i;

		MessageMapBasedGroundingExtender(final AtomicGrounding<?> grounding, final OWLObjectProperty[] properties)
		{
			i = 0;
			this.inputMappings = grounding.getInputMappings();
			this.outputMappings = grounding.getInputMappings();
			this.properties = properties;
		}

		/* @see java.util.concurrent.Callable#call() */
		public Integer call()
		{
			for (final MessageMap<?> mm : inputMappings)
			{
				mm.addProperty(properties[r.nextInt(properties.length)], kb.createIndividual(
					owlClasses.get(r.nextInt(owlClasses.size())).getURI(), URIUtils.createURI(base, "ind" + i++)));
			}
			for (final MessageMap<?> mm : outputMappings)
			{
				mm.addProperty(properties[r.nextInt(properties.length)], kb.createIndividual(
					owlClasses.get(r.nextInt(owlClasses.size())).getURI(), URIUtils.createURI(base, "ind" + i++)));
			}

			// factor 2: one statement for property and one for rdf:type on newly created individual
			return 2 * (inputMappings.size() + outputMappings.size());
		}
	}
}
