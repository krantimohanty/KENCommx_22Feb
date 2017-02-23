package webskitters.com.stockup.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by webskitters on 11/7/2016.
 */

public class Notification {
    @SerializedName("notification_id")
    @Expose
    private String notificationId;
    @SerializedName("order_id")
    @Expose
    private String orderId;
    @SerializedName("customer_id")
    @Expose
    private String customerId;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("device_id")
    @Expose
    private String deviceId;
    @SerializedName("is_read")
    @Expose
    private String isRead;
    @SerializedName("created_time")
    @Expose
    private String createdTime;

    /**
     *
     * @return
     * The notificationId
     */
    public String getNotificationId() {
        return notificationId;
    }

    /**
     *
     * @param notificationId
     * The notification_id
     */
    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    /**
     *
     * @return
     * The orderId
     */
    public String getOrderId() {
        return orderId;
    }

    /**
     *
     * @param orderId
     * The order_id
     */
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    /**
     *
     * @return
     * The customerId
     */
    public String getCustomerId() {
        return customerId;
    }

    /**
     *
     * @param customerId
     * The customer_id
     */
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    /**
     *
     * @return
     * The message
     */
    public String getMessage() {
        return message;
    }

    /**
     *
     * @param message
     * The message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     *
     * @return
     * The deviceId
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     *
     * @param deviceId
     * The device_id
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     *
     * @return
     * The isRead
     */
    public String getIsRead() {
        return isRead;
    }

    /**
     *
     * @param isRead
     * The is_read
     */
    public void setIsRead(String isRead) {
        this.isRead = isRead;
    }

    /**
     *
     * @return
     * The createdTime
     */
    public String getCreatedTime() {
        return createdTime;
    }

    /**
     *
     * @param createdTime
     * The created_time
     */
    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }
}
