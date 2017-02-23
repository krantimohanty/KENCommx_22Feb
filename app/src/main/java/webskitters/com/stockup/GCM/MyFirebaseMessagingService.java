package webskitters.com.stockup.GCM;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.appsflyer.AFInAppEventParameterName;
import com.appsflyer.AppsFlyerLib;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import webskitters.com.stockup.LoginActivity;
import webskitters.com.stockup.NotificationActivity;
import webskitters.com.stockup.ProductDetailsActivity;
import webskitters.com.stockup.Utils.Utils;

/**
 * Created by Ravi Tamada on 08/08/16.
 * www.androidhive.info
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();

    private NotificationUtils notificationUtils;


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.e(TAG, "From: " + remoteMessage.getFrom());

        if (remoteMessage == null)
            return;

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.e(TAG, "Notification Body: " + remoteMessage.getNotification().getBody());
            handleNotification(remoteMessage.getNotification().getBody());
        }

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.e(TAG, "Data Payload: " + remoteMessage.getData().toString());
            Log.e(TAG, "remoteMessage: " + remoteMessage.toString());

            try {
                /*JSONObject json = new JSONObject(remoteMessage.getData().toString());
                //String test = "{\"type\":1, \"badge\":1, \"message\":\"This is for test\", \"senderid\":19484110841}";
                //JSONObject json = new JSONObject(test);
                handleDataMessage(json);*/

                String type = remoteMessage.getData().get("type");
                String message = remoteMessage.getData().get("message");
                //boolean isBackground = data.getBoolean("is_background");
                //String imageUrl = data.getString("image");
                String badge = remoteMessage.getData().get("badge");
                String senderid = remoteMessage.getData().get("senderid");

                handleDataMessage(type,message,badge,senderid);


            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
            }
        }
    }



    private void handleNotification(String message) {
        if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
            // app is in foreground, broadcast the push message
            Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
            pushNotification.putExtra("message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

            // play notification sound
            NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
            notificationUtils.playNotificationSound();
        }else{
            // If the app is in background, firebase itself handles the notification
        }
    }


    /*private void handleDataMessage(String type, String message, String badge, String senderid) {
        }
    */
    //private void handleDataMessage(JSONObject json) {
    //Log.e(TAG, "push json: " + json.toString());
    private void handleDataMessage(String type, String message, String badge, String senderid){


            Log.e(TAG, "type: " + type);
            Log.e(TAG, "message: " + message);
            Log.e(TAG, "senderid: " + senderid);

            Map<String, Object> eventValue = new HashMap<String, Object>();
            AppsFlyerLib.getInstance().trackEvent(getApplicationContext(), "af_opened_from_push_notification",eventValue);

            if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {

                Intent resultIntent = new Intent(getApplicationContext(), NotificationActivity.class);
                resultIntent.putExtra("message", message);
                int time = (int) (System.currentTimeMillis());
                Timestamp tsTemp = new Timestamp(time);
                String timeStamp =  tsTemp.toString();
                showNotificationMessage(getApplicationContext(), type, message, timeStamp, resultIntent);

            }else{

                Intent resultIntent = new Intent(getApplicationContext(), NotificationActivity.class);
                resultIntent.putExtra("message", message);
                int time = (int) (System.currentTimeMillis());
                Timestamp tsTemp = new Timestamp(time);
                String timeStamp =  tsTemp.toString();
                showNotificationMessage(getApplicationContext(), type, message, timeStamp, resultIntent);
            }
        }


    /**
     * Showing notification with text only
     */
    private void showNotificationMessage(Context context, String title, String message, String timeStamp, Intent intent) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage("StockUpApp", message, timeStamp, intent);
    }

    /**
     * Showing notification with text and image
     */
    private void showNotificationMessageWithBigImage(Context context, String title, String message, String timeStamp, Intent intent, String imageUrl) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent, imageUrl);
    }
}
