package app.movie.android.com.movieapplication;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by basma on 24/11/16.
 */

public class ReviewsAdapter extends ArrayAdapter<Review> {
    private final Context context;
    private final ArrayList<Review> reviews;

    public ReviewsAdapter(Context c,
                          ArrayList<Review> reviews) {
        super(c, -1, reviews);
        context = c;
        this.reviews = reviews;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //get a layout inflater of  the calling activity
        View newView = convertView;

        if (newView == null) {
            LayoutInflater inflater;
            inflater = LayoutInflater.from(context);
            newView = inflater.inflate(R.layout.list_item_details, null);
        }

        Review review = reviews.get(position);

        if (review != null) {
            TextView review_title = (TextView) newView.findViewById(R.id.review_title);
            TextView review_content = (TextView) newView.findViewById(R.id.review_content);

            if (review_title != null) {
                review_title.setText(review.getREVIEW_TITLE());
            }

            if (review_content != null) {
                review_content.setText(review.getREVIEW_CONTENT());
                Log.d("","*********** Content : " + review.getREVIEW_CONTENT());
            }
        }

        return newView;
    }
}
