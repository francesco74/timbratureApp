package it.lucca.provincia.android.timbrature.InstanceManager;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import it.lucca.provincia.android.timbrature.Models.Causale;
import it.lucca.provincia.android.timbrature.Utility.App;

public class CausaliManager {
    private final static String CAUSALI = "CAUSALI";

    private ArrayList<Causale> causali;
    private static CausaliManager instance = null;

    public CausaliManager() {
        causali = new ArrayList<Causale>();
    }

    public static final CausaliManager getInstance() {
        if (instance == null) {
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").create();

            instance = new CausaliManager();

            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.get());
            String causaliPrefs = prefs.getString(CAUSALI, "");

            try {
                Type type = new TypeToken<ArrayList<Causale>>() {}.getType();

                instance.causali = gson.fromJson(causaliPrefs, type);
            } catch (Exception ex) {
                instance.causali = new ArrayList<Causale>();
            }
        }

        return instance;
    }

    public static final CausaliManager newInstance () {
        instance = new CausaliManager();

        save();

        return instance;
    }

    public void addNoSave (String id, String descrizione) {

        Causale causale= new Causale(id, descrizione);
        causali.add(causale);

    }

    public int size() {

        return causali.size();
    }

    public Causale get(int position) {
        return causali.get(position);
    }

    public ArrayList<Causale> getList() {
        return causali;
    }

    public static final void save () {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").create();

        String json = gson.toJson(instance.causali);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.get());
        SharedPreferences.Editor preferencesEditor = prefs.edit();
        preferencesEditor.putString(CAUSALI, json );

        preferencesEditor.commit();
    }
}
