package webskitters.com.stockup.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by webskitters on 12/13/2016.
 */

public class OrderStstusResponse {
    @SerializedName("order_id")
    @Expose
    private String orderId;
    @SerializedName("order_status_label")
    @Expose
    private String orderStatusLabel;
    @SerializedName("order_status")
    @Expose
    private String orderStatus;
    @SerializedName("order_date")
    @Expose
    private String orderDate;
    @SerializedName("order_update_date")
    @Expose
    private String orderUpdateDate;

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
     * The orderStatusLabel
     */
    public String getOrderStatusLabel() {
        return orderStatusLabel;
    }

    /**
     *
     * @param orderStatusLabel
     * The order_status_label
     */
    public void setOrderStatusLabel(String orderStatusLabel) {
        this.orderStatusLabel = orderStatusLabel;
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
     * The orderUpdateDate
     */
    public String getOrderUpdateDate() {
        return orderUpdateDate;
    }

    /**
     *
     * @param orderUpdateDate
     * The order_update_date
     */
    public void setOrderUpdateDate(String orderUpdateDate) {
        this.orderUpdateDate = orderUpdateDate;
    }
}
