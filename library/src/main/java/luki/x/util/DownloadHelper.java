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
package luki.x.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;

import luki.x.XConfig;
import luki.x.util.NetStatusUtils.NetType;

@SuppressWarnings("unused")
public class DownloadHelper {

	public static final String EXTRA_MAX_SIZE = "maxSize";
	public static final String EXTRA_CURRENT = "current";
	public static final String ACTION_FINISH = XConfig.sContext.getPackageName() + ".luki.broadcast.download_file.helper.finish";
	public static final String ACTION_CHANGE = XConfig.sContext.getPackageName() + ".luki.broadcast.download_file.helper.change";

	private static final String TAG = DownloadHelper.class.getSimpleName();
	private static Handler handler = new MyHandler();
	private BroadcastReceiver receiver = new NetBroadcastReceiver();
	private Thread downloadThread;

	private OnDownloadProgressUpgradeListener listener;
	private DownloadBean downloadBean;
	private boolean isInterrupt;
	private Context context;

	public DownloadHelper(Context context, DownloadBean bean) {
		this.context = context;
		this.downloadBean = bean;
		check();
	}

	/**
	 *
	 */
	private void check() {
		if (downloadBean == null || !downloadBean.validate()) {
			throw new IllegalArgumentException("DownloadHelper.downloadBean or it's field can't be null");
		}
	}

	public synchronized void pause() {
		this.isInterrupt = true;
	}

	public synchronized void start() {
		this.isInterrupt = false;
		if (downloadThread == null) {
			downloadThread = new DownloadThread();
		}
		// 下载路径
		if (!downloadThread.isAlive())
			downloadThread.start();
	}

	/**
	 * 断点续传下载<BR>
	 */
	public void setOnDownloadProgressUpgradeListener(OnDownloadProgressUpgradeListener listener) {
		this.listener = listener;
	}

	public interface OnDownloadProgressUpgradeListener {
		void onDownloadProgressUpgrade(long current, long maxSize);
	}

	public static class DownloadBean {
		public String downloadUrl;
		public String filePath;
		public String fileName;

		boolean validate() {
			return !(TextUtils.isEmpty(downloadUrl) || TextUtils.isEmpty(filePath) || TextUtils.isEmpty(fileName));
		}
	}

	private static class MyHandler extends Handler {

		public void handleMessage(Message msg) {
			DownloadHelper task = (DownloadHelper) msg.obj;
			long current = msg.getData().getLong(EXTRA_CURRENT);
			long maxSize = msg.getData().getLong(EXTRA_MAX_SIZE);
			if (current > maxSize) {
				current = maxSize;
			}
			if (task.listener != null) {
				if (current == maxSize) {
					Intent intent = new Intent();
					intent.setAction(ACTION_FINISH);
					task.context.sendBroadcast(intent);
				}
				task.listener.onDownloadProgressUpgrade(current, maxSize);
			}
			Intent intent = new Intent();
			intent.setAction(ACTION_CHANGE);
			intent.putExtra(EXTRA_CURRENT, current);
			intent.putExtra(EXTRA_MAX_SIZE, maxSize);
			task.context.sendBroadcast(intent);
		}

	}

	private class DownloadThread extends Thread {

		@SuppressWarnings("deprecation")
		public void run() {

			HttpURLConnection httpURLConnection;
			BufferedInputStream bis;
			// 下载文件
			try {
				byte[] buf = new byte[10240];
				long remoteFileSize = getRemoteFileSize(downloadBean.downloadUrl);

				// 获取文件对象，开始往文件里面写内容
				File file = new File(downloadBean.filePath + "/" + downloadBean.fileName);
				if (remoteFileSize != 0 && file.exists() && file.length() >= remoteFileSize) {
					sendMessage(remoteFileSize, remoteFileSize);
					return;
				}
				File tempFile = new File(downloadBean.filePath + "/" + downloadBean.fileName + ".tmp");
				long tempFileSize = tempFile.length();
				// 验证下载文件的完整性
				if (remoteFileSize != 0 && tempFileSize >= remoteFileSize) {
					if (tempFile.renameTo(file)) {
						sendMessage(tempFileSize, remoteFileSize);
					}
					return;
				}
				File parentFile = tempFile.getParentFile();
				if ((parentFile.mkdirs() || parentFile.isDirectory()) && !tempFile.exists()) {
					//noinspection ResultOfMethodCallIgnored
					tempFile.createNewFile();
				}

				URL url = new URL(downloadBean.downloadUrl);
				httpURLConnection = (HttpURLConnection) url.openConnection();

				// 设置User-Agent
				httpURLConnection.setRequestProperty("User-Agent", "Net");
				// 设置续传开始
				httpURLConnection.setRequestProperty("Range", "bytes=" + tempFileSize + "-");
				// 获取输入流
				bis = new BufferedInputStream(httpURLConnection.getInputStream());

				// 检查本地文件
				RandomAccessFile rndFile = new RandomAccessFile(downloadBean.filePath + "/" + downloadBean.fileName + ".tmp", "rw");
				rndFile.seek(tempFileSize);
				int i = 0;
				int size;
				long downloadFileSize = 0;
				while (!isInterrupt && (size = bis.read(buf)) != -1) {
					//if (i > 500) break;
					rndFile.write(buf, 0, size);
					i++;
					downloadFileSize += size;
					if (i >= 2 && downloadFileSize + tempFileSize != remoteFileSize) {
						i = 0;
						sendMessage(downloadFileSize + tempFileSize, remoteFileSize);
					}
				}
				httpURLConnection.disconnect();
				if (downloadFileSize + tempFileSize >= remoteFileSize) {
					sendMessage(remoteFileSize, remoteFileSize);
					if (tempFile.renameTo(file)) {
						//noinspection ResultOfMethodCallIgnored
						tempFile.delete();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				if (e instanceof UnknownHostException || e instanceof ConnectException || e instanceof SocketException) {
					context.registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
				}
			}
			downloadThread = null;
		}

		private long getRemoteFileSize(String url) {
			long size = 0;
			try {
				HttpURLConnection httpUrl = (HttpURLConnection) (new URL(url)).openConnection();
				size = httpUrl.getContentLength();
				httpUrl.disconnect();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return size;
		}


		private void sendMessage(long current, long fileSize) {
			Message msg = handler.obtainMessage(0, DownloadHelper.this);
			Bundle data = msg.getData();
			data.putLong(EXTRA_CURRENT, current);
			data.putLong(EXTRA_MAX_SIZE, fileSize);
			msg.sendToTarget();
		}
	}

	private class NetBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context c, Intent intent) {
			final NetType type = NetStatusUtils.getNetworkType();
			switch (type) {
				case NONE: // 无网络
					break;
				case WIFI:// wifi
					try {
						context.unregisterReceiver(this);
					} catch (Exception ignored) {
					}
					start();
					break;
				default:// 手机网络
					break;
			}
		}

	}


}
