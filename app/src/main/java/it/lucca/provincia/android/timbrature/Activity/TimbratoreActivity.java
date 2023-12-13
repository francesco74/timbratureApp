package it.lucca.provincia.android.timbrature.Activity;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import it.lucca.provincia.android.timbrature.ArrayAdapters.CausaliArrayAdapter;
import it.lucca.provincia.android.timbrature.AsyncTaskFragment.LeggiOrarioServerAsyncTaskFragment;
import it.lucca.provincia.android.timbrature.AsyncTaskFragment.ListaTimbratureAsyncTaskFragment;
import it.lucca.provincia.android.timbrature.AsyncTaskFragment.TimbraturaAsyncTaskFragment;
import it.lucca.provincia.android.timbrature.BroadcastReceiver.GeofenceTransitionReceiver;
import it.lucca.provincia.android.timbrature.CustomDialogs.PrivacyDialogFragment;
import it.lucca.provincia.android.timbrature.InstanceManager.CausaliManager;
import it.lucca.provincia.android.timbrature.InstanceManager.POIManager;
import it.lucca.provincia.android.timbrature.InstanceManager.TimbratureManager;
import it.lucca.provincia.android.timbrature.InstanceManager.UtenteManager;
import it.lucca.provincia.android.timbrature.Models.Causale;
import it.lucca.provincia.android.timbrature.Models.POI;
import it.lucca.provincia.android.timbrature.R;
import it.lucca.provincia.android.timbrature.Utility.GpsUtils;

import static it.lucca.provincia.android.timbrature.Utility.Const.COGNOME;
import static it.lucca.provincia.android.timbrature.Utility.Const.DESCRIZIONE_CAUSALE;
import static it.lucca.provincia.android.timbrature.Utility.Const.GEOFENCE_TRANSITION_ACTION;
import static it.lucca.provincia.android.timbrature.Utility.Const.GEOFENCE_TRANSITION_ENTER;
import static it.lucca.provincia.android.timbrature.Utility.Const.GEOFENCE_TRANSITION_EXIT;
import static it.lucca.provincia.android.timbrature.Utility.Const.GEOFENCE_TRANSITION_ID;
import static it.lucca.provincia.android.timbrature.Utility.Const.GEOFENCE_TRANSITION_POI;
import static it.lucca.provincia.android.timbrature.Utility.Const.ID_CAUSALE;
import static it.lucca.provincia.android.timbrature.Utility.Const.LOCATION_DISPLACEMENT;
import static it.lucca.provincia.android.timbrature.Utility.Const.LOCATION_FATEST_INTERVAL;
import static it.lucca.provincia.android.timbrature.Utility.Const.LOCATION_UPDATE_INTERVAL;
import static it.lucca.provincia.android.timbrature.Utility.Const.NOME;
import static it.lucca.provincia.android.timbrature.Utility.Const.SETZOOM;
import static it.lucca.provincia.android.timbrature.Utility.Const.TIMBRATURA_ENTRATA;
import static it.lucca.provincia.android.timbrature.Utility.Const.TIMBRATURA_USCITA;
import static it.lucca.provincia.android.timbrature.Utility.Utility.showAlert;
import static it.lucca.provincia.android.timbrature.Utility.Utility.showException;

public class TimbratoreActivity
        extends AppCompatActivity
        implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener,
        ResultCallback<Status>,
        OnMapReadyCallback,
        LeggiOrarioServerAsyncTaskFragment.TaskCallbacks,
        TimbraturaAsyncTaskFragment.TaskCallbacks,
        ListaTimbratureAsyncTaskFragment.TaskCallbacks,
        PrivacyDialogFragment.NoticeDialogListener {

    private GoogleMap googleMap; // Might be null if Google Play services APK is not available.

    private static final String TAG_TASK_FRAGMENT = "task_fragment";
    private static final String TIMBRATURA_TASK_FRAGMENT = "TIMBRATURA_TASK_FRAGMENT";
    private static final String LISTA_TIMBRATURE_TASK_FRAGMENT = "LISTA_TIMBRATURE_TASK_FRAGMENT";

    private static final int FIVE_MINUTES = 5000 * 60 * 2;

    private String fenceId = "";
    private Location fenceLocation = null;

    private List<Geofence> myFences = new ArrayList<>();
    private FusedLocationProviderClient fusedLocationProviderClient;
    private GoogleApiClient googleApiClient;
    private GeofencingClient geofencingClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private boolean setZoom;
    boolean locationUpdate = false;
    boolean geofencesUpdate = false;

    private PendingIntent geofencePendingIntent;

    private Button btnTimbraEntrata, btnTimbraUscita;
    private TextView tvOrario, tvCausale;
    private String idCausale, descrizioneCausale;
    private String cognome, nome;

    AlertDialog loading = null;

    @Override
    public void onPrivacyDialogClick(DialogFragment dialog) {
        // User touched the dialog's positive button
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("PrivacyDialogFragment");
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        }
    }

    @Override
    public void onTimbraturaStarted() {
        if (loading != null) {
            loading.show();
        }
    }

    @Override
    public void onTimbraturaFinished(JSONObject jsonResult) {
        if (loading != null) {
            loading.dismiss();
        }

        FragmentManager fm = getSupportFragmentManager();

        // Rimuovo il fragment
        TimbraturaAsyncTaskFragment timbraturaAsyncTaskFragment = (TimbraturaAsyncTaskFragment) fm.findFragmentByTag(TIMBRATURA_TASK_FRAGMENT);
        if (timbraturaAsyncTaskFragment != null) {
            try {
                fm.beginTransaction().remove(timbraturaAsyncTaskFragment).commit();
            } catch (Exception ex) {
                Log.d("TIMBRATURE", "Eccezione sulla rimozione del frammento");
            }
        }

        if (jsonResult != null) {
            try {
                boolean success = jsonResult.getBoolean("success");
                if (success) {

                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);

                    builder.setTitle("Timbratura");
                    builder.setCancelable(false);
                    builder.setMessage(jsonResult.getString("message"));
                    builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finishAffinity();
                        }

                    });

                    android.app.AlertDialog alert = builder.create();
                    alert.show();

                } else {
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(TimbratoreActivity.this);

                    builder.setTitle("Errore");
                    builder.setCancelable(false);
                    builder.setMessage(jsonResult.getString("message"));
                    builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }

                    });

                    android.app.AlertDialog alert = builder.create();
                    alert.show();
                }

            } catch (JSONException e) {
                showException(this, e, false);
            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("Errore");
            builder.setCancelable(false);
            builder.setMessage("Errore generale, impossibile avviare la timbratura");
            builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }

            });

            AlertDialog alert = builder.create();
            alert.show();
        }

    }

    @Override
    public void onListaTimbratureStarted() {
        if (loading != null) {
            loading.show();
        }
    }

    @Override
    public void onListaTimbratureFinished(JSONObject jsonResult) {
        if (loading != null) {
            loading.dismiss();
        }

        FragmentManager fm = getSupportFragmentManager();

        // Rimuovo il fragment
        ListaTimbratureAsyncTaskFragment listaTimbratureAsyncTaskFragment = (ListaTimbratureAsyncTaskFragment) fm.findFragmentByTag(LISTA_TIMBRATURE_TASK_FRAGMENT);
        if (listaTimbratureAsyncTaskFragment != null) {
            try {
                fm.beginTransaction().remove(listaTimbratureAsyncTaskFragment).commit();
            } catch (Exception ex) {
                Log.d("TIMBRATURE", "Eccezione sulla rimozione del frammento");
            }
        }

        if (jsonResult != null) {
            try {
                boolean success = jsonResult.getBoolean("success");
                if (success) {
                    TimbratureManager timbratureManager = TimbratureManager.newInstance();
                    if (jsonResult.has("timbrature")) {
                        JSONArray listaTimbratureJson = jsonResult.getJSONArray("timbrature");
                        for(int i=0; i<listaTimbratureJson.length(); i++) {
                            JSONObject timbraturaJson = listaTimbratureJson.getJSONObject(i);

                            timbratureManager.addNoSave(timbraturaJson);
                        }
                        timbratureManager.save();

                        Intent listaTimbratureActivity = new Intent(this, ListaTimbratureActivity.class);
                        startActivity(listaTimbratureActivity);

                    } else {
                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(TimbratoreActivity.this);

                        builder.setTitle("Attenzione");
                        builder.setCancelable(false);
                        builder.setMessage("Nessuna timbratura presente");
                        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }

                        });

                        android.app.AlertDialog alert = builder.create();
                        alert.show();
                    }


                } else {
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(TimbratoreActivity.this);

                    builder.setTitle("Errore");
                    builder.setCancelable(false);
                    builder.setMessage(jsonResult.getString("message"));
                    builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }

                    });

                    android.app.AlertDialog alert = builder.create();
                    alert.show();
                }

            } catch (JSONException e) {
                showException(this, e, false);
            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("Errore");
            builder.setCancelable(false);
            builder.setMessage("Errore generale, impossibile avviare la timbratura");
            builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }

            });

            AlertDialog alert = builder.create();
            alert.show();
        }

    }

    @Override
    public void onAggiornaOrario(JSONObject jsonResult) {
        FragmentManager fm = getSupportFragmentManager();

       if (jsonResult != null) {
           try {
               boolean success = jsonResult.getBoolean("success");
               if (success) {
                   tvOrario.setText(jsonResult.getString("orario"));
               } else {
                   tvOrario.setText(jsonResult.getString("message"));
               }
           } catch (JSONException e) {
               showException(this, e, false);
           }
       }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            switch (intent.getAction()) {
                case GEOFENCE_TRANSITION_ENTER:
                    fenceLocation = intent.getExtras().getParcelable(GEOFENCE_TRANSITION_POI);
                    fenceId = intent.getExtras().getString(GEOFENCE_TRANSITION_ID);
                    if (!fenceLocation.isFromMockProvider()) {
                        String descrizionePoi = POIManager.getInstance().getDescrizione(fenceId);
                        Toast.makeText(context, String.format("Rilevato ingresso in %1$s", descrizionePoi), Toast.LENGTH_SHORT).show();

                        btnTimbraEntrata.setBackgroundResource(R.drawable.timbra_active_button);
                        btnTimbraEntrata.setEnabled(true);

                        btnTimbraUscita.setBackgroundResource(R.drawable.timbra_active_button);
                        btnTimbraUscita.setEnabled(true);
                    }
                    break;

                case GEOFENCE_TRANSITION_EXIT:
                    fenceLocation = intent.getExtras().getParcelable(GEOFENCE_TRANSITION_POI);
                    fenceId = intent.getExtras().getString(GEOFENCE_TRANSITION_ID);
                    if (!fenceLocation.isFromMockProvider()) {
                        String descrizionePoi = POIManager.getInstance().getDescrizione(fenceId);
                        Toast.makeText(context, String.format("Rilevata uscita da %1$s", descrizionePoi), Toast.LENGTH_SHORT).show();

                        btnTimbraEntrata.setBackgroundResource(R.drawable.timbra_inactive_button);
                        btnTimbraEntrata.setEnabled(false);

                        btnTimbraUscita.setBackgroundResource(R.drawable.timbra_inactive_button);
                        btnTimbraUscita.setEnabled(false);
                    }
                    break;
            }

        }
    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ID_CAUSALE, idCausale);
        outState.putString(DESCRIZIONE_CAUSALE, descrizioneCausale);
        outState.putString(NOME, nome);
        outState.putString(COGNOME, cognome);
        outState.putBoolean(SETZOOM, setZoom);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.timbratore, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        FragmentManager fm = getSupportFragmentManager();

            switch (id) {
            case R.id.action_lista_timbrature:
                ListaTimbratureAsyncTaskFragment listaTimbratureAsyncTaskFragment = (ListaTimbratureAsyncTaskFragment) fm.findFragmentByTag(LISTA_TIMBRATURE_TASK_FRAGMENT);
                if (listaTimbratureAsyncTaskFragment == null) {
                    ListaTimbratureAsyncTaskFragment taskFragment = ListaTimbratureAsyncTaskFragment.newInstance(UtenteManager.getInstance().getAuthCode());
                    fm.beginTransaction().add(taskFragment, LISTA_TIMBRATURE_TASK_FRAGMENT).commit();
                } else {
                    Toast.makeText(TimbratoreActivity.this, "Operazione ancora in corso", Toast.LENGTH_SHORT).show();
                }

                return true;

            case R.id.action_privacy:
                DialogFragment dialog = new PrivacyDialogFragment();
                dialog.setCancelable(false);
                dialog.show(getSupportFragmentManager(), "PrivacyDialogFragment");
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timbratore);

        if(savedInstanceState == null){
            // E' un nuovo avvio
            idCausale = "";
            descrizioneCausale = getResources().getString(R.string.nessuna_causale);
            setZoom = true;

            Intent intent = getIntent();
            cognome = intent.getStringExtra(COGNOME);
            nome = intent.getStringExtra(NOME);

        } else {
            // Rotazione o resume
            idCausale = savedInstanceState.getString(ID_CAUSALE,"");
            descrizioneCausale = savedInstanceState.getString(DESCRIZIONE_CAUSALE,"");
            cognome = savedInstanceState.getString(COGNOME,"");
            nome = savedInstanceState.getString(NOME,"");
            setZoom = savedInstanceState.getBoolean(SETZOOM);

        }

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        TextView tvDipendente = findViewById(R.id.tvDipendente);
        tvDipendente.setText(cognome + " " + nome);

        tvOrario = (TextView) findViewById(R.id.tvOrario);
        tvOrario.setText("Acquisizione orario in corso...");

        tvCausale = (TextView) findViewById(R.id.tvCausale);
        tvCausale.setText(descrizioneCausale);
        tvCausale.setOnClickListener(this);

        /*
        ImageButton btnListaTimbrature = (ImageButton) findViewById(R.id.btnListaTimbrature);
        btnListaTimbrature.setOnClickListener(this);
*/
        ImageButton btnCancellaCausale = (ImageButton) findViewById(R.id.btnCancellaCausale);
        btnCancellaCausale.setOnClickListener(this);

        btnTimbraEntrata = (Button) findViewById(R.id.btnTimbraEntrata);
        btnTimbraEntrata.setOnClickListener(this);

        btnTimbraUscita = (Button) findViewById(R.id.btnTimbraUscita);
        btnTimbraUscita.setOnClickListener(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        geofencingClient = LocationServices.getGeofencingClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if ((locationResult == null) || (googleMap == null)){
                    return;
                }

                //Toast.makeText(TimbratoreActivity.this, "Nuova posizione ricevuta", Toast.LENGTH_SHORT).show();
                Location location = locationResult.getLastLocation();

                if (setZoom) {
                    moveToLocation(new POI("Posizione", location.getLatitude(), location.getLongitude(), 0, "Posizione attuale", 18), false);
                    setZoom = false;
                } else {
                    moveToLocation(new POI("Posizione", location.getLatitude(), location.getLongitude(), 0, "Posizione attuale", googleMap.getCameraPosition().zoom), true);
                }

            }
        };

        LayoutInflater inflater = this.getLayoutInflater();
        View loadingView = inflater.inflate(R.layout.loading, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setView(loadingView);
        loading = builder.create();
        TextView tvLoading = (TextView) loadingView.findViewById(R.id.tvMessaggio);
        tvLoading.setText("Attendere...");

    }

    private void createLocationRequest() {
        try {
            if (locationRequest == null) {
                LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
                locationRequest = LocationRequest.create();
                locationRequest.setInterval(LOCATION_UPDATE_INTERVAL);
                locationRequest.setFastestInterval(LOCATION_FATEST_INTERVAL);
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                locationRequest.setSmallestDisplacement(LOCATION_DISPLACEMENT);

                builder.addLocationRequest(locationRequest);

                SettingsClient settingsClient = LocationServices.getSettingsClient(this);
                Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

                task.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
                    @Override
                    public void onComplete(Task<LocationSettingsResponse> task) {
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
                        locationUpdate = true;
                        addGeofences();
                    }
                });
            } else {
                if (!locationUpdate) {
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
                    locationUpdate = true;
                }

                if (!geofencesUpdate) {
                    addGeofences();
                }

            }



        } catch (SecurityException ex) {
            showAlert(this, "Errore", "Alcuni permessi non risultato concessi.\nProva a riavviare la APP", true);
        } catch (Exception ex) {
            showException(this, ex, true);
        }
    }


    private boolean CheckGpsStatus(){
        boolean result;

        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        result = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        return result;
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        try {
            ConnectivityManager cm =
                    (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            FragmentManager fm = getSupportFragmentManager();

            String authCode = UtenteManager.getInstance().getAuthCode();

            switch (v.getId()) {
                /*
                case R.id.btnListaTimbrature:
                    ListaTimbratureAsyncTaskFragment listaTimbratureAsyncTaskFragment = (ListaTimbratureAsyncTaskFragment) fm.findFragmentByTag(LISTA_TIMBRATURE_TASK_FRAGMENT);
                    if (listaTimbratureAsyncTaskFragment == null) {
                        ListaTimbratureAsyncTaskFragment taskFragment = ListaTimbratureAsyncTaskFragment.newInstance(UtenteManager.getInstance().getAuthCode());
                        fm.beginTransaction().add(taskFragment, LISTA_TIMBRATURE_TASK_FRAGMENT).commit();
                    } else {
                        Toast.makeText(TimbratoreActivity.this, "Operazione ancora in corso", Toast.LENGTH_SHORT).show();
                    }

                    break;
*/
                case R.id.btnCancellaCausale:
                    tvCausale.setText(getResources().getString(R.string.nessuna_causale));
                    descrizioneCausale = getResources().getString(R.string.nessuna_causale);
                    idCausale = "";

                    break;

                case R.id.btnTimbraEntrata:
                    if (isConnected && CheckGpsStatus()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("H:m:s");
                        Date timeWithoutDate = sdf.parse(sdf.format(new Date()));
                        Date fenceTimeWithoutDate = sdf.parse(sdf.format(fenceLocation.getTime()));

                        long timeDelta = timeWithoutDate.getTime() - fenceTimeWithoutDate.getTime();

                        if (timeDelta < FIVE_MINUTES) {
                            /* Per evitare, magari, che una persona entri nella zona sensibile, spenga il GPS e tenga acceso (verde) il pulsante.
                                La differenza la l'istante di acquisizione e l'orario attuale deve essere inferiore ai 2 minuti
                             */
                            TimbraturaAsyncTaskFragment timbraturaAsyncTaskFragment = (TimbraturaAsyncTaskFragment) fm.findFragmentByTag(TIMBRATURA_TASK_FRAGMENT);
                            if (timbraturaAsyncTaskFragment == null) {
                                TimbraturaAsyncTaskFragment taskFragment = TimbraturaAsyncTaskFragment.newInstance(TIMBRATURA_ENTRATA, idCausale, fenceId, fenceLocation, authCode);
                                fm.beginTransaction().add(taskFragment, TIMBRATURA_TASK_FRAGMENT).commit();
                            } else {
                                Toast.makeText(TimbratoreActivity.this, "Timbratura ancora in corso", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            showAlert(this, "Errore", "Posizione rilevata non valida (Data: " + timeWithoutDate.getTime() + " Fence: " + fenceTimeWithoutDate.getTime() + ")", false );
                        }
                    } else {
                        showAlert(this, "Errore", "Mancanza di connessione dati o GPS, impossibile eseguire la timbratura", false );
                    }

                    break;

                case R.id.btnTimbraUscita:


                    if (isConnected && CheckGpsStatus()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("H:m:s");
                        Date timeWithoutDate = sdf.parse(sdf.format(new Date()));
                        Date fenceTimeWithoutDate = sdf.parse(sdf.format(fenceLocation.getTime()));

                        long timeDelta = timeWithoutDate.getTime() - fenceTimeWithoutDate.getTime();

                        if (timeDelta < FIVE_MINUTES) {
                            TimbraturaAsyncTaskFragment timbraturaAsyncTaskFragment = (TimbraturaAsyncTaskFragment) fm.findFragmentByTag(TIMBRATURA_TASK_FRAGMENT);
                            if (timbraturaAsyncTaskFragment == null) {
                                TimbraturaAsyncTaskFragment taskFragment = TimbraturaAsyncTaskFragment.newInstance(TIMBRATURA_USCITA, idCausale, fenceId, fenceLocation, authCode);
                                fm.beginTransaction().add(taskFragment, TIMBRATURA_TASK_FRAGMENT).commit();
                            } else {
                                Toast.makeText(TimbratoreActivity.this, "Timbratura ancora in corso", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            showAlert(this, "Errore", "Posizione rilevata non valida (Data: " + timeWithoutDate.getTime() + " Fence: " + fenceTimeWithoutDate.getTime() + ")", false );
                        }
                    } else {
                        showAlert(this, "Errore", "Mancanza di connessione dati o GPS, impossibile eseguire la timbratura", false );
                    }

                    break;

                case R.id.tvCausale:
                    AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
                    builderSingle.setIcon(R.mipmap.ic_launcher);
                    builderSingle.setTitle("Seleziona la causale");
                    ArrayList<Causale> causali = CausaliManager.getInstance().getList();

                    final CausaliArrayAdapter causaliArrayAdapter = new CausaliArrayAdapter(this, causali);

                    builderSingle.setNeutralButton("Annulla", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    builderSingle.setAdapter(causaliArrayAdapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Causale causale = causaliArrayAdapter.getItem(which);

                            tvCausale.setText(causale.getDescription());

                            descrizioneCausale = causale.getDescription();
                            idCausale = causale.getId();
                        }
                    });
                    builderSingle.show();
                    break;

            }
        } catch (Exception ex) {
            showException(this, ex, false);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        btnTimbraEntrata.setEnabled(false);
        btnTimbraUscita.setEnabled(false);
        btnTimbraEntrata.setBackgroundResource(R.drawable.timbra_inactive_button);
        btnTimbraUscita.setBackgroundResource(R.drawable.timbra_inactive_button);

        FragmentManager fm = getSupportFragmentManager();

        LeggiOrarioServerAsyncTaskFragment leggiOrarioServerAsyncTaskFragment = (LeggiOrarioServerAsyncTaskFragment) fm.findFragmentByTag(TAG_TASK_FRAGMENT);
        if (leggiOrarioServerAsyncTaskFragment == null) {
            LeggiOrarioServerAsyncTaskFragment taskFragment = LeggiOrarioServerAsyncTaskFragment.newInstance(UtenteManager.getInstance().getAuthCode());
            fm.beginTransaction().add(taskFragment, TAG_TASK_FRAGMENT).commit();
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(GEOFENCE_TRANSITION_EXIT);
        filter.addAction(GEOFENCE_TRANSITION_ENTER);
        registerReceiver(receiver, filter);

        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        new GpsUtils(this).turnGPSOn(new GpsUtils.onGpsListener() {
            @Override
            public void gpsStatus(boolean isGPSEnable) {
                // turn on GPS
                if (isGPSEnable) {
                    setUpMapIfNeeded();
                    createLocationRequest();
                } else {
                    showAlert(TimbratoreActivity.this, "Errore", "Impossibile avviare la app se non viene acceso il GPS", true);
                }
            }
        });


    }

    @Override
    public void onDestroy() {
        Log.i("Timbrature", "onDestroy()");
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        try {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        } catch (Exception ex) {
            Log.d("SPLASH", "Eccezione sulla rimozione degli update GPS");
        } finally {
            locationUpdate = false;
        }

        try {
            geofencingClient.removeGeofences(geofencePendingIntent)
                    .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Geofences removed
                            // ...
                        }
                    })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Failed to remove geofences
                            // ...
                        }
                    });

        } catch (Exception ex) {
            Log.d("SPLASH", "Eccezione sulla rimozione dei geofences");
        } finally {
            geofencesUpdate = false;
        }

        FragmentManager fm = getSupportFragmentManager();
        LeggiOrarioServerAsyncTaskFragment leggiOrarioServerAsyncTaskFragment = (LeggiOrarioServerAsyncTaskFragment) fm.findFragmentByTag(TAG_TASK_FRAGMENT);
        if (leggiOrarioServerAsyncTaskFragment != null) {
            try {
                fm.beginTransaction().remove(leggiOrarioServerAsyncTaskFragment).commit();
            } catch (Exception ex) {
                Log.d("SPLASH", "Eccezione sulla rimozione del frammento");
            }
        }

        unregisterReceiver(receiver);

        super.onPause();
    }

    @Override
    protected void onStop() {
        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }
        googleApiClient = null;

        super.onStop();
    }


    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (googleMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        } else {
            if (googleApiClient == null) {
                googleApiClient = new GoogleApiClient.Builder(this)
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build();
                googleApiClient.connect();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap gMap) {
        googleMap = gMap;
        // Check if we were successful in obtaining the map.
        if (googleMap != null) {
            try {
                googleMap.setBuildingsEnabled(true);
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                googleMap.setMyLocationEnabled(true);

                // Aggiungo la raffigurazione delle zone sensibili
                POIManager poiManager = POIManager.getInstance();
                for (int i = 0; i < poiManager.size(); i++) {
                    addMarker(poiManager.get(i));
                }

                googleApiClient = new GoogleApiClient.Builder(this)
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build();
                googleApiClient.connect();

            } catch (SecurityException e) {
                showAlert(this, "Errore", "Alcuni permessi non risultato concessi.\nProva a riavviare la APP", true);

            }
        }
    }


    /**
     * Add a map marker at the place specified.
     *
     * @param poi the place to take action on
     */
    private void addMarker(POI poi) {
        try {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(poi.getLatLng()).title(poi.getDescrizione());

            /*
            if (!TextUtils.isEmpty(place.getSnippet())) {
                markerOptions.snippet(place.getSnippet());
            }
            */

            BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_marcatempo);
            Bitmap b = bitmapdraw.getBitmap();
            Bitmap smallMarker = Bitmap.createScaledBitmap(b, 80, 80, false);

            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
            googleMap.addMarker(markerOptions);
            drawGeofenceAroundTarget(poi);
        } catch (Exception ex){
            showException(this, ex, false);
        }
    }

    /**
     * If our place has a fence radius greater than 0 then draw a circle around it.
     *
     * @param poi the place to take action on
     */
    private void drawGeofenceAroundTarget(POI poi) {
        if (poi.getRaggio() <= 0) {
            // Nothing to draw
            return;
        }
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(poi.getLatLng());
        circleOptions.fillColor(Color.argb(0x55, 0x00, 0x00, 0xff));
        circleOptions.strokeColor(Color.argb(0xaa, 0x00, 0x00, 0xff));
        circleOptions.radius(poi.getRaggio() - 5);
        googleMap.addCircle(circleOptions);
    }

    /**
     * Update our map's location to the place specified.
     *
     * @param poi place to take action on
     */
    private void moveToLocation(final POI poi, boolean animate) {
        try {
            if (poi != null) {
                if (animate) {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(poi.getLatLng(), poi.getZoomLevel()), new GoogleMap.CancelableCallback() {
                        @Override
                        public void onFinish() {
                            googleMap.animateCamera(CameraUpdateFactory.zoomTo(poi.getZoomLevel()), 2000, null);
                        }

                        @Override
                        public void onCancel() {
                            // Nothing to see here.
                        }
                    });
                } else {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(poi.getLatLng(), poi.getZoomLevel()));
                }
            }
        } catch (Exception ex){
            showException(this, ex, false);
        }
    }

    /**
     * If our place has a fence radius > 0 then add it to our monitored fences.
     *
     * @param poi the place to take action on
     */
    private void addFence(POI poi) {
        if (poi.getRaggio() <= 0) {
            // Nothing to monitor
            return;
        }
        /*
        Geofence geofence = new Geofence.Builder()
                .setCircularRegion(poi.getLatitudine(), poi.getLongitudine(), poi.getRaggio())
                .setRequestId(poi.getId()) // every fence must have an ID
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT)
                .setExpirationDuration(Geofence.NEVER_EXPIRE) // how long do we care about this geofence?
                .setLoiteringDelay(LOITERING)
                .build();
        */

        Geofence geofence = new Geofence.Builder()
                .setCircularRegion(poi.getLatitudine(), poi.getLongitudine(), poi.getRaggio())
                .setRequestId(poi.getId()) // every fence must have an ID
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .setExpirationDuration(Geofence.NEVER_EXPIRE) // how long do we care about this geofence?
                .setNotificationResponsiveness(0)
                .build();
        myFences.add(geofence);
    }


    private void addGeofences () {
        try {
            if (myFences.size() == 0) {
                POIManager poiManager = POIManager.getInstance();
                for (int i = 0; i < poiManager.size(); i++) {
                    addFence(poiManager.get(i));
                }
            }

            if (!geofencesUpdate) {
                geofencePendingIntent = getRequestPendingIntent();
                geofencingClient.addGeofences(getGeofencingRequest(myFences), geofencePendingIntent)
                        .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                geofencesUpdate = true;
                            }
                        })
                        .addOnFailureListener(this, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                if (!(exception instanceof ApiException)) {
                                    showException(TimbratoreActivity.this, exception, true);
                                } else {
                                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(TimbratoreActivity.this);

                                    builder.setTitle("Errore");
                                    builder.setCancelable(false);
                                    builder.setMessage("Impossibile continuare. Prova ad attivare la geolocalizzazione e riavvia la APP.");
                                    builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {

                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            System.exit(0);
                                        }

                                    });

                                    android.app.AlertDialog alert = builder.create();
                                    alert.show();
                                }

                            }
                        })
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (!task.isSuccessful()) {
                                    Log.e("Geofences", "Impossibile aggiungere i punti sensibili");
                                }
                            }
                        });
            }
        } catch (SecurityException ex) {
            showAlert(this, "Errore", "Alcuni permessi non risultato concessi.\nProva a riavviare la APP", true);
        } catch (Exception ex) {
            showException(this, ex, true);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {

        //Toast.makeText(this, "GoogleApiClient Connected", Toast.LENGTH_SHORT).show();
    }

    private GeofencingRequest getGeofencingRequest(List<Geofence> geofences) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        //builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL);
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofences);
        return builder.build();
    }

    @Override
    public void onConnectionSuspended(int i) {
        //Toast.makeText(this, "GoogleApiClient Connection Suspended", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, "GoogleApiClient Connection Failed", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onResult(Status status) {
        String toastMessage;

        /*
        if (status.isSuccess()) {
            toastMessage = "Success: We Are Monitoring Our Fences";
        } else {
            toastMessage = "Error: We Are NOT Monitoring Our Fences";
        }
        Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();

         */
    }

    /**
     * Returns the current PendingIntent to the caller.
     *
     * @return The PendingIntent used to create the current set of geofences
     */
    public PendingIntent getRequestPendingIntent() {
        return createRequestPendingIntent();
    }

    /**
     * Get a PendingIntent to send with the request to add Geofences. Location
     * Services issues the Intent inside this PendingIntent whenever a geofence
     * transition occurs for the current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence
     * transitions.
     */
    private PendingIntent createRequestPendingIntent() {
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        } else {
            Intent intent = new Intent(this, GeofenceTransitionReceiver.class);
            intent.setAction(GEOFENCE_TRANSITION_ACTION);
            return PendingIntent.getBroadcast(this, 3, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        }
    }





}