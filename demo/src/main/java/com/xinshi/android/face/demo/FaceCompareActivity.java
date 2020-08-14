package com.xinshi.android.face.demo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.xinshi.android.face.config.EnvConfig;
import com.xinshi.android.face.demo.util.DemoUtils;
import com.xinshi.android.face.exceptions.FaceException;
import com.xinshi.android.face.image.FaceImage;
import com.xinshi.android.face.jni.Tool;
import com.xinshi.android.xsfacesdk.helper_v3.XsFaceSDKHelper;

import java.io.File;

/***
 * 人脸比对
 */
public class FaceCompareActivity extends BaseActivity {
    Button addImageButton1, addImageButton2;
    ImageView imageView1, imageView2;
    TextView faceCompareResultView;
    FaceImage faceImage1, faceImage2;
    Button returnButton;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_compare);

        returnButton = findViewByIdAndSetListener(R.id.return_button);
        addImageButton1 = findViewByIdAndSetListener(R.id.add_image1_button);
        addImageButton2 = findViewByIdAndSetListener(R.id.add_image2_button);
        imageView1 = findViewById(R.id.image_view1);
        imageView2 = findViewById(R.id.image_view2);
        faceCompareResultView = findViewById(R.id.face_compare_result_view);
    }

    @Override
    public void onClick(View v) {
        /*
        Bitmap bitmap1 = Tool.loadBitmap(new File(EnvConfig.getRootPath()+"/c1.jpeg"));
        Bitmap bitmap2 = Tool.loadBitmap(new File(EnvConfig.getRootPath()+"/c2.jpeg"));
        double sim = 0;
        try {
            FaceImage faceImage1=XsFaceSDKHelper.bitmapToFaceImage(bitmap1);
            FaceImage faceImage2=XsFaceSDKHelper.bitmapToFaceImage(bitmap2);
            sim = XsFaceSDKHelper.faceCompare(faceImage1, faceImage2);
            Log.i(FaceCompareActivity.class.getSimpleName(),String.format("相似度: %f", sim));
        } catch (FaceException e) {
            e.printStackTrace();
        }*/
        if (v == addImageButton1) {
            showFileChooser("请选择图片1", 1);
        } else if (v == addImageButton2) {
            showFileChooser("请选择图片2", 2);
        } else if (returnButton == v) {
            finish();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            try {
                Uri uri = data.getData();
                String path = DemoUtils.getPath(this, uri);
                if (path != null) {
                    Bitmap bitmap = Tool.loadBitmap(new File(path));
                    if (requestCode == 1) {
                        imageView1.setImageBitmap(bitmap);
                        faceImage1 = XsFaceSDKHelper.bitmapToFaceImage(bitmap);
                    } else if (requestCode == 2) {
                        imageView2.setImageBitmap(bitmap);
                        faceImage2 = XsFaceSDKHelper.bitmapToFaceImage(bitmap);
                    }
                    if (faceImage1 != null && faceImage2 != null) {
                        Tool.saveToJpeg(faceImage1.toBitmap(), new File(String.format("%s/aaaa.jpg", EnvConfig.getRootPath())));
                        Tool.saveToJpeg(faceImage2.toBitmap(), new File(String.format("%s/bbbb.jpg", EnvConfig.getRootPath())));
                        double sim = XsFaceSDKHelper.faceCompare(faceImage1, faceImage2);
                        faceCompareResultView.setText(String.format("相似度: %f", sim));
                    }
                }
            } catch (Throwable e) {
                showToast(e.toString());
            }
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }
}
