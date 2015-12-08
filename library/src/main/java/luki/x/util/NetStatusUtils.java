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
package luki.x.util;

import java.util.Locale;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * 网络工具类
 * 
 * @author Luki
 */
public class NetStatusUtils {

	private static ConnectivityManager connectivityManager;

	/**
	 * 网络类型<BR/>
	 * <B> <LI>NONE</LI><BR/>
	 * <LI>WIFI</LI> <BR/>
	 * <LI>CMWAP</LI><BR/>
	 * <LI>CMNET</LI><BR/>
	 * </B>
	 * 
	 * @author Luki
	 */
	public static enum NetType {
		NONE,
		WIFI,
		CMWAP,
		CMNET
	}

	public static void init(Context context) {
		connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	}

	/**
	 * 获取当前网络类型
	 * 
	 * @return <LI>NONE ：没有网络</LI><BR>
	 *         <LI>WIFI ：WIFI网络</LI><BR>
	 *         <LI>CMWAP：WAP网络</LI><BR>
	 *         <LI>CMNET：NET网络</LI>
	 */
	public static NetType getNetworkType() {
		check();
		NetType netType = NetType.NONE;
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if (networkInfo == null) {
			return netType;
		}
		int nType = networkInfo.getType();
		if (nType == ConnectivityManager.TYPE_MOBILE) {
			String extraInfo = networkInfo.getExtraInfo();
			if (extraInfo != null && !extraInfo.equals("")) {
				if (extraInfo.toLowerCase(Locale.CHINA).equals("cmnet")) {
					netType = NetType.CMNET;
				} else {
					netType = NetType.CMWAP;
				}
			}
		} else if (nType == ConnectivityManager.TYPE_WIFI) {
			netType = NetType.WIFI;
		}
		return netType;
	}

	/**
	 * 检测网络是否可用
	 * 
	 * @return
	 */
	public static boolean isNetworkConnected() {
		check();
		NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
		return ni != null && ni.isConnectedOrConnecting();
	}

	private static void check() {
		if (connectivityManager == null) {
			throw new IllegalArgumentException("pelase invoke NetStatusUtils.init");
		}
	}
}
