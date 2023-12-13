package it.lucca.provincia.android.timbrature.BroadcastReceiver;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;


import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import static it.lucca.provincia.android.timbrature.Utility.Const.GEOFENCE_TRANSITION_ENTER;
import static it.lucca.provincia.android.timbrature.Utility.Const.GEOFENCE_TRANSITION_EXIT;
import static it.lucca.provincia.android.timbrature.Utility.Const.GEOFENCE_TRANSITION_ID;
import static it.lucca.provincia.android.timbrature.Utility.Const.GEOFENCE_TRANSITION_POI;

public class GeofenceTransitionReceiver extends BroadcastReceiver {

    public static final String TAG = GeofenceTransitionReceiver.class.getSimpleName();

    private Context context;

    public GeofenceTransitionReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "onReceive(context, intent)");
        this.context = context;
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if(event != null){
            if(event.hasError()){
                onError(event.getErrorCode());
            } else {
                int transition = event.getGeofenceTransition();
                if(transition == Geofence.GEOFENCE_TRANSITION_ENTER || transition == Geofence.GEOFENCE_TRANSITION_DWELL || transition == Geofence.GEOFENCE_TRANSITION_EXIT){
                    String[] geofenceIds = new String[event.getTriggeringGeofences().size()];
                    for (int index = 0; index < event.getTriggeringGeofences().size(); index++) {
                        geofenceIds[index] = event.getTriggeringGeofences().get(index).getRequestId();
                    }
                    Intent timbratoreIntent = new Intent();
                    timbratoreIntent.putExtra(GEOFENCE_TRANSITION_ID, geofenceIds[0]);
                    timbratoreIntent.putExtra(GEOFENCE_TRANSITION_POI, event.getTriggeringLocation());
                    if (transition == Geofence.GEOFENCE_TRANSITION_ENTER || transition == Geofence.GEOFENCE_TRANSITION_DWELL) {
                        //onEnteredGeofences(geofenceIds);
                        timbratoreIntent.setAction(GEOFENCE_TRANSITION_ENTER);
                    } else {
                        //onExitedGeofences(geofenceIds);
                        timbratoreIntent.setAction(GEOFENCE_TRANSITION_EXIT);
                    }

                    context.sendBroadcast(timbratoreIntent);
                }
            }
        }
    }

    protected void onEnteredGeofences(String[] geofenceIds) {

        for (String fenceId : geofenceIds) {
            Toast.makeText(context, String.format("Rilevato ingresso in %1$s", fenceId), Toast.LENGTH_SHORT).show();
        }
    }

    protected void onExitedGeofences(String[] geofenceIds){
       for (String fenceId : geofenceIds) {
            Toast.makeText(context, String.format("Rilevata uscita da  %1$s", fenceId), Toast.LENGTH_SHORT).show();

        }

    }

    protected void onError(int errorCode){
        Toast.makeText(context, String.format("onError(%1$d)", errorCode), Toast.LENGTH_SHORT).show();
        Log.e(TAG, String.format("onError(%1$d)", errorCode));
    }



}

