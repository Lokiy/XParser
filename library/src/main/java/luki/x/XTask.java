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

import java.io.Serializable;

import luki.x.base.AsyncTask;
import luki.x.task.AsyncResult;
import luki.x.task.TaskConfig;
import luki.x.task.TaskEngine;
import luki.x.task.TaskStatusListener;

/**
 * A simple {@link AsyncTask} to do AsyncTask.
 * 
 * @author Luki
 * @version 1.0 2014-1-24
 * @since 1.0
 * @param <T>
 */
public final class XTask<T extends Serializable> extends TaskEngine<T> {

	private TaskStatusListener mCallBack;

	/*public*/XTask(TaskStatusListener callBack, TaskConfig config) {
		this.mCallBack = callBack;
		if (config != null) {
			super.setConfig(config);
		}
	}

	@Override
	protected void onPreExecute() {
		if (mCallBack != null) {
			mCallBack.onStart();
		}
		super.onPreExecute();
	}

	@Override
	protected void onCancelled() {
		if (mCallBack != null) {
			mCallBack.onCancel();
		}
		super.onCancelled();
	}

	@Override
	protected void onPostExecute(AsyncResult<T> result) {
		if (mCallBack != null) {
			mCallBack.onEnd();
		}
		super.onPostExecute(result);
	}
}
