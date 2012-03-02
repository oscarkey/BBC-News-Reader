/*******************************************************************************
 * BBC News Reader
 * Released under the BSD License. See README or LICENSE.
 * Copyright (c) 2011, Digital Lizard (Oscar Key, Thomas Boby)
 * All rights reserved.
 ******************************************************************************/
package com.digitallizard.bbcnewsreader.resource.web;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.util.ByteArrayBuffer;

public class ImageDownloader {
	public static byte[] getImage(URL url) throws Exception {
		URLConnection connection = url.openConnection();
		
		InputStream stream = connection.getInputStream();
		BufferedInputStream inputbuffer = new BufferedInputStream(stream, 8000);
		
		ByteArrayBuffer arraybuffer = new ByteArrayBuffer(50);
		int current = 0;
		while ((current = inputbuffer.read()) != -1) {
			arraybuffer.append((byte) current);
		}
		
		byte[] image = arraybuffer.toByteArray();
		
		return image;
	}
}
