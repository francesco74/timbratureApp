package it.lucca.provincia.android.timbrature.ArrayAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import it.lucca.provincia.android.timbrature.Models.Causale;
import it.lucca.provincia.android.timbrature.R;

public class CausaliArrayAdapter extends ArrayAdapter<Causale> {
    private static class ViewHolder {
        private TextView codice, descrizione;
        private LinearLayout linearLayout;
    }

    Context context;

    public CausaliArrayAdapter(Context context, ArrayList<Causale> causali) {
        super(context, 0, causali);

        this.context = context;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        // Get the data item for this position
        Causale causale = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (view == null) {
            // If there's no view to re-use, inflate a brand new view for row
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            view = inflater.inflate(R.layout.causali, parent, false);
            viewHolder.codice = (TextView) view.findViewById(R.id.tvCodice);
            viewHolder.descrizione = (TextView) view.findViewById(R.id.tvDescrizione);
            viewHolder.linearLayout = (LinearLayout) view.findViewById(R.id.llCausale);
            // Cache the viewHolder object inside the fresh view
            view.setTag(viewHolder);
        } else {
            // View is being recycled, retrieve the viewHolder object from tag
            viewHolder = (ViewHolder) view.getTag();
        }
        // Populate the data from the data object via the viewHolder object
        // into the template view.
        viewHolder.codice.setText(causale.getId());
        viewHolder.descrizione.setText(causale.getDescription());
        if ((position % 2) == 0) {
            viewHolder.linearLayout.setBackgroundColor(context.getResources().getColor(R.color.evenColor));
        } else {
            viewHolder.linearLayout.setBackgroundColor(context.getResources().getColor(R.color.oddColor));
        }
        // Return the completed view to render on screen
        return view;
    }
}