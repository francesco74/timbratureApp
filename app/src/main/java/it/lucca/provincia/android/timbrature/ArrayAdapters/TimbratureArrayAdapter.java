package it.lucca.provincia.android.timbrature.ArrayAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import it.lucca.provincia.android.timbrature.Models.Timbratura;
import it.lucca.provincia.android.timbrature.R;

public class TimbratureArrayAdapter extends ArrayAdapter<Timbratura> {
    private static class ViewHolder {
        private TextView lettore, tipo, orario, causale;
        private LinearLayout linearLayout;
    }

    Context context;

    public TimbratureArrayAdapter(Context context, ArrayList<Timbratura> timbratura) {
        super(context, 0, timbratura);

        this.context = context;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        // Get the data item for this position
        Timbratura timbratura = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (view == null) {
            // If there's no view to re-use, inflate a brand new view for row
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            view = inflater.inflate(R.layout.timbratura, parent, false);
            viewHolder.orario = (TextView) view.findViewById(R.id.tvOrario);
            viewHolder.tipo = (TextView) view.findViewById(R.id.tvTipo);
            viewHolder.causale = (TextView) view.findViewById(R.id.tvCausale);
            viewHolder.lettore = (TextView) view.findViewById(R.id.tvLettore);
            viewHolder.linearLayout = (LinearLayout) view.findViewById(R.id.llTimbratura);
            // Cache the viewHolder object inside the fresh view
            view.setTag(viewHolder);
        } else {
            // View is being recycled, retrieve the viewHolder object from tag
            viewHolder = (ViewHolder) view.getTag();
        }
        // Populate the data from the data object via the viewHolder object
        // into the template view.
        viewHolder.causale.setText(timbratura.getCausale());
        viewHolder.orario.setText(timbratura.getOrario());
        viewHolder.tipo.setText("(" + timbratura.getTipoTimbratura() + ")");
        viewHolder.lettore.setText(timbratura.getLettore());

        if ((position % 2) == 0) {
            viewHolder.linearLayout.setBackgroundColor(context.getResources().getColor(R.color.evenColor));
        } else {
            viewHolder.linearLayout.setBackgroundColor(context.getResources().getColor(R.color.oddColor));
        }
        // Return the completed view to render on screen
        return view;
    }
}