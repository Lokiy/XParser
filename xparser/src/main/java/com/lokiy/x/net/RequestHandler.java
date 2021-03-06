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

import com.lokiy.x.XConfig;
import com.lokiy.x.XParser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * which does post/get method and return the response string.
 *
 * @author Luki
 */
public interface RequestHandler {

	/**
	 * POST
	 *
	 * @param url    url
	 * @param params request params
	 * @return respond String
	 *
	 * @throws Exception
	 */
	String post(String url, RequestParams params) throws Exception;

	/**
	 * GET
	 *
	 * @param url    url
	 * @param params request params
	 * @return respond String
	 *
	 * @throws Exception
	 */
	String get(String url, RequestParams params) throws Exception;


	class RequestParams {
		public int retryTimes = 2;
		public int timeOut = 15 * 1000;
		private Map<String, String> params;
		private Map<String, String> headers;
		protected RequestParams(Map<String, String> headers, Map<String, String> params) {
			XConfig config = XParser.INSTANCE.getXConfig();
			if (headers == null)
				headers = new HashMap<>();
			if (params == null)
				params = new HashMap<>();
			if (config.requestHeaders != null)
				headers.putAll(config.requestHeaders);
			if (config.requestExtras != null)
				params.putAll(config.requestExtras);
			this.params = params;
			this.headers = headers;
		}

		public static RequestParams createRequestParams(Map<String, String> headers, Map<String, String> params, List<Object> dataList) {
			if (dataList == null) {
				return new RequestParams(headers, params);
			} else {
				return new DataRequestParams(headers, params, dataList);
			}
		}

		public RequestParams setRetryTimes(int retryTimes) {
			this.retryTimes = retryTimes;
			return this;
		}

		public RequestParams setTimeOut(int timeOut) {
			this.timeOut = timeOut;
			return this;
		}

		public Map<String, String> getParams() {
			return params;
		}

		public Map<String, String> getHeaders() {
			return headers;
		}

	}

	class DataRequestParams extends RequestParams {
		private List<Object> dataList;

		protected DataRequestParams(Map<String, String> headers, Map<String, String> params, List<Object> dataList) {
			super(headers, params);
			this.dataList = dataList;
		}

		public List<Object> getDataList() {
			return dataList;
		}
	}

}
