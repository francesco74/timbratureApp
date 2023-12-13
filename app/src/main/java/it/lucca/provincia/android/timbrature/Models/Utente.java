package it.lucca.provincia.android.timbrature.Models;

public class Utente {
    private String matricola;
    private String nome, cognome;
    private String authCode;

    public Utente(String matricola, String nome, String cognome, String authCode) {
        this.matricola = matricola;
        this.nome = nome;
        this.cognome = cognome;
        this.authCode = authCode;
    }

    public String getNome() {
        return nome;
    }
    public String getCognome() {
        return cognome;
    }
    public String getMatricola() {
        return matricola;
    }
    public String getAuthCode() {
        return authCode;
    }


}
