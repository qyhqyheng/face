package com.xinshi.android.face.demo.sync;

import android.annotation.SuppressLint;
import android.util.Log;

import com.xinshi.android.xsfacesdk.network.FaceServerExecutor;
import com.xinshi.android.xsfacesdk.network.protocol.JSONRequest;
import com.xinshi.android.xsfacesdk.network.protocol.JSONResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

/***
 * Lan Http Server局域网Web服务器
 */
public class LanHttpServer extends NanoHTTPD {
    static final String TAG = LanHttpServer.class.getName();
    private FaceServerExecutor faceServerExecutor;

    /**
     * 主构造函数，也用来启动http服务
     *
     * @param port 端口号
     */
    public LanHttpServer(int port) {
        super(port);
        String domain = String.format("http://%s:%s", getHostIP(), port);
        faceServerExecutor = new FaceServerExecutor(domain);
    }

    public FaceServerExecutor getFaceServerExecutor() {
        return faceServerExecutor;
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
                Log.d(TAG, String.format("request,command[%s],jsonParam=[%s]\r\n", command, json));
                JSONRequest request = new JSONRequest("", command, json);
                //其他非JSON请求
                if (request.getCommand().equals(JSONRequest.COMMAND_DOWNLOAD_FACE_IMG)) {
                    byte[] res = (byte[]) faceServerExecutor.executeNotJson(request);
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(res);
                    return newFixedLengthResponse(Response.Status.OK, "image/jpeg", byteArrayInputStream, res.length);
                } else {
                    //通用JSON请求
                    JSONResponse response = faceServerExecutor.execute(request);
                    if (response.getResponseCode() == com.xinshi.android.xsfacesdk.network.protocol.Response.RESPONSE_OK) {
                        Log.d(TAG, String.format("%s success(duration=%d).\r\n", url, System.currentTimeMillis() - start));
                    } else {
                        Log.d(TAG, String.format("%s error(duration=%d): errcode=%d, message=%s\r\n", url,
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
                Log.d(TAG, String.format("%s failure(duration=%d): %s.\r\n", url, System.currentTimeMillis() - start, e.toString()));
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", r.toString());
            }
        } else {
            String msg = "<html><body><h1>Face Local server</h1>\n" + "</body></html>\n";
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/html", msg);
        }
    }

    protected JSONObject parseJsonBody(IHTTPSession session) throws IOException, JSONException {
        //json参数
        JSONObject json = new JSONObject();
        String contentLength = session.getHeaders().get("content-length");
        if (contentLength != null && contentLength.length() > 0) {
            int left = Integer.valueOf(contentLength);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] b = new byte[10240];
            int len;
            InputStream stream = session.getInputStream();
            while (left > 0 && (len = stream.read(b)) > 0) {
                baos.write(b, 0, len);
                left -= len;
            }
            if (baos.size() > 0) {
                json = new JSONObject(baos.toString("utf-8"));
            }
        }
        return json;
    }

    private String getHostIP() {
        String hostIp = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    String ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip)) {
                        hostIp = ia.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            Log.i(TAG, "SocketException", e);
        }
        return hostIp;
    }

}
