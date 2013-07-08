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
import com.friedran.appengine.dashboard.client.AppEngineDashboardClient;

import java.io.InputStream;

public class DashboardLoadFragment extends Fragment {

    public static final String CHART_URL_BACKGROUND_COLOR_SUFFIX = "&chf=bg,s,E8E8E8";

    private AppEngineDashboardClient mAppEngineClient;
    private String mApplicationId;

    public DashboardLoadFragment(AppEngineDashboardClient client, String applicationID) {
        mAppEngineClient = client;
        mApplicationId = applicationID;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dashboard_load_fragment, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Get the chart URL and render it asynchronously
        mAppEngineClient.executeGetChartUrl(mApplicationId, 0, 0, new AppEngineDashboardClient.PostExecuteCallback() {
            @Override
            public void run(Bundle result) {
                if (!result.getBoolean(AppEngineDashboardClient.KEY_RESULT)) {
                    // TODO: Display an error message
                    Log.e("DashboardLoadFragment", "GetChartURL has failed");
                    return;
                }

                String chartUrl = result.getString(AppEngineDashboardClient.KEY_CHART_URL);
                new DownloadImageTask((ImageView) getActivity().findViewById(R.id.load_chart_image))
                        .execute(chartUrl + CHART_URL_BACKGROUND_COLOR_SUFFIX);
            }
        });
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
