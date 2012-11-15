// The MIT License
//
// Copyright (c) 2004 Evren Sirin
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to
// deal in the Software without restriction, including without limitation the
// rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
// sell copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
// IN THE SOFTWARE.

/*
 * Created on Dec 12, 2004
 */
package impl.jena;

import impl.owl.CastingList;
import impl.owl.OWLIndividualListImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mindswap.common.Parser;
import org.mindswap.common.Variable;
import org.mindswap.owl.OWLIndividual;
import org.mindswap.owl.OWLKnowledgeBaseManager;
import org.mindswap.owl.OWLModel;
import org.mindswap.owl.OWLObjectConverterRegistry;
import org.mindswap.owl.OWLProvider;
import org.mindswap.pellet.PelletOptions;
import org.mindswap.pellet.jena.PelletReasonerFactory;
import org.mindswap.query.ABoxQuery;
import org.mindswap.query.QueryLanguage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.rulesys.OWLFBRuleReasonerFactory;
import com.hp.hpl.jena.reasoner.rulesys.OWLMicroReasonerFactory;
import com.hp.hpl.jena.reasoner.rulesys.OWLMiniReasonerFactory;
import com.hp.hpl.jena.reasoner.rulesys.RDFSRuleReasonerFactory;
import com.hp.hpl.jena.reasoner.transitiveReasoner.TransitiveReasonerFactory;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.LocationMapper;

/**
 *
 * @author unascribed
 * @version $Rev: 2530 $; $Author: thorsten $; $Date: 2010-07-30 20:46:00 +0300 (Fri, 30 Jul 2010) $
 */
public class JenaOWLProvider implements OWLProvider
{
	// Implementation note: This class should *not* maintain any state between
	// the registerConverters(..) method derived from OWLObjectConverterProvider
	// and methods directly derived from OWLProvider! The reason is that two
	// instances of this class will be created; one used by the OWLObjectConverterRegistry
	// to register all converters and one hold by the OWLFactory.

	private static final Logger logger = LoggerFactory.getLogger(JenaOWLProvider.class);

	private static final OWLIndividualListImpl<OWLIndividual> EMPTY_LIST =
		new OWLIndividualListImpl<OWLIndividual>(Collections.<OWLIndividual>emptyList());

	private enum Reasoners { Pellet, OWL, OWLMini, OWLMicro, RDFS, Transitive }

	/**
	 *
	 */
	public JenaOWLProvider()
	{
		super();
	}

	static final Reasoner createReasoner(final String reasonerName)
	{
		// Pellet reasoner (first, because it is most likely used)
		if (Reasoners.Pellet.name().equals(reasonerName))
		{
			PelletOptions.USE_CLASSIFICATION_MONITOR = PelletOptions.MonitorType.NONE; // mute console outputs
			return PelletReasonerFactory.theInstance().create();
		}
		// Jena built-in reasoners
		else if (Reasoners.OWL.name().equals(reasonerName))
		{
			return OWLFBRuleReasonerFactory.theInstance().create(null);
		}
		else if (Reasoners.OWLMini.name().equals(reasonerName))
		{
			return OWLMiniReasonerFactory.theInstance().create(null);
		}
		else if (Reasoners.OWLMicro.name().equals(reasonerName))
		{
			return OWLMicroReasonerFactory.theInstance().create(null);
		}
		else if (Reasoners.RDFS.name().equals(reasonerName))
		{
			return RDFSRuleReasonerFactory.theInstance().create(null);
		}
		else if (Reasoners.Transitive.name().equals(reasonerName))
		{
			return TransitiveReasonerFactory.theInstance().create(null);
		}

		return null;
	}

	/* @see org.mindswap.owl.OWLProvider#getReasonerNames() */
	public List<String> getReasonerNames()
	{
		List<String> reasoners = new ArrayList<String>();
		for (Reasoners r : Reasoners.values())
		{
			reasoners.add(r.name());
		}
		return reasoners;
	}

	/* @see org.mindswap.owl.OWLProvider#createKB() */
	public OWLKnowledgeBaseImpl createKB()
	{
		// We need to create a new OntModelSpec because KBs must be able to set the reasoner independently.
		// Otherwise, setting/removing a reasoner would affect all KB instances.
		final OntModelSpec spec = createOntSpec(null);
		return new OWLKnowledgeBaseImpl(new OWLKnowledgeBaseManagerImpl(spec));
	}

	/* @see org.mindswap.owl.OWLProvider#createKB(org.mindswap.owl.OWLKnowledgeBaseManager) */
	public OWLKnowledgeBaseImpl createKB(final OWLKnowledgeBaseManager manager)
	{
		if (manager != null)
		{
			if (manager instanceof OWLKnowledgeBaseManagerImpl)
				return new OWLKnowledgeBaseImpl((OWLKnowledgeBaseManagerImpl) manager);

			logger.debug("Instance of {} expected, actual type {}. Will use default KB manager instead.",
				OWLKnowledgeBaseManagerImpl.class, manager.getClass());
		}
		return createKB();
	}

	/* @see org.mindswap.owl.OWLProvider#createKBManager() */
	public OWLKnowledgeBaseManager createKBManager()
	{
		final FileManager fm = new FileManager(new LocationMapper(LocationMapper.get()));
		FileManager.setStdLocators(fm);
		final OntDocumentManager odm = new OntDocumentManager(fm, OntDocumentManager.DEFAULT_METADATA_PATH);

		// We need to create a new OntModelSpec because KBs must be able to set the reasoner independently.
		// Otherwise, setting/removing a reasoner would affect all KB instances.
		final OntModelSpec spec = new OntModelSpec(
			OntModelSpec.OWL_DL_MEM.getBaseModelMaker(), OntModelSpec.OWL_DL_MEM.getImportModelMaker(),
			odm, null, OntModelSpec.OWL_DL_MEM.getLanguage());

		return new OWLKnowledgeBaseManagerImpl(spec);
	}

	/* @see org.mindswap.owl.OWLProvider#createIndividualList() */
	public <T extends OWLIndividual> OWLIndividualListImpl<T> createIndividualList() {
		return new OWLIndividualListImpl<T>();
	}

	/* @see org.mindswap.owl.OWLProvider#createIndividualList(int) */
	public <T extends OWLIndividual> OWLIndividualListImpl<T> createIndividualList(final int capacity)
	{
		return new OWLIndividualListImpl<T>(capacity);
	}

	/* @see org.mindswap.owl.OWLProvider#createABoxQueryParser(org.mindswap.owl.OWLModel, org.mindswap.query.QueryLanguage) */
	public Parser<String, ABoxQuery<Variable>> createABoxQueryParser(final OWLModel model, final QueryLanguage lang)
	{
		return new ABoxQueryParser(model, lang);
	}

	/* @see org.mindswap.owl.OWLProvider#castList(java.util.List, java.lang.Class) */
	public <T extends OWLIndividual> OWLIndividualListImpl<T> castList(
		final List<? extends OWLIndividual> list, final Class<T> castTarget)
	{
		assert (list != null && castTarget != null) : "Illegal: list and/or cast target parameter was null.";

		final OWLIndividualListImpl<T> result = createIndividualList();
		T element;
		for (final OWLIndividual individual : list)
		{
			element = individual.castTo(castTarget);
			result.add(element);
		}
		return result;
	}

	/* @see org.mindswap.owl.OWLProvider#wrapList(java.util.List, java.lang.Class) */
	public <T extends OWLIndividual> CastingList<T> wrapList(final List<? extends OWLIndividual> list,
		final Class<T> castTarget)
	{
		// assertions as specified by interface JavaDoc are implemented in constructor
		return new CastingList<T>(list, castTarget);
	}

	/* @see org.mindswap.owl.OWLProvider#emptyIndividualList() */
	@SuppressWarnings("unchecked") // cast in method is not critical since EMPTY_LIST is empty and immutable
	public <T extends OWLIndividual> OWLIndividualListImpl<T> emptyIndividualList()
	{
		return (OWLIndividualListImpl<T>) EMPTY_LIST;
	}

	/* @see org.mindswap.owl.OWLProvider#unmodifiableIndividualList(java.util.List) */
	public <T extends OWLIndividual> OWLIndividualListImpl<T> unmodifiableIndividualList(final List<T> list)
	{
		return new OWLIndividualListImpl<T>(Collections.unmodifiableList(list));
	}

	/* @see org.mindswap.owl.OWLObjectConverterProvider#registerConverters(org.mindswap.owl.OWLObjectConverterRegistry) */
	public void registerConverters(final OWLObjectConverterRegistry registry)
	{
		OWLConverters.registerConverters(registry);
	}

	/**
	 * Utility method that can be used by classes in this package. It creates
	 * a new specification for OWL DL models that are stored in main memory and
	 * do no additional entailment reasoning, i.e., {@link OntModelSpec#getReasoner()}
	 * and {@link OntModelSpec#getReasonerFactory()} are initially <code>null</code>.
	 *
	 * @param source A model specification from which to create a copy, or
	 * 	<code>null</code> to create a copy of {@link OntModelSpec#OWL_DL_MEM}.
	 * @return A new ontology model specification instance.
	 */
	static OntModelSpec createOntSpec(final OntModelSpec source)
	{
		if (source == null) return new OntModelSpec(OntModelSpec.OWL_DL_MEM);

		final OntModelSpec spec = new OntModelSpec(source);
		spec.setReasoner(null);
		spec.setReasonerFactory(null);
		return spec;
	}

}
