package org.universAAL.service.asor.test;

import java.io.File;
import java.util.List;

import org.universAAL.middleware.bus.junit.BusTestCase;
import org.universAAL.middleware.bus.permission.AccessControl;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.DefaultServiceCaller;
import org.universAAL.middleware.service.ServiceRequest;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.ontology.asor.Asor;
import org.universAAL.ontology.asor.AsorOntology;
import org.universAAL.ontology.asor.Script;
import org.universAAL.ontology.asor.ScriptEngine;
import org.universAAL.ontology.lighting.LightingOntology;
import org.universAAL.ontology.location.LocationOntology;
import org.universAAL.ontology.phThing.PhThingOntology;
import org.universAAL.ontology.shape.ShapeOntology;
import org.universAAL.service.asor.AsorProvider;
import org.universAAL.service.asor.AsorActivator;
import org.universAAL.container.JUnit.JUnitModuleContext;
import org.universAAL.container.JUnit.JUnitModuleContext.LogLevel;

public class Test extends BusTestCase {
	static boolean isSetUp = false;
	static DefaultServiceCaller caller;

	String NAMESPACE = "http://ontology.universAAL.org/AsorTest.owl#";
	String OUTPUT = NAMESPACE + "output";

	String[] ppControls = new String[] { Asor.PROP_CONTROLS };
	String[] ppSupports = new String[] { Asor.PROP_SUPPORTS };

	public void tearDown() {
		// don't do anything here so we don't have to set up again
	}

	public void setUp() throws Exception {
		if (isSetUp)
			return;

		super.setUp();
		isSetUp = true;

		OntologyManagement.getInstance().register(mc, new LocationOntology());
		OntologyManagement.getInstance().register(mc, new ShapeOntology());
		OntologyManagement.getInstance().register(mc, new PhThingOntology());
		OntologyManagement.getInstance().register(mc, new LightingOntology());

		mc.setAttribute(AccessControl.PROP_MODE, "none");
		mc.setAttribute(AccessControl.PROP_MODE_UPDATE, "always");

		OntologyManagement.getInstance().register(mc, new AsorOntology());
		AsorActivator.mc = mc;

		//((JUnitModuleContext) mc).setLogLevel(LogLevel.DEBUG);

		// new ExecutionEngine(mc, this).execute2("");
		// ExecutionEngine e = new ExecutionEngine(mc, this);
		// e.execute3();
		// e.execute4();
		// e.execute2("");

		// new Watcher(null, confHome.getAbsolutePath());
		// if (true)
		// return;

		AsorProvider asor = new AsorProvider(new File("."));
		while (asor.hasRunning()) {
		}

		// List<ScriptEngine> en = EngineInfo.getEngines();
		// for (ScriptEngine e : en) {
		// System.out.println(" ---------- found engine: \n" + serialize(e));
		// }

		caller = new DefaultServiceCaller(mc);
	}

	public int getNumberOfScripts() {
		ServiceRequest req = new ServiceRequest(new Asor(), null);
		req.addRequiredOutput(OUTPUT, ppControls);
		ServiceResponse sr = caller.call(req);
		assertTrue(sr != null);
		assertTrue(sr.getCallStatus() == CallStatus.succeeded);

		List<Object> lst = sr.getOutput(OUTPUT);

		if (lst == null)
			return 0;
		return lst.size();
	}

	public void testAddScript() {
		// get the number od existing scripts
		int num = getNumberOfScripts();

		// setup a small script
		Script script = new Script();
		script.setName("testScript1");
		script.setContent("println(\"hello from my own testScript1\");");
		script.setPersistent(false);

		// add script and check answer
		ServiceRequest req = new ServiceRequest(new Asor(), null);
		req.addAddEffect(ppControls, script);
		req.addRequiredOutput(OUTPUT, ppControls);

		ServiceResponse sr = caller.call(req);
		assertTrue(sr != null);
		System.out.println(sr.getCallStatus());
		assertTrue(sr.getCallStatus() == CallStatus.succeeded);

		List<Object> lst = sr.getOutput(OUTPUT);
		assertTrue(lst != null);
		assertTrue(lst.size() == 1);
		assertTrue(lst.get(0) instanceof Script);
		String uri = ((Script) lst.get(0)).getURI();

		assertTrue(getNumberOfScripts() == num + 1);

		// remove script
		req = new ServiceRequest(new Asor(), null);
		req.addValueFilter(ppControls, new Script(uri));
		req.addRemoveEffect(ppControls);

		sr = caller.call(req);
		assertTrue(sr != null);
		System.out.println(sr.getCallStatus());
		assertTrue(sr.getCallStatus() == CallStatus.succeeded);

		assertTrue(getNumberOfScripts() == num);
	}

	public void testGetEngines() {
		ServiceRequest req = new ServiceRequest(new Asor(), null);
		req.addRequiredOutput(OUTPUT, ppSupports);

		ServiceResponse sr = caller.call(req);
		assertTrue(sr != null);
		assertTrue(sr.getCallStatus() == CallStatus.succeeded);

		List<Object> lst = sr.getOutput(OUTPUT);
		assertTrue(lst != null);
		assertTrue(lst.size() == 1);
		assertTrue(lst.get(0) instanceof ScriptEngine);
		System.out.println("-- Found the following engine:\n" + serialize((Resource) lst.get(0)));
	}

	public void testgetScripts() {
		ServiceRequest req = new ServiceRequest(new Asor(), null);
		req.addRequiredOutput(OUTPUT, ppControls);

		ServiceResponse sr = caller.call(req);
		assertTrue(sr != null);
		assertTrue(sr.getCallStatus() == CallStatus.succeeded);

		List<Object> lst = sr.getOutput(OUTPUT);
		assertTrue(lst != null);
		assertTrue(lst.size() == 2);
		assertTrue(lst.get(0) instanceof Resource);
		assertTrue(lst.get(1) instanceof Resource);
		for (int i = 0; i < lst.size(); i++) {
			System.out.println("-- Found the following Script (" + (i + 1) + "/" + lst.size() + "):\n"
					+ serialize((Resource) lst.get(0)));
		}
	}
}
