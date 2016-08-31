package com.tinmegali.hamer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.*;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.util.Calendar;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.net.URL;

/**
 * A background Thread that download images
 * and also communicates with the {@link CounterThread}
 *
 * To illustrate how post/send {@link Runnable}/{@link Message} works
 * we will use multiple Handlers, each one addressing some kind of resource.
 *
 * {@link #postHandler}     responsible only to post Runnable to the WorkerThread
 *
 * {@link #responseHandler} Handler received from the {@link MessageActivity} and {@link RunnableActivity}
 *                          responsible to post/send Runnable/Message on the UI.
 *
 * {@link #handlerMsgImgDownloader}  send and processes download Messages on the WorkerThread
 *
 * {@link #handlerCounter}  receive Messages from {@link CounterThread} with
 *                          'tick' and 'done' information
 *
 * # All imagesUrls taken from https://pixabay.com/
 */
public class WorkerThread extends HandlerThread {

    public static final String TAG = WorkerThread.class.getSimpleName();

    // This Handler will be responsible only
    // to post Runnable on this Thread
    private Handler postHandler;

    // Handler received from the MessageActivity and RunnableActivity
    // responsible to receive Runnable calls that will be processed
    // on the UI. The callback will help on this process.
    private WeakReference<Handler> responseHandler;

    // send and processes download Messages on the WorkerThread
    private HandlerMsgImgDownloader handlerMsgImgDownloader;

    // receive Messages from CounterThread with
    // 'tick' and 'done' information
    private HandlerCounter handlerCounter;

    // Callback from the UI
    // it is a WeakReference because it can be unvalidated
    // during "configuration changes" and other events
    private WeakReference<Callback> callback;

    private final String imageAUrl =
            "https://pixabay.com/static/uploads/photo/2016/08/05/18/28/mobile-phone-1572901_960_720.jpg";
    private final String imageBUrl =
            "https://pixabay.com/static/uploads/photo/2015/01/20/13/13/ipad-605439_960_720.jpg";

    private final String[] imagesUrls = new String[]{
            "https://pixabay.com/static/uploads/photo/2015/08/07/00/41/lg-878843_960_720.jpg",
            "https://pixabay.com/static/uploads/photo/2015/11/28/21/47/iphone-1067983_960_720.jpg",
            "https://pixabay.com/static/uploads/photo/2014/09/26/22/53/tablet-462950_960_720.png",
            "https://pixabay.com/static/uploads/photo/2015/08/07/00/41/lg-878845_960_720.jpg"
    };

    // Thread responsible to create a simple counter
    // It illustrates how a Message can be passed
    // between background Threads
    private CounterThread counterThread;


    /**
     * The constructor receives a Handler e a Callback from the UI
     * @param responseHandler   in charge to post Runnable to the UI
     * @param callback          works together with the responseHandler
     *                          allowing calls directly on the UI
     */
    public WorkerThread(Handler responseHandler, Callback callback) {
        super(TAG);
        this.responseHandler = new WeakReference<>(responseHandler);
        this.callback = new WeakReference<>(callback);
    }

    /**
     * Constructor used on the {@link MessageActivity}
     * @param responseHandler   sent by the Activity. responsible to
     *                          send Messages to the UI
     */
    public WorkerThread(Handler responseHandler){
        super(TAG);
        this.responseHandler = new WeakReference<>(responseHandler);
    }

    /**
     * Hook method called from the uiThread
     * responsible to null ui references
     */
    public void onDestroy(){
        Log.d(TAG, "onDestroy()");
        responseHandler = null;
        callback = null;
    }

    /**
     * reestablish a Handler with the UI after a destruct event
     * @param responseHandler   Handler received from the UI
     */
    public void setResponseHandler(Handler responseHandler) {
        Log.d(TAG, "setResponseHandler("+responseHandler+")");
        this.responseHandler = new WeakReference<>(responseHandler);
    }

    /**
     * reestablish a Callback with the UI after a destruct event
     * @param callback  received from the UI.
     */
    public void setCallback(Callback callback) {
        Log.d(TAG, "setCallback("+callback+")");
        this.callback = new WeakReference<>(callback);
    }

    /**
     * Prepare the postHandler.
     * It must be called after the Thread has started
     */
    public void prepareHandler() {
        Log.d(TAG, "prepareHandler()");
        postHandler = new Handler(getLooper());
    }

    /**
     * post a Runnable to the WorkerThread
     * Download a bitmap and sends the image
     * to the UI {@link RunnableActivity}
     * using the {@link #responseHandler} with
     * help from the {@link #callback}
     */
    public void downloadWithRunnable() {
        Log.d(TAG, "downloadWithRunnable()");
        showOperationOnUI("Downloading image with Runnable");

        // post Runnable to WorkerThread
        postHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    // showing progress on the UI
                    showProgress();
                    // showing feedback text on the UI
                    showFeedbackOnUI("Executing operation...");
                    // sleeps for 2 seconds to emulate long running operation
                    TimeUnit.SECONDS.sleep(2);
                    // Download image and sends to UI
                    downloadImage(imageAUrl);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // Hide progressBar and sends feedback to UI
                hideProgress();
                showOperationOnUI("Runnable operation ended");
            }
        });
    }

    /**
     * show a Toast on the UI.
     * schedules the task considering the current time.
     * It could be scheduled at any time, we're
     * using 5 seconds to facilitates the debugging
     */
    public void toastAtTime(){
        Log.d(TAG, "toastAtTime(): current - " + Calendar.getInstance().toString());

        // seconds to add on current time
        int delaySeconds = 5;

        // testing using a real date
        Calendar scheduledDate = Calendar.getInstance();
        // setting a future date considering the delay in seconds define
        // we're using this approach just to facilitate the testing.
        // it could be done using a user defined date also
        scheduledDate.set(
                scheduledDate.get(Calendar.YEAR),
                scheduledDate.get(Calendar.MONTH),
                scheduledDate.get(Calendar.DAY_OF_MONTH),
                scheduledDate.get(Calendar.HOUR_OF_DAY),
                scheduledDate.get(Calendar.MINUTE),
                scheduledDate.get(Calendar.SECOND) + delaySeconds
        );
        Log.d(TAG, "toastAtTime(): scheduling at - " + scheduledDate.toString());
        long scheduled = calculateUptimeMillis(scheduledDate);

        // posting Runnable at specific time
        postHandler.postAtTime(
                new Runnable() {
            @Override
            public void run() {
                if ( callback != null ) {
                    callback.get().showToast(
                            "Toast called using 'postAtTime()'."
                    );
                }
            }
        }, scheduled);
    }

    /**
     * Calculates the {@link SystemClock#uptimeMillis()} to
     * a given Calendar date.
     */
    private long calculateUptimeMillis(Calendar calendar){
        long time = calendar.getTimeInMillis();
        long currentTime = Calendar.getInstance().getTimeInMillis();
        long diff = time - currentTime;
        return SystemClock.uptimeMillis() + diff;
    }


    /**
     * Keys to identify the keys of {@link Message#what}
     * from Messages send by the {@link #handlerMsgImgDownloader}
     */
    private final int MSG_DOWNLOAD_IMG = 0;         // msg that download a single img
    private final int MSG_DOWNLOAD_RANDOM_IMG = 1;  // msg that download random img

    /**
     * sends a Message to the current Thread
     * using the {@link #handlerMsgImgDownloader}
     * to download a single image.
     */
    public void downloadWithMessage(){
        Log.d(TAG, "downloadWithMessage()");
        showOperationOnUIMSG("Sending Message...");
        if ( handlerMsgImgDownloader == null )
            handlerMsgImgDownloader = new HandlerMsgImgDownloader(getLooper());
        Message message = Message.obtain(handlerMsgImgDownloader, MSG_DOWNLOAD_IMG,imageBUrl);
        handlerMsgImgDownloader.sendMessage(message);
    }

    /**
     * sends a Message to the current Thread
     * using the {@link #handlerMsgImgDownloader}
     * to download a random image.
     */
    public void downloadRandomWithMessage(){
        Log.d(TAG, "downloadRandomWithMessage()");
        showOperationOnUIMSG("Sending Message...");
        if ( handlerMsgImgDownloader == null )
            handlerMsgImgDownloader = new HandlerMsgImgDownloader(getLooper());
        Message message = Message.obtain(handlerMsgImgDownloader, MSG_DOWNLOAD_RANDOM_IMG, imagesUrls);
        handlerMsgImgDownloader.sendMessage(message);
    }

    /**
     * Handler responsible to manage the Download image.
     * It send and handle Messages identifying then using
     * the {@link Message#what}
     *      {@link #MSG_DOWNLOAD_IMG} : single image
     *      {@link #MSG_DOWNLOAD_RANDOM_IMG} : random image
     */
    private class HandlerMsgImgDownloader extends Handler {
        private HandlerMsgImgDownloader(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            showProgressMSG(true);
            switch ( msg.what ) {
                case MSG_DOWNLOAD_IMG: {
                    // receives a single url and download it
                    String url = (String) msg.obj;
                    showFeedbackOnUIMSG("Executing operation...");
                    downloadImageMSG(url);
                    break;
                }
                case MSG_DOWNLOAD_RANDOM_IMG: {
                    // receives a String[] with multiple urls
                    // download a image randomly
                    String[] urls = (String[]) msg.obj;
                    Random random = new Random();
                    String url = urls[random.nextInt(urls.length)];
                    downloadImageMSG(url);
                    showFeedbackOnUIMSG("Executing random download");
                }
            }
            showProgressMSG(false);
            showOperationOnUIMSG("Message handled");
        }
    }

    /**
     * Download a bitmap using its url and
     * send to the UI the image downloaded
     */
    private void downloadImage(String urlStr){
        Log.d(TAG, "downloadImage()");

        // Create a connection
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlStr);
            connection = (HttpURLConnection) url.openConnection();

            // get the stream from the url
            InputStream in = new BufferedInputStream(connection.getInputStream());
            final Bitmap bitmap = BitmapFactory.decodeStream(in);
            if ( bitmap != null ) {
                // send the bitmap downloaded and a feedback to the UI
                loadImageOnUI( bitmap );
                showFeedbackOnUI("Image downloaded");
            } else {
                showFeedbackOnUI("Error downloading image");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if ( connection != null )
                connection.disconnect();
        }
    }

    /**
     * Download a bitmap using its url and
     * send to the UI the image downloaded.
     * The only difference with {@link #downloadImage(String)}
     * is that it sends back the image to the UI
     * using a Message
     */
    private void downloadImageMSG(String urlStr){
        Log.d(TAG, "downloadImage()");

        // Create a connection
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlStr);
            connection = (HttpURLConnection) url.openConnection();

            // get the stream from the url
            InputStream in = new BufferedInputStream(connection.getInputStream());
            final Bitmap bitmap = BitmapFactory.decodeStream(in);
            if ( bitmap != null ) {
                // send the bitmap downloaded and a feedback to the UI
                loadImageOnUIMSG( bitmap );
                showFeedbackOnUIMSG("Image downloaded");
            } else {
                showFeedbackOnUIMSG("Error downloading image");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if ( connection != null )
                connection.disconnect();
        }
    }

    /**
     * Show a Toast after a delayed time.
     *
     * send a Message with delayed time on the WorkerThread
     * and sends a new Message to {@link MessageActivity}
     * with a text after the message is processed
     */
    public void startMessageDelay(){
        Log.d(TAG, "startMessageDelay()");

        // message delay
        long delay = 5000;
        String msgText = "Hello from WorkerThread!";

        // Handler responsible to send Message to WorkerThread
        // using Handler.Callback() to avoid the need to extend the Handler class
        Handler handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                responseHandler.get().sendMessage(
                        responseHandler.get().obtainMessage(MessageActivity.KEY_MSG_TOAST, msg.obj)
                );
                return true;
            }
        });

        // sending message
        handler.sendMessageDelayed(
                handler.obtainMessage(0,msgText),
                delay
        );

    }

    /**
     * Start a CountDownTimer on another background Thread.
     *
     * It shows how it is possible to communicate between
     * background Threads using Handlers.
     */
    public void startTimer(long totalTime, long timeToTick){
        Log.d(TAG, "startTimer("+totalTime+", "+timeToTick+")");
        if ( counterThread == null ) {

            // Creates a Handler responsible to manage
            // messages received from CounterThread
            if ( handlerCounter == null )
                handlerCounter = new HandlerCounter();

            // The new Thread receives a Handler associated with
            // the WorkerTread as parameter.
            counterThread = new CounterThread( handlerCounter );
        } else {
            counterThread.quit();
        }
        counterThread.startCounter(totalTime, timeToTick);
    }

    /**
     * Handler responsible to manage the CountDownTimer.
     *
     * It will handle the Messages received from the
     * {@link CounterThread} with the CountDownTimer info.
     */
    private class HandlerCounter extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // Ticking message
                case CounterThread.KEY_MSG_TICK: {
                    long time = (long) msg.obj;
                    showFeedbackOnUIMSG("Time remaining: " + Long.toString(time));
                    showProgressMSG(true);
                    break;
                }
                // Done message
                case CounterThread.KEY_MSG_DONE: {
                    showFeedbackOnUIMSG("Timer is done!");
                    showProgressMSG(false);
                    break;
                }
            }
        }
    }

    /**
     * sends a feedback to the ui
     * posting a Runnable to the {@link #responseHandler}
     * and using the {@link Callback}
     */
    private void showFeedbackOnUI(final String msg) {
        Log.d(TAG, "showFeedbackOnUI(" + msg + ")");
        if ( checkResponse() ) {
            responseHandler.get().post(
                    new Runnable() {
                        @Override
                        public void run() {
                            callback.get().showFeedbackText(msg);
                        }
                    }
            );
        } else {
            Log.w(TAG, "responseHandler unavailable");
        }
    }

    /**
     * sends a feedback to the ui
     * posting a Runnable to the {@link #responseHandler}
     * and using the {@link Callback}
     */
    private void showOperationOnUI(final String msg) {
        Log.d(TAG, "showOperationOnUI(" + msg + ")");
        if ( checkResponse() ) {
            responseHandler.get().post(
                    new Runnable() {
                        @Override
                        public void run() {
                            callback.get().showOperation(msg);
                        }
                    }
            );
        } else {
            Log.w(TAG, "responseHandler unavailable");
        }
    }

    /**
     * sends a Bitmap to the ui
     * posting a Runnable to the {@link #responseHandler}
     * and using the {@link Callback}
     */
    private void loadImageOnUI(final Bitmap image){
        Log.d(TAG, "loadImageOnUI("+image+")");
        if (checkResponse() ) {
            responseHandler.get().post(
                    new Runnable() {
                        @Override
                        public void run() {
                            callback.get().loadImage(image);
                        }
                    }
            );
        }
    }
    /**
     * Show progressBar on the UI.
     * It uses the {@link #responseHandler} to
     * post a Runnable on the UI, using
     * the {@link Callback#showProgress()} to access
     * the progressBar directly
     */
    private void showProgress(){
        Log.d(TAG, "showProgress()");
        if ( checkResponse() ) {
            responseHandler.get().post(
                    new Runnable() {
                        @Override
                        public void run() {
                            callback.get().showProgress(true);
                        }
                    }
            );
        }
    }

    // Hide progressBar on the UI.
    // uses same logic as showProgress()
    private void hideProgress(){
        Log.d(TAG, "hideProgress()");
        if ( checkResponse() ) {
            responseHandler.get().post(
                    new Runnable() {
                        @Override
                        public void run() {
                            callback.get().showProgress(false);
                        }
                    }
            );
        }
    }



    /**
     * Sends Message to the UI using
     * the {@link #responseHandler}
     */
    private void sendMsgToUI(Message msg){
        Log.d(TAG, "sendMsgToUI("+msg+")");
        if (checkResponse()){
            responseHandler.get().sendMessage(msg);
        }
    }

    /**
     * sends a feedback to the ui
     * sending a Message
     */
    private void showFeedbackOnUIMSG(final String msg) {
        Log.d(TAG, "showFeedbackOnUI(" + msg + ")");
        if ( checkResponse() ) {
            sendMsgToUI(
                    responseHandler.get().obtainMessage(MessageActivity.KEY_MSG_FEEDBACK, msg)
            );
        } else {
            Log.w(TAG, "responseHandler unavailable");
        }
    }

    /**
     * sends a feedback to the ui
     * sending a Message to the {@link #responseHandler}
     */
    private void showOperationOnUIMSG(final String msg) {
        Log.d(TAG, "showOperationOnUIMSG(" + msg + ")");
        if ( checkResponse() ) {
            sendMsgToUI(
                    responseHandler.get().obtainMessage(
                            MessageActivity.KEY_MSG_FEEDBACK_OP, msg
                    )
            );
        } else {
            Log.w(TAG, "responseHandler unavailable");
        }
    }

    /**
     * sends a Bitmap to the ui
     * sending a Message to the {@link #responseHandler}
     */
    private void loadImageOnUIMSG(final Bitmap image){
        Log.d(TAG, "loadImageOnUI("+image+")");
        if (checkResponse() ) {
            sendMsgToUI(
                    responseHandler.get().obtainMessage(MessageActivity.KEY_MSG_IMAGE, image)
            );
        }
    }

    /**
     * Show/Hide progressBar on the UI.
     * It uses the {@link #responseHandler} to
     * send a Message on the UI
     */
    private void showProgressMSG(boolean show){
        Log.d(TAG, "showProgressMSG()");
        if ( checkResponse() ) {
            sendMsgToUI(
                    responseHandler.get().obtainMessage(MessageActivity.KEY_MSG_PROGRESS, show)
            );
        }
    }

    // verify if responseHandler is available
    // if not the Activity is passing by some destruction event
    private boolean checkResponse(){
        return responseHandler.get() != null;
    }

    /**
     * Interface to facilitate calls on the UI
     */
    public interface Callback {
        void showFeedbackText(String msg);
        void showOperation(String msg);
        void loadImage(Bitmap image);
        void showProgress(boolean show);
        void showToast(String msg);
    }

}
