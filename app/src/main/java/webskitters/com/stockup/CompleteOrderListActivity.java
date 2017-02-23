package webskitters.com.stockup;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.appsflyer.AppsFlyerLib;

import java.util.ArrayList;
import java.util.HashMap;

import webskitters.com.stockup.Utils.Constants;
import webskitters.com.stockup.Utils.Utils;
import webskitters.com.stockup.adapter.CompleteOrderListAdapter;
import webskitters.com.stockup.adapter.PastOrderListAdapter;
import webskitters.com.stockup.model.CompleteOrderListRequest;
import webskitters.com.stockup.model.PastOrderRequest;
import webskitters.com.stockup.retrofit_call.RestCallback;
import webskitters.com.stockup.retrofit_call.RestService;

//import com.appsflyer.AppsFlyerLib;

public class CompleteOrderListActivity extends AppCompatActivity {

    public static ArrayList<HashMap<String, String>> listCompleteOrders = new ArrayList<HashMap<String, String>>();

    RestService restService;
    private ProgressDialog pDialog;
    Utils utils;
    SharedPreferences sharedPreferenceUser;
    SharedPreferences.Editor sharedPrefEditior;

    public static String Key_order_id = "order_id";
    public static String Key_order_amount = "order_amount";
    public static String Key_currency = "currency";
    public static String Key_order_status = "order_status";
    public static String Key_order_date = "order_date";

    ImageView imgBack;
    ListView lv_complete_order;
    Typeface typeFaceSegoeuiReg;
    TextView txt_title, txt_order_hist;
    public static int height=0, width=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_complete_order_list);
        utils=new Utils(CompleteOrderListActivity.this);
        typeFaceSegoeuiReg = Typeface.createFromAsset(getAssets(),"Roboto-Regular.ttf");
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        height = displaymetrics.heightPixels;
        width = displaymetrics.widthPixels;
        restService=new RestService(this);
        utils = new Utils(CompleteOrderListActivity.this);

        // Track Data : Add to activities where tracking needed
        AppsFlyerLib.getInstance().sendDeepLinkData(this);

        initFields();
    }

    private void initFields() {
        sharedPreferenceUser=getSharedPreferences(Constants.strShPrefUserPrefName, Context.MODE_PRIVATE);

        imgBack=(ImageView)findViewById(R.id.img_back);

        txt_title=(TextView)findViewById(R.id.txt_top_bar);
        txt_title.setTypeface(typeFaceSegoeuiReg);
        txt_order_hist=(TextView)findViewById(R.id.txt_order_dtl);
        txt_order_hist.setTypeface(typeFaceSegoeuiReg);

        lv_complete_order = (ListView) findViewById(R.id.lv_complete_order);
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                CompleteOrderListActivity.this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });

        if (utils.isConnectionPossible()){
            String strCustId = sharedPreferenceUser.getString(Constants.strShPrefUserId, "");
            if (!strCustId.equalsIgnoreCase("")) {
                getPastOrder(strCustId);
            }
            else {
                
            }
        }
    }

    private void getPastOrder(final String strCustId) {
        final ProgressDialog pDialog=new ProgressDialog(CompleteOrderListActivity.this);
        pDialog.show();
        pDialog.setMessage("Loading...");
        restService.getCompleteOrder(strCustId, new RestCallback<CompleteOrderListRequest>() {
            @Override
            public void success(CompleteOrderListRequest responce) {
                int reqStatus = responce.getStatus();
                int reqSuccess = responce.getSuccess();
                if (reqStatus == 200 && reqSuccess == 1) {
                    int size = responce.getData().getOrderData().size();
                    listCompleteOrders = new ArrayList<HashMap<String, String>>();
                    for (int i = 0; i < size; i++) {
                        HashMap<String, String> mapPastOrdList = new HashMap<String, String>();
                        mapPastOrdList.put(Key_order_id, responce.getData().getOrderData().get(i).getOrderId().toString());
                        mapPastOrdList.put(Key_order_amount, responce.getData().getOrderData().get(i).getOrderAmount().toString());
                        mapPastOrdList.put(Key_currency, responce.getData().getOrderData().get(i).getCurrency().toString());
                        mapPastOrdList.put(Key_order_status, responce.getData().getOrderData().get(i).getOrderStatus().toString());
                        mapPastOrdList.put(Key_order_date, responce.getData().getOrderData().get(i).getOrderDate().toString());
                        listCompleteOrders.add(mapPastOrdList);
                    }
                    lv_complete_order.setAdapter(new CompleteOrderListAdapter(CompleteOrderListActivity.this, listCompleteOrders));
                } else {
                    //getDialogOK(responce.getErrorMsg());
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


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        CompleteOrderListActivity.this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}
