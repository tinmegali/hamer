package com.tinmegali.hamer.util;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.tinmegali.hamer.MessageActivity;
import com.tinmegali.hamer.R;

/**
 * Base Activity with some helper methods and the basic UI.
 * It also initializes a FragmentRetainer and saves the UI states on it
 */
public abstract class BaseActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = BaseActivity.class.getSimpleName();

    // Fragment to retain some data during configuration changes
    public RetainedFragment retainedFragment;

    // Holds the image downloaded by the WorkerThread
    protected ImageView myImage;

    protected TextView feedback, operation;
    protected ProgressBar progressBar;

    // RetainedFragment keys
    protected final String KEY_IMAGE = "image-view";
    protected final String KEY_PROGRESS_STATUS = "progressbar-status";

    protected void initBasicUI(){
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        myImage = (ImageView) findViewById(R.id.myimage);
        feedback = (TextView) findViewById(R.id.feedback);
        operation = (TextView) findViewById(R.id.operation);
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
        String fdbText = (String) retainedFragment.getObj(Integer.toString(feedback.getId()));
        if ( fdbText != null )
            feedback.setText( fdbText );

        Bitmap bitmap = (Bitmap) retainedFragment.getObj(KEY_IMAGE);
        if ( bitmap != null )
            myImage.setImageBitmap( bitmap );

        if ( retainedFragment.getObj(KEY_PROGRESS_STATUS) != null ) {
            int progStatus = (int) retainedFragment.getObj(KEY_PROGRESS_STATUS);
            if (progStatus == View.VISIBLE)
                progressBar.setVisibility(View.VISIBLE);
            else
                progressBar.setVisibility(View.GONE);
        }
    }

    // saves all persistent data on retainedFragment
    protected void saveData(){
        Log.d(TAG, "saveData()");
        retainedFragment.putObj(Integer.toString(feedback.getId()), feedback.getText());
        retainedFragment.putObj(KEY_PROGRESS_STATUS, progressBar.getVisibility());
        if ( myImage.getDrawable() != null ) {
            Bitmap bitmap = ((BitmapDrawable) myImage.getDrawable()).getBitmap();
            retainedFragment.putObj(KEY_IMAGE, bitmap);
        }
    }


}
