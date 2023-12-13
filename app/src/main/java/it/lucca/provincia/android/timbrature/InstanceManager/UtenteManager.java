package it.lucca.provincia.android.timbrature.InstanceManager;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import it.lucca.provincia.android.timbrature.Models.Utente;
import it.lucca.provincia.android.timbrature.Utility.App;

public class UtenteManager {
    private final static String UTENTE = "UTENTE";

    private Utente utente;
    private static UtenteManager instance = null;

    public UtenteManager(String matricola, String nome, String cognome, String authCode) {
        utente = new Utente(matricola, nome, cognome, authCode);
    }

    public static final UtenteManager getInstance() {
        if (instance == null) {
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").create();

            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.get());
            String causaliPrefs = prefs.getString(UTENTE, "");

            try {
                Type type = new TypeToken<Utente>() {}.getType();

                instance.utente = gson.fromJson(causaliPrefs, type);
            } catch (Exception ex) {
                instance = null;
            }
        }

        return instance;
    }

    public static final UtenteManager newInstance (String matricola, String nome, String cognome, String authCode) {
        instance = new UtenteManager(matricola, nome, cognome, authCode);

        save();

        return instance;
    }





    public String getNome() {
        return utente.getNome();
    }
    public String getCognome () {
        return utente.getCognome();
    }
    public String getMatricola() {
        return utente.getMatricola();
    }
    public String getAuthCode() {
        return utente.getAuthCode();
    }


    public static final void save () {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").create();

        String json = gson.toJson(instance.utente);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.get());
        SharedPreferences.Editor preferencesEditor = prefs.edit();
        preferencesEditor.putString(UTENTE, json );

        preferencesEditor.commit();
    }
}
