package com.xinshi.android.face.demo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.xinshi.android.face.demo.sync.LanHttpServer;
import com.xinshi.android.xsfacesdk.helper_v3.XsFaceSDKFaceLibHelper;
import com.xinshi.android.xsfacesdk.network.protocol.JSONRequest;
import com.xinshi.android.xsfacesdk.network.protocol.JSONResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/***
 * 本地HttpServer，服务启动后，可配合python脚本对底库进行管理
 */
public class LocalServerActivity extends BaseActivity {
    private static final String TAG = LocalServerActivity.class.getName();
    TextView logView;
    Button stopButton, startButton, returnButton, clearDeletedPerson;
    EditText portEdit;
    MyWebServer webServer;


    /**
     * 本地服务器：商户可参考{@link com.xinshi.android.face.demo.sync.LanHttpServer}在设备上启动Web服务器
     * 本地同步客户端：商户可参考{@link com.xinshi.android.face.demo.sync.LanPersonSyncClient}在设备上启动同步客户端
     * 使用时：1台设备启动`WebServer`作为同步服务，其他设备启动`LanPersonSyncClient`作为同步客户端
     */
    private class MyWebServer extends LanHttpServer {
        /**
         * 主构造函数，也用来启动http服务
         */
        public MyWebServer(int port) {
            super(port);
        }

        /**
         * 解析的主入口函数，所有请求从这里进，也从这里出
         */
        @SuppressLint("DefaultLocale")
        @Override
        public Response serve(IHTTPSession session) {
            if (session.getMethod().equals(Method.POST) || session.getMethod().equals(Method.GET)) {
                long start = System.currentTimeMillis();
                String url = session.getUri();
                try {
                    //json参数
                    JSONObject json = parseJsonBody(session);
                    //url参数
                    Map<String, String> urlParams = session.getParms();
                    for (Map.Entry<String, String> entry : urlParams.entrySet()) {
                        json.put(entry.getKey(), entry.getValue());
                    }
                    if (url.startsWith("/")) {
                        url = url.substring(1);
                    }
                    String[] urls = url.split("/");
                    if (urls.length == 0) {
                        throw new Exception(String.format("Url[%s]不正确", url));
                    }
                    String command = urls[0];
                    JSONRequest request = new JSONRequest("", command, json);
                    //其他非JSON请求
                    if (request.getCommand().equals(JSONRequest.COMMAND_DOWNLOAD_FACE_IMG)) {
                        byte[] res = (byte[]) getFaceServerExecutor().executeNotJson(request);
                        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(res);
                        doLogMessage(String.format("%s success(duration=%d).\r\n", url, System.currentTimeMillis() - start));
                        return newFixedLengthResponse(Response.Status.OK, "image/jpeg", byteArrayInputStream, res.length);
                    } else {
                        //通用JSON请求
                        JSONResponse response = getFaceServerExecutor().execute(request);
                        if (response.getResponseCode() == com.xinshi.android.xsfacesdk.network.protocol.Response.RESPONSE_OK) {
                            doLogMessage(String.format("%s success(duration=%d).\r\n", url, System.currentTimeMillis() - start));
                        } else {
                            doLogMessage(String.format("%s error(duration=%d): errcode=%d, message=%s\r\n", url,
                                    System.currentTimeMillis() - start, response.getResponseCode(), response.getErrorMessage()));
                        }
                        return newFixedLengthResponse(Response.Status.OK, "text/json", response.getData().toString());
                    }
                } catch (Throwable e) {
                    Log.d(TAG, "error", e);
                    JSONObject r = new JSONObject();
                    try {
                        r.put("success", false);
                        r.put("error", e.toString());
                    } catch (JSONException e1) {
                    }
                    doLogMessage(String.format("%s failure(duration=%d): %s.\r\n", url, System.currentTimeMillis() - start, e.toString()));
                    return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", r.toString());
                }
            } else {
                String msg = "<html><body><h1>Face Local server</h1>\n" + "</body></html>\n";
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/html", msg);
            }
        }
    }

    static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private void doLogMessage(String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logView.append(sdf.format(new Date()) + " " + msg);
                int scrollAmount = logView.getLayout().getLineTop(logView.getLineCount())
                        - logView.getHeight();
                if (scrollAmount > 0)
                    logView.scrollTo(0, scrollAmount);
                else
                    logView.scrollTo(0, 0);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_server);
        portEdit = findViewById(R.id.bind_port_edit);
        startButton = findViewByIdAndSetListener(R.id.start_button);
        stopButton = findViewByIdAndSetListener(R.id.stop_button);
        returnButton = findViewByIdAndSetListener(R.id.return_button);
        clearDeletedPerson = findViewByIdAndSetListener(R.id.clear_deleted_person);
        logView = findViewById(R.id.log_view);
        logView.setMovementMethod(ScrollingMovementMethod.getInstance());
        startServer();
    }

    @Override
    public void onClick(View v) {
        if (v == startButton) {
            startServer();
        } else if (v == stopButton) {
            stopServer();
        } else if (v == returnButton) {
            finish();
        } else if (v == clearDeletedPerson) {
            clearDeletedPersons();
        }
    }

    private void clearDeletedPersons() {
        try {
            clearDeletedPerson.setEnabled(false);
            //清空已删除人员列表
            Log.i(TAG, "清空已删除人员列表");
            XsFaceSDKFaceLibHelper.clearDeletedPersons();
        } finally {
            clearDeletedPerson.setEnabled(true);
        }
    }

    private void stopServer() {
        if (webServer != null) {
            webServer.stop();
            webServer = null;
            logView.append("server stop.\r\n");
        }
    }

    private void startServer() {
        if (webServer == null) {
            try {
                int port = Integer.valueOf(portEdit.getText().toString());
                webServer = new MyWebServer(port);
                webServer.start();
                logView.append(String.format("server started: %s \r\n", webServer.getFaceServerExecutor().getDomain()));
            } catch (Throwable e) {
                Log.e(TAG, String.format("HttpServer启动失败[%s]", e.toString()));
                showToast(e.toString());
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webServer != null) {
            webServer.stop();
            webServer = null;
        }
    }
}
