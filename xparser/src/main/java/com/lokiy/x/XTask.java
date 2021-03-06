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

package com.lokiy.x;

import com.lokiy.x.task.AsyncResult;
import com.lokiy.x.task.OnTaskCallBack;
import com.lokiy.x.task.TaskConfig;
import com.lokiy.x.task.TaskParams;
import com.lokiy.x.task.OnTaskStatusListener;
import com.lokiy.x.task.base.AsyncTask;

import java.io.Serializable;

/**
 * A simple {@link AsyncTask} to do AsyncTask.
 *
 * @param <T>
 * @author Luki
 * @version 1.0 2014-1-24
 * @since 1.0
 */
public abstract class XTask<T extends Serializable> extends AsyncTask<TaskParams<T>, Void, AsyncResult<T>> {

	protected TaskConfig mConfig;
	private OnTaskStatusListener mCallBack;

	public XTask(OnTaskStatusListener callBack, TaskConfig config) {
		this.mCallBack = callBack;
		this.mConfig = config;
	}

	@Override
	protected void onPreExecute() {
		if (mCallBack != null) {
			mCallBack.onStart();
		}
		super.onPreExecute();
	}

	@Override
	protected void onPostExecute(AsyncResult<T> result) {
		if (mCallBack != null) {
			mCallBack.onEnd();
		}
		super.onPostExecute(result);
	}

	@Override
	protected void onCancelled() {
		if (mCallBack != null) {
			mCallBack.onCancel();
		}
		super.onCancelled();
	}

	public abstract OnTaskCallBack<AsyncResult<T>> getListener();
}
