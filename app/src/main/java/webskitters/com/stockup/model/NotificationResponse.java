package webskitters.com.stockup.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by webskitters on 11/7/2016.
 */

public class NotificationResponse {
    @SerializedName("notification")
    @Expose
    private List<Notification> notification = new ArrayList<Notification>();

    /**
     *
     * @return
     * The notification
     */
    public List<Notification> getNotification() {
        return notification;
    }

    /**
     *
     * @param notification
     * The notification
     */
    public void setNotification(List<Notification> notification) {
        this.notification = notification;
    }

}
