package online.danshub.dan.maplarm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
    Some of the code from this class comes from: https://github.com/googlesamples/android-play-location/blob/master/Geofencing/app/src/main/java/com/google/android/gms/location/sample/geofencing/GeofenceErrorMessages.java
 */

public class GeofenceTransitionsIntentService extends IntentService {
    private static final String TAG = "GEOFENCING";
    private MediaPlayer alarmSound = new MediaPlayer();
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public GeofenceTransitionsIntentService(String name) {
        super(name);
    }

    public GeofenceTransitionsIntentService() {
        super("MapLarmGeofenceIntentService");
    }

    private void alarmSound() {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        String music_file = preferences.getString("alarm_file_path", "");

        Log.v(TAG, music_file);

        if (music_file.isEmpty()) {
            alarmSound = MediaPlayer.create(this, R.raw.oldfashionedschoolbelldanielsimon);
            Log.v(TAG, "Default Alarm Noise Selected");
        } else {
            try {
                // TODO Fix this
                Uri uri = Uri.parse(music_file);
                alarmSound.setDataSource(this, uri);
                Log.v(TAG, "Custom Alarm Noise Selected");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        alarmSound.start();
    }

    public MediaPlayer getAlarm() {
        return alarmSound;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
            Log.e(TAG, "Code: " + geofencingEvent.getErrorCode());
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Handle When Entered Geofence
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            String geofenceTransitionDetails = getGeofenceTransitionDetails(
                    geofenceTransition,
                    triggeringGeofences
            );

            Toast.makeText(getApplicationContext(), geofenceTransitionDetails, Toast.LENGTH_LONG).show();
            Log.i(TAG, geofenceTransitionDetails);

            alarmSound();
        }
    }

    /**
     * Gets transition details and returns them as a formatted string.
     *
     * @param geofenceTransition    The ID of the geofence transition.
     * @param triggeringGeofences   The geofence(s) triggered.
     * @return                      The transition details formatted as String.
     */
    private String getGeofenceTransitionDetails(
            int geofenceTransition,
            List<Geofence> triggeringGeofences) {

        String geofenceTransitionString = getTransitionString(geofenceTransition);

        // Get the Ids of each geofence that was triggered.
        ArrayList<String> triggeringGeofencesIdsList = new ArrayList<>();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        String triggeringGeofencesIdsString = TextUtils.join(", ",  triggeringGeofencesIdsList);

        return geofenceTransitionString + ": " + triggeringGeofencesIdsString;
    }

    /**
     * Maps geofence transition types to their human-readable equivalents.
     *
     * @param transitionType    A transition type constant defined in Geofence
     * @return                  A String indicating the type of transition
     */
    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return getString(R.string.geofence_transition_entered);
            default:
                return getString(R.string.unknown_geofence_transition);
        }
    }
}
