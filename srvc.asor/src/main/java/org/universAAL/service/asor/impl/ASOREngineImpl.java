/**
 * 
 *  OCO Source Materials 
 *      Copyright IBM Corp. 2012 
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
package org.universAAL.service.asor.impl;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.mindswap.exceptions.ExecutionException;
import org.mindswap.owl.OWLDataValue;
import org.mindswap.owl.OWLFactory;
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
import org.universAAL.service.asor.impl.osgi.ASOREngineException;
import org.universAAL.service.asor.impl.osgi.IASOREngine;


/**
 * 
 *  @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 *	
 *  Aug 12, 2012
 *
 */
public class ASOREngineImpl implements IASOREngine {

	public Map<String, Object> startProcess(String serviceURI, Map<String, Object> inputs, String[] outputs) 
		throws ASOREngineException {
		
		// Prepare the engine + the process
		final OWLKnowledgeBase kb = OWLFactory.createKB();
		kb.setReasoner("Pellet");
		URI uri = null;
		Service service = null;
		try {
			uri = URIUtils.createURI(serviceURI);
			service = kb.readService(uri);
		} catch (IOException e) {
			throw new ASOREngineException("Unable to read service from serviceURI [" + serviceURI + "]; Error [" + 
					e.getMessage() + "]");
		}
		
		final Process process = service.getProcess();
		final ProcessExecutionEngine exec = OWLSFactory.createExecutionEngine();
//		exec.setCaching(true);

		// Populate the input parameters
		ValueMap<Input, OWLValue> owlsInputs = populateInputs(inputs, kb, process);
		
		// Execute the script
		ValueMap<Output, OWLValue> owlsOutputs;
		try {
			owlsOutputs = exec.execute(process, owlsInputs, kb);
		} catch (ExecutionException e) {
			throw new ASOREngineException("Error when executing the process for serviceURI [" + serviceURI + 
					"]; Error[" + e.getMessage() + "]");
		}
		
		Map<String, Object> outputsResult = populateOutputs(outputs, process, owlsOutputs);
		
		return outputsResult;
	}

	private ValueMap<Input, OWLValue> populateInputs(
			Map<String, Object> inputs, final OWLKnowledgeBase kb,
			final Process process) {
		ValueMap<Input, OWLValue> owlsInputs = new ValueMap<Input, OWLValue>();
		
		// Iterate over the given inputs and create for each one owlInput
		if (null != inputs && !inputs.isEmpty()) {
			for (String inputKey : inputs.keySet()) {
				Object value = inputs.get(inputKey);
				final Input owlInput = process.getInput(inputKey);
				owlsInputs.setValue(owlInput, kb.createDataValue(value));
			}
		}
		return owlsInputs;
	}
	
	private Map<String, Object> populateOutputs(String[] outputs,
			final Process process, ValueMap<Output, OWLValue> owlsOutputs) {
		// Populate the outputs result
		Map<String, Object> outputsResult = null;
		
		if (null != outputs && outputs.length > 0) {
			outputsResult = new HashMap<String, Object>();
			
			// Iterate over the required outputs
			for (String outputKey : outputs) {
				// Extract the output value
				final Output output = process.getOutput(outputKey);
				OWLValue outputValue = owlsOutputs.getValue(output);
				OWLDataValue outputDataValue = outputValue.castTo(OWLDataValue.class);
				outputsResult.put(outputKey, outputDataValue.getValue());
			}
		}
		return outputsResult;
	}

}
