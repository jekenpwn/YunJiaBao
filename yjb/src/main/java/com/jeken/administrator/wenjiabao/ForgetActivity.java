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

public class ForgetActivity extends AppCompatActivity {

    private final int HTTP_0K = 1;
    private final int SMS_YES = 2;
    private final int SMS_NO = 3;

    private final int DELAY_BTN = 10;
    private String smsCode;
    private final String SMS_TEMPLATEID = "32496";
    private final String URL = "http://120.55.171.72/repassword.asp?";//"http://114.215.237.170/edituser.asp?";

    private TextView tv_forget_error;
    private EditText et_forget_username,et_forget_newpasswd,
            et_forget_confirm,et_forget_phone,et_forget_check;
    private Button btn_forget_change,btn_forget_check;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == HTTP_0K){
                String message = (String) msg.obj;
                if (message.equals("1")){
                    tv_forget_error.setText("修改成功！！");
                    mHandler.sendEmptyMessageDelayed(4,1000);
                }else if (message.equals("0")){
                    tv_forget_error.setText("修改失败，请确认输入信息");
                }
            }else if (msg.what == SMS_YES){
                Toast.makeText(ForgetActivity.this,"验证码发送成功，请留意短信",Toast.LENGTH_SHORT).show();
                countdown();//延时10s后再让用户重新发送
            }else if (msg.what == SMS_NO){
                tv_forget_error.setText("验证码发送失败");
            }else if (msg.what == DELAY_BTN){
                String str = (String) msg.obj;
                btn_forget_check.setText(str);
            }else if(msg.what == 4){
                startActivity(new Intent(ForgetActivity.this,LoginActivity.class));
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget);

        findView();
    }

    private void findView() {

        tv_forget_error = (TextView) findViewById(R.id.tv_forget_error);
        et_forget_username = (EditText) findViewById(R.id.et_forget_username);
        et_forget_newpasswd = (EditText) findViewById(R.id.et_forget_newpasswd);
        et_forget_confirm = (EditText) findViewById(R.id.et_forget_confirm);
        et_forget_phone = (EditText) findViewById(R.id.et_forget_phone);
        et_forget_check = (EditText) findViewById(R.id.et_forget_check);


        btn_forget_change = (Button) findViewById(R.id.btn_forget_change);
        btn_forget_check = (Button) findViewById(R.id.btn_forget_check);
    }

    /**
     * 提交修改信息按键
     * @param v
     */
    public void submitListener(View v){
        //清除提示信息
        tv_forget_error.setText("");
        String username = et_forget_username.getText().toString();
        String passwd = et_forget_newpasswd.getText().toString();
        String confirm = et_forget_confirm.getText().toString();
        String phone = et_forget_phone.getText().toString();
        String check = et_forget_check.getText().toString();
        if (checkInput(username,passwd,confirm,phone,check)){
            if(check.equals(smsCode)){
                // checkRegisterHttp(username,id,passwd);
                //请求发送修改密码
                String url = URL+"tel="+phone+"&username="+username+"&password="+passwd;
                Log.e("TAG","Forget:url"+url);//调试测试用，无bug注释
                new HttpGet(mHandler,url,HTTP_0K);
                //Toast.makeText(this,"注册成功",Toast.LENGTH_SHORT).show();
            }else {
                tv_forget_error.setText("验证码错误");
            }

        }
    }

    /**
     * 短息验证过才让发送修改密码
     * @param v
     */
    public void checkListener(View v){
        String phone = et_forget_phone.getText().toString();
        if(isPhoneNum(phone)){
            smsCode = (int) ((Math.random() * 9 + 1) * 100000)+"";
            SmsSend sms = new SmsSend(SMS_TEMPLATEID,smsCode,phone,mHandler);
            sms.handSms();
        }else {
            tv_forget_error.setText("手机号码格式不对");
        }
    }

    public boolean  checkInput(String username,String newpasswd,String confirm,String phone,String check){
        if (username.equals("")){
            tv_forget_error.setText("修改用户帐号不能为空");
            return false;
        }else if (newpasswd.equals("")){
            tv_forget_error.setText("修改密码不能为空");
            return false;
        }else if (!confirm.equals(newpasswd)){
            tv_forget_error.setText("密码不匹配");
            return false;
        }else if (phone.equals("")){
            tv_forget_error.setText("手机不能为空");
            return false;
        }else if(check.equals("")){
            tv_forget_error.setText("验证码不能为空");
            return false;
        }
        return true;

    }

    /**
     * 验证手机号码是否合法，减少服务器负担
     * @param phone
     * @return
     */
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
        btn_forget_check.setClickable(false);
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
        btn_forget_check.setClickable(true);
    }

}
