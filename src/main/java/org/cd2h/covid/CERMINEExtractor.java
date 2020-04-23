package org.cd2h.covid;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.jdom.Element;

import pl.edu.icm.cermine.ContentExtractor;
import pl.edu.icm.cermine.exception.AnalysisException;

public class CERMINEExtractor {
    static String filePrefix = "/Volumes/Pegasus0/COVID/";

    public static void main(String[] args) throws AnalysisException, IOException {
	System.setProperty("java.awt.headless", "true");

	ContentExtractor extractor = new ContentExtractor();
	InputStream inputStream = new FileInputStream(filePrefix+"2020.04.21.042911v1.full.pdf");
	extractor.setPDF(inputStream);
	Element result = extractor.getContentAsNLM();
	System.out.println(result.getText());
    }

}
