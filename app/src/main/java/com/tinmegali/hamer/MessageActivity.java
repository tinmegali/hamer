package com.tinmegali.hamer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MessageActivity extends RunnableActivity {

    public static Intent getNavIntent(Context context){
        Intent intent = new Intent(context, MessageActivity.class);
        return intent;
    }

    private final String TAG = MessageActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        myImage = (ImageView) findViewById(R.id.myimage);
        feedback = (TextView) findViewById(R.id.feedback);
        operation = (TextView) findViewById(R.id.operation);

        Button btn = (Button) findViewById(R.id.btn_1);
        Button btn2 = (Button) findViewById(R.id.btn_2);
        Button btn3 = (Button) findViewById(R.id.btn_3);
        btn.setOnClickListener(this);
        btn2.setOnClickListener(this);
        btn3.setOnClickListener(this);

        startFragRetainer();
        recoverData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate( R.menu.menu_main, menu );
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.messages).setEnabled(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch ( item.getItemId() ) {
            case R.id.runnables:{
                startActivity(RunnableActivity.getNavIntent(this));
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isChangingConfigurations()) {
            Log.d(TAG, "onDestroy() - changing configurations...");
            if ( workerThread != null ) {
                workerThread.onDestroy();
            }
        } else {
            Log.d(TAG, "onDestroy()");
            if ( workerThread != null ) {
                workerThread.onDestroy();
                workerThread.quit();
            }
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_1: {
                downloadImage();
                break;
            }
            case R.id.btn_2: {
                downloadRndImage();
                break;
            }
            case R.id.btn_3:{
                startCounter();
                break;
            }
        }
    }

    private void downloadImage() {
        Log.d(TAG, "downloadWithMessage()");
        initWorkerThread();
        workerThread.downloadWithMessage();
    }

    private void downloadRndImage(){
        Log.d(TAG, "downloadRndImage()");
        initWorkerThread();
        workerThread.downloadRandomWithMessage();
    }

    private void startCounter(){
        Log.d(TAG, "startCounter()");
        initWorkerThread();
        workerThread.startTimer(10000, 1000);
    }
}
