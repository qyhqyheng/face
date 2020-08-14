package com.xinshi.android.face.demo;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tencent.bugly.crashreport.BuglyLog;
import com.tencent.bugly.crashreport.CrashReport;
import com.xinshi.android.face.db.DbPerson;
import com.xinshi.android.face.demo.sync.LanPersonSyncClient;
import com.xinshi.android.face.exceptions.FaceException;
import com.xinshi.android.face.exceptions.SDKException;
import com.xinshi.android.xsfacesdk.XsFaceSDK;
import com.xinshi.android.xsfacesdk.XsFaceSDKAuthConfig;
import com.xinshi.android.xsfacesdk.XsSdkEnvConfig;
import com.xinshi.android.xsfacesdk.helper_v3.InitFailReason;
import com.xinshi.android.xsfacesdk.helper_v3.XsFaceSDKCameraHelper;
import com.xinshi.android.xsfacesdk.helper_v3.XsFaceSDKFaceLibHelper;
import com.xinshi.android.xsfacesdk.helper_v3.XsFaceSDKFaceLibParams;
import com.xinshi.android.xsfacesdk.helper_v3.XsFaceSDKHelper;
import com.xinshi.android.xsfacesdk.helper_v3.XsFaceSDKInitCallback;
import com.xinshi.android.xsfacesdk.helper_v3.XsFaceSDKInitHelper;
import com.xinshi.android.xsfacesdk.helper_v3.XsFaceSDKInitParams;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/***
 * Demo启动后的加载中Loading页
 */
public class SplashActivity extends BaseActivity implements XsFaceSDKInitCallback, XsFaceSDKFaceLibHelper.FaceLibCallback {
    private static final String TAG = "SplashActivity";
    TextView promptLabel;
    protected static final int PERMISSIONS_REQUEST_CODE = 99;
    static public SplashActivity instance;
    static public SplashActivity currentInstance;
    static AtomicBoolean inited = new AtomicBoolean(false);
    static boolean faceLibInited = false;
    ProgressBar progressBar;
    static Thread reInitOnFailThread = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        currentInstance = this;//启动界面被隐藏后，会重新创建一个实例.该变量用于表示当前看到的activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        promptLabel = findViewById(R.id.prompt_label);
        progressBar = findViewById(R.id.progress_bar);
        //XsFaceSDKAuthConfig.setDomain("http://192.168.1.100/api");

        if (!inited.get()) {
            instance = this;
            SharedPreferences sp = this.getPreferences(MODE_PRIVATE);
            //设置授权地址，用于环境切换测试。实际集成过程中，不需要设置
            int domainPosition = sp.getInt("domainPosition", 0);
            switch (domainPosition) {
                case 0:
                    XsFaceSDKAuthConfig.setDomain("PROD");
                    break;
                case 1:
                    XsFaceSDKAuthConfig.setDomain("UAT");
                    break;
                case 2:
                    XsFaceSDKAuthConfig.setDomain("TEST");
                    break;
                case 3:
                    XsFaceSDKAuthConfig.setDomain("DEV");
                    break;
            }
            if (requestPermissions(false)) {
                initSDK();
            }
        } else {
            if (faceLibInited) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                SplashActivity displayActivity = (SplashActivity) currentInstance;
                displayActivity.finish();
            }
        }
    }

    /***
     * 申请权限
     * @param permissions 权限列表
     * @return
     */
    private String[] checkOrAddPermissions(String... permissions) {
        List<String> ret = new ArrayList<>();
        if (permissions != null) {
            for (String permission : permissions) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (this.getApplicationInfo().targetSdkVersion >= Build.VERSION_CODES.M) {
                        if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                            ret.add(permission);
                        }
                    } else {
                        if (PermissionChecker.checkSelfPermission(this, permission) != PermissionChecker.PERMISSION_GRANTED) {
                            ret.add(permission);
                        }
                    }
                }
            }
        }
        return ret.toArray(new String[0]);
    }

    /**
     * 申请安卓权限
     */
    private boolean requestPermissions(boolean isQuery) {
        String[] permissions = checkOrAddPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_PHONE_STATE
        );
        if (permissions != null && permissions.length > 0) {
            if (!isQuery) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(permissions, PERMISSIONS_REQUEST_CODE);
                }
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (requestPermissions(true)) {
                initSDK();
            }
        }
    }

    /***
     * 初始化SDK
     */
    protected void initSDK() {
        XsFaceSDKInitParams params = new XsFaceSDKInitParams();
        //这里设置SDK的配置根路径，重要
        params.rootPath = String.format("%s/%s", Environment.getExternalStorageDirectory().getPath(),
                getApplicationInfo().packageName);
        XsFaceSDKInitHelper.getAuthConfig().configSerialAuthInfo(XsFaceSDKInitHelper.getAuthConfig().getSerialNo());
        //如果采用芯片授权方式
        //XsFaceSDKInitHelper.getAuthConfig().configChipAuthInfo();
        //params.chipKeyDataFile="/sdcard/chipdata.txt";
        //如果采用secretkey授权方式
        //XsFaceSDKInitHelper.getAuthConfig().configSecKeyAuthInfo("b88ae500-b13f-4279-9599-17b37cf08430","76368712-a291-47e1-ab23-625a34d20c11");
        //如果采用token授权方式
        //XsFaceSDKInitHelper.getAuthConfig().configTokenAuthInfo("b88ae500-b13f-4279-9599-17b37cf08430","76368712-a291-47e1-ab23-625a34d20c11");
        XsFaceSDKInitHelper.init(this, params, this);
    }

    /***
     * 初始化SDK失败
     * @param reason 失败原因
     * @param e      异常信息
     */
    @Override
    public void onInitFail(InitFailReason reason, Throwable e) {
        Log.e(TAG, reason.toString(), e);
        switch (reason) {
            case NETWORK_ERROR:
            case UNEXCEPT_ERROR:
                showToast(e == null ? reason.toString() : String.format("%s: %s", reason, e));
                break;
            default:
                break;
        }
        //初始化失败后续跳转到激活界面，人工干预后再重新初始化
        showToast(e == null ? reason.toString() : String.format("%s: %s", reason, e));
        Intent intent = new Intent(this, InputAuthInfoActivity.class);
        startActivity(intent);

        //bugly日志上报
        BuglyLog.e(TAG, reason.toString(), e);
        CrashReport.postCatchedException(e);
        //保存logcat日志
        saveLogCatLog();
        //SDK初始化失败自动重试机制：设备硬件未初始化好，需要定时重试
        handleReInitOnFail(reason);
    }

    /***
     保存logcat日志-用于定位问题
     */
    private void saveLogCatLog() {
        new Thread(() -> {
            try {
                if (!new File(XsSdkEnvConfig.getRootPath()).exists()) {
                    new File(XsSdkEnvConfig.getRootPath()).mkdir();
                }
                String cmd = String.format("logcat -f %s/sdk_init_fail.log", XsSdkEnvConfig.getRootPath());
                Log.i(TAG, String.format("保存logcat日志:%s", cmd));
                Runtime.getRuntime().exec(cmd);
            } catch (IOException ex) {
                Log.e(TAG, ex.toString(), ex);
            }
        }).start();
    }

    /**
     * SDK初始化失败自动重试机制
     *
     * 有些不稳定的设备，在系统刚重启的一段时间内，获取到的设备信息可能与已授权的设备信息不一致，所以需要间隔一段时间自动重试
     *
     * @param reason 上次重试失败原因
     */
    private void handleReInitOnFail(InitFailReason reason) {
        if (reInitOnFailThread == null) {
            reInitOnFailThread = new Thread(() -> {
                try {
                    //第一次阻塞等待回调方法结束后，再异步执行初始化
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.i("sdk_init_thread", "SDK初始化重试线程开始运行...");
                while (!inited.get()) {
                    try {
                        showToast(String.format("激活失败:%s，开始自动重试...", reason));
                        Log.i("sdk_init_thread", "尝试SDK初始化重试...");
                        //随机设置1个序列号表示配置更新需要
                        XsFaceSDKInitHelper.getAuthConfig().setForceReinit(true);
                        SplashActivity.instance.initSDK();
                    } catch (Throwable ex) {
                        Log.e(TAG, ex.getMessage(), ex);
                    } finally {
                        try {
                            //3m后重试
                            Thread.sleep(180000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            Log.e("sdk_init_thread", String.format("SDK初始化重试线程中断:%s", e.getMessage()), e);
                        }
                    }
                }
                Log.i("sdk_init_thread", "初始化成功，退出SDK重试线程...");
            });
            reInitOnFailThread.start();
        }
    }

    /***
     * 初始化SDK成功
     */
    @Override
    public void onInitSuccess() {
        //装入人脸数据
        try {
            inited.set(true);
            if (reInitOnFailThread != null) {
                reInitOnFailThread.interrupt();
            }
            XsFaceSDKFaceLibParams params = new XsFaceSDKFaceLibParams();
            params.isSaveFaceLibImage = true;
            params.autoUpdateFeatures = true;
            params.faceLibImagesSavePath = String.format("%s/faceimages", String.format("%s/%s", Environment.getExternalStorageDirectory().getPath(),
                    getApplicationInfo().packageName));
            params.faceLibDbFilePath = String.format("%s/db/faceperson", String.format("%s/%s", Environment.getExternalStorageDirectory().getPath(), getApplicationInfo().packageName));
            Log.d(TAG, String.format("正在初始化底库[%s]...", params.faceLibDbFilePath));
            setPromptLabelMsg("正在初始化底库...");
            XsFaceSDKFaceLibHelper.init(this, params, this);
        } catch (SDKException e) {
            e.printStackTrace();
            showToast(e.getMessage());
        }
    }


    /***
     * 启动同步客户端
     * 局域网内启动设备A上的作为同步服务器，其他设备启动同步客户端从设备A同步人员数据
     */
    public void startPersonSyncClient() {
        //获取配置文件中的同步地址
        String domain = getString(R.string.sync_server_domain);
        LanPersonSyncClient.SyncConfig syncConfig = new LanPersonSyncClient.SyncConfig();
        syncConfig.setUrlGetFullSyncPersons(String.format("%s/get_full_sync_persons", domain));
        syncConfig.setUrlGetIncSyncPersons(String.format("%s/get_inc_sync_persons", domain));
        syncConfig.setUrlUpdatePersonSyncStates(String.format("%s/update_person_sync_status", domain));
        //配置为每60s执行一次增量同步
        LanPersonSyncClient syncClient = new LanPersonSyncClient(false, 60, syncConfig);
        //异步线程执行人员同步
        syncClient.start();
    }

    /***
     * 初始化人脸底库成功
     */
    @Override
    public void onFaceLibInitSuccess() {
        faceLibInited = true;
        setPromptLabelMsg("底库初始化完成");
        Log.d(TAG, "底库初始化完成");

        //TODO 如果需要开启和服务端的人员同步，取消注释以下代码，服务端接口需参考标准同步接口文档实现
        // setPromptLabelMsg("正在初始化人员同步客户端...");
        // startPersonSyncClient();

        //装入摄像头配置文件
        XsFaceSDKCameraHelper.loadConfig(this, null);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        SplashActivity displayActivity = (SplashActivity) currentInstance;
        displayActivity.finish();
        try {
            XsFaceSDKHelper.setPassRateLevel("low");
        } catch (FaceException e) {
            e.printStackTrace();
        }
    }

    /***
     * 初始化人脸底库失败
     */
    @Override
    public void onFaceLibInitFail(int reason, Throwable e) {
        setPromptLabelMsg("底库初始化失败");
        Log.d(TAG, "底库初始化失败");
        showToast(e.toString());
    }

    /***
     * 刷新人脸底库的人脸特征进度
     * @param progress 加载进度
     * @param max 总人脸数
     */
    @Override
    public void onRefreshFaceFeatureProgress(int progress, int max) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SplashActivity displayActivity = (SplashActivity) currentInstance;
                displayActivity.promptLabel.setText("正在抽取特征...");
                if (displayActivity.progressBar.getVisibility() != View.VISIBLE) {
                    displayActivity.progressBar.setVisibility(View.VISIBLE);
                }
                displayActivity.progressBar.setMax(max);
                displayActivity.progressBar.setProgress(progress);
            }
        });
    }

    /***
     * 刷新人脸底库的人脸特征失败
     * @param person 人员对象
     * @param e 异常
     */
    @Override
    public void onRefreshFaceFeatureFail(DbPerson person, Throwable e) {
        String msg = String.format("重抽特征失败: %s", e == null ? "意外错误" : e.toString());
        setPromptLabelMsg(msg);
        Log.d(TAG, msg, e);
    }

    /***
     * 人脸搜索缓存中人脸特征加载进度
     * @param progress 加载进度
     * @param max 总人脸数
     */
    @Override
    public void onLoadFaceFeatureProgress(int progress, int max) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SplashActivity displayActivity = (SplashActivity) currentInstance;
                displayActivity.promptLabel.setText("正在装载特征...");
                if (displayActivity.progressBar.getVisibility() != View.VISIBLE) {
                    displayActivity.progressBar.setVisibility(View.VISIBLE);
                }
                displayActivity.progressBar.setMax(max);
                displayActivity.progressBar.setProgress(progress);
            }
        });
    }

    protected void setPromptLabelMsg(String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SplashActivity displayActivity = (SplashActivity) currentInstance;
                displayActivity.promptLabel.setText(msg);
            }
        });
    }
}
