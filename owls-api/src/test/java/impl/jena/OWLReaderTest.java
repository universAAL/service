/*
 * Created 26.03.2009
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeNoException;
import static org.junit.Assume.assumeNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mindswap.owl.OWLFactory;
import org.mindswap.owl.OWLKnowledgeBase;
import org.mindswap.owl.OWLKnowledgeBaseManager;
import org.mindswap.owl.OWLOntology;
import org.mindswap.owl.OWLSyntax;
import org.mindswap.owls.service.Service;
import org.mindswap.owls.vocabulary.OWLS;
import org.mindswap.utils.URIUtils;
import org.mindswap.utils.Utils;

import examples.ExampleURIs;

/**
 * JUnit tests for class {@link OWLReaderImpl}.
 *
 * @author unascribed
 * @version $Rev: 2530 $; $Author: thorsten $; $Date: 2010-07-30 20:46:00 +0300 (Fri, 30 Jul 2010) $
 */
public class OWLReaderTest
{
	@BeforeClass
	public static void note()
	{
		System.out.println("Note that this test requires the local host to be connected to the Internet and " +
			"the availability of remote resources, which might change, move, or disappear. Consequently, " +
			"in case this test fails this does not necessarily mean that there is a problem with the code!");
	}

	/**
	 * Test method for {@link impl.jena.OWLReaderImpl#read(java.io.Reader, java.net.URI)}.
	 */
	@Test
	public final void testReadReaderURI()
	{
		String invalidXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		OWLKnowledgeBase kb = OWLFactory.createKB();
		try
		{
			kb.read(new StringReader(invalidXML), null);
			fail(IOException.class + " expected.");
		}
		catch (IOException ignore)
		{
			// nice
		}
	}

	/**
	 * Test method for {@link impl.jena.OWLReaderImpl#read(InputStream, java.net.URI)}.
	 */
	@Test
	public final void testReadInputStreamURI() throws IOException
	{
		// try to read ont that imports owls:Service, which is available
		InputStream availableImport = ClassLoader.getSystemResourceAsStream("owl/available_import.owl");
		assumeNotNull(availableImport);
		OWLKnowledgeBase kb = OWLFactory.createKB();
		try
		{
			OWLOntology ont = kb.read(availableImport, null);
			assertNotNull(ont);
			Set<OWLOntology> imports = ont.getImports(true);
			assertEquals(1, imports.size()); // the file exactly has one import
			OWLOntology serviceOnt = imports.iterator().next();
			assertEquals(URIUtils.standardURI(OWLS.SERVICE_NS), serviceOnt.getURI());
		}
		catch (IOException e)
		{
			fail("Unexpected exception " + e);
		}
		availableImport.close();

		// try to read ont that imports another ont that does not exist and we ignore failure
		InputStream unavailableImport = ClassLoader.getSystemResourceAsStream("owl/unavailable_import.owl");
		assumeNotNull(unavailableImport);
		kb = OWLFactory.createKB();
		kb.getReader().setIgnoreFailedImport(true);
		try
		{
			OWLOntology ont = kb.read(unavailableImport, null);
			assertNotNull(ont);
			Set<OWLOntology> imports = ont.getImports(true);
			assertEquals(1, imports.size()); // imported ontology exists but should be empty
			OWLOntology unavailableOnt = imports.iterator().next();
			assertEquals(0, ((OWLOntologyImpl) unavailableOnt).getImplementation().size());
		}
		catch (IOException e)
		{
			fail("Unexpected exception " + e);
		}
		unavailableImport.close();

		// try to read ont that imports another ont that does not exist and we do not ignore failure
		unavailableImport = ClassLoader.getSystemResourceAsStream("owl/unavailable_import.owl");
		assumeNotNull(unavailableImport);
		kb = OWLFactory.createKB();
		kb.getReader().setIgnoreFailedImport(false);
		try
		{
			kb.read(unavailableImport, null);
			fail("Exception expected.");
		}
		catch (IOException e)
		{
			// nice
		}
		unavailableImport.close();

		// do the same again <-- must throw exception again even if caching is used
		unavailableImport = ClassLoader.getSystemResourceAsStream("owl/unavailable_import.owl");
		assumeNotNull(unavailableImport);
		kb = OWLFactory.createKB();
		kb.getReader().setIgnoreFailedImport(false);
		try
		{
			kb.read(unavailableImport, null);
			fail("Exception expected.");
		}
		catch (IOException e)
		{
			// nice
		}
		unavailableImport.close();
	}

	@Test
	public final void testReadTurtleReader()
	{
		String ttl =
			"@prefix owl:     <http://www.w3.org/2002/07/owl#> ." + Utils.LINE_SEPARATOR +
			"<http://on.cs.unibas.ch/available_import.ttl>" + Utils.LINE_SEPARATOR +
			"   a       owl:Ontology ;" + Utils.LINE_SEPARATOR +
			"   owl:imports <http://www.daml.org/services/owl-s/1.2/Service.owl> .";
		OWLKnowledgeBase kb = OWLFactory.createKB();
		kb.getReader().setSyntax(OWLSyntax.TURTLE);
		try
		{
			OWLOntology ont = kb.read(new StringReader(ttl), null);
			assertNotNull(ont);
			ont.containsOntology(OWLS.Service.describedBy.getOntology().getURI());
		}
		catch (IOException e)
		{
			fail("Unexpected exception " + e);
		}
	}

	@Test
	public final void testReadTurtleInputStream() throws IOException
	{
		InputStream availableImport = ClassLoader.getSystemResourceAsStream("owl/available_import.ttl");
		assumeNotNull(availableImport);
		OWLKnowledgeBase kb = OWLFactory.createKB();
		kb.getReader().setSyntax(OWLSyntax.TURTLE);
		try
		{
			OWLOntology ont = kb.read(availableImport, null);
			assertNotNull(ont);
			ont.containsOntology(OWLS.Service.describedBy.getOntology().getURI());
		}
		catch (IOException e)
		{
			fail("Unexpected exception " + e);
		}
		availableImport.close();
		
		// once again with wrong format specified --> should raise exception
		availableImport = ClassLoader.getSystemResourceAsStream("owl/available_import.ttl");
		kb.getReader().setSyntax(OWLSyntax.RDFXML);
		try
		{
			kb.read(availableImport, null);
			fail("IOException expected.");
		}
		catch (IOException e)
		{
			// nice
		}
		availableImport.close();
	}

	/**
	 * Tests reading two example OWL-S services from a local file concurrently
	 * by multiple threads. We only want to analyze if typical concurrent
	 * modification exceptions occur.
	 */
	@Test
	public final void testReadUsingMultipleThreads() throws Exception
	{
		int numOfConcurrentTasks = 5;
		List<Callable<Exception>> tasks = new ArrayList<Callable<Exception>>(numOfConcurrentTasks);
		ExecutorService executor = Executors.newFixedThreadPool(numOfConcurrentTasks);

		for (int i = 0; i < numOfConcurrentTasks; i++)
		{
			tasks.add(new Callable<Exception>() {
				public Exception call() throws Exception
				{
					try
					{
						OWLKnowledgeBase kb = OWLFactory.createKB();
//						kb.getReader().setIgnoreFailedImport(true);
						File f = new File("src/examples/owl-s/1.2/FrenchDictionary.owl");
						FileInputStream fis = new FileInputStream(f);
						Service service = kb.readService(fis, ExampleURIs.FRENCH_DICTIONARY_OWLS12);
						if (service == null) return new Exception("Service null upon read.");
						fis.close();
						return null;
					}
					catch (IOException ioe)
					{
						return ioe;
					}
				}
			});

			tasks.add(new Callable<Exception>() {
				public Exception call() throws Exception
				{
					try
					{
						OWLKnowledgeBase kb = OWLFactory.createKB();
//						kb.getReader().setIgnoreFailedImport(true);
						File f = new File("src/examples/owl-s/1.2/CurrencyConverter.owl");
						FileInputStream fis = new FileInputStream(f);
						Service service = kb.readService(fis, ExampleURIs.CURRENCY_CONVERTER_OWLS12);
						if (service == null) return new Exception("Service null upon read.");
						fis.close();
						return null;
					}
					catch (IOException ioe)
					{
						return ioe;
					}
				}
			});
		}

		List<Future<Exception>> futures = executor.invokeAll(tasks);

		boolean fail = false;
		StringBuilder errors = new StringBuilder();
		for (Future<Exception> future : futures)
		{
			Exception e = future.get();
			if (e != null)
			{
				fail = true;
				errors.append(" ").append(e.toString());
			}
		}

		if (fail)
		{
			fail("Callable threw exception. Details:" + errors);
		}
	}
	
	@Test
	public void testWithDedicatedOWLKBManager()
	{
		OWLKnowledgeBaseManager kbm = OWLFactory.createKBManager();
		OWLKnowledgeBase kb1 = OWLFactory.createKB(kbm);
		URI processURI = ExampleURIs.FIND_CHEAPER_BOOK_OWLS12;
		
		try
		{
			// 1.) - from original location
			long time1 = System.nanoTime();
			OWLOntology ont1 = kb1.read(processURI);
			time1 = System.nanoTime() - time1;
			System.out.printf("First read of %s from the Web took                         %4dms%n",
				processURI, TimeUnit.NANOSECONDS.toMillis(time1));
			assertEquals(processURI, ont1.getURI());
			
			// 2.) - just try again --> returns already loaded one in KB
			long time2 = System.nanoTime();
			OWLOntology ont2 = kb1.read(processURI);
			time2 = System.nanoTime() - time2;
			System.out.printf("Second read of %s (same KB --> use existing one) took      %4dµs%n",
				processURI, TimeUnit.NANOSECONDS.toMicros(time2));
			
			assertEquals(ont1, ont2);
			assertTrue(time2 < time1);
			
			// 3.) - use new KB but same KB manager --> load from cache
			OWLKnowledgeBase kb2 = OWLFactory.createKB(kbm);
			long time3 = System.nanoTime();
			OWLOntology ont3 = kb2.read(processURI);
			time3 = System.nanoTime() - time3;
			System.out.printf("Third read of %s (new KB & existing KB manager cache) took %4dµs%n",
				processURI, TimeUnit.NANOSECONDS.toMicros(time3));
			
			assertEquals(ont2.getURI(), ont3.getURI());
			assertEquals(ont2.size(), ont3.size());
			assertTrue(time3 < time1);
			
			// 4.) - partially clear KB manager cache --> load Process ont but not imports from Web again
			kbm.clear(processURI);
			OWLKnowledgeBase kb3 = OWLFactory.createKB(kbm);
			long time4 = System.nanoTime();
			OWLOntology ont4 = kb3.read(processURI);
			time4 = System.nanoTime() - time4;
			System.out.printf("Forth read of %s (partially cleared KB manager cache) took %4dms%n",
				processURI, TimeUnit.NANOSECONDS.toMillis(time4));
			
			assertEquals(processURI, ont4.getURI());
			assertEquals(ont3.size(), ont4.size());
			assertTrue(time3 < time4);
			
			// 5.) entirely clear KB manager cache --> load process and imports from Web again
			kbm.clear(null);
			OWLKnowledgeBase kb4 = OWLFactory.createKB(kbm);
			long time5 = System.nanoTime();
			OWLOntology ont5 = kb4.read(processURI);
			time5 = System.nanoTime() - time5;
			System.out.printf("Fifth read of %s (entirely cleared KB manager cache) took  %4dms%n",
				processURI, TimeUnit.NANOSECONDS.toMillis(time5));
			
			assertEquals(processURI, ont5.getURI());
			assertEquals(ont3.size(), ont5.size());
			assertTrue(time4 < time5);
			
			// 6.a) turn off caching for KB manager --> load process and imports from Web again
			kbm.setCaching(false);
			OWLKnowledgeBase kb5 = OWLFactory.createKB(kbm);
			long time6 = System.nanoTime();
			OWLOntology ont6 = kb5.read(processURI);
			time6 = System.nanoTime() - time6;
			System.out.printf("Sixth read of %s (turn off KB manager caching) took        %4dms%n",
				processURI, TimeUnit.NANOSECONDS.toMillis(time6));
			
			assertEquals(processURI, ont6.getURI());
			assertEquals(ont3.size(), ont6.size());
			assertTrue(time4 < time6);
			
			// 6.b) try again --> times of a) and b) should not differ much
			OWLKnowledgeBase kb6 = OWLFactory.createKB(kbm);
			long time7 = System.nanoTime();
			OWLOntology ont7 = kb6.read(processURI);
			time7 = System.nanoTime() - time7;
			System.out.printf("Seventh read of %s (no KB manager caching) took            %4dms%n",
				processURI, TimeUnit.NANOSECONDS.toMillis(time7));
			
			assertEquals(processURI, ont7.getURI());
			assertEquals(ont3.size(), ont7.size());
// does not work perfectly: caching remains active in Jena for imported ontologies (ImportModelMaker)
//			assertEquals(time6, time7, time6 / 3); // allow ~33% difference
		}
		catch (IOException e)
		{
			assumeNoException(e);
		}
	}

}
