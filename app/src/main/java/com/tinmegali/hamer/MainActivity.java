package com.tinmegali.hamer;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.tinmegali.hamer.util.RetainedFragment;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, WorkerThread.Callback {

    private final String TAG = MainActivity.class.getSimpleName();
    private RetainedFragment retainedFragment;
    private ImageView myImage;
    private TextView feedback;
    private ProgressBar progressBar;
    private WorkerThread workerThread;
    private Handler uiHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn1 = (Button) findViewById(R.id.btn_1);
        Button btn2 = (Button) findViewById(R.id.btn_2);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        myImage = (ImageView) findViewById(R.id.myimage);
        feedback = (TextView) findViewById(R.id.feedback);

        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);

        uiHandler = new Handler();

        startFragRetainer();
        recoverData();
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
            workerThread.onDestroy();
        } else {
            Log.d(TAG, "onDestroy()");
            workerThread.onDestroy();
            workerThread.quit();
        }

    }

    // starts a FragmentRetainer instance
    // or recover if already exists
    private void startFragRetainer(){
        Log.d(TAG, "startFragRetainer()");
        retainedFragment = (RetainedFragment) getFragmentManager()
                .findFragmentByTag(RetainedFragment.TAG);
        if ( retainedFragment == null ) {
            retainedFragment = new RetainedFragment();
            getFragmentManager().beginTransaction()
                    .add(retainedFragment, RetainedFragment.TAG)
                    .commit();
        }
    }

    // recover all data saved on retainedFragment
    private void recoverData(){
        Log.d(TAG, "recoverData()");
        workerThread = (WorkerThread) retainedFragment.getObj(WorkerThread.TAG);
        if( workerThread != null){
            workerThread.setCallback(this);
            workerThread.setResponseHandler(uiHandler);
        }
        String fdbText = (String) retainedFragment.getObj(Integer.toString(feedback.getId()));
        if ( fdbText != null )
            feedback.setText( fdbText );

        Bitmap bitmap = (Bitmap) retainedFragment.getObj(KEY_IMAGE);
        if ( bitmap != null )
            myImage.setImageBitmap( bitmap );

    }

    // saves all persistent data on retainedFragment
    private void saveData(){
        Log.d(TAG, "saveData()");
        retainedFragment.putObj(WorkerThread.TAG, workerThread);
        retainedFragment.putObj(Integer.toString(feedback.getId()), feedback.getText());
        Bitmap bitmap = ((BitmapDrawable)myImage.getDrawable()).getBitmap();
        retainedFragment.putObj(KEY_IMAGE, bitmap);
    }

    private final String KEY_IMAGE = "image-view";

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_1: {
                postRunnable();
            }

            case R.id.btn_2: {
                sendMessage();
            }
        }
    }

    // initialized WorkerThread
    private void initWorkerThread(){
        Log.d(TAG, "initWorkerThread()");
        if ( workerThread == null ) {
            workerThread = new WorkerThread(uiHandler, this);
            workerThread.start();
            workerThread.prepareHandler();
        }
    }

    // post a Runnable on the WorkingThread
    private void postRunnable() {
        Log.d(TAG, "postRunnable()");

        initWorkerThread();
        workerThread.postRunnable();
    }

    private void sendMessage() {
        Log.d(TAG, "sendMessage()");
    }

    public void showToast(String msg) {
        Log.d(TAG, "showToast("+msg+")");
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showFeedbackText(String msg) {
        Log.d(TAG, "showFeedbackText("+msg+")");
        feedback.setText(msg);
    }

    @Override
    public void loadImage(Bitmap image) {
        Log.d(TAG, "loadImage("+image+")");
        myImage.setImageBitmap(image);
    }

    @Override
    public void showProgress(boolean show) {
        Log.d(TAG, "showProgress("+show+")");
        if ( show )
            progressBar.setVisibility(View.VISIBLE);
        else
            progressBar.setVisibility(View.GONE);
    }
}
