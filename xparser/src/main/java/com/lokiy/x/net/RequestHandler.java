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

import com.lokiy.x.task.TaskConfig;

import java.util.List;
import java.util.Map;

/**
 * which does post/get method and return the response string.
 *
 * @author Luki
 *
 */
public interface RequestHandler {

	/**
	 * POST 
	 * @param url url
	 * @param params request params
	 * @param headers headers
	 *
	 * @return respond String
	 * @throws Exception
	 */
	String post(String url, Map<String, String> params, Map<String, String> headers, List<Object> dataList) throws Exception;

	/**
	 * GET 
	 *
	 * @param url url
	 * @param headers headers
	 * @return respond String
	 */
	String get(String url, Map<String, String> headers) throws Exception;

	/**
	 *
	 */
	void setHttpConfig(TaskConfig config);
}
