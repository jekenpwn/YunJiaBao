package com.jeken.administrator.smssend;

import android.os.Handler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Created by Administrator on 2016-11-16.
 */

public class SmsSend {
    private final int SMS_YES = 2;
    private final int SMS_NO = 3;
    private final int SMS_OUT = 4;
    //验证码
    private String CODE;
    //短信平台 Account sid
    public final static String SMS_SID = "9716bae35fbe11322745beb477c42aa9";
    //短信平台 Auth Token
    public final static String SMS_TOKEN = "bddd47a6c199136f449862a2e7901a4a";
    //短信平台 SMS_APPID
    public final static String SMS_APPID = "2ad210d8a6894d738228c2bb706e20c1";
    // 短信模板ID SMS_TEMPLATEID
    private String SMS_TEMPLATEID;
    //手机号码
    private String PHONE;
    private Handler mHandler;
    public SmsSend(String templaId,String code,String phone,Handler mHandler){
        this.SMS_TEMPLATEID = templaId;
        this.CODE = code;
        this.PHONE = phone;
        this.mHandler = mHandler;
    }

    public void handSms(){
        new Thread(new Runnable() {
            public void run() {
                JsonReqClient client = new JsonReqClient();
                String result = client.sendVerificationCode(SMS_APPID, SMS_SID,SMS_TOKEN, CODE, PHONE, SMS_TEMPLATEID);
                //{"resp":{"respCode":"000000","templateSMS":{"createDate":"20140820145658","smsId":"d2c49329f363b802fb3531d9c67b54f8"}}}
                if (result != null && result.length() > 0) {
                     Gson gson = new Gson();
                     JasonBeanResp jason = gson.fromJson(result,new TypeToken<JasonBeanResp>(){}.getType());
                     String resoCode = jason.getResp().getRespCode();
                    if (resoCode.equals("000000")){
                         mHandler.sendEmptyMessage(SMS_YES);
                    }
                }else {
                    mHandler.sendEmptyMessage(SMS_NO);
                }
            }
        }).start();
    }
}
