package com.tinmegali.hamer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.tinmegali.hamer.util.BaseActivity;
import com.tinmegali.hamer.util.RetainedFragment;

/**
 * Activity that illustrates the use of posting {@link Runnable}
 *
 * It creates a background Thread using {@link WorkerThread}
 * via {@link Handler}.
 *
 * calls a event {@link #downloadImgWithRunnable()}
 * who post a Runnable to the {@link #workerThread} using
 * and send back the image download to the Activity using
 * the {@link #uiHandler} give to the {@link #workerThread}
 *
 * The Activity implement {@link com.tinmegali.hamer.WorkerThread.Callback}
 * a callback that gives UI methods to the UI Thread.
 * Although those methods can only be accessed by posting
 * a {@link Runnable} using the {@link Handler} sent to the bg thread
 * by this Activity.
 */
public class RunnableActivity extends BaseActivity
        implements View.OnClickListener, WorkerThread.Callback {

    // Create a Intent to navigate to this Activity
    public static Intent getNavIntent(Context context){
        Intent intent = new Intent(context, RunnableActivity.class);
        return intent;
    }

    private final String TAG = RunnableActivity.class.getSimpleName();

    // BackgroundTread responsible to download the Image
    protected WorkerThread workerThread;

    // Handler that allows communication between
    // the WorkerThread and the Activity
    protected Handler uiHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initBasicUI();
        Button btn1 = (Button) findViewById(R.id.btn_1);
        btn1.setOnClickListener(this);

        uiHandler = new Handler();
        startFragRetainer();
        recoverData();
    }

    // recover all data saved on retainedFragment
    @Override
    protected void recoverData(){
        super.recoverData();
        workerThread = (WorkerThread) retainedFragment.getObj(WorkerThread.TAG);
        if( workerThread != null){
            workerThread.setCallback(this);
            workerThread.setResponseHandler(uiHandler);
        }
    }

    // saves all persistent data on retainedFragment
    @Override
    protected void saveData(){
        Log.d(TAG, "saveData()");
        super.saveData();
        retainedFragment.putObj(WorkerThread.TAG, workerThread);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_1: {
                downloadImgWithRunnable();
                break;
            }
        }
    }

    /**
     * Initialize the {@link WorkerThread} instance
     * only if hasn't been initialized yet.
     */
    public void initWorkerThread(){
        Log.d(TAG, "initWorkerThread()");
        if ( workerThread == null ) {
            workerThread = new WorkerThread(uiHandler, this);
            workerThread.start();
            workerThread.prepareHandler();
        }
    }

    /**
     * Starts the downloading process
     * on the {@link WorkerThread#downloadWithRunnable()}
     */
    private void downloadImgWithRunnable() {
        Log.d(TAG, "downloadWithRunnable()");

        initWorkerThread();
        workerThread.downloadWithRunnable();
    }

    /**
     * Callback from {@link WorkerThread}
     * Shows a feedback text on {@link #feedback}
     */
    @Override
    public void showFeedbackText(String msg) {
        Log.d(TAG, "showFeedbackText("+msg+")");
        feedback.setText(msg);
    }

    /**
     * Callback from {@link WorkerThread}
     * Shows a feedback text on {@link #operation}
     */
    @Override
    public void showOperation(String msg) {
        Log.d(TAG, "showOperation("+msg+")");
        operation.setText(msg);
    }

    /**
     * Callback from {@link WorkerThread}
     * Shows a image on the {@link #myImage}
     */
    @Override
    public void loadImage(Bitmap image) {
        Log.d(TAG, "loadImage("+image+")");
        myImage.setImageBitmap(image);
    }

    /**
     * Callback from {@link WorkerThread}
     * Show/Hide {@link #progressBar}
     */
    @Override
    public void showProgress(boolean show) {
        Log.d(TAG, "showProgress("+show+")");
        if ( show )
            progressBar.setVisibility(View.VISIBLE);
        else
            progressBar.setVisibility(View.GONE);
    }
}
