/**
 * Copyright (C) 2014 Luki(liulongke@gmail.com)
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
package luki.x.net;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import luki.x.base.INetEngine;
import luki.x.base.XLog;
import luki.x.task.TaskConfig;
/*
@SuppressWarnings("deprecation")
public class XNetEngine implements INetEngine {
	private static final String TAG = XNetEngine.class.getSimpleName();
	private static HttpParams httpParams = new BasicHttpParams();
	private static int DEFAULT_SOCKET_TIMEOUT = 15000;
	private static HttpClient httpClient;
	private int RETRY_TIMES = 2;

	public XNetEngine() {
		this.setDefaultHostnameVerifier();
	}

	private void setDefaultHostnameVerifier() {
		HostnameVerifier hv = new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};
		HttpsURLConnection.setDefaultHostnameVerifier(hv);
	}

	public String post(String url, Map<String, String> params, Map<String, String> headers) throws Exception {
		HttpPost httpPost = new HttpPost(url);
		ArrayList<NameValuePair> p = new ArrayList<>();
		Iterator l = params.keySet().iterator();

		String key;
		while (l.hasNext()) {
			key = (String) l.next();
			p.add(new BasicNameValuePair(key, params.get(key)));
		}

		httpPost.setEntity(new UrlEncodedFormEntity(p, "UTF-8"));
		int length = headers != null && headers.size() > 0 ? headers.size() : 0;
		Iterator i;
		if (length > 0) {
			ArrayList<Header> list = new ArrayList<>();
			Header[] temp = new Header[length];
			i = headers.keySet().iterator();

			while (i.hasNext()) {
				String time = (String) i.next();
				list.add(new BasicHeader(time, headers.get(time)));
			}

			temp = list.toArray(temp);
			httpPost.setHeaders(temp);
		}

		String result = null;
		int times = 0;

		do {
			try {
				HttpClient httpClient = this.getHttpClient();
				HttpResponse httpResponse = httpClient.execute(httpPost);
				result = EntityUtils.toString(httpResponse.getEntity());
				times = 3;
			} catch (Exception e) {
				++times;
				XLog.w(TAG, "times:%d, %s", times, e.toString());
				if (times >= this.RETRY_TIMES) {
					throw e;
				}

				try {
					Thread.sleep(1000L);
				} catch (InterruptedException ignored) {
				}
			} finally {
				if (times >= this.RETRY_TIMES) {
					try {
						httpPost.abort();
					} catch (Exception ignored) {
					}
				}

			}
		} while (times < this.RETRY_TIMES);

		return result;
	}

	public String get(String url, Map<String, String> headers) throws Exception {
		if (TextUtils.isEmpty(url)) {
			XLog.i(TAG, "GET-URL is Null");
			return "";
		} else {
			XLog.v(TAG, url);
			HttpGet httpGet = new HttpGet(url);
			int l = headers != null && headers.size() > 0 ? headers.size() : 0;
			Header[] tem;
			if (l > 0) {
				ArrayList<Header> arrayList = new ArrayList<>();
				tem = new Header[l];

				for (String key : headers.keySet()) {
					arrayList.add(new BasicHeader(key, headers.get(key)));
				}

				tem = arrayList.toArray(tem);
				httpGet.setHeaders(tem);
			}

			String result = null;
			int times = 0;

			do {
				try {
					HttpClient httpClient = this.getHttpClient();
					InputStream is = this.getHttpInputStream(httpClient, httpGet);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					byte[] buff = new byte[1024];

					int length;
					while ((length = is.read(buff)) != -1) {
						baos.write(buff, 0, length);
					}

					is.close();
					byte[] contentByteArray = baos.toByteArray();
					baos.close();
					result = new String(contentByteArray);
					times = 3;
				} catch (Exception e) {
					++times;
					if (times >= this.RETRY_TIMES) {
						throw e;
					}

					try {
						Thread.sleep(1000L);
					} catch (InterruptedException ignored) {
					}
				} finally {
					if (times >= this.RETRY_TIMES) {
						try {
							httpGet.abort();
						} catch (Exception ignored) {
						}
					}

				}
			} while (times < this.RETRY_TIMES);

			return result;
		}
	}

	public void setHttpConfig(TaskConfig config) {
		DEFAULT_SOCKET_TIMEOUT = config.timeOut;
		this.RETRY_TIMES = config.retryTimes;
	}

	private InputStream getHttpInputStream(HttpClient httpClient, HttpGet httpGet) throws Exception {
		HttpResponse httpResponse = httpClient.execute(httpGet);
		int statusCode = httpResponse.getStatusLine().getStatusCode();
		if (statusCode != 200) {
			throw new NetworkErrorException("statusCode != HttpStatus.SC_OK");
		} else {
			return httpResponse.getEntity().getContent();
		}
	}

	private HttpClient getHttpClient() {
		if (httpClient == null) {
			ConnManagerParams.setTimeout(httpParams, 1000L);
			HttpConnectionParams.setConnectionTimeout(httpParams, DEFAULT_SOCKET_TIMEOUT);
			HttpConnectionParams.setSoTimeout(httpParams, DEFAULT_SOCKET_TIMEOUT);
			ConnManagerParams.setMaxConnectionsPerRoute(httpParams, new ConnPerRouteBean(15000));
			ConnManagerParams.setMaxTotalConnections(httpParams, 10);
			HttpProtocolParams.setUseExpectContinue(httpParams, true);
			HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(httpParams, "UTF-8");
			HttpConnectionParams.setTcpNoDelay(httpParams, true);
			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
			ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(httpParams, schemeRegistry);
			httpClient = new DefaultHttpClient(manager, httpParams);
		}

		return httpClient;
	}

	@SuppressWarnings("unused")
	public InputStream getHttpInputStream(String url) throws Exception {
		HttpGet httpGet = new HttpGet(url);
		HttpClient httpClient = this.getHttpClient();
		return this.getHttpInputStream(httpClient, httpGet);
	}
}*/

/**
 * XNetEngine
 */
public class XNetEngine implements INetEngine {
	private static final String TAG = XNetEngine.class.getSimpleName();
	private static int DEFAULT_SOCKET_TIMEOUT = 15 * 1000;
	private int RETRY_TIMES = 2;

	private TaskConfig config;

	public XNetEngine() {
		setDefaultHostnameVerifier();
	}

	private void setDefaultHostnameVerifier() {
		HostnameVerifier hv = new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};
		HttpsURLConnection.setDefaultHostnameVerifier(hv);
	}


	public String post(String url, Map<String, String> params, Map<String, String> headers) throws Exception {
		if (TextUtils.isEmpty(url)) {
			XLog.i(TAG, "POST-URL is Null");
			return "";
		} else
			XLog.v(TAG, url);
		String result = null;
		int time = 0;
		do {
			try {
				if (headers == null)
					headers = new HashMap<>();
				if (config.requestHeaders != null)
					headers.putAll(config.requestHeaders);
				if (params == null)
					params = new HashMap<>();
				if (config.requestExtras != null)
					headers.putAll(config.requestExtras);

				result = HttpRequest.sendPost(url, DEFAULT_SOCKET_TIMEOUT, params, headers);
				time = 3;
			} catch (Exception e) {
				time++;
				XLog.w(TAG, "times:%d, %s", time, e.toString());
				if (time < RETRY_TIMES) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException ignored) {
					}
					continue;
				}
				throw e;
			}
		} while (time < RETRY_TIMES);
		return result;
	}

	public String get(String url, Map<String, String> headers) throws Exception {
		if (TextUtils.isEmpty(url)) {
			XLog.i(TAG, "GET-URL is Null");
			return "";
		} else
			XLog.v(TAG, url);
		String result = null;
		int time = 0;
		do {
			try {
				if (headers == null)
					headers = new HashMap<>();
				if (config.requestHeaders != null)
					headers.putAll(config.requestHeaders);
				if (config.requestExtras != null) {
					if (!url.contains("?")) {
						url += "?";
					}
					for (String key : config.requestExtras.keySet()) {
						url += key + "=" + config.requestExtras.get(key) + "&";
					}
					url = url.substring(0, url.length() - 1);
				}
				result = HttpRequest.sendGet(url, DEFAULT_SOCKET_TIMEOUT, headers);
				time = 3;
			} catch (Exception e) {
				time++;
				if (time < RETRY_TIMES) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException ignored) {
					}
					continue;
				}
				throw e;
			}
		} while (time < RETRY_TIMES);
		return result;
	}

	public void setHttpConfig(TaskConfig config) {
		this.config = config;
		DEFAULT_SOCKET_TIMEOUT = config.timeOut;
		RETRY_TIMES = config.retryTimes;
	}
}
