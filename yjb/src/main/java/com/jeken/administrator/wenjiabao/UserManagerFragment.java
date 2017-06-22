package com.jeken.administrator.wenjiabao;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jeken.administrator.nethandle.HttpGet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017-02-20.
 */

public class UserManagerFragment extends Fragment {

    private View v;
    private final int HTTP_OK_ID_USERS = 1;
    private final int HTTP_OK_DEVICE_LIST =2;
    private final int HTTP_OK_UNBIND = 3;
    private String URL_List_Device = "http://120.55.171.72/points.asp?";//获取设备列表
    private String URL_ID_USERS = "http://120.55.171.72/userlist.asp?pid=";
    private HorizontalScrollView hs_usermanagerfg_listdevice;//当前设备选择
    private ListView lv_usermanagerfg_users;//id设备用户列表
    private TextView tv_usermanagerfg_id;//当前设备id
    private TextView tv_usermanagerfg_zerodevice;
    private Button btn_usermanagerfg_cancel;//删除设备按键

    private ArrayList<String> data = new ArrayList<String>();//放绑定设备的列表
    private List<Button> list_btn = new ArrayList<Button>();//放按键
    private List<Map<String,String>> list_user = new ArrayList<Map<String,String>>();//选择设备后显示用户列表
    private MyAdapter adapter = new MyAdapter();
    LinearLayout hs_ll;
    private View.OnClickListener btn_listener;

    private String ID = "";
    private boolean isfist = true;
    private int which_btn = -1;
    private int last_btn = -1;
    private boolean canUnbind = true;//是否让解绑按键可操作，未选择列表时不可以操作
    private int which_user = -1;//解绑列表设备下标
    private String NAME;
    private String CHECKCODE;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == HTTP_OK_ID_USERS){
                String message = (String) msg.obj;
                //Log.e("UserManager","httpMessage:"+message);
                try {
                    list_user.clear();
                    JSONArray ja = new JSONArray(message);
                    JSONObject obj = new JSONObject(ja.get(0).toString());//拿第一条数据看是有自己主用户
                    if (obj.getString("username").equals(NAME)){//自己是主用户
                        for (int i = 0;i < ja.length();i++){
                            JSONObject jo = new JSONObject(ja.get(i).toString());
                            Map<String,String> map = new HashMap<String, String>();
                            map.put("username",jo.getString("username"));
                            map.put("tel",jo.getString("tel"));
                            list_user.add(map);
                        }
                    }else {//自己不是主用户,只能看自己的信息
                        for (int j = 0; j < ja.length();j++){
                            JSONObject jo = new JSONObject(ja.get(j).toString());
                            if (jo.getString("username").equals(NAME)){
                                Map<String,String> map = new HashMap<String,String>();
                                map.put("username",jo.getString("username"));
                                map.put("tel",jo.getString("tel"));
                                list_user.add(map);
                            }
                        }
                    }
                    if (isfist){
                        lv_usermanagerfg_users.setAdapter(adapter);//第一次要设置适配器
                        isfist = false;
                    }else {
                        adapter.notifyDataSetChanged();//数据改变
                        //adapter.notifyDataSetInvalidated();
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            else if (msg.what == HTTP_OK_DEVICE_LIST){
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
                    allBindDeviceListener();
                    createHorizotnalScrollChildView();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else if (msg.what == HTTP_OK_UNBIND){
                String message  = (String) msg.obj;
                if (message.equals("1")){
                    list_user.remove(which_user);
                    adapter.notifyDataSetChanged();
                    btn_usermanagerfg_cancel.setEnabled(false);
                    canUnbind = true;
                    adapter.setSelectedPosition(-1);
                }else {
                    Toast.makeText(getContext(),"服务器出错，请再次删除",Toast.LENGTH_LONG).show();
                }
            }
        }
    };
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_usermanager,container,false);

        Bundle bundle = getArguments();
        NAME = bundle.getString("NAME");
        CHECKCODE = bundle.getString("CHECKCODE");
        //请求获取绑定设备列表
        URL_List_Device += "username="+NAME+"&checksn="+CHECKCODE;
        findView();
        new HttpGet(mHandler,URL_List_Device,HTTP_OK_DEVICE_LIST);//发送http获取绑定设备list列表
        return v;
    }

    private void findView() {
        hs_usermanagerfg_listdevice = (HorizontalScrollView) v.findViewById(R.id.hs_usermanagerfg_listdevice);
        lv_usermanagerfg_users = (ListView) v.findViewById(R.id.lv_usermanagerfg_users);
        tv_usermanagerfg_id = (TextView) v.findViewById(R.id.tv_usermanagerfg_id);
        tv_usermanagerfg_zerodevice = (TextView) v.findViewById(R.id.tv_usermanagerfg_zerodevice);
        btn_usermanagerfg_cancel = (Button) v.findViewById(R.id.btn_usermanagerfg_cancel);

        lv_usermanagerfg_users.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (position > 0){//有次设备
                    if (canUnbind){
                        canUnbind = false;
                        btn_usermanagerfg_cancel.setEnabled(true);
                    }
                    which_user = position;//哪个设备可以删除
                    adapter.setSelectedPosition(position);
                    adapter.notifyDataSetChanged();
                }

            }
        });

        btn_usermanagerfg_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unBindUsers();
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
            hs_usermanagerfg_listdevice.addView(hs_ll);
        }else {
            //tv_usermanagerfg_zerodevice.setText("无设备可选");
            tv_usermanagerfg_zerodevice.setVisibility(View.VISIBLE);
        }
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
                tv_usermanagerfg_id.setText(id);
                ID = id;
                if (last_btn >= 0){//上一个选中的恢复
                    list_btn.get(last_btn).setBackgroundResource(R.drawable.hs_btn_unpressed);
                }
                btn.setBackgroundResource(R.drawable.hs_btn_pressed);//选中本次的
                new HttpGet(mHandler,URL_ID_USERS+ID,HTTP_OK_ID_USERS);
            }
        };
    }
    //删除用户列表
    private void unBindUsers(){
        new AlertDialog.Builder(getActivity())
                .setTitle("删除用户")
                .setMessage("确认删除该用户？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {//确认要删除了
                       String name = list_user.get(which_user).get("username");//
                       String url = "http://120.55.171.72/unreg.asp?pid="+ID+"&username="+name;
                       new HttpGet(mHandler,url,HTTP_OK_UNBIND);

                    }
                })
                .setNegativeButton("取消",null)
                .show();
    }

    class MyAdapter extends BaseAdapter {
        private int selectedPosition = -1;
        @Override
        public int getCount() {
            return list_user.size();
        }

        @Override
        public Object getItem(int position) {
            return list_user.get(position);
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

            View view = getActivity().getLayoutInflater().inflate(R.layout.lvitem_id_users,null);

            TextView tv_num = (TextView) view.findViewById(R.id.tv_lvusers_num);
            TextView tv_name = (TextView) view.findViewById(R.id.tv_lvusers_name);
            TextView tv_tel = (TextView) view.findViewById(R.id.tv_lvusers_tel);
            tv_num.setText(position+1+"");
            Map<String,String> map = list_user.get(position);
            tv_name.setText(map.get("username").toString());
            tv_tel.setText(map.get("tel").toString());
            if (position == 0) view.setBackgroundResource(R.drawable.table_row_disable);
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
