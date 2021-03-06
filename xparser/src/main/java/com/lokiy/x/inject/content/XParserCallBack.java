/**
 * Copyright (C) 2014 Luki(liulongke@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lokiy.x.inject.content;

import android.view.View;

/**
 * parse callBack
 * 
 * @author Luki
 */
public interface XParserCallBack extends ParserCallBack {

	/**
	 * When the tag is Ignore, it will be invoked.
	 * 
	 * @param key content description of view
	 * @param v view
	 */
	void onIgnoreView(String key, View v);
}