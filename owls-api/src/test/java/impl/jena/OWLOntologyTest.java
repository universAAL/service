/*
 * Created 15.03.2009
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
package impl.jena;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.mindswap.owl.OWLFactory;
import org.mindswap.owl.OWLKnowledgeBase;
import org.mindswap.owl.OWLOntology;
import org.mindswap.owls.vocabulary.OWLS;
import org.mindswap.utils.URIUtils;

import com.hp.hpl.jena.ontology.OntModel;

/**
 * JUnit tests for {@link OWLOntologyImpl}.
 *
 * @author unascribed
 * @version $Rev: 2269 $; $Author: thorsten $; $Date: 2009-08-19 18:21:09 +0300 (Wed, 19 Aug 2009) $
 */
public class OWLOntologyTest
{
	private static final URI BASE_URI_1 = URI.create("http://localhost/dummy.owl");
	private static final URI BASE_URI_2 = URI.create("http://localhost/bummy.owl");

	/**
	 * Tests for {@link OWLOntologyImpl#addImport(OWLOntology)}.
	 */
	@Test
	public final void testAddImport()
	{
		OWLKnowledgeBase kb = OWLFactory.createKB();
		OWLOntology ont = kb.createOntology(BASE_URI_1);

		Set<OWLOntology> imports = ont.getImports(true);
		assertNotNull(imports);
		assertEquals(0, imports.size());

		imports = ont.getImports(false);
		assertNotNull(imports);
		assertEquals(0, imports.size());

		ont.addImport(OWLS.Actor.Actor.getOntology());

		// test with imports closure
		Set<OWLOntology> actorImports = OWLS.Actor.Actor.getOntology().getImports(true);
		imports = ont.getImports(true);
		assertEquals(actorImports.size(), imports.size() - 1); // minus Actor ontology itself
		for (Iterator<OWLOntology> it = imports.iterator(); it.hasNext(); )
		{
			OWLOntology o = it.next();
			for (Iterator<OWLOntology> iit = actorImports.iterator(); iit.hasNext(); )
			{
				if (o.getURI().equals(iit.next().getURI()))
				{
					iit.remove();
					it.remove();
				}
			}
		}
		assertEquals(1, imports.size()); // Actor ontology must be left
		assertEquals(OWLS.Actor.Actor.getOntology().getURI(), imports.iterator().next().getURI());

		// test with direct import only
		imports = ont.getImports(false);
		assertEquals(1, imports.size());
		assertEquals(OWLS.Actor.Actor.getOntology().getURI(), imports.iterator().next().getURI());

		// try to add second time --> must be ignored
		Set<OWLOntology> before = ont.getImports(true);
		ont.addImport(OWLS.Actor.Actor.getOntology());
		Set<OWLOntology> after = ont.getImports(true);
		for (Iterator<OWLOntology> it = before.iterator(); it.hasNext(); )
		{
			OWLOntology o = it.next();
			for (Iterator<OWLOntology> iit = after.iterator(); iit.hasNext(); )
			{
				if (o.getURI().equals(iit.next().getURI()))
				{
					iit.remove();
					it.remove();
				}
			}
		}
		assertEquals(0, before.size());
		assertEquals(0, after.size());
	}

	/**
	 * Tests for {@link OWLOntologyImpl#addImports(java.util.Collection)}.
	 */
	@Test
	public void testAddImports()
	{
		OWLKnowledgeBase kb = OWLFactory.createKB();
		OWLOntology ont = kb.createOntology(BASE_URI_1);

		Set<OWLOntology> imports = ont.getImports(true);
		assertNotNull(imports);
		assertEquals(0, imports.size());

		List<OWLOntology> theImports = new ArrayList<OWLOntology>();
		theImports.add(OWLS.Actor.Actor.getOntology());
		theImports.add(OWLS.Profile.contactInformation.getOntology());
		ont.addImports(theImports); //Will import Actor only! Actor comes first and Profile is imported by Actor

		imports = ont.getImports(false);
		assertEquals(1, imports.size()); // Actor only --> size == 1
		for (Iterator<OWLOntology> it = imports.iterator(); it.hasNext(); )
		{
			OWLOntology o = it.next();
			if (OWLS.Actor.Actor.getOntology().getURI().equals(o.getURI()) ||
				OWLS.Profile.contactInformation.getOntology().getURI().equals(o.getURI()))
			{
				it.remove();
			}
		}
		assertEquals(0, imports.size());

		// add yet some other ontologies; should also prevent Profile to be added twice
		OWLS.addOWLSImports(ont);
		imports = ont.getImports(true);
		for (Iterator<OWLOntology> it = imports.iterator(); it.hasNext(); )
		{
			OWLOntology o = it.next();
			if (OWLS.Actor.Actor.getOntology().getURI().equals(o.getURI()) ||
				OWLS.Profile.contactInformation.getOntology().getURI().equals(o.getURI()) ||
				OWLS.Process.AnyOrder.getOntology().getURI().equals(o.getURI()) ||
				OWLS.Service.describedBy.getOntology().getURI().equals(o.getURI()) ||
				OWLS.Grounding.operation.getOntology().getURI().equals(o.getURI()) ||
				OWLS.Expression.AlwaysTrue.getOntology().getURI().equals(o.getURI()) ||
				URIUtils.createURI(OWLS.URI + "generic/ObjectList.owl").equals(o.getURI()))
			{
				it.remove();
			}
		}
		assertEquals(0, imports.size());
	}

	/**
	 * Tests for {@link OWLOntologyImpl#removeAll(boolean)}.
	 */
	@Test
	public void testRemoveAll()
	{
		OWLKnowledgeBase kb = OWLFactory.createKB();
		OWLOntology ont = kb.createOntology(BASE_URI_1);

		OWLS.addOWLSImports(ont);
		ont.removeAll(true);
		assertEquals(0, ((OntModel) ont.getImplementation()).size());

		ont = kb.createOntology(BASE_URI_1);
		OWLS.addOWLSImports(ont);
		ont.removeAll(false);
		long ontSize = ((OntModel) ont.getImplementation()).size();

		OWLOntology ont1 = kb.createOntology(BASE_URI_2);
		OWLS.addOWLSImports(ont1);
		long ont1Size = ((OntModel) ont1.getImplementation()).size();

		assertTrue(0 < ontSize && ontSize < ont1Size);
	}

	/**
	 * Tests for {@link OWLOntologyImpl#removeImport(OWLOntology)} and
	 * {@link OWLOntologyImpl#removeImports(Set)}.
	 */
	@Test
	public void testRemoveImport()
	{
		OWLKnowledgeBase kb = OWLFactory.createKB();
		OWLOntology ont = kb.createOntology(BASE_URI_1);

		OWLS.addOWLSImports(ont);
		assertTrue(ont.getImports(false).size() > 0);

		// remove direct imports and check if zero finally
		Set<OWLOntology> imports = ont.getImports(false);
		for (OWLOntology ontology : imports)
		{
			ont.removeImport(ontology);
		}
		imports = ont.getImports(false);
		assertEquals(0, imports.size());
		// import statements must be removed as well, but one owl:Ontology statement remains
		assertEquals(1, ((OntModel) ont.getImplementation()).size());

		// remove one ontology and check
		OWLS.addOWLSImports(ont);
		imports = ont.getImports(false);
		int size = imports.size();
		OWLOntology impOnt = imports.iterator().next();
		ont.removeImport(impOnt);
		assertEquals(size - 1, ont.getImports(false).size());
		Set<String> impURIs = ((OntModel) ont.getImplementation()).listImportedOntologyURIs(false);
		for (String uri : impURIs)
		{
			assertFalse(uri.equals(impOnt.getURI().toString()));
		}

		imports = ont.getImports(false);
		ont.removeImports(imports);
		imports = ont.getImports(false); // irrelevant here whether we use true or false
		assertEquals(0, imports.size());
	}

	/**
	 * Tests writing and reading {@link OWLOntologyImpl}.
	 */
	@Test
	public void testWriteReadImports() throws Exception
	{
		OWLKnowledgeBase kb = OWLFactory.createKB();
		OWLOntology ont = kb.createOntology(BASE_URI_1);
		OWLS.addOWLSImports(ont);

		StringWriter sw = new StringWriter();
		ont.write(sw, BASE_URI_1);
		sw.flush();

		String serial = sw.toString();
		StringReader sr = new StringReader(serial);

		OWLKnowledgeBase kb_clone = OWLFactory.createKB();
		OWLOntology cloneOnt = kb_clone.read(sr, BASE_URI_1);

		Set<OWLOntology> imports = ont.getImports(true);
		Set<OWLOntology> cloneImports = cloneOnt.getImports(true);
		assertEquals(imports.size(), cloneImports.size());

		OntModel ontModel = (OntModel) ont.getImplementation();
		OntModel cloneModel = (OntModel) cloneOnt.getImplementation();
		assertTrue(ontModel.isIsomorphicWith(cloneModel));
	}

}
