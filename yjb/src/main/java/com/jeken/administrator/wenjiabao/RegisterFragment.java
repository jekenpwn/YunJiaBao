package com.jeken.administrator.wenjiabao;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.jeken.administrator.nethandle.HttpGet;
import com.jeken.administrator.smssend.SmsSend;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016-11-09.
 */

public class RegisterFragment extends Fragment {

    private View v;


    private final String SMS_TEMPLATEID = "37391";
    private final int HTTP_0K = 1;
    private final int HTTP_0K_2 = 4;
    private final int HTTP_OK_BIND_SECOND = 5;
    private final int HTTP_OK_UNBIND = 6;
    private boolean isShow = false;
    private boolean canUnbind = true;//是否让解绑按键可操作，未选择列表时不可以操作
    private int which_device = -1;//解绑列表设备下标
    private String URL= "http://120.55.171.72/points.asp?";
    //注册的所有EditText
    private TextView tv_registerfg_id;
    private TextView tv_registerfg_error;

    private Button btn_registerfg_scan;
    private Button btn_registerfg_bind;
    private Button btn_registerfg_list;
    private Button btn_registerfg_unbind;
    private ListView lv_registerfg;
    private TextView tv_registerfg_selected;
    private LinearLayout ll_registerfg_devicelist;
    private ArrayList<String> data = new ArrayList<String>();
    private MyAdapter adapter = new MyAdapter();
    private String NAME;
    private String CHECKCODE;


    private LocalBroadcastManager localBroadcastManager;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
             String result = intent.getStringExtra("SCANRESULT");
            tv_registerfg_id.setText(result);
        }
    };

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == HTTP_0K){
                String message  = (String) msg.obj;
                if (message.length()>=4){
                    try {
                        JSONArray ja = new JSONArray(message);
                        for (int i = 0 ;i < ja.length();i++){
                            JSONObject jo = new JSONObject(ja.get(i).toString());
                            String tmp = jo.getString("pid");
                            int len_id = tmp.length();
                            if (len_id < 8){
                                for(int j = 0;j<(8-len_id);j++)
                                    tmp = "0" + tmp;
                            }
                            if (!data.contains(tmp))
                                data.add(tmp);
                        }
                        mHandler.sendEmptyMessage(9);//发确认让自己大师工作
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }else if (msg.what == HTTP_0K_2){
                String message = (String) msg.obj;
                if (message.equals("1")){
                    tv_registerfg_error.setText("绑定成功");
                    new HttpGet(mHandler,URL,HTTP_0K);
                }else if (message.equals("0")){
                    tv_registerfg_error.setText("服务器出错，请重新绑定");
                }else if (!message.equals("")){
                    //tv_registerfg_error.setText("设备ID不存在");
                    //设备已经被绑定，请求次设备绑定
                    String[] str = message.split("@");
                    if (str.length >= 2){
                        SmsSend sms = new SmsSend(SMS_TEMPLATEID,str[0],str[1],mHandler);
                        sms.handSms();
                        requestBindSecond();
                    }

                }

            } else if (msg.what == HTTP_OK_BIND_SECOND){
                String message = (String) msg.obj;
                if (message.equals("1")){
                    tv_registerfg_error.setText("绑定成功");
                }else {
                    tv_registerfg_error.setText("验证码错误，请重新请求绑定");
                }
            }else if(msg.what == HTTP_OK_UNBIND){
                String message = (String) msg.obj;
                if (message.equals("1")){

                    data.remove(which_device);
                    adapter.notifyDataSetChanged();
                    btn_registerfg_unbind.setEnabled(false);
                    canUnbind = true;
                    adapter.setSelectedPosition(-1);
                }else {
                    tv_registerfg_error.setText("解绑失败");
                }

            }else if (msg.what == 9){
                adapter = new MyAdapter();
                lv_registerfg.setAdapter(adapter);
//                Toast.makeText(getContext(),"size:"+data.size()+"size:"+adapter.getCount(),Toast.LENGTH_SHORT).show();
            }
        }
    };



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_register,container,false);
        Bundle bundle = getArguments();
        NAME = bundle.getString("NAME");
        CHECKCODE = bundle.getString("CHECKCODE");
        //TEST
        Log.e("TAG","CODE="+CHECKCODE);
        //调试
        URL = URL + "username="+NAME+"&checksn="+CHECKCODE;

        findView();
        new HttpGet(mHandler,URL,HTTP_0K);
        setAllListener();
        broadcastReciver();

        return v;
    }

    private void findView() {

        //注册的所有EditText初始化对象
        tv_registerfg_id = (TextView) v.findViewById(R.id.tv_registerfg_id);
        tv_registerfg_error = (TextView) v.findViewById(R.id.tv_registerfg_error);
        btn_registerfg_scan  = (Button) v.findViewById(R.id.btn_registerfg_scan);
        btn_registerfg_bind  = (Button) v.findViewById(R.id.btn_registerfg_bind);
        btn_registerfg_list  = (Button) v.findViewById(R.id.btn_registerfg_list);
        btn_registerfg_unbind = (Button) v.findViewById(R.id.btn_registerfg_unbind);
        lv_registerfg = (ListView) v.findViewById(R.id.lv_registerfg);
        tv_registerfg_selected = (TextView) v.findViewById(R.id.tv_registerfg_selected);
        ll_registerfg_devicelist = (LinearLayout) v.findViewById(R.id.ll_registerfg_devicelist);
        btn_registerfg_unbind.setEnabled(false);

    }
    private void setAllListener(){
        //二维码扫描的按键时间监听
        btn_registerfg_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //扫描操作
                IntentIntegrator integrator = new IntentIntegrator(getActivity());
                integrator.setPrompt("请将二维码置于取景框内扫描");
                integrator.initiateScan();
            }
        });
        //绑定按键事件监听
        btn_registerfg_bind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //注册按键按下就清空错误信息
                tv_registerfg_error.setText("");
                //获得输入的bind信息
                String id = tv_registerfg_id.getText().toString();
                if (id.equals("")||id.length()!=8){
                    tv_registerfg_error.setText("绑定设备ID非法");
                }else {
                    String url = "http://120.55.171.72/reg.asp?pid="+id+"&username="
                            +NAME+"&checksn="+CHECKCODE;
                    new HttpGet(mHandler,url,HTTP_0K_2);
                }
            }
        });
        //解绑设备按键监听
        btn_registerfg_unbind.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(getActivity())
                        .setTitle("设备解绑")
                        .setMessage("警告！\n您正在解绑设备ID为"+data.get(which_device)+"的设备,"
                        +"解绑后将无法对该设备进制管理，是否确定解绑？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String id = data.get(which_device);
                                String url = "http://120.55.171.72/unreg.asp?pid="+id+"&username="+NAME;
                                new HttpGet(mHandler,url,HTTP_OK_UNBIND);
                            }
                        })
                        .setNegativeButton("取消",null)
                        .show();
            }
        });
        //显示或者隐藏列表按键监听
        btn_registerfg_list.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (isShow){
                      ll_registerfg_devicelist.setVisibility(View.INVISIBLE);
                      btn_registerfg_list.setText("显示设备列表");
                }else {
                    ll_registerfg_devicelist.setVisibility(View.VISIBLE);
                    btn_registerfg_list.setText("隐藏设备列表");
                }
                isShow = !isShow;
            }
        });


        lv_registerfg.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (canUnbind){
                    canUnbind = false;
                    btn_registerfg_unbind.setEnabled(true);
                }
                which_device = position;
                adapter.setSelectedPosition(position);
                adapter.notifyDataSetInvalidated();

                tv_registerfg_selected.setText(data.get(position));
                   //项目该需求，注释
//                Intent intent = new Intent("com.jeken.postid");
//                intent.putExtra("ID",data.get(position));
//                getContext().sendBroadcast(intent);

                //让Service更新ID
//                Intent serviceIntent = new Intent(ACTION_UDDATA_ID);
//                serviceIntent.putExtra("ID",data.get(position));
//                getActivity().sendBroadcast(serviceIntent);
            }
        });

    }


   //接收扫描二维码的返回信息
    public void broadcastReciver(){
        localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.jeken.scan");
        localBroadcastManager.registerReceiver(broadcastReceiver,intentFilter);
    }
    //在请求后发现设备已经被绑定时，进行次设备绑定流程
    public void requestBindSecond(){
        final EditText et = new EditText(getContext());
        et.setInputType(InputType.TYPE_CLASS_NUMBER);
        new AlertDialog.Builder(getActivity())
                .setMessage("设备已经被绑定，我们将为您发送验证码到主用户手机请求是否同意您的绑定，请随后向主用户索要通行码！")
                .setPositiveButton("继续",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new AlertDialog.Builder(getActivity())
                                .setMessage("请输入主用户提供的验证码")
                                .setView(et)
                                .setPositiveButton("完成", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String checkcode = et.getText().toString();
                                        String url = "http://120.55.171.72/reg2.asp?username="+NAME+
                                                "&pid="+tv_registerfg_id.getText().toString()+
                                                "&checksn="+checkcode;
                                        new HttpGet(mHandler,url,HTTP_OK_BIND_SECOND);
                                    }
                                })
                                .show();
                    }
                })
                .setNegativeButton("取消",null)
                .show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        localBroadcastManager.unregisterReceiver(broadcastReceiver);
    }

    class MyAdapter extends BaseAdapter {
        private int selectedPosition = -1;
        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }
        public void setSelectedPosition(int position) {
            selectedPosition = position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = getActivity().getLayoutInflater().inflate(R.layout.listview_item,null);

            TextView tv_num = (TextView) view.findViewById(R.id.tv_lvitem_num);
            TextView tv_id = (TextView) view.findViewById(R.id.tv_lvitem_id);
            tv_num.setText(position+1+"");
            tv_id.setText(data.get(position));
            if (position == selectedPosition){
                 view.setSelected(true);
                view.setPressed(true);
                view.setBackgroundResource(R.drawable.edit_shape);
            }else {
                view.setSelected(false);
                view.setPressed(false);
            }
            return view;
        }
    }


}
