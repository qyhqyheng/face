package com.xinshi.android.face.demo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.xinshi.android.face.data.FaceData;
import com.xinshi.android.face.data.FaceImageData;
import com.xinshi.android.face.data.SearchedPerson;
import com.xinshi.android.face.db.DbPerson;
import com.xinshi.android.face.demo.util.UITools;
import com.xinshi.android.face.image.FaceImage;
import com.xinshi.android.face.view.SingleCameraView;
import com.xinshi.android.xsfacesdk.helper_v3.XsFaceSDKCameraHelper;
import com.xinshi.android.xsfacesdk.helper_v3.XsFaceSDKFaceLibHelper;
import com.xinshi.android.xsfacesdk.helper_v3.XsFaceSDKHelper;
import com.xinshi.android.xsfacesdk.helper_v3.XsFaceSDKSceneHelper;
import com.xinshi.android.xsfacesdk.scene.FaceRegisterScene;

import java.io.IOException;

/***
 * 人脸注册场景
 */
public class FaceRegisterActivity extends BaseActivity implements FaceRegisterScene.FaceRegisterSceneCallback {
    final static String TAG = "FaceRegisterActivity";
    CheckBox faceDeduplicationButton;
    Button returnButton;

    SingleCameraView visCameraView;
    FaceRegisterScene.FaceRegisterSceneParams sceneParams;
    FaceRegisterScene faceRegisterScene;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_register);

        faceDeduplicationButton = findViewByIdAndSetListener(R.id.face_deduplication_btton);
        returnButton = findViewByIdAndSetListener(R.id.return_button);
        visCameraView = findViewById(R.id.camera_view);

        //初始化人脸识别场景
        initFaceRegisterScene();

        faceDeduplicationButton.setChecked(sceneParams.isSearchExists);
    }

    /***
     * 初始化人脸识别场景
     */
    private void initFaceRegisterScene() {
        try {
            //设置可见光摄像头View
            visCameraView.setCamera(XsFaceSDKCameraHelper.getVisCamera());
            //设置人脸注册场景参数
            sceneParams = new FaceRegisterScene.FaceRegisterSceneParams();
            sceneParams.qualityDetectConfig.blueThreshold = 0.7f;
            sceneParams.qualityDetectConfig.occ_detect = false;
            sceneParams.maxSearchFailTimes = 1;
            //底库人脸建议大一点
            sceneParams.minFaceSize = 100;
            //人脸注册时，强制启用单人脸追踪
            XsFaceSDKHelper.getSDKConfig().setSingleTrack(true);
            //创建人脸注册场景
            faceRegisterScene = XsFaceSDKSceneHelper.createFaceRegisterRecScene(visCameraView, sceneParams, this);
        } catch (Throwable e) {
            Log.d(TAG, "init failure:", e);
        }
    }

    /***
     * 人脸注册-人脸识别结果回调
     * @param faceData       人脸数据
     * @param features       人脸特征
     * @param faceImage      人脸图片
     * @param searchedPerson 搜索到的人脸，为null表示未搜索到，或者如果isSearchExistst=false时，未搜索
     */
    @Override
    public void onRegister(FaceData faceData, float[] features, FaceImage faceImage, SearchedPerson searchedPerson) {
        //显示对话框，暂停识别
        faceRegisterScene.pause();
        if (searchedPerson == null) {
            //底库中不存在识别到的人脸，提示新增人脸
            UITools.showEditDialog(this, "输入姓名", null,
                    new UITools.EditDialogCallback() {
                        @Override
                        public void onOk(String text) {
                            DbPerson person = null;
                            try {
                                person = XsFaceSDKFaceLibHelper.putPersonImage(text, 0, "",
                                        new FaceImageData(faceImage, faceData), features, null, null, 0, false);
                            } catch (Throwable e) {
                                if (person != null) {
                                    try {
                                        XsFaceSDKFaceLibHelper.removePerson(person);
                                    } catch (Throwable ex) {
                                    }
                                }
                                showToast(e.toString());
                            } finally {
                                faceRegisterScene.resume();//恢复识别
                            }
                        }

                        @Override
                        public void onCancel() {
                            faceRegisterScene.resume();//恢复识别
                        }
                    });
        } else {
            //底库中已存在识别到的人脸，提示是否替换
            UITools.showReplaceFaceDialog(this, "警告", "人脸已经存在，需要替换吗?",
                    new UITools.ReplaceFaceDialogCallback() {
                        @Override
                        public void onOk1() {
                            try {
                                //替换人脸1
                                String personCode = searchedPerson.getPerson().getPersonCode();
                                XsFaceSDKFaceLibHelper.putPersonImage(personCode, null, null,
                                        new FaceImageData(faceImage, faceData), features, null, null, 0, false);
                            } catch (Throwable e) {
                                showToast(e.toString());
                            } finally {
                                faceRegisterScene.resume();//恢复识别
                            }
                        }

                        @Override
                        public void onOk2() {
                            try {
                                //替换人脸2
                                String personCode = searchedPerson.getPerson().getPersonCode();
                                XsFaceSDKFaceLibHelper.putPersonImage(personCode, null, null, null, null,
                                        new FaceImageData(faceImage, faceData), features, 0, false);
                            } catch (Throwable e) {
                                showToast(e.toString());
                            } finally {
                                faceRegisterScene.resume();//恢复识别
                            }
                        }

                        @Override
                        public void onCancel() {
                            faceRegisterScene.resume();
                        }
                    });
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == faceDeduplicationButton) {
            if (sceneParams.isSearchExists != isChecked) {
                sceneParams.isSearchExists = faceDeduplicationButton.isChecked();
                faceRegisterScene.resetParams(sceneParams);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            faceRegisterScene.stop();
        } catch (IOException e) {
        }
    }

    @Override
    public void onClick(View v) {
        if (v == returnButton) {
            finish();
        }
    }
}
