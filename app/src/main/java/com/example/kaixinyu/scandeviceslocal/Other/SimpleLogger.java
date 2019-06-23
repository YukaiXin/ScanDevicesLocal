package com.example.kaixinyu.scandeviceslocal.Other;

import android.util.Log;

import com.example.kaixinyu.scandeviceslocal.Other.support.ext.net.IpScanner;


/**
 * Created by kxyu on 2019/5/26
 */

public class SimpleLogger implements IpScanner.ScannerLogger {
    private String TAG = SimpleLogger.class.getSimpleName();


    @Override
    public void onScanLogPrint(String msg) {
        Log.v(TAG,">>>"+msg);
    }
}
