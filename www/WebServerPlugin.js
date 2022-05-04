var exec = require('cordova/exec');

module.exports = {
    startServer: function(success, error) {
      exec(success, error, "WebServerPlugin", "startServer", []);
    },
    stopServer: function(success, error) {
      exec(success, error, "WebServerPlugin", "stopServer", []);
    }
};