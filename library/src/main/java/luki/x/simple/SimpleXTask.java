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
package luki.x.simple;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Map;

import luki.x.XParser;
import luki.x.XTask;
import luki.x.task.AsyncResult;
import luki.x.task.AsyncResult.LoadFrom;
import luki.x.task.AsyncResult.ResultStatus;
import luki.x.task.TaskCallBack;
import luki.x.task.TaskParams;
import luki.x.task.TaskStatusListener;
import android.util.Log;

/**
 * Universal asynchronous task paradigm
 * 
 * @author Luki
 */
@SuppressWarnings("unchecked")
public class SimpleXTask<T extends Serializable> implements TaskStatusListener {
	private boolean isTasking;
	protected String TAG;
	private XTask<T> mCurrentTask;

	public SimpleXTask() {
		TAG = getClass().getSimpleName();
	}

	/**
	 * cancel task
	 */
	public synchronized void cancel() {
		mCurrentTask.cancel(true);
		mCurrentTask.getListener().onCancel();
	}

	/**
	 *  composed with itself params for request.
	 * 
	 * @param params params
	 */
	public void task(TaskParams<T> params) {
		if (!isTasking()) {
			isTasking = true;
			mCurrentTask = XParser.INSTANCE.getXTask(this);
			mCurrentTask.execute(params);
		} else
			Log.v(TAG, "tasking");
	}

	/**
	 * <LI>默认POST</LI><BR>
	 * <LI>加载缓存</LI><BR>
	 * <LI>用JSON解析</LI><BR>
	 * <LI>解析类似为List则解析结果=result.list 否则=result.t ps: 若不解析则解析结果=result.msg</LI> <BR>
	 * 
	 * @param url 路径
	 * @param map 参数
	 * @param type 解析类型
	 * @param listener 回调
	 */
	public void task(String url, Map<String, String> map, Type type, SimpleTaskBack<T> listener) {
		TaskParams<T> params = new TaskParams.Builder<T>(url).listener(listener).params(map).type(type).build();
		task(params);
	}

	public synchronized boolean isTasking() {
		return isTasking;
	}

	public void onEnd() {
		isTasking = false;
	}

	public static abstract class SimpleTaskBack<T> implements TaskCallBack<AsyncResult<T>> {

		public void onResult(AsyncResult<T> result) {
			if (result.status == ResultStatus.SUCCESS) {
				onSuccess(result, result.loadedFrom);
			} else
				onFailed(result, result.loadedFrom);
		}

		public void onCancel() {
		}

		public abstract void onSuccess(AsyncResult<T> result, LoadFrom loadFrom);

		public void onFailed(AsyncResult<T> result, LoadFrom loadFrom) {

		}
	}

	public void onStart() {

	}

	public void onCancel() {

	}
}
