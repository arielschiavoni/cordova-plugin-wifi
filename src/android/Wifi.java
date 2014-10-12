package com.arielschiavoni.cordova.plugin.wifi;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.ScanResult;

import java.util.List;

public class Wifi extends CordovaPlugin {

    private static final String SCAN = "scan";
    private WifiManager wifiManager;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        this.wifiManager = (WifiManager) cordova.getActivity().getSystemService(Context.WIFI_SERVICE);
    }

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
        if (action.equals(SCAN)) {
            return this.scan(callbackContext);
        }
        callbackContext.error("Incorrect action parameter: " + action);
        return false;
    }

    /**
     *  This method uses the callbackContext.success method to send a JSONArray
     *  of the scanned networks.
     *  @param  callbackContext A Cordova callback context
     *  @return true
     */
    private boolean scan(CallbackContext callbackContext) throws JSONException {
        List<ScanResult> scanResults = wifiManager.getScanResults();
        JSONArray returnList = new JSONArray();

        for (ScanResult scan : scanResults) {
            JSONObject wifi = new JSONObject();
            wifi.put("ssid", scan.SSID);
            wifi.put("bssid", scan.BSSID);
            wifi.put("capabilities", scan.capabilities);
            wifi.put("frequency", scan.frequency);
            wifi.put("level", scan.level);
            returnList.put(wifi);
        }

        callbackContext.success(returnList);
        return true;
    }
}