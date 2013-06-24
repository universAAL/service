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
package org.universAAL.service.asor.impl.osgi;

import java.io.File;
import java.io.FileFilter;

import org.mindswap.owl.OWLObjectConverter;
import org.mindswap.owl.OWLObjectConverterRegistry;
import org.mindswap.owls.process.ControlConstruct;
import org.osgi.framework.BundleContext;
import org.universAAL.middleware.container.Container;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.osgi.uAALBundleContainer;
import org.universAAL.middleware.service.ServiceCaller;
import org.universAAL.middleware.serialization.MessageContentSerializer;
import org.universAAL.service.asor.impl.ASOREngineImpl;
import org.universAAL.service.asor.impl.ASORServiceMngr;
import org.universAAL.service.asor.impl.servicecaller.ASORServiceCaller;
import org.universAAL.service.asor.owls.extensions.ASORCallService;


/**
 * 
 *  @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 *	
 *  Aug 12, 2012
 *
 */
public class ASORModule {
	
	public Container container;
	public ModuleContext moduleContext;
    public static Object[] asorEngineFetchParams;
    public static Object[] asorEngineShareParams;
    
    private static final String subFolderName = "ASOR_Scripts";
    private static final String mainScriptsFolder = new File(subFolderName).getAbsolutePath();
    
    // Temporary, just for phase III - create hard-coded ServiceCaller - TODO: consider to add it to the main script...
    public static final String tempServiceCallerKey = "TempAsorServiceCaller";
    	
	public ASORModule(BundleContext context) {
		this.container = uAALBundleContainer.THE_CONTAINER;
		asorEngineFetchParams = asorEngineShareParams = 
			new Object[] { IASOREngine.class.getName() };
		moduleContext = uAALBundleContainer.THE_CONTAINER.registerModule(new Object[] { context });
		MessageContentSerializer serializer = 
			(MessageContentSerializer) moduleContext.getContainer().fetchSharedObject(
					moduleContext,
					new Object[] { MessageContentSerializer.class.getName() });
		ASORServiceMngr.getInstance().setMessageContentSerializer(serializer);
	}
	
	private ASORModule() {} // Just for test
	
	public void startModule() {
		final IASOREngine asorEngine = new ASOREngineImpl();
		container.shareObject(moduleContext, asorEngine, asorEngineShareParams);
		System.out.println("ASOR Module has been started");
		
		// Register new convertors
		registerOwlsConvertors();
		
		// Create service caller
		createServiceCaller();
		
		// Start all main scripts - each one in a separated thread
		new Thread() {

			@Override
			public void run() {
				startMainScripts(asorEngine);
			}
		}.start();
		
		System.out.println("Bundle!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
	}

	private void registerOwlsConvertors() {
		final OWLObjectConverter<ASORCallService> converter = new ASORCallService.ServiceCallerConverter();
		OWLObjectConverterRegistry.instance().registerConverter(ASORCallService.class, converter); // (1)
		OWLObjectConverterRegistry.instance().extendByConverter(ControlConstruct.class, converter); // (2)
	}

	private void createServiceCaller() {
		ServiceCaller asorServiceCaller = new ASORServiceCaller(moduleContext);
		
		ASORServiceMngr.getInstance().addServiceCaller(tempServiceCallerKey, asorServiceCaller);
	}

	private void startMainScripts(IASOREngine asorEngine) {
		// By default search in current dir + ASOR_Scripts
		File dir = new File(mainScriptsFolder);
		if (!dir.isDirectory()) {
			return;
		}
		
		// Create a filter that will return *.ttl files
		FileFilter fileFilter = new FileFilter() {
		    public boolean accept(File file) {
		        return file.isFile() && file.getName().endsWith(".ttl");
		    }
		};
		
		for (File file : dir.listFiles(fileFilter)) {
			String filePath = file.getAbsolutePath().replace("\\", "/");
			
			try {
				asorEngine.startProcess(filePath, null, null);
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
				}
			} catch (ASOREngineException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] arr) {
		IASOREngine asorEngine = new ASOREngineImpl();
		System.out.println("ASOR Module has been started");
		
		ASORModule module = new ASORModule();
		
		// Register new convertors
		module.registerOwlsConvertors();
		
		// Create service caller
		module.createServiceCaller();
		
		// Start all main scripts - each one in a separated thread
		module.startMainScripts(asorEngine);
	}
}
