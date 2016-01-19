XParser
=======

XParser 一个能很方便的使用数据库、网络等的工具，主要利用反射构建出来的ORM

Usage
-------
使用方法：

1.在使用的module下的build.gradle里面添加上对应的引用即可

	dependencies {
		compile 'com.lokiy.xparser:xparser:1.2.0'
	}

2.混淆的时候注意添加上

	-keep class com.lokiy.x.inject.content.XAdapter {*;}
	-keep class * implements com.lokiy.x.inject.content.XAdapter{*;}
	-keepclassmembers class * implements java.io.Serializable {
		static final long serialVersionUID;
		private static final java.io.ObjectStreamField[] serialPersistentFields;
		private void writeObject(java.io.ObjectOutputStream);
		private void readObject(java.io.ObjectInputStream);
		java.lang.Object writeReplace();
		java.lang.Object readResolve();
	}


Credits
-------

Author: Luki Liu

The code in this project is licensed under the Apache Software License 2.0.
<br />
Copyright (c) 2011 readyState Software Ltd.