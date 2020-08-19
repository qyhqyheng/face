package com.xinshi.android.face.demo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;

import com.xinshi.android.face.camera.FaceCamera;
import com.xinshi.android.face.data.FaceData;
import com.xinshi.android.face.data.FaceDetectData;
import com.xinshi.android.face.data.LivenessDetectMode;
import com.xinshi.android.face.data.SearchedPerson;
import com.xinshi.android.face.demo.util.DemoUtils;
import com.xinshi.android.face.exceptions.FaceException;
import com.xinshi.android.face.image.FaceImage;
import com.xinshi.android.face.view.DoubleCameraView;
import com.xinshi.android.xsfacesdk.XsFaceSDK;
import com.xinshi.android.xsfacesdk.XsSdkEnvConfig;
import com.xinshi.android.xsfacesdk.helper_v3.XsFaceSDKCameraHelper;
import com.xinshi.android.xsfacesdk.helper_v3.XsFaceSDKSceneHelper;
import com.xinshi.android.xsfacesdk.scene.CommonFaceRecScene;
import com.xinshi.android.xsfacesdk.scene.SpecifiedFaceRecScene;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/***
 * 通用人脸识别场景
 */
public class FaceRecActivity extends BaseActivity implements CommonFaceRecScene.CommonFaceRecSceneCallback {
    static final String TAG = "FaceRecActivity";
    static final int REC_TIME_LEN_SECONDS = 5;
    Button returnButton;
    Spinner livenessModeSpinner;
    CheckBox reRecCheckbox;
    ArrayAdapter<LivenessDetectMode> livenessDetectModeArrayAdapter;
    CheckBox meteringCheckbox;
    CheckBox repeatCloseOpenCamera;


    //双目摄像头View
    DoubleCameraView visCameraView;
    //识别参数,在ParamsConfigActivity配置识别参数
    SpecifiedFaceRecScene.SpecifiedFaceRecSceneParams sceneParams;
    //识别场景
    static CommonFaceRecScene faceRecScene;




    int counter=0;
    Timer timer = new Timer();
    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            try {
                String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS ").format(new Date());
                if ((counter%8) == 0) {
                    Log.i(TAG, "open camera");
                    writeLog(time+" open camera\n");
                    //打开摄像头
                    faceRecScene.start();
                } else if ((counter%8) == 5) {
                    Log.i(TAG, "close camera");
                    writeLog(time +" close camera\n");
                    //关闭摄像头
                    faceRecScene.stop();
                }
            } catch (Exception e) {

            }
            counter++;
        }
    };

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_rec);

        visCameraView = findViewById(R.id.camera_view);
        returnButton = findViewByIdAndSetListener(R.id.return_button);
        livenessModeSpinner = findViewByIdAndSetListener(R.id.liveness_mode_spinner);
        livenessDetectModeArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, DemoUtils.getSupportLivenessModes(XsFaceSDKCameraHelper.getNirCamera() != null));
        livenessModeSpinner.setAdapter(livenessDetectModeArrayAdapter);
        meteringCheckbox = findViewByIdAndSetListener(R.id.metering_checkbox);
        reRecCheckbox = findViewByIdAndSetListener(R.id.rerec_checkbox);
        repeatCloseOpenCamera=findViewByIdAndSetListener(R.id.repeat_checkbox);


        try {
            //visCameraView.setDrawFaceBox(false);//如果不需要绘制人脸框，就设置为false
            //visCameraView.setDrawFaceName(false);//如果不需要显示人脸框上的文字，就设置为false
            visCameraView.setCamera(XsFaceSDKCameraHelper.getVisCamera());
            visCameraView.setNirCamera(XsFaceSDKCameraHelper.getNirCamera());

            //如果参数传递的，需指定要识别的人脸特征
            float[] feature = getIntent().getFloatArrayExtra("feature");
            //ParamsConfigActivity配置识别参数
            sceneParams = ParamsConfigActivity.getRecSceneParams(feature);
            //初始化场景
            initFaceRecScene(feature);

            meteringCheckbox.setChecked(sceneParams.autoMetering);
            livenessModeSpinner.setSelection(livenessDetectModeArrayAdapter.getPosition(sceneParams.livenessDetectMode));
        } catch (Throwable e) {
            Log.d(TAG, "init failure:", e);
        }

    }

    /***
     * 初始化通用识别场景
     */
    private void initFaceRecScene(float[] feature) throws FaceException {
        if (feature == null) {
            //创建通用的人脸识别场景
            faceRecScene = XsFaceSDKSceneHelper.createCommonFaceRecScene(visCameraView, sceneParams, this);
        } else {
            //创建指定人脸图片的识别场景
            faceRecScene = XsFaceSDKSceneHelper.createSpecifiedFaceRecScene(visCameraView, sceneParams, this);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent == livenessModeSpinner) {
            LivenessDetectMode livenessDetectMode = (LivenessDetectMode) livenessModeSpinner.getSelectedItem();
            if (livenessDetectMode != sceneParams.livenessDetectMode) {
                sceneParams.livenessDetectMode = livenessDetectMode;
                faceRecScene.resetParams(sceneParams);
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == meteringCheckbox) {
            if (sceneParams.autoMetering != meteringCheckbox.isChecked()) {
                sceneParams.autoMetering = meteringCheckbox.isChecked();
                faceRecScene.resetParams(sceneParams);
            }
        } else if (buttonView == reRecCheckbox) {
            if ((isChecked && sceneParams.reRecInterval == 0) ||
                    (!isChecked && sceneParams.reRecInterval > 0)) {
                if (isChecked)
                    sceneParams.reRecInterval = 5000;
                else
                    sceneParams.reRecInterval = 0;
                faceRecScene.resetParams(sceneParams);
            }
        } else if (buttonView == repeatCloseOpenCamera) {
            if (isChecked) {
                timer.schedule(task,0,1000);
            }else{
                timer.cancel();
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(this.getIntent().getBooleanExtra("isRepeatRectRunning", false)){
            repeatCloseOpenCamera.setVisibility(View.INVISIBLE);
            //如果当前处于循环测试中，启动识别后，延时指定时间后，自动关闭页面停止识别
            new Handler().postDelayed(new Runnable(){
                public void run() {
                    //execute the task
                    try {
                        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS ").format(new Date());
                        writeLog(time+" 停止识别\n");
                        finish();

                    } catch (Exception e) {

                    }
                }
            }, REC_TIME_LEN_SECONDS*1000);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            //停止预览，并释放摄像头
            faceRecScene.stop();
            try {
                this.finalize();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void onClick(View v) {
        if (v == returnButton) {
            finish();
        }
    }

    @Override
    public void onLivenessAttack(CommonFaceRecScene.FaceRecSceneData sceneData, SearchedPerson searchedPerson, List<SearchedPerson> searchedPersons) {
        Log.d(TAG, String.format("攻击：疑似人员: %s", searchedPerson != null ? searchedPerson.getPerson() : "无"));
        MainApplication.playSound(R.raw.detect_attack);
    }

    @Override
    public void onRecStranger(CommonFaceRecScene.FaceRecSceneData sceneData, SearchedPerson searchedPerson) {
        Log.d(TAG, String.format("识别到陌生人：最相似的人 %s", searchedPerson != null ? searchedPerson.getPerson() : "无"));
        MainApplication.playSound(R.raw.detect_stranger);
    }

    @Override
    public void onFaceRecComplete(CommonFaceRecScene.FaceRecSceneData sceneData, SearchedPerson searchedPerson, List<SearchedPerson> searchedPersons) {
        Log.d(TAG, String.format("识别成功：%s", searchedPerson.getPerson()));
        MainApplication.playSound(R.raw.rec_success);
    }

    @Override
    public QualityProcessValue onFaceQualityFail(FaceData faceData, QualityResult qualityResult) {
        Log.d(TAG, String.format("质量异常：%s", qualityResult.toString()));
        if (sceneParams.qualityDetectConfig.occ_detect && sceneParams.qualityDetectConfig.occ_pass) {
            if (qualityResult == QualityResult.FACE_BLUR) {
                //当前要求遮挡通行，所以忽视人脸模糊
                return QualityProcessValue.QUALITY_ACCEPT;
            }
        }
        return QualityProcessValue.QUALITY_REJECT;
    }

    /***
     * 不需要可以不实现
     */
    @Override
    public void onImageFrame(FaceCamera camera, FaceImage faceImage) {
        //Log.d(TAG, "摄像头图片回调");
        //if (camera.getCameraParams().getCameraType() == CameraParams.CameraType.VIS_CAMERA) {
        //    //彩色照片
        //} else if (camera.getCameraParams().getCameraType() == CameraParams.CameraType.NIR_CAMERA) {
        //    //黑白照片
        //}
    }

    /***
     * 不需要可以不实现
     */
    @Override
    public void onFaceDetected(FaceDetectData faceDetectData) {
        //Log.d(TAG, "检测到人脸");
    }

    /***
     * 不需要可以不实现
     */
    public void onFaceChanged(FaceDetectData detectData, List<FaceData> addFaces, List<FaceData> lostFaces) {
        //Log.d(TAG, "检测到人脸改变");
    }
}