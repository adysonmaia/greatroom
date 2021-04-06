package br.ufc.great.greatroom.service.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import br.ufc.great.greatroom.R;
import br.ufc.great.greatroom.controller.PubSubController;

/**
 * Created by adyson on 18/11/15.
 */
public class RegistrationIntentService extends IntentService {
    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    private static final String TAG = "RegIntentService";

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        try {
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            PubSubController.getInstance().setGcmToken(token);
            Log.i(TAG, "GCM Registration Token: " + token);
            sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, true).apply();
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
            sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, false).apply();
        }
    }
}
