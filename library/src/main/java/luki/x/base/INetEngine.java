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

package luki.x.base;

import java.util.List;
import java.util.Map;

import luki.x.task.TaskConfig;

/**
 *
 *
 * @author Luki
 * @date Oct 13, 2014 2:32:02 PM
 *
 */
public interface INetEngine {

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
