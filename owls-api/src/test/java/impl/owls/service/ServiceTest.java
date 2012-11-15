/*
 * Created 14.02.2009
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
package impl.owls.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;

import org.junit.Test;
import org.mindswap.owl.OWLFactory;
import org.mindswap.owl.OWLIndividual;
import org.mindswap.owl.OWLIndividualList;
import org.mindswap.owl.OWLKnowledgeBase;
import org.mindswap.owl.OWLOntology;
import org.mindswap.owl.vocabulary.OWL;
import org.mindswap.owls.grounding.Grounding;
import org.mindswap.owls.grounding.UPnPGrounding;
import org.mindswap.owls.grounding.WSDLGrounding;
import org.mindswap.owls.process.Process;
import org.mindswap.owls.profile.Profile;
import org.mindswap.owls.service.Service;
import org.mindswap.owls.vocabulary.FLAServiceOnt;
import org.mindswap.owls.vocabulary.OWLS;
import org.mindswap.utils.URIUtils;

/**
 *
 * @author unascribed
 * @version $Rev: 2269 $; $Author: thorsten $; $Date: 2009-08-19 18:21:09 +0300 (Wed, 19 Aug 2009) $
 */
public class ServiceTest
{
	private static final URI baseURI = URI.create("http://myexample/myservice");
	private static final URI serviceURI = URIUtils.createURI(baseURI, "MyService");

	private Service createService()
	{
		OWLKnowledgeBase kb = OWLFactory.createKB();
		OWLOntology ont = kb.createOntology(baseURI);
		OWLS.addOWLSImports(ont);
		ont.addImport(FLAServiceOnt.caName.getOntology());
		assertEquals(ont.getURI(), baseURI);

		Service service = ont.createService(serviceURI);
		assertFalse(service.isAnon());
		assertTrue(service.isIndividual());
		assertTrue(service.isType(OWLS.Service.Service));
		assertEquals(service.getURI(), serviceURI);
		return service;
	}

	/**
	 * Test method for {@link impl.owls.service.ServiceImpl#addGrounding(org.mindswap.owls.grounding.Grounding)},
	 * {@link impl.owls.service.ServiceImpl#getGroundings()},
	 * {@link impl.owls.service.ServiceImpl#getGrounding()}.
	 */
	@Test
	public void testAddGetGrounding()
	{
		Service service = createService();
		OWLIndividualList<Grounding> groundings = service.getGroundings();
		assertNotNull(groundings);
		assertEquals(0, groundings.size());

		URI wsdlGroundingURI = URIUtils.createURI(baseURI, "WSDLGrounding");
		WSDLGrounding wsdlGrounding = service.getOntology().createWSDLGrounding(wsdlGroundingURI);
		assertNotNull(wsdlGrounding);
		assertEquals(wsdlGroundingURI, wsdlGrounding.getURI());
		assertEquals(OWLS.Grounding.WsdlGrounding, wsdlGrounding.getType());

		service.addGrounding(wsdlGrounding);
		groundings = service.getGroundings();
		assertNotNull(groundings);
		assertEquals(1, groundings.size());

		Grounding<?,?> grounding = groundings.get(0);
		assertEquals(wsdlGrounding, grounding);
		assertEquals(wsdlGrounding.getType(), grounding.getType());
		assertEquals(wsdlGrounding.getURI(), grounding.getURI());

		URI upnpGroundingURI = URIUtils.createURI(baseURI, "UPnPGrounding");
		UPnPGrounding upnpGrounding = service.getOntology().createUPnPGrounding(upnpGroundingURI);
		OWLS.addOWLSImport(upnpGrounding.getOntology(), FLAServiceOnt.NS); // not yet in model
		assertNotNull(upnpGrounding);
		assertEquals(upnpGroundingURI, upnpGrounding.getURI());
		assertEquals(FLAServiceOnt.UPnPGrounding, upnpGrounding.getType());

		service.addGrounding(upnpGrounding);
		groundings = service.getGroundings();
		assertNotNull(groundings);
		assertEquals(2, groundings.size());

		for (Grounding<?,?> g : groundings)
		{
			if (g.canCastTo(WSDLGrounding.class))
			{
				assertEquals(wsdlGrounding, g);
				assertEquals(wsdlGrounding.getType(), g.getType());
				assertEquals(wsdlGrounding.getURI(), g.getURI());
			}
			if (g.canCastTo(UPnPGrounding.class))
			{
				assertEquals(upnpGrounding, g);
				assertEquals(upnpGrounding.getType(), g.getType());
				assertEquals(upnpGrounding.getURI(), g.getURI());
			}
		}

		grounding = service.getGrounding();
		assertNotNull(grounding);
		assertTrue(wsdlGrounding.getURI().equals(grounding.getURI()) ||
			upnpGrounding.getURI().equals(grounding.getURI()));

	}

	/**
	 * Test method for {@link impl.owls.service.ServiceImpl#addProfile(org.mindswap.owls.profile.Profile)},
	 * {@link impl.owls.service.ServiceImpl#getProfiles()},
	 * {@link impl.owls.service.ServiceImpl#getProfile()}.
	 */
	@Test
	public void testAddGetProfile()
	{
		Service service = createService();
		URI profileURI1 = URIUtils.createURI(baseURI, "Profile");
		Profile profile1 = service.getOntology().createProfile(profileURI1);
		assertNotNull(profile1);
		assertFalse(profile1.isAnon());
		assertEquals(profileURI1, profile1.getURI());

		OWLIndividualList<Profile> profiles = service.getProfiles();
		assertNotNull(profiles);
		assertEquals(0, profiles.size());

		service.addProfile(profile1);
		profiles = service.getProfiles();
		assertNotNull(profiles);
		assertEquals(1, profiles.size());
		assertEquals(profile1.getURI(), profiles.get(0).getURI());
		assertEquals(service, profile1.getService());

		URI profileURI2 = URIUtils.createURI(baseURI, "AnotherProfile");
		Profile profile2 = service.getOntology().createProfile(profileURI2);
		service.addProfile(profile2);
		profiles = service.getProfiles();
		assertNotNull(profiles);
		assertEquals(2, profiles.size());
		assertEquals(service, profile2.getService());

		boolean p1 = false, p2 = false;
		for (Profile p : profiles)
		{
			if (p.getURI().equals(profileURI1)) p1 = true;
			if (p.getURI().equals(profileURI2)) p2 = true;
			assertEquals(service, p.getService());
		}
		assertTrue(p1 && p2);

		Profile p = service.getProfile();
		assertNotNull(p);
		assertEquals(OWLS.Profile.Profile, p.getType());
		assertTrue(profileURI1.equals(p.getURI()) || profileURI2.equals(p.getURI()));
	}

	/**
	 * Test method for {@link impl.owls.service.ServiceImpl#addProvider(org.mindswap.owl.OWLIndividual)},
	 * {@link impl.owls.service.ServiceImpl#getProviders()}.
	 */
	@Test
	public void testAddGetProvider()
	{
		Service service = createService();
		URI providerURI1 = URIUtils.createURI(baseURI, "Provider");
		OWLIndividual provider1 = service.getOntology().createInstance(null, providerURI1);
		assertEquals(OWL.Thing, provider1.getType());

		OWLIndividualList<?> providers = service.getProviders();
		assertNotNull(providers);
		assertEquals(0, providers.size());

		service.addProvider(provider1);
		providers = service.getProviders();
		assertEquals(1, providers.size());
		assertEquals(provider1, providers.get(0));

		URI providerURI2 = URIUtils.createURI(baseURI, "Provider2");
		OWLIndividual provider2 = service.getOntology().createInstance(null, providerURI2);
		service.addProvider(provider2);
		providers = service.getProviders();
		assertEquals(2, providers.size());

		boolean p1 = false, p2 = false;
		for (OWLIndividual p : providers)
		{
			if (p.getURI().equals(providerURI1)) p1 = true;
			if (p.getURI().equals(providerURI2)) p2 = true;
		}
		assertTrue(p1 && p2);
	}

	/**
	 * Test method for {@link impl.owls.service.ServiceImpl#getName()},
	 * {@link impl.owls.service.ServiceImpl#getName(java.lang.String)},
	 * {@link impl.owls.service.ServiceImpl#getNames()},
	 * {@link impl.owls.service.ServiceImpl#setName(java.lang.String)}.
	 */
	@Test
	public void testGetSetName()
	{
		Service service = createService();
		Profile profile = service.getOntology().createProfile(null);
		service.addProfile(profile);

		assertNull(service.getName());
		assertNull(service.getName(null));

		String name1 = "n1", name2 = "n2";

		service.setName(name1);
		assertEquals(name1, service.getName());
		assertEquals(name1, service.getName(null));
		assertNull(service.getName("ch"));

		service.setName(name2);
		assertEquals(name2, service.getName());
		assertEquals(name2, service.getName(null));
		assertNull(service.getName("ch"));
	}

	/**
	 * Test method for {@link impl.owls.service.ServiceImpl#getProcess()},
	 * {@link impl.owls.service.ServiceImpl#setProcess(org.mindswap.owls.process.Process)}.
	 */
	@Test
	public void testGetSetProcess()
	{
		Service service = createService();
		URI processURI = URIUtils.createURI(baseURI, "AP");
		Process process = service.getOntology().createAtomicProcess(processURI);
		assertNull(service.getProcess());
		service.setProcess(process);

		assertEquals(service, process.getService());
		assertEquals(process, service.getProcess());

		processURI = URIUtils.createURI(baseURI, "AP2");
		process = service.getOntology().createAtomicProcess(processURI);
		service.setProcess(process);

		assertEquals(service, process.getService());
		assertEquals(process, service.getProcess());
	}

	/**
	 * Test method for {@link impl.owls.service.ServiceImpl#removeGrounding(org.mindswap.owls.grounding.Grounding)}.
	 */
	@Test
	public void testRemoveGrounding()
	{
		Service service = createService();

		URI wsdlGroundingURI = URIUtils.createURI(baseURI, "WSDLGrounding");
		URI upnpGroundingURI = URIUtils.createURI(baseURI, "UPnPGrounding");
		WSDLGrounding wsdlGrounding = service.getOntology().createWSDLGrounding(wsdlGroundingURI);
		UPnPGrounding upnpGrounding = service.getOntology().createUPnPGrounding(upnpGroundingURI);

		service.addGrounding(wsdlGrounding);
		service.addGrounding(upnpGrounding);
		assertEquals(2, service.getGroundings().size());

		service.removeGrounding(wsdlGrounding);
		assertNull(wsdlGrounding.getService());
		assertEquals(1, service.getGroundings().size());
		assertEquals(upnpGrounding, service.getGroundings().get(0));

		service.removeGrounding(wsdlGrounding); // should not have any effect
		assertEquals(1, service.getGroundings().size());

		service.removeGrounding(upnpGrounding);
		assertNull(upnpGrounding.getService());
		assertEquals(0, service.getGroundings().size());
	}

	/**
	 * Test method for {@link impl.owls.service.ServiceImpl#removeGrounding(Grounding)}.
	 */
	@Test
	public void testRemoveGroundings()
	{
		Service service = createService();

		URI wsdlGroundingURI = URIUtils.createURI(baseURI, "WSDLGrounding");
		URI upnpGroundingURI = URIUtils.createURI(baseURI, "UPnPGrounding");
		WSDLGrounding wsdlGrounding = service.getOntology().createWSDLGrounding(wsdlGroundingURI);
		UPnPGrounding upnpGrounding = service.getOntology().createUPnPGrounding(upnpGroundingURI);

		service.addGrounding(wsdlGrounding);
		service.addGrounding(upnpGrounding);
		assertEquals(2, service.getGroundings().size());

		service.removeGrounding(null);
		assertNull(wsdlGrounding.getService());
		assertNull(upnpGrounding.getService());
		assertEquals(0, service.getGroundings().size());
	}

	/**
	 * Test method for {@link impl.owls.service.ServiceImpl#removeProcess()}.
	 */
	@Test
	public void testRemoveProcess()
	{
		Service service = createService();
		URI processURI = URIUtils.createURI(baseURI, "AP");
		Process process = service.getOntology().createAtomicProcess(processURI);

		service.setProcess(process);
		service.removeProcess();

		assertNull(service.getProcess());
		assertNull(process.getService());
	}

	/**
	 * Test method for {@link impl.owls.service.ServiceImpl#removeProfile(org.mindswap.owls.profile.Profile)}.
	 */
	@Test
	public void testRemoveProfile()
	{
		Service service = createService();
		URI profileURI1 = URIUtils.createURI(baseURI, "Profile");
		URI profileURI2 = URIUtils.createURI(baseURI, "AnotherProfile");
		Profile profile1 = service.getOntology().createProfile(profileURI1);
		Profile profile2 = service.getOntology().createProfile(profileURI2);

		service.addProfile(profile1);
		service.addProfile(profile2);
		assertEquals(2, service.getProfiles().size());

		service.removeProfile(profile2);
		assertNull(profile2.getService());
		assertEquals(1, service.getProfiles().size());
		assertEquals(profile1, service.getProfiles().get(0));

		service.removeProfile(profile2); // should not have any effect
		assertEquals(1, service.getProfiles().size());

		service.removeProfile(profile1);
		assertNull(profile1.getService());
		assertEquals(0, service.getProfiles().size());
	}

	/**
	 * Test method for {@link impl.owls.service.ServiceImpl#removeProfile(Profile)}.
	 */
	@Test
	public void testRemoveProfiles()
	{
		Service service = createService();
		URI profileURI1 = URIUtils.createURI(baseURI, "Profile");
		URI profileURI2 = URIUtils.createURI(baseURI, "AnotherProfile");
		Profile profile1 = service.getOntology().createProfile(profileURI1);
		Profile profile2 = service.getOntology().createProfile(profileURI2);

		service.addProfile(profile1);
		service.addProfile(profile2);
		assertEquals(2, service.getProfiles().size());

		service.removeProfile(null);
		assertNull(profile1.getService());
		assertNull(profile2.getService());
		assertEquals(0, service.getProfiles().size());
	}

	/**
	 * Test method for {@link impl.owls.service.ServiceImpl#removeProvider(org.mindswap.owl.OWLIndividual)}.
	 */
	@Test
	public void testRemoveProvider()
	{
		Service service = createService();
		URI providerURI1 = URIUtils.createURI(baseURI, "Provider");
		URI providerURI2 = URIUtils.createURI(baseURI, "Provider2");
		OWLIndividual provider1 = service.getOntology().createInstance(null, providerURI1);
		OWLIndividual provider2 = service.getOntology().createInstance(null, providerURI2);

		service.addProvider(provider1);
		service.addProvider(provider2);
		assertEquals(2, service.getProviders().size());

		service.removeProvider(provider2);
		assertEquals(1, service.getProviders().size());
		assertEquals(provider1, service.getProviders().get(0));

		service.removeProvider(provider2); // should not have any effect
		assertEquals(1, service.getProviders().size());

		service.removeProvider(provider1);
		assertEquals(0, service.getProviders().size());
	}

	/**
	 * Test method for {@link impl.owls.service.ServiceImpl#removeProvider(OWLIndividual)}.
	 */
	@Test
	public void testRemoveProviders()
	{
		Service service = createService();
		URI providerURI1 = URIUtils.createURI(baseURI, "Provider");
		URI providerURI2 = URIUtils.createURI(baseURI, "Provider2");
		OWLIndividual provider1 = service.getOntology().createInstance(null, providerURI1);
		OWLIndividual provider2 = service.getOntology().createInstance(null, providerURI2);

		service.addProvider(provider1);
		service.addProvider(provider2);
		assertEquals(2, service.getProviders().size());

		service.removeProvider(null);
		assertEquals(0, service.getProviders().size());
	}
}
