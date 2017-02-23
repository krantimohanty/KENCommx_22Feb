package webskitters.com.stockup;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

//import com.appsflyer.AppsFlyerLib;

import com.appsflyer.AppsFlyerLib;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import webskitters.com.stockup.Utils.Constants;
import webskitters.com.stockup.Utils.NotificationCount;
import webskitters.com.stockup.Utils.Utils;
import webskitters.com.stockup.model.NotificationCountRequest;
import webskitters.com.stockup.retrofit_call.RestCallback;
import webskitters.com.stockup.retrofit_call.RestService;

public class LandingActivity extends Activity {
    //Button btn_drinks, btn_foods, btn_day, btn_gift;
    ImageView img_drink, img_gourmets, img_day, img_gift;
            //btn_grocery;

    SharedPreferences shPrefUserSelection;
    SharedPreferences.Editor toEdit;
    private ImageView img_events;
    String strAge="";
    Format format;
    private DatePickerDialog fromDatePickerDialog;
    String strDate = "";
    int mYear=0,mMonth=0,mDay=0;
    Utils utils;
    SharedPreferences sharedPreferenceUser;
    String customer_id="";
    RestService restService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_landing);

        utils=new Utils(LandingActivity.this);
        restService=new RestService(this);
        // Track Data : Add to activities where tracking needed
        AppsFlyerLib.getInstance().sendDeepLinkData(this);

        initFields();
        sharedPreferenceUser=getSharedPreferences(Constants.strShPrefUserPrefName, Context.MODE_PRIVATE);
        customer_id=sharedPreferenceUser.getString(Constants.strShPrefUserId, "");
        if(utils.isConnectionPossible()) {
            if (!customer_id.equalsIgnoreCase("")) {
                updateNotiStat(customer_id);
            }
        }
        Calendar now = Calendar.getInstance();
        Log.e("time", now.getTime().toString());
        now.add(Calendar.MINUTE,40);
        Log.e("time", now.getTime().toString());
    }

    private void initFields() {

        shPrefUserSelection = this.getSharedPreferences(Constants.strShPrefUserSelection, Context.MODE_PRIVATE);
        strAge = shPrefUserSelection.getString(Constants.strAge, "");
        img_drink = (ImageView) findViewById(R.id.img_drink);
        img_gourmets = (ImageView) findViewById(R.id.img_gourmets);
        img_day = (ImageView) findViewById(R.id.img_day);
        img_gift = (ImageView) findViewById(R.id.img_gift);
        img_events=(ImageView)findViewById(R.id.img_events);
        img_events.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialogCoverage();
            }
        });
        img_drink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (strAge.equalsIgnoreCase("")) {
                    getDialogAge();
                } else {
                    toEdit = shPrefUserSelection.edit();
                    toEdit.putString(Constants.strShUserShopSelected, Constants.strShUserShopDrink);
                    toEdit.putString(Constants.strAge,Constants.strAgeNumber);
                    toEdit.commit();
                    Intent intentSlider = new Intent(LandingActivity.this, PinCodeActivity.class);
                    //finish();
                    startActivity(intentSlider);
                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                }
            }
        });
        img_gourmets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Put Grocery selection for landing page
                toEdit = shPrefUserSelection.edit();
                toEdit.putString(Constants.strShUserShopSelected, Constants.strShUserShopFood);
                toEdit.commit();

                Intent intentSlider = new Intent(LandingActivity.this, PinCodeActivity.class);
                //finish();
                startActivity(intentSlider);
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
            }
        });
        img_day.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Put Grocery selection for landing page
                toEdit = shPrefUserSelection.edit();
                toEdit.putString(Constants.strShUserShopSelected, Constants.strShUserShopDay);
                toEdit.commit();

                Intent intentSlider = new Intent(LandingActivity.this, PinCodeActivity.class);
                //finish();
                startActivity(intentSlider);
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
            }
        });
        img_gift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Put Grocery selection for landing page
                toEdit = shPrefUserSelection.edit();
                toEdit.putString(Constants.strShUserShopSelected, Constants.strShUserShopGift);
                toEdit.commit();
                Intent intentSlider = new Intent(LandingActivity.this, PinCodeActivity.class);
                //finish();
                startActivity(intentSlider);
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
            }
        });
        img_events.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* getDialogCoverage();*/
                toEdit = shPrefUserSelection.edit();
                toEdit.putString(Constants.strShUserShopSelected, Constants.strShUserShopEvent);
                toEdit.commit();

                Intent intentSlider = new Intent(LandingActivity.this, PinCodeActivity.class);
                //finish();
                startActivity(intentSlider);
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
            }
        });
    }


    private void getDialogCoverage() {
        final Dialog dialog = new Dialog(LandingActivity.this);
        Window window = dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
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
    private void getDialogAge() {
        final Dialog dialog = new Dialog(LandingActivity.this);
        Window window = dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_age_confirmation);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        //TextView header=(TextView)dialog.findViewById(R.id.header);
        TextView msg=(TextView)dialog.findViewById(R.id.msg);
        final TextView txt_date=(TextView)dialog.findViewById(R.id.txt_date);

        final Button btn_yes=(Button)dialog.findViewById(R.id.btn_yes);
        Button btn_no=(Button)dialog.findViewById(R.id.btn_no);
        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        format= new SimpleDateFormat("dd/MM/yyyy");
        //header.setText("Error");
        msg.setText("Please provide your date of birth to confirm you are at least 18 years of age and may view alcoholic content.");
        Calendar newDate = Calendar.getInstance();
        newDate.set(newDate.get(Calendar.YEAR), newDate.get(Calendar.MONTH), newDate.get(Calendar.DAY_OF_MONTH));
        mYear=newDate.get(Calendar.YEAR);
        mMonth=newDate.get(Calendar.MONTH);
        mDay=newDate.get(Calendar.DAY_OF_MONTH);
        strDate = new SimpleDateFormat("dd/MM/yyyy").format(newDate.getTime());
        txt_date.setHint("DD/MM/YYYY");
        //txt_date.setText(format.format(newDate.getTime()));
        txt_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDateTimeField(txt_date,btn_yes);
            }
        });


        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Put Grocery selection for landing page
                Calendar newDate = Calendar.getInstance();
                newDate.set(mYear, mMonth, mDay);

                Long dtSelected = newDate.getTimeInMillis();
                //Long dtCur = Calendar.getInstance().getTimeInMillis();

                Calendar cal = Calendar.getInstance();
                cal.set(cal.get(Calendar.YEAR)-18, cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
                Long dt18 = cal.getTimeInMillis();
                dialog.dismiss();
                if (dtSelected<=dt18){
                    // Proceed

                    toEdit = shPrefUserSelection.edit();
                    toEdit.putString(Constants.strShUserShopSelected, Constants.strShUserShopDrink);
                    toEdit.putString(Constants.strAge,Constants.strAgeNumber);
                    toEdit.commit();
                    Intent intentSlider = new Intent(LandingActivity.this, PinCodeActivity.class);
                    //finish();
                    startActivity(intentSlider);
                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                }
                else {
                    // Disable button
                    btn_yes.setVisibility(View.GONE);
                    utils.displayAlert("You are not 18 years of age or older. As a result you will not be allowed to proceed to our Drinks store.");
                }
                dialog.dismiss();


            }
        });

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
   /* private void getDialogAge() {
        final Dialog dialog = new Dialog(LandingActivity.this);
        Window window = dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_age_confirmation);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        //TextView header=(TextView)dialog.findViewById(R.id.header);
        TextView msg=(TextView)dialog.findViewById(R.id.msg);
        final TextView txt_date=(TextView)dialog.findViewById(R.id.txt_date);

        final Button btn_yes=(Button)dialog.findViewById(R.id.btn_yes);
        Button btn_no=(Button)dialog.findViewById(R.id.btn_no);
        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        format= new SimpleDateFormat("dd MMMM yyyy");
        //header.setText("Error");
        msg.setText("Please provide your date of birth to confirm you are at least 18 years of age and may view alcoholic content.");
        Calendar newDate = Calendar.getInstance();
        newDate.set(newDate.get(Calendar.YEAR), newDate.get(Calendar.MONTH), newDate.get(Calendar.DAY_OF_MONTH));
        mYear=newDate.get(Calendar.YEAR);
        mMonth=newDate.get(Calendar.MONTH);
        mDay=newDate.get(Calendar.DAY_OF_MONTH);
        strDate = new SimpleDateFormat("dd MMMM yyyy").format(newDate.getTime());
        txt_date.setText(format.format(newDate.getTime()));
        txt_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDateTimeField(txt_date,btn_yes);
            }
        });


        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Put Grocery selection for landing page
                Calendar newDate = Calendar.getInstance();
                newDate.set(mYear, mMonth, mDay);

                Long dtSelected = newDate.getTimeInMillis();
                //Long dtCur = Calendar.getInstance().getTimeInMillis();

                Calendar cal = Calendar.getInstance();
                cal.set(cal.get(Calendar.YEAR)-18, cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
                Long dt18 = cal.getTimeInMillis();
                dialog.dismiss();
                if (dtSelected<=dt18){
                    // Proceed

                    toEdit = shPrefUserSelection.edit();
                    toEdit.putString(Constants.strShUserShopSelected, Constants.strShUserShopDrink);
                    toEdit.putString(Constants.strAge,Constants.strAgeNumber);
                    toEdit.commit();
                    Intent intentSlider = new Intent(LandingActivity.this, PinCodeActivity.class);
                    //finish();
                    startActivity(intentSlider);
                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                }
                else {
                    // Disable button
                    btn_yes.setVisibility(View.GONE);
                    utils.displayAlert("You are not 18 years of age or older. As a result you will not be allowed to proceed to our Drinks store.");
                }
                dialog.dismiss();


            }
        });

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }*/


    private void setDateTimeField(final TextView txt_date,final Button btn_yes) {

        Calendar newCalendar = Calendar.getInstance();
        fromDatePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                Long dtSelected = newDate.getTimeInMillis();

                mYear=newDate.get(Calendar.YEAR);
                mMonth=newDate.get(Calendar.MONTH);
                mDay=newDate.get(Calendar.DAY_OF_MONTH);
                Calendar cal = Calendar.getInstance();
                cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
                Long dt18 = cal.getTimeInMillis();

                Calendar newDate18 = Calendar.getInstance();
                newDate18.set(18, 0, 0);
                if (dtSelected<=dt18){
                    // Proceed
                    btn_yes.setVisibility(View.VISIBLE);
                }
                else {
                    // Disable button
                    btn_yes.setVisibility(View.GONE);
                }
                strDate = new SimpleDateFormat("dd/MM/yyyy").format(newDate.getTime());
                txt_date.setText(strDate);
            }

        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        Calendar cal = Calendar.getInstance();
        //cal.set(cal.get(Calendar.YEAR) - 18, cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

        fromDatePickerDialog.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        fromDatePickerDialog.getDatePicker().setMaxDate(cal.getTimeInMillis() - 1000);
        fromDatePickerDialog.show();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();

        getDialogConfirm();
    }

    private void getDialogConfirm() {
        final Dialog dialog = new Dialog(LandingActivity.this);
        Window window = dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_exit);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        //TextView header=(TextView)dialog.findViewById(R.id.header);
        TextView msg = (TextView) dialog.findViewById(R.id.msg);
        msg.setText("Do you want to exit from StockupAPP?");
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
                finish();
                moveTaskToBack(true);
                LandingActivity.this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
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

    private void updateNotiStat(String customer_id) {
        //final ProgressDialog pDialog=new ProgressDialog(LandingActivity.this);
        //pDialog.show();
        //pDialog.setMessage("Loading..");
        restService.getNotificationCount(customer_id, new RestCallback<NotificationCountRequest>() {
            @Override
            public void success(NotificationCountRequest responce) {
                int reqStatus = responce.getStatus();
                int reqSuccess = responce.getSuccess();
                if (reqStatus == 200 && reqSuccess == 1) {
                    //displayAlertRefresh(responce.getErrorMsg());
                    //isUpdateNeed = true;
                    Constants.push_count=responce.getData().getCount();
                    // Call
                    if(Constants.push_count!=null&&!Constants.push_count.equalsIgnoreCase("")&&!Constants.push_count.isEmpty())
                        NotificationCount.setBadge(LandingActivity.this, Integer.parseInt(Constants.push_count));
                } else {
                    //utils.displayAlert(responce.getErrorMsg());
                }
                //pDialog.dismiss();
            }

            @Override
            public void invalid() {
               // pDialog.dismiss();
            }

            @Override
            public void failure() {
                //pDialog.dismiss();
            }
        });
    }
}
