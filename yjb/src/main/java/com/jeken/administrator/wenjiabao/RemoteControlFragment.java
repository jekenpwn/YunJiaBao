package com.jeken.administrator.wenjiabao;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jeken.administrator.nethandle.HttpGet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016-11-13.
 */

public class RemoteControlFragment extends Fragment {

    private final String ACTION_UDDATA_ID = "com.jeken.updata.id";
    private final int HTTP_OK = 1;
    private final int HTTP_OK_DEVICE_LIST =2;
    private final String ACTION_UDP_NO = "com.jeken.udp.no";
    private final String ACTION_UDP_OFF = "com.jeken.udp.off";
    private View v;
    private TextView tv_control_state;
    private TextView tv_remotefg_zerodevice;
    private TextView tv_control_id;//当前设备
    private TextView tv_control_netstatus;//当前设备是否联网

    private Button btn_remote_on,btn_remote_off;
    private ArrayList<String> data = new ArrayList<String>();//放绑定设备的列表
    private List<Button> list_btn = new ArrayList<Button>();
    LinearLayout hs_ll;
    private View.OnClickListener btn_listener;
    //private ImageView iv_remotefg_state;
    private HorizontalScrollView hs_remote_chooise;
    private String ID ="";
    private String URL = "http://120.55.171.72/status.asp?pid=";
    private String URL_List_Device = "http://120.55.171.72/points.asp?";
    private String NAME;
    private String CHECKCODE;
    private boolean flag_online = true;
    private boolean flag_btn = false;
    private int which_btn = -1;
    private int last_btn = -1;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == HTTP_OK){
                String message = (String) msg.obj;
                if (message.equals("3")){
                    tv_control_state.setTextColor(Color.BLUE);
                    tv_control_state.setText("开");
                    //iv_remotefg_state.setBackgroundResource(R.drawable.wateron);
                }else if (message.equals("2")){
                    tv_control_state.setTextColor(Color.RED);
                    tv_control_state.setText("关");
                    //iv_remotefg_state.setBackgroundResource(R.drawable.wateroff);
                }else {
                    Toast.makeText(getContext(),"服务器异常！！！",Toast.LENGTH_LONG).show();
                }
            }else if (msg.what == HTTP_OK_DEVICE_LIST){//http 获取列表结果
                String message  = (String) msg.obj;
                try {
                    JSONArray ja = new JSONArray(message);
                    for (int i = 0; i < ja.length(); i++) {
                        JSONObject jo = new JSONObject(ja.get(i).toString());
                        String tmp = jo.getString("pid");
                        int len_id = tmp.length();
                        if (len_id < 8) {
                            for (int j = 0; j < (8 - len_id); j++)
                                tmp = "0" + tmp;
                        }
                        if (!data.contains(tmp))
                            data.add(tmp);
                    }
                    mHandler.sendEmptyMessage(10);//发确认让自己大师工作
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else if (msg.what == 9){//设备状态查询
                new HttpGet(mHandler,URL+ID,HTTP_OK);
            }else if (msg.what == 10){//完成绑定设备列表获取
                allBindDeviceListener();
                createHorizotnalScrollChildView();
            }
        }
    };

    private LocalBroadcastManager localBroadcastManager;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String result = intent.getStringExtra("ONLINE");
            if (result.equals("0")&&which_btn >= 0){
                tv_control_netstatus.setTextColor(Color.RED);
                tv_control_netstatus.setText("离线");
                if (flag_btn) setBtnEnableStatus(false);
                flag_online = false;
                flag_btn = false;
            }else if (result.equals("1")&&which_btn >= 0){
                tv_control_netstatus.setTextColor(Color.BLUE);
                tv_control_netstatus.setText("已联网");
                if (!flag_btn) setBtnEnableStatus(true);
                flag_online = true;
                flag_btn = true;
            }
        }
    };
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_remotecontrol,container,false);

        Bundle bundle = getArguments();
        NAME = bundle.getString("NAME");
        CHECKCODE = bundle.getString("CHECKCODE");

        //请求获取绑定设备列表
        URL_List_Device += "username="+NAME+"&checksn="+CHECKCODE;

        //组件对象初始化
        findView();
        new HttpGet(mHandler,URL_List_Device,HTTP_OK_DEVICE_LIST);//发送http获取绑定设备list列表
        //广播接收设备状态改变
        broadcastReciver();
        return v;
    }

    private void findView() {
        tv_control_state = (TextView) v.findViewById(R.id.tv_control_state);
        tv_remotefg_zerodevice = (TextView) v.findViewById(R.id.tv_remotefg_zerodevice);
        tv_control_id = (TextView) v.findViewById(R.id.tv_control_id);
        tv_control_netstatus = (TextView) v.findViewById(R.id.tv_control_netstatus);
        btn_remote_on = (Button) v.findViewById(R.id.btn_remote_on);
        btn_remote_off = (Button) v.findViewById(R.id.btn_remote_off);
        //iv_remotefg_state = (ImageView) v.findViewById(iv_remotefg_state);
        hs_remote_chooise = (HorizontalScrollView) v.findViewById(R.id.hs_remote_chooise);

        btn_remote_on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flag_online){
                    sendUDPBroadCast(ACTION_UDP_NO);
                    //延时1s等待udp到达
                    mHandler.sendEmptyMessageDelayed(9,1000);
                    new HttpGet(mHandler,URL+ID,HTTP_OK);
                }else {
                    Toast.makeText(getContext(),"设备不在线",Toast.LENGTH_SHORT).show();
                }

            }
        });
        btn_remote_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flag_online){
                    sendUDPBroadCast(ACTION_UDP_OFF);
                    //延时1s等待udp到达
                    mHandler.sendEmptyMessageDelayed(9,1000);
                    new HttpGet(mHandler,URL+ID,HTTP_OK);
                }else {
                    Toast.makeText(getContext(),"设备不在线",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    //为HorizontalScrollView创建子视图，通过获取已绑定设备列表来创建n个button
    public void createHorizotnalScrollChildView(){
        if (data.size() > 0){
            Log.e("Tag","devicesize="+data.size());
            hs_ll = new LinearLayout(getActivity());
            hs_ll.setOrientation(LinearLayout.HORIZONTAL);
            for (int i = 0;i < data.size();i++){
                Button btn  = new Button(getActivity());
                btn.setBackgroundResource(R.drawable.hs_btn_unpressed);
                btn.setTextColor(getResources().getColor(R.color.hs_border));
                btn.setText(i+1+"");
                btn.setOnClickListener(btn_listener);
                list_btn.add(btn);
                hs_ll.addView(btn);
            }
            hs_remote_chooise.addView(hs_ll);
        }else {
            tv_remotefg_zerodevice.setText("无设备可选");
        }
    }
    //查询状态，联网信息,更新UDPserver ID
    public void rearchDeviceStatus(String ID,int position){
        new HttpGet(mHandler,URL+ID,HTTP_OK);
        //new HttpGet(mHandler,"http://120.55.171.72/online.asp?pid="+ID,HTTP_OK);
        //传ID给activity，让其重新轮询新的ID设备状态
        Intent intent = new Intent("com.jeken.postid");
        intent.putExtra("ID",data.get(position));
        getContext().sendBroadcast(intent);

        //让Service更新ID
        Intent serviceIntent = new Intent(ACTION_UDDATA_ID);
        serviceIntent.putExtra("ID",data.get(position));
        getActivity().sendBroadcast(serviceIntent);
    }
    //建立对绑定设备列表中button的监听
    public void allBindDeviceListener(){
        btn_listener = new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Button btn = (Button)v;
                last_btn = which_btn;//记录上一次
                which_btn = list_btn.indexOf(btn);//显示当前选择设备id
                String id = data.get(which_btn);
                tv_control_id.setText(id);
                ID = id;
                rearchDeviceStatus(id,which_btn);
                if (last_btn >= 0){//上一个选中的恢复
                    list_btn.get(last_btn).setBackgroundResource(R.drawable.hs_btn_unpressed);
                }
                btn.setBackgroundResource(R.drawable.hs_btn_pressed);//选中本次的

            }
        };
    }

    //发送广播给UDPService
    public void sendUDPBroadCast(String com){
        Intent intent =  new Intent();
        intent.setAction(com);
        getActivity().sendBroadcast(intent);
    }

    //接收广播设备状态改变了
    public void broadcastReciver(){
        localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.jeken.ifonline");
        localBroadcastManager.registerReceiver(broadcastReceiver,intentFilter);
    }

    //按键使能状态设置
    public void setBtnEnableStatus(boolean status){
        btn_remote_off.setEnabled(status);
        btn_remote_on.setEnabled(status);
        if (status){
            btn_remote_off.setBackgroundResource(R.drawable.button_control);
            btn_remote_on.setBackgroundResource(R.drawable.button_control);
        }else {
            btn_remote_off.setBackgroundResource(R.drawable.hui);
            btn_remote_on.setBackgroundResource(R.drawable.hui);
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        localBroadcastManager.unregisterReceiver(broadcastReceiver);
    }
}
