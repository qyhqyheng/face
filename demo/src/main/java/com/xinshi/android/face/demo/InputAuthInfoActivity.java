package com.xinshi.android.face.demo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.xinshi.android.xsfacesdk.XsFaceSDK;
import com.xinshi.android.xsfacesdk.XsFaceSDKAuthConfig;
import com.xinshi.android.xsfacesdk.helper_v3.XsFaceSDKInitHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/***
 * 输入SDK授权信息页
 */
public class InputAuthInfoActivity extends BaseActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener, AdapterView.OnItemSelectedListener {
    EditText serialNoEdit, appkeyEdit, secretkeyEdit, tokenEdit;
    Button activateButton;
    RadioGroup authRadioGroup;
    LinearLayout serialLayout, appkeyLayyout, secretkeyLayout, tokenLayout, chipkeyLayout;
    TextView mDeviceId;
    final int SERIAL_AUTH = 1;
    final int SECRETKEY_AUTH = 2;
    final int TOKEN_AUTH = 3;
    final int CHIP_AUTH = 4;
    //授权类型
    int authType = 1;

    //TODO 是否开通包年SDK：如果云端已申请开通包年，此处设置为true，云端会自动分配序列号
    final boolean isSdkYearly = false;

    Spinner domainSpinner;
    List<MyPair> runEnvList;

    class MyPair extends Pair<String, String> {
        public MyPair(String first, String second) {
            super(first, second);
        }

        @Override
        public String toString() {
            return first;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_auth_info);
        runEnvList = new ArrayList<>();
        runEnvList.add(new MyPair("生产环境", "PROD"));
        runEnvList.add(new MyPair("UAT环境", "UAT"));
        runEnvList.add(new MyPair("测试环境", "TEST"));
        runEnvList.add(new MyPair("开发环境", "DEV"));
        mDeviceId = findViewById(R.id.deviceid);
        domainSpinner = findViewById(R.id.active_env_spinner);
        domainSpinner.setOnItemSelectedListener(this);
        domainSpinner.setAdapter(new ArrayAdapter<MyPair>(this, android.R.layout.simple_list_item_1, runEnvList));

        SharedPreferences sp = SplashActivity.instance.getPreferences(MODE_PRIVATE);
        int domainPosition = sp.getInt("domainPosition", 0);
        domainSpinner.setSelection(domainPosition);
        serialNoEdit = findViewById(R.id.serial_no_edit);
        XsFaceSDKAuthConfig authConfig = XsFaceSDKInitHelper.getAuthConfig();
        if (authConfig != null && authConfig.getSerialNo() != null)
            serialNoEdit.setText(authConfig.getSerialNo());
        appkeyEdit = findViewById(R.id.appkey_edit);
        if (authConfig != null && authConfig.getAppkey() != null)
            appkeyEdit.setText(authConfig.getAppkey());
        secretkeyEdit = findViewById(R.id.secret_edit);
        if (authConfig != null && authConfig.getSecurityKey() != null)
            secretkeyEdit.setText(authConfig.getSecurityKey());
        tokenEdit = findViewById(R.id.token_edit);
        if (authConfig != null && authConfig.getToken() != null)
            tokenEdit.setText(authConfig.getToken());
        authRadioGroup = findViewById(R.id.group_auth);
        authRadioGroup.setOnCheckedChangeListener(this);

        activateButton = findViewById(R.id.active_button);
        activateButton.setOnClickListener(this);

        serialLayout = findViewById(R.id.serial_no_group);

        appkeyLayyout = findViewById(R.id.appkey_group);
        appkeyLayyout.setVisibility(View.INVISIBLE);

        secretkeyLayout = findViewById(R.id.secretkey_group);
        secretkeyLayout.setVisibility(View.INVISIBLE);

        tokenLayout = findViewById(R.id.token_group);
        tokenLayout.setVisibility(View.INVISIBLE);

        chipkeyLayout = findViewById(R.id.chipkey_group);
        chipkeyLayout.setVisibility(View.INVISIBLE);

        authRadioGroup.check(R.id.serial_auth);
        JSONObject deviceInfo = null;
        try {
            deviceInfo=new JSONObject(XsFaceSDK.getDeviceInfo());
            String displayInfo = String.format("device_id：%s\nbuild_serial：%s\ncpu_serial：%s\nandroid_id：%s\nmanufacturer：%s", XsFaceSDK.getDeviceIdForce(), deviceInfo.getString("build_serial"), deviceInfo.getString("cpu_serial"), deviceInfo.getString("android_id"), deviceInfo.getString("manufacturer"));
            Log.i("InputAuthInfoActivity", displayInfo);
            mDeviceId.setText(displayInfo);

        } catch (JSONException e) {
        }
    }

    @Override
    public void onClick(View v) {
        if (v == activateButton) {
            switch (authType) {
                case SERIAL_AUTH:
                    //序列号激活
                    XsFaceSDKInitHelper.getAuthConfig().configSerialAuthInfo(serialNoEdit.getText().toString());
                    break;
                case SECRETKEY_AUTH:
                    //SecretKey激活
                    XsFaceSDKInitHelper.getAuthConfig().configSecKeyAuthInfo(appkeyEdit.getText().toString(), secretkeyEdit.getText().toString(), isSdkYearly);
                    break;
                case TOKEN_AUTH:
                    //Token激活
                    XsFaceSDKInitHelper.getAuthConfig().configTokenAuthInfo(appkeyEdit.getText().toString(), tokenEdit.getText().toString(), isSdkYearly);
                    break;
                case CHIP_AUTH:
                    //加密芯片激活
                    XsFaceSDKInitHelper.getAuthConfig().configChipAuthInfo();
                    break;
            }
            SplashActivity.instance.initSDK();
            finish();
        }
    }

    private void refreshInputVisible(int checkedId) {
        switch (checkedId) {
            case R.id.serial_auth:
                serialLayout.setVisibility(View.VISIBLE);
                appkeyLayyout.setVisibility(View.INVISIBLE);
                secretkeyLayout.setVisibility(View.INVISIBLE);
                tokenLayout.setVisibility(View.INVISIBLE);
                chipkeyLayout.setVisibility(View.INVISIBLE);
                break;
            case R.id.sec_key_auth:
                appkeyLayyout.setVisibility(View.VISIBLE);
                secretkeyLayout.setVisibility(View.VISIBLE);
                tokenLayout.setVisibility(View.INVISIBLE);
                chipkeyLayout.setVisibility(View.INVISIBLE);
                serialLayout.setVisibility(View.INVISIBLE);
                ;
                break;
            case R.id.token_auth:
                appkeyLayyout.setVisibility(View.VISIBLE);
                tokenLayout.setVisibility(View.VISIBLE);
                secretkeyLayout.setVisibility(View.INVISIBLE);
                chipkeyLayout.setVisibility(View.INVISIBLE);
                serialLayout.setVisibility(View.INVISIBLE);
                break;
            case R.id.chip_auth:
                //加密芯片授权方式
                chipkeyLayout.setVisibility(View.VISIBLE);
                serialLayout.setVisibility(View.INVISIBLE);
                appkeyLayyout.setVisibility(View.INVISIBLE);
                secretkeyLayout.setVisibility(View.INVISIBLE);
                tokenLayout.setVisibility(View.INVISIBLE);
                break;
        }

    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        refreshInputVisible(checkedId);
        XsFaceSDKAuthConfig config = XsFaceSDKInitHelper.getAuthConfig();
        int id = group.getCheckedRadioButtonId();
        switch (group.getCheckedRadioButtonId()) {
            case R.id.serial_auth:
                //序列号授权方式
                if (config != null) {
                    if (config.getSerialNo() != null && !config.getSerialNo().isEmpty())
                        serialNoEdit.setText(config.getSerialNo());
                }
                authType = SERIAL_AUTH;
                break;
            case R.id.sec_key_auth:
                //appkey+secretKey授权方式
                if (config != null) {
                    if (config.getAppkey() != null && !config.getAppkey().isEmpty())
                        appkeyEdit.setText(config.getAppkey());
                    if (config.getSecurityKey() != null && !config.getSecurityKey().isEmpty())
                        secretkeyEdit.setText(config.getSecurityKey());
                }
                authType = SECRETKEY_AUTH;
                break;
            case R.id.token_auth:
                //appkey+token授权方式
                if (config != null) {
                    if (config.getAppkey() != null && !config.getAppkey().isEmpty())
                        appkeyEdit.setText(config.getAppkey());
                    if (config.getToken() != null && !config.getToken().isEmpty())
                        tokenEdit.setText(config.getToken());
                }
                authType = TOKEN_AUTH;
                break;
            case R.id.chip_auth:
                //加密芯片授权方式
                authType = CHIP_AUTH;
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent == domainSpinner) {
            XsFaceSDKAuthConfig.setDomain(runEnvList.get(position).second);
            SharedPreferences sp = SplashActivity.instance.getPreferences(MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt("domainPosition", position);
            editor.commit();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onBackPressed() {
        //用户按返回键时，退出app
        super.onBackPressed();
        SplashActivity.instance.finish();
        System.exit(0);
    }
}
