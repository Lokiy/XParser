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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lokiy.x.XParser;
import com.lokiy.x.util.InjectUtils;
import com.lokiy.x.util.WidgetUtils;
import com.lokiy.x.util.WidgetUtils.ResType;

import java.util.List;

/**
 * Auto parser the contentDescription link method or set value and so no.<BR>
 * See more<BR>
 * {@link #nameGenerator(int)}<BR>
 * resource name generate rule. {@link #defaultLayoutResId()}<BR>
 * default layout resource id {@link #onFailedInjectView(String, View, int)}<BR>
 * parsed failed view callback {@link #onBindInjectHolder(InjectHolder, int)}<BR>
 * other
 *
 * @author Luki
 * @param <T>
 */
public class InjectAdapter<T> extends BaseAdapter<T> {
	private int mRes;

	public InjectAdapter() {
		this(0);
	}

	public InjectAdapter(int res) {
		this.mRes = res;
	}

	@SuppressWarnings("unused")
	public InjectAdapter(List<? extends T> list) {
		TAG = getClass().getSimpleName();
		addAll(list);
	}

	/**
	 * get item view
	 * @param position position
	 * @param convertView convert view
	 * @param parent parent
	 * @return item view
	 */
	protected View getItemView(final int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			int resId = WidgetUtils.getRes(parent.getContext(), nameGenerator(position), ResType.LAYOUT);
			if (resId == 0) {
				resId = defaultLayoutResId();
			}
			convertView = LayoutInflater.from(parent.getContext()).inflate(resId, parent, false);
		}
		convertView.setTag(XParser.INSTANCE.getXConfig().HOLDER_POSITION, position);

		restoreItem(position, convertView);

		XParser.INSTANCE.parse(this, getItem(position), convertView, new ParserCallBack() {

			@Override
			public void OnFailedInjectView(String key, View v) {
				InjectAdapter.this.onFailedInjectView(key, v, position);
			}

			@Override
			public void onBindInjectHolder(InjectHolder holder) {
				InjectAdapter.this.onBindInjectHolder(holder, position);
			}
		});
		return convertView;
	}

	/**
	 * restore the item.
	 *
	 * @param position position
	 * @param convertView convert view
	 */
	@SuppressWarnings("UnusedParameters")
	protected void restoreItem(int position, View convertView) {

	}

	/**
	 * if the view failed to parse,the method will be invoked.
	 *
	 * @param key ContentDescription
	 * @param v v
	 * @param position position
	 */
	@SuppressWarnings("UnusedParameters")
	public void onFailedInjectView(String key, View v, int position) {}

	/**
	 * Layout item name generator.
	 *
	 * @return item resource id
	 */
	@SuppressWarnings("UnusedParameters")
	protected String nameGenerator(int position) {
		return "";
	}

	/**
	 * Default layout resource id.
	 *
	 * @return default layout resource id of the item
	 */
	protected int defaultLayoutResId() {
		return mRes;
	}

	/**
	 * configuration the contentView which already injected.
	 *
	 * @param holder holder
	 * @param position position
	 */
	public void onBindInjectHolder(InjectHolder holder, int position) {}

	/**
	 * return the value for given key
	 *
	 * @param field field
	 * @param data data
	 * @return object
	 */
	protected Object getValueByField(String field, Object data) {
		return InjectUtils.getValueByTag(field, data);
	}

	/**
	 * return the value for given key
	 *
	 * @param key key
	 * @param data data
	 * @return object
	 */
	@SuppressWarnings("unused")
	protected Object getValueByKey(String key, Object data) {
		ParseHolder holder = InjectUtils.getParserHolder(InjectConfig.INSTANCE.parseClass, key, null);
		return getValueByField(holder.value, data);
	}
}
