package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.myapplication.com.example.myapplication.dao.ActivityDao;
import com.example.myapplication.utils.ToastUtil;

public class MainActivity extends AppCompatActivity implements ActivityDao {


    private Context mContext;
    private Button mBt_save;
    private EditText mEd_ssid;
    private EditText mEd_passwd;
    private Spinner mSp_sec;
    private String sec_str;
    private String ssid_str;
    private String key_str;


    private static final String[] spinnerStr = {"NONE", "WEP", "WPA/WPA2"};

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
    }

    @Override
    public void initData() {


        mBt_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null == ssid_str) {
                    ToastUtil.showToast(mContext, "Default SSID");
                    ssid_str = "**";//默认ssid
                } else {
                    ssid_str = mEd_ssid.getText().toString().trim();
                }

                if (null == key_str) {
                    ToastUtil.showToast(mContext, "默认密码为空");
                    key_str = " ";//默认ssid
                } else {
                    key_str = mEd_passwd.getText().toString().trim();
                }

            }
        });
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
}
