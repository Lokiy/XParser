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
package luki.x.task;

import java.io.Serializable;

import luki.x.util.NetStatusUtils.NetType;

/**
 * 任务返回封装类
 * 
 * @author Luki
 * @param <T>
 */
public final class AsyncResult<T> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8895005939818095491L;
	public String resultStr;
	public T t;

	public LoadFrom loadedFrom = LoadFrom.NET;
	/** 网络格式 */
	public NetType netType = NetType.NONE;
	/** 保留(暂时不用) */
	public Throwable e;
	/** 标志当前任务状态 成功 失败 错误 空数据 */
	public ResultStatus status = ResultStatus.SUCCESS;
	public TaskParams<T> params;

	@Override
	public String toString() {
		return "AsyncResult [t=" + t + ", resultStr=" + resultStr + ", loadedFrom=" + loadedFrom + ", netType=" + netType + ", e=" + e
				+ ", resultStatus=" + status + "]";
	}

	/**
	 * 请求结果状态
	 * 
	 * @author Luki
	 */
	public static enum ResultStatus {
		/** 请求成功 */
		SUCCESS,
		/** 请求失败 */
		FAILED,
		/** 重复 */
		REPEAT,
		/** 出现错误 */
		ERROR,
		/** 返回结果为空 */
		EMPTY

	}

	public enum LoadFrom {
		NET,
		CACHE
	}
}
