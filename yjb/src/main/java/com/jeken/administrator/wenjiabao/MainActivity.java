package com.jeken.administrator.wenjiabao;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jude.rollviewpager.RollPagerView;
import com.jude.rollviewpager.adapter.LoopPagerAdapter;
import com.jude.rollviewpager.hintview.ColorPointHintView;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private RollPagerView rp_main_banner;
    private TextView tv_main_water,tv_main_security,tv_main_video,tv_main_train;
    private TextView tv_main_condition,tv_main_tv,tv_main_led,tv_main_air;

    private String NAME;
    private String PWD;
    private String CHECKCODE;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        NAME = intent.getStringExtra("NAME");
        PWD = intent.getStringExtra("PWD");
        CHECKCODE = intent.getStringExtra("CHECKCODE");
        //测试用
        //Toast.makeText(this,"name="+NAME+"\npasswd="+PWD+"\ncheckcode="+CHECKCODE,Toast.LENGTH_SHORT).show();
        findView();

    }

    private void findView() {
        //banner
        rp_main_banner = (RollPagerView) findViewById(R.id.rp_main_banner);
        rp_main_banner.setPlayDelay(2500);
        rp_main_banner.setAnimationDurtion(500);
        rp_main_banner.setAdapter(new TestLoopAdapter(rp_main_banner));
        rp_main_banner.setHintView(new ColorPointHintView(this, Color.WHITE,Color.GRAY));

        //tv
        tv_main_water = (TextView) findViewById(R.id.tv_main_water);
        tv_main_security = (TextView) findViewById(R.id.tv_main_security);
        tv_main_video = (TextView) findViewById(R.id.tv_main_vido);
        tv_main_train = (TextView) findViewById(R.id.tv_main_train);
        tv_main_condition = (TextView) findViewById(R.id.tv_main_condition);
        tv_main_tv = (TextView) findViewById(R.id.tv_main_tv);
        tv_main_led = (TextView) findViewById(R.id.tv_main_led);
        tv_main_air = (TextView) findViewById(R.id.tv_main_air);
        //setOnClickListener
        tv_main_water.setOnClickListener(this);
        tv_main_security.setOnClickListener(this);
        tv_main_video.setOnClickListener(this);
        tv_main_train.setOnClickListener(this);
        tv_main_condition.setOnClickListener(this);
        tv_main_tv.setOnClickListener(this);
        tv_main_led.setOnClickListener(this);
        tv_main_air.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (v == tv_main_water){

            Intent intent = new Intent(this,WaterActivity.class);
            intent.putExtra("NAME",NAME);
            intent.putExtra("PWD",PWD);
            intent.putExtra("CHECKCODE",CHECKCODE);
            startActivity(intent);
        }else {
            Toast.makeText(this,"开发中，敬请期待吧....",Toast.LENGTH_SHORT).show();
        }
    }


    private class TestLoopAdapter extends LoopPagerAdapter
    {
        private int[] imgs = {R.drawable.banner1,R.drawable.banner2,R.drawable.banner3,R.drawable.banner4};  // 本地图片
        private int count = imgs.length; // banner上图片的数量

        public TestLoopAdapter(RollPagerView viewPager)
        {
            super(viewPager);
        }

        @Override
        public View getView(ViewGroup container, int position)
        {
            //final int picNo = position + 1;
            ImageView view = new ImageView(container.getContext());
            view.setImageResource(imgs[position]);
            view.setScaleType(ImageView.ScaleType.CENTER_CROP);
            view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));


            return view;
        }

        @Override
        public int getRealCount()
        {
            return count;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //销毁前停止服务
        //stopService(new Intent(this,UDPService.class));
    }
}
