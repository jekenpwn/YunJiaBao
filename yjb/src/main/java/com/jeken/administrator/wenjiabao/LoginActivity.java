package com.jeken.administrator.wenjiabao;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.jeken.administrator.nethandle.HttpGet;

public class LoginActivity extends AppCompatActivity {

    private final int HTTP_OK = 1;
    private EditText et_login_username;
    private EditText et_login_passwd;
    private TextView tv_login_error;
    private SharedPreferences sp;
    private CheckBox cb_login_acount,cb_login_pwd;
    private String URL = "http://120.55.171.72/login.asp?";//"http://114.215.237.170/login.asp?";

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {

            if (msg.what == HTTP_OK){
                String message = (String) msg.obj;//拿数据
                if(message.equals("0")){//登录信息错误
                    tv_login_error.setText("请正确输入帐号密码");
                }else if (!message.equals("")){//登录正确
                    Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                    //带着ID一起传过去
                    //intent.putExtra("ID",message);
                    //携带正确的账户密码给Home
                    intent.putExtra("NAME",et_login_username.getText().toString());
                    intent.putExtra("PWD",et_login_passwd.getText().toString());
                    intent.putExtra("CHECKCODE",message);//验证码
                    startActivity(intent);
                }else{
                    Log.e("TAG","login:"+message);//测试用
                    tv_login_error.setText("服务器出错！");
                }
                //登录成功后，要检查是否记住帐号密码，有就保存
                if (cb_login_acount.isChecked()){
                    sp.edit().putString("remeberuser",et_login_username.getText().toString()).commit();
                }
                if (cb_login_pwd.isChecked()){
                    sp.edit().putString("remeberpwd",et_login_passwd.getText().toString()).commit();
                }
            }

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        sp = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        findView();

        remeberListener();
    }



    private void findView() {
        et_login_username= (EditText) findViewById(R.id.et_login_username);
        et_login_passwd = (EditText) findViewById(R.id.et_login_passwd);
        tv_login_error = (TextView) findViewById(R.id.tv_login_error);
        cb_login_acount = (CheckBox) findViewById(R.id.cb_login_acount);
        cb_login_pwd = (CheckBox) findViewById(R.id.cb_login_pwd);

        String user = sp.getString("remeberuser","");
        String pwd = sp.getString("remeberpwd","");

        boolean flag = sp.getBoolean("acount_flag",false);
        if (flag){//判断上次是否勾选了记住密码帐号
            cb_login_acount.setSelected(true);
            if (!user.equals(""))
                et_login_username.setText(user);
        }
        flag = sp.getBoolean("pwd_flag",false);
        if (flag){
            cb_login_pwd.setSelected(true);
            if (!pwd.equals(""))
                et_login_passwd.setText(pwd);
        }

    }

    private void remeberListener() {
        cb_login_acount.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    sp.edit().putBoolean("acount_flag",isChecked).commit();
                }else {
                    sp.edit().putBoolean("acount_flag",isChecked).commit();
                }
            }
        });

        cb_login_pwd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    sp.edit().putBoolean("pwd_flag",isChecked).commit();
                }else {
                    sp.edit().putBoolean("pwd_flag",isChecked).commit();
                }
            }
        });
    }

    /**
     * 对登录按键事件监听
     * @param v
     */
    public void btnLoginListener(View v){

        //点击登录去除错误提醒
        tv_login_error.setText("");
        //获得登录信息
        String username = et_login_username.getText().toString();
        String passwd = et_login_passwd.getText().toString();
        if(checkLoginInfo( username,passwd)){//带着目标设备的ip和port跟着intent传递到Home
            String url = URL+"username="+username+"&password="+passwd;
            //进行httpget请求
            HttpGet httpGet = new HttpGet(mHandler,url,HTTP_OK);
        }

    }

    /**
     * 跳转到注册页面
     * @param v
     */
    public void registerListener(View v){
        startActivity(new Intent(this,RegisterActivity.class));
    }
    /**
     * 忘记密码跳转监听
     * @param v
     */
    public void changeListener(View v){

        startActivity(new Intent(LoginActivity.this,ForgetActivity.class));
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
