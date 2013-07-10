/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.friedran.appengine.dashboard.gui;

import android.content.Context;
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
import android.view.animation.AnimationUtils;
import android.widget.*;
import com.friedran.appengine.dashboard.R;
import com.friedran.appengine.dashboard.client.AppEngineDashboardClient;

import java.io.InputStream;

public class DashboardLoadFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    public static final String CHART_URL_BACKGROUND_COLOR_SUFFIX = "&chf=bg,s,E8E8E8";
    public static final int CHART_HEIGHT_PIXELS = 240;
    public static final int CHART_MAX_WIDTH_PIXELS = 1000;
    public static final String KEY_TIME_ID = "KEY_TIME_ID";
    public static final String KEY_METRIC_TYPE_ID = "KEY_METRIC_TYPE_ID";
    private AppEngineDashboardClient mAppEngineClient;
    private String mApplicationId;
    private ChartAdapter mChartGridAdapter;
    private DisplayMetrics mDisplayMetrics;

    int mDisplayedTimeID;

    public DashboardLoadFragment(AppEngineDashboardClient client, String applicationID) {
        mAppEngineClient = client;
        mApplicationId = applicationID;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.load_fragment, container, false);

        setSpinnerWithItems(layout, R.array.load_time_options, R.id.load_chart_time_spinner);
        mDisplayedTimeID = 0;

        mDisplayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);

        mChartGridAdapter = new ChartAdapter(getActivity());
        GridView chartsGridView = (GridView) layout.findViewById(R.id.load_charts_grid);
        chartsGridView.setAdapter(mChartGridAdapter);

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
    }

    /**
     * Called when the time spinner is selected
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position != mDisplayedTimeID) {
            Log.i("DashboardLoadFragment", "Time option selected: " + mDisplayedTimeID + " ==> " + position);
            mDisplayedTimeID = position;
            mChartGridAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Do Nothing
    }

    private class ChartAdapter extends BaseAdapter {
        private Context mContext;
        private String[] mAppEngineMetrics;

        public ChartAdapter(Context c) {
            mContext = c;
            mAppEngineMetrics = getResources().getStringArray(R.array.load_metric_options);
        }

        // Disables highlighting items
        @Override
        public boolean areAllItemsEnabled()
        {
            return false;
        }

        // Disables highlighting items
        @Override
        public boolean isEnabled(int position)
        {
            return false;
        }

        @Override
        public int getCount() {
            return mAppEngineMetrics.length;
        }

        @Override
        public Object getItem(int position) {
            return mAppEngineMetrics[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View chartView;

            if (convertView == null) {
                chartView = inflater.inflate(R.layout.load_charts_grid_item, null);
            } else {
                chartView = convertView;
            }

            TextView textView = (TextView) chartView.findViewById(R.id.load_chart_title);
            textView.setText(mAppEngineMetrics[position]);

            if (shouldUpdateChartImage((Bundle) chartView.getTag(), position)) {
                // Load the image asynchronously, while displaying the progress animation
                ViewSwitcher switcher = (ViewSwitcher) chartView.findViewById(R.id.load_chart_switcher);
                switchChartToProgress(switcher);

                executeGetAndDisplayChart(chartView, mDisplayedTimeID, position);
            }

            return chartView;
        }

        private boolean shouldUpdateChartImage(Bundle chartDisplayedParameters, int currentMetricID) {
            return (chartDisplayedParameters == null) ||
                   (chartDisplayedParameters.getInt(KEY_TIME_ID) != mDisplayedTimeID) ||
                   (chartDisplayedParameters.getInt(KEY_METRIC_TYPE_ID) != currentMetricID);
        }
    }

    private void executeGetAndDisplayChart(final View chartView, final int selectedTimeWindow, final int metricTypeID) {
        mAppEngineClient.executeGetChartUrl(mApplicationId, metricTypeID, selectedTimeWindow,
                new AppEngineDashboardClient.PostExecuteCallback() {
                    @Override
                    public void run(Bundle result) {
                        if (!result.getBoolean(AppEngineDashboardClient.KEY_RESULT)) {
                            Log.e("DashboardLoadFragment", "GetChartURL has failed");
                            return;
                        }

                        String chartUrl = result.getString(AppEngineDashboardClient.KEY_CHART_URL);
                        chartUrl = chartUrl.replaceAll("chs=\\d+x\\d+",
                                String.format("chs=%sx%s", Math.min(mDisplayMetrics.widthPixels, CHART_MAX_WIDTH_PIXELS), CHART_HEIGHT_PIXELS));
                        chartUrl += CHART_URL_BACKGROUND_COLOR_SUFFIX;

                        Log.i("DashboardLoadFragment", String.format("Downloading chart (%s, %s) from: %s", selectedTimeWindow, metricTypeID, chartUrl));
                        new ChartDownloadTask(getActivity(), chartView, selectedTimeWindow, metricTypeID, chartUrl).execute();
                    }
                });
    }


    private class ChartDownloadTask extends AsyncTask<String, Void, Bitmap> {
        Context mContext;
        View mChartView;
        int mTimeWindowID;
        int mMetricTypeID;
        String mUrl;

        public ChartDownloadTask(Context context, View chartView, int timeWindowID, int metricTypeID, String url) {
            mContext = context;
            mChartView = chartView;
            mTimeWindowID = timeWindowID;
            mMetricTypeID = metricTypeID;
            mUrl = url;
        }

        protected Bitmap doInBackground(String... params) {
            Bitmap decodedBitmap = null;
            try {
                InputStream in = new java.net.URL(mUrl).openStream();
                decodedBitmap = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return decodedBitmap;
        }

        protected void onPostExecute(Bitmap result) {
            ImageView chartImageView = (ImageView) mChartView.findViewById(R.id.load_chart_image);
            chartImageView.setImageBitmap(result);

            ViewSwitcher chartSwitcherView = (ViewSwitcher) mChartView.findViewById(R.id.load_chart_switcher);
            switchChartToImage(chartSwitcherView);

            Bundle displayedParameters = new Bundle();
            displayedParameters.putInt(KEY_TIME_ID, mDisplayedTimeID);
            displayedParameters.putInt(KEY_METRIC_TYPE_ID, mMetricTypeID);
            mChartView.setTag(displayedParameters);
        }
    }

    private void switchChartToProgress(ViewSwitcher switcher) {
        if (switcher.getDisplayedChild() != 0) {
            switcher.showPrevious();
        }
    }

    private void switchChartToImage(ViewSwitcher switcher) {
        if (switcher.getDisplayedChild() != 1) {
            switcher.setAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.fadein));
            switcher.showNext();
        }
    }
}
