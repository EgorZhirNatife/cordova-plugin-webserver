var exec = require('cordova/exec');

module.exports = {
    startServer: function(success, error) {
      exec(success, error, "LocalhostWebServer", "startServer", []);
    },
    stopServer: function() {
      stopServer(success, error, "LocalhostWebServer", "stopServer", []);
    }
};