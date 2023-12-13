package it.lucca.provincia.android.timbrature.InstanceManager;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;

import it.lucca.provincia.android.timbrature.Models.Timbratura;
import it.lucca.provincia.android.timbrature.Utility.App;

public class TimbratureManager {
    private final static String TIMBRATURE = "TIMBRATURE";

    private ArrayList<Timbratura> timbrature;
    private static TimbratureManager instance = null;

    public TimbratureManager() {
        timbrature = new ArrayList<Timbratura>();
    }

    public static final TimbratureManager getInstance() {
        if (instance == null) {
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").create();

            instance = new TimbratureManager();

            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.get());
            String causaliPrefs = prefs.getString(TIMBRATURE, "");

            try {
                Type type = new TypeToken<ArrayList<Timbratura>>() {}.getType();

                instance.timbrature = gson.fromJson(causaliPrefs, type);
            } catch (Exception ex) {
                instance = TimbratureManager.newInstance();
            }
        }

        return instance;
    }

    public static final TimbratureManager newInstance () {
        instance = new TimbratureManager();

        save();

        return instance;
    }

    public void addNoSave (String lettore, String orario, String tipo_timbratura, String causale) {

        Timbratura timbratura = new Timbratura(lettore, orario, tipo_timbratura, causale);
        timbrature.add(timbratura);

    }

    public void addNoSave (JSONObject timbraturaJson) throws JSONException {

        Timbratura timbratura = new Timbratura(
                timbraturaJson.getString("lettore"),
                timbraturaJson.getString("orario"),
                timbraturaJson.getString("tipo_timbratura"),
                timbraturaJson.getString("causale"));
        timbrature.add(timbratura);

    }

    public int size() {

        return timbrature.size();
    }

    public Timbratura get(int position) {
        return timbrature.get(position);
    }

    public ArrayList<Timbratura> getList() {
        return timbrature;
    }

    public static final void save () {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").create();

        String json = gson.toJson(instance.timbrature);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.get());
        SharedPreferences.Editor preferencesEditor = prefs.edit();
        preferencesEditor.putString(TIMBRATURE, json );

        preferencesEditor.commit();
    }
}
