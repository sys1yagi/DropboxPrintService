/*
 * Copyright (C) 2013 Toshihiro Yagi. https://github.com/sys1yagi
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
package jp.mydns.sys1yagi.android.dropboxprintservice;

import java.io.InputStream;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;

/**
 * @author yagitoshihiro
 * 
 */
public class DropboxAPISession {

    private final static String PREF_NAME = "dropbox.settings";
    private final static String KEY_ACCESS_TOKEN = "token";
    private final static String KEY_ACCESS_TOKEN_SECRET = "secret";

    private final static AccessType ACCESS_TYPE = AccessType.APP_FOLDER;

    private static DropboxAPISession INSTANCE = null;

    private SharedPreferences mPreference;
    private DropboxAPI<AndroidAuthSession> mDBApi;
    private AccessTokenPair mTokens;

    public static DropboxAPISession getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new DropboxAPISession(context);
        }
        return INSTANCE;
    }

    private DropboxAPISession(Context context) {

        mPreference = context.getSharedPreferences(PREF_NAME,
                Context.MODE_PRIVATE);
        String token = mPreference.getString(KEY_ACCESS_TOKEN, null);
        String secret = mPreference.getString(KEY_ACCESS_TOKEN_SECRET, null);
        if (token != null && secret != null) {
            AppKeyPair appKeys = new AppKeyPair(Settings.API_KEY,
                    Settings.API_SECRET);
            AndroidAuthSession session = new AndroidAuthSession(appKeys,
                    ACCESS_TYPE);
            mDBApi = new DropboxAPI<AndroidAuthSession>(session);
            mTokens = new AccessTokenPair(token, secret);
            mDBApi.getSession().setAccessTokenPair(mTokens);
        }

    }

    public void startAuth(Activity activity) {
        AppKeyPair appKeys = new AppKeyPair(Settings.API_KEY,
                Settings.API_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeys,
                ACCESS_TYPE);
        mDBApi = new DropboxAPI<AndroidAuthSession>(session);
        mDBApi.getSession().startAuthentication(activity);
    }

    public boolean endAuth() {
        if (mDBApi != null && mDBApi.getSession().authenticationSuccessful()) {
            try {
                mDBApi.getSession().finishAuthentication();
                mTokens = mDBApi.getSession().getAccessTokenPair();
                mPreference.edit().putString(KEY_ACCESS_TOKEN, mTokens.key)
                        .putString(KEY_ACCESS_TOKEN_SECRET, mTokens.secret)
                        .apply();
                return true;
            } catch (IllegalStateException e) {
                Log.i("DbAuthLog", "Error authenticating", e);
            }
        }
        return false;
    }

    public boolean isAuthNow() {
        return mDBApi != null && !isAuthed();
    }

    public boolean isAuthed() {
        return mTokens != null;
    }

    public AccessTokenPair getTokens() {
        return mTokens;
    }

    public Entry putFile(String name, InputStream in, int size) {
        try {
            return mDBApi.putFile(name, in, size, null, null);
        } catch (DropboxException e) {
            e.printStackTrace();
        }
        return null;
    }
}
