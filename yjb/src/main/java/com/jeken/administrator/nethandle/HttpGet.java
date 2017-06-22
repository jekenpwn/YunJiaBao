package com.jeken.administrator.nethandle;


import android.os.Handler;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 处理请求返回数据不是很大的，不然用Handler来传递大量数据不是明智的选择
 * Created by Administrator on 2016-11-08.
 */

public class HttpGet {

    private int HTTP_OK;
    //线程向activity发信息
    private Handler mHandler;
    private String url;
    public HttpGet(Handler mHandler,String url,int HTTP_OK){
        this.mHandler = mHandler;
        this.url = url;
        this.HTTP_OK = HTTP_OK;

        UrlRequest();
    }

    private void UrlRequest(){

        new Thread(){
            HttpURLConnection conn  = null;
            @Override
            public void run() {
                try {
                    //打开URL并创建HTTP连接对象
                    URL myurl = new URL(url);
                    //设置参数
                    conn = (HttpURLConnection) myurl.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setReadTimeout(7000);
                    conn.setConnectTimeout(9000);
                    //连接
                    conn.connect();
                    //响应码200才正常
                    if (conn.getResponseCode()==200){
                        //得到InputStream, 并读取成String
                        InputStream is = conn.getInputStream();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int len = -1;
                        while((len=is.read(buffer))!=-1) {
                            baos.write(buffer, 0, len);
                        }
                        //http响应信息发回Handler处理
                        mHandler.obtainMessage(HTTP_OK, baos.toString()).sendToTarget();
                        baos.close();
                        is.close();
                    }
                }catch (Exception e){
                    if (conn != null)
                        conn.disconnect();
                }finally {
                    if (conn != null)
                        conn.disconnect();
                }
            }
        }.start();

    }
}
