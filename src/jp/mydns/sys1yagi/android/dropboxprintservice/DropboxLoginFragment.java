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

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class DropboxLoginFragment extends Fragment {

    public static DropboxLoginFragment newInstance() {
        return new DropboxLoginFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, null, false);

        if (DropboxAPISession.getInstance(getActivity()).isAuthed()) {
            alreadyLogin(view);
        } else {
            view.findViewById(R.id.button1).setOnClickListener(
                    new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DropboxAPISession.getInstance(getActivity()).startAuth(
                                    getActivity());
                        }
                    });
        }
        return view;
    }

    private void alreadyLogin(View view) {
        ((TextView) view.findViewById(R.id.textView1)).setText("ログイン済です");
        view.findViewById(R.id.button1).setEnabled(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (DropboxAPISession.getInstance(getActivity()).isAuthNow()) {
            if (DropboxAPISession.getInstance(getActivity()).endAuth()) {
                alreadyLogin(getView());
            }
        }
    }
}
