package com.jeken.administrator.wenjiabao;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jeken.administrator.nethandle.HttpGet;
import com.jeken.administrator.smssend.SmsSend;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RegisterActivity extends AppCompatActivity {

    private final int HTTP_0K = 1;
    private final int SMS_YES = 2;
    private final int SMS_NO = 3;
    private final int DELAY_BTN = 10;
    private String smsCode;
    private final String URL = "http://120.55.171.72/adduser.asp?";//"http://114.215.237.170/adduser.asp?";
    private final String SMS_TEMPLATEID = "32494";
    //注册的所有EditText
    private EditText et_register_username,
            et_register_passwd,et_register_confirm,
            et_register_phone,et_register_check;
    private TextView tv_register_error;

    private Button btn_register_submit,btn_register_check;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == HTTP_0K){
                String message = (String) msg.obj;
                if (message.equals("1")){
                    tv_register_error.setText("注册成功！！");
                    mHandler.sendEmptyMessageDelayed(4,1000);
                }else if (message.equals("3")){
                    tv_register_error.setText("用户名已经被注册");
                }else if (message.equals("4")){
                    tv_register_error.setText("手机号码已经被注册");
                }else if (message.equals("2")){
                    tv_register_error.setText("注册失败，重新注册");
                }else {
                    tv_register_error.setText("网络错误");
                }
            }else if (msg.what == SMS_YES){
                Toast.makeText(RegisterActivity.this,"验证码发送成功，请留意短信",Toast.LENGTH_SHORT).show();
                countdown();//延时10s后再让用户重新发送
            }else if (msg.what == SMS_NO){
                tv_register_error.setText("验证码发送失败");
            }else if (msg.what == DELAY_BTN){
                String str = (String) msg.obj;
                btn_register_check.setText(str);
            }else if (msg.what == 4){
                startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        findView();
    }

    private void findView() {
        //注册的所有EditText初始化对象
        et_register_username = (EditText) findViewById(R.id.et_register_username);
        et_register_passwd = (EditText) findViewById(R.id.et_register_passwd);
        et_register_confirm = (EditText) findViewById(R.id.et_register_confirm);
        et_register_phone = (EditText) findViewById(R.id.et_register_phone);
        et_register_check = (EditText) findViewById(R.id.et_register_check);

        tv_register_error = (TextView) findViewById(R.id.tv_register_error);

        btn_register_submit = (Button) findViewById(R.id.btn_register_submit);
        btn_register_check = (Button) findViewById(R.id.btn_register_check);
    }

    /**
     * 发送随机生成的6位验证码到云之迅平台
     * @param v
     */
    public void smsListener(View v){
        String phone = et_register_phone.getText().toString();
        if(isPhoneNum(phone)){
            smsCode = (int) ((Math.random() * 9 + 1) * 100000)+"";
            SmsSend sms = new SmsSend(SMS_TEMPLATEID,smsCode,phone,mHandler);
            sms.handSms();
        }else {
            tv_register_error.setText("手机号码格式不对");
        }
    }
    /**
     * 确认注册上交清单到服务器
     * 在此之前：1、检查密码是否匹配，不匹配重新输入
     *           2、检查是否漏填信息，有漏填重新输入
     *           3、http请求查询是否用户存在，存在重新输入
     *           4、http请求手机号码是否已经被注册了
     * @param v
     */
    public void submitListener(View v){

        //注册按键按下就清空错误信息
        tv_register_error.setText("");
        //获得输入的注册信息
        String username = et_register_username.getText().toString();
        String passwd = et_register_passwd.getText().toString();
        String confirm = et_register_confirm.getText().toString();
        String phone = et_register_phone.getText().toString();
        String check = et_register_check.getText().toString();

        String smsMessage = et_register_check.getText().toString();
        if (checkRegisterInfo(username,passwd,confirm,phone,check)){
            if(smsMessage.equals(smsCode)){
                // checkRegisterHttp(username,id,passwd);
                String url = URL+"tel="+phone+"&username="+username+"&password="+passwd;
                Log.e("TAG","register:url="+url);//调试信息，确认app无bug后注释，防止泄漏信息
                new HttpGet(mHandler,url,HTTP_0K);
            }else {
                tv_register_error.setText("验证码错误");
            }

        }
        //if()
    }



    /**
     * app端验证输入信息
     * @param username
     * @param passwd
     * @param confirm
     * @return
     */
    public boolean checkRegisterInfo(String username,String passwd,String confirm,String phone,String check){
        if (username.equals("")){
            tv_register_error.setText("帐号不能空");
            //Toast.makeText(this,,Toast.LENGTH_SHORT).show();
            return false;
        }else if (passwd.equals("")){
            tv_register_error.setText("密码不能空");
            //Toast.makeText(this,"密码不能空",Toast.LENGTH_SHORT).show();
            return false;
        }else if (confirm.equals("")|| !passwd.equals(confirm) ){
            tv_register_error.setText("密码不匹配");
            //Toast.makeText(this,"密码不匹配",Toast.LENGTH_SHORT).show();
            return false;
        }else if(phone.equals("")){
            tv_register_error.setText("手机号码不能空");
            return false;
        }else if(check.equals("")){
            tv_register_error.setText("验证码不能为空");
        }
        //Toast.makeText(this,"注册成功",Toast.LENGTH_SHORT).show();
        return true;
    }

    public boolean isPhoneNum(String phone){
        //手机验证正则"^((1[3,5,8][0-9])|(14[5,7])|(17[0,6,7,8]))//d{8}$"
        boolean isValid = false;
        CharSequence inputStr = phone;
        Pattern p = Pattern.compile("^1[34578]\\d{9}$");
        Matcher m = p.matcher(inputStr);
        if(m.matches()) {
            isValid = true;
        }
        return isValid;
    }

    /**
     * 延时10s让用后再次发送验证码
     */
    public void countdown(){
        btn_register_check.setClickable(false);
        new Thread(){
            @Override
            public void run() {
                for (int i=10;i>0;i--){
                    mHandler.obtainMessage(DELAY_BTN,i+"s后重新发送").sendToTarget();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                mHandler.obtainMessage(DELAY_BTN,"重新发送").sendToTarget();
            }
        }.start();
        btn_register_check.setClickable(true);
    }
}
