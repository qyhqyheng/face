package com.xinshi.android.face.demo;

import android.annotation.SuppressLint;
import android.app.Application;
import android.media.SoundPool;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.tencent.bugly.crashreport.CrashReport;
import com.xinshi.android.xsfacesdk.XsFaceSDK;

import java.util.HashMap;
import java.util.Map;

/**
 * 主程序
 */
public class MainApplication extends Application {
    static SoundPool soundPool;
    @SuppressLint("UseSparseArrays")
    static Map<Integer, Integer> soundsMap = new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();
        initBugly();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            initSound();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initSound() {
        if (soundPool == null) {
            soundPool = new SoundPool.Builder().build();
            //4个声音：检测到人脸、攻击、陌生人、识别
            soundsMap.put(R.raw.detect_stranger, soundPool.load(this, R.raw.detect_stranger, 1));
            soundsMap.put(R.raw.detect_attack, soundPool.load(this, R.raw.detect_attack, 1));
            soundsMap.put(R.raw.rec_success, soundPool.load(this, R.raw.rec_success, 1));
        }
    }

    static long lastPlayTime = 0;
    static int lastSoundId = 0;
    public static void playSound(int resId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Integer soundId = soundsMap.get(resId);
            if (soundId == null) {
                return;
            }
            //2秒内不播报相同的声音
            if (System.currentTimeMillis() - lastPlayTime > 2000
                    || lastSoundId != soundId) {
                soundPool.play(soundId,
                        1.0f,      //左耳道音量【0~1】
                        1.0f,      //右耳道音量【0~1】
                        1,         //播放优先级【0表示最低优先级】
                        0,         //循环模式【0表示循环一次，-1表示一直循环，其他表示数字+1表示当前数字对应的循环次数】
                        1          //播放速度【1是正常，范围从0~2】
                );
                lastSoundId = soundId;
                lastPlayTime = System.currentTimeMillis();
            }
        }
    }

    /**
     * 初始化bugly
     */
    private void initBugly() {
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(getApplicationContext());
        strategy.setAppVersion(BuildConfig.VERSION_NAME);
        strategy.setAppPackageName(BuildConfig.APPLICATION_ID);
        strategy.setDeviceID(XsFaceSDK.getDeviceIdNotSafe());
        //Bugly会在启动20s后联网同步数据
        strategy.setAppReportDelay(20000);
        CrashReport.initCrashReport(getApplicationContext(), getString(R.string.bugly_app_id), true);
        CrashReport.setUserId(this, XsFaceSDK.getDeviceIdNotSafe());
    }

    @Override
    public void onLowMemory() {
        android.os.Process.killProcess(android.os.Process.myPid());
        super.onLowMemory();
    }
}
