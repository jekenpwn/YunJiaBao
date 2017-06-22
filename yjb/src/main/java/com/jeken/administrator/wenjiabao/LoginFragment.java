package com.jeken.administrator.wenjiabao;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jeken.administrator.nethandle.HttpGet;

/**
 * Created by Administrator on 2016-11-09.
 */

public class LoginFragment extends Fragment {

    private View v;
    private final int HTTP_OK = 1;
    private EditText et_login_username;
    private EditText et_login_passwd;
    private TextView tv_login_error;
    private Button btn_login;

    private String URL = "http://114.215.237.170/login.asp?";

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {

            if (msg.what == HTTP_OK){
                String message = (String) msg.obj;//拿数据
                Log.e("TAG","login:"+message);
                if(message.equals("err")){//密码错误
                    tv_login_error.setText("请正确输入帐号密码");
                }else{//登录正确
                    Toast.makeText(getContext(),"登录成功",Toast.LENGTH_SHORT).show();

                }
            }
        }
    };
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_login,container,false);
        findView();
        return v;
    }

    private void findView() {
        et_login_username= (EditText) v.findViewById(R.id.et_login_username);
        et_login_passwd = (EditText) v.findViewById(R.id.et_login_passwd);
        tv_login_error = (TextView) v.findViewById(R.id.tv_login_error);
        btn_login = (Button) v.findViewById(R.id.btn_login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击登录去除错误提醒
                tv_login_error.setText("");
                //获得登录信息
                String username = et_login_username.getText().toString();
                String passwd = et_login_passwd.getText().toString();
                if(checkLoginInfo( username,passwd)){//带着目标设备的ip和port跟着intent传递到Home
                    String url = URL+"username="+username+"&password="+passwd;
                    new HttpGet(mHandler,url,HTTP_OK);
                    //new HttpPost("http://114.215.237.170/login.asp",url,mHandler,HTTP_OK);
                    //checkLoginHttp(username,passwd);//此时请求服务器登录了
                    //先跳，等接口在开发登录界面
                    //Intent intent = new Intent(this,HomeActivity.class);
                    //startActivity(intent);
                }
            }
        });
    }



    /**
     * 检测输入是否合法再请求，以防用户过度错误登录请求服务器
     * @param username
     * @param passwd
     * @return
     */
    public boolean checkLoginInfo( String username,String passwd){
        if (username.equals("")){
            tv_login_error.setText("帐号不能为空");
            //Toast.makeText(this,"帐号不能为空",Toast.LENGTH_SHORT).show();
            return false;
        }else if (passwd.equals("")){
            tv_login_error.setText("密码不能空");
            //Toast.makeText(this,"密码不能空",Toast.LENGTH_SHORT).show();
            return false;
        }//http请求登录信息是否正确
        return true;
    }


}
