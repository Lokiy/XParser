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
package com.lokiy.x.task;

import com.lokiy.x.XLog;
import com.lokiy.x.XParser;
import com.lokiy.x.XTask;
import com.lokiy.x.db.DBHelper;
import com.lokiy.x.db.DBSelection;
import com.lokiy.x.db.util.DBUtils;
import com.lokiy.x.net.RequestHandler;
import com.lokiy.x.task.AsyncResult.LoadFrom;
import com.lokiy.x.task.AsyncResult.ResultStatus;
import com.lokiy.x.task.base.AsyncTask;
import com.lokiy.x.util.MD5;
import com.lokiy.x.util.NetStatusUtils;

import java.io.Serializable;
import java.util.Map;

/**
 * Task core
 *
 * @param <T>
 * @author Luki
 */
public class TaskHandler<T extends Serializable> extends XTask<T> {

	private static final String CACHE_DATA_DB = "cache_data";
	private TaskParams<T> mParams;

	public TaskHandler(OnTaskStatusListener callBack, TaskConfig config) {
		super(callBack, config);
	}

	@SafeVarargs
	@Override
	protected final AsyncResult<T> doInBackground(TaskParams<T>... taskParams) {
		AsyncResult<T> result = new AsyncResult<>();
		result.params = mParams;
		String httpUrl = mParams.url;

		String generateKey = mParams.generateKey();
		String key = MD5.md5s(generateKey);
		long millis = System.currentTimeMillis();
		try {
			if ((NetStatusUtils.isNetworkConnected() && (mParams.isForceRefresh || isCacheDataFailure(key)))) {
				String resultString = null;
				try {
					RequestHandler.RequestParams requestParams = RequestHandler.RequestParams.createRequestParams(mParams.getHeaders(), mParams.getParams(), mParams.getDataList()).setTimeOut(mParams.timeOut);
					switch (mParams.method) {
						case GET:
							resultString = mConfig.requestHandler.get(httpUrl, requestParams);
							break;
						case POST:
						default:
							resultString = mConfig.requestHandler.post(httpUrl, requestParams);
							break;
					}
					log(resultString, millis);
					parse(result, resultString);
				} catch (Exception e) {
					result.status = ResultStatus.ERROR;
					result.e = e;
					log(e.toString(), millis);
				}
				if (result.status == ResultStatus.SUCCESS && mParams.isAllowLoadCache) {
					if (resultString != null) {
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

	@SafeVarargs
	@Override
	public final AsyncTask<TaskParams<T>, Void, AsyncResult<T>> execute(TaskParams<T>... params) {
		this.mParams = params[0];
		mParams.setTaskConfig(mConfig);
		if (mParams.isParallel) {
			return super.executeOnExecutor(THREAD_POOL_EXECUTOR, params);
		} else
			return super.execute(params);
	}

	/**
	 * Is cache data Failure
	 *
	 * @param key       key
	 * @return true Failure
	 */
	private boolean isCacheDataFailure(String key) {
		boolean isFailure;
		if (!mParams.isAllowLoadCache) {
			return true;
		}
		DBSelection<TaskResult> selection = new DBSelection<>();
		selection.selection = "key=? and " + DBUtils.TIME_COLUMN + ">?";
		selection.selectionArgs = new String[]{
				key,
				String.valueOf(System.currentTimeMillis() - mParams.cacheTime)
		};
		DBHelper create = XParser.INSTANCE.getDBHelper(CACHE_DATA_DB);
		TaskResult results = create.findBySelection(TaskResult.class, selection);
		isFailure = results == null;
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
						obj = mConfig.dataParser.from(json, mParams.type);
					} catch (Exception e) {
						if (mConfig.errorType != null) {
							try {
								obj = mConfig.dataParser.from(json, mConfig.errorType);
							} catch (Exception e1) {
								throw e;
							}
						} else
							throw e;
					}

					result.t = (T) obj;
				}
			} else
				result.status = ResultStatus.FAILED;
		} else
			result.status = ResultStatus.FAILED;
	}

	private void saveObject(Serializable result, String key) {
		DBHelper create = XParser.INSTANCE.getDBHelper(CACHE_DATA_DB);
		TaskResult bean = new TaskResult();
		bean.setKey(key);
		bean.setValue(result.toString());
		create.save(bean);
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
		DBSelection<TaskResult> selection = new DBSelection<>();
		selection.selection = "key=?";
		selection.selectionArgs = new String[]{key};
		DBHelper create = XParser.INSTANCE.getDBHelper(CACHE_DATA_DB);
		TaskResult results = create.findBySelection(TaskResult.class, selection);
		if (results != null) {
			String value = results.getValue();
			parse(result, value);
		}

		result.loadedFrom = LoadFrom.CACHE;
		return result;
	}

	@Override
	protected void onPreExecute() {
	}

	@Override
	protected void onPostExecute(AsyncResult<T> result) {
		if (mParams != null && getListener() != null)
			getListener().onResult(result);
	}

	@Override
	public final OnTaskCallBack<AsyncResult<T>> getListener() {
		return mParams.listener;
	}
}