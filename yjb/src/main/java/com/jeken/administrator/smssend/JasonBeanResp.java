package com.jeken.administrator.smssend;

/**
 * Created by Administrator on 2016-11-17.
 */

public class JasonBeanResp {
    //{"resp":{"respCode":"000000","templateSMS":{"createDate":"20140820145658","smsId":"d2c49329f363b802fb3531d9c67b54f8"}}}
    private Resp resp;

    public Resp getResp() {
        return resp;
    }

    public void setResp(Resp resp) {
        this.resp = resp;
    }
}
class Resp {
    private String respCode;
    private TemplateSMS templateSMS;

    public String getRespCode() {
        return respCode;
    }

    public void setRespCode(String respCode) {
        this.respCode = respCode;
    }

    public TemplateSMS getTemplateSMS() {
        return templateSMS;
    }

    public void setTemplateSMS(TemplateSMS templateSMS) {
        this.templateSMS = templateSMS;
    }
}
class TemplateSMS{
    private String createDate;
    private String smsId;

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getSmsId() {
        return smsId;
    }

    public void setSmsId(String smsId) {
        this.smsId = smsId;
    }
}