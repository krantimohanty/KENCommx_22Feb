package webskitters.com.stockup;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

//import com.appsflyer.AppsFlyerLib;

import com.appsflyer.AppsFlyerLib;

import webskitters.com.stockup.Utils.Constants;
import webskitters.com.stockup.Utils.Utils;
import webskitters.com.stockup.model.GetAddressRequest;
import webskitters.com.stockup.model.SaveAddressRequest;
import webskitters.com.stockup.retrofit_call.RestCallback;
import webskitters.com.stockup.retrofit_call.RestService;

public class MyAddressActivity extends AppCompatActivity {

    TextView txt_name, txt_Mobile, txt_address, txt_country_code;
    LinearLayout ll_find_from_map;
    Button btn_save, btn_search;
    ImageView img_back;

    RestService restService;
    Utils utils;
    SharedPreferences sharedPreferenceUser;
    SharedPreferences shPrefUserSelection;
    SharedPreferences shPrefDeliverAddr;
    SharedPreferences.Editor sharedPrefEditior;
    String forAct = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_my_address);
        restService = new RestService(this);
        utils = new Utils(MyAddressActivity.this);
        // Track Data : Add to activities where tracking needed
        AppsFlyerLib.getInstance().sendDeepLinkData(this);
        initFields();
    }

    private void initFields() {

        sharedPreferenceUser=getSharedPreferences(Constants.strShPrefUserPrefName, Context.MODE_PRIVATE);
        txt_name = (TextView) findViewById(R.id.txt_name);
        txt_Mobile = (TextView) findViewById(R.id.txt_Mobile);
        txt_country_code=(TextView)findViewById(R.id.txt_country_code);
        txt_address = (TextView) findViewById(R.id.txt_address);
        ll_find_from_map = (LinearLayout) findViewById(R.id.ll_search_from_map);
        btn_save = (Button) findViewById(R.id.btn_save);
        btn_search = (Button) findViewById(R.id.btn_search);
        img_back = (ImageView) findViewById(R.id.img_back);

        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyAddressActivity.this, MapActivity.class);
                intent.putExtra("for", "MyAddress");
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
            }
        });
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                MyAddressActivity.this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (forAct.equalsIgnoreCase("map")){
                    String strAddr=shPrefDeliverAddr.getString(Constants.strShPrefDelAddr, "");
                    String strLat=shPrefDeliverAddr.getString(Constants.strShPrefDelLat, "");
                    String strLong=shPrefDeliverAddr.getString(Constants.strShPrefDelLong, "");
                    String strCustId = sharedPreferenceUser.getString(Constants.strShPrefUserId, "");
                    saveAddress(strCustId, strLat, strLong, strAddr);
                }
            }
        });
        Bundle extras=getIntent().getExtras();
        if(extras!=null){
            forAct=extras.getString("for");
        }

        shPrefUserSelection = this.getSharedPreferences(Constants.strShPrefUserSelection, Context.MODE_PRIVATE);

        shPrefDeliverAddr = this.getSharedPreferences(Constants.strShPrefDelAddrName, Context.MODE_PRIVATE);
        String strMap=shPrefDeliverAddr.getString(Constants.strShPrefDelAddr, "");
        if (forAct.equalsIgnoreCase("map")) {
            txt_address.setText(strMap);
        }

        String strCustId = sharedPreferenceUser.getString(Constants.strShPrefUserId, "");
        String strCustFname = sharedPreferenceUser.getString(Constants.strShPrefUserFname, "");
        String strCustLname = sharedPreferenceUser.getString(Constants.strShPrefUserLname, "");
        String strCustPh = sharedPreferenceUser.getString(Constants.strShPrefUserPhone, "");
        String strCountryCode = sharedPreferenceUser.getString(Constants.strShPrefUserCountryCode, "");
        txt_name.setText(strCustFname + " " + strCustLname);

        txt_Mobile.setText(strCustPh);
        txt_country_code.setText("+"+strCountryCode);
        /*if(strCountryCode!=null&&!strCountryCode.equalsIgnoreCase("")&&!strCountryCode.isEmpty()){
            txt_country_code.setText("+"+strCountryCode);
        }else{
            txt_country_code.setText("+27");
        }*/

        if (!strCustId.equalsIgnoreCase("")){
            if (!forAct.equalsIgnoreCase("map")) {
                getMyAddress(strCustId);
            }
        }
        else {
            //getDialogOK("Please Signin to continue..");
        }
    }

    private void saveAddress(String strCustId, String strLat, String strLong, String strAddr) {
        final ProgressDialog pDialog=new ProgressDialog(MyAddressActivity.this);
        pDialog.show();
        pDialog.setMessage("Loading...");
        restService.saveAddress(strCustId, strAddr, strLat, strLong, new RestCallback<SaveAddressRequest>() {

            @Override
            public void success(SaveAddressRequest responce) {
                int reqStatus = responce.getStatus();
                int reqSuccess = responce.getSuccess();
                if (reqStatus == 200 && reqSuccess == 1) {
                    //displayAlert(responce.getData().getSuccessMessage());
                    displayAlertSaveAddress(responce.getData().getSuccessMessage());
                } else {
                    utils.displayAlert(responce.getErrorMsg());
                }
                pDialog.dismiss();
            }

            @Override
            public void invalid() {
                pDialog.dismiss();
            }

            @Override
            public void failure() {
                pDialog.dismiss();
            }
        });
    }

    private void getMyAddress(String strCustId) {
        final ProgressDialog pDialog=new ProgressDialog(MyAddressActivity.this);
        pDialog.show();
        pDialog.setMessage("Fetching your address..");
        restService.getAddress(strCustId, new RestCallback<GetAddressRequest>() {

            @Override
            public void success(GetAddressRequest responce) {
                int reqStatus = responce.getStatus();
                int reqSuccess = responce.getSuccess();
                if (reqStatus == 200 && reqSuccess == 1) {
                    String strAddress = responce.getData().getCustomerData().getAddress();
                    String strLat = responce.getData().getCustomerData().getLatitude();
                    String strLong = responce.getData().getCustomerData().getLongitude();
                    txt_address.setText(strAddress);

                } else {
                    if(!Constants.fromCheckout)
                    displayAlert(responce.getErrorMsg());
                    //getDialogOK(responce.getErrorMsg());
                }
                pDialog.dismiss();
            }

            @Override
            public void invalid() {
                pDialog.dismiss();
            }

            @Override
            public void failure() {
                pDialog.dismiss();
            }
        });
    }



    public void displayAlert(String message)
    {
        // TODO Auto-generated method stub
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MyAddressActivity.this);
        //alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setTitle("Stockup") ;
        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Intent intent = new Intent(MyAddressActivity.this, MapActivity.class);
                intent.putExtra("for", "MyAddress");
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
            }
        });
        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        TextView myMsg = new TextView(MyAddressActivity.this);
        myMsg.setText(message);
        myMsg.setPadding(20, 20, 20, 20);
        myMsg.setTextSize(16);
        myMsg.setTextColor(Color.BLACK);
        myMsg.setGravity(Gravity.CENTER|Gravity.CENTER_HORIZONTAL);
        alertDialogBuilder.setView(myMsg);

        TextView title = new TextView(MyAddressActivity.this);
        // You Can Customise your Title here
        title.setVisibility(View.GONE);
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

        final Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        LinearLayout.LayoutParams positiveButtonLL = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
        positiveButtonLL.gravity = Gravity.CENTER|Gravity.CENTER_VERTICAL;
        positiveButton.setTextColor(Color.parseColor("#048BCD"));
        positiveButton.setLayoutParams(positiveButtonLL);

    }
    public void displayAlertSaveAddress(String message)
    {
        // TODO Auto-generated method stub
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MyAddressActivity.this);
        //alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setTitle("Stockup") ;
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                if(Constants.fromCheckout){
                    Intent intent = new Intent(MyAddressActivity.this, CheckOutActivity.class);
                    startActivity(intent);
                    finish();
                }else
                {
                    finish();
                }
                Constants.fromCheckout=false;
                MyAddressActivity.this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });
        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
        }
        });
        TextView myMsg = new TextView(MyAddressActivity.this);
        myMsg.setText(message);
        myMsg.setPadding(20, 20, 20, 20);
        myMsg.setTextSize(16);
        myMsg.setTextColor(Color.BLACK);
        myMsg.setGravity(Gravity.CENTER|Gravity.CENTER_HORIZONTAL);
        alertDialogBuilder.setView(myMsg);

        TextView title = new TextView(MyAddressActivity.this);
        // You Can Customise your Title here
        title.setVisibility(View.GONE);
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

        final Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        LinearLayout.LayoutParams positiveButtonLL = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
        positiveButtonLL.gravity = Gravity.CENTER|Gravity.CENTER_VERTICAL;
        positiveButton.setTextColor(Color.parseColor("#048BCD"));
        positiveButton.setLayoutParams(positiveButtonLL);

    }
    @Override
    public void onBackPressed() {

            super.onBackPressed();
            finish();
            MyAddressActivity.this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

    }
}
