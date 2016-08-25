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

public class RunnableActivity extends AppCompatActivity
        implements View.OnClickListener, WorkerThread.Callback {

    public static Intent getNavIntent(Context context){
        Intent intent = new Intent(context, RunnableActivity.class);
        return intent;
    }

    private final String TAG = RunnableActivity.class.getSimpleName();
    public RetainedFragment retainedFragment;
    protected ImageView myImage;

    protected TextView feedback, operation;
    protected ProgressBar progressBar;
    protected WorkerThread workerThread;
    protected Handler uiHandler;

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
                postRunnable();
                break;
            }
        }
    }

    // initialized WorkerThread
    public void initWorkerThread(){
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
    public void showOperation(String msg) {
        Log.d(TAG, "showOperation("+msg+")");
        operation.setText(msg);
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
