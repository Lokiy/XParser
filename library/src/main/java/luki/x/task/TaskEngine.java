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
package luki.x.task;

import java.io.Serializable;
import java.util.Map;

import luki.x.XParser;
import luki.x.base.AsyncTask;
import luki.x.base.IDBHelper;
import luki.x.base.XLog;
import luki.x.db.DBSelection;
import luki.x.task.AsyncResult.LoadFrom;
import luki.x.task.AsyncResult.ResultStatus;
import luki.x.util.CacheUtil;
import luki.x.util.DBUtils;
import luki.x.util.MD5;
import luki.x.util.NetStatusUtils;
import luki.x.util.NetUtils;

/**
 * 任务核心类
 *
 * @param <T>
 * @author Luki
 */
public class TaskEngine<T extends Serializable> extends AsyncTask<TaskParams<T>, Void, AsyncResult<T>> {

	private static final String CACHE_DATA_DB = "cache_data";
	private static final NetUtils mNetUtils = NetUtils.INSTANCE;
	private static final CacheUtil mCacheUtil = CacheUtil.getIntance();
	private static TaskConfig taskConfig = new TaskConfig();
	private TaskParams<T> mParams;
	private TaskConfig config;

	/**
	 *
	 */
	public TaskEngine() {}

	public static synchronized boolean isInit() {
		return taskConfig == null;
	}

	@SafeVarargs
	@Override
	protected final AsyncResult<T> doInBackground(TaskParams<T>... params) {
		AsyncResult<T> result = new AsyncResult<>();
		result.params = mParams;
		String httpUrl = mParams.url;

		String generateKey = mParams.generateKey();
		String key = MD5.md5s(generateKey);
		long millis = System.currentTimeMillis();
		Map<String, String> requestParams = mParams.getParams();
		try {
			if ((NetStatusUtils.isNetworkConnected() && (mParams.isForceRefresh || isCacheDataFailure(key, mParams.cacheTime)))) {
				String resultString = null;
				try {
					switch (mParams.method) {
						case GET:
							resultString = mNetUtils.get(httpUrl, mParams.getHeaders());
							break;
						case POST:
						default:
							resultString = mNetUtils.post(httpUrl, requestParams, mParams.getHeaders());
								break;
					}
					log(resultString, millis);
					parse(result, resultString);
				} catch (Exception e) {
					result.status = ResultStatus.ERROR;
					result.e = e;
					log(e.toString(), millis);
//					XLog.e(TAG, e.toString());
				}
				if (result.status == ResultStatus.SUCCESS && mParams.isAllowLoadCache) {
					if (!config.cacheInDB) {
						saveObject(result, key);
					} else if (resultString != null) {
						saveObject(resultString, key);
					}
				}
			} else if (mParams.isAllowLoadCache) {
				result = readObject(key);
			}
		} catch (Exception e) {
			log(e.toString(), millis);
			result.e = e;
			XLog.e(mParams.TAG, e.toString());
			if (mParams.isAllowLoadCache) {
				try {
					result = readObject(key);
				} catch (Exception ignored) {
				}
			} else {
				result = new AsyncResult<>();
				result.status = ResultStatus.FAILED;
			}
		}
		result.netType = NetStatusUtils.getNetworkType();
		return result;
	}

	@Override
	protected void onPreExecute() {
//		XLog.start(TAG);
	}

	@Override
	protected void onPostExecute(AsyncResult<T> result) {
		if (mParams != null && getListener() != null)
			getListener().onResult(result);
//		XLog.end(mParams.TAG);
	}

	@SafeVarargs
	@Override
	public final AsyncTask<TaskParams<T>, Void, AsyncResult<T>> execute(TaskParams<T>... params) {
		if (config == null) {
			config = taskConfig;
		}
		this.mParams = params[0];
		mParams.setTaskConfig(config);
		if (mParams.isParallel) {
			return super.executeOnExecutor(THREAD_POOL_EXECUTOR, params);
		} else
			return super.execute(params);
	}

	public final TaskCallBack<AsyncResult<T>> getListener() {
		return mParams.listener;
	}

	/**
	 * Is cache data Failure
	 *
	 * @param key       key
	 * @param cacheTime cache time
	 * @return true Failure
	 */
	private boolean isCacheDataFailure(String key, long cacheTime) {
		boolean isFailure;
		if (!mParams.isAllowLoadCache) {
			return true;
		}
		if (config.cacheInDB) {
			DBSelection<TaskResult> selection = new DBSelection<>();
			selection.selection = "key=? and " + DBUtils.TIME_COLUMN + ">?";
			selection.selectionArgs = new String[]{
					key,
					String.valueOf(System.currentTimeMillis() - mParams.cacheTime)
			};
			IDBHelper create = XParser.INSTANCE.getDBHelper(CACHE_DATA_DB);
			TaskResult results = create.findBySelection(TaskResult.class, selection);
			isFailure = results == null;
		} else {
			isFailure = mCacheUtil.isCacheDataFailure(key, cacheTime);
		}
		return isFailure;
	}

	private void log(String resultString, long millis) {
		if (XLog.isLogging()) {
			StringBuilder sb = new StringBuilder();
			sb.append("\n------------START--------------");
			sb.append("\n");
			sb.append("url=").append(mParams.url);
			sb.append("\n");
			sb.append("Method=").append(mParams.method);
			sb.append("\n");
			Map<String, String> headers = mParams.headers;
			if (headers != null && headers.size() > 0) {
				sb.append("-----------header--------------\n");
				for (String k : headers.keySet()) {
					sb.append("`").append(k).append("`=`").append(headers.get(k)).append("`\n");
				}

			}
			Map<String, String> map = mParams.getParams();
			if (map != null && map.size() > 0) {
				sb.append("-----------params--------------\n");
				for (String str : map.keySet()) {
					sb.append("`").append(str).append("`=`").append(map.get(str)).append("`\n");
				}
			}
			sb.append("result=").append(resultString);
			sb.append("\n");
			sb.append("-------------END---------------");
			sb.append("\n");
			XLog.d(mParams.TAG, sb.toString());
			long coast = System.currentTimeMillis() - millis;
			if (coast < 500) {
				XLog.v(mParams.TAG, "^_^!It's spend %d milliseconds to get data from %s", coast, mParams.url);
			} else if (coast < 1000) {
				XLog.d(mParams.TAG, "^_^!It's spend %d milliseconds to get data from %s", coast, mParams.url);
			} else if (coast < 3000) {
				XLog.i(mParams.TAG, "-_-!It's spend %d milliseconds to get data from %s", coast, mParams.url);
			} else if (coast < 5000) {
				XLog.w(mParams.TAG, "-_-!!It's spend %d milliseconds to get data from %s", coast, mParams.url);
			} else {
				XLog.e(mParams.TAG, "-_-!!!It's spend %d milliseconds to get data from %s", coast, mParams.url);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void parse(AsyncResult<T> result, String json) throws Exception {
		if (json != null) {
			if (!json.equals("") && !json.equals("[]")) {
				result.resultStr = json;
				if (mParams.isParse) {
					Object obj;
					try {
						obj = config.dataParser.from(json, mParams.type);
					} catch (Exception e) {
						if (config.errorType != null) {
							try {
								obj = config.dataParser.from(json, config.errorType);
							} catch (Exception e1) {
								throw e;
							}
						} else
							throw e;
					}

					result.t = (T) obj;
				}
			} else if (json.equals("[]"))
				result.status = ResultStatus.EMPTY;
			else
				result.status = ResultStatus.FAILED;
		} else
			result.status = ResultStatus.FAILED;
	}

	private void saveObject(Serializable result, String key) {
		if (config.cacheInDB) {
			IDBHelper create = XParser.INSTANCE.getDBHelper(CACHE_DATA_DB);
			TaskResult bean = new TaskResult();
			bean.setKey(key);
			bean.setValue(result.toString());
			create.save(bean);
		} else {
			mCacheUtil.saveObject(result, key);
		}
	}

	/**
	 * Read object from Cache or DB
	 *
	 * @param key key
	 * @return AsyncResult
	 *
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private AsyncResult<T> readObject(String key) throws Exception {

		AsyncResult<T> result = new AsyncResult<>();
		if (config.cacheInDB) {
			DBSelection<TaskResult> selection = new DBSelection<>();
			selection.selection = "key=?";
			selection.selectionArgs = new String[]{key};
			IDBHelper create = XParser.INSTANCE.getDBHelper(CACHE_DATA_DB);
			TaskResult results = create.findBySelection(TaskResult.class, selection);
			if (results != null) {
				String value = results.getValue();
				parse(result, value);
			}
		} else {
			result = (AsyncResult<T>) mCacheUtil.readObject(key);
		}

		if (result == null) {
			result = new AsyncResult<>();
			result.status = ResultStatus.FAILED;
		} else {
			result.loadedFrom = LoadFrom.CACHE;
		}
		return result;
	}

	public final void setConfig(TaskConfig config) {
		mNetUtils.getNetEngine().setHttpConfig(config);
		if (config.isDefault) {
			taskConfig = config;
		} else {
			this.config = config;
		}
	}
}