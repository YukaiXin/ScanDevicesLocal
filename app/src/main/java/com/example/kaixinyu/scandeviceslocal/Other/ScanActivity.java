package com.example.kaixinyu.scandeviceslocal.Other;


import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kaixinyu.scandeviceslocal.Other.support.ext.net.IpScanner;
import com.example.kaixinyu.scandeviceslocal.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;



/**
 * Created by kxyu on 2019/5/26
 */
public class ScanActivity extends Activity implements IpScanner.ScanCallback,View.OnClickListener {

    private String TAG = this.getClass().getSimpleName();

    private ProgressDialog mProgressDialog = null;
    private IpScanner mIpScanner = null;
    private TextView mTitle =null, tvScan;
    private EditText edPort;
    private Handler mUiHandler = new Handler(Looper.getMainLooper());
    private DismissAction mDismissAction = new DismissAction();
    private List<String> mIps = new ArrayList<>(5);
    private String mHostIp;
    private ListView mListView = null;
    private ListAdapter mListAdapter = new ListAdapter();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        mListView = (ListView) this.findViewById(R.id.result);
        mListView.setAdapter(mListAdapter);
        tvScan = findViewById(R.id.tv_scan);
        edPort = findViewById(R.id.et_port);
        tvScan.setOnClickListener(this);
        mTitle = (TextView)this.findViewById(R.id.tv_title);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCanceledOnTouchOutside(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_scan:
                int port = 5555;
                String portS = edPort.getText().toString();
                if(!TextUtils.isEmpty(portS)){
                    port = Integer.valueOf(portS);
                }
                mProgressDialog.show();
                mIpScanner = new IpScanner(port,this)
                        .setScannerLogger(new SimpleLogger());
                mIpScanner.startScan();
                break;
        }
    }


    private class ListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mIps.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            TextView tv = new TextView(ScanActivity.this);
            tv.setText("设备:"+ mIps.get(i));
            tv.setGravity(Gravity.LEFT);
            return tv;
        }
    }

    private class DismissAction implements Runnable {

        @Override
        public void run() {
            mProgressDialog.dismiss();
            mTitle.setText("本机 ip:"+mHostIp+" | device :"+mIps.size());
            if (mIps.size() <= 0) {
                toast("设备没有发现!");
            } else {
                mListAdapter.notifyDataSetChanged();
            }
        }
    }

    private void toast(String msg) {
        Toast.makeText(this,msg,Toast.LENGTH_LONG).show();
    }


    @Override
    public void onFound(Set<String> ip, String hostIp, int port) {
        mIps.addAll(ip);
        mHostIp = hostIp;
        mUiHandler.post(mDismissAction);
    }

    @Override
    public void onNotFound(String hostIp, int port) {
        mIps.clear();
        mHostIp = hostIp;
        Log.v(TAG,"没有发现 "+hostIp);
        mUiHandler.post(mDismissAction);
    }
}

