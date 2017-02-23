package webskitters.com.stockup;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

//import com.appsflyer.AppsFlyerLib;

import com.appsflyer.AFInAppEventParameterName;
import com.appsflyer.AFInAppEventType;
import com.appsflyer.AppsFlyerLib;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import webskitters.com.stockup.Utils.Constants;
import webskitters.com.stockup.Utils.Utils;
import webskitters.com.stockup.adapter.AddToCartListAllItemAdapter;
import webskitters.com.stockup.adapter.TipCatSpinnerAdapter;
import webskitters.com.stockup.model.ApplyCouponCodeRequest;
import webskitters.com.stockup.model.CartTotalRequest;
import webskitters.com.stockup.model.GetAddressRequest;
import webskitters.com.stockup.model.GetDeliveryFeeRequest;
import webskitters.com.stockup.model.ServiceTaxRequest;
import webskitters.com.stockup.retrofit_call.RestCallback;
import webskitters.com.stockup.retrofit_call.RestService;
import webskitters.com.stockup.webview.StockupWebViewActivity;

/**
 * Created by android on 9/21/2016.
 */
public class CheckOutActivity extends AppCompatActivity {

    private Typeface typeFaceSegoeuiReg;
    private Typeface typeFaceSegoeuiBold;
    Utils utils;
    SharedPreferences shPrefDeliverAddr;
    SharedPreferences.Editor toEditAddr;
    String fname="", lName="", shipingAdd="", bilingAdd="", forshipingbiling="";
    Bundle extras;
    ImageView imgBack;
    RelativeLayout rel_add, rel_pay_with, rel_delivery_type, rel_tips;
    private TextView txt_addr, txt_addr_right, txt_card;
    String isAsap="";
    View vw_tips;
    private PopupWindow pw;
    int width=0, height=0;
    TextView txt_tips, txt_subheader;
    TextView txt_next;
    private String strIsCard="";
    private String strMap;
    private TextView txt_delivery_type, txt_sub_total, txt_tip_percent, txt_total;
    private double tip=0;
    NumberFormat nf;
    private RestService restService;
    TextView txt_delivery_fee;
    private TextView txt_tip;
    private ProgressDialog pDialog;
    private SharedPreferences sharedPreferenceUser;
    String customer_id="";
    SharedPreferences shPrefUserSelection;
    SharedPreferences.Editor toEdit;
    LinearLayout lin_cart;
    ImageView imgCart;
    Button btn_count;
    EditText ext_comment;
    TextView txt_delivery_free_view;
    ScrollView scrollview;
    EditText ext_promo_code;
    TextView txt_apply_promo;
    TextView txt_promo_code_discount_price, txt_promo_code_name;
    CheckBox chk_deliver_it_to_me;
    LinearLayout lin_name, lin_contact_num;
    EditText ext_name, ext_surname, ext_number, ext_counrty_code;
    View vw_name, vw_number;
    LinearLayout ll_approve, ll_reject;
    ImageView img_apprv, img_reject;
    String isInRange="";
    private String store_id="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_check_out);
        sharedPreferenceUser=this.getSharedPreferences(Constants.strShPrefUserPrefName, Context.MODE_PRIVATE);
        customer_id=sharedPreferenceUser.getString(Constants.strShPrefUserId, "");
        shPrefDeliverAddr = this.getSharedPreferences(Constants.strShPrefDelAddrName, Context.MODE_PRIVATE);
        isInRange = shPrefDeliverAddr.getString(Constants.strShPrefDeliver, "");
        typeFaceSegoeuiReg = Typeface.createFromAsset(getAssets(),"Roboto-Regular.ttf");
        typeFaceSegoeuiBold = Typeface.createFromAsset(getAssets(),"ROBOTO-BOLD_0.TTF");
        utils=new Utils(CheckOutActivity.this);
        // Track Data : Add to activities where tracking needed
        AppsFlyerLib.getInstance().sendDeepLinkData(this);
        shPrefDeliverAddr = this.getSharedPreferences(Constants.strShPrefDelAddrName, Context.MODE_PRIVATE);
        toEditAddr=shPrefDeliverAddr.edit();

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;

        nf= NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);
        nf.setRoundingMode(RoundingMode.HALF_UP);

        initFields();

        if(!customer_id.equalsIgnoreCase("")){
            getCartTotal(customer_id);
            getMyAddress(customer_id);
            //displayAlert("Address");
        }
    }

    private void getMyAddress(String strCustId) {
        final ProgressDialog pDialog=new ProgressDialog(CheckOutActivity.this);
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
                    //txt_address.setText(strAddress);
                } else {
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
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CheckOutActivity.this);
        //alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setTitle("Stockup") ;
        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Intent intent = new Intent(CheckOutActivity.this, MyAddressActivity.class);
                //intent.putExtra("for", "MyAddress");
                Constants.fromCheckout=true;
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
        TextView myMsg = new TextView(CheckOutActivity.this);
        myMsg.setText(message);
        myMsg.setPadding(20, 20, 20, 20);
        myMsg.setTextSize(16);
        myMsg.setTextColor(Color.BLACK);
        myMsg.setGravity(Gravity.CENTER|Gravity.CENTER_HORIZONTAL);
        alertDialogBuilder.setView(myMsg);

        TextView title = new TextView(CheckOutActivity.this);
        // You Can Customise your Title here
        title.setText("Stockup");
        title.setVisibility(View.GONE);
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
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,1));
        LinearLayout.LayoutParams positiveButtonLL = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
        positiveButtonLL.gravity = Gravity.CENTER|Gravity.CENTER_VERTICAL;
        positiveButton.setTextColor(Color.parseColor("#048BCD"));
        positiveButton.setLayoutParams(positiveButtonLL);

        final Button negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        negativeButton.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,1));
        LinearLayout.LayoutParams negativeButtonLL = (LinearLayout.LayoutParams) negativeButton.getLayoutParams();
        positiveButtonLL.gravity = Gravity.CENTER|Gravity.CENTER_VERTICAL;
        negativeButton.setTextColor(Color.parseColor("#048BCD"));
        negativeButton.setLayoutParams(negativeButtonLL);

    }
    private void initFields() {

        shPrefDeliverAddr = this.getSharedPreferences(Constants.strShPrefDelAddrName, Context.MODE_PRIVATE);
        strIsCard=shPrefDeliverAddr.getString("is_card_details_inputed", "");
        strMap=shPrefDeliverAddr.getString(Constants.strShPrefDelCallFrom, "No");
        //radioGroup= (RadioGroup)findViewById(R.id.rdgrp_payment);
        ///////////////Peach Payment//////////////////
        /*ll_approve = (LinearLayout) findViewById(R.id.ll_approve);
        ll_reject = (LinearLayout) findViewById(R.id.ll_reject);
        img_apprv = (ImageView) findViewById(R.id.img_apprv);
        img_reject = (ImageView) findViewById(R.id.img_reject);
        ll_approve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                img_apprv.setImageDrawable(getResources().getDrawable(R.drawable.active));
                img_reject.setImageDrawable(getResources().getDrawable(R.drawable.normal));
                Constants.paymentType="Peach Payment";
            }
        });
        ll_reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                img_apprv.setImageDrawable(getResources().getDrawable(R.drawable.normal));
                img_reject.setImageDrawable(getResources().getDrawable(R.drawable.active));
                Constants.paymentType="Payfast";
            }
        });

        if(Constants.paymentType.equalsIgnoreCase("Peach Payment")){
            img_apprv.setImageDrawable(getResources().getDrawable(R.drawable.active));
            img_reject.setImageDrawable(getResources().getDrawable(R.drawable.normal));
            Constants.paymentType="Peach Payment";
        }else  if(Constants.paymentType.equalsIgnoreCase("Payfast")){
            img_apprv.setImageDrawable(getResources().getDrawable(R.drawable.normal));
            img_reject.setImageDrawable(getResources().getDrawable(R.drawable.active));
            Constants.paymentType="Payfast";
        }*/
        /*radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                View radioButton = radioGroup.findViewById(i);
                int index = radioGroup.indexOfChild(radioButton);
            }
        });*/
        lin_name=(LinearLayout)findViewById(R.id.lin_name);
        lin_contact_num=(LinearLayout)findViewById(R.id.lin_contact_num);
        ext_name=(EditText)findViewById(R.id.ext_name);
        ext_surname=(EditText)findViewById(R.id.ext_surname);
        vw_name=(View)findViewById(R.id.view_name);

        ext_number=(EditText)findViewById(R.id.ext_num);
        ext_counrty_code=(EditText)findViewById(R.id.ext_country_code);
        vw_number=(View)findViewById(R.id.view_num);

        chk_deliver_it_to_me=(CheckBox)findViewById(R.id.chk_deliver_it_to_me);
        chk_deliver_it_to_me.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    lin_name.setVisibility(View.VISIBLE);
                    vw_name.setVisibility(View.VISIBLE);
                    lin_contact_num.setVisibility(View.VISIBLE);
                    vw_number.setVisibility(View.VISIBLE);
                    Constants.deliverItToMe="No";

                    Constants.deliverPersonName=ext_name.getText().toString();
                    Constants.deliverPersonSurName=ext_surname.getText().toString();
                    Constants.deliverPersonCountryCode=ext_counrty_code.getText().toString();
                    Constants.deliverPersonNumner=ext_number.getText().toString();

                }else
                {
                    lin_name.setVisibility(View.GONE);
                    vw_name.setVisibility(View.GONE);
                    lin_contact_num.setVisibility(View.GONE);
                    vw_number.setVisibility(View.GONE);
                    Constants.deliverItToMe="Yes";
                    Constants.deliverPersonName="";
                    Constants.deliverPersonSurName="";
                    Constants.deliverPersonCountryCode="";
                    Constants.deliverPersonNumner="";
                }
            }
        });

        if(Constants.deliverItToMe.equalsIgnoreCase("No")){
            lin_name.setVisibility(View.VISIBLE);
            vw_name.setVisibility(View.VISIBLE);
            lin_contact_num.setVisibility(View.VISIBLE);
            vw_number.setVisibility(View.VISIBLE);
            Constants.deliverItToMe="No";
            ext_name.setText(Constants.deliverPersonName);
            ext_surname.setText(Constants.deliverPersonSurName);
            ext_counrty_code.setText(Constants.deliverPersonCountryCode);
            ext_number.setText(Constants.deliverPersonNumner);
            chk_deliver_it_to_me.setChecked(true);
        }else if(Constants.deliverItToMe.equalsIgnoreCase("Yes")){
            lin_name.setVisibility(View.GONE);
            vw_name.setVisibility(View.GONE);
            lin_contact_num.setVisibility(View.GONE);
            vw_number.setVisibility(View.GONE);
            Constants.deliverItToMe="Yes";
            chk_deliver_it_to_me.setChecked(false);
            Constants.deliverPersonName="";
            Constants.deliverPersonNumner="";
            Constants.deliverPersonSurName="";
            Constants.deliverPersonCountryCode="";
            ext_surname.setText(Constants.deliverPersonSurName);
            ext_counrty_code.setText(Constants.deliverPersonCountryCode);
            ext_name.setText(Constants.deliverPersonName);
            ext_number.setText(Constants.deliverPersonNumner);
        }

        txt_delivery_fee=(TextView)findViewById(R.id.txt_delivery_fee);
        txt_delivery_free_view=(TextView)findViewById(R.id.txt_delivery_free_view);
        scrollview=(ScrollView)findViewById(R.id.scrollview);
        txt_sub_total=(TextView)findViewById(R.id.txt_sub_total);
        txt_sub_total.setText("R"+nf.format(AddToCartListAllItemsActivity.total_price));
        txt_tip_percent=(TextView)findViewById(R.id.txt_tip_percent);
        txt_total=(TextView)findViewById(R.id.txt_total);
        txt_total.setText("R"+nf.format(AddToCartListAllItemsActivity.total_price));
        imgBack=(ImageView)findViewById(R.id.img_back);
        ext_comment=(EditText) findViewById(R.id.ext_comment);

        if(Constants.deliveryInstruction!=null){

            ext_comment.setText(Constants.deliveryInstruction);
        }

        ext_promo_code=(EditText)findViewById(R.id.ext_promo_code);
        if(Constants.promo_coupon!=null&&!Constants.promo_coupon.equalsIgnoreCase("")){
            ext_promo_code.setText(Constants.promo_coupon);
        }

        txt_apply_promo=(TextView)findViewById(R.id.txt_apply_promo_code);
        txt_promo_code_name=(TextView)findViewById(R.id.txt_promo_code);
        txt_promo_code_discount_price=(TextView)findViewById(R.id.txt_promo_code_price);

        if(Constants.promo_coupon_discount_price!=null&&!Constants.promo_coupon_discount_price.equalsIgnoreCase("")){
            txt_promo_code_discount_price.setText("-R"+Constants.promo_coupon_discount_price);
            txt_apply_promo.setText("  Remove  ");
            txt_promo_code_name.setText("Promo Code ("+Constants.promo_coupon+")");
        }

        txt_apply_promo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!ext_promo_code.getText().toString().trim().equalsIgnoreCase("")&&!ext_promo_code.getText().toString().isEmpty()&&ext_promo_code.getText().toString().length()>0&&txt_apply_promo.getText().toString().contains("Apply")){
                    getPromoDiscountPrice("coupon321", Constants.qoute_id, ext_promo_code.getText().toString());}
                else  if(!ext_promo_code.getText().toString().equalsIgnoreCase("")&&!ext_promo_code.getText().toString().isEmpty()&&ext_promo_code.getText().toString().length()>0&&txt_apply_promo.getText().toString().contains("Remove")){
                    removePromoDiscountPrice("coupon321", Constants.qoute_id, ext_promo_code.getText().toString());}
                else{
                    utils.displayAlert("Enter Promo Code.");
                }
            }
        });

        ext_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollview.scrollTo(0, height);
            }
        });

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CheckOutActivity.this, AddToCartListAllItemsActivity.class);
                startActivity(intent);
                finish();
                Constants.deliverytypeCheckout = "";
                Constants.deliverytypeCart = "";
                Constants.deliveryDate = "";
                Constants.deliveryDateCheckout = "";
                AddToCartListAllItemsActivity.size=0;
                Constants.promo_coupon_discount_price="";
                Constants.promo_coupon="";
                Constants.deliveryInstruction=ext_comment.getText().toString();
                CheckOutActivity.this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });

        imgCart=(ImageView)findViewById(R.id.img_cart_icon);
        lin_cart=(LinearLayout)findViewById(R.id.lin_cart);
        btn_count=(Button)findViewById(R.id.btn_count);
        lin_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CheckOutActivity.this, AddToCartListAllItemsActivity.class);
                intent.putExtra("context_act1", "webskitters.com.stockup.CheckOutActivity");
                startActivity(intent);
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                Constants.deliveryInstruction=ext_comment.getText().toString();
            }
        });
        txt_next=(TextView)findViewById(R.id.txt_next);
        txt_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isInRange.equalsIgnoreCase("true")) {
                    if (Constants.deliverItToMe.equalsIgnoreCase("No")) {
                        Constants.deliverItToMe = "No";
                        Constants.deliverPersonSurName = ext_surname.getText().toString().trim();
                        Constants.deliverPersonName = ext_name.getText().toString().trim();
                        Constants.deliverPersonCountryCode = ext_counrty_code.getText().toString().trim();
                        Constants.deliverPersonNumner = ext_number.getText().toString().trim();
                        ;
                        chk_deliver_it_to_me.setChecked(true);
                    }
                    Constants.deliveryInstruction = ext_comment.getText().toString();
                    if (!strMap.equalsIgnoreCase("Yes")) {
                        utils.displayAlert("Provide delivery address.");
                    }/*else if(!strIsCard.equalsIgnoreCase("Yes")){
                    utils.displayAlert("Provide valid payment details.");
                }*/ else if (Constants.deliverItToMe.equalsIgnoreCase("no") && (ext_name.getText().toString().equalsIgnoreCase("") || ext_name.getText().toString().isEmpty())) {
                        utils.displayAlert("Please provide all required recipient details.");
                    } else if (Constants.deliverItToMe.equalsIgnoreCase("no") && (ext_surname.getText().toString().equalsIgnoreCase("") || ext_surname.getText().toString().isEmpty())) {
                        utils.displayAlert("Please provide all required recipient details.");
                    } else if (Constants.deliverItToMe.equalsIgnoreCase("no") && (ext_counrty_code.getText().toString().equalsIgnoreCase("") || ext_counrty_code.getText().toString().isEmpty())) {
                        utils.displayAlert("Please provide all required recipient details.");
                        ext_counrty_code.setFocusable(true);
                    } else if (Constants.deliverItToMe.equalsIgnoreCase("no") && (ext_number.getText().toString().equalsIgnoreCase("") || ext_number.getText().toString().isEmpty())) {
                        utils.displayAlert("Please provide all required recipient details.");
                    } else if (Constants.deliverItToMe.equalsIgnoreCase("no") && ext_counrty_code.getText().toString().length() < 1) {
                        utils.displayAlert("Please provide correct country code.");
                        ext_counrty_code.setFocusable(true);
                    } else if (Constants.deliverItToMe.equalsIgnoreCase("no") && ext_number.getText().toString().length() < 1) {
                        utils.displayAlert("There seems to be an error in the format of the telephone number provided.");
                    } else if (!txt_delivery_type.getText().toString().equalsIgnoreCase("ASAP") && !txt_delivery_type.getText().toString().contains("No Rush")) {
                        utils.displayAlert("Please provide delivery times.");
                    } else if (txt_delivery_type.getText().toString().contains("No Rush") & (Constants.deliveryDate == null || Constants.deliveryDate.isEmpty())) {

                        utils.displayAlert("Please provide delivery times.");
                    } else {
                        ArrayList<String> arrIds = new ArrayList<String>();
                        ArrayList<String> arrQnt = new ArrayList<String>();

                        for (int i = 0; i < AddToCartListAllItemAdapter.listAddToCartList.size(); i++) {
                            arrIds.add(AddToCartListAllItemAdapter.listAddToCartList.get(i).get("product_id"));
                            arrQnt.add(AddToCartListAllItemAdapter.listAddToCartList.get(i).get("qty"));
                        }

                        Map<String, Object> eventValue = new HashMap<String, Object>();

                        //eventValue.put(AFInAppEventParameterName.REVENUE, txt_total.getText().toString());
                        eventValue.put(AFInAppEventParameterName.CONTENT_ID, arrIds);
                        eventValue.put(AFInAppEventParameterName.CONTENT_TYPE, "");
                        eventValue.put(AFInAppEventParameterName.CURRENCY, "ZAR");
                        eventValue.put(AFInAppEventParameterName.QUANTITY, arrQnt);
                        // utils.trackEvent(CheckOutActivity.this.getApplicationContext(), "af_initiated_checkout", eventValue);
                        AppsFlyerLib.getInstance().trackEvent(CheckOutActivity.this.getApplicationContext(), AFInAppEventType.INITIATED_CHECKOUT, eventValue);

                        Intent intent = new Intent(CheckOutActivity.this, CheckoutFinalActivity.class);
                        if (Constants.deliveryDate != null & !Constants.deliveryDate.isEmpty()) {
                            intent.putExtra("date", Constants.deliveryDate);
                        } else {
                            intent.putExtra("date", "Needed ASAP");
                        }
                        startActivity(intent);
                        finish();
                        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                    }
                }else{
                    getDialogBrowse("We are unable to process your order due to a lack of coverage at your delivery address.");
                }
            }
        });

        txt_subheader=(TextView)findViewById(R.id.txt_subheader);
        customCheckBoxTextView(txt_subheader);

        rel_add=(RelativeLayout)findViewById(R.id.rel_addr);
        rel_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CheckOutActivity.this, MapActivity.class);
                intent.putExtra("for", "Checkout");
                startActivity(intent);
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                Constants.deliveryInstruction=ext_comment.getText().toString();
            }
        });

        rel_pay_with=(RelativeLayout)findViewById(R.id.rel_pay_with);
        txt_card=(TextView)findViewById(R.id.txt_card);
        txt_card.setTypeface(typeFaceSegoeuiReg);
        //txt_card_right=(TextView)findViewById(R.id.txt_card_right);

        rel_pay_with.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent intent = new Intent(CheckOutActivity.this, AddPaymentActivity.class);
                intent.putExtra("for", "Checkout");
                startActivity(intent);
                //finish();
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);*/
                Constants.deliveryInstruction=ext_comment.getText().toString();
            }
        });

        rel_delivery_type=(RelativeLayout)findViewById(R.id.rel_delivery_type);
        txt_delivery_type=(TextView)findViewById(R.id.txt_delivery_type);
        txt_delivery_type.setTypeface(typeFaceSegoeuiReg);
        rel_delivery_type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constants.deliveryInstruction=ext_comment.getText().toString();
                if (!strMap.equalsIgnoreCase("Yes")) {
                    utils.displayAlert("Provide delivery address.");
                } else {
                    if (Constants.deliverytypeCart.equalsIgnoreCase("asap")) {
                        getDialogCoverage();
                    } else {
                        if (Constants.arrMixed != null && Constants.arrMixed.contains("ASAP") && Constants.arrMixed.contains("NO RUSH")) {
                            getDeliverytype();
                        } else {
                            Intent intent = new Intent(CheckOutActivity.this, DeliveryChargeActivity.class);
                            intent.putExtra("isasap", "No");
                            startActivity(intent);
                            finish();
                            Constants.deliverytypeCheckout = "No Rush";
                            overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                        }
                    }
                }
            }
        });

        txt_tips=(TextView)findViewById(R.id.txt_tips);
        txt_tip=(TextView)findViewById(R.id.txt_tip);
        txt_tip.setText("Tip your \"knock , knock\" team");
        vw_tips=(View)findViewById(R.id.view_tips);

        rel_tips=(RelativeLayout)findViewById(R.id.rel_tips);
        rel_tips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //hideSoftKeyboard(CheckOutActivity.this);
                callPopUpSpecialDiscount(vw_tips);
            }
        });


        txt_addr=(TextView)findViewById(R.id.txt_addr);
        txt_addr.setTypeface(typeFaceSegoeuiReg);
        txt_addr_right=(TextView)findViewById(R.id.txt_addr_right);

        String callFromMap = shPrefDeliverAddr.getString(Constants.strShPrefDelCallFrom, "No");
        if (callFromMap.equalsIgnoreCase("Yes")/*&&forshipingbiling.equalsIgnoreCase("shiping")*/){
            String strLat = shPrefDeliverAddr.getString(Constants.strShPrefDelLat,"");
            String strLong = shPrefDeliverAddr.getString(Constants.strShPrefDelLong,"");
            String strAddr = shPrefDeliverAddr.getString(Constants.strShPrefDelAddr,"");
            txt_addr.setText(strAddr);
            txt_addr_right.setVisibility(View.INVISIBLE);
            txt_delivery_type.setText("Select delivery method");
        }
       /* if (strIsCard.equalsIgnoreCase("Yes")){
            txt_card.setText(shPrefDeliverAddr.getString("nickname", ""));
            txt_card_right.setVisibility(View.INVISIBLE);
        }*/
        if(Constants.deliverytypeCheckout.equalsIgnoreCase("asap")){
            txt_delivery_type.setText("ASAP");
            /*rel_delivery_type.setClickable(true);
            rel_delivery_type.setEnabled(true);*/
        }

        else if (Constants.deliverytypeCheckout.contains("No Rush")){
            if(Constants.deliveryDate!=null&!Constants.deliveryDate.isEmpty())

                if(Constants.deliverytime.equalsIgnoreCase("")||Constants.deliverytime.isEmpty())
                {
                    txt_delivery_type.setText("No Rush: " +Constants.deliveryDay+", "+ Constants.deliveryDateCheckout.replace("-", "/"));
                }else
                {
                    txt_delivery_type.setText("No Rush: " +Constants.deliveryDay+", "+ Constants.deliveryDateCheckout.replace("-", "/")+", "+Constants.deliverytime);
                }
            else{
                txt_delivery_type.setText("No Rush");
            }
        }
        else
        {
            /*rel_delivery_type.setClickable(false);
            rel_delivery_type.setEnabled(false);*/

        }
        Double total = AddToCartListAllItemsActivity.total_price + tip;
        txt_total.setText("R" + nf.format(total));

        JSONArray jsonArray=new JSONArray();
        JSONObject jObjImages;
        String ids="";
        ArrayList<String> id=new ArrayList<>();

        for(int i=0; i< AddToCartListAllItemAdapter.listAddToCartList.size(); i++){

                ids=ids+","+AddToCartListAllItemAdapter.listAddToCartList.get(i).get("product_id");

                id.add(AddToCartListAllItemAdapter.listAddToCartList.get(i).get("product_id"));
                //Toast.makeText(getApplicationContext(), "JSONDATA: "+jsonArray.toString(), Toast.LENGTH_LONG).show();

            if(ids.startsWith(",")){
                ids=ids.substring(1, ids.length());
            }
        }
        getDeliveryFee(ids, customer_id);

    }
    private void getDialogBrowse(String strMsg) {
        final Dialog dialog = new Dialog(CheckOutActivity.this);
        Window window = dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_alert);

        TextView txt_header=(TextView)dialog.findViewById(R.id.header);
        TextView txt_msg=(TextView)dialog.findViewById(R.id.msg);
        TextView btn_update_address = (TextView)dialog.findViewById(R.id.btn_sign_in);
        TextView btn_browse = (TextView)dialog.findViewById(R.id.btn_browse);
        btn_update_address.setText("Update Address");
        txt_header.setText("We are rolling out!");
        txt_msg.setText(strMsg);


        btn_browse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();

                shPrefUserSelection = CheckOutActivity.this.getSharedPreferences(Constants.strShPrefUserSelection, Context.MODE_PRIVATE);
                store_id=shPrefUserSelection.getString(Constants.strShUserProductId, "");

                if(store_id.equalsIgnoreCase("")){
                    Intent intent=new Intent(CheckOutActivity.this, PinCodeActivity.class);
                    finish();
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                }else{
                    Intent intent=new Intent(CheckOutActivity.this, SubCategoryActivity.class);
                    finish();
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                }

            }
        });

        btn_update_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                Intent intent = new Intent(CheckOutActivity.this, MapActivity.class);
                intent.putExtra("for", "Checkout");
                finish();
                startActivity(intent);
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
            }
        });

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void getCartTotal(String customer_id) {

        restService.getCartTotal(customer_id, new RestCallback<CartTotalRequest>() {
            @Override
            public void success(CartTotalRequest object) {

                if(object.getStatus()==200&&object.getSuccess()==1){

                    btn_count.setText(object.getData().getTotalQty().toString());
                    //txt_total_price.setText(object.getData().getTotalPrice());
                }

            }

            @Override
            public void invalid() {
                Toast.makeText(CheckOutActivity.this, "Problem while fetching tracking list", Toast.LENGTH_LONG).show();
            }

            @Override
            public void failure() {
                Toast.makeText(CheckOutActivity.this, "Error parsing tracking list", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getPromoDiscountPrice(String coupan, String qoute_id, final String coupon) {
        pDialog=new ProgressDialog(CheckOutActivity.this);
        pDialog.setMessage("Loading...");
        pDialog.setCanceledOnTouchOutside(false);
        pDialog.show();
        restService.ApplyCoupon(coupon, qoute_id, txt_addr.getText().toString(), new RestCallback<ApplyCouponCodeRequest>() {
            @Override
            public void success(ApplyCouponCodeRequest object) {

                Double price=0.0;
                if(object.getStatus()==200&&object.getSuccess()==1){
                    Constants.promo_coupon=coupon;
                    if(object.getData().getTotalDiscount().toString().contains("-")) {

                        Constants.promo_coupon_discount_price = object.getData().getTotalDiscount().toString().substring(1, object.getData().getTotalDiscount().toString().length());
                    }else{
                        Constants.promo_coupon_discount_price=object.getData().getTotalDiscount().toString();
                    }
                    txt_promo_code_discount_price.setText("-R"+nf.format(Double.parseDouble(Constants.promo_coupon_discount_price)));
                    txt_apply_promo.setText("  Remove  ");
                    if(txt_total.getText().toString().contains(",")){
                        price=Double.parseDouble(txt_total.getText().toString().substring(1, txt_total.getText().toString().length()).replace(",",""));
                    }else{
                        price=Double.parseDouble(txt_total.getText().toString().substring(1, txt_total.getText().toString().length()));
                    }
                    price = price - Double.parseDouble(Constants.promo_coupon_discount_price);
                    txt_total.setText("R"+nf.format(price));
                    txt_promo_code_name.setText("Promo Code ("+Constants.promo_coupon+")");
                    utils.displayAlert(object.getData().getSuccessMsg());
                }else{
                    utils.displayAlert(object.getErrorMsg());
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
                Toast.makeText(CheckOutActivity.this, "Problem while fetching tracking list", Toast.LENGTH_LONG).show();

            }

            @Override
            public void failure() {
                if(pDialog!=null){
                    pDialog.dismiss();
                }
                Toast.makeText(CheckOutActivity.this, "Error parsing tracking list", Toast.LENGTH_LONG).show();

            }
        });

    }

    private void removePromoDiscountPrice(String coupan, String qoute_id, final String coupon) {
        pDialog=new ProgressDialog(CheckOutActivity.this);
        pDialog.setMessage("Loading...");
        pDialog.setCanceledOnTouchOutside(false);
        pDialog.show();
        restService.RemoveCoupon(coupon, qoute_id, txt_addr.getText().toString(), "1", new RestCallback<ApplyCouponCodeRequest>() {
            @Override
            public void success(ApplyCouponCodeRequest object) {

                Double price=0.0;
                if(object.getStatus()==200&&object.getSuccess()==1){
                    Constants.promo_coupon=coupon;
                    /*if(object.getData().getTotalDiscount().toString().contains("-")) {

                        Constants.promo_coupon_discount_price = object.getData().getTotalDiscount().toString().substring(1, object.getData().getTotalDiscount().toString().length());
                    }else{
                        Constants.promo_coupon_discount_price=object.getData().getTotalDiscount().toString();
                    }*/
                    txt_promo_code_discount_price.setText("R0.00");

                    txt_apply_promo.setText("  Apply  ");
                    //price = AddToCartListAllItemsActivity.total_price;
                    if(txt_total.getText().toString().contains(",")){
                        price=Double.parseDouble(txt_total.getText().toString().substring(1, txt_total.getText().toString().length()).replace(",",""))+Double.parseDouble(Constants.promo_coupon_discount_price);;
                    }else{
                        price=Double.parseDouble(txt_total.getText().toString().substring(1, txt_total.getText().toString().length()))+Double.parseDouble(Constants.promo_coupon_discount_price);;
                    }
                    ext_promo_code.setText("");
                    txt_promo_code_name.setText("Promo Code");
                    txt_total.setText("R"+nf.format(price));
                    utils.displayAlert(object.getData().getSuccessMsg());
                }else{
                    utils.displayAlert(object.getErrorMsg());
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
                Toast.makeText(CheckOutActivity.this, "Problem while fetching tracking list", Toast.LENGTH_LONG).show();

            }

            @Override
            public void failure() {
                if(pDialog!=null){
                    pDialog.dismiss();
                }
                Toast.makeText(CheckOutActivity.this, "Error parsing tracking list", Toast.LENGTH_LONG).show();

            }
        });

    }
    private void getDeliveryFee(String ids, String customer_id) {

        ids=ids;
        pDialog=new ProgressDialog(CheckOutActivity.this);
        pDialog.setMessage("Loading...");
        pDialog.setCanceledOnTouchOutside(false);
        pDialog.show();
        restService=new RestService(this);
        restService.getDeleveryFee(ids, customer_id, new RestCallback <GetDeliveryFeeRequest> () {
            @Override
            public void success(GetDeliveryFeeRequest obj) {
                Constants.isFirstTimeDelivery=obj.getData().getDeliveryFeeData().get(0).getAmount();
                /*if(Constants.deliverytypeCheckout.toLowerCase().equalsIgnoreCase("asap")){
                    Constants.isFirstTimeDelivery=obj.getData().getDeliveryFeeData().get(0).getAmount();
                    Constants.deliveryfee = Float.parseFloat(obj.getData().getDeliveryFeeData().get(0).getAmount());
                }else {
                    Constants.isFirstTimeDelivery=ob+j.getData().getDeliveryFeeData().get(0).getAmount();
                    Constants.deliveryfee = Float.parseFloat(obj.getData().getDeliveryFeeData().get(0).getAmount());
                }*/
                Constants.isFirstTimeDelivery=obj.getData().getDeliveryFeeData().get(0).getAmount();
                Constants.deliveryfee = Float.parseFloat(obj.getData().getDeliveryFeeData().get(0).getAmount());
                /*if(Constants.isFirstTimeDelivery.equalsIgnoreCase("0")){
                    txt_delivery_free_view.setText("Free 1st 60' Order");
                }*/

                txt_delivery_fee.setText("R"+nf.format(Double.parseDouble(Constants.isFirstTimeDelivery)));
                Double price=0.00;

                if(Constants.strTipPercent!=null&&!Constants.strTipPercent.toString().equalsIgnoreCase("")){

                    if (Constants.strTipPercent.equalsIgnoreCase("0")) {
                        Constants.strTipPercent="0";
                        tip = 0.00;
                        txt_tip_percent.setText("R" + nf.format(tip));
                        Constants.driver_tip = tip;
                        //price = AddToCartListAllItemsActivity.total_price + tip;
                        price = AddToCartListAllItemsActivity.total_price + tip+Constants.deliveryfee;
                        if(!Constants.promo_coupon_discount_price.equalsIgnoreCase("")||!Constants.promo_coupon_discount_price.isEmpty()){
                            price=price-Double.parseDouble(Constants.promo_coupon_discount_price);
                        }

                        txt_total.setText("R" + nf.format(price));
                        txt_tips.setText("R" + nf.format(tip) + " (" + 0 + "%)");
                    } else if (Constants.strTipPercent.equalsIgnoreCase("5")) {
                        Constants.strTipPercent="5";
                        tip = AddToCartListAllItemsActivity.total_price * 5 / 100;
                        txt_tip_percent.setText("R" + nf.format(tip));
                        Constants.driver_tip = tip;
                        price = AddToCartListAllItemsActivity.total_price + tip+Constants.deliveryfee;
                        //price = AddToCartListAllItemsActivity.total_price + tip;
                        if(!Constants.promo_coupon_discount_price.equalsIgnoreCase("")||!Constants.promo_coupon_discount_price.isEmpty()){
                            price=price-Double.parseDouble(Constants.promo_coupon_discount_price);
                        }
                        txt_total.setText("R" + nf.format(price));
                        txt_tips.setText("R" + nf.format(tip) + " (" + 5 + "%)");

                    } else if (Constants.strTipPercent.equalsIgnoreCase("10")) {
                        Constants.strTipPercent="10";
                        tip = AddToCartListAllItemsActivity.total_price * 10 / 100;
                        txt_tip_percent.setText("R" + nf.format(tip));
                        Constants.driver_tip = tip;
                        price = AddToCartListAllItemsActivity.total_price + tip+Constants.deliveryfee;
                        if(!Constants.promo_coupon_discount_price.equalsIgnoreCase("")||!Constants.promo_coupon_discount_price.isEmpty()){
                            price=price-Double.parseDouble(Constants.promo_coupon_discount_price);
                        }
                        txt_total.setText("R" + nf.format(price));
                        txt_tips.setText("R" + nf.format(tip) + " (" + 10 + "%)");

                    } else if (Constants.strTipPercent.equalsIgnoreCase("15")) {
                        Constants.strTipPercent="15";
                        tip = AddToCartListAllItemsActivity.total_price * 15 / 100;
                        txt_tip_percent.setText("R" + nf.format(tip));
                        Constants.driver_tip = tip;
                        //price = AddToCartListAllItemsActivity.total_price + tip;
                        price = AddToCartListAllItemsActivity.total_price + tip+Constants.deliveryfee;
                        if(!Constants.promo_coupon_discount_price.equalsIgnoreCase("")||!Constants.promo_coupon_discount_price.isEmpty()){
                            price=price-Double.parseDouble(Constants.promo_coupon_discount_price);
                        }
                        txt_total.setText("R" + nf.format(price));
                        txt_tips.setText("R" + nf.format(tip) + " (" + 15 + "%)");

                    }
                }else{
                    price = AddToCartListAllItemsActivity.total_price + tip+Constants.deliveryfee;
                    txt_total.setText("R" + nf.format(price));
                }

                //Double total = AddToCartListAllItemsActivity.total_price + Constants.deliveryfee;
                //txt_total.setText("R" + nf.format(total));
                if (pDialog != null) {
                    pDialog.dismiss();
                }
            }

            @Override
            public void invalid() {

                Double price=0.00;
                txt_delivery_fee.setText("R"+nf.format(price));
                Constants.isFirstTimeDelivery="0";

                /*if(Constants.isFirstTimeDelivery.equalsIgnoreCase("0")){
                    txt_delivery_free_view.setText("Free 1st 60' Order");
                }*/
                if(Constants.strTipPercent!=null&&!Constants.strTipPercent.toString().equalsIgnoreCase("")){

                    if (Constants.strTipPercent.equalsIgnoreCase("0")) {
                        Constants.strTipPercent="0";
                        tip = 0.00;
                        txt_tip_percent.setText("R" + nf.format(tip));
                        Constants.driver_tip = tip;
                        price = AddToCartListAllItemsActivity.total_price + tip+Constants.deliveryfee;
                        //price = AddToCartListAllItemsActivity.total_price + tip;
                        if(!Constants.promo_coupon_discount_price.equalsIgnoreCase("")||!Constants.promo_coupon_discount_price.isEmpty()){
                            price=price-Double.parseDouble(Constants.promo_coupon_discount_price);
                        }
                        txt_total.setText("R" + nf.format(price));
                        txt_tips.setText("R" + nf.format(tip) + " (" + 0 + "%)");
                    } else if (Constants.strTipPercent.equalsIgnoreCase("5")) {
                        Constants.strTipPercent="5";
                        tip = AddToCartListAllItemsActivity.total_price * 5 / 100;
                        txt_tip_percent.setText("R" + nf.format(tip));
                        Constants.driver_tip = tip;
                        price = AddToCartListAllItemsActivity.total_price + tip+Constants.deliveryfee;
                       // price = AddToCartListAllItemsActivity.total_price + tip;
                        if(!Constants.promo_coupon_discount_price.equalsIgnoreCase("")||!Constants.promo_coupon_discount_price.isEmpty()){
                            price=price-Double.parseDouble(Constants.promo_coupon_discount_price);
                        }
                        txt_total.setText("R" + nf.format(price));
                        txt_tips.setText("R" + nf.format(tip) + " (" + 5 + "%)");

                    } else if (Constants.strTipPercent.equalsIgnoreCase("10")) {
                        Constants.strTipPercent="10";
                        tip = AddToCartListAllItemsActivity.total_price * 10 / 100;
                        txt_tip_percent.setText("R" + nf.format(tip));
                        Constants.driver_tip = tip;
                        //price = AddToCartListAllItemsActivity.total_price + tip;
                        price = AddToCartListAllItemsActivity.total_price + tip+Constants.deliveryfee;
                        if(!Constants.promo_coupon_discount_price.equalsIgnoreCase("")||!Constants.promo_coupon_discount_price.isEmpty()){
                            price=price-Double.parseDouble(Constants.promo_coupon_discount_price);
                        }
                        txt_total.setText("R" + nf.format(price));
                        txt_tips.setText("R" + nf.format(tip) + " (" + 10 + "%)");

                    } else if (Constants.strTipPercent.equalsIgnoreCase("15")) {
                        Constants.strTipPercent="15";
                        tip = AddToCartListAllItemsActivity.total_price * 15 / 100;
                        txt_tip_percent.setText("R" + nf.format(tip));
                        Constants.driver_tip = tip;
                        price = AddToCartListAllItemsActivity.total_price + tip+Constants.deliveryfee;
                        //price = AddToCartListAllItemsActivity.total_price + tip;
                        if(!Constants.promo_coupon_discount_price.equalsIgnoreCase("")||!Constants.promo_coupon_discount_price.isEmpty()){
                            price=price-Double.parseDouble(Constants.promo_coupon_discount_price);
                        }
                        txt_total.setText("R" + nf.format(price));
                        txt_tips.setText("R" + nf.format(tip) + " (" + 15 + "%)");

                    }
                }else{
                    price = AddToCartListAllItemsActivity.total_price + tip+Constants.deliveryfee;
                    txt_total.setText("R" + nf.format(price));
                }

                //Double total = AddToCartListAllItemsActivity.total_price + Constants.deliveryfee;
                //txt_total.setText("R" + nf.format(total));
                if (pDialog != null) {
                    pDialog.dismiss();
                }
            }

            @Override
            public void failure() {

                Double price=0.00;
                txt_delivery_fee.setText("R"+nf.format(price));
                Constants.isFirstTimeDelivery="0";

                /*if(Constants.isFirstTimeDelivery.equalsIgnoreCase("0")){
                    txt_delivery_free_view.setText("Free 1st 60' Order");
                }*/
                if(Constants.strTipPercent!=null&&!Constants.strTipPercent.toString().equalsIgnoreCase("")){

                    if (Constants.strTipPercent.equalsIgnoreCase("0")) {
                        Constants.strTipPercent="0";
                        tip = 0.00;
                        txt_tip_percent.setText("R" + nf.format(tip));
                        Constants.driver_tip = tip;
                        price = AddToCartListAllItemsActivity.total_price + tip+Constants.deliveryfee;
                        //price = AddToCartListAllItemsActivity.total_price + tip;
                        if(!Constants.promo_coupon_discount_price.equalsIgnoreCase("")||!Constants.promo_coupon_discount_price.isEmpty()){
                            price=price-Double.parseDouble(Constants.promo_coupon_discount_price);
                        }
                        txt_total.setText("R" + nf.format(price));
                        txt_tips.setText("R" + nf.format(tip) + " (" + 0 + "%)");
                    } else if (Constants.strTipPercent.equalsIgnoreCase("5")) {
                        Constants.strTipPercent="5";
                        tip = AddToCartListAllItemsActivity.total_price * 5 / 100;
                        txt_tip_percent.setText("R" + nf.format(tip));
                        Constants.driver_tip = tip;
                        price = AddToCartListAllItemsActivity.total_price + tip +Constants.deliveryfee;
                        //price = AddToCartListAllItemsActivity.total_price + tip;
                        if(!Constants.promo_coupon_discount_price.equalsIgnoreCase("")||!Constants.promo_coupon_discount_price.isEmpty()){
                            price=price-Double.parseDouble(Constants.promo_coupon_discount_price);
                        }
                        txt_total.setText("R" + nf.format(price));
                        txt_tips.setText("R" + nf.format(tip) + " (" + 5 + "%)");

                    } else if (Constants.strTipPercent.equalsIgnoreCase("10")) {
                        Constants.strTipPercent="10";
                        tip = AddToCartListAllItemsActivity.total_price * 10 / 100;
                        txt_tip_percent.setText("R" + nf.format(tip));
                        Constants.driver_tip = tip;
                        price = AddToCartListAllItemsActivity.total_price + tip+Constants.deliveryfee;
                        //price = AddToCartListAllItemsActivity.total_price + tip;
                        if(!Constants.promo_coupon_discount_price.equalsIgnoreCase("")||!Constants.promo_coupon_discount_price.isEmpty()){
                            price=price-Double.parseDouble(Constants.promo_coupon_discount_price);
                        }
                        txt_total.setText("R" + nf.format(price));
                        txt_tips.setText("R" + nf.format(tip) + " (" + 10 + "%)");

                    } else if (Constants.strTipPercent.equalsIgnoreCase("15")) {
                        Constants.strTipPercent="15";
                        tip = AddToCartListAllItemsActivity.total_price * 15 / 100;
                        txt_tip_percent.setText("R" + nf.format(tip));
                        Constants.driver_tip = tip;
                        price = AddToCartListAllItemsActivity.total_price + tip+Constants.deliveryfee;
                        //price = AddToCartListAllItemsActivity.total_price + tip;
                        if(!Constants.promo_coupon_discount_price.equalsIgnoreCase("")||!Constants.promo_coupon_discount_price.isEmpty()){
                            price=price-Double.parseDouble(Constants.promo_coupon_discount_price);
                        }
                        txt_total.setText("R" + nf.format(price));
                        txt_tips.setText("R" + nf.format(tip) + " (" + 15 + "%)");

                    }
                }else{
                    price = AddToCartListAllItemsActivity.total_price + tip+Constants.deliveryfee;
                    txt_total.setText("R" + nf.format(price));
                }

                if (pDialog != null) {
                    pDialog.dismiss();
                }
            }
        });
    }

    private void callPopUpSpecialDiscount(View anchorView) {
        pw = new PopupWindow(dropDownMenuSpecialDiscount(R.layout.pop_up_menu, new Vector()),anchorView.getWidth(),height/3, true);
        pw.setBackgroundDrawable(new BitmapDrawable());
        pw.setOutsideTouchable(true);
        pw.showAsDropDown(anchorView);
        pw.update();
    }
    private View dropDownMenuSpecialDiscount(int layout, Vector menuItem)
    {
        View view = null;
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(layout, null, false);
        final ArrayList<String> arrItem=new ArrayList<>();
        arrItem.add("0%");
        arrItem.add("5%");
        arrItem.add("10%");
        arrItem.add("15%");

        TipCatSpinnerAdapter searchLangAdapter = new TipCatSpinnerAdapter(CheckOutActivity.this, arrItem);
        ListView listView = (ListView)view.findViewById(R.id.pop_up_menu_list);
        listView.setAdapter(searchLangAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                double tip = 0;
                if (position == 0) {
                    Constants.strTipPercent="0";
                    tip = 0.00;
                    txt_tip_percent.setText("R" + nf.format(tip));
                    Constants.driver_tip = tip;
                    Double price = AddToCartListAllItemsActivity.total_price + tip;

                    if(!Constants.promo_coupon_discount_price.equalsIgnoreCase("")||!Constants.promo_coupon_discount_price.isEmpty()){
                        price=price-Double.parseDouble(Constants.promo_coupon_discount_price);
                    }
                    txt_total.setText("R" + nf.format(price+Constants.deliveryfee));
                    txt_tips.setText("R" + nf.format(tip) + " (" + arrItem.get(position) + ")");
                } else if (position == 1) {
                    Constants.strTipPercent="5";
                    tip = AddToCartListAllItemsActivity.total_price * 5 / 100;
                    txt_tip_percent.setText("R" + nf.format(tip));
                    Constants.driver_tip = tip;
                    Double price = AddToCartListAllItemsActivity.total_price + tip;
                    if(!Constants.promo_coupon_discount_price.equalsIgnoreCase("")||!Constants.promo_coupon_discount_price.isEmpty()){
                        price=price-Double.parseDouble(Constants.promo_coupon_discount_price);
                    }
                    txt_total.setText("R" + nf.format(price+Constants.deliveryfee));
                    txt_tips.setText("R" + nf.format(tip) + " (" + arrItem.get(position) + ")");

                } else if (position == 2) {
                    Constants.strTipPercent="10";
                    tip = AddToCartListAllItemsActivity.total_price * 10 / 100;
                    txt_tip_percent.setText("R" + nf.format(tip));
                    Constants.driver_tip = tip;
                    Double price = AddToCartListAllItemsActivity.total_price + tip;
                    if(!Constants.promo_coupon_discount_price.equalsIgnoreCase("")||!Constants.promo_coupon_discount_price.isEmpty()){
                        price=price-Double.parseDouble(Constants.promo_coupon_discount_price);
                    }
                    txt_total.setText("R" + nf.format(price+Constants.deliveryfee));
                    txt_tips.setText("R" + nf.format(tip) + " (" + arrItem.get(position) + ")");

                } else if (position == 3) {
                    Constants.strTipPercent="15";
                    tip = AddToCartListAllItemsActivity.total_price * 15 / 100;
                    txt_tip_percent.setText("R" + nf.format(tip));
                    Constants.driver_tip = tip;
                    Double price = AddToCartListAllItemsActivity.total_price + tip;
                    if(!Constants.promo_coupon_discount_price.equalsIgnoreCase("")||!Constants.promo_coupon_discount_price.isEmpty()){
                        price=price-Double.parseDouble(Constants.promo_coupon_discount_price);
                    }
                    txt_total.setText("R" + nf.format(price+Constants.deliveryfee));
                    txt_tips.setText("R" + nf.format(tip) + " (" + arrItem.get(position) + ")");

                }
                pw.dismiss();

            }
        });

        return view;
    }

    private void getCustomPercent() {
        final Dialog dialog = new Dialog(CheckOutActivity.this);
        Window window = dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_forget_pass);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        TextView txt_header=(TextView)dialog.findViewById(R.id.header);
        txt_header.setText("Stockup");
        TextView txt_header1=(TextView)dialog.findViewById(R.id.header1);
        txt_header1.setVisibility(View.INVISIBLE);
        final EditText etEmailId=(EditText)dialog.findViewById(R.id.msg);
        etEmailId.setHint("Provide tip percent.");
        etEmailId.setTypeface(typeFaceSegoeuiReg);
        etEmailId.setMaxEms(2);
        etEmailId.setInputType(InputType.TYPE_CLASS_NUMBER);
        TextView btn_no=(TextView)dialog.findViewById(R.id.btn_cancel);
        TextView btn_yes=(TextView)dialog.findViewById(R.id.btn_ok);
        btn_yes.setText("OK");
        btn_no.setText("Cancel");
        btn_no.setAllCaps(false);
        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etEmailId.getText().toString().equalsIgnoreCase("0")){
                    txt_tips.setText("R 0.00" +" (" + etEmailId.getText().toString() + ")");
                    dialog.dismiss();
                }
                else if(etEmailId.getText().toString().length()>0){
                    int i=Integer.parseInt(etEmailId.getText().toString());
                    tip = AddToCartListAllItemsActivity.total_price*i / 100;
                    tip = Double.parseDouble(new DecimalFormat("##.####").format(tip));

                    txt_tip_percent.setText("R" + nf.format(tip));
                    Double price=AddToCartListAllItemsActivity.total_price + tip;
                    if(Constants.deliverytypeCheckout.equalsIgnoreCase("asap")){
                        price=price+10.00;
                        Constants.deliveryfee=10.00;
                    }else{
                        price=price+5.00;
                        Constants.deliveryfee=5.00;
                    }
                    txt_total.setText("R" + nf.format(price));
                    txt_tips.setText("R" + nf.format(tip) + " (" + etEmailId.getText().toString() + ")");
                    Constants.driver_tip=tip;
                    dialog.dismiss();
                }


            }
        });
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
    /////////////////////Hiding soft keyboard/////////////
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }
    private void getDialogCoverage() {
        int width=0, height=0;
        final Dialog dialog = new Dialog(CheckOutActivity.this);
        Window window = dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_delivery_schedule);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;

        width = (int) (width * 0.75);
        height = (int) (height * 0.45);
        dialog.getWindow().setLayout(width, height);

        LinearLayout lin_asap=(LinearLayout)dialog.findViewById(R.id.lin_asap);
        LinearLayout lin_no_rush=(LinearLayout)dialog.findViewById(R.id.lin_no_rush);

        final ImageView img_asap=(ImageView)dialog.findViewById(R.id.img_asap);
        final ImageView img_no_rush=(ImageView)dialog.findViewById(R.id.img_no_rush);

        TextView btn_no=(TextView)dialog.findViewById(R.id.txt_cancel);
        TextView btn_yes=(TextView)dialog.findViewById(R.id.txt_save);
        //isAsap="Yes";
        if(Constants.deliverytypeCheckout.equalsIgnoreCase("No Rush")){
            isAsap="No";
            img_asap.setImageResource(R.drawable.checkout_radio_blue_normal);
            img_no_rush.setImageResource(R.drawable.checkout_radio_active_blue);

        }else if(Constants.deliverytypeCheckout.equalsIgnoreCase("ASAP")){
            isAsap="Yes";
            img_asap.setImageResource(R.drawable.checkout_radio_active_blue);
            img_no_rush.setImageResource(R.drawable.checkout_radio_blue_normal);
        }
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
                if(isAsap.equalsIgnoreCase("Yes")){
                    txt_delivery_type.setText("ASAP");
                    Constants.deliverytypeCheckout="asap";
                    Constants.deliveryDate="Needed ASAP";
                    Constants.deliveryDateCheckout="ASAP";

                }else{
                    Constants.deliverytypeCheckout="No Rush";
                    txt_delivery_type.setText("No Rush");
                    Constants.deliveryDate="";
                    Constants.deliveryDateCheckout="No Rush";
                    Intent intent=new Intent(CheckOutActivity.this, DeliveryChargeActivity.class);
                    intent.putExtra("isasap", isAsap);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                }

            }
        });
        lin_asap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAsap="Yes";
                Constants.deliverytypeCheckout="asap";
                img_asap.setImageResource(R.drawable.checkout_radio_active_blue);
                img_no_rush.setImageResource(R.drawable.checkout_radio_blue_normal);
                //Constants.deliveryDate="Needed ASAP";
            }
        });
        lin_no_rush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialogComingSoon();
                /*isAsap="No";
                Constants.deliverytypeCheckout="No Rush";
                img_asap.setImageResource(R.drawable.checkout_radio_blue_normal);
                img_no_rush.setImageResource(R.drawable.checkout_radio_active_blue);*/

            }
        });
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void getDeliverytype() {
        final Dialog dialog = new Dialog(CheckOutActivity.this);
        Window window = dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_delivery_type);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        TextView header=(TextView)dialog.findViewById(R.id.header);
        header.setTypeface(typeFaceSegoeuiBold);
        TextView msg=(TextView)dialog.findViewById(R.id.msg);
        msg.setTypeface(typeFaceSegoeuiReg);
        TextView btn_yes=(TextView)dialog.findViewById(R.id.btn_ok);
        TextView btn_no=(TextView)dialog.findViewById(R.id.btn_cancel);
        //header.setText("Error");
        msg.setText("At the moment, we are unable to process both ASAP and scheduled deliveries in one cart. To proceed with either type of delivery, ensure your cart contains only items from same delivery category.");
        btn_yes.setText("Schedule");
        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Put Grocery selection for landing page
                dialog.dismiss();
                Intent intent = new Intent(CheckOutActivity.this, DeliveryChargeActivity.class);
                intent.putExtra("isasap", "No");
                startActivity(intent);
                finish();
                Constants.deliverytypeCheckout = "No Rush";
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);

            }
        });
        btn_no.setText("Update Cart");
        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Put Grocery selection for landing page
                dialog.dismiss();
                Intent intent=new Intent(CheckOutActivity.this, AddToCartListAllItemsActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
            }
        });
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void getDialogComingSoon() {
        final Dialog dialog = new Dialog(CheckOutActivity.this);
        Window window = dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_coming_soon);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        Button btn_yes=(Button)dialog.findViewById(R.id.btn_yes);
        btn_yes.setText("Ok");
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


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent=new Intent(CheckOutActivity.this, AddToCartListAllItemsActivity.class);
        startActivity(intent);
        finish();
        Constants.deliverytypeCart="";
        Constants.deliverytypeCheckout="";
        Constants.deliveryDate="";
        Constants.deliveryDateCheckout="";
        AddToCartListAllItemsActivity.size=0;
        Constants.promo_coupon_discount_price="";
        Constants.promo_coupon="";
        CheckOutActivity.this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private void customCheckBoxTextView(TextView view) {
        String init = "By continuing to use Stockup you agree to the most recent ";
        String terms = "Terms and Conditions";
        String and = " & ";
        String privacy = "Privacy Policy";
        String dot=".";
        //String last = "have been updated. By continuing to use Stockup you agree to the most recent Terms of Service and Privacy Policy. ";

        SpannableStringBuilder spanTxt = new SpannableStringBuilder(
                init);
        spanTxt.append(terms);

        spanTxt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                //callTermsCondition("file:///android_asset/Terms_and_Conditions.html", "Terms & Conditions");
                Intent urlPP = new Intent(CheckOutActivity.this, StockupWebViewActivity.class);
                urlPP.putExtra("header", "Terms and Conditions");
                urlPP.putExtra("url", Constants.urlTermsCond);
                startActivity(urlPP);
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
            }
        }, init.length(), init.length() + terms.length(), 0);
        spanTxt.setSpan(new ForegroundColorSpan(Color.parseColor("#FBAE3C")), init.length(), init.length() + terms.length(), 0);
        spanTxt.append(and);
        spanTxt.setSpan(new ForegroundColorSpan(Color.parseColor("#ffffff")), init.length() + terms.length(), init.length() + terms.length() + and.length(), 0);
        spanTxt.append(privacy);
        spanTxt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                //callTermsCondition("file:///android_asset/privacy policy.html", "Privacy Policy");
                Intent urlPP = new Intent(CheckOutActivity.this, StockupWebViewActivity.class);
                urlPP.putExtra("header", "Privacy Policy");
                urlPP.putExtra("url", Constants.urlPrivacyPolicy);
                startActivity(urlPP);
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
            }
        }, init.length() + terms.length() + and.length(), init.length() + terms.length() + and.length() + privacy.length(), 0);
        spanTxt.setSpan(new ForegroundColorSpan(Color.parseColor("#FBAE3C")), init.length() + terms.length() + and.length(), init.length() + terms.length() + and.length() + privacy.length(), 0);

        spanTxt.append(dot);
        spanTxt.setSpan(new ForegroundColorSpan(Color.parseColor("#ffffff")), init.length() + terms.length() + and.length() + privacy.length(), init.length() + terms.length() + and.length() + privacy.length()+ dot.length(), 0);

        view.setMovementMethod(LinkMovementMethod.getInstance());
        view.setText(spanTxt, TextView.BufferType.SPANNABLE);
    }
}
