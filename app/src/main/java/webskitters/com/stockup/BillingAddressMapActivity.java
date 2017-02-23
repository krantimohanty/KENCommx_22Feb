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

import com.appsflyer.AppsFlyerLib;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
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
import java.util.List;
import java.util.Locale;

import webskitters.com.stockup.LocationUtils.LocationProvider;
import webskitters.com.stockup.Utils.Constants;
import webskitters.com.stockup.Utils.ServiceHandler;
import webskitters.com.stockup.Utils.Utils;
import webskitters.com.stockup.model.GetAllHubRequest;
import webskitters.com.stockup.retrofit_call.RestCallback;
import webskitters.com.stockup.retrofit_call.RestService;

//import com.appsflyer.AppsFlyerLib;

public class BillingAddressMapActivity extends AppCompatActivity /*implements OnMapReadyCallback*/ {

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
    //private static final String API_KEY = "AIzaSyCAXQsfmfFTdVCGeKzQwbxYg1LmAmTO5vE";      // working but wrong place


    //Live Credential for Place API Server Key//////////////////
    private static final String API_KEY = "AIzaSyA0G5QyYfannmvdBmirUbPG7kgwh9E84NQ";


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
    private SharedPreferences sharedPreferenceUser;
    String customer_id="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_billing_address_map);
        sharedPreferenceUser=getSharedPreferences(Constants.strShPrefUserPrefName, Context.MODE_PRIVATE);
        customer_id=sharedPreferenceUser.getString(Constants.strShPrefUserId, "");
        Bundle extras = getIntent().getExtras();
        utils = new Utils(BillingAddressMapActivity.this);
        restService=new RestService(this);

        getSysTime = System.currentTimeMillis();

        // Track Data : Add to activities where tracking needed
        AppsFlyerLib.getInstance().sendDeepLinkData(this);

        initFields();
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
                map.getUiSettings().setCompassEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                map.setTrafficEnabled(true);
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

                            Geocoder geoCoder = new Geocoder(BillingAddressMapActivity.this, Locale.getDefault());
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

        if (checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION, getApplicationContext(), BillingAddressMapActivity.this)) {
            CheckLocationEnable();
            mLocationProvider = new LocationProvider(this, null);
            mLocationProvider.connect();
        } else {
            requestPermission(android.Manifest.permission.ACCESS_FINE_LOCATION, PERMISSION_REQUEST_CODE_LOCATION, getApplicationContext(), BillingAddressMapActivity.this);
        }
        img_cur_loc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //setLocationOnMap(Double.parseDouble(Constants.strCurLat), Double.parseDouble(Constants.strCurLong));
                if (Constants.flagLocation) {
                    getAddress();
                }
                else {
                    Toast.makeText(BillingAddressMapActivity.this, "Unable to fetch location..", Toast.LENGTH_SHORT).show();
                }
            }
        });

        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message="";
                Intent intent=new Intent();
                intent.putExtra("MESSAGE",message);
                setResult(2,intent);
                finish();//finishing activity
                BillingAddressMapActivity.this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });
        txt_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!movedLatitude.equalsIgnoreCase("") && !movedLongitude.equalsIgnoreCase("")) {
                    toEdit = shPrefDeliverAddr.edit();
                    toEdit.putString(Constants.strShPrefBillingLat, movedLatitude);
                    toEdit.putString(Constants.strShPrefBillingLong, movedLongitude);
                    toEdit.putString(Constants.strShPrefBillingAddr, strAddr);
                    toEdit.commit();
                    if (strAddr.length() > 0) {
                        String message=strAddr.toString();
                        Intent intent=new Intent();
                        intent.putExtra("MESSAGE",message);
                        setResult(2,intent);
                        finish();//finishing activity
                        BillingAddressMapActivity.this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    } else {
                        utils.displayAlert("Please provide a valid address.");
                        et_moved_addr.requestFocus();
                    }
                }
                else {
                    utils.displayAlert("Unable to fetch your current location. Please try again.");
                }
            }
        });
    }

    private void getAddress() {
        Geocoder geoCoder = new Geocoder(BillingAddressMapActivity.this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            latitude = Double.parseDouble(Constants.strCurLat);
            longitude = Double.parseDouble(Constants.strCurLong);
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
        boolean hasPermission = (ContextCompat.checkSelfPermission(BillingAddressMapActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
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
        String message="";
        Intent intent=new Intent();
        intent.putExtra("MESSAGE",message);
        setResult(2,intent);
        finish();//finishing activity
        BillingAddressMapActivity.this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        super.onBackPressed();
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(BillingAddressMapActivity.this);
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
                pDialog2 = new ProgressDialog(BillingAddressMapActivity.this);
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
                    strAddr = resultSearchPlaceDetail.getString("formatted_address");
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

}
