package com.example.kaixinyu.scandeviceslocal;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by kxyu on 2019/5/24
 */
public class ScanLocalDevice {

    private static final String TAG = ScanLocalDevice.class.getSimpleName();
    public static final int MESSAGE_IP = 1;
    private static ScanLocalDevice instance = null;
    private String mDevAddress;// 本机IP地址-完整
    private String mLocAddress;// 局域网IP地址头,如：192.168.1.
    private Set<String> canUseSet = new HashSet<>();
    private final int TIME_OUT = 3000;
    private ExecutorService pool;
    private  final int threadAcount = 254;



    public static ScanLocalDevice getInstance(){
      if(instance == null){
          instance = new ScanLocalDevice();
      }
      return instance;
    }


    public ScanLocalDevice(){

        mDevAddress = getHostIP();// 获取本机IP地址
        mLocAddress = getLocAddrIndex(mDevAddress);// 获取本地ip前缀

    }

    public void startScanDevice(Integer port, ScanDeviceListener listener) {
        this.listener = listener;

        // 创建一个线程池，多个线程同步执行
        pool = Executors.newFixedThreadPool(threadAcount);
        for (int i = 1; i < threadAcount; i++) {
            String currnetIp = mLocAddress + i;
            pool.execute(new PingRunner(currnetIp, port));
        }

        pool.shutdown();
        try {
            while (!pool.isTerminated()) {
                Thread.sleep(100);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            Log.i(TAG, " 结束 ");
            listener.scanFinish();
        }
    }

    public void stopScan(){
        if(pool != null){
            pool.shutdownNow();
        }
        pool = null;
        listener = null;
    }

    private class PingRunner implements Runnable {
        private String host = "";
        private int port = 0;

        public PingRunner(String host, int port) {
            this.host = host;
            this.port = port;
        }

        Socket s = new Socket();

        @Override
        public void run() {
            SocketAddress add = new InetSocketAddress(host, port);
            try {
                s.connect(add, TIME_OUT);// 超时3秒
                Log.i(TAG, " IP："+host+" 可用");
                if(listener != null){
                    listener.availableIp(host);
                }
            } catch (IOException e) {
                Log.i(TAG, " IP："+host+" 超时");
            } finally {
                try {
                    s.close();
                } catch (IOException e) {

                }
            }
        }
    }

    /**
     * 获取ip地址
     * @return
     */
    public static String getHostIP() {

        String hostIp = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    String ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip)) {
                        hostIp = ia.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            Log.i(TAG, "SocketException");
            e.printStackTrace();
        }
        return hostIp;

    }
    /**
     * TODO<获取本机IP前缀>
     *
     * @param devAddress
     *            // 本机IP地址
     * @return String
     */
    private String getLocAddrIndex(String devAddress) {
        if (!devAddress.equals("")) {
            return devAddress.substring(0, devAddress.lastIndexOf(".") + 1);
        }
        return null;
    }

    private ScanDeviceListener listener;

    public void setListener(ScanDeviceListener listener) {
        this.listener = listener;
    }

    interface ScanDeviceListener{

        void availableIp(String ip);
        void scanFinish();
    }


}
