package com.jeken.administrator.nethandle;

import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import static android.R.attr.id;

/**
 * Created by Administrator on 2016-11-08.
 */

public class UDPsend {

    private static  byte[] bytes = new byte[20];
    private DatagramSocket ds;
    private DatagramPacket dp;
    private String HOST = "120.55.171.72";
    private int PORT = 60001;
    private String ID;

    public UDPsend(String ID){

        this.ID = ID;

        initBytes();
    }

    public void  udpID(String ID){
        this.ID = ID;
        initBytes();
    }
    /**
     * 初始化20个字节，附上ID
     *
     */
    public void initBytes(){
        byte[] ids = new byte[4];
        for(int i = 0;i<4;i++){
            ids[i] = (byte)((id&(0xff<<(i*8)))>>(i*8));
        }
        int len_id = ID.length();
        if (len_id < 8){
            for(int i = 0;i<(8-len_id);i++)
                ID = "0" + ID;
        }
        int tmp;
        for(int j = 0;j < 4;j++){
            tmp = (ID.charAt(j*2)-48)*16+(ID.charAt(j*2+1)-48);
            ids[3-j] = (byte)(tmp&0xff);
        }
        //帧头
        bytes[0] = (byte) 0xA8; bytes[1] = (byte) 0x53;
        bytes[2] = (byte) 0xD0; bytes[3] = (byte) 0x26;
        //app向电路板发数据     //模块ID低位
        bytes[4] = (byte) 0x02; bytes[5] = ids[0];
        bytes[6] = ids[1];       bytes[7] = ids[2];
        //模块ID高位
        bytes[8] = ids[3];       bytes[9] = (byte) 0x00;
        bytes[10] = (byte) 0x01;bytes[11] = (byte) 0x00;//app查询位
        //来水                   //指示来水，无效
        bytes[12] = (byte) 0x00;bytes[13] = (byte) 0x00;
        bytes[14] = (byte) 0x00;bytes[15] = (byte) 0x00;
        bytes[16] = (byte) 0x00;bytes[17] = (byte) 0x00;
        bytes[18] = (byte) 0x00;bytes[19] = (byte) 0x00;//检验和
    }

    /**
     * 发送UDP到转发服务器
     *
     */
    private void UDPconn(){
        new Thread(){
            @Override
            public void run() {
                try {
                    ds = new DatagramSocket();
                    dp = new DatagramPacket(bytes,0,bytes.length, InetAddress.getByName(HOST),PORT);
                    ds.send(dp);
                    Log.e("TAG","门限发！");
                } catch (Exception e) {
                    Log.e("TAG","UDP开异常--"+HOST+"--"+PORT);
                    e.printStackTrace();
                }finally {
                    ds.close();
                }

            }
        }.start();
    }

    /**
     * 发送开设备指令
     */
    public void sendOffDriver(){

        bytes[11] = (byte) 0x02;//app查询位
        bytes[18] = (byte) 0x00;//清门限

        bytes[12] = (byte) 0x01;//停水

        int check = (bytes[5]&0xff)+(bytes[6]&0xff)+(bytes[7]&0xff)+(bytes[8]&0xff)+0x06;
        bytes[19] = (byte)(check&0xff);//检验和
        UDPconn();
    }

    /**
     * 发送关设备指令
     */
    public void sendOnDriver(){

        bytes[11] = (byte) 0x02;//app查询位
        bytes[18] = (byte) 0x00;//清门限

        bytes[12] = (byte) 0x00;//来水

        int check = (bytes[5]&0xff)+(bytes[6]&0xff)+(bytes[7]&0xff)+(bytes[8]&0xff)+0x05;
        bytes[19] = (byte)(check&0xff);//检验和
        UDPconn();
    }

    /**
     * 发送水阀关闭时间
     */
    public void sendShutTime(final int time){

        bytes[18] = (byte)(time&0xff);
        bytes[11] = (byte) 0x03;//app门限时间
        bytes[12] = (byte) 0x03;//怕服务器误解

        int check = (bytes[5]&0xff)+(bytes[6]&0xff)+(bytes[7]&0xff)+(bytes[8]&0xff)+(bytes[18]&0xff)+0x09;
        bytes[19] = (byte)(check&0xff);//检验和
        UDPconn();
    }
}
