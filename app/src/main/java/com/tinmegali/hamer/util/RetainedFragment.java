package com.tinmegali.hamer.util;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tinmegali on 24/08/16.
 */
public class RetainedFragment extends Fragment {

    public static final String TAG = RetainedFragment.class.getSimpleName();
    private Map<String, Object> objMap = new HashMap<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public void putObj(String key, Object object){
        Log.d(TAG, "putObj("+key+", "+object+")");
        objMap.put(key, object);
    }

    public Object getObj(String key) {
        Log.d(TAG, "getObj("+key+")");
        return objMap.get(key);
    }
}