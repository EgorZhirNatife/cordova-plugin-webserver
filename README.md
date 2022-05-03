# cordova-plugin-webserver
Cordova plugin for localhost web server written in Kotlin and Ktor

## Install plugin
    cordova plugin add https://github.com/EgorZhirNatife/cordova-plugin-webserver.git
## Supported platforms
- __Android@9.0.0__

## Let's start
- __First step:__ Add from the plugin folder "cert" keystore.bks to ```YourCordovaProject\platforms\android\app\src\main\res\raw\{add keystore.bks here}```
- __Second step:__ In your index.js file add these line to start server
```js
cordova.plugins.webServer.startServer(function(result) { console.log(result); }, function(error) { console.log(error); })
```
- __Third step:__ Build and run the application. Voila, enjoy the web server!
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
https://localhost:3005/static-content/{your path from assets directory}
```
__Example__ with serving index.html:
```
https://localhost:3005/static-content/www/index.html
```
## Executing Cordova methods
POST request:
```
curl -X POST -F service=CordovaServiceName -F action=CordovaMethod -F args=["Args"] https://localhost:3005/cordova-request
```
__Example__ with common cordova request:
```js
Q.Users.Cordova.Labels.get(["e"], function(data) { console.log(data); }, function(err) { console.log(err); })
```
equivalent to
```
curl -X POST -F service=QUsersCordova -F action=get -F args=[["e"]] https://localhost:3005/cordova-request
```
