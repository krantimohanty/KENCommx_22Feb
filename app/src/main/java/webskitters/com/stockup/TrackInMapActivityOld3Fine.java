package webskitters.com.stockup;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import webskitters.com.stockup.LocationUtils.DataParser;
import webskitters.com.stockup.Utils.Constants;
import webskitters.com.stockup.Utils.Utils;

public class TrackInMapActivityOld3Fine extends AppCompatActivity {

    public static GoogleMap map;
    ImageView img_back;
    Utils utils;
    static float mapZoomLevel = 16;
    Timer timer;
    public Timer t = new Timer();

    MarkerOptions marker;
    Marker mkr;
    int totCount = 0;
    int callCount = 0;
    Polyline polylineFinal;
    Polyline polylineMain;

    //LatLng origin, dest;
    LatLng actualOrigin = new LatLng(22.5855143,88.4186604), actualDestination = new LatLng(22.5817479,88.333449);
    LatLng origin = new LatLng(22.590406, 88.413807), dest = new LatLng(22.562900, 88.396340);
    LatLng driverFrom = new LatLng(22.580133, 88.390646), driverTo = new LatLng(22.562900, 88.396340);
    LatLng driverFrom1 = new LatLng(22.576872, 88.412866), driverTo1 = new LatLng(22.557618, 88.412017);

    int countTimerChange = 0;

    LatLng DeliveryAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_in_map);
        utils=new Utils(this);

        initFields();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(t != null) {
            t.cancel();
            t = null;
        }
    }

    private void initFields() {
        //shPrefDeliverAddr = this.getSharedPreferences(Constants.strShPrefDelAddrName, Context.MODE_PRIVATE);
        img_back = (ImageView) findViewById(R.id.img_back);

        // This should be initialized
        //map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        Constants.flagMapPage = true;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        //map_hostel.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.getUiSettings().setAllGesturesEnabled(true);
        map.setMyLocationEnabled(true);
        map.setBuildingsEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);

        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (utils.isConnectionPossible()){
            //getDriverStartLocation();
            getDestination();
        }else{
            utils.displayAlert("Internet connection is not available, Turn it on and proceed.");
        }

        /*t.scheduleAtFixedRate(new TimerTask() {
                                  @Override
                                  public void run() {
                                      //getDriverLocation();
                                      // Getting URL to the Google Directions API
                                      String url;
                                      if (countTimerChange%2 == 0) {
                                          url = getUrl(driverFrom, driverTo);
                                      }
                                      else {
                                          url = getUrl(driverFrom1, driverTo1);
                                      }
                                      countTimerChange++;
                                      //Log.d("onMapClick", url.toString());
                                      FetchUrl FetchUrl = new FetchUrl("change");

                                      // Start downloading json data from Google Directions API
                                      FetchUrl.execute(url);
                                  }
                              }, 0,
                20000);*/


        // Getting URL to the Google Directions API
        String url = getUrl(actualOrigin, actualDestination);
        //Log.d("onMapClick", url.toString());
        FetchUrl FetchUrl = new FetchUrl("actual");

        // Start downloading json data from Google Directions API
        FetchUrl.execute(url);

        //new FetchUrl(url).execute();


    }


    private void getDriverLocation() {
        //Showing the progress dialog
        //final ProgressDialog pDialog = ProgressDialog.show(TrackInMapActivity.this, "Connecting...", "Please wait...", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.urlMain+ Constants.urlGetLocation,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        try {
                            JSONObject jObject = new JSONObject(s);
                            JSONObject obj_result = jObject.getJSONObject("result");
                            String resStat = obj_result.getString("error");
                            if (resStat.equalsIgnoreCase("false")) {
                                JSONObject objData = obj_result.getJSONObject("data");
                                String res_latitude = objData.getString("latitude");
                                String res_longitude = objData.getString("longitude");

                                processResp(res_latitude, res_longitude);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        finally {
                            //pDialog.dismiss();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Dismissing the progress dialog
                        //pDialog.dismiss();

                        //Showing toast
//                        Toast.makeText(getActivity(), volleyError.getMessage().toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                //String image = getStringImage(bim);
                Map<String, String> params = new Hashtable<String, String>();
                params.put("user_id", "190");
                return params;
            }
        };

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(TrackInMapActivityOld3Fine.this);

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void processResp(String res_latitude, String res_longitude) {
        //mapZoomLevel = map.getCameraPosition().zoom;
        /*if (mapZoomLevel>12){
            mapZoomLevel = map.getCameraPosition().zoom;
        }
        CameraPosition cameraPosition = new CameraPosition.Builder().target(
                new LatLng(Double.parseDouble(res_latitude), Double.parseDouble(res_longitude))).zoom(16).build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


        MarkerOptions marker = new MarkerOptions().position(new LatLng(Double.parseDouble(res_latitude), Double.parseDouble(res_longitude)))
                //.title(complete)
                //.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_icon_pin));
                .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("delivery_icon",50,60)))
                .flat(true);
        //map_restaurent.addMarker(marker);
        Marker mkr = map.addMarker(marker);*/


        if (callCount == 0) {
            origin = new LatLng(Double.parseDouble(res_latitude), Double.parseDouble(res_longitude));
            dest = new LatLng(Double.parseDouble(res_latitude), Double.parseDouble(res_longitude));
        }
        else {
            origin = dest;
            dest = new LatLng(Double.parseDouble(res_latitude), Double.parseDouble(res_longitude));
        }
        callCount++;

        marker = new MarkerOptions().position(new LatLng(Double.parseDouble(res_latitude), Double.parseDouble(res_longitude)))
                .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("map_icon_pin", 50, 60)));

        // Getting URL to the Google Directions API
        //String url = getUrl(origin, dest);
        String url = getUrl(new LatLng(Double.parseDouble(res_latitude), Double.parseDouble(res_longitude)), DeliveryAddress);
        //Log.d("onMapClick", url.toString());

        // It's type should be "change", currently doing as "actual" to match with IOS
        FetchUrl FetchUrl = new FetchUrl("actual");

        // Start downloading json data from Google Directions API
        FetchUrl.execute(url);
    }

    public Bitmap resizeMapIcons(String iconName, int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

    // Methods for Direction
    private String getUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Sensor enabled
        String sensor = "sensor=true";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }
    // Fetches data from url passed
    private class FetchUrl extends AsyncTask<String, Void, String> {

        String type;
        // it may be "actual" or "change"

        FetchUrl(String type){
            this.type = type;
        }

        @Override
        protected String doInBackground(String... url) {
            // For storing data from web service
            String data = "";
            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask(type);
            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        String type;
        // it may be "actual" or "change"

        ParserTask(String type){
            this.type = type;
        }


        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jObject = new JSONObject(jsonData[0]);
                //Log.d("ParserTask",jsonData[0].toString());
                DataParser parser = new DataParser();
                //Log.d("ParserTask", parser.toString());
                // Starts parsing data
                routes = parser.parse(jObject);
                //Log.d("ParserTask","Executing routes");
                //Log.d("ParserTask",routes.toString());
            } catch (Exception e) {
                //Log.d("ParserTask",e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;
            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();
                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }

                final Handler handler = new Handler();
                final ArrayList<LatLng> finalPoints = points;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        {
                            if (totCount < finalPoints.size()) {
                                try {
                                    marker = new MarkerOptions().position(finalPoints.get(totCount))
                                            .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("map_icon_pin", 50, 60)));
                                } catch (Exception e) {
                                    Log.d("exception", e.toString());
                                }
                                double rotAngle = 0;
                                double dis = 0;
                                if (totCount > 0) {
                                    LatLng posPrev = finalPoints.get(totCount - 1);
                                    LatLng posCurr = finalPoints.get(totCount);
                                    dis = distance(posPrev.latitude, posPrev.longitude, posCurr.latitude, posPrev.longitude);
                                    //rotAngle = bearingBetweenLocations(posPrev.latitude, posPrev.longitude, posCurr.latitude, posPrev.longitude);
                                    //mkr = mMap.addMarker(marker);
                                    //marker.rotation((float) rotAngle);
                                    //mkr.setRotation((float) rotAngle);
                                    mkr.setPosition(posCurr);

                                    if (posPrev.latitude == posCurr.latitude && posCurr.latitude == posCurr.longitude) {
                                        handler.removeCallbacksAndMessages(this);
                                    }
                                } else {
                                    mkr = map.addMarker(marker);
                                }
                                if (type.equalsIgnoreCase("change"))
                                if (totCount == 0) {
                                    CameraPosition cameraPosition = new CameraPosition.Builder().target(finalPoints.get(totCount)).zoom(16).build();
                                    map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                } else {
                                    CameraPosition cameraPosition = new CameraPosition.Builder().target(finalPoints.get(totCount)).zoom(16).build();
                                    map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                }

							/*mMap.addMarker(new MarkerOptions().position(finalPoints.get(totCount))
									.icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("car_pin", 50, 60))));*/
                                if (dis > 5) {
                                    handler.postDelayed(this, 500);
                                } else {
                                    handler.postDelayed(this, 50);
                                }
                                //marker.setVisible(true);
                                if (totCount < finalPoints.size() - 1)
                                    totCount++;
                                else {
                                    handler.removeCallbacksAndMessages(this);
                                }
                            }
                        }
                    }
                });


                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                if (type.equalsIgnoreCase("actual")) {
                    lineOptions.color(Color.RED);
                }
                else {
                    lineOptions.color(Color.GREEN);
                }
                //Log.d("onPostExecute","onPostExecute lineoptions decoded");
            }
            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null) {
                if (type.equalsIgnoreCase("actual")) {
                    polylineMain = map.addPolyline(lineOptions);
                }
                else {
                    if (polylineFinal!=null) {
                        polylineFinal.remove();
                    }
                    polylineFinal = map.addPolyline(lineOptions);
                }
                /*CameraPosition cameraPosition = new CameraPosition.Builder().target(
                        new LatLng(Double.parseDouble(Constants.strCurLat), Double.parseDouble(Constants.strCurLong))).zoom(16).build();
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));*/
            }
            else {
                Log.d("onPostExecute","without Polylines drawn");
            }
        }
    }

    /**
     * A method to download json data from url
     */
    private static String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();
            // Connecting to url
            urlConnection.connect();
            // Reading data from url
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            Log.d("downloadUrl", data.toString());
            br.close();
        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // End of Methods for direction

    public static double distance(double lat_a, double lng_a, double lat_b, double lng_b)
    {
        double earthRadius = 3958.75;
        double latDiff = Math.toRadians(lat_b-lat_a);
        double lngDiff = Math.toRadians(lng_b-lng_a);
        double a = Math.sin(latDiff /2) * Math.sin(latDiff /2) +
                Math.cos(Math.toRadians(lat_a)) * Math.cos(Math.toRadians(lat_b)) *
                        Math.sin(lngDiff /2) * Math.sin(lngDiff /2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double distance = earthRadius * c;

        int meterConversion = 1609;

        return distance * meterConversion;
    }



    public static String str_address = "";
    public static Double destLat = 0.0;
    public static Double destLong = 0.0;

    private void getDestination() {
        //Showing the progress dialog
        final ProgressDialog pDialog = ProgressDialog.show(TrackInMapActivityOld3Fine.this, "Loading...", "Please Wait...", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Constants.urlMainStockUp + Constants.urlGetDelData,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        try {
                            JSONObject jObject = new JSONObject(s);
                            str_address = jObject.getString("address");
                            destLat = Double.parseDouble(jObject.getString("latitude"));
                            destLong = Double.parseDouble(jObject.getString("longitude"));
                            DeliveryAddress = new LatLng(destLat, destLong);

                            marker = new MarkerOptions().position(new LatLng(destLat, destLong))
                                    .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("map_icon_pin", 50, 60)));
                            t.scheduleAtFixedRate(new TimerTask() {
                                                      @Override
                                                      public void run() {
                                                          getDriverLocation();
                                                      }
                                                  }, 0,
                                    10000);
                            /*toEdit = shPrefDeliverDtl.edit();
                            toEdit.putString(Constants.strShPrefDelLat, destLat+"");
                            toEdit.putString(Constants.strShPrefDelLong, destLong+"");
                            toEdit.putString(Constants.strShPrefDelAddr, str_address);
                            toEdit.commit();*/
                            /*if (resStat.equalsIgnoreCase("false")) {
                                JSONObject objData = obj_result.getJSONObject("data");
                                String res_latitude = objData.getString("latitude");
                                String res_longitude = objData.getString("longitude");

                                processResp(res_latitude, res_longitude);
                            }*/
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        finally {
                            pDialog.dismiss();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Dismissing the progress dialog
                        pDialog.dismiss();

                        //Showing toast
//                        Toast.makeText(getActivity(), volleyError.getMessage().toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                //String image = getStringImage(bim);
                Map<String, String> params = new Hashtable<String, String>();
                return params;
            }
        };

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(TrackInMapActivityOld3Fine.this);

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }


}
