package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.provider.MediaStore;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import lee.suk.cameracrop.R;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    private TextView fromTv;
    private TextView contentTv;

    private IntentFilter intentFilter;
    private MessageReceiver messageReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        getSms();
    }

    private void getSms() {
        intentFilter = new IntentFilter();                 intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        messageReceiver = new MessageReceiver();
        //设置较高的优先级
        intentFilter.setPriority(100);
        registerReceiver(messageReceiver, intentFilter);
    }

    private void initView() {
        fromTv = (TextView) findViewById(R.id.sms_from_txt);
        contentTv = (TextView) findViewById(R.id.sms_content_txt);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(messageReceiver);
    }

    class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            //提取短信消息
            Object[] pdus = (Object[]) bundle.get("pdus");
            SmsMessage[] messages = new SmsMessage[pdus.length];
            for (int i = 0; i < messages.length; i++) {
                messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
            }
            //获取发送方号码
            String address = messages[0].getOriginatingAddress();

            String fullMessage = "";
            for (SmsMessage message : messages) {
                //获取短信内容
                fullMessage += message.getMessageBody();
            }
            //截断广播,阻止其继续被Android自带的短信程序接收到
            abortBroadcast();
            fromTv.setText(address);
            contentTv.setText(fullMessage);
        }
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            ((Button) findViewById(R.id.takephoto)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    startActivityForResult(intent, 1);
                }
            });
        }


        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            if (resultCode == Activity.RESULT_OK){
                String sdStatus = Environment.getExternalStorageState();

                if(!sdStatus.equals(Environment.MEDIA_MOUNTED)){
                    System.out.println(" ------------- sd card is not avaiable ---------------");
                    return;
                }


                String name = "photo.jpg";

                Bundle bundle = data.getExtras();
                Bitmap bitmap = (Bitmap) bundle.get("data");
                String fileName = "sdcard"+"/"+name;

                FileOutputStream fos =null;

                try {
                    System.out.println(fileName);
                    fos=new FileOutputStream(fileName);
                    bitmap.compress(Bitmap.CompressFormat.JPEG,100,fos);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }finally {
                    try {
                        fos.flush();
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                ((ImageView) findViewById(R.id.picture)).setImageBitmap(bitmap);
            }
        }
    }
    private Button btn_2;
    private ImageView iv_image;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(this.getClass().getName(), "onCreate");
        setContentView(R.layout.activity_main);
        btn_2 = findViewById(R.id.btn_2);
        iv_image = findViewById(R.id.iv_image);
        btn_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, null);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, 2);
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2) {
            // 从相册返回的数据
            Log.e(this.getClass().getName(), "Result:" + data.toString());
            if (data != null) {
                // 得到图片的全路径
                Uri uri = data.getData();
                iv_image.setImageURI(uri);
                Log.e(this.getClass().getName(), "Uri:" + String.valueOf(uri));
            }
        }
    }
}
