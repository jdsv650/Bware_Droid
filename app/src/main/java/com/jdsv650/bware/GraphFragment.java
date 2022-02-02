package com.jdsv650.bware;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

import org.json.JSONArray;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;
import static com.jdsv650.bware.Constants.PREFS_NAME;


/**
 * A simple {@link Fragment} subclass.
 */
public class GraphFragment extends Fragment {

    private SharedPreferences preferences;
    public static final MediaType MEDIA_TYPE = MediaType.parse("application/json");
    OkHttpClient client;

    String[] states = { "", "", "", "", ""};
    Integer[] count = { 0, 0, 0, 0, 0};

    GraphView graph;

    public GraphFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        client = new OkHttpClient.Builder()
                .connectTimeout(Constants.timeout, TimeUnit.SECONDS) // defaults 10 seconds - not enough if
                .writeTimeout(Constants.timeout, TimeUnit.SECONDS)   // api hasn't been hit recently
                .readTimeout(Constants.timeout, TimeUnit.SECONDS)
                .build();

        // get shared prefs
        preferences = getActivity().getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_graph, container, false);

        graph = (GraphView) view.findViewById(R.id.graph);

        InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);

        View v = getActivity().getCurrentFocus();

        if (v != null)
        {
            inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }

        clearStatesAndCount();
        getTopFive();

        return view;
    }

    private void drawGraph()
    {
        if (graph == null) { return; }

        BarGraphSeries<DataPoint> series = new BarGraphSeries<>(new DataPoint[] {
                new DataPoint(0,0),
                new DataPoint(1, count[0]),
                new DataPoint(2, count[1]),
                new DataPoint(3, count[2]),
                new DataPoint(4, count[3]),
                new DataPoint(5, count[4]),
                new DataPoint(6, 0)
        });

        series.setSpacing(20);

        series.setDrawValuesOnTop(true);
        series.setColor(R.color.colorCadmiumOrange);
        series.setValuesOnTopColor(R.color.colorCadmiumOrange);
        series.setAnimated(true);

        graph.getGridLabelRenderer().setGridColor(R.color.colorWhite);
        graph.getGridLabelRenderer().setHorizontalAxisTitle("State");

        // use static /state/ labels for horizontal labels
        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph);
        staticLabelsFormatter.setHorizontalLabels(new String[] {"", states[0], states[1], states[2], states[3], states[4], ""});
        graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);

        graph.addSeries(series);

    }


    // call api to get bridge count for top 5
    private void getTopFive()
    {
        String urlAsString = Constants.baseUrlAsString + "/api/Bridge/GetCountForStates";

        String token = preferences.getString("access_token","");  // get token

        if (token != "")  // token stored
        {
            String urlEncoded = Uri.encode(urlAsString);

            Request request = new Request.Builder()
                    .url(urlAsString)
                    .addHeader("Authorization", "Bearer " + token)
                    .build();

            OkHttpClient trustAllclient = Helper.trustAllSslClient(client);

            trustAllclient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    String mMessage = e.getMessage().toString();
                    Log.w("failure Response", mMessage);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "Request failed, Please check connection and try again", Toast.LENGTH_SHORT).show();

                        }
                    });
                    //call.cancel();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    final String mMessage = response.body().string();
                    Log.w("success Response", mMessage);

                    if (response.isSuccessful()){
                        try {
                            final JSONArray jsonArray = new JSONArray(mMessage);

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    try
                                    {
                                        // [{"State":"NC","NumberOfBridges":3545},{"State":"PA","NumberOfBridges":2530},{"State":"NY","NumberOfBridges":2325},{"State":"LA","NumberOfBridges":496},{"State":"MN","NumberOfBridges":385}]
                                        //Toast.makeText(getActivity(), mMessage, Toast.LENGTH_LONG).show();

                                        for (Integer i = 0; i < jsonArray.length(); i++) {
                                            try {
                                                states[i] = jsonArray.getJSONObject(i).optString("State", "");
                                                count[i] = jsonArray.getJSONObject(i).optInt("NumberOfBridges", 0);
                                            }
                                            catch (Exception ex)
                                            {

                                            }
                                        }

                                        drawGraph();

                                    }
                                    catch (Exception ex)
                                    {
                                        Toast.makeText(getActivity(), "Error retrieving info please try again", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        } catch (Exception e){
                            e.printStackTrace();
                        }

                    } // end response success
                    else   // unsuccessful response
                    {
                        if (response.code() == 400 || response.code() == 401) // received a response from server
                        {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run()
                                {
                                    Toast.makeText(getActivity(), "Please verify username and password", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        else
                        {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run()
                                {
                                    Toast.makeText(getActivity(), "Network related error. Please try again", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        return;
                    }

                }
            });

        }
        else  // no token found
        {
            Toast.makeText(getActivity(), "Please verify username and password", Toast.LENGTH_SHORT).show();

        }

    }

    private void clearStatesAndCount()
    {
        for (int i=0; i< states.length; i++)
        {
            states[i] = "";
        }

        for (int i=0; i< count.length; i++)
        {
            count[i] = 0;
        }
    }

}
