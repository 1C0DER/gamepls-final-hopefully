package uk.ac.rgu.gamepls;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

public class AppsAdapter extends ArrayAdapter<App> {
    public AppsAdapter(Context context, ArrayList<App> appsList) {
        super(context, 0, appsList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        App app = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_app, parent, false);
        }

        // Populate the views
        TextView appNameTextView = convertView.findViewById(R.id.app_name_tv);
        TextView usageDurationTextView = convertView.findViewById(R.id.usage_duration_tv);
        TextView usagePercentageTextView = convertView.findViewById(R.id.usage_perc_tv);
        ImageView appIconImageView = convertView.findViewById(R.id.icon_img);
        ProgressBar usageProgressBar = convertView.findViewById(R.id.progressBar);

        appNameTextView.setText(app.appName);
        usageDurationTextView.setText(app.usageDuration);
        usagePercentageTextView.setText(app.usagePercentage + "%");
        appIconImageView.setImageDrawable(app.appIcon);
        usageProgressBar.setProgress(app.usagePercentage);

        return convertView;
    }
}
