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

import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;

/**
 * Classes that implement this Interface will realize one (and only one)
 * {@link ServiceProfile}. This class helps manage {@link ServiceProfile}
 * independently, and if needed, hierarchically.
 *
 * @author amedrano
 * @see DefaultServiceCallee
 */
public interface ServiceProfileModel {

	/**
	 * This method will define the {@link ServiceProfile} to which the
	 * implementation class will respond.
	 *
	 * @return a well formed {@link ServiceProfile} for the implemented service.
	 */
	public ServiceProfile getServiceProfile();

	/**
	 * This method acts as a callback for the {@link ServiceProfile} defined in
	 * {@link ServiceProfileModel#getServiceProfile()}. There is no need to
	 * check the coincidence as this will be done by
	 * {@link DefaultServiceCallee}
	 *
	 * @param call
	 *            the {@link ServiceCall} which contains all the input
	 *            parameters needed for the {@link ServiceProfile}
	 * @return a {@link ServiceResponse} that contains all the output parameters
	 *         	  as weel as the {@link CallStatus} and/or specific error.
	 */
	public ServiceResponse handleCall(ServiceCall call);

}
