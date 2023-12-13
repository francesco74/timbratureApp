package it.lucca.provincia.android.timbrature.Models;

public class Timbratura {
    private String lettore;
    private String orario;
    private String tipo_timbratura;
    private String causale;

    public Timbratura(String lettore, String orario, String tipo_timbratura, String causale) {
        this.lettore = lettore;
        this.orario = orario;
        this.tipo_timbratura = tipo_timbratura;
        this.causale = causale;
    }

    public String getOrario() {
        return orario;
    }
    public String getLettore() {
        return lettore;
    }
    public String getTipoTimbratura() {
        return tipo_timbratura;
    }
    public String getCausale() {
        return causale;
    }


}
