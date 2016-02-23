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
	 * @param url           url
	 * @param requestParams params
	 * @return URL response
	 */
	public static String sendGet(String url, RequestHandler.RequestParams requestParams) throws IOException {
		String result = "";
		BufferedReader in = null;
		try {
			if (!url.contains("?")) {
				url += "?";
			}
			for (String key : requestParams.params.keySet()) {
				url += key + "=" + requestParams.params.get(key) + "&";
			}
			url = url.substring(0, url.length() - 1);

			URL realUrl = new URL(url);
			// open connection
			HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(requestParams.timeOut);
			// set request headers
			connection.setRequestProperty("Connection", "Keep-Alive");
			for (String key : requestParams.headers.keySet()) {
				connection.setRequestProperty(key, requestParams.headers.get(key));
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
	 * @param url           URL
	 * @param requestParams param
	 * @return response
	 *
	 * @throws IOException
	 */
	@SuppressWarnings("ConstantConditions")
	public static String sendPost(String url, RequestHandler.RequestParams requestParams) throws IOException {
		Map<String, String> params = requestParams.params;
		Map<String, String> headers = requestParams.headers;
		List<Object> dataList = requestParams.dataList;
		params = params == null ? new HashMap<String, String>() : params;
		headers = headers == null ? new HashMap<String, String>() : headers;
		dataList = dataList == null ? new ArrayList<>() : dataList;

		String result = "";
		try {
			URL realUrl = new URL(url);
			// open connection
			HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
			connection.setRequestMethod("POST");
			connection.setConnectTimeout(requestParams.timeOut);
			connection.setReadTimeout(requestParams.timeOut);
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setUseCaches(false);
			// set header
			connection.setRequestProperty("Connection", "Keep-Alive");
			for (String key : headers.keySet()) {
				connection.setRequestProperty(key, headers.get(key));
			}
			if (!dataList.isEmpty()) {
				new DataPostHandler(params, dataList, connection).invoke();
			} else {
				new StringPostHandler(params, connection).invoke();
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
				if (dataList.isEmpty()) {
					for (Object fis : dataList) {
						if (fis instanceof InputStream) {
							((InputStream) fis).close();
						}
					}
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return result;
	}

	/**
	 *
	 */
	private static class DataPostHandler {
		private final List<Object> dataList;
		private final Map<String, String> params;
		private final HttpURLConnection connection;
		String boundary = System.currentTimeMillis() + "";
		String end = "\r\n";
		String twoHyphens = "--";
		private DataOutputStream dos;

		public DataPostHandler(Map<String, String> params, List<Object> dataList, HttpURLConnection connection) {
			this.params = params;
			this.dataList = dataList;
			this.connection = connection;
		}

		public DataPostHandler invoke() throws IOException {
			StringBuilder param = new StringBuilder();
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
					param.append(twoHyphens).append(boundary).append(end)
							.append("Content-Type: application/octet-stream").append(end)
							.append("Content-Disposition: form-data; filename=\"file").append(i).append("\"; name=\"file").append(i).append("\"").append(end)
							.append(end);
					byte[] data = new byte[fis.available()];
					if (fis.read(data) != -1) {
						dos.writeBytes(param.toString());
						dos.write(data);
					}
					try {
						fis.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else if (obj instanceof String) {
					dos.write(((String) obj).getBytes());
				}
			}

			if (!params.isEmpty()) {
				param.setLength(0);
				for (String key : params.keySet()) {
					param.append(end)
							.append(twoHyphens).append(boundary).append(end)
							.append("Content-Type: text/plain").append(end)
							.append("Content-Disposition: form-data; name=\"").append(key).append("\"").append(end)
							.append(end).append(URLEncoder.encode(params.get(key), "UTF-8")).append(end)
							.append(twoHyphens).append(boundary).append(twoHyphens);
				}
			}
			dos.writeBytes(param.toString());
			dos.flush();

			try {
				dos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return this;
		}
	}

	private static class StringPostHandler {
		private Map<String, String> params;
		private DataOutputStream dos;
		private PrintWriter out;
		private HttpURLConnection connection;

		public StringPostHandler(Map<String, String> params, HttpURLConnection connection) {
			this.params = params;
			this.connection = connection;
		}

		public StringPostHandler invoke() throws IOException {
			StringBuilder param = new StringBuilder();
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
			try {
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				dos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return this;
		}
	}
}