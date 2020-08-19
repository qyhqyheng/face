package com.xinshi.android.face.demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.xinshi.android.face.config.EnvConfig;
import com.xinshi.android.face.data.AddedPerson;
import com.xinshi.android.face.data.FaceData;
import com.xinshi.android.face.data.FaceImageData;
import com.xinshi.android.face.data.FaceQuality;
import com.xinshi.android.face.db.DbFaceItem;
import com.xinshi.android.face.db.DbPerson;
import com.xinshi.android.face.demo.util.DemoUtils;
import com.xinshi.android.face.demo.util.UITools;
import com.xinshi.android.face.exceptions.FaceException;
import com.xinshi.android.face.exceptions.FaceQualityException;
import com.xinshi.android.face.image.FaceImage;
import com.xinshi.android.face.jni.Tool;
import com.xinshi.android.xsfacesdk.XsFaceSDK;
import com.xinshi.android.xsfacesdk.XsFaceSearchLibrary;
import com.xinshi.android.xsfacesdk.helper_v3.XsFaceSDKFaceLibHelper;
import com.xinshi.android.xsfacesdk.helper_v3.XsFaceSDKHelper;
import com.xinshi.android.xsfacesdk.util.XsFaceSDKUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/***
 * 人脸底库管理
 */
public class FaceLibManagerActivity extends BaseActivity implements AbsListView.OnScrollListener {
    static final String TAG = "FaceLibManagerActivity";
    ListView faceListView;
    Button btnAddFace;
    FaceListViewAdapter listItemAdapter;
    Button queryPersonButton, returnButton;
    EditText searchEditText;
    TextView personTotalTextView;
    Button testRefreshFeatureButton, btnBatchAddFace;

    private static final int FILE_SELECT_CODE_ADD = 0;
    private static final int FILE_SELECT_CODE_EDIT1 = 1;
    private static final int FILE_SELECT_CODE_EDIT2 = 2;

    //批量导入人员操作的数据源目录名
    final String IMPORT_IMAGES_PATH = "import_images";
    final String IMPORT_FAIL_IMAGES_PATH = "import_fail_images";
    DbPerson selectedPerson;
    FaceImage<?> lastFaceImage;
    FaceData lastFaceData;
    float[] lastFacefeature;
    boolean queryCompleted = false;
    static AtomicBoolean isImporting = new AtomicBoolean(false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_lib_manager);
        testRefreshFeatureButton = findViewByIdAndSetListener(R.id.test_reextract__button);
        returnButton = findViewById(R.id.return_button);
        returnButton.setOnClickListener(this);
        queryPersonButton = findViewById(R.id.query_person_button);
        queryPersonButton.setOnClickListener(this);
        btnAddFace = findViewById(R.id.add_person_button);
        btnAddFace.setOnClickListener(this);
        btnBatchAddFace = findViewById(R.id.btn_batch_add);
        btnBatchAddFace.setOnClickListener(this);

        searchEditText = findViewById(R.id.search_edit_text);
        personTotalTextView = findViewById(R.id.person_total_text_view);
        faceListView = findViewById(R.id.face_listview);
        listItemAdapter = new FaceListViewAdapter(this);
        // 添加并且显示
        faceListView.setAdapter(listItemAdapter);
        faceListView.setOnScrollListener(this);
        // 添加点击
        Activity _this = this;
        faceListView.setOnItemClickListener((parent, view, position, id) -> {
            setTitle("点击第" + position + "项");
            DbPerson person = (DbPerson) parent.getItemAtPosition(position);
            if (person != null) {
                DbFaceItem faceItem = (DbFaceItem) person.getFaceItem1();
                Bitmap bitmap = XsFaceSDKFaceLibHelper.getFaceBitmap(faceItem, 300);
                Bitmap bitmap1 = null;
                if (person.getFaceItem2() != null)
                    bitmap1 = XsFaceSDKFaceLibHelper.getFaceBitmap(person.getFaceItem2(), 300);
                if (bitmap1 != null)
                    UITools.showImageDialog2(_this, person.getPersonCode(), "", bitmap, bitmap1, null);
                else
                    UITools.showImageDialog(_this, person.getPersonCode(), "", bitmap, null);
            }
        });

        // 添加长按点击
        faceListView.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
            menu.setHeaderTitle("操作");
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            selectedPerson = listItemAdapter.personList.get(info.position);
            menu.add(0, info.position, 0, "修改姓名");
            menu.add(0, info.position, 1, "修改照片1");
            menu.add(0, info.position, 2, "修改照片2");
            menu.add(0, info.position, 3, "删除照片2");
            menu.add(0, info.position, 4, "删除人员");
        });
        //刷新统计数据
        updatePersonCountView();
    }

    @Override
    public void onClick(View v) {
        if (v == btnAddFace) {
            showFileChooser("添加照片", FILE_SELECT_CODE_ADD);
        } else if (v == btnBatchAddFace) {
            //批量导入人员
            // TODO 需提前复制文件到sd卡的com.xinshi.android.face.demo/batch_import_person_images目录内
            batchImportPerson();
        } else if (v == queryPersonButton) {
            listItemAdapter.query(searchEditText.getText().toString());
            queryCompleted = false;
        } else if (v == testRefreshFeatureButton) {
            Activity activity = this;
            UITools.showConfirmDialog(this, "警告", "此操作将退出程序，重新启动后将重新抽取特征，可能耗时较长。确定要测试吗?", new UITools.ConfirmDialogCallback() {
                @Override
                public void onOk() {
                    XsFaceSDKFaceLibHelper.setFeatureVersion("force");
                    Intent intent = new Intent(activity, SplashActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    activity.startActivity(intent);
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            });
        } else if (v == returnButton) {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            String path = DemoUtils.getPath(this, uri);
            if (path != null) {
                Bitmap bitmap = Tool.loadBitmap(new File(path));
                putPersonImage(bitmap, requestCode);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE) {
            if (view.getLastVisiblePosition() == view.getCount() - 1) {
                if (!queryCompleted) {
                    queryCompleted = !listItemAdapter.queryNextPage();
                }
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }


    /***
     * 批量导入人员
     */
    private void batchImportPerson() {
        if (isImporting.get()) {
            showToast("正在导入中...请勿重新操作");
            return;
        }
        File imgDir = new File(String.format("%s/%s", EnvConfig.getRootPath(), IMPORT_IMAGES_PATH));
        if (!imgDir.exists()) {
            imgDir.mkdirs();
        }
        File failDir = new File(String.format("%s/%s", EnvConfig.getRootPath(), IMPORT_FAIL_IMAGES_PATH));

        File[] files = imgDir.listFiles();
        if (files.length == 0) {
            String msg = String.format("请将图片提前复制到SD卡的%s目录，再执行批量添加", imgDir.getAbsolutePath());
            showToast(msg);
            Log.e(TAG, msg);
        } else {
            new Thread(() -> {
                int success = 0;
                long start = System.currentTimeMillis();
                StringBuilder logMsg = new StringBuilder();
                String msg = String.format("批量导入人员照片开始,total[%s]", files.length);
                logMsg.append(msg).append("\r\n");
                Log.i(TAG, msg);
                isImporting.set(true);
                for (File f : files) {
                    if (f.getName().toLowerCase().endsWith(".jpg") || f.getName().toLowerCase().endsWith(".png")) {
                        try {
                            importPersonByFile(f);
                            success++;
                            msg = String.format("导入人员照片成功-[%s]", f.getName());
                            logMsg.append(msg).append("\r\n");
                            Log.d(TAG, msg);
                        } catch (Throwable e) {
                            if (!failDir.exists()) {
                                failDir.mkdirs();
                            }
                            //移动文件
                            f.renameTo(new File(String.format("%s/%s/%s", EnvConfig.getRootPath(), IMPORT_FAIL_IMAGES_PATH, f.getName())));
                            msg = String.format("导入人员照片失败-[%s]: %s", f.getName(), e.getMessage());
                            logMsg.append(msg).append("\r\n");
                            //将失败图片移动到导入异常目录
                            Log.e(TAG, msg);
                        }
                    }
                }
                msg = String.format("批量导入人员照片结束,total[%s],success[%s],fail[%s]，耗时[%s]s", files.length, success,
                        files.length - success, (System.currentTimeMillis() - start) / 1000);
                logMsg.append(msg).append("\r\n");
                Log.i(TAG, msg);
                File logFile = new File(String.format("%s/import_images.log", EnvConfig.getRootPath()));
                try {
                    XsFaceSDKUtils.saveFile(logFile, logMsg.toString().getBytes("utf-8"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                isImporting.set(false);
            }).start();
            showToast(String.format("正在后台异步导入[%s]张人员照片", files.length));
        }
    }

    /***
     * 导入单个图片
     */
    private void importPersonByFile(File f) throws FaceException, IOException {
        Bitmap bitmap = Tool.loadBitmap(f);
        if (bitmap == null) {
            throw new FaceException(String.format("加载图片文件失败[%s]", f.getName()));
        }
        String fileName = getBaseName(f.getName());
        FaceImage faceImage = XsFaceSDKHelper.bitmapToFaceImage(bitmap);
        //人脸检测
        FaceData faceData = XsFaceSDK.instance.getVisFaceDetector().detectMaxFace(faceImage, true);
        if (faceData == null) {
            throw new FaceException("图片中未检测到人脸");
        }
        checkFaceQuality(faceImage, faceData);
        //人员新增
        XsFaceSDKFaceLibHelper.putPersonFile(fileName, null, null,
                f.getAbsolutePath(), null, null, null,
                System.currentTimeMillis(), true);
    }

    private void checkFaceQuality(FaceImage faceImage, FaceData faceData) throws FaceException {
        //人脸质量检测
        FaceQuality quality = XsFaceSDKHelper.qualityDetect(faceImage, faceData, true, false, true, false, false);
        //TODO 检测指标和检测阈值可根据实际情况修改
        if (quality.getPitch() > 20 || quality.getYaw() > 30 || quality.getRoll() > 20)
            throw new FaceQualityException(quality, "脸不正");
        if (quality.getBlur() < 0.85)
            throw new FaceQualityException(quality, "人脸模糊");
    }

    public static String getBaseName(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index == -1) {
            return fileName;
        } else {
            return fileName.substring(0, index);
        }
    }


    private void putPersonImage(Bitmap bitmap, int requestCode) {
        try {
            FaceImage img = XsFaceSDKHelper.bitmapToFaceImage(bitmap);
            //人脸检测
            FaceData faceData = XsFaceSDKHelper.maxFaceDetect(img);
            if (faceData == null) {
                showToast("未检测到人脸");
                throw new FaceException("未检测到人脸");
            }
            //质量判断
            checkFaceQuality(img, faceData);
            float[] feature = XsFaceSDKHelper.extractFaceFeature(img, faceData);
            if (feature == null)
                throw new FaceException("抽取特征失败，照片质量较低");
            switch (requestCode) {
                case FILE_SELECT_CODE_ADD:
                    lastFaceImage = img;
                    lastFaceData = faceData;
                    lastFacefeature = feature;
                    showEditBox(true);
                    break;
                case FILE_SELECT_CODE_EDIT1: {
                    DbFaceItem item = selectedPerson.getFaceItem1();
                    //人员更新-人脸照片1
                    XsFaceSDKFaceLibHelper.putPersonImage(selectedPerson.getPersonCode(), 0, "",
                            new FaceImageData(img, faceData), feature, null, null, 0, false);
                    item.setImageFileName(item.getImageFileName());
                    listItemAdapter.notifyDataSetChanged();
                    updatePersonCountView();
                    break;
                }
                case FILE_SELECT_CODE_EDIT2: {
                    //人员更新-人脸照片2
                    DbPerson person = XsFaceSDKFaceLibHelper.putPersonImage(selectedPerson.getPersonCode(), 0, "",
                            null, null, new FaceImageData(img, faceData), feature, 0, false);
                    selectedPerson.setFaceItem2(person.getFaceItem2());
                    listItemAdapter.notifyDataSetChanged();
                    updatePersonCountView();
                    break;
                }
            }
        } catch (Throwable e) {
            showToast(e.getMessage());
        }
    }

    protected void updatePersonCountView() {
        XsFaceSearchLibrary searchLibrary = (XsFaceSearchLibrary) XsFaceSDK.instance.getFaceSearchLibrary();
        personTotalTextView.setText(String.format("总人数：%d，总人脸数：%d", searchLibrary.getPersonCount(), searchLibrary.getFaceCount()));
    }

    public void showEditBox(final boolean add) {
        final Context _this = this;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("输入姓名");
        final EditText input = new EditText(this);
        if (!add && selectedPerson != null)
            input.setText(selectedPerson.getPersonCode());
        //input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (add) {
                            try {
                                //人员新增
                                String personCode = input.getText().toString();
                                DbPerson person = XsFaceSDKFaceLibHelper.putPersonImage(personCode, 0, "",
                                        new FaceImageData(lastFaceImage, lastFaceData), lastFacefeature, null, null, 0, true);
                                listItemAdapter.add(person);
                                updatePersonCountView();
                            } catch (Throwable e) {
                                showToast(e.getMessage());
                            }
                        } else {
                            try {
                                //人员编码更新
                                Log.e(getClass().getName(), String.format("人员Code更新,personId[%s]", selectedPerson.getPersonId()));
                                selectedPerson.setPersonCode(input.getText().toString());
                                XsFaceSDKFaceLibHelper.updatePersonCode(selectedPerson.getPersonId(), selectedPerson.getPersonCode());
                                listItemAdapter.notifyDataSetChanged();
                            } catch (FaceException e) {
                                showToast(e.getMessage());
                            }
                        }
                    }
                });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getOrder()) {
            case 0:
                showEditBox(false);
                break;
            case 1:
                showFileChooser("选择照片", FILE_SELECT_CODE_EDIT1);
                break;
            case 2:
                showFileChooser("选择照片", FILE_SELECT_CODE_EDIT2);
                break;
            case 3:
                UITools.showConfirmDialog(this, "警告", "确定要删除照片吗?",
                        new UITools.ConfirmDialogCallback() {
                            @Override
                            public void onOk() {
                                try {
                                    //删除人脸2
                                    XsFaceSDKFaceLibHelper.removePersonFace2(selectedPerson.getPersonCode(), 0);
                                    selectedPerson.setFaceItem2(null);
                                    updatePersonCountView();
                                } catch (Throwable e) {
                                    showToast(e.toString());
                                }
                                listItemAdapter.notifyDataSetChanged();
                            }
                        });
                break;
            case 4:
                UITools.showConfirmDialog(this, "警告", "确定要删除该人员吗?",
                        new UITools.ConfirmDialogCallback() {
                            @Override
                            public void onOk() {
                                removePerson(selectedPerson);
                                listItemAdapter.remove(item.getItemId());
                                updatePersonCountView();
                            }
                        });
                break;
            default:
                return super.onContextItemSelected(item);
        }
        return true;
    }

    private void removePerson(DbPerson selectedPerson) {
        try {
            XsFaceSDKFaceLibHelper.removePerson(selectedPerson);
        } catch (FaceException e) {
            showToast(e.getMessage());
        }
    }

    /**
     * 重新抽取特征示例代码
     */
    private void refreshFaceLibFeatures() {
        for (int i = 0; true; i += 1000) {
            Pair<Integer, List<DbPerson>> persons = XsFaceSDKFaceLibHelper.queryPerson(i == 0 ? -1 : i, 1000, null);
            for (DbPerson person : persons.second) {
                try {
                    XsFaceSDKFaceLibHelper.refreshPersonFeature(person, true, true);
                } catch (Throwable e) {
                }
            }
            if (persons.second.size() < 1000) break;
        }
    }


    static class FaceListViewAdapter extends BaseAdapter {
        Context context;
        List<DbPerson> personList = new ArrayList<>();
        int totalCount;
        int fetchedCount;
        String filter;

        public FaceListViewAdapter(Context context) {
            this.context = context;
            query(null);
        }

        public void query(String filter) {
            this.filter = filter;
            personList.clear();
            Pair<Integer, List<DbPerson>> v = XsFaceSDKFaceLibHelper.queryPerson(-1, 100, filter);
            if (v != null) {
                Log.i(getClass().getName(), String.format("人员查询结果:[%s]", v.first));
                totalCount = v.first;
                fetchedCount = v.second.size();
                personList.addAll(v.second);
            } else {
                Log.i(getClass().getName(), "查询结果为空");
                totalCount = 0;
                fetchedCount = 0;
            }
            this.notifyDataSetChanged();
        }

        public boolean queryNextPage() {
            Pair<Integer, List<DbPerson>> v = XsFaceSDKFaceLibHelper.queryPerson(fetchedCount, 100, filter);
            if (v != null) {
                fetchedCount += v.second.size();
                for (DbPerson p : v.second) {
                    if (!personList.contains(p))
                        personList.add(p);
                }
            }
            this.notifyDataSetChanged();
            return v != null && v.second.size() >= 100;
        }

        public void add(DbPerson person) {
            personList.add(0, person);
            notifyDataSetChanged();
        }

        public void remove(int position) {
            personList.remove(position);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return personList.size();
        }

        @Override
        public Object getItem(int position) {
            return personList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return personList.get(position).getPersonId();
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView;
            if(convertView==null){
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                itemView = inflater.inflate(R.layout.face_listview_item_layout, null);
            }else{
                itemView=convertView;
            }
            AddedPerson info = personList.get(position);
            TextView textView = itemView.findViewById(R.id.ItemTitle);
            ImageView imageView = itemView.findViewById(R.id.ItemImage);
            TextView itemImageInfo = itemView.findViewById(R.id.ItemImageInfo);
            textView.setText(info.getName());
            try {
                DbFaceItem faceItem = (DbFaceItem) info.getFaceItem1();
                Bitmap bitmap = XsFaceSDKFaceLibHelper.getFaceBitmap(faceItem);// Tool.loadBitmap(new File(String.format("%s/faceimages/%s", EnvConfig.getRootPath(), faceItem.getImageFileName())));
                if (bitmap == null) {
                    imageView.setImageResource(R.drawable.noimage);
                } else
                    imageView.setImageBitmap(bitmap);
                itemImageInfo.setText(String.format("状态: %s", faceItem.getFaceStatusString()));
                faceItem = (DbFaceItem) info.getFaceItem2();
                if (faceItem != null) {
                    imageView = itemView.findViewById(R.id.ItemImage1);
                    itemImageInfo = itemView.findViewById(R.id.ItemImageInfo1);
                    bitmap = XsFaceSDKFaceLibHelper.getFaceBitmap(faceItem);// Tool.loadBitmap(new File(String.format("%s/faceimages/%s", EnvConfig.getRootPath(), faceItem.getImageFileName())));
                    if (bitmap == null) {
                        imageView.setImageResource(R.drawable.noimage);
                    } else
                        imageView.setImageBitmap(bitmap);
                    itemImageInfo.setText(String.format("状态: %s", faceItem.getFaceStatusString()));
                    faceItem = (DbFaceItem) info.getFaceItem2();
                }
            } catch (Throwable e) {
                Log.e("FaceManagerActivity", "error:", e);
            }
            return itemView;

        }
    }

}
