/**
 * Copyright (C) 2016 Luki(liulongke@gmail.com)
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
package com.lokiy.x.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.kince.indicator.widget.IndicatorProgressBar;

import com.lokiy.x.util.DownloadHelper;


/**
 * DownloadHelperFragment
 * Created by Luki on 2016/1/9.
 * Version:1
 */
public class DownloadHelperFragment extends Fragment implements DownloadHelper.OnDownloadProgressUpgradeListener {

	private IndicatorProgressBar mProgress;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getActivity().setTitle(getClass().getSimpleName());

		View view = inflater.inflate(R.layout.fragment_download_helper, container, false);

		mProgress = (IndicatorProgressBar) view.findViewById(R.id.regularprogressbar);
		mProgress.setProgress(0);

		DownloadHelper.DownloadBean bean = new DownloadHelper.DownloadBean();
		bean.downloadUrl = "http://static1.romzj.com/file/shuame/ShuameSetup_3.5.2.187_chrome.exe";
		bean.fileName = "ShuameSetup_3.5.2.187_chrome.exe";
		bean.filePath = "/mnt/sdcard/";
		final DownloadHelper helper = new DownloadHelper(getActivity(), bean);
		helper.setOnDownloadProgressUpgradeListener(this);
		Button start = (Button) view.findViewById(R.id.start);
		start.setOnClickListener(new View.OnClickListener() {
			/**
			 * Called when a view has been clicked.
			 *
			 * @param v The view that was clicked.
			 */
			@Override
			public void onClick(View v) {
				helper.start();
			}
		});
		Button pause = (Button) view.findViewById(R.id.pause);
		pause.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				helper.pause();
			}
		});
		return view;
	}

	@Override
	public void onDownloadProgressUpgrade(long current, long maxSize) {
		mProgress.setProgress((int) (current * 100f / maxSize));
	}

	/**
	 * when download failed, it will be invoked.
	 */
	@Override
	public void onDownloadFailed() {
		Toast.makeText(getActivity(), "下载失败！", Toast.LENGTH_LONG).show();
	}
}
