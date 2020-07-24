package org.cd2h.covid;

import java.sql.Connection;

import org.apache.log4j.Logger;

import edu.uiowa.NLP_grammar.syntaxTree;
import edu.uiowa.NLP_grammar.syntaxMatch.syntaxMatch;
import edu.uiowa.NLP_grammar.syntaxMatch.syntaxMatchFunction;
import edu.uiowa.NLP_grammar.syntaxMatch.syntaxMatcher;
import edu.uiowa.NLP_grammar.syntaxMatch.comparator.placeNameComparator;
import edu.uiowa.PubMedCentral.comparator.GeoNameComparator;
import edu.uiowa.PubMedCentral.entity.PlaceName;
import edu.uiowa.Translator.comparators.anatomicalEntityComparator;
import edu.uiowa.Translator.comparators.biologicalProcessComparator;
import edu.uiowa.Translator.comparators.cellularComponentComparator;
import edu.uiowa.Translator.comparators.chemicalSubstanceComparator;
import edu.uiowa.Translator.comparators.drugComparator;
import edu.uiowa.Translator.comparators.geneComparator;
import edu.uiowa.Translator.comparators.humanPhenotypeComparator;
import edu.uiowa.Translator.comparators.molecularActivityComparator;
import edu.uiowa.Translator.comparators.molecularEntityComparator;
import edu.uiowa.Translator.comparators.namedThingComparator;
import edu.uiowa.Translator.comparators.organismalEntityComparator;
import edu.uiowa.Translator.comparators.proteinComparator;
import edu.uiowa.Translator.comparators.publicationComparator;
import edu.uiowa.Translator.comparators.rnaComparator;
import edu.uiowa.Translator.comparators.sequenceComparator;
import edu.uiowa.UMLS.Concept;
import edu.uiowa.UMLS.Semantics;
import edu.uiowa.UMLS.comparators.activityComparator;
import edu.uiowa.UMLS.comparators.anatomicalStructureComparator;
import edu.uiowa.UMLS.comparators.biologicalFunctionComparator;
import edu.uiowa.UMLS.comparators.bodyPartComparator;
import edu.uiowa.UMLS.comparators.conceptComparator;
import edu.uiowa.UMLS.comparators.conceptualRelationshipComparator;
import edu.uiowa.UMLS.comparators.disciplineComparator;
import edu.uiowa.UMLS.comparators.diseaseComparator;
import edu.uiowa.UMLS.comparators.entityComparator;
import edu.uiowa.UMLS.comparators.eventComparator;
import edu.uiowa.UMLS.comparators.findingComparator;
import edu.uiowa.UMLS.comparators.functionalConceptComparator;
import edu.uiowa.UMLS.comparators.functionalRelationshipComparator;
import edu.uiowa.UMLS.comparators.groupAttributeComparator;
import edu.uiowa.UMLS.comparators.groupComparator;
import edu.uiowa.UMLS.comparators.humanProcessComparator;
import edu.uiowa.UMLS.comparators.injuryComparator;
import edu.uiowa.UMLS.comparators.intellectualProductComparator;
import edu.uiowa.UMLS.comparators.languageComparator;
import edu.uiowa.UMLS.comparators.manufacturedObjectComparator;
import edu.uiowa.UMLS.comparators.naturalProcessComparator;
import edu.uiowa.UMLS.comparators.organicChemicalComparator;
import edu.uiowa.UMLS.comparators.organismAttributeComparator;
import edu.uiowa.UMLS.comparators.organismComparator;
import edu.uiowa.UMLS.comparators.organizationComparator;
import edu.uiowa.UMLS.comparators.pathologicalFunctionComparator;
import edu.uiowa.UMLS.comparators.physicalRelationshipComparator;
import edu.uiowa.UMLS.comparators.physiologicalFunctionComparator;
import edu.uiowa.UMLS.comparators.processComparator;
import edu.uiowa.UMLS.comparators.qualitativeConceptComparator;
import edu.uiowa.UMLS.comparators.quantitativeConceptComparator;
import edu.uiowa.UMLS.comparators.relationshipComparator;
import edu.uiowa.UMLS.comparators.spatialConceptComparator;
import edu.uiowa.UMLS.comparators.spatialRelationshipComparator;
import edu.uiowa.UMLS.comparators.substanceComparator;
import edu.uiowa.UMLS.comparators.temporalConceptComparator;
import edu.uiowa.UMLS.comparators.temporalRelationshipComparator;
import edu.uiowa.UMLS.comparators.transcriptionFactorComparator;
import edu.uiowa.extraction.Decorator;

public class BioRxivDecorator extends Decorator {
    static Logger logger = Logger.getLogger(BioRxivDecorator.class);
    
    Connection conn = null;

    public BioRxivDecorator(Connection conn) throws Exception {
	super();
	logger.info("BioRxivDecorator");
	this.conn = conn;
	initialize(conn);
    }
    
    private void initialize(Connection conn) throws Exception {
	edu.uiowa.Translator.entity.Entity.initialize(conn);
	Concept.initialize(conn, true);
	PlaceName.initialize();
	
	activityComparator.initialize(conn, "covid_biorxiv");
	anatomicalStructureComparator.initialize(conn, "covid_biorxiv");
	biologicalFunctionComparator.initialize(conn, "covid_biorxiv");
	bodyPartComparator.initialize(conn, "covid_biorxiv");
	conceptComparator.initialize(conn, "covid_biorxiv");
	temporalConceptComparator.initialize(conn, "covid_biorxiv");
	qualitativeConceptComparator.initialize(conn, "covid_biorxiv");
	quantitativeConceptComparator.initialize(conn, "covid_biorxiv");
	functionalConceptComparator.initialize(conn, "covid_biorxiv");
	spatialConceptComparator.initialize(conn, "covid_biorxiv");
	conceptualRelationshipComparator.initialize(conn, "covid_biorxiv");
	disciplineComparator.initialize(conn, "covid_biorxiv");
	diseaseComparator.initialize(conn, "covid_biorxiv");
	entityComparator.initialize(conn, "covid_biorxiv");
	eventComparator.initialize(conn, "covid_biorxiv");
	findingComparator.initialize(conn, "covid_biorxiv");
	functionalRelationshipComparator.initialize(conn, "covid_biorxiv");
	groupComparator.initialize(conn, "covid_biorxiv");
	groupAttributeComparator.initialize(conn, "covid_biorxiv");
	humanProcessComparator.initialize(conn, "covid_biorxiv");
	injuryComparator.initialize(conn, "covid_biorxiv");
	intellectualProductComparator.initialize(conn, "covid_biorxiv");
	languageComparator.initialize(conn, "covid_biorxiv");
	manufacturedObjectComparator.initialize(conn, "covid_biorxiv");
	naturalProcessComparator.initialize(conn, "covid_biorxiv");
	organicChemicalComparator.initialize(conn, "covid_biorxiv");
	organismComparator.initialize(conn, "covid_biorxiv");
	organismAttributeComparator.initialize(conn, "covid_biorxiv");
	organizationComparator.initialize(conn, "covid_biorxiv");
	pathologicalFunctionComparator.initialize(conn, "covid_biorxiv");
	physiologicalFunctionComparator.initialize(conn, "covid_biorxiv");
	processComparator.initialize(conn, "covid_biorxiv");
	relationshipComparator.initialize(conn, "covid_biorxiv");
	spatialRelationshipComparator.initialize(conn, "covid_biorxiv");
	substanceComparator.initialize(conn, "covid_biorxiv");
	temporalRelationshipComparator.initialize(conn, "covid_biorxiv");
	transcriptionFactorComparator.initialize(conn, "covid_biorxiv");
    }

    @Override
    public boolean decorateTree(syntaxTree theTree) throws Exception {
	decorateTree(theTree, "Activity", new activityComparator());
	decorateTree(theTree, "AnatomicalStructure", new anatomicalStructureComparator());
	decorateTree(theTree, "BiologicalFunction", new biologicalFunctionComparator());
	decorateTree(theTree, "BodyPart", new bodyPartComparator());
	decorateTree(theTree, "Concept", new conceptComparator());
	decorateTree(theTree, "TemporalConcept", new temporalConceptComparator());
	decorateTree(theTree, "QualitativeConcept", new qualitativeConceptComparator());
	decorateTree(theTree, "QuantitativeConcept", new quantitativeConceptComparator());
	decorateTree(theTree, "FunctionalConcept", new functionalConceptComparator());
	decorateTree(theTree, "SpatialConcept", new spatialConceptComparator());
	decorateTree(theTree, "ConceptualRelationship", new conceptualRelationshipComparator());
	decorateTree(theTree, "Discipline", new disciplineComparator());
	decorateTree(theTree, "Disease", new diseaseComparator());
	decorateTree(theTree, "Entity", new entityComparator());
	decorateTree(theTree, "Event", new eventComparator());
	decorateTree(theTree, "Finding", new findingComparator());
	decorateTree(theTree, "FunctionalRelationship", new functionalRelationshipComparator());
	decorateTree(theTree, "Group", new groupComparator());
	decorateTree(theTree, "GroupAttribute", new groupAttributeComparator());
	decorateTree(theTree, "HumanProcess", new humanProcessComparator());
	decorateTree(theTree, "Injury", new injuryComparator());
	decorateTree(theTree, "IntellectualProduct", new intellectualProductComparator());
	decorateTree(theTree, "Language", new languageComparator());
	decorateTree(theTree, "ManufacuredObject", new manufacturedObjectComparator());
	decorateTree(theTree, "NaturalProcess", new naturalProcessComparator());
	decorateTree(theTree, "OrganicChemical", new organicChemicalComparator());
	decorateTree(theTree, "Organism", new organismComparator());
	decorateTree(theTree, "OrganismAttribute", new organismAttributeComparator());
	decorateTree(theTree, "Organization", new organizationComparator());
	decorateTree(theTree, "PathologicalFunction", new pathologicalFunctionComparator());
	decorateTree(theTree, "PhysicalRelationship", new physicalRelationshipComparator());
	decorateTree(theTree, "PhysiologicalFunction", new physiologicalFunctionComparator());
	decorateTree(theTree, "Process", new processComparator());
	decorateTree(theTree, "Relationship", new relationshipComparator());
	decorateTree(theTree, "SpatialRelationship", new spatialRelationshipComparator());
	decorateTree(theTree, "Substance", new substanceComparator());
	decorateTree(theTree, "TemporalRelationship", new temporalRelationshipComparator());
	decorateTree(theTree, "TranscriptionFactor", new transcriptionFactorComparator());

	decorateTree(theTree, "PlaceName", new placeNameComparator());
	decorateTree(theTree, "GeoName", new GeoNameComparator());
	
	decorateTree(theTree, "TranslatorAnatomicalEntity", new anatomicalEntityComparator());
	decorateTree(theTree, "TranslatorBiologicalProcess", new biologicalProcessComparator());
	decorateTree(theTree, "TranslatorCellularComponent", new cellularComponentComparator());
	decorateTree(theTree, "TranslatorChemicalSubstance", new chemicalSubstanceComparator());
	decorateTree(theTree, "TranslatorDisease", new edu.uiowa.Translator.comparators.diseaseComparator());
	decorateTree(theTree, "TranslatorDrug", new drugComparator());
	decorateTree(theTree, "TranslatorGene", new geneComparator());
	decorateTree(theTree, "TranslatorHumanPhenotype", new humanPhenotypeComparator());
	decorateTree(theTree, "TranslatorMolecularActivity", new molecularActivityComparator());
	decorateTree(theTree, "TranslatorMolecularEntity", new molecularEntityComparator());
	decorateTree(theTree, "TranslatorNamedThing", new namedThingComparator());
	decorateTree(theTree, "TranslatorOrganismalEntity", new organismalEntityComparator());
	decorateTree(theTree, "TranslatorProtein", new proteinComparator());
	decorateTree(theTree, "TranslatorPublication", new publicationComparator());
	decorateTree(theTree, "TranslatorRNA", new rnaComparator());
	decorateTree(theTree, "TranslatorSequence", new sequenceComparator());

	return false;
    }

    private boolean decorateTree(syntaxTree theTree, String entity, syntaxMatchFunction function) throws Exception {
	return decorateTree2(theTree, entity, entity, function);
    }
    
    private boolean decorateTree2(syntaxTree theTree, String entity, String entityClass, syntaxMatchFunction function) throws Exception {
	decorateTree(theTree, "is" + entity + "(NN)", entityClass, function);
	decorateTree(theTree, "is" + entity + "(NNP)", entityClass, function);
	decorateTree(theTree, "is" + entity + "(NNPS)", entityClass, function);
	decorateTree(theTree, "is" + entity + "(NNS)", entityClass, function);
	decorateTree(theTree, "is" + entity + "(NP)", entityClass, function);	
	decorateTree(theTree, "is" + entity + "(NAC)", entityClass, function);	
	decorateTree(theTree, "is" + entity + "(NX)", entityClass, function);	
	return true;
    }
    
    private boolean decorateTree(syntaxTree theTree, String pattern, String entity, syntaxMatchFunction function) throws Exception {
	syntaxMatcher theMatcher = new syntaxMatcher(pattern);
 	theMatcher.registerFunction(pattern, function);
	theMatcher.registerFunction("isActivity", new activityComparator());
	theMatcher.registerFunction("isAnatomicalStructure", new anatomicalStructureComparator());
	theMatcher.registerFunction("isBiologicalFunction", new biologicalFunctionComparator());
	theMatcher.registerFunction("isBodyPart", new bodyPartComparator());
	theMatcher.registerFunction("isConcept", new conceptComparator());
	theMatcher.registerFunction("isTemporalConcept", new temporalConceptComparator());
	theMatcher.registerFunction("isQualitativeConcept", new qualitativeConceptComparator());
	theMatcher.registerFunction("isQuantitativeConcept", new quantitativeConceptComparator());
	theMatcher.registerFunction("isFunctionalConcept", new functionalConceptComparator());
	theMatcher.registerFunction("isSpatialConcept", new spatialConceptComparator());
	theMatcher.registerFunction("isConceptualRelationship", new conceptualRelationshipComparator());
	theMatcher.registerFunction("isDiscipline", new disciplineComparator());
	theMatcher.registerFunction("isDisease", new diseaseComparator());
	theMatcher.registerFunction("isEntity", new entityComparator());
	theMatcher.registerFunction("isEvent", new eventComparator());
	theMatcher.registerFunction("isFinding", new findingComparator());
	theMatcher.registerFunction("isFunctionalRelationship", new functionalRelationshipComparator());
	theMatcher.registerFunction("isGroup", new groupComparator());
	theMatcher.registerFunction("isGroupAttribute", new groupAttributeComparator());
	theMatcher.registerFunction("isHumanProcess", new humanProcessComparator());
	theMatcher.registerFunction("isInjury", new injuryComparator());
	theMatcher.registerFunction("isIntellectualProduct", new intellectualProductComparator());
	theMatcher.registerFunction("isLanguage", new languageComparator());
	theMatcher.registerFunction("isManufacturedObject", new manufacturedObjectComparator());
	theMatcher.registerFunction("isNaturalProcess", new naturalProcessComparator());
	theMatcher.registerFunction("isOrganicChemical", new organicChemicalComparator());
	theMatcher.registerFunction("isOrganism", new organismComparator());
	theMatcher.registerFunction("isOrganismAttribute", new organismAttributeComparator());
	theMatcher.registerFunction("isOrganization", new organizationComparator());
	theMatcher.registerFunction("isPathologicalFunction", new pathologicalFunctionComparator());
	theMatcher.registerFunction("isPhysicalRelationship", new physicalRelationshipComparator());
	theMatcher.registerFunction("isPhysiologicalFunction", new physiologicalFunctionComparator());
	theMatcher.registerFunction("isProcess", new processComparator());
	theMatcher.registerFunction("isRelationship", new relationshipComparator());
	theMatcher.registerFunction("isSpatialRelationship", new spatialRelationshipComparator());
	theMatcher.registerFunction("isSubstance", new substanceComparator());
	theMatcher.registerFunction("isTemporalRelationship", new temporalRelationshipComparator());
	theMatcher.registerFunction("isTranscriptionFactor", new transcriptionFactorComparator());

	theMatcher.registerFunction("isPlaceName", new placeNameComparator());
	theMatcher.registerFunction("isGeoName", new GeoNameComparator());
	
	theMatcher.registerFunction("isTranslatorAnatomicalEntity", new anatomicalEntityComparator());
	theMatcher.registerFunction("isTranslatorBiologicalProcess", new biologicalProcessComparator());
	theMatcher.registerFunction("isTranslatorCellularComponent", new cellularComponentComparator());
	theMatcher.registerFunction("isTranslatorChemicalSubstance", new chemicalSubstanceComparator());
	theMatcher.registerFunction("isTranslatorDisease", new edu.uiowa.Translator.comparators.diseaseComparator());
	theMatcher.registerFunction("isTranslatorDrug", new drugComparator());
	theMatcher.registerFunction("isTranslatorGene", new geneComparator());
	theMatcher.registerFunction("isTranslatorHumanPhenotype", new humanPhenotypeComparator());
	theMatcher.registerFunction("isTranslatorMolecularActivity", new molecularActivityComparator());
	theMatcher.registerFunction("isTranslatorMolecularEntity", new molecularEntityComparator());
	theMatcher.registerFunction("isTranslatorNamedThing", new namedThingComparator());
	theMatcher.registerFunction("isTranslatorOrganismalEntity", new organismalEntityComparator());
	theMatcher.registerFunction("isTranslatorProtein", new proteinComparator());
	theMatcher.registerFunction("isTranslatorPublication", new publicationComparator());
	theMatcher.registerFunction("isTranslatorRNA", new rnaComparator());
	theMatcher.registerFunction("isTranslatorSequence", new sequenceComparator());
	
	logger.trace("pattern: " + pattern + "\tentity: " + entity + "\ttree: " + theTree.trimmedPhraseAsString());
	if (theMatcher.hasMatch(theTree)) {
	    logger.info("<<<<< matched >>>>>");
	    for (syntaxMatch theMatchNode : theMatcher.matches()) {
		syntaxTree matchPhrase = theMatchNode.getPhrase();
		logger.info("\tentity: " + entity);
		logger.info("\tmatchPhrase: " + matchPhrase.trimmedPhraseAsString());
		logger.info("\t\twordnet: " + matchPhrase.getWordNetEntry());
		logger.info("\t\tthesaurus entry: " + matchPhrase.getNASAThesaurusEntry());
		logger.info("\t\tumls: " + matchPhrase.getUMLSEntry());
		logger.info("\t\tumls semantics: " + matchPhrase.getUMLSSemantics());
		logger.info("\t\tentity: " + matchPhrase.getEntityID());
		logger.info("\t\tentity class: " + matchPhrase.getEntityClass());
		if (entity.equals("*")) {
		    matchPhrase.setEntity(matchPhrase.getEntityClass());
		} else if (matchPhrase.getEntityClass() != null) {
		    matchPhrase.setEntity(matchPhrase.getEntityClass());
//		} else if (Semantics.getByEntityName(entity) == null) {
//		    matchPhrase.setEntity(entity);
		} else if (matchPhrase.getUMLSSemantics() != null){
		    matchPhrase.setEntity(Semantics.getByEntityName(entity));
		    logger.info("entity: " + entity + "\tmatch node: " + matchPhrase.treeString() + "\t" + matchPhrase.getParent().getFragmentString(true,true) + "\t" + matchPhrase.getUMLSSemantics());
		} else {
		    matchPhrase.setEntity(entity);
		}
		if (theMatchNode.getPhrase().getFragmentStringVector2().size() == 0) {
		    logger.debug("** fragment is empty!");
		} else
		    logger.debug("fragment: " + theMatchNode.getPhrase().getFragmentStringVector2().firstElement());
//		for (int i = 1; i <= theMatchNode.matchCount(); i++)
//		    logger.info("\tmatch slot [" + i + "]: " + theMatchNode.getMatch(i).treeString());
	    }
	}

	return true;
    }
}
