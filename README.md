# ServerCommunicator
Simple server communicator for RESTful data source
## Version 0.1.3

### Description
Library module for communicating with your REST server

### Usage
Import library in your application. Either as a `.aar` file in your `libs` folder
or as a maven dependency. Library is published to jcenter, so you just have to
include this dependency. **The library is not in maven central repo, so it probably
won't be found with Android Studio dependency manager search**

    dependencies {
        ...
        compile 'com.doodeec.utils:serverCommunicator:0.1.3'
    }

In your code, you can then use it via `ServerRequest` and `ImageServerRequest` classes.

    ServerRequest request = new ServerRequest(BaseServerRequest.RequestType.GET, new JSONRequestListener() {
        @Override
        public void onSuccess(JSONObject object) {
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
    });
    request.executeInParallel(url);

Both ServerRequest and ImageServerRequest return `CancellableServerRequest` which is an interface
wrapper around AsyncTask `cancel(boolean)` method.


You can also use custom request headers with `setHeaders` method.<br/>
**BEWARE** that this method will add map parameter to existing map. If you want to clear the map,
use `clearHeaders` method before setting your custom headers.

### License
Released under Apache v2.0 License

### Author
Dusan Bartos<br/>
[doodeec@gmail.com](mailto:doodeec@gmail.com)<br/>
[doodeec.com](http://doodeec.com)
