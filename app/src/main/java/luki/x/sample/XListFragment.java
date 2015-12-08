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
package luki.x.sample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ListFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

import luki.x.inject.content.InjectAdapter;
import luki.x.sample.adapter.SimpleListAdapter;
import luki.x.simple.SimpleAdapter;

/**
 * XListFragment
 * Created by Luki on 2015/11/13.
 * Version:1
 */
public class XListFragment extends ListFragment {

	private SimpleListAdapter mAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		List<String> list = dumpData();
		mAdapter = new SimpleListAdapter(getActivity());
		mAdapter.addAll(list);
		setListAdapter(mAdapter);
		setHasOptionsMenu(true);
	}

	@NonNull
	private List<String> dumpData() {
		List<String> list = new ArrayList<>();
		for (int i = 0; i < 50; i++) {
			list.add(String.valueOf(i));
		}
		return list;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.x_list, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		int columnCount = mAdapter.getColumnCount();
		if (id == R.id.action_add) {
			mAdapter = new SimpleListAdapter(getActivity());
			mAdapter.addAll(dumpData());
			mAdapter.setColumnCount(columnCount + 1);
			setListAdapter(mAdapter);
			return true;
		} else if (id == R.id.action_sub) {
			mAdapter = new SimpleListAdapter(getActivity());
			mAdapter.addAll(dumpData());
			mAdapter.setColumnCount(columnCount - 1);
			setListAdapter(mAdapter);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
