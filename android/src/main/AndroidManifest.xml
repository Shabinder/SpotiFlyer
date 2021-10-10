<?xml version="1.0" encoding="utf-8"?>
<!--
  ~  * Copyright (c)  2021  Shabinder Singh
  ~  * This program is free software: you can redistribute it and/or modify
  ~  * it under the terms of the GNU General Public License as published by
  ~  * the Free Software Foundation, either version 3 of the License, or
  ~  * (at your option) any later version.
  ~  *
  ~  * This program is distributed in the hope that it will be useful,
  ~  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~  * GNU General Public License for more details.
  ~  *
  ~  *  You should have received a copy of the GNU General Public License
  ~  *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
  -->

<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    package="com.shabinder.spotiflyer">

    <queries>
        <package android:name="com.gaana" />
        <package android:name="com.spotify.music" />
        <package android:name="com.jio.media.jiobeats" />
        <package android:name="com.soundcloud.android" />
        <package android:name="com.google.android.youtube" />
        <package android:name="com.google.android.apps.youtube.music" />
    </queries>

    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.QUERY_ALL_PACKAGES" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
        tools:ignore="ScopedStorage" />
    <uses-permission android:name="android.permission.READ_STORAGE_PERMISSION" />
    <uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE"
        tools:ignore="ProtectedPermissions,ScopedStorage" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />

    <application
        android:name=".App"
        android:allowBackup="false"
        android:supportsRtl="true"
        android:usesCleartextTraffic="true"
        android:theme="@style/Theme.AppCompat.Light.NoActionBar"
        android:icon="@mipmap/ic_launcher"
        android:hardwareAccelerated="true"
        android:largeHeap="true"
        android:label="SpotiFlyer"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:configChanges="orientation|screenSize"
        android:forceDarkAllowed="true"
        android:extractNativeLibs="true"
        android:requestLegacyExternalStorage="true"
        tools:targetApi="q">
        <activity
            android:name=".ui.SplashScreenActivity"
            android:theme="@style/SplashTheme"
            android:hardwareAccelerated="true"
            >
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <activity android:name=".MainActivity"
            android:launchMode="singleTask"
            android:hardwareAccelerated="true"
            android:theme="@style/Theme.SpotiFlyer"
            >
            <intent-filter>
                <category android:name="android.intent.category.DEFAULT" />
                <action android:name="android.intent.action.SEND" />
                <data android:mimeType="text/plain" />
            </intent-filter>
        </activity>

        <service android:name=".service.ForegroundService"/>
        <service android:name="org.openudid.OpenUDID_service"
            tools:ignore="ExportedService,IntentFilterExportedReceiver">
            <intent-filter>
                <action android:name="org.openudid.GETUDID" />
            </intent-filter>
        </service>

        <receiver android:name="ly.count.android.sdk.ReferrerReceiver" android:exported="true"
            tools:ignore="ExportedReceiver">
            <intent-filter>
                <action android:name="com.android.vending.INSTALL_REFERRER" />
            </intent-filter>
        </receiver>
    </application>
</manifest>