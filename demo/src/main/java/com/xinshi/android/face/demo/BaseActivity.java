package com.xinshi.android.face.demo;


import android.app.Activity;
import android.content.Intent;
import android.support.annotation.IdRes;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

public class BaseActivity extends Activity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener, AdapterView.OnItemSelectedListener {

    protected void showFileChooser(String msg, int code) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, msg), code);
        } catch (android.content.ActivityNotFoundException ex) {
            showToast("Please install a File Manager.");
        }
    }

    <T extends View> T findViewByIdAndSetListener(@IdRes int id) {
        T view = findViewById(id);

        if (view instanceof CheckBox) {
            ((CheckBox) view).setOnCheckedChangeListener(this);
        }
        if (view instanceof RadioButton) {
            ((RadioButton) view).setOnCheckedChangeListener(this);
        } else if (view instanceof Button) {
            view.setOnClickListener(this);
        } else if (view instanceof Spinner) {
            ((Spinner) view).setOnItemSelectedListener(this);
        }
        return view;
    }

    public void showToast(final String msg) {
        final Activity activity = this;
        //主线程运行
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(activity, msg, Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
