// The MIT License
//
// Copyright (c) 2004 Evren Sirin
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to
// deal in the Software without restriction, including without limitation the
// rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
// sell copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
// IN THE SOFTWARE.
package org.mindswap.owls.validator;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mindswap.exceptions.CastingException;
import org.mindswap.exceptions.InvalidURIException;
import org.mindswap.owl.OWLFactory;
import org.mindswap.owl.OWLIndividualList;
import org.mindswap.owl.OWLKnowledgeBase;
import org.mindswap.owl.OWLOntology;
import org.mindswap.owls.expression.Condition;
import org.mindswap.owls.expression.Expression;
import org.mindswap.owls.grounding.AtomicGrounding;
import org.mindswap.owls.grounding.Grounding;
import org.mindswap.owls.grounding.JavaAtomicGrounding;
import org.mindswap.owls.grounding.MessageMap;
import org.mindswap.owls.grounding.WSDLAtomicGrounding;
import org.mindswap.owls.process.AnyOrder;
import org.mindswap.owls.process.AsProcess;
import org.mindswap.owls.process.AtomicProcess;
import org.mindswap.owls.process.Choice;
import org.mindswap.owls.process.CompositeProcess;
import org.mindswap.owls.process.ControlConstruct;
import org.mindswap.owls.process.ControlConstructVisitor;
import org.mindswap.owls.process.ForEach;
import org.mindswap.owls.process.IfThenElse;
import org.mindswap.owls.process.Perform;
import org.mindswap.owls.process.Process;
import org.mindswap.owls.process.Produce;
import org.mindswap.owls.process.RepeatUntil;
import org.mindswap.owls.process.RepeatWhile;
import org.mindswap.owls.process.Result;
import org.mindswap.owls.process.Sequence;
import org.mindswap.owls.process.Split;
import org.mindswap.owls.process.SplitJoin;
import org.mindswap.owls.process.variable.Input;
import org.mindswap.owls.process.variable.InputBinding;
import org.mindswap.owls.process.variable.Local;
import org.mindswap.owls.process.variable.Output;
import org.mindswap.owls.process.variable.OutputBinding;
import org.mindswap.owls.process.variable.ProcessVar;
import org.mindswap.owls.process.variable.ResultVar;
import org.mindswap.owls.profile.Profile;
import org.mindswap.owls.profile.ServiceCategory;
import org.mindswap.owls.profile.ServiceParameter;
import org.mindswap.owls.service.Service;
import org.mindswap.owls.vocabulary.OWLS;
import org.mindswap.utils.URIUtils;

/**
 * Implements validation of OWL-S services including their process, profile,
 * and grounding for missing elements, inconsistencies, or other incorrect
 * specifications.
 * <p>
 * The set of validation rules is currently hard coded by this class, i.e.,
 * there is no extension mechanism to plug-in new rules.
 * IMPROVE implement extensible (and rule based) validation approach.
 *
 * @author unascribed
 * @version $Rev: 2396 $; $Author: thorsten $; $Date: 2010-01-15 12:34:01 +0200 (Fri, 15 Jan 2010) $
 */
public class OWLSValidator
{
	/*
	what about validating preconditions?
	  is that necessary?

	warn about using depreciated terms?
	  how to find out which are depreciated heh

	--Process--

	Binding
	  1 toParam,
	  <= 1 valueSource, valueData, valueSpecifier

	ValueOf
	  1 theVar
	  <= 1 fromProcess

	CompositeProcess
	  1 composedOf
	  <= 1 invocable
	  <= 1 computedOutput
	  <= 1 computedInput
	  <= 1 computedEffect
	  <= 1 computedPrecondition

	everything in a ControlConstructBag/ControlConstructList should be of type ControlConstruct

	Sequence
	  1, components -> ControlConstructList

	Split
	  1, components -> ControlConstructBag

	Any-Order
	  1, components -> ControlConstructBag

	Choice
	  1, components -> ControlConstructBag

	If-Then-Else
	  1, ifCondition
	  1, then
	  <= 1, else

	Repeat-While
	  1, whileCondition
	  1, whileProcess -> ControlConstruct

	Repeat-Until
	  1, untilCondition
	  1, untilProcess -> ControlConstruct

	*/

	private static final int CODE_INVALID_VALUE = 0;
	private static final int CODE_MISSING_VALUE = 1;

	private Map<Service, Set<ValidationMessage>> mMessageMap;

	/**
	 * Query the given ontology for all OWL-S service instances and validate
	 * them according to the rules implemented by this class.
	 * <p>
	 * Note that a Pellet reasoner will be temporarily attached to the backing
	 * {@link OWLOntology#getKB() KB} if there was none before; it will be detached
	 * upon return.
	 *
	 * @param theOntology The ontology containing (either directly or by imported
	 * 	ontologies) the services to validate.
	 * @return A validation report about the service(s) found.
	 * @throws ValidationException See {@link ValidationException.Error} for the
	 * 	different kinds of events that may cause this exception.
	 */
	public ValidationReport validate(final OWLOntology theOntology) throws ValidationException
	{
		if (theOntology == null) throw ValidationException.createParameterException("OWL ontoloy null", null);

		OWLKnowledgeBase aKb = theOntology.getKB();
		boolean reasonerSet = false;
		if (aKb.getReasoner() == null) // if set already assume that it supports required DL features
		{
			aKb.setReasoner("Pellet");
			reasonerSet = true;
		}
		try
		{
			return validateKb(aKb);
		}
		finally // make sure to finally detach reasoner if there was none originally
		{
			if (reasonerSet) aKb.setReasoner(null);
		}
	}

	/**
	 * Read a OWL-S document from the given resource (in fact, the string is assumed
	 * to be a URL) and validate it according to the rules implemented by this class.
	 *
	 * @param theURI The location of the OWL-S document to validate.
	 * @return A validation report about the service(s) found in the document.
	 * @throws ValidationException See {@link ValidationException.Error} for the
	 * 	different kinds of events that may cause this exception.
	 */
	public ValidationReport validate(final String theURI) throws ValidationException
	{
		try
		{
			return validate(URIUtils.createURI(theURI));
		}
		catch (InvalidURIException e)
		{
			throw ValidationException.createParameterException("Invalid OWL-S document URI", e);
		}
	}

	/**
	 * Read a OWL-S document from the given resource (in fact, the URI is assumed
	 * to be a URL) and validate it according to the rules implemented by this class.
	 *
	 * @param theURI The location of the OWL-S document to validate.
	 * @return A validation report about the service(s) found in the document.
	 * @throws ValidationException See {@link ValidationException.Error} for the
	 * 	different kinds of events that may cause this exception.
	 */
	public ValidationReport validate(final URI theURI) throws ValidationException
	{
		final OWLKnowledgeBase aKb = OWLFactory.createKB();
		aKb.setReasoner("Pellet");

		try
		{
			aKb.read(theURI);
		}
		catch (final IOException ioe)
		{
			throw ValidationException.createIOException("Failed to read resource " + theURI + ".", ioe);
		}
		catch (final Exception ex)
		{
			throw ValidationException.createParseException("Failed to load OWL-S ontology.", ex);
		}

		return validateKb(aKb);
	}

	private ValidationReport validateKb(final OWLKnowledgeBase theKb) throws ValidationException
	{
		mMessageMap = new HashMap<Service, Set<ValidationMessage>>();

		if (!theKb.isConsistent())
		{
			throw ValidationException.createInconsistencyException("Inconsistent knowledge base.");
		}

		final OWLIndividualList<Service> aServiceList = theKb.getServices(false);
		for (Service aService : aServiceList)
		{
			validateService(aService);
		}
		return new ValidationReport(mMessageMap);
	}

	private void validateService(final Service theService)
	{
		// first of all, make sure that each service is considered initially valid
		mMessageMap.put(theService, new HashSet<ValidationMessage>());

		validateProfileForService(theService);

		validateProcessForService(theService);

		validateGroundingForService(theService);
	}

	private void validateProcessForService(final Service theService)
	{
		final OWLIndividualList<Process> processes = OWLFactory.wrapList(
			theService.getProperties(OWLS.Service.describedBy), Process.class);
		if (processes.size() > 1)
		{
			// invalid, can only have 0,1 describedBy
			final ValidationMessage msg = new ValidationMessage(CODE_INVALID_VALUE,
				"Cannot specify more than one describedBy for a service (" + theService.getLocalName() + ")");
			addMessage(theService,msg);
			return;
		}

		if (processes.isEmpty())
		{
//			final ValidationMessage msg = new ValidationMessage(CODE_MISSING_VALUE,
//				"No process specified for service: "+theService.getLocalName());
//			addMessage(theService, msg);
			return;
		}

		final Process theProcess;
		try
		{
			theProcess = processes.get(0);
		}
		catch (final CastingException ex)
		{
			final ValidationMessage msg = new ValidationMessage(CODE_INVALID_VALUE,
				"Process individual specified for service (" + theService.getLocalName() +
				") is not an instance of " + OWLS.Process.Process);
			addMessage(theService, msg);
			return;
		}


		try {
			// TODO: do processes really require profiles?, do they require to belong to a service?
			if (theProcess.getProfile() == null)
			{
				final ValidationMessage msg = new ValidationMessage(CODE_MISSING_VALUE,
					"No profile specified for process: "+theProcess.getLocalName());
				addMessage(theService, msg);
			}
		}
		catch (final CastingException ce) {
			addMessage(theService, CODE_INVALID_VALUE, ce.getMessage());
		}

		if (theProcess.getNames().size() > 1)
		{
			// process must have a name
			final ValidationMessage msg = new ValidationMessage(CODE_MISSING_VALUE,
				"Process '"+theProcess.getLocalName()+"' can specify at most one name");
			addMessage(theService, msg);
		}

//		ParameterList aParamList = aProcess.getParameters();
//		for (int i = 0; i < aParamList.size(); i++)
//		{
//		Parameter aParam = aParamList.parameterAt(i);
//		validateParameter(theService,aParam);
//		}

		final OWLIndividualList<Result> aResultList = theProcess.getResults();
		for (Result aResult : aResultList)
		{
			validateResult(theService, aResult);
		}

		final OWLIndividualList<Input> aInputList = theProcess.getInputs();
		for (Input aInput : aInputList)
		{
			validateInput(theService, aInput);
		}

		final OWLIndividualList<Output> aOutputList = theProcess.getOutputs();
		for (Output aOutput : aOutputList)
		{
			validateOutput(theService, aOutput);
		}

		if (theProcess.canCastTo(CompositeProcess.class))
			validateCompositeProcess(theProcess.castTo(CompositeProcess.class));
		else if (theProcess.canCastTo(AtomicProcess.class))
			validateAtomicProcess(theProcess.castTo(AtomicProcess.class));
		else
		{
			// TODO: is this an error?
				System.err.println("WTF!");
		}
	}

	private void validateLocal(final Service theService, final Local theLocal)
	{
		validateParameter(theService,theLocal);
	}

	private void validateInput(final Service theService, final Input theInput)
	{
		validateParameter(theService,theInput);
	}

	private void validateOutput(final Service theService, final Output theOutput)
	{
		validateParameter(theService,theOutput);
	}

	private void validateParameter(final Service theService, final ProcessVar theParam)
	{
		if (theParam.getParamType() == null)
		{
			final ValidationMessage msg = new ValidationMessage(CODE_MISSING_VALUE,
				"A paramType must be specified for ProcessVar: " + theParam.getLocalName());
			addMessage(theService,msg);
		}

		if (!theParam.getParamType().isDataType())
		{
			final ValidationMessage msg = new ValidationMessage(CODE_INVALID_VALUE,
				"The paramType for parameter: '"+theParam.getLocalName()+ "' is not specified properly. " +
				"It is supposed to be a datatype property, but it is specifed as an object property. " +
				"Please change the declaration.");
			addMessage(theService,msg);
		}

		// TODO what about getConstantValue() ??
	}

	private void validateAtomicProcess(final AtomicProcess theAtomicProcess)
	{
		// TODO:  is the AtomicGrounding required?
		// what else needs to be validated on an atomic process
	}

	private void validateCompositeProcess(final CompositeProcess theCompositeProcess)
	{
		final Service aService = theCompositeProcess.getService();

		if (theCompositeProcess.getComposedOf() == null)
		{
			final ValidationMessage msg = new ValidationMessage(CODE_MISSING_VALUE,"");
			addMessage(theCompositeProcess.getService(),msg);
		}

		final ControlConstruct aCC = theCompositeProcess.getComposedOf();
		final List<ControlConstruct> aList = aCC.getConstructs();
		for (ControlConstruct tempCC : aList)
		{
			validateControlConstruct(aService, tempCC);
		}

		try {
			final OWLIndividualList<Process> aProcessList = aCC.getAllProcesses(false);
			for (Process aProcess : aProcessList)
			{
				// TODO: validate each process?
				//System.err.println("proc: "+aProcess);
			}
		}
		catch (final CastingException ex) {
			addMessage(aService, CODE_INVALID_VALUE, ex.getMessage());
		}

		final OWLIndividualList<Local> aParamList = theCompositeProcess.getLocals();
		for (Local aLocal : aParamList)
		{
			validateLocal(aService, aLocal);
		}

		// maybe just verify that there are at least two processes for the composite
		// and that the perform and process list match up?
	}

	private void validateGroundingForService(final Service theService)
	{
		Grounding<?, ?> aGrounding = null;

		try {
			aGrounding = theService.getGrounding();
		}
		catch (final Exception ex) {
			ex.printStackTrace();
			// likely a cast exception, we'll ignore the exception, aGrounding will be null
			// and the below error message will be recorded.
		}

		if (aGrounding == null)
		{
			final ValidationMessage msg = new ValidationMessage(CODE_MISSING_VALUE,
				"No grounding specified for service: "+theService.getLocalName());
			addMessage(theService, msg);
			return;
		}

		if (aGrounding.getProperties(OWLS.Service.supportedBy).size() > 1)
		{
			// invalid, can only have 0,1 supportedBy
			final ValidationMessage msg = new ValidationMessage(CODE_INVALID_VALUE,
				"The grounding '"+aGrounding.getLocalName()+"' cannot have more than one supportedBy property");
			addMessage(theService, msg);
		}

		if (aGrounding.getService() == null)
		{
			final ValidationMessage msg = new ValidationMessage(CODE_MISSING_VALUE,
				"The grounding '"+aGrounding.getLocalName()+"' does not specify a service.");
			addMessage(theService, msg);
		}

		// TODO: is at least one grounding required?
		try {
			final OWLIndividualList<? extends AtomicGrounding<?>> aGroundingList = aGrounding.getAtomicGroundings();
			for (AtomicGrounding<?> aAtomicGrounding : aGroundingList)
			{
				if (aAtomicGrounding.getProcess() == null)
				{
					final ValidationMessage msg = new ValidationMessage(CODE_MISSING_VALUE,
						"The atomic grounding '"+aAtomicGrounding.getLocalName()+"' does not specify its process.");
					addMessage(theService, msg);
				}

				if (aAtomicGrounding.canCastTo(JavaAtomicGrounding.class))
				{
				}
				else if (aAtomicGrounding.canCastTo(WSDLAtomicGrounding.class))
				{
					validateWSDLAtomicGrounding(theService,aAtomicGrounding.castTo(WSDLAtomicGrounding.class));
				}
			}
		}
		catch (final CastingException ex)
		{
			addMessage(theService, CODE_INVALID_VALUE, ex.getMessage());
		}
	}

	private void validateWSDLAtomicGrounding(final Service theService, final WSDLAtomicGrounding theWSDLAtomicGrounding)
	{
		// todo:  are the wsdlInputMessage/wsdlOutputMessage required?  what about getWSDL()?
		if (theWSDLAtomicGrounding.getOperationRef() == null)
			addMessage(theService,CODE_INVALID_VALUE,"The grounding '"+theWSDLAtomicGrounding.getLocalName()+
				"' has a missing, or invalid operationRef.");
		else {
			if (theWSDLAtomicGrounding.getOperationRef().getPortType() == null)
				addMessage(theService, CODE_MISSING_VALUE,"The grounding '"+theWSDLAtomicGrounding.getLocalName()+
					"' must specify a portType for its operationRef.");

			if (theWSDLAtomicGrounding.getOperationRef().getOperation() == null)
				addMessage(theService, CODE_MISSING_VALUE,"The grounding '"+theWSDLAtomicGrounding.getLocalName()+
					"' must specify an operation for its operationRef");
		}

		// TODO: are you required to have input and output maps?

		final OWLIndividualList<? extends MessageMap<String>> inputMap = theWSDLAtomicGrounding.getInputMappings();
		for (final MessageMap<String> aMap : inputMap)
		{
			if (aMap.getGroundingParameter() == null)
				addMessage(theService, CODE_MISSING_VALUE, "The input map for grounding '" +
					theWSDLAtomicGrounding.getLocalName()+"' requires a grounding parameter (wsdlMessagePart).");

			if (aMap.getOWLSParameter() == null && aMap.getTransformation() == null)
				addMessage(theService, CODE_MISSING_VALUE, "The input map for grounding '" +
					theWSDLAtomicGrounding.getLocalName()+"' must specify either an owlsParameter or an xlstTransformation.");
		}

		final OWLIndividualList<? extends MessageMap<String>> outputMap = theWSDLAtomicGrounding.getOutputMappings();
		for (final MessageMap<String> aMap : outputMap)
		{
			if (aMap.getOWLSParameter() == null)
				addMessage(theService, CODE_MISSING_VALUE, "The output map for grounding '" +
					theWSDLAtomicGrounding.getLocalName()+"' requires an owlsParameter.");

			if (aMap.getGroundingParameter() == null && aMap.getTransformation() == null)
				addMessage(theService, CODE_MISSING_VALUE, "The output map for grounding '" +
					theWSDLAtomicGrounding.getLocalName()+
					"' must specify either an grounding parameter (wsdlMessagePart) or an xlstTransformation.");
		}
	}

	void validateSplitJoin(final Service service, final SplitJoin theSplitJoin)
	{
		final OWLIndividualList<ControlConstruct> ccBag = theSplitJoin.getConstructs();
		for (ControlConstruct cc : ccBag)
		{
			validateControlConstruct(service, cc);
		}
	}

	private void validateControlConstruct(final Service service, final ControlConstruct cc)
	{
		cc.accept(new ControlConstructVisitor() {

			public void visit(final SplitJoin sj) { validateSplitJoin(service, sj);	}

			public void visit(final Split st)
			{
				// TODO implement
			}

			public void visit(final org.mindswap.owls.process.Set set)
			{
				// TODO implement
			}

			public void visit(final Sequence sq)
			{
				// TODO implement
			}

			public void visit(final RepeatWhile rw)
			{
				// TODO implement
			}

			public void visit(final RepeatUntil ru)
			{
				// TODO implement
			}

			public void visit(final Produce pr)
			{
				// TODO implement
			}

			public void visit(final Perform pf)	{ validatePerform(service, pf); }

			public void visit(final IfThenElse ite)
			{
				// TODO implement
			}

			public void visit(final ForEach fe)
			{
				// TODO implement
			}

			public void visit(final Choice ch)
			{
				// TODO implement
			}

			public void visit(final AsProcess ap)
			{
				// TODO implement
			}

			public void visit(final AnyOrder ao)
			{
				// TODO implement
			}
		});

	}

	private void validateResult(final Service theService, final Result theResult)
	{
		final OWLIndividualList<ResultVar> resultVars = theResult.getResultVars();
		for (ResultVar resultVar : resultVars)
		{
			validateParameter(theService,resultVar);
		}

		final OWLIndividualList<Expression> effects = theResult.getEffects();
		for (Expression<?> effect : effects)
		{
			// TODO result effects
		}

		final OWLIndividualList<Condition> conditions = theResult.getConditions();
		for (Condition<?> condition : conditions)
		{
			// TODO result conditions
		}

		final OWLIndividualList<OutputBinding> aBindingList = theResult.getBindings();
		for (final OutputBinding outputBinding : aBindingList)
		{
			if (outputBinding.getProcessVar() == null) addMessage(theService, CODE_MISSING_VALUE,
				"Result '" + theResult.getLocalName() + "' must specify an Output for its toParam property.");
		}
	}

	void validatePerform(final Service theService, final Perform thePerform)
	{
		try {
			if (thePerform.getProcess() == null)
			{
				final ValidationMessage msg = new ValidationMessage(CODE_MISSING_VALUE,
					"Perform '"+thePerform.getLocalName()+"' must specify a process");
				addMessage(theService, msg);
			}
			//else validateProcess(theService, thePerform.getProcess());
		}
		catch (final CastingException ex) {
			addMessage(theService,CODE_INVALID_VALUE,ex.getMessage());
		}
		catch (final Exception e) {
			addMessage(theService,CODE_INVALID_VALUE,e.getMessage());
		}

		final OWLIndividualList<InputBinding> aBindingList = thePerform.getBindings();
		for (final InputBinding inputBinding : aBindingList)
		{
			try {
				if (inputBinding.getProcessVar() == null) addMessage(theService, CODE_MISSING_VALUE,
					"Perform '"+thePerform.getLocalName()+"' must specify an Input for the toParam property.");
			}
			catch (final CastingException ex) {
				addMessage(theService,CODE_INVALID_VALUE,ex.getMessage());
			}
			catch (final Exception e) {
				addMessage(theService,CODE_INVALID_VALUE,e.getMessage());
			}
		}

		// TODO:  is there any way to validate the valueSource for the perform?
	}

	private void validateProfileForService(final Service theService)
	{
		Profile aProfile = null;

		try {
			aProfile = theService.getProfile();
		}
		catch (final Exception ex) {
			// cast exception most likely, let the null check below handle the error message
		}

		if (aProfile == null)
		{
			final ValidationMessage msg = new ValidationMessage(CODE_MISSING_VALUE,
				"No profile specified for service: "+theService.getLocalName());
			addMessage(theService, msg);
			return;
		}

		if (aProfile.getService() == null)
		{
			final ValidationMessage msg = new ValidationMessage(CODE_MISSING_VALUE,
				"No service specified for profile: " + aProfile.getLocalName() +
				"; double check the presentedBy property on the profile to make sure its correctly specified");
			addMessage(theService, msg);
		}

		if (aProfile.getProperties(OWLS.Profile.serviceName).size() != 1)
		{
			// invalid, must have a service name
			final ValidationMessage msg = new ValidationMessage(CODE_MISSING_VALUE,
				"The profile '"+aProfile.getLocalName()+"' must specify only one serviceName. ");
			addMessage(theService, msg);
		}

		if (aProfile.getProperties(OWLS.Profile.textDescription).size() != 1)
		{
			// invalid, must have a text description
			final ValidationMessage msg = new ValidationMessage(CODE_MISSING_VALUE,
				"The profile '"+aProfile.getLocalName()+"' must specify only one textDescription.");
			addMessage(theService, msg);
		}

		if (!aProfile.getInputs().equals(theService.getProcess().getInputs()))
		{
			// profile inputs should match process inputs
			final ValidationMessage msg = new ValidationMessage(CODE_INVALID_VALUE,
				"Profile inputs for service '"+theService.getLocalName()+"' do not match the process inputs!");
			addMessage(theService, msg);
		}

		if (!aProfile.getOutputs().equals(theService.getProcess().getOutputs()))
		{
			// profile outputs should match process outputs
			final ValidationMessage msg = new ValidationMessage(CODE_INVALID_VALUE,
				"Profile outputs for service '"+theService.getLocalName()+"' do not match the process outputs!");
			addMessage(theService, msg);
		}

		OWLIndividualList<ServiceParameter> serviceParameters = aProfile.getServiceParameters();
		for (ServiceParameter serviceParameter : serviceParameters)
		{
			validateServiceParameter(theService, serviceParameter);
		}

		OWLIndividualList<ServiceCategory> serviceCategories = aProfile.getCategories();
		for (ServiceCategory serviceCategory : serviceCategories)
		{
			validateServiceCategory(theService, serviceCategory);
		}

		try {
			if (aProfile.getProcess() == null)
			{
				final ValidationMessage msg = new ValidationMessage(CODE_MISSING_VALUE,
					"Profile '"+aProfile.getLocalName()+"' do not specify a process!");
				addMessage(theService, msg);
			}
		}
		catch (final CastingException ex) {
			addMessage(theService, CODE_INVALID_VALUE, ex.getMessage());
		}
		catch (final NullPointerException npe) {
			// we'll do nothing in this case for now, the above check should catch this problem
			// you'll get an NPE here if the service for this profile is not properly specified.
			// there is a check above that will print a msg for this, so I think we can
			// ignore it here
		}
		catch (final Exception e) {
			addMessage(theService,CODE_INVALID_VALUE,e.getMessage());
		}

		final OWLIndividualList<Result> aResultList = aProfile.getResults();
		for (Result aResult : aResultList)
		{
			validateResult(theService, aResult);
		}

		// what about Classification, Products??
	}

	private void validateServiceParameter(final Service theService, final ServiceParameter theServiceParameter)
	{
		if (theServiceParameter.getName() == null)
		{
			final ValidationMessage msg = new ValidationMessage(CODE_MISSING_VALUE,
				"Must specify a name for serviceParameter: "+theServiceParameter.getLocalName());
			addMessage(theService, msg);
		}

		if (theServiceParameter.getParameter() == null)
		{
			final ValidationMessage msg = new ValidationMessage(CODE_MISSING_VALUE,
				"Must specify a parameter for serviceParameter: "+theServiceParameter.getLocalName());
			addMessage(theService, msg);
		}
	}

	private void validateServiceCategory(final Service theService, final ServiceCategory theServiceCategory)
	{
		if (theServiceCategory.getCode() == null)
		{
			final ValidationMessage msg = new ValidationMessage(CODE_MISSING_VALUE,
				"Must specify a code for serviceCategory: "+theServiceCategory.getLocalName());
			addMessage(theService, msg);
		}

		if (theServiceCategory.getValue() == null)
		{
			final ValidationMessage msg = new ValidationMessage(CODE_MISSING_VALUE,
				"Must specify a value for serviceCategory "+theServiceCategory.getLocalName());
			addMessage(theService, msg);
		}

		if (theServiceCategory.getTaxonomy() == null)
		{
			final ValidationMessage msg = new ValidationMessage(CODE_MISSING_VALUE,
				"Must specify a taxonomy for serviceCategory: "+theServiceCategory.getLocalName());
			addMessage(theService, msg);
		}

		if (theServiceCategory.getName() == null)
		{
			final ValidationMessage msg = new ValidationMessage(CODE_MISSING_VALUE,
				"Must specify a name for serviceCategory: "+theServiceCategory.getLocalName());
			addMessage(theService, msg);
		}
	}

	private void addMessage(final Service theService, final int theCode, final String theMsg)
	{
		addMessage(theService,new ValidationMessage(theCode,theMsg));
	}

	private void addMessage(final Service theService, final ValidationMessage theMsg)
	{
		Set<ValidationMessage> aSet = mMessageMap.get(theService);
		if (aSet == null) aSet = new HashSet<ValidationMessage>();
		aSet.add(theMsg);

		mMessageMap.put(theService, aSet);
	}

	public static void main(final String[] args)
	{
		System.err.println("validator main");

//		OWLSValidator aValidator = new OWLSValidator();

		try {

//			OWLKnowledgeBase kb = OWLFactory.createKB();
//			kb.setReasoner("RDFS");
//			Service aService = kb.readService("http://bai-hu.ethz.ch/next/ont/comp/NMRCompSampleSetup.owl");
//			aService.removeProperty(org.mindswap.owls.vocabulary.OWLS_1_1.Service.supports,aService.getGrounding());
//			System.err.println(aService.getProperty(org.mindswap.owls.vocabulary.OWLS_1_1.Service.supports));
//			System.err.println(aService.getGrounding());

			//new OWLSValidator().validate("http://bai-hu.ethz.ch/next/ont/comp/NMRCompSampleSetup.owl").print(System.err);

//			org.mindswap.owl.OWLConfig.setStrictConversion(false);
			//new OWLSValidator().validate(new java.io.File("BookPrice.owl").toURL().toExternalForm().replaceAll(" ","%20")).print(System.err);
			//new OWLSValidator().validate("http://www.kellyjoe.com/services/commissioning/CommissioningService.owl").print(System.err);
			new OWLSValidator().validate(new File("composite.owl").toURI()).print(System.err);

			// this code illustrates the cast exception that arises when getting the service parameters for a profile
//			String aURI = "http://www.mindswap.org/2004/owl-s/1.1/sensor/GetData2.owl";
//			OWLKnowledgeBase kb = OWLFactory.createKB();
//			kb.setReasoner("Pellet");
//			Service aService = kb.readService(aURI);
//			OWLIndividualList aList = aService.getProfile().getServiceParameters();
//			for (int i = 0; i < aList.size(); i++)
//			System.err.println(aList.individualAt(i));

			//String aURI = "http://www.mindswap.org/2004/owl-s/1.1/sensor/GetData2.owl";
			//String aURI = "http://www.mindswap.org/2004/owl-s/1.1/BNPrice.owl";
			//String aURI = "http://www.mindswap.org/2004/owl-s/1.1/BookPrice.owl";
			//String aURI = "http://www.mindswap.org/2004/owl-s/1.1/AmazonBookPrice.owl";
			//String aURI = "http://www.mindswap.org/2004/owl-s/1.1/BookFinder.owl";
			//String aURI = "http://www.mindswap.org/2004/owl-s/1.1/BabelFishTranslator.owl";
//			String aURI = "http://www.mindswap.org/2004/owl-s/1.1/CheaperBookFinder.owl";
			//String aURI = "http://www.mindswap.org/2004/owl-s/1.1/Dictionary.owl";
			//String aURI = "http://www.mindswap.org/2004/owl-s/1.1/CurrencyConverter.owl";
//			String aURI = "http://www.mindswap.org/2004/owl-s/1.1/FindCheaperBook.owl";
			//String aURI = "http://www.mindswap.org/2004/owl-s/1.1/FindLatLong.owl";
//			String aURI = "http://www.mindswap.org/2004/owl-s/1.1/FrenchDictionary.owl";
			//String aURI = "http://www.mindswap.org/2004/owl-s/1.1/ZipCodeFinder.owl";

//			ValidationReport aReport = aValidator.validate(aURI);
//			aReport.print(System.err);
		}
		catch (final Exception ex) {
			ex.printStackTrace();
		}
	}
}
