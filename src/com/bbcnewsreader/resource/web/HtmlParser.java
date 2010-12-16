package com.bbcnewsreader.resource.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

public class HtmlParser {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public static void getPage(URI uri) throws ClientProtocolException, IOException {
		// TODO Auto-generated method stub
		HttpClient client = new DefaultHttpClient();
		//HttpGet request = new HttpGet(uri.toString());
		HttpGet request = new HttpGet(uri.toString());
		HttpResponse response = client.execute(request);

		String html = "";
		InputStream in = response.getEntity().getContent();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in),50000);
		StringBuilder str = new StringBuilder();
		String line = null;
		while((line = reader.readLine()) != null)
		{
		    str.append(line);
		}
		in.close();
		html = str.toString();
		Log.v("TEST",html);
		Log.v("TEST",Integer.toString(html.length()));
		String parsed;
		Pattern p=Pattern.compile("<div class=\"storybody\">.*?</div>");
		Matcher m = p.matcher(html);
		parsed=m.toMatchResult().group(0);
		
		Log.v("TEST","parsed:"+parsed);
		Log.v("TEST","nothing");
	}

}
