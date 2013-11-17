/**
 * 
 */
package jp.mydns.sys1yagi.android.dropboxprintservice;

import android.printservice.PrintJob;
import android.printservice.PrintService;
import android.printservice.PrinterDiscoverySession;

/**
 * @author yagitoshihiro
 *
 */
public class SamplePrintService extends PrintService {
    @Override
    protected PrinterDiscoverySession onCreatePrinterDiscoverySession() {
        // プリンタの検索セッション用オブジェクトを返す
        return null;
    }

    @Override
    protected void onPrintJobQueued(PrintJob paramPrintJob) {
        // 印刷処理をする
    }

    @Override
    protected void onRequestCancelPrintJob(PrintJob paramPrintJob) {
        // キャンセルリクエスト
    }

}
