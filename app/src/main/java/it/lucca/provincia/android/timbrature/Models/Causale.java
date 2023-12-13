package it.lucca.provincia.android.timbrature.Models;

public class Causale {
    private String id;
    private String description;

    public Causale (String id, String description) {
        this.id = id;
        this.description = description;
    }

    public String getId() {
        return id;
    }
    public String getDescription() {
        return description;
    }


}
