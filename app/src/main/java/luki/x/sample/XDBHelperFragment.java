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
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Random;

import luki.x.XParser;
import luki.x.sample.bean.Favorite;
import luki.x.sample.bean.User;
import luki.x.sample.bean.UserDetail;

/**
 * XDBHelperFragment
 * Created by Luki on 2015/11/13.
 * Version:1
 */
public class XDBHelperFragment extends Fragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getActivity().setTitle(getClass().getSimpleName());
		return inflater.inflate(R.layout.fragment_x_dbhelper, container, false);
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		User user = dumpData(new Random().nextInt(100));
		XParser.INSTANCE.getDBHelper().save(user);
	}

	private User dumpData(int dx) {
		User user = new User();
		user.birthday = dx + 18;
		user.name = dx + "张三";
		user.phone = dx + "18888888888";
		UserDetail userDetail = new UserDetail();
		userDetail.address = dx + "浙江省杭州市西湖区";
		userDetail.code = 310000 + dx;
		user.detail = userDetail;
		ArrayList<Favorite> favorites = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			Favorite favorite = new Favorite();
			favorite.name = dx + "足球" + (i + 1);
			favorite.type = i + 1;
			favorites.add(favorite);
		}
		user.favoriteList = favorites;
		return user;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.x_dbhelper, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_add) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
