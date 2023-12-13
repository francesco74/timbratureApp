package it.lucca.provincia.android.timbrature.Utility;

import android.content.Context;
import android.content.DialogInterface;

import java.security.SecureRandom;
import java.util.ArrayList;

public class Utility {

    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    static SecureRandom rnd = new SecureRandom();

    public static String randomString(int len){
        StringBuilder sb = new StringBuilder(len);
        for(int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }

    public static void showException (Context ctx, Exception ex, boolean exit) {
        showAlert(ctx, "Errore", ex.getMessage(), exit);

    }

    public static String Implode(ArrayList<String> items) {
        String result = "";

        for (String item : items) {
            if ((item != null) && (item.length() != 0)) {

                if (result.length() == 0) {
                    result = item;
                } else {
                    result += "|" + item;
                }
            }
        }

        return result;
    }

    public static void showAlert (Context ctx, String title, String message, final boolean exit) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ctx);

        builder.setTitle(title);
        builder.setCancelable(false);
        builder.setMessage(message);
        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                if (exit) {
                    System.exit(0);
                }
            }

        });

        android.app.AlertDialog alert = builder.create();
        alert.show();
    }
}
