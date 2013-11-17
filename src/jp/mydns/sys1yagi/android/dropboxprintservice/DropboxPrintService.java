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
import java.util.ArrayList;
import java.util.List;

import com.dropbox.client2.DropboxAPI.Entry;

import android.os.AsyncTask;
import android.os.Handler;
import android.print.PrintAttributes;
import android.print.PrinterCapabilitiesInfo;
import android.print.PrinterId;
import android.print.PrinterInfo;
import android.printservice.PrintJob;
import android.printservice.PrintService;
import android.printservice.PrinterDiscoverySession;
import android.util.Log;
import android.widget.Toast;

/**
 * @author yagitoshihiro
 * 
 */
public class DropboxPrintService extends PrintService {

    private final static String TAG = DropboxPrintService.class.getSimpleName();
    private final static String PRINTER_ID = "dropbox.print.service";

    private Handler mHandler = new Handler();

    @Override
    protected PrinterDiscoverySession onCreatePrinterDiscoverySession() {
        Log.d(TAG, "onCreatePrinterDiscoverySession()");

        return new PrinterDiscoverySession() {
            @Override
            public void onStartPrinterDiscovery(List<PrinterId> priorityList) {
                Log.d(TAG, "onStartPrinterDiscovery()");
                for (PrinterId id : priorityList) {
                    Log.d(TAG, "printerId:" + id.getLocalId());
                }

                final List<PrinterInfo> printers = new ArrayList<PrinterInfo>();
                final PrinterId printerId = generatePrinterId(PRINTER_ID);
                final PrinterInfo.Builder builder = new PrinterInfo.Builder(
                        printerId, "Dropbox Printer", PrinterInfo.STATUS_IDLE);
                PrinterCapabilitiesInfo.Builder capBuilder = new PrinterCapabilitiesInfo.Builder(
                        printerId);
                capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_A4, true);
                capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_B5, false);
                capBuilder.addMediaSize(PrintAttributes.MediaSize.NA_LETTER, false);
                capBuilder.addResolution(new PrintAttributes.Resolution(
                        "Default", "default resolution", 600, 600), true);
                capBuilder.setColorModes(PrintAttributes.COLOR_MODE_COLOR
                        | PrintAttributes.COLOR_MODE_MONOCHROME,
                        PrintAttributes.COLOR_MODE_COLOR);
                builder.setCapabilities(capBuilder.build());
                printers.add(builder.build());
                addPrinters(printers);
            }

            @Override
            public void onStopPrinterDiscovery() {
                Log.d(TAG, "onStopPrinterDiscovery()");
            }

            @Override
            public void onValidatePrinters(List<PrinterId> printerIds) {
                Log.d(TAG, "onValidatePrinters()");
                for (PrinterId id : printerIds) {
                    Log.d(TAG, "printerId:" + id.getLocalId());
                }
            }

            @Override
            public void onStartPrinterStateTracking(PrinterId printerId) {
                Log.d(TAG, "onStartPrinterStateTracking(printerId: "
                        + printerId.getLocalId() + ")");
            }

            @Override
            public void onStopPrinterStateTracking(PrinterId printerId) {
                Log.d(TAG,
                        "onStopPrinterStateTracking(printerId: "
                                + printerId.getLocalId() + ")");
            }

            @Override
            public void onDestroy() {
                Log.d(TAG, "onDestroy()");
            }
        };
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
        printJob.start();

        File tmp = new File(getExternalCacheDir(), printJob.getDocument()
                .getInfo().getName());
        Log.d(TAG, "save tmp file:" + tmp.getAbsolutePath());
        try {
            Log.d(TAG, "copy start");
            FileOutputStream fos = new FileOutputStream(tmp);
            FileInputStream fin = new FileInputStream(printJob.getDocument()
                    .getData().getFileDescriptor());
            int c;
            byte[] bytes = new byte[1024];

            while ((c = fin.read(bytes)) != -1) {
                fos.write(bytes, 0, c);
            }
            fos.close();
            fin.close();
            Log.d(TAG, "copy end");
        } catch (Exception e) {
            e.printStackTrace();
            printJob.fail(e.getMessage());
            return;
        } finally {
            Log.d(TAG, "copy complete");
        }
        AsyncTask<File, Void, PrintStatus> task = new AsyncTask<File, Void, PrintStatus>() {
            @Override
            protected PrintStatus doInBackground(File... params) {
                Log.d(TAG, "send start.");
                PrintStatus status = new PrintStatus();
                File file = params[0];

                DropboxAPISession session = DropboxAPISession
                        .getInstance(getApplicationContext());
                if (session.isAuthed()) {
                    toast("印刷開始");
                    Entry entry;
                    try {
                        entry = session.putFile(file.getName(),
                                new FileInputStream(file), (int) file.length());
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

            protected void onPostExecute(PrintStatus result) {
                if (result.isError()) {
                    printJob.fail(result.getMessage());
                } else {
                    printJob.complete();
                }
                toast(result.getMessage());
            };
        };
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, tmp);
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
