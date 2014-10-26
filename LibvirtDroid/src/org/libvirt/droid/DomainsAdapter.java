package org.libvirt.droid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DomainsAdapter extends ArrayAdapter<DomainProxy> {

    public DomainsAdapter(Context context, DomainProxy[] domains) {
        super(context, android.R.layout.activity_list_item, domains);
    }

    private static class ItemHolder {
        ImageView image;
        TextView name;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        ItemHolder holder;
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = mInflater.inflate(android.R.layout.activity_list_item, null);

            holder = new ItemHolder();
            holder.image = (ImageView) v.findViewById(android.R.id.icon);
            holder.name = (TextView) v.findViewById(android.R.id.text1);

            v.setTag(holder);
        } else {
            holder = (ItemHolder) v.getTag();
        }

        DomainProxy dom = getItem(position);

        holder.name.setText(dom.getName());

        int iconId = R.drawable.domain_shutoff;
        switch (dom.getInfo().state) {
        case VIR_DOMAIN_RUNNING:
            iconId = R.drawable.domain_running;
            break;
        case VIR_DOMAIN_PAUSED:
            iconId = R.drawable.domain_paused;
            break;
        default:
        }
        holder.image.setImageResource(iconId);

        return v;
    }
}
