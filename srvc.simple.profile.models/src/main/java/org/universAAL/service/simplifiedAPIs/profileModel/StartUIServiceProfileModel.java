/*******************************************************************************
 * Copyright 2011 Universidad Politécnica de Madrid
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.universAAL.service.simplifiedAPIs.profileModel;

import org.universAAL.middleware.service.owl.InitialServiceDialog;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;

/**
 * This Class helps create a {@link ServiceProfileModel} for Services that create a UI Form.
 *<br><br>
 *Usage: <br>
 *<pre><code>import java.util.Locale;

import org.universAAL.middleware.owl.supply.LevelRating;
import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.ui.rdf.Form;
import org.universAAL.middleware.ui.UIRequest;
import org.universAAL.middleware.ui.owl.PrivacyLevel;
import org.universAAL.samples.lighting.uiclient.SharedResources;

public class Sample extends StartUIServiceProfileModel {

	Sample(){
		super("serviceClassURI", "vendor", "description", "startServiceURI");
	}
	
	public ServiceResponse handleCall(ServiceCall call) {
		Form mainDialog = new Form()
		UICaller uiCaller = new UICaller();
		
		// add your FormControl s
		// ...
		
		// build the UIRequest
		UIRequest out = new UIRequest(User, mainDialog,
				LevelRating.middle, Locale.ENGLISH, PrivacyLevel.insensible);
		// send the UIRequest
		uiCallse.sendUIRequest(out);
		return new ServiceResponse(CallStatus.succeeded);;
	}

}</code><pre><br>
 *  
 *  @author amedrano
 */
public abstract class StartUIServiceProfileModel implements ServiceProfileModel {

	private ServiceProfile profile;

	/**
	 * Creates a {@link ServiceProfileModel} who's profile is  the response to the 
	 * call to  
	 * {@link InitialServiceDialog#createInitialDialogProfile(String, String, String, String)}
	 * @param serviceClassURI 1st parameter of the call.
	 * @param vendor 1st parameter of the call.
	 * @param description 1st parameter of the call.
	 * @param startServiceURI 1st parameter of the call.
	 * 
	 * @see InitialServiceDialog#createInitialDialogProfile(String, String, String, String) is "the call" 
	 * referenced adobe.
	 */
	public StartUIServiceProfileModel(String serviceClassURI,
            String vendor,
            String description,
            String startServiceURI) {
		
		profile = InitialServiceDialog.createInitialDialogProfile(
				serviceClassURI,
				vendor,
				description,
				startServiceURI);
	}
	

	/**
	 * Returns the Created a service profile in the constructor call to  
	 * {@link InitialServiceDialog#createInitialDialogProfile(String, String, String, String)}
	 * 
	 * @see InitialServiceDialog
	 */
	public final ServiceProfile getServiceProfile() {
		return profile;
	}

}
