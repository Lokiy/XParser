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
package com.lokiy.x.simple;

import android.view.View;

import com.lokiy.x.inject.content.InjectHolder;
import com.lokiy.x.inject.content.XParserCallBack;

/**
 * @author Luki
 */
public class SimpleParserCallBack implements XParserCallBack {

	public void OnFailedInjectView(String key, View v) {

	}

	public void onBindInjectHolder(InjectHolder holder) {

	}

	public void onIgnoreView(String key, View v) {

	}

}
