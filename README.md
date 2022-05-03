# cordova-plugin-webserver
Cordova plugin for localhost web server written in Kotlin and Ktor

## Install plugin
    cordova plugin add https://github.com/EgorZhirNatife/cordova-plugin-webserver.git
## Supported platforms
- __Android@9.0.0__

## Example:

#### Start Server
```js
cordova.plugins.webServer.startServer(function(result) { console.log(result); }, function(error) { console.log(error); })
```
#### Stop Server
```js
cordova.plugins.webServer.stopServer(function(result) { console.log(result); }, function(error) { console.log(error); })
```
# Requests

## Serving static content
GET request:
```
http://127.0.0.1:8080/static-content/{your path from assets directory}
```
__Example__ with serving index.html:
```
http://127.0.0.1:8080/static-content/www/index.html
```
## Executing Cordova methods
POST request:
```
curl -X POST -F service=CordovaServiceName -F action=CordovaMethod -F args=["Args"] http://127.0.0.1:8080/cordova-request
```
__Example__ with common cordova request:
```js
Q.Users.Cordova.Labels.get(["e"], function(data) { console.log(data); }, function(err) { console.log(err); })
```
equivalent to
```
curl -X POST -F service=QUsersCordova -F action=get -F args=[["e"]] http://192.168.0.101:8080/cordova-request
```
