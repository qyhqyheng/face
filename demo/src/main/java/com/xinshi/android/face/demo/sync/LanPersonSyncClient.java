package com.xinshi.android.face.demo.sync;

import android.util.Log;

import com.xinshi.android.face.http.HttpUtils;
import com.xinshi.android.xsfacesdk.helper_v3.XsFaceSDKInitHelper;
import com.xinshi.android.xsfacesdk.network.AbstractPersonSyncClient;
import com.xinshi.android.xsfacesdk.network.SyncPerson;
import com.xinshi.android.xsfacesdk.network.SyncPersonList;
import com.xinshi.android.xsfacesdk.network.SyncPersonResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 * 局域网人员同步客户端
 * 全量同步：new LanPersonSyncClient(true, 60).start();
 * 增量同步：new LanPersonSyncClient(false, 60).start();
 */
public class LanPersonSyncClient extends AbstractPersonSyncClient {
    private static final String TAG = LanPersonSyncClient.class.getName();
    public String urlGetFullSyncPersons;
    public String urlGetIncSyncPersons;
    public String urlUpdatePersonSyncStates;

    /***
     * 同步配置参数
     */
    public static class SyncConfig {
        /***
         * 全量同步接口地址
         */
        private String urlGetFullSyncPersons;
        /***
         * 增量同步接口地址
         */
        private String urlGetIncSyncPersons;
        /***
         * 同步状态上传接口地址
         */
        private String urlUpdatePersonSyncStates;

        public String getUrlGetFullSyncPersons() {
            return urlGetFullSyncPersons;
        }

        public void setUrlGetFullSyncPersons(String urlGetFullSyncPersons) {
            this.urlGetFullSyncPersons = urlGetFullSyncPersons;
        }

        public String getUrlGetIncSyncPersons() {
            return urlGetIncSyncPersons;
        }

        public void setUrlGetIncSyncPersons(String urlGetIncSyncPersons) {
            this.urlGetIncSyncPersons = urlGetIncSyncPersons;
        }

        public String getUrlUpdatePersonSyncStates() {
            return urlUpdatePersonSyncStates;
        }

        public void setUrlUpdatePersonSyncStates(String urlUpdatePersonSyncStates) {
            this.urlUpdatePersonSyncStates = urlUpdatePersonSyncStates;
        }
    }

    /**
     * 构造同步客户端
     *
     * @param fullSyncMode 是否全量同步模式
     * @param syncInterval 两次同步的间隔时间，以秒为单位。
     * @param syncConfig   同步配置参数
     */
    public LanPersonSyncClient(boolean fullSyncMode, int syncInterval, SyncConfig syncConfig) {
        super(fullSyncMode, syncInterval);
        if (fullSyncMode) {
            this.urlGetFullSyncPersons = syncConfig.getUrlGetFullSyncPersons();
        } else {
            this.urlGetIncSyncPersons = syncConfig.getUrlGetIncSyncPersons();
        }
        this.urlUpdatePersonSyncStates = syncConfig.getUrlUpdatePersonSyncStates();
    }

    @Override
    public SyncPersonList getFullSyncPersons(int firstIndex) throws Exception {
        SyncPersonList syncList = new SyncPersonList();
        try {
            JSONObject params = new JSONObject();
            params.put("first_index", firstIndex);
            params.put("device_code", XsFaceSDKInitHelper.getAuthConfig().getSerialNo());
            params.put("auth_info", "");
            Log.d(TAG, String.format("全量同步，url[%s],request:%s", urlGetFullSyncPersons, params.toString()));
            JSONObject res = HttpUtils.jsonPost(urlGetFullSyncPersons, params, "ZqznPersonSync");
            boolean success = res.optBoolean("success", false);
            if (success) {
                JSONObject resData = res.getJSONObject("data");
                List<SyncPerson> addUpdateList = new ArrayList<>();
                JSONArray personList = resData.optJSONArray("person_list");
                for (int i = 0; i < personList.length(); i++) {
                    SyncPerson syncPerson = new SyncPerson();
                    syncPerson.loadFromJson(personList.getJSONObject(i));
                    addUpdateList.add(syncPerson);
                }
                syncList.addUpdateList = addUpdateList;
                syncList.hasNext = resData.optBoolean("has_next");
            } else {
                Log.e(TAG, res.toString());
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
            throw e;
        }
        return syncList;
    }

    @Override
    public SyncPersonList getIncSyncPersons(long lastSyncTime) throws Exception {
        SyncPersonList syncList = new SyncPersonList();
        try {
            JSONObject params = new JSONObject();
            params.put("last_sync_time", lastSyncTime);
            params.put("device_code", XsFaceSDKInitHelper.getAuthConfig().getSerialNo());
            params.put("auth_info", "");
            Log.d(TAG, String.format("增量同步，url[%s],request:%s", urlGetIncSyncPersons, params.toString()));
            JSONObject res = HttpUtils.jsonPost(urlGetIncSyncPersons, params, "ZqznPersonSync");
            boolean success = res.optBoolean("success", false);
            if (success) {
                JSONObject resData = res.getJSONObject("data");
                List<SyncPerson> addUpdateList = new ArrayList<>();
                Map<String, Long> removeMap = new HashMap<>();

                JSONArray personList = resData.optJSONArray("add_update_list");
                for (int i = 0; i < personList.length(); i++) {
                    SyncPerson updatePerson = new SyncPerson();
                    updatePerson.loadFromJson(personList.getJSONObject(i));
                    addUpdateList.add(updatePerson);
                }
                JSONArray removeList = resData.optJSONArray("remove_list");
                for (int i = 0; i < removeList.length(); i++) {
                    SyncPerson deleteOerson = new SyncPerson();
                    deleteOerson.loadFromJson(removeList.getJSONObject(i));
                    removeMap.put(deleteOerson.personCode, deleteOerson.lastModified);
                }
                syncList.addUpdateList = addUpdateList;
                syncList.removeList = removeMap;
                syncList.hasNext = resData.optBoolean("has_next");
            } else {
                Log.e(TAG, res.toString());
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
            throw e;
        }
        return syncList;
    }

    @Override
    public boolean updatePersonSyncStatus(List<SyncPersonResult> syncPersonResults) throws Exception {
        boolean success;
        try {
            JSONObject params = new JSONObject();
            params.put("sync_data", syncPersonResults);
            params.put("device_code", XsFaceSDKInitHelper.getAuthConfig().getSerialNo());
            params.put("auth_info", "");
            Log.d(TAG, String.format("上报同步状态，url[%s],request:%s", urlUpdatePersonSyncStates, params.toString()));
            JSONObject res = HttpUtils.jsonPost(urlUpdatePersonSyncStates, params, "ZqznPersonSync");
            Log.d(TAG, String.format("上报同步状态结果:%s", res));
            success = res.optBoolean("success", false);
            if (!success) {
                Log.e(TAG, res.toString());
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
            throw e;
        }
        return success;
    }
}
