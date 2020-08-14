package com.xinshi.android.face.demo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.xinshi.android.face.config.EnvConfig;
import com.xinshi.android.face.exceptions.FaceException;
import com.xinshi.android.face.util.NormalizeHelper;
import com.xinshi.android.xsfacesdk.XsSDKConfig;
import com.xinshi.android.xsfacesdk.helper_v3.XsFaceSDKHelper;
import com.xinshi.android.xsfacesdk.scene.SpecifiedFaceRecScene;
import com.xinshi.android.xsfacesdk.util.XsFaceSDKUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/***
 * 参数配置：人脸追踪参数配置
 */
public class ParamsConfigActivity extends BaseActivity {
    static final String TAG = "ParamsConfigActivity";
    static final String DEMO_CONFIG = "demo_config.txt";

    CheckBox cbxSingleTrack, cbxSaveRecImg, cbxQuailtyDetect;
    EditText framesPerTrackEdit, framesPerDetectEdit, quickFramesPerDetectEdit;
    EditText quickTimesEdit, maxSearchFailTimesEdit;
    EditText minFaceSizeEdit, simThresholdEdit, blurEdit, pitchEdit, yawEdit, rollEdit;
    Button okButton, cancelButton;
    Spinner simThresholdSpinner;
    //识别通过率列表
    ArrayAdapter<PassRateMode> passRateModeArrayAdapter;
    //通过率模式 // TODO: 2019/12/10 客户可根据自己的数据情况测试后设置不同的通过率模式
    static PassRateMode currentPassRateMode = PassRateMode.LOW;


    //通用识别场景参数
    static SpecifiedFaceRecScene.SpecifiedFaceRecSceneParams sceneParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_params_config);

        maxSearchFailTimesEdit = findViewById(R.id.max_search_fail_times_edit);
        cbxSingleTrack = findViewById(R.id.single_track_check_box);
        cbxSaveRecImg = findViewById(R.id.cbx_save_rec_img);
        cbxQuailtyDetect = findViewByIdAndSetListener(R.id.quality_detect_checkbox);

        pitchEdit = findViewById(R.id.pitch_edit);
        yawEdit = findViewById(R.id.yaw_edit);
        rollEdit = findViewById(R.id.roll_edit);
        framesPerTrackEdit = findViewById(R.id.frames_per_track_edit);
        framesPerDetectEdit = findViewById(R.id.frames_per_detect_edit);
        quickFramesPerDetectEdit = findViewById(R.id.quick_frames_per_detect_edit);
        quickTimesEdit = findViewById(R.id.quick_times_edit);
        minFaceSizeEdit = findViewById(R.id.min_face_size_edit);
        blurEdit = findViewById(R.id.blur_edit);
        simThresholdEdit = findViewById(R.id.sim_threshold_edit);
        okButton = findViewByIdAndSetListener(R.id.ok_button);
        cancelButton = findViewByIdAndSetListener(R.id.cancel_button);

        //SDK识别参数
        XsSDKConfig config = XsFaceSDKHelper.getSDKConfig();
        cbxSingleTrack.setChecked(config.singleTrack);
        framesPerDetectEdit.setText(Integer.toString(config.framesPerDetect));
        framesPerTrackEdit.setText(Integer.toString(config.framesPerTrack));
        quickFramesPerDetectEdit.setText(Integer.toString(config.quickFramesPerDetect));
        quickTimesEdit.setText(Integer.toString(config.quickTimeMilliSeconds / 1000));
        simThresholdSpinner = findViewByIdAndSetListener(R.id.sim_threshold_spinner);
        passRateModeArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, PassRateMode.toArray());
        simThresholdSpinner.setAdapter(passRateModeArrayAdapter);

        //初始化Demo场景配置
        initSceneParams(null);

        //设置界面配置项
        cbxSaveRecImg.setChecked(sceneParams.isSaveRecPictures);
        //检测
        minFaceSizeEdit.setText(String.valueOf(sceneParams.minFaceSize));
        //识别
        simThresholdEdit.setText(String.valueOf(sceneParams.simThreshold));
        maxSearchFailTimesEdit.setText(String.valueOf(sceneParams.maxSearchFailTimes));
        simThresholdSpinner.setSelection(currentPassRateMode.ordinal());
        //质量
        cbxQuailtyDetect.setChecked(sceneParams.isQualityDetect);
        blurEdit.setText(String.valueOf(sceneParams.qualityDetectConfig.blueThreshold));
        pitchEdit.setText(String.valueOf(sceneParams.qualityDetectConfig.pitchThreshold));
        yawEdit.setText(String.valueOf(sceneParams.qualityDetectConfig.yawThreshold));
        rollEdit.setText(String.valueOf(sceneParams.qualityDetectConfig.rollThreshold));
        //根据是否质量检测控制质量检测阈值
        handleQualityOptions(cbxQuailtyDetect.isChecked());
        cbxQuailtyDetect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            handleQualityOptions(isChecked);
        });
    }

    private void handleQualityOptions(boolean isQualityCheck) {
        blurEdit.setEnabled(isQualityCheck);

        pitchEdit.setEnabled(isQualityCheck);
        yawEdit.setEnabled(isQualityCheck);
        rollEdit.setEnabled(isQualityCheck);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent == simThresholdSpinner) {
            PassRateMode mode = (PassRateMode) parent.getSelectedItem();
            try {
                //设置识别通过率模式
                if (!XsFaceSDKHelper.setPassRateLevel(mode.getName())) {
                    showToast("配置配置失败，不支持的通过率等级.");
                } else {
                    currentPassRateMode = mode;
                }
            } catch (FaceException e) {
                Log.d("ParamsConfigActivity", "配置失败: ", e);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v == okButton) {
            try {
                //保存识别参数
                XsSDKConfig config = XsFaceSDKHelper.getSDKConfig();
                config.singleTrack = cbxSingleTrack.isChecked();
                config.framesPerDetect = Integer.valueOf(framesPerDetectEdit.getText().toString());
                config.framesPerTrack = Integer.valueOf(framesPerTrackEdit.getText().toString());
                config.quickFramesPerDetect = Integer.valueOf(quickFramesPerDetectEdit.getText().toString());
                config.quickTimeMilliSeconds = Integer.valueOf(quickTimesEdit.getText().toString()) * 1000;
                config.save();

                //设置Demo识别场景参数
                boolean success = setCommonSceneRecParam();
                if (success) {
                    //保存Demo场景参数
                    saveSceneParams();
                    finish();
                }
            } catch (Throwable e) {
                showToast(e.toString());
            }
        } else if (v == cancelButton) {
            finish();
        }
    }

    public static SpecifiedFaceRecScene.SpecifiedFaceRecSceneParams getRecSceneParams(float[] feature) {
        //初始化场景参数配置
        initSceneParams(feature);
        return sceneParams;
    }

    /***
     * 初始化场景参数配置
     * @param feature 指定人脸识别场景，需指定要识别的人脸特征
     */
    public static void initSceneParams(float[] feature) {
        if (sceneParams == null) {
            sceneParams = new SpecifiedFaceRecScene.SpecifiedFaceRecSceneParams();
        }
        if (feature != null) {
            SpecifiedFaceRecScene.SpecifiededFaceFeature faceFeature = new SpecifiedFaceRecScene.SpecifiededFaceFeature("person1", feature);
            sceneParams.searchFaceFeatures = new SpecifiedFaceRecScene.SpecifiededFaceFeature[]{faceFeature};
        }
        //配置是否支持陌生人继续做活体
        //sceneParams.strangerLivenessDetect=true;
        //如果需要佩戴口罩才能通过，配置下面两个个参数
        //sceneParams.qualityDetectConfig.occ_detect=true;
        //sceneParams.qualityDetectConfig.occ_pass=true;
        try {
            //识别日志，默认开启
            sceneParams.isSaveRecLog = true;
            sceneParams.recLogPath = new File(String.format("%s/face_rec_log", EnvConfig.getRootPath()));
            sceneParams.recLogPath.mkdirs();

            String recSceneParamsFile = String.format("%s/%s", EnvConfig.getRootPath(), DEMO_CONFIG);
            File configFile = new File(recSceneParamsFile);
            if (configFile.exists()) {
                //读取demo配置文件中的识别配置参数
                String jsonStr = new String(XsFaceSDKUtils.readFile(configFile));
                Log.i(TAG, String.format("读取SD卡Demo场景识别参数[%s]", jsonStr));
                JSONObject recSceneParamJson = new JSONObject(jsonStr).optJSONObject("rec_secene_params");
                sceneParams.minFaceSize = recSceneParamJson.getInt("minFaceSize");
                sceneParams.simThreshold = recSceneParamJson.getDouble("simThreshold");
                sceneParams.maxSearchFailTimes = recSceneParamJson.getInt("maxSearchFailTimes");
                currentPassRateMode = PassRateMode.valueOfString(recSceneParamJson.getString("passRateLevel"));
                sceneParams.isSaveRecPictures = recSceneParamJson.getBoolean("isSaveRecPictures");
                sceneParams.isQualityDetect = recSceneParamJson.getBoolean("isQualityDetect");
                sceneParams.qualityDetectConfig.blueThreshold = recSceneParamJson.getDouble("qualityDetectConfig.blueThreshold");
                sceneParams.qualityDetectConfig.pitchThreshold = recSceneParamJson.getDouble("qualityDetectConfig.pitchThreshold");
                sceneParams.qualityDetectConfig.yawThreshold = recSceneParamJson.getDouble("qualityDetectConfig.yawThreshold");
                sceneParams.qualityDetectConfig.rollThreshold = recSceneParamJson.getDouble("qualityDetectConfig.rollThreshold");
            } else {
                Log.i(TAG, "初始化Demo场景识别参数...");
                //人脸检测大小-用于控制识别距离
                sceneParams.minFaceSize = 20;
                //人脸质量检测参数
                sceneParams.isQualityDetect = true;
                //人脸质量检测阈值
                sceneParams.qualityDetectConfig.blueThreshold = 0.88;
                sceneParams.qualityDetectConfig.pitchThreshold = 10;
                sceneParams.qualityDetectConfig.yawThreshold = 20;
                sceneParams.qualityDetectConfig.rollThreshold = 20;
                //人脸识别参数
                currentPassRateMode = PassRateMode.MEDIUM;
                sceneParams.simThreshold = NormalizeHelper.SIM_HIGH;
                sceneParams.maxSearchFailTimes = 3;
                // FIXME 注意：建议调试时设置为true，上线时应设置为false
                sceneParams.isSaveRecPictures = true;
            }
        } catch (Exception e) {
            Log.e(TAG, String.format("Demo识别场景参数初始化异常[%s]", e.getMessage()));
        }
    }

    /***
     * 保存场景参数
     */
    public void saveSceneParams() throws IOException, JSONException {
        String demoConfig = String.format("%s/%s", EnvConfig.getRootPath(), DEMO_CONFIG);
        File configFile = new File(demoConfig);
        JSONObject jsonObject = new JSONObject();
        saveSceneParams(jsonObject);
        String str = jsonObject.toString();
        configFile.getParentFile().mkdirs();
        OutputStream stream = new FileOutputStream(configFile);
        try {
            stream.write(str.getBytes("utf-8"));
            stream.flush();
            //将数据同步到达物理存储设备
            FileDescriptor fd = ((FileOutputStream) stream).getFD();
            fd.sync();
        } finally {
            stream.close();
        }
    }

    public void saveSceneParams(JSONObject jsonObject) throws JSONException {
        JSONObject recSceneParamJson = new JSONObject();
        recSceneParamJson.put("minFaceSize", sceneParams.minFaceSize);
        recSceneParamJson.put("simThreshold", sceneParams.simThreshold);
        recSceneParamJson.put("maxSearchFailTimes", sceneParams.maxSearchFailTimes);
        recSceneParamJson.put("isSaveRecPictures", sceneParams.isSaveRecPictures);
        recSceneParamJson.put("passRateLevel", currentPassRateMode.getName());
        recSceneParamJson.put("isQualityDetect", sceneParams.isQualityDetect);
        recSceneParamJson.put("qualityDetectConfig.blueThreshold", sceneParams.qualityDetectConfig.blueThreshold);
        recSceneParamJson.put("qualityDetectConfig.pitchThreshold", sceneParams.qualityDetectConfig.pitchThreshold);
        recSceneParamJson.put("qualityDetectConfig.yawThreshold", sceneParams.qualityDetectConfig.yawThreshold);
        recSceneParamJson.put("qualityDetectConfig.rollThreshold", sceneParams.qualityDetectConfig.rollThreshold);

        jsonObject.put("rec_secene_params", recSceneParamJson);
    }


    /***
     * 设置通用场景人脸识别参数
     */
    private boolean setCommonSceneRecParam() {
        //设置最小人脸大小
        int minFaceSize = Integer.valueOf(minFaceSizeEdit.getText().toString());
        if (minFaceSize < 20) {
            showToast("输入人脸大小不能小于20");
            return false;
        } else {
            sceneParams.minFaceSize = minFaceSize;
        }
        //设置相似度阈值
        double sim = Double.valueOf(simThresholdEdit.getText().toString());
        if (sim >= 1 || sim <= 0) {
            showToast("人脸相似度阈值有效取值范围(0,1)");
            return false;
        } else {
            sceneParams.simThreshold = sim;
        }
        //设置模糊阈值
        double blur = Double.valueOf(blurEdit.getText().toString());
        if (blur >= 1 || blur <= 0) {
            showToast("人脸模糊阈值有效取值范围(0,1)");
            return false;
        } else {
            sceneParams.qualityDetectConfig.blueThreshold = blur;
        }

        //设置俯仰角
        double pitch = Double.valueOf(pitchEdit.getText().toString());
        if (pitch > 90 || pitch < 0) {
            showToast("俯仰角有效取值范围[0,90]");
            return false;
        } else {
            sceneParams.qualityDetectConfig.pitchThreshold = pitch;
        }
        //设置偏航角
        double yaw = Double.valueOf(yawEdit.getText().toString());
        if (yaw > 90 || yaw < 0) {
            showToast("偏航角有效取值范围[0,90]");
            return false;
        } else {
            sceneParams.qualityDetectConfig.yawThreshold = yaw;
        }
        //设置翻滚角
        double roll = Double.valueOf(rollEdit.getText().toString());
        if (roll > 90 || roll < 0) {
            showToast("翻滚角有效取值范围[0,90]");
            return false;
        } else {
            sceneParams.qualityDetectConfig.rollThreshold = roll;
        }

        //设置识别最大失败次数
        int maxSearchFailTimes = Integer.valueOf(maxSearchFailTimesEdit.getText().toString());
        Log.i(TAG, "maxSearchFailTimes-" + sim);
        sceneParams.maxSearchFailTimes = maxSearchFailTimes;
        //设置是否保存识别图片
        sceneParams.isSaveRecPictures = cbxSaveRecImg.isChecked();
        //设置是否开启质量检测
        sceneParams.isQualityDetect = cbxQuailtyDetect.isChecked();
        return true;
    }

    /***
     * 通过率模式
     */
    enum PassRateMode {
        LOW {
            @Override
            public String toString() {
                return "低";
            }

            @Override
            public String getName() {
                return "low";
            }
        }, MEDIUM {
            @Override
            public String toString() {
                return "中";
            }

            @Override
            public String getName() {
                return "medium";
            }
        }, HIGH {
            @Override
            public String toString() {
                return "高";
            }

            @Override
            public String getName() {
                return "high";
            }
        };

        abstract public String getName();

        static PassRateMode[] toArray() {
            return new PassRateMode[]{PassRateMode.LOW, PassRateMode.MEDIUM, PassRateMode.HIGH};
        }

        static PassRateMode valueOfString(String key) {
            PassRateMode[] r = toArray();
            for (PassRateMode m : r) {
                if (m.getName().equals(key)) return m;
            }
            return PassRateMode.MEDIUM;
        }
    }

}
