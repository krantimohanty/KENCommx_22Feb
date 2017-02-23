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
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

//import com.appsflyer.AppsFlyerLib;

import com.appsflyer.AFInAppEventParameterName;
import com.appsflyer.AppsFlyerLib;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import webskitters.com.stockup.Utils.Constants;
import webskitters.com.stockup.Utils.DialogType;
import webskitters.com.stockup.Utils.NotificationCount;
import webskitters.com.stockup.Utils.Utils;
import webskitters.com.stockup.adapter.DrinksCatAdapter;
import webskitters.com.stockup.adapter.MenuListAdapter;
import webskitters.com.stockup.dbhelper.AddToCartTable;
import webskitters.com.stockup.dialog.ConfirmationDialog;
import webskitters.com.stockup.model.CartTotalRequest;
import webskitters.com.stockup.model.LogOutRequest;
import webskitters.com.stockup.model.MainCategoryRequest;
import webskitters.com.stockup.model.NotificationCountRequest;
import webskitters.com.stockup.model.SearchResultRequest;
import webskitters.com.stockup.model.SlideMenuOption;
import webskitters.com.stockup.retrofit_call.RestCallback;
import webskitters.com.stockup.retrofit_call.RestService;
import webskitters.com.stockup.webview.StockupWebViewActivity;

public class DrinkCategoriesActivity extends AppCompatActivity {

    GridView gridview;

    //////////////////NavView////////////////////
    MenuListAdapter listAdapter;
    ExpandableListView expListView;
    DrawerLayout drawer;
    private Toolbar toolbar;
    private ImageView img_profile;
    private TextView txt_name, txt_email;
    public static String strCatName="";
    private ImageView btn_nav;
    SharedPreferences shPrefUserSelection;
    SharedPreferences.Editor toEdit;
    String strUserSelectionShopType = "";
    private Typeface typeFaceSegoeuiReg;
    private TextView tv_what_do_you_want;
    private String store_id="", address="";
    private ProgressDialog pDialog;
    RestService restService;
    Utils utils;

    private SharedPreferences shPrefDeliverAddr;
    SharedPreferences.Editor toEditAddr;
    SharedPreferences sharedPreferenceUser;
    SharedPreferences.Editor sharedPrefEditior;

    private String customer_id="";
    private LinearLayout img_landing_icon;
    ImageView img_search_icon;
    private ArrayList<String> arrSearchResultItemID;
    private ArrayList<String> arrSearchResultItemName;
    SharedPreferences.Editor toEditUser;

    private TextView txt_cart_price, txtSeeAll;
    AddToCartTable mAddToCartTable;
    ArrayList<HashMap<String, String>> data=new ArrayList<HashMap<String, String>>();
    private NumberFormat nf;
    int qtycount=0;
    private double tota1_price=0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_categories_drink);
        utils=new Utils(DrinkCategoriesActivity.this);
        restService=new RestService(this);
        mAddToCartTable=new AddToCartTable(this);
        data= mAddToCartTable.getAll();
        nf= NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);
        nf.setRoundingMode(RoundingMode.HALF_UP);
        typeFaceSegoeuiReg = Typeface.createFromAsset(getAssets(), "Roboto-Regular.ttf");

        // Track Data : Add to activities where tracking needed
        AppsFlyerLib.getInstance().sendDeepLinkData(this);

        initFields();

        if(utils.isConnectionPossible()){
            getSubCateoryList(address, "10");
            if(customer_id!=null){
                updateNotiStat(customer_id);
            }

        }else
        {
            utils.displayAlert("Internet connection is not available. Try again later.");
        }


        setDrawer();
        setNavigation();
        setDrawerMenu();
    }

    private void getSubCateoryList(String address, String store_id) {

        pDialog=new ProgressDialog(this);
        pDialog.show();
        pDialog.setMessage("Loading... Please wait");
        restService.mainCategoryList(address, store_id, new RestCallback<MainCategoryRequest>() {
            @Override
            public void success(MainCategoryRequest object) {

                object.getData().getSubCategoryList();
                gridview.setAdapter(new DrinksCatAdapter(DrinkCategoriesActivity.this, object.getData().getSubCategoryList()));

                if (pDialog != null)
                    pDialog.dismiss();
            }

            @Override
            public void invalid() {

                if (pDialog != null)
                    pDialog.dismiss();

                Toast.makeText(DrinkCategoriesActivity.this, "Problem while fetching category list", Toast.LENGTH_LONG).show();
            }

            @Override
            public void failure() {

                if (pDialog != null)
                    pDialog.dismiss();

                Toast.makeText(DrinkCategoriesActivity.this, "Connection Timeout. Try again.", Toast.LENGTH_LONG).show();

            }
        });

    }

    private void initFields(){
        shPrefUserSelection = this.getSharedPreferences(Constants.strShPrefUserSelection, Context.MODE_PRIVATE);
        strUserSelectionShopType = shPrefUserSelection.getString(Constants.strShUserShopSelected, "");
        sharedPreferenceUser=getSharedPreferences(Constants.strShPrefUserPrefName, Context.MODE_PRIVATE);

        customer_id=sharedPreferenceUser.getString(Constants.strShPrefUserId, "");
        ////////////////////////////////////////////////////////////////
        shPrefDeliverAddr = this.getSharedPreferences(Constants.strShPrefDelAddrName, Context.MODE_PRIVATE);
        toEditAddr=shPrefDeliverAddr.edit();

        img_search_icon=(ImageView)findViewById(R.id.img_search_icon);
        img_search_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getDialogCoverage();
                getDialogSearch();
            }
        });

        tv_what_do_you_want=(TextView)findViewById(R.id.tv_what_do_you_want);
        tv_what_do_you_want.setTypeface(typeFaceSegoeuiReg);



        img_landing_icon=(LinearLayout)findViewById(R.id.lin_landing);
        img_landing_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DrinkCategoriesActivity.this, LandingActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);

            }
        });
        btn_nav=(ImageView) findViewById(R.id.btn_nav);
        btn_nav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(Gravity.START);
            }
        });
        txtSeeAll=(TextView) findViewById(R.id.txt_see_all_products);
        txtSeeAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toEdit = shPrefUserSelection.edit();
                toEdit.putString(Constants.strShUserProductId, "10");
                toEdit.putString(Constants.strShUserStoreId, "10");
                toEdit.putString(Constants.strShStoreID, "yes");
                toEdit.commit();
                Intent intent = new Intent(DrinkCategoriesActivity.this, SubCategoryActivity.class);

                startActivity(intent);
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
            }
        });


        txt_cart_price=(TextView)findViewById(R.id.txt_cart_total);
        txt_cart_price.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DrinkCategoriesActivity.this, AddToCartListAllItemsActivity.class);
                intent.putExtra("context_act1", "webskitters.com.stockup.DrinkCategoriesActivity");
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
            }
        });



        String callFromMap = shPrefDeliverAddr.getString(Constants.strShPrefDelCallFrom, "No");
        if (callFromMap.equalsIgnoreCase("Yes")/*&&forshipingbiling.equalsIgnoreCase("shiping")*/){
            String strLat = shPrefDeliverAddr.getString(Constants.strShPrefDelLat,"");
            String strLong = shPrefDeliverAddr.getString(Constants.strShPrefDelLong,"");
            address = shPrefDeliverAddr.getString(Constants.strShPrefDelAddr,"");
        }

        gridview = (GridView) findViewById(R.id.gridview);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
               /* Toast.makeText(DrinkCategoriesActivity.this, "" + position,
                        Toast.LENGTH_SHORT).show();*/
                //getDialogCoverage();
                Intent intent = new Intent(DrinkCategoriesActivity.this, SubCategoryActivity.class);
                intent.putExtra("cat_type", "Drinks");
                toEdit = shPrefUserSelection.edit();
                toEdit.putString(Constants.strShUserStoreId, "10");
                toEdit.commit();
                startActivity(intent);
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
            }
        });
        if(!customer_id.equalsIgnoreCase("")||!customer_id.isEmpty()){
            getCartTotal(customer_id);
        }else{
            tota1_price=0.0;
            qtycount=0;
            for(int i=0; i<data.size(); i++){
                String single_price=data.get(i).get("price").toString();
                if(single_price.startsWith("R")){
                    single_price= single_price.substring(1, single_price.length());
                }
                if(single_price!=null ||!single_price.equalsIgnoreCase("")){
                    double a=Double.parseDouble(single_price);
                    a=a*Integer.parseInt(data.get(i).get("qty"));
                    tota1_price=tota1_price+a;
                    qtycount=qtycount+Integer.parseInt(data.get(i).get("qty"));
                }
                txt_cart_price.setText("R" + nf.format(tota1_price));
            }
        }
    }
    private void getCartTotal(String customer_id) {

        restService.getCartTotal(customer_id, new RestCallback<CartTotalRequest>() {
            @Override
            public void success(CartTotalRequest object) {


                if(object.getStatus()==200&&object.getSuccess()==1){

                    txt_cart_price.setText(object.getData().getCurrency()+object.getData().getTotalPrice());
                }
            }

            @Override
            public void invalid() {

               // Toast.makeText(DrinkCategoriesActivity.this, "Problem while fetching tracking list", Toast.LENGTH_LONG).show();

            }

            @Override
            public void failure() {

               // Toast.makeText(DrinkCategoriesActivity.this, "Error parsing tracking list", Toast.LENGTH_LONG).show();

            }
        });

    }
    private void getDialogSearch() {
        final Dialog dialog = new Dialog(DrinkCategoriesActivity.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_auto_search);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ImageView img_search=(ImageView)dialog.findViewById(R.id.img_search);

        ImageView img_back=(ImageView)dialog.findViewById(R.id.img_back_search);
        final ListView lv=(ListView)dialog.findViewById(R.id.listview);
        final EditText et_search = (EditText) dialog.findViewById(R.id.et_search);
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard(DrinkCategoriesActivity.this);
                if(dialog.isShowing())
                    dialog.dismiss();
            }
        });
        img_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openSOftKeyboard(DrinkCategoriesActivity.this, et_search);
            }
        });
        ImageView img_clear = (ImageView) dialog.findViewById(R.id.img_clear);
        img_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_search.setText("");
            }
        });

        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {


            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


            }

            @Override
            public void afterTextChanged(Editable s) {
                String strSearch = et_search.getText().toString().trim();
                if (strSearch.length() > 2) {
                    getAutoSearchKey(strSearch, dialog, lv, false);
                }else{
                    getAutoSearchKey("", dialog, lv, false);
                }
            }
        });
        et_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    //performSearch();
                    // Search button from keyboard clicked
                    String strSearch = et_search.getText().toString().trim();
                    getAutoSearchKey(strSearch, dialog, lv, true);
                    return true;
                }
                return false;
            }
        });
        dialog.show();
    }

    /////////////////////Hiding soft keyboard/////////////
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    public static void openSOftKeyboard(Activity activity, EditText et_search){

        InputMethodManager inputMethodManager =
                (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(),
                InputMethodManager.SHOW_FORCED, 0);
        et_search.setFocusable(true);
        et_search.requestFocus();
    }
    private void getAutoSearchKey(final String strSearch, final Dialog dialog, final ListView lv, final boolean showDialog) {

        String strCatType = "";

        customer_id=sharedPreferenceUser.getString(Constants.strShPrefUserId, "");



        restService.getAutoSearch(strSearch, "10", new RestCallback<SearchResultRequest>() {

            @Override
            public void success(SearchResultRequest responce) {

                int reqStatus = responce.getStatus();
                int reqSuccess = responce.getSuccess();
                if (reqStatus == 200 && reqSuccess == 1) {

                    Map<String, Object> eventValue = new HashMap<String, Object>();
                    eventValue.put(AFInAppEventParameterName.SEARCH_STRING, strSearch);
                    eventValue.put(AFInAppEventParameterName.SUCCESS, "af_success");
                    utils.trackEvent(DrinkCategoriesActivity.this.getApplicationContext(), "af_search", eventValue);

                    int size = responce.getData().getProductData().size();
                    arrSearchResultItemID = new ArrayList<String>();
                    arrSearchResultItemName = new ArrayList<String>();
                    for (int i = 0; i < size; i++) {
                        arrSearchResultItemID.add(responce.getData().getProductData().get(i).getProductId().toString().trim());
                        arrSearchResultItemName.add(responce.getData().getProductData().get(i).getProductName().toString().trim());
                    }

                    List<HashMap<String, String>> aList = new ArrayList<HashMap<String, String>>();
                    for (int i = 0; i < arrSearchResultItemName.size(); i++) {
                        HashMap<String, String> hm = new HashMap<String, String>();
                        hm.put("txt", arrSearchResultItemName.get(i));
                        aList.add(hm);
                    }
                    String[] from = {"txt"};
                    int[] to = {R.id.txt};
                    SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), aList, R.layout.autocomplete_layout, from, to);

                    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {

                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),InputMethodManager.RESULT_UNCHANGED_SHOWN);

                            HashMap<String, String> hm = (HashMap<String, String>) arg0.getAdapter().getItem(position);
                            String strNameLocation = hm.get("txt");
                            String strSearchKey = arrSearchResultItemName.get(position);

                            customer_id=sharedPreferenceUser.getString(Constants.strShPrefUserId, "");

                            toEditUser= sharedPreferenceUser.edit();
                            toEditUser.putString("ProductId", arrSearchResultItemID.get(position));

                            toEditUser.commit();
                            toEdit = shPrefUserSelection.edit();
                            toEdit.putString(Constants.strShUserProductId, "10");
                            toEdit.putString(Constants.strShStoreID, "yes");
                            toEdit.commit();
                            //dialog.dismiss();
                            Intent intent = new Intent(DrinkCategoriesActivity.this, ProductDetailsActivity.class);
                            finish();
                            startActivity(intent);

                        }
                    };
                    lv.setOnItemClickListener(itemClickListener);
                    lv.setAdapter(adapter);
                    // lv.setThreshold(1);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                            InputMethodManager.RESULT_UNCHANGED_SHOWN);
                } else {
                    if(showDialog)
                    utils.displayAlert("No Result Found with Search Term "+strSearch);
                    lv.setAdapter(null);


                }
            }

            @Override
            public void invalid() {
                //Toast.makeText(DrinkCategoriesActivity.this, "Error parsing tracking list", Toast.LENGTH_LONG).show();
            }

            @Override
            public void failure() {
               // Toast.makeText(DrinkCategoriesActivity.this, "Error parsing tracking list", Toast.LENGTH_LONG).show();
            }
        });
    }
    private void getDialogCoverage() {
        final Dialog dialog = new Dialog(DrinkCategoriesActivity.this);
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

    private void setDrawer() {
        /*toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, R.drawable.inner_logo, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
    }


    private void setNavigation() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        img_profile = (ImageView) findViewById(R.id.img_profile);
        txt_name = (TextView) findViewById(R.id.txt_name);
        txt_email = (TextView) findViewById(R.id.txt_email);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);

            } else {
            toEdit=shPrefUserSelection.edit();
            toEdit.putString(Constants.strShUserStoreID,"");
            toEdit.commit();
            Intent intent=new Intent(DrinkCategoriesActivity.this, PinCodeActivity.class);
            startActivity(intent);
            finish();
            DrinkCategoriesActivity.this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }

    }
    //parse data from asset
    private ArrayList<SlideMenuOption> prepareListData() {
        ArrayList<SlideMenuOption> parentSlideMenuOptions = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(getdata());
            JSONArray jsonArray = jsonObject.optJSONArray("parentMenu");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject1 = jsonArray.optJSONObject(i);
                SlideMenuOption slideMenuOption = new SlideMenuOption();
                slideMenuOption.setId(jsonObject1.optInt("menuId"));
                slideMenuOption.setName(jsonObject1.optString("menuName"));


                JSONArray jsonArraychild = jsonObject1.optJSONArray("menuoption");
                ArrayList<SlideMenuOption> childSlideMenuOptions = new ArrayList<>();
                if (jsonArraychild != null) {
                    for (int j = 0; j < jsonArraychild.length(); j++) {
                        JSONObject jsonObject2 = jsonArraychild.optJSONObject(j);
                        SlideMenuOption slideMenuOption1 = new SlideMenuOption();
                        slideMenuOption1.setId(jsonObject2.optInt("menuId"));
                        slideMenuOption1.setName(jsonObject2.optString("menuName"));
                        slideMenuOption1.setImage(jsonObject2.optInt("menuImage"));
                        /*if(strCatName.equalsIgnoreCase(jsonObject2.optString("menuName").toString())){

                        }else{*/
                        childSlideMenuOptions.add(slideMenuOption1);
                        //}

                    }
                }
                slideMenuOption.setSlideMenuOptionsChild(childSlideMenuOptions);
                parentSlideMenuOptions.add(slideMenuOption);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return parentSlideMenuOptions;
    }

    // Reading json file from assets folder
    private String getdata() {
        StringBuffer sb = new StringBuffer();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(getAssets().open(
                    "menuoptionjson.json")));
            String temp;
            while ((temp = br.readLine()) != null)
                sb.append(temp);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close(); // stop reading
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    private void setDrawerMenu() {

        expListView = (ExpandableListView) findViewById(R.id.menu_exp_list);
        final ArrayList<SlideMenuOption> slideMenuOptions = prepareListData();
        View footerView = ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.menu_footer, null, false);
        expListView.addFooterView(footerView);
        listAdapter = new MenuListAdapter(DrinkCategoriesActivity.this, slideMenuOptions);
        expListView.setAdapter(listAdapter);
        //expListView.expandGroup(0);
        // Listview Group click listener
        expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

                optionSelefctedToday(slideMenuOptions.get(groupPosition).getId());
                return false;
            }
        });
        // Listview on child click listener
        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                optionSelefctedToday(slideMenuOptions.get(groupPosition).getSlideMenuOptionsChild().get(childPosition).getId());
                return false;
            }
        });
        footerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeDrawer(0);
                ConfirmationDialog confirmationDialog = new ConfirmationDialog(DrinkCategoriesActivity.this, DialogType.LOGOUT, new ConfirmationDialog.Confiramtion() {
                    @Override
                    public void accept() {
                        //logOut();
                    }
                });
                if (!confirmationDialog.isShowing())
                    confirmationDialog.show();
            }
        });
    }

    private void optionSelefcted(int id) {

        switch (id){
            case 3:
                Intent intentAddToCart=new Intent(DrinkCategoriesActivity.this, AddToCartListAllItemsActivity.class);
                intentAddToCart.putExtra("context_act1", "webskitters.com.stockup.DrinkCategoriesActivity");
                startActivity(intentAddToCart);
                finish();
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                break;
            case 4:
                if(!customer_id.equalsIgnoreCase("")){
                    //getProductDetails(customer_id);
                    if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
                        drawer.closeDrawer(GravityCompat.START);
                    }
                    doLogOut();
                }
                else{
                    Intent intentSign=new Intent(DrinkCategoriesActivity.this, LoginActivity.class);
                    intentSign.putExtra("context_act1", "webskitters.com.stockup.DrinkCategoriesActivity");
                    startActivity(intentSign);
                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                } break;
            case 5:
                Intent intentPromoCode = new Intent(DrinkCategoriesActivity.this, PromoCodeActivity.class);
                startActivity(intentPromoCode);
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                break;
            case 6:
                Intent urlExpVid = new Intent(DrinkCategoriesActivity.this, StockupWebViewActivity.class);
                urlExpVid.putExtra("header","Why Use Stockup");
                urlExpVid.putExtra("url","https://www.youtube.com/watch?v=VWlidmiTluo&feature=youtu.be");
                startActivity(urlExpVid);
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                break;
            case 8:
                Constants.strCatName="Drinks";
                if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);}
                toEdit = shPrefUserSelection.edit();
                toEdit.putString(Constants.strShUserShopSelected, Constants.strShUserShopDay);
                toEdit.putString(Constants.strShUserStoreID, "10");
                toEdit.commit();


                break;
            case 9:

                Constants.strCatName="Gourmet Treats";
                if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);}
                toEdit = shPrefUserSelection.edit();
                toEdit.putString(Constants.strShUserShopSelected, Constants.strShUserShopFood);
                toEdit.putString(Constants.strShUserStoreID, "11");
                toEdit.commit();
                Intent intentFoods=new Intent(DrinkCategoriesActivity.this, FoodCategoriesActivity.class);
                startActivity(intentFoods);
                finish();
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                break;
            case 10:

                Constants.strCatName="Day-To-Day";
                if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                }
                toEdit = shPrefUserSelection.edit();
                toEdit.putString(Constants.strShUserShopSelected, Constants.strShUserShopDay);
                toEdit.putString(Constants.strShUserStoreID, "12");
                toEdit.commit();
                Intent intentDay=new Intent(DrinkCategoriesActivity.this, Day2DayCategoriesActivity.class);
                startActivity(intentDay);
                finish();
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                break;
            case 11:

                Constants.strCatName="Gifts";
                if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                }
                toEdit = shPrefUserSelection.edit();
                toEdit.putString(Constants.strShUserShopSelected, Constants.strShUserShopGift);
                toEdit.putString(Constants.strShUserStoreID, "13");
                toEdit.commit();
                Intent intentGifts=new Intent(DrinkCategoriesActivity.this, GiftCategoriesActivity.class);
                startActivity(intentGifts);
                finish();
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);

                break;
            case 12:

                Constants.strCatName="Events & Services";
                if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                }
                toEdit = shPrefUserSelection.edit();
                toEdit.putString(Constants.strShUserShopSelected, Constants.strShUserShopEvent);
                toEdit.putString(Constants.strShUserStoreID, "14");
                toEdit.commit();
                Intent intentEvents=new Intent(DrinkCategoriesActivity.this, EventCategoriesActivity.class);
                startActivity(intentEvents);
                finish();
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                break;
            case 13:
                sharedPreferenceUser=getSharedPreferences(Constants.strShPrefUserPrefName, Context.MODE_PRIVATE);
                customer_id=sharedPreferenceUser.getString(Constants.strShPrefUserId, "");
                if (customer_id.equalsIgnoreCase("")){
                    displayAlert("To proceed, kindly sign into your account");
                }
                else {
                    Intent intentMyDetails = new Intent(DrinkCategoriesActivity.this, MyDetailsActivity.class);
                    startActivity(intentMyDetails);
                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                }
                break;
            case 14:
                getDialogCoverage();
                break;
            case 15:
                sharedPreferenceUser=getSharedPreferences(Constants.strShPrefUserPrefName, Context.MODE_PRIVATE);
                customer_id=sharedPreferenceUser.getString(Constants.strShPrefUserId, "");
                // getDialogCoverage();
                if (customer_id.equalsIgnoreCase("")){
                    displayAlert("To proceed, kindly sign into your account");
                }
                else {
                    Intent intentMyAddress = new Intent(DrinkCategoriesActivity.this, MyAddressActivity.class);
                    startActivity(intentMyAddress);
                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                }
                break;
            case 16:
                sharedPreferenceUser=getSharedPreferences(Constants.strShPrefUserPrefName, Context.MODE_PRIVATE);
                customer_id=sharedPreferenceUser.getString(Constants.strShPrefUserId, "");
                if (customer_id.equalsIgnoreCase("")){
                    displayAlert("To proceed, kindly sign into your account");
                }
                else {
                    Intent intentMyWishList = new Intent(DrinkCategoriesActivity.this, MyWishListActivity.class);
                    startActivity(intentMyWishList);
                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                }
                break;
            case 17:
                sharedPreferenceUser=getSharedPreferences(Constants.strShPrefUserPrefName, Context.MODE_PRIVATE);
                customer_id=sharedPreferenceUser.getString(Constants.strShPrefUserId, "");
                if (customer_id.equalsIgnoreCase("")){
                    displayAlert("To proceed, kindly sign into your account");
                }
                else {
                    Intent intentShoppingList = new Intent(DrinkCategoriesActivity.this, MyShoppingListActivity.class);
                    startActivity(intentShoppingList);
                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                }
                break;
            case 18:
                sharedPreferenceUser=getSharedPreferences(Constants.strShPrefUserPrefName, Context.MODE_PRIVATE);
                customer_id=sharedPreferenceUser.getString(Constants.strShPrefUserId, "");
                if (customer_id.equalsIgnoreCase("")){
                    displayAlert("To proceed, kindly sign into your account");
                }
                else {
                    Intent intentPastOrders = new Intent(DrinkCategoriesActivity.this, PastOrderListActivity.class);
                    startActivity(intentPastOrders);
                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                }
                break;
            case 19:
                getDialogCoverage();

                break;
            case 20:
                //getDialogCoverage();


                Intent intentServe = new Intent(DrinkCategoriesActivity.this, StockupWebViewActivity.class);
                intentServe.putExtra("header","Where We Serve");
                intentServe.putExtra("url","http://hireipho.nextmp.net/stockup/where-serve");
                startActivity(intentServe);
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                break;
            case 21:
                //getDialogCoverage();


                Intent intentFaq = new Intent(DrinkCategoriesActivity.this, StockupWebViewActivity.class);
                intentFaq.putExtra("header","FAQs");
                intentFaq.putExtra("url","http://hireipho.nextmp.net/stockup/faqs");
                startActivity(intentFaq);
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                break;
            case 22:
                //getDialogCoverage();


                Intent urlPP = new Intent(DrinkCategoriesActivity.this, StockupWebViewActivity.class);
                urlPP.putExtra("header","Privacy Policy");
                urlPP.putExtra("url","http://hireipho.nextmp.net/stockup/privacy_policy");
                startActivity(urlPP);
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                break;
            case 23:
                //getDialogCoverage();


                Intent urlTandC = new Intent(DrinkCategoriesActivity.this, StockupWebViewActivity.class);
                urlTandC.putExtra("header","Terms and Conditions");
                urlTandC.putExtra("url", "http://hireipho.nextmp.net/stockup/terms-condition");
                startActivity(urlTandC);
                break;
        }
    }

    private void optionSelefctedToday(int id) {

        switch (id){


            case 4:
                sharedPreferenceUser = getSharedPreferences(Constants.strShPrefUserPrefName, Context.MODE_PRIVATE);
                customer_id = sharedPreferenceUser.getString(Constants.strShPrefUserId, "");
                if (customer_id.equalsIgnoreCase("")) {
                    //utils.displayAlert("Please SignIn to continue..");
                    displayAlert("To proceed, kindly sign into your account");
                }else {
                    Intent intentNot = new Intent(DrinkCategoriesActivity.this, NotificationActivity.class);
                    intentNot.putExtra("context_act1", "webskitters.com.stockup.DrinkCategoriesActivity");
                    finish();
                    startActivity(intentNot);
                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                }
                break;

            case 5:
                Intent intent = new Intent(DrinkCategoriesActivity.this, AddToCartListAllItemsActivity.class);
                intent.putExtra("context_act1", "webskitters.com.stockup.DrinkCategoriesActivity");
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);


                break;

            case 6:
                sharedPreferenceUser = getSharedPreferences(Constants.strShPrefUserPrefName, Context.MODE_PRIVATE);
                customer_id = sharedPreferenceUser.getString(Constants.strShPrefUserId, "");
                if (customer_id.equalsIgnoreCase("")) {
                    //utils.displayAlert("Please SignIn to continue..");
                    displayAlert("To proceed, kindly sign into your account");
                }else {
                    Intent intentPromoCode = new Intent(DrinkCategoriesActivity.this, PromoCodeActivity.class);
                    startActivity(intentPromoCode);
                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                }

                break;

            case 7:
                Intent urlExpVid = new Intent(DrinkCategoriesActivity.this, StockupWebViewActivity.class);
                urlExpVid.putExtra("header","Why Use Stockup");
                urlExpVid.putExtra("url","https://www.youtube.com/watch?v=VWlidmiTluo&feature=youtu.be");
                startActivity(urlExpVid);
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                break;

            case 9:

                if(!customer_id.equalsIgnoreCase("")){
                    //getProductDetails(customer_id);
                    if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
                        drawer.closeDrawer(GravityCompat.START);
                    }
                    doLogOut();
                }
                else{
                    Intent intentSign=new Intent(DrinkCategoriesActivity.this, LoginActivity.class);
                    intentSign.putExtra("context_act1", "webskitters.com.stockup.DrinkCategoriesActivity");
                    startActivity(intentSign);
                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                }
                break;
            case 10:
                Constants.strCatName="Drinks";
                if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);}
                toEdit = shPrefUserSelection.edit();
                toEdit.putString(Constants.strShUserShopSelected, Constants.strShUserShopDay);
                toEdit.putString(Constants.strShUserStoreID, "10");
                toEdit.putString(Constants.strShUserStoreId, "10");
                toEdit.commit();

                break;
            case 11:

                Constants.strCatName="Gourmet Treats";
                if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);}
                toEdit = shPrefUserSelection.edit();
                toEdit.putString(Constants.strShUserShopSelected, Constants.strShUserShopFood);
                toEdit.putString(Constants.strShUserStoreID, "11");
                toEdit.putString(Constants.strShUserStoreId, "11");
                toEdit.commit();
                Intent intentFoods=new Intent(DrinkCategoriesActivity.this, FoodCategoriesActivity.class);
                startActivity(intentFoods);
                finish();
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                break;
            case 12:

                Constants.strCatName="Day-To-Day";
                if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                }
                toEdit = shPrefUserSelection.edit();
                toEdit.putString(Constants.strShUserShopSelected, Constants.strShUserShopDay);
                toEdit.putString(Constants.strShUserStoreID, "12");
                toEdit.putString(Constants.strShUserStoreId, "12");
                toEdit.commit();
                Intent intentDay=new Intent(DrinkCategoriesActivity.this, Day2DayCategoriesActivity.class);
                startActivity(intentDay);
                finish();
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                break;
            case 13:

                Constants.strCatName="Gifts";
                if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                }
                toEdit = shPrefUserSelection.edit();
                toEdit.putString(Constants.strShUserShopSelected, Constants.strShUserShopGift);
                toEdit.putString(Constants.strShUserStoreID, "13");
                toEdit.putString(Constants.strShUserStoreId, "13");
                toEdit.commit();
                Intent intentGifts=new Intent(DrinkCategoriesActivity.this, GiftCategoriesActivity.class);
                startActivity(intentGifts);
                finish();
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);

                break;
            case 14:

                //getDialogCoverage();
                Constants.strCatName="Events & Services";
                if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                }
                toEdit = shPrefUserSelection.edit();
                toEdit.putString(Constants.strShUserShopSelected, Constants.strShUserShopEvent);
                toEdit.putString(Constants.strShUserStoreID, "14");
                toEdit.putString(Constants.strShUserStoreId, "14");
                toEdit.commit();
                Intent intentEvents=new Intent(DrinkCategoriesActivity.this, EventCategoriesActivity.class);
                startActivity(intentEvents);
                finish();
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                break;
            case 15:
                sharedPreferenceUser=getSharedPreferences(Constants.strShPrefUserPrefName, Context.MODE_PRIVATE);
                customer_id=sharedPreferenceUser.getString(Constants.strShPrefUserId, "");
                if (customer_id.equalsIgnoreCase("")){
                    //utils.displayAlert("Please SignIn to continue..");
                    displayAlert("To proceed, kindly sign into your account");
                }
                else {
                    Intent intentMyDetails = new Intent(DrinkCategoriesActivity.this, MyDetailsActivity.class);
                    startActivity(intentMyDetails);
                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                }
                break;
            case 16:
                //getDialogCoverage();
                sharedPreferenceUser=getSharedPreferences(Constants.strShPrefUserPrefName, Context.MODE_PRIVATE);
                customer_id=sharedPreferenceUser.getString(Constants.strShPrefUserId, "");
                if (customer_id.equalsIgnoreCase("")){
                    displayAlert("To proceed, kindly sign into your account");
                }
                else {
                    Intent intentMyAddress = new Intent(DrinkCategoriesActivity.this, MyAddressActivity.class);
                    startActivity(intentMyAddress);
                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                }

                break;
            case 17:
                sharedPreferenceUser=getSharedPreferences(Constants.strShPrefUserPrefName, Context.MODE_PRIVATE);
                customer_id=sharedPreferenceUser.getString(Constants.strShPrefUserId, "");
                if (customer_id.equalsIgnoreCase("")){
                    displayAlert("To proceed, kindly sign into your account");
                }
                else {
                    Intent intentMyWishList = new Intent(DrinkCategoriesActivity.this, MyWishListActivity.class);
                    startActivity(intentMyWishList);
                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                }
                break;
            case 18:
                sharedPreferenceUser=getSharedPreferences(Constants.strShPrefUserPrefName, Context.MODE_PRIVATE);
                customer_id=sharedPreferenceUser.getString(Constants.strShPrefUserId, "");
                if (customer_id.equalsIgnoreCase("")){
                    displayAlert("To proceed, kindly sign into your account");
                }
                else {
                    Intent intentShoppingList = new Intent(DrinkCategoriesActivity.this, MyShoppingListActivity.class);
                    startActivity(intentShoppingList);
                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                }
                break;
            case 19:
                sharedPreferenceUser=getSharedPreferences(Constants.strShPrefUserPrefName, Context.MODE_PRIVATE);
                customer_id=sharedPreferenceUser.getString(Constants.strShPrefUserId, "");
                if (customer_id.equalsIgnoreCase("")){
                    displayAlert("To proceed, kindly sign into your account");
                }
                else {
                    Intent intentPastOrders = new Intent(DrinkCategoriesActivity.this, PastOrderListActivity.class);
                    startActivity(intentPastOrders);
                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                }
                break;
            case 20:
                getDialogCoverage();
                /*Intent intentTrackOrders=new Intent(PinCodeActivity.this, TrackOrderListActivity.class);
                startActivity(intentTrackOrders);
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);*/
                break;
            case 21:
                //getDialogCoverage();
                /*String urlServe = "http://hireipho.nextmp.net/stockup/where-serve";
                Intent intentServe = new Intent(Intent.ACTION_VIEW);
                intentServe.setData(Uri.parse(urlServe));
                startActivity(intentServe);*/

                Intent intentServe = new Intent(DrinkCategoriesActivity.this, StockupWebViewActivity.class);
                intentServe.putExtra("header","Where We Serve");
                intentServe.putExtra("url",Constants.urlWhereWeServe);
                startActivity(intentServe);
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                break;
            case 22:
                //getDialogCoverage();
                /*String urlFaq = "http://hireipho.nextmp.net/stockup/faqs";
                Intent intentFaq = new Intent(Intent.ACTION_VIEW);
                intentFaq.setData(Uri.parse(urlFaq));
                startActivity(intentFaq);*/

                Intent intentFaq = new Intent(DrinkCategoriesActivity.this, StockupWebViewActivity.class);
                intentFaq.putExtra("header","FAQs");
                intentFaq.putExtra("url",Constants.urlFAQs);
                startActivity(intentFaq);
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                break;
            case 23:
                //getDialogCoverage();
                /*String urlPP = "http://hireipho.nextmp.net/stockup/terms-condition";
                Intent intentPP = new Intent(Intent.ACTION_VIEW);
                intentPP.setData(Uri.parse(urlPP));
                startActivity(intentPP);*/

                Intent urlPP = new Intent(DrinkCategoriesActivity.this, StockupWebViewActivity.class);
                urlPP.putExtra("header","Privacy Policy");
                urlPP.putExtra("url",Constants.urlPrivacyPolicy);
                startActivity(urlPP);
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                break;
            case 24:
                //getDialogCoverage();
                /*String urlTandC = "http://hireipho.nextmp.net/stockup/terms-condition";
                Intent intentTandC = new Intent(Intent.ACTION_VIEW);
                intentTandC.setData(Uri.parse(urlTandC));
                startActivity(intentTandC);*/

                Intent urlTandC = new Intent(DrinkCategoriesActivity.this, StockupWebViewActivity.class);
                urlTandC.putExtra("header","Terms and Conditions");
                urlTandC.putExtra("url", Constants.urlTermsCond);
                startActivity(urlTandC);
                break;

            case 25:
                //getDialogCoverage();
                /*String urlTandC = "http://hireipho.nextmp.net/stockup/terms-condition";
                Intent intentTandC = new Intent(Intent.ACTION_VIEW);
                intentTandC.setData(Uri.parse(urlTandC));
                startActivity(intentTandC);*/

                sharedPreferenceUser = getSharedPreferences(Constants.strShPrefUserPrefName, Context.MODE_PRIVATE);
                customer_id = sharedPreferenceUser.getString(Constants.strShPrefUserId, "");
                if (customer_id.equalsIgnoreCase("")) {
                    //utils.displayAlert("Please SignIn to continue..");
                    displayAlert("To proceed, kindly sign into your account");
                }else {
                    Intent intentMyPayments = new Intent(DrinkCategoriesActivity.this, SavedCardListActivity.class);
                    startActivity(intentMyPayments);
                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                }
                //getDialogCoverage();
                break;

            case 26:
                utils.getDialogRateUs();
                break;
            case 27:
                utils.shareUrl("");
                break;
            case 28:
                sharedPreferenceUser=getSharedPreferences(Constants.strShPrefUserPrefName, Context.MODE_PRIVATE);
                customer_id=sharedPreferenceUser.getString(Constants.strShPrefUserId, "");
                if (customer_id.equalsIgnoreCase("")){
                    displayAlert("To proceed, kindly sign into your account");
                }
                else {
                    Intent intentCompleteOrders = new Intent(DrinkCategoriesActivity.this, CompleteOrderListActivity.class);
                    startActivity(intentCompleteOrders);
                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                }
                break;
        }
    }
    private void optionSelefctedNew(int id) {

        switch (id){
            case 3:
                Intent intentAddToCart=new Intent(DrinkCategoriesActivity.this, AddToCartListAllItemsActivity.class);
                startActivity(intentAddToCart);
                finish();
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                break;
            case 4:
                if(!customer_id.equalsIgnoreCase("")){
                    //getProductDetails(customer_id);
                    if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
                        drawer.closeDrawer(GravityCompat.START);
                    }
                    doLogOut();
                }
                else{
                    Intent intentSign=new Intent(DrinkCategoriesActivity.this, LoginActivity.class);
                    intentSign.putExtra("context_act1", "webskitters.com.stockup.DrinkCategoriesActivity");
                    startActivity(intentSign);
                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                } break;

            case 6:
                Constants.strCatName="Drinks";
                if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);}
                toEdit = shPrefUserSelection.edit();
                toEdit.putString(Constants.strShUserShopSelected, Constants.strShUserShopDrink);
                toEdit.putString(Constants.strShUserStoreID, "10");
                toEdit.commit();

               /* Intent intentDay2Day=new Intent(DrinkCategoriesActivity.this, Day2DayCategoriesActivity.class);
                startActivity(intentDay2Day);
                finish();*/
                break;
            case 7:
                Constants.strCatName="Gourmet Treats";
                if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);}
                toEdit = shPrefUserSelection.edit();
                toEdit.putString(Constants.strShUserShopSelected, Constants.strShUserShopFood);
                toEdit.putString(Constants.strShUserStoreID, "11");
                toEdit.commit();
                Intent intentFoods=new Intent(DrinkCategoriesActivity.this, FoodCategoriesActivity.class);
                startActivity(intentFoods);
                finish();
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                break;
            case 8:
                Constants.strCatName="Day-To-Day";
                if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                }
                toEdit = shPrefUserSelection.edit();
                toEdit.putString(Constants.strShUserShopSelected, Constants.strShUserShopDay);
                toEdit.putString(Constants.strShUserStoreID, "12");
                toEdit.commit();
                Intent intentDay=new Intent(DrinkCategoriesActivity.this, Day2DayCategoriesActivity.class);
                startActivity(intentDay);
                finish();
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                break;
            case 9:
                Constants.strCatName="Gifts";
                if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                }
                toEdit = shPrefUserSelection.edit();
                toEdit.putString(Constants.strShUserShopSelected, Constants.strShUserShopGift);
                toEdit.putString(Constants.strShUserStoreID, "13");
                toEdit.commit();
                Intent intentGifts=new Intent(DrinkCategoriesActivity.this, GiftCategoriesActivity.class);
                startActivity(intentGifts);
                finish();
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);

                break;
            case 10:

                Constants.strCatName="Events & Services";
                if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                }
                toEdit = shPrefUserSelection.edit();
                toEdit.putString(Constants.strShUserShopSelected, Constants.strShUserShopEvent);
                toEdit.putString(Constants.strShUserStoreID, "14");
                toEdit.commit();
                Intent intentEvents=new Intent(DrinkCategoriesActivity.this, EventCategoriesActivity.class);
                startActivity(intentEvents);
                finish();
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                break;
            case 11:
                sharedPreferenceUser=getSharedPreferences(Constants.strShPrefUserPrefName, Context.MODE_PRIVATE);
                customer_id=sharedPreferenceUser.getString(Constants.strShPrefUserId, "");
                if (customer_id.equalsIgnoreCase("")){
                    displayAlert("To proceed, kindly sign into your account");
                }
                else {
                    Intent intentMyDetails = new Intent(DrinkCategoriesActivity.this, MyDetailsActivity.class);
                    startActivity(intentMyDetails);
                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                }
                break;
            case 12:
                getDialogCoverage();
                break;
            case 13:
                sharedPreferenceUser=getSharedPreferences(Constants.strShPrefUserPrefName, Context.MODE_PRIVATE);
                customer_id=sharedPreferenceUser.getString(Constants.strShPrefUserId, "");
                // getDialogCoverage();
                if (customer_id.equalsIgnoreCase("")){
                    displayAlert("To proceed, kindly sign into your account");
                }
                else {
                    Intent intentMyAddress = new Intent(DrinkCategoriesActivity.this, MyAddressActivity.class);
                    startActivity(intentMyAddress);
                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                }
                break;
            case 14:
                sharedPreferenceUser=getSharedPreferences(Constants.strShPrefUserPrefName, Context.MODE_PRIVATE);
                customer_id=sharedPreferenceUser.getString(Constants.strShPrefUserId, "");
                if (customer_id.equalsIgnoreCase("")){
                    displayAlert("To proceed, kindly sign into your account");
                }
                else {
                    Intent intentMyWishList = new Intent(DrinkCategoriesActivity.this, MyWishListActivity.class);
                    startActivity(intentMyWishList);
                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                }
                break;
            case 15:
                sharedPreferenceUser=getSharedPreferences(Constants.strShPrefUserPrefName, Context.MODE_PRIVATE);
                customer_id=sharedPreferenceUser.getString(Constants.strShPrefUserId, "");
                if (customer_id.equalsIgnoreCase("")){
                    displayAlert("To proceed, kindly sign into your account");
                }
                else {
                    Intent intentShoppingList = new Intent(DrinkCategoriesActivity.this, MyShoppingListActivity.class);
                    startActivity(intentShoppingList);
                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                }
                break;
            case 16:
                sharedPreferenceUser=getSharedPreferences(Constants.strShPrefUserPrefName, Context.MODE_PRIVATE);
                customer_id=sharedPreferenceUser.getString(Constants.strShPrefUserId, "");
                if (customer_id.equalsIgnoreCase("")){
                    displayAlert("To proceed, kindly sign into your account");
                }
                else {
                    Intent intentPastOrders = new Intent(DrinkCategoriesActivity.this, PastOrderListActivity.class);
                    startActivity(intentPastOrders);
                    finish();
                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                }
                break;

            case 17:
                //getDialogCoverage();
                /*String urlServe = "http://hireipho.nextmp.net/stockup/where-serve";
                Intent intentServe = new Intent(Intent.ACTION_VIEW);
                intentServe.setData(Uri.parse(urlServe));
                startActivity(intentServe);*/

                Intent intentServe = new Intent(DrinkCategoriesActivity.this, StockupWebViewActivity.class);
                intentServe.putExtra("header","Where We Serve");
                intentServe.putExtra("url","http://stockup.co.za.dedi1025.jnb1.host-h.net/where-serve");
                startActivity(intentServe);
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                break;
            case 18:
                //getDialogCoverage();
                /*String urlFaq = "http://hireipho.nextmp.net/stockup/faqs";
                Intent intentFaq = new Intent(Intent.ACTION_VIEW);
                intentFaq.setData(Uri.parse(urlFaq));
                startActivity(intentFaq);*/

                Intent intentFaq = new Intent(DrinkCategoriesActivity.this, StockupWebViewActivity.class);
                intentFaq.putExtra("header","FAQs");
                intentFaq.putExtra("url","http://stockup.co.za.dedi1025.jnb1.host-h.net/faqs");
                startActivity(intentFaq);
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                break;
            case 19:
                //getDialogCoverage();
                /*String urlPP = "http://hireipho.nextmp.net/stockup/terms-condition";
                Intent intentPP = new Intent(Intent.ACTION_VIEW);
                intentPP.setData(Uri.parse(urlPP));
                startActivity(intentPP);*/

                Intent urlPP = new Intent(DrinkCategoriesActivity.this, StockupWebViewActivity.class);
                urlPP.putExtra("header","Privacy Policy");
                urlPP.putExtra("url","http://stockup.co.za.dedi1025.jnb1.host-h.net/privacy_policy");
                startActivity(urlPP);
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                break;
            case 20:
                //getDialogCoverage();
                /*String urlTandC = "http://hireipho.nextmp.net/stockup/terms-condition";
                Intent intentTandC = new Intent(Intent.ACTION_VIEW);
                intentTandC.setData(Uri.parse(urlTandC));
                startActivity(intentTandC);*/

                Intent urlTandC = new Intent(DrinkCategoriesActivity.this, StockupWebViewActivity.class);
                urlTandC.putExtra("header","Terms and Conditions");
                urlTandC.putExtra("url", "http://stockup.co.za.dedi1025.jnb1.host-h.net/terms-condition");
                startActivity(urlTandC);
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                break;
        }
    }
    public void displayAlert(String message)
    {
        // TODO Auto-generated method stub
        message="To proceed, sign into your account.";
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        //alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setTitle("Stockup") ;
        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Intent intent=new Intent(DrinkCategoriesActivity.this, LoginActivity.class);
                intent.putExtra("context_act1", "webskitters.com.stockup.DrinkCategoriesActivity");
                finish();
                startActivity(intent);
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
    private void closeDrawer(long time) {
        Handler handler
                = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                drawer.closeDrawers();
            }
        }, time);
    }

    public void displayAlertLogOut(String message)
    {
        // TODO Auto-generated method stub
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        //alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setTitle("Stockup") ;
        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();

                initFields();
                getSubCateoryList(address, "10");
                setDrawer();
                setNavigation();
                setDrawerMenu();
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
        title.setVisibility(View.GONE);
        title.setText("Stockup");
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

    private void doLogOut() {
        final ProgressDialog pDialog=new ProgressDialog(DrinkCategoriesActivity.this);
        pDialog.show();
        pDialog.setMessage("Logging you out from your account..");
        String strCustId = sharedPreferenceUser.getString(Constants.strShPrefUserId, "");
        restService.logOut(strCustId, new RestCallback<LogOutRequest>() {
            @Override
            public void success(LogOutRequest responce) {
                int reqStatus = responce.getStatus();
                int reqSuccess = responce.getSuccess();
                if (reqStatus == 200 && reqSuccess == 1) {
                    sharedPrefEditior = sharedPreferenceUser.edit();
                    sharedPrefEditior.clear();
                    sharedPrefEditior.commit();


                    //Toast.makeText(DrinkCategoriesActivity.this, "Successfully Logout", Toast.LENGTH_LONG).show();
                    displayAlertLogOut("You have been signed out successfully.");

                    utils.disconnectFromFacebook();
                } else {
                    Toast.makeText(DrinkCategoriesActivity.this, responce.getErrorMsg(), Toast.LENGTH_LONG).show();
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
    private void updateNotiStat(String customer_id) {
        final ProgressDialog pDialog=new ProgressDialog(DrinkCategoriesActivity.this);
        pDialog.show();
        pDialog.setMessage("Loading..");
        restService.getNotificationCount(customer_id, new RestCallback<NotificationCountRequest>() {
            @Override
            public void success(NotificationCountRequest responce) {
                int reqStatus = responce.getStatus();
                int reqSuccess = responce.getSuccess();
                if (reqStatus == 200 && reqSuccess == 1) {
                    //displayAlertRefresh(responce.getErrorMsg());
                    //isUpdateNeed = true;
                    Constants.push_count=responce.getData().getCount();
                    if(Constants.push_count!=null&&!Constants.push_count.equalsIgnoreCase("")&&!Constants.push_count.isEmpty())
                        NotificationCount.setBadge(DrinkCategoriesActivity.this, Integer.parseInt(Constants.push_count));
                } else {
                    //utils.displayAlert(responce.getErrorMsg());
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


}


