package app.movie.android.com.movieapplication;

/**
 * Created by basma on 25/11/16.
 */

public class Video {
    private final String VIDEO_TITLE, VIDEO_KEY;
    public Video(String video_name, String video_key) {
        this.VIDEO_TITLE = video_name;
        this.VIDEO_KEY = video_key;
    }

    public String getVIDEO_TITLE() {
        return VIDEO_TITLE;
    }

    public String getVIDEO_KEY() {
        return VIDEO_KEY;
    }
}
