package com.dabai.FileClear;

import android.app.*;
import android.os.*;
import android.support.annotation.RequiresApi;
import android.widget.*;

import android.view.*;
import android.content.*;
import android.net.*;



public class AboutActivity extends Activity
{

    TextView te1,te2,te3;
    ImageView im;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // TODO: Implement this method
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        im = (ImageView)findViewById(R.id.aboutImageView1);
        te1 = (TextView)findViewById(R.id.aboutTextView1);
        te2 = (TextView)findViewById(R.id.aboutTextView2);
        te3 = (TextView)findViewById(R.id.aboutTextView3);


        im.setImageDrawable(getApplicationContext().getDrawable(R.drawable.ic_launcher_foreground));
        te1.setText(getString(R.string.app_name));
        te2.setText("一款可以帮助你快速删除标记的目录及文件");
        te3.setText(new DabaiUtils().getVersionName(getApplicationContext()));

    }



    public void jz(View v){


       if (new DabaiUtils().isalipayClientAvailable(this)){


        Intent intent = new Intent();
//Intent intent = new Intent(Intent.ACTION_VIEW,uri);
        intent.setAction("android.intent.action.VIEW");
        //支付宝二维码解析
        Uri content_url = Uri.parse("alipayqr://platformapi/startapp?saId=10000007&qrcode=https://qr.alipay.com/fkx01035mwddyzyt49elx09");
        intent.setData(content_url);
        startActivity(intent);

        new DabaiUtils().toast(getApplicationContext(),"谢谢老板！");


    }else
       {
           new DabaiUtils().toast(getApplicationContext(),"你首先要有支付宝，才能进行捐赠哦~");
       }



}


    public void bzwd(View view) {
        startActivity(new Intent(this, HelpActivity.class));

    }
}

