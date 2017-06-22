package com.jeken.administrator.wenjiabao;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.jeken.administrator.nethandle.UDPsend;

/**
 * UDPSerivce 在登录成功后启动此服务，负责UDPsend的工作
 */
public class UDPService extends Service {

    private final String ACTION_UDP_NO = "com.jeken.udp.no";
    private final String ACTION_UDP_OFF = "com.jeken.udp.off";
    private final String ACTION_UDP_TIME = "com.jeken.udp.time";
   // private final String ACTION_DRIVER_STATE = "com.jeken.driver.state";
    private final String ACTION_UDDATA_ID = "com.jeken.updata.id";

    private String ID;//传递ID过来
    private BroadcastReceiver receiver;
    private UDPsend udpSend;
    public UDPService(){
        super();
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String tmp = intent.getStringExtra("ID");
        if (tmp != null){
            ID = tmp;
        }else {
            ID = "00000000";
        }
        udpSend = new UDPsend(ID);
        //广播接收发送UDP指令
        BoradCastReciverHandle();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    private void BoradCastReciverHandle(){

        receiver =  new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String ACTION = intent.getAction();
                if (ACTION == ACTION_UDP_NO){//远程开
                    udpSend.sendOnDriver();
                    //updateStateDriver();//更新状态
                }else if (ACTION == ACTION_UDP_OFF){//远程关
                    udpSend.sendOffDriver();
                    //updateStateDriver();//更新状态
                }else if (ACTION == ACTION_UDP_TIME){//远程设置水阀时间
                    udpSend.sendShutTime(Integer.parseInt(intent.getStringExtra("TIME")));
                }else if (ACTION == ACTION_UDDATA_ID){
                    //udpSend = new UDPsend(intent.getStringExtra("ID"));
                    udpSend.udpID(intent.getStringExtra("ID"));
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_UDP_NO);
        intentFilter.addAction(ACTION_UDP_OFF);
        intentFilter.addAction(ACTION_UDP_TIME);
        intentFilter.addAction(ACTION_UDDATA_ID);
        registerReceiver(receiver,intentFilter);
    }

//    private void updateStateDriver(){
//        final String url = "http://120.55.171.72/status.asp?pid="+ID;
//            new Thread(){
//                HttpURLConnection conn  = null;
//                @Override
//                public void run() {
//                    try {
//                        //打开URL并创建HTTP连接对象
//                        URL myurl = new URL(url);
//                        //设置参数
//                        conn = (HttpURLConnection) myurl.openConnection();
//                        conn.setRequestMethod("GET");
//                        conn.setReadTimeout(7000);
//                        conn.setConnectTimeout(9000);
//                        //连接
//                        conn.connect();
//                        //响应码200才正常
//                        if (conn.getResponseCode()==200){
//                            //得到InputStream, 并读取成String
//                            InputStream is = conn.getInputStream();
//                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                            byte[] buffer = new byte[1024];
//                            int len = -1;
//                            while((len=is.read(buffer))!=-1) {
//                                baos.write(buffer, 0, len);
//                            }
//                            //http响应信息发回Handler处理
//                            String state = baos.toString();
//                            Intent intent = new Intent(ACTION_DRIVER_STATE);
//                            if (state.equals("1")){//水阀状态为关
//                                intent.putExtra("STATE","关");
//                                //sendBroadcast(intent);
//                                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
//                            }else if (state.equals("0")){//水阀状态为开
//                                intent.putExtra("STATE","开");
//                                //sendBroadcast(intent);
//                                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
//                            }
//                            baos.close();
//                            is.close();
//                        }
//                    }catch (Exception e){
//                        if (conn != null)
//                            conn.disconnect();
//                    }finally {
//                        if (conn != null)
//                            conn.disconnect();
//                    }
//                }
//            }.start();
//
//        }

}
