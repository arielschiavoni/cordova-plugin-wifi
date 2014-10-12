var exec = require("cordova/exec");

var Wifi = function(){};

Wifi.prototype.scan = function (successCallback, errorCallback) {
	cordova.exec(successCallback, errorCallback, "Wifi", "scan", []);
};

module.exports = new Wifi();