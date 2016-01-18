/**
 * Copyright (C) 2015 Luki(liulongke@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 　　　　http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lokiy.x.sample.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

import java.util.Random;

import com.lokiy.x.inject.content.InjectHolder;
import com.lokiy.x.sample.R;
import com.lokiy.x.simple.SimpleAdapter;

/**
 * SimpleListAdapter
 * Created by Luki on 2015/11/16.
 * Version:1
 */
public class SimpleListAdapter extends SimpleAdapter<String> {
	private int mColumnCount = 5;
	private Snackbar make;

	public SimpleListAdapter(Context context) {
		super(context);
	}

	@Override
	public int getColumnCount() {
		return mColumnCount;
	}

	public void setColumnCount(int columnCount) {
		if (columnCount <= 2) {
			columnCount = 2;
		}
		this.mColumnCount = columnCount;
		notifyDataSetChanged();
	}

	@Override
	protected int getItemHeight(int w) {
		return w;
	}

	@Override
	protected int defaultLayoutResId() {
		return R.layout.x_list_item;
	}

	@Override
	public void configViews(InjectHolder holder, final int position) {
		TextView v = holder.findViewByString(R.string.v_this);
		Random r = new Random();
		int red = r.nextInt(255);
		int green = r.nextInt(255);
		int blue = r.nextInt(255);
		v.setBackgroundColor(Color.argb(255, red, green, blue));
		v.setTextColor(Color.argb(255, 255 - red, 255 - green, 255 - blue));
	}

	public void onClick(View v, int position) {
		Snackbar.make(v, position + "", Snackbar.LENGTH_SHORT).show();
	}
}
