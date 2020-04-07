package org.cd2h.covid;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.servlet.jsp.JspTagException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.facet.index.FacetFields;
import org.apache.lucene.facet.params.FacetIndexingParams;
import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter.OrdinalMap;
import org.apache.lucene.facet.util.TaxonomyMergeUtils;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import edu.uiowa.lucene.biomedical.BiomedicalAnalyzer;
import edu.uiowa.slis.GitHubTagLib.util.LocalProperties;
import edu.uiowa.slis.GitHubTagLib.util.PropertyLoader;

public class FacetIndexer {
    static Logger logger = Logger.getLogger(FacetIndexer.class);
    LocalProperties prop_file = null;
    static Connection wintermuteConn = null;
    static Connection deepConn = null;
    static String pathPrefix = "/usr/local/CD2H/lucene/";
    static String[] sites = {
	    			pathPrefix + "biorxiv",
	    			pathPrefix + "litcovid",
	    			pathPrefix + "chictr",
	    			pathPrefix + "clinical_trials"
	    			};
    

    public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException, JspTagException {
        PropertyConfigurator.configure(args[0]);
	wintermuteConn = getConnection("lucene");
	deepConn = getConnection("lucene");

	switch (args[1]) {
	case "-index":
	    indexBioRxiv();
	    indexLitCOVID();
	    indexClinicalTrials();
	    break;
	case "clinical_trials":
	    indexClinicalTrials();
	    break;
	case "chictr":
	    indexChiCTRTrials();
	    break;
	case "biorxiv":
	    indexBioRxiv();
	    break;
	case "litcovid":
	    indexLitCOVID();
	    break;
	case "-merge":
	    mergeIndices(sites, pathPrefix + "covidsearch");
	    break;
	}
    }
    
    public static void mergeIndices(String[] requests, String targetPath) throws SQLException, CorruptIndexException, IOException {
	IndexWriterConfig config = new IndexWriterConfig(org.apache.lucene.util.Version.LUCENE_43, new BiomedicalAnalyzer());
	config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
	config.setRAMBufferSizeMB(500);
	IndexWriter theWriter = new IndexWriter(FSDirectory.open(new File(targetPath)), config);
	DirectoryTaxonomyWriter taxoWriter = new DirectoryTaxonomyWriter(FSDirectory.open(new File(targetPath + "_tax")));
	OrdinalMap map = new org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter.MemoryOrdinalMap();
	FacetIndexingParams params = new FacetIndexingParams();
	
	logger.info("sites: " + requests);
	for (String site : requests) {
	    logger.info("merging " + site + "...");
	    Directory index = FSDirectory.open(new File(site));
	    Directory index_tax = FSDirectory.open(new File(site + "_tax"));
	    TaxonomyMergeUtils.merge(index, index_tax, map, theWriter, taxoWriter, params);
	    index_tax.close();
	    index.close();
	}

	taxoWriter.close();
	theWriter.close();
	logger.info("done");
    }

    static void indexBioRxiv() throws IOException, SQLException {
	Directory indexDir = FSDirectory.open(new File(pathPrefix + "biorxiv"));
	Directory taxoDir = FSDirectory.open(new File(pathPrefix + "biorxiv_tax"));

	IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_43, new BiomedicalAnalyzer());
	config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
	IndexWriter indexWriter = new IndexWriter(indexDir, config);

	// Writes facet ords to a separate directory from the main index
	DirectoryTaxonomyWriter taxoWriter = new DirectoryTaxonomyWriter(taxoDir);

	// Reused across documents, to add the necessary facet fields
	FacetFields facetFields = new FacetFields(taxoWriter);

	indexBioRxiv(indexWriter, facetFields);

	taxoWriter.close();
	indexWriter.close();
    }
    
    @SuppressWarnings("deprecation")
    static void indexBioRxiv(IndexWriter indexWriter, FacetFields facetFields) throws IOException, SQLException {
	int count = 0;
	logger.info("indexing bioRxiv/medRxiv preprints...");
	PreparedStatement stmt = wintermuteConn.prepareStatement("select doi,title,authors,link,site,pub_date,abstract from covid_biorxiv.biorxiv_current");
	ResultSet rs = stmt.executeQuery();

	while (rs.next()) {
	    count++;
	    String doi = rs.getString(1);
	    String title = rs.getString(2);
	    String authors = rs.getString(3);
	    String link = rs.getString(4);
	    String site = rs.getString(5);
	    String pub_date = rs.getString(6);
	    String abstr = rs.getString(7);

	    logger.trace("preprint: " + doi + "\t" + title);

	    Document theDocument = new Document();
	    List<CategoryPath> paths = new ArrayList<CategoryPath>();

	    paths.add(new CategoryPath("Entity/Preprint", '/'));
	    paths.add(new CategoryPath("Source/"+site, '/'));
	    
	    theDocument.add(new Field("source", site, Field.Store.YES, Field.Index.NOT_ANALYZED));
	    theDocument.add(new Field("uri", link, Field.Store.YES, Field.Index.NOT_ANALYZED));
	    theDocument.add(new Field("id", doi, Field.Store.YES, Field.Index.NOT_ANALYZED));

	    if (title == null) {
		theDocument.add(new Field("label", site+" "+ doi + " ", Field.Store.YES, Field.Index.ANALYZED));
	    } else {
		theDocument.add(new Field("label", title + " ", Field.Store.YES, Field.Index.ANALYZED));		
		theDocument.add(new Field("content", title + " ", Field.Store.NO, Field.Index.ANALYZED));
	    }
	    
	    if (authors != null)
		theDocument.add(new Field("content", authors + " ", Field.Store.NO, Field.Index.ANALYZED));
	    if (pub_date != null)
		theDocument.add(new Field("content", pub_date + " ", Field.Store.NO, Field.Index.ANALYZED));
	    if (abstr != null)
		theDocument.add(new Field("content", abstr + " ", Field.Store.NO, Field.Index.ANALYZED));

	    PreparedStatement substmt = wintermuteConn.prepareStatement("select contents from covid_biorxiv.biorxiv_text where doi = ?");
	    substmt.setString(1, doi);
	    ResultSet subrs = substmt.executeQuery();
	    while (subrs.next()) {
		String contents = subrs.getString(1);
		theDocument.add(new Field("content", contents + " ", Field.Store.NO, Field.Index.ANALYZED));
		logger.trace("\tcontents: " + contents);
	    }
	    substmt.close();
		
	    facetFields.addFields(theDocument, paths);
	    indexWriter.addDocument(theDocument);
	}
	stmt.close();
	logger.info("\tpublications indexed: " + count);
    }

    static void indexLitCOVID() throws IOException, SQLException {
	Directory indexDir = FSDirectory.open(new File(pathPrefix + "litcovid"));
	Directory taxoDir = FSDirectory.open(new File(pathPrefix + "litcovid_tax"));

	IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_43, new BiomedicalAnalyzer());
	config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
	IndexWriter indexWriter = new IndexWriter(indexDir, config);

	// Writes facet ords to a separate directory from the main index
	DirectoryTaxonomyWriter taxoWriter = new DirectoryTaxonomyWriter(taxoDir);

	// Reused across documents, to add the necessary facet fields
	FacetFields facetFields = new FacetFields(taxoWriter);

	indexMEDLINE(indexWriter, facetFields);

	taxoWriter.close();
	indexWriter.close();
    }
    
    @SuppressWarnings("deprecation")
    static void indexMEDLINE(IndexWriter indexWriter, FacetFields facetFields) throws IOException, SQLException {
	int count = 0;
	logger.info("indexing MEDLINE articles...");
	PreparedStatement stmt = wintermuteConn.prepareStatement("select pmid,article_title from covid_litcovid.article_title where seqnum = 1");
	ResultSet rs = stmt.executeQuery();

	while (rs.next()) {
	    count++;
	    int pmid = rs.getInt(1);
	    String title = rs.getString(2);

	    logger.trace("article: " + pmid + "\t" + title);

	    Document theDocument = new Document();
	    List<CategoryPath> paths = new ArrayList<CategoryPath>();

	    paths.add(new CategoryPath("Entity/Article", '/'));
	    paths.add(new CategoryPath("Source/LitCOVID", '/'));
	    
	    theDocument.add(new Field("source", "LitCOVID", Field.Store.YES, Field.Index.NOT_ANALYZED));
	    theDocument.add(new Field("uri", "https://www.ncbi.nlm.nih.gov/pubmed/" + pmid, Field.Store.YES, Field.Index.NOT_ANALYZED));
	    theDocument.add(new Field("id", pmid + "", Field.Store.YES, Field.Index.NOT_ANALYZED));

	    if (title == null) {
		theDocument.add(new Field("label", "PubMed "+ pmid + " ", Field.Store.YES, Field.Index.ANALYZED));
	    } else {
		theDocument.add(new Field("label", title + " ", Field.Store.YES, Field.Index.ANALYZED));		
		theDocument.add(new Field("content", title + " ", Field.Store.NO, Field.Index.ANALYZED));
	    }
	    
	    indexMEDLINE(pmid, theDocument);
		
	    facetFields.addFields(theDocument, paths);
	    indexWriter.addDocument(theDocument);
	}
	stmt.close();
	logger.info("\tpublications indexed: " + count);
    }

    @SuppressWarnings("deprecation")
    static void indexMEDLINE(int pmid, Document theDocument) throws SQLException {
	PreparedStatement stmt = wintermuteConn.prepareStatement("select abstract from covid_litcovid.abstract where pmid = ?");
	stmt.setInt(1, pmid);
	ResultSet rs = stmt.executeQuery();
	while (rs.next()) {
	    String theAbstract = rs.getString(1);
	    theDocument.add(new Field("content", theAbstract + " ", Field.Store.NO, Field.Index.ANALYZED));
	    logger.trace("\tabstract: " + theAbstract);
	}
	stmt.close();

	stmt = wintermuteConn.prepareStatement("select last_name,fore_name,initials,suffix,collective_name from covid_litcovid.author where pmid = ?");
	stmt.setInt(1, pmid);
	rs = stmt.executeQuery();
	while (rs.next()) {
	    String last_name = rs.getString(1);
	    String fore_name = rs.getString(2);
	    String initials = rs.getString(3);
	    String suffix = rs.getString(4);
	    String collective_name = rs.getString(5);
	    if (last_name != null)
		theDocument.add(new Field("content", last_name + " ", Field.Store.NO, Field.Index.ANALYZED));
	    if (fore_name != null)
		theDocument.add(new Field("content", fore_name + " ", Field.Store.NO, Field.Index.ANALYZED));
	    if (initials != null)
		theDocument.add(new Field("content", initials + " ", Field.Store.NO, Field.Index.ANALYZED));
	    if (suffix != null)
		theDocument.add(new Field("content", suffix + " ", Field.Store.NO, Field.Index.ANALYZED));
	    if (collective_name != null)
		theDocument.add(new Field("content", collective_name + " ", Field.Store.NO, Field.Index.ANALYZED));
	    logger.trace("\tauthor: " + last_name + ", " + fore_name);
	}
	stmt.close();

	stmt = wintermuteConn.prepareStatement("select affiliation from covid_litcovid.author_affiliation where pmid = ?");
	stmt.setInt(1, pmid);
	rs = stmt.executeQuery();
	while (rs.next()) {
	    String affiliation = rs.getString(1);
	    theDocument.add(new Field("content", affiliation + " ", Field.Store.NO, Field.Index.ANALYZED));
	    logger.trace("\taffiliation: " + affiliation);
	}
	stmt.close();

	stmt = wintermuteConn.prepareStatement("select keyword from covid_litcovid.keyword where pmid = ?");
	stmt.setInt(1, pmid);
	rs = stmt.executeQuery();
	while (rs.next()) {
	    String keyword = rs.getString(1);
	    theDocument.add(new Field("content", keyword + " ", Field.Store.NO, Field.Index.ANALYZED));
	    logger.trace("\tkeyword: " + keyword);
	}
	stmt.close();

	stmt = wintermuteConn.prepareStatement("select descriptor_name from covid_litcovid.mesh_heading where pmid = ?");
	stmt.setInt(1, pmid);
	rs = stmt.executeQuery();
	while (rs.next()) {
	    String descriptor = rs.getString(1);
	    theDocument.add(new Field("content", descriptor + " ", Field.Store.NO, Field.Index.ANALYZED));
	    logger.trace("\tdescriptor: " + descriptor);
	}
	stmt.close();

	stmt = wintermuteConn.prepareStatement("select registry_number,name_of_substance from covid_litcovid.chemical where pmid = ?");
	stmt.setInt(1, pmid);
	rs = stmt.executeQuery();
	while (rs.next()) {
	    String registry = rs.getString(1);
	    String substance = rs.getString(2);
	    theDocument.add(new Field("content", registry + " ", Field.Store.NO, Field.Index.ANALYZED));
	    theDocument.add(new Field("content", substance + " ", Field.Store.NO, Field.Index.ANALYZED));
	    logger.trace("\tregistry: " + registry + "\tsubstance: " + substance);
	}
	stmt.close();

	stmt = wintermuteConn.prepareStatement("select gene_symbol from covid_litcovid.gene_symbol where pmid = ?");
	stmt.setInt(1, pmid);
	rs = stmt.executeQuery();
	while (rs.next()) {
	    String gene = rs.getString(1);
	    theDocument.add(new Field("content", gene + " ", Field.Store.NO, Field.Index.ANALYZED));
	    logger.trace("\tgene: " + gene);
	}
	stmt.close();

    }

    static void indexClinicalTrials() throws IOException, SQLException {
	Directory indexDir = FSDirectory.open(new File(pathPrefix + "clinical_trials"));
	Directory taxoDir = FSDirectory.open(new File(pathPrefix + "clinical_trials_tax"));

	IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_43, new BiomedicalAnalyzer());
	config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
	IndexWriter indexWriter = new IndexWriter(indexDir, config);

	// Writes facet ords to a separate directory from the main index
	DirectoryTaxonomyWriter taxoWriter = new DirectoryTaxonomyWriter(taxoDir);

	// Reused across documents, to add the necessary facet fields
	FacetFields facetFields = new FacetFields(taxoWriter);
	
//	indexClinicalTrialOfficialContact(indexWriter, facetFields);
	indexClinicalTrials(indexWriter, facetFields);

	taxoWriter.close();
	indexWriter.close();
    }
    
    @SuppressWarnings("deprecation")
    static void indexClinicalTrialOfficialContact(IndexWriter indexWriter, FacetFields facetFields) throws SQLException, IOException {
	int count = 0;
	logger.info("indexing ClinicalTrials.gov official contacts...");
	PreparedStatement stmt = wintermuteConn.prepareStatement("select overall_official_name,overall_official_role,overall_official_affiliation,count(*) from clinical_trials.overall_official group by 1,2,3 order by 4 desc");
	ResultSet rs = stmt.executeQuery();
	while (rs.next()) {
	    String name = rs.getString(1);
	    String title = rs.getString(2);
	    String site = rs.getString(3);
	    
	    logger.debug("login: " + name + "\t" + site);

	    Document theDocument = new Document();
	    List<CategoryPath> paths = new ArrayList<CategoryPath>();
	    
	    theDocument.add(new Field("source", "ClinicalTrials.gov", Field.Store.YES, Field.Index.NOT_ANALYZED));
	    paths.add(new CategoryPath("Source/ClinicalTrials.gov", '/'));

	    theDocument.add(new Field("uri", "http://ClinicalTrials.gov/"+name, Field.Store.YES, Field.Index.NOT_ANALYZED));
	    if (name != null ) {
		theDocument.add(new Field("label", name+(title == null ? "" : (", "+title)) + " ", Field.Store.YES, Field.Index.ANALYZED));
		theDocument.add(new Field("content", name + " ", Field.Store.NO, Field.Index.ANALYZED));
	    }
	    if (title == null)
		paths.add(new CategoryPath("Entity/Person/unknown", '/'));
	    else {
		theDocument.add(new Field("title", title + " ", Field.Store.YES, Field.Index.ANALYZED));
		theDocument.add(new Field("content", title + " ", Field.Store.NO, Field.Index.ANALYZED));
		try {
		    paths.add(new CategoryPath("Entity/Person/"+title, '/'));
		} catch (Exception e) {
		    logger.error("error adding title facet", e);
		}
	    }
	    
	    if (site != null) {
		theDocument.add(new Field("site", site, Field.Store.YES, Field.Index.NOT_ANALYZED));
		theDocument.add(new Field("content", site + " ", Field.Store.NO, Field.Index.ANALYZED));
		try {
		    paths.add(new CategoryPath("Site/"+site.replaceAll("/", "_"), '/'));
		} catch (Exception e) {
		    logger.error("error adding site facet", e);
		}
	    } else {
		paths.add(new CategoryPath("Site/unknown", '/'));		
	    }

	    facetFields.addFields(theDocument, paths);
	    indexWriter.addDocument(theDocument);
	    count++;
	}
	stmt.close();
	logger.info("\tusers indexed: " + count);
    }
    
    @SuppressWarnings("deprecation")
    static void indexClinicalTrials(IndexWriter indexWriter, FacetFields facetFields) throws SQLException, IOException {
	int count = 0;
	logger.info("indexing ClinicalTrials.gov trials...");
	PreparedStatement stmt = wintermuteConn.prepareStatement("select id,nct_id,brief_title,official_title,overall_status,study_type from clinical_trials.study");
	ResultSet rs = stmt.executeQuery();
	while (rs.next()) {
	    int ID = rs.getInt(1);
	    String nctID = rs.getString(2);
	    String briefTitle = rs.getString(3);
	    String title = rs.getString(4);
	    String status = rs.getString(5);
	    String type = rs.getString(6);
	    
	    logger.debug("trial: " + nctID + "\t" + briefTitle);

	    Document theDocument = new Document();
	    List<CategoryPath> paths = new ArrayList<CategoryPath>();
	    
	    theDocument.add(new Field("source", "ClinicalTrials.gov", Field.Store.YES, Field.Index.NOT_ANALYZED));
	    paths.add(new CategoryPath("Source/ClinicalTrials.gov", '/'));
	    paths.add(new CategoryPath("Entity/Clinical Trial", '/'));

	    theDocument.add(new Field("uri", "https://clinicaltrials.gov/ct2/show/"+nctID, Field.Store.YES, Field.Index.NOT_ANALYZED));
	    theDocument.add(new Field("content", nctID + " ", Field.Store.NO, Field.Index.ANALYZED));
	    if (briefTitle != null ) {
		theDocument.add(new Field("label", briefTitle + " ", Field.Store.YES, Field.Index.ANALYZED));
		theDocument.add(new Field("content", briefTitle + " ", Field.Store.NO, Field.Index.ANALYZED));
	    }
	    if (title != null)  {
		theDocument.add(new Field("content", title + " ", Field.Store.NO, Field.Index.ANALYZED));
	    }
	    
	    if (status != null) {
		theDocument.add(new Field("content", status + " ", Field.Store.NO, Field.Index.ANALYZED));
		try {
		    paths.add(new CategoryPath("Status/"+status, '/'));
		} catch (Exception e) {
		    logger.error("error adding status facet", e);
		}
	    }

	    PreparedStatement substmt = wintermuteConn.prepareStatement("select phase from clinical_trials.phase where id = ?");
	    substmt.setInt(1, ID);
	    ResultSet subrs = substmt.executeQuery();
	    while (subrs.next()) {
		String phase = subrs.getString(1);
		if (phase == null || phase.startsWith("Http"))
		    continue;
		theDocument.add(new Field("content", phase + " ", Field.Store.NO, Field.Index.ANALYZED));
		try {
		    paths.add(new CategoryPath("Phase/" + phase, '/'));
		} catch (Exception e) {
		    logger.error("error adding phase facet", e);
		}
	    }
	    substmt.close();

	    if (type != null) {
		theDocument.add(new Field("content", type + " ", Field.Store.NO, Field.Index.ANALYZED));
		try {
		    paths.add(new CategoryPath("Type/"+type, '/'));
		} catch (Exception e) {
		    logger.error("error adding type facet", e);
		}
	    }

	    substmt = wintermuteConn.prepareStatement("select condition from clinical_trials.condition where id = ?");
	    substmt.setInt(1, ID);
	    subrs = substmt.executeQuery();
	    while (subrs.next()) {
		String condition = subrs.getString(1);
		if (condition == null || condition.startsWith("Http"))
		    continue;
		theDocument.add(new Field("content", condition + " ", Field.Store.NO, Field.Index.ANALYZED));
		try {
		    paths.add(new CategoryPath("Condition/" + condition, '/'));
		} catch (Exception e) {
		    logger.error("error adding condition facet", e);
		}
	    }
	    substmt.close();

	    facetFields.addFields(theDocument, paths);
	    indexWriter.addDocument(theDocument);
	    count++;
	}
	stmt.close();
	logger.info("\ttrials indexed: " + count);
    }
    
    static void indexChiCTRTrials() throws IOException, SQLException {
	Directory indexDir = FSDirectory.open(new File(pathPrefix + "chictr"));
	Directory taxoDir = FSDirectory.open(new File(pathPrefix + "chictr_tax"));

	IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_43, new BiomedicalAnalyzer());
	config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
	IndexWriter indexWriter = new IndexWriter(indexDir, config);

	// Writes facet ords to a separate directory from the main index
	DirectoryTaxonomyWriter taxoWriter = new DirectoryTaxonomyWriter(taxoDir);

	// Reused across documents, to add the necessary facet fields
	FacetFields facetFields = new FacetFields(taxoWriter);
	
	indexChiCTRTrials(indexWriter, facetFields);

	taxoWriter.close();
	indexWriter.close();
    }
    
    @SuppressWarnings("deprecation")
    static void indexChiCTRTrials(IndexWriter indexWriter, FacetFields facetFields) throws SQLException, IOException {
	int count = 0;
	logger.info("indexing ChiCTR trials...");
	PreparedStatement stmt = wintermuteConn.prepareStatement("select id,reg_name,primary_sponsor,public_title,acronym,scientific_title,scientific_acronym,target_size,recruitment_status,url,study_type,study_design,phase,hc_freetext,i_freetext from covid_chictr.study");
	ResultSet rs = stmt.executeQuery();
	while (rs.next()) {
	    String ID = rs.getString(1);
	    String reg_name = rs.getString(2);
	    String primary_sponsor = rs.getString(3);
	    String public_title = rs.getString(4);
	    String acronym = rs.getString(5);
	    String scientific_title = rs.getString(6);
	    String scientific_acronym = rs.getString(7);
	    String target_size = rs.getString(8);
	    String recruitment_status = rs.getString(9);
	    String url = rs.getString(10);
	    String study_type = rs.getString(11);
	    String study_design = rs.getString(12);
	    String phase = rs.getString(13);
	    String hc_freetext = rs.getString(14);
	    String i_freetext = rs.getString(15);
	    
	    logger.debug("trial: " + ID + "\t" + public_title);

	    Document theDocument = new Document();
	    List<CategoryPath> paths = new ArrayList<CategoryPath>();
	    
	    theDocument.add(new Field("source", reg_name, Field.Store.YES, Field.Index.NOT_ANALYZED));
	    paths.add(new CategoryPath("Source/"+reg_name, '/'));
	    paths.add(new CategoryPath("Entity/Clinical Trial", '/'));

	    theDocument.add(new Field("uri", url, Field.Store.YES, Field.Index.NOT_ANALYZED));
	    theDocument.add(new Field("content", ID + " ", Field.Store.NO, Field.Index.ANALYZED));
	    if (scientific_title != null ) {
		theDocument.add(new Field("label", scientific_title + " ", Field.Store.YES, Field.Index.ANALYZED));
		theDocument.add(new Field("content", scientific_title + " ", Field.Store.NO, Field.Index.ANALYZED));
	    }
	    if (primary_sponsor != null)  {
		theDocument.add(new Field("content", primary_sponsor + " ", Field.Store.NO, Field.Index.ANALYZED));
	    }
	    if (public_title != null)  {
		theDocument.add(new Field("content", public_title + " ", Field.Store.NO, Field.Index.ANALYZED));
	    }
	    if (acronym != null)  {
		theDocument.add(new Field("content", acronym + " ", Field.Store.NO, Field.Index.ANALYZED));
	    }
	    if (scientific_acronym != null)  {
		theDocument.add(new Field("content", scientific_acronym + " ", Field.Store.NO, Field.Index.ANALYZED));
	    }
	    if (target_size != null)  {
		theDocument.add(new Field("content", target_size + " ", Field.Store.NO, Field.Index.ANALYZED));
	    }
	    if (study_design != null)  {
		theDocument.add(new Field("content", study_design + " ", Field.Store.NO, Field.Index.ANALYZED));
	    }
	    if (hc_freetext != null)  {
		theDocument.add(new Field("content", hc_freetext + " ", Field.Store.NO, Field.Index.ANALYZED));
	    }
	    if (i_freetext != null)  {
		theDocument.add(new Field("content", i_freetext + " ", Field.Store.NO, Field.Index.ANALYZED));
	    }
	    
	    if (recruitment_status != null) {
		theDocument.add(new Field("content", recruitment_status + " ", Field.Store.NO, Field.Index.ANALYZED));
		try {
		    paths.add(new CategoryPath("Status/"+recruitment_status, '/'));
		} catch (Exception e) {
		    logger.error("error adding status facet", e);
		}
	    }
	    if (study_type != null) {
		theDocument.add(new Field("content", study_type + " ", Field.Store.NO, Field.Index.ANALYZED));
		try {
		    paths.add(new CategoryPath("Type/"+study_type, '/'));
		} catch (Exception e) {
		    logger.error("error adding type facet", e);
		}
	    }
	    if (phase != null) {
		theDocument.add(new Field("content", phase + " ", Field.Store.NO, Field.Index.ANALYZED));
		try {
		    paths.add(new CategoryPath("Phase/"+phase, '/'));
		} catch (Exception e) {
		    logger.error("error adding type facet", e);
		}
	    }

	    PreparedStatement substmt = wintermuteConn.prepareStatement("select inclusion_criteria,exclusion_criteria from covid_chictr.criteria where id = ?");
	    substmt.setString(1, ID);
	    ResultSet subrs = substmt.executeQuery();
	    while (subrs.next()) {
		String inclusion_criteria = subrs.getString(1);
		String exclusion_criteria = subrs.getString(1);
		if (inclusion_criteria != null) {
		    theDocument.add(new Field("content", inclusion_criteria + " ", Field.Store.NO, Field.Index.ANALYZED));
		}
		if (exclusion_criteria != null) {
		    theDocument.add(new Field("content", exclusion_criteria + " ", Field.Store.NO, Field.Index.ANALYZED));
		}
	    }
	    substmt.close();

	    substmt = wintermuteConn.prepareStatement("select prim_outcome from covid_chictr.primary_outcome where id = ?");
	    substmt.setString(1, ID);
	    subrs = substmt.executeQuery();
	    while (subrs.next()) {
		String prim_outcome = subrs.getString(1);
		if (prim_outcome != null) {
		    theDocument.add(new Field("content", prim_outcome + " ", Field.Store.NO, Field.Index.ANALYZED));
		}
	    }
	    substmt.close();

	    substmt = wintermuteConn.prepareStatement("select sec_id from covid_chictr.secondary_id where id = ?");
	    substmt.setString(1, ID);
	    subrs = substmt.executeQuery();
	    while (subrs.next()) {
		String sec_id = subrs.getString(1);
		if (sec_id != null) {
		    theDocument.add(new Field("content", sec_id + " ", Field.Store.NO, Field.Index.ANALYZED));
		}
	    }
	    substmt.close();

	    substmt = wintermuteConn.prepareStatement("select sec_outcome from covid_chictr.secondary_outcome where id = ?");
	    substmt.setString(1, ID);
	    subrs = substmt.executeQuery();
	    while (subrs.next()) {
		String sec_outcome = subrs.getString(1);
		if (sec_outcome != null) {
		    theDocument.add(new Field("content", sec_outcome + " ", Field.Store.NO, Field.Index.ANALYZED));
		}
	    }
	    substmt.close();

	    substmt = wintermuteConn.prepareStatement("select source_support from covid_chictr.source_support where id = ?");
	    substmt.setString(1, ID);
	    subrs = substmt.executeQuery();
	    while (subrs.next()) {
		String source_support = subrs.getString(1);
		if (source_support != null) {
		    theDocument.add(new Field("content", source_support + " ", Field.Store.NO, Field.Index.ANALYZED));
		}
	    }
	    substmt.close();

	    facetFields.addFields(theDocument, paths);
	    indexWriter.addDocument(theDocument);
	    count++;
	}
	stmt.close();
	logger.info("\ttrials indexed: " + count);
    }
    
    public static Connection getConnection(String property_file) throws SQLException, ClassNotFoundException {
	LocalProperties prop_file = PropertyLoader.loadProperties(property_file);
	Class.forName("org.postgresql.Driver");
	Properties props = new Properties();
	props.setProperty("user", prop_file.getProperty("jdbc.user"));
	props.setProperty("password", prop_file.getProperty("jdbc.password"));
//	if (use_ssl.equals("true")) {
//	    props.setProperty("sslfactory", "org.postgresql.ssl.NonValidatingFactory");
//	    props.setProperty("ssl", "true");
//	}
	Connection conn = DriverManager.getConnection(prop_file.getProperty("jdbc.url"), props);
	conn.setAutoCommit(false);
	return conn;
    }

}
