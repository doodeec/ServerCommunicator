# CHANGELOG

## 1.3.5

* SSL context access (for custom trust store configuration)
* hostname verifier configuration support

## 1.3.4

* more error types to determine some common status response codes

## 1.3.3

* fix for empty responses (status code 200 without response body)

## 1.3.2

* UI thread optimization
* using GSON as default, JSONObject/JSONArray response can be used via GSON alternatives JsonObject/JsonArray (lowercase)
* possibility to change default buffer size for response stream
* CommunicatorResponse object now contains original stream for simpler debugging

## 1.3.1

* improved handling of class cast exceptions
* CommunicatorResponse object now contains URL of the request

## 1.3.0

* internal GSON support

## 1.2.2

* internal handling of EOFException during POST request

## 1.2.1

* added response interceptor
* added debug logging
* error handling improvement
* API 23 support