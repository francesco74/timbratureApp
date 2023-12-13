package it.lucca.provincia.android.timbrature.AsyncTaskFragment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import it.lucca.provincia.android.timbrature.Utility.JSONParser;

import static it.lucca.provincia.android.timbrature.Utility.Const.AUTH_CODE;
import static it.lucca.provincia.android.timbrature.Utility.Const.LEGGI_ORARIO_PERIODO;
import static it.lucca.provincia.android.timbrature.Utility.Const.getOrario;

public class LeggiOrarioServerAsyncTaskFragment extends Fragment {

    String authCode = "";

    public interface TaskCallbacks {
        void onAggiornaOrario(JSONObject result);
    }

    private TaskCallbacks pCallbacks = null;
    private AsyncLeggiOrarioServer asyncLeggiOrarioServer;
    ScheduledFuture<?> futureLeggoOrario = null;
    ScheduledThreadPoolExecutor scheduledAcquisisciOrario = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1);

    public static LeggiOrarioServerAsyncTaskFragment newInstance(String authCode) {
        LeggiOrarioServerAsyncTaskFragment f = new LeggiOrarioServerAsyncTaskFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();

        args.putString(AUTH_CODE, authCode);
        f.setArguments(args);

        return f;

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof TaskCallbacks){
            pCallbacks = (TaskCallbacks) context;
        }
    }

    Runnable runnableLeggiOrario = new Runnable() {
        @Override
        public void run() {
            asyncLeggiOrarioServer = new AsyncLeggiOrarioServer();
            asyncLeggiOrarioServer.execute();
        }
    };

    /**
     * This method will only be called once when the retained
     * Fragment is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authCode =  getArguments().getString(AUTH_CODE);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);

        futureLeggoOrario = scheduledAcquisisciOrario.scheduleAtFixedRate(runnableLeggiOrario, 0, LEGGI_ORARIO_PERIODO, TimeUnit.MILLISECONDS);
    }

    /**
     * Set the callback to null so we don't accidentally leak the
     * Activity instance.
     */
    @Override
    public void onDetach() {
        super.onDetach();
        pCallbacks = null;

        if (futureLeggoOrario != null) {
            futureLeggoOrario.cancel(true);
        }
    }

    private class AsyncLeggiOrarioServer extends AsyncTask<Void, Void, JSONObject> {
        JSONParser jsonParser = new JSONParser();

       @Override
        protected JSONObject doInBackground(Void... args) {
            JSONObject json = null;

            try {
                HashMap<String, String> params = new HashMap<>();
                params.put("auth_code", authCode);

                json = jsonParser.makeHttpRequest(getOrario, "POST", params);

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
                pCallbacks.onAggiornaOrario(result);
            }
        }

    }
}
