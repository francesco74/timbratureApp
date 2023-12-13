package it.lucca.provincia.android.timbrature.Models;

public class Privacy {
    private boolean accepted;


    public Privacy() {
        this.accepted = false;
    }

    public  boolean isAccepted() {
        return accepted;
    }

    public  void setPrivacy(boolean accepted) {
        this.accepted = accepted;
    }

}
