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
package org.universAAL.sieg.simplifiedAPIs.profileModel;

import org.universAAL.middleware.service.owl.InitialServiceDialog;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;

public abstract class StartUIServiceProfileModel implements ServiceProfileModel {


	protected String serviceClassURI;
	protected String vendor = "universAAL";
	protected String description = "";
	protected String serviceStartURI;

	public ServiceProfile getServiceProfile() {
		return InitialServiceDialog.createInitialDialogProfile(
				serviceClassURI,
				vendor,
				description,
				serviceStartURI);
	}

}
