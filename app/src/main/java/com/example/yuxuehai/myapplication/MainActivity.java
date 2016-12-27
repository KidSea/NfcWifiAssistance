package com.example.yuxuehai.myapplication;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {


    private TextView mTextView;
    private NfcAdapter mNfcAdapter;
    private NdefMessage mNdefPushMessage;
    private PendingIntent mPendingIntent;
    private String readResult;
    private IntentFilter mNdef;
    private String[][] techListsArray;
    private IntentFilter[] mIntentFilters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.tv_info);
        //获取NFC控制器
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        init();


    }

    private void init() {
        mTextView = (TextView) findViewById(R.id.tv_info);
        //获取NFC控制器
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mNfcAdapter == null) {
            mTextView.setText("设备不支持NFC！");
            return;
        }
        if (!mNfcAdapter.isEnabled()) {
            mTextView.setText("请在系统设置中先启用NFC功能！");
            return;
        }

        mPendingIntent = PendingIntent.
                getActivity(this, 0, new Intent(this, getClass()).
                        addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        mNdef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);

        try {
            mNdef.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }

        mIntentFilters = new IntentFilter[]{mNdef};
        techListsArray = new String[][]{new String[]{NfcF.class.getName()}};

    }


    @Override
    protected void onResume() {
        super.onResume();

        mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, null,
                null);

        //判別消息內容
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            readFromTag(getIntent());
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mNfcAdapter != null) {
            //隐式启动
            mNfcAdapter.disableForegroundDispatch(this);
           // mNfcAdapter.disableForegroundNdefPush(this);
        }
    }

    //16进制字符串转换为String
    private String hexString = "0123456789ABCDEF";

    public String decode(String bytes) {
        if (bytes.length() != 30) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream(
                bytes.length() / 2);
        // 将每2位16进制整数组装成一个字节
        for (int i = 0; i < bytes.length(); i += 2)
            baos.write((hexString.indexOf(bytes.charAt(i)) << 4 | hexString
                    .indexOf(bytes.charAt(i + 1))));
        return new String(baos.toByteArray());
    }

    // 字符序列转换为16进制字符串
    private static String bytesToHexString(byte[] src, boolean isPrefix) {
        StringBuilder stringBuilder = new StringBuilder();
        if (isPrefix == true) {
            stringBuilder.append("0x");
        }
        if (src == null || src.length <= 0) {
            return null;
        }
        char[] buffer = new char[2];
        for (int i = 0; i < src.length; i++) {
            buffer[0] = Character.toUpperCase(Character.forDigit(
                    (src[i] >>> 4) & 0x0F, 16));
            buffer[1] = Character.toUpperCase(Character.forDigit(src[i] & 0x0F,
                    16));
            System.out.println(buffer);
            stringBuilder.append(buffer);
        }
        return stringBuilder.toString();
    }

    private boolean readFromTag(Intent intent) {
        Parcelable[] rawArray = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        NdefMessage mNdefMsg = (NdefMessage) rawArray[0];
        NdefRecord mNdefRecord = mNdefMsg.getRecords()[0];
        try {
            if (mNdefRecord != null) {
                readResult = new String(mNdefRecord.getPayload(), "UTF-8");
                String str = readResult.substring(3,readResult.length());
                mTextView.setText(str);
                return true;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        ;
        return false;
    }


    //获取系统隐式启动的
    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);

    }
}
