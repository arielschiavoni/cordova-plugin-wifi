var exec = require("cordova/exec");

var Wifi = function(){};

Wifi.prototype.scan = function (successCallback, errorCallback) {
	cordova.exec(successCallback, errorCallback, "Wifi", "scan", []);
};

Wifi.prototype.connect = function (ssid, successCallback, errorCallback) {
	cordova.exec(successCallback, errorCallback, "Wifi", "connect", [ssid]);
};

module.exports = new Wifi();