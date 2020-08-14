package com.xinshi.android.face.demo;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Toast;

import com.xinshi.android.face.data.FaceData;
import com.xinshi.android.face.data.FaceImageData;
import com.xinshi.android.face.data.SearchedPerson;
import com.xinshi.android.face.db.DbPerson;
import com.xinshi.android.face.demo.util.UITools;
import com.xinshi.android.face.image.FaceImage;
import com.xinshi.android.face.view.SingleCameraView;
import com.xinshi.android.xsfacesdk.helper_v3.XsFaceSDKCameraHelper;
import com.xinshi.android.xsfacesdk.helper_v3.XsFaceSDKFaceLibHelper;
import com.xinshi.android.xsfacesdk.helper_v3.XsFaceSDKHelper;
import com.xinshi.android.xsfacesdk.helper_v3.XsFaceSDKSceneHelper;
import com.xinshi.android.xsfacesdk.scene.FaceRegisterScene;

import java.io.IOException;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FaceRegisterFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FaceRegisterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FaceRegisterFragment extends Fragment implements CompoundButton.OnCheckedChangeListener,View.OnClickListener,FaceRegisterScene.FaceRegisterSceneCallback{
    final static String TAG = "FaceRegisterFragment";
    CheckBox faceDeduplicationButton;
    Button returnButton;

    SingleCameraView visCameraView;
    FaceRegisterScene.FaceRegisterSceneParams sceneParams;
    FaceRegisterScene faceRegisterScene;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public FaceRegisterFragment() {
        // Required empty public constructor
    }

    public void stopScene() {
        try {
            faceRegisterScene.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void startScene() {
        try {
            faceRegisterScene.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FaceRegisterFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FaceRegisterFragment newInstance(String param1, String param2) {
        FaceRegisterFragment fragment = new FaceRegisterFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("FaceRegisterFragment", "onCreate");
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_face_register, container, false);

        faceDeduplicationButton = findViewByIdAndSetListener(R.id.face_deduplication_btton,view);
        returnButton = findViewByIdAndSetListener(R.id.return_button,view);
        visCameraView = view.findViewById(R.id.camera_view);


        Log.i("FaceRegisterFragment", "onCreateView");


        return view;
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
        Log.i("FaceRegisterFragment", "onAttach");
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.i("FaceRegisterFragment", "onDetach");
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        if (v == returnButton) {
            this.getActivity().finish();
        }
    }
    @Override
    public void onStart(){
        super.onStart();
        //初始化人脸识别场景
        initFaceRegisterScene();
    }
    @Override
    public void onPause() {
        super.onPause();
        Log.i("FaceRegisterFragment", "onPause");
        try {
            faceRegisterScene.stop();
        } catch (IOException e) {
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        Log.i("FaceRegisterFragment", "onResume");
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.i("FaceRegisterFragment", "onDestroy");
    }
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == faceDeduplicationButton) {
            if (sceneParams.isSearchExists != isChecked) {
                sceneParams.isSearchExists = faceDeduplicationButton.isChecked();
                faceRegisterScene.resetParams(sceneParams);
            }
        }
    }

    @Override
    public void onRegister(FaceData faceData, float[] features, FaceImage faceImage, SearchedPerson searchedPerson) {
        //显示对话框，暂停识别
        faceRegisterScene.pause();
        if (searchedPerson == null) {
            //底库中不存在识别到的人脸，提示新增人脸
            UITools.showEditDialog(this.getActivity(), "输入姓名", null,
                    new UITools.EditDialogCallback() {
                        @Override
                        public void onOk(String text) {
                            DbPerson person = null;
                            try {
                                person = XsFaceSDKFaceLibHelper.putPersonImage(text, 0, "",
                                        new FaceImageData(faceImage, faceData), features, null, null, 0, false);
                            } catch (Throwable e) {
                                if (person != null) {
                                    try {
                                        XsFaceSDKFaceLibHelper.removePerson(person);
                                    } catch (Throwable ex) {
                                    }
                                }
                                showToast(e.toString());
                            } finally {
                                faceRegisterScene.resume();//恢复识别
                            }
                        }

                        @Override
                        public void onCancel() {
                            faceRegisterScene.resume();//恢复识别
                        }
                    });
        } else {
            //底库中已存在识别到的人脸，提示是否替换
            UITools.showReplaceFaceDialog(this.getActivity(), "警告", "人脸已经存在，需要替换吗?",
                    new UITools.ReplaceFaceDialogCallback() {
                        @Override
                        public void onOk1() {
                            try {
                                //替换人脸1
                                String personCode = searchedPerson.getPerson().getPersonCode();
                                XsFaceSDKFaceLibHelper.putPersonImage(personCode, null, null,
                                        new FaceImageData(faceImage, faceData), features, null, null, 0, false);
                            } catch (Throwable e) {
                                showToast(e.toString());
                            } finally {
                                faceRegisterScene.resume();//恢复识别
                            }
                        }

                        @Override
                        public void onOk2() {
                            try {
                                //替换人脸2
                                String personCode = searchedPerson.getPerson().getPersonCode();
                                XsFaceSDKFaceLibHelper.putPersonImage(personCode, null, null, null, null,
                                        new FaceImageData(faceImage, faceData), features, 0, false);
                            } catch (Throwable e) {
                                showToast(e.toString());
                            } finally {
                                faceRegisterScene.resume();//恢复识别
                            }
                        }

                        @Override
                        public void onCancel() {
                            faceRegisterScene.resume();
                        }
                    });
        }
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

    <T extends View> T findViewByIdAndSetListener(@IdRes int id, View parent) {
        T view = parent.findViewById(id);
        if (view instanceof CheckBox) {
            ((CheckBox) view).setOnCheckedChangeListener(this);
        }
        if (view instanceof RadioButton) {
            ((RadioButton) view).setOnCheckedChangeListener(this);
        } else if (view instanceof Button) {
            view.setOnClickListener(this);
        }
        return view;
    }

    /***
     * 初始化人脸识别场景
     */
    private void initFaceRegisterScene() {
        try {
            //设置可见光摄像头View
            visCameraView.setCamera(XsFaceSDKCameraHelper.getVisCamera());
            //设置人脸注册场景参数
            sceneParams = new FaceRegisterScene.FaceRegisterSceneParams();
            sceneParams.qualityDetectConfig.blueThreshold = 0.7f;
            sceneParams.qualityDetectConfig.occ_detect = false;
            sceneParams.maxSearchFailTimes = 1;
            //底库人脸建议大一点
            sceneParams.minFaceSize = 100;
            faceDeduplicationButton.setChecked(sceneParams.isSearchExists);
            //人脸注册时，强制启用单人脸追踪
            XsFaceSDKHelper.getSDKConfig().setSingleTrack(true);
            //创建人脸注册场景
            faceRegisterScene = XsFaceSDKSceneHelper.createFaceRegisterRecScene(visCameraView, sceneParams, this);
        } catch (Throwable e) {
            Log.d(TAG, "init failure:", e);
        }
    }
    protected void showToast(final String msg) {
        final Activity activity = this.getActivity();
        //主线程运行
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(activity, msg, Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }
}
