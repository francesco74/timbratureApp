
package it.lucca.provincia.android.timbrature.Activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import it.lucca.provincia.android.timbrature.AsyncTaskFragment.SplashSMSRequestAsyncTaskFragment;
import it.lucca.provincia.android.timbrature.AsyncTaskFragment.SplashParamsAsyncTaskFragment;
import it.lucca.provincia.android.timbrature.CustomDialogs.PrivacyDialogFragment;
import it.lucca.provincia.android.timbrature.CustomDialogs.ProminentDisclosureAccessFineLocationDialogFragment;
import it.lucca.provincia.android.timbrature.CustomDialogs.ProminentDisclosureBackgroundLocationDialogFragment;
import it.lucca.provincia.android.timbrature.CustomDialogs.ProminentDisclosureWriteExternalStorageDialogFragment;
import it.lucca.provincia.android.timbrature.InstanceManager.CausaliManager;
import it.lucca.provincia.android.timbrature.InstanceManager.POIManager;
import it.lucca.provincia.android.timbrature.InstanceManager.PrivacyManager;
import it.lucca.provincia.android.timbrature.InstanceManager.UtenteManager;
import it.lucca.provincia.android.timbrature.R;
import it.lucca.provincia.android.timbrature.Utility.App;
import it.lucca.provincia.android.timbrature.Utility.AppSignatureHelper;

import static it.lucca.provincia.android.timbrature.Utility.Const.COGNOME;
import static it.lucca.provincia.android.timbrature.Utility.Const.NOME;
import static it.lucca.provincia.android.timbrature.Utility.Utility.showAlert;
import static it.lucca.provincia.android.timbrature.Utility.Utility.showException;

public class SplashActivity
        extends AppCompatActivity
        implements
            SplashParamsAsyncTaskFragment.TaskCallbacks,
            SplashSMSRequestAsyncTaskFragment.TaskCallbacks,
            ProminentDisclosureBackgroundLocationDialogFragment.NoticeDialogListener,
            ProminentDisclosureAccessFineLocationDialogFragment.NoticeDialogListener,
            ProminentDisclosureWriteExternalStorageDialogFragment.NoticeDialogListener,
            PrivacyDialogFragment.NoticeDialogListener {

    AlertDialog alertDialogLoading = null;
    AlertDialog alertDialogSmsWaiting = null;
    private static final String TAG_PARAM_TASK_FRAGMENT = "task_param_fragment";
    private static final String TAG_SMS_TASK_FRAGMENT = "task_sms_fragment";

    private static final String ProminentDisclosureAccessFineLocationDialogFragment = "ProminentDisclosureAccessFineLocationDialogFragment";
    private static final String ProminentDisclosureBackgroundLocationDialogFragment = "ProminentDisclosureBackgroundLocationDialogFragment";
    private static final String ProminentDisclosureWriteExternalStorageDialogFragment = "ProminentDisclosureWriteExternalStorageDialogFragment";
    private static final String PrivacyDialogFragment = "PrivacyDialogFragment";
    
    private static final String STATUS_GETTING_PARAMETERS = "STATUS_GETTING_PARAMETERS";
    private static final String STATUS_SMS_REQUEST = "STATUS_SMS_REQUEST";
    private static final String STATUS_WAITING_SMS = "STATUS_WAITING_SMS";

    private static final String STATUS = "STATUS";
    private static final String PRIVACY = "PRIVACY";

    private String status = "";
    private boolean privacy = false;

    String[] perms = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
    };

    private static final int ACCESS_FINE_LOCATION_PERMISSION = 1;
    private static final int WRITE_EXTERNAL_STORAGE_PERMISSION = 2;
    private static final int ACCESS_BACKGROUND_LOCATION_PERMISSION = 4;

    int permissionMask = 0; // Permessi accordati
    int permissionMandatory = 7; // Permessi minimi affinchè la APP possa avviarsi
    int permissionStatusCheck = 0; // utilizzato per capire se il permesso è stato analizzato
    int permissionStatusCheckComplete = 7;

    @Override
    public void onPrivacyDialogClick(DialogFragment dialog) {
        // User touched the dialog's positive button
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(PrivacyDialogFragment);
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        }

        privacy = true;
        verificaPermessi();
    }

    @Override
    public void onProminentDisclosureBackgroundLocationDialogClick(DialogFragment dialog) {
        // User touched the dialog's positive button
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, ACCESS_BACKGROUND_LOCATION_PERMISSION);

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(ProminentDisclosureBackgroundLocationDialogFragment);
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        }
    }

    @Override
    public void onProminentDisclosureWriteExternalStorageDialogClick(DialogFragment dialog) {
        // User touched the dialog's positive button
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_PERMISSION);

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(ProminentDisclosureWriteExternalStorageDialogFragment);
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        }
    }

    @Override
    public void onProminentDisclosureAccessFineLocationDialogClick(DialogFragment dialog) {
        // User touched the dialog's positive button
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION_PERMISSION);

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(ProminentDisclosureAccessFineLocationDialogFragment);
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        }
    }

    @Override
    public void onSplashParamsTaskStarted() {
        if (alertDialogLoading != null) {
            alertDialogLoading.show();
        }
    }

    @Override
    public void onSplashSMSRequestTaskStarted() {
        if (alertDialogSmsWaiting != null) {
            alertDialogSmsWaiting.show();
        }
    }


    private void acquisisciDati(JSONObject jsonResult) throws JSONException {
        // Acquisisco le causali
        JSONArray arrayCausali = jsonResult.getJSONArray("causali");
        if (arrayCausali.length() == 0) {
            Toast.makeText(App.get(), "Nessuna causale ricevuta dal server", Toast.LENGTH_LONG).show();
        } else {
            CausaliManager causaliManager = CausaliManager.newInstance();
            for (int i = 0; i < arrayCausali.length(); i++) {
                causaliManager.addNoSave(arrayCausali.getJSONObject(i).getString("id"), arrayCausali.getJSONObject(i).getString("descrizione"));
            }
            causaliManager.save();
        }

        // Acquisisco i POI
        JSONArray arrayPOI = jsonResult.getJSONArray("poi");
        if (arrayPOI.length() == 0) {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);

            builder.setTitle("Errore");
            builder.setCancelable(false);
            builder.setMessage("Nessun punto di timbratura ricevuto dal server. Impossibile continuare !");
            builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    System.exit(0);
                }

            });

            android.app.AlertDialog alert = builder.create();
            alert.show();
        } else {
            final FragmentManager fm = getSupportFragmentManager();

            POIManager poiManager = POIManager.newInstance();
            for (int i = 0; i < arrayPOI.length(); i++) {
                poiManager.addNoSave(arrayPOI.getJSONObject(i).getString("id"), arrayPOI.getJSONObject(i).getDouble("latitudine"), arrayPOI.getJSONObject(i).getDouble("longitudine"), arrayPOI.getJSONObject(i).getDouble("raggio"), arrayPOI.getJSONObject(i).getString("descrizione"));
            }

            poiManager.save();

            // Acquisisco i dati dell'utente
            UtenteManager.newInstance(jsonResult.getString("matricola"), jsonResult.getString("cognome"), jsonResult.getString("nome"), jsonResult.getString("auth_code"));

            if (jsonResult.getBoolean("request_sms_auth") ) {
                Toast.makeText(App.get(), "E' necessario eseguire una autenticazione mediante SMS. Attendere...", Toast.LENGTH_LONG).show();

                //Intent smsAuthIntent = new Intent(this, SMSAuthActivity.class);
                //startActivity(smsAuthIntent);
                //finish();

                SplashSMSRequestAsyncTaskFragment taskFragment = SplashSMSRequestAsyncTaskFragment.newInstance(jsonResult.getString("auth_code"));
                fm.beginTransaction().add(taskFragment, TAG_PARAM_TASK_FRAGMENT).commit();

                status = STATUS_SMS_REQUEST;
            } else {
                Toast.makeText(App.get(), "Benvenuto " + jsonResult.getString("nome"), Toast.LENGTH_LONG).show();

                Intent timbratureIntent = new Intent(this, TimbratoreActivity.class);
                timbratureIntent.putExtra(NOME, jsonResult.getString("nome"));
                timbratureIntent.putExtra(COGNOME, jsonResult.getString("cognome"));
                startActivity(timbratureIntent);
                finish();
            }
        }
    }

    private void acquisisciSMS() {
        // Get an instance of SmsRetrieverClient, used to start listening for a matching
        // SMS message.
        SmsRetrieverClient client = SmsRetriever.getClient(this );

        // Starts SmsRetriever, which waits for ONE matching SMS message until timeout
        // (5 minutes). The matching SMS message will be sent via a Broadcast Intent with
        // action SmsRetriever#SMS_RETRIEVED_ACTION.
        Task<Void> task = client.startSmsRetriever();

        // Listen for success/failure of the start Task. If in a background thread, this
        // can be made blocking using Tasks.await(task, [timeout]);
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showException(SplashActivity.this, e, true);
            }
        });
    }

    @Override
    public void onSplashSMSRequestTaskFinished(final JSONObject jsonResult) {
        /*if (alertDialogSmsWaiting != null) {
            alertDialogSmsWaiting.dismiss();
        }*/

        FragmentManager fm = getSupportFragmentManager();

        // Rimuovo il fragment
        SplashSMSRequestAsyncTaskFragment splashSmsRequestAsyncTaskFragment = (SplashSMSRequestAsyncTaskFragment) fm.findFragmentByTag(TAG_SMS_TASK_FRAGMENT);
        if (splashSmsRequestAsyncTaskFragment != null) {
            try {
                fm.beginTransaction().remove(splashSmsRequestAsyncTaskFragment).commit();
            } catch (Exception ex) {
                Log.d("SPLASH", "Eccezione sulla rimozione del frammento");
            }
        }

        if (jsonResult != null) {
            try {
                Boolean success = jsonResult.getBoolean("success");
                if (success) {
                    acquisisciSMS();
                    status = STATUS_WAITING_SMS;

                } else {
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(SplashActivity.this);

                    builder.setTitle("Errore");
                    builder.setCancelable(false);
                    builder.setMessage(jsonResult.getString("message"));
                    builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            System.exit(0);
                        }

                    });

                    android.app.AlertDialog alert = builder.create();
                    alert.show();

                }

            } catch (JSONException e) {
                showException(SplashActivity.this, e, true);
            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("Errore");
            builder.setCancelable(false);
            builder.setMessage("Errore generale, impossibile autenticarsi tramite SMS");
            builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    System.exit(0);
                }

            });

            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    @Override
    public void onSplashParamsTaskFinished(final JSONObject jsonResult) {

        if (alertDialogLoading != null) {
            alertDialogLoading.dismiss();
        }

        FragmentManager fm = getSupportFragmentManager();

        // Rimuovo il fragment
        SplashParamsAsyncTaskFragment splashParamsAsyncTaskFragment = (SplashParamsAsyncTaskFragment) fm.findFragmentByTag(TAG_PARAM_TASK_FRAGMENT);
        if (splashParamsAsyncTaskFragment != null) {
            try {
                fm.beginTransaction().remove(splashParamsAsyncTaskFragment).commit();
            } catch (Exception ex) {
                Log.d("SPLASH", "Eccezione sulla rimozione del frammento");
            }
        }

        if (jsonResult != null) {
            try {
                Boolean success = jsonResult.getBoolean("success");
                if (success) {
                    String messaggio = "";

                    try {
                        messaggio = jsonResult.getString("message");
                    } catch (Exception ex) {
                        // Non importante la sua gestione
                    }

                    if (messaggio.length() != 0) {
                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(SplashActivity.this);

                        builder.setTitle("Informazione");
                        builder.setCancelable(false);
                        builder.setMessage(messaggio);
                        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                try {
                                    acquisisciDati(jsonResult);
                                } catch (JSONException e) {
                                    showException(SplashActivity.this, e, true);
                                }
                            }

                        });

                        android.app.AlertDialog alert = builder.create();
                        alert.show();
                    } else {
                        acquisisciDati(jsonResult);
                    }


                } else {
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(SplashActivity.this);

                    builder.setTitle("Errore");
                    builder.setCancelable(false);
                    builder.setMessage(jsonResult.getString("message"));
                    builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            System.exit(0);
                        }

                    });

                    android.app.AlertDialog alert = builder.create();
                    alert.show();

                }

            } catch (JSONException e) {
                showException(SplashActivity.this, e, true);
            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("Errore");
            builder.setCancelable(false);
            builder.setMessage("Errore generale, impossibile avviare l'applicazione");
            builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    System.exit(0);
                }

            });

            AlertDialog alert = builder.create();
            alert.show();
        }

    }

    private void avviaApp() {
        Log.d("SPLASH", "avviaApp");

        final FragmentManager fm = getSupportFragmentManager();

        if (checkPermission(perms)) {

            switch (status) {
                case STATUS_SMS_REQUEST:
                    // Sono in attesa dell'SMS di autenticazione ? Verifico...
                    SplashSMSRequestAsyncTaskFragment splashSmsRequestAsyncTaskFragment = (SplashSMSRequestAsyncTaskFragment) fm.findFragmentByTag(TAG_SMS_TASK_FRAGMENT);
                    if (splashSmsRequestAsyncTaskFragment == null) {
                        // Non ho il frammento di verifica tramite SMS che vado quindi ad istanziare
                        SplashSMSRequestAsyncTaskFragment taskFragment = new SplashSMSRequestAsyncTaskFragment();
                        fm.beginTransaction().add(taskFragment, TAG_SMS_TASK_FRAGMENT).commit();
                    } else {
                        // Messaggio
                        Toast.makeText(App.get(), "Richiesta invio SMS in corso...", Toast.LENGTH_LONG).show();
                    }
                    break;

                case STATUS_WAITING_SMS:
                    Toast.makeText(App.get(), "In attesa dell'SMS di autenticazione...", Toast.LENGTH_LONG).show();
                    break;

                default:
                    // Devo sempre avviare l'acquisizione dei parametri dal server
                    SplashParamsAsyncTaskFragment splashParamsAsyncTaskFragment = (SplashParamsAsyncTaskFragment) fm.findFragmentByTag(TAG_PARAM_TASK_FRAGMENT);
                    if (splashParamsAsyncTaskFragment == null) {
                        // Non ho il frammento di richiesta parametri che vado quindi ad istanziare

                        SplashParamsAsyncTaskFragment taskFragment = new SplashParamsAsyncTaskFragment();
                        fm.beginTransaction().add(taskFragment, TAG_PARAM_TASK_FRAGMENT).commit();

                        status = STATUS_GETTING_PARAMETERS;
                    } else {
                        Toast.makeText(App.get(), "Avvio in corso...", Toast.LENGTH_LONG).show();
                    }
                    break;

            }
        } else {
            showAlert(this, "Permessi mancanti", "Non hai accettato i permessi necessari per avviare l'APP.\n\nModifica i permessi o reistalla l'APP", true);
        }
    }

    private void verificaPermessi () {
        if (permissionStatusCheck == permissionStatusCheckComplete) {
            avviaApp();
        } else {
            requestPermission(perms);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d("SPLASH", "onResume");

        if (privacy) {
            verificaPermessi();
        } else {
            DialogFragment dialog = new PrivacyDialogFragment();
            dialog.setCancelable(false);
            dialog.show(getSupportFragmentManager(), PrivacyDialogFragment);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.d("SPLASH", "onPause");

    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d("SPLASH", "onStop");

    }

    private boolean checkPermission(String[] permissions) {
        boolean result = false;
        int permissionTest = 0;

        for (final String permission : permissions) {
            switch (permission) {
                case Manifest.permission.ACCESS_FINE_LOCATION:
                    if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                        permissionTest = permissionTest | ACCESS_FINE_LOCATION_PERMISSION;
                    }
                    break;

                case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                            permissionTest = permissionTest | WRITE_EXTERNAL_STORAGE_PERMISSION;

                            PrivacyManager.getInstance().setPrivacy(privacy);
                        }
                    } else {
                        permissionTest = permissionTest | WRITE_EXTERNAL_STORAGE_PERMISSION;
                        PrivacyManager.getInstance().setPrivacy(privacy);
                    }
                    break;

                case Manifest.permission.ACCESS_BACKGROUND_LOCATION:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                            permissionTest = permissionTest | ACCESS_BACKGROUND_LOCATION_PERMISSION;
                        }
                    } else {
                        permissionTest = permissionTest | ACCESS_BACKGROUND_LOCATION_PERMISSION;
                    }
                    break;


            }
        }

        if ((permissionTest & permissionMandatory)  == permissionMandatory) {
            result = true;
        }

        return result;

    }

    private boolean  permissionAlreadyChecked(String permission) {
        boolean result = false;

        switch (permission) {
            case Manifest.permission.ACCESS_FINE_LOCATION:
                if ((permissionStatusCheck & ACCESS_FINE_LOCATION_PERMISSION) == ACCESS_FINE_LOCATION_PERMISSION) {
                    result = true;
                }
                break;

            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                if ((permissionStatusCheck & WRITE_EXTERNAL_STORAGE_PERMISSION) == WRITE_EXTERNAL_STORAGE_PERMISSION) {
                    result = true;
                }
                break;

            case Manifest.permission.ACCESS_BACKGROUND_LOCATION:
                if ((permissionStatusCheck & ACCESS_BACKGROUND_LOCATION_PERMISSION) == ACCESS_BACKGROUND_LOCATION_PERMISSION) {
                    result = true;
                }
                break;

        }


        return result;
    }

    private void requestPermission(String[] permissions) {

        for (final String permission : permissions) {
            if (! permissionAlreadyChecked(permission)) {

                switch (permission) {
                    case Manifest.permission.ACCESS_FINE_LOCATION:
                        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                            DialogFragment dialog = new ProminentDisclosureAccessFineLocationDialogFragment();
                            dialog.setCancelable(false);
                            dialog.show(getSupportFragmentManager(), ProminentDisclosureAccessFineLocationDialogFragment);

                            return;
                        } else {
                            permissionMask = permissionMask | ACCESS_FINE_LOCATION_PERMISSION;
                            permissionStatusCheck = permissionStatusCheck | ACCESS_FINE_LOCATION_PERMISSION;
                        }
                        break;

                    case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                                DialogFragment dialog = new ProminentDisclosureWriteExternalStorageDialogFragment();
                                dialog.setCancelable(false);
                                dialog.show(getSupportFragmentManager(), ProminentDisclosureWriteExternalStorageDialogFragment);

                                return;
                            } else {
                                PrivacyManager.getInstance().setPrivacy(privacy);

                                permissionMask = permissionMask | WRITE_EXTERNAL_STORAGE_PERMISSION;
                                permissionStatusCheck = permissionStatusCheck | WRITE_EXTERNAL_STORAGE_PERMISSION;
                            }
                        } else {
                            PrivacyManager.getInstance().setPrivacy(privacy);

                            permissionMask = permissionMask | WRITE_EXTERNAL_STORAGE_PERMISSION;
                            permissionStatusCheck = permissionStatusCheck | WRITE_EXTERNAL_STORAGE_PERMISSION;
                        }
                        break;

                    case Manifest.permission.ACCESS_BACKGROUND_LOCATION:
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                                DialogFragment dialog = new ProminentDisclosureBackgroundLocationDialogFragment();
                                dialog.setCancelable(false);
                                dialog.show(getSupportFragmentManager(), ProminentDisclosureBackgroundLocationDialogFragment);

                                return;
                            } else {
                                permissionMask = permissionMask | ACCESS_BACKGROUND_LOCATION_PERMISSION;
                                permissionStatusCheck = permissionStatusCheck | ACCESS_BACKGROUND_LOCATION_PERMISSION;
                            }
                        } else {
                            permissionMask = permissionMask | ACCESS_BACKGROUND_LOCATION_PERMISSION;
                            permissionStatusCheck = permissionStatusCheck | ACCESS_BACKGROUND_LOCATION_PERMISSION;
                        }

                        break;
                }
            }
        }

        if (permissionStatusCheck == permissionStatusCheckComplete) {
            avviaApp();
        }

    }
    /*

    private void permissionError () {
        Toast.makeText(App.get(), "Attenzione: non sono stati accettati i permessi richiesti !", Toast.LENGTH_LONG).show();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean result = false;
            for (String perm : perms) {
                result = result || ActivityCompat.shouldShowRequestPermissionRationale(this, perm);
            }

            if (result) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle("Attenzione");
                builder.setCancelable(false);
                builder.setMessage("Devi accettare i permessi richiesti per poter utilizzare l'app !");
                builder.setCancelable(false);

                builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        requestPermission(perms);
                        dialog.dismiss();
                    }

                });

                AlertDialog alert = builder.create();
                alert.show();

            }
        }
    }
*/
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case ACCESS_FINE_LOCATION_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        permissionMask = permissionMask | ACCESS_FINE_LOCATION_PERMISSION;
                    }

                }

                permissionStatusCheck = permissionStatusCheck | ACCESS_FINE_LOCATION_PERMISSION;
                break;
            }

            case ACCESS_BACKGROUND_LOCATION_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
                        permissionMask = permissionMask | ACCESS_BACKGROUND_LOCATION_PERMISSION;
                    }

                }

                permissionStatusCheck = permissionStatusCheck | ACCESS_BACKGROUND_LOCATION_PERMISSION;
                break;

            case WRITE_EXTERNAL_STORAGE_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
                        permissionMask = permissionMask | WRITE_EXTERNAL_STORAGE_PERMISSION;

                        // Adesso che ho i permessi, salvo lo stato della privacy
                        PrivacyManager.getInstance().setPrivacy(privacy);
                    }

                }

                permissionStatusCheck = permissionStatusCheck | WRITE_EXTERNAL_STORAGE_PERMISSION;

                break;


        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATUS, status);
        outState.putBoolean(PRIVACY, privacy);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        String version, appSignature;

        if(savedInstanceState == null){
            // E' un nuovo avvio
            status = "";
            if ((ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)) {
                privacy = PrivacyManager.getInstance().isAccepted();
            } else {
                privacy = false;
            }
        } else {
            // Rotazione o resume
            status = savedInstanceState.getString(STATUS,"");
            privacy = savedInstanceState.getBoolean(PRIVACY,false);
        }

        LayoutInflater inflater = this.getLayoutInflater();
        View loadingView = inflater.inflate(R.layout.loading, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false); // if you want user to wait for some process to finish,
        builder.setView(loadingView);
        alertDialogLoading = builder.create();
        TextView tvLoading = (TextView) loadingView.findViewById(R.id.tvMessaggio);
        tvLoading.setText("Accesso al sistema delle timbrature in corso. Attendere...");

        View smsWaitingView = inflater.inflate(R.layout.loading, null);
        AlertDialog.Builder builderSmsWaitingBuilder = new AlertDialog.Builder(this);
        builderSmsWaitingBuilder.setCancelable(false); // if you want user to wait for some process to finish,
        builderSmsWaitingBuilder.setView(smsWaitingView);
        alertDialogSmsWaiting = builderSmsWaitingBuilder.create();
        TextView tvSmsWaiting = (TextView) smsWaitingView.findViewById(R.id.tvMessaggio);
        tvSmsWaiting.setText("Autenticazione tramite SMS in corso. Attendere...");

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName + " (" + pInfo.versionCode + ")";
        } catch (PackageManager.NameNotFoundException ex) {
            version = "?";
        }

        try {
            AppSignatureHelper appSignatureHelper = new AppSignatureHelper(this);
            ArrayList<String> signatures = appSignatureHelper.getAppSignatures();

            appSignature = signatures.get(0);
        } catch (Exception ex) {
            appSignature = "?";
        }

        CausaliManager.newInstance();

        TextView tvVersion = (TextView) findViewById(R.id.tvVersion);
        tvVersion.setText(version);

        TextView tvAppSignature = (TextView) findViewById(R.id.tvAppSignature);
        tvAppSignature.setText(appSignature);

    }

}
