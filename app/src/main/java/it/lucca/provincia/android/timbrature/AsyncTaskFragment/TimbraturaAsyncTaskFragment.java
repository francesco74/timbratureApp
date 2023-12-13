package it.lucca.provincia.android.timbrature.AsyncTaskFragment;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;

import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import it.lucca.provincia.android.timbrature.Utility.App;
import it.lucca.provincia.android.timbrature.Utility.JSONParser;

import static it.lucca.provincia.android.timbrature.Utility.Const.AUTH_CODE;
import static it.lucca.provincia.android.timbrature.Utility.Const.GEOFENCE_TRANSITION_ID;
import static it.lucca.provincia.android.timbrature.Utility.Const.GEOFENCE_TRANSITION_POI;
import static it.lucca.provincia.android.timbrature.Utility.Const.ID_CAUSALE;
import static it.lucca.provincia.android.timbrature.Utility.Const.TIPO_TIMBRATURA;
import static it.lucca.provincia.android.timbrature.Utility.Const.setTimbratura;

public class TimbraturaAsyncTaskFragment extends Fragment {

    double latitudine, longitudine;
    String tipoTimbratura, idCausale, fenceId;
    String authCode = "";

    public static TimbraturaAsyncTaskFragment newInstance(String tipoTimbratura, String idCausale, String fenceId, Location fenceLocation, String authCode) {
        TimbraturaAsyncTaskFragment f = new TimbraturaAsyncTaskFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();

        args.putParcelable(GEOFENCE_TRANSITION_POI, fenceLocation);
        args.putString(GEOFENCE_TRANSITION_ID, fenceId);
        args.putString(TIPO_TIMBRATURA, tipoTimbratura);
        args.putString(ID_CAUSALE, idCausale);

        args.putString(AUTH_CODE, authCode);

        f.setArguments(args);

        return f;

    }

    public interface TaskCallbacks {
        void onTimbraturaStarted();
        void onTimbraturaFinished(JSONObject result);
    }

    private TaskCallbacks pCallbacks = null;
    private TimbraturaAsyncTask timbraturaAsyncTask;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof TaskCallbacks){
            pCallbacks = (TaskCallbacks) context;
        }
    }


    /**
     * This method will only be called once when the retained
     * Fragment is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Location fenceLocation = getArguments().getParcelable(GEOFENCE_TRANSITION_POI);
        latitudine = fenceLocation.getLatitude();
        longitudine = fenceLocation.getLongitude();
        fenceId =  getArguments().getString(GEOFENCE_TRANSITION_ID);
        tipoTimbratura = getArguments().getString(TIPO_TIMBRATURA);
        idCausale = getArguments().getString(ID_CAUSALE);

        authCode = getArguments().getString(AUTH_CODE);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);

        // Create and execute the background task.
        timbraturaAsyncTask = new TimbraturaAsyncTask();
        timbraturaAsyncTask.execute();
    }

    /**
     * Set the callback to null so we don't accidentally leak the
     * Activity instance.
     */
    @Override
    public void onDetach() {
        super.onDetach();
        pCallbacks = null;
    }

    private class TimbraturaAsyncTask extends AsyncTask<Void, String, JSONObject> {
        JSONParser jsonParser = new JSONParser();

        @Override
        protected void onPreExecute() {
            if (pCallbacks != null) {
                pCallbacks.onTimbraturaStarted();
            }
        }

        @Override
        protected JSONObject doInBackground(Void... args) {
            String androidID = "";
            JSONObject json = null;

            try {
                androidID = Settings.Secure.getString(App.get().getContentResolver(), Settings.Secure.ANDROID_ID);


                HashMap<String, String> params = new HashMap<>();

                params.put("android_id", androidID);

                params.put("id_causale", idCausale);
                params.put("latitudine", Double.toString(latitudine));
                params.put("longitudine", Double.toString(longitudine));
                params.put("fence_id", fenceId);
                params.put("tipo_timbratura",tipoTimbratura);

                params.put("auth_code",authCode);

                json = jsonParser.makeHttpRequest(setTimbratura, "POST", params);

            } catch (SecurityException ex) {
                try {
                    json = new JSONObject();
                    json.put("success", false);
                    json.put("message", ex.getMessage());
                } catch (JSONException innerEx) {
                    json = null;
                }
            } catch (Exception ex) {
                try {
                    json = new JSONObject();
                    json.put("success", false);
                    json.put("message", ex.getMessage());
                } catch (JSONException innerEx) {
                    json = null;
                }
            }

            return json;

        }

        @Override
        protected void onPostExecute(JSONObject result) {
            if (pCallbacks != null) {
                pCallbacks.onTimbraturaFinished(result);
            }
        }

    }
}
