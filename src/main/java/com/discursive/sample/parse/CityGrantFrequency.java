package com.discursive.sample.parse;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;

public class CityGrantFrequency {

	Logger logger = Logger.getLogger( CityGrantFrequency.class );
	
	public static void main(String args[]) throws Exception {
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.INFO);
		new CityGrantFrequency().go();
	}

	@SuppressWarnings("unchecked")
	public void go() throws Exception {
		Directory index = new SimpleFSDirectory( new File("index"));
        IndexReader reader = IndexReader.open( index, true );
        TermEnum terms = reader.terms( );
        List<Freq> termList = new ArrayList<Freq>( );
        while( terms.next( ) ) {
            if( terms.term( ).field( ).equals( "city" ) ) {
            	termList.add( new Freq( terms.term().text(), terms.docFreq( ) ) );
            }
        }
        Collections.sort( termList, new BeanComparator( "freq" ) );
        for( Freq freq : termList ) {
        	System.out.println( freq.freq + " " + freq.term );
        }
	}
	
	public class Freq {
		String term;
		int freq;
		public Freq(String term, int freq) {
			this.term = term;
			this.freq = freq;
		}
		public String getTerm() { return term; }
		public int getFreq() { return freq; }
	}

}
