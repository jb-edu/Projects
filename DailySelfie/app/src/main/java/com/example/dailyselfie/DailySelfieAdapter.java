package com.example.dailyselfie;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;


/**
 * Created by jb-edu on 15-06-23.
 */
public class DailySelfieAdapter extends ArrayAdapter {

    private Context mContext;
    private static final String FILE_REF_PREFIX = "file://";

    public DailySelfieAdapter(Context context) {
        super(context, R.layout.list_item);
        this.mContext = context;
    }

    /**
     * This method implements the ViewHolder pattern as described at:
     *      http://developer.android.com/training/improving-layouts/smooth-scrolling.html#ViewHolder
     *
     * "Picasso" is used to automatically handle the re-sizing and caching of Bitmap thumbnails.
     * I.e. it will appropriately size a thumbnail from the original source file to fit the
     * ImageView (reducing memory consumption) and maintain a cache of thumbnails (to reduce
     * re-loads) while freeing up memory as needed to ensure integrity of the application.
     *
     * For more information on Picasso, please visit: http://square.github.io/picasso/
     */
    @Override
    public View getView (int position, View convertView, ViewGroup parent) {

        final int pos = position;
        final ViewHolder holder;

        if (convertView == null) {
            convertView =
                    LayoutInflater.from(getContext()).inflate(R.layout.list_item, null);
            holder = new ViewHolder();
            holder.image = (ImageView) convertView.findViewById(R.id.thumbnail_img);
            holder.text = (TextView) convertView.findViewById(R.id.thumbnail_text);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.check);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        DailySelfie selfie = (DailySelfie) getItem(position);
        holder.checkBox.setOnCheckedChangeListener(null);

        // Thumbnail sizing, caching, loading into ImageView, etc., is handled here by Picasso
        Picasso
                .with(mContext)
                .load(FILE_REF_PREFIX + selfie.getImagePath())
                .fit()
                .centerCrop()
                .placeholder(R.drawable.thumbnail_placeholder)
                .error(R.drawable.thumbnail_error)
                .noFade()
                .into(holder.image);

        holder.text.setText(selfie.getDate().toString());
        holder.checkBox.setChecked(selfie.isChecked());
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton
                .OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ((DailySelfie)getItem(pos)).setChecked(isChecked);
            }
        });

        convertView.setTag(holder);

        return convertView;
    }

    private static class ViewHolder {
        public ImageView image;
        public TextView text;
        public CheckBox checkBox;
    }

}
