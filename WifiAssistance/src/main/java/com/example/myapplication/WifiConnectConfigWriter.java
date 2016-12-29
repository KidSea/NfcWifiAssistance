package com.example.myapplication;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.example.myapplication.Service.MyActivityFinishHandler;
import com.example.myapplication.Service.WriteTask;
import com.example.myapplication.com.example.myapplication.dao.ActivityDao;
import com.example.myapplication.domain.SimpleWifiInfo;
import com.example.myapplication.utils.BobNdefMessage;
import com.example.myapplication.utils.SimpleWifiInfoConverter;
import com.example.myapplication.utils.ToastUtil;

/**
 * Created by yuxuehai on 16-12-28.
 */

public class WifiConnectConfigWriter extends AppCompatActivity implements ActivityDao {
    private static final String Tag_ASSIST = "[WifiConnectConfigWriter]-";
    private Context mContext;
    private Button mBt_cancel;
    private IntentFilter[] mFilters;
    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;
    private String[][] mTechLists;
    private SimpleWifiInfo mSimpleWifiInfo;
    private NdefMessage NDEFMsg2Write = null;
    private WriteTask mWriteTask;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_nfc);
        mContext = this;

        CheckNFCFunction();

        initUI();
        initData();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSimpleWifiInfo = WifiConnectConfigWriter.this.getIntent().getParcelableExtra("SIMPLE_WIFI_INFO");
        enableForegroundDispatch();
    }

    private void enableForegroundDispatch() {
        if (mNfcAdapter != null) {
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        disableForegroundDispatch();
    }

    private void disableForegroundDispatch() {
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag detectTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (detectTag != null && mSimpleWifiInfo != null) {

            if (supportedTechs(detectTag.getTechList())) {
                String payLoadStr = SimpleWifiInfoConverter.toString(mSimpleWifiInfo);
                NDEFMsg2Write = BobNdefMessage.getNdefMsg_from_RTD_TEXT(payLoadStr, false, false);
                mWriteTask = (WriteTask) new WriteTask(this, NDEFMsg2Write, detectTag).execute();
            } else {
                ToastUtil.showToast(this, "This tag type is not supported");
            }
        }
    }

    private boolean supportedTechs(String[] techList) {
        boolean tech_ndef = false;
        for (String tech : techList) {
            if (tech.equals("android.nfc.tech.Ndef")
                    || tech.equals("android.nfc.tech.NdefFormatable")) {
                tech_ndef = true;
            } else {
                tech_ndef = false;
            }
        }
        if (tech_ndef) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void initUI() {

        mBt_cancel = (Button) findViewById(R.id.bt_cancel);
        mBt_cancel.setOnClickListener(new MyActivityFinishHandler(this));
    }

    @Override
    public void initData() {


        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);


        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter ndfeDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter techDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);

        mFilters = new IntentFilter[]{ndfeDetected};

        mTechLists = new String[][]{
                new String[]{
                        Ndef.class.getName()
                },
                new String[]{
                        NdefFormatable.class.getName()
                }
        };
    }

    private void CheckNFCFunction() {

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            ToastUtil.showToast(this, "该手机不支持NFC");
            return;
        }
        if (!mNfcAdapter.isEnabled()) {
            ToastUtil.showToast(this, "请在系统设置中先启用NFC功能！");
            return;
        }
    }
}
