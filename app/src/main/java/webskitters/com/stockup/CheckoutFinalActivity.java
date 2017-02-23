package webskitters.com.stockup;

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
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

//import com.appsflyer.AppsFlyerLib;

import com.appsflyer.AppsFlyerLib;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.RoundingMode;
import java.text.Format;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import webskitters.com.stockup.Utils.Constants;
import webskitters.com.stockup.Utils.Utils;
import webskitters.com.stockup.adapter.AddToCartListAllItemAdapter;
import webskitters.com.stockup.adapter.ViewAllCheckoutItemListAdapter;
import webskitters.com.stockup.adapter.ViewAllRatesReviewListAdapter;
import webskitters.com.stockup.model.CartTotalRequest;
import webskitters.com.stockup.model.PaymentRequest;
import webskitters.com.stockup.model.ServiceTaxRequest;
import webskitters.com.stockup.retrofit_call.RestCallback;
import webskitters.com.stockup.retrofit_call.RestService;

/**
 * Created by android on 9/21/2016.
 */
public class CheckoutFinalActivity extends AppCompatActivity {

    RelativeLayout rel_add_payment;
    private Typeface typeFaceSegoeuiReg;
    private Typeface typeFaceSegoeuiBold;
    SharedPreferences.Editor toEdit;
    Utils utils;
    SharedPreferences shPrefDeliverAddr;
    SharedPreferences shP;
    SharedPreferences.Editor toEditAddr;
    String fname="", lName="", shipingAdd="", bilingAdd="", forshipingbiling="";
    Bundle extras;
    ImageView imgBack;

    RelativeLayout rel_add, rel_pay_with;
    private ProgressDialog pDialog;
    RestService restService;
    TextView txt_place_order;
    private TextView txt_addr;

    int width=0, height=0;
    TextView txt_card, txt_date;
    private String strDate="";
    EditText ext_comment;
    TextView txt_sub_total_price, txt_delivery_fee, txt_driver_tip, txt_total;
    private NumberFormat nf;
    ExpandableHeightListView lv_item_list;

    LinearLayout lin_cart;
    ImageView imgCart;
    Button btn_count;

    private SharedPreferences sharedPreferenceUser;
    String customer_id="";
    LinearLayout lin_place_order;
    TextView txt_header;
    TextView txt_delivery_fee_view;
    Format format, format_date;
    TextView txt_promo_code_discount_price, txt_promo_code;
    CheckBox chk_deliver_it_to_me;
    LinearLayout lin_name, lin_contact_num;
    EditText ext_name, ext_surname, ext_number, ext_counrty_code;
    View vw_name, vw_number;
    ScrollView scrollview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_check_out_complete_order);
        typeFaceSegoeuiReg = Typeface.createFromAsset(getAssets(),
                "Roboto-Regular.ttf");
        typeFaceSegoeuiBold = Typeface.createFromAsset(getAssets(),
                "ROBOTO-BOLD_0.TTF");
        utils=new Utils(CheckoutFinalActivity.this);
        restService=new RestService(this);
        sharedPreferenceUser=this.getSharedPreferences(Constants.strShPrefUserPrefName, Context.MODE_PRIVATE);
        customer_id=sharedPreferenceUser.getString(Constants.strShPrefUserId, "");
        extras=getIntent().getExtras();
        if(extras!=null){
            strDate=extras.getString("date","");
        }

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;

        nf= NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);
        nf.setRoundingMode(RoundingMode.HALF_UP);

        // Track Data : Add to activities where tracking needed
        AppsFlyerLib.getInstance().sendDeepLinkData(this);

        initFields();

        getTax();
        if(!customer_id.equalsIgnoreCase("")){
            getCartTotal(customer_id);
        }

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

                Toast.makeText(CheckoutFinalActivity.this, "Problem while fetching tracking list", Toast.LENGTH_LONG).show();

            }

            @Override
            public void failure() {
                Toast.makeText(CheckoutFinalActivity.this, "Error parsing tracking list", Toast.LENGTH_LONG).show();
            }
        });

    }
    private void getTax(){
        pDialog=new ProgressDialog(CheckoutFinalActivity.this);
        pDialog.setMessage("Loading...");
        pDialog.setCanceledOnTouchOutside(false);
        pDialog.show();
        restService=new RestService(this);
        restService.getServiceTax(new RestCallback<ServiceTaxRequest>() {
            @Override
            public void success(ServiceTaxRequest obj) {
                //if (obj.getSuccess() == 200 && obj.getStatus() == 1) {
                    Constants.serviceTax = Float.parseFloat(obj.getData().getTaxData().get(0).getTax());

                    //AddToCartListAllItemsActivity.total_price=AddToCartListAllItemsActivity.total_price/Constants.serviceTax;
                    //txt_vat.setText("R"+nf.format(AddToCartListAllItemsActivity.total_price*14/100));
                    //txt_service_tax.setText("R" + nf.format(AddToCartListAllItemsActivity.total_price*Constants.serviceTax/100));
                    //double service_fee=((AddToCartListAllItemsActivity.total_price*Constants.serviceTax)/100);
                    //double vat=((AddToCartListAllItemsActivity.total_price*14)/100);
                    /*double driver_tip=Double.parseDouble(txt_driver_tip.getText().toString());
                    double delivery_fee=Double.parseDouble(txt_delivery_fee.getText().toString());*/
                    double total=AddToCartListAllItemsActivity.total_price+Constants.driver_tip+Constants.deliveryfee;
                    //AddToCartListAllItemsActivity.total_price=total;
                if(!Constants.promo_coupon_discount_price.equalsIgnoreCase("")||!Constants.promo_coupon_discount_price.isEmpty()){
                    total=total-Double.parseDouble(Constants.promo_coupon_discount_price);
                }
                    txt_total.setText("R"+nf.format(total));

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
    private void initFields() {

            shPrefDeliverAddr = this.getSharedPreferences(Constants.strShPrefDelAddrName, Context.MODE_PRIVATE);
            sharedPreferenceUser = this.getSharedPreferences(Constants.strShPrefUserPrefName, Context.MODE_PRIVATE);

            scrollview=(ScrollView)findViewById(R.id.scrollview);

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


            ext_comment=(EditText) findViewById(R.id.ext_comment);
            if(Constants.deliveryInstruction!=null){
                ext_comment.setText(Constants.deliveryInstruction);
                ext_comment.setSelection(Constants.deliveryInstruction.length());
            }
            imgBack = (ImageView) findViewById(R.id.img_back);
            imgBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Constants.deliveryInstruction=ext_comment.getText().toString();
                    if(Constants.deliverItToMe.equalsIgnoreCase("No")){
                        lin_name.setVisibility(View.VISIBLE);
                        vw_name.setVisibility(View.VISIBLE);
                        lin_contact_num.setVisibility(View.VISIBLE);
                        vw_number.setVisibility(View.VISIBLE);
                        Constants.deliverItToMe="No";
                        Constants.deliverPersonName=ext_name.getText().toString();
                        Constants.deliverPersonSurName=ext_surname.getText().toString();
                        Constants.deliverPersonCountryCode=ext_counrty_code.getText().toString();
                        Constants.deliverPersonNumner=ext_number.getText().toString();
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
                    Intent intent=new Intent(CheckoutFinalActivity.this, CheckOutActivity.class);
                    startActivity(intent);
                    finish();
                    CheckoutFinalActivity.this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                }
            });
            txt_header=(TextView)findViewById(R.id.txt_header);

            lv_item_list=(ExpandableHeightListView)findViewById(R.id.lv_item_list);
            lv_item_list.setAdapter(new ViewAllCheckoutItemListAdapter(CheckoutFinalActivity.this, AddToCartListAllItemAdapter.listAddToCartList));
            lv_item_list.setExpanded(true);
            lv_item_list.setFocusable(false);
            txt_promo_code_discount_price=(TextView)findViewById(R.id.txt_promo_code_price);
            txt_promo_code=(TextView)findViewById(R.id.txt_promo_code);
            if(!Constants.promo_coupon_discount_price.equalsIgnoreCase("")||!Constants.promo_coupon_discount_price.isEmpty()){
                txt_promo_code_discount_price.setText("-R"+nf.format(Double.parseDouble(Constants.promo_coupon_discount_price)));
                txt_promo_code.setText("Promo Code ("+Constants.promo_coupon+")");
            }else{
                txt_promo_code_discount_price.setText("R0.00");
                txt_promo_code.setText("Promo Code");
            }

            txt_sub_total_price=(TextView)findViewById(R.id.txt_sub_total_price);
            txt_sub_total_price.setText("R"+nf.format(AddToCartListAllItemsActivity.total_price));
            txt_delivery_fee=(TextView)findViewById(R.id.txt_delivery_fee);
            txt_delivery_fee.setText("R"+nf.format(Constants.deliveryfee));
            txt_delivery_fee_view=(TextView)findViewById(R.id.txt_delivery_fee_view);

            txt_driver_tip=(TextView)findViewById(R.id.txt_driver_tip);
            txt_driver_tip.setText("R"+nf.format(Constants.driver_tip));
            /*if(Constants.isFirstTimeDelivery.equalsIgnoreCase("0")){
                txt_delivery_fee_view.setText("Free 1st 60' Order");
            }*/
            txt_total=(TextView)findViewById(R.id.txt_total);
            rel_add_payment = (RelativeLayout) findViewById(R.id.rel_add_payment);
            /*rel_add_payment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(CheckoutFinalActivity.this, AddPaymentActivity.class);
                    intent.putExtra("for", "CheckoutFinal");
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                }
            });*/

            rel_add = (RelativeLayout) findViewById(R.id.rel_addr);
            rel_add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*Intent intent = new Intent(CheckoutFinalActivity.this, MapActivity.class);
                    intent.putExtra("for", "CheckoutFinal");
                    finish();
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);*/
                }
            });

            rel_pay_with = (RelativeLayout) findViewById(R.id.rel_pay_with);
            rel_pay_with.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(CheckoutFinalActivity.this, AddPaymentActivity.class);
                    intent.putExtra("for", "CheckoutFinal");
                    finish();
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                }
            });

            txt_date = (TextView) findViewById(R.id.txt_date);
            if (Constants.deliveryDateCheckout != null && !Constants.deliveryDateCheckout.equalsIgnoreCase("")) {
                if(Constants.deliveryDate.contains("No Rush")) {
                    if(Constants.deliverytime.equalsIgnoreCase("")||Constants.deliverytime.isEmpty()){
                        txt_date.setText(Constants.deliveryDay+", "+ Constants.deliveryDateCheckout.replace("-", "/"));
                    }else{
                        txt_date.setText(Constants.deliveryDay+", "+ Constants.deliveryDateCheckout.replace("-", "/")+", "+Constants.deliverytime);
                    }

                }
                else{
                    txt_date.setText("ASAP");
                }
            }
            lin_place_order=(LinearLayout)findViewById(R.id.lin_place_order);
            txt_place_order = (TextView) findViewById(R.id.txt_place_order);
            txt_place_order.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(Constants.fromOrderStatus.equalsIgnoreCase("Yes")){
                        Intent intent=new Intent(CheckoutFinalActivity.this, OrderStatusActivity.class);
                        intent.putExtra("comingfrom", "CheckoutFinal");
                        startActivity(intent);
                        finish();
                        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                    }else {
                        getPayment();
                    }
                }
            });
            txt_addr = (TextView) findViewById(R.id.txt_addr);
            String callFromMap = shPrefDeliverAddr.getString(Constants.strShPrefDelCallFrom, "No");
            if (callFromMap.equalsIgnoreCase("Yes")/*&&forshipingbiling.equalsIgnoreCase("shiping")*/) {
                String strLat = shPrefDeliverAddr.getString(Constants.strShPrefDelLat, "");
                String strLong = shPrefDeliverAddr.getString(Constants.strShPrefDelLong, "");
                String strAddr = shPrefDeliverAddr.getString(Constants.strShPrefDelAddr, "");
                txt_addr.setText(strAddr);
            }
            txt_card = (TextView) findViewById(R.id.txt_card);
            /*if(Constants.paymentType.equalsIgnoreCase("Peach Payment")){
                txt_card.setText("Pay with Peach Payment");
            }else if(Constants.paymentType.equalsIgnoreCase("Payfast")){
                txt_card.setText("Pay with Payfast");
            }*/
            //txt_card.setText(shPrefDeliverAddr.getString("nickname", ""));

            imgCart=(ImageView)findViewById(R.id.img_cart_icon);
            lin_cart=(LinearLayout)findViewById(R.id.lin_cart);
            btn_count=(Button)findViewById(R.id.btn_count);
            lin_cart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(CheckoutFinalActivity.this, AddToCartListAllItemsActivity.class);
                    intent.putExtra("context_act1", "webskitters.com.stockup.CheckoutFinalActivity");
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                }
            });

            if(Constants.fromOrderStatus.equalsIgnoreCase("Yes")){
                txt_place_order.setText("ORDER STATUS");
                imgBack.setVisibility(View.GONE);
                txt_header.setText("Order Details");
                rel_add.setClickable(false);
                rel_pay_with.setClickable(false);
                lin_cart.setVisibility(View.INVISIBLE);

                chk_deliver_it_to_me.setEnabled(false);
                chk_deliver_it_to_me.setClickable(false);

                ext_name.setEnabled(false);
                ext_name.setClickable(false);

                ext_surname.setEnabled(false);
                ext_surname.setClickable(false);

                ext_counrty_code.setEnabled(false);
                ext_counrty_code.setClickable(false);

                ext_number.setEnabled(false);
                ext_number.setClickable(false);

                ext_comment.setEnabled(false);
                ext_comment.setClickable(false);


            }

    }

    @Override
    public void onBackPressed() {

        if(Constants.fromOrderStatus.equalsIgnoreCase("Yes")){

        }else {
            super.onBackPressed();
            Constants.deliveryInstruction=ext_comment.getText().toString();
            if(Constants.deliverItToMe.equalsIgnoreCase("No")){
                lin_name.setVisibility(View.VISIBLE);
                vw_name.setVisibility(View.VISIBLE);
                lin_contact_num.setVisibility(View.VISIBLE);
                vw_number.setVisibility(View.VISIBLE);
                Constants.deliverItToMe="No";
                Constants.deliverPersonName=ext_name.getText().toString();
                Constants.deliverPersonSurName=ext_surname.getText().toString();
                Constants.deliverPersonCountryCode=ext_counrty_code.getText().toString();
                Constants.deliverPersonNumner=ext_number.getText().toString();
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
            Intent intent=new Intent(CheckoutFinalActivity.this, CheckOutActivity.class);
            startActivity(intent);
            finish();
            CheckoutFinalActivity.this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
    }

    private void getPayment() {

        if (Constants.deliverItToMe.equalsIgnoreCase("no") && (ext_name.getText().toString().equalsIgnoreCase("") || ext_name.getText().toString().isEmpty())) {
            utils.displayAlert("Please provide all required recipient details.");
        } else if (Constants.deliverItToMe.equalsIgnoreCase("no") && (ext_surname.getText().toString().equalsIgnoreCase("") || ext_surname.getText().toString().isEmpty())) {
            utils.displayAlert("Please provide all required recipient details.");
        } else if (Constants.deliverItToMe.equalsIgnoreCase("no") && (ext_counrty_code.getText().toString().equalsIgnoreCase("") || ext_counrty_code.getText().toString().isEmpty())) {
            utils.displayAlert("Please provide all required recipient details.");
            ext_counrty_code.setFocusable(true);
        } else if (Constants.deliverItToMe.equalsIgnoreCase("no") && (ext_number.getText().toString().equalsIgnoreCase("") || ext_number.getText().toString().isEmpty())) {
            utils.displayAlert("Please provide all required recipient details.");
        } else if (Constants.deliverItToMe.equalsIgnoreCase("no") && ext_counrty_code.getText().toString().length() < 1) {
            utils.displayAlert("Please provide all required recipient details.");
            ext_counrty_code.setFocusable(true);
        } else if (Constants.deliverItToMe.equalsIgnoreCase("no") && ext_number.getText().toString().length() < 1) {
            utils.displayAlert("There seems to be an error in the format of the telephone number provided.");
        } else {
            Constants.deliverPersonName=ext_name.getText().toString();
            Constants.deliverPersonSurName=ext_surname.getText().toString();
            Constants.deliverPersonCountryCode=ext_counrty_code.getText().toString();
            Constants.deliverPersonNumner=ext_number.getText().toString();
            String same_as_ship_addr = "";
            if (Constants.deliverItToMe.equalsIgnoreCase("No")) {
                Constants.fName = Constants.deliverPersonName;
                Constants.lName = Constants.deliverPersonSurName;
                //Constants.lName=sharedPreferenceUser.getString(Constants.strShPrefUserLname,"");;
                Constants.mobilenum = "+" + Constants.deliverPersonCountryCode + Constants.deliverPersonNumner;
                same_as_ship_addr = "1";
            } else {
                Constants.lName = sharedPreferenceUser.getString(Constants.strShPrefUserLname, "");
                Constants.mobilenum = sharedPreferenceUser.getString(Constants.strShPrefUserPhone, "");
                Constants.fName = sharedPreferenceUser.getString(Constants.strShPrefUserFname, "");
                same_as_ship_addr = "0";
            }

            Constants.emailAdd = sharedPreferenceUser.getString(Constants.strShPrefUserEmail, "");
            Constants.address = shPrefDeliverAddr.getString(Constants.strShPrefDelAddr, "");
            Constants.latitude = shPrefDeliverAddr.getString(Constants.strShPrefDelLat, "");
            Constants.longitude = shPrefDeliverAddr.getString(Constants.strShPrefDelLong, "");
            Constants.cc_owner = shPrefDeliverAddr.getString("card_person_name", "");
            Constants.cc_owner_surname = shPrefDeliverAddr.getString("card_person_surname", "");
            Constants.cc_number = shPrefDeliverAddr.getString("card_number", "");
            Constants.cc_type = shPrefDeliverAddr.getString("card_type", "");
            Constants.cc_cvv = shPrefDeliverAddr.getString("cvv_cvv", "");
            Constants.cc_expiry_month = shPrefDeliverAddr.getString("card_expiry_month", "");
            Constants.cc_expiry_year = shPrefDeliverAddr.getString("card_expiry_year", "");
            Constants.cc_owner = shPrefDeliverAddr.getString("card_type", "");
            Constants.cc_card_type = shPrefDeliverAddr.getString("card_type_short", "");
            Constants.cc_nickname = shPrefDeliverAddr.getString("nickname", "");
            Constants.cc_token = shPrefDeliverAddr.getString("token", "");
            //Constants.cc_type=shPrefDeliverAddr.getString("cvv_number","");
            if (Constants.cc_owner_surname != null & !Constants.cc_owner_surname.isEmpty()) {
                Constants.cc_owner = Constants.cc_owner + " " + Constants.cc_owner_surname;
            }

            if (strDate.equalsIgnoreCase("NeededASAP")) {
                Constants.deliveryDateCheckout = "asap";
            } else {

            }

            String paymentType="";


            JSONArray jsonArray = new JSONArray();
            JSONObject jObjImages;
            for (int i = 0; i < AddToCartListAllItemAdapter.listAddToCartList.size(); i++) {
                jObjImages = new JSONObject();
                try {
                    jObjImages.put("attribute_id", AddToCartListAllItemAdapter.listAddToCartList.get(i).get("attribute_id"));
                    jObjImages.put("product_id", AddToCartListAllItemAdapter.listAddToCartList.get(i).get("product_id"));
                    jObjImages.put("option_id", AddToCartListAllItemAdapter.listAddToCartList.get(i).get("option_id"));
                    jObjImages.put("qty", AddToCartListAllItemAdapter.listAddToCartList.get(i).get("qty"));

                    jsonArray.put(i, jObjImages);
                    //Toast.makeText(getApplicationContext(), "JSONDATA: "+jsonArray.toString(), Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            String osVersion=utils.getAndroidVersion();
            String remoteipV4=utils.getIPAddress(true);
            String remoteipV6=utils.getIPAddress(false);
            String remoteip="Android - "+osVersion+" - "+remoteipV4+" - "+remoteipV6;
            pDialog = new ProgressDialog(CheckoutFinalActivity.this);
            pDialog.show();
            pDialog.setCancelable(false);
            pDialog.setCanceledOnTouchOutside(false);
            pDialog.setMessage("Loading... Please wait");
            restService.getPayment(Constants.fName, Constants.lName, Constants.emailAdd, Constants.mobilenum, Constants.address, Constants.latitude, Constants.longitude, jsonArray.toString(), "peach_payment", Constants.cc_number, Constants.cc_owner, Constants.cc_card_type, Constants.cc_cvv, Constants.cc_expiry_month, Constants.cc_expiry_year, Constants.cc_nickname, Constants.cc_token, Constants.deliverytypeCheckout, Constants.deliveryDateCheckout, ext_comment.getText().toString(), Constants.promo_coupon, "" + Constants.driver_tip, same_as_ship_addr, remoteip, new RestCallback<PaymentRequest>() {
                @Override
                public void success(PaymentRequest obj) {
                    pDialog.dismiss();

                    if (obj.getSuccess().toString().equalsIgnoreCase("1")) {

                        //getDialogAge(obj.getData().getOrderId());


                        Calendar now = Calendar.getInstance();
                        //Log.e("time", now.getTime().toString());

                        now.add(Calendar.MINUTE, 55);
                        //Log.e("time", now.getTime().toString());
                        Calendar now2 = Calendar.getInstance();
                        //Log.e("time", now.getTime().toString());

                        now2.add(Calendar.MINUTE, 0);

                        Calendar now1 = Calendar.getInstance();
                        //Log.e("time", now.getTime().toString());
                        now1.add(Calendar.MINUTE, 60);
                        // Log.e("time", now1.getTime().toString());

                        format = new SimpleDateFormat("HH:mm", Locale.UK);
                        format_date=new SimpleDateFormat("EEE, dd MMMM yyyy HH:mm", Locale.UK);
                        Calendar newDate = Calendar.getInstance();

                        Constants.order_status_date=format_date.format(newDate.getTime());

                        Constants.lastDeliveryTime0 = format.format(now2.getTime());
                        Constants.lastDeliveryTime40 = format.format(now.getTime());
                        Constants.lastDeliveryTime15 = format.format(now1.getTime());

                        /*if(Constants.paymentType.equalsIgnoreCase("Peach Payment")){
                            Intent intent = new Intent(CheckoutFinalActivity.this, AddPaymentActivity.class);
                            intent.putExtra("OrderID", obj.getData().getOrderId());
                            String price = "";
                            if (txt_total.getText().toString().contains(",")) {
                                price = txt_total.getText().toString().substring(1, txt_total.getText().toString().length()).replace(",", "");
                            } else {
                                price = txt_total.getText().toString().substring(1, txt_total.getText().toString().length());
                            }
                            intent.putExtra("TotalAmount", price);
                            intent.putExtra("Name", AddToCartListAllItemAdapter.listAddToCartList.get(0));
                            intent.putExtra("Description", ext_comment.getText().toString());
                            intent.putExtra("EmailAdd", Constants.emailAdd);
                            startActivity(intent);
                            //finish();
                            overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                        }else  if(Constants.paymentType.equalsIgnoreCase("Payfast")){*/
                            Intent intent = new Intent(CheckoutFinalActivity.this, AddPaymentActivity.class);
                            intent.putExtra("OrderID", obj.getData().getOrderId());
                            String price = "";
                            if (txt_total.getText().toString().contains(",")) {
                                price = txt_total.getText().toString().substring(1, txt_total.getText().toString().length()).replace(",", "");
                            } else {
                                price = txt_total.getText().toString().substring(1, txt_total.getText().toString().length());
                            }
                            intent.putExtra("TotalAmount", price);
                            intent.putExtra("Name", AddToCartListAllItemAdapter.listAddToCartList.get(0));
                            intent.putExtra("Description", ext_comment.getText().toString());
                            intent.putExtra("EmailAdd", Constants.emailAdd);
                            startActivity(intent);
                            //finish();
                            overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                        //}



                    } else {
                        displayAlert(obj.getErrorMsg());
                    }
                }

                @Override
                public void invalid() {
                    pDialog.dismiss();
                    utils.displayAlert("Problem while requesting.");
                }

                @Override
                public void failure() {
                    pDialog.dismiss();
                    utils.displayAlert("Internet connection is not available, try again.");
                }
            });
        }
    }
    public void displayAlert(String message)
    {
        // TODO Auto-generated method stub
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CheckoutFinalActivity.this);
        //alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setTitle("Stockup") ;
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                /*Intent intent = new Intent(CheckoutFinalActivity.this, AddPaymentActivity.class);
                intent.putExtra("for", "CheckoutFinal");
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);*/
            }
        });
        TextView myMsg = new TextView(CheckoutFinalActivity.this);
        myMsg.setText(message);
        myMsg.setPadding(20, 20, 20, 20);
        myMsg.setTextSize(16);
        myMsg.setTypeface(typeFaceSegoeuiReg);
        myMsg.setTextColor(Color.BLACK);
        myMsg.setGravity(Gravity.CENTER|Gravity.CENTER_HORIZONTAL);
        alertDialogBuilder.setView(myMsg);

        TextView title = new TextView(CheckoutFinalActivity.this);
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

    }
    /*private void getDialogAge(String orderid) {
        final Dialog dialog = new Dialog(CheckoutFinalActivity.this);
        Window window = dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_place_order);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        TextView header=(TextView)dialog.findViewById(R.id.header);
        header.setTypeface(typeFaceSegoeuiBold);
        TextView msg=(TextView)dialog.findViewById(R.id.msg);
        msg.setTypeface(typeFaceSegoeuiReg);
        TextView btn_yes=(TextView)dialog.findViewById(R.id.btn_ok);
        AddToCartListAllItemsActivity.size=0;
        Constants.orderID=orderid;
        //header.setText("Error");
        msg.setText("Your order " + orderid + " has been successfully placed. We are soooo onto it!");

        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                //Log.e("time", now.getTime().toString());

                now.add(Calendar.MINUTE,45);
                //Log.e("time", now.getTime().toString());
                Calendar now2 = Calendar.getInstance();
                //Log.e("time", now.getTime().toString());

                now2.add(Calendar.MINUTE,0);

                Calendar now1 = Calendar.getInstance();
                //Log.e("time", now.getTime().toString());
                now1.add(Calendar.MINUTE,55);
               // Log.e("time", now1.getTime().toString());

                format= new SimpleDateFormat("HH:mm", Locale.UK);

                Constants.lastDeliveryTime0=format.format(now2.getTime());
                Constants.lastDeliveryTime40=format.format(now.getTime());
                Constants.lastDeliveryTime15=format.format(now1.getTime());
                // Put Grocery selection for landing page
                *//*if(Constants.strTipPercent!=null)
                    Constants.strTipPercent="";
                dialog.dismiss();

                Constants.deliverytypeCheckout="";
                Constants.deliverytypeCart="";
                Constants.deliveryDate="";
                Constants.deliveryDateCheckout="";*//*
                dialog.dismiss();
                *//*if(txt_date.getText().toString().equalsIgnoreCase("ASAP")){
                Intent intent=new Intent(CheckoutFinalActivity.this, OrderStatusActivity.class);
                startActivity(intent);
                finish();}
                else{
                    if(Constants.strTipPercent!=null)
                        Constants.strTipPercent="";

                    Constants.deliverytypeCheckout="";
                    Constants.deliverytypeCart="";
                    Constants.deliveryDate="";
                    Constants.deliveryDateCheckout="";
                    Constants.promo_coupon_discount_price="";
                    Constants.promo_coupon="";
                    Intent intent=new Intent(CheckoutFinalActivity.this, LandingActivity.class);
                    startActivity(intent);
                    finish();

                }*//*
                Constants.emailAdd=sharedPreferenceUser.getString(Constants.strShPrefUserEmail,"");
                Intent intent=new Intent(CheckoutFinalActivity.this, PayMentGateWay.class);
                intent.putExtra("OrderID", Constants.orderID);
                String price="";
                if(txt_total.getText().toString().contains(",")){
                    price=txt_total.getText().toString().substring(1, txt_total.getText().toString().length()).replace(",","");
                }else{
                    price=txt_total.getText().toString().substring(1, txt_total.getText().toString().length());
                }
                intent.putExtra("TotalAmount", price);
                intent.putExtra("Name", AddToCartListAllItemAdapter.listAddToCartList.get(0));
                intent.putExtra("Description", ext_comment.getText().toString());
                intent.putExtra("EmailAdd", Constants.emailAdd);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                //CheckoutFinalActivity.this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }*/
}
