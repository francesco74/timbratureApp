package it.lucca.provincia.android.timbrature.InstanceManager;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import it.lucca.provincia.android.timbrature.Models.Privacy;
import it.lucca.provincia.android.timbrature.Utility.App;

public class PrivacyManager {
    private final static String PRIVACY = "PRIVACY";

    private Privacy privacy;
    private static PrivacyManager instance = null;

    public PrivacyManager() {
        privacy = new Privacy();
    }

    public static final PrivacyManager getInstance() {
        if (instance == null) {
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").create();

            instance = new PrivacyManager();
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.get());
            String privacyPrefs = prefs.getString(PRIVACY, "");

            try {
                Type type = new TypeToken<Privacy>() {}.getType();

                instance.privacy = gson.fromJson(privacyPrefs, type);
                if (instance.privacy == null) {
                    instance.privacy = new Privacy();
                }

            } catch (Exception ex) {
                instance = newInstance();
            }
        }

        return instance;
    }

    public static final PrivacyManager newInstance () {
        instance = new PrivacyManager();

        save();

        return instance;
    }


    public boolean isAccepted() {
        return privacy.isAccepted();
    }
    public void setPrivacy(boolean accepted) {
        privacy.setPrivacy(accepted);
        save();
    }

    public static final void save () {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").create();

        String json = gson.toJson(instance.privacy);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.get());
        SharedPreferences.Editor preferencesEditor = prefs.edit();
        preferencesEditor.putString(PRIVACY, json );

        preferencesEditor.commit();
    }
}
