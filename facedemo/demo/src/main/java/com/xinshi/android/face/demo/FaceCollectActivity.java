package com.xinshi.android.face.demo;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.xinshi.android.face.data.FaceData;
import com.xinshi.android.face.demo.util.UITools;
import com.xinshi.android.face.image.FaceImage;
import com.xinshi.android.face.view.SingleCameraView;
import com.xinshi.android.xsfacesdk.helper_v3.XsFaceSDKCameraHelper;
import com.xinshi.android.xsfacesdk.helper_v3.XsFaceSDKSceneHelper;
import com.xinshi.android.xsfacesdk.scene.FaceCollectScene;

import java.io.IOException;

/***
 * 人脸采集场景
 */
public class FaceCollectActivity extends BaseActivity implements FaceCollectScene.FaceCollectSceneCallback {
    static final String TAG = "FaceCollectActivity";
    SingleCameraView visCameraView;
    FaceCollectScene.FaceCollectSceneParams sceneParams;
    FaceCollectScene faceCollectScene;
    Button returnButton;
    CheckBox continuousCollectButton;
    ImageView imageView[];
    int currentImageViewIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_collect);

        returnButton = findViewByIdAndSetListener(R.id.return_button);
        continuousCollectButton = findViewByIdAndSetListener(R.id.continuous_collect_button);
        visCameraView = findViewById(R.id.camera_view);
        imageView = new ImageView[8];
        imageView[0] = findViewByIdAndSetListener(R.id.image_view1);
        imageView[1] = findViewByIdAndSetListener(R.id.image_view2);
        imageView[2] = findViewByIdAndSetListener(R.id.image_view3);
        imageView[3] = findViewByIdAndSetListener(R.id.image_view4);
        imageView[4] = findViewByIdAndSetListener(R.id.image_view5);
        imageView[5] = findViewByIdAndSetListener(R.id.image_view6);
        imageView[6] = findViewByIdAndSetListener(R.id.image_view7);
        imageView[7] = findViewByIdAndSetListener(R.id.image_view8);
        Display display = this.getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int w = Math.min(metrics.widthPixels, metrics.heightPixels) / 8;
        for (ImageView view : imageView) {
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.width = w;
            layoutParams.height = w;
            view.setLayoutParams(layoutParams);
        }
        try {
            visCameraView.setCamera(XsFaceSDKCameraHelper.getVisCamera());
            sceneParams = new FaceCollectScene.FaceCollectSceneParams();
            sceneParams.minFaceSize = 100;
            faceCollectScene = XsFaceSDKSceneHelper.createFaceCollectRecScene(visCameraView, sceneParams, this);
        } catch (Throwable e) {
            Log.d(TAG, "init failure:", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            faceCollectScene.stop();
        } catch (IOException e) {
        }
    }

    @Override
    public void onClick(View v) {
        if (v == returnButton) {
            finish();
        }
    }

    @Override
    public void onCollectFace(FaceImage image, FaceData faceData, Bitmap bitmap) {
        if (continuousCollectButton.isChecked()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (currentImageViewIndex >= imageView.length) currentImageViewIndex = 0;
                    imageView[currentImageViewIndex].setImageBitmap(bitmap);
                    currentImageViewIndex++;
                }
            });
        } else {
            faceCollectScene.pause();
            UITools.showImageDialog(this, "采集到照片", "", bitmap, new UITools.AlertDialogCallback() {
                @Override
                public void onOk() {
                    faceCollectScene.resume();
                }
            });
        }
    }
}
