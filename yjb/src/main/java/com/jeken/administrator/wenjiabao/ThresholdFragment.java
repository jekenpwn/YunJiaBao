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
import android.widget.EditText;
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
 * Created by Administrator on 2016-11-19.
 */

public class ThresholdFragment extends Fragment {

    private View v;

    private final int HTTP_OK = 1;
    private final int HTTP_OK_DEVICE_LIST =2;
    private final int HTTP_OK_STATUS = 3;
    private final String ACTION_UDDATA_ID = "com.jeken.updata.id";//广播UDPserver改变发送的id
    private final String ACTION_UDP_TIME = "com.jeken.udp.time";//广播UDPserver改变发送的设备时限
    //设置水阀关闭时间
    private int shutTime = 3;

    private String ID="";
    private EditText et_thresholdfg_time;
    private Button btn_thresholdfg_add,btn_thresholdfg_sub,btn_thresholdfg_submit;
    private TextView tv_thresholdfg_value;
    private TextView tv_thresholdfg_zerodevice;
    private TextView tv_thresholdfg_netstatus;
    private TextView tv_thresholdfg_id;
    private HorizontalScrollView hs_thresholdfg_chooise;
    private ArrayList<String> data = new ArrayList<String>();//放绑定设备的列表
    private List<Button> list_btn = new ArrayList<Button>();
    private LinearLayout hs_ll;
    private View.OnClickListener btn_listener;
    private String URL = "http://120.55.171.72/value.asp?pid=";
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
            if (msg.what == HTTP_OK) {
                String message = (String) msg.obj;
                if (!message.equals("")) {
                    tv_thresholdfg_value.setText(message + "分钟");
                }
            } else if (msg.what == HTTP_OK_STATUS){
                String message = (String) msg.obj;
                if (message.equals("3")) {
                    tv_thresholdfg_value.setTextColor(Color.BLUE);
                    tv_thresholdfg_value.setText("开");

                } else if (message.equals("2")) {
                    tv_thresholdfg_value.setTextColor(Color.RED);
                    tv_thresholdfg_value.setText("关");
                } else {
                    Toast.makeText(getContext(), "服务器异常！！！", Toast.LENGTH_LONG).show();
                }
            }else if(msg.what == HTTP_OK_DEVICE_LIST ){
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
            }else if (msg.what == 10){//完成绑定设备列表获取
                allBindDeviceListener();
                createHorizotnalScrollChildView();
            }else if (msg.what == 11){
                if (ID!=null&&!ID.equals("")&&flag_online){
                    String url = URL + ID;
                    new HttpGet(mHandler,url,HTTP_OK);
                }
                mHandler.sendEmptyMessageDelayed(11,2500);
            }
        }
    };

    private LocalBroadcastManager localBroadcastManager;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String result = intent.getStringExtra("ONLINE");
            if (result.equals("0")&&which_btn >= 0){
                tv_thresholdfg_netstatus.setTextColor(Color.RED);
                tv_thresholdfg_netstatus.setText("离线");
                flag_online = false;
                if (flag_btn) setBtnEnableStatus(false);
                flag_btn = false;
            }else if (result.equals("1")&&which_btn >= 0){
                tv_thresholdfg_netstatus.setTextColor(Color.BLUE);
                tv_thresholdfg_netstatus.setText("已联网");
                flag_online = true;
                if (!flag_btn) setBtnEnableStatus(true);
                flag_btn = true;
            }
        }
    };
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_threshold,container,false);
        Bundle bundle = getArguments();
        NAME = bundle.getString("NAME");
        CHECKCODE = bundle.getString("CHECKCODE");
//        if (!ID.equals("")){
//            URL = URL + ID;
//            new HttpGet(mHandler,URL,HTTP_OK);
//        }else {
//            Toast.makeText(getContext(),"请在设备设置选择设备",Toast.LENGTH_LONG).show();
//        }
        URL_List_Device += "username="+NAME+"&checksn="+CHECKCODE;
        findView();
        new HttpGet(mHandler,URL_List_Device,HTTP_OK_DEVICE_LIST);//发送http获取绑定设备list列表
        allListener();
        //注册接收广播
        broadcastReciver();
        mHandler.sendEmptyMessageDelayed(11,1000);//延时1s后开启轮询水阀门限改变
        return v;
    }

    /**
     *
     */
    private void findView() {
        et_thresholdfg_time = (EditText) v.findViewById(R.id.et_thresholdfg_time);
        btn_thresholdfg_add = (Button) v.findViewById(R.id.btn_thresholdfg_add);
        btn_thresholdfg_sub = (Button) v.findViewById(R.id.btn_thresholdfg_sub);
        btn_thresholdfg_submit = (Button) v.findViewById(R.id.btn_thresholdfg_submit);
        tv_thresholdfg_value = (TextView) v.findViewById(R.id.tv_thresholdfg_value);
        tv_thresholdfg_zerodevice = (TextView) v.findViewById(R.id.tv_thresholdfg_zerodevice);
        tv_thresholdfg_netstatus = (TextView) v.findViewById(R.id.tv_thresholdfg_netstatus);
        tv_thresholdfg_id = (TextView) v.findViewById(R.id.tv_thresholdfg_id);
        hs_thresholdfg_chooise = (HorizontalScrollView) v.findViewById(R.id.hs_thresholdfg_chooise);
        et_thresholdfg_time.setText("3");
    }

    /**
     * 对所有xml中的按键进行监听
     */
    private void allListener(){

        btn_thresholdfg_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (which_btn == -1){
                    Toast.makeText(getContext(),"请选择设备",Toast.LENGTH_SHORT).show();
                }else if (flag_online){
                    if (shutTime >= 60){
                        Toast.makeText(getActivity(),"关水门限时间不可大于60分钟",Toast.LENGTH_SHORT).show();
                        et_thresholdfg_time.setText("60");
                    } else {
                        shutTime += 3;
                        et_thresholdfg_time.setText(shutTime+"");
                    }
                }else{
                    Toast.makeText(getContext(),"设备不在线",Toast.LENGTH_SHORT).show();
                }
            }
        });
        btn_thresholdfg_sub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (which_btn == -1){
                    Toast.makeText(getContext(),"请选择设备",Toast.LENGTH_SHORT).show();
                }else if (flag_online){
                    if (shutTime <= 3){
                        Toast.makeText(getActivity(),"关水门限时间不可低于3分钟",Toast.LENGTH_SHORT).show();
                        et_thresholdfg_time.setText("3");
                    } else {
                        shutTime -= 3;
                        et_thresholdfg_time.setText(shutTime+"");
                    }
                }else{
                    Toast.makeText(getContext(),"设备不在线",Toast.LENGTH_SHORT).show();
                }
            }
        });
        btn_thresholdfg_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (which_btn == -1){
                    Toast.makeText(getContext(),"请选择设备",Toast.LENGTH_SHORT).show();
                }else if (flag_online){
                    Intent intent =  new Intent(ACTION_UDP_TIME);
                    intent.putExtra("TIME",et_thresholdfg_time.getText().toString());
                    getActivity().sendBroadcast(intent);

                    mHandler.sendEmptyMessageDelayed(9,1000);
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
            hs_thresholdfg_chooise.addView(hs_ll);
        }else {
            tv_thresholdfg_zerodevice.setText("无设备可选");
        }
    }
    //查询状态，联网信息,更新UDPserver ID
    public void rearchDeviceStatus(String ID,int position){
        new HttpGet(mHandler,URL+ID,HTTP_OK);//查一次水阀的门限
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
                which_btn = list_btn.indexOf(btn);
                String id = data.get(which_btn);
                tv_thresholdfg_id.setText(id);
                ID = id;//更新ID
                rearchDeviceStatus(id,which_btn);
                if (last_btn >= 0){//上一个选中的恢复
                    list_btn.get(last_btn).setBackgroundResource(R.drawable.hs_btn_unpressed);
                }
                btn.setBackgroundResource(R.drawable.hs_btn_pressed);//选中本次的

            }
        };
    }
    //设置按键是否使能
    public  void setBtnEnableStatus(boolean status){
        btn_thresholdfg_add.setEnabled(status);
        btn_thresholdfg_sub.setEnabled(status);
        btn_thresholdfg_submit.setEnabled(status);

    }
    //接收广播设备状态改变了
    public void broadcastReciver(){
        localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.jeken.ifonline");
        localBroadcastManager.registerReceiver(broadcastReceiver,intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        localBroadcastManager.unregisterReceiver(broadcastReceiver);
    }
}
