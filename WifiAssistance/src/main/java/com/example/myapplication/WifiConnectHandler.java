package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.example.myapplication.domain.SimpleWifiInfo;
import com.example.myapplication.utils.WiFiConnect;
import com.example.myapplication.utils.WifiAdmin;
import com.google.common.base.Preconditions;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

/**
 * Created by yuxuehai on 16-12-28.
 */

public class WifiConnectHandler extends AppCompatActivity {
    private static final String TAG_ASSIST = "[WifiConnectHandler]-";


    @Override
    protected void onResume() {
        super.onResume();
        handleIntent();
        finish();
    }

    private void handleIntent() {
        resolveNdefMessagesIntent(getIntent());
    }

    private void resolveNdefMessagesIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action))
        // if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action) ||
        // NfcAdapter.ACTION_TECH_DISCOVERED.equals(action))
        {
            NdefMessage[] messages = null;
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                messages = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    messages[i] = (NdefMessage) rawMsgs[i];
                }
            } else {
                // Unknown tag type
                byte[] empty = new byte[]{};
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty);
                NdefMessage msg = new NdefMessage(new NdefRecord[]{
                        record
                });
                messages = new NdefMessage[]{
                        msg
                };
            }
            // Setup the views
            // setTitle(R.string.title_scanned_tag);
            processNDEFTag_RTDText(messages);
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
        } else if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
        } else {
            finish();
            return;
        }

    }

    private void processNDEFTag_RTDText(NdefMessage[] messages) {
        // TODO Auto-generated method stub
        if (messages == null || messages.length == 0) {
            return;
        }

        for (int i = 0; i < messages.length; i++) {
            int length = messages[i].getRecords().length;
            NdefRecord[] records = messages[i].getRecords();
            for (int j = 0; j < length; j++) {
                for (NdefRecord record : records) {
                    if (isTextRecord(record)) {
                        parseRTD_TEXTRecord(record);
                    }
                }
            }
        }
    }

    private void parseRTD_TEXTRecord(NdefRecord record) {
        Preconditions.checkArgument(record.getTnf() == NdefRecord.TNF_WELL_KNOWN);
        Preconditions.checkArgument(Arrays.equals(record.getType(), NdefRecord.RTD_TEXT));

        String payloadStr = "";
        byte[] payload = record.getPayload();
        Byte statusByte = record.getPayload()[0];

        String textEncoding = ((statusByte & 0200) == 0) ? "UTF-8" : "UTF-16";// 0x80=0200
        int languageCodeLength = statusByte & 0077; // & 0x3F=0077(bit 5 to 0)
        String languageCode = new String(payload, 1, languageCodeLength, Charset.forName("UTF-8"));
        try {
            payloadStr = new String(payload, languageCodeLength + 1, payload.length
                    - languageCodeLength - 1, textEncoding);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String wifiConfigString[] = payloadStr.split(";");

        String type, ssid, key;
        if (wifiConfigString[0] == null || wifiConfigString[1] == null
                || wifiConfigString[2] == null) {
            return;
        }
        type = wifiConfigString[0];
        ssid = wifiConfigString[1];
        key = wifiConfigString[2];
        SimpleWifiInfo wifiInfo = new SimpleWifiInfo(type, ssid, key);

        // Method 1
         setNewWifi(wifiInfo);

        // Method 2
        WifiAdmin wifiAdmin = new WifiAdmin(this);
        wifiAdmin.openWifi();
        wifiAdmin.addNetwork(wifiAdmin.CreateWifiInfo(ssid, key, type));

        // Method 3
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WiFiConnect wiFiConnect = new WiFiConnect(wifiManager);
        WiFiConnect.WifiCipherType Type;
        if (type.equals("none")) {
            Type = WiFiConnect.WifiCipherType.WIFICIPHER_NOPASS;
        } else if (type.equals("wep")) {
            Type = WiFiConnect.WifiCipherType.WIFICIPHER_WEP;
        } else if (type.equals("wpa")) {
            Type = WiFiConnect.WifiCipherType.WIFICIPHER_WPA;
        } else {
            Type = WiFiConnect.WifiCipherType.WIFICIPHER_INVALID;
        }
        boolean flag = wiFiConnect.Connect(ssid, key, Type);
        if (flag == true) {
            showLongToast("Now connected to known network \"" + wifiInfo.getSsid());
        } else {
            showLongToast("Creating connection failed.");
        }
    }


    protected void setNewWifi(SimpleWifiInfo wifiInfo) {
        WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);

        boolean foundAKnownNetwork = false;
        List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration wifiConfiguration : configuredNetworks) {
            if (wifiConfiguration.SSID.equals("\"" + wifiInfo.getSsid() + "\"")) {
                foundAKnownNetwork = true;
                boolean result = wifiManager.enableNetwork(wifiConfiguration.networkId, true);
                if (result) {
                    showLongToast("Now connected to known network \""
                            + wifiInfo.getSsid()
                            + "\". If you want to set a new WPA key, please delete the network first.");
                } else {
                    showLongToast("Connection to a known network failed.");
                }
            }
        }

        if (!foundAKnownNetwork) {
            setupNewNetwork(wifiInfo, wifiManager);
        }
    }

    protected void setupNewNetwork(SimpleWifiInfo wifiInfo, WifiManager wifiManager) {
        WifiConfiguration wc = new WifiConfiguration();
        wc.SSID = "\"" + wifiInfo.getSsid() + "\"";

        if (wifiInfo.isKeyPreHashed())
            wc.preSharedKey = wifiInfo.getKey();
        else
            wc.preSharedKey = "\"" + wifiInfo.getKey() + "\"";

        int networkId = wifiManager.addNetwork(wc);
        boolean result = wifiManager.enableNetwork(networkId, true);

        if (result) {
            showLongToast("Now connected to \"" + wifiInfo.getSsid() + "\"");
            wifiManager.saveConfiguration();
        } else {
            showLongToast("Creating connection failed. " + wc);
        }
    }



    private void showLongToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }

    private boolean isTextRecord(NdefRecord record) {
        if (record.getTnf() == NdefRecord.TNF_WELL_KNOWN) {
            if (Arrays.equals(record.getType(), NdefRecord.RTD_TEXT)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }


}
