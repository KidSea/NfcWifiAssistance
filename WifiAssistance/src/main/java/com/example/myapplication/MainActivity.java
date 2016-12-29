package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.myapplication.com.example.myapplication.dao.ActivityDao;
import com.example.myapplication.domain.SimpleWifiInfo;
import com.example.myapplication.utils.ToastUtil;

public class MainActivity extends AppCompatActivity implements ActivityDao {

    private static  final  String Tag_ASSIST = "[NfcWifiAssistant]-";
    private static final String[] spinnerStr = {"NONE", "WEP", "WPA/WPA2"};

    private Context mContext;
    private Button mBt_save;
    private EditText mEd_ssid;
    private EditText mEd_passwd;
    private Spinner mSp_sec;
    private String sec_str = null;
    private String ssid_str = null;
    private String key_str = null;
    private AlertDialog alertDialog = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        initUI();
        initData();
    }

    @Override
    public void initUI() {
        mBt_save = (Button) findViewById(R.id.bt_save);
        mEd_ssid = (EditText) findViewById(R.id.ed_ssid);
        mEd_passwd = (EditText) findViewById(R.id.ed_passwd);
        mSp_sec = (Spinner) findViewById(R.id.sp_sec);

        //将可选内容与ArrayAdapter连接起来
        ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, spinnerStr);

        //设置下拉风格
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //设置适配器
        mSp_sec.setAdapter(mAdapter);
        //添加Spinner事件
        mSp_sec.setOnItemSelectedListener(new SpinnerXMLSelectedListener());
        //设置默认值
        mSp_sec.setVisibility(View.VISIBLE);
        mSp_sec.setSelection(2, true);



        mEd_ssid.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                ssid_str = mEd_ssid.getText().toString();
            }
        });


        mEd_passwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                key_str = mEd_passwd.getText().toString();
            }
        });


    }

    @Override
    public void initData() {


        mBt_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null == ssid_str) {
                    ToastUtil.showToast(mContext, "Default SSID");
                    ssid_str = "yuxuehai";//默认ssid
                } else {
                    ssid_str = mEd_ssid.getText().toString().trim();
                }

                if (null == key_str) {
                    ToastUtil.showToast(mContext, "Default KEY, NULL");
                    key_str = "111111";//默认key
                } else {
                    key_str = mEd_passwd.getText().toString();
                }
                SimpleWifiInfo simpleWifiInfo = new SimpleWifiInfo(sec_str, ssid_str, key_str);

                Intent intent = new Intent(mContext, WifiConnectConfigWriter.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("SIMPLE_WIFI_INFO", simpleWifiInfo);
                MainActivity.this.startActivityForResult(intent, 100);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        String str = item.getTitle().toString();
        switch (item.getItemId()) {

            case R.id.nfc_setting:
                ToastUtil.showToast(mContext,str);
                Intent setnfc = new Intent(Settings.ACTION_NFC_SETTINGS);
                startActivity(setnfc);
                break;
            case R.id.wifi_setting:
                Intent setwifi = new Intent(Settings.ACTION_SETTINGS);
                startActivity(setwifi);
                ToastUtil.showToast(mContext,str);
                break;
            case R.id.action_about:
                ToastUtil.showToast(mContext,str);
                dialog();
                break;
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            finish();
        }
    }

    class SpinnerXMLSelectedListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            //监听选择事件
            switch (position) {
                case 0:
                    sec_str = "none";
                    break;
                case 1:
                    sec_str = "wep";
                    break;
                case 2:
                    sec_str = "wpa";
                    break;
                default:
                    sec_str = "wpa";
                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    protected void dialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(getString(R.string.dilog_notice));
        alertDialog = builder.create();
        builder.setCancelable(true);// back
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
    }
}
