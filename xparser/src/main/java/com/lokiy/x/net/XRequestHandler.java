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
package com.lokiy.x.net;

import android.text.TextUtils;

import com.lokiy.x.XConfig;
import com.lokiy.x.XLog;
import com.lokiy.x.XParser;

import java.io.IOException;
import java.util.HashMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

/**
 * XRequestHandler
 */
public class XRequestHandler implements RequestHandler {
	private static final String TAG = XRequestHandler.class.getSimpleName();

	public XRequestHandler() {
		HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		});
	}

	public String post(String url, RequestParams requestParams) throws Exception {
		if (TextUtils.isEmpty(url)) {
			XLog.i(TAG, "POST-URL is Null");
			return "";
		} else
			XLog.v(TAG, url);
		String result = null;
		int time = 0;
		do {
			try {
				result = onPost(url, requestParams);
				time = 3;
			} catch (Exception e) {
				time++;
				XLog.w(TAG, "times:%d, %s", time, e.toString());
				if (time < requestParams.retryTimes) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException ignored) {
					}
					continue;
				}
				throw e;
			}
		} while (time < requestParams.retryTimes);
		return result;
	}

	protected String onPost(String url, RequestParams requestParams) throws IOException {
		return HttpRequest.sendPost(url, requestParams);
	}

	public String get(String url, RequestParams requestParams) throws Exception {
		if (TextUtils.isEmpty(url)) {
			XLog.i(TAG, "GET-URL is Null");
			return "";
		} else
			XLog.v(TAG, url);
		String result = null;
		int time = 0;
		do {
			try {
				result = onGet(url, requestParams);
				time = 3;
			} catch (Exception e) {
				time++;
				if (time < requestParams.retryTimes) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException ignored) {
					}
					continue;
				}
				throw e;
			}
		} while (time < requestParams.retryTimes);
		return result;
	}

	protected String onGet(String url, RequestParams requestParams) throws IOException {
		return HttpRequest.sendGet(url, requestParams);
	}
}
