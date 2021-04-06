package br.ufc.great.greatroom.service.gcm;

import android.os.Bundle;

import br.ufc.great.greatroom.controller.PubSubController;

/**
 * Created by adyson on 18/11/15.
 */
public class GcmListenerService extends com.google.android.gms.gcm.GcmListenerService {
    private static final String TAG = "GcmListenerService";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {
        if (from.startsWith("/topics/")) {
            PubSubController.getInstance().publishToTopic(from, data);
        }
    }
}
