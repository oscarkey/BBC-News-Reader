/*******************************************************************************
 * BBC News Reader
 * Released under the BSD License. See README or LICENSE.
 * Copyright (c) 2011, Digital Lizard (Oscar Key, Thomas Boby)
 * All rights reserved.
 ******************************************************************************/
package com.digitallizard.bbcnewsreader.resource.web;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.ByteArrayBuffer;

public class HtmlParser {
	
	private static final String USER_AGENT = "Mozilla/5.0 (Linux; U; Android 2.2.1; en-us; MB525 Build/3.4.2-107_JDN-9) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1";
	
	/**
	 * @param args
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public static byte[] getPage(String stringUrl) throws Exception {
		URL url = new URL(stringUrl);
		URLConnection connection = url.openConnection();
		System.setProperty("http.agent", "");
		connection.setRequestProperty("User-Agent", USER_AGENT);
		
		InputStream stream = connection.getInputStream();
		BufferedInputStream inputbuffer = new BufferedInputStream(stream);
		
		ByteArrayBuffer arraybuffer = new ByteArrayBuffer(50);
		int current = 0;
		while ((current = inputbuffer.read()) != -1) {
			arraybuffer.append((byte) current);
		}
		return arraybuffer.toByteArray();
	}
	
	public static String parsePage(byte[] bytes) {
		// FIXME needs a tidy up
		if (bytes != null) {
			// convert the bytes into a string
			String html = new String(bytes);
			// trying parsing the page for news
			final String[] parsedNews = html.split("<div class=\"story-body\">", 2);
			if (parsedNews.length > 1) {
				// assume there are start and stop tags
				return parsedNews[1].split("<div class=\"share-this\">", 2)[0];
			}
			else {
				// try parsing for sport
				final String[] parsedSport = html.split("<article class=\"mod story\">");
				if(parsedSport.length > 1) {
					return parsedSport[1].split("</article>", 2)[0];
				}
				else {
					// just return the entire page as a last resort
					return html;
				}
			}
		}
		else {
			return "";
		}
	}
}
