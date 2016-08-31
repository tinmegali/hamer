package com.tinmegali.hamer;

import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

/**
 * A HandlerThread with a simple CountDownTimer
 * It helps to understand how Handler can be used to create communication
 * between different background Threads
 *
 * Receiver a Handler associated with the {@link WorkerThread} and
 * send back the CountDown results via Message objects
 *
 */
public class CounterThread extends HandlerThread {

    private static final String TAG = CounterThread.class.getSimpleName();

    private Handler responseHandler;
    private boolean isTicking;

    /**
     * Receives a response Handler to deliver the results
     * of the CountDownTimer to the {@link WorkerThread}
     */
    public CounterThread(final Handler responseHandler) {
        super(TAG);
        this.responseHandler = responseHandler;
    }

    // Simple timer to show how it is possible to communicate between Threads
    public void startCounter(long totalTime, long tickTime){
        Log.d(TAG, "startCounter("+totalTime+","+tickTime+")");
        if ( isTicking )
            return;

        new CountDownTimer(totalTime, tickTime) {
            @Override
            public void onTick(long millisUntilFinished) {
                sendTick(millisUntilFinished / 1000);
                isTicking = true;
            }

            @Override
            public void onFinish() {
                sendTimerDone();
                isTicking = false;
            }
        }.start();
    }

    // Sends the tick information to the WorkerThread via Handler
    private void sendTick(long timeToFinish){
        Log.d(TAG, "sendTick("+timeToFinish+")");
        responseHandler.sendMessage( getTickMsg(timeToFinish ));
    }

    // Message keys to allow the response Handler
    // to correctly identify the messages
    public static final int KEY_MSG_TICK    = 0;
    public static final int KEY_MSG_DONE    = 1;


    private void sendTimerDone(){
        Log.d(TAG, "sendTimerDone()");
        responseHandler.sendMessage(getDoneMsg());

        Thread thread =  new Thread(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    /**
     * Creates a message to be send to the WorkerThread
     * Notice that the responseHandler is offered during the
     * Message creation.
     */
    private Message getTickMsg(long timeToFinish){
        Log.d(TAG, "getTickMsg("+timeToFinish+")");
        return Message.obtain(responseHandler,KEY_MSG_TICK,timeToFinish);
    }

    private Message getDoneMsg(){
        Log.d(TAG, "getDoneMsg()");
        return Message.obtain(responseHandler, KEY_MSG_DONE);
    }
}
