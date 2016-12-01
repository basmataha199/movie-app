package app.movie.android.com.movieapplication;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by basma on 21/10/16.
 */

public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<Movie> mThumbIds;
    private final String PATH_BASE_URI = "http://image.tmdb.org/t/p/";
    private final String THUMB_SIZE = "w342";

    public ImageAdapter(Context c, ArrayList<Movie> mThumbIds) {
        this.mContext = c;
        this.mThumbIds = new ArrayList<>(mThumbIds);
    }
    @Override
    public int getCount(){
        return mThumbIds.size();
    }
    //Not needed
    @Override
    public Object getItem(int position) { return null; }

    //Not needed
    @Override
    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter.
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        } else {
            imageView = (ImageView) convertView;
        }

        Picasso.with(mContext).load(
                PATH_BASE_URI + THUMB_SIZE + "/" +
                mThumbIds.get(position).getPosterPath()).
                into(imageView);
        return imageView;
    }

    public ArrayList<Movie> getMovies() { return mThumbIds; }
}