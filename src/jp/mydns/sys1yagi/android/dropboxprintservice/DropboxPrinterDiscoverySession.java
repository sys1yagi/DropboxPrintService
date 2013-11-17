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

import java.util.ArrayList;
import java.util.List;

import android.print.PrintAttributes;
import android.print.PrinterCapabilitiesInfo;
import android.print.PrinterId;
import android.print.PrinterInfo;
import android.printservice.PrintService;
import android.printservice.PrinterDiscoverySession;
import android.util.Log;

/**
 * @author yagitoshihiro
 * 
 */
public class DropboxPrinterDiscoverySession extends PrinterDiscoverySession {

    private final static String TAG = DropboxPrinterDiscoverySession.class
            .getSimpleName();
    private final static String PRINTER_ID = "dropbox.print.service";

    private PrintService mPrintService;
    
    public DropboxPrinterDiscoverySession(PrintService printServie){
        mPrintService = printServie;
    }
    
    @Override
    public void onStartPrinterDiscovery(List<PrinterId> printers) {
        Log.d(TAG, "onStartPrinterDiscovery()");
        for (PrinterId id : printers) {
            Log.d(TAG, "printerId:" + id.getLocalId());
        }
        List<PrinterInfo> addPrinters = new ArrayList<PrinterInfo>();
        PrinterId printerId = mPrintService.generatePrinterId(PRINTER_ID);
        
        PrinterInfo.Builder builder = new PrinterInfo.Builder(printerId,
                "Dropbox Printer", PrinterInfo.STATUS_IDLE);
        
        PrinterCapabilitiesInfo.Builder capBuilder =
          new PrinterCapabilitiesInfo.Builder(printerId);
        capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_A4, true);
        capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_B5, false);
        capBuilder.addResolution(new PrintAttributes.Resolution(
                "Default", "default resolution", 600, 600), true);
        capBuilder.setColorModes(PrintAttributes.COLOR_MODE_COLOR
                | PrintAttributes.COLOR_MODE_MONOCHROME,
                PrintAttributes.COLOR_MODE_COLOR);
        
        builder.setCapabilities(capBuilder.build());
        addPrinters.add(builder.build());
        addPrinters(addPrinters);
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
        Log.d(TAG,
                "onStartPrinterStateTracking(printerId: "
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
}
