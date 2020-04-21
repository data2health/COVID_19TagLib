package org.cd2h.covid;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.servlet.jsp.JspTagException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
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

public class FacetIndexer implements Runnable {
    static Logger logger = Logger.getLogger(FacetIndexer.class);
    static DecimalFormat formatter = new DecimalFormat("00");
    LocalProperties prop_file = null;
    static Connection wintermuteConn = null;
    static Connection deepConn = null;
    static String pathPrefix = "/usr/local/CD2H/lucene/";
    static String[] sites = {
	    			pathPrefix + "biorxiv",
	    			pathPrefix + "litcovid",
	    			pathPrefix + "chictr",
	    			pathPrefix + "ictrp",
	    			pathPrefix + "clinical_trials"
	    			};
    

    public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException, JspTagException, InterruptedException {
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
	case "ictrp":
	    indexICTRPTrials();
	    break;
	case "biorxiv":
	    indexBioRxiv();
	    break;
	case "litcovid":
	    indexLitCOVID();
	    break;
	case "medline":
//	    indexMEDLINE();
	    mergeMEDLINE();
	    break;
	case "results":
	    indexTrialResults();
	    break;
	case "-merge":
	    mergeIndices(sites, pathPrefix + "covidsearch");
	    break;
	}
    }
    
    int threadID = 0;
    Connection threadConn = null;
    ArticleRequest theRequest = null;
    
    public FacetIndexer(int threadID) throws ClassNotFoundException, SQLException {
	//
	// Normally only instantiated for threading purposes
	//
	this.threadID = threadID;
	this.threadConn = getConnection("lucene");
	this.theRequest = new ArticleRequest();
	
	// we currently default in the run method to parallelizing MEDLINE indexing
    }
    
    public void run() {
	try {
	    indexMEDLINEThread();
	} catch (Exception e) {
	    logger.error("["+formatter.format(threadID)+" exception raised: ",e);
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
    
    static void indexBioRxiv(IndexWriter indexWriter, FacetFields facetFields) throws IOException, SQLException {
	int count = 0;
	logger.info("indexing bioRxiv/medRxiv preprints...");
	PreparedStatement stmt = wintermuteConn.prepareStatement("select doi from covid_biorxiv.biorxiv_current");
	ResultSet rs = stmt.executeQuery();

	while (rs.next()) {
	    count++;
	    String doi = rs.getString(1);

	    Document theDocument = new Document();
	    List<CategoryPath> paths = new ArrayList<CategoryPath>();
	    paths.add(new CategoryPath("Entity/Preprint", '/'));
	    
	    indexBioRxiv(doi, theDocument, paths, "uri");
	    
	    facetFields.addFields(theDocument, paths);
	    indexWriter.addDocument(theDocument);
	}
	stmt.close();
	logger.info("\tpublications indexed: " + count);
    }

    @SuppressWarnings("deprecation")
    static void indexBioRxiv(String doi, Document theDocument, List<CategoryPath> paths, String urlLabel) throws IOException, SQLException {
	PreparedStatement stmt = wintermuteConn.prepareStatement("select title,authors,link,site,pub_date,abstract from covid_biorxiv.biorxiv_current where doi = ?");
	stmt.setString(1, doi);
	ResultSet rs = stmt.executeQuery();

	while (rs.next()) {
	    String title = rs.getString(1);
	    String authors = rs.getString(2);
	    String link = rs.getString(3);
	    String site = rs.getString(4);
	    String pub_date = rs.getString(5);
	    String abstr = rs.getString(6);

	    logger.trace("preprint: " + doi + "\t" + title);

	    if (urlLabel.equals("pub uri"))
		    paths.add(new CategoryPath("Publication Source/"+site, '/'));
	    else
		    paths.add(new CategoryPath("Source/"+site, '/'));
	    
	    theDocument.add(new Field("source", site, Field.Store.YES, Field.Index.NOT_ANALYZED));
	    theDocument.add(new Field("pub source", site, Field.Store.YES, Field.Index.NOT_ANALYZED));
	    theDocument.add(new Field(urlLabel, link, Field.Store.YES, Field.Index.NOT_ANALYZED));
	    theDocument.add(new Field("id", doi, Field.Store.YES, Field.Index.NOT_ANALYZED));

	    if (title == null) {
		theDocument.add(new Field("label", site+" "+ doi + " ", Field.Store.YES, Field.Index.ANALYZED));
	    } else {
		theDocument.add(new Field("label", title + " ", Field.Store.YES, Field.Index.ANALYZED));		
		theDocument.add(new Field("pub label", title + " ", Field.Store.YES, Field.Index.ANALYZED));		
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
	}
	stmt.close();
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

	logger.info("indexing LitCOVID articles...");
	indexMEDLINE(wintermuteConn, indexWriter, facetFields, "LitCOVID", "covid_litcovid");

	taxoWriter.close();
	indexWriter.close();
    }
    
    static PreparedStatement qstmt = null;
    static ResultSet qrs = null;
    
    static class ArticleRequest {
	int pmid = 0;
	String title = null;
    }
    
    static boolean next(ArticleRequest theRequest) throws SQLException {
	if (qrs.next()) {
	    theRequest.pmid = qrs.getInt(1);
	    theRequest.title = qrs.getString(2);
	    return true;
	}
	return false;
    }
    
    static void indexMEDLINE() throws IOException, SQLException, InterruptedException, ClassNotFoundException {
	logger.info("indexing MEDLINE...");
	qstmt = wintermuteConn.prepareStatement("select pmid,article_title from medline.article_title where seqnum = 1 order by pmid");
	qrs = qstmt.executeQuery();

	int maxCrawlerThreads = Runtime.getRuntime().availableProcessors();
	Thread[] scannerThreads = new Thread[maxCrawlerThreads];
	String[] fileNames = new String[maxCrawlerThreads];
	
	for (int i = 0; i < maxCrawlerThreads; i++) {
	    logger.info("starting thread " + i);
	    Thread theThread = new Thread(new FacetIndexer(i));
	    theThread.setPriority(Math.max(theThread.getPriority() - 2, Thread.MIN_PRIORITY));
	    theThread.start();
	    scannerThreads[i] = theThread;
	    fileNames[i] = pathPrefix+"medline"+formatter.format(i);
	}

	for (int i = 0; i < maxCrawlerThreads; i++) {
	    scannerThreads[i].join();
	}
	logger.info("indexing completed.");
	qstmt.close();
	mergeIndices(fileNames, pathPrefix+"medline");
    }
    
    static void mergeMEDLINE () throws CorruptIndexException, SQLException, IOException {
	int maxCrawlerThreads = Runtime.getRuntime().availableProcessors();
	String[] fileNames = new String[maxCrawlerThreads];
	
	for (int i = 0; i < maxCrawlerThreads; i++) {
	    fileNames[i] = pathPrefix+"medline"+formatter.format(i);
	}
	mergeIndices(fileNames, pathPrefix+"medline");
    }
    
    void indexMEDLINEThread() throws IOException, SQLException {
	Directory indexDir = FSDirectory.open(new File(pathPrefix + "medline"+formatter.format(threadID)));
	Directory taxoDir = FSDirectory.open(new File(pathPrefix + "medline"+formatter.format(threadID)+"_tax"));

	IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_43, new BiomedicalAnalyzer());
	config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
	IndexWriter indexWriter = new IndexWriter(indexDir, config);

	// Writes facet ords to a separate directory from the main index
	DirectoryTaxonomyWriter taxoWriter = new DirectoryTaxonomyWriter(taxoDir);

	// Reused across documents, to add the necessary facet fields
	FacetFields facetFields = new FacetFields(taxoWriter);

	logger.info("["+formatter.format(threadID)+"] indexing MEDLINE articles...");
	int count = 0;
	while (next(theRequest)) {
	    if ((++count % 1000) == 0)
		logger.info("[" + formatter.format(threadID) + "] indexing: " + theRequest.pmid + " : " + theRequest.title);
	    indexMEDLINE(threadConn, indexWriter, facetFields, "MEDLINE", "medline", theRequest.pmid, theRequest.title);
	}

	taxoWriter.close();
	indexWriter.close();
    }
    
    static void indexMEDLINE(Connection threadConn, IndexWriter indexWriter, FacetFields facetFields, String source, String schemaName) throws IOException, SQLException {
	int count = 0;
	PreparedStatement stmt = threadConn.prepareStatement("select pmid,article_title from "+schemaName+".article_title where seqnum = 1");
	ResultSet rs = stmt.executeQuery();

	while (rs.next()) {
	    count++;
	    int pmid = rs.getInt(1);
	    String title = rs.getString(2);
	    
	    indexMEDLINE(threadConn, indexWriter, facetFields, source, schemaName, pmid, title);
	}
	stmt.close();
	logger.info("\tpublications indexed: " + count);
    }
    

    @SuppressWarnings("deprecation")
    static void indexMEDLINE(Connection threadConn, IndexWriter indexWriter, FacetFields facetFields, String source, String schemaName, int pmid, String title) throws IOException, SQLException {
	logger.trace("article: " + pmid + "\t" + title);

	Document theDocument = new Document();
	List<CategoryPath> paths = new ArrayList<CategoryPath>();

	paths.add(new CategoryPath("Entity/Article", '/'));
	paths.add(new CategoryPath("Source/" + source, '/'));

	theDocument.add(new Field("source", source, Field.Store.YES, Field.Index.NOT_ANALYZED));
	theDocument.add(new Field("uri", "https://www.ncbi.nlm.nih.gov/pubmed/" + pmid, Field.Store.YES, Field.Index.NOT_ANALYZED));
	theDocument.add(new Field("id", pmid + "", Field.Store.YES, Field.Index.NOT_ANALYZED));

	if (title == null) {
	    theDocument.add(new Field("label", "PubMed " + pmid + " ", Field.Store.YES, Field.Index.ANALYZED));
	} else {
	    theDocument.add(new Field("label", title + " ", Field.Store.YES, Field.Index.ANALYZED));
	    theDocument.add(new Field("content", title + " ", Field.Store.NO, Field.Index.ANALYZED));
	}

	indexMEDLINE(threadConn, pmid, theDocument, schemaName);

	facetFields.addFields(theDocument, paths);
	indexWriter.addDocument(theDocument);
    }
    

    @SuppressWarnings("deprecation")
    static void indexMEDLINE(Connection threadConn, int pmid, Document theDocument, List<CategoryPath> paths, String source, String schemaName, String urlLabel) throws SQLException {
	PreparedStatement stmt = threadConn.prepareStatement("select article_title from "+schemaName+".article_title where pmid = ? and seqnum = 1");
	stmt.setInt(1, pmid);
	ResultSet rs = stmt.executeQuery();

	while (rs.next()) {
	    String title = rs.getString(1);

	    logger.trace("article: " + pmid + "\t" + title);

	    if (urlLabel.equals("pub uri"))
		    paths.add(new CategoryPath("Publication Source/"+source, '/'));
	    else
		    paths.add(new CategoryPath("Source/"+source, '/'));
	    
	    theDocument.add(new Field("source", source, Field.Store.YES, Field.Index.NOT_ANALYZED));
	    theDocument.add(new Field("pub source", source, Field.Store.YES, Field.Index.NOT_ANALYZED));
	    theDocument.add(new Field(urlLabel, "https://www.ncbi.nlm.nih.gov/pubmed/" + pmid, Field.Store.YES, Field.Index.NOT_ANALYZED));
	    theDocument.add(new Field("pub id", pmid + "", Field.Store.YES, Field.Index.NOT_ANALYZED));

	    if (title == null) {
		theDocument.add(new Field("pub label", "PubMed "+ pmid + " ", Field.Store.YES, Field.Index.ANALYZED));
	    } else {
		theDocument.add(new Field("pub label", title + " ", Field.Store.YES, Field.Index.ANALYZED));		
		theDocument.add(new Field("content", title + " ", Field.Store.NO, Field.Index.ANALYZED));
	    }
	    
	    indexMEDLINE(threadConn, pmid, theDocument, schemaName);
	}
	stmt.close();
   }

    @SuppressWarnings("deprecation")
    static void indexMEDLINE(Connection threadConn, int pmid, Document theDocument, String schemaName) throws SQLException {
	PreparedStatement stmt = threadConn.prepareStatement("select abstract from "+schemaName+".abstract where pmid = ?");
	stmt.setInt(1, pmid);
	ResultSet rs = stmt.executeQuery();
	while (rs.next()) {
	    String theAbstract = rs.getString(1);
	    theDocument.add(new Field("content", theAbstract + " ", Field.Store.NO, Field.Index.ANALYZED));
	    logger.trace("\tabstract: " + theAbstract);
	}
	stmt.close();

	stmt = threadConn.prepareStatement("select last_name,fore_name,initials,suffix,collective_name from "+schemaName+".author where pmid = ?");
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

	stmt = threadConn.prepareStatement("select affiliation from "+schemaName+".author_affiliation where pmid = ?");
	stmt.setInt(1, pmid);
	rs = stmt.executeQuery();
	while (rs.next()) {
	    String affiliation = rs.getString(1);
	    theDocument.add(new Field("content", affiliation + " ", Field.Store.NO, Field.Index.ANALYZED));
	    logger.trace("\taffiliation: " + affiliation);
	}
	stmt.close();

	stmt = threadConn.prepareStatement("select keyword from "+schemaName+".keyword where pmid = ?");
	stmt.setInt(1, pmid);
	rs = stmt.executeQuery();
	while (rs.next()) {
	    String keyword = rs.getString(1);
	    theDocument.add(new Field("content", keyword + " ", Field.Store.NO, Field.Index.ANALYZED));
	    logger.trace("\tkeyword: " + keyword);
	}
	stmt.close();

	stmt = threadConn.prepareStatement("select descriptor_name from "+schemaName+".mesh_heading where pmid = ?");
	stmt.setInt(1, pmid);
	rs = stmt.executeQuery();
	while (rs.next()) {
	    String descriptor = rs.getString(1);
	    theDocument.add(new Field("content", descriptor + " ", Field.Store.NO, Field.Index.ANALYZED));
	    logger.trace("\tdescriptor: " + descriptor);
	}
	stmt.close();

	stmt = threadConn.prepareStatement("select registry_number,name_of_substance from "+schemaName+".chemical where pmid = ?");
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

	stmt = threadConn.prepareStatement("select gene_symbol from "+schemaName+".gene_symbol where pmid = ?");
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
    

    static void indexChiCTRTrials(IndexWriter indexWriter, FacetFields facetFields) throws SQLException, IOException {
	int count = 0;
	logger.info("indexing ChiCTR trials...");
	PreparedStatement stmt = wintermuteConn.prepareStatement("select id from covid_chictr.study");
	ResultSet rs = stmt.executeQuery();
	while (rs.next()) {
	    String ID = rs.getString(1);

	    Document theDocument = new Document();
	    List<CategoryPath> paths = new ArrayList<CategoryPath>();

	    paths.add(new CategoryPath("Entity/Clinical Trial", '/'));
	    indexChiCTRTrial(ID, theDocument, paths, "uri");

	    facetFields.addFields(theDocument, paths);
	    indexWriter.addDocument(theDocument);
	    count++;
	}
	stmt.close();
	logger.info("\ttrials indexed: " + count);
    }

    @SuppressWarnings("deprecation")
    static void indexChiCTRTrial(String ID, Document theDocument, List<CategoryPath> paths, String urlLabel) throws SQLException {
	PreparedStatement stmt = wintermuteConn.prepareStatement("select reg_name,primary_sponsor,public_title,acronym,scientific_title,scientific_acronym,target_size,recruitment_status,url,study_type,study_design,phase,hc_freetext,i_freetext from covid_chictr.study where id = ?");
	stmt.setString(1, ID);
	ResultSet rs = stmt.executeQuery();
	while (rs.next()) {
	    String reg_name = rs.getString(1);
	    String primary_sponsor = rs.getString(2);
	    String public_title = rs.getString(3);
	    String acronym = rs.getString(4);
	    String scientific_title = rs.getString(5);
	    String scientific_acronym = rs.getString(6);
	    String target_size = rs.getString(7);
	    String recruitment_status = rs.getString(8);
	    String url = rs.getString(9);
	    String study_type = rs.getString(10);
	    String study_design = rs.getString(11);
	    String phase = rs.getString(12);
	    String hc_freetext = rs.getString(13);
	    String i_freetext = rs.getString(14);
	    
	    logger.debug("trial: " + ID + "\t" + public_title);

	    theDocument.add(new Field("source", reg_name, Field.Store.YES, Field.Index.NOT_ANALYZED));
	    paths.add(new CategoryPath("Source/"+reg_name, '/'));

	    theDocument.add(new Field(urlLabel, url, Field.Store.YES, Field.Index.NOT_ANALYZED));
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
		String exclusion_criteria = subrs.getString(2);
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

	}
	stmt.close();
    }
    
    static void indexICTRPTrials() throws IOException, SQLException {
	Directory indexDir = FSDirectory.open(new File(pathPrefix + "ictrp"));
	Directory taxoDir = FSDirectory.open(new File(pathPrefix + "ictrp_tax"));

	IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_43, new BiomedicalAnalyzer());
	config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
	IndexWriter indexWriter = new IndexWriter(indexDir, config);

	// Writes facet ords to a separate directory from the main index
	DirectoryTaxonomyWriter taxoWriter = new DirectoryTaxonomyWriter(taxoDir);

	// Reused across documents, to add the necessary facet fields
	FacetFields facetFields = new FacetFields(taxoWriter);
	
	logger.info("indexing WHO ICTRP trials...");
	indexICTRPTrials(indexWriter, facetFields);

	taxoWriter.close();
	indexWriter.close();
    }
    
    static void indexICTRPTrials(IndexWriter indexWriter, FacetFields facetFields) throws SQLException, IOException {
	int count = 0;
	PreparedStatement stmt = wintermuteConn.prepareStatement("select trialid from who_ictrp.who where source_register != 'ChiCTR'"); // and source_register != 'ClinicalTrials.gov'");
	ResultSet rs = stmt.executeQuery();
	while (rs.next()) {
	    String ID = rs.getString(1);

	    Document theDocument = new Document();
	    List<CategoryPath> paths = new ArrayList<CategoryPath>();

	    paths.add(new CategoryPath("Entity/Clinical Trial", '/'));
	    indexICTRPTrial(ID, theDocument, paths, "uri");

	    facetFields.addFields(theDocument, paths);
	    indexWriter.addDocument(theDocument);
	    count++;
	}
	stmt.close();
	logger.info("\ttrials indexed: " + count);
    }

    @SuppressWarnings("deprecation")
    static void indexICTRPTrial(String ID, Document theDocument, List<CategoryPath> paths, String urlLabel) throws SQLException, IOException {
	PreparedStatement stmt = wintermuteConn.prepareStatement("select public_title,scientific_title,web_address,study_type,study_design,phase,countries,contact_firstname,contact_affiliation,inclusion_criteria,exclusion_criteria,condition,intervention,primary_outcome,recruitment_status,source_register from who_ictrp.who where trialid = ?");
	stmt.setString(1, ID);
	ResultSet rs = stmt.executeQuery();
	while (rs.next()) {
	    String public_title = rs.getString(1);
	    String scientific_title = rs.getString(2);
	    String url = rs.getString(3);
	    String study_type = rs.getString(4);
	    String study_design = rs.getString(5);
	    String phase = rs.getString(6);
	    String countries = rs.getString(7);
	    String contact_firstname = rs.getString(8);
	    String contact_affiliation = rs.getString(9);
	    String inclusion_criteria = rs.getString(10);
	    String exclusion_criteria = rs.getString(11);
	    String condition = rs.getString(12);
	    String intervention = rs.getString(13);
	    String primary_outcome = rs.getString(14);
	    String recruitment_status = rs.getString(15);
	    String source_register = rs.getString(16);
	    
	    logger.debug("trial: " + ID + "\t" + public_title);

	    theDocument.add(new Field("source", source_register, Field.Store.YES, Field.Index.NOT_ANALYZED));
	    paths.add(new CategoryPath("Trial Source/"+source_register,'/'));

	    theDocument.add(new Field(urlLabel, url, Field.Store.YES, Field.Index.NOT_ANALYZED));
	    theDocument.add(new Field("content", ID + " ", Field.Store.NO, Field.Index.ANALYZED));
	    if (scientific_title != null ) {
		theDocument.add(new Field("label", scientific_title + " ", Field.Store.YES, Field.Index.ANALYZED));
		theDocument.add(new Field("content", scientific_title + " ", Field.Store.NO, Field.Index.ANALYZED));
	    }
	    if (public_title != null)  {
		theDocument.add(new Field("content", public_title + " ", Field.Store.NO, Field.Index.ANALYZED));
	    }
	    if (public_title != null)  {
		theDocument.add(new Field("content", public_title + " ", Field.Store.NO, Field.Index.ANALYZED));
	    }
	    if (study_design != null)  {
		theDocument.add(new Field("content", study_design + " ", Field.Store.NO, Field.Index.ANALYZED));
	    }
	    if (countries != null)  {
		theDocument.add(new Field("content", countries + " ", Field.Store.NO, Field.Index.ANALYZED));
	    }
	    if (contact_firstname != null)  {
		theDocument.add(new Field("content", contact_firstname + " ", Field.Store.NO, Field.Index.ANALYZED));
	    }
	    if (study_design != null)  {
		theDocument.add(new Field("content", study_design + " ", Field.Store.NO, Field.Index.ANALYZED));
	    }
	    if (contact_affiliation != null)  {
		theDocument.add(new Field("content", contact_affiliation + " ", Field.Store.NO, Field.Index.ANALYZED));
	    }
	    if (inclusion_criteria != null)  {
		theDocument.add(new Field("content", inclusion_criteria + " ", Field.Store.NO, Field.Index.ANALYZED));
	    }
	    if (exclusion_criteria != null)  {
		theDocument.add(new Field("content", exclusion_criteria + " ", Field.Store.NO, Field.Index.ANALYZED));
	    }
	    if (condition != null)  {
		theDocument.add(new Field("content", condition + " ", Field.Store.NO, Field.Index.ANALYZED));
	    }
	    if (intervention != null)  {
		theDocument.add(new Field("content", intervention + " ", Field.Store.NO, Field.Index.ANALYZED));
	    }
	    if (primary_outcome != null)  {
		theDocument.add(new Field("content", primary_outcome + " ", Field.Store.NO, Field.Index.ANALYZED));
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

	    PreparedStatement substmt = wintermuteConn.prepareStatement("select intervention_type,intervention_name from clinical_trials.intervention natural join clinical_trials.study where nct_id = ?");
	    substmt.setString(1, ID);
	    ResultSet subrs = substmt.executeQuery();
	    while (subrs.next()) {
		String intervention_type = subrs.getString(1);
		String intervention_name = subrs.getString(2);
		logger.info(ID + ": " + intervention_type + " : " + intervention_name);
		try {
		    paths.add(new CategoryPath("Intervention/"+intervention_type+"/"+intervention_name, '/'));
		} catch (Exception e) {
		    logger.error("error adding intervention facet", e);
		}
	    }
	    substmt.close();
	}
	stmt.close();
    }
    
    static Hashtable<String,Vector<String>> chictrPreprintEmailCache = new Hashtable<String,Vector<String>>();
    static Hashtable<String,Vector<String>> chictrPreprintIDCache = new Hashtable<String,Vector<String>>();
    static Hashtable<String,Vector<String>> chictrPubEmailCache = new Hashtable<String,Vector<String>>();
    static Hashtable<String,Vector<String>> chictrPubIDCache = new Hashtable<String,Vector<String>>();
    static Hashtable<String,Vector<String>> whoPreprintEmailCache = new Hashtable<String,Vector<String>>();
    static Hashtable<String,Vector<String>> whoPreprintIDCache = new Hashtable<String,Vector<String>>();
    static Hashtable<String,Vector<String>> whoPubEmailCache = new Hashtable<String,Vector<String>>();
    static Hashtable<String,Vector<String>> whoPubIDCache = new Hashtable<String,Vector<String>>();
    
    @SuppressWarnings("deprecation")
    public static void indexTrialResults() throws SQLException, IOException {
	int count = 0;
	initializeCaches();
	Directory indexDir = FSDirectory.open(new File(pathPrefix + "trial_results"));
	Directory taxoDir = FSDirectory.open(new File(pathPrefix + "trial_results_tax"));

	IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_43, new BiomedicalAnalyzer());
	config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
	IndexWriter indexWriter = new IndexWriter(indexDir, config);

	// Writes facet ords to a separate directory from the main index
	DirectoryTaxonomyWriter taxoWriter = new DirectoryTaxonomyWriter(taxoDir);

	// Reused across documents, to add the necessary facet fields
	FacetFields facetFields = new FacetFields(taxoWriter);
	
	
	logger.info("scanning trial-preprint bindings...");
	PreparedStatement stmt = wintermuteConn.prepareStatement("select id,doi from covid.trial_preprint_map");
	ResultSet rs = stmt.executeQuery();
	while (rs.next()) {
	    String ID = rs.getString(1);
	    String doi = rs.getString(2);
	    logger.info("\ttrial: " + ID + "\tdoi: " + doi);

	    Document theDocument = new Document();
	    List<CategoryPath> paths = new ArrayList<CategoryPath>();
	    
	    if (chictrPreprintEmailCache.containsKey(ID) || chictrPreprintIDCache.containsKey(ID)) {
		theDocument.add(new Field("trial source", "ChiCTR", Field.Store.YES, Field.Index.NOT_ANALYZED));
		paths.add(new CategoryPath("Trial Source/ChiCTR",'/'));
		indexChiCTRTrial(ID, theDocument, paths, "trial uri");
	    } else if (whoPreprintEmailCache.containsKey(ID) || whoPreprintIDCache.containsKey(ID)) {
		theDocument.add(new Field("trial source", "WHO ICTRP", Field.Store.YES, Field.Index.NOT_ANALYZED));
		indexICTRPTrial(ID, theDocument, paths, "trial uri");
	    } else {
		theDocument.add(new Field("trial source", "Unknown", Field.Store.YES, Field.Index.NOT_ANALYZED));
		paths.add(new CategoryPath("Trial Source/Unknown",'/'));		
	    }
	    indexBioRxiv(doi, theDocument, paths, "pub uri");
	    
	    facetFields.addFields(theDocument, paths);
	    indexWriter.addDocument(theDocument);
	    count++;
	    
	}
	stmt.close();
	logger.info("preprint count: " + count);
	
	count = 0;
	logger.info("scanning trial-publication bindings...");
	stmt = wintermuteConn.prepareStatement("select id,pmid from covid.trial_pub_map");
	rs = stmt.executeQuery();
	while (rs.next()) {
	    String ID = rs.getString(1);
	    String pmid = rs.getString(2);
	    logger.info("\ttrial: " + ID + "\tpmid: " + pmid);

	    Document theDocument = new Document();
	    List<CategoryPath> paths = new ArrayList<CategoryPath>();
	    
	    if (chictrPubEmailCache.containsKey(ID) || chictrPubIDCache.containsKey(ID)) {
		theDocument.add(new Field("trial source", "ChiCTR", Field.Store.YES, Field.Index.NOT_ANALYZED));
		paths.add(new CategoryPath("Trial Source/ChiCTR",'/'));
		indexChiCTRTrial(ID, theDocument, paths, "trial uri");
	    } else if (whoPubEmailCache.containsKey(ID) || whoPubIDCache.containsKey(ID)) {
		theDocument.add(new Field("trial source", "WHO ICTRP", Field.Store.YES, Field.Index.NOT_ANALYZED));
		indexICTRPTrial(ID, theDocument, paths, "trial uri");
	    } else {
		theDocument.add(new Field("trial source", "Unknown", Field.Store.YES, Field.Index.NOT_ANALYZED));
		paths.add(new CategoryPath("Trial Source/Unknown",'/'));		
	    }
	    indexMEDLINE(wintermuteConn, Integer.parseInt(pmid), theDocument, paths, "LitCOVID", "covid_litcovid", "pub uri");
	    
	    facetFields.addFields(theDocument, paths);
	    indexWriter.addDocument(theDocument);
	    count++;
	}
	stmt.close();
	logger.info("publication count: " + count);

	taxoWriter.close();
	indexWriter.close();
    }
    
    public static void initializeCaches() throws SQLException {
	initializeCache(chictrPreprintEmailCache, "chictr_preprint_email_map", "doi");
	initializeCache(chictrPreprintIDCache, "chictr_preprint_id_map", "doi");
	initializeCache(chictrPubEmailCache, "chictr_pub_email_map", "pmid");
	initializeCache(chictrPubIDCache, "chictr_pub_id_map", "pmid");
	initializeCache(whoPreprintEmailCache, "who_preprint_email_map", "doi");
	initializeCache(whoPreprintIDCache, "who_preprint_id_map", "doi");
	initializeCache(whoPubEmailCache, "who_pub_email_map", "pmid");
	initializeCache(whoPubIDCache, "who_pub_id_map", "pmid");
    }
    
    public static void initializeCache(Hashtable<String,Vector<String>> cache, String tableName, String attributeName) throws SQLException {
	int count = 0;
	logger.info("initializing " + tableName + " cache...");
	
	PreparedStatement stmt = wintermuteConn.prepareStatement("select id,"+attributeName+" from covid."+tableName);
	ResultSet rs = stmt.executeQuery();
	while (rs.next()) {
	    count++;
	    String ID = rs.getString(1);
	    String pub = rs.getString(2);
	    Vector<String> pubs = cache.get(ID);
	    if (pubs == null) {
		pubs = new Vector<String>();
		cache.put(ID, pubs);
	    }
	    pubs.add(pub);
	}
	stmt.close();
	
	logger.info("\tcount: " + count);
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
//	conn.setAutoCommit(false);
	return conn;
    }

}
