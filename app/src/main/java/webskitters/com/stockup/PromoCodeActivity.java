package webskitters.com.stockup;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.invitereferrals.invitereferrals.InviteReferralsApi;

import webskitters.com.stockup.Utils.Constants;
import webskitters.com.stockup.Utils.Utils;
import webskitters.com.stockup.adapter.MyShoppingListAdapter;
import webskitters.com.stockup.model.CouponCodeRequset;
import webskitters.com.stockup.model.GetAddressRequest;
import webskitters.com.stockup.retrofit_call.RestCallback;
import webskitters.com.stockup.retrofit_call.RestService;

public class PromoCodeActivity extends AppCompatActivity {

    ImageView img_back;
    TextView txt_promo_code;
    Button btn_share_code;
    SharedPreferences sharedPreferenceUser;
    private String customer_id="";
    private String customer_name="";
    RestService restService;
    Utils utils;
    static final int REQUEST_CODE_MY_PICK = 1;
    private SharedPreferences shPrefUserSelection;
    String store_id="";
    Long getSysTime;
    TextView txt_msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_promo_code);
        sharedPreferenceUser=this.getSharedPreferences(Constants.strShPrefUserPrefName, Context.MODE_PRIVATE);
        customer_id=sharedPreferenceUser.getString(Constants.strShPrefUserId, "");
        InviteReferralsApi.getInstance(this).initialize();
        customer_name=sharedPreferenceUser.getString(Constants.strShPrefUserFname,"");
        restService=new RestService(this);
        utils=new Utils(PromoCodeActivity.this);
        initfields();
        if(utils.isConnectionPossible()){
        getCouponCode(customer_id);}
        else{
            utils.displayAlert("Internet connection is not available. Try again later.");
        }

    }

    private void initfields() {
        img_back = (ImageView) findViewById(R.id.img_back);
        txt_promo_code = (TextView) findViewById(R.id.txt_promo_code);
        txt_promo_code.setTextIsSelectable(true);
        btn_share_code = (Button) findViewById(R.id.btn_share_code);

        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                PromoCodeActivity.this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });
        btn_share_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareUrl(customer_name);
            }
        });

        txt_msg=(TextView)findViewById(R.id.txt_msg);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*if(requestCode==REQUEST_CODE_MY_PICK&&data!=null){
            Toast.makeText(getApplicationContext(), "ResultCode"+requestCode, Toast.LENGTH_LONG).show();
        }*/

        if(requestCode==REQUEST_CODE_MY_PICK) {
            long curTime = System.currentTimeMillis();
            long mills = curTime - getSysTime;
            int intMilSec = (int) mills;
            //int sec = (int) (mills/1000);
            if (intMilSec > 3000) {
                displayAlert("");
            }
        }
    }

    public void shareUrl(String strCustName){
        //Bitmap bmpImg = screenShot(rl_main);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        //shareIntent.putExtra(Intent.EXTRA_TEXT, MyShoppingListAdapter.shopping_list_name);
        shareIntent.putExtra(Intent.EXTRA_TEXT, strCustName+" has given you a FREE Promo Code (up to R50). To claim it, use your Stockup app or sign up at Stockup.co.za & use "+txt_promo_code.getText().toString()+" coupon code at checkout.");
        //shareIntent.putExtra(Intent.EXTRA_STREAM, "");
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        this.startActivityForResult(shareIntent, REQUEST_CODE_MY_PICK);
        getSysTime = System.currentTimeMillis();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        PromoCodeActivity.this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private void getDialogCoverage() {
        final Dialog dialog = new Dialog(PromoCodeActivity.this);
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

    private void getCouponCode(String strCustId) {
        final ProgressDialog pDialog=new ProgressDialog(PromoCodeActivity.this);
        pDialog.show();
        pDialog.setMessage("Loading...");
        restService.CouponCodeRequset(strCustId, new RestCallback<CouponCodeRequset>() {

            @Override
            public void success(CouponCodeRequset responce) {
                int reqStatus = responce.getStatus();
                int reqSuccess = responce.getSuccess();
                if (reqStatus == 200 && reqSuccess == 1) {
                    String[] discount=responce.getData().getTotalDiscount().toString().split(".");
                    if(discount.length>0)
                    txt_msg.setText("Your R"+discount[0].toString().trim()+" Promo Code!");
                    txt_promo_code.setText(responce.getData().getRegistCoupon());
                } else {
                    utils.displayAlert(responce.getErrorMsg());
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
        message="Well done for sharing your code! Now you can further enjoy the benefits of using Stockup.";
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        //alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setTitle("Stockup") ;
        alertDialogBuilder.setPositiveButton("Shop On", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                shPrefUserSelection = PromoCodeActivity.this.getSharedPreferences(Constants.strShPrefUserSelection, Context.MODE_PRIVATE);
                store_id=shPrefUserSelection.getString(Constants.strShUserProductId, "");
                if(store_id.equalsIgnoreCase("")){
                    Intent intent=new Intent(PromoCodeActivity.this, PinCodeActivity.class);
                    finish();
                    startActivity(intent);
                }else{
                    Intent intent=new Intent(PromoCodeActivity.this, SubCategoryActivity.class);
                    finish();
                    startActivity(intent);
                }
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
            }
        });
        TextView myMsg = new TextView(this);
        myMsg.setText(message);
        myMsg.setPadding(20, 20, 20, 20);
        myMsg.setTextSize(16);
        //myMsg.setTypeface(typeFaceSegoeuiReg);
        myMsg.setTextColor(Color.BLACK);
        myMsg.setGravity(Gravity.CENTER|Gravity.CENTER_HORIZONTAL);
        alertDialogBuilder.setView(myMsg);

        TextView title = new TextView(this);
        // You Can Customise your Title here
        title.setText("Stockup");
        title.setVisibility(View.GONE);
        title.setBackgroundColor(Color.TRANSPARENT);
        title.setPadding(15, 20, 15, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.BLACK);
        // title.setTypeface(typeFaceSegoeuiBold);
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
}
