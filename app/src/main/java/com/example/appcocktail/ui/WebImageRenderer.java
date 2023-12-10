package com.example.appcocktail.ui;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import androidx.core.content.res.ResourcesCompat;
import com.example.appcocktail.R;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class WebImageRenderer implements Runnable{
    private final URL url;
    private final ImageView target;
    private final Activity activity;

    /**
     * Renders image given in url. If image is not found, then default image is rendered.
     *
     * @param activity current activity
     * @param image_url web url containing an image
     * @param target ImageView where image is to be rendered
     * @throws MalformedURLException thrown if url syntax is invalid
     */
    public WebImageRenderer(Activity activity, String image_url, ImageView target) throws MalformedURLException {
        this.activity = activity;
        this.url = new URL(image_url+"");
        this.target = target;
    }

    @Override
    public void run() {
        Drawable d;
        try {

            InputStream is = (InputStream) this.url.getContent();
            d = Drawable.createFromStream(is, "cocktailDB");
            is.close();
        }catch (Exception e){
            d = ResourcesCompat.getDrawable(activity.getResources(), R.drawable.no_image_sm, null);
        }
        // altering views on UI thread
        Drawable finalDrawable = d;
        activity.runOnUiThread(() -> target.setImageDrawable(finalDrawable));

    }
}
