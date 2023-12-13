package it.lucca.provincia.android.timbrature.BroadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;

import it.lucca.provincia.android.timbrature.Activity.TimbratoreActivity;
import it.lucca.provincia.android.timbrature.InstanceManager.UtenteManager;
import it.lucca.provincia.android.timbrature.R;
import it.lucca.provincia.android.timbrature.Utility.Utility;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static it.lucca.provincia.android.timbrature.Utility.Const.COGNOME;
import static it.lucca.provincia.android.timbrature.Utility.Const.NOME;

public class SMSBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
            Bundle extras = intent.getExtras();
            Status status = (Status) extras.get(SmsRetriever.EXTRA_STATUS);

            switch(status.getStatusCode()) {
                case CommonStatusCodes.SUCCESS:
                    // Get SMS message contents
                    String message = (String) extras.get(SmsRetriever.EXTRA_SMS_MESSAGE);
                    UtenteManager utenteManager = UtenteManager.getInstance();
                    if (utenteManager != null) {
                        // Extract one-time code from the message and complete verification
                        if (message.contains(utenteManager.getAuthCode())) {
                            Intent timbratureIntent = new Intent(context, TimbratoreActivity.class);
                            timbratureIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK) ;
                            timbratureIntent.putExtra(NOME, utenteManager.getNome());
                            timbratureIntent.putExtra(COGNOME, utenteManager.getCognome());
                            context.startActivity(timbratureIntent);
                        } else {
                            Utility.showAlert(context, context.getString(R.string.SMSErrorMessage), context.getString(R.string.SMSInvalidMessage), true);
                        }
                    } else {
                        Utility.showAlert(context, context.getString(R.string.GenericErrorMessage), context.getString(R.string.GenericErrorDescription), true);
                    }

                    break;
                case CommonStatusCodes.TIMEOUT:
                    // Waiting for SMS timed out (5 minutes)
                    Utility.showAlert(context, context.getString(R.string.SMSErrorMessage), context.getString(R.string.SMSTimeoutMessage), true);
                    break;
            }
        }
    }

}
