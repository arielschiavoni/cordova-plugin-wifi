package com.arielschiavoni.cordova.plugin.wifi;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.ScanResult;

import java.util.List;

public class Wifi extends CordovaPlugin {

    private static final String SCAN = "scan";
    private static final String CONNECT = "connect";

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
        } else if (action.equals(CONNECT)) {
            return this.connect(callbackContext, data);
        }
        callbackContext.error("Incorrect action parameter: " + action);
        return false;
    }

    /**
     *  Scan wifi networks.
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

    /**
     *  Connect to a network.
     *  @param  callbackContext A Cordova callback context
     *  @param  data JSON Array, with [0] being SSID to connect
     *  @return true if network connected, false if failed
     */
    private boolean connect(CallbackContext callbackContext, JSONArray data) throws JSONException {
        String ssid = data.getString(0);
        int networkIdToConnect = getNetworkId(ssid);
        if (networkIdToConnect > 0) {
            wifiManager.disableNetwork(networkIdToConnect);
            wifiManager.enableNetwork(networkIdToConnect, true);
            callbackContext.success("Network " + ssid + " connected!");
            return true;
        }
        else {
            callbackContext.error("Network " + ssid + " not found!");
            return false;
        }
    }

    /**
     *  Get the network id from the list of configured networks using SSID.
     */
    private int getNetworkId(String ssid) {
        List<WifiConfiguration> currentNetworks = wifiManager.getConfiguredNetworks();
        int networkId = -1;
        for (WifiConfiguration test : currentNetworks) {
            if ( test.SSID.equals(ssid) ) {
                networkId = test.networkId;
            }
        }
        return networkId;
    }
}