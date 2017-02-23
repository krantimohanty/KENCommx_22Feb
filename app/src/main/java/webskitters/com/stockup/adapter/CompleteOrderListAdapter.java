package webskitters.com.stockup.adapter;

import android.app.Activity;
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
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import webskitters.com.stockup.CompleteOrderListActivity;
import webskitters.com.stockup.MyShoppingListActivity;
import webskitters.com.stockup.OrderHistoryActivity;
import webskitters.com.stockup.PastOrderListActivity;
import webskitters.com.stockup.PinCodeActivity;
import webskitters.com.stockup.ProductDetailsActivity;
import webskitters.com.stockup.R;
import webskitters.com.stockup.RateReviewActivity;
import webskitters.com.stockup.ReOrderActivity;
import webskitters.com.stockup.SubCategoryActivity;
import webskitters.com.stockup.Utils.Constants;
import webskitters.com.stockup.Utils.Utils;
import webskitters.com.stockup.model.RateReviewRequest;
import webskitters.com.stockup.model.RatingSubmitRequest;
import webskitters.com.stockup.model.RatingSubmitResponse;
import webskitters.com.stockup.retrofit_call.RestCallback;
import webskitters.com.stockup.retrofit_call.RestService;

/**
 * Created by Partha on 9/9/2016.
 */
public class CompleteOrderListAdapter extends BaseAdapter {
    private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater=null;
    ViewHolder holder = null;
    String strTappedView="";
    float time_star=0, prof_star=0, packing_star=0, other_star=0;
    Typeface typeFaceSegoeuiReg, typeFaceSegoeuiBold, typeFaceSegoeuiMedium;
    ProgressDialog pw;
    RestService restService;
    Utils utils;
    private SharedPreferences sharedPreferenceUser;

    public CompleteOrderListAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
        activity = a;
        data=d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        typeFaceSegoeuiReg = Typeface.createFromAsset(activity.getAssets(), "Roboto-Regular.ttf");
        typeFaceSegoeuiMedium = Typeface.createFromAsset(activity.getAssets(),"Roboto-Light.ttf");
        typeFaceSegoeuiBold = Typeface.createFromAsset(activity.getAssets(),"ROBOTO-BOLD_0.TTF");
        restService=new RestService(activity);
        utils=new Utils(activity);
        sharedPreferenceUser=activity.getSharedPreferences(Constants.strShPrefUserPrefName, Context.MODE_PRIVATE);

    }
    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class ViewHolder{
        TextView txt_order_no, txt_order_amt_currency, txt_date, txt_order_amt, txt_order;
        Button btn_rate;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        LayoutInflater mInflater = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_complete_order_list, null);
            holder = new ViewHolder();
            holder.txt_order = (TextView) convertView.findViewById(R.id.txt_ord);
            holder.txt_order_no = (TextView) convertView.findViewById(R.id.txt_order_no);
            holder.txt_date = (TextView) convertView.findViewById(R.id.txt_date);
            holder.txt_order_amt_currency = (TextView) convertView.findViewById(R.id.txt_order_amt_currency);
            holder.txt_order_amt = (TextView) convertView.findViewById(R.id.txt_order_amt);
            holder.btn_rate = (Button) convertView.findViewById(R.id.btn_rate);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.txt_order.setTypeface(typeFaceSegoeuiReg);
        holder.txt_order_no.setTypeface(typeFaceSegoeuiReg);
        holder.txt_date.setTypeface(typeFaceSegoeuiReg);
        holder.txt_order_amt.setTypeface(typeFaceSegoeuiReg);

        HashMap<String, String> mapShopList = new HashMap<String, String>();
        mapShopList = data.get(position);

        holder.txt_order_no.setText(mapShopList.get(CompleteOrderListActivity.Key_order_id));
        holder.txt_order_amt_currency.setText(mapShopList.get(CompleteOrderListActivity.Key_currency));
        holder.txt_order_amt.setText(mapShopList.get(CompleteOrderListActivity.Key_order_amount));
        holder.txt_date.setText(mapShopList.get(CompleteOrderListActivity.Key_order_date));

        holder.btn_rate.setTag(position);

        holder.btn_rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Code here
                ///////////getting position of the view in List for position references///////////
                int i=Integer.parseInt(v.getTag().toString());
                ////////Opening Service Rating dialog/////////////////////
                dialogRateService(i);
            }
        });
        return convertView;
    }

    private void dialogRateService(final int i) {
        final Dialog dialog = new Dialog(activity);
        Window window = dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_rate_us);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        int width = (int) (CompleteOrderListActivity.width * 0.75);
        int height = (int) (CompleteOrderListActivity.height * 0.90);
        dialog.getWindow().setLayout(width, height);
        //activity.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        final ImageView img_time, img_professionalism, img_packing, img_other;
        final EditText ext_comment;
        final RatingBar ratingBar;
        Button btnSubmit;

        img_time=(ImageView)dialog.findViewById(R.id.img_time);
        img_professionalism=(ImageView)dialog.findViewById(R.id.img_professional);
        img_packing=(ImageView)dialog.findViewById(R.id.img_packing);
        img_other=(ImageView)dialog.findViewById(R.id.img_other);

        ext_comment=(EditText)dialog.findViewById(R.id.ext_comment);

        ratingBar=(RatingBar)dialog.findViewById(R.id.rating_bar);

        btnSubmit=(Button)dialog.findViewById(R.id.btn_submit);

        img_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strTappedView="clickedTime";
                ratingBar.setRating(time_star);
                img_time.setBackgroundResource(R.drawable.arrival_time_active);
            }
        });
        img_professionalism.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strTappedView="clickedProf";
                ratingBar.setRating(prof_star);
                img_professionalism.setBackgroundResource(R.drawable.prof_icon_active);
            }
        });
        img_packing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strTappedView="clickedPacking";
                ratingBar.setRating(packing_star);
                img_packing.setBackgroundResource(R.drawable.packing_icon_active);
            }
        });
        img_other.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strTappedView="clickedOther";
                ratingBar.setRating(other_star);
                img_other.setBackgroundResource(R.drawable.others_icon_active);
            }
        });
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {

                if(strTappedView.equalsIgnoreCase("clickedTime")){
                    time_star=ratingBar.getRating();

                }else if(strTappedView.equalsIgnoreCase("clickedProf")){
                    prof_star=ratingBar.getRating();

                }else if(strTappedView.equalsIgnoreCase("clickedPacking")){
                    packing_star=ratingBar.getRating();

                }else if(strTappedView.equalsIgnoreCase("clickedOther")){
                    other_star=ratingBar.getRating();

                }else{
                    time_star=ratingBar.getRating();
                }
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //dialog.cancel();
                String strCustId = sharedPreferenceUser.getString(Constants.strShPrefUserId, "");
                HashMap<String, String> mapShopList = new HashMap<String, String>();
                mapShopList = data.get(i);

                String order_id=mapShopList.get(CompleteOrderListActivity.Key_order_id);
                if(strTappedView.equalsIgnoreCase("")){
                    utils.displayAlert("Provide rating on these views");
                }else if(time_star==0){
                    utils.displayAlert("Provide your rating on Arrival Time");
                }else if(prof_star==0){
                    utils.displayAlert("Provide your rating on Professionalism");
                }else if(packing_star==0){
                    utils.displayAlert("Provide your rating on Packing");
                }else if(other_star==0){
                    utils.displayAlert("Provide your rating on Others");
                }/*else if(ext_comment.getText().toString().equalsIgnoreCase("")){
                    utils.displayAlert("Provide your rating on Professionalism");
                }*/else{
                    dialog.dismiss();
                    serviceRating(strCustId, order_id, ext_comment.getText().toString().trim(),"", ""+time_star, ""+prof_star, ""+packing_star, ""+other_star);
                }

            }
        });

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void serviceRating(String customer_id, String order_id, String comment,String rating, String arrival, String professionalism, String packing, String other) {
        pw = new ProgressDialog(activity);
        pw.show();
        pw.setMessage("Loading... Please wait.");
        restService.serviceRating(customer_id, order_id, comment,rating, arrival, professionalism, packing, other, new RestCallback<RatingSubmitRequest>() {
            @Override
            public void success(RatingSubmitRequest object) {
                if(object.getStatus()==200&object.getSuccess()==1){
                    displayAlert(object.getData().getSuccessMsg());
                }else{
                    utils.displayAlert(object.getErrorMsg());
                }

                if (pw != null)
                    pw.dismiss();


            }

            @Override
            public void invalid() {

                if (pw != null)
                    pw.dismiss();

                Toast.makeText(activity, "Problem while fetching tracking list", Toast.LENGTH_LONG).show();

            }

            @Override
            public void failure() {

                if (pw != null)
                    pw.dismiss();

                Toast.makeText(activity, "Error parsing tracking list", Toast.LENGTH_LONG).show();

            }

        });
    }
    public void displayAlert(String message)
    {
        // TODO Auto-generated method stub
        //message="To proceed, sign into your account.";
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
        //alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setTitle("Stockup") ;
        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Intent intentLogin = new Intent(activity, PinCodeActivity.class);
                activity.finish();
                activity.startActivity(intentLogin);
                activity.overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
            }
        });
        TextView myMsg = new TextView(activity);
        myMsg.setText(message);
        myMsg.setPadding(20, 20, 20, 20);
        myMsg.setTextSize(16);
        myMsg.setTypeface(typeFaceSegoeuiReg);
        myMsg.setTextColor(Color.BLACK);
        myMsg.setGravity(Gravity.CENTER|Gravity.CENTER_HORIZONTAL);
        alertDialogBuilder.setView(myMsg);

        TextView title = new TextView(activity);
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

    private void getDialogCoverage() {
        final Dialog dialog = new Dialog(activity);
        Window window = dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_age);
        activity.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

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
}
