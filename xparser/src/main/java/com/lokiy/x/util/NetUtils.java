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
package com.lokiy.x.util;

import com.lokiy.x.net.RequestHandler;
import com.lokiy.x.net.XNetEngine;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 关于网络请求数据
 *
 * @author Luki
 */
public enum NetUtils {
	INSTANCE;

	private RequestHandler mNetEngine = new XNetEngine();

	/**
	 * GET 请求数据
	 *
	 * @param url     request url
	 * @param headers header
	 * @return response string
	 */
	public String get(String url, Map<String, String> headers) throws Exception {
		return mNetEngine.get(url, headers);
	}

	/**
	 * POST 请求数据
	 *
	 * @param headers header
	 * @param params  params
	 * @return response string
	 *
	 * @throws IOException
	 */

	public String post(String url, Map<String, String> params, Map<String, String> headers, List<Object> dataList) throws Exception {
		return mNetEngine.post(url, params, headers, dataList);
	}

	public RequestHandler getNetEngine() {
		return mNetEngine;
	}

	public void setNetEngine(RequestHandler engine) {
		mNetEngine = engine;
	}

	public enum Method {
		GET,
		POST
	}
}
