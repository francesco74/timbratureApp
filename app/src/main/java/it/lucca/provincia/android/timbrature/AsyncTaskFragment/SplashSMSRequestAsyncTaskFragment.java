package it.lucca.provincia.android.timbrature.AsyncTaskFragment;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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
import static it.lucca.provincia.android.timbrature.Utility.Const.smsRequest;

public class SplashSMSRequestAsyncTaskFragment extends Fragment {

    String AuthCode = "";
    public interface TaskCallbacks {
        void onSplashSMSRequestTaskStarted();
        void onSplashSMSRequestTaskFinished(JSONObject result);
    }

    private TaskCallbacks pCallbacks = null;
    private AsyncSMSRequest asyncSMSRequest;

    public static SplashSMSRequestAsyncTaskFragment newInstance(String AuthCode) {
        SplashSMSRequestAsyncTaskFragment f = new SplashSMSRequestAsyncTaskFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();

        args.putString(AUTH_CODE, AuthCode);
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

    /**
     * This method will only be called once when the retained
     * Fragment is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AuthCode =  getArguments().getString(AUTH_CODE);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);

        // Create and execute the background task.
        asyncSMSRequest = new AsyncSMSRequest();
        asyncSMSRequest.execute();
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

    private class AsyncSMSRequest extends AsyncTask<Void, String, JSONObject> {
        JSONParser jsonParser = new JSONParser();

        @Override
        protected void onPreExecute() {
            if (pCallbacks != null) {
                pCallbacks.onSplashSMSRequestTaskStarted();
            }
        }

        @Override
        protected JSONObject doInBackground(Void... args) {
            String androidID = "";
            int version = 0;
            JSONObject json = null;

            try {
                androidID = Settings.Secure.getString(App.get().getContentResolver(), Settings.Secure.ANDROID_ID);

                try {
                    PackageInfo pInfo = App.get().getPackageManager().getPackageInfo(App.get().getPackageName(), 0);
                    version = pInfo.versionCode;
                } catch (PackageManager.NameNotFoundException ex) {
                    version = 0;
                }

                HashMap<String, String> params = new HashMap<>();

                params.put("android_id", androidID);
                params.put("app_version", Integer.toString(version));
                params.put("auth_code", AuthCode);

                json = jsonParser.makeHttpRequest(smsRequest, "POST", params);

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
                pCallbacks.onSplashSMSRequestTaskFinished(result);
            }
        }

    }
}
