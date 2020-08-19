package com.xinshi.android.face.demo;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Spinner;

import com.xinshi.android.face.data.LivenessDetectMode;
import com.xinshi.android.face.data.SearchedPerson;
import com.xinshi.android.face.demo.util.DemoUtils;
import com.xinshi.android.face.exceptions.FaceException;
import com.xinshi.android.face.view.DoubleCameraView;
import com.xinshi.android.xsfacesdk.XsSdkEnvConfig;
import com.xinshi.android.xsfacesdk.helper_v3.XsFaceSDKCameraHelper;
import com.xinshi.android.xsfacesdk.helper_v3.XsFaceSDKSceneHelper;
import com.xinshi.android.xsfacesdk.scene.CommonFaceRecScene;
import com.xinshi.android.xsfacesdk.scene.SpecifiedFaceRecScene;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FaceRecFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FaceRecFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FaceRecFragment extends Fragment implements CompoundButton.OnCheckedChangeListener, View.OnClickListener, AdapterView.OnItemSelectedListener,  CommonFaceRecScene.CommonFaceRecSceneCallback{
    static final String TAG = "FaceRecFragment";
    Button returnButton;
    Spinner livenessModeSpinner;
    CheckBox reRecCheckbox;
    ArrayAdapter<LivenessDetectMode> livenessDetectModeArrayAdapter;
    CheckBox meteringCheckbox;
    CheckBox repeatCloseOpenCamera;


    //双目摄像头View
    DoubleCameraView visCameraView;
    //识别参数,在ParamsConfigActivity配置识别参数
    SpecifiedFaceRecScene.SpecifiedFaceRecSceneParams sceneParams;
    //识别场景
    static CommonFaceRecScene faceRecScene;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public FaceRecFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FaceRecFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FaceRecFragment newInstance(String param1, String param2) {
        FaceRecFragment fragment = new FaceRecFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("FaceRecFragment", "onCreate");
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        super.onCreate(savedInstanceState);
        Log.i("FaceRecFragment", "onCreateView");

        View view= inflater.inflate(R.layout.fragment_face_rec, container, false);

        visCameraView = view.findViewById(R.id.camera_view);
        returnButton = findViewByIdAndSetListener(R.id.return_button,view);
        livenessModeSpinner = findViewByIdAndSetListener(R.id.liveness_mode_spinner,view);
        livenessDetectModeArrayAdapter = new ArrayAdapter<LivenessDetectMode>(this.getActivity(), android.R.layout.simple_list_item_1, DemoUtils.getSupportLivenessModes(XsFaceSDKCameraHelper.getNirCamera() != null));
        livenessModeSpinner.setAdapter(livenessDetectModeArrayAdapter);
        meteringCheckbox = findViewByIdAndSetListener(R.id.metering_checkbox,view);
        reRecCheckbox = findViewByIdAndSetListener(R.id.rerec_checkbox,view);
        repeatCloseOpenCamera=findViewByIdAndSetListener(R.id.repeat_checkbox,view);
        return view;
    }


    /***
     * 初始化通用识别场景
     */
    private void initFaceRecScene(float[] feature) throws FaceException {
        if (feature == null) {
            //创建通用的人脸识别场景
            faceRecScene = XsFaceSDKSceneHelper.createCommonFaceRecScene(visCameraView, sceneParams, this);
        } else {
            //创建指定人脸图片的识别场景
            faceRecScene = XsFaceSDKSceneHelper.createSpecifiedFaceRecScene(visCameraView, sceneParams, this);
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.i("FaceRecFragment", "onAttach");
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("识别FaceRecFragment", "onResume");
    }
    @Override
    public void onDestroy(){
        super.onDestroy();

        Log.i("FaceRecFragment", "onDestory");
    }
    @Override
    public void onDetach() {
        super.onDetach();
        Log.i("FaceRecFragment", "onDetach");
        mListener = null;

    }
    private void initFaceRecScene() {
        try {
            visCameraView.setCamera(XsFaceSDKCameraHelper.getVisCamera());
            visCameraView.setNirCamera(XsFaceSDKCameraHelper.getNirCamera());

            //ParamsConfigActivity配置识别参数
            sceneParams = ParamsConfigActivity.getRecSceneParams(null);
            //初始化场景
            initFaceRecScene(null);

            meteringCheckbox.setChecked(sceneParams.autoMetering);
            livenessModeSpinner.setSelection(livenessDetectModeArrayAdapter.getPosition(sceneParams.livenessDetectMode));
        } catch (Throwable e) {
            Log.d(TAG, "init failure:", e);
        }
    }
    public void stopScene() {
        try {
            faceRecScene.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void startScene() {
        try {
            faceRecScene.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onStart(){
        super.onStart();
        Log.i("FaceRecFragment", "onStart");
        initFaceRecScene();
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.e("识别FaceRecFragment", "onPause");
        try {
            //停止预览，并释放摄像头
            faceRecScene.stop();
            try {
                this.finalize();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        } catch (Exception e) {
        }
    }
    @Override
    public void onClick(View v) {
        if (v == returnButton) {
            this.getActivity().finish();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent == livenessModeSpinner) {
            LivenessDetectMode livenessDetectMode = (LivenessDetectMode) livenessModeSpinner.getSelectedItem();
            if (livenessDetectMode != sceneParams.livenessDetectMode) {
                sceneParams.livenessDetectMode = livenessDetectMode;
                faceRecScene.resetParams(sceneParams);
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == meteringCheckbox) {
            if (sceneParams.autoMetering != meteringCheckbox.isChecked()) {
                sceneParams.autoMetering = meteringCheckbox.isChecked();
                faceRecScene.resetParams(sceneParams);
            }
        } else if (buttonView == reRecCheckbox) {
            if ((isChecked && sceneParams.reRecInterval == 0) ||
                    (!isChecked && sceneParams.reRecInterval > 0)) {
                if (isChecked)
                    sceneParams.reRecInterval = 5000;
                else
                    sceneParams.reRecInterval = 0;
                faceRecScene.resetParams(sceneParams);
            }
        } else if (buttonView == repeatCloseOpenCamera) {
            if (isChecked) {
                timer.schedule(task,0,1000);
            }else{
                timer.cancel();
            }
        }

    }

    @Override
    public void onFaceRecComplete(CommonFaceRecScene.FaceRecSceneData sceneData, SearchedPerson searchedPerson, List<SearchedPerson> searchedPersons) {
        Log.e(TAG, String.format("识别成功：%s", searchedPerson.getPerson()));
        MainApplication.playSound(R.raw.rec_success);
    }

    @Override
    public void onRecStranger(CommonFaceRecScene.FaceRecSceneData sceneData, SearchedPerson searchedPerson) {
        Log.e(TAG, String.format("识别到陌生人：最相似的人 %s", searchedPerson != null ? searchedPerson.getPerson() : "无"));
        MainApplication.playSound(R.raw.detect_stranger);
    }

    @Override
    public void onLivenessAttack(CommonFaceRecScene.FaceRecSceneData sceneData, SearchedPerson searchedPerson, List<SearchedPerson> searchedPersons) {
        Log.e(TAG, String.format("攻击：疑似人员: %s", searchedPerson != null ? searchedPerson.getPerson() : "无"));
        MainApplication.playSound(R.raw.detect_attack);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
    <T extends View> T findViewByIdAndSetListener(@IdRes int id,View parent) {
        T view = parent.findViewById(id);
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

    int counter=0;
    Timer timer = new Timer();
    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            try {
                String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS ").format(new Date());
                if ((counter%8) == 0) {
                    Log.i(TAG, "open camera");
                    writeLog(time+" open camera\n");
                    //打开摄像头
                    faceRecScene.start();
                } else if ((counter%8) == 5) {
                    Log.i(TAG, "close camera");
                    writeLog(time +" close camera\n");
                    //关闭摄像头
                    faceRecScene.stop();
                }
            } catch (Exception e) {}
            counter++;
        }
    };

    private void writeLog(String log ){
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(String.format("%s/%s", XsSdkEnvConfig.getRootPath(), "repeatRecFace.log"), true);
            fileOutputStream.write(log.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
