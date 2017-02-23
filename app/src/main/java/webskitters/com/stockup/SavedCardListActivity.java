package webskitters.com.stockup;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.appsflyer.AppsFlyerLib;

import java.util.ArrayList;
import java.util.List;

import webskitters.com.stockup.Utils.Constants;
import webskitters.com.stockup.Utils.Utils;
import webskitters.com.stockup.adapter.SavedCardAdapter;
import webskitters.com.stockup.adapter.ViewAllRatesReviewListAdapter;
import webskitters.com.stockup.model.CardDetails;
import webskitters.com.stockup.model.CardDetailsRequest;
import webskitters.com.stockup.model.DeleteCardRequest;
import webskitters.com.stockup.model.ViewAllReviewRequest;
import webskitters.com.stockup.retrofit_call.RestCallback;
import webskitters.com.stockup.retrofit_call.RestService;

//import com.appsflyer.AppsFlyerLib;

public class SavedCardListActivity extends AppCompatActivity {

    ExpandableHeightListView lv_rates_reviews_list;
    ImageView imgBack;
    TextView tv_signin;
    private Utils utils;
    private ArrayList<String> arrRatingList;
    RestService restService;
    private ProgressDialog pw;

    private Typeface typeFaceSegoeuiReg;
    private Typeface typeFaceSegoeuiBold;
    private String customer_id="", product_id="", product_image="", product_name="";
    private SharedPreferences sharedPreferenceUser;
    List<CardDetails> arrCard = new ArrayList<>();
    SharedPreferences shPrefDeliverAddr;
    SharedPreferences.Editor toEdit;
    ListView lvCardList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_view_allsaved_card_list);
        utils=new Utils(SavedCardListActivity.this);
        restService=new RestService(this);
        typeFaceSegoeuiReg = Typeface.createFromAsset(getAssets(),
                "Roboto-Regular.ttf");
        typeFaceSegoeuiBold = Typeface.createFromAsset(getAssets(),
                "ROBOTO-BOLD_0.TTF");
        Bundle extras=getIntent().getExtras();
        if(extras!=null){
            product_image=extras.getString("productImage","");
            product_name=extras.getString("productName","");
            product_id=extras.getString("productId","");
        }

        // Track Data : Add to activities where tracking needed
        AppsFlyerLib.getInstance().sendDeepLinkData(this);
        shPrefDeliverAddr = this.getSharedPreferences(Constants.strShPrefDelAddrName, Context.MODE_PRIVATE);
        sharedPreferenceUser = getSharedPreferences(Constants.strShPrefUserPrefName, Context.MODE_PRIVATE);
        initFields();
        arrCard = new ArrayList<>();
        if(utils.isConnectionPossible()){

            getSavedCard(customer_id);
        }else{
            utils.displayAlert("Internet connection is not available, Try again later");
        }


    }
    private void getSavedCard(String customer_id) {
        final ProgressDialog pDialog=new ProgressDialog(SavedCardListActivity.this);
        pDialog.show();
        pDialog.setMessage("Loading..");
        String strCustId = sharedPreferenceUser.getString(Constants.strShPrefUserId, "");
        restService.getSaveCard(strCustId, new RestCallback<CardDetailsRequest>() {
            @Override
            public void success(CardDetailsRequest responce) {
                int reqStatus = responce.getStatus();
                int reqSuccess = responce.getSuccess();
                if (reqStatus == 200 && reqSuccess == 1) {
                    arrCard = responce.getData().getToken();


                    SavedCardAdapter searchLangAdapter = new SavedCardAdapter(SavedCardListActivity.this, arrCard);
                    //ArrayAdapter<String> adapter = new ArrayAdapter<String>(Registration1Activity.this,android.R.layout.simple_list_item_1,android.R.id.text1,values);


                    lv_rates_reviews_list.setAdapter(searchLangAdapter);
                    lv_rates_reviews_list.setExpanded(true);
                    lv_rates_reviews_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

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
                            toEdit.putString("card_type_short", "");
                            toEdit.putString("cvv_cvv", "");
                            toEdit.putString("nickname", arrCard.get(position).getCardNickname());
                            toEdit.putString("token", arrCard.get(position).getToken());
                            toEdit.putString("is_from_saved_card", "Yes");
                            toEdit.putString("is_card_details_inputed", "Yes");
                            toEdit.commit();
                            if(pDialog.isShowing())
                                pDialog.dismiss();
                            finish();
                            SavedCardListActivity.this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                        }
                    });

                } else {

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

    private void deleteCard(String token_id, final int i) {
        final ProgressDialog pDialog=new ProgressDialog(SavedCardListActivity.this);
        pDialog.show();
        pDialog.setMessage("Loading..");
        String strCustId = sharedPreferenceUser.getString(Constants.strShPrefUserId, "");
        restService.deleteCard(token_id, new RestCallback<DeleteCardRequest>() {
            @Override
            public void success(DeleteCardRequest responce) {
                int reqStatus = responce.getStatus();
                int reqSuccess = responce.getSuccess();
                if (reqStatus == 200 && reqSuccess == 1) {
                    getSavedCard(customer_id);

                    arrCard.remove(i);
                    if(arrCard.size()==0){
                        lv_rates_reviews_list.setVisibility(View.INVISIBLE);
                    }
                    SavedCardAdapter searchLangAdapter = new SavedCardAdapter(SavedCardListActivity.this, arrCard);
                    //ArrayAdapter<String> adapter = new ArrayAdapter<String>(Registration1Activity.this,android.R.layout.simple_list_item_1,android.R.id.text1,values);


                    lv_rates_reviews_list.setAdapter(searchLangAdapter);
                } else {
                    //getSavedCard(customer_id);
                    if(arrCard.size()==0){
                        lv_rates_reviews_list.setVisibility(View.INVISIBLE);
                    }
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

    private void initFields() {
        imgBack=(ImageView)findViewById(R.id.img_back);
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                SavedCardListActivity.this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });

        tv_signin=(TextView)findViewById(R.id.tv_signin);
        //tv_signin.setText("Add Shopping List");
        tv_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent intent=new Intent(ShoppingListActivity.this, LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);*/
            }
        });

        lv_rates_reviews_list=(ExpandableHeightListView)findViewById(R.id.lv_rates_reviews_list);
        lv_rates_reviews_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                getDialogConfirm(position);
                //getDialog(position);
                return true;
            }
        });

    }
    private void getDialog(final int i) {
        final Dialog dialog = new Dialog(SavedCardListActivity.this);
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
                deleteCard(arrCard.get(i).getTokenId(),  i);

            }
        });
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
    private void getDialogConfirm(final int i) {
        final Dialog dialog = new Dialog(SavedCardListActivity.this);
        Window window = dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_exit);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        //TextView header=(TextView)dialog.findViewById(R.id.header);
        TextView msg = (TextView) dialog.findViewById(R.id.msg);
        msg.setText("\nDo you want to delete this card?");
        TextView txt_date = (TextView) dialog.findViewById(R.id.txt_date);
        txt_date.setVisibility(View.GONE);

        final Button btn_yes=(Button)dialog.findViewById(R.id.btn_yes);
        btn_yes.setText("Yes");
        Button btn_no=(Button)dialog.findViewById(R.id.btn_no);
        btn_no.setText("No");

        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                deleteCard(arrCard.get(i).getTokenId(),  i);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        SavedCardListActivity.this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }


}
