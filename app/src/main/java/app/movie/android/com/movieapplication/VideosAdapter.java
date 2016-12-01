package app.movie.android.com.movieapplication;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by basma on 25/11/16.
 */

public class VideosAdapter extends ArrayAdapter<Video> implements View.OnClickListener{
    private final Context context;
    private final ArrayList<Video> videos;
    private Video videoSelected;

    public VideosAdapter(Context c,
                          ArrayList<Video> videos) {
        super(c, -1, videos);
        context = c;
        this.videos = videos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //get a layout inflater of  the calling activity
        View newView = convertView;

        if (newView == null) {
            LayoutInflater inflater;
            inflater = LayoutInflater.from(context);
            newView = inflater.inflate(R.layout.list_item_buttons, null);
        }

        videoSelected = videos.get(position);

        if (videoSelected != null) {
            TextView video_title = (TextView) newView.findViewById(R.id.trailer_name);

            if (video_title != null) {
                video_title.setText(videoSelected.getVIDEO_TITLE());
            }

            ImageButton button = (ImageButton) newView
                    .findViewById(R.id.trailer_video_btn);
            button.setTag(position);
            button.setOnClickListener(this);
        }

        return newView;
    }

    @Override
    public void onClick(View v) {
        // make an explicit intent to a player app.
        Intent videoPlayer = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://www.youtube.com/watch?v="+
                        videoSelected.getVIDEO_KEY()));
        context.startActivity(videoPlayer);
    }
}
