package br.ufc.great.greatroom.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import br.ufc.great.greatroom.controller.GroupController;
import br.ufc.great.greatroom.model.GroupModel;
import br.ufc.great.greatroom.util.LogFileHelper;
import br.ufc.great.greatroom.util.ServerApi;

public class BeaconService extends Service implements BeaconConsumer, RangeNotifier, MonitorNotifier {
    private static final String TAG = BeaconService.class.getSimpleName();
    private static final int CHECK_BLUETOOTH_PERIOD = 280; /* seconds */

    private static final int FIND_GROUPS_PERIOD_BACKGROUND = 240; /* seconds */
    private static final int FIND_GROUPS_PERIOD_FOREGROUND = 10; /* seconds */

    private static BeaconService instance;

    private BeaconManager beaconManager;
    private ServerApi serverApi;
    private GroupController groupController;
    private final BeaconBinder binder = new BeaconBinder();
    private Collection<Beacon> beacons;
    private boolean findingGroups;
    private boolean disableBluetoothAfterScan;
    private boolean inBackgroundMode;
    private Date lastScan;
    private Region region;
    private int bindCount;
    private FindGroupsTask findGroupsTask;
    private ScheduledFuture findGroupsFuture;
    private CheckBluetoothTask checkBluetoothTask;
    private ScheduledFuture checkBluetoothFuture;
    private ScheduledThreadPoolExecutor scheduler;

    public BeaconService() {
        instance = this;
    }

    public static BeaconService getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        beacons = new ArrayList<>();
        bindCount = 0;
        findingGroups = false;
        disableBluetoothAfterScan = false;
        inBackgroundMode = false;
        lastScan = new Date();
        region = new Region("GREatRoom", null, null, null);

        groupController = GroupController.getInstance();
        serverApi = new ServerApi(getApplicationContext());

        scheduler = new ScheduledThreadPoolExecutor(4);
        findGroupsTask = new FindGroupsTask();
        checkBluetoothTask = new CheckBluetoothTask();
        findGroupsFuture = null;
        checkBluetoothFuture = null;

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));  // iBeacons
        beaconManager.bind(this);

        setBackgroundMode(false);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        bindCount++;
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        bindCount--;
        if (bindCount <= 0)
            stopSelf();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        try {
            beaconManager.stopMonitoringBeaconsInRegion(region);
            beaconManager.stopRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
        }
        beaconManager.unbind(this);
        scheduler.shutdown();
        super.onDestroy();
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(this);
        beaconManager.setMonitorNotifier(this);
        try {
            beaconManager.startRangingBeaconsInRegion(region);
            beaconManager.startMonitoringBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
        int previousSize = 0;
        int currentSize = 0;
        synchronized (beacons) {
            previousSize = beacons.size();
            if (collection.isEmpty()) {
                beacons.clear();
            } else {
                for (Beacon beacon : collection) {
                    beacons.remove(beacon);
                    beacons.add(beacon);
                }
            }
            currentSize = beacons.size();
        }

        Date now = new Date();
        long dateDiff = TimeUnit.MILLISECONDS.toSeconds(now.getTime() - lastScan.getTime());
        boolean findGroups =
                (currentSize > 0 && previousSize == 0) ||
                        (dateDiff >= CHECK_BLUETOOTH_PERIOD);
//        Log.d(TAG, "didRangeBeaconsInRegion beacons size: " + currentSize + " findGroups: " + findGroups);

        lastScan = now;
        if (findGroups)
            findGroups();
        if (disableBluetoothAfterScan)
            disableBluetooth();
    }

    @Override
    public void didEnterRegion(Region region) {
        LogFileHelper.log(TAG, "didEnterRegion", new Date().toString());
    }

    @Override
    public void didExitRegion(Region region) {
        LogFileHelper.log(TAG, "didExitRegion", new Date().toString());
    }

    @Override
    public void didDetermineStateForRegion(int state, Region region) {
    }

    public synchronized void setBackgroundMode(boolean enable) {
        if (inBackgroundMode == enable && findGroupsFuture != null)
            return;
        Log.d(TAG, "setBackgroundMode " + enable);
        inBackgroundMode = enable;

        int findGroupsTime = FIND_GROUPS_PERIOD_FOREGROUND;
        if (findGroupsFuture != null) {
            findGroupsFuture.cancel(true);
            scheduler.remove(findGroupsTask);
            findGroupsFuture = null;
        }
        if (checkBluetoothFuture != null) {
            checkBluetoothFuture.cancel(true);
            scheduler.remove(checkBluetoothTask);
            checkBluetoothFuture = null;
        }

        if (enable) {
            findGroupsTime = FIND_GROUPS_PERIOD_BACKGROUND;
            checkBluetoothFuture = scheduler.scheduleWithFixedDelay(checkBluetoothTask,
                    CHECK_BLUETOOTH_PERIOD,
                    CHECK_BLUETOOTH_PERIOD, TimeUnit.SECONDS);
        } else {
            disableBluetoothAfterScan = false;
        }

        findGroupsFuture = scheduler.scheduleWithFixedDelay(findGroupsTask,
                findGroupsTime,
                findGroupsTime, TimeUnit.SECONDS);
    }

    private void findGroups() {
        findGroups(false);
    }

    private void findGroups(boolean clearCache) {
        Collection<Beacon> locations = new ArrayList<>();
        synchronized (beacons) {
            locations.addAll(beacons);
            if (clearCache)
                beacons.clear();
        }

        if (locations.size() > 0) {
            findingGroups = true;
            serverApi.findGroupsNearby(locations, new ServerApi.GroupsCallback() {
                @Override
                public void onSuccessResponse(List<GroupModel> list) {
                    findingGroups = false;
                    setGroups(list);
                }
            });
        } else {
            findingGroups = false;
            setGroups(new ArrayList<GroupModel>());
        }
    }

    private void setGroups(List<GroupModel> list) {
        groupController.setGroups(list);
    }

    private void enabledBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
            disableBluetoothAfterScan = true;
        }
        Log.d(TAG, "enabledBluetooth disableBluetoothAfterScan: " + disableBluetoothAfterScan);
    }

    private void disableBluetooth() {
        disableBluetoothAfterScan = false;
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.d(TAG, "disableBluetooth enabled: " + bluetoothAdapter.isEnabled());
        if (bluetoothAdapter.isEnabled()) {
            checkBluetoothFuture.cancel(true);
            scheduler.remove(checkBluetoothTask);
            checkBluetoothFuture = scheduler.scheduleWithFixedDelay(checkBluetoothTask, CHECK_BLUETOOTH_PERIOD, CHECK_BLUETOOTH_PERIOD, TimeUnit.SECONDS);

            bluetoothAdapter.disable();
        }
    }

    private class FindGroupsTask implements Runnable {
        @Override
        public void run() {
            if (!findingGroups) {
                findGroups(true);
            }
        }
    }

    private class CheckBluetoothTask implements Runnable {
        @Override
        public void run() {
            BeaconService.this.enabledBluetooth();
        }
    }

    public class BeaconBinder extends Binder {
        public BeaconService getService() {
            return BeaconService.this;
        }
    }
}
