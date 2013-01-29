/**
 * 
 *  OCO Source Materials 
 *      © Copyright IBM Corp. 2012 
 *
 *      See the NOTICE file distributed with this work for additional 
 *      information regarding copyright ownership 
 *       
 *      Licensed under the Apache License, Version 2.0 (the "License"); 
 *      you may not use this file except in compliance with the License. 
 *      You may obtain a copy of the License at 
 *       	http://www.apache.org/licenses/LICENSE-2.0 
 *       
 *      Unless required by applicable law or agreed to in writing, software 
 *      distributed under the License is distributed on an "AS IS" BASIS, 
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *      See the License for the specific language governing permissions and 
 *      limitations under the License. 
 *
 */
package org.universAAL.service.asor.owls.extensions;

import impl.jena.OWLIndividualImpl;
import impl.owls.process.constructs.ControlConstructImpl;

import java.net.URI;
import java.util.List;

import org.mindswap.exceptions.CastingException;
import org.mindswap.exceptions.ExecutionException;
import org.mindswap.owl.OWLClass;
import org.mindswap.owl.OWLFactory;
import org.mindswap.owl.OWLIndividual;
import org.mindswap.owl.OWLIndividualList;
import org.mindswap.owl.OWLObject;
import org.mindswap.owl.OWLObjectConverter;
import org.mindswap.owl.OWLValue;
import org.mindswap.owls.process.ControlConstruct;
import org.mindswap.owls.process.ControlConstructVisitor;
import org.mindswap.owls.process.Process;
import org.mindswap.owls.process.execution.ExecutionContext;
import org.mindswap.owls.process.execution.ExecutionSupport;
import org.mindswap.owls.process.variable.Output;
import org.mindswap.query.ValueMap;
import org.mindswap.utils.URIUtils;
import org.universAAL.middleware.service.ServiceCaller;
import org.universAAL.middleware.service.ServiceRequest;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.process.ProcessOutput;
import org.universAAL.service.asor.impl.ASORServiceMngr;
import org.universAAL.service.asor.impl.osgi.ASORModule;
import org.universAAL.service.asor.impl.parsers.JenaModelParser;
import org.universAAL.service.asor.impl.parsers.ProcessOutputParser;
import org.universAAL.service.asor.impl.servicecaller.RequestsMngr;

import com.hp.hpl.jena.ontology.OntModel;



/**
 * 
 *  @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 *	
 *  Aug 16, 2012
 *
 */
public class ASORCallService extends ControlConstructImpl<ASORCallService> {

	private static final URI uri = URI.create("http://ontology.universAAL.org/ASOR.owl#");

	private static final String constructName = "ASOR:CallService";
	
	public ASORCallService(final OWLIndividual ind) {
		super(ind);
		thiz = this;
		
	}

	@Override
	protected void doPrepare(ExecutionContext context) {
	}

	public <C extends ExecutionContext> void execute(C context,
			ExecutionSupport<C> target) throws ExecutionException {
		
		ASORServiceMngr asorServiceMngr = ASORServiceMngr.getInstance();
		// Extract the service request content as string
		String serviceRequestAsStr = getServiceRequestAsString(context);

		// De-serialize into ServiceRequest
		ServiceRequest sr = 
			(ServiceRequest) asorServiceMngr.getMessageContentSerializer().deserialize(serviceRequestAsStr);

		ServiceCaller serviceCaller = asorServiceMngr.getServiceCaller(ASORModule.tempServiceCallerKey);
		
		// send the request
		ServiceResponse serviceResponse = RequestsMngr.getInstance().sendRequest(serviceCaller, sr);
		
		// Populate the context with the outputs - iterate over the outputs
		ValueMap<Output, OWLValue> map = new ValueMap<Output, OWLValue>();
		List outputsList = serviceResponse.getOutputs();
		if (null != outputsList) {
			for (Object obj : outputsList) {
				ProcessOutput po = (ProcessOutput)obj;

				// Serialize the output to string 
				String resourceAsStr = ProcessOutputParser.extractParameterValue(po);

				// The output URI
				String outputURI = po.getURI();

				// Create the mode
				OntModel model = JenaModelParser.createModel(resourceAsStr);

				// Create the OwlValue
				JenaModelParser.addOutputToMap(model, this.getKB(), outputURI, map);
			}
		}
		
		context.addOutputs(map, false);
	}
	

	public OWLIndividualList<Process> getAllProcesses(boolean recursive) {
		return null;
	}

	public String getConstructName() {
		return constructName;
	}

	public OWLIndividualList<ControlConstruct> getConstructs() {
		return null;
	}

	public void accept(ControlConstructVisitor visitor) {
	}
	
	public static class ServiceCallerConverter implements OWLObjectConverter<ASORCallService> {
		// the OWL class defined for extended services
		private static final OWLClass asorCallService = OWLFactory.createKB().createClass(
			URIUtils.createURI(uri, "CallService"));

		public boolean canCast(final OWLObject object, final boolean strictConversion) {
			return (object instanceof OWLIndividual) &&
				((OWLIndividual) object).isType(asorCallService);
		}

		public ASORCallService cast(final OWLObject object, final boolean strictConversion) {
			if (canCast(object, strictConversion)) {

				ASORCallService ser = new ASORCallService((OWLIndividual) object);
				//ASORServiceMngr.getInstance().addServiceCaller(ser.getOntology().getURI(), serviceCaller)
				
				return ser;
			}
			
			throw CastingException.create(object, ASORCallService.class);
		}
	}
	
	public <C extends ExecutionContext> String getServiceRequestAsString(C context) {
		String serviceRequestAsString = "";
		
		OWLIndividual ind = getPropertyAsIndividual(
				URIUtils.createURI(
						uri + "serviceRequest"));
		if (null != ind) {
			serviceRequestAsString = TurtleRepresentationsBridge.toTurtle((OWLIndividualImpl) ind, context);
		}
		return serviceRequestAsString;
	}
}
