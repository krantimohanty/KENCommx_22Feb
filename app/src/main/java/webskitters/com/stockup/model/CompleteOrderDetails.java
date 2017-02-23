package webskitters.com.stockup.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by webskitters on 11/4/2016.
 */

public class CompleteOrderDetails {
    @SerializedName("order_id")
    @Expose
    private String orderId;
    @SerializedName("order_amount")
    @Expose
    private String orderAmount;
    @SerializedName("currency")
    @Expose
    private String currency;
    @SerializedName("order_status")
    @Expose
    private String orderStatus;
    @SerializedName("order_date")
    @Expose
    private String orderDate;
    @SerializedName("service_rating")
    @Expose
    private String serviceRating;

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
     * The orderAmount
     */
    public String getOrderAmount() {
        return orderAmount;
    }

    /**
     *
     * @param orderAmount
     * The order_amount
     */
    public void setOrderAmount(String orderAmount) {
        this.orderAmount = orderAmount;
    }

    /**
     *
     * @return
     * The currency
     */
    public String getCurrency() {
        return currency;
    }

    /**
     *
     * @param currency
     * The currency
     */
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     *
     * @return
     * The orderStatus
     */
    public String getOrderStatus() {
        return orderStatus;
    }

    /**
     *
     * @param orderStatus
     * The order_status
     */
    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    /**
     *
     * @return
     * The orderDate
     */
    public String getOrderDate() {
        return orderDate;
    }

    /**
     *
     * @param orderDate
     * The order_date
     */
    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    /**
     *
     * @return
     * The serviceRating
     */
    public String getServiceRating() {
        return serviceRating;
    }

    /**
     *
     * @param serviceRating
     * The service_rating
     */
    public void setServiceRating(String serviceRating) {
        this.serviceRating = serviceRating;
    }
}
