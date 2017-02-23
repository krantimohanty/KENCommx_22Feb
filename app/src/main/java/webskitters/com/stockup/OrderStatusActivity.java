package webskitters.com.stockup;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.DisplayMetrics;
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
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.appsflyer.AppsFlyerLib;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import webskitters.com.stockup.Utils.Constants;
import webskitters.com.stockup.Utils.Utils;
import webskitters.com.stockup.model.OrderStatusRequest;
import webskitters.com.stockup.model.RateReviewRequest;
import webskitters.com.stockup.model.RatingSubmitRequest;
import webskitters.com.stockup.retrofit_call.RestCallback;
import webskitters.com.stockup.retrofit_call.RestService;
import webskitters.com.stockup.webview.StockupWebViewActivity;

//import com.appsflyer.AppsFlyerLib;

public class OrderStatusActivity extends AppCompatActivity {
    ProgressBar progress;
    ImageView imgBack;
    TextView tv_signin;
    private Utils utils;
    private TextView txt_order_details, txt_track_order;
    private String customer_id="", product_id="", product_image="", product_name="";
    SharedPreferences sharedPreferenceUser;
    private Typeface typeFaceSegoeuiReg;
    private Typeface typeFaceSegoeuiBold;

    TextView txt_name, txt_order_id, txt_order_place;
    private String customer_name="";
    private SharedPreferences shPrefUserSelection;
    String store_id="";
    Timer t;
    ImageView img_cat, img_gift_box, img_van, img_box;
    private NumberFormat nf;
    Runnable runnable;
    Handler handler = new Handler();
    TextView txt_on_the_way,txt_order_accepted;
    TextView txt_order_time, txt_accepted_time, txt_on_way_time,txt_order_date;
    private ProgressDialog pw;
    RestService restService;
    public static int height=0, width=0;
    boolean iscomplete=false;

    String comingFrom="";

    LinearLayout lin_bottom_bar;
    String strTappedView="";
    boolean isintime=true,isProf=true, isPacking=true, isOther=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_order_status_new);
        utils = new Utils(OrderStatusActivity.this);
        typeFaceSegoeuiReg = Typeface.createFromAsset(getAssets(),"Roboto-Regular.ttf");
        typeFaceSegoeuiBold = Typeface.createFromAsset(getAssets(),"ROBOTO-BOLD_0.TTF");
        sharedPreferenceUser = this.getSharedPreferences(Constants.strShPrefUserPrefName, Context.MODE_PRIVATE);
        customer_id = sharedPreferenceUser.getString(Constants.strShPrefUserId, "");
        customer_name = sharedPreferenceUser.getString(Constants.strShPrefUserFname, "");
        restService=new RestService(OrderStatusActivity.this);
        // Track Data : Add to activities where tracking needed
        AppsFlyerLib.getInstance().sendDeepLinkData(this);
        Bundle extras=getIntent().getExtras();
        if(extras!=null){
            comingFrom=extras.getString("comingfrom");
        }

        initFields();

        if(comingFrom.equalsIgnoreCase("CheckoutFinal")||comingFrom.equalsIgnoreCase("Splash")){
            img_van.setImageResource(R.drawable.check_active);
            img_cat.setImageResource(R.drawable.check_active);
            img_gift_box.setImageResource(R.drawable.check_active);

            if(AddToCartListAllItemsActivity.total_price==0){
                lin_bottom_bar.setVisibility(View.GONE);
            }

        }
        t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {

                try {
                    Thread.sleep(5000);
                    loadBubble2(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(5000);
                    loadBubble2(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(5000);
                    loadBubble2(3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                /*try {
                    Thread.sleep(5000);
                    loadBubble2(4);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/

            }

        },5000,15000);
    }
    public void loadBubble2(final int i) {
        runnable = new Runnable() {
            public void run() {

                if(i==1){
                    img_gift_box.setImageResource(R.drawable.check_active);
                    img_cat.setImageResource(R.drawable.check_active);
                }else if(i==2&&!iscomplete){
                    img_van.setImageResource(R.drawable.check_active);
                    img_cat.setImageResource(R.drawable.check_active);
                    img_gift_box.setImageResource(R.drawable.check_active);
                    orderStatus(Constants.orderID);
                }/*else if(i==3&!iscomplete){
                  img_box.setImageResource(R.drawable.check_active);
                  img_cat.setImageResource(R.drawable.check_active);
                  img_gift_box.setImageResource(R.drawable.check_active);
                  img_van.setImageResource(R.drawable.check_active);
                  //dialogRateService();
               }*/
            }
            // handler.postDelayed(this, 5000);
        };
        handler.postDelayed(runnable, 0); // for initial delay..*/

    }


    private void initFields() {

        nf= NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);
        nf.setRoundingMode(RoundingMode.HALF_UP);

        txt_order_time=(TextView)findViewById(R.id.txt_order_place_time);
        txt_order_time.setText(Constants.lastDeliveryTime0);
        txt_accepted_time=(TextView)findViewById(R.id.txt_order_accepted_time);
        txt_accepted_time.setText(Constants.lastDeliveryTime40);
        txt_on_way_time=(TextView)findViewById(R.id.txt_order_on_way_time);
        txt_on_way_time.setText(Constants.lastDeliveryTime15);
        txt_order_date=(TextView)findViewById(R.id.txt_order_date);
        if(Constants.order_status_date!=null){
            txt_order_date.setText("Order Placed : "+Constants.order_status_date);
        }
        txt_order_accepted=(TextView)findViewById(R.id.txt_order_accepted);
        //txt_order_accepted.setText("Order accepted and getting packed!\nExpect delivery at "+Constants.lastDeliveryTime40);
        customCheckBoxTextView1(txt_order_accepted);
        txt_on_the_way=(TextView)findViewById(R.id.txt_on_the_way);
        //txt_order_accepted.setText("Expected Delivery\n"+Constants.lastDeliveryTime40);
        //customCheckBoxTextView1(txt_order_accepted);
        img_cat=(ImageView)findViewById(R.id.img_cat);
        img_gift_box=(ImageView)findViewById(R.id.img_gift_box);
        img_van=(ImageView)findViewById(R.id.img_van);
        img_box=(ImageView)findViewById(R.id.img_box);

        progress=(ProgressBar)findViewById(R.id.progress);
        lin_bottom_bar=(LinearLayout)findViewById(R.id.lin_bottom_bar);
        txt_name=(TextView)findViewById(R.id.txt_name);
        txt_name.setText(customer_name);
        txt_order_id=(TextView)findViewById(R.id.txt_order_id);
        txt_order_id.setText("Order ID : "+Constants.orderID);
        //txt_price=(TextView)findViewById(R.id.txt_price);
        //txt_price.setText("R "+nf.format(AddToCartListAllItemsActivity.total_price));
        txt_order_place=(TextView)findViewById(R.id.txt_order_place);
        txt_order_place.setText("Order placed for delivery ASAP!");
        txt_order_details=(TextView)findViewById(R.id.txt_order_details);
        txt_order_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Constants.fromOrderStatus="Yes";
                Intent intent=new Intent(OrderStatusActivity.this, CheckoutFinalActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);

            }
        });
        txt_track_order=(TextView)findViewById(R.id.txt_track_order);
        txt_track_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(Constants.strTipPercent!=null)
                    Constants.strTipPercent="";
                if(Constants.deliverItToMe!=null) {
                    Constants.deliverItToMe = "";
                    Constants.deliverPersonName = "";
                    Constants.deliverPersonNumner = "";
                }
                Constants.deliverytypeCheckout="";
                Constants.deliverytypeCart="";
                Constants.deliveryDate="";
                Constants.deliveryDateCheckout="";
                Constants.fromOrderStatus="";
                Constants.promo_coupon_discount_price="";
                Constants.promo_coupon="";
                Constants.deliveryInstruction="";
                shPrefUserSelection = OrderStatusActivity.this.getSharedPreferences(Constants.strShPrefUserSelection, Context.MODE_PRIVATE);
                store_id = shPrefUserSelection.getString(Constants.strShUserProductId, "");


                if (store_id.equalsIgnoreCase("")) {

                    Intent intent = new Intent(OrderStatusActivity.this, PinCodeActivity.class);
                    finish();
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);

                } else {
                    Intent intent = new Intent(OrderStatusActivity.this, SubCategoryActivity.class);
                    finish();
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                }
            }
        });
    }


    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        //finish();
        //OrderStatusActivity.this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
    private void dialogRateService() {
        final Dialog dialog = new Dialog(OrderStatusActivity.this);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
        runnable=null;
        t.cancel();
    }

    private void serviceRating(String customer_id, String order_id, String comment, String rating,String arrival, String professionalism, String packing, String other, final Dialog dialog) {
        pw = new ProgressDialog(OrderStatusActivity.this);
        pw.show();
        pw.setMessage("Loading... Please wait.");
        restService.serviceRating(customer_id, order_id, comment, rating,arrival, professionalism, packing, other, new RestCallback<RatingSubmitRequest>() {
            @Override
            public void success(RatingSubmitRequest object) {
                if(object.getStatus()==200&object.getSuccess()==1){
                    displayAlert(object.getData().getSuccessMsg());
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

    private void orderStatus(String order_id) {
        //pw = new ProgressDialog(OrderStatusActivity.this);
        //pw.show();
        //pw.setMessage("Loading... Please wait.");
        restService.getOrderStatus(order_id, new RestCallback<OrderStatusRequest>() {
            @Override
            public void success(OrderStatusRequest object) {
                if(object.getStatus()==200&object.getSuccess()==1){
                    //displayAlert(object.getData().getSuccessMsg());

                    if(object.getData().getOrderStatus().toLowerCase().toString().equalsIgnoreCase("complete")){
                        dialogRateService();
                        img_box.setImageResource(R.drawable.check_active);
                        img_cat.setImageResource(R.drawable.check_active);
                        img_gift_box.setImageResource(R.drawable.check_active);
                        img_van.setImageResource(R.drawable.check_active);
                        iscomplete=true;
                    }else{

                    }

                }else{
                    //utils.displayAlert(object.getErrorMsg());
                }

               /* if (pw != null)
                    pw.dismiss();*/


            }

            @Override
            public void invalid() {

                /*if (pw != null)
                    pw.dismiss();*/

                //Toast.makeText(OrderStatusActivity.this, "Problem while fetching tracking list", Toast.LENGTH_LONG).show();

            }

            @Override
            public void failure() {

                /*if (pw != null)
                    pw.dismiss();*/

                //Toast.makeText(OrderStatusActivity.this, "Error parsing tracking list", Toast.LENGTH_LONG).show();

            }

        });
    }
    /*public void displayAlert(String message)
    {
        // TODO Auto-generated method stub
        //message="To proceed, sign into your account.";
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(OrderStatusActivity.this);
        //alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setTitle("Stockup") ;
        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Intent intentLogin = new Intent(OrderStatusActivity.this, PinCodeActivity.class);
                finish();
                startActivity(intentLogin);
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
            }
        });
        TextView myMsg = new TextView(OrderStatusActivity.this);
        myMsg.setText(message);
        myMsg.setPadding(20, 20, 20, 20);
        myMsg.setTextSize(16);
        myMsg.setTypeface(typeFaceSegoeuiReg);
        myMsg.setTextColor(Color.BLACK);
        myMsg.setGravity(Gravity.CENTER|Gravity.CENTER_HORIZONTAL);
        alertDialogBuilder.setView(myMsg);

        TextView title = new TextView(OrderStatusActivity.this);
        // You Can Customise your Title here
        title.setText("Stockup");
        title.setVisibility(View.GONE);
        title.setBackgroundColor(Color.TRANSPARENT);
        title.setPadding(15, 20, 15, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.BLACK);
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

    }*/
    private void getDialogCoverage() {
        final Dialog dialog = new Dialog(OrderStatusActivity.this);
        Window window = dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_age);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        TextView header=(TextView)dialog.findViewById(R.id.header);
        TextView msg=(TextView)dialog.findViewById(R.id.msg);
        Button btn_no=(Button)dialog.findViewById(R.id.btn_no);
        Button btn_yes=(Button)dialog.findViewById(R.id.btn_yes);
        btn_yes.setText("Ok");
        btn_no.setText("Cancel");

        header.setText("Stockup");
        msg.setText("Coming Soon");
        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    public void displayAlert(String message)
    {
        // TODO Auto-generated method stub
        //message="To proceed, sign into your account.";
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(OrderStatusActivity.this);
        //alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setTitle("Stockup") ;
        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Intent intentLogin = new Intent(OrderStatusActivity.this, LandingActivity.class);
                finish();
                startActivity(intentLogin);
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
            }
        });
        TextView myMsg = new TextView(OrderStatusActivity.this);
        myMsg.setText(message);
        myMsg.setPadding(20, 20, 20, 20);
        myMsg.setTextSize(16);
        myMsg.setTypeface(typeFaceSegoeuiReg);
        myMsg.setTextColor(Color.BLACK);
        myMsg.setGravity(Gravity.CENTER|Gravity.CENTER_HORIZONTAL);
        alertDialogBuilder.setView(myMsg);

        TextView title = new TextView(OrderStatusActivity.this);
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


    /*private void customCheckBoxTextView(TextView view) {
        String init = "Order accepted and getting packed!*//*\nExpected Delivery "+Constants.lastDeliveryTime40;
        String terms = "Expected Delivery "+Constants.lastDeliveryTime40;

        SpannableStringBuilder spanTxt = new SpannableStringBuilder(
                init);
        *//*spanTxt.append(terms);

        *//**//*spanTxt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                //callTermsCondition("file:///android_asset/Terms_and_Conditions.html", "Terms & Conditions");
                Intent urlPP = new Intent(OrderStatusActivity.this, StockupWebViewActivity.class);
                urlPP.putExtra("header","Terms and Conditions");
                urlPP.putExtra("url","http://stockup.co.za.dedi1025.jnb1.host-h.net/terms-condition");
                startActivity(urlPP);
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
            }
        }, init.length(), init.length() + terms.length(), 0);*//*
        spanTxt.setSpan(new RelativeSizeSpan(.8f),  init.length(), init.length() + terms.length(), 0);
        spanTxt.setSpan(new ForegroundColorSpan(Color.parseColor("#cc0909")), init.length(), init.length() + terms.length(), 0);

        view.setMovementMethod(LinkMovementMethod.getInstance());
        view.setText(spanTxt, TextView.BufferType.SPANNABLE);
    }*/
    private void customCheckBoxTextView1(TextView view) {
        String init = "Order accepted and getting packed!\n";
        String terms = "Expected Delivery "+Constants.lastDeliveryTime40;


        SpannableStringBuilder spanTxt = new SpannableStringBuilder(
                init);
        spanTxt.append(terms);

        /*spanTxt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                //callTermsCondition("file:///android_asset/Terms_and_Conditions.html", "Terms & Conditions");
                Intent urlPP = new Intent(OrderStatusActivity.this, StockupWebViewActivity.class);
                urlPP.putExtra("header","Terms and Conditions");
                urlPP.putExtra("url","http://stockup.co.za.dedi1025.jnb1.host-h.net/terms-condition");
                startActivity(urlPP);
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
            }
        }, init.length(), init.length() + terms.length(), 0);*/
        spanTxt.setSpan(new RelativeSizeSpan(1.2f),  init.length(), init.length() + terms.length(), 0);
        spanTxt.setSpan(new ForegroundColorSpan(Color.parseColor("#FBAE3C")), init.length(), init.length() + terms.length(), 0);

        view.setMovementMethod(LinkMovementMethod.getInstance());
        view.setText(spanTxt, TextView.BufferType.SPANNABLE);
    }
}
