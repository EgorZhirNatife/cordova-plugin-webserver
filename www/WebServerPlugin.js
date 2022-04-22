var exec = require('cordova/exec');

module.exports = {
    startServer: function(success, error) {
      exec(success, error, "WebServerPlugin", "startServer", []);
    },
    stopServer: function() {
      stopServer(success, error, "WebServerPlugin", "stopServer", []);
    }
};