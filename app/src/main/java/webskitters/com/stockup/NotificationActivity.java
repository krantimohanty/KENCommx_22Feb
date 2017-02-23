package webskitters.com.stockup;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import webskitters.com.stockup.Utils.Constants;
import webskitters.com.stockup.Utils.NotificationCount;
import webskitters.com.stockup.Utils.Utils;
import webskitters.com.stockup.adapter.NotificationAdapter;
import webskitters.com.stockup.model.AdvertiseClickRequest;
import webskitters.com.stockup.model.DeleteNotificationRequest;
import webskitters.com.stockup.model.NotiStatUpdateRequest;
import webskitters.com.stockup.model.Notification;
import webskitters.com.stockup.model.NotificationRequest;
import webskitters.com.stockup.retrofit_call.RestCallback;
import webskitters.com.stockup.retrofit_call.RestService;

public class NotificationActivity extends AppCompatActivity {

    ImageView img_back;
    ListView lv_notification;
    RestService restService;
    Utils utils;
    List<Notification> arrNoti = null;
    SharedPreferences sharedPreferenceUser;

    Boolean isUpdateNeed = false;
    private String strInt="";
    Class<?> nIntent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_notification);
        utils=new Utils(NotificationActivity.this);
        restService = new RestService(this);
        Intent intentGet = getIntent();
        if (intentGet.hasExtra("context_act1")){
            //act = intentGet.getExtras().getParcelable("context_act");
            strInt = intentGet.getStringExtra("context_act1");
            //strIntFor = "map";
        }
        try {
            nIntent = Class.forName(strInt);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        initfields();
    }

    private void initfields() {

        sharedPreferenceUser = this.getSharedPreferences(Constants.strShPrefUserPrefName, Context.MODE_PRIVATE);
        img_back = (ImageView) findViewById(R.id.img_back);
        lv_notification = (ListView) findViewById(R.id.lv_notification);

        if (utils.isConnectionPossible()){
            getNotifications();
        }
        else {
            utils.displayAlert("Internet Connection Not Available.");
        }

        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(nIntent!=null) {
                    Intent intent = new Intent(NotificationActivity.this, nIntent);
                    finish();
                    startActivity(intent);
                }else{
                    Intent intent = new Intent(NotificationActivity.this, PinCodeActivity.class);
                    finish();
                    startActivity(intent);
                }
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });
        lv_notification.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getDialogNotificationDtl(position);
            }
        });

    }

    private void dialogConfDel(final int position) {
        final Dialog dialog = new Dialog(NotificationActivity.this);
        Window window = dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_remove_list);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView tvHeader = (TextView) dialog.findViewById(R.id.header);
        tvHeader.setText("Stockup");
        TextView tvMsg = (TextView) dialog.findViewById(R.id.textView_remove_list);
        tvMsg.setText("Are you sure you wish to delete this shopping list?");
        TextView btn_no = (TextView) dialog.findViewById(R.id.btn_cancel);
        TextView btn_yes = (TextView) dialog.findViewById(R.id.btn_ok);


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
                deleteNoti(arrNoti.get(position).getNotificationId());
            }
        });
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void getDialogNotificationDtl(final int position) {
        final Dialog dialog = new Dialog(NotificationActivity.this);
        Window window = dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_notification);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        ListView listview = (ListView) dialog.findViewById(R.id.listview);
        TextView txt_msg = (TextView) dialog.findViewById(R.id.txt_msg);
        TextView txt_delete = (TextView) dialog.findViewById(R.id.txt_delete);
        TextView txt_ok = (TextView) dialog.findViewById(R.id.txt_ok);

        txt_msg.setText(arrNoti.get(position).getMessage());

        if (!arrNoti.get(position).getIsRead().equalsIgnoreCase("1")) {
            updateNotiStat(arrNoti.get(position).getNotificationId());
        }

        txt_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                if (isUpdateNeed){
                    isUpdateNeed = false;
                    Intent intent = new Intent(NotificationActivity.this, NotificationActivity.class);
                    finish();
                    startActivity(intent);
                    //overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                }
            }
        });
        txt_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                deleteNoti(arrNoti.get(position).getNotificationId());
            }
        });

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    private void updateNotiStat(String notificationId) {
        final ProgressDialog pDialog=new ProgressDialog(NotificationActivity.this);
        pDialog.show();
        pDialog.setMessage("Loading..");
        restService.updateNoti(notificationId, new RestCallback<NotiStatUpdateRequest>() {
            @Override
            public void success(NotiStatUpdateRequest responce) {
                int reqStatus = responce.getStatus();
                int reqSuccess = responce.getSuccess();
                if (reqStatus == 200 && reqSuccess == 1) {
                    //displayAlertRefresh(responce.getErrorMsg());
                    isUpdateNeed = true;
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

    private void deleteNoti(String notificationId) {
        final ProgressDialog pDialog=new ProgressDialog(NotificationActivity.this);
        pDialog.show();
        pDialog.setMessage("Loading..");
        restService.delNotification(notificationId, new RestCallback<DeleteNotificationRequest>() {
            @Override
            public void success(DeleteNotificationRequest responce) {
                int reqStatus = responce.getStatus();
                int reqSuccess = responce.getSuccess();
                if (reqStatus == 200 && reqSuccess == 1) {
                    displayAlertRefresh(responce.getErrorMsg());
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

    private void getNotifications() {
        final ProgressDialog pDialog=new ProgressDialog(NotificationActivity.this);
        pDialog.show();
        pDialog.setMessage("Loading..");
        String strCustId = sharedPreferenceUser.getString(Constants.strShPrefUserId, "");
        restService.getNotification(strCustId, new RestCallback<NotificationRequest>() {
            @Override
            public void success(NotificationRequest responce) {
                int reqStatus = responce.getStatus();
                int reqSuccess = responce.getSuccess();
                if (reqStatus == 200 && reqSuccess == 1) {
                    arrNoti = responce.getData().getNotification();
                    NotificationAdapter notiAdapter = new NotificationAdapter(NotificationActivity.this, arrNoti);

                    lv_notification.setAdapter(notiAdapter);
                } else {
                    displayAlert(responce.getErrorMsg());
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
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(NotificationActivity.this);
        //alertDialogBuilder.setMessage(message);
        //alertDialogBuilder.setTitle("Stockup") ;
        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                finish();
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });
        TextView myMsg = new TextView(NotificationActivity.this);
        myMsg.setText(message);
        myMsg.setPadding(20, 20, 20, 20);
        myMsg.setTextSize(16);
        myMsg.setTextColor(Color.BLACK);
        myMsg.setGravity(Gravity.CENTER|Gravity.CENTER_HORIZONTAL);
        alertDialogBuilder.setView(myMsg);

        TextView title = new TextView(NotificationActivity.this);
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
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        LinearLayout.LayoutParams positiveButtonLL = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
        positiveButtonLL.gravity = Gravity.CENTER|Gravity.CENTER_VERTICAL;
        positiveButton.setTextColor(Color.parseColor("#048BCD"));
        positiveButton.setLayoutParams(positiveButtonLL);
    }

    public void displayAlertRefresh(String message)
    {
        // TODO Auto-generated method stub
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(NotificationActivity.this);
        //alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setTitle("Stockup") ;
        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Intent intent = new Intent(NotificationActivity.this, NotificationActivity.class);
                finish();
                startActivity(intent);
                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
            }
        });
        TextView myMsg = new TextView(NotificationActivity.this);
        myMsg.setText(message);
        myMsg.setPadding(20, 20, 20, 20);
        myMsg.setTextSize(16);
        myMsg.setTextColor(Color.BLACK);
        myMsg.setGravity(Gravity.CENTER|Gravity.CENTER_HORIZONTAL);
        alertDialogBuilder.setView(myMsg);

        TextView title = new TextView(NotificationActivity.this);
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
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        LinearLayout.LayoutParams positiveButtonLL = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
        positiveButtonLL.gravity = Gravity.CENTER|Gravity.CENTER_VERTICAL;
        positiveButton.setTextColor(Color.parseColor("#048BCD"));
        positiveButton.setLayoutParams(positiveButtonLL);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(nIntent!=null) {
            Intent intent = new Intent(NotificationActivity.this, nIntent);
            finish();
            startActivity(intent);
        }else{
            Intent intent = new Intent(NotificationActivity.this, PinCodeActivity.class);
            finish();
            startActivity(intent);
        }
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}
