# cordova-plugin-webserver
Cordova plugin for localhost web server written in Kotlin and Ktor

## Install plugin
    cordova plugin add https://github.com/EgorZhirNatife/cordova-plugin-webserver.git
## Supported platforms
- __Android@9.0.0__

## Let's start
- __First step:__ In your index.js file add this line to start the web server
```js
cordova.plugins.webServer.startServer(function(result) { console.log(result); }, function(error) { console.log(error); })
```
- __Second step:__ Build and run your app. Voila, enjoy the web server!
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
http://localhost:3005/static-content/{your path from assets directory}
```
__Example__ with serving index.html:
```
http://localhost:3005/static-content/www/index.html
```
## Executing Cordova methods
POST request:
```
curl -X POST -F service=CordovaServiceName -F action=CordovaMethod -F args=["Args"] http://localhost:3005/cordova-request
```
__Example__ with common cordova request:
```js
Q.Users.Cordova.Labels.get(["e"], function(data) { console.log(data); }, function(err) { console.log(err); })
```
equivalent to
```
curl -X POST -F service=QUsersCordova -F action=get -F args=[["e"]] http://localhost:3005/cordova-request
```
