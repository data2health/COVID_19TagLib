package org.cd2h.covid;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import edu.uiowa.GRID.Address;
import edu.uiowa.GRID.Institute;
import edu.uiowa.GeoNames.GeoName;
import edu.uiowa.NLP_grammar.syntaxTree;
import edu.uiowa.NLP_grammar.syntaxMatch.syntaxMatch;
import edu.uiowa.NLP_grammar.syntaxMatch.syntaxMatcher;
import edu.uiowa.NLP_grammar.syntaxMatch.comparator.emailComparator;
import edu.uiowa.NLP_grammar.syntaxMatch.comparator.entityComparator;
import edu.uiowa.NLP_grammar.syntaxMatch.comparator.urlComparator;
import edu.uiowa.PubMedCentral.comparator.PersonComparator;
import edu.uiowa.PubMedCentral.entity.AnatomicalStructure;
import edu.uiowa.PubMedCentral.entity.BiologicalFunction;
import edu.uiowa.PubMedCentral.entity.BodyPart;
import edu.uiowa.PubMedCentral.entity.ClinicalTrialRegistration;
import edu.uiowa.PubMedCentral.entity.Collaboration;
import edu.uiowa.PubMedCentral.entity.Disease;
import edu.uiowa.PubMedCentral.entity.Event;
import edu.uiowa.PubMedCentral.entity.Finding;
import edu.uiowa.PubMedCentral.entity.Injury;
import edu.uiowa.PubMedCentral.entity.ManufacturedObject;
import edu.uiowa.PubMedCentral.entity.OrganicChemical;
import edu.uiowa.PubMedCentral.entity.Organism;
import edu.uiowa.PubMedCentral.entity.Organization;
import edu.uiowa.PubMedCentral.entity.PathologicalFunction;
import edu.uiowa.PubMedCentral.entity.Person;
import edu.uiowa.PubMedCentral.entity.PhysiologicalFunction;
import edu.uiowa.PubMedCentral.entity.PlaceName;
import edu.uiowa.PubMedCentral.entity.Resource;
import edu.uiowa.PubMedCentral.entity.TranscriptionFactor;
import edu.uiowa.concept.Concept;
import edu.uiowa.concept.ExhaustiveVectorConceptRecognizer;
import edu.uiowa.concept.detector.GRIDDetector;
import edu.uiowa.concept.detector.GeoNamesDetector;
import edu.uiowa.concept.detector.UMLSDetector;
import edu.uiowa.entity.Entity;
import edu.uiowa.extraction.LocalProperties;
import edu.uiowa.extraction.Template;
import edu.uiowa.extraction.TemplateInstantiator;
import edu.uiowa.lex.Sentence;
import edu.uiowa.lex.basicLexerToken;

public class BioRxivInstantiator extends TemplateInstantiator {
    Hashtable<String, Person> personHash = new Hashtable<String, Person>();
    Hashtable<String, PlaceName> placeNameHash = new Hashtable<String, PlaceName>();
    Hashtable<String, Organism> organismHash = new Hashtable<String, Organism>();
    Hashtable<String, OrganicChemical> organicChemicalHash = new Hashtable<String, OrganicChemical>();
    Hashtable<String, Event> eventHash = new Hashtable<String, Event>();
    Hashtable<String, Disease> diseaseHash = new Hashtable<String, Disease>();
    Hashtable<String, AnatomicalStructure> anatomicalStructureHash = new Hashtable<String, AnatomicalStructure>();
    Hashtable<String, BiologicalFunction> biologicalFunctionHash = new Hashtable<String, BiologicalFunction>();
    Hashtable<String, BodyPart> bodyPartHash = new Hashtable<String, BodyPart>();
    Hashtable<String, Finding> findingHash = new Hashtable<String, Finding>();
    Hashtable<String, Injury> injuryHash = new Hashtable<String, Injury>();
    Hashtable<String, ManufacturedObject> manufacturedObjectHash = new Hashtable<String, ManufacturedObject>();
    Hashtable<String, PathologicalFunction> pathologicalFunctionHash = new Hashtable<String, PathologicalFunction>();
    Hashtable<String, PhysiologicalFunction> physiologicalFunctionHash = new Hashtable<String, PhysiologicalFunction>();
    Hashtable<String, TranscriptionFactor> transcriptionFactorHash = new Hashtable<String, TranscriptionFactor>();
    
    PersonComparator personComparator = new PersonComparator();
    String doi = null;

    public BioRxivInstantiator(LocalProperties prop_file, Connection conn, String  doi) throws ClassNotFoundException, SQLException {
	super(prop_file, conn);
	this.doi = doi;
	initialize();
    }
    
    private void initialize() {
	breakToken.put("(", "");
	breakToken.put(")", "");
	breakToken.put(":", "");
	breakToken.put("award", "");
	breakToken.put("Award", "");
	breakToken.put("grant", "");
	breakToken.put("Grant", "");
	breakToken.put("contract", "");
	breakToken.put("Contract", "");
	breakToken.put("awards", "");
	breakToken.put("Awards", "");
	breakToken.put("grants", "");
	breakToken.put("Grants", "");
	breakToken.put("contracts", "");
	breakToken.put("Contracts", "");
    }

    @Override
    protected void instantiateEntity(int id, syntaxTree constituent, Template template) throws Exception {
	// not used in this application
    }

   @Override
    protected void instantiateEntity(String id, syntaxTree constituent, Template template) throws Exception {
	logger.debug("matched template: " + template);
	switch (template.relation) {
	case "clinical_trial":
	    ClinicalTrialRegistration clinicalTrial = clinicalTrialMatch(constituent, template.tgrep);
	    if (clinicalTrial == null) {
		// unlike some of the other patterns, these drive on specific strings in the text, so a null match is frequent
		logger.debug("clinical_trial instantiation failed! : " + clinicalTrial);
		logger.debug("\t\t" + template.tgrep);
		logger.debug("\t\t" + constituent.getFragmentString());
		logger.debug("\t\t" + constituent.treeString());
		break;
	    }
	    storeClinicalTrial(doi, clinicalTrial);
	    constituent.setEntityClass("ClinicalTrial");
	    bindNamedEntity(constituent, template, clinicalTrial);
	    break;
	case "anatomical_structure":
	    AnatomicalStructure anatomicalStructure = anatomicalStructureMatch(constituent, template.tgrep);
	    if (anatomicalStructure == null) {
		logger.debug("anatomicalStructure instantiation failed! : " + anatomicalStructure);
		logger.debug("\t\t" + template.tgrep);
		logger.debug("\t\t" + constituent.getFragmentString());
		logger.debug("\t\t" + constituent.treeString());
		break;
	    }
	    storeAnatomicalStructure(doi, anatomicalStructure);
	    constituent.setEntityClass("AnatomicalStructure");
	    bindNamedEntity(constituent, template, anatomicalStructure);
	    break;
	case "biological_function":
	    BiologicalFunction biologicalFunction = biologicalFunctionMatch(constituent, template.tgrep);
	    if (biologicalFunction == null) {
		logger.debug("biologicalFunction instantiation failed! : " + biologicalFunction);
		logger.debug("\t\t" + template.tgrep);
		logger.debug("\t\t" + constituent.getFragmentString());
		logger.debug("\t\t" + constituent.treeString());
		break;
	    }
	    storeBiologicalFunction(doi, biologicalFunction);
	    constituent.setEntityClass("BiologicalFunction");
	    bindNamedEntity(constituent, template, biologicalFunction);
	    break;
	case "body_part":
	    BodyPart bodyPart = bodyPartMatch(constituent, template.tgrep);
	    if (bodyPart == null) {
		logger.debug("bodyPart instantiation failed! : " + bodyPart);
		logger.debug("\t\t" + template.tgrep);
		logger.debug("\t\t" + constituent.getFragmentString());
		logger.debug("\t\t" + constituent.treeString());
		break;
	    }
	    storeBodyPart(doi, bodyPart);
	    constituent.setEntityClass("BodyPart");
	    bindNamedEntity(constituent, template, bodyPart);
	    break;
	case "disease":
	    Disease disease = diseaseMatch(constituent, template.tgrep);
	    if (disease == null) {
		logger.debug("disease instantiation failed! : " + disease);
		logger.debug("\t\t" + template.tgrep);
		logger.debug("\t\t" + constituent.getFragmentString());
		logger.debug("\t\t" + constituent.treeString());
		break;
	    }
	    storeDisease(doi, disease);
	    constituent.setEntityClass("Disease");
	    bindNamedEntity(constituent, template, disease);
	    break;
	case "event":
	    Event event = eventMatch(constituent, template.tgrep);
	    if (event == null) {
		logger.debug("event instantiation failed! : " + event);
		logger.debug("\t\t" + template.tgrep);
		logger.debug("\t\t" + constituent.getFragmentString());
		logger.debug("\t\t" + constituent.treeString());
		break;
	    }
	    storeEvent(doi, event);
	    constituent.setEntityClass("Event");
	    bindNamedEntity(constituent, template, event);
	    break;
	case "finding":
	    Finding finding = findingMatch(constituent, template.tgrep);
	    if (finding == null) {
		logger.debug("finding instantiation failed! : " + finding);
		logger.debug("\t\t" + template.tgrep);
		logger.debug("\t\t" + constituent.getFragmentString());
		logger.debug("\t\t" + constituent.treeString());
		break;
	    }
	    storeFinding(doi, finding);
	    constituent.setEntityClass("Event");
	    bindNamedEntity(constituent, template, finding);
	    break;
	case "injury":
	    Injury injury = injuryMatch(constituent, template.tgrep);
	    if (injury == null) {
		logger.debug("injury instantiation failed! : " + injury);
		logger.debug("\t\t" + template.tgrep);
		logger.debug("\t\t" + constituent.getFragmentString());
		logger.debug("\t\t" + constituent.treeString());
		break;
	    }
	    storeInjury(doi, injury);
	    constituent.setEntityClass("Injury");
	    bindNamedEntity(constituent, template, injury);
	    break;
	case "location":
	    PlaceName place = placeNameMatch(constituent, template.tgrep);
	    if (place == null) {
		logger.debug("location instantiation failed! : " + place);
		logger.debug("\t\t" + template.tgrep);
		logger.debug("\t\t" + constituent.getFragmentString());
		logger.debug("\t\t" + constituent.treeString());
		break;
	    }
	    storePlaceName(doi, place);
	    constituent.setEntityClass("Location");
	    bindNamedEntity(constituent, template, place);
	    break;
	case "manufactored_object":
	    ManufacturedObject manufacturedObject = manufacturedObjectMatch(constituent, template.tgrep);
	    if (manufacturedObject == null) {
		logger.debug("injury instantiation failed! : " + manufacturedObject);
		logger.debug("\t\t" + template.tgrep);
		logger.debug("\t\t" + constituent.getFragmentString());
		logger.debug("\t\t" + constituent.treeString());
		break;
	    }
	    storeManufacturedObject(doi, manufacturedObject);
	    constituent.setEntityClass("ManufacturedObject");
	    bindNamedEntity(constituent, template, manufacturedObject);
	    break;
	case "organic_chemical":
	    OrganicChemical organicChemical = organicChemicalMatch(constituent, template.tgrep);
	    if (organicChemical == null) {
		logger.debug("organic_chemical instantiation failed! : " + organicChemical);
		logger.debug("\t\t" + template.tgrep);
		logger.debug("\t\t" + constituent.getFragmentString());
		logger.debug("\t\t" + constituent.treeString());
		break;
	    }
	    storeOrganicChemical(doi, organicChemical);
	    constituent.setEntityClass("OrganicChemical");
	    bindNamedEntity(constituent, template, organicChemical);
	    break;
	case "organism":
	    Organism organism =  organismMatch(constituent, template.tgrep);
	    if (organism == null) {
		logger.debug("organism instantiation failed! : " + organism);
		logger.debug("\t\t" + template.tgrep);
		logger.debug("\t\t" + constituent.getFragmentString());
		logger.debug("\t\t" + constituent.treeString());
		break;
	    }
	    storeOrganism(doi, organism);
	    constituent.setEntityClass("Organism");
	    bindNamedEntity(constituent, template, organism);
	    break;
	case "pathological_function":
	    PathologicalFunction pathologicalFunction =  pathologicalFunctionMatch(constituent, template.tgrep);
	    if (pathologicalFunction == null) {
		logger.debug("organism instantiation failed! : " + pathologicalFunction);
		logger.debug("\t\t" + template.tgrep);
		logger.debug("\t\t" + constituent.getFragmentString());
		logger.debug("\t\t" + constituent.treeString());
		break;
	    }
	    storePathologicalFunction(doi, pathologicalFunction);
	    constituent.setEntityClass("PathologicalFunction");
	    bindNamedEntity(constituent, template, pathologicalFunction);
	    break;
	case "physiological_function":
	    PhysiologicalFunction physiologicalFunction =  physiologicalFunctionMatch(constituent, template.tgrep);
	    if (physiologicalFunction == null) {
		logger.debug("organism instantiation failed! : " + physiologicalFunction);
		logger.debug("\t\t" + template.tgrep);
		logger.debug("\t\t" + constituent.getFragmentString());
		logger.debug("\t\t" + constituent.treeString());
		break;
	    }
	    storePhysiologicalFunction(doi, physiologicalFunction);
	    constituent.setEntityClass("PhysiologicalFunction");
	    bindNamedEntity(constituent, template, physiologicalFunction);
	    break;
	case "transcription_factor":
	    TranscriptionFactor transcriptionFactor =  transcriptionFactorMatch(constituent, template.tgrep);
	    if (transcriptionFactor == null) {
		logger.debug("organism instantiation failed! : " + transcriptionFactor);
		logger.debug("\t\t" + template.tgrep);
		logger.debug("\t\t" + constituent.getFragmentString());
		logger.debug("\t\t" + constituent.treeString());
		break;
	    }
	    storeTranscriptionFactor(doi, transcriptionFactor);
	    constituent.setEntityClass("TranscriptionFactor");
	    bindNamedEntity(constituent, template, transcriptionFactor);
	    break;
	default:
	    break;
	}
	
    }
    
   private void storeAnatomicalStructure(String doi, AnatomicalStructure anatomicalStructure) throws SQLException {
	int id = 0;

	AnatomicalStructure match = anatomicalStructureHash.get(anatomicalStructure.toString());
	if (match != null) {
	    anatomicalStructure.setID(match.getID());
	} else {
	    try {
		PreparedStatement insert = conn.prepareStatement("insert into covid_model.anatomical_structure(anatomical_structure,umls_id,umls_match_string) values(?,?,?)", Statement.RETURN_GENERATED_KEYS);
		insert.setString(1, anatomicalStructure.toString());
		insert.setString(2, anatomicalStructure.getUmlsConcept());
		insert.setString(3, anatomicalStructure.getUmlsMatchString());
		insert.execute();
		ResultSet rs = insert.getGeneratedKeys();
		while (rs.next()) {
		    id = rs.getInt(1);
		    logger.info("new biologicalFunction id: " + id + " : " + anatomicalStructure);
		    anatomicalStructure.setID(id);
		    anatomicalStructureHash.put(anatomicalStructure.toString(), anatomicalStructure);
		}
		insert.close();
	    } catch (SQLException e) {
		if (e.getSQLState().equals("23505")) {
		    conn.rollback();
		    PreparedStatement select = conn.prepareStatement("select id from covid_model.anatomical_structure where anatomical_structure = ?");
		    select.setString(1, anatomicalStructure.toString());
		    ResultSet rs = select.executeQuery();
		    while (rs.next()) {
			id = rs.getInt(1);
			logger.debug("organism id: " + id);
			anatomicalStructure.setID(id);
			anatomicalStructureHash.put(anatomicalStructure.toString(), anatomicalStructure);
		    }
		    select.close();

		} else {
		    e.printStackTrace();
		}
	    } finally {
		conn.commit();
	    }
	}
	try {
	    PreparedStatement insert = conn.prepareStatement("insert into covid_model.anatomical_structure_mention(anatomical_structure_id,doi) values(?,?)");
	    insert.setInt(1, anatomicalStructure.getID());
	    insert.setString(2, doi);
	    insert.execute();
	} catch (SQLException e) {
	    if (e.getSQLState().equals("23505")) {
		conn.rollback();
	    } else {
		e.printStackTrace();
	    }
	} finally {
	    conn.commit();
	}
	
   }

   private AnatomicalStructure anatomicalStructureMatch(syntaxTree constituent, String pattern) throws Exception {
       AnatomicalStructure theAnatomicalStructure = null;
	syntaxMatcher theMatcher = new syntaxMatcher(pattern);
	if (theMatcher.isMatch(constituent)) {
	    Vector<basicLexerToken> matchVector = theMatcher.matchesAsTokens();
	    logger.debug("organism vector: " + matchVector);
	    theAnatomicalStructure = new AnatomicalStructure(pruneMatchVector(matchVector));
	    logger.debug("biological function entity: " + theAnatomicalStructure);

	    ExhaustiveVectorConceptRecognizer umlsRecognizer = new ExhaustiveVectorConceptRecognizer(new UMLSDetector(), ExhaustiveVectorConceptRecognizer.Direction.BOTH, false);
	    List umlsResults = umlsRecognizer.recognize(new Sentence(matchVector), getScanFence(constituent));
	    logger.debug("UMLS results: " + umlsResults);
	    for (Concept concept : (List<Concept>)umlsResults) {
		Vector<edu.uiowa.UMLS.Concept> umlsConcepts = (Vector<edu.uiowa.UMLS.Concept>)concept.getKey();
		if (umlsConcepts.size() == 1) {
		    theAnatomicalStructure.setUmlsMatch(umlsConcepts.get(0).getConceptID(),(String)concept.getPhrase() + " := " + umlsConcepts.get(0));
		    logger.debug("\tUMLS match: " + umlsConcepts.get(0));
		} else {
		    theAnatomicalStructure.setUmlsMatch(umlsConcepts.get(0).getConceptID(),(String)concept.getPhrase() + " :? " + umlsConcepts.get(0));
		    for (edu.uiowa.UMLS.Concept umlsConcept : umlsConcepts) {
			logger.debug("\tUMLS option: " + umlsConcept);
		    }
		}
	    }
	}
	return theAnatomicalStructure;
  }

   private void storeBiologicalFunction(String doi, BiologicalFunction biologicalFunction) throws SQLException {
	int id = 0;

	BiologicalFunction match = biologicalFunctionHash.get(biologicalFunction.toString());
	if (match != null) {
	    biologicalFunction.setID(match.getID());
	} else {
	    try {
		PreparedStatement insert = conn.prepareStatement("insert into covid_model.biological_function(biological_function,umls_id,umls_match_string) values(?,?,?)", Statement.RETURN_GENERATED_KEYS);
		insert.setString(1, biologicalFunction.toString());
		insert.setString(2, biologicalFunction.getUmlsConcept());
		insert.setString(3, biologicalFunction.getUmlsMatchString());
		insert.execute();
		ResultSet rs = insert.getGeneratedKeys();
		while (rs.next()) {
		    id = rs.getInt(1);
		    logger.info("new biologicalFunction id: " + id + " : " + biologicalFunction);
		    biologicalFunction.setID(id);
		    biologicalFunctionHash.put(biologicalFunction.toString(), biologicalFunction);
		}
		insert.close();
	    } catch (SQLException e) {
		if (e.getSQLState().equals("23505")) {
		    conn.rollback();
		    PreparedStatement select = conn.prepareStatement("select id from covid_model.biological_function where biological_function = ?");
		    select.setString(1, biologicalFunction.toString());
		    ResultSet rs = select.executeQuery();
		    while (rs.next()) {
			id = rs.getInt(1);
			logger.debug("organism id: " + id);
			biologicalFunction.setID(id);
			biologicalFunctionHash.put(biologicalFunction.toString(), biologicalFunction);
		    }
		    select.close();

		} else {
		    e.printStackTrace();
		}
	    } finally {
		conn.commit();
	    }
	}
	try {
	    PreparedStatement insert = conn.prepareStatement("insert into covid_model.biological_function_mention(biological_function_id,doi) values(?,?)");
	    insert.setInt(1, biologicalFunction.getID());
	    insert.setString(2, doi);
	    insert.execute();
	} catch (SQLException e) {
	    if (e.getSQLState().equals("23505")) {
		conn.rollback();
	    } else {
		e.printStackTrace();
	    }
	} finally {
	    conn.commit();
	}
	
   }

   private BiologicalFunction biologicalFunctionMatch(syntaxTree constituent, String pattern) throws Exception {
       BiologicalFunction theBiologicalFunction = null;
	syntaxMatcher theMatcher = new syntaxMatcher(pattern);
	if (theMatcher.isMatch(constituent)) {
	    Vector<basicLexerToken> matchVector = theMatcher.matchesAsTokens();
	    logger.debug("organism vector: " + matchVector);
	    theBiologicalFunction = new BiologicalFunction(pruneMatchVector(matchVector));
	    logger.debug("biological function entity: " + theBiologicalFunction);

	    ExhaustiveVectorConceptRecognizer umlsRecognizer = new ExhaustiveVectorConceptRecognizer(new UMLSDetector(), ExhaustiveVectorConceptRecognizer.Direction.BOTH, false);
	    List umlsResults = umlsRecognizer.recognize(new Sentence(matchVector), getScanFence(constituent));
	    logger.debug("UMLS results: " + umlsResults);
	    for (Concept concept : (List<Concept>)umlsResults) {
		Vector<edu.uiowa.UMLS.Concept> umlsConcepts = (Vector<edu.uiowa.UMLS.Concept>)concept.getKey();
		if (umlsConcepts.size() == 1) {
		    theBiologicalFunction.setUmlsMatch(umlsConcepts.get(0).getConceptID(),(String)concept.getPhrase() + " := " + umlsConcepts.get(0));
		    logger.debug("\tUMLS match: " + umlsConcepts.get(0));
		} else {
		    theBiologicalFunction.setUmlsMatch(umlsConcepts.get(0).getConceptID(),(String)concept.getPhrase() + " :? " + umlsConcepts.get(0));
		    for (edu.uiowa.UMLS.Concept umlsConcept : umlsConcepts) {
			logger.debug("\tUMLS option: " + umlsConcept);
		    }
		}
	    }
	}
	return theBiologicalFunction;
  }

   private void storeBodyPart(String doi, BodyPart bodyPart) throws SQLException {
	int id = 0;

	BodyPart match = bodyPartHash.get(bodyPart.toString());
	if (match != null) {
	    bodyPart.setID(match.getID());
	} else {
	    try {
		PreparedStatement insert = conn.prepareStatement("insert into covid_model.body_part(body_part,umls_id,umls_match_string) values(?,?,?)", Statement.RETURN_GENERATED_KEYS);
		insert.setString(1, bodyPart.toString());
		insert.setString(2, bodyPart.getUmlsConcept());
		insert.setString(3, bodyPart.getUmlsMatchString());
		insert.execute();
		ResultSet rs = insert.getGeneratedKeys();
		while (rs.next()) {
		    id = rs.getInt(1);
		    logger.info("new biologicalFunction id: " + id + " : " + bodyPart);
		    bodyPart.setID(id);
		    bodyPartHash.put(bodyPart.toString(), bodyPart);
		}
		insert.close();
	    } catch (SQLException e) {
		if (e.getSQLState().equals("23505")) {
		    conn.rollback();
		    PreparedStatement select = conn.prepareStatement("select id from covid_model.body_part where body_part = ?");
		    select.setString(1, bodyPart.toString());
		    ResultSet rs = select.executeQuery();
		    while (rs.next()) {
			id = rs.getInt(1);
			logger.debug("organism id: " + id);
			bodyPart.setID(id);
			bodyPartHash.put(bodyPart.toString(), bodyPart);
		    }
		    select.close();

		} else {
		    e.printStackTrace();
		}
	    } finally {
		conn.commit();
	    }
	}
	try {
	    PreparedStatement insert = conn.prepareStatement("insert into covid_model.body_part_mention(body_part_id,doi) values(?,?)");
	    insert.setInt(1, bodyPart.getID());
	    insert.setString(2, doi);
	    insert.execute();
	} catch (SQLException e) {
	    if (e.getSQLState().equals("23505")) {
		conn.rollback();
	    } else {
		e.printStackTrace();
	    }
	} finally {
	    conn.commit();
	}
	
   }

   private BodyPart bodyPartMatch(syntaxTree constituent, String pattern) throws Exception {
       BodyPart theBodyPart = null;
	syntaxMatcher theMatcher = new syntaxMatcher(pattern);
	if (theMatcher.isMatch(constituent)) {
	    Vector<basicLexerToken> matchVector = theMatcher.matchesAsTokens();
	    logger.debug("organism vector: " + matchVector);
	    theBodyPart = new BodyPart(pruneMatchVector(matchVector));
	    logger.debug("biological function entity: " + theBodyPart);

	    ExhaustiveVectorConceptRecognizer umlsRecognizer = new ExhaustiveVectorConceptRecognizer(new UMLSDetector(), ExhaustiveVectorConceptRecognizer.Direction.BOTH, false);
	    List umlsResults = umlsRecognizer.recognize(new Sentence(matchVector), getScanFence(constituent));
	    logger.debug("UMLS results: " + umlsResults);
	    for (Concept concept : (List<Concept>)umlsResults) {
		Vector<edu.uiowa.UMLS.Concept> umlsConcepts = (Vector<edu.uiowa.UMLS.Concept>)concept.getKey();
		if (umlsConcepts.size() == 1) {
		    theBodyPart.setUmlsMatch(umlsConcepts.get(0).getConceptID(),(String)concept.getPhrase() + " := " + umlsConcepts.get(0));
		    logger.debug("\tUMLS match: " + umlsConcepts.get(0));
		} else {
		    theBodyPart.setUmlsMatch(umlsConcepts.get(0).getConceptID(),(String)concept.getPhrase() + " :? " + umlsConcepts.get(0));
		    for (edu.uiowa.UMLS.Concept umlsConcept : umlsConcepts) {
			logger.debug("\tUMLS option: " + umlsConcept);
		    }
		}
	    }
	}
	return theBodyPart;
  }

   private void storeFinding(String doi, Finding finding) throws SQLException {
	int id = 0;

	Finding match = findingHash.get(finding.toString());
	if (match != null) {
	    finding.setID(match.getID());
	} else {
	    try {
		PreparedStatement insert = conn.prepareStatement("insert into covid_model.finding(finding,umls_id,umls_match_string) values(?,?,?)", Statement.RETURN_GENERATED_KEYS);
		insert.setString(1, finding.toString());
		insert.setString(2, finding.getUmlsConcept());
		insert.setString(3, finding.getUmlsMatchString());
		insert.execute();
		ResultSet rs = insert.getGeneratedKeys();
		while (rs.next()) {
		    id = rs.getInt(1);
		    logger.info("new finding id: " + id + " : " + finding);
		    finding.setID(id);
		    findingHash.put(finding.toString(), finding);
		}
		insert.close();
	    } catch (SQLException e) {
		if (e.getSQLState().equals("23505")) {
		    conn.rollback();
		    PreparedStatement select = conn.prepareStatement("select id from covid_model.finding where finding = ?");
		    select.setString(1, finding.toString());
		    ResultSet rs = select.executeQuery();
		    while (rs.next()) {
			id = rs.getInt(1);
			logger.debug("finding id: " + id);
			finding.setID(id);
			findingHash.put(finding.toString(), finding);
		    }
		    select.close();

		} else {
		    e.printStackTrace();
		}
	    } finally {
		conn.commit();
	    }
	}
	try {
	    PreparedStatement insert = conn.prepareStatement("insert into covid_model.finding_mention(finding_id,doi) values(?,?)");
	    insert.setInt(1, finding.getID());
	    insert.setString(2, doi);
	    insert.execute();
	} catch (SQLException e) {
	    if (e.getSQLState().equals("23505")) {
		conn.rollback();
	    } else {
		e.printStackTrace();
	    }
	} finally {
	    conn.commit();
	}
	
   }

   private Finding findingMatch(syntaxTree constituent, String pattern) throws Exception {
       Finding theFinding = null;
	syntaxMatcher theMatcher = new syntaxMatcher(pattern);
	if (theMatcher.isMatch(constituent)) {
	    Vector<basicLexerToken> matchVector = theMatcher.matchesAsTokens();
	    logger.debug("organism vector: " + matchVector);
	    theFinding = new Finding(pruneMatchVector(matchVector));
	    logger.debug("biological function entity: " + theFinding);

	    ExhaustiveVectorConceptRecognizer umlsRecognizer = new ExhaustiveVectorConceptRecognizer(new UMLSDetector(), ExhaustiveVectorConceptRecognizer.Direction.BOTH, false);
	    List umlsResults = umlsRecognizer.recognize(new Sentence(matchVector), getScanFence(constituent));
	    logger.debug("UMLS results: " + umlsResults);
	    for (Concept concept : (List<Concept>)umlsResults) {
		Vector<edu.uiowa.UMLS.Concept> umlsConcepts = (Vector<edu.uiowa.UMLS.Concept>)concept.getKey();
		if (umlsConcepts.size() == 1) {
		    theFinding.setUmlsMatch(umlsConcepts.get(0).getConceptID(),(String)concept.getPhrase() + " := " + umlsConcepts.get(0));
		    logger.debug("\tUMLS match: " + umlsConcepts.get(0));
		} else {
		    theFinding.setUmlsMatch(umlsConcepts.get(0).getConceptID(),(String)concept.getPhrase() + " :? " + umlsConcepts.get(0));
		    for (edu.uiowa.UMLS.Concept umlsConcept : umlsConcepts) {
			logger.debug("\tUMLS option: " + umlsConcept);
		    }
		}
	    }
	}
	return theFinding;
  }

   private void storeInjury(String doi, Injury injury) throws SQLException {
	int id = 0;

	Injury match = injuryHash.get(injury.toString());
	if (match != null) {
	    injury.setID(match.getID());
	} else {
	    try {
		PreparedStatement insert = conn.prepareStatement("insert into covid_model.injury(injury,umls_id,umls_match_string) values(?,?,?)", Statement.RETURN_GENERATED_KEYS);
		insert.setString(1, injury.toString());
		insert.setString(2, injury.getUmlsConcept());
		insert.setString(3, injury.getUmlsMatchString());
		insert.execute();
		ResultSet rs = insert.getGeneratedKeys();
		while (rs.next()) {
		    id = rs.getInt(1);
		    logger.info("new injury id: " + id + " : " + injury);
		    injury.setID(id);
		    injuryHash.put(injury.toString(), injury);
		}
		insert.close();
	    } catch (SQLException e) {
		if (e.getSQLState().equals("23505")) {
		    conn.rollback();
		    PreparedStatement select = conn.prepareStatement("select id from covid_model.injury where injury = ?");
		    select.setString(1, injury.toString());
		    ResultSet rs = select.executeQuery();
		    while (rs.next()) {
			id = rs.getInt(1);
			logger.debug("injury id: " + id);
			injury.setID(id);
			injuryHash.put(injury.toString(), injury);
		    }
		    select.close();

		} else {
		    e.printStackTrace();
		}
	    } finally {
		conn.commit();
	    }
	}
	try {
	    PreparedStatement insert = conn.prepareStatement("insert into covid_model.injury_mention(injury_id,doi) values(?,?)");
	    insert.setInt(1, injury.getID());
	    insert.setString(2, doi);
	    insert.execute();
	} catch (SQLException e) {
	    if (e.getSQLState().equals("23505")) {
		conn.rollback();
	    } else {
		e.printStackTrace();
	    }
	} finally {
	    conn.commit();
	}
	
   }

   private Injury injuryMatch(syntaxTree constituent, String pattern) throws Exception {
       Injury theInjury = null;
	syntaxMatcher theMatcher = new syntaxMatcher(pattern);
	if (theMatcher.isMatch(constituent)) {
	    Vector<basicLexerToken> matchVector = theMatcher.matchesAsTokens();
	    logger.debug("injury vector: " + matchVector);
	    theInjury = new Injury(pruneMatchVector(matchVector));
	    logger.debug("injury entity: " + theInjury);

	    ExhaustiveVectorConceptRecognizer umlsRecognizer = new ExhaustiveVectorConceptRecognizer(new UMLSDetector(), ExhaustiveVectorConceptRecognizer.Direction.BOTH, false);
	    List umlsResults = umlsRecognizer.recognize(new Sentence(matchVector), getScanFence(constituent));
	    logger.debug("UMLS results: " + umlsResults);
	    for (Concept concept : (List<Concept>)umlsResults) {
		Vector<edu.uiowa.UMLS.Concept> umlsConcepts = (Vector<edu.uiowa.UMLS.Concept>)concept.getKey();
		if (umlsConcepts.size() == 1) {
		    theInjury.setUmlsMatch(umlsConcepts.get(0).getConceptID(),(String)concept.getPhrase() + " := " + umlsConcepts.get(0));
		    logger.debug("\tUMLS match: " + umlsConcepts.get(0));
		} else {
		    theInjury.setUmlsMatch(umlsConcepts.get(0).getConceptID(),(String)concept.getPhrase() + " :? " + umlsConcepts.get(0));
		    for (edu.uiowa.UMLS.Concept umlsConcept : umlsConcepts) {
			logger.debug("\tUMLS option: " + umlsConcept);
		    }
		}
	    }
	}
	return theInjury;
  }

   private void storeManufacturedObject(String doi, ManufacturedObject manufacturedObject) throws SQLException {
	int id = 0;

	ManufacturedObject match = manufacturedObjectHash.get(manufacturedObject.toString());
	if (match != null) {
	    manufacturedObject.setID(match.getID());
	} else {
	    try {
		PreparedStatement insert = conn.prepareStatement("insert into covid_model.manufactured_object(manufactured_object,umls_id,umls_match_string) values(?,?,?)", Statement.RETURN_GENERATED_KEYS);
		insert.setString(1, manufacturedObject.toString());
		insert.setString(2, manufacturedObject.getUmlsConcept());
		insert.setString(3, manufacturedObject.getUmlsMatchString());
		insert.execute();
		ResultSet rs = insert.getGeneratedKeys();
		while (rs.next()) {
		    id = rs.getInt(1);
		    logger.info("new manufactured object id: " + id + " : " + manufacturedObject);
		    manufacturedObject.setID(id);
		    manufacturedObjectHash.put(manufacturedObject.toString(), manufacturedObject);
		}
		insert.close();
	    } catch (SQLException e) {
		if (e.getSQLState().equals("23505")) {
		    conn.rollback();
		    PreparedStatement select = conn.prepareStatement("select id from covid_model.manufactured_object where manufactured_object = ?");
		    select.setString(1, manufacturedObject.toString());
		    ResultSet rs = select.executeQuery();
		    while (rs.next()) {
			id = rs.getInt(1);
			logger.debug("manufactured object id: " + id);
			manufacturedObject.setID(id);
			manufacturedObjectHash.put(manufacturedObject.toString(), manufacturedObject);
		    }
		    select.close();

		} else {
		    e.printStackTrace();
		}
	    } finally {
		conn.commit();
	    }
	}
	try {
	    PreparedStatement insert = conn.prepareStatement("insert into covid_model.manufactured_object_mention(manufactured_object_id,doi) values(?,?)");
	    insert.setInt(1, manufacturedObject.getID());
	    insert.setString(2, doi);
	    insert.execute();
	} catch (SQLException e) {
	    if (e.getSQLState().equals("23505")) {
		conn.rollback();
	    } else {
		e.printStackTrace();
	    }
	} finally {
	    conn.commit();
	}
	
   }

   private ManufacturedObject manufacturedObjectMatch(syntaxTree constituent, String pattern) throws Exception {
       ManufacturedObject theManufacturedObject = null;
	syntaxMatcher theMatcher = new syntaxMatcher(pattern);
	if (theMatcher.isMatch(constituent)) {
	    Vector<basicLexerToken> matchVector = theMatcher.matchesAsTokens();
	    logger.debug("manufactured_object vector: " + matchVector);
	    theManufacturedObject = new ManufacturedObject(pruneMatchVector(matchVector));
	    logger.debug("manufactured_object entity: " + theManufacturedObject);

	    ExhaustiveVectorConceptRecognizer umlsRecognizer = new ExhaustiveVectorConceptRecognizer(new UMLSDetector(), ExhaustiveVectorConceptRecognizer.Direction.BOTH, false);
	    List umlsResults = umlsRecognizer.recognize(new Sentence(matchVector), getScanFence(constituent));
	    logger.debug("UMLS results: " + umlsResults);
	    for (Concept concept : (List<Concept>)umlsResults) {
		Vector<edu.uiowa.UMLS.Concept> umlsConcepts = (Vector<edu.uiowa.UMLS.Concept>)concept.getKey();
		if (umlsConcepts.size() == 1) {
		    theManufacturedObject.setUmlsMatch(umlsConcepts.get(0).getConceptID(),(String)concept.getPhrase() + " := " + umlsConcepts.get(0));
		    logger.debug("\tUMLS match: " + umlsConcepts.get(0));
		} else {
		    theManufacturedObject.setUmlsMatch(umlsConcepts.get(0).getConceptID(),(String)concept.getPhrase() + " :? " + umlsConcepts.get(0));
		    for (edu.uiowa.UMLS.Concept umlsConcept : umlsConcepts) {
			logger.debug("\tUMLS option: " + umlsConcept);
		    }
		}
	    }
	}
	return theManufacturedObject;
  }

    private void storeOrganism(String doi, Organism organism) throws SQLException {
	int id = 0;

	Organism match = organismHash.get(organism.toString());
	if (match != null) {
	    organism.setID(match.getID());
	} else {
	    try {
		PreparedStatement insert = conn.prepareStatement("insert into covid_model.organism(organism,umls_id,umls_match_string) values(?,?,?)", Statement.RETURN_GENERATED_KEYS);
		insert.setString(1, organism.toString());
		insert.setString(2, organism.getUmlsConcept());
		insert.setString(3, organism.getUmlsMatchString());
		insert.execute();
		ResultSet rs = insert.getGeneratedKeys();
		while (rs.next()) {
		    id = rs.getInt(1);
		    logger.info("new organism id: " + id + " : " + organism);
		    organism.setID(id);
		    organismHash.put(organism.toString(), organism);
		}
		insert.close();
	    } catch (SQLException e) {
		if (e.getSQLState().equals("23505")) {
		    conn.rollback();
		    PreparedStatement select = conn.prepareStatement("select id from covid_model.organism where organism = ?");
		    select.setString(1, organism.toString());
		    ResultSet rs = select.executeQuery();
		    while (rs.next()) {
			id = rs.getInt(1);
			logger.debug("organism id: " + id);
			organism.setID(id);
			organismHash.put(organism.toString(), organism);
		    }
		    select.close();

		} else {
		    e.printStackTrace();
		}
	    } finally {
		conn.commit();
	    }
	}
	try {
	    PreparedStatement insert = conn.prepareStatement("insert into covid_model.organism_mention(organism_id,doi) values(?,?)");
	    insert.setInt(1, organism.getID());
	    insert.setString(2, doi);
	    insert.execute();
	} catch (SQLException e) {
	    if (e.getSQLState().equals("23505")) {
		conn.rollback();
	    } else {
		e.printStackTrace();
	    }
	} finally {
	    conn.commit();
	}
	
    }

    private Organism organismMatch(syntaxTree constituent, String pattern) throws Exception {
	Organism theOrganism = null;
	syntaxMatcher theMatcher = new syntaxMatcher(pattern);
	if (theMatcher.isMatch(constituent)) {
	    Vector<basicLexerToken> matchVector = theMatcher.matchesAsTokens();
	    logger.debug("organism vector: " + matchVector);
	    theOrganism = new Organism(pruneMatchVector(matchVector));
	    logger.debug("organism entity: " + theOrganism);

	    ExhaustiveVectorConceptRecognizer umlsRecognizer = new ExhaustiveVectorConceptRecognizer(new UMLSDetector(), ExhaustiveVectorConceptRecognizer.Direction.BOTH, false);
	    List umlsResults = umlsRecognizer.recognize(new Sentence(matchVector), getScanFence(constituent));
	    logger.debug("UMLS results: " + umlsResults);
	    for (Concept concept : (List<Concept>)umlsResults) {
		Vector<edu.uiowa.UMLS.Concept> umlsConcepts = (Vector<edu.uiowa.UMLS.Concept>)concept.getKey();
		if (umlsConcepts.size() == 1) {
		    theOrganism.setUmlsMatch(umlsConcepts.get(0).getConceptID(),(String)concept.getPhrase() + " := " + umlsConcepts.get(0));
		    logger.debug("\tUMLS match: " + umlsConcepts.get(0));
		} else {
		    theOrganism.setUmlsMatch(umlsConcepts.get(0).getConceptID(),(String)concept.getPhrase() + " :? " + umlsConcepts.get(0));
		    for (edu.uiowa.UMLS.Concept umlsConcept : umlsConcepts) {
			logger.debug("\tUMLS option: " + umlsConcept);
		    }
		}
	    }
	}
	return theOrganism;
   }

    private void storeOrganicChemical(String doi, OrganicChemical organicChemical) throws SQLException {
	int id = 0;

	OrganicChemical match = organicChemicalHash.get(organicChemical.toString());
	if (match != null) {
	    organicChemical.setID(match.getID());
	} else {
	    try {
		PreparedStatement insert = conn.prepareStatement("insert into covid_model.organic_chemical(organic_chemical,umls_id,umls_match_string) values(?,?,?)", Statement.RETURN_GENERATED_KEYS);
		insert.setString(1, organicChemical.toString());
		insert.setString(2, organicChemical.getUmlsConcept());
		insert.setString(3, organicChemical.getUmlsMatchString());
		insert.execute();
		ResultSet rs = insert.getGeneratedKeys();
		while (rs.next()) {
		    id = rs.getInt(1);
		    logger.info("new organic chemical id: " + id + " : " + organicChemical);
		    organicChemical.setID(id);
		    organicChemicalHash.put(organicChemical.toString(), organicChemical);
		}
		insert.close();
	    } catch (SQLException e) {
		if (e.getSQLState().equals("23505")) {
		    conn.rollback();
		    PreparedStatement select = conn.prepareStatement("select id from covid_model.organic_chemical where organic_chemical = ?");
		    select.setString(1, organicChemical.toString());
		    ResultSet rs = select.executeQuery();
		    while (rs.next()) {
			id = rs.getInt(1);
			logger.debug("organic chemical id: " + id);
			organicChemical.setID(id);
			organicChemicalHash.put(organicChemical.toString(), organicChemical);
		    }
		    select.close();

		} else {
		    e.printStackTrace();
		}
	    } finally {
		conn.commit();
	    }
	}
	try {
	    PreparedStatement insert = conn.prepareStatement("insert into covid_model.organic_chemical_mention(organic_chemical_id,doi) values(?,?)");
	    insert.setInt(1, organicChemical.getID());
	    insert.setString(2, doi);
	    insert.execute();
	} catch (SQLException e) {
	    if (e.getSQLState().equals("23505")) {
		conn.rollback();
	    } else {
		e.printStackTrace();
	    }
	} finally {
	    conn.commit();
	}
    }

    private OrganicChemical organicChemicalMatch(syntaxTree constituent, String pattern) throws Exception {
	OrganicChemical theOrganicChemical = null;
	syntaxMatcher theMatcher = new syntaxMatcher(pattern);
	if (theMatcher.isMatch(constituent)) {
	    Vector<basicLexerToken> matchVector = theMatcher.matchesAsTokens();
	    logger.debug("organic chemical vector: " + matchVector);
	    theOrganicChemical = new OrganicChemical(pruneMatchVector(matchVector));
	    logger.debug("organic chemical entity: " + theOrganicChemical);

	    ExhaustiveVectorConceptRecognizer umlsRecognizer = new ExhaustiveVectorConceptRecognizer(new UMLSDetector(), ExhaustiveVectorConceptRecognizer.Direction.BOTH, false);
	    List umlsResults = umlsRecognizer.recognize(new Sentence(matchVector), getScanFence(constituent));
	    logger.debug("UMLS results: " + umlsResults);
	    for (Concept concept : (List<Concept>)umlsResults) {
		Vector<edu.uiowa.UMLS.Concept> umlsConcepts = (Vector<edu.uiowa.UMLS.Concept>)concept.getKey();
		if (umlsConcepts.size() == 1) {
		    theOrganicChemical.setUmlsMatch(umlsConcepts.get(0).getConceptID(),(String)concept.getPhrase() + " := " + umlsConcepts.get(0));
		    logger.debug("\tUMLS match: " + umlsConcepts.get(0));
		} else {
		    theOrganicChemical.setUmlsMatch(umlsConcepts.get(0).getConceptID(),(String)concept.getPhrase() + " :? " + umlsConcepts.get(0));
		    for (edu.uiowa.UMLS.Concept umlsConcept : umlsConcepts) {
			logger.debug("\tUMLS option: " + umlsConcept);
		    }
		}
	    }
	}
	return theOrganicChemical;
   }

    private void storePathologicalFunction(String doi, PathologicalFunction pathologicalFunction) throws SQLException {
	int id = 0;

	PathologicalFunction match = pathologicalFunctionHash.get(pathologicalFunction.toString());
	if (match != null) {
	    pathologicalFunction.setID(match.getID());
	} else {
	    try {
		PreparedStatement insert = conn.prepareStatement("insert into covid_model.pathological_function(pathological_function,umls_id,umls_match_string) values(?,?,?)", Statement.RETURN_GENERATED_KEYS);
		insert.setString(1, pathologicalFunction.toString());
		insert.setString(2, pathologicalFunction.getUmlsConcept());
		insert.setString(3, pathologicalFunction.getUmlsMatchString());
		insert.execute();
		ResultSet rs = insert.getGeneratedKeys();
		while (rs.next()) {
		    id = rs.getInt(1);
		    logger.info("new organic chemical id: " + id + " : " + pathologicalFunction);
		    pathologicalFunction.setID(id);
		    pathologicalFunctionHash.put(pathologicalFunction.toString(), pathologicalFunction);
		}
		insert.close();
	    } catch (SQLException e) {
		if (e.getSQLState().equals("23505")) {
		    conn.rollback();
		    PreparedStatement select = conn.prepareStatement("select id from covid_model.pathological_function where pathological_function = ?");
		    select.setString(1, pathologicalFunction.toString());
		    ResultSet rs = select.executeQuery();
		    while (rs.next()) {
			id = rs.getInt(1);
			logger.debug("organic chemical id: " + id);
			pathologicalFunction.setID(id);
			pathologicalFunctionHash.put(pathologicalFunction.toString(), pathologicalFunction);
		    }
		    select.close();

		} else {
		    e.printStackTrace();
		}
	    } finally {
		conn.commit();
	    }
	}
	try {
	    PreparedStatement insert = conn.prepareStatement("insert into covid_model.pathological_function_mention(pathological_function_id,doi) values(?,?)");
	    insert.setInt(1, pathologicalFunction.getID());
	    insert.setString(2, doi);
	    insert.execute();
	} catch (SQLException e) {
	    if (e.getSQLState().equals("23505")) {
		conn.rollback();
	    } else {
		e.printStackTrace();
	    }
	} finally {
	    conn.commit();
	}
    }

    private PathologicalFunction pathologicalFunctionMatch(syntaxTree constituent, String pattern) throws Exception {
	PathologicalFunction thePathologicalFunction = null;
	syntaxMatcher theMatcher = new syntaxMatcher(pattern);
	if (theMatcher.isMatch(constituent)) {
	    Vector<basicLexerToken> matchVector = theMatcher.matchesAsTokens();
	    logger.debug("pathological_function vector: " + matchVector);
	    thePathologicalFunction = new PathologicalFunction(pruneMatchVector(matchVector));
	    logger.debug("pathological_function entity: " + thePathologicalFunction);

	    ExhaustiveVectorConceptRecognizer umlsRecognizer = new ExhaustiveVectorConceptRecognizer(new UMLSDetector(), ExhaustiveVectorConceptRecognizer.Direction.BOTH, false);
	    List umlsResults = umlsRecognizer.recognize(new Sentence(matchVector), getScanFence(constituent));
	    logger.debug("UMLS results: " + umlsResults);
	    for (Concept concept : (List<Concept>)umlsResults) {
		Vector<edu.uiowa.UMLS.Concept> umlsConcepts = (Vector<edu.uiowa.UMLS.Concept>)concept.getKey();
		if (umlsConcepts.size() == 1) {
		    thePathologicalFunction.setUmlsMatch(umlsConcepts.get(0).getConceptID(),(String)concept.getPhrase() + " := " + umlsConcepts.get(0));
		    logger.debug("\tUMLS match: " + umlsConcepts.get(0));
		} else {
		    thePathologicalFunction.setUmlsMatch(umlsConcepts.get(0).getConceptID(),(String)concept.getPhrase() + " :? " + umlsConcepts.get(0));
		    for (edu.uiowa.UMLS.Concept umlsConcept : umlsConcepts) {
			logger.debug("\tUMLS option: " + umlsConcept);
		    }
		}
	    }
	}
	return thePathologicalFunction;
   }

    private void storePhysiologicalFunction(String doi, PhysiologicalFunction physiologicalFunction) throws SQLException {
	int id = 0;

	PhysiologicalFunction match = physiologicalFunctionHash.get(physiologicalFunction.toString());
	if (match != null) {
	    physiologicalFunction.setID(match.getID());
	} else {
	    try {
		PreparedStatement insert = conn.prepareStatement("insert into covid_model.physiological_function(physiological_function,umls_id,umls_match_string) values(?,?,?)", Statement.RETURN_GENERATED_KEYS);
		insert.setString(1, physiologicalFunction.toString());
		insert.setString(2, physiologicalFunction.getUmlsConcept());
		insert.setString(3, physiologicalFunction.getUmlsMatchString());
		insert.execute();
		ResultSet rs = insert.getGeneratedKeys();
		while (rs.next()) {
		    id = rs.getInt(1);
		    logger.info("new physiological_function id: " + id + " : " + physiologicalFunction);
		    physiologicalFunction.setID(id);
		    physiologicalFunctionHash.put(physiologicalFunction.toString(), physiologicalFunction);
		}
		insert.close();
	    } catch (SQLException e) {
		if (e.getSQLState().equals("23505")) {
		    conn.rollback();
		    PreparedStatement select = conn.prepareStatement("select id from covid_model.physiological_function where physiological_function = ?");
		    select.setString(1, physiologicalFunction.toString());
		    ResultSet rs = select.executeQuery();
		    while (rs.next()) {
			id = rs.getInt(1);
			logger.debug("physiological_function id: " + id);
			physiologicalFunction.setID(id);
			physiologicalFunctionHash.put(physiologicalFunction.toString(), physiologicalFunction);
		    }
		    select.close();

		} else {
		    e.printStackTrace();
		}
	    } finally {
		conn.commit();
	    }
	}
	try {
	    PreparedStatement insert = conn.prepareStatement("insert into covid_model.physiological_function_mention(physiological_function_id,doi) values(?,?)");
	    insert.setInt(1, physiologicalFunction.getID());
	    insert.setString(2, doi);
	    insert.execute();
	} catch (SQLException e) {
	    if (e.getSQLState().equals("23505")) {
		conn.rollback();
	    } else {
		e.printStackTrace();
	    }
	} finally {
	    conn.commit();
	}
    }

    private PhysiologicalFunction physiologicalFunctionMatch(syntaxTree constituent, String pattern) throws Exception {
	PhysiologicalFunction thePhysiologicalFunction = null;
	syntaxMatcher theMatcher = new syntaxMatcher(pattern);
	if (theMatcher.isMatch(constituent)) {
	    Vector<basicLexerToken> matchVector = theMatcher.matchesAsTokens();
	    logger.debug("physiological_function vector: " + matchVector);
	    thePhysiologicalFunction = new PhysiologicalFunction(pruneMatchVector(matchVector));
	    logger.debug("physiological_function entity: " + thePhysiologicalFunction);

	    ExhaustiveVectorConceptRecognizer umlsRecognizer = new ExhaustiveVectorConceptRecognizer(new UMLSDetector(), ExhaustiveVectorConceptRecognizer.Direction.BOTH, false);
	    List umlsResults = umlsRecognizer.recognize(new Sentence(matchVector), getScanFence(constituent));
	    logger.debug("UMLS results: " + umlsResults);
	    for (Concept concept : (List<Concept>)umlsResults) {
		Vector<edu.uiowa.UMLS.Concept> umlsConcepts = (Vector<edu.uiowa.UMLS.Concept>)concept.getKey();
		if (umlsConcepts.size() == 1) {
		    thePhysiologicalFunction.setUmlsMatch(umlsConcepts.get(0).getConceptID(),(String)concept.getPhrase() + " := " + umlsConcepts.get(0));
		    logger.debug("\tUMLS match: " + umlsConcepts.get(0));
		} else {
		    thePhysiologicalFunction.setUmlsMatch(umlsConcepts.get(0).getConceptID(),(String)concept.getPhrase() + " :? " + umlsConcepts.get(0));
		    for (edu.uiowa.UMLS.Concept umlsConcept : umlsConcepts) {
			logger.debug("\tUMLS option: " + umlsConcept);
		    }
		}
	    }
	}
	return thePhysiologicalFunction;
   }

    private void storeTranscriptionFactor(String doi, TranscriptionFactor transcriptionFactor) throws SQLException {
	int id = 0;

	TranscriptionFactor match = transcriptionFactorHash.get(transcriptionFactor.toString());
	if (match != null) {
	    transcriptionFactor.setID(match.getID());
	} else {
	    try {
		PreparedStatement insert = conn.prepareStatement("insert into covid_model.transcription_factor(transcription_factor,umls_id,umls_match_string) values(?,?,?)", Statement.RETURN_GENERATED_KEYS);
		insert.setString(1, transcriptionFactor.toString());
		insert.setString(2, transcriptionFactor.getUmlsConcept());
		insert.setString(3, transcriptionFactor.getUmlsMatchString());
		insert.execute();
		ResultSet rs = insert.getGeneratedKeys();
		while (rs.next()) {
		    id = rs.getInt(1);
		    logger.info("new transcription_factor id: " + id + " : " + transcriptionFactor);
		    transcriptionFactor.setID(id);
		    transcriptionFactorHash.put(transcriptionFactor.toString(), transcriptionFactor);
		}
		insert.close();
	    } catch (SQLException e) {
		if (e.getSQLState().equals("23505")) {
		    conn.rollback();
		    PreparedStatement select = conn.prepareStatement("select id from covid_model.transcription_factor where transcription_factor = ?");
		    select.setString(1, transcriptionFactor.toString());
		    ResultSet rs = select.executeQuery();
		    while (rs.next()) {
			id = rs.getInt(1);
			logger.debug("transcription_factor id: " + id);
			transcriptionFactor.setID(id);
			transcriptionFactorHash.put(transcriptionFactor.toString(), transcriptionFactor);
		    }
		    select.close();

		} else {
		    e.printStackTrace();
		}
	    } finally {
		conn.commit();
	    }
	}
	try {
	    PreparedStatement insert = conn.prepareStatement("insert into covid_model.transcription_factor_mention(transcription_factor_id,doi) values(?,?)");
	    insert.setInt(1, transcriptionFactor.getID());
	    insert.setString(2, doi);
	    insert.execute();
	} catch (SQLException e) {
	    if (e.getSQLState().equals("23505")) {
		conn.rollback();
	    } else {
		e.printStackTrace();
	    }
	} finally {
	    conn.commit();
	}
    }

    private TranscriptionFactor transcriptionFactorMatch(syntaxTree constituent, String pattern) throws Exception {
	TranscriptionFactor theTranscriptionFactor = null;
	syntaxMatcher theMatcher = new syntaxMatcher(pattern);
	if (theMatcher.isMatch(constituent)) {
	    Vector<basicLexerToken> matchVector = theMatcher.matchesAsTokens();
	    logger.debug("transcription_factor vector: " + matchVector);
	    theTranscriptionFactor = new TranscriptionFactor(pruneMatchVector(matchVector));
	    logger.debug("transcription_factor entity: " + theTranscriptionFactor);

	    ExhaustiveVectorConceptRecognizer umlsRecognizer = new ExhaustiveVectorConceptRecognizer(new UMLSDetector(), ExhaustiveVectorConceptRecognizer.Direction.BOTH, false);
	    List umlsResults = umlsRecognizer.recognize(new Sentence(matchVector), getScanFence(constituent));
	    logger.debug("UMLS results: " + umlsResults);
	    for (Concept concept : (List<Concept>)umlsResults) {
		Vector<edu.uiowa.UMLS.Concept> umlsConcepts = (Vector<edu.uiowa.UMLS.Concept>)concept.getKey();
		if (umlsConcepts.size() == 1) {
		    theTranscriptionFactor.setUmlsMatch(umlsConcepts.get(0).getConceptID(),(String)concept.getPhrase() + " := " + umlsConcepts.get(0));
		    logger.debug("\tUMLS match: " + umlsConcepts.get(0));
		} else {
		    theTranscriptionFactor.setUmlsMatch(umlsConcepts.get(0).getConceptID(),(String)concept.getPhrase() + " :? " + umlsConcepts.get(0));
		    for (edu.uiowa.UMLS.Concept umlsConcept : umlsConcepts) {
			logger.debug("\tUMLS option: " + umlsConcept);
		    }
		}
	    }
	}
	return theTranscriptionFactor;
   }

    private void storeEvent(String doi, Event event) throws SQLException {
	int id = 0;

	Event match = eventHash.get(event.toString());
	if (match != null) {
	    event.setID(match.getID());
	} else {
	    try {
		PreparedStatement insert = conn.prepareStatement("insert into covid_model.event(event) values(?)", Statement.RETURN_GENERATED_KEYS);
		insert.setString(1, event.toString());
		insert.execute();
		ResultSet rs = insert.getGeneratedKeys();
		while (rs.next()) {
		    id = rs.getInt(1);
		    logger.info("new event id: " + id + " : " + event);
		    event.setID(id);
		    eventHash.put(event.toString(), event);
		}
		insert.close();
	    } catch (SQLException e) {
		if (e.getSQLState().equals("23505")) {
		    conn.rollback();
		    PreparedStatement select = conn.prepareStatement("select id from covid_model.event where event = ?");
		    select.setString(1, event.toString());
		    ResultSet rs = select.executeQuery();
		    while (rs.next()) {
			id = rs.getInt(1);
			logger.debug("event id: " + id);
			event.setID(id);
			eventHash.put(event.toString(), event);
		    }
		    select.close();

		} else {
		    e.printStackTrace();
		}
	    } finally {
		conn.commit();
	    }
	}
	try {
	    PreparedStatement insert = conn.prepareStatement("insert into covid_model.event_mention(event_id,doi) values(?,?)");
	    insert.setInt(1, event.getID());
	    insert.setString(2, doi);
	    insert.execute();
	} catch (SQLException e) {
	    if (e.getSQLState().equals("23505")) {
		conn.rollback();
	    } else {
		e.printStackTrace();
	    }
	} finally {
	    conn.commit();
	}
    }

    private Event eventMatch(syntaxTree constituent, String pattern) throws Exception {
	Event theEvent = null;
	syntaxMatcher theMatcher = new syntaxMatcher(pattern);
	if (theMatcher.isMatch(constituent)) {
	    logger.debug("event vector: " + theMatcher.matchesAsTokens());
	    theEvent = new Event(pruneMatchVector(theMatcher.matchesAsTokens()));
	    logger.debug("event entity: " + theEvent);
	}
	return theEvent;
    }

    private void storeDisease(String doi, Disease disease) throws SQLException {
	int id = 0;

	Disease match = diseaseHash.get(disease.toString());
	if (match != null) {
	    disease.setID(match.getID());
	} else {
	    try {
		PreparedStatement insert = conn.prepareStatement("insert into covid_model.disease(disease,umls_id,umls_match_string) values(?,?,?)", Statement.RETURN_GENERATED_KEYS);
		insert.setString(1, disease.toString());
		insert.setString(2, disease.getUmlsConcept());
		insert.setString(3, disease.getUmlsMatchString());
		insert.execute();
		ResultSet rs = insert.getGeneratedKeys();
		while (rs.next()) {
		    id = rs.getInt(1);
		    logger.info("new disease id: " + id + " : " + disease);
		    disease.setID(id);
		    diseaseHash.put(disease.toString(), disease);
		}
		insert.close();
	    } catch (SQLException e) {
		if (e.getSQLState().equals("23505")) {
		    conn.rollback();
		    PreparedStatement select = conn.prepareStatement("select id from covid_model.disease where disease = ?");
		    select.setString(1, disease.toString());
		    ResultSet rs = select.executeQuery();
		    while (rs.next()) {
			id = rs.getInt(1);
			logger.debug("disease id: " + id);
			disease.setID(id);
			diseaseHash.put(disease.toString(), disease);
		    }
		    select.close();

		} else {
		    e.printStackTrace();
		}
	    } finally {
		conn.commit();
	    }
	}
	try {
	    PreparedStatement insert = conn.prepareStatement("insert into covid_model.disease_mention(disease_id,doi) values(?,?)");
	    insert.setInt(1, disease.getID());
	    insert.setString(2, doi);
	    insert.execute();
	} catch (SQLException e) {
	    if (e.getSQLState().equals("23505")) {
		conn.rollback();
	    } else {
		e.printStackTrace();
	    }
	} finally {
	    conn.commit();
	}
    }

    private Disease diseaseMatch(syntaxTree constituent, String pattern) throws Exception {
	Disease theDisease = null;
	syntaxMatcher theMatcher = new syntaxMatcher(pattern);
	if (theMatcher.isMatch(constituent)) {
	    Vector<basicLexerToken> matchVector = theMatcher.matchesAsTokens();
	    logger.debug("disease vector: " + matchVector);
	    theDisease = new Disease(pruneMatchVector(matchVector));
	    logger.debug("disease entity: " + theDisease);

	    ExhaustiveVectorConceptRecognizer umlsRecognizer = new ExhaustiveVectorConceptRecognizer(new UMLSDetector(), ExhaustiveVectorConceptRecognizer.Direction.BOTH, false);
	    List umlsResults = umlsRecognizer.recognize(new Sentence(matchVector), getScanFence(constituent));
	    logger.debug("UMLS results: " + umlsResults);
	    for (Concept concept : (List<Concept>)umlsResults) {
		Vector<edu.uiowa.UMLS.Concept> umlsConcepts = (Vector<edu.uiowa.UMLS.Concept>)concept.getKey();
		if (umlsConcepts.size() == 1) {
		    theDisease.setUmlsMatch(umlsConcepts.get(0).getConceptID(),(String)concept.getPhrase() + " := " + umlsConcepts.get(0));
		    logger.debug("\tUMLS match: " + umlsConcepts.get(0));
		} else {
		    theDisease.setUmlsMatch(umlsConcepts.get(0).getConceptID(),(String)concept.getPhrase() + " :? " + umlsConcepts.get(0));
		    for (edu.uiowa.UMLS.Concept umlsConcept : umlsConcepts) {
			logger.debug("\tUMLS option: " + umlsConcept);
		    }
		}
	    }
	}
	return theDisease;
   }

    PlaceName placeNameMatch(syntaxTree theNode, String pattern) throws Exception {
	PlaceName thePlaceName = null;
	syntaxMatcher theMatcher = null;
	try {
	    theMatcher = new syntaxMatcher(pattern);
	} catch (Exception e1) {
	    logger.error("*** error parsing placeNameMatch pattern: " + pattern);
	    System.exit(0);
	}
	if (theMatcher.isMatch(theNode)) {
	    Vector<basicLexerToken> matchVector = theMatcher.matchesAsTokens();
	    logger.debug("placeName vector: " + matchVector);
	    thePlaceName = new PlaceName(pruneMatchVector(matchVector));
	    logger.debug("placeName entity: " + thePlaceName);

	    ExhaustiveVectorConceptRecognizer geonameRecognizer = new ExhaustiveVectorConceptRecognizer(new GeoNamesDetector(), false);
	    List geoResults = geonameRecognizer.recognize(new Sentence(matchVector), getScanFence(theNode));
	    logger.debug("GeoNames results: " + geoResults);
	    for (Concept concept : (List<Concept>) geoResults) {
		Vector<GeoName> geonames = (Vector<GeoName>) concept.getKey();
		if (thePlaceName.getGeonamesID() == 0 && geonames.size() == 1) {
		    thePlaceName.setGeonamesID(geonames.get(0).getId());
		    thePlaceName.setGeonamesMatchString((String) concept.getPhrase() + " := " + geonames.get(0));
		} else if (thePlaceName.getGeonamesID() == 0) {
		    boolean firstAddress = true;
		    for (GeoName geoname : geonames) {
			syntaxMatcher locationMatcher = null;
			String matchPattern = null;
			try {
			    matchPattern = syntaxTree.generateNodePattern(theNode) + " >>[S <<(/" + geoname.getAncestorContext(true) + "/) ]";
			    locationMatcher = new syntaxMatcher(matchPattern);
			    if (locationMatcher != null && locationMatcher.hasMatch(theNode)) {
				Vector<basicLexerToken> addressMatchVector = locationMatcher.matchesAsTokens();
				logger.debug("GeoName option: " + geoname);
				logger.debug("location match pattern: " + syntaxTree.generateNodePattern(theNode) + " >>[S <<(/" + geoname.getAncestorContext(true) + "/) ]");
				logger.debug("\tGeoName match vector: " + addressMatchVector);
				if (firstAddress) {
				    thePlaceName.setGeonamesID(geoname.getId());
				    thePlaceName.setGeonamesMatchString((String) concept.getPhrase() + " :< " + geoname.getAncestorContext(true));
				    firstAddress = false;
				}
			    }
			} catch (Exception e) {
			    logger.error("placeNameMatch: trapping erronous pattern: " + matchPattern);
			}
		    }
		    if (firstAddress) {
			GeoName heuristicMatch = heuristicGeoNameMatch(geonames);
			if (heuristicMatch != null) {
			    thePlaceName.setGeonamesID(heuristicMatch.getId());
			    thePlaceName.setGeonamesMatchString(concept.getPhrase() + " :? " + heuristicMatch.getAncestorContext());
			}
		    }
		}
	    }
	}

	return thePlaceName;
    }
    
    GeoName heuristicGeoNameMatch(Vector<GeoName> geonames) {
	GeoName aGeoName = null;
	GeoName lGeoName = null;
	GeoName pGeoName = null;
	GeoName sGeoName = null;
	GeoName otherGeoName = null;
	
	for (GeoName current : geonames) {
	    switch (current.getFeatureClass()) {
	    case "A" :
		if (aGeoName == null || aGeoName.getPopulation() < current.getPopulation())
		    aGeoName = current;
		break;
	    case "L" :
		if (lGeoName == null || lGeoName.getPopulation() < current.getPopulation())
		    lGeoName = current;
		break;
	    case "P" :
		if (pGeoName == null || pGeoName.getPopulation() < current.getPopulation())
		    pGeoName = current;
		break;
	    case "S" :
		if (sGeoName == null || sGeoName.getPopulation() < current.getPopulation())
		    sGeoName = current;
		break;
            default:
		if (otherGeoName == null || otherGeoName.getPopulation() < current.getPopulation())
		    otherGeoName = current;
		break;
	    }
	}
	
	if (pGeoName != null)
	    return pGeoName;
	
	if (aGeoName != null)
	    return aGeoName;
	
	if (lGeoName != null)
	    return lGeoName;
	
	if (sGeoName != null)
	    return sGeoName;
	
	return otherGeoName;
    }

    Organization organizationMatch(syntaxTree theNode, String pattern) throws Exception {
	logger.debug("organizationMatch: " + getScanFence(theNode) + " : " + theNode.treeString() + "\t" + pattern);
	Organization establishment = null;
	syntaxMatcher theMatcher = new syntaxMatcher(pattern);
	registerMatchFunctions(theMatcher);
	if (theMatcher.hasMatch(theNode)) {
	    Vector<basicLexerToken> matchVector = theMatcher.matchesAsTokens();
	    logger.debug("organization vector: " + matchVector);
	    establishment = new Organization(pruneMatchVector(matchVector));

	    ExhaustiveVectorConceptRecognizer gridRecognizer = new ExhaustiveVectorConceptRecognizer(new GRIDDetector(), false);
	    List theResults = gridRecognizer.recognize(new Sentence(matchVector), getScanFence(theNode));
	    logger.debug("GRID results: " + theResults);
	    boolean firstConcept = true;
	    for (Concept concept : (List<Concept>)theResults) {
		Vector<Institute> institutes = (Vector<Institute>)concept.getKey();
		if (firstConcept && institutes.size() == 1) {
		    establishment.setGridID(institutes.get(0).getId());
		    establishment.setGridMatchString((String)concept.getPhrase());
		} else if (firstConcept) {
		    boolean firstAddress = true;
		    for (Institute institute : institutes) {
			logger.debug("GRID option: " + institute);
			if (institute.getAddresses().size() > 0) {
			    Address address = institute.getAddresses().firstElement();
			    logger.debug("location match pattern: " + syntaxTree.generateNodePattern(theNode)+" >>[S <<(/" + address.getCity() + "|" + address.getState() + "|" + address.getCountryish() + "/) ]");
			    syntaxMatcher locationMatcher = null;
			    String matchPattern = null;
			    try {
				matchPattern = syntaxTree.generateNodePattern(theNode) + " >>[S <<(/" + address.getCity() + "|" + address.getState() + "|" + address.getCountryish() + "/) ]";
				locationMatcher = new syntaxMatcher(matchPattern);
				if (locationMatcher != null && locationMatcher.hasMatch(theNode)) {
				    Vector<basicLexerToken> addressMatchVector = locationMatcher.matchesAsTokens();
				    logger.debug("\tGRID address match vector: " + addressMatchVector);
				    if (firstAddress) {
					establishment.setGridID(institute.getId());
					establishment.setGridMatchString((String) concept.getPhrase());
					firstAddress = false;
				    }
				}
			    } catch (Exception e) {
				logger.error("organizationMatch: trapping erronous pattern: " + matchPattern);
			    }
			}
		    }
		}
		firstConcept = false;
	    }

	    ExhaustiveVectorConceptRecognizer geonameRecognizer = new ExhaustiveVectorConceptRecognizer(new GeoNamesDetector(), false);
	    List geoResults = geonameRecognizer.recognize(new Sentence(matchVector), getScanFence(theNode));
	    logger.debug("GeoNames results: " + geoResults);
	    firstConcept = true;
	    for (Concept concept : (List<Concept>)geoResults) {
		Vector<GeoName> geonames = (Vector<GeoName>)concept.getKey();
		if (firstConcept && geonames.size() == 1) {
		    establishment.setGeonamesID(geonames.get(0).getId());
		    establishment.setGeonamesMatchString((String)concept.getPhrase() + " := " + geonames.get(0).getAncestorContext());
		} else if (firstConcept) {
		    boolean firstAddress = true;
		    for (GeoName geoname : geonames) {
			logger.debug("GeoName option: " + geoname);
			logger.debug("location match pattern: " + syntaxTree.generateNodePattern(theNode) + " >>[S <<(/" + geoname.getAncestorContext(true) + "/) ]");
			syntaxMatcher locationMatcher = null;
			String matchPattern = null;
			try {
			    matchPattern = syntaxTree.generateNodePattern(theNode) + " >>[S <<(/" + geoname.getAncestorContext(true) + "/) ]";
			    locationMatcher = new syntaxMatcher(matchPattern);
			    if (locationMatcher != null & locationMatcher.hasMatch(theNode)) {
				Vector<basicLexerToken> addressMatchVector = locationMatcher.matchesAsTokens();
				logger.debug("\tGeoName match vector: " + addressMatchVector);
				if (firstAddress) {
				    establishment.setGeonamesID(geoname.getId());
				    establishment.setGeonamesMatchString((String) concept.getPhrase() + " :< " + geoname.getAncestorContext(true));
				    firstAddress = false;
				}
			    }
			} catch (Exception e) {
			    logger.error("organizationMatch: trapping erronous pattern: " + matchPattern);
			}
		    }
		    if (firstAddress) {
			GeoName heuristicMatch = heuristicGeoNameMatch(geonames);
			if (heuristicMatch != null) {
			    establishment.setGeonamesID(heuristicMatch.getId());
			    establishment.setGeonamesMatchString(concept.getPhrase() + " :? " + heuristicMatch.getAncestorContext());
			}
		    }
		}
		firstConcept = false;
	    }

	    logger.debug("organization entity: " + establishment);
	}
	return establishment;
    }
    
    int getScanFence(syntaxTree theNode) {
	int count = theNode.getLeadingLeafCount();
	if (count > 0)
	    return count;
	
	syntaxTree constituent = theNode.getConstituent(0);
	if (constituent.getConstituent(0) == null)
	    return 0;
	else
	    return constituent.getLeadingLeafCount();
    }

    protected void registerMatchFunctions(syntaxMatcher theMatcher) {
	theMatcher.registerFunction("isEntity", new entityComparator());
	theMatcher.registerFunction("isEmail", new emailComparator());
	theMatcher.registerFunction("isURL", new urlComparator());
    }

    Collaboration collaborationMatch(syntaxTree theNode, String pattern) throws Exception {
	Collaboration collaboration = null;
	syntaxMatcher theMatcher = new syntaxMatcher(pattern);
	if (theMatcher.isMatch(theNode)) {
	    logger.debug("collaboration vector: " + theMatcher.matchesAsTokens());
	    collaboration = new Collaboration(pruneMatchVector(theMatcher.matchesAsTokens()));
	    logger.debug("collaboration entity: " + collaboration);
	}
	return collaboration;
    }

    ClinicalTrialRegistration clinicalTrialMatch(syntaxTree theNode, String pattern) throws Exception {
	ClinicalTrialRegistration clinicalTrial = null;
	syntaxMatcher theMatcher = new syntaxMatcher(pattern);
//	if (theMatcher.isMatch(theNode)) {
//	    logger.info("clinical trial vector: " + theMatcher.matchesAsTokens());
//	    clinicalTrial = new ClinicalTrialRegistration(theMatcher.matchesAsTokens());
//	    logger.info("clinical trial entity: " + clinicalTrial);
//	}
	if (theMatcher.hasMatch(theNode)) {
	    Enumeration<syntaxMatch> theEnum = theMatcher.matches(theNode);
	    while (theEnum.hasMoreElements()) {
		syntaxMatch theMatchNode = theEnum.nextElement();
		logger.info("clinical trial match pattern: " + pattern + "\tnode: " + theMatchNode.getPhrase().treeString() + "\t" + theMatchNode.getPhrase().getFragmentString());
		for (int i = 1; i <= theMatchNode.matchCount(); i++)
		    logger.info("\tmatch slot [" + i + "]: " + theMatchNode.getMatch(i).trimmedPhraseAsString());
		if (theMatchNode.matchCount() == 1)
		    clinicalTrial = new ClinicalTrialRegistration(theMatchNode.getMatch(1).trimmedPhraseAsString());
		else
		    clinicalTrial = new ClinicalTrialRegistration(theMatchNode.getMatch(1).trimmedPhraseAsString(),theMatchNode.getMatch(2).trimmedPhraseAsString());
	    }
	}
	return clinicalTrial;
    }

    Resource resourceMatch(syntaxTree theNode, String pattern) throws Exception {
	Resource resource = null;
	syntaxMatcher theMatcher = new syntaxMatcher(pattern);
	if (theMatcher.isMatch(theNode)) {
	    Vector<basicLexerToken> matchVector = theMatcher.matchesAsTokens();
	    logger.debug("resource vector: " + matchVector);
	    resource = new Resource(pruneMatchVector(matchVector));
	    logger.debug("resource entity: " + resource);

	    ExhaustiveVectorConceptRecognizer umlsRecognizer = new ExhaustiveVectorConceptRecognizer(new UMLSDetector(), ExhaustiveVectorConceptRecognizer.Direction.BOTH, false);
	    List umlsResults = umlsRecognizer.recognize(new Sentence(matchVector), getScanFence(theNode));
	    logger.debug("UMLS results: " + umlsResults);
	    for (Concept concept : (List<Concept>)umlsResults) {
		Vector<edu.uiowa.UMLS.Concept> umlsConcepts = (Vector<edu.uiowa.UMLS.Concept>)concept.getKey();
		if (umlsConcepts.size() == 1) {
		    resource.setUmlsMatch(umlsConcepts.get(0).getConceptID(),(String)concept.getPhrase() + " := " + umlsConcepts.get(0));
		    logger.debug("\tUMLS match: " + umlsConcepts.get(0));
		} else {
		    resource.setUmlsMatch(umlsConcepts.get(0).getConceptID(),(String)concept.getPhrase() + " :? " + umlsConcepts.get(0));
		    for (edu.uiowa.UMLS.Concept umlsConcept : umlsConcepts) {
			logger.debug("\tUMLS option: " + umlsConcept);
		    }
		}
	    }
	}
	return resource;
    }

    void storePerson(String doi, Person thePerson) throws SQLException {
	int id = 0;

	Person match = personHash.get(thePerson.toString());
	if (match != null) {
	    thePerson.setID(match.getID());
	} else {
	    try {
		PreparedStatement insert = conn.prepareStatement("insert into person(first_name,last_name,middle_name,title,appendix) values(?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
		insert.setString(1, thePerson.getFirstName());
		insert.setString(2, thePerson.getSurname());
		insert.setString(3, thePerson.getMiddleName());
		insert.setString(4, thePerson.getTitle());
		insert.setString(5, thePerson.getAppendix());
		insert.execute();
		ResultSet rs = insert.getGeneratedKeys();
		while (rs.next()) {
		    id = rs.getInt(1);
		    logger.info("new person id: " + id + "\t" + thePerson);
		    thePerson.setID(id);
		    personHash.put(thePerson.toString(), thePerson);
		}
		insert.close();
	    } catch (SQLException e) {
		if (e.getSQLState().equals("23505")) {
		    conn.rollback();
		    PreparedStatement select = conn.prepareStatement("select id from person where first_name = ? and last_name = ? and middle_name = ? and title = ? and appendix = ?");
		    select.setString(1, thePerson.getFirstName());
		    select.setString(2, thePerson.getSurname());
		    select.setString(3, thePerson.getMiddleName());
		    select.setString(4, thePerson.getTitle());
		    select.setString(5, thePerson.getAppendix());
		    boolean found = false;
		    ResultSet rs = select.executeQuery();
		    while (rs.next()) {
			id = rs.getInt(1);
			logger.debug("person id: " + id);
			thePerson.setID(id);
			personHash.put(thePerson.toString(), thePerson);
			found = true;
		    }
		    select.close();
		    if (!found)
			logger.error("failed to retrieve person: " + thePerson);
		} else {
		    e.printStackTrace();
		}
	    } finally {
		conn.commit();
	    }
	}
	try {
	    PreparedStatement insert = conn.prepareStatement("insert into person_mention(person_id,doi) values(?,?)");
	    insert.setInt(1, thePerson.getID());
	    insert.setString(2, doi);
	    insert.execute();
	} catch (SQLException e) {
	    if (e.getSQLState().equals("23505")) {
		conn.rollback();
	    } else {
		e.printStackTrace();
	    }
	} finally {
	    conn.commit();
	}
    }

    void storePlaceName(String doi, PlaceName thePlaceName) throws SQLException {
	int id = 0;

	PlaceName match = placeNameHash.get(thePlaceName.toString());
	if (match != null) {
	    thePlaceName.setID(match.getID());
	} else {
	    try {
		PreparedStatement insert = conn.prepareStatement("insert into location(location,geonames_id,geonames_match_string) values(?,?,?)", Statement.RETURN_GENERATED_KEYS);
		insert.setString(1, thePlaceName.toString());
		if (thePlaceName.getGeonamesID() == 0)
		    insert.setNull(2, Types.INTEGER);
		else
		    insert.setInt(2, thePlaceName.getGeonamesID());
		insert.setString(3, thePlaceName.getGeonamesMatchString());
		insert.execute();
		ResultSet rs = insert.getGeneratedKeys();
		while (rs.next()) {
		    id = rs.getInt(1);
		    logger.info("new placeName id: " + id + " : " + thePlaceName);
		    thePlaceName.setID(id);
		    placeNameHash.put(thePlaceName.toString(), thePlaceName);
		}
		insert.close();
	    } catch (SQLException e) {
		if (e.getSQLState().equals("23505")) {
		    conn.rollback();
		    PreparedStatement select = conn.prepareStatement("select id,geonames_id from location where location = ?");
		    select.setString(1, thePlaceName.toString());
		    ResultSet rs = select.executeQuery();
		    while (rs.next()) {
			id = rs.getInt(1);
			int geonames_id = rs.getInt(2);
			logger.debug("placeName id: " + id);
			thePlaceName.setID(id);
			thePlaceName.setGeonamesID(geonames_id);
			placeNameHash.put(thePlaceName.toString(), thePlaceName);
		    }
		    select.close();

		} else {
		    e.printStackTrace();
		}
	    } finally {
		conn.commit();
	    }
	}
	try {
	    PreparedStatement insert = conn.prepareStatement("insert into location_mention(location_id,doi) values(?,?)");
	    insert.setInt(1, thePlaceName.getID());
	    insert.setString(2, doi);
	    insert.execute();
	} catch (SQLException e) {
	    if (e.getSQLState().equals("23505")) {
		conn.rollback();
	    } else {
		e.printStackTrace();
	    }
	} finally {
	    conn.commit();
	}
    }


    void storeClinicalTrial(String doi, ClinicalTrialRegistration clinicalTrial) throws SQLException {
	PreparedStatement insert = conn.prepareStatement("insert into clinical_trial values(?,?,?)");
	insert.setString(1, doi);
	insert.setString(2, clinicalTrial.getPrefix());
	insert.setString(3, clinicalTrial.getIdentifier());
	insert.execute();
	insert.close();
	conn.commit();
    }
    

    @Override
    public void resolveID(int id, Entity elementAt) throws SQLException {
	// TODO Auto-generated method stub
	
    }

}
