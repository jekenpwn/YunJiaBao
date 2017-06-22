package com.jeken.administrator.wenjiabao;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jeken.administrator.nethandle.HttpGet;
import com.jeken.administrator.nethandle.UseWaterTime;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cn.aigestudio.datepicker.cons.DPMode;
import cn.aigestudio.datepicker.views.DatePicker;

/**
 * Created by Administrator on 2016-11-19.
 */

public class RearchFragment extends Fragment {

    private View v;

    private final int HTTP_OK = 1;
    private final int HTTP_OK_DEVICE_LIST =2;
    private TextView tv_rearchfg_starttime,tv_rearchfg_endtime,tv_rearchfg_counttime,tv_rearchfg_settime;
    private DatePicker dp_rearchfg_date;
    private Spinner sp_rearchfg_which ,sp_rearchfg_timetype;
    private Button btn_rearchfg_submit,btn_rearchfg_time;
   // private LinearLayout ll_rearchfg_usetime;
    private TextView tv_rearchfg_zerodevice;
    private HorizontalScrollView hs_rearchfg_chooise;
    private ArrayList<String> data = new ArrayList<String>();//放绑定设备的列表
    private List<Button> list_btn = new ArrayList<Button>();
    private LinearLayout hs_ll;
    private View.OnClickListener btn_listener;
    private boolean while_flag;
    private boolean rearch_which = true;
    private int which_btn = -1;
    private int last_btn = -1;
    private String ID;
    private String URL_List_Device = "http://120.55.171.72/points.asp?";
    private String NAME;
    private String CHECKCODE;
    private short TYPE_TIME = 0;
    private short TIME = 0;

    private String time_during = "";
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == HTTP_OK){
                try{
                    String message = (String) msg.obj;
                    UseWaterTime usetime = new UseWaterTime(message);
                    long time = usetime.caluTime();
                    String str1 = "时间段：  "+tv_rearchfg_starttime.getText().toString()+"  到  "+tv_rearchfg_endtime.getText().toString();
                    String str2 = "使用时间：" + usetime.formatTime(time);
                    //String str3 = usetime.useState();
                    //Toast.makeText(getContext(),str1+"\n"+str2+"\n"+str3,Toast.LENGTH_LONG).show();
                    //ll_rearchfg_usetime.setVisibility(View.VISIBLE);
                    //tv_rearchfg_counttime.setText(str1+"\n\n"+str2);
                    TextView tv = new TextView(getContext());
                    tv.setTextColor(Color.RED);
                    tv.setTextSize(15);
                    tv.setText(time_during+"\n\n"+str2);
                    new AlertDialog.Builder(getActivity())
                            .setTitle("用水时间查询")
                            .setView(tv)
                            .setPositiveButton("确定",null)
                            .show();
                }catch (Exception e){
                    Log.e("TAG","查询用水时间时异常，最外防御截获");
                    Toast.makeText(getContext(),"服务器出错",Toast.LENGTH_SHORT).show();
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
            }else if (msg.what == 10){//完成绑定设备列表获取
                allBindDeviceListener();
                createHorizotnalScrollChildView();
            }
        }
    };
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_rearch,container,false);
        Bundle bundle = getArguments();
        ID = bundle.getString("ID");
        NAME = bundle.getString("NAME");
        CHECKCODE = bundle.getString("CHECKCODE");

        //请求获取绑定设备列表
        URL_List_Device += "username="+NAME+"&checksn="+CHECKCODE;
        //Toast.makeText(getContext(),"ID:"+ID,Toast.LENGTH_SHORT).show();
        findView();
        new HttpGet(mHandler,URL_List_Device,HTTP_OK_DEVICE_LIST);//发送http获取绑定设备list列表
        allListener();
        return v;
    }

    /**
     * 初始化全部组件
     */
    private void findView() {
        tv_rearchfg_starttime = (TextView) v.findViewById(R.id.tv_rearchfg_starttime);
        tv_rearchfg_endtime = (TextView) v.findViewById(R.id.tv_rearchfg_endtime);
        //tv_rearchfg_counttime = (TextView) v.findViewById(R.id.tv_rearchfg_counttime);
        tv_rearchfg_settime = (TextView) v.findViewById(R.id.tv_rearchfg_settime);
        tv_rearchfg_zerodevice = (TextView) v.findViewById(R.id.tv_rearchfg_zerodevice);
        dp_rearchfg_date = (DatePicker) v.findViewById(R.id.dp_rearchfg_date);
        sp_rearchfg_which = (Spinner) v.findViewById(R.id.sp_rearchfg_which);
        sp_rearchfg_timetype = (Spinner) v.findViewById(R.id.sp_rearchfg_timetype);
        btn_rearchfg_submit = (Button) v.findViewById(R.id.btn_rearchfg_submit);
        btn_rearchfg_time = (Button) v.findViewById(R.id.btn_rearchfg_time);
        //ll_rearchfg_usetime = (LinearLayout) v.findViewById(R.id.ll_rearchfg_usetime);
        hs_rearchfg_chooise = (HorizontalScrollView) v.findViewById(R.id.hs_rearchfg_chooise);
        Calendar c = Calendar.getInstance();
        String time = c.get(Calendar.YEAR)+"-"+(c.get(Calendar.MONTH)+1)+"-"+c.get(Calendar.DAY_OF_MONTH);
        tv_rearchfg_starttime.setText(time);
        tv_rearchfg_endtime.setText(time);
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
            hs_rearchfg_chooise.addView(hs_ll);
        }else {
            tv_rearchfg_zerodevice.setText("无设备可选");
        }
    }
    //建立对绑定设备列表中button的监听
    public void allBindDeviceListener(){
        btn_listener = new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Button btn = (Button)v;
                last_btn = which_btn;//记录上一次
                which_btn = list_btn.indexOf(btn);
                ID = data.get(which_btn);//设置ID
                if (last_btn >= 0){//上一个选中的恢复
                    list_btn.get(last_btn).setBackgroundResource(R.drawable.hs_btn_unpressed);
                }
                btn.setBackgroundResource(R.drawable.hs_btn_pressed);//选中本次的
            }
        };
    }
    /**
     * 监听所有点击事件
     */
    private void allListener() {

        tv_rearchfg_starttime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dp_rearchfg_date.setVisibility(View.VISIBLE);
                while_flag = true;

            }
        });
        tv_rearchfg_endtime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dp_rearchfg_date.setVisibility(View.VISIBLE);
                while_flag = false;
            }
        });

        tv_rearchfg_settime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText et = new EditText(getContext());
                et.setInputType(InputType.TYPE_CLASS_NUMBER);
                String showTile = null;
                String type = null;
                if (TYPE_TIME == 0) {
                    showTile = "请输入天偏移量";
                    et.setHint("1-29之间的数字");
                    type = "天";
                }else if (TYPE_TIME == 1) {
                    showTile = "请输入月偏移量";
                    et.setHint("1-11之间的数字");
                    type = "月";
                }
                else if (TYPE_TIME == 2){
                    showTile = "请输入年偏移量";
                    et.setHint("1-20之间的数字");
                    type = "年";
                }

                final String finalType = type;
                new AlertDialog.Builder(getActivity())
                         .setTitle(showTile)
                         .setView(et)
                         .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialog, int which) {
                                  try {
                                      tv_rearchfg_settime.setText("最近"+et.getText().toString()+ finalType);
                                      TIME = Short.parseShort(et.getText().toString());
                                  }catch (Exception e){
                                      Toast.makeText(getContext(),"请输入偏移量",Toast.LENGTH_SHORT).show();
                                      tv_rearchfg_settime.setText("设置查询偏移量");
                                  }
                             }
                         })
                         .show();
            }
        });

        Calendar c = Calendar.getInstance();
        dp_rearchfg_date.setDate(c.get(Calendar.YEAR),c.get(Calendar.MONTH)+1);
        dp_rearchfg_date.setMode(DPMode.SINGLE);

        dp_rearchfg_date.setOnDatePickedListener(new DatePicker.OnDatePickedListener() {
            @Override
            public void onDatePicked(String date) {
                if (while_flag){
                    tv_rearchfg_starttime.setText(date);
                }else {
                    tv_rearchfg_endtime.setText(date);
                }
                dp_rearchfg_date.setVisibility(View.INVISIBLE);
                //Toast.makeText(getContext(),date,Toast.LENGTH_SHORT).show();
            }
        });
        sp_rearchfg_which.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0){
                    rearch_which = true;
                }else if (position == 1){
                    rearch_which = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        sp_rearchfg_timetype.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0){
                    TYPE_TIME = 0;
                }else if (position == 1){
                    TYPE_TIME = 1;
                }else if (position == 2){
                    TYPE_TIME = 2;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btn_rearchfg_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TIME == 0){
                    Toast.makeText(getContext(),"请设置最近偏移量",Toast.LENGTH_SHORT).show();
                }else if(which_btn == -1) {
                    Toast.makeText(getContext(),"请在最下方选择查询设备",Toast.LENGTH_LONG).show();
                }else {
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                    Calendar c = Calendar.getInstance();
                    int year = c.get(Calendar.YEAR);
                    int month = c.get(Calendar.MONTH)+1;
                    int date = c.get(Calendar.DATE);
                    String end_time = year+"-"+month+"-"+date;
                    String start_time="";
                    try{
                        switch (TYPE_TIME) {
                            case 0:
                                long tmp = df.parse(end_time).getTime()-TIME*24*3600000;
                                start_time = df.format(tmp);
                                break;
                            case 1:
                                long mon = TIME*24;
                                mon = (mon*3600000*30);
                                long tmp1 = df.parse(end_time).getTime()-mon;
                                start_time = df.format(tmp1);
                                break;
                            case 2:
                                start_time = (year - TIME) + "-" + month + "-" + date;
                                break;
                        }
                    }catch(Exception e){

                    }


                    if (rearch_which){

                        String URL = "http://120.55.171.72/query.asp?pid="+ID+"&starttime="+start_time
                                +"&endtime="+end_time+"&maxnum=5000";//设置最多处理500条数据，超过没办法，目前服务器不处理，后面真没办法自行解决。
                        //Log.e("TAG",TYPE_TIME+"--"+TIME+"--"+URL);
                        Intent intent = new Intent(getContext(),DataRearchActivity.class);
                        intent.putExtra("URL",URL);
                        startActivity(intent);
                    }else {
                        String URL = "http://120.55.171.72/query.asp?pid="+ID+"&starttime="+start_time
                                +"&endtime="+end_time+"&maxnum=5000";//设置最多处理500条数据，超过没办法，目前服务器不处理，后面真没办法自行解决。
                        //Log.e("TAG",TYPE_TIME+"--"+TIME+"--"+URL);
                        time_during = "时间段：  "+start_time+"  到  "+end_time;
                        new HttpGet(mHandler,URL,HTTP_OK);
                    }
                }
            }
        });

        btn_rearchfg_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                long start_ms = 0;
                long end_ms = 0;
                String start = tv_rearchfg_starttime.getText().toString();
                String end = tv_rearchfg_endtime.getText().toString();
                try {
                    start_ms = df.parse(start).getTime();
                    end_ms = df.parse(end).getTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (ID != null){//如果设备为null则表明还没选择可用设备
                    if (start.equals("开始时间")){
                        Toast.makeText(getContext(),"请输入查询的开始时间",Toast.LENGTH_SHORT).show();
                    } else if (end.equals("结束时间")){
                        Toast.makeText(getContext(),"请输入查询的结束时间",Toast.LENGTH_SHORT).show();
                    }else if(start_ms > end_ms){
                        Toast.makeText(getContext(),"时间段不在查询范围",Toast.LENGTH_SHORT).show();
                    }else {
                        //ll_rearchfg_usetime.setVisibility(View.INVISIBLE);
                        if (rearch_which){
                            String URL = "http://120.55.171.72/query.asp?pid="+ID+"&starttime="+start
                                    +"&endtime="+end+"&maxnum=5000";//设置最多处理500条数据，超过没办法，目前服务器不处理，后面真没办法自行解决。
                            Intent intent = new Intent(getContext(),DataRearchActivity.class);
                            intent.putExtra("URL",URL);
                            startActivity(intent);
                            //Toast.makeText(getContext(),"查询方式:开关情况\n开始时间"+start+"\n结束时间："+end,Toast.LENGTH_LONG).show();
                        }else {
                            String URL = "http://120.55.171.72/query.asp?pid="+ID+"&starttime="+start
                                    +"&endtime="+end+"&maxnum=5000";//设置最多处理500条数据，超过没办法，目前服务器不处理，后面真没办法自行解决。
                            time_during = "时间段：  "+start+"  到  "+end;
                            new HttpGet(mHandler,URL,HTTP_OK);
                            //Toast.makeText(getContext(),"查询方式:用水情况\n开始时间"+start+"\n结束时间："+end,Toast.LENGTH_LONG).show();
                        }

                    }
                }else {
                    Toast.makeText(getContext(),"请先在最下方选择设备",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


}
