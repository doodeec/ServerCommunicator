package com.doodeec.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.doodeec.utils.network.RequestError;
import com.doodeec.utils.network.RequestType;
import com.doodeec.utils.network.ServerRequest;
import com.doodeec.utils.network.ServerRequest2;
import com.doodeec.utils.network.listener.BaseRequestListener;
import com.google.gson.JsonObject;

public class MainActivity extends AppCompatActivity {

    private static final String URL = "http://ip.jsontest.com/";
    private static final String TAG1 = ServerRequest.class.getSimpleName();
    private static final String TAG2 = ServerRequest2.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ServerRequest.enableDebug(true);
        ServerRequest2.enableDebug(true);

        executeOld();
        executeNew();
    }

    private void executeOld() {
        ServerRequest<JsonObject> request = new ServerRequest<JsonObject>(RequestType.GET, new BaseRequestListener<JsonObject>() {
            @Override
            public void onError(RequestError error) {
                Log.e(TAG1, error.getMessage());
            }

            @Override
            public void onSuccess(JsonObject response) {
                Log.i(TAG1, "SUCCESS");
            }

            @Override
            public void onCancelled() {
                Log.d(TAG1, "CANCEL");
            }

            @Override
            public void onProgress(Integer progress) {
                Log.d(TAG1, progress + " %");
            }
        }, JsonObject.class);
        request.execute(URL);
    }

    private void executeNew() {
        ServerRequest2<JsonObject> request2 = new ServerRequest2<JsonObject>(RequestType.GET, new BaseRequestListener<JsonObject>() {
            @Override
            public void onError(RequestError error) {
                Log.e(TAG2, error.getMessage());
            }

            @Override
            public void onSuccess(JsonObject response) {
                Log.i(TAG2, "SUCCESS");
            }

            @Override
            public void onCancelled() {
                Log.d(TAG2, "CANCEL");
            }

            @Override
            public void onProgress(Integer progress) {
                Log.d(TAG2, progress + " %");
            }
        }, JsonObject.class);
        request2.execute(URL);
    }
}
