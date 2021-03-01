package org.cd2h.covid;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import edu.uiowa.slis.GitHubTagLib.util.LocalProperties;
import edu.uiowa.slis.GitHubTagLib.util.PropertyLoader;

public class PMCModeler {
	static Logger logger = Logger.getLogger(PMCModeler.class);
	protected static LocalProperties prop_file = null;
    static DecimalFormat formatter = new DecimalFormat("00");
	static Connection conn = null;
	static Hashtable<String, Tag> leaves = new Hashtable<String, Tag>();

	public static void main(String[] args) throws Exception {
		PropertyConfigurator.configure("log4j.info");
		initialize();
		
		Tag root = new Tag("root");

		fetchRecords(root);
		
		logger.info("Tree count:");
		
		emit(root, 0);
		generateScript(root);
	} 
	
	static void generateScript(Tag root) throws FileNotFoundException {
		PrintWriter writer = new PrintWriter("workspace/COVID_19TagLib/src/non-packaged-resources/covid_pmc_generated_views.sql");
		writer.println("drop view covid_pmc.paragraph_staging_filter cascade;\n"
				+ "\n"
				+ "create view covid_pmc.paragraph_staging_filter as\n"
				+ "select\n"
				+ "	pmcid,\n"
				+ "	p as orig,\n"
				+ "	p::text\n"
				+ "from covid_pmc.paragraph_staging;\n"
				+ "\n"
				+ "");
		writer.println("create view covid_pmc.paragraph_staging_comment_filter as\n"
				+ "select\n"
				+ "	pmcid,\n"
				+ "	orig,\n"
				+ "	regexp_replace(p, '<!--[^>]*>', '', 'g') as p\n"
				+ "from covid_pmc.paragraph_staging_filter;\n"
				+ "\n"
				+ "");
		
		String previousView = "covid_pmc.paragraph_staging_comment_filter";
		int iteration = 0;
		while (root.children.size() > 0) {
			logger.info("");
			logger.info("iteration: " + ++iteration);
			logger.info("");
//			emit(root, 0);

			// find the current leaves
			leaves = new Hashtable<String, Tag>();
			analyze(root);
			
			// emit them
			Enumeration<Tag> leafEnum = leaves.elements();
			while (leafEnum.hasMoreElements()) {
				Tag current = leafEnum.nextElement();
				logger.info("leaf: " + current.tag + " : " + current.count + " hasContent:" + current.hasContent + " isSingleton: " + current.isSingleton);
				String viewName = generateViewName(iteration, current.tag);
				String view = "create view " + viewName + " as\n"
						+ "select\n"
						+ "	pmcid,\n"
						+ "	orig,\n"
						+ "	regexp_replace(p, '<"+current.tag+"(?: [^>]*)?>([^<]*)</"+current.tag+">', '\\1', 'g') as p\n"
						+ "from " + previousView + ";\n"
						+ "";
				writer.println(view);
				previousView = viewName;
				
				if (current.isSingleton) {
					String viewName2 = generateViewName2(iteration, current.tag);
					String view2 = "create view " + viewName2 + " as\n"
							+ "select\n"
							+ "	pmcid,\n"
							+ "	orig,\n"
							+ "	regexp_replace(p, '<"+current.tag+"(?: [^>]*)?/>', '\\1', 'g') as p\n"
							+ "from " + previousView + ";\n"
							+ "";
					writer.println(view2);
					previousView = viewName2;
				}
			}
			
			// prune current leaves from the tree
			prune(root);
		}
		writer.println("create view covid_pmc.sec_para_final as\n"
				+ "select\n"
				+ "	pmcid,\n"
				+ "	orig,\n"
				+ "	p\n"
				+ "from " + previousView + ";\n"
				+ "\n"
				+ "select * from covid_pmc.sec_para_final where p ~ '<' limit 10;\n"
				+ "");
		writer.close();
	}
	
	static String generateViewName(int iteration, String tag) {
		return "covid_pmc.paragraph_staging_filter_" + formatter.format(iteration) + "_" + tag.replaceAll("[-:]", "_");
	}

	static String generateViewName2(int iteration, String tag) {
		return "covid_pmc.paragraph_staging_filter_" + formatter.format(iteration) + "a_" + tag.replaceAll("[-:]", "_");
	}

	static void fetchRecords(Tag root) throws Exception {
		PreparedStatement fetchStmt = conn.prepareStatement("select pmcid, p::text from covid_pmc.paragraph_staging");
		ResultSet rs = fetchStmt.executeQuery();
		while (rs.next()) {
			int pmcid = rs.getInt(1);
			String paragraphText = rs.getString(2);
			try {
				Element paragraph = parseDocument(pmcid,paragraphText);
				walk(root, paragraph, 0);
			} catch (java.lang.InternalError e) {
				logger.error("Exception raised: " + e);
			}
		}
	}

	static Element parseDocument(int pmcid, String paragraph) throws Exception {
		logger.info("scanning " + pmcid + "...");
		Document document = null;
		SAXReader reader = new SAXReader(false);
		document = reader.read(new ByteArrayInputStream(paragraph.getBytes()));

		Element root = document.getRootElement();
		logger.info("document root: " + root.asXML());
		return root;
	}
	
	static void walk(Tag parent, Element element, int indent) {
		String label = (element.getNamespacePrefix().length() > 0 ? element.getNamespacePrefix()+":" : "") + element.getName();
		logger.info(indent(indent) + label);

		Tag current = parent.children.get(label);
		if (current == null) {
			current = new Tag(label);
			parent.children.put(label, current);
		}
		current.count++;
		if (element.hasContent())
			current.hasContent = true;
		if (!element.hasContent())
			current.isSingleton = true;
		
	    for (int i = 0, size = element.nodeCount(); i < size; i++) {
	        Node node = element.node(i);
	        if (node instanceof Element) {
	            walk(current, (Element) node, indent+1);
	        }
	        else {
//	            logger.info("element: " + element);
	        }
	    }
	}
	
	static void emit(Tag current, int indent) {
		logger.info(indent(indent) + current.tag + " : " + current.count);
		
		Enumeration<Tag> children = current.children.elements();
		while (children.hasMoreElements()) {
			Tag child = children.nextElement();
			emit(child, indent+1);
		}
	}
	
	static void analyze(Tag current) {
		if (current.children.size() == 0) {
			logger.trace("leaf: " + current.tag + " : " + current.count);
			Tag leaf = leaves.get(current.tag);
			if (leaf == null) {
				leaf = new Tag(current.tag);
				leaves.put(current.tag, leaf);
			}
			leaf.count += current.count;
			leaf.hasContent = leaf.hasContent || current.hasContent;
			leaf.isSingleton = leaf.isSingleton || current.isSingleton;
		}
		
		Enumeration<Tag> children = current.children.elements();
		while (children.hasMoreElements()) {
			Tag child = children.nextElement();
			analyze(child);
		}
	}
	
	static void prune(Tag current) {
		Enumeration<Tag> children = current.children.elements();
		while (children.hasMoreElements()) {
			Tag child = children.nextElement();
			if (child.children.size() == 0) {
				current.children.remove(child.tag);
			} else
				prune(child);
		}
	}
	
	static String indent(int indent) {
		StringBuffer str = new StringBuffer();
		for (int i = 0; i < indent; i++)
			str.append("  ");
		return str.toString();
	}

	static public void initialize() throws ClassNotFoundException, SQLException {
		prop_file = PropertyLoader.loadProperties("pmc");

		conn = getConnection();
	}

	public static Connection getConnection() throws SQLException, ClassNotFoundException {
		Class.forName("org.postgresql.Driver");
		Properties props = new Properties();
		props.setProperty("user", prop_file.getProperty("jdbc.user.name"));
		props.setProperty("password", prop_file.getProperty("jdbc.user.password"));
		Connection conn = DriverManager.getConnection(prop_file.getProperty("jdbc.url"), props);
		return conn;
	}

	public static void simpleStmt(String queryString) {
		try {
			logger.info("executing " + queryString + "...");
			PreparedStatement beginStmt = conn.prepareStatement(queryString);
			beginStmt.executeUpdate();
			beginStmt.close();
		} catch (Exception e) {
			logger.error("Error in database initialization: " + e);
			e.printStackTrace();
		}
	}
	
	static class Tag {
		String tag = null;
		int count = 0;
		boolean hasContent = false;
		boolean isSingleton = false;
		Hashtable<String, Tag> children = new Hashtable<String, Tag>();
		
		Tag(String tag) {
			this.tag = tag;
		}
	}
}
