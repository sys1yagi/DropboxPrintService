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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.os.AsyncTask;
import android.os.Handler;
import android.printservice.PrintJob;
import android.printservice.PrintService;
import android.printservice.PrinterDiscoverySession;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI.Entry;

/**
 * @author yagitoshihiro
 * 
 */
public class DropboxPrintService extends PrintService {

    private final static String TAG = DropboxPrintService.class.getSimpleName();

    private Handler mHandler = new Handler();

    @Override
    protected PrinterDiscoverySession onCreatePrinterDiscoverySession() {
        Log.d(TAG, "onCreatePrinterDiscoverySession()");
        return new DropboxPrinterDiscoverySession(this);
    }

    private static class PrintStatus {
        private String mMessage;
        private boolean isError;

        public String getMessage() {
            return mMessage;
        }

        public void setMessage(String message) {
            mMessage = message;
        }

        public boolean isError() {
            return isError;
        }

        public void setError(boolean isError) {
            this.isError = isError;
        }

    }

    @Override
    protected void onPrintJobQueued(final PrintJob printJob) {
        Log.d(TAG, "onPrintJobQueued(printer id = "
                + printJob.getInfo().getPrinterId().getLocalId()
                + ", job id = " + printJob.getId());

        AsyncTask<Void, Void, PrintStatus> task = new AsyncTask<Void, Void, PrintStatus>() {
            private boolean isPreExecuteError = false;
            private File mTmp;

            @Override
            protected void onPreExecute() {
                printJob.start();

                mTmp = new File(getExternalCacheDir(), printJob.getDocument()
                        .getInfo().getName());
                Log.d(TAG, "save tmp file:" + mTmp.getAbsolutePath());
                FileOutputStream fos = null;
                FileInputStream fin = null;
                try {
                    Log.d(TAG, "copy start");
                    fos = new FileOutputStream(mTmp);
                    fin = new FileInputStream(printJob.getDocument().getData()
                            .getFileDescriptor());
                    int c;
                    byte[] bytes = new byte[1024];

                    while ((c = fin.read(bytes)) != -1) {
                        fos.write(bytes, 0, c);
                    }
                    Log.d(TAG, "copy end");
                } catch (Exception e) {
                    e.printStackTrace();
                    printJob.fail(e.getMessage());
                    isPreExecuteError = true;
                    mTmp = null;
                    return;
                } finally {
                    Log.d(TAG, "copy complete");
                    if(fos != null){try{fos.close();}catch(Exception e){}}
                    if(fin != null){try{fin.close();}catch(Exception e){}}
                }
            }

            @Override
            protected PrintStatus doInBackground(Void... params) {
                Log.d(TAG, "send start.");
                PrintStatus status = new PrintStatus();
                if (isPreExecuteError) {
                    status.setMessage("保存失敗");
                    status.setError(true);
                }
                DropboxAPISession session = DropboxAPISession
                        .getInstance(getApplicationContext());
                if (session.isAuthed()) {
                    toast("印刷開始");
                    Entry entry;
                    try {
                        entry = session.putFile(mTmp.getName(),
                                new FileInputStream(mTmp), (int) mTmp.length());
                        if (entry != null) {
                            status.setMessage("保存完了:" + entry.fileName());
                            status.setError(false);
                        } else {
                            status.setMessage("保存失敗");
                            status.setError(true);
                        }
                    } catch (FileNotFoundException e) {
                        status.setMessage("保存失敗:" + e.getMessage());
                        status.setError(true);
                        e.printStackTrace();
                    }
                } else {
                    status.setMessage("ログインしてません");
                    status.setError(true);
                }
                return status;
            }
            
            @Override
            protected void onPostExecute(PrintStatus result) {
                if(printJob.isCancelled()) {
                    toast("キャンセルされました");
                } else if (result.isError()) {
                    printJob.fail(result.getMessage());
                } else {
                    printJob.complete();
                }
                toast(result.getMessage());
            };
        };
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    public void toast(final String message) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    @Override
    protected void onRequestCancelPrintJob(PrintJob printJob) {
        Log.d(TAG, "onRequestCancelPrintJob(printer id = "
                + printJob.getInfo().getPrinterId().getLocalId()
                + ", job id = " + printJob.getId());
    }

}
