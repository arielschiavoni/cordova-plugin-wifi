package com.arielschiavoni.cordova.plugin.wifi;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.ScanResult;

import java.util.List;

public class Wifi extends CordovaPlugin {

    private static final String SCAN = "scan";
    private static final String CONNECT = "connect";

    private static final String AUTH_TYPE_WPA = "WPA";
    private static final String AUTH_TYPE_WEP = "WEP";

    private WifiManager wifiManager;
    private IntentFilter intentFilter;
    private BroadcastReceiver receiver;
    private CallbackContext callbackContext;

    /**
     * Constructor.
     */
    public Wifi() {
        this.receiver = null;
    }

    /**
     * Stop scan receiver.
     */
    public void onDestroy() {
        this.removeScanReceiver();
    }

    /**
     * Stop scan receiver.
     */
    private void removeScanReceiver() {
        if (this.receiver != null) {
            cordova.getActivity().unregisterReceiver(this.receiver);
        }
    }

    /**
     * Add scan receiver.
     */
    private void addScanReceiver() {
        // We need to listen to scan results events
        cordova.getActivity().registerReceiver(this.receiver, intentFilter);
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        this.wifiManager = (WifiManager) cordova.getActivity().getSystemService(Context.WIFI_SERVICE);
        this.callbackContext = null;
        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        this.receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    sendScanResults();
                } catch (JSONException e) {
                    callbackContext.error(e.getMessage());
                }
            }
        };
    }

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;
        if(!wifiManager.isWifiEnabled()) wifiManager.setWifiEnabled(true);
        if (action.equals(SCAN)) {
            return this.startScan();
        } else if (action.equals(CONNECT)) {
            return this.connect(data);
        }
        callbackContext.error("Incorrect action parameter: " + action);
        return false;
    }

    /**
     *  Scan wifi networks.
     *  @return true
     */
    private boolean startScan() {
        addScanReceiver();
        if (wifiManager.startScan()) {
            return true;
        } else {
            callbackContext.error("Scan failed");
            return false;
        }
    }

    /**
     *  Get wifi scan results and send them.
     *  @return true
     */
    private boolean sendScanResults() throws JSONException {
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
        removeScanReceiver();
        return true;
    }

    /**
     *  Connect to a network.
     *  @param data JSON Array with [0] == SSID, [1] == auth type [2] == password
     *  @return true if network connected, false if failed
     */
    private boolean connect(JSONArray data) throws JSONException {
        String ssid = data.getString(0);
        int networkId = getNetworkId(ssid);
        if (networkId > 0) {
            wifiManager.enableNetwork(networkId, true);
            callbackContext.success("Network " + ssid + " connected!");
            return true;
        } else {
            networkId = registerNetwork(ssid, data.getString(1), data.getString(2));
            if(networkId > 0) {
                wifiManager.enableNetwork(networkId, true);
                callbackContext.success("Network " + ssid + "registered and connected!");
                return true;
            } else {
                callbackContext.error("Could not connect to network: " + ssid);
                return false;
            }
        }
    }

    /**
     *  Get the network id from the list of configured networks using SSID.
     */
    private int getNetworkId(String ssid) {
        List<WifiConfiguration> currentNetworks = wifiManager.getConfiguredNetworks();
        int networkId = -1;
        ssid = "\"".concat(ssid).concat("\"");
        for (WifiConfiguration test : currentNetworks) {
            if ( test.SSID.equals(ssid) ) {
                networkId = test.networkId;
            }
        }
        return networkId;
    }

    /**
     * Register a network to the list of available WiFi networks.
     * @return true if add successful, false if add fails
     */
    private int registerNetwork(String ssid, String authType, String password) throws JSONException {
        WifiConfiguration wifi = new WifiConfiguration();
        int networkId = -1;

        if (authType.equals(AUTH_TYPE_WPA)) {
            wifi.SSID = "\"".concat(ssid).concat("\"");
            wifi.preSharedKey = "\"".concat(password).concat("\"");

            wifi.status = WifiConfiguration.Status.ENABLED;
            wifi.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wifi.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wifi.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            wifi.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wifi.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wifi.allowedProtocols.set(WifiConfiguration.Protocol.RSN);

            networkId = wifiManager.addNetwork(wifi);
            if(networkId == -1) {
                callbackContext.error("Error trying to register network: " + ssid);
            } else {
                wifiManager.saveConfiguration();
            }

        } else if (authType.equals(AUTH_TYPE_WEP)) {
            callbackContext.error("WEP unsupported");
        } else {
            callbackContext.error("Authentication Type Not Supported: " + authType);
        }
        return networkId;
    }

}