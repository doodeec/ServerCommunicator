package com.doodeec.utils.sampleservercommunicator;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.doodeec.utils.network.BaseServerRequest;
import com.doodeec.utils.network.FileServerRequest;
import com.doodeec.utils.network.RequestError;
import com.doodeec.utils.network.RequestType;
import com.doodeec.utils.network.listener.BaseRequestListener;

import java.io.File;

public class MainActivity extends AppCompatActivity implements BaseRequestListener<File> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                download();
            }
        });
    }

    void download() {
        BaseServerRequest.enableDebug(true);
        String url = "http://www.cbu.edu.zm/downloads/pdf-sample.pdf";

        FileServerRequest fsr = new FileServerRequest(getFilesDir(), RequestType.GET, this);
        fsr.execute(url);
    }

    @Override
    public void onError(RequestError error) {
        Log.e(getClass().getSimpleName(), error.getMessage());
        Toast.makeText(this, "Error " + error.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSuccess(File response) {
        Log.i(getClass().getSimpleName(), "loaded " + response.getAbsolutePath());
        Toast.makeText(this, "File loaded", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCancelled() {
        Log.w(getClass().getSimpleName(), "Cancelled");
    }

    @Override
    public void onProgress(Integer progress) {
        Log.w(getClass().getSimpleName(), "Progress " + progress + "%");
    }
}
