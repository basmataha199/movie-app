package app.movie.android.com.movieapplication;

/**
 * Created by basma on 24/11/16.
 */

public class Review {

    private final String REVIEW_TITLE, REVIEW_CONTENT;

    public Review(String detail_title, String detail_content) {
        this.REVIEW_TITLE = detail_title;
        this.REVIEW_CONTENT = detail_content;
    }

    public String getREVIEW_TITLE() {
        return REVIEW_TITLE;
    }

    public String getREVIEW_CONTENT() {
        return REVIEW_CONTENT;
    }
}
