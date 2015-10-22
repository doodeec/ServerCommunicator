# ServerCommunicator
Simple server communicator for RESTful data source

[ ![Download](https://api.bintray.com/packages/doodeec/maven/server-communicator/images/download.svg) ](https://bintray.com/doodeec/maven/server-communicator/_latestVersion)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.doodeec.utils/serverCommunicator/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.doodeec.utils/serverCommunicator)

[JavaDoc Documentation](http://doodeec.github.io/ServerCommunicator/)<br/>

### Description
Library module for communicating with your REST server.
The library provides classes for communicating with REST API, while requests are fully maintainable
(can be cloned to be executed repeatedly, can be cancelled while executing, can be used with custom headers
and custom interceptor) from your app.


### Basic Usage
Import library in your application. Either as a `.aar` file in your `libs` folder
or as a maven dependency.

    dependencies {
        ...
        compile 'com.doodeec.utils:serverCommunicator:1.3.5@aar'
    }

In your code, you can then use it via `ServerRequest` and `ImageServerRequest` classes.

    ServerRequest request = new ServerRequest(BaseServerRequest.RequestType.GET, new BaseRequestListener<MyObject>() {
        @Override
        public void onSuccess(MyObject object) {
            ...
        }

        @Override
        public void onError(RequestError requestError) {
            ...
        }

        @Override
        public void onCancelled() {
            ...
        }

        @Override
        public void onProgress(Integer progress) {
            ...
        }
    }, MyObject.class);
    request.executeInParallel(url);

Both ServerRequest and ImageServerRequest return `CancellableServerRequest` which is an interface
wrapper around AsyncTask `cancel(boolean)` method.


### Request Headers
You can also use custom request headers with `setHeaders` method.<br/>
**BEWARE** that this method will add map parameter to existing map. If you want to clear the map,
use `clearHeaders` method before setting your custom headers.

You can also specify response charset, which is **UTF-8** by default, but can be overridden to
any of these:

- ASCII
- US-ASCII
- utf-8
- utf-16
- utf-16be
- utf-16le
- cp1250
- cp852
- iso-8859-1
- iso-8859-2


### SSL
For custom SSL configuration, it is possible to retrieve SSL context defined for HTTPS connections.
You can use static method `getSSLContext` for this purpose.
Another SSL configuration might involve modifying the HostnameVerifier, you can easily set your
own via `setHostNameVerifier(HostnameVerifier)` method.


### GZIP
GZIP is supported by default.


### Response Interceptor
Interceptors can be used for i.e. middleware authentication (handling of expired tokens while preserving
the relative context of the original request)

    request.setInterceptor(new ResponseInterceptor() {
            @Override
            public boolean onProcessStatus(int i) {
                // status code 401
                if (i == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    //authorize
                    return true;    // return true to intercept
                }
                return false;       // return false to continue
            }
        });


### Debugging
To enable debug logs, just use this method (typically in Application object when app is starting)

    ServerRequest.enableDebug(BuildConfig.DEBUG);


### Tips
When using with large data-stream, consider optimizing buffer size. The default size is 2kB

    ServerRequest.setBufferSize(4096);



### License
Released under Apache v2.0 License

### Author
Dusan Bartos<br/>
[doodeec@gmail.com](mailto:doodeec@gmail.com)<br/>
[doodeec.com](http://doodeec.com)
