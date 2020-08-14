package com.xinshi.android.face.demo.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.xinshi.android.face.demo.R;

public class UITools {
    public interface AlertDialogCallback {
        default void onOk() {

        }
    }

    public interface ConfirmDialogCallback {
        default void onOk() {

        }

        default void onCancel() {

        }
    }

    /***
     * 人脸注册-人脸替换操作回调
     */
    public interface ReplaceFaceDialogCallback {
        default void onOk1() {

        }

        default void onOk2() {

        }

        default void onCancel() {

        }
    }

    /***
     * 人脸注册-人员信息编辑操作回调
     */
    public interface EditDialogCallback {
        default void onOk(String text) {

        }

        default void onCancel() {

        }
    }

    /***
     * 底库管理-确认操作回调
     */
    static public void showConfirmDialog(Context context, String title, String message, ConfirmDialogCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                if (callback != null)
                    callback.onOk();
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (callback != null)
                    callback.onCancel();
                dialog.cancel();
            }
        });
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                if (callback != null)
                    callback.onCancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /***
     * 人脸注册-弹出人脸替换对话框
     */
    static public void showReplaceFaceDialog(Context context, String title, String message, ReplaceFaceDialogCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(title);
        builder.setMessage(message);

        builder.setNegativeButton("替换人脸1", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                if (callback != null)
                    callback.onOk1();
                dialog.dismiss();
            }
        });
        builder.setPositiveButton("替换人脸2", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                if (callback != null)
                    callback.onOk2();
                dialog.dismiss();
            }
        });
        builder.setNeutralButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (callback != null)
                    callback.onCancel();
                dialog.cancel();
            }
        });
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                if (callback != null)
                    callback.onCancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    static public void showAlertDialog(Context context, String title, String message, AlertDialogCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                if (callback != null)
                    callback.onOk();
                dialog.dismiss();
            }
        });

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                if (callback != null)
                    callback.onOk();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /***
     * 人脸注册-弹出人员信息编辑对话框
     */
    static public void showEditDialog(Context context, String title, String text, EditDialogCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(title);
        final EditText input = new EditText(context);
        if (text != null)
            input.setText(text);
        //input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                if (callback != null)
                    callback.onOk(input.getText().toString());
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (callback != null)
                    callback.onCancel();
                dialog.cancel();
            }
        });
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                if (callback != null)
                    callback.onCancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }


    static public void showImageDialog(Context context, String title, String message, Bitmap bitmap, AlertDialogCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(title);
        builder.setMessage(message);
        LayoutInflater factory = LayoutInflater.from(context);
        final View view = factory.inflate(R.layout.image_view_dialog, null);
        ImageView imageView = view.findViewById(R.id.dialog_imageview);
        imageView.setImageBitmap(bitmap);
        builder.setView(view);

        builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (callback != null)
                    callback.onOk();
                dialog.cancel();
            }
        });
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                if (callback != null)
                    callback.onOk();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    static public void showImageDialog2(Context context, String title, String message, Bitmap bitmap, Bitmap bitmap1, AlertDialogCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(title);
        builder.setMessage(message);
        LayoutInflater factory = LayoutInflater.from(context);
        final View view = factory.inflate(R.layout.image_view_dialog2, null);
        ImageView imageView = view.findViewById(R.id.dialog_imageview);
        imageView.setImageBitmap(bitmap);
        ImageView imageView2 = view.findViewById(R.id.dialog_imageview1);
        imageView2.setImageBitmap(bitmap1);
        builder.setView(view);

        builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (callback != null)
                    callback.onOk();
                dialog.cancel();
            }
        });
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                if (callback != null)
                    callback.onOk();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
