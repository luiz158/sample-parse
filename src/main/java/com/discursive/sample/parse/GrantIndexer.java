package com.discursive.sample.parse;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.xmlrules.DigesterLoader;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.xml.sax.SAXException;

public class GrantIndexer {

	private Logger logger = Logger.getLogger( GrantIndexer.class );
	
	private IndexWriter indexWriter;
    private Digester digester;
    private DigestContext context;
    public GrantIndexer(IndexWriter pIndexWriter) {
        indexWriter = pIndexWriter;
    }
    
    public void init( ) {
        URL grantRules = 
            GrantIndexer.class.getResource("grant-digester-rules.xml");
        digester = DigesterLoader.createDigester( grantRules );
    }
        
    public void index(File grantsXml) throws IOException, SAXException {
        context = new DigestContext( );
        digester.push( context );
        digester.parse( grantsXml );
    }
    
    public void processEntry( ) {
        Document doc = new Document( );
        doc.add(new Field("id", context.grantId, Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field("zip", context.zip, Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field("funding", context.funding, Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field("city", context.city, Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field("state", context.state, Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field("title", context.title, Field.Store.YES, Field.Index.ANALYZED));
        doc.add(new Field("desc", context.desc, Field.Store.YES, Field.Index.ANALYZED));
        
        try {
            indexWriter.addDocument( doc );
        } catch( IOException ioe ) {
            logger.error( "Unable to add document to index", ioe);
        }
    }
    
    public class DigestContext {
        String grantId, zip, funding, state, city, title, desc;
        public void setGrantId(String grantId) { this.grantId = grantId; }
        public void setZip(String zip) { this.zip = zip; }
        public void setFunding(String funding) { this.funding = funding; }
        public void setCity(String city) { this.city = city; }
        public void setState(String state) { this.state = state; }
        public void setTitle(String title) { this.title = title; }
        public void setDesc(String desc) { this.desc = desc; }
		public void grantEnd( ) {
            processEntry( );
        }
    }
}
