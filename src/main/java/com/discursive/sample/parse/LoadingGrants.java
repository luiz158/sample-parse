package com.discursive.sample.parse;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.codehaus.staxmate.SMInputFactory;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;

public class LoadingGrants {

	Logger logger = Logger.getLogger( LoadingGrants.class );
	
	private String couchDbUrl = "http://localhost:5984/grants/_bulk_docs";
	private int bulkSize = 1000;

	public static void main(String[] args) throws Exception {
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.DEBUG);
		new LoadingGrants().go();
	}
	
	public void go() throws Exception {
		Reader reader = new FileReader( "./data/grants.xml" );
		
		HttpClient client = new DefaultHttpClient();
		
		// Use Staxmate to parse grants.xml as a stream
		SMInputFactory inf = new SMInputFactory(XMLInputFactory.newInstance());
		File grantsFile = new File("./data/grants.xml");
		SMHierarchicCursor rootC = inf.rootElementCursor( reader );
		rootC.advance();
		// Advance through the "content" elements under the root element.
		QName content = new QName("http://www.w3.org/2005/Atom", "content");
		SMInputCursor contentC = rootC.descendantElementCursor(content);
		JSONArray bulkArray = new JSONArray();
		int counter = 1;
		while( contentC.getNext() != null ) {
			// Create a JSON Object that contains all of the elements under
			// feed/entry/content - this is possible because the grant data is 
			// a series of flat XML elements
			SMInputCursor childC = contentC.childElementCursor();
			JSONObject json = new JSONObject();
			while( childC.getNext() != null ) {
				json.put( childC.getLocalName(), childC.collectDescendantText() );
			}
			bulkArray.add( json );
			
			// Only Write Records to Couch in Bulk
			if( counter % bulkSize == 0 ) {
				postToCouch(couchDbUrl, client, bulkArray);
				bulkArray.clear();
			}
			counter++;
		}				
		// Post the last batch to Couch
		postToCouch(couchDbUrl, client, bulkArray);

		// Close the Stax stream
		rootC.getStreamReader().closeCompletely();
	}

	private void postToCouch(String couchDbUrl, HttpClient client,
			JSONArray jsonArray) throws UnsupportedEncodingException, IOException,
			ClientProtocolException {
		JSONObject request = new JSONObject();
		request.put( "all_or_nothing", true );
		request.put( "docs", jsonArray);
		
		StringEntity entity = new StringEntity(request.toString(), "UTF-8");
		HttpPost post = new HttpPost(couchDbUrl);
		post.setEntity(entity);
		HttpResponse response = client.execute(post);
		HttpEntity ent = response.getEntity();
		if (ent != null) {
			ent.consumeContent();
		}
	}
	
	
}
