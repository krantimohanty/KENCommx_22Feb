package webskitters.com.stockup;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.appsflyer.AFInAppEventParameterName;
import com.appsflyer.AFInAppEventType;
import com.appsflyer.AppsFlyerLib;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import webskitters.com.stockup.Utils.Constants;
import webskitters.com.stockup.Utils.Utils;
import webskitters.com.stockup.adapter.AddToCartListAllItemAdapter;
import webskitters.com.stockup.model.PaymentStatusUpdateRequest;
import webskitters.com.stockup.model.ServiceTaxRequest;
import webskitters.com.stockup.retrofit_call.RestCallback;
import webskitters.com.stockup.retrofit_call.RestService;

/**
 * Created by Admin on 7/16/2016.
 */
public class PayMentGateWay extends Activity {

    String itemName="", orderDescription="", orderID="", total_price="", email_add="";
    WebView webView ;
    final Activity activity = this;
    private String tag = "PayMentGateWay";
    ProgressDialog progressDialog ;
    int error=0;
    ProgressDialog pDialog ;
    private Calendar cal;
    private int day, month, year;
    String date;
    SharedPreferences shPrefVisitorDetails;
    SharedPreferences.Editor toEdit;
    Context con;
    private RestService restService;
    ImageView img_back;
    Utils utils;


    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.activity_payment_gateway);
        img_back=(ImageView)findViewById(R.id.img_back);
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                getDialogConfirm("Would you like to cancel your order payment?");
                /*updateOrderPaymentStatus(orderID, "cancel");
                finish();
                PayMentGateWay.this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);*/
            }
        });
        restService=new RestService(this);
        utils=new Utils(PayMentGateWay.this);
        con = PayMentGateWay.this;

        progressDialog = new ProgressDialog(activity);
        shPrefVisitorDetails = this.getSharedPreferences("SP-SHENANIGANS", Context.MODE_PRIVATE);
        cal = Calendar.getInstance();

        day = cal.get(Calendar.DAY_OF_MONTH);

        month = cal.get(Calendar.MONTH);
        year = cal.get(Calendar.YEAR);

        date=year + "-"+ (month + 1) + "-" + day;

        Intent oIntent  = getIntent();

        if(oIntent.getExtras()!=null) {
            //itemName = oIntent.getExtras().getString("Name");
            total_price = oIntent.getExtras().getString("TotalAmount");
            orderID = oIntent.getExtras().getString("OrderID");
            //orderDescription = oIntent.getExtras().getString("Description");
            email_add = oIntent.getExtras().getString("EmailAdd");
        }
        //webView = new WebView(this);
        webView = (WebView) findViewById(R.id.webview);

        webView.setWebViewClient(new MyWebViewClient(){

            boolean loadingFinished = true;
            boolean redirect = false;
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains("payfastcancel")){
                    /*Intent intent = new Intent(PayMentGateWay.this, LandingActivity.class);
                    intent.putExtra("strUrl","payfastcancel");
                    finish();
                    progressDialog = null;
                    startActivity(intent);*/

                    updateOrderPaymentStatus(orderID, "cancel");
                }
                if (url.contains("payfastreturn")){

                    Map<String, Object> eventValue = new HashMap<String, Object>();
                    eventValue.put(AFInAppEventParameterName.REVENUE, total_price);
                    /*eventValue.put(AFInAppEventParameterName.PRICE, total_price);
                    eventValue.put(AFInAppEventParameterName.CONTENT_ID, "");
                    eventValue.put(AFInAppEventParameterName.CONTENT_TYPE, "");*/
                    eventValue.put(AFInAppEventParameterName.CURRENCY, "ZAR");
                    /*eventValue.put(AFInAppEventParameterName.RECEIPT_ID, orderID);*/
                    AppsFlyerLib.getInstance().trackEvent(PayMentGateWay.this.getApplicationContext(), AFInAppEventType.PURCHASE, eventValue);
                    //utils.trackEvent(PayMentGateWay.this.getApplicationContext(), AFInAppEventType.PURCHASE, eventValue);
                    updateOrderPaymentStatus(orderID, "complete");


                }
                if (url.contains("payfastnotify")){
                    Intent intent = new Intent(PayMentGateWay.this, OrderStatusActivity.class);
                    intent.putExtra("strUrl","payfastnotify");
                    finish();
                    progressDialog = null;
                    startActivity(intent);
                }

                if (!loadingFinished) {
                    redirect = true;
                }
                loadingFinished = false;
                view.loadUrl(url);
                return true;
            }
            @Override
            public void onPageFinished(WebView view, final String url) {
                super.onPageFinished(view, url);
                Log.d("onPageFinished", "--------->called");

                if(!redirect){
                    loadingFinished = true;
                }
                if(loadingFinished && !redirect){
                    if (progressDialog!=null && con!=null)
                        progressDialog.dismiss();
                }
                else{
                    redirect = false;
                }
            }
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Log.d("onPageStarted","---------->called");
                loadingFinished = false;
                //make sure dialog is showing
                if(progressDialog!=null && !progressDialog.isShowing()){
                    progressDialog.show();
                }
            }
        });

        webView.setVisibility(View.VISIBLE);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setCacheMode(2);
        webView.getSettings().setDomStorageEnabled(true);
        webView.clearHistory();
        webView.clearCache(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setUseWideViewPort(false);
        webView.getSettings().setLoadWithOverviewMode(false);
        String strPayFastUrl = makeParams();
        webView.loadUrl(strPayFastUrl);
    }

    private class MyWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

         if(url.startsWith("http")){
                //Toast.makeText(getApplicationContext(),url ,Toast.LENGTH_LONG).show();
                progressDialog.show();
                view.loadUrl(url);
                System.out.println("myresult "+url);
                //return true;
            } else {
                return false;
            }

            return true;
        }
    }
    public String makeParams(){
        // Create the order in your DB and get the ID

        String amount = total_price;
        String orderId = orderID;
        String name = "Stockup order";
        String description = "Refer to order";

        String site = "";
        String merchant_id = "";
        String merchant_key = "";

        // Check if we are using the test or live system
        //String paymentMode = "test";
        String paymentMode = "live";

        if(paymentMode.equalsIgnoreCase("test"))
        {
            site = "https://sandbox.payfast.co.za/eng/process?";
            merchant_id = "10000100";
            merchant_key = "46f0cd694581a";
        }
        else if(paymentMode.equalsIgnoreCase("live"))
        {
            site = "https://www.payfast.co.za/eng/process?";
            merchant_id = "10148454";
            merchant_key = "7v63bukqpvyp4";
        }
        else
        {
           Toast.makeText(PayMentGateWay.this,"Cannot process payment if PaymentMode (in web.config) value is unknown.",Toast.LENGTH_LONG);
        }

        // Build the query string for payment site

        StringBuilder str = new StringBuilder();
        str.append( "merchant_id=" + merchant_id);
        str.append( "&merchant_key=" + merchant_key);
        str.append( "&return_url=" + Constants.return_url);
        str.append( "&cancel_url=" + Constants.cancel_url);
        str.append( "&notify_url=" + Constants.notify_url);

        str.append( "&m_payment_id=" + orderId);
        str.append( "&amount=" +amount);
        str.append( "&item_name=" +name);
        str.append( "&item_description=" +description);

        //https://sandbox.payfast.co.za/eng/process?merchant_id=10000100&merchant_key=46f0cd694581a&m_payment_id=100001458&amount=1000&item_name=PArtha&item_description=testPay

        return site+str.toString();

    }
    private void updateOrderPaymentStatus(final String orderID, final String status){
        pDialog=new ProgressDialog(PayMentGateWay.this);
        pDialog.setMessage("Loading...");
        pDialog.setCanceledOnTouchOutside(false);
        pDialog.show();
        restService=new RestService(this);
        restService.updateOrderPaymentStatus(orderID, status,new RestCallback<PaymentStatusUpdateRequest>() {
            @Override
            public void success(PaymentStatusUpdateRequest obj) {


                    if (obj.getSuccess() == 1 && obj.getStatus() == 200&&status.toLowerCase().equalsIgnoreCase("complete")) {
                       /* if (Constants.deliverytypeCheckout.toLowerCase().equalsIgnoreCase("asap")) {
                            Intent intent = new Intent(PayMentGateWay.this, OrderStatusActivity.class);
                            startActivity(intent);
                            finish();
                            Toast.makeText(getApplication(), "Payment successfull", Toast.LENGTH_LONG).show();

                        } else {
                            Toast.makeText(getApplication(), "Payment successfull", Toast.LENGTH_LONG).show();
                            if (Constants.strTipPercent != null)
                                Constants.strTipPercent = "";

                            Constants.deliverytypeCheckout = "";
                            Constants.deliverytypeCart = "";
                            Constants.deliveryDate = "";
                            Constants.deliveryDateCheckout = "";
                            Constants.promo_coupon_discount_price = "";
                            Constants.promo_coupon = "";
                            Intent intent = new Intent(PayMentGateWay.this, LandingActivity.class);
                            startActivity(intent);
                            finish();

                        }
                        PayMentGateWay.this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);*/
                        getDialogAge(orderID, status);
                    } else {
                        Toast.makeText(getApplicationContext(), "Transaction Cancelled", Toast.LENGTH_LONG).show();

                        finish();
                        PayMentGateWay.this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    }

                if(pDialog!=null){
                    pDialog.dismiss();
                }
                //}
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

    private void getDialogAge(String orderid, final String status) {
        final Dialog dialog = new Dialog(PayMentGateWay.this);
        Window window = dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_place_order);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        TextView header=(TextView)dialog.findViewById(R.id.header);
        //header.setTypeface(typeFaceSegoeuiBold);
        TextView msg=(TextView)dialog.findViewById(R.id.msg);
        //msg.setTypeface(typeFaceSegoeuiReg);
        TextView btn_yes=(TextView)dialog.findViewById(R.id.btn_ok);
        AddToCartListAllItemsActivity.size=0;
        Constants.orderID=orderid;
        //header.setText("Error");
        msg.setText("Your order " + orderid + " has been successfully placed. We are soooo onto it!");

        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dialog!=null){
                    dialog.dismiss();
                }
                if(status.equalsIgnoreCase("complete"))
                    if (Constants.deliverytypeCheckout.toLowerCase().equalsIgnoreCase("asap")) {
                        Intent intent = new Intent(PayMentGateWay.this, OrderStatusActivity.class);
                        startActivity(intent);
                        finish();
                        //Toast.makeText(getApplication(), "Payment successfull", Toast.LENGTH_LONG).show();
                        //getDialogAge(orderID, status);
                    } else {
                        //Toast.makeText(getApplication(), "Payment successfull", Toast.LENGTH_LONG).show();

                        Constants.deliverytypeCheckout = "";
                        Constants.deliverytypeCart = "";
                        Constants.deliveryDate = "";
                        Constants.deliveryDateCheckout = "";
                        Constants.promo_coupon_discount_price = "";
                        Constants.promo_coupon = "";
                        Intent intent = new Intent(PayMentGateWay.this, LandingActivity.class);
                        startActivity(intent);
                        finish();

                    }
                if (Constants.strTipPercent != null)
                    Constants.strTipPercent = "";

                PayMentGateWay.this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        getDialogConfirm("Would you like to cancel your order payment?");
    }

    private void getDialogConfirm(String msg) {
        final Dialog dialog = new Dialog(PayMentGateWay.this);
        Window window = dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_exit);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        //TextView header=(TextView)dialog.findViewById(R.id.header);
        TextView txt_msg = (TextView) dialog.findViewById(R.id.msg);
        txt_msg.setText(msg);
        TextView txt_date = (TextView) dialog.findViewById(R.id.txt_date);
        txt_date.setVisibility(View.GONE);

        final Button btn_yes=(Button)dialog.findViewById(R.id.btn_yes);
        btn_yes.setText("YES");
        Button btn_no=(Button)dialog.findViewById(R.id.btn_no);
        btn_no.setText("NO");

        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                updateOrderPaymentStatus(orderID, "cancel");
                finish();
                PayMentGateWay.this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });
        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
}
