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
public class RunnableActivity extends AppCompatActivity
        implements View.OnClickListener, WorkerThread.Callback {

    // Create a Intent to navigate to this Activity
    public static Intent getNavIntent(Context context){
        Intent intent = new Intent(context, RunnableActivity.class);
        return intent;
    }

    private final String TAG = RunnableActivity.class.getSimpleName();

    // Fragment to retain some data during configuration changes
    public RetainedFragment retainedFragment;

    // Holds the image downloaded by the WorkerThread
    protected ImageView myImage;

    protected TextView feedback, operation;
    protected ProgressBar progressBar;

    // BackgroundTread responsible to download the Image
    protected WorkerThread workerThread;

    // Handler that allows communication between
    // the WorkerThread and the Activity
    protected Handler uiHandler;

    // RetainedFragment keys
    protected final String KEY_IMAGE = "image-view";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn1 = (Button) findViewById(R.id.btn_1);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        myImage = (ImageView) findViewById(R.id.myimage);
        feedback = (TextView) findViewById(R.id.feedback);
        operation = (TextView) findViewById(R.id.operation);

        btn1.setOnClickListener(this);

        uiHandler = new Handler();
        startFragRetainer();
        recoverData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.runnables).setEnabled(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch ( item.getItemId() ) {
            case R.id.messages:{
                startActivity(MessageActivity.getNavIntent(this));
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
        // Informs the workerThread about destruction events
        if (isChangingConfigurations()) {
            Log.d(TAG, "onDestroy() - changing configurations...");
            if ( workerThread != null )
                workerThread.onDestroy();
        } else {
            Log.d(TAG, "onDestroy()");
            if ( workerThread != null ) {
                workerThread.onDestroy();
                workerThread.quit();
            }
        }

    }

    // starts a FragmentRetainer instance
    // or recover if already exists
    public void startFragRetainer(){
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
    protected void recoverData(){
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
    protected void saveData(){
        Log.d(TAG, "saveData()");
        retainedFragment.putObj(WorkerThread.TAG, workerThread);
        retainedFragment.putObj(Integer.toString(feedback.getId()), feedback.getText());
        if ( myImage.getDrawable() != null ) {
            Bitmap bitmap = ((BitmapDrawable) myImage.getDrawable()).getBitmap();
            retainedFragment.putObj(KEY_IMAGE, bitmap);
        }
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

    // initialized WorkerThread

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

    // post a Runnable on the WorkingThread

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
