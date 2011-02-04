package com.bbcnewsreader.resource.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.bbcnewsreader.ResourceInterface;

public class HtmlParser {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public static String getPage(ResourceInterface handler, URI uri){
		try{
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(uri.toString());
			HttpResponse response = client.execute(request);
	
			String html = "";
			InputStream in = response.getEntity().getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in),100000);
			StringBuilder str = new StringBuilder();
			String line = null;
			while((line = reader.readLine()) != null)
			{
			    str.append(line);
			}
			in.close();
			html = str.toString();
			String parsed;
			parsed = html.split("<div class=\"storybody\">")[1];
			parsed = parsed.split("</div>")[0];
			return parsed;
		}
		catch(Exception e){
			handler.reportError(false, "There was an error retrieving the article.", e.getMessage());
			return null;
		}
	}

}
