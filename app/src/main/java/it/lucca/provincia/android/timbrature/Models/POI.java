package it.lucca.provincia.android.timbrature.Models;

import com.google.android.gms.maps.model.LatLng;

public class POI {
    private String id;
    private double latitudine;
    private double longitudine;
    private String descrizione;
    private float raggio;
    private float zoom;

    public POI(String id, double latitudine, double longitudine, float raggio, String descrizione, float zoom) {
        this.id = id;
        this.latitudine = latitudine;
        this.longitudine = longitudine;
        this.descrizione = descrizione;
        this.raggio = raggio;
        this.zoom = zoom;
    }

    public POI(String id, double latitudine, double longitudine, float raggio, String descrizione) {
        this.id = id;
        this.latitudine = latitudine;
        this.longitudine = longitudine;
        this.descrizione = descrizione;
        this.raggio = raggio;
        this.zoom = 0;
    }

    public double getLatitudine() {
        return latitudine;
    }
    public double getLongitudine() {
        return longitudine;
    }
    public LatLng getLatLng() {
        return new LatLng(latitudine, longitudine);
    }
    public String getDescrizione() {
        return descrizione;
    }
    public float getRaggio() {
        return raggio;
    }

    public String getId () {
        return id;
    }

    public float getZoomLevel () {
        return zoom;
    }


}
