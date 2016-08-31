package com.tinmegali.hamer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.tinmegali.hamer.util.BaseActivity;

/**
 * Activity that illustrate the use of {@link android.os.Message} objects.
 *
 * It calls methods on the {@link WorkerThread} to download a image,
 * download a random image or start a count down counter.
 *
 * All those tasks are done asynchronously on the {@link #workerThread}
 * using Message objects called  on the thread.
 *
 * The results are sent back to the Activity using the {@link #uiHandler}
 * passed to the {@link WorkerThread} during its constructions or
 * after configuration changes. {@link Message} objects are send
 * to the {@link #uiHandler} and processed by the {@link MessageHandler}
 */
public class MessageActivity extends BaseActivity {

    // Create a Intent to navigate to this Activity
    public static Intent getNavIntent(Context context){
        Intent intent = new Intent(context, MessageActivity.class);
        return intent;
    }

    private final String TAG = MessageActivity.class.getSimpleName();

    // BackgroundTread responsible to download the Image
    protected WorkerThread workerThread;

    // Handler that allows communication between
    // the WorkerThread and the Activity
    protected MessageHandler uiHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        initBasicUI();

        Button btn = (Button) findViewById(R.id.btn_1);
        Button btn2 = (Button) findViewById(R.id.btn_2);
        Button btn3 = (Button) findViewById(R.id.btn_3);
        Button btn4 = (Button) findViewById(R.id.btn_4);
        btn.setOnClickListener(this);
        btn2.setOnClickListener(this);
        btn3.setOnClickListener(this);
        btn4.setOnClickListener(this);

        uiHandler = new MessageHandler();
        startFragRetainer();
        recoverData();
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
            case R.id.btn_4:{
                startMessageDelay();
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
            workerThread = new WorkerThread(uiHandler);
            workerThread.start();
            workerThread.prepareHandler();
        }
    }

    /**
     * Asks the {@link #workerThread} to start
     * downloading a Image.
     */
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

    /**
     * send a Message with a delayed time
     */
    private void startMessageDelay(){
        Log.d(TAG, "startMessageDelay()");
        initWorkerThread();
        workerThread.startMessageDelay();
    }

    // Message identifier used on Message.what() field
    public static final int KEY_MSG_FEEDBACK    = 0;
    public static final int KEY_MSG_FEEDBACK_OP = 1;
    public static final int KEY_MSG_IMAGE       = 2;
    public static final int KEY_MSG_PROGRESS    = 3;
    public static final int KEY_MSG_TOAST       = 4;

    /**
     * Handler responsible to manage communication
     * from the {@link WorkerThread}. It sends Messages
     * back to the {@link MessageActivity} and handle
     * those Messages
     */
    public class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // handle feedback text
                case KEY_MSG_FEEDBACK:{
                    feedback.setText((String) msg.obj);
                    break;
                }
                // handle feedback operation
                case KEY_MSG_FEEDBACK_OP:{
                    operation.setText((String) msg.obj);
                    break;
                }
                // handle image
                case KEY_MSG_IMAGE:{
                    Bitmap bmp = (Bitmap) msg.obj;
                    myImage.setImageBitmap(bmp);
                    break;
                }
                // handle progressBar calls
                case KEY_MSG_PROGRESS: {
                    if ( (boolean) msg.obj )
                        progressBar.setVisibility(View.VISIBLE);
                    else
                        progressBar.setVisibility(View.GONE);
                    break;
                }

                // handle toast sent with a Message delay
                case KEY_MSG_TOAST:{
                    String msgText = (String)msg.obj;
                    Toast.makeText(getApplicationContext(), msgText, Toast.LENGTH_LONG ).show();
                    break;
                }
            }
        }
    }
}
