package webskitters.com.stockup;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.appsflyer.AppsFlyerLib;

import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;

import webskitters.com.stockup.GCM.Config;
import webskitters.com.stockup.LocationUtils.LocationProvider;
import webskitters.com.stockup.Utils.Constants;
import webskitters.com.stockup.Utils.Utils;
import webskitters.com.stockup.model.CurrentVersionRequest;
import webskitters.com.stockup.model.DeviceIdInstalRequest;
import webskitters.com.stockup.model.GetAddressRequest;
import webskitters.com.stockup.model.LastOrderStatusRequest;
import webskitters.com.stockup.model.OrderStatusRequest;
import webskitters.com.stockup.model.RatingSubmitRequest;
import webskitters.com.stockup.retrofit_call.RestCallback;
import webskitters.com.stockup.retrofit_call.RestService;

public class SplashActivity extends Activity {

    int count = 0;
    Timer timer;
    private static int SPLASH_TIME_OUT = 2500;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    private static final int PERMISSION_REQUEST_CODE_LOCATION = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 0;
    LocationManager manager;
    public static LocationProvider mLocationProvider;
    static final String TAG = "FCM";
    String firebaseRegid = "";
    RestService restService;
    Utils utils;
    SharedPreferences sharedPreferenceUser;
    SharedPreferences.Editor sharedPrefEditior;
    public static int height=0, width=0;
    boolean iscomplete=false;
    String strTappedView="";
    //float time_star=0, prof_star=0, packing_star=0, other_star=0;
    boolean isintime=true,isProf=true, isPacking=true, isOther=true;
    private ProgressDialog pw;
    private String customer_id="";
    Format format;
    private Typeface typeFaceSegoeuiReg;
    private Typeface typeFaceSegoeuiBold;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        restService = new RestService(this);
        utils = new Utils(SplashActivity.this);
        typeFaceSegoeuiReg = Typeface.createFromAsset(getAssets(),
                "Roboto-Regular.ttf");
        typeFaceSegoeuiBold = Typeface.createFromAsset(getAssets(),
                "ROBOTO-BOLD_0.TTF");
        // Tracking any event
        //AnalyticsApplication.getInstance().trackEvent("Splash Screen","Create","Page Shown");

        // Tracking Screen
        //AnalyticsApplication.getInstance().trackScreenView("Splash Screen");


        initFields();
        getIdAppsFlier();

        // Initialize AppsFlier Lib
        AppsFlyerLib.getInstance().startTracking(this.getApplication(),"tZAZfbkbXhmKhtgsYv5usn");

        // Track Data : Add to activities where tracking needed
        AppsFlyerLib.getInstance().sendDeepLinkData(this);

        //trigger 'loadIMEI'
        //loadIMEI();

        permissionPhoneState();

        /*SplashTimertask timertask = new SplashTimertask();
        timer = new Timer();
        timer.schedule(timertask, 2 * 1000);*/
    }

    private void permissionPhoneState() {
        if (checkPermission(Manifest.permission.READ_PHONE_STATE, getApplicationContext(), SplashActivity.this)) {
            // Set Values IMEI & SECURE_ID
            String strImei = getImei();
            String strSecureId = getSecureId();
            AppsFlyerLib.getInstance().setImeiData(strImei);
            AppsFlyerLib.getInstance().setAndroidIdData(strSecureId);

            permissionLocation();
        }
        else {
            requestPermission(Manifest.permission.READ_PHONE_STATE, REQUEST_CODE_ASK_PERMISSIONS, getApplicationContext(), SplashActivity.this);
        }
    }

    private void permissionLocation() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, getApplicationContext(), SplashActivity.this)) {
            CheckLocationEnable();
            mLocationProvider=new LocationProvider(this, null);
            mLocationProvider.connect();
        }
        else {
            requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, PERMISSION_REQUEST_CODE_LOCATION, getApplicationContext(), SplashActivity.this);
        }
    }

    private void getIdAppsFlier() {

    }

    private void initFields() {
        // Initialisation here
        sharedPreferenceUser=this.getSharedPreferences(Constants.strShPrefUserPrefName, Context.MODE_PRIVATE);
    }

    public void splashHandlar(int timeOut){
        displayFirebaseRegId();
        String strIsRegDevice = sharedPreferenceUser.getString(Constants.strShPrefUserDeviceReg,"");
        if (!strIsRegDevice.equalsIgnoreCase("yes") && utils.isConnectionPossible()) {
            saveDeviceId();
        }
        /*if (utils.isConnectionPossible()) {
            checkUpdateFromServer(timeOut);
        }
        else {*/
            splashHandlerStart(timeOut);
        //}
    }

    private void checkUpdateFromServer(final int timeOut) {
        restService.getCurrentVersion(new RestCallback<CurrentVersionRequest>() {

            @Override
            public void success(CurrentVersionRequest responce) {
                int reqStatus = responce.getStatus();
                int reqSuccess = responce.getSuccess();
                if (reqStatus == 200 && reqSuccess == 1) {
                    String strServerVersion = responce.getData().getVersion();
                    if (strServerVersion.equalsIgnoreCase(Constants.strAppVersion)){
                        splashHandlerStart(timeOut);
                    }
                    else {
                        displayAlert(responce.getData().getMsg());
                    }
                } else {
                    splashHandlerStart(timeOut);
                }
            }

            @Override
            public void invalid() {
                splashHandlerStart(timeOut);
            }

            @Override
            public void failure() {
                splashHandlerStart(timeOut);
            }
        });
    }

    public void splashHandlerStart(int timeOut){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                sharedPreferenceUser = SplashActivity.this.getSharedPreferences(Constants.strShPrefUserPrefName, Context.MODE_PRIVATE);
                customer_id = sharedPreferenceUser.getString(Constants.strShPrefUserId, "");
                if(!customer_id.equalsIgnoreCase("")&&!customer_id.isEmpty()){
                    lastOrderDetails(customer_id);
                }
                else {
                    Intent in = new Intent(getApplicationContext(), SliderActivity.class);
                    startActivity(in);
                    finish();
                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                }

            }
        }, timeOut);
    }


    private void lastOrderDetails(String customer_id) {

        restService.getLastOrderDetails(customer_id, new RestCallback<LastOrderStatusRequest>() {
            @Override
            public void success(LastOrderStatusRequest object) {
                if(object.getStatus()==200&object.getSuccess()==1){

                    Constants.orderID=object.getData().getOrderId();
                    if(object.getData().getOrderStatus().toLowerCase().toString().equalsIgnoreCase("complete")&&object.getData().getHasServiceRating().toString().equalsIgnoreCase("0")){
                        dialogRateService();
                    }else if(object.getData().getOrderStatus().toLowerCase().toString().equalsIgnoreCase("complete")&&object.getData().getHasServiceRating().toString().equalsIgnoreCase("1")){
                        Intent in = new Intent(getApplicationContext(), SliderActivity.class);
                        startActivity(in);
                        finish();
                        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                    }else if(object.getData().getOrderStatus().toLowerCase().toString().equalsIgnoreCase("cancelled")){
                        Intent in = new Intent(getApplicationContext(), SliderActivity.class);
                        startActivity(in);
                        finish();
                        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                    }else{
                        SimpleDateFormat df = new SimpleDateFormat("HH:mm");
                        Date d1=null;
                        try {
                            d1= df.parse(object.getData().getOrderTime());

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        Calendar now = Calendar.getInstance();
                        //Log.e("time", now.getTime().toString());
                        now.setTime(d1);
                        now.add(Calendar.MINUTE, 55);
                        //Log.e("time", now.getTime().toString());
                        Calendar now2 = Calendar.getInstance();
                        //Log.e("time", now.getTime().toString());
                        now2.setTime(d1);
                        now2.add(Calendar.MINUTE, 0);

                        Calendar now1 = Calendar.getInstance();
                        //Log.e("time", now.getTime().toString());
                        now1.setTime(d1);
                        now1.add(Calendar.MINUTE, 60);
                        // Log.e("time", now1.getTime().toString());

                        format = new SimpleDateFormat("HH:mm", Locale.UK);
                        Constants.order_status_date=object.getData().getOrderDate();
                        Constants.lastDeliveryTime0 = format.format(now2.getTime());
                        Constants.lastDeliveryTime40 = format.format(now.getTime());
                        Constants.lastDeliveryTime15 = format.format(now1.getTime());
                        Intent in = new Intent(getApplicationContext(), OrderStatusActivity.class);
                        in.putExtra("comingfrom", "Splash");
                        startActivity(in);
                        finish();
                        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                    }

                }else{
                    Intent in = new Intent(getApplicationContext(), SliderActivity.class);
                    startActivity(in);
                    finish();
                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                }

               /* if (pw != null)
                    pw.dismiss();*/


            }

            @Override
            public void invalid() {
                Intent in = new Intent(getApplicationContext(), SliderActivity.class);
                startActivity(in);
                finish();
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                /*if (pw != null)
                    pw.dismiss();*/

                //Toast.makeText(OrderStatusActivity.this, "Problem while fetching tracking list", Toast.LENGTH_LONG).show();

            }

            @Override
            public void failure() {
                Intent in = new Intent(getApplicationContext(), SliderActivity.class);
                startActivity(in);
                finish();
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                /*if (pw != null)
                    pw.dismiss();*/

                //Toast.makeText(OrderStatusActivity.this, "Error parsing tracking list", Toast.LENGTH_LONG).show();

            }

        });
    }
    private void dialogRateService() {
        final Dialog dialog = new Dialog(SplashActivity.this);
        Window window = dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_rate_us);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;

        dialog.getWindow().setAttributes(lp);
        /*DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        height = displaymetrics.heightPixels;
        width = displaymetrics.widthPixels;
        int dialogWidth = (int)(width * 0.85);
        int dialogHeight = (int) (height * 0.85);
        dialog.getWindow().setLayout(dialogWidth, dialogHeight);*/
        this.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        final ImageView img_time, img_professionalism, img_packing, img_other;
        final EditText ext_comment;
        final RatingBar ratingBar;
        final LinearLayout lin_parent_view, lin_comment;
        final TextView txt_comment, txt_qstn;
        Button btnSubmit;

        img_time=(ImageView)dialog.findViewById(R.id.img_time);
        img_professionalism=(ImageView)dialog.findViewById(R.id.img_professional);
        img_packing=(ImageView)dialog.findViewById(R.id.img_packing);
        img_other=(ImageView)dialog.findViewById(R.id.img_other);
        lin_parent_view=(LinearLayout)dialog.findViewById(R.id.lin_parent_view);
        lin_comment=(LinearLayout)dialog.findViewById(R.id.lin_comment);

        txt_qstn=(TextView)dialog.findViewById(R.id.txt_question);

        ext_comment=(EditText)dialog.findViewById(R.id.ext_comment);

        ratingBar=(RatingBar)dialog.findViewById(R.id.rating_bar);

        btnSubmit=(Button)dialog.findViewById(R.id.btn_submit);

        img_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strTappedView="clickedTime";
                if(!isintime){
                    img_time.setBackgroundResource(R.drawable.arrival_time_inactive);
                    isintime=true;
                }else{
                    img_time.setBackgroundResource(R.drawable.arrival_time_active);
                    isintime=false;
                }

            }
        });
        img_professionalism.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strTappedView="clickedProf";
                if(!isProf){
                    img_professionalism.setBackgroundResource(R.drawable.prof_icon_inactive);
                    isProf=true;
                }else{
                    img_professionalism.setBackgroundResource(R.drawable.prof_icon_active);
                    isProf=false;
                }

            }
        });
        img_packing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strTappedView="clickedPacking";
                if(!isPacking){
                    img_packing.setBackgroundResource(R.drawable.packing_icon_inactive);
                    isPacking=true;
                }else{
                    img_packing.setBackgroundResource(R.drawable.packing_icon_active);
                    isPacking=false;
                }
            }
        });
        img_other.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strTappedView="clickedOther";

                if(!isOther){
                    img_other.setBackgroundResource(R.drawable.other_icon_inactive);
                    isOther=true;
                    lin_comment.setVisibility(View.GONE);
                }else{
                    img_other.setBackgroundResource(R.drawable.others_icon_active);
                    isOther=false;
                    lin_comment.setVisibility(View.VISIBLE);
                }

            }
        });
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {

                /*if(strTappedView.equalsIgnoreCase("clickedTime")){
                    time_star=ratingBar.getRating();

                }else if(strTappedView.equalsIgnoreCase("clickedProf")){
                    prof_star=ratingBar.getRating();

                }else if(strTappedView.equalsIgnoreCase("clickedPacking")){
                    packing_star=ratingBar.getRating();

                }else if(strTappedView.equalsIgnoreCase("clickedOther")){
                    other_star=ratingBar.getRating();

                }else{
                    time_star=ratingBar.getRating();
                }*/
               // lin_parent_view.setVisibility(View.VISIBLE);

                txt_qstn.setVisibility(View.VISIBLE);
                if(lin_parent_view.getVisibility()!=View.VISIBLE){
                    slideToBottom(lin_parent_view);
                }

                if(ratingBar.getRating()>3){

                    txt_qstn.setText("What did we get right?");
                }else if(ratingBar.getRating()<3||ratingBar.getRating()==3){

                    txt_qstn.setText("What didn't work for you?");
                }
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String strCustId = sharedPreferenceUser.getString(Constants.strShPrefUserId, "");

                int rating= (int) ratingBar.getRating();
                int inTime=0, prof=0, packing=0,other=0;
                if(!isintime){
                    inTime=1;
                }
                if(!isProf){
                    prof=1;
                }
                if(!isPacking){
                    packing=1;
                }
                if(!isOther){
                    other=1;
                }

                /*if(strTappedView.equalsIgnoreCase("")){
                    utils.displayAlert("Provide rating on these views");
                }else*/
                if(ratingBar.getRating()==0){
                    utils.displayAlert("Provide your rating.");
                }/*else if(prof_star==0){
                    utils.displayAlert("Provide your rating on Professionalism");
                }else if(packing_star==0){
                    utils.displayAlert("Provide your rating on Packing");
                }else if(other_star==0){
                    utils.displayAlert("Provide your rating on Others");
                }*//*else if(ext_comment.getText().toString().equalsIgnoreCase("")){
                    utils.displayAlert("Provide your rating on Professionalism");
                }*/else{
                    serviceRating(strCustId, Constants.orderID, ext_comment.getText().toString().trim(), ""+rating, ""+inTime, ""+prof, ""+packing, ""+other, dialog);
                }

            }
        });
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    // To animate view slide out from top to bottom
    public void slideToBottom(View view) {
        Animation anim = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.down_from_top);
        view.startAnimation(anim);
        view.setVisibility(View.VISIBLE);
    }
    private void saveDeviceId() {
        restService.saveDeviceIdInstal(firebaseRegid, "android", new RestCallback<DeviceIdInstalRequest>() {
            @Override
            public void success(DeviceIdInstalRequest responce) {
                int reqStatus = responce.getStatus();
                int reqSuccess = responce.getSuccess();
                if (reqStatus == 200 && reqSuccess == 1) {
                    // Success
                    sharedPrefEditior = sharedPreferenceUser.edit();
                    sharedPrefEditior.putString(Constants.strShPrefUserDeviceReg, "yes");
                    sharedPrefEditior.commit();
                } else {

                }
            }

            @Override
            public void invalid() {
            }

            @Override
            public void failure() {
            }
        });
    }
    private void serviceRating(String customer_id, String order_id, String comment, String rating,String arrival, String professionalism, String packing, String other, final Dialog dialog) {
        pw = new ProgressDialog(SplashActivity.this);
        pw.show();
        pw.setMessage("Loading... Please wait.");
        restService.serviceRating(customer_id, order_id, comment, rating,arrival, professionalism, packing, other, new RestCallback<RatingSubmitRequest>() {
            @Override
            public void success(RatingSubmitRequest object) {
                if(object.getStatus()==200&object.getSuccess()==1){
                    displayAlertOnSuccessRate(object.getData().getSuccessMsg());
                    if(dialog!=null){
                        dialog.dismiss();
                    }
                }else
                {
                    utils.displayAlert(object.getErrorMsg());
                }

                if (pw != null)
                    pw.dismiss();


            }

            @Override
            public void invalid() {

                if (pw != null)
                    pw.dismiss();

                //Toast.makeText(OrderStatusActivity.this, "Problem while fetching tracking list", Toast.LENGTH_LONG).show();

            }

            @Override
            public void failure() {

                if (pw != null)
                    pw.dismiss();

                //Toast.makeText(OrderStatusActivity.this, "Error parsing tracking list", Toast.LENGTH_LONG).show();

            }

        });
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
                    splashHandlar(SPLASH_TIME_OUT);
                }
                //splashHandlar(SPLASH_TIME_OUT);
                break;
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    // Set Values IMEI & SECURE_ID
                    /*String strImei = getImei();
                    String strSecureId = getSecureId();
                    AppsFlyerLib.getInstance().setImeiData(strImei);
                    AppsFlyerLib.getInstance().setAndroidIdData(strSecureId);*/

                    permissionPhoneState();


                } else {
                    checkPermission();
                    // Permission Denied
                    permissionLocation();
                }
                splashHandlar(SPLASH_TIME_OUT);
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    public static boolean checkPermission(String strPermission,Context _c,Activity _a){
        int result = ContextCompat.checkSelfPermission(_c, strPermission);
        if (result == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
            return false;
        }
    }
    public void requestPermission(String strPermission, int perCode, Context _c, Activity _a){
        if (ActivityCompat.shouldShowRequestPermissionRationale(_a, strPermission)){
            ActivityCompat.requestPermissions(_a,new String[]{strPermission},perCode);
            //Toast.makeText(VisitorHomePageActivity.this,"GPS permission allows us to access location data. Please allow in App Settings for additional functionality.",Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(_a,new String[]{strPermission},perCode);
        }
    }
    private boolean checkPermission() {
        boolean flag = true;
        //String[] permissions = {"android.permission.READ_PHONE_STATE"};
        boolean hasPermission = (ContextCompat.checkSelfPermission(SplashActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int hasLocationPermission = checkSelfPermission(Manifest.permission.READ_PHONE_STATE);
                if (hasLocationPermission != PackageManager.PERMISSION_GRANTED) { requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_CODE_ASK_PERMISSIONS);
                    flag = false;
                    return false;
                }
            }
        } else {
            flag = true;
        }
        return flag;
    }

    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(SplashActivity.this);
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
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
                splashHandlar(6000);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                splashHandlar(SPLASH_TIME_OUT);
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    public void CheckLocationEnable()
    {
        if (Build.VERSION.SDK_INT >= 17)
        {
            LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            {
                showSettingsAlert();
            }
            else {
                splashHandlar(SPLASH_TIME_OUT);
            }
        }
        else
        {
            Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
            intent.putExtra("enabled", true);
            sendBroadcast(intent);
            splashHandlar(SPLASH_TIME_OUT);
        }
    }

    public String getImei(){
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String device_id = telephonyManager.getDeviceId();
        return device_id;
    }
    public String getSecureId(){
        TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String secure_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        return secure_id;
    }
    private void displayFirebaseRegId() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        firebaseRegid = pref.getString("regId", null);

        Log.e(TAG, "Firebase reg id: " + firebaseRegid);
    }



    /**
     * Called when the 'loadIMEI' function is triggered.
     */
    /*public void loadIMEI() {
        // Check if the READ_PHONE_STATE permission is already available.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            // READ_PHONE_STATE permission has not been granted.
            requestReadPhoneStatePermission();
        } else {
            // READ_PHONE_STATE permission is already been granted.
            doPermissionGrantedStuffs();
        }
    }*/



    /**
     * Requests the READ_PHONE_STATE permission.
     * If the permission has been denied previously, a dialog will prompt the user to grant the
     * permission, otherwise it is requested directly.
     */
    /*private void requestReadPhoneStatePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_PHONE_STATE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            new AlertDialog.Builder(LoadingActivity.this)
                    .setTitle("Permission Request")
                    .setMessage(getString(R.string.permission_read_phone_state_rationale))
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //re-request
                            ActivityCompat.requestPermissions(LoadingActivity.this,
                                    new String[]{Manifest.permission.READ_PHONE_STATE},
                                    MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
                        }
                    })
                    .setIcon(R.drawable.onlinlinew_warning_sign)
                    .show();
        } else {
            // READ_PHONE_STATE permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE},
                    MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
        }
    }*/

    public void displayAlertOnSuccessRate(String message)
    {
        // TODO Auto-generated method stub
        //message="To proceed, sign into your account.";
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SplashActivity.this);
        //alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setTitle("Stockup") ;
        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Intent intentLogin = new Intent(SplashActivity.this, SliderActivity.class);
                finish();
                startActivity(intentLogin);
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
            }
        });
        TextView myMsg = new TextView(SplashActivity.this);
        myMsg.setText(message);
        myMsg.setPadding(20, 20, 20, 20);
        myMsg.setTextSize(16);
        myMsg.setTypeface(typeFaceSegoeuiReg);
        myMsg.setTextColor(Color.BLACK);
        myMsg.setGravity(Gravity.CENTER|Gravity.CENTER_HORIZONTAL);
        alertDialogBuilder.setView(myMsg);

        TextView title = new TextView(SplashActivity.this);
        // You Can Customise your Title here
        title.setText("Stockup");
        title.setBackgroundColor(Color.TRANSPARENT);
        title.setPadding(15, 20, 15, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.BLACK);
        title.setVisibility(View.GONE);
        title.setTypeface(typeFaceSegoeuiBold);
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

        final Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        LinearLayout.LayoutParams positiveButtonLL = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
        positiveButtonLL.gravity = Gravity.CENTER|Gravity.CENTER_VERTICAL;
        positiveButton.setTextColor(Color.parseColor("#048BCD"));
        positiveButton.setLayoutParams(positiveButtonLL);
    }
    public void displayAlert(String message)
    {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SplashActivity.this);
        //alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setTitle("Alert!") ;
        alertDialogBuilder.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                finish();
                Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=" + SplashActivity.this.getPackageName());
                //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                try
                {
                    startActivity(intent);
                }
                catch (ActivityNotFoundException anfe)
                {
                }
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
            }
        });
        TextView myMsg = new TextView(SplashActivity.this);
        myMsg.setText(message);
        myMsg.setPadding(20, 25, 20, 10);
        myMsg.setTextSize(16);
        myMsg.setTextColor(Color.BLACK);
        myMsg.setGravity(Gravity.CENTER|Gravity.CENTER_HORIZONTAL);
        alertDialogBuilder.setView(myMsg);

        TextView title = new TextView(SplashActivity.this);
        // You Can Customise your Title here
        title.setText("Alert!");
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



        final Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        LinearLayout.LayoutParams positiveButtonLL = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
        positiveButtonLL.gravity = Gravity.CENTER|Gravity.CENTER_VERTICAL;
        positiveButton.setTextColor(Color.parseColor("#048BCD"));
        positiveButton.setLayoutParams(positiveButtonLL);



        // TODO Auto-generated method stub
       /* AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SplashActivity.this);
        //alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setTitle("Alert!") ;
        alertDialogBuilder.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                finish();
                Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=" + SplashActivity.this.getPackageName());
                //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                try
                {
                    startActivity(intent);
                }
                catch (ActivityNotFoundException anfe)
                {
                }
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
            }
        });
        TextView myMsg = new TextView(SplashActivity.this);
        myMsg.setText(message);
        myMsg.setPadding(20, 20, 20, 20);
        myMsg.setTextSize(16);
        myMsg.setTextColor(Color.BLACK);
        myMsg.setGravity(Gravity.CENTER|Gravity.CENTER_HORIZONTAL);
        alertDialogBuilder.setView(myMsg);

        TextView title = new TextView(SplashActivity.this);
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
        alertDialog.show();*/

        /*final Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        LinearLayout.LayoutParams positiveButtonLL = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
        positiveButtonLL.gravity = Gravity.CENTER|Gravity.CENTER_VERTICAL;
        positiveButton.setTextColor(Color.parseColor("#048BCD"));
        positiveButton.setLayoutParams(positiveButtonLL);*/

        /*final Button negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        negativeButton.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,0));
        LinearLayout.LayoutParams negativeButtonLL = (LinearLayout.LayoutParams) negativeButton.getLayoutParams();
        positiveButtonLL.gravity = Gravity.CENTER|Gravity.CENTER_VERTICAL;
        negativeButton.setTextColor(Color.parseColor("#048BCD"));
        negativeButton.setLayoutParams(negativeButtonLL);*/

    }



}
