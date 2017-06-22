package com.jeken.administrator.nethandle;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

/**
 * Created by Administrator on 2016-11-20.
 */

public class UseWaterTime {
    private String json;
    private JSONArray array;
    boolean on_flag = false;
    boolean off_flag = false;
    DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    long on_time = 0;
    long off_time = 0;
    long sum_time = 0;
    long on_curtime = 0;
    long start_end = 0;
    int on_add = 0;
    public UseWaterTime(String json){
        this.json = json;
        try {
            array = new JSONArray(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public boolean haveData(){
        if (array.length() == 0){
            return false;
        }else {
            return true;
        }
    }
    public  long caluTime(){
        if (haveData()){
            int len = array.length();

            Log.e("TAG","jsonlen:"+len);
            for (int i = 0;i < len;i++){
                try {
                    JSONObject oj = new JSONObject(array.get(i).toString());
                    if (oj.getString("state").equals("1")){
                        off_flag = true;
                        off_time = df.parse(oj.getString("time")).getTime();
                    }else if (oj.getString("state").equals("0")){
                        on_flag = true;
                        if (on_add > 0){
                            on_curtime = df.parse(oj.getString("time")).getTime();
                            on_add++;
                        }else {
                            on_time = df.parse(oj.getString("time")).getTime();
                            on_add++;
                        }
                    }
                    if (on_flag&&off_flag){
                        if (off_time > on_time){
                            sum_time += (off_time - on_time);
                        }else {
                            sum_time += (on_time - off_time);
                        }
                        on_flag = off_flag = false;
                        on_add = 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("TAG","统计用水时间异常");
                }

            }

            if (on_flag&&off_flag){
                if (off_time > on_time){
                    sum_time += (off_time - on_time);
                }else {
                    sum_time += (on_time - off_time);
                }
                on_flag = off_flag = false;
                on_add = 0;
            }
            if (on_add > 0){
                sum_time += (on_curtime-on_time);
            }
            //最后是开的情况，加上到现在的时间
            try {
                JSONObject oj = new JSONObject(array.get(len-1).toString());
                if (oj.getString("state").equals("0")){
                    long enddtime  =  df.parse(oj.getString("time")).getTime();
                    long duringtime = System.currentTimeMillis()-enddtime;
                    sum_time += duringtime;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            //统计使用率用，先不用保留。
            try {
                JSONObject jo = new JSONObject(array.get(len-1).toString());
                start_end =  df.parse(jo.getString("time")).getTime();
                jo = new JSONObject(array.get(0).toString());
                start_end = start_end-(df.parse(jo.getString("time")).getTime());
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("TAG","统计用水时间段出错");
            }
        }
        return sum_time;
    }

    /**
     * 使用率计算
     * @return
     */
    public String useState(){
        String state = new DecimalFormat("0.00000").format((sum_time/start_end)*100);
        if (state.equals("0.00000")){
            return "使用率为：使用频率基本为0";
        }else {
            return "使用率为："+ state+"%";
        }
    }

    /**
     * 毫秒转换为更直观数据
     * @param ms
     * @return
     */
    public String formatTime(long ms) {
        int ss = 1000;
        int mi = ss * 60;
        int hh = mi * 60;
        int dd = hh * 24;

        long day = ms / dd;
        long hour = (ms - day * dd) / hh;
        long minute = (ms - day * dd - hour * hh) / mi;
        long second = (ms - day * dd - hour * hh - minute * mi) / ss;
        long milliSecond = ms - day * dd - hour * hh - minute * mi - second * ss;

        StringBuffer sb = new StringBuffer();
        if(day > 0) {
            sb.append(day+"天");
        }
        if(hour > 0) {
            sb.append(hour+"小时");
        }
        if(minute > 0) {
            sb.append(minute+"分");
        }
        if(second > 0) {
            sb.append(second+"秒");
        }
        if(milliSecond > 0) {
            sb.append(milliSecond+"毫秒");
        }
        if (ms == 0){
            sb.append("没有使用");
        }
        return sb.toString();
    }

}
