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

package luki.x;

import android.content.Context;
import android.view.WindowManager;

import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.Map;

import luki.x.base.IDataParser;
import luki.x.base.INetEngine;
import luki.x.base.IParser;
import luki.x.base.XLog;
import luki.x.inject.content.InjectParser;
import luki.x.util.CacheUtil;
import luki.x.util.NetStatusUtils;

/**
 * Presents configuration for {@link XParser}
 * 
 * @author Luki
 * @see XParser
 * @see luki.x.base.IParser
 * @see luki.x.base.INetEngine
 * @since 1.1.1
 */
@SuppressWarnings("deprecation")
public class XConfig {
	public static final int HOLDER_KEY = R.integer.holder_key;
	public static final int HOLDER_POSITION = R.integer.holder_position;
	public static final int HOLDER_PARSER_KEY = R.integer.holder_parser_key;

	public static int SCREEN_WIDTH;
	public static Context sContext;
	Map<String, String> requestExtras;
	Map<String, String> requestHeaders;
	Type errorType;
	IParser userParser;
	INetEngine netEngine;
	boolean cacheInDB;
	int timeout;
	int times;
	IDataParser dataParser;
	boolean enableDefaultParserLogging;

	/**
	 * Builder for {@link XConfig}
	 *
	 * @author Luki
	 */
	public static class Builder {

		private Map<String, String> requestHeaders;
		private Map<String, String> requestExtras;
		private Context context;
		private boolean writeLogs;
		private Type errorType;
		private boolean cacheInDB = true;
		private IParser userParser;
		private INetEngine netEngine;
		private int timeout = 15 * 1000;
		private int times = 1;
		private IDataParser dataParser;
		private boolean enabledDefaultParserLogging;

		public Builder(Context context) {
			this.context = context.getApplicationContext();
		}

		/**
		 * Enables detail logging of {@link XParser} work. To prevent detail logs don't call this method.
		 * Consider {@link luki.x.base.XLog#disableLogging()} to disable XParser logging completely (even error logs)
		 */
		public Builder writeDebugLogs() {
			this.writeLogs = true;
			return this;
		}

		/** Additional parameters for each request */
		public Builder requestExtras(Map<String, String> extras) {
			this.requestExtras = extras;
			return this;
		}

		/** It' will be used this analysis when parsing failed */
		public Builder errorType(Type errorType) {
			this.errorType = errorType;
			return this;
		}

		/**
		 * True, cache in the inside of the DB but efficiency low. False, cache in the inside of the File but efficiency
		 * high
		 */
		public Builder cacheInDB(boolean cacheInDB) {
			this.cacheInDB = cacheInDB;
			return this;
		}

		/**
		 * request header
		 */
		public Builder requestHeaders(Map<String, String> headers) {
			this.requestHeaders = headers;
			return this;
		}

		/**
		 * Set up your own parser
		 */
		public Builder userParser(InjectParser parser) {
			this.userParser = parser;
			return this;
		}

		/**
		 * Set up your own net engine.
		 */
		public Builder netEngine(INetEngine engine) {
			this.netEngine = engine;
			return this;
		}

		/**
		 * Set up task timeout. Default is 15 sec.
		 */
		public Builder taskTimeout(int timeout) {
			this.timeout = timeout;
			return this;
		}

		/**
		 * Set up task retry times. Default is 2.
		 */
		public Builder taskRetryTimes(int times) {
			this.times = times;
			return this;
		}

		public Builder taskDataParser(IDataParser dataParser) {
			this.dataParser = dataParser;
			return this;
		}
		
		public Builder enabledDefaultParserLogging(boolean b){
			this.enabledDefaultParserLogging = b;
			return this;
		}

		/** Builds configured {@link XConfig} object */
		public XConfig build() {
			check();
			return new XConfig(this);
		}

		/**
		 * 
		 */
		private void check() {
			if (dataParser == null) {
				throw new IllegalArgumentException("DataParser can't be null");
			}
		}

	}

	/**
	 * create default config.
	 * 
	 * @param context context
	 * @return XConfig
	 */
	public static XConfig createDefaultConfig(Context context) {
		return new Builder(context).taskDataParser(new IDataParser() {
			private Gson gson = new Gson();
			@Override
			public Object from(String result, Type clazz) throws Exception {
				return gson.fromJson(result, clazz);
			}
		}).build();
	}

	private XConfig(final Builder builder) {
		sContext = builder.context;
		boolean writeLogs = builder.writeLogs;
		this.requestExtras = builder.requestExtras;
		this.errorType = builder.errorType;
		this.cacheInDB = builder.cacheInDB;
		this.requestHeaders = builder.requestHeaders;
		this.userParser = builder.userParser;
		this.netEngine = builder.netEngine;
		this.timeout = builder.timeout;
		this.times = builder.times;
		this.dataParser = builder.dataParser;
		this.enableDefaultParserLogging = builder.enabledDefaultParserLogging;
		
		if (writeLogs) {
			XLog.enableLogging();
		} else
			XLog.disableLogging();
		if (enableDefaultParserLogging) {
			XLog.enableDefaultParserLogging();
		} else {
			XLog.disableDefaultParserLogging();
		}
		init(sContext);
	}

	@SuppressWarnings("deprecation")
	private void init(Context context) {
		CacheUtil.init(context);
		NetStatusUtils.init(context);
		sContext = context.getApplicationContext();
		WindowManager wm = (WindowManager) sContext.getSystemService(Context.WINDOW_SERVICE);
		SCREEN_WIDTH = wm.getDefaultDisplay().getWidth();
	}
}
