package com.example.kaixinyu.scandeviceslocal;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.UiThread;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.example.kaixinyu.scandeviceslocal.Other.ScanActivity;

import java.util.HashSet;
import java.util.Set;

import static com.example.kaixinyu.scandeviceslocal.ScanLocalDevice.MESSAGE_IP;

public class MainActivity extends AppCompatActivity implements ScanLocalDevice.ScanDeviceListener {

    private String TAG = this.getClass().getSimpleName();
    private Set<String> mHostSet = new HashSet<>();
    private TextView tvBtn, tvScanTxt, tvGoTo;
    private EditText etPort;
    private StringBuilder mStrB;
    private String mLocalAddr = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvBtn = findViewById(R.id.tv_sacn_device);
        tvScanTxt = findViewById(R.id.tv_txt);
        tvGoTo = findViewById(R.id.tv_goto);
        etPort = findViewById(R.id.et_port);
        tvGoTo.setOnClickListener(onClickListener);
        tvBtn.setOnClickListener(onClickListener);
        mLocalAddr = ScanLocalDevice.getHostIP();
    }


    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.tv_sacn_device:
                    tvScanTxt.setText("搜索设备...");
                    tvBtn.setEnabled(false);
                    mHostSet.clear();
                    new Thread(){
                        @Override
                        public void run() {
                            super.run();
                            String strPort = etPort.getText().toString();
                            int port = TextUtils.isEmpty(strPort)?5555:Integer.valueOf(strPort);
                            mStrB = new StringBuilder();
                            mStrB.append("端口号： "+port+"\n");
                            ScanLocalDevice.getInstance().startScanDevice(port,MainActivity.this);
                        }
                    }.start();
                    break;
                case R.id.tv_goto:
                    Intent intent = new Intent();
                    intent.setClass(getApplicationContext(), ScanActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    };


    @Override
    public void availableIp(final String ip) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mHostSet.size() == 0)
                    mStrB.append("可连接设备IP：\n\n");

                if(mHostSet.add(ip)) {
                    if(ip.equals(mLocalAddr)){
                        mStrB.append(ip+" (本机)");
                    }else {
                        mStrB.append(ip);
                    }
                    mStrB.append("\n");
                    tvScanTxt.setText(mStrB);
                }

            }
        });

    }

    @Override
    public void scanFinish(){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mHostSet.size() <= 0){
                    mStrB.append("未搜索到可用设备");
                     tvScanTxt.setText(mStrB);
                }else {
                    mStrB.append("搜索结束");
                    tvScanTxt.setText(mStrB);
                }
                tvBtn.setEnabled(true);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        ScanLocalDevice.getInstance().stopScan();
    }

}
