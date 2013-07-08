package com.friedran.appengine.dashboard.gui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.friedran.appengine.dashboard.R;

import java.io.InputStream;

public class DashboardLoadFragment extends Fragment {

    public static final String CHART_URL_BACKGROUND_COLOR_SUFFIX = "&chf=bg,s,E8E8E8";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dashboard_load_fragment, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        String imageUrl = "https://www.google.com/chart?chco=0077cc&chd=e:AACFEKGQIVKbMgOlQrSwU2W7ZBbGdLfRhWjclhnmpsrxt3v8yC0H2M4S6X8d,AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAQAAAyqAAAAAAAAAAAAAAAAAA&chg=0,20.00,1,2&chls=2,0,0&chm=R,7f7f7f,0,0.977,0.979|R,7f7f7f,0,0.814,0.816|R,7f7f7f,0,0.651,0.653|R,7f7f7f,0,0.489,0.490|R,7f7f7f,0,0.326,0.327|R,7f7f7f,0,0.163,0.164|R,7f7f7f,0,0.000,0.001|B,eaf0f4,0,0,0&chs=750x185&cht=lxy&chxl=0%3A%7Cnow%7C-5m%7C-10m%7C-15m%7C-20m%7C-25m%7C-30m%7C1%3A%7C0.080%7C0.160%7C0.240%7C0.320%7C0.400&chxp=0,97.7,81.4,65.1,48.9,32.6,16.3,0.0|1,20.0,40.0,60.0,80.0,100.0&chxs=&chxt=x,y";

        new DownloadImageTask((ImageView) getActivity().findViewById(R.id.load_chart_image))
                .execute(imageUrl + CHART_URL_BACKGROUND_COLOR_SUFFIX);
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
