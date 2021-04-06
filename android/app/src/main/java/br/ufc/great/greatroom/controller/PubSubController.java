package br.ufc.great.greatroom.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import br.ufc.great.greatroom.R;
import br.ufc.great.greatroom.model.GroupModel;

/**
 * Created by adyson on 30/11/15.
 */
public class PubSubController {
    private static final String TAG = PubSubController.class.getSimpleName();
    private static PubSubController instance = null;
    public static final String EVENT_GROUP_FILES_CHANGED = "/topics/group.#id#.files.changed";
    public static final String EVENT_GROUP_OBJECTS_CHANGED = "/topics/group.#id#.objects.changed";

    private Context context;
    private LocalBroadcastManager localBroadcastManager;
    private Map<String, Integer> subscribeCount;
    private String gcmToken;

    public PubSubController() {
        context = AppController.getInstance().getApplicationContext();
        localBroadcastManager = LocalBroadcastManager.getInstance(context);
        subscribeCount = new HashMap<>();
        gcmToken = null;
    }

    public static PubSubController getInstance() {
        if (null == instance)
            instance = new PubSubController();
        return instance;
    }

    public void subscribeToGroupEvent(final String event, final GroupModel group, final BroadcastReceiver receiver) {
        String topic = this.getGroupTopic(event, group);
        localBroadcastManager.registerReceiver(receiver, new IntentFilter(topic));
        subscribeToGcmTopic(topic);
    }

    public void unsubscribeToGroupEvent(final String event, final GroupModel group, final BroadcastReceiver receiver) {
        String topic = this.getGroupTopic(event, group);
        localBroadcastManager.unregisterReceiver(receiver);
        unsubscribeToGcmTopic(topic);
    }

    public void publishToTopic(final String topic, Bundle data) {
        Intent intent = new Intent(topic);
        intent.putExtras(data);
        localBroadcastManager.sendBroadcast(intent);
    }

    private String getGroupTopic(final String event, final GroupModel group) {
        return event.replaceAll("#id#", "" + group.getId());
    }

    public void setGcmToken(String token) {
        gcmToken = token;
    }

    private String getGcmToken() {
        if (gcmToken == null || gcmToken.isEmpty()) {
            try {
                InstanceID instanceID = InstanceID.getInstance(context);
                gcmToken = instanceID.getToken(context.getString(R.string.gcm_defaultSenderId),
                        GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return gcmToken;
    }

    private void subscribeToGcmTopic(final String topic) {
        SubscribeToGcmTopicTask task = new SubscribeToGcmTopicTask();
        task.execute(topic);
    }

    private synchronized void unsubscribeToGcmTopic(final String topic) {
        UnsubscribeToGcmTopicTask task = new UnsubscribeToGcmTopicTask();
        task.execute(topic);
    }

    private class SubscribeToGcmTopicTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            synchronized (subscribeCount) {
                try {
                    String topic = strings[0];
                    int count = 0;
                    if (subscribeCount.containsKey(topic))
                        count = subscribeCount.get(topic);

                    if (count <= 0) {
                        GcmPubSub pubSub = GcmPubSub.getInstance(context);
                        pubSub.subscribe(getGcmToken(), topic, null);
                    }

                    subscribeCount.put(topic, count + 1);
                } catch (IOException e) {
                }
            }

            return null;
        }
    }

    private class UnsubscribeToGcmTopicTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            synchronized (subscribeCount) {
                try {
                    String topic = strings[0];
                    int count = 0;
                    if (subscribeCount.containsKey(topic))
                        count = subscribeCount.get(topic) - 1;

                    if (count <= 0) {
                        GcmPubSub pubSub = GcmPubSub.getInstance(context);
                        pubSub.unsubscribe(getGcmToken(), topic);
                    }

                    subscribeCount.put(topic, count);
                } catch (IOException e) {

                }
            }

            return null;
        }
    }
}
