package br.ufc.great.greatroom.service.gcm;

import android.content.Intent;

/**
 * Created by adyson on 18/11/15.
 */
public class InstanceIDListenerService extends com.google.android.gms.iid.InstanceIDListenerService {

    private static final String TAG = "InstanceIDListenerService";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. This call is initiated by the
     * InstanceID provider.
     */
    @Override
    public void onTokenRefresh() {
        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
    }
}
