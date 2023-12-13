package it.lucca.provincia.android.timbrature.CustomDialogs;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.w3c.dom.Text;

import it.lucca.provincia.android.timbrature.R;
import it.lucca.provincia.android.timbrature.Utility.Utility;

import static it.lucca.provincia.android.timbrature.Utility.Const.privacyWebPage;

public class PrivacyDialogFragment extends DialogFragment {

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface NoticeDialogListener {
        public void onPrivacyDialogClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    NoticeDialogListener listener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (NoticeDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(getActivity().toString() + " must implement NoticeDialogListener");
        }
    }


    /** The system calls this only when creating the layout in a dialog. */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View privacyView = inflater.inflate(R.layout.privacy, null);

        final TextView tv = (TextView)privacyView.findViewById(R.id.textView1);
        final WebView wv = (WebView) privacyView.findViewById(R.id.webView1);
        wv.getSettings().setJavaScriptEnabled(true); // enable javascript
        wv.getSettings().setLoadWithOverviewMode(true);

        wv.clearCache(true);

        wv.setWebViewClient(new WebViewClient() {

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(requireActivity(), description, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                tv.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                tv.setVisibility(View.GONE);
            }

        });
        wv.loadUrl(privacyWebPage);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(privacyView)
            .setNeutralButton("Ho capito", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // Send the positive button event back to the host activity
                    listener.onPrivacyDialogClick(PrivacyDialogFragment.this);
                    dialog.dismiss();
                }
            });
        return builder.create();





    }


}
