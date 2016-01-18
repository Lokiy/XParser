/**
 * Copyright (C) 2015 Luki(liulongke@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lokiy.x.net;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class HttpRequest {

	/**
	 * do get
	 *
	 * @param url     url
	 * @param headers headers
	 * @return URL response
	 */
	public static String sendGet(String url, int timeoutMillis, Map<String, String> headers) throws IOException {
		String result = "";
		BufferedReader in = null;
		try {
			URL realUrl = new URL(url);
			// open connection
			HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(timeoutMillis);
			// set request headers
			connection.setRequestProperty("Connection", "Keep-Alive");
			for (String key : headers.keySet()) {
				connection.setRequestProperty(key, headers.get(key));
			}
			// connect
			connection.connect();
			// get response
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} finally {// close input steam
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * do post
	 *
	 * @param url            URL
	 * @param timeoutMillis timeout
	 * @param params        param
	 * @param headers       header
	 * @param dataList      data
	 * @return response
	 *
	 * @throws IOException
	 */
	public static String sendPost(String url, int timeoutMillis, Map<String, String> params, Map<String, String> headers, List<Object> dataList) throws IOException {
		params = params == null ? new HashMap<String, String>() : params;
		headers = headers == null ? new HashMap<String, String>() : headers;
		dataList = dataList == null ? new ArrayList<>() : dataList;

		DataOutputStream dos = null;
		BufferedReader in = null;
		PrintWriter out = null;
		String result = "";

		String boundary = System.currentTimeMillis() + "";
		String end = "\r\n";
		String twoHyphens = "--";
		try {
			URL realUrl = new URL(url);
			// open connection
			HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
			connection.setRequestMethod("POST");
			connection.setConnectTimeout(timeoutMillis);
			connection.setReadTimeout(timeoutMillis);
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setUseCaches(false);
			// set header
			connection.setRequestProperty("Connection", "Keep-Alive");
			for (String key : headers.keySet()) {
				connection.setRequestProperty(key, headers.get(key));
			}

			StringBuilder param = new StringBuilder();
			if (!dataList.isEmpty()) {
				connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
				connection.connect();
				dos = new DataOutputStream(connection.getOutputStream());
				for (int i = 0; i < dataList.size(); i++) {
					Object obj = dataList.get(i);
					if (obj instanceof File || obj instanceof InputStream) {
						InputStream fis;
						if (obj instanceof File) {
							fis = new FileInputStream((File) obj);
						} else {
							fis = (InputStream) obj;
						}
						dataList.set(i, fis);
						String p = twoHyphens + boundary + end + "Content-Type: application/octet-stream" + end + "Content-Disposition: form-data; filename=\"file" + i + "\"; name=\"file" + i + "\"" + end + end;

						byte[] data = new byte[fis.available()];
						if (fis.read(data) != -1) {
							dos.writeBytes(p);
							dos.write(data);
						}

					} else if (obj instanceof String) {
						dos.write(((String) obj).getBytes());
					}
				}

				if (!params.isEmpty()) {
					for (String key : params.keySet()) {
						param.append(end).append(twoHyphens).append(boundary).append(end).append("Content-Type: text/plain").append(end).append("Content-Disposition: form-data; name=\"").append(key).append("\"").append(end).append(end).append(URLEncoder.encode(params.get(key), "UTF-8")).append(end).append(twoHyphens).append(boundary).append(twoHyphens);
					}
				}
				dos.writeBytes(param.toString());
				dos.flush();
			} else {
				connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				dos = new DataOutputStream(connection.getOutputStream());

				out = new PrintWriter(connection.getOutputStream());
				for (String key : params.keySet()) {
					param.append(key).append("=").append(URLEncoder.encode(params.get(key), "UTF-8")).append("&");
				}
				if (param.length() > 0) {
					param.deleteCharAt(param.length() - 1);
				}
				out.print(param.toString());
				out.flush();
			}
			if (connection.getResponseCode() == 200) {
				InputStream is = connection.getInputStream();
				InputStreamReader isr = new InputStreamReader(is, "utf-8");
				BufferedReader br = new BufferedReader(isr);
				result = br.readLine();
				is.close();
			} else {
				result = String.valueOf(connection.getResponseCode());
			}
		} finally {// close input steam
			try {
				if (dos != null) {
					dos.close();
				}
				if (dataList.isEmpty()) {
					for (Object fis : dataList) {
						if (fis instanceof InputStream) {
							((InputStream) fis).close();
						}
					}
				}
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return result;
	}

}