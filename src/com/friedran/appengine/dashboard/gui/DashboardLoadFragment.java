package com.friedran.appengine.dashboard.gui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.friedran.appengine.dashboard.R;
import com.friedran.appengine.dashboard.client.AppEngineDashboardClient;

import java.io.InputStream;

public class DashboardLoadFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    public static final String CHART_URL_BACKGROUND_COLOR_SUFFIX = "&chf=bg,s,E8E8E8";
    public static final int CHART_HEIGHT_PIXELS = 240;
    public static final int CHART_MAX_WIDTH_PIXELS = 1000;
    private AppEngineDashboardClient mAppEngineClient;
    private String mApplicationId;
    private int mDisplayedMetricTypeID;
    private int mDisplayedTimeWindowID;
    private Spinner mMetricSpinner;
    private Spinner mTimeSpinner;
    private DisplayMetrics mMetrics;

    public DashboardLoadFragment(AppEngineDashboardClient client, String applicationID) {
        mAppEngineClient = client;
        mApplicationId = applicationID;
        resetDisplayedOptions();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.dashboard_load_fragment, container, false);

        mMetricSpinner = setSpinnerWithItems(layout, R.array.load_metric_options, R.id.load_chart_metric_spinner);
        mTimeSpinner = setSpinnerWithItems(layout, R.array.load_time_options, R.id.load_chart_time_spinner);

        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);

        return layout;
    }

    private Spinner setSpinnerWithItems(LinearLayout layout, int optionsListResourceID, int spinnerResourceID) {
        Spinner spinner = (Spinner) layout.findViewById(spinnerResourceID);

        // Set options list
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                optionsListResourceID, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Set listener
        spinner.setOnItemSelectedListener(this);

        return spinner;
    }

    @Override
    public void onResume() {
        super.onResume();

        executeLoadingChartIfChanged(true);
    }

    /**
     * Called when one of the spinners is changed
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        executeLoadingChartIfChanged(false);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Do Nothing
    }

    /**
     * Gets and renders the chart URL asynchronously
     */
    private void executeLoadingChartIfChanged(boolean forceLoad) {
        final int selectedMetricType = mMetricSpinner.getSelectedItemPosition();
        final int selectedTimeWindow = mTimeSpinner.getSelectedItemPosition();

        // If the options haven't changed then don't reload the chart (unless forced)
        if (!forceLoad && selectedMetricType == mDisplayedMetricTypeID && selectedTimeWindow == mDisplayedTimeWindowID)
            return;

        mDisplayedMetricTypeID = selectedMetricType;
        mDisplayedTimeWindowID = selectedTimeWindow;

        mAppEngineClient.executeGetChartUrl(mApplicationId, selectedMetricType, selectedTimeWindow,
                new AppEngineDashboardClient.PostExecuteCallback() {
                    @Override
                    public void run(Bundle result) {
                        if (!result.getBoolean(AppEngineDashboardClient.KEY_RESULT)) {
                            // TODO: Display an error message
                            Log.e("DashboardLoadFragment", "GetChartURL has failed");
                            resetDisplayedOptions();
                            return;
                        }

                        String chartUrl = result.getString(AppEngineDashboardClient.KEY_CHART_URL);
                        chartUrl = chartUrl.replaceAll("chs=\\d+x\\d+",
                                String.format("chs=%sx%s", Math.min(mMetrics.widthPixels, CHART_MAX_WIDTH_PIXELS), CHART_HEIGHT_PIXELS));
                        chartUrl += CHART_URL_BACKGROUND_COLOR_SUFFIX;
                        Log.i("DashboardLoadFragment", "Downloading chart from: " + chartUrl);

                        new DownloadImageTask((ImageView) getActivity().findViewById(R.id.load_chart_image)).execute(chartUrl);
                    }
                });
    }

    private void resetDisplayedOptions() {
        mDisplayedMetricTypeID = -1;
        mDisplayedTimeWindowID = -1;
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
                resetDisplayedOptions();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
