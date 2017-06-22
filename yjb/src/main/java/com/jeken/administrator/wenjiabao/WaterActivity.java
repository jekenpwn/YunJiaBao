package com.jeken.administrator.wenjiabao;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.jeken.administrator.nethandle.HttpGet;

import java.util.ArrayList;

import static com.jeken.administrator.wenjiabao.R.drawable.rearch;

public class WaterActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private int HTTP_OK = 1;
    private int REARCH_ONLINE = 2;

    private Integer[] ICON = {R.drawable.remoteswitch,R.drawable.devicebind, rearch,R.drawable.reliefswitch,R.drawable.usermananger};
    private GridView gv_water;
    private GVAdapter adapter;
    private ArrayList<String> data = new ArrayList<String>();

    private String NAME;
    private String PWD;
    private String CHECKCODE;
    private String ID="";

    private boolean flag = true;
    private boolean flag_prev=true;
    private boolean flag_next=true;
    private boolean isFirst = true;
    private String URL="http://120.55.171.72/online.asp?pid=";
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ID = intent.getStringExtra("ID");
            flag_prev = flag_next = true;
            flag = true;
            isFirst = true;
            //Log.e("Water","updateID:"+ID);//dbug
        }
    };

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == REARCH_ONLINE){
                if (ID!=null&&!ID.equals("")){
                    String url = URL + ID;
                    new HttpGet(mHandler,url,HTTP_OK);
                }
                mHandler.sendEmptyMessageDelayed(REARCH_ONLINE,2000);
            }else if (msg.what == HTTP_OK){//不断请求设备状态，改变则发广播告知fragment
                String message = (String) msg.obj;
                
                if (flag){
                    if (message.equals("0")) flag_prev = false;
                    else if (message.equals("1")) flag_prev = true;
                }else {
                    if (message.equals("0")) flag_next = false;
                    else if (message.equals("1")) flag_next = true;
                }
                flag = !flag;
                if ((flag_prev != flag_next)||isFirst){
                    Intent intent = new Intent("com.jeken.ifonline");
                    intent.putExtra("ONLINE",message);
                    LocalBroadcastManager.getInstance(WaterActivity.this).sendBroadcast(intent);
                    //掉线提示
//                    if (message.equals("0"))
//                        Toast.makeText(WaterActivity.this,"设备已经离线",Toast.LENGTH_LONG).show();
                    //Log.e("Water","online:"+message);
                    if (isFirst) isFirst = false;
                }

            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water);

        Intent intent = getIntent();
        NAME = intent.getStringExtra("NAME");
        PWD = intent.getStringExtra("PWD");
        CHECKCODE = intent.getStringExtra("CHECKCODE");


        Fragment fragment = new RemoteControlFragment();
        Bundle bundle = new Bundle();
        bundle.putString("ID",ID);
        bundle.putString("NAME",NAME);
        bundle.putString("CHECKCODE",CHECKCODE);
        fragment.setArguments(bundle);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.myFrameLayout,fragment);
        ft.commit();

        findView();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.jeken.postid");
        registerReceiver(receiver,intentFilter);
        startService(new Intent(this,UDPService.class));

        mHandler.sendEmptyMessageDelayed(REARCH_ONLINE,2500);//每个两秒去查询下设备是否在线

    }

    private void findView() {
        gv_water = (GridView) findViewById(R.id.gv_water);
        //数据准备
        data.add("远程开关");
        data.add("设备设置");
        data.add("历史查询");
        data.add("水阀设置");
        data.add("用户管理");
        //创建适配器
        adapter = new GVAdapter();
        gv_water.setAdapter(adapter);
        gv_water.setOnItemClickListener(this);
    }



    class GVAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){//空才创建，避免浪费内存导致内存泄漏
                convertView =  View.inflate(WaterActivity.this, R.layout.gridview_item, null);
            }

            ImageView imageView = (ImageView) convertView.findViewById(R.id.item_icon);
            TextView textView = (TextView) convertView.findViewById(R.id.item_name);
            imageView.setImageResource(ICON[position]);
            textView.setText(data.get(position));


            return convertView;
        }
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
          if (position == 0){
              Fragment fragment = new RemoteControlFragment();
              Bundle bundle = new Bundle();
              bundle.putString("ID",ID);
              bundle.putString("NAME",NAME);
              bundle.putString("CHECKCODE",CHECKCODE);
              fragment.setArguments(bundle);
              FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
              ft.replace(R.id.myFrameLayout,fragment);
              ft.commit();
          }else if (position == 1){
              Fragment fragment = new RegisterFragment();
              Bundle bundle = new Bundle();
              bundle.putString("NAME",NAME);
              bundle.putString("PWD",PWD);
              bundle.putString("CHECKCODE",CHECKCODE);
              fragment.setArguments(bundle);
              FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
              ft.replace(R.id.myFrameLayout,fragment);
              ft.commit();
          }else  if (position == 2){

              Fragment fragment = new RearchFragment();
              Bundle bundle = new Bundle();
              bundle.putString("NAME",NAME);
              bundle.putString("CHECKCODE",CHECKCODE);
              fragment.setArguments(bundle);
              FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
              ft.replace(R.id.myFrameLayout,fragment);
              ft.commit();
          }else if (position == 3){
              Fragment fragment = new ThresholdFragment();
              Bundle bundle = new Bundle();
              bundle.putString("ID",ID);
              bundle.putString("NAME",NAME);
              bundle.putString("CHECKCODE",CHECKCODE);
              fragment.setArguments(bundle);
              FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
              ft.replace(R.id.myFrameLayout,fragment);
              ft.commit();
          }else if (position == 4){
              Fragment fragment = new UserManagerFragment();
              Bundle bundle = new Bundle();
              bundle.putString("NAME",NAME);
              bundle.putString("CHECKCODE",CHECKCODE);
              fragment.setArguments(bundle);
              FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
              ft.replace(R.id.myFrameLayout,fragment);
              ft.commit();
          }
    }
  //二维码扫描返回，广播给设备绑定fragment
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanResult != null) {
            String result = scanResult.getContents();
            Intent intent = new Intent("com.jeken.scan");
            intent.putExtra("SCANRESULT",result);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //最后把服务解绑
        unregisterReceiver(receiver);
        stopService(new Intent(this,UDPService.class));
    }
}
