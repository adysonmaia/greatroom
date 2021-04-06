package br.ufc.great.greatroom.controller;


import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import org.altbeacon.beacon.powersave.BackgroundPowerSaver;

import java.util.Date;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import br.ufc.great.greatroom.model.PersonModel;
import br.ufc.great.greatroom.service.BeaconService;
import br.ufc.great.greatroom.util.LogFileHelper;
import br.ufc.great.greatroom.util.LruBitmapCache;

/**
 * Created by belmondorodrigues on 05/10/2015.
 */
public class AppController extends Application {

    public static final String TAG = AppController.class
            .getSimpleName();

    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private PersonModel user;
    private ServiceConnection beaconServiceConnection;
    private BeaconService beaconService;
    private BackgroundPowerSaver backgroundPowerSaver;
    private ScheduledThreadPoolExecutor scheduler;
    private ScheduledFuture startBackgroundModeFuture;
    private StartBackgroundModeTask startBackgroundModeTask;

    private static AppController mInstance;

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        user = null;
        backgroundPowerSaver = new BackgroundPowerSaver(this);

        scheduler = new ScheduledThreadPoolExecutor(2);
        startBackgroundModeTask = new StartBackgroundModeTask();
        startBackgroundModeFuture = null;

        beaconService = null;
        beaconServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                BeaconService.BeaconBinder binder = (BeaconService.BeaconBinder) iBinder;
                beaconService = binder.getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                beaconService = null;
            }
        };
        Intent playerIntent = new Intent(this, BeaconService.class);
        bindService(playerIntent, beaconServiceConnection, Context.BIND_AUTO_CREATE);

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable e) {
                long startTime = System.currentTimeMillis();
                LogFileHelper.error(TAG, thread.getName(), startTime, e.getMessage());
            }
        });

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks());
        LogFileHelper.log(TAG, "onCreate", new Date().toString());
    }

    @Override
    public void onTerminate() {
        if (beaconService != null)
            unbindService(beaconServiceConnection);
        LogFileHelper.log(TAG, "onTerminate", new Date().toString());
        super.onTerminate();
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public ImageLoader getImageLoader() {
        getRequestQueue();
        if (mImageLoader == null) {
            mImageLoader = new ImageLoader(this.mRequestQueue,
                    new LruBitmapCache());
        }
        return this.mImageLoader;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public PersonModel getUser() {
        return user;
    }

    public void setUser(PersonModel user) {
        this.user = user;
    }

    private void stopBackgroundMode() {
        if (startBackgroundModeFuture != null) {
            startBackgroundModeFuture.cancel(true);
            scheduler.remove(startBackgroundModeTask);
            startBackgroundModeFuture = null;
        }
        if (beaconService != null) {
            beaconService.setBackgroundMode(false);
        }
    }

    private void startBackgroundMode() {
        if (startBackgroundModeFuture != null &&
                !startBackgroundModeFuture.isDone() && !startBackgroundModeFuture.isCancelled()) {
            return;
        }
        if (startBackgroundModeFuture != null) {
            startBackgroundModeFuture.cancel(true);
            scheduler.remove(startBackgroundModeTask);
            startBackgroundModeFuture = null;
        }

        startBackgroundModeFuture = scheduler.schedule(startBackgroundModeTask, 5, TimeUnit.SECONDS);
    }

    private class ActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
        private Integer count;
        private BluetoothAdapter bluetoothAdapter;

        public ActivityLifecycleCallbacks() {
            count = 0;
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }

        @Override
        public void onActivityCreated(Activity activity, Bundle bundle) {

        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {
            if (count < 1)
                LogFileHelper.log(TAG, "onActivityResumed", activity.getClass().getSimpleName(), new Date().toString());

            ++count;
            if (!bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.enable();
            }
            stopBackgroundMode();
        }

        @Override
        public void onActivityPaused(Activity activity) {
            --count;
            if (count < 1) {
                LogFileHelper.log(TAG, "onActivityPaused", activity.getClass().getSimpleName(), new Date().toString());
                startBackgroundMode();
            }
        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    }

    private class StartBackgroundModeTask implements Runnable {
        @Override
        public void run() {
            if (beaconService != null)
                beaconService.setBackgroundMode(true);
        }
    }
}