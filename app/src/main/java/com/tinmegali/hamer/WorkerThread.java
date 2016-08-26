package com.tinmegali.hamer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.net.URL;

/**
 * Created by tinmegali on 24/08/16.
 *
 * All imagesUrls from https://pixabay.com/
 */
public class WorkerThread extends HandlerThread {

    public static final String TAG = WorkerThread.class.getSimpleName();

    private Handler handler;
    private WeakReference<Handler> responseHandler;
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


    // constructor receives a responseHandler (probably and uiHandler)
    // and a callback
    public WorkerThread(Handler responseHandler, Callback callback) {
        super(TAG);
        this.responseHandler = new WeakReference<>(responseHandler);
        this.callback = new WeakReference<>(callback);
    }

    // Hook method called from the uiThread
    // responsible to null ui references
    public void onDestroy(){
        Log.d(TAG, "onDestroy()");
        responseHandler = null;
        callback = null;
    }

    public void setResponseHandler(Handler responseHandler) {
        Log.d(TAG, "setResponseHandler("+responseHandler+")");
        this.responseHandler = new WeakReference<>(responseHandler);
    }

    public void setCallback(Callback callback) {
        Log.d(TAG, "setCallback("+callback+")");
        this.callback = new WeakReference<>(callback);
    }

    // Prepare the handler. It must be called after the Thread has started
    public void prepareHandler() {
        Log.d(TAG, "prepareHandler()");
        handler = new Handler(getLooper());
    }

    // post a Runnable to the current Thread
    public void postRunnable() {
        Log.d(TAG, "postRunnable()");
        showOperationOnUI("Posting Runnable");
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    showProgress();
                    showFeedbackOnUI("Executing operation...");
                    // sleeps for 2 seconds to emulate long running operation
                    TimeUnit.SECONDS.sleep(2);
                    downloadImage(imageAUrl);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                hideProgress();
                showOperationOnUI("Runnable operation ended");
            }
        });
    }


    private final int MSG_DOWNLOAD_IMG = 0;
    private final int MSG_DOWNLOAD_RANDOM_IMG = 1;

    // send a Message to the current Thread
    // and download a single image
    public void downloadWithMessage(){
        Log.d(TAG, "downloadWithMessage()");
        showOperationOnUI("Sending Message...");
        HandlerImgDownload handlerImgDownload = new HandlerImgDownload(getLooper());
        Message message = Message.obtain(handlerImgDownload, MSG_DOWNLOAD_IMG,imageBUrl);
        handlerImgDownload.sendMessage(message);
    }

    // send a Message to the current Thread
    // and download a random image
    public void downloadRandomWithMessage(){
        Log.d(TAG, "downloadRandomWithMessage()");
        showOperationOnUI("Sending Message...");
        HandlerImgDownload handlerImgDownload = new HandlerImgDownload(getLooper());
        Message message = Message.obtain(handlerImgDownload, MSG_DOWNLOAD_RANDOM_IMG, imagesUrls);
        handlerImgDownload.sendMessage(message);
    }

    /**
     * Handler responsible to manage the Download image task
     */
    private class HandlerImgDownload extends Handler {
        private HandlerImgDownload(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            showProgress();
            switch ( msg.what ) {
                case MSG_DOWNLOAD_IMG: {
                    // receives a single url and download it
                    String url = (String) msg.obj;
                    showFeedbackOnUI("Executing operation...");
                    downloadImage(url);
                    break;
                }
                case MSG_DOWNLOAD_RANDOM_IMG: {
                    // receives a String[] with multiple urls
                    // download a image randomly
                    String[] urls = (String[]) msg.obj;
                    Random random = new Random();
                    String url = urls[random.nextInt(urls.length)];
                    downloadImage(url);
                    showFeedbackOnUI("Executing random download");
                }
            }
            hideProgress();
            showOperationOnUI("Message handled");
        }
    }

    // Download an image
    private void downloadImage(String urlStr){
        Log.d(TAG, "downloadImage()");

        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlStr);
            connection = (HttpURLConnection) url.openConnection();

            InputStream in = new BufferedInputStream(connection.getInputStream());
            final Bitmap bitmap = BitmapFactory.decodeStream(in);
            if ( bitmap != null ) {
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

    private CounterThread counterThread;
    public void startTimer(long totalTime, long timeToTick){
        Log.d(TAG, "startTimer("+totalTime+", "+timeToTick+")");
        if ( counterThread == null ) {
            counterThread = new CounterThread(new HandlerCounter());
        } else {
            counterThread.quit();
        }
        counterThread.startCounter(totalTime, timeToTick);
    }

    /**
     * Handler responsible to manage the CountDownTimer.
     */
    private class HandlerCounter extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CounterThread.KEY_MSG_TICK: {
                    long time = (long) msg.obj;
                    showFeedbackOnUI("Time remaining: " + Long.toString(time));
                    showProgress();
                    break;
                }
                case CounterThread.KEY_MSG_DONE: {
                    showFeedbackOnUI("Time is done!");
                    hideProgress();
                    break;
                }
            }
        }
    }

    // sends a feedback to the ui
    // posting a Runnable to the responseHandler
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
    // sends a feedback to the ui
    // posting a Runnable to the responseHandler
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

    // Loads a Bitmap on the callback
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

    private boolean checkResponse(){
        return responseHandler.get() != null;
    }

    public interface Callback {
        void showFeedbackText(String msg);
        void showOperation(String msg);
        void loadImage(Bitmap image);
        void showProgress(boolean show);
    }

}
