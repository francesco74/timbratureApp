package it.lucca.provincia.android.timbrature.InstanceManager;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import it.lucca.provincia.android.timbrature.Models.Causale;
import it.lucca.provincia.android.timbrature.Models.POI;
import it.lucca.provincia.android.timbrature.Utility.App;

public class POIManager {
    private final static String POI = "POI";

    private ArrayList<POI> poi;
    private static POIManager instance = null;

    public POIManager() {
        poi = new ArrayList<POI>();
    }

    public static final POIManager getInstance() {
        if (instance == null) {
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").create();

            instance = new POIManager();

            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.get());
            String poiPrefs = prefs.getString(POI, "");

            try {
                Type type = new TypeToken<ArrayList<Causale>>() {}.getType();

                instance.poi = gson.fromJson(poiPrefs, type);
            } catch (Exception ex) {
                instance = POIManager.newInstance();
            }
        }

        return instance;
    }

    public static final POIManager newInstance () {
        instance = new POIManager();

        save();

        return instance;
    }

    public void addNoSave (String id, double latitudine, double longitudine, double raggio, String descrizione, double zoom) {

        POI localPoi = new POI(id, latitudine, longitudine, (float)raggio, descrizione, (float)zoom);
        poi.add(localPoi);

    }

    public void addNoSave (String id, double latitudine, double longitudine, double raggio, String descrizione) {

        POI localPoi = new POI(id, latitudine, longitudine, (float)raggio, descrizione);
        poi.add(localPoi);

    }



    public int size() {

        return poi.size();
    }

    public String getDescrizione(String id) {
        String descrizione = "";

        for (POI singlePoi : poi) {
            if (singlePoi.getId().equals(id)) {
                descrizione = singlePoi.getDescrizione();
            }
        }

        return descrizione;
    }

    public POI get(int position) {
        return poi.get(position);
    }


    public static final void save () {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").create();

        String json = gson.toJson(instance.poi);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.get());
        SharedPreferences.Editor preferencesEditor = prefs.edit();
        preferencesEditor.putString(POI, json );

        preferencesEditor.commit();
    }
}
