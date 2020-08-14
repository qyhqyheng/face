package com.xinshi.android.face.demo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.xinshi.android.face.demo.util.DemoUtils;
import com.xinshi.android.face.exceptions.FaceException;
import com.xinshi.android.face.jni.Tool;
import com.xinshi.android.xsfacesdk.XsFaceSDK;
import com.xinshi.android.xsfacesdk.XsSdkEnvConfig;
import com.xinshi.android.xsfacesdk.activity.CameraConfigActivity;
import com.xinshi.android.xsfacesdk.helper_v3.XsFaceSDKCameraHelper;
import com.xinshi.android.xsfacesdk.helper_v3.XsFaceSDKHelper;
import com.xinshi.android.xsfacesdk.helper_v3.XsFaceSDKInitHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/***
 * Demo程序主界面
 */
public class MainActivity extends BaseActivity implements View.OnClickListener {
    static final int IDLE_TIME_LEN_SECONDS = 3;
    Button cameraConfigButton;
    Button faceRecButton, faceRegisterButton,fragmentFaceRecButton;
    Button faceLibManagerButton;
    Button paramsConfigButton;
    Button faceCollectButton, localServerButton;
    Button faceCompareButton;
    Button spedifiedFaceRecButton;
    Button usbcameraSetButton;
    Button btnPersonSync;
    Button repeatFaceRecButton;

    TextView versionTextView;
    boolean isRepeatRectRunning=false;



    private void writeLog(String log ){
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(String.format("%s/%s", XsSdkEnvConfig.getRootPath(), "repeatRecFace.log"), true);
            fileOutputStream.write(log.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isRepeatRectRunning) {
            new Handler().postDelayed(new Runnable(){
                public void run() {
                    //execute the task
                    if (!isRepeatRectRunning) {
                        return;
                    }
                    String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS ").format(new Date());
                    if (checkHashCameras()) {
                        Intent intent = new Intent(MainActivity.this, FaceRecActivity.class);
                        intent.putExtra("isRepeatRectRunning", true);
                        startActivity(intent);
                    }
                    writeLog(time+" 开始识别\n");
                }
            }, IDLE_TIME_LEN_SECONDS*1000);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraConfigButton = findViewByIdAndSetListener(R.id.camrea_config_button);
        faceRecButton = findViewByIdAndSetListener(R.id.face_rect_button);
        faceRegisterButton = findViewByIdAndSetListener(R.id.face_register_button);
        faceLibManagerButton = findViewByIdAndSetListener(R.id.facelib_manager_button);
        paramsConfigButton = findViewByIdAndSetListener(R.id.params_config_button);
        faceCollectButton = findViewByIdAndSetListener(R.id.face_collect_button);
        localServerButton = findViewByIdAndSetListener(R.id.local_server_button);
        faceCompareButton = findViewByIdAndSetListener(R.id.face_compare_button);
        spedifiedFaceRecButton = findViewByIdAndSetListener(R.id.specified_face_rec_button);
        //usbcameraSetButton = findViewByIdAndSetListener(R.id.usbcamrea_set_button);
        repeatFaceRecButton = findViewByIdAndSetListener(R.id.repeat_rec_button);
        fragmentFaceRecButton = findViewByIdAndSetListener(R.id.fragment_face_rect_button);
        btnPersonSync = findViewByIdAndSetListener(R.id.btn_person_sync);
        versionTextView = findViewById(R.id.version_textview);
        String sdkVersion = XsFaceSDK.getVersion();
        String featureVersion = this.getString(R.string.algo_feature_version);
        versionTextView.setText(String.format("sdk版本：%s\n识别算法版本：%s\ndevice id:%s", sdkVersion, featureVersion, XsFaceSDK.getDeviceID()));
    }

    boolean checkHashCameras() {
        if (XsFaceSDKCameraHelper.getAvaliableCameras().size() == 0) {
            showToast("没有可用的摄像头");
            return false;
        } else
            return true;
    }

    @Override
    public void onClick(View v) {
        if (v == usbcameraSetButton) {
            /*Intent intent = new Intent(this, UVCCameraSetActivity.class);
            startActivity(intent);*/
        } else if (v == cameraConfigButton) {
            if (checkHashCameras()) {
                Intent intent = new Intent(this, CameraConfigActivity.class);
                startActivity(intent);
            }
        } else if (v == faceRecButton) {
            if (checkHashCameras()) {
                Intent intent = new Intent(this, FaceRecActivity.class);
                startActivity(intent);
            }
        } else if (v == faceRegisterButton) {
            if (checkHashCameras()) {
                Intent intent = new Intent(this, FaceRegisterActivity.class);
                startActivity(intent);
            }
        } else if (v == faceLibManagerButton) {
            Intent intent = new Intent(this, FaceLibManagerActivity.class);
            startActivity(intent);
        } else if (v == paramsConfigButton) {
            Intent intent = new Intent(this, ParamsConfigActivity.class);
            startActivity(intent);
        } else if (v == faceCollectButton) {
            Intent intent = new Intent(this, FaceCollectActivity.class);
            startActivity(intent);
        } else if (v == localServerButton) {
            Intent intent = new Intent(this, LocalServerActivity.class);
            startActivity(intent);
        } else if (v == faceCompareButton) {
            Intent intent = new Intent(this, FaceCompareActivity.class);
            startActivity(intent);
        } else if (v == spedifiedFaceRecButton) {
            showFileChooser("选择图片", 1);
//            Intent intent = new Intent(this, FaceCompareActivity.class);
//            startActivity(intent);
        } else if (v == btnPersonSync) {
            startActivity(new Intent(this, PersonSyncActivity.class));
        } else if (v == repeatFaceRecButton) {
            if (repeatFaceRecButton.getText().equals("启动循环识别")) {
                isRepeatRectRunning=true;
                repeatFaceRecButton.setText("停止循环识别");
                //打开识别页面，在识别页面中停留5秒钟，然后关闭识别页面回到主页面停留3秒，再打开识别页面。如此返回循环
                if (checkHashCameras()) {
                    Intent intent = new Intent(this, FaceRecActivity.class);
                    intent.putExtra("isRepeatRectRunning", true);
                    startActivity(intent);
                }
            }else{
                //停止循环识别测试
                isRepeatRectRunning=false;
                repeatFaceRecButton.setText("启动循环识别");
            }
        } else if (v == fragmentFaceRecButton) {
            startActivity(new Intent(this, FragmentFaceRecActivity.class));
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            String path = DemoUtils.getPath(this, uri);
            if (path != null) {
                Bitmap bitmap = Tool.loadBitmap(new File(path));
                try {
                    if (bitmap != null) {
                        float[] feature = XsFaceSDKHelper.extractFaceFeature(bitmap);
                        Intent intent = new Intent(this, FaceRecActivity.class);
                        intent.putExtra("feature", feature);
                        startActivity(intent);
                    }
                } catch (FaceException e) {
                    showToast(e.toString());
                }
            }
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }
}
