package it.lucca.provincia.android.timbrature.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

import it.lucca.provincia.android.timbrature.ArrayAdapters.TimbratureArrayAdapter;
import it.lucca.provincia.android.timbrature.InstanceManager.TimbratureManager;
import it.lucca.provincia.android.timbrature.Models.Timbratura;
import it.lucca.provincia.android.timbrature.R;

import static it.lucca.provincia.android.timbrature.Utility.Utility.showException;

public class ListaTimbratureActivity extends AppCompatActivity {

    TimbratureArrayAdapter timbratureArrayAdapter;
    private ListView lvTimbrature;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_timbrature);

        try {
            ArrayList<Timbratura> timbrature = TimbratureManager.getInstance().getList();

            timbratureArrayAdapter = new TimbratureArrayAdapter(this, timbrature);

            lvTimbrature = (ListView) findViewById(R.id.lvTimbrature);
            Button btnChiudi = (Button) findViewById(R.id.btnChiudi);
            btnChiudi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

        } catch (Exception ex) {
            showException(this, ex, false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {

            lvTimbrature.setAdapter(timbratureArrayAdapter);

        } catch (Exception ex) {
            showException(this, ex, false);
        }

    }
}
