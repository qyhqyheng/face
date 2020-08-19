package com.xinshi.android.face.demo;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;

public class FragmentFaceRecActivity extends FragmentActivity implements FaceRecFragment.OnFragmentInteractionListener,View.OnClickListener {
    Button backButton;
    Button regButton;
    Button recButton;
    private FaceRecFragment faceRecFragment;
    private FaceRegisterFragment faceRegisterFragment;

    FragmentManager fm;
    private int lastIndex=-1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_face_rec);
        initview();

    }
    private void initview(){
        backButton = (Button) findViewById(R.id.back_button);
        regButton = (Button) findViewById(R.id.reg_button);
        recButton = (Button) findViewById(R.id.rec_button);
        backButton.setOnClickListener(this);
        regButton.setOnClickListener(this);
        recButton.setOnClickListener(this);
        fm = getSupportFragmentManager();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
    @Override
    public void onClick(View v) {
        if (v == backButton) {
            finish();
        } else if (v == regButton) {
            setTabSelection(0);
        } else if (v == recButton) {
            setTabSelection(1);
        }
    }

    private void setTabSelection(int index){
        if (lastIndex == index) {
            return;
        }
        FragmentTransaction ft = fm.beginTransaction();
        hideFragment(ft);
        switch (index) {
            case 0:
                if(faceRegisterFragment==null){
                    faceRegisterFragment = new FaceRegisterFragment();
                    ft.add(R.id.fl, faceRegisterFragment);
                }else{
                    faceRegisterFragment.startScene();
                    ft.show(faceRegisterFragment);
                }

                break;
            case 1:

                if(faceRecFragment==null){
                    faceRecFragment = new FaceRecFragment();
                    ft.add(R.id.fl, faceRecFragment);
                }else{
                    faceRecFragment.startScene();
                    ft.show(faceRecFragment);
                }
                break;


        }
        ft.commit();
        lastIndex = index;
    }
    //用于隐藏fragment
    private void hideFragment(FragmentTransaction ft) {
        if (faceRecFragment != null) {
            faceRecFragment.stopScene();
            ft.hide(faceRecFragment);
        }
        if (faceRegisterFragment != null) {
            faceRegisterFragment.stopScene();
            ft.hide(faceRegisterFragment);
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (faceRecFragment != null) {
            faceRecFragment.stopScene();
        }
        if (faceRegisterFragment != null) {
            faceRegisterFragment.stopScene();
        }
    }
}
