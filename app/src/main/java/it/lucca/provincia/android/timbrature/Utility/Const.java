package it.lucca.provincia.android.timbrature.Utility;

public class Const {
    public static final int CONNECTION_TIMEOUT = 240000; // 240 secondi
    public static final int READ_TIMEOUT = 60000; // 60 secondi
    public static final int LOITERING = 1000; // 1 secondo

    public static final int IMEI_MAX_RETRY = 3;

    public static final String GEOFENCE_TRANSITION_ACTION = "GEOFENCE_TRANSITION_ACTION";
    public static final String GEOFENCE_TRANSITION_ENTER = "GEOFENCE_TRANSITION_ENTER";
    public static final String GEOFENCE_TRANSITION_EXIT = "GEOFENCE_TRANSITION_EXIT";
    public static final String GEOFENCE_TRANSITION_ID = "GEOFENCE_TRANSITION_ID";
    public static final String GEOFENCE_TRANSITION_POI = "GEOFENCE_TRANSITION_POI";

    public static final String AUTH_CODE = "SMS_CODE";

    public static final String ID_CAUSALE = "ID_CAUSALE";
    public static final String DESCRIZIONE_CAUSALE = "DESCRIZIONE_CAUSALE";
    public static final String LATITUDINE_MARCATURA = "LATITUDINE_MARCATURA";
    public static final String LONGITUDINE_MARCATURA = "LONGITUDINE_MARCATURA";
    public static final String NOME = "NOME";
    public static final String COGNOME = "COGNOME";
    public static final String SETZOOM = "SETZOOM";
    public static final String TIPO_TIMBRATURA = "TIPO_TIMBRATURA";
    public static final String TIMBRATURE_LIST = "TIMBRATURE_LIST";

    public static final String TIMBRATURA_USCITA = "U";
    public static final String TIMBRATURA_ENTRATA = "E";

    public static final long LEGGI_ORARIO_PERIODO = 30000; // ms

    public static final int LOCATION_UPDATE_INTERVAL = 10000; // 10 sec
    public static final int LOCATION_FATEST_INTERVAL = 10000; // 10 sec
    public static final int LOCATION_DISPLACEMENT = 3; // 3 meters

    public static final int REQUEST_CHECK_SETTINGS = 1000;
    public static final int GPS_REQUEST = 1010;

    public final static String getParameters = "https://timbrature.provincia.lucca.it/webservices/get_parameters.php";
    public final static String smsRequest = "https://timbrature.provincia.lucca.it/webservices/sms_request.php";
    public final static String getOrario = "https://timbrature.provincia.lucca.it/webservices/get_orario.php";
    public final static String setTimbratura = "https://timbrature.provincia.lucca.it/webservices/set_timbratura.php";
    public final static String listaTimbrature = "https://timbrature.provincia.lucca.it/webservices/lista_timbrature.php";

    public final static String privacyWebPage = "https://www.iubenda.com/privacy-policy/14017782";

/*
    public final static String getParameters = "http://192.168.201.138/timbrature/webservices/get_parameters.php";
    public final static String smsRequest = "http://192.168.201.138/timbrature/webservices/sms_request.php";
    public final static String getOrario = "http://192.168.201.138/timbrature/webservices/get_orario.php";
    public final static String setTimbratura = "http://192.168.201.138/timbrature/webservices/set_timbratura.php";
    public final static String listaTimbrature = "http://192.168.201.138/timbrature/webservices/lista_timbrature.php";
*/
}
