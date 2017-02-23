package webskitters.com.stockup;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.appsflyer.AppsFlyerLib;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.VisibleRegion;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import webskitters.com.stockup.LocationUtils.LocationProvider;
import webskitters.com.stockup.Utils.Constants;
import webskitters.com.stockup.Utils.ServiceHandler;
import webskitters.com.stockup.Utils.Utils;
import webskitters.com.stockup.model.GetAllHubRequest;
import webskitters.com.stockup.model.ServiceTaxRequest;
import webskitters.com.stockup.retrofit_call.RestCallback;
import webskitters.com.stockup.retrofit_call.RestService;

//import com.appsflyer.AppsFlyerLib;

public class MapActivity extends AppCompatActivity /*implements OnMapReadyCallback*/ {

    ImageView img_back, img_cur_loc, btn_get_position, img_cross;
    TextView txt_done;
    //EditText et_moved_addr;
    AutoCompleteTextView et_moved_addr;

    public static GoogleMap map;
    //MapFragment map;
    //MapView map;
    LocationManager manager;
    private LocationProvider mLocationProvider;

    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    private static final int PERMISSION_REQUEST_CODE_LOCATION = 1;

    Double latitude = 0.0, longitude = 0.0;
    static float mapZoomLevel = 16;
    String movedLatitude = "", movedLongitude = "";
    String strAddr = "";
    RestService restService;
    Utils utils;

    SharedPreferences shPrefDeliverAddr;
    SharedPreferences.Editor toEdit;
    String forAct = "", lName, shipingAdd, bilingAdd, forshipingbiling;
    private TextView txt_clear;

    Boolean callPlaceApi = false;

    private static final String LOG_TAG = "Google Places Autocomplete";
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";
    //private static final String API_KEY = "AIzaSyDw9qLvwHgJQs3UP0nDduVzwd5uJ0DMmaw";      // working but wrong place
    //Live Credential for Place API Server Key//////////////////
    private static final String API_KEY = "AIzaSyA0G5QyYfannmvdBmirUbPG7kgwh9E84NQ";         //working
    //Live Credential for Place API//////////////////
    //private static final String API_KEY = "AIzaSyAghcTcRdGe7hPCHJ5wPFNP-tJ7dr_XiRo";

    static String strPlaceName;
    static String strPlaceId;
    public static ArrayList placeDetailName;
    public static ArrayList placeDetailId;
    static final String KEY_description = "description";
    static final String KEY_place_id = "place_id";
    String strSearchId = "";

    JSONObject resultSearchPlaceDetail = null;
    JSONObject objLocation = null;
    JSONObject objGeometry = null;
    static final String KEY_RESULT1 = "result";
    Long getSysTime;

    public static ArrayList<HashMap<String, String>> listHub = new ArrayList<HashMap<String, String>>();
    public static String KEY_title = "title";
    public static String KEY_address = "address";
    public static String KEY_latitude = "latitude";
    public static String KEY_longitude = "longitude";
    public static String KEY_range = "range";
    private SharedPreferences sharedPreferenceUser;
    String customer_id="";
    TextView txt_header;
    ProgressDialog pDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_map);
        sharedPreferenceUser=getSharedPreferences(Constants.strShPrefUserPrefName, Context.MODE_PRIVATE);
        customer_id=sharedPreferenceUser.getString(Constants.strShPrefUserId, "");
        Bundle extras = getIntent().getExtras();
        utils = new Utils(MapActivity.this);
        restService=new RestService(this);
        if (extras != null) {
            forAct = extras.getString("for");
        }
        getSysTime = System.currentTimeMillis();

        // Track Data : Add to activities where tracking needed
        AppsFlyerLib.getInstance().sendDeepLinkData(this);
        
        if (utils.isConnectionPossible()){
            getAllHub();
            //getAllHubStatic();
        }

        initFields();
    }

    private void getAllHubStatic(){
        String response="{\"status\":200,\"success\":1,\"error_msg\":\"\",\"data\":{\"hub_location\":[{\"title\":\"Riverside Bryanston \",\"address\":\"317 Bryanston Dr, Sandton\",\"latitude\":\"-26.058740\",\"longitude\":\"28.046890\",\"range\":\"6\"},{\"title\":\"Grayston Shopping coverage\",\"address\":\"Corner Grayston Drive and Helen Road, Strathavon, Sandton, Greater Johannesburg, Gauteng, South Africa\\r\\n\",\"latitude\":\"-26.100900\",\"longitude\":\"28.066466\",\"range\":\"5\"},{\"title\":\"Morningside Shopping Centre\",\"address\":\"223 Rivonia Rd & Outspan Rd, Morningside, Johannesburg, 2196\",\"latitude\":\"-26.083649\",\"longitude\":\"28.060831\",\"range\":\"5\"},{\"title\":\"Nicolway Bryanston\",\"address\":\"Corner William Nicol Drive and Wedgewood link, Bryanston, Sandton, 2191\",\"latitude\":\"-26.051175\",\"longitude\":\"28.021097\",\"range\":\"5\"},{\"title\":\"Morning Glen Woodmead coverage\",\"address\":\"Kelvin Dr & Bowling Avenue, Morningside Manor, Johannesburg, 2052\",\"latitude\":\"-26.066921\",\"longitude\":\"28.075529\",\"range\":\"5\"},{\"title\":\"Rosebank Mall\",\"address\":\"12 Baker St, Johannesburg, 2196\\r\\n\",\"latitude\":\"-26.148495\",\"longitude\":\"28.039992\",\"range\":\"5\"},{\"title\":\"Ferndale Village\",\"address\":\"413 Pine Ave, Randburg, 2194\",\"latitude\":\"-26.0814\",\"longitude\":\"27.9973\",\"range\":\"5\"},{\"title\":\"Broadacres Shopping centre\",\"address\":\"164 Blenheim Rd, Sandton, 2055\\r\\n\",\"latitude\":\"-26.001807\",\"longitude\":\"27.981296\",\"range\":\"5\"},{\"title\":\"Cedar Square Fourways coverage\",\"address\":\"Cedar Rd, Sandton, 2068\\r\\n\",\"latitude\":\"-26.008645\",\"longitude\":\"27.991747\",\"range\":\"5\"},{\"title\":\"Dainfern Square coverage\",\"address\":\"Broadacres Dr, Midrand\\r\\n\",\"latitude\":\"-25.991425\",\"longitude\":\"28.008943\",\"range\":\"5\"},{\"title\":\"Lonehill Shopping Centre\",\"address\":\"22 Lonehill Blvd, Sandton, 2062\\r\\n\",\"latitude\":\"-26.013690\",\"longitude\":\"28.027000\",\"range\":\"5\"},{\"title\":\"Benmore Gardens Shopping Centre\",\"address\":\"Grayston Dr & Benmore Rd, Johannesburg, 2196\\r\\n\",\"latitude\":\"-26.100852\",\"longitude\":\"28.050624\",\"range\":\"6\"},{\"title\":\"Sunninghill Village\",\"address\":\"97 Edison Cres & Maxwell Dr, Sandton, 2157\\r\\n\",\"latitude\":\"-26.038042\",\"longitude\":\"28.080477\",\"range\":\"5\"},{\"title\":\"Parkview Spar\",\"address\":\"58 Tyrone Ave, Johannesburg, 2193\\r\\n\",\"latitude\":\"-26.160100\",\"longitude\":\"28.026410\",\"range\":\"5\"},{\"title\":\"Parktown Quarter\",\"address\":\"Cnr 3rd Ave & 7th Ave, Parktown North, Johannesburg, 2193\\r\\n\",\"latitude\":\"-26.144799\",\"longitude\":\"28.028725\",\"range\":\"5\"},{\"title\":\"kolkata\",\"address\":\"BF-170, 1st Avenue, Sector 1, Kolkata, West Bengal, India\",\"latitude\":\"22.597234\",\"longitude\":\"88.420881\",\"range\":\"10\"},{\"title\":\"Hyde Park shopping centre\",\"address\":\"Jan Smuts Ave & William Nicol Drive, Hyde Park, Johannesburg, 2196\",\"latitude\":\"-26.1252\",\"longitude\":\"28.0333\",\"range\":\"5\"},{\"title\":\"Fourways Gardens Shopping Centre\",\"address\":\"Fourways Gardens Shopping Centre, Next To The ATM's, Uranium St, Johannesburg\",\"latitude\":\"-26.0076\",\"longitude\":\"28.0033\",\"range\":\"5\"},{\"title\":\" Midway Mall Bramley \",\"address\":\"280 Corlett Drive, Bramley Gardens, Johannesburg\",\"latitude\":\"-26.1256\",\"longitude\":\"28.0940\",\"range\":\"1\"},{\"title\":\"BluBird Shopping Centre\",\"address\":\"Corner Atholl-Oaklands Road And Fort Street, Johannesburg, 2057, South Africa\",\"latitude\":\"-26.1296\",\"longitude\":\"28.0652\",\"range\":\"5\"},{\"title\":\"Balfour Park Shopping Centre\",\"address\":\"Corner Athol and Johannesburg Roads, Highlands North, Pretoria, South Africa\",\"latitude\":\"-26.1370\",\"longitude\":\"28.0891\",\"range\":\"6\"}]}}";
        JSONObject jObject = null;
        try {
            jObject = new JSONObject(response);

        String resStat = jObject.getString("status");
        String resSuccess = jObject.getString("success");
        if (resStat.equalsIgnoreCase("200")&&resSuccess.equalsIgnoreCase("1")) {
            JSONArray jArr = jObject.getJSONObject("data").getJSONArray("hub_location");
            int size = jArr.length();
            listHub = new ArrayList<HashMap<String, String>>();
            if (size>0){
                for (int i = 0; i < size; i++) {
                    HashMap<String, String> mapHubList = new HashMap<String, String>();
                    String strHubLat = jArr.getJSONObject(i).getString("latitude").toString();
                    String strHubLong = jArr.getJSONObject(i).getString("longitude").toString();
                    String strRad = jArr.getJSONObject(i).getString("range").toString();
                    String strAddr = jArr.getJSONObject(i).getString("title").toString();


                    mapHubList.put(KEY_title, jArr.getJSONObject(i).getString("title").toString());
                    mapHubList.put(KEY_address, strAddr);
                    mapHubList.put(KEY_latitude, strHubLat);
                    mapHubList.put(KEY_longitude, strHubLong);
                    mapHubList.put(KEY_range, strRad);

                    listHub.add(mapHubList);

                            /*CircleOptions circleOptions = new CircleOptions()
                                    .center(new LatLng(Double.parseDouble(strHubLat), Double.parseDouble(strHubLong)))
                                    .radius(Double.parseDouble(strRad)*1000)
                                    //.fillColor(0x40ff0000)
                                    .strokeColor(Color.RED)
                                    .strokeWidth(4)
                                    ;
                            Circle circle = map.addCircle(circleOptions);*/
                }
                //drawCircles();
            }

        }
    } catch (JSONException e) {
        e.printStackTrace();
    }

    }
    private void getAllHubManual() {
        //Showing the progress dialog
        final ProgressDialog pDialog = ProgressDialog.show(MapActivity.this, "Loading...", "Please wait...", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Constants.strGetAllHub,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        try {
                            JSONObject jObject = new JSONObject(s);
                            String resStat = jObject.getString("status");
                            String resSuccess = jObject.getString("success");
                            if (resStat.equalsIgnoreCase("200")&&resSuccess.equalsIgnoreCase("1")) {
                                JSONArray jArr = jObject.getJSONObject("data").getJSONArray("hub_location");
                                int size = jArr.length();
                                listHub = new ArrayList<HashMap<String, String>>();
                                if (size>0){
                                    for (int i = 0; i < size; i++) {
                                        HashMap<String, String> mapHubList = new HashMap<String, String>();
                                        String strHubLat = jArr.getJSONObject(i).getString("latitude").toString();
                                        String strHubLong = jArr.getJSONObject(i).getString("longitude").toString();
                                        String strRad = jArr.getJSONObject(i).getString("range").toString();
                                        String strAddr = jArr.getJSONObject(i).getString("title").toString();


                                        mapHubList.put(KEY_title, jArr.getJSONObject(i).getString("title").toString());
                                        mapHubList.put(KEY_address, strAddr);
                                        mapHubList.put(KEY_latitude, strHubLat);
                                        mapHubList.put(KEY_longitude, strHubLong);
                                        mapHubList.put(KEY_range, strRad);

                                        listHub.add(mapHubList);

                            /*CircleOptions circleOptions = new CircleOptions()
                                    .center(new LatLng(Double.parseDouble(strHubLat), Double.parseDouble(strHubLong)))
                                    .radius(Double.parseDouble(strRad)*1000)
                                    //.fillColor(0x40ff0000)
                                    .strokeColor(Color.RED)
                                    .strokeWidth(4)
                                    ;
                            Circle circle = map.addCircle(circleOptions);*/
                                    }
                                    //drawCircles();
                                }
                            }
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
                        //Toast.makeText(MapActivity.this, volleyError.getMessage().toString(), Toast.LENGTH_LONG).show();
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
        RequestQueue requestQueue = Volley.newRequestQueue(MapActivity.this);

        //Adding request to the queue
        requestQueue.add(stringRequest);

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                150000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


    }

    private void getAllHub() {
        pDialog=new ProgressDialog(MapActivity.this);
        pDialog.setMessage("Loading...");
        pDialog.setCanceledOnTouchOutside(false);
        pDialog.show();
        restService.getAllHub(new RestCallback<GetAllHubRequest>() {
            @Override
            public void success(GetAllHubRequest responce) {
                if (responce.getSuccess() == 1 && responce.getStatus() == 200) {
                    int size = responce.getData().getHubLocation().size();
                    listHub = new ArrayList<HashMap<String, String>>();
                    if (size>0){
                        for (int i = 0; i < size; i++) {
                            HashMap<String, String> mapHubList = new HashMap<String, String>();
                            String strHubLat = responce.getData().getHubLocation().get(i).getLatitude().toString();
                            String strHubLong = responce.getData().getHubLocation().get(i).getLongitude().toString();
                            String strRad = responce.getData().getHubLocation().get(i).getRange().toString();
                            String strAddr = responce.getData().getHubLocation().get(i).getAddress().toString();
                            mapHubList.put(KEY_title, responce.getData().getHubLocation().get(i).getTitle().toString());
                            mapHubList.put(KEY_address, responce.getData().getHubLocation().get(i).getAddress().toString());
                            mapHubList.put(KEY_latitude, strHubLat);
                            mapHubList.put(KEY_longitude, strHubLong);
                            mapHubList.put(KEY_range, strRad);
                            listHub.add(mapHubList);
                            /*CircleOptions circleOptions = new CircleOptions()
                                    .center(new LatLng(Double.parseDouble(strHubLat), Double.parseDouble(strHubLong)))
                                    .radius(Double.parseDouble(strRad)*1000)
                                    //.fillColor(0x40ff0000)
                                    .strokeColor(Color.RED)
                                    .strokeWidth(4)
                                    ;
                            Circle circle = map.addCircle(circleOptions);*/
                        }
                        //drawCircles();
                    }
                }

                if(pDialog!=null){
                    pDialog.dismiss();
                }
            }

            @Override
            public void invalid() {
                if(pDialog!=null){
                    pDialog.dismiss();
                }
            }

            @Override
            public void failure() {
                if(pDialog!=null){
                    pDialog.dismiss();
                }
            }
        });
    }

    private void initFields() {
        shPrefDeliverAddr = this.getSharedPreferences(Constants.strShPrefDelAddrName, Context.MODE_PRIVATE);
        img_back = (ImageView) findViewById(R.id.img_back);
        img_cross = (ImageView) findViewById(R.id.img_cross);
        img_cross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_moved_addr.setText("");
                strAddr = "";
                et_moved_addr.setSelection(et_moved_addr.getText().toString().length());
                callPlaceApi = true;
            }
        });
        txt_header=(TextView)findViewById(R.id.txt_top_bar);

        if(forAct!=null&&forAct.equalsIgnoreCase("MyAddress")){
            txt_header.setText("Billing Address");
        }
        img_cur_loc = (ImageView) findViewById(R.id.img_cur_loc);
        txt_clear = (TextView) findViewById(R.id.txt_clear);
        txt_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_moved_addr.setText("");
                strAddr = "";
                et_moved_addr.setSelection(et_moved_addr.getText().toString().length());
                callPlaceApi = true;
            }
        });

        btn_get_position = (ImageView) findViewById(R.id.btn_get_position);
        txt_done = (TextView) findViewById(R.id.txt_done);
        et_moved_addr = (AutoCompleteTextView) findViewById(R.id.et_moved_addr);

        // Google place API
        et_moved_addr.setAdapter(new GooglePlacesAutocompleteAdapter(this, R.layout.list_item));
        et_moved_addr.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String str = (String) parent.getItemAtPosition(position);
                String strName = placeDetailName.get(position).toString();
                strSearchId = placeDetailId.get(position).toString();
                et_moved_addr.setText(str);
                et_moved_addr.setSelection(et_moved_addr.getText().toString().length());

                String url = "https://maps.googleapis.com/maps/api/place/details/json?placeid="
                        + strSearchId
                        + "&key="
                        + API_KEY;

                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                new get_search_place_detail().execute();
            }
        });

        (((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))).getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                //map_hostel.getUiSettings().setZoomControlsEnabled(true);
                map.getUiSettings().setCompassEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
                //map.getUiSettings().setAllGesturesEnabled(true);
                //map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                map.setTrafficEnabled(true);
                //map.setIndoorEnabled(false);
                map.setBuildingsEnabled(true);
                map.getUiSettings().setMapToolbarEnabled(true);
                map.getUiSettings().setTiltGesturesEnabled(true);
                map.getUiSettings().setZoomControlsEnabled(true);
                map.getUiSettings().setCompassEnabled(true);

                getAddress();

                map.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                    @Override
                    public void onCameraIdle() {
                        long curTime = System.currentTimeMillis();
                        long mills = curTime - getSysTime;
                        int intMilSec = (int) mills;
                        //int sec = (int) (mills/1000);
                        if (intMilSec > 2000){
                            //flagAutoSearch=false;
                            VisibleRegion visibleRegion = map.getProjection()
                                    .getVisibleRegion();

                            Point x = map.getProjection().toScreenLocation(
                                    visibleRegion.farRight);

                            Point y = map.getProjection().toScreenLocation(
                                    visibleRegion.nearLeft);

                            Point centerPoint = new Point(x.x / 2, y.y / 2);

                            LatLng centerFromPoint = map.getProjection().fromScreenLocation(
                                    centerPoint);

                            movedLatitude = String.valueOf(centerFromPoint.latitude);
                            movedLongitude = String.valueOf(centerFromPoint.longitude);

                            Geocoder geoCoder = new Geocoder(MapActivity.this, Locale.getDefault());
                            List<Address> addresses = null;
                            try {
                                addresses = geoCoder.getFromLocation(Double.parseDouble(movedLatitude), Double.parseDouble(movedLongitude), 1);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            String address = "";
                            if (addresses != null && addresses.size() > 0) {
                                String movedLocality = addresses.get(0).getSubLocality();
                                int addlineIndex = addresses.get(0).getMaxAddressLineIndex();
                                String strFullAddr = addresses.get(0).getAddressLine(0);
                                for (int i = 1; i < addlineIndex; i++) {
                                    strFullAddr = strFullAddr + ", " + addresses.get(0).getAddressLine(i);
                                }
                                et_moved_addr.setText(strFullAddr);
                                strAddr = strFullAddr;
                                et_moved_addr.setSelection(et_moved_addr.getText().toString().length());
                                callPlaceApi = false;
                            }
                        }
                    }
                });
            }
        });

        Constants.flagMapPage = true;

        if (checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION, getApplicationContext(), MapActivity.this)) {
            CheckLocationEnable();
            mLocationProvider = new LocationProvider(this, null);
            mLocationProvider.connect();
        } else {
            requestPermission(android.Manifest.permission.ACCESS_FINE_LOCATION, PERMISSION_REQUEST_CODE_LOCATION, getApplicationContext(), MapActivity.this);
        }
        img_cur_loc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //setLocationOnMap(Double.parseDouble(Constants.strCurLat), Double.parseDouble(Constants.strCurLong));
                if (Constants.flagLocation) {
                    getAddress();
                }
                else {
                    Toast.makeText(MapActivity.this, "Unable to fetch location..", Toast.LENGTH_SHORT).show();
                }
            }
        });

        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(forAct!=null){
                    if(forAct.equalsIgnoreCase("Checkout")){
                        Intent intent = new Intent(MapActivity.this, CheckOutActivity.class);
                        finish();
                        startActivity(intent);
                    }else if(forAct.equalsIgnoreCase("MyAddress")){
                        Intent intent = new Intent(MapActivity.this, MyAddressActivity.class);
                        intent.putExtra("for", "map");
                        finish();
                        startActivity(intent);
                    }
                    else if(forAct.equalsIgnoreCase("Pincode")){
                        Intent intent = new Intent(MapActivity.this, PinCodeActivity.class);
                        finish();
                        startActivity(intent);
                    }
                    else if(forAct.equalsIgnoreCase("AddToCartListAllItemsActivity")){
                        Intent intent = new Intent(MapActivity.this, AddToCartListAllItemsActivity.class);
                        intent.putExtra("context_act1", "webskitters.com.stockup.PinCodeActivity");
                        finish();
                        startActivity(intent);
                    }
                    else if(forAct.equalsIgnoreCase("MyAddress")){
                        Intent intent = new Intent(MapActivity.this, MyAddressActivity.class);
                        intent.putExtra("for", "map");
                        finish();
                        startActivity(intent);
                    }else if(forAct.equalsIgnoreCase("SubCategoryActivity")){
                        Intent intent = new Intent(MapActivity.this, SubCategoryActivity.class);
                        //intent.putExtra("for", "map");
                        finish();
                        startActivity(intent);
                    }

                    else if(forAct.equalsIgnoreCase("ProductDetailsActivity")){
                        Intent intent = new Intent(MapActivity.this, ProductDetailsActivity.class);
                        //intent.putExtra("for", "map");
                        finish();
                        startActivity(intent);
                    }else if(forAct.equalsIgnoreCase("MyWishListActivity")){
                        Intent intent = new Intent(MapActivity.this, MyWishListActivity.class);
                        //intent.putExtra("for", "map");
                        finish();
                        startActivity(intent);
                    }
                    else if(forAct.equalsIgnoreCase("MyShoppingListAllItemsActivity")){
                        Intent intent = new Intent(MapActivity.this, MyShoppingListAllItemsActivity.class);
                        //intent.putExtra("for", "map");
                        finish();
                        startActivity(intent);
                    }
                    else{
                        Intent intent = new Intent(MapActivity.this, PinCodeActivity.class);
                        finish();
                        startActivity(intent);
                    }
                }else{
                    finish();
                }
                MapActivity.this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });
        txt_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!movedLatitude.equalsIgnoreCase("") && !movedLongitude.equalsIgnoreCase("")) {
                    if (locationInHub()) {
                        toEdit = shPrefDeliverAddr.edit();
                        toEdit.putString(Constants.strShPrefDelCallFrom, "Yes");
                        toEdit.putString(Constants.strShPrefDeliver, "true");
                        toEdit.putString(Constants.strShPrefDelLat, movedLatitude);
                        toEdit.putString(Constants.strShPrefDelLong, movedLongitude);
                        //toEdit.putString(Constants.strShPrefDelAddr, et_moved_addr.getText().toString().trim());
                        toEdit.putString(Constants.strShPrefDelAddr, strAddr);
                        toEdit.commit();
                        if (strAddr.length() > 0) {
                            if (forAct.equalsIgnoreCase("Checkout")) {
                                Intent intent = new Intent(MapActivity.this, CheckOutActivity.class);
                                finish();
                                startActivity(intent);
                            } else if (forAct.equalsIgnoreCase("Pincode")) {
                                Intent intent = new Intent(MapActivity.this, PinCodeActivity.class);
                                finish();
                                startActivity(intent);
                            } else if (forAct.equalsIgnoreCase("MyAddress")) {
                                Intent intent = new Intent(MapActivity.this, MyAddressActivity.class);
                                intent.putExtra("for", "map");
                                finish();
                                startActivity(intent);
                            }
                            else if(forAct.equalsIgnoreCase("SubCategoryActivity")){
                                Intent intent = new Intent(MapActivity.this, SubCategoryActivity.class);
                                //intent.putExtra("for", "map");
                                finish();
                                startActivity(intent);
                            }
                            else if(forAct.equalsIgnoreCase("ProductDetailsActivity")){
                                Intent intent = new Intent(MapActivity.this, ProductDetailsActivity.class);
                                //intent.putExtra("for", "map");
                                finish();
                                startActivity(intent);
                            }else if(forAct.equalsIgnoreCase("MyWishListActivity")){
                                Intent intent = new Intent(MapActivity.this, MyWishListActivity.class);
                                //intent.putExtra("for", "map");
                                finish();
                                startActivity(intent);
                            }
                            else if(forAct.equalsIgnoreCase("MyShoppingListAllItemsActivity")){
                                Intent intent = new Intent(MapActivity.this, MyShoppingListAllItemsActivity.class);
                                //intent.putExtra("for", "map");
                                finish();
                                startActivity(intent);
                            }
                            else if(forAct.equalsIgnoreCase("AddToCartListAllItemsActivity")){
                                Intent intent = new Intent(MapActivity.this, AddToCartListAllItemsActivity.class);
                                intent.putExtra("context_act1", "webskitters.com.stockup.PinCodeActivity");
                                finish();
                                startActivity(intent);
                            }else {
                                Intent intent = new Intent(MapActivity.this, PinCodeActivity.class);
                                finish();
                                startActivity(intent);
                            }
                            MapActivity.this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                        } else {
                            utils.displayAlert("Please provide a valid address.");
                            et_moved_addr.requestFocus();
                        }
                    }
                    else {
                        toEdit = shPrefDeliverAddr.edit();
                        toEdit.putString(Constants.strShPrefDelCallFrom, "Yes");
                        toEdit.putString(Constants.strShPrefDeliver, "true");
                        toEdit.putString(Constants.strShPrefDelLat, movedLatitude);
                        toEdit.putString(Constants.strShPrefDelLong, movedLongitude);
                        //toEdit.putString(Constants.strShPrefDelAddr, et_moved_addr.getText().toString().trim());
                        toEdit.putString(Constants.strShPrefDelAddr, strAddr);
                        toEdit.commit();
                        getDialogBrowse("We have either closed for the day or your delivery address is out of our service area. We are rolling out across South Africa!\n\nBrowse our store or change your delivery address to experience our world!");
                    }
                }
                else {
                    utils.displayAlert("Unable to fetch your current location. Please try again.");
                }
            }
        });

    }

    private boolean locationInHub() {
        Boolean insideReg = false;
        LatLng cur = new LatLng(Double.parseDouble(movedLatitude), Double.parseDouble(movedLongitude));
        for (int i = 0; i < listHub.size(); i++){
            /*LatLng bound = new LatLng(Double.parseDouble(listHub.get(i).get(KEY_latitude)),
                    Double.parseDouble(listHub.get(i).get(KEY_longitude)));*/
            double distCalculated = distance(Double.parseDouble(movedLatitude), Double.parseDouble(movedLongitude),
                    Double.parseDouble(listHub.get(i).get(KEY_latitude)),
                    Double.parseDouble(listHub.get(i).get(KEY_longitude)));
            double dis = Double.parseDouble(listHub.get(i).get(KEY_range));
            if (dis>distCalculated){
                insideReg = true;
                break;
            }
        }
        return insideReg;
    }

    private void getAddress() {
        Geocoder geoCoder = new Geocoder(MapActivity.this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            latitude = Double.parseDouble(Constants.strCurLat);
            longitude = Double.parseDouble(Constants.strCurLong);
            /*prevLat = String.valueOf(latitude);
            prevLong = String.valueOf(longitude);
            nextLat = String.valueOf(latitude);
            nextLong = String.valueOf(longitude);
            setLatitude = String.valueOf(latitude);
            setLongitude = String.valueOf(longitude);*/
            addresses = geoCoder.getFromLocation(Double.parseDouble(Constants.strCurLat), Double.parseDouble(Constants.strCurLong), 1);
            movedLatitude = Constants.strCurLat;
            movedLongitude = Constants.strCurLong;
        } catch (IOException e) {
            e.printStackTrace();
        }
        String address = "";
        if (addresses != null && addresses.size() > 0) {
            String movedLocality = addresses.get(0).getSubLocality();
            int addlineIndex = addresses.get(0).getMaxAddressLineIndex();
            String strFullAddr = addresses.get(0).getAddressLine(0);
            for (int i = 1; i < addlineIndex; i++) {
                strFullAddr = strFullAddr + ", " + addresses.get(0).getAddressLine(i);
            }
            et_moved_addr.setText(strFullAddr);
            strAddr = strFullAddr;
            et_moved_addr.setSelection(et_moved_addr.getText().toString().length());
        }
        setLocationOnMap(Double.parseDouble(Constants.strCurLat), Double.parseDouble(Constants.strCurLong));
    }

    private void setLocationOnMap(double pLat, double pLong) {
        CameraPosition cameraPosition = new CameraPosition.Builder().target(
                new LatLng(pLat, pLong)).zoom(mapZoomLevel).build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public void CheckLocationEnable() {
        if (Build.VERSION.SDK_INT >= 17) {
            LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                showSettingsAlert();

            } else {
                //splashHandlar(SPLASH_TIME_OUT);
            }
        } else {
            Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
            intent.putExtra("enabled", true);
            sendBroadcast(intent);
            //splashHandlar(SPLASH_TIME_OUT);
        }
    }

    public static boolean checkPermission(String strPermission, Context _c, Activity _a) {
        int result = ContextCompat.checkSelfPermission(_c, strPermission);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private boolean checkPermission() {
        boolean flag = true;
        //String[] permissions = {"android.permission.READ_PHONE_STATE"};
        boolean hasPermission = (ContextCompat.checkSelfPermission(MapActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int hasLocationPermission = checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE);
                if (hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{android.Manifest.permission.READ_PHONE_STATE}, REQUEST_CODE_ASK_PERMISSIONS);
                    flag = false;
                    return false;
                }
            }
        } else {
            flag = true;
        }
        return flag;
    }

    public void requestPermission(String strPermission, int perCode, Context _c, Activity _a) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(_a, strPermission)) {
            ActivityCompat.requestPermissions(_a, new String[]{strPermission}, perCode);
            //Toast.makeText(VisitorHomePageActivity.this,"GPS permission allows us to access location data. Please allow in App Settings for additional functionality.",Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(_a, new String[]{strPermission}, perCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //new FetchTask().execute();
                    //checkPermission();
                    CheckLocationEnable();
                } else {
                    Toast.makeText(getApplicationContext(), "Permission Denied, You cannot access location data.", Toast.LENGTH_LONG).show();
                    //splashHandlar(SPLASH_TIME_OUT);
                }
                //splashHandlar(SPLASH_TIME_OUT);
                break;
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                } else {
                    checkPermission();
                    // Permission Denied
                }
                //splashHandlar(SPLASH_TIME_OUT);
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(forAct!=null){
            if(forAct.equalsIgnoreCase("Checkout")){
                Intent intent = new Intent(MapActivity.this, CheckOutActivity.class);
                finish();
                startActivity(intent);
            }
            else if(forAct.equalsIgnoreCase("Pincode")){
                Intent intent = new Intent(MapActivity.this, PinCodeActivity.class);
                finish();
                startActivity(intent);
            }
            else if(forAct.equalsIgnoreCase("MyAddress")){
                Intent intent = new Intent(MapActivity.this, MyAddressActivity.class);
                intent.putExtra("for", "map");
                finish();
                startActivity(intent);
            }
            else if(forAct.equalsIgnoreCase("SubCategoryActivity")){
                Intent intent = new Intent(MapActivity.this, SubCategoryActivity.class);
                //intent.putExtra("for", "map");
                finish();
                startActivity(intent);
            }else if(forAct.equalsIgnoreCase("ProductDetailsActivity")){
                Intent intent = new Intent(MapActivity.this, ProductDetailsActivity.class);
                //intent.putExtra("for", "map");
                finish();
                startActivity(intent);
            }else if(forAct.equalsIgnoreCase("MyWishListActivity")){
                Intent intent = new Intent(MapActivity.this, MyWishListActivity.class);
                //intent.putExtra("for", "map");
                finish();
                startActivity(intent);
            }
            else if(forAct.equalsIgnoreCase("MyShoppingListAllItemsActivity")){
                Intent intent = new Intent(MapActivity.this, MyShoppingListAllItemsActivity.class);
                //intent.putExtra("for", "map");
                finish();
                startActivity(intent);
            }
            else if(forAct.equalsIgnoreCase("AddToCartListAllItemsActivity")){
                Intent intent = new Intent(MapActivity.this, AddToCartListAllItemsActivity.class);
                intent.putExtra("context_act1", "webskitters.com.stockup.PinCodeActivity");
                finish();
                startActivity(intent);
            }else{
                Intent intent = new Intent(MapActivity.this, PinCodeActivity.class);
                finish();
                startActivity(intent);
            }
        }else{
            finish();
        }

        MapActivity.this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapActivity.this);
        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

            }
        });
        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");
        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
                //splashHandlar(6000);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                //splashHandlar(SPLASH_TIME_OUT);
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }
    class GooglePlacesAutocompleteAdapter extends ArrayAdapter implements Filterable {
        private ArrayList resultList;

        public GooglePlacesAutocompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public String getItem(int index) {
            return (String) resultList.get(index);
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        // Retrieve the autocomplete results.
                        resultList = autocomplete(constraint.toString());

                        // Assign the data to the FilterResults
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
            return filter;
        }
    }
    public static ArrayList autocomplete(String input) {
        ArrayList resultList = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?key=" + API_KEY);
            //sb.append("&components=country:in");
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));

            //Log.d("jjhdfhlsdfasgasd", sb.toString());

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return resultList;
        } catch (IOException e) {
            e.printStackTrace();
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList(predsJsonArray.length());
            placeDetailId = null;
            placeDetailName = null;
            placeDetailName = new ArrayList();
            placeDetailId = new ArrayList();
            for (int i = 0; i < predsJsonArray.length(); i++) {
                //System.out.println(predsJsonArray.getJSONObject(i).getString("description"));
                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));

                JSONObject c = null;
                c = predsJsonArray.getJSONObject(i);
                strPlaceName = c.getString(KEY_description).toString();
                strPlaceId = c.getString(KEY_place_id).toString();
                placeDetailName.add(strPlaceName);
                placeDetailId.add(strPlaceId);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultList;
    }

    public class get_search_place_detail extends AsyncTask<Void, Void, Void> {

        DefaultHttpClient httpclient1;
        HttpPost httppost1;
        HttpGet httpget1;
        HttpResponse response1;
        HttpEntity entity;
        HttpURLConnection connection = null;

        String url = "https://maps.googleapis.com/maps/api/place/details/json?placeid="
                + strSearchId
                + "&key="
                + API_KEY;

        // https://maps.googleapis.com/maps/api/place/search/xml?location=22,88&radius=1&types=store&hasNextPage=true&nextPage()=true&sensor=false&key=AIzaSyCAXQsfmfFTdVCGeKzQwbxYg1LmAmTO5vE
        // Use token from first url
        //https://maps.googleapis.com/maps/api/place/search/xml?location=Enter Latitude,Enter Longitude&radius=10000&types=store&hasNextPage=true&nextPage()=true&sensor=false&key=enter google_map_key &pagetoken="Enter the first page token Value"


        JSONObject jsonObj = null;
        String jsonStr = null;
        Boolean respMsg = false;
        ProgressDialog pDialog2;

        @Override
        protected void onPreExecute() {
            Log.e("getPlaceDetailsurl", url);
            super.onPreExecute();

            try {
                pDialog2 = new ProgressDialog(MapActivity.this);
                pDialog2.setCancelable(true);
                pDialog2.setMessage("Please wait...");
                pDialog2.show();
                pDialog2.setCanceledOnTouchOutside(false);

            } catch (Exception e) {

            }
        }

        @SuppressWarnings("deprecation")
        @Override
        protected Void doInBackground(Void... params) {
            ServiceHandler sh = new ServiceHandler();
            // Making a request to url and getting response
            jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);
            if (jsonStr != null) {
                try {
                    jsonObj = new JSONObject(jsonStr);
                    // Getting JSON Array node
                    resultSearchPlaceDetail = jsonObj.getJSONObject(KEY_RESULT1);
                    objGeometry = resultSearchPlaceDetail.getJSONObject("geometry");
                    objLocation = objGeometry.getJSONObject("location");
                    movedLatitude = objLocation.getString("lat");
                    movedLongitude = objLocation.getString("lng");
                    String strAddrFromApi = resultSearchPlaceDetail.getString("formatted_address");
                    strAddr = resultSearchPlaceDetail.getString("formatted_address");
                    for (int i=strAddrFromApi.length()-1; i>0; i--){
                        if (strAddrFromApi.charAt(i) == ','){
                            strAddr = strAddrFromApi.substring(0, i);
                            break;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
           /* getPlaceDataFromJson();*/

            if (pDialog2.isShowing()) {
                pDialog2.dismiss();
            }
            getPlaceDataFromJson();
            //new get_restaurents().execute();
            try {
                //addMultipleMarker(mapMarkerList);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void getPlaceDataFromJson(){
        // looping through All Contacts
        try {
            getSysTime = System.currentTimeMillis();
            /*jsonObj = new JSONObject(jsonStr);
            // Getting JSON Array node
            resultSearchPlaceDetail = jsonObj.getJSONObject(KEY_RESULT1);*/
            objGeometry = resultSearchPlaceDetail.getJSONObject("geometry");
            objLocation = objGeometry.getJSONObject("location");
            movedLatitude = objLocation.getString("lat").toString();
            movedLongitude = objLocation.getString("lng").toString();
            //setLocationOnMap(Double.valueOf(objLocation.getString("lat").toString()),Double.valueOf(objLocation.getString("lng").toString()));
            setLocationOnMap(Double.valueOf(movedLatitude),Double.valueOf(movedLongitude));
            //latitude=Double.valueOf(objLocation.getString("lat").toString());
            //longitude=Double.valueOf(objLocation.getString("lng").toString());
            //addMultipleMarker(mapMarkerList);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

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

        return (distance * meterConversion)/1000;
    }

    public void displayAlert(String message)
    {
        // TODO Auto-generated method stub
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MapActivity.this);

        //alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setTitle("Stockup") ;
        alertDialogBuilder.setPositiveButton("Browse", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                toEdit = shPrefDeliverAddr.edit();
                toEdit.putString(Constants.strShPrefDelCallFrom, "Yes");
                toEdit.putString(Constants.strShPrefDeliver, "false");
                toEdit.putString(Constants.strShPrefDelLat, movedLatitude);
                toEdit.putString(Constants.strShPrefDelLong, movedLongitude);
                //toEdit.putString(Constants.strShPrefDelAddr, et_moved_addr.getText().toString().trim());
                toEdit.putString(Constants.strShPrefDelAddr, strAddr);
                toEdit.commit();
                if (strAddr.length() > 0) {
                    if(forAct.equalsIgnoreCase("Checkout")){
                        Intent intent = new Intent(MapActivity.this, CheckOutActivity.class);
                        finish();
                        startActivity(intent);
                    }
                    else if(forAct.equalsIgnoreCase("Pincode")){
                        Intent intent = new Intent(MapActivity.this, PinCodeActivity.class);
                        finish();
                        startActivity(intent);
                    }
                    else if(forAct.equalsIgnoreCase("MyAddress")){
                        Intent intent = new Intent(MapActivity.this, MyAddressActivity.class);
                        intent.putExtra("for", "map");
                        finish();
                        startActivity(intent);
                    }
                    else if(forAct.equalsIgnoreCase("AddToCartListAllItemsActivity")){
                        Intent intent = new Intent(MapActivity.this, AddToCartListAllItemsActivity.class);
                        intent.putExtra("context_act1", "webskitters.com.stockup.PinCodeActivity");
                        finish();
                        startActivity(intent);
                    }
                    else if(forAct.equalsIgnoreCase("SubCategoryActivity")){
                        Intent intent = new Intent(MapActivity.this, SubCategoryActivity.class);
                        //intent.putExtra("for", "map");
                        finish();
                        startActivity(intent);
                    }

                    else if(forAct.equalsIgnoreCase("ProductDetailsActivity")){
                        Intent intent = new Intent(MapActivity.this, ProductDetailsActivity.class);
                        //intent.putExtra("for", "map");
                        finish();
                        startActivity(intent);
                    }else if(forAct.equalsIgnoreCase("MyWishListActivity")){
                        Intent intent = new Intent(MapActivity.this, MyWishListActivity.class);
                        //intent.putExtra("for", "map");
                        finish();
                        startActivity(intent);
                    }
                    else if(forAct.equalsIgnoreCase("MyShoppingListAllItemsActivity")){
                        Intent intent = new Intent(MapActivity.this, MyShoppingListAllItemsActivity.class);
                        //intent.putExtra("for", "map");
                        finish();
                        startActivity(intent);
                    }else{
                        Intent intent = new Intent(MapActivity.this, PinCodeActivity.class);
                        finish();
                        startActivity(intent);
                    }
                    MapActivity.this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                } else {
                    utils.displayAlert("Please provide a valid address.");
                    et_moved_addr.requestFocus();
                }
            }
        });
        alertDialogBuilder.setNegativeButton("Sign In", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Intent intent = new Intent(MapActivity.this, LoginActivity.class);
                //intent.putExtra("context_act1", "webskitters.com.stockup.MapActivity");
                //intent.putExtra("from", "map");
                finish();
                startActivity(intent);
                MapActivity.this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });
        TextView myMsg = new TextView(MapActivity.this);
        myMsg.setText(message);
        myMsg.setPadding(20, 20, 20, 20);
        myMsg.setTextSize(16);
        myMsg.setTextColor(Color.BLACK);
        myMsg.setGravity(Gravity.CENTER|Gravity.CENTER_HORIZONTAL);
        alertDialogBuilder.setView(myMsg);

        TextView title = new TextView(MapActivity.this);
        // You Can Customise your Title here
        title.setText("Stockup");
        title.setBackgroundColor(Color.TRANSPARENT);
        title.setPadding(15, 20, 15, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.BLACK);
        title.setTextSize(20);

        myMsg.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        LinearLayout.LayoutParams positiveButtonLLl = (LinearLayout.LayoutParams) myMsg.getLayoutParams();
        positiveButtonLLl.gravity = Gravity.CENTER|Gravity.CENTER_VERTICAL;
        myMsg.setLayoutParams(positiveButtonLLl);

        alertDialogBuilder.setCustomTitle(title);
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();

        if(!customer_id.equalsIgnoreCase("")||!customer_id.isEmpty()){
            alertDialog.getButton(alertDialog.BUTTON_NEGATIVE).setVisibility(View.GONE);
        }


        final Button negButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        /*negButton.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        LinearLayout.LayoutParams negButtonLL = (LinearLayout.LayoutParams) negButton.getLayoutParams();
        negButtonLL.gravity = Gravity.LEFT|Gravity.CENTER_VERTICAL;*/
        negButton.setTextColor(Color.parseColor("#048BCD"));
        //negButton.setLayoutParams(negButtonLL);


        final Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        /*positiveButton.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        LinearLayout.LayoutParams positiveButtonLL = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
        positiveButtonLL.gravity = Gravity.RIGHT|Gravity.CENTER_VERTICAL;*/
        positiveButton.setTextColor(Color.parseColor("#048BCD"));
        //positiveButton.setLayoutParams(positiveButtonLL);
    }

    private void getDialogBrowse(String strMsg) {
        final Dialog dialog = new Dialog(MapActivity.this);
        Window window = dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_alert);

        TextView txt_header=(TextView)dialog.findViewById(R.id.header);
        TextView txt_msg=(TextView)dialog.findViewById(R.id.msg);
        TextView btn_sign_in = (TextView)dialog.findViewById(R.id.btn_sign_in);
        TextView btn_browse = (TextView)dialog.findViewById(R.id.btn_browse);

        txt_header.setText("We are rolling out!");
        txt_msg.setText(strMsg);

        if(!customer_id.equalsIgnoreCase("")||!customer_id.isEmpty()){
            //alertDialog.getButton(alertDialog.BUTTON_NEGATIVE).setVisibility(View.GONE);
            btn_sign_in.setVisibility(View.GONE);

            btn_browse.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT, 2));
            LinearLayout.LayoutParams positiveButtonLL = (LinearLayout.LayoutParams) btn_sign_in.getLayoutParams();
            positiveButtonLL.gravity = Gravity.CENTER|Gravity.CENTER_VERTICAL;
            btn_sign_in.setLayoutParams(positiveButtonLL);

        }

        btn_browse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                toEdit = shPrefDeliverAddr.edit();
                toEdit.putString(Constants.strShPrefDelCallFrom, "Yes");
                toEdit.putString(Constants.strShPrefDeliver, "false");
                toEdit.putString(Constants.strShPrefDelLat, movedLatitude);
                toEdit.putString(Constants.strShPrefDelLong, movedLongitude);
                //toEdit.putString(Constants.strShPrefDelAddr, et_moved_addr.getText().toString().trim());
                toEdit.putString(Constants.strShPrefDelAddr, strAddr);
                toEdit.commit();
                if (strAddr.length() > 0) {
                    if(forAct.equalsIgnoreCase("Checkout")){
                        Intent intent = new Intent(MapActivity.this, CheckOutActivity.class);
                        finish();
                        startActivity(intent);
                    }
                    else if(forAct.equalsIgnoreCase("Pincode")){
                        Intent intent = new Intent(MapActivity.this, PinCodeActivity.class);
                        finish();
                        startActivity(intent);
                    }
                    else if(forAct.equalsIgnoreCase("MyAddress")){
                        Intent intent = new Intent(MapActivity.this, MyAddressActivity.class);
                        intent.putExtra("for", "map");
                        finish();
                        startActivity(intent);
                    }
                    else if(forAct.equalsIgnoreCase("AddToCartListAllItemsActivity")){
                        Intent intent = new Intent(MapActivity.this, AddToCartListAllItemsActivity.class);
                        intent.putExtra("context_act1", "webskitters.com.stockup.PinCodeActivity");
                        finish();
                        startActivity(intent);
                    }else if(forAct.equalsIgnoreCase("SubCategoryActivity")){
                        Intent intent = new Intent(MapActivity.this, SubCategoryActivity.class);
                        //intent.putExtra("for", "map");
                        finish();
                        startActivity(intent);
                    }

                    else if(forAct.equalsIgnoreCase("ProductDetailsActivity")){
                        Intent intent = new Intent(MapActivity.this, ProductDetailsActivity.class);
                        //intent.putExtra("for", "map");
                        finish();
                        startActivity(intent);
                    }else if(forAct.equalsIgnoreCase("MyWishListActivity")){
                        Intent intent = new Intent(MapActivity.this, MyWishListActivity.class);
                        //intent.putExtra("for", "map");
                        finish();
                        startActivity(intent);
                    }
                    else if(forAct.equalsIgnoreCase("MyShoppingListAllItemsActivity")){
                        Intent intent = new Intent(MapActivity.this, MyShoppingListAllItemsActivity.class);
                        //intent.putExtra("for", "map");
                        finish();
                        startActivity(intent);
                    }else{
                        Intent intent = new Intent(MapActivity.this, PinCodeActivity.class);
                        finish();
                        startActivity(intent);
                    }
                    MapActivity.this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                } else {
                    utils.displayAlert("Please provide a valid address.");
                    et_moved_addr.requestFocus();
                }
            }
        });
        btn_sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                Intent intent = new Intent(MapActivity.this, LoginActivity.class);
                //intent.putExtra("context_act1", "webskitters.com.stockup.MapActivity");
                //intent.putExtra("from", "map");
                finish();
                startActivity(intent);
                MapActivity.this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }


}
