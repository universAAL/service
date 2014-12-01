/*	
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute for Computer Graphics Research
	
	See the NOTICE file distributed with this work for additional 
	information regarding copyright ownership
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	  http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */
package org.universAAL.service.asor;

import java.util.ArrayList;
import java.util.List;

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceCallee;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.ontology.asor.Asor;
import org.universAAL.ontology.asor.Script;
import org.universAAL.ontology.asor.ScriptEngine;

public class AsorIntegration extends ServiceCallee {

    private AsorProvider as;
    private static int cntScript = 0;

    public static final String NAMESPACE = "http://ontology.universAAL.org/AsorIntegration.owl#";
    static final String SERVICE_GET_ENGINES = NAMESPACE + "getEngines";
    static final String SERVICE_GET_SCRIPTS = NAMESPACE + "getScripts";
    static final String SERVICE_ADD_SCRIPT = NAMESPACE + "addScript";
    // static final String SERVICE_ADD_SCRIPT_PERS = NAMESPACE +
    // "addScriptPers";
    // static final String SERVICE_ADD_SCRIPT_TEMP = NAMESPACE +
    // "addScriptTemp";
    static final String SERVICE_REMOVE_SCRIPT = NAMESPACE + "removeScript";
    // static final String SERVICE_ACTIVATE_SCRIPT = NAMESPACE +
    // "activateScript";

    static final String INPUT_SCRIPT = NAMESPACE + "script";
    static final String INPUT_SCRIPT_URI = NAMESPACE + "scriptURI";
    static final String INPUT_CONTENT = NAMESPACE + "content";
    static final String INPUT_IS_PERSISTENT = NAMESPACE + "isPersistent";

    static final String OUTPUT_ENGINES = NAMESPACE + "outputEngines";
    static final String OUTPUT_ENGINE_NAME = NAMESPACE + "outputEngineName";
    static final String OUTPUT_ENGINE_VERSION = NAMESPACE
	    + "outputEngineVersion";
    static final String OUTPUT_ENGINE_LANG_VER = NAMESPACE
	    + "outputEngineLanguageVersion";
    static final String OUTPUT_ENGINE_MIMETYPES = NAMESPACE
	    + "outputEngineMimeTypes";
    static final String OUTPUT_ENGINE_FILE_EXTENSIONS = NAMESPACE
	    + "outputEngineFileExtensions";
    static final String OUTPUT_ENGINE_LANGS = NAMESPACE
	    + "outputEngineLanguages";
    static final String OUTPUT_SCRIPTS = NAMESPACE + "outputScripts";

    protected AsorIntegration(AsorProvider as) {
	super(AsorActivator.mc, createProfiles());
	this.as = as;
    }

    private static ServiceProfile[] createProfiles() {
	List<ServiceProfile> profiles = new ArrayList<ServiceProfile>(20);

	String[] ppControls = new String[] { Asor.PROP_CONTROLS };
	String[] ppSupports = new String[] { Asor.PROP_SUPPORTS };
	// String[] ppSuppLang = new String[] { Asor.PROP_SUPPORTS,
	// ScriptEngine.PROP_LANGUAGE_CLASSIFIER };

	// ------
	Asor getEngines = new Asor(SERVICE_GET_ENGINES);
	getEngines.addOutput(OUTPUT_ENGINES, ScriptEngine.MY_URI, 0, -1,
		ppSupports);
	// getEngines.addOutput(OUTPUT_ENGINE_NAME,
	// TypeMapper.getDatatypeURI(String.class), 0, 1, new String[] {
	// Asor.PROP_SUPPORTS, ScriptEngine.PROP_NAME });
	// getEngines.addOutput(OUTPUT_ENGINE_VERSION,
	// TypeMapper.getDatatypeURI(String.class), 0, 1, new String[] {
	// Asor.PROP_SUPPORTS, ScriptEngine.PROP_VERSION });
	// getEngines.addOutput(OUTPUT_ENGINE_LANG_VER,
	// TypeMapper.getDatatypeURI(String.class), 0, 1,
	// new String[] { Asor.PROP_SUPPORTS,
	// ScriptEngine.PROP_LANGUAGE_VERSION });
	// getEngines.addOutput(OUTPUT_ENGINE_MIMETYPES,
	// TypeMapper.getDatatypeURI(String.class), 0, -1, new String[] {
	// Asor.PROP_SUPPORTS, ScriptEngine.PROP_MIME_TYPES });
	// getEngines
	// .addOutput(OUTPUT_ENGINE_FILE_EXTENSIONS,
	// TypeMapper.getDatatypeURI(String.class), 0, -1,
	// new String[] { Asor.PROP_SUPPORTS,
	// ScriptEngine.PROP_FILE_EXTENSIONS });
	// getEngines.addOutput(OUTPUT_ENGINE_LANGS, LanguageClassifier.MY_URI,
	// 0,
	// -1, ppSuppLang);
	profiles.add(getEngines.getProfile());

	// ------
	Asor getScripts = new Asor(SERVICE_GET_SCRIPTS);
	getScripts.addOutput(OUTPUT_SCRIPTS, Script.MY_URI, 0, -1, ppControls);
	profiles.add(getScripts.getProfile());

	// ------
	Asor addScript = new Asor(SERVICE_ADD_SCRIPT);
	addScript.addInputWithAddEffect(INPUT_SCRIPT, Script.MY_URI, 1, 1,
		ppControls);
	// addScript.addFilteringInput(INPUT_IS_PERSISTENT,
	// TypeMapper.getDatatypeURI(Boolean.class), 0, 1, new String[] {
	// Asor.PROP_CONTROLS, Script.PROP_IS_PERSISTENT });
	// addScript.addFilteringInput(INPUT_CONTENT,
	// TypeMapper.getDatatypeURI(String.class), 1, 1, new String[] {
	// Asor.PROP_CONTROLS, Script.PROP_CONTENT });
	// addScript.addOutput(OUTPUT_SCRIPTS, Script.MY_URI, 0, 1, ppControls);
	addScript.addOutput(OUTPUT_SCRIPTS, Script.MY_URI, 1, 1, ppControls);
	profiles.add(addScript.getProfile());

	// ------
	Asor removeScript = new Asor(SERVICE_REMOVE_SCRIPT);
	removeScript.addInputWithRemoveEffect(INPUT_SCRIPT, Script.MY_URI, 1,
		1, ppControls);
	profiles.add(removeScript.getProfile());

	return profiles.toArray(new ServiceProfile[profiles.size()]);
    }

    @Override
    public void communicationChannelBroken() {
    }

    @Override
    public ServiceResponse handleCall(ServiceCall call) {
	if (call == null)
	    return null;

	String operation = call.getProcessURI();
	if (operation == null)
	    return null;

	if (operation.startsWith(SERVICE_GET_ENGINES))
	    return getEngines();

	if (operation.startsWith(SERVICE_GET_SCRIPTS))
	    return getScripts();

	Object inScriptObj = call.getInputValue(INPUT_SCRIPT);
	if (inScriptObj == null)
	    return null;
	if (!(inScriptObj instanceof Script))
	    return null;
	Script inScript = (Script) inScriptObj;

	if (operation.startsWith(SERVICE_REMOVE_SCRIPT))
	    return removeScript(inScript);

	// Object inPersistent = true;
	// call.getInputValue(INPUT_IS_PERSISTENT);
	// if (inPersistent == null)
	// return null;
	// if (!(inPersistent instanceof Boolean))
	// return null;

	if (operation.startsWith(SERVICE_ADD_SCRIPT))
	    return addScript(inScript);

	return null;
    }

    private ServiceResponse removeScript(Script script) {
	System.out.println("-- removeScript");
	as.removeScript(script);
	return new ServiceResponse(CallStatus.succeeded);
    }

    private ServiceResponse addScript(Script script) {
	System.out.println("-- addscript");
	if (script.getContent() == null) {
	    LogUtils.logDebug(AsorActivator.mc, AsorIntegration.class,
		    "addScript",
		    "Can not add script because the content is empty");
	    return new ServiceResponse(CallStatus.serviceSpecificFailure);
	}

	if (script.getName() == null) {
	    cntScript++;
	    script.setName(AsorProvider.BASE + "addedScript" + cntScript);
	}

	String uri = as.addScript(script);
	if (uri == null)
	    return new ServiceResponse(CallStatus.serviceSpecificFailure);

	ServiceResponse sr = new ServiceResponse(CallStatus.succeeded);
	sr.addOutput(OUTPUT_SCRIPTS, new Script(uri));
	return sr;
    }

    private ServiceResponse getScripts() {
	ServiceResponse sr = new ServiceResponse(CallStatus.succeeded);
	List<Script> lst = as.getScripts();
	if (lst.size() != 0) {
	    sr.addOutput(OUTPUT_SCRIPTS, lst);
	}
	return sr;
    }

    private ServiceResponse getEngines() {
	ServiceResponse sr = new ServiceResponse(CallStatus.succeeded);
	List<ScriptEngine> engines = EngineInfo.getEngines();
	sr.addOutput(OUTPUT_ENGINES, engines);
	return sr;
    }
}
