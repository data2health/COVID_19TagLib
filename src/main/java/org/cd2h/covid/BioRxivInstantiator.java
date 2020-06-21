package org.cd2h.covid;

import java.sql.Connection;
import java.sql.SQLException;

import edu.uiowa.NLP_grammar.syntaxTree;
import edu.uiowa.entity.Entity;
import edu.uiowa.extraction.LocalProperties;
import edu.uiowa.extraction.Template;
import edu.uiowa.extraction.TemplateInstantiator;

public class BioRxivInstantiator extends TemplateInstantiator {

    public BioRxivInstantiator(LocalProperties prop_file, Connection conn) throws ClassNotFoundException, SQLException {
	super(prop_file, conn);
	// TODO Auto-generated constructor stub
    }

    @Override
    protected void instantiateEntity(int id, syntaxTree constituent, Template template) throws Exception {
	// TODO Auto-generated method stub
	
    }

    @Override
    public void resolveID(int id, Entity elementAt) throws SQLException {
	// TODO Auto-generated method stub
	
    }

}
