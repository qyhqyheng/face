package com.xinshi.android.face.demo;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.xinshi.android.face.db.DbDeletedPerson;
import com.xinshi.android.face.db.DbPerson;
import com.xinshi.android.face.demo.sync.LanPersonSyncClient;
import com.xinshi.android.xsfacesdk.XsFaceSDK;
import com.xinshi.android.xsfacesdk.XsFaceSearchLibrary;
import com.xinshi.android.xsfacesdk.helper_v3.XsFaceSDKFaceLibHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/***
 * 人员同步Activity
 * 用于测试人员同步客户端，可指定同步服务器和是否全量同步
 */
public class PersonSyncActivity extends BaseActivity {
    TextView logView;
    EditText etSyncDomain;
    Button btnStop, btnStart, btnReturn, btnStatis;
    CheckBox cbxIsFullSync;
    //同步客户端
    LanPersonSyncClient syncClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_sync);
        btnStart = findViewByIdAndSetListener(R.id.btn_start_sync);
        btnStop = findViewByIdAndSetListener(R.id.btn_stop_sync);
        btnReturn = findViewByIdAndSetListener(R.id.btn_return);
        btnStatis = findViewByIdAndSetListener(R.id.btn_lib_statis);
        etSyncDomain = findViewByIdAndSetListener(R.id.et_sync_domain);
        cbxIsFullSync = findViewById(R.id.cbx_full_sync);
        logView = findViewById(R.id.tv_sync_log);
        logView.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    @Override
    public void onClick(View v) {
        if (v == btnStart) {
            if (syncClient == null) {
                try {
                    //同步服务器根域名：http://10.20.111.44:8080
                    String domain = etSyncDomain.getText().toString();
                    //是否全量同步
                    boolean isFullSync = cbxIsFullSync.isChecked();
                    startSyncClient(domain, isFullSync);
                    doLogMessage("person sync service started..\n");
                } catch (Throwable e) {
                    showToast(e.toString());
                }
            }
        } else if (v == btnStop) {
            if (syncClient != null) {
                syncClient.stop();
                syncClient = null;
                doLogMessage("person sync service  stop.\r\n");
            }
        } else if (v == btnStatis) {
            String logItem = PersonLibStatis();
            doLogMessage(String.format("%s.\r\n", logItem));
        } else if (v == btnReturn) {
            finish();
        }
    }


    static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /***
     * 底库统计
     */
    private String PersonLibStatis() {
        Pair<Integer, List<DbPerson>> personStatis = XsFaceSDKFaceLibHelper.queryPerson(-1, 0, null);
        Pair<Integer, List<DbDeletedPerson>> deletedPersonStatis = XsFaceSDKFaceLibHelper.queryDeletedPersonByLastModified(-1, 0, 0);
        XsFaceSearchLibrary searchLibrary = (XsFaceSearchLibrary) XsFaceSDK.instance.getFaceSearchLibrary();
        long lastSyncTime = XsFaceSDKFaceLibHelper.getLastSyncTimeInCache();
        return String.format("[%s]底库最后一次同步时间[%s],底库人员数[%s],底库已删除人员数[%s],\r\n" +
                        "搜索缓存总人数：%s，搜索缓存总人脸数：%s\r\n", sdf.format(new Date()), lastSyncTime,
                personStatis.first, deletedPersonStatis.first,
                searchLibrary.getPersonCount(), searchLibrary.getFaceCount());
    }

    /***
     * 启动同步客户端
     * 1.从局域网同步：需在局域网内启动设备A上的作为同步服务器，其他设备启动同步客户端从设备A同步人员数据
     * 2.从云端同步：参考LanPersonSyncClient实现远程人员同步接口
     */
    public void startSyncClient(String domain, boolean isFullSync) {
        if (syncClient != null) {
            syncClient.stop();
            syncClient = null;
        }
        LanPersonSyncClient.SyncConfig syncConfig = new LanPersonSyncClient.SyncConfig();
        syncConfig.setUrlGetFullSyncPersons(String.format("%s/get_full_sync_persons", domain));
        syncConfig.setUrlGetIncSyncPersons(String.format("%s/get_inc_sync_persons", domain));
        syncConfig.setUrlUpdatePersonSyncStates(String.format("%s/update_person_sync_status", domain));
        //配置为增量同步，每间隔60秒执行1次同步
        syncClient = new LanPersonSyncClient(isFullSync, 60, syncConfig);
        syncClient.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (syncClient != null) {
            syncClient.stop();
            syncClient = null;
        }
    }

    private void doLogMessage(String msg) {
        runOnUiThread(() -> {
            logView.append(msg);
            int scrollAmount = logView.getLayout().getLineTop(logView.getLineCount()) - logView.getHeight();
            if (scrollAmount > 0) {
                logView.scrollTo(0, scrollAmount);
            } else {
                logView.scrollTo(0, 0);
            }
        });
    }
}
