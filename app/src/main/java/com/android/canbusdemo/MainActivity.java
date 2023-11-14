package com.android.canbusdemo;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.canbus.CanBusHelper;
import com.android.canbus.CanBusHelper.CanBusCallback;
import com.android.canbus.CanBusHelper.UpdateReturnCallback;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {
    private int recvCnt = 0;
    private int recvErrCnt = 0;
    private boolean sendEnable = false;

    private UIHandler mUiHandler = new UIHandler();

//    private RadioButton mRadio0 = null;
//    private RadioButton mRadio1 = null;
//    private Spinner mFrameType = null;
//    private Spinner mFrameFormat = null;
    private Spinner mComBaudrate = null;
    private Spinner mCanBaudrate = null;

    private MyEditText mDisplay = null;
    /*private EditText mSendTimes = null;*/
//    private EditText mFrameId = null;
    /*private EditText mFrameData = null;*/
//    private EditText mSendInterval = null;
//    private EditText mTimes = null;
//    private EditText mTimesDelay = null;

    private Button mReset = null;
    private Button mClear = null;
    /*private Button mSend = null;*/
    /*private Button mStop = null;*/
    private Button mOpen = null;
    private Button mClose = null;

//    private CheckBox mCheckIncrease = null;

    private MyTextView mRecvRimes = null;

//    private TextView mExecTime = null;

    private CanBusHelper mCanBusHelper = new CanBusHelper();

    private long startTime = 0;
    private long endTime = 0;

    //Filter
    private AlertDialog mFilterDialog = null;
    private Spinner filter_mode = null;
    private Spinner filter_channel = null;
    private EditText filter_start = null;
    private EditText filter_end = null;
    private ListView filter_list = null;
    private MyAdapter filter_adapter = null;
    private List<String> channels = new ArrayList<String>();
    private List<String> formats = new ArrayList<String>();
    private List<String> startids = new ArrayList<String>();
    private List<String> endids = new ArrayList<String>();
    private MyArrays id_stanard_list0 = null;
    private MyArrays id_extend_list0 = null;
    private MyArrays id_stanard_list1 = null;
    private MyArrays id_extend_list1 = null;
    private RadioButton filter_list_mode = null;
    private RadioButton filter_mask_mode = null;
    private CheckBox filter_enable = null;
    private boolean filter_enable_flag = false;

    //OTA
    private AlertDialog mOtaDialog = null;
    private Button ota_close = null;
    private Button ota_start = null;
    private Button ota_select = null;
    private TextView ota_path = null;
    private TextView ota_result = null;
    private TextView ota_version = null;
    private ProgressBar ota_progress = null;

    //MENU
    private AlertDialog mMenuDialog = null;
    private Button menu_close = null;
    private CheckBox menu_enable = null;
    private boolean menu_save_enable = false;
    private TextView menu_version = null;

    int dialogX, dialogY;

    private TextView mSetDisplayDouble = null;
    private TextView mIdDisplay = null;
    private int filter_mode_select = 0;
//    private Button mFuncs = null;
    private AlertDialog mFunctionsDialog = null;
    private Button mOta = null;
    private Button mFilter = null;
    private Button mCollect = null;
    private EditText mAdcVref = null;
    private TextView mAdcValue = null;
    private TextView mAppVersion = null;
    private TextView mLibraryVersion = null;
    private Spinner mAdcChannel = null;
    private Button mFunctionsClose = null;
    private UpdateUnixDateTime mUpdateDateTimeThread = null;
    private TextView mRtcShowValue = null;
    private EditText mRtcValueSet = null;
    private Button mSetRtc = null;
    private float vref = 3.3f;
    private boolean isRTC = true;
    private int screenWidth, screenHeight;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //锁定横屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        bindUI();

        Display defaultDisplay = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        defaultDisplay.getSize(point);
        dialogX = point.x - 40;
        dialogY = point.y - 50;
        System.out.println("zyz --> point.x --> " + point.x + ", point.y --> " + point.y);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;
        initPermission();
    }

    private void initPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // 检查权限状态
                if (this.shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) && this.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    /**
                     * 用户彻底拒绝授予权限，一般会提示用户进入设置权限界面
                     * 第一次授权失败之后，退出App再次进入时，再此处重新调出允许权限提示框
                     */
                    this.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    Log.d("info:", "-----get--Permissions--success--1-");
                } else {
                    /**
                     * 用户未彻底拒绝授予权限
                     * 第一次安装时，调出的允许权限提示框，之后再也不提示
                     */
                    Log.d("info:", "-----get--Permissions--success--2-");
                    this.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                }
            } else {
//                sv.postDelayed(this::initPush, 1000);
            }
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            mMenuDialog = new AlertDialog.Builder(MainActivity.this).create();
            mMenuDialog.show();
            mMenuDialog.getWindow().setContentView(R.layout.menu);
            setDialog(mMenuDialog, 600, 400);

            menu_close = mMenuDialog.findViewById(R.id.close);
            menu_enable = mMenuDialog.findViewById(R.id.save_enable);
            menu_version = mMenuDialog.findViewById(R.id.menu_version);
            menu_enable.setEnabled(mOpen.isEnabled());
            menu_enable.setChecked(menu_save_enable);
            menu_enable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    menu_save_enable = isChecked;
                }
            });
            menu_version.setText(APKVersionCodeUtils.getVerName(this));

            menu_close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMenuDialog.dismiss();
                }
            });
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null)
            return;

        String imageAbsolutePath = FileUtils.getImageAbsolutePath(this, data.getData());
        if (imageAbsolutePath == null)
            return;
        String fileName = (new File(imageAbsolutePath)).getName();
        if (!(fileName.contains(getString(R.string.rule_ota_bin_file_name_contain)) && fileName.contains(getString(R.string.rule_rule_ota_bin_file_name_tail)))) {
            ShowMessage(getString(R.string.warn_illegal_bin_file));
            return;
        }
        ota_path.setText(FileUtils.getImageAbsolutePath(this, data.getData()));
        ota_start.setEnabled(true);
        ota_close.setEnabled(false);

//        String scheme = data.getScheme();
//        if (scheme == null)
//            return;
//        if (resultCode == Activity.RESULT_OK && scheme.equals(getString(R.string.rule_file))){
//            Uri uri = data.getData();//得到uri，后面就是将uri转化成file的过程。
//            if (uri == null)
//                return;
//            String path = uri.getPath();
//
//            if (path == null)
//                return;
//            String fileName = (new File(path)).getName();
//            if (!(fileName.contains(getString(R.string.rule_ota_bin_file_name_contain)) && fileName.contains(getString(R.string.rule_rule_ota_bin_file_name_tail)))){
//                ShowMessage(getString(R.string.warn_illegal_bin_file));
//                return;
//            }
//

//        }
    }

    private void bindUI() {
        mDisplay = (MyEditText) findViewById(R.id.display);

       /* mSendTimes = (EditText) findViewById(R.id.sendTimes);
        setEditTextInhibitInputType(mSendTimes, false, 8);*/
        /*mFrameId = (EditText) findViewById(R.id.frameId);
        setEditTextInhibitInputType(mFrameId, true, 8);*/
       /* mFrameData = (EditText) findViewById(R.id.frameData);
        setEditTextInhibitInputType(mFrameData, true, 23);*/
        /*mSendInterval = (EditText) findViewById(R.id.sendInterval);
        setEditTextInhibitInputType(mSendInterval, false, 5);*/
        /*mTimes = (EditText) findViewById(R.id.times);
        setEditTextInhibitInputType(mTimes, false, 8);
        mTimesDelay = (EditText) findViewById(R.id.timesDelay);
        setEditTextInhibitInputType(mTimesDelay, false, 5);*/

        /*mRadio0 = (RadioButton) findViewById(R.id.radio0);
        mRadio1 = (RadioButton) findViewById(R.id.radio1);
        mRadio0.setOnClickListener(new onRadioButtonClickListener());
        mRadio1.setOnClickListener(new onRadioButtonClickListener());*/

        mComBaudrate = (Spinner) findViewById(R.id.baudrateCom);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.baudrates_name,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mComBaudrate.setAdapter(adapter);
        mComBaudrate.setSelection(6);//115200
//        mComBaudrate.setSelection(10);//1000000

        mCanBaudrate = (Spinner) findViewById(R.id.baudrateCan);
        ArrayAdapter<CharSequence> adapters = ArrayAdapter.createFromResource(
                this, R.array.baudrate_can,
                android.R.layout.simple_spinner_item);
        adapters.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCanBaudrate.setAdapter(adapters);
        mCanBaudrate.setSelection(6);

        /*mFrameType = (Spinner) findViewById(R.id.frameType);
        mFrameFormat = (Spinner) findViewById(R.id.frameFormat);
        ArrayAdapter<CharSequence> adapter0 = ArrayAdapter.createFromResource(
                this, R.array.type,
                android.R.layout.simple_spinner_item);
        adapter0.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mFrameType.setAdapter(adapter0);
        mFrameType.setSelection(0);
        mFrameType.setSelection(1);

        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(
                this, R.array.format,
                android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mFrameFormat.setAdapter(adapter1);
        mFrameFormat.setSelection(0);*/

        mReset = findViewById(R.id.reset);
        mReset.setOnClickListener(new onButtonClickListener());
        mClear = (Button) findViewById(R.id.clear);
        mClear.setOnClickListener(new onButtonClickListener());
       /* mSend = (Button) findViewById(R.id.send);
        mSend.setOnClickListener(new onButtonClickListener());*/
        /*mStop = (Button) findViewById(R.id.stop);
        mStop.setOnClickListener(new onButtonClickListener());*/
        mOpen = (Button) findViewById(R.id.open);
        mOpen.setOnClickListener(new onButtonClickListener());
        mClose = (Button) findViewById(R.id.close);
        mClose.setOnClickListener(new onButtonClickListener());

//        mCheckIncrease = (CheckBox) findViewById(R.id.checkIncrease);

        mRecvRimes = (MyTextView) findViewById(R.id.recvRimes);

        /*mExecTime = (TextView) findViewById(R.id.execTime);*/

        filter_adapter = new MyAdapter(MainActivity.this, channels, formats, startids, endids);
//        mFuncs = findViewById(R.id.func);
//        mFuncs.setOnClickListener(new onButtonClickListener());
    }

    /**
     * 指定EditText输入数字字母
     *
     * @param editText
     */
    public static void setEditTextInhibitInputType(EditText editText, boolean allowSpace, int len) {
        final boolean allow = allowSpace;
        final int length = len;
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (
//                            !Character.isLetterOrDigit(source.charAt(i))
                            !Character.isDigit(source.charAt(i))
                                    || Character.toString(source.charAt(i)).equals("π")) {
                        if ((allow && length == 23)
                                && (Character.toString(source.charAt(i)).equals(" ")
                                || Character.toString(source.charAt(i)).equals("A")
                                || Character.toString(source.charAt(i)).equals("B")
                                || Character.toString(source.charAt(i)).equals("C")
                                || Character.toString(source.charAt(i)).equals("D")
                                || Character.toString(source.charAt(i)).equals("E")
                                || Character.toString(source.charAt(i)).equals("F")
                                || Character.toString(source.charAt(i)).equals("a")
                                || Character.toString(source.charAt(i)).equals("b")
                                || Character.toString(source.charAt(i)).equals("c")
                                || Character.toString(source.charAt(i)).equals("d")
                                || Character.toString(source.charAt(i)).equals("e")
                                || Character.toString(source.charAt(i)).equals("f")))
                            return null;
                        if ((allow && length == 8)
                                && (Character.toString(source.charAt(i)).equals("A")
                                || Character.toString(source.charAt(i)).equals("B")
                                || Character.toString(source.charAt(i)).equals("C")
                                || Character.toString(source.charAt(i)).equals("D")
                                || Character.toString(source.charAt(i)).equals("E")
                                || Character.toString(source.charAt(i)).equals("F")
                                || Character.toString(source.charAt(i)).equals("a")
                                || Character.toString(source.charAt(i)).equals("b")
                                || Character.toString(source.charAt(i)).equals("c")
                                || Character.toString(source.charAt(i)).equals("d")
                                || Character.toString(source.charAt(i)).equals("e")
                                || Character.toString(source.charAt(i)).equals("f")))
                            return null;
                        return "";
                    }
                }
                return null;
            }
        };
        editText.setFilters(new InputFilter[]{filter, new InputFilter.LengthFilter(len)});
    }

    /**
     * 设置二选一选项按钮
     */
    /*private class onRadioButtonClickListener implements RadioButton.OnClickListener {
        @Override
        public void onClick(View v) {
            if (v == mRadio0) {
                if (mRadio0.isChecked()) {
                    mRadio1.setChecked(false);
                }
            } else if (v == mRadio1) {
                if (mRadio1.isChecked()) {
                    mRadio0.setChecked(false);
                }
            } else if (v == filter_list_mode) {
                if (filter_list_mode.isChecked()) {
                    if (filter_adapter.getCount() != 0) {
                        filter_list_mode.setChecked(false);
                        ShowMessage(getString(R.string.list_not_zero));
                        return;
                    }

                    filter_mode_select = 0;
                    filter_mask_mode.setChecked(false);
                    mSetDisplayDouble.setText(getString(R.string.range));
                    mIdDisplay.setText(getString(R.string.id_range));
                    filter_end.setEnabled(filter_mode.getSelectedItemPosition() == 1 || filter_mode.getSelectedItemPosition() == 3);

                    ArrayAdapter<CharSequence> adapter0 = ArrayAdapter.createFromResource(
                            MainActivity.this, filter_mode_select == 0 ? R.array.filter_list_mode : R.array.filter_mask_mode,
                            android.R.layout.simple_spinner_item);
                    adapter0.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    filter_mode.setAdapter(adapter0);
                    filter_mode.setSelection(0);
                    filter_mode.setOnItemSelectedListener(new onSpinnerSelectListener());
                }
            } else if (v == filter_mask_mode) {
                if (filter_mask_mode.isChecked()) {
                    if (filter_adapter.getCount() != 0) {
                        filter_mask_mode.setChecked(false);
                        ShowMessage(getString(R.string.list_not_zero));
                        return;
                    }
                    filter_mode_select = 1;
                    filter_list_mode.setChecked(false);
                    mSetDisplayDouble.setText(" Mask id: 0x");
                    mIdDisplay.setText("ID : 0x");
                    filter_end.setEnabled(true);
                    ArrayAdapter<CharSequence> adapter0 = ArrayAdapter.createFromResource(
                            MainActivity.this, filter_mode_select == 0 ? R.array.filter_list_mode : R.array.filter_mask_mode,
                            android.R.layout.simple_spinner_item);
                    adapter0.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    filter_mode.setAdapter(adapter0);
                    filter_mode.setSelection(0);
                    filter_mode.setOnItemSelectedListener(new onSpinnerSelectListener());
                }
            }
        }
    }
*/

    private void setDialog(AlertDialog dialog, int width, int height) {
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = width;
        params.height = height;
        dialog.getWindow().setAttributes(params);
        dialog.setCancelable(false);
    }

    private boolean getAllFilterId(MyArrays idStandardList0, MyArrays idExtendList0, MyArrays idStandardList1, MyArrays idExtendList1) {
        String[] list_mode = getResources().getStringArray(R.array.filter_list_mode);
        String[] mask_mode = getResources().getStringArray(R.array.filter_mask_mode);
        int start, end, num;
        if (filter_mode_select == 0) {
            for (int i = 0; i < channels.size(); i++) {
                if (channels.get(i).equals("0")) {
                    if (formats.get(i).equals(list_mode[0])) {
                        if (idStandardList0.getLength() > 28)
                            return false;
                        idStandardList0.add(checkoutIDRule(startids.get(i), 8, 0));
                    } else if (formats.get(i).equals(list_mode[1])) {
                        start = checkoutIDRule(startids.get(i), 8, 0);
                        end = checkoutIDRule(endids.get(i), 8, 0);
                        num = end - start + 1;//加一包括最后一个
                        for (int j = 0; j < num; j++) {
                            if (idStandardList0.getLength() > 28)
                                return false;
                            idStandardList0.add(start + j);
                        }
                    } else if (formats.get(i).equals(list_mode[2])) {
                        if (idExtendList0.getLength() > 28)
                            return false;
                        idExtendList0.add(checkoutIDRule(startids.get(i), 8, 0));
                    } else if (formats.get(i).equals(list_mode[3])) {
                        start = checkoutIDRule(startids.get(i), 8, 0);
                        end = checkoutIDRule(endids.get(i), 8, 0);
                        num = end - start + 1;//加一包括最后一个
                        for (int j = 0; j < num; j++) {
                            if (idExtendList0.getLength() > 28)
                                return false;
                            idExtendList0.add(start + j);
                        }
                    }
                } else {
                    if (formats.get(i).equals(list_mode[0])) {
                        if (idStandardList1.getLength() > 28)
                            return false;
                        idStandardList1.add(checkoutIDRule(startids.get(i), 8, 0));
                    } else if (formats.get(i).equals(list_mode[1])) {
                        start = checkoutIDRule(startids.get(i), 8, 0);
                        end = checkoutIDRule(endids.get(i), 8, 0);
                        num = end - start + 1;//加一包括最后一个
                        for (int j = 0; j < num; j++) {
                            if (idStandardList1.getLength() > 28)
                                return false;
                            idStandardList1.add(start + j);
                        }
                    } else if (formats.get(i).equals(list_mode[2])) {
                        if (idExtendList1.getLength() > 28)
                            return false;
                        idExtendList1.add(checkoutIDRule(startids.get(i), 8, 0));
                    } else if (formats.get(i).equals(list_mode[3])) {
                        start = checkoutIDRule(startids.get(i), 8, 0);
                        end = checkoutIDRule(endids.get(i), 8, 0);
                        num = end - start + 1;//加一包括最后一个
                        for (int j = 0; j < num; j++) {
                            if (idExtendList1.getLength() > 28)
                                return false;
                            idExtendList1.add(start + j);
                        }
                    }
                }
            }
        } else {
            for (int i = 0; i < channels.size(); i++) {
                if (channels.get(i).equals("0")) {
                    if (formats.get(i).equals(mask_mode[0])) {
                        if (idStandardList0.getLength() > 28)
                            return false;
                        idStandardList0.add_mask(checkoutIDRule(startids.get(i), 8, 0));
                        idStandardList0.add_mask(checkoutIDRule(endids.get(i), 8, 0));
                    } else if (formats.get(i).equals(mask_mode[1])) {
                        if (idStandardList0.getLength() > 28)
                            return false;
                        idExtendList0.add_mask(checkoutIDRule(startids.get(i), 8, 0));
                        idExtendList0.add_mask(checkoutIDRule(endids.get(i), 8, 0));
                    }
                } else {
                    if (formats.get(i).equals(mask_mode[0])) {
                        if (idStandardList1.getLength() > 28)
                            return false;
                        idStandardList1.add_mask(checkoutIDRule(startids.get(i), 8, 0));
                        idStandardList1.add_mask(checkoutIDRule(endids.get(i), 8, 0));
                    } else if (formats.get(i).equals(mask_mode[1])) {
                        if (idStandardList1.getLength() > 28)
                            return false;
                        idExtendList1.add_mask(checkoutIDRule(startids.get(i), 8, 0));
                        idExtendList1.add_mask(checkoutIDRule(endids.get(i), 8, 0));
                    }
                }
            }
        }
        return true;
    }

    private class onSpinnerSelectListener implements Spinner.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (parent == filter_mode) {
                if (position == 0) {
                    filter_end.setEnabled(false || filter_mask_mode.isChecked());
                } else if (position == 1) {
                    filter_end.setEnabled(true);
                } else if (position == 2) {
                    filter_end.setEnabled(false || filter_mask_mode.isChecked());
                } else if (position == 3) {
                    filter_end.setEnabled(true);
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    private class onButtonClickListener implements Button.OnClickListener {
        @Override
        public void onClick(View v) {
            /*if (v == mSend) {
                if (mOpen.isEnabled()) {
                    ShowMessage(getString(R.string.warn_open_first));
                    return;
                }

                mSend.setEnabled(false);
                mStop.setEnabled(true);

                int id = 0;
                id = checkoutIDRule(mFrameId.getText().toString(), 8, 0);
                if (id < 0) {
                    mSend.setEnabled(true);
                    mStop.setEnabled(false);
                    return;
                }
                int[] valueData = checkoutDataRule(mFrameData.getText().toString());
                if (valueData == null) {
                    mSend.setEnabled(true);
                    mStop.setEnabled(false);
                    return;
                }
                if (mRadio0.isChecked()) {
                    startTime = System.currentTimeMillis();
                    sendOneFrame(0, mFrameType.getSelectedItemPosition(),
                            mFrameFormat.getSelectedItemPosition(),
                            8,
                            id,
                            valueData);
                } else {
                    if (mSendTimes.getText().toString().equals("") || Integer.parseInt(mSendTimes.getText().toString()) < 1) {
                        mSend.setEnabled(true);
                        mStop.setEnabled(false);
                        ShowMessage(getString(R.string.warn_frames_less_than_1));
                        return;
                    }
                    if (mSendInterval.getText().toString().equals("")) {
                        mSend.setEnabled(true);
                        mStop.setEnabled(false);
                        ShowMessage(getString(R.string.warn_send_interval_set_null));
                        return;
                    }
                    if (mTimes.getText().toString().equals("") || Integer.parseInt(mTimes.getText().toString()) < 1) {
                        mSend.setEnabled(true);
                        mStop.setEnabled(false);
                        ShowMessage(getString(R.string.warn_send_frames_less_than_1));
                        return;
                    }
                    if (mTimesDelay.getText().toString().equals("")) {
                        mSend.setEnabled(true);
                        mStop.setEnabled(false);
                        ShowMessage(getString(R.string.warn_send_interval_set_null));
                        return;
                    }

                    startTime = System.currentTimeMillis();
                    sendEnable = true;
                    sendMultipleFrame(0, mFrameType.getSelectedItemPosition(),
                            mFrameFormat.getSelectedItemPosition(),
                            8,
                            id,
                            valueData,
                            Integer.parseInt(mSendTimes.getText().toString()),
                            Integer.parseInt(mSendInterval.getText().toString()),
                            mCheckIncrease.isChecked());
                }
            } else if (v == mStop) {
                sendEnable = false;*/
            if (v == mClear) {
                mDisplay.setText("");
                mRecvRimes.setMyText(0);
                recvCnt = 0;
            } else if (v == mReset) {
                mReset.setEnabled(false);
                mOpen.setEnabled(false);
                mClose.setEnabled(false);
                mClear.setEnabled(false);
//                mFuncs.setEnabled(false);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int i = 0;
                        for (i = 0; i < mComBaudrate.getCount(); i++) {
//                            System.out.println("zyz --> Start find baudrate --> "+Integer.parseInt(mComBaudrate.getItemAtPosition(i).toString()));
                            if (mCanBusHelper.trySerialBaudrate(0, Integer.parseInt(mComBaudrate.getItemAtPosition(i).toString()), 8, 0, 1) == 0) {
//                                System.out.println("zyz --> Found serial baudrate --> "+Integer.parseInt(mComBaudrate.getItemAtPosition(i).toString()));
                                final int count = i;
                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ShowMessage("Found current " + Integer.parseInt(mComBaudrate.getItemAtPosition(count).toString()) + ", Reset to 115200 !");
                                    }
                                });
                                break;
                            }
//                            System.out.println("zyz --> At baudrate "+Integer.parseInt(mComBaudrate.getItemAtPosition(i).toString())+" could not found !");
                        }
                        if (i == mComBaudrate.getCount()) {
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ShowMessage("Reset failed !");
                                }
                            });
                        }
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mReset.setEnabled(true);
                                mOpen.setEnabled(true);
//                                mFuncs.setEnabled(true);
                                mClear.setEnabled(true);
                            }
                        });
                    }
                }).start();

            } else if (v == mOpen) {
                mOpen.setEnabled(false);
                mReset.setEnabled(false);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        /****************************************************************************************************************************************************************************************/
                        /****************************************************************************************************************************************************************************************/
                        /****************************************************************************************************************************************************************************************/
                        int ret = mCanBusHelper.setSerialBaudrate(0, Integer.parseInt(mComBaudrate.getSelectedItem().toString()), 8, 0, 1);
                        if (ret < 0) {
                            System.out.println("set serial baudrate falied !!!");
                            SendMessage(11, -1000, null);
                            return;
                        }
                        /****************************************************************************************************************************************************************************************/
                        /****************************************************************************************************************************************************************************************/
                        /****************************************************************************************************************************************************************************************/
                        if (mCanBusHelper.initialize(0, Integer.parseInt(mComBaudrate.getSelectedItem().toString()), Integer.parseInt(mCanBaudrate.getSelectedItem().toString())) == 0) {
                            Thread canThread = new Thread(mCanRunnable);
                            canThread.start();
                            SendMessage(10, -1000, null);
                        } else {
                            SendMessage(11, -1000, null);
                        }
                    }
                }).start();
            } else if (v == mClose) {
                mClose.setEnabled(false);
                new Thread(mDelayOpenRunnable).start();
            /*} else if (v == mFuncs) {
                functionsButtonClickEvent();*/
            } else if (v == mCollect) {
                mAdcValue.setText("");
                if (mAdcVref.getText() != null && !mAdcVref.getText().toString().equals("")) {
                    vref = Float.parseFloat(mAdcVref.getText().toString());
                    if (vref <= 0.0f) {
                        ShowMessage(getString(R.string.warn_v_ref));
                        return;
                    }
                }
                final float value = mCanBusHelper.getAdcValue(mAdcChannel.getSelectedItemPosition(), vref);
                if (value < 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ShowMessage("Get adc value failed --> " + value);
                        }
                    });
                    System.out.println("zyz --> get adc value failed --> " + value);
                }
                if (mFunctionsDialog == null || (!mFunctionsDialog.isShowing())) {
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdcValue.setText(value + "");
                    }
                });
            } else if (v == mOta) {
                mOtaDialog = new AlertDialog.Builder(MainActivity.this).create();
                mOtaDialog.show();
                mOtaDialog.getWindow().setContentView(R.layout.ota);
                setDialog(mOtaDialog, dialogX, dialogY);
                ota_select = (Button) mOtaDialog.findViewById(R.id.ota_select);
                ota_start = (Button) mOtaDialog.findViewById(R.id.ota_start);
                ota_close = (Button) mOtaDialog.findViewById(R.id.ota_close);
                ota_path = (TextView) mOtaDialog.findViewById(R.id.ota_path);
                ota_result = (TextView) mOtaDialog.findViewById(R.id.ota_result);
                ota_version = (TextView) mOtaDialog.findViewById(R.id.ota_version);
                ota_progress = (ProgressBar) mOtaDialog.findViewById(R.id.progress);

                ota_progress.setMax(100);
                ota_start.setEnabled(false);
                ota_select.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        startActivityForResult(intent, 1);

                    }
                });
                ota_start.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ota_start.setEnabled(false);
                        ota_close.setEnabled(false);
                        ota_progress.setVisibility(View.VISIBLE);
                        ota_progress.setProgress(0);
                        ota_result.setText("");
                        OtaThread pOtaThread = new OtaThread(ota_path.getText().toString());
                        pOtaThread.start();
                    }
                });
                ota_close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mOtaDialog.dismiss();
                        mOtaDialog = null;
                    }
                });

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SendMessage(5, -1000, mCanBusHelper.getVersion(Integer.parseInt(mComBaudrate.getSelectedItem().toString())));
                    }
                }).start();
            } else if (v == mFilter) {
                mFilterDialog = new AlertDialog.Builder(MainActivity.this).create();
                mFilterDialog.show();
                mFilterDialog.getWindow().setContentView(R.layout.filter_rule);
                setDialog(mFilterDialog, dialogX, dialogY);
                mFilterDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

                filter_enable = (CheckBox) mFilterDialog.findViewById(R.id.filter_enable);
                filter_enable.setChecked(filter_enable_flag);
                filter_list_mode = (RadioButton) mFilterDialog.findViewById(R.id.filter_list_mode);
                filter_mask_mode = (RadioButton) mFilterDialog.findViewById(R.id.filter_mask_mode);
                /*filter_list_mode.setOnClickListener(new onRadioButtonClickListener());
                filter_mask_mode.setOnClickListener(new onRadioButtonClickListener());*/
                filter_list = (ListView) mFilterDialog.findViewById(R.id.filter_list);
                filter_start = (EditText) mFilterDialog.findViewById(R.id.filter_start);
                filter_end = (EditText) mFilterDialog.findViewById(R.id.filter_end);
                filter_channel = (Spinner) mFilterDialog.findViewById(R.id.filter_channel);
                filter_mode = (Spinner) mFilterDialog.findViewById(R.id.filter_mode);
                ArrayAdapter<CharSequence> adapter0 = ArrayAdapter.createFromResource(
                        MainActivity.this, R.array.filter_channel,
                        android.R.layout.simple_spinner_item);
                adapter0.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                filter_channel.setAdapter(adapter0);
                mSetDisplayDouble = mFilterDialog.findViewById(R.id.set_display_double);
                mIdDisplay = mFilterDialog.findViewById(R.id.id_display);
                filter_list_mode.setChecked(filter_mode_select == 0);
                filter_mask_mode.setChecked(filter_mode_select == 1);
                adapter0 = ArrayAdapter.createFromResource(
                        MainActivity.this, filter_mode_select == 0 ? R.array.filter_list_mode : R.array.filter_mask_mode,
                        android.R.layout.simple_spinner_item);
                adapter0.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                filter_mode.setAdapter(adapter0);
                filter_mode.setSelection(0);
                filter_mode.setOnItemSelectedListener(new onSpinnerSelectListener());

                filter_list.setAdapter(filter_adapter);
                filter_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                        filter_adapter.remove(position);
                        return true;
                    }
                });
                mFilterDialog.findViewById(R.id.filter_ok).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        id_stanard_list0 = new MyArrays();
                        id_extend_list0 = new MyArrays();
                        id_stanard_list1 = new MyArrays();
                        id_extend_list1 = new MyArrays();
                        if (!getAllFilterId(id_stanard_list0, id_extend_list0, id_stanard_list1, id_extend_list1)) {
                            ShowMessage(getString(R.string.warn_out_of_limit_filter_num));
                            return;
                        }

                        if (filter_enable_flag) {
                            if (id_stanard_list0.getLength() == 0 && id_extend_list0.getLength() == 0 &&
                                    id_stanard_list1.getLength() == 0 && id_extend_list1.getLength() == 0) {
                                ShowMessage(getString(R.string.warn_no_filter));
                                return;
                            }
                        }

                        mFilterDialog.dismiss();
                        mOtaDialog = null;

                        if (filter_enable_flag) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    if (filter_list_mode.isChecked()) { //list mode
                                        if (id_stanard_list0.getLength() > 0 || id_extend_list0.getLength() > 0) {
                                            SendMessage(6, mCanBusHelper.setListIdFilter(0, Integer.parseInt(mComBaudrate.getSelectedItem().toString()),
                                                    id_stanard_list0.getArrays(), id_extend_list0.getArrays()), null);
                                        }

                                        if (id_stanard_list1.getLength() > 0 || id_extend_list1.getLength() > 0) {
                                            SendMessage(7, mCanBusHelper.setListIdFilter(1, Integer.parseInt(mComBaudrate.getSelectedItem().toString()),
                                                    id_stanard_list1.getArrays(), id_extend_list1.getArrays()), null);
                                        }
                                    } else { //mask mode
                                        if (id_stanard_list0.getLength() > 0 || id_extend_list0.getLength() > 0) {
                                            SendMessage(8, mCanBusHelper.setMaskIdFilter(0, Integer.parseInt(mComBaudrate.getSelectedItem().toString()),
                                                    id_stanard_list0.getArrays(), id_extend_list0.getArrays()), null);
                                        }

                                        if (id_stanard_list1.getLength() > 0 || id_extend_list1.getLength() > 0) {
                                            SendMessage(9, mCanBusHelper.setMaskIdFilter(1, Integer.parseInt(mComBaudrate.getSelectedItem().toString()),
                                                    id_stanard_list1.getArrays(), id_extend_list1.getArrays()), null);
                                        }
                                    }
                                }
                            }).start();
                        } else {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    SendMessage(12, mCanBusHelper.closeCanbusIdFilter(0, Integer.parseInt(mComBaudrate.getSelectedItem().toString())), null);
                                    SendMessage(13, mCanBusHelper.closeCanbusIdFilter(1, Integer.parseInt(mComBaudrate.getSelectedItem().toString())), null);
                                }
                            }).start();
                        }
                    }
                });

                ((CheckBox) mFilterDialog.findViewById(R.id.filter_enable)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        filter_enable.setChecked(isChecked);
                        filter_enable_flag = isChecked;
                        if (channels.size() <= 0) {
                            ShowMessage(getString(R.string.warn_no_filter));
                            filter_enable.setChecked(false);
                            filter_enable_flag = false;
                        }
                    }
                });

                mFilterDialog.findViewById(R.id.filter_add).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (filter_mode_select == 0) {
                            if (filter_mode.getSelectedItemPosition() == 0) {
                                int id = checkoutIDRule(filter_start.getText().toString(), 3, 0);
                                if (id < 0) {
                                    return;
                                }
                                if (id > 2047) {
                                    ShowMessage(getString(R.string.warn_standard_id_value_out_of_limit));
                                    return;
                                }
                                if (filter_adapter.add(filter_channel.getSelectedItem().toString(), filter_mode.getSelectedItem().toString(), filter_start.getText().toString(), "") < 0) {
                                    ShowMessage(getString(R.string.warn_the_same_filter_id));
                                }
                            } else if (filter_mode.getSelectedItemPosition() == 1) {
                                int startId = checkoutIDRule(filter_start.getText().toString(), 3, 0);
                                if (startId < 0)
                                    return;
                                if (startId > 2047) {
                                    ShowMessage(getString(R.string.warn_standard_start_id_value_out_of_limit));
                                    return;
                                }
                                int endId = checkoutIDRule(filter_end.getText().toString(), 3, 0);
                                if (endId < 0)
                                    return;
                                if (endId > 2047) {
                                    ShowMessage(getString(R.string.warn_standard_end_id_value_out_of_limit));
                                    return;
                                }
                                if (endId <= startId) {
                                    ShowMessage(getString(R.string.warn_end_is_more_than_end_id_value));
                                    return;
                                }
                                if (filter_adapter.add(filter_channel.getSelectedItem().toString(), filter_mode.getSelectedItem().toString(), filter_start.getText().toString(), filter_end.getText().toString()) < 0) {
                                    ShowMessage(getString(R.string.warn_the_same_filter_id));
                                }
                            } else if (filter_mode.getSelectedItemPosition() == 2) {
                                int id = checkoutIDRule(filter_start.getText().toString(), 8, 0);
                                if (id < 0) {
                                    return;
                                }
                                if (id > 536870911) {
                                    ShowMessage(getString(R.string.warn_extend_id_value_out_of_limit));
                                    return;
                                }
                                if (filter_adapter.add(filter_channel.getSelectedItem().toString(), filter_mode.getSelectedItem().toString(), filter_start.getText().toString(), "") < 0) {
                                    ShowMessage(getString(R.string.warn_the_same_filter_id));
                                }
                            } else if (filter_mode.getSelectedItemPosition() == 3) {
                                int startId = checkoutIDRule(filter_start.getText().toString(), 8, 0);
                                if (startId < 0)
                                    return;
                                if (startId > 536870911) {
                                    ShowMessage(getString(R.string.warn_extend_start_id_value_out_of_limit));
                                    return;
                                }
                                int endId = checkoutIDRule(filter_end.getText().toString(), 8, 0);
                                if (endId < 0)
                                    return;
                                if (endId > 536870911) {
                                    ShowMessage(getString(R.string.warn_extend_end_id_value_out_of_limit));
                                    return;
                                }
                                if (endId <= startId) {
                                    ShowMessage(getString(R.string.warn_end_is_more_than_end_id_value));
                                    return;
                                }
                                if (filter_adapter.add(filter_channel.getSelectedItem().toString(), filter_mode.getSelectedItem().toString(), filter_start.getText().toString(), filter_end.getText().toString()) < 0) {
                                    ShowMessage(getString(R.string.warn_the_same_filter_id));
                                }
                            }
                        } else {
                            if (filter_mode.getSelectedItemPosition() == 0) {
                                int id = checkoutIDRule(filter_start.getText().toString(), 3, 0);
                                if (id < 0) {
                                    return;
                                }
                                if (id > 2047) {
                                    ShowMessage(getString(R.string.warn_standard_id_value_out_of_limit));
                                    return;
                                }
                                filter_adapter.add_mask(filter_channel.getSelectedItem().toString(), filter_mode.getSelectedItem().toString(), filter_start.getText().toString(), filter_end.getText().toString());
                            } else if (filter_mode.getSelectedItemPosition() == 1) {
                                int id = checkoutIDRule(filter_start.getText().toString(), 8, 0);
                                if (id < 0) {
                                    return;
                                }
                                if (id > 536870911) {
                                    ShowMessage(getString(R.string.warn_extend_id_value_out_of_limit));
                                    return;
                                }
                                filter_adapter.add_mask(filter_channel.getSelectedItem().toString(), filter_mode.getSelectedItem().toString(), filter_start.getText().toString(), filter_end.getText().toString());
                            }
                        }
                    }
                });
            }
        }
    }

    private void functionsButtonClickEvent() {
        if (mFunctionsDialog != null)
            return;
        mFunctionsDialog = new AlertDialog.Builder(MainActivity.this).create();
        mFunctionsDialog.show();
        mFunctionsDialog.getWindow().setContentView(R.layout.functions);
        setDialog(mFunctionsDialog, screenWidth, screenHeight);
        mFunctionsDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

        //Set title
        ((TextView) mFunctionsDialog.findViewById(R.id.config_title)).setText(getString(R.string.func));

        mAppVersion = mFunctionsDialog.findViewById(R.id.demo_version);
        mAppVersion.setText(APKVersionCodeUtils.getVerName(this));

        mLibraryVersion = mFunctionsDialog.findViewById(R.id.lib_version);
        mLibraryVersion.setText(mCanBusHelper.getLibraryVersion());

        mAdcChannel = mFunctionsDialog.findViewById(R.id.adc_channel);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(
                this, R.array.adc_channel,
                android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mAdcChannel.setAdapter(adapter2);
        mAdcChannel.setSelection(0);

        mCollect = mFunctionsDialog.findViewById(R.id.adc_collect);
        mCollect.setOnClickListener(new onButtonClickListener());

        mAdcValue = mFunctionsDialog.findViewById(R.id.adc_value);
        mAdcVref = mFunctionsDialog.findViewById(R.id.adc_vref);
//
        mFunctionsClose = mFunctionsDialog.findViewById(R.id.close);
        mFunctionsClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUpdateDateTimeThread != null) {
                    mUpdateDateTimeThread.setAlive(false);
                    mUpdateDateTimeThread = null;
                }
                mFunctionsDialog.dismiss();
                mFunctionsDialog = null;
            }
        });

        mOta = mFunctionsDialog.findViewById(R.id.ota);
        mOta.setOnClickListener(new onButtonClickListener());
        mFilter = mFunctionsDialog.findViewById(R.id.filter);
        mRtcShowValue = mFunctionsDialog.findViewById(R.id.rtc_show);
        mRtcValueSet = mFunctionsDialog.findViewById(R.id.rtc_data);
        mSetRtc = mFunctionsDialog.findViewById(R.id.rtc_set);
        LinearLayout layout = mFunctionsDialog.findViewById(R.id.lin_rtc);
        layout.setVisibility(isRTC ? View.VISIBLE : View.GONE);

        mFilter.setOnClickListener(new onButtonClickListener());
        mCollect.setEnabled(!mOpen.isEnabled());
        mOta.setEnabled(mOpen.isEnabled());
        mFilter.setEnabled(mOpen.isEnabled());
        mSetRtc.setEnabled(!mOpen.isEnabled());
        mSetRtc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSetRtc.setEnabled(false);
                if (mRtcValueSet == null || mRtcValueSet.getText().toString().equals("")) {
                    ShowMessage("Please set data time first !");
                    mSetRtc.setEnabled(true);
                    return;
                }
                final String data = mRtcValueSet.getText().toString();
                final long time = SimpleDateUtils.getSimpleDateTimeToMcu(data);
                if (time < 1104508800000L || time > 4102415999000L) {//2005-2099
                    ShowMessage("Date and time format error or out of range, time setting range cannot exceed 2005~2099!");
                    mSetRtc.setEnabled(true);
                    return;
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int cnt = 3;
                        while (cnt > 0) {
                            if (mFunctionsDialog == null || (!mFunctionsDialog.isShowing())) {
                                return;
                            }
                            int ret = mCanBusHelper.setMcuRtcValue(0, time);
                            System.out.println("zyz --> setMcuRtcValue --> " + ret);
                            if (ret < 0) {
                                cnt--;
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                continue;
                            }
                            if (mFunctionsDialog == null || (!mFunctionsDialog.isShowing())) {
                                return;
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (mFunctionsDialog == null || (!mFunctionsDialog.isShowing())) {
                                        return;
                                    }
                                    mSetRtc.setEnabled(true);
                                    mRtcShowValue.setText(data);
                                    if (mUpdateDateTimeThread == null) {
                                        mUpdateDateTimeThread = new UpdateUnixDateTime(time / 1000);
                                        mUpdateDateTimeThread.start();
                                    } else {
                                        mUpdateDateTimeThread.setmDateTimeCount(time / 1000);
                                    }
                                }
                            });
                            break;
                        }
                        if (cnt <= 0) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (mFunctionsDialog == null || (!mFunctionsDialog.isShowing())) {
                                        return;
                                    }
                                    ShowMessage("Set failed !!!");
                                    mSetRtc.setEnabled(true);
                                }
                            });
                            return;
                        }
                    }
                }).start();
            }
        });
        if (!mOpen.isEnabled() && isRTC) {
            new Thread(mGetMcuRtcRunnable).start();
        }
    }

    private Runnable mGetMcuRtcRunnable = new Runnable() {
        long value = 0;

        @Override
        public void run() {
            int cnt = 3;
            while (cnt > 0) {
                if (mFunctionsDialog == null || (!mFunctionsDialog.isShowing())) {
                    return;
                }
                value = mCanBusHelper.getMcuRtcValue(0);
                System.out.println("zyz --> get mcu value --> " + value);
                if (value < 0) {
                    cnt--;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                final String data = SimpleDateUtils.getSimpleDateFromMcu(value);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mFunctionsDialog == null || (!mFunctionsDialog.isShowing())) {
                            return;
                        }
                        if (mUpdateDateTimeThread != null) {
                            System.out.println("zyz0 --> mUpdateDateTimeThread != null");
                            return;
                        }
                        mRtcShowValue.setText(data);
                        mRtcValueSet.setText(SimpleDateUtils.getSimpleDateFromAndroid(System.currentTimeMillis()));
                        mUpdateDateTimeThread = new UpdateUnixDateTime(value);
                        mUpdateDateTimeThread.start();
                    }
                });
                break;
            }
            if (cnt <= 0) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mFunctionsDialog == null || (!mFunctionsDialog.isShowing())) {
                            return;
                        }
                        ShowMessage("Get mcu rtc failed !!!");
                    }
                });
            }
        }
    };

    private class UIHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                mRecvRimes.setMyText(++recvCnt);
                if (recvCnt > 10000000) {
                    recvErrCnt = 0;
                    recvCnt = 0;
                }
            /*} else if (msg.what == 1) {
                endTime = System.currentTimeMillis();
                double sendExecTime = (endTime - startTime) / 1000f;
                mExecTime.setText(String.format("%.3f", sendExecTime) + " s");*/
//                mSend.setEnabled(true);
//                mStop.setEnabled(false);
            } else if (msg.what == 2) {
                mOpen.setEnabled(true);
                mReset.setEnabled(true);
                mClose.setEnabled(false);
//                mSend.setEnabled(false);
            }
//            else if(msg.what == 3){
//                mErrRimes.setMyText(++recvErrCnt);
//            }
            else if (msg.what == 4) {
                if (msg.arg1 > 0) {
                    ota_progress.setProgress(msg.arg1);
                    if (msg.arg1 == 100) {
                        ota_result.setText(getString(R.string.show_successs));
                        ota_result.setTextColor(Color.GREEN);
                    }
                } else {
                    ota_result.setText(getString(R.string.show_fail));
                    if (msg.arg1 == -2) {
                        ota_result.setText(getString(R.string.show_fail_msg));
                    }
                    ota_progress.setProgress(0);

                    ota_result.setTextColor(Color.RED);
                }
                ota_start.setEnabled(true);
                ota_close.setEnabled(true);
            } else if (msg.what == 5) {
                if (mOtaDialog != null) {
                    ota_version.setText(msg.obj.toString());
                }
            } else if (msg.what == 6) {
                if (msg.arg1 < 0) {
                    ShowMessage(getString(R.string.warn_can0_set_standard_filter_fail) + msg.arg1);
                } else {
                    ShowMessage(getString(R.string.warn_can0_set_standard_filter_success));
                }
            } else if (msg.what == 7) {
                if (msg.arg1 < 0) {
                    ShowMessage(getString(R.string.warn_can1_set_standard_filter_fail) + msg.arg1);
                } else {
                    ShowMessage(getString(R.string.warn_can1_set_standard_filter_success));
                }
            } else if (msg.what == 8) {
                if (msg.arg1 < 0) {
                    ShowMessage(getString(R.string.warn_can0_set_extend_filter_fail) + msg.arg1);
                } else {
                    ShowMessage(getString(R.string.warn_can0_set_extend_filter_success));
                }
            } else if (msg.what == 9) {
                if (msg.arg1 < 0) {
                    ShowMessage(getString(R.string.warn_can1_set_extend_filter_fail) + msg.arg1);
                } else {
                    ShowMessage(getString(R.string.warn_can1_set_extend_filter_success));
                }
            } else if (msg.what == 10) {
                mClose.setEnabled(true);
//                mSend.setEnabled(true);
            } else if (msg.what == 11) {
                mOpen.setEnabled(true);
                mReset.setEnabled(true);
                ShowMessage(getString(R.string.warn_open_faile));
                mClose.setEnabled(false);
            } else if (msg.what == 12) {
                if (msg.arg1 < 0) {
                    filter_enable_flag = false;
                    filter_enable.setChecked(false);
                    ShowMessage(getString(R.string.warn_can0_close_if_filter_fail) + msg.arg1);
                } else {
                    ShowMessage(getString(R.string.warn_can0_close_if_filter_success));
                }
            } else if (msg.what == 13) {
                if (msg.arg1 < 0) {
                    filter_enable_flag = true;
                    filter_enable.setChecked(true);
                    ShowMessage(getString(R.string.warn_can1_close_if_filter_fail) + msg.arg1);
                } else {
                    ShowMessage(getString(R.string.warn_can1_close_if_filter_success));
                }
            }
        }
    }

    /**
     * doing delay to do
     * Time required to execute the function,you can reduce delays
     */
    private final Runnable mDelayOpenRunnable = new Runnable() {
        @Override
        public void run() {
            mCanBusHelper.uninitialize();
        }
    };

    public class OtaThread extends Thread {
        private String path;

        OtaThread(String path) {
            this.path = path;
        }

        @Override
        public void run() {
            super.run();
            int res = mCanBusHelper.updateFirmware(new UpdateReturnCallback() {
                @Override
                public void onProgressUpdate(int value) {
                    SendMessage(4, value, null);
                }
            }, path, Integer.parseInt(mComBaudrate.getSelectedItem().toString()));
            if (res == 0) {
                SendMessage(4, 100, null);
            } else if (res == -19) {
                SendMessage(4, -2, null);
            } else {
                SendMessage(4, -1, null);
            }
        }
    }

    private final Runnable mCanRunnable = new Runnable() {
        @Override
        public void run() {
            final FileHelper fFile = new FileHelper();
            if (menu_save_enable) {
                if (fFile.open() < 0) {
                    System.out.println("zyz --> open log failed !!!");
                    return;
                }
            }
            mCanBusHelper.readCan(new CanBusCallback() {
                @Override
                public void onSetError() {
                }

                @Override
                public void onSendError() {
                    sendEnable = false;
                }

                @Override
                public void onReceiveCanbusData(int FF, int RTR, int DLC, int ID, int[] DATA) {
                    Log.d("qwe", "onReceiveCanbusData: " + DATA.length);
                    if (menu_save_enable) {
                        if (fFile.write(FF, RTR, ID, DLC, DATA) < 0)
                            System.out.println("zyz --> write failed !!!");
                    }
                    SendMessage(0, -1000, null);
                    mDisplay.setCanBusData(FF, RTR, DLC, ID, DATA);
                }
            });
            if (menu_save_enable) {
                fFile.close();
            }
            SendMessage(2, -1000, null);
        }
    };

    private int checkoutIDRule(String ID, int max_length, int min_length) {
        if (ID == null || ID.length() > max_length || ID.length() <= min_length) {
            ShowMessage(getString(R.string.warn_id_length_illegal));
            return -1;
        }

        for (int i = 0; i < ID.length(); i++) {
            if ((ID.getBytes()[i] < 0x30 || ID.getBytes()[i] > 0x39)
                    && (ID.getBytes()[i] < 0x41 || ID.getBytes()[i] > 0x46)
                    && (ID.getBytes()[i] < 0x61 || ID.getBytes()[i] > 0x66)) {
                ShowMessage(getString(R.string.warn_id_contains_illegal_char));
                return -2;
            }
        }

        BigInteger bigInt = new BigInteger(ID, 16);
        long longValue = bigInt.longValue();
        if (longValue > 536870911) {
            ShowMessage(getString(R.string.warn_id_size_exceeds));
            return -3;
        }
        return bigInt.intValue();
    }

    private int[] checkoutDataRule(String DATA) {
        if (DATA == null || DATA.length() != 23) {
            ShowMessage(getString(R.string.warn_data_len_unreasonable));
            return null;
        }

        for (int i = 0; i < 23; i++) {
            if (i != 2 && i != 5 && i != 8 && i != 11 && i != 14 && i != 17 && i != 20) {
                if ((DATA.getBytes()[i] < 0x30 || DATA.getBytes()[i] > 0x39)
                        && (DATA.getBytes()[i] < 0x41 || DATA.getBytes()[i] > 0x46)
                        && (DATA.getBytes()[i] < 0x61 || DATA.getBytes()[i] > 0x66)) {
                    ShowMessage(getString(R.string.warn_data_contains_illegal_char));
                    return null;
                }
            } else {
                if (DATA.getBytes()[i] != 0x20) {
                    ShowMessage(getString(R.string.warn_data_contains_illegal_char));
                    return null;
                }
            }
        }

        int[] data = new int[8];
        for (int i = 0; i < 8; i++) {
            BigInteger bigInt0 = new BigInteger(DATA.substring(i * 3, i * 3 + 1), 16);
            BigInteger bigInt1 = new BigInteger(DATA.substring(i * 3 + 1, i * 3 + 2), 16);
            data[i] = bigInt0.intValue() * 16 + bigInt1.intValue();
        }

        return data;
    }

    private void sendOneFrame(int can_interface, int FF, int RTR, int DLC, int ID, int[] data) {
        if (mCanBusHelper.sendFrame(can_interface, FF, RTR, DLC, ID, data) <= 0) {
            ShowMessage(getString(R.string.warn_send_fail));
        }
        Message msg = new Message();
        msg.what = 1;
        mUiHandler.sendMessage(msg);
    }

    /*private void sendMultipleFrame(final int can_interface, final int FF, final int RTR, final int DLC, final int ID, final int[] data, final int times, final int sendInterval, final boolean isIncrease) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                int allTimes = Integer.parseInt(mTimes.getText().toString());
                int timesDealy = Integer.parseInt(mTimesDelay.getText().toString());
                int sendTime = times;
                int id = ID;
                int ret = 0;
                for (int i = 0; i < allTimes; i++) {
                    sendTime = times;
                    while (((sendTime--) > 0) && sendEnable) {
                        ret = mCanBusHelper.sendFrame(can_interface, FF, RTR, DLC, id, data);
                        if (ret <= 0) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ShowMessage(getString(R.string.warn_send_fail));
                                }
                            });
                            sendEnable = false;
                            continue;
                        }
                        if (isIncrease) {
                            id++;
                        }
                        try {
                            Thread.sleep(sendInterval);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (!sendEnable)
                        break;
                    try {
                        Thread.sleep(timesDealy);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                Message msg = new Message();
                msg.what = 1;
                mUiHandler.sendMessage(msg);
            }
        }).start();
    }*/

    private void ShowMessage(String sMsg) {
        Toast.makeText(MainActivity.this, sMsg, Toast.LENGTH_SHORT).show();
    }

    private void SendMessage(int what, int arg1, Object obj) {
        Message msg = mUiHandler.obtainMessage();
        msg.what = what;
        if (arg1 > -999)
            msg.arg1 = arg1;
        if (obj != null)
            msg.obj = obj;
        mUiHandler.sendMessage(msg);
    }

    @Override
    protected void onDestroy() {
        mCanBusHelper.uninitialize();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
        super.onDestroy();
    }

    private class UpdateUnixDateTime extends Thread {
        private long mDateTimeCount = 0;
        private boolean alive = true;

        public UpdateUnixDateTime(long time) {
            mDateTimeCount = time;
        }

        @Override
        public void run() {
            while (true) {
                if (mFunctionsDialog == null || (!mFunctionsDialog.isShowing())) {
                    return;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (mFunctionsDialog == null || (!mFunctionsDialog.isShowing())) {
                    return;
                }
                if (!alive)
                    return;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mRtcShowValue.setText(SimpleDateUtils.getSimpleDateFromMcu(++mDateTimeCount));
                    }
                });
            }
        }

        private void setAlive(boolean live) {
            alive = live;
        }

        private void setmDateTimeCount(long time) {
            mDateTimeCount = time;
        }
    }

    ;
}
