package com.jeken.administrator.wenjiabao;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jeken.administrator.nethandle.HttpGet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DataRearchActivity extends AppCompatActivity {

    private final int HTTP_OK = 1;
    private String URL;
    private ListView lv_data;
    private JSONArray ja;
    private MyAdapter adapter = new MyAdapter();
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == HTTP_OK){
                String json = (String) msg.obj;
                //Log.e("TAG",json);
                try {
                    ja = new JSONArray(json);
                    lv_data.setAdapter(adapter);
//                    String tmp = ja.get(0).toString();
//                    Toast.makeText(DataRearchActivity.this,tmp,Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("TAG","查询来水停水记录异常，最外防御出错");
                    Toast.makeText(DataRearchActivity.this,"服务器出错",Toast.LENGTH_LONG).show();
                }

            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_rearch);
        URL = getIntent().getStringExtra("URL");
        //Toast.makeText(this,URL,Toast.LENGTH_LONG).show();
        findView();

    }

    private void findView() {
         lv_data = (ListView) findViewById(R.id.lv_data);
         new HttpGet(mHandler,URL,HTTP_OK);
    }

    class MyAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return ja.length();
        }

        @Override
        public Object getItem(int position) {
            JSONObject jo = null;
            try {
                jo = new JSONObject(ja.get(position).toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jo;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null){
                convertView = View.inflate(DataRearchActivity.this,R.layout.item_listview_request,null);
            }

            TextView item_lv_num = (TextView) convertView.findViewById(R.id.item_lv_num);
            TextView item_lv_id = (TextView) convertView.findViewById(R.id.item_lv_id);
            TextView item_lv_state = (TextView) convertView.findViewById(R.id.item_lv_state);
            TextView item_lv_time = (TextView) convertView.findViewById(R.id.item_lv_time);
            String state = "";
            try {
                JSONObject jo = new JSONObject(ja.get(position).toString());
                item_lv_num.setText(jo.getString("num"));
                item_lv_time.setText(jo.getString("time"));
                String id = jo.getString("id");
                int len_id = id.length();
                if (len_id < 8){
                    for(int j = 0;j<(8-len_id);j++)
                        id = "0" + id;
                }
                item_lv_id.setText(id);
                state = jo.getString("state");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (state.equals("0")){
                item_lv_state.setText("开");
            }else if(state.equals("1")) {
                item_lv_state.setText("关");
            }else if (state.equals("2")){
                item_lv_state.setText("电磁阀通电");
            }else if (state.equals("3")){
                item_lv_state.setText("电磁阀断电");
            }
            return convertView;
        }
    }
}
