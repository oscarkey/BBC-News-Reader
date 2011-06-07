package com.digitallizard.bbcnewsreader.resource.web;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.ByteArrayBuffer;

public class HtmlParser {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public static byte[] getPage(String stringUrl) throws Exception {
		URL url = new URL(stringUrl);
		URLConnection connection = url.openConnection();
		
		InputStream stream = connection.getInputStream();
		BufferedInputStream inputbuffer = new BufferedInputStream(stream);
		
		ByteArrayBuffer arraybuffer = new ByteArrayBuffer(50);
		int current = 0;
        while ((current = inputbuffer.read()) != -1) {
                arraybuffer.append((byte) current);
        }
        return arraybuffer.toByteArray();
	}
	
	public static String parsePage(byte[] bytes){
		if(bytes != null){
			//convert the bytes into a string
			String html = new String(bytes);
			//parse the page
			final String[] parsed = html.split("<div class=\"storybody\">", 2);
			if(parsed.length > 1) {
				// assume there are start and stop tags
				return parsed[1].split("</div>", 2)[0];
			} else{
				return parsed[0];
			}
		}
		else{
			return "";
		}
	}
}
