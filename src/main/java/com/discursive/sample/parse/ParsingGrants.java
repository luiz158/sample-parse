package com.discursive.sample.parse;

import java.io.File;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;

public class ParsingGrants {

	private Logger logger = Logger.getLogger(ParsingGrants.class);

	public static void main(String[] args) throws Exception {
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.INFO);
		new ParsingGrants().go();
	}

	public void go() throws Exception {
		logger.info("Creating Index");
		Directory index = new SimpleFSDirectory(new File("index"));
		IndexWriter writer = new IndexWriter(index, new SimpleAnalyzer(), true,
				IndexWriter.MaxFieldLength.UNLIMITED);
		GrantIndexer grantIndexer = new GrantIndexer(writer);
		grantIndexer.init();
		grantIndexer.index(new File("./data/grants.xml"));
		writer.optimize();
		writer.close();
		logger.info("Parsing Complete, Index Created");
	}

}
