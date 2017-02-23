package webskitters.com.stockup;

import android.*;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.BoolRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

//import com.appsflyer.AppsFlyerLib;

import com.appsflyer.AppsFlyerLib;
import com.mobile.connect.PWConnect;
import com.mobile.connect.exception.PWError;
import com.mobile.connect.exception.PWException;
import com.mobile.connect.exception.PWProviderNotInitializedException;
import com.mobile.connect.listener.PWTokenObtainedListener;
import com.mobile.connect.listener.PWTransactionListener;
import com.mobile.connect.payment.PWCurrency;
import com.mobile.connect.payment.PWPaymentParams;
import com.mobile.connect.payment.credit.PWCreditCardType;
import com.mobile.connect.provider.PWTransaction;
import com.mobile.connect.service.PWProviderBinder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;
import scanpay.it.ScanPay;
import scanpay.it.ScanPayActivity;
import webskitters.com.stockup.Utils.Constants;
import webskitters.com.stockup.Utils.Utils;
import webskitters.com.stockup.adapter.SavedCardAdapter;
import webskitters.com.stockup.adapter.TipCatSpinnerAdapter;
import webskitters.com.stockup.dbhelper.AddToCartTable;
import webskitters.com.stockup.model.CardDetails;
import webskitters.com.stockup.model.CardDetailsRequest;
import webskitters.com.stockup.model.DeleteCardRequest;
import webskitters.com.stockup.model.LogOutRequest;
import webskitters.com.stockup.model.PaymentStatusUpdateRequest;
import webskitters.com.stockup.model.PeachPaymentStatusRequest;
import webskitters.com.stockup.retrofit_call.RestCallback;
import webskitters.com.stockup.retrofit_call.RestService;

/**
 * Created by android on 9/21/2016.
 */
public class AddPaymentActivity extends AppCompatActivity implements PWTokenObtainedListener, PWTransactionListener{

    private Typeface typeFaceSegoeuiReg;
    private Typeface typeFaceSegoeuiBold;
    Utils utils;
    SharedPreferences shPrefDeliverAddr, sharedPreferenceUser;
    SharedPreferences.Editor toEdit;
    String strSurName="", strCardNumber="", strExpiryMonth="",strExpiryYear="", forAct="", strNickName="";
    Bundle extras;
    ImageView imgBack, imgCamera;

    //ZenyEditText txt_expiry_year;
    RelativeLayout rel_add, rel_pay_with;
    private EditText txt_expiry_month,  txt_cvv, txt_card_number, txt_card_person_name, txt_card_person_surname, et_nickname;
    TextView txt_save_card;
    String strText = "";
    private String softketback;
    int count1=0;
    private LinearLayout lin_mm;
    private PopupWindow pw;
    int width=0, height=0;
    private TextView txt_mm, txt_yyyy;
    private LinearLayout lin_yyyy;
    RelativeLayout rel_card_type;
    TextView txt_card_type;
    private String strCardType="";
    private String card_short_type="";

    ScrollView mSrScrollView;
    private int MY_SCAN_REQUEST_CODE=100;
    static final int YOUR_RESULT_DEFINE = 4, CAMERA_PERMISSION_REQUEST_CODE = 101;
    TextView txt_scan_card;
    String customer_id = "";
    RestService restService;
    private TextView txt_saved_card;
    List<CardDetails> arrCard;

    ////////////////////
    TextView txt_payment_status;

    Button btn_pay;
    TextView txt_success;
    String strName = "", strCardNo = "", strCVV = "", month = "", year = "";
    Double amtPay = 0.0;
    ProgressDialog pDialog;
    boolean callTokenSave = false;
    boolean saveTokenOnCheck=false;

    private PWProviderBinder _binder;

    // Test
    private static final String APPLICATIONIDENTIFIER = "peach.Stockup.mcommerce.test";
    private static final String PROFILETOKEN = "7e352877b32a11e6a349d9c796a85012";
    // Live
    //private static final String APPLICATIONIDENTIFIER = "peach.Stockup.mcommerce";
    //private static final String PROFILETOKEN = "e933ab24b32811e68fe38909e007f9c9";

    String itemName="", orderDescription="", orderID="", total_price="", email_add="";
    CheckBox chk_save_card;
    private boolean save;
    private String card_type="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_add_payment);
        startService(new Intent(this, com.mobile.connect.service.PWConnectService.class));
        bindService(new Intent(this,com.mobile.connect.service.PWConnectService.class),
                _serviceConnection, Context.BIND_AUTO_CREATE);
        typeFaceSegoeuiReg = Typeface.createFromAsset(getAssets(),
                "Roboto-Regular.ttf");
        typeFaceSegoeuiBold = Typeface.createFromAsset(getAssets(),
                "ROBOTO-BOLD_0.TTF");
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;

        utils=new Utils(AddPaymentActivity.this);
        restService = new RestService(this);
        Bundle extras=getIntent().getExtras();
        if(extras!=null){
            forAct=extras.getString("for");
        }

        Intent oIntent  = getIntent();

        if(oIntent.getExtras()!=null) {
            //itemName = oIntent.getExtras().getString("Name");
            total_price = oIntent.getExtras().getString("TotalAmount");
            amtPay=Double.parseDouble(total_price);
            orderID = oIntent.getExtras().getString("OrderID");
            //orderDescription = oIntent.getExtras().getString("Description");
            email_add = oIntent.getExtras().getString("EmailAdd");
        }

        // Track Data : Add to activities where tracking needed
        AppsFlyerLib.getInstance().sendDeepLinkData(this);

        initFields();
    }

    private void initFields() {
        shPrefDeliverAddr = this.getSharedPreferences(Constants.strShPrefDelAddrName, Context.MODE_PRIVATE);
        mSrScrollView=(ScrollView)findViewById(R.id.scrollview);
        txt_payment_status=(TextView)findViewById(R.id.txt_payment_status);
        rel_card_type=(RelativeLayout)findViewById(R.id.rel_card_type);
        txt_saved_card=(TextView)findViewById(R.id.txt_saved_card);
        rel_card_type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard(AddPaymentActivity.this);
                callPopUpCardType(rel_card_type);
            }
        });
        txt_card_type=(TextView)findViewById(R.id.txt_card_type);
        txt_card_type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard(AddPaymentActivity.this);
                callPopUpCardType(rel_card_type);
            }
        });
        chk_save_card=(CheckBox)findViewById(R.id.chk_save_card);
        chk_save_card.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    saveTokenOnCheck=true;
                }else{
                    saveTokenOnCheck=false;
                }
            }
        });
        txt_scan_card=(TextView)findViewById(R.id.txt_scan_card);
        txt_scan_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPhotoFromCamera();
            }
        });

        sharedPreferenceUser=this.getSharedPreferences(Constants.strShPrefUserPrefName, Context.MODE_PRIVATE);
        customer_id=sharedPreferenceUser.getString(Constants.strShPrefUserId, "");
        if (utils.isConnectionPossible()){
            getSavedCard(customer_id);
        }
        else {
            txt_saved_card.setVisibility(View.GONE);
        }

        imgBack=(ImageView)findViewById(R.id.img_back);
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPeachPayStatus("cancel");
                AddPaymentActivity.this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });
        imgCamera=(ImageView)findViewById(R.id.img_camera);
        imgCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getDialogCoverage();
                getPhotoFromCamera();
            }
        });
        txt_card_number=(EditText)findViewById(R.id.txt_card_number);
        if(!strCardNumber.equalsIgnoreCase("")&&strCardNumber!=null){
            txt_card_number.setText(strCardNumber);
        }

        if(!strCardType.equalsIgnoreCase("")&&strCardType!=null){
            txt_card_type.setText(strCardType);
        }

        /*txt_expiry_year=(ZenyEditText)findViewById(R.id.et_exp_dt);
        if(!strExpiryMonthYear.equalsIgnoreCase("")&&strExpiryMonthYear!=null){
            txt_expiry_year.setText(strExpiryMonthYear);
        }*/

        lin_mm=(LinearLayout)findViewById(R.id.lin_mm);
        lin_mm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard(AddPaymentActivity.this);
                callPopUpMonth(lin_mm);

            }
        });

        lin_yyyy=(LinearLayout)findViewById(R.id.lin_yyyy);
        lin_yyyy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard(AddPaymentActivity.this);
                callPopUpYear(lin_yyyy);
            }
        });

        txt_mm=(TextView)findViewById(R.id.txt_mm);
        if(!strExpiryMonth.equalsIgnoreCase("")&&strExpiryMonth!=null){
            txt_mm.setText(strExpiryMonth);
        }
        txt_yyyy=(TextView)findViewById(R.id.txt_yyyy);

        if(!strExpiryYear.equalsIgnoreCase("")&&strExpiryYear!=null){
            txt_yyyy.setText(strExpiryYear);
        }

        txt_cvv=(EditText)findViewById(R.id.et_cvv);
        if(!strCVV.equalsIgnoreCase("")&&strCVV!=null){
            txt_cvv.setText(strCVV);
        }
        txt_card_person_name=(EditText)findViewById(R.id.et_name);
        if(!strName.equalsIgnoreCase("")&&strName!=null){
            txt_card_person_name.setText(strName);
        }
        txt_card_person_surname=(EditText)findViewById(R.id.et_surname) ;
        if(!strSurName.equalsIgnoreCase("")&&strSurName!=null){
            txt_card_person_surname.setText(strSurName);
        }
        et_nickname=(EditText)findViewById(R.id.et_nickname);
        if(!strNickName.equalsIgnoreCase("")&&strNickName!=null){
            et_nickname.setText(strNickName);
        }
        et_nickname.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mSrScrollView.scrollTo(0, height);
                return false;
            }
        });
        txt_save_card=(TextView)findViewById(R.id.txt_save_card);
        txt_save_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (txt_card_person_name.getText().toString().trim().equalsIgnoreCase("")) {
                    utils.displayAlert("Provide First Name.");
                }
                else if (txt_card_person_surname.getText().toString().trim().equalsIgnoreCase("")) {
                    utils.displayAlert("Provide Surname.");
                }else if (txt_card_number.getText().toString().trim().equalsIgnoreCase("")) {
                    utils.displayAlert("Provide Card Number.");
                }else if (txt_card_type.getText().toString().trim().equalsIgnoreCase("")) {
                    utils.displayAlert("Provide Card Type.");
                } else if (txt_mm.getText().toString().trim().equalsIgnoreCase("")) {
                    utils.displayAlert("Provide a valid card expiry month.");
                } else if (txt_yyyy.getText().toString().trim().equalsIgnoreCase("")) {
                    utils.displayAlert("Provide a valid card expiry year.");
                } else if (txt_cvv.getText().toString().trim().equalsIgnoreCase("")) {
                    utils.displayAlert("Please enter card CVV number.");
                } else if (et_nickname.getText().toString().trim().equalsIgnoreCase("")) {
                    utils.displayAlert("Provide a nickname for this card.");
                } else if (!utils.cardExpiryDate(txt_mm.getText().toString().trim(),txt_yyyy.getText().toString().trim())){
                    utils.displayAlert("Provide a valid card expiry date (MM/YYYY).");
                } else{
                    toEdit = shPrefDeliverAddr.edit();
                    toEdit.putString("card_person_name", txt_card_person_name.getText().toString());
                    toEdit.putString("card_person_surname", txt_card_person_surname.getText().toString());
                    toEdit.putString("card_number", txt_card_number.getText().toString());
                    toEdit.putString("card_expiry_month", txt_mm.getText().toString());
                    toEdit.putString("card_expiry_year", txt_yyyy.getText().toString());
                    toEdit.putString("card_type", txt_card_type.getText().toString());
                    toEdit.putString("card_type_short", card_short_type);
                    toEdit.putString("cvv_cvv", txt_cvv.getText().toString());
                    toEdit.putString("nickname", et_nickname.getText().toString());
                    toEdit.putString("token", "");
                    toEdit.putString("is_from_saved_card", "No");
                    toEdit.putString("is_card_details_inputed", "Yes");
                    toEdit.commit();
                    callTokenSave=true;
                    save=false;
                    payUsingPeach();
                    }

            }
        });
        txt_saved_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getDialogSavedCard();
                //callPopUpSavedCard(txt_saved_card);
                getDialogSearch();
            }
        });
    }


    private void getSavedCard(String customer_id) {
        arrCard = new ArrayList<>();
        final ProgressDialog pDialog=new ProgressDialog(AddPaymentActivity.this);
        pDialog.show();
        pDialog.setMessage("Loading... Please wait.");
        String strCustId = sharedPreferenceUser.getString(Constants.strShPrefUserId, "");
        restService.getSaveCard(strCustId, new RestCallback<CardDetailsRequest>() {
            @Override
            public void success(CardDetailsRequest responce) {
                int reqStatus = responce.getStatus();
                int reqSuccess = responce.getSuccess();
                if (reqStatus == 200 && reqSuccess == 1) {
                    arrCard = responce.getData().getToken();
                } else {
                    txt_saved_card.setVisibility(View.GONE);
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

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setPeachPayStatus("cancel");
        /*if (forAct.equalsIgnoreCase("Checkout")) {
            Intent intent = new Intent(AddPaymentActivity.this, CheckOutActivity.class);
            finish();
            startActivity(intent);
        } else {
            Intent intent = new Intent(AddPaymentActivity.this, CheckoutFinalActivity.class);
            finish();
            startActivity(intent);
        } *//*Intent intent = new Intent(AddPaymentActivity.this, CheckoutFinalActivity.class);
        finish();
        startActivity(intent);
        AddPaymentActivity.this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);*/
    }

    private void callPopUpCardType(View anchorView) {

        pw = new PopupWindow(callPopUpCardType(R.layout.pop_up_menu, new Vector()),anchorView.getWidth(),height/2, true);
        pw.setBackgroundDrawable(new BitmapDrawable());
        pw.setOutsideTouchable(true);
        pw.showAsDropDown(anchorView);
        pw.update();
    }

    private View callPopUpCardType(int layout, Vector menuItem)
    {
        View view = null;
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(layout, null, false);
        //String [] values = MyProfileEditListParser.LangNameList.toArray(new String[0]);

        final ArrayList<String> arrItem=new ArrayList<>();

        arrItem.add("Visa");
        arrItem.add("MasterCard");
       // arrItem.add("JCB");
        final ArrayList<String> arrItemShortType=new ArrayList<>();

        arrItemShortType.add("VI");
        arrItemShortType.add("MC");
       // arrItemShortType.add("JCB");

        TipCatSpinnerAdapter searchLangAdapter = new TipCatSpinnerAdapter(AddPaymentActivity.this, arrItem);
        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(Registration1Activity.this,android.R.layout.simple_list_item_1,android.R.id.text1,values);

        ListView listView = (ListView)view.findViewById(R.id.pop_up_menu_list);
        listView.setAdapter(searchLangAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

                txt_card_type.setText(arrItem.get(position));
                card_type=arrItem.get(position);
                card_short_type=arrItemShortType.get(position);
                //AtnCareApplicationClass.strPrefLang = MyProfileEditListParser.LangIdList.get(position).toString();
                //AtnCareApplicationClass.strPrefLangName = MyProfileEditListParser.LangNameList.get(position);
                //etPrefLang.setTag("1");
                //etPrefLang.setTextSize(LoginActivity.textSizelogin + 4);
                pw.dismiss();

            }
        });

        return view;
    }
    private void callPopUpMonth(View anchorView) {

        pw = new PopupWindow(dropDownMenuMonth(R.layout.pop_up_menu, new Vector()),anchorView.getWidth(),height/2, true);
        pw.setBackgroundDrawable(new BitmapDrawable());
        pw.setOutsideTouchable(true);
        pw.showAsDropDown(anchorView);
        pw.update();
    }

    private View dropDownMenuMonth(int layout, Vector menuItem)
    {
        View view = null;
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(layout, null, false);
        //String [] values = MyProfileEditListParser.LangNameList.toArray(new String[0]);

        final ArrayList<String> arrItem=new ArrayList<>();

        arrItem.add("01");
        arrItem.add("02");
        arrItem.add("03");
        arrItem.add("04");
        arrItem.add("05");
        arrItem.add("06");
        arrItem.add("07");
        arrItem.add("08");
        arrItem.add("09");
        arrItem.add("10");
        arrItem.add("11");
        arrItem.add("12");

        TipCatSpinnerAdapter searchLangAdapter = new TipCatSpinnerAdapter(AddPaymentActivity.this, arrItem);
        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(Registration1Activity.this,android.R.layout.simple_list_item_1,android.R.id.text1,values);

        ListView listView = (ListView)view.findViewById(R.id.pop_up_menu_list);
        listView.setAdapter(searchLangAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

                txt_mm.setText(arrItem.get(position));
                //AtnCareApplicationClass.strPrefLang = MyProfileEditListParser.LangIdList.get(position).toString();
                //AtnCareApplicationClass.strPrefLangName = MyProfileEditListParser.LangNameList.get(position);
                //etPrefLang.setTag("1");
                //etPrefLang.setTextSize(LoginActivity.textSizelogin + 4);
                pw.dismiss();

            }
        });

        return view;
    }
    private void getDialogSearch() {
        final Dialog dialog = new Dialog(AddPaymentActivity.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_auto_search);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        TextView txt_header=(TextView) dialog.findViewById(R.id.txt_header);
        txt_header.setText("Saved Cards");
        RelativeLayout rel_header=(RelativeLayout)dialog.findViewById(R.id.rel_header);
        rel_header.setBackgroundColor(Color.parseColor("#70B74E"));
        ImageView imgCreditCrdIcon=(ImageView)dialog.findViewById(R.id.img_credit_card);
        imgCreditCrdIcon.setVisibility(View.VISIBLE);
        LinearLayout rel=(LinearLayout) dialog.findViewById(R.id.lin_header);
        rel.setVisibility(View.GONE);
        ImageView img_back=(ImageView)dialog.findViewById(R.id.img_back_search);
        final ListView lv=(ListView)dialog.findViewById(R.id.listview);
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard(AddPaymentActivity.this);
                if(dialog.isShowing())
                    dialog.dismiss();
            }
        });

        SavedCardAdapter searchLangAdapter = new SavedCardAdapter(AddPaymentActivity.this, arrCard);
        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(Registration1Activity.this,android.R.layout.simple_list_item_1,android.R.id.text1,values);


        lv.setAdapter(searchLangAdapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

                //txt_mm.setText(arrItem.get(position));
                //AtnCareApplicationClass.strPrefLang = MyProfileEditListParser.LangIdList.get(position).toString();
                //AtnCareApplicationClass.strPrefLangName = MyProfileEditListParser.LangNameList.get(position);
                //etPrefLang.setTag("1");
                //etPrefLang.setTextSize(LoginActivity.textSizelogin + 4);
                String expiry=arrCard.get(position).getExpire();
                String[] monthyear=expiry.split("/");
                String month=monthyear[0];
                String year=monthyear[1];

                toEdit = shPrefDeliverAddr.edit();
                toEdit.putString("card_person_name", arrCard.get(position).getCardNickname());
                toEdit.putString("card_person_surname", "");
                toEdit.putString("card_number", arrCard.get(position).getCardLastFour());
                toEdit.putString("card_expiry_month", month);
                toEdit.putString("card_expiry_year", year);
                toEdit.putString("card_type", arrCard.get(position).getCardBrand());
                if(arrCard.get(position).getCardBrand().equalsIgnoreCase("VISA")){
                    toEdit.putString("card_type_short", "VI");
                }else if(arrCard.get(position).getCardBrand().equalsIgnoreCase("MASTER")){
                    toEdit.putString("card_type_short", "MC");
                }

                toEdit.putString("cvv_cvv", "");
                toEdit.putString("nickname", arrCard.get(position).getCardNickname());
                toEdit.putString("token", arrCard.get(position).getToken());
                toEdit.putString("is_from_saved_card", "Yes");
                toEdit.putString("is_card_details_inputed", "Yes");
                toEdit.commit();
                getDialogConfirm(arrCard.get(position).getCardNickname(),arrCard.get(position).getToken(), dialog );


                }
        });

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                getDialog(position, lv);
                return true;
            }
        });

        dialog.show();
    }

    private void getDialogConfirm(final String nickname, final String token, final Dialog parent) {
        final Dialog dialog = new Dialog(AddPaymentActivity.this);
        Window window = dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_exit);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        //TextView header=(TextView)dialog.findViewById(R.id.header);
        TextView msg = (TextView) dialog.findViewById(R.id.msg);
        msg.setText("\nShould we process payment using card "+nickname+"?");
        TextView txt_date = (TextView) dialog.findViewById(R.id.txt_date);
        txt_date.setVisibility(View.GONE);

        final Button btn_yes=(Button)dialog.findViewById(R.id.btn_yes);
        btn_yes.setText("Yes");
        Button btn_no=(Button)dialog.findViewById(R.id.btn_no);
        btn_no.setText("Change");

        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                payUsingToken(token, nickname);
                callTokenSave=true;
                save=true;
                if(parent!=null&&parent.isShowing())
                    parent.dismiss();
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
    private void getDialog(final int i, final ListView lv) {
        final Dialog dialog = new Dialog(AddPaymentActivity.this);
        Window window = dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_age);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView tvHeader=(TextView)dialog.findViewById(R.id.header);
        tvHeader.setText("Stockup");
        TextView tvMsg=(TextView)dialog.findViewById(R.id.msg);
        tvMsg.setText("Do you want to delete this card?");
        Button btn_no=(Button)dialog.findViewById(R.id.btn_no);
        Button btn_yes=(Button)dialog.findViewById(R.id.btn_yes);


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
                deleteCard(arrCard.get(i).getTokenId(), i, lv);

            }
        });
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
    private void deleteCard(String token_id, final int i, final ListView lv) {
        final ProgressDialog pDialog=new ProgressDialog(AddPaymentActivity.this);
        pDialog.show();
        pDialog.setMessage("Loading..");
        String strCustId = sharedPreferenceUser.getString(Constants.strShPrefUserId, "");
        restService.deleteCard(token_id, new RestCallback<DeleteCardRequest>() {
            @Override
            public void success(DeleteCardRequest responce) {
                int reqStatus = responce.getStatus();
                int reqSuccess = responce.getSuccess();
                if (reqStatus == 200 && reqSuccess == 1) {
                    arrCard.remove(i);
                    if(arrCard.size()==0){
                        lv.setVisibility(View.INVISIBLE);
                    }
                    SavedCardAdapter searchLangAdapter = new SavedCardAdapter(AddPaymentActivity.this, arrCard);
                    //ArrayAdapter<String> adapter = new ArrayAdapter<String>(Registration1Activity.this,android.R.layout.simple_list_item_1,android.R.id.text1,values);


                    lv.setAdapter(searchLangAdapter);
                    //getSavedCard(customer_id);
                } else {
                    if(arrCard.size()==0){
                        lv.setVisibility(View.INVISIBLE);
                    }
                    //getSavedCard(customer_id);

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


    private void callPopUpYear(View anchorView) {

        pw = new PopupWindow(dropDownMenuYear(R.layout.pop_up_menu, new Vector()),anchorView.getWidth(),height/2, true);
        pw.setBackgroundDrawable(new BitmapDrawable());
        pw.setOutsideTouchable(true);
        pw.showAsDropDown(anchorView);
        pw.update();
    }
    private View dropDownMenuYear(int layout, Vector menuItem)
    {
        View view = null;
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(layout, null, false);
        final ArrayList<String> arrItem=new ArrayList<>();
        arrItem.add("2016");
        arrItem.add("2017");
        arrItem.add("2018");
        arrItem.add("2019");
        arrItem.add("2020");
        arrItem.add("2021");
        arrItem.add("2022");
        arrItem.add("2023");
        arrItem.add("2024");
        arrItem.add("2025");
        arrItem.add("2026");
        arrItem.add("2027");
        arrItem.add("2028");
        arrItem.add("2029");
        arrItem.add("2030");
        TipCatSpinnerAdapter searchLangAdapter = new TipCatSpinnerAdapter(AddPaymentActivity.this, arrItem);
        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(Registration1Activity.this,android.R.layout.simple_list_item_1,android.R.id.text1,values);
        ListView listView = (ListView)view.findViewById(R.id.pop_up_menu_list);
        listView.setAdapter(searchLangAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

                txt_yyyy.setText(arrItem.get(position));
                pw.dismiss();

            }
        });

        return view;
    }
    private void getDialogSavedCard() {
        final Dialog dialog = new Dialog(AddPaymentActivity.this);
        Window window = dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_saved_card);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        ListView listview = (ListView) dialog.findViewById(R.id.listview);
        TextView txt_ok = (TextView) dialog.findViewById(R.id.txt_ok);
        TextView txt_cancel = (TextView) dialog.findViewById(R.id.txt_cancel);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // get the selected value
            }
        });

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }
   /* @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(forAct.equalsIgnoreCase("Checkout")){
            Intent intent = new Intent(AddPaymentActivity.this, CheckOutActivity.class);
            finish();
            startActivity(intent);
        }else{
            Intent intent = new Intent(AddPaymentActivity.this, CheckoutFinalActivity.class);
            finish();
            startActivity(intent);
        }

        AddPaymentActivity.this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }*/



    // ============ SCANPAY METHODS ================
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // The result code is ScanPay.RESULT_SCAN_SUCCESS when we succeed to scan
        if (requestCode == YOUR_RESULT_DEFINE && resultCode == ScanPay.RESULT_SCAN_SUCCESS)
        {
            scanpay.it.CreditCard creditCard = (scanpay.it.CreditCard) data.getParcelableExtra(ScanPay.EXTRA_CREDIT_CARD);
            txt_card_number.setText(creditCard.number);
            txt_mm.setText(creditCard.month);
            txt_yyyy.setText("20"+creditCard.year);
            txt_cvv.setText(creditCard.cvv);
            //Toast.makeText(this, creditCard.number + " " + creditCard.month + "/" + creditCard.year + " " + creditCard.cvv.length(), Toast.LENGTH_LONG).show();
        }
        // The result code is ScanPay.RESULT_SCAN_CANCEL when the user back
        else if (requestCode == YOUR_RESULT_DEFINE && resultCode == ScanPay.RESULT_SCAN_CANCEL)
        {
            Toast.makeText(this, "Scan cancel", Toast.LENGTH_LONG).show();
        }
    }

    public boolean checkPermissionForCamera(){
        int result = ContextCompat.checkSelfPermission(AddPaymentActivity.this, android.Manifest.permission.CAMERA);
        if (result == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
            return false;
        }
    }

    public void requestPermissionCamera(String strPermission, int perCode, Context _c, Activity _a){
        if (ActivityCompat.shouldShowRequestPermissionRationale(_a, strPermission)){
            ActivityCompat.requestPermissions(_a,new String[]{strPermission},perCode);
            //Toast.makeText(VisitorHomePageActivity.this,"GPS permission allows us to access location data. Please allow in App Settings for additional functionality.",Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(_a, new String[]{strPermission}, perCode);
        }
    }

    private void payUsingToken(String token, String name){

        strName = name;
        strCardNo = txt_card_number.getText().toString().trim();
        strCVV = txt_cvv.getText().toString().trim();

        month = txt_mm.getText().toString().trim();
        year = txt_yyyy.getText().toString().trim();

        //PWPaymentParams paymentParams = null;

        String customIdentifier  = orderID;
        String givenName = strName;
        String familyName = strName;
       /* String street = "1st Avenue";
        String zip = "711109";
        String city = "Kolkata";
        String state = "West Bengal";
        String countryCode = "+91";*/
        String email = sharedPreferenceUser.getString(Constants.strShPrefUserEmail,"");
        // create token parameters
        PWPaymentParams tokenParams = null;

        String IPaddress = utils.getIPAddress(true) +"-"+ utils.getIPAddress(false);

        try {
            tokenParams = _binder.getPaymentParamsFactory().createTokenPaymentParams(amtPay, PWCurrency.SOUTH_AFRICA_RAND, customIdentifier, token);
            tokenParams.setCustomIdentifier(customIdentifier);
            tokenParams.setCustomerGivenName(givenName);
            tokenParams.setCustomerFamilyName(familyName);
            /*tokenParams.setCustomerAddressStreet(street);
            tokenParams.setCustomerAddressZip(zip);
            tokenParams.setCustomerAddressCity(city);
            tokenParams.setCustomerAddressState(state);
            tokenParams.setCustomerAddressCountryCode(countryCode);*/
            tokenParams.setCustomerEmail(email);
            tokenParams.setCustomerIP(IPaddress);

        } catch (PWProviderNotInitializedException e) {
            setStatusText("Error: Provider not initialized!", false);
            e.printStackTrace();
            return;
        } catch (PWException e) {
            setStatusText("Error: Invalid Parameters!", false);
            e.printStackTrace();
            return;
        }
        try {
            pDialog = new ProgressDialog(AddPaymentActivity.this);
            pDialog.show();
            pDialog.setTitle("Warning");
            pDialog.setMessage("Your payment is under process. Please do not press back or home button until the process is completed.");
            pDialog.setCanceledOnTouchOutside(false);
            _binder.createAndRegisterDebitTransaction(tokenParams);
        } catch(PWException e) {
            setStatusText("Error: Could not contact Gateway!", false);
            e.printStackTrace();
        }
    }

    private void payUsingPeach() {
        strName = txt_card_person_name.getText().toString().trim()+" "+txt_card_person_surname.getText().toString().trim();
        strCardNo = txt_card_number.getText().toString().trim();
        strCVV = txt_cvv.getText().toString().trim();

        //if (txt_mm.getText().toString().trim().length() == 7){
            month = txt_mm.getText().toString().trim();
            year = txt_yyyy.getText().toString().trim();
        /*}
        else {
            Toast.makeText(AddPaymentActivity.this,"Expiary Date not in correct Format.", Toast.LENGTH_LONG).show();
        }*/

        PWPaymentParams paymentParams = null;

        String customIdentifier  = orderID;
        String givenName = strName;
        String familyName = strName;
       /* String street = "1st Avenue";
        String zip = "711109";
        String city = "Kolkata";
        String state = "West Bengal";
        String countryCode = "+91";*/
        String email = sharedPreferenceUser.getString(Constants.strShPrefUserEmail,"");
        String IPaddress = Utils.getIPAddress(true)+"-"+ utils.getIPAddress(false);
        try {

            if(card_type.equalsIgnoreCase("MasterCard")){
                paymentParams = _binder.getPaymentParamsFactory().createCreditCardPaymentParams(amtPay, PWCurrency.SOUTH_AFRICA_RAND, customIdentifier, strName, PWCreditCardType.MASTERCARD, strCardNo, year, month, strCVV);
            }else{
                paymentParams = _binder.getPaymentParamsFactory().createCreditCardPaymentParams(amtPay, PWCurrency.SOUTH_AFRICA_RAND, customIdentifier, strName, PWCreditCardType.VISA, strCardNo, year, month, strCVV);
            }
            paymentParams.setCustomIdentifier(customIdentifier);
            paymentParams.setCustomerGivenName(givenName);
            paymentParams.setCustomerFamilyName(familyName);
            /*paymentParams.setCustomerAddressStreet(street);
            paymentParams.setCustomerAddressZip(zip);
            paymentParams.setCustomerAddressCity(city);
            paymentParams.setCustomerAddressState(state);
            paymentParams.setCustomerAddressCountryCode(countryCode);*/
            paymentParams.setCustomerEmail(email);
            paymentParams.setCustomerIP(IPaddress);

        } catch (PWProviderNotInitializedException e) {
            if (pDialog != null && pDialog.isShowing()){
                pDialog.dismiss();
            }
            e.printStackTrace();
            return;
        } catch (PWException e) {
            e.printStackTrace();
            return;
        }

        try {
            pDialog = new ProgressDialog(AddPaymentActivity.this);
            pDialog.show();
            pDialog.setTitle("Warning");
            pDialog.setMessage("Your payment is under process. Please do not press back or home button until the process is completed.");
            pDialog.setCanceledOnTouchOutside(false);
            _binder.createAndRegisterDebitTransaction(paymentParams);
        } catch (PWException e) {
            if (pDialog != null && pDialog.isShowing()){
                pDialog.dismiss();
            }
            setStatusText("Error: Could not contact Gateway!", false);
            //Toast.makeText(PeachPaymentActivity.this,"Error: Could not contact Gateway!", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unbindService(_serviceConnection);
        stopService(new Intent(this,
                com.mobile.connect.service.PWConnectService.class));
    }
    public void getPhotoFromCamera() {

        if (!checkPermissionForCamera()) {
            //requestPermissionForCamera();
            requestPermissionCamera(android.Manifest.permission.CAMERA, CAMERA_PERMISSION_REQUEST_CODE, getApplicationContext(), AddPaymentActivity.this);
        } else {
            Intent scanActivity = new Intent(AddPaymentActivity.this, ScanPayActivity.class);
            scanActivity.putExtra(ScanPay.EXTRA_TOKEN, "7ebf2e0b72c151a3eb9f49cb");

            //Put true if you want use your own manual entry UI
            scanActivity.putExtra(ScanPay.EXTRA_SHOULD_SHOW_CONFIRMATION_VIEW, true);

            // You can hide button like this
            scanActivity.putExtra(ScanPay.EXTRA_SHOULD_SHOW_MANUAL_ENTRY_BUTTON, false);
            startActivityForResult(scanActivity, YOUR_RESULT_DEFINE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case CAMERA_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getPhotoFromCamera();
                } else {
                    Toast.makeText(getApplicationContext(), "Permission Denied, You cannot access location data.", Toast.LENGTH_LONG).show();
                    //splashHandlar(SPLASH_TIME_OUT);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    String cardToken="";
    @Override
    public void obtainedToken(String token, PWTransaction pwTransaction) {
        /*if (callTokenSave){
            saveCard();

        }*/
        cardToken=token;
        setStatusText("", true);        // Call method to store token in server.
        // Call setPeachPayStatus from thread
    }

    @Override
    public void transactionSucceeded(PWTransaction pwTransaction) {

        if(save){
            setStatusText("", true);
            save=false;
        }else {
            if (callTokenSave&&saveTokenOnCheck) {
                saveCard();
            } else {
                setStatusText("", true);
            }
        }
    }

    @Override
    public void transactionFailed(PWTransaction pwTransaction, PWError pwError) {
        setStatusText("Error contacting the gateway.", false);
        if (pDialog != null && pDialog.isShowing()){
            pDialog.dismiss();
        }
        Log.e("TokenizationActivity", pwError.getErrorMessage());
    }

    @Override
    public void creationAndRegistrationSucceeded(PWTransaction pwTransaction) {
        setStatusText("Processing...", false);
        try {
            if (callTokenSave) {
                _binder.debitTransaction(pwTransaction);
                //callTokenSave=false;
            }
            else {
                _binder.obtainToken(pwTransaction);
            }
        } catch (PWException e) {
            if (pDialog != null && pDialog.isShowing()){
                pDialog.dismiss();
            }
            setStatusText("Invalid Transaction.", false);
            e.printStackTrace();
        }
    }

    @Override
    public void creationAndRegistrationFailed(PWTransaction pwTransaction, PWError pwError) {
        if (pDialog != null && pDialog.isShowing()){
            pDialog.dismiss();
        }
        setStatusText("Error contacting the gateway.", false);
        Log.e("TokenizationActivity", pwError.getErrorMessage());
    }
    private void saveCard() {
        PWPaymentParams paymentParams = null;
        try {
            if(card_type.equalsIgnoreCase("MasterCard")){
                paymentParams = _binder
                        .getPaymentParamsFactory()
                        .createCreditCardTokenizationParams(strName, PWCreditCardType.MASTERCARD, strCardNo, year, month, strCVV);
            }else{
                paymentParams = _binder
                        .getPaymentParamsFactory()
                        .createCreditCardTokenizationParams(strName, PWCreditCardType.VISA, strCardNo, year, month, strCVV);
            }


        } catch (PWProviderNotInitializedException e) {
            if (pDialog != null && pDialog.isShowing()){
                pDialog.dismiss();
            }
            setStatusText("Error: Provider not initialized!", false);
            e.printStackTrace();
            return;
        } catch (PWException e) {
            if (pDialog != null && pDialog.isShowing()){
                pDialog.dismiss();
            }
            setStatusText("Error: Invalid Parameters!", false);
            e.printStackTrace();
            return;
        }

        callTokenSave = false;

        try {
            _binder.createAndRegisterObtainTokenTransaction(paymentParams);
        } catch (PWException e) {
            if (pDialog != null && pDialog.isShowing()){
                pDialog.dismiss();
            }
            setStatusText("Error: Could not contact Gateway!", false);
            //Toast.makeText(PeachPaymentActivity.this,"Error: Could not contact Gateway!", Toast.LENGTH_LONG).show();
            //setStatusText("Error: Could not contact Gateway!");
            e.printStackTrace();
        }
    }
    private ServiceConnection _serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            _binder = (PWProviderBinder) service;
            // we have a connection to the service
            try {
                _binder.initializeProvider(PWConnect.PWProviderMode.TEST,
                        APPLICATIONIDENTIFIER, PROFILETOKEN);
                _binder.addTransactionListener(AddPaymentActivity.this);
                _binder.addTokenObtainedListener(AddPaymentActivity.this);
               /* _binder.initializeProvider(PWConnect.PWProviderMode.LIVE,
                        APPLICATIONIDENTIFIER, PROFILETOKEN);*/
            } catch (PWException ee) {
                //setStatusText("Error initializing the provider.");
                setStatusText("Error initializing the provider.", false);
                //Toast.makeText(PeachPaymentActivity.this,"Error initializing the provider.", Toast.LENGTH_LONG).show();
                // error initializing the provider
                ee.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            _binder = null;
        }
    };
    private void setStatusText(final String msg, final Boolean boolPaymentStatus) {
        runOnUiThread(new Runnable() {
            public void run() {
                txt_payment_status.setVisibility(View.VISIBLE);
                txt_payment_status.setText(msg);
                if (boolPaymentStatus) {
                    setPeachPayStatus("complete");
                }
            }
        });
    }

    // Update Payment Status after payment success or cancel
    private void setPeachPayStatus(final String status){
        pDialog=new ProgressDialog(AddPaymentActivity.this);
        pDialog.setMessage("Loading... Please wait.");
        pDialog.setCanceledOnTouchOutside(false);
        pDialog.show();
        restService=new RestService(this);

        //String cc_owner = shPrefDeliverAddr.getString("card_person_name", "");
        String cc_owner = shPrefDeliverAddr.getString("nickname", "");
        String cc_number = shPrefDeliverAddr.getString("card_number", "");
        String cc_exp_month = shPrefDeliverAddr.getString("card_expiry_month", "");
        String cc_exp_year = shPrefDeliverAddr.getString("card_expiry_year", "");
        String cc_type = shPrefDeliverAddr.getString("card_type_short", "");;
        String account_number_count_of_digits = String.valueOf(cc_number.length());
        //String token = shPrefDeliverAddr.getString("token", "");
        String cc_last4="";
        if(cc_number.length()==4){
            cc_last4= cc_number;
        }else if(cc_number.length()>4){
            cc_last4= cc_number.substring(cc_number.length()-4);
        }else{
            cc_last4="";
        }

        restService.setPeachPayStatus(orderID, orderID, status, cc_last4, cc_owner, cc_exp_month,
                cc_exp_year, cc_type, cardToken, account_number_count_of_digits,new RestCallback<PeachPaymentStatusRequest>() {
            @Override
            public void success(PeachPaymentStatusRequest obj) {


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
                    getDialogOrderPlace(orderID, status);
                } else {
                    Toast.makeText(getApplicationContext(), "Transaction Cancelled", Toast.LENGTH_LONG).show();

                    finish();
                    AddPaymentActivity.this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
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

    private void getDialogOrderPlace(String orderid, final String status) {
        final Dialog dialog = new Dialog(AddPaymentActivity.this);
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
                        Intent intent = new Intent(AddPaymentActivity.this, OrderStatusActivity.class);
                        intent.putExtra("comingfrom", "PaymentPage");
                        startActivity(intent);
                        finish();
                        toEdit = shPrefDeliverAddr.edit();
                        toEdit.putString("card_person_name", "");
                        toEdit.putString("card_person_surname", "");
                        toEdit.putString("card_number", "");
                        toEdit.putString("card_expiry_month", "");
                        toEdit.putString("card_expiry_year", "");
                        toEdit.putString("card_type", "");
                        toEdit.putString("card_type_short", "");
                        toEdit.putString("cvv_cvv", "");
                        toEdit.putString("nickname", "");
                        toEdit.putString("token", "");
                        toEdit.putString("is_from_saved_card", "No");
                        toEdit.putString("is_card_details_inputed", "Yes");
                        toEdit.commit();

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
                        Intent intent = new Intent(AddPaymentActivity.this, LandingActivity.class);
                        startActivity(intent);
                        finish();

                    }
                if (Constants.strTipPercent != null)
                    Constants.strTipPercent = "";

                AddPaymentActivity.this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

}
