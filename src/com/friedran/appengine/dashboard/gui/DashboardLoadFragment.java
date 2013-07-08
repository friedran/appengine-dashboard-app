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
import android.widget.*;
import com.friedran.appengine.dashboard.R;
import com.friedran.appengine.dashboard.client.AppEngineDashboardClient;

import java.io.InputStream;

public class DashboardLoadFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    public static final String CHART_URL_BACKGROUND_COLOR_SUFFIX = "&chf=bg,s,E8E8E8";
    private AppEngineDashboardClient mAppEngineClient;
    private String mApplicationId;
    private int mDisplayedMetricTypeID;
    private int mDisplayedTimeWindowID;

    private Spinner mMetricSpinner;
    private Spinner mTimeSpinner;

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

        executeLoadingChartIfChanged();
    }

    /**
     * Called when one of the spinners is changed
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        executeLoadingChartIfChanged();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Do Nothing
    }

    /**
     * Gets and renders the chart URL asynchronously
     */
    private void executeLoadingChartIfChanged() {
        final int selectedMetricType = mMetricSpinner.getSelectedItemPosition();
        final int selectedTimeWindow = mTimeSpinner.getSelectedItemPosition();

        if (selectedMetricType == mDisplayedMetricTypeID && selectedTimeWindow == mDisplayedTimeWindowID)
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
                resetDisplayedOptions();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    private void resetDisplayedOptions() {
        mDisplayedMetricTypeID = -1;
        mDisplayedTimeWindowID = -1;
    }
}
