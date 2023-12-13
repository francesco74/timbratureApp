package it.lucca.provincia.android.timbrature.AsyncTaskFragment;

import android.content.Context;
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
import static it.lucca.provincia.android.timbrature.Utility.Const.listaTimbrature;

public class ListaTimbratureAsyncTaskFragment extends Fragment {

    String authCode = "";

    public static ListaTimbratureAsyncTaskFragment newInstance(String authCode) {
        ListaTimbratureAsyncTaskFragment f = new ListaTimbratureAsyncTaskFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();

        args.putString(AUTH_CODE, authCode);
        f.setArguments(args);

        return f;

    }

    public interface TaskCallbacks {
        void onListaTimbratureStarted();
        void onListaTimbratureFinished(JSONObject result);
    }

    private TaskCallbacks pCallbacks = null;
    private ListaTimbratureAsyncTask listaTimbratureAsyncTask;

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

        authCode =  getArguments().getString(AUTH_CODE);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);

        // Create and execute the background task.
        listaTimbratureAsyncTask = new ListaTimbratureAsyncTask();
        listaTimbratureAsyncTask.execute();
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

    private class ListaTimbratureAsyncTask extends AsyncTask<Void, String, JSONObject> {
        JSONParser jsonParser = new JSONParser();

        @Override
        protected void onPreExecute() {
            if (pCallbacks != null) {
                pCallbacks.onListaTimbratureStarted();
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
                params.put("auth_code", authCode);

                json = jsonParser.makeHttpRequest(listaTimbrature, "POST", params);

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
                pCallbacks.onListaTimbratureFinished(result);
            }
        }

    }
}
