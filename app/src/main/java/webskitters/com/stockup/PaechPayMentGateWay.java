package webskitters.com.stockup;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import webskitters.com.stockup.Utils.Constants;
import webskitters.com.stockup.model.PaymentStatusUpdateRequest;
import webskitters.com.stockup.retrofit_call.RestCallback;
import webskitters.com.stockup.retrofit_call.RestService;

/**
 * Created by Admin on 7/16/2016.
 */
public class PaechPayMentGateWay extends Activity {

    String itemName="", orderDescription="", orderID="", total_price="", email_add="";
    private ArrayList<String> post_val = new ArrayList<String>();
    private String post_Data="";
    WebView webView ;
    final Activity activity = this;
    private String tag = "PayMentGateWay";
    private String hash,hashSequence;
    ProgressDialog progressDialog ;



    static String getFirstName, getNumber, getEmailAddress, getRechargeAmt;


    ProgressDialog pDialog ;

    private Calendar cal;
    private int day, month, year;
    String date;

    SharedPreferences shPrefVisitorDetails;
    SharedPreferences.Editor toEdit;

    Context con;
    private RestService restService;
    ImageView img_back;
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
                finish();
                PaechPayMentGateWay.this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });
        restService=new RestService(this);
        con = PaechPayMentGateWay.this;

        progressDialog = new ProgressDialog(activity);

        cal = Calendar.getInstance();

        day = cal.get(Calendar.DAY_OF_MONTH);

        month = cal.get(Calendar.MONTH);
        year = cal.get(Calendar.YEAR);

        date=year + "-"+ (month + 1) + "-" + day;

        Intent oIntent  = getIntent();

        if(oIntent.getExtras()!=null) {
            itemName = oIntent.getExtras().getString("Name");
            total_price = oIntent.getExtras().getString("TotalAmount");
            orderID = oIntent.getExtras().getString("OrderID");
            orderDescription = oIntent.getExtras().getString("Description");
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
                    /*Intent intent = new Intent(PayMentGateWay.this, OrderStatusActivity.class);
                    intent.putExtra("strUrl","payfastreturn");
                    finish();
                    progressDialog = null;
                    startActivity(intent);*/

                    updateOrderPaymentStatus(orderID, "complete");


                }
                if (url.contains("payfastnotify")){
                    Intent intent = new Intent(PaechPayMentGateWay.this, OrderStatusActivity.class);
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
                else
                {
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
        String strPayFastUrl="";
        try {
            strPayFastUrl= request();
        }catch (Exception e){

        }

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
    private String request() throws IOException {
        // Create the order in your DB and get the ID

        /*URL url = new URL("https://test.oppwa.com/v1/registrations/{id}/payments");

        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);

        String data = ""
                + "authentication.userId=8a8294174e735d0c014e78cf266b1794"
                + "&authentication.password=qyyfHCN83e"
                + "&authentication.entityId=8a8294174e735d0c014e78cf26461790"
                + "&amount=92.00"
                + "&currency=EUR"
                + "&paymentType=DB";

        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        wr.writeBytes(data);
        wr.flush();
        wr.close();
        int responseCode = conn.getResponseCode();
        InputStream is;

        if (responseCode >= 400) is = conn.getErrorStream();
        else is = conn.getInputStream();

        return IOUtils.toString(is);*/

        URL url = new URL("https://test.oppwa.com/v1/checkouts");

        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);

        String data = ""
                + "authentication.userId=8a829417574ca9ad01574d095545025a"
                + "&authentication.password=538DKQAe5h"
                + "&authentication.entityId=    8a829417574ca9ad01574d10bd1a0272"
                + "&amount=92.00"
                + "&currency=ZAR"
                + "&paymentType=DB";

        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        wr.writeBytes(data);
        wr.flush();
        wr.close();
        int responseCode = conn.getResponseCode();
        InputStream is;

        if (responseCode >= 400) is = conn.getErrorStream();
        else is = conn.getInputStream();

        return IOUtils.toString(is);
    }
    private void updateOrderPaymentStatus(final String orderID, final String status){
        pDialog=new ProgressDialog(PaechPayMentGateWay.this);
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
                        PaechPayMentGateWay.this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
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
        final Dialog dialog = new Dialog(PaechPayMentGateWay.this);
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
                        Intent intent = new Intent(PaechPayMentGateWay.this, OrderStatusActivity.class);
                        startActivity(intent);
                        finish();
                        //Toast.makeText(getApplication(), "Payment successfull", Toast.LENGTH_LONG).show();
                        //getDialogAge(orderID, status);
                    } else {
                        //Toast.makeText(getApplication(), "Payment successfull", Toast.LENGTH_LONG).show();
                        if (Constants.strTipPercent != null)
                            Constants.strTipPercent = "";

                        Constants.deliverytypeCheckout = "";
                        Constants.deliverytypeCart = "";
                        Constants.deliveryDate = "";
                        Constants.deliveryDateCheckout = "";
                        Constants.promo_coupon_discount_price = "";
                        Constants.promo_coupon = "";
                        Intent intent = new Intent(PaechPayMentGateWay.this, LandingActivity.class);
                        startActivity(intent);
                        finish();

                    }
                PaechPayMentGateWay.this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
}
