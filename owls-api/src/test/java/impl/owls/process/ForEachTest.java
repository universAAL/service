/*
 * Created 04.06.2009
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
package impl.owls.process;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assume.assumeNotNull;
import impl.owls.grounding.JavaAtomicGroundingImpl;
import impl.owls.process.constructs.ForEachImpl;

import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.sound.midi.Instrument;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mindswap.exceptions.CastingException;
import org.mindswap.exceptions.TransformationException;
import org.mindswap.owl.OWLClass;
import org.mindswap.owl.OWLDataProperty;
import org.mindswap.owl.OWLDataValue;
import org.mindswap.owl.OWLFactory;
import org.mindswap.owl.OWLIndividual;
import org.mindswap.owl.OWLKnowledgeBase;
import org.mindswap.owl.OWLModel;
import org.mindswap.owl.OWLOntology;
import org.mindswap.owl.OWLTransformator;
import org.mindswap.owl.OWLValue;
import org.mindswap.owl.list.OWLList;
import org.mindswap.owl.vocabulary.RDF;
import org.mindswap.owls.OWLSFactory;
import org.mindswap.owls.process.Process;
import org.mindswap.owls.process.execution.ProcessExecutionEngine;
import org.mindswap.owls.process.variable.Input;
import org.mindswap.owls.process.variable.Output;
import org.mindswap.owls.service.Service;
import org.mindswap.query.ValueMap;
import org.mindswap.utils.URIUtils;

import examples.ExampleURIs;


/**
 * JUnit integration test for {@link ForEachImpl}. Example process is loaded
 * and executed. Indirectly tests {@link impl.swrl.ClassAtomImpl} because
 * it is used to express a process precondition.
 * Furthermore, this test also covers {@link JavaAtomicGroundingImpl} and
 * related classes, because the atomic process uses a Java grounding.
 *
 * @author unascribed
 * @version $Rev: 2703 $; $Author: thorsten $; $Date: 2011-05-13 17:13:07 +0300 (Fri, 13 May 2011) $
 */
public class ForEachTest
{
	static final URI URI_repetitions = URIUtils.createURI(ExampleURIs.FOREACH_OWLS12, "repetitions");
	static final URI URI_length = URIUtils.createURI(ExampleURIs.FOREACH_OWLS12, "length");
	static final URI URI_note = URIUtils.createURI(ExampleURIs.FOREACH_OWLS12, "note");
	static final URI URI_Note = URIUtils.createURI(ExampleURIs.FOREACH_OWLS12, "Note");
	static final URI URI_MidiNote = URIUtils.createURI(ExampleURIs.FOREACH_OWLS12, "MidiNote");

	static final int DATA[][] = {
		// Partitur "Alle meine Entchen" {{Tonhoehe, DauerInViertelNoten, AnzahlWdh},...}
		{60,  1, 1}, //C
		{62,  1, 1}, //D
		{64,  1, 1}, //E
		{65,  1, 1}, //F
		{67,  2, 2}, //G,G
		{69,  1, 4}, //A,A,A,A
		{67,  4, 1}, //G
		{69,  1, 4}, //A,A,A,A
		{67,  4, 1}, //G
		{65,  1, 4}, //F,F,F,F
		{64,  2, 2}, //E,E
		{62,  1, 4}, //D,D,D,D
		{60,  4, 1}  //C
	};

	static OWLClass CLASS_Note;
	static OWLClass CLASS_MidiNote;
	static OWLDataProperty PROPERTY_note;
	static OWLDataProperty PROPERTY_length;
	static OWLDataProperty PROPERTY_repetitions;

	static Synthesizer synth;
	static Receiver rcvr;
	static ExecutorService executor;

	@BeforeClass
	public static final void setUp()
	{
		try
		{
			synth = MidiSystem.getSynthesizer();
			synth.open();

			System.out.println("Synthesizer device info:      " + synth.getDeviceInfo());
			System.out.println("Synthesizer max polyphony:    " + synth.getMaxPolyphony());
			System.out.println("Synthesizer max receivers:    " + synth.getMaxReceivers());
			System.out.println("Synthesizer max transmitters: " + synth.getMaxTransmitters());
			System.out.print("Synthesizer instruments:      ");
			Instrument[] instruments = synth.getAvailableInstruments();
			for (int i = 0; i < instruments.length; i++)
			{
				System.out.print(instruments[i].getName() + ((i < instruments.length - 1)? ", " : ""));
			}
			System.out.printf("%n%n");

			rcvr = synth.getReceiver();
		}
		catch (MidiUnavailableException e)
		{
			// ignore test, see first line of testForEach()
		}

		executor = Executors.newSingleThreadExecutor();
	}

	@AfterClass
	public static final void tearDown() throws InterruptedException
	{
		executor.shutdown();
		executor.awaitTermination(100, TimeUnit.SECONDS);
		if (synth != null) synth.close();
	}

	@Ignore // target method of the Java grounding specified in the test process
	public static final void playNote(final MidiNote note)
	{
		executor.submit(new Callable<Object> () {
			public Object call() throws InvalidMidiDataException
			{
				ShortMessage msg = new ShortMessage();
				for (int j = 0; j < note.repetitions; ++j)
				{
					// Note on
					msg.setMessage(ShortMessage.NOTE_ON, 0, note.note, 64);
					rcvr.send(msg, -1);

					// Pause --> determines player tempo
					try {	Thread.sleep((long) (note.length * 4 * 200)); } // mult by 4 because we get quarter notes
					catch (InterruptedException e) {	/* ignore */ }

					// Note off
					msg.setMessage(ShortMessage.NOTE_OFF, 0, note.note, 0);
					rcvr.send(msg, -1);
				}
				return null;
			}
		});
	}

	@Test
	public void testForEach() throws Exception
	{
		assumeNotNull(synth, rcvr); // avoid that test fails for machines that do not have a MIDI device

		long read = System.nanoTime();
		final OWLKnowledgeBase kb = OWLFactory.createKB();
		final InputStream inpStr = ClassLoader.getSystemResourceAsStream("owls/1.2/ForEach.owl");
		final Service service = kb.readService(inpStr, URIUtils.createURI(
			ExampleURIs.FOREACH_OWLS12, "ForEachService")); // service name required because file specifies two services
		read = (System.nanoTime() - read) / 1000000;
		final Process process = service.getProcess();

		CLASS_Note = kb.getClass(URI_Note);
		CLASS_MidiNote = kb.getClass(URI_MidiNote);
		PROPERTY_note = kb.getDataProperty(URI_note);
		PROPERTY_length = kb.getDataProperty(URI_length);
		PROPERTY_repetitions = kb.getDataProperty(URI_repetitions);
		assumeNotNull(CLASS_Note, CLASS_MidiNote, PROPERTY_note, PROPERTY_length, PROPERTY_repetitions);

		kb.setReasoner("Pellet");
		kb.prepare();

		// verify some facts that should be entailed by our tiny note ontology
		assertTrue(CLASS_MidiNote.isSubTypeOf(CLASS_Note));
		OWLIndividual note = kb.getIndividual(URIUtils.createURI(ExampleURIs.FOREACH_OWLS12, "halfc"));
		assertNotNull(note);
		assertTrue(note.isType(CLASS_MidiNote)); // inferred knowledge
		assertTrue(note.isType(CLASS_Note)); // MidiNote is subsumed by Note

//		((OntModel) kb.getImplementation()).register(new StatementListener() {
//			@Override
//			public void addedStatement(final Statement s)
//			{
//				System.out.println("added   " + s.toString());
//			}
//
//			@Override
//			public void removedStatement(final Statement s)
//			{
//				System.out.println("removed " + s.toString());
//			}
//		});

		// prepare input notes list
		final ValueMap<Input, OWLValue> inputs = new ValueMap<Input, OWLValue>();
		inputs.setValue(process.getInput("notes"), prepareInputNotes(service.getOntology()));

		final ProcessExecutionEngine exec = OWLSFactory.createExecutionEngine();
		exec.getExecutionValidator().setPreconditionCheck(true);

		System.out.printf("Number of statements in KB before execution: %d%n", kb.size());

		long exet = System.nanoTime();
		ValueMap<Output, OWLValue> outputs = exec.execute(process, inputs, kb);
		exet = (System.nanoTime() - exet) / 1000000;

		System.out.printf("Read took %5dms, Execution took %4dms%n", read, exet);
		System.out.printf("Number of statements in KB after execution: %d%n", kb.size());

		assertNotNull(outputs);
		assertEquals(0, outputs.size());
	}

	private OWLList<OWLValue> prepareInputNotes(final OWLOntology ont)
	{
		OWLList<OWLValue> notes = ont.createList(RDF.ListVocabulary);
		for (int i = DATA.length - 1; i >= 0; i--)
		{
			// Let's create instances of Note. By setting one of the properties they can be inferred
			// to be MidiNotes, c.f. ForEach.owl for detailed explanations why this is entailed.
			final OWLIndividual n = ont.createInstance(CLASS_Note, null);
// use this instead if no reasoner is attached (MidiNote trivially entailed, i.e., explicit fact)
//			final OWLIndividual n = ont.createInstance(CLASS_MidiNote, null);
			n.setProperty(PROPERTY_note, Integer.valueOf(DATA[i][0]));
			n.setProperty(PROPERTY_length, Float.valueOf(DATA[i][1] / 4.0f));
			n.setProperty(PROPERTY_repetitions, Integer.valueOf(DATA[i][2]));
			notes = notes.cons(n);
		}
		return notes;
	}

	public static final class MidiNote
	{
		final int note;
		final float length;
		final int repetitions;

		public MidiNote(final int note, final float length, final int repetitions)
		{
			this.note = note;
			this.length = length;
			this.repetitions = repetitions;
		}
	}

	/**
	 * Transforms instances of the OWL concept {@link ForEachTest#URI_MidiNote}
	 * to instances of the class {@link MidiNote}.
	 */
	public static final class NoteTransformator implements OWLTransformator
	{
		/* @see org.mindswap.owl.OWLTransformator#fromOWL(org.mindswap.owl.OWLValue, org.mindswap.owl.OWLModel) */
		public Object fromOWL(final OWLValue source, final OWLModel model) throws TransformationException
		{
			OWLIndividual ind;
			try
			{
				ind = source.castTo(OWLIndividual.class);
			}
			catch (CastingException e)
			{
				throw new TransformationException(e);
			}

			final OWLDataValue nv = ind.getProperty(PROPERTY_note);
			final OWLDataValue nl = ind.getProperty(PROPERTY_length);
			final OWLDataValue nr = ind.getProperty(PROPERTY_repetitions);
			final int note = ((Integer) nv.getValue()).intValue();
			final float length = ((Float) nl.getValue()).floatValue();
			final int repetitions = ((Integer) nr.getValue()).intValue();

			return new MidiNote(note, length, repetitions);
		}

		/* @see org.mindswap.owl.OWLTransformator#toOWL(java.lang.Object, org.mindswap.owl.OWLModel) */
		public OWLValue toOWL(final Object source, final OWLModel model) throws TransformationException
		{
			// grounding method playNote(..) returns void, thus, we don't need to do anything here
			return null;
		}
	}

}
