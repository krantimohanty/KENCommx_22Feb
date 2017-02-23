package webskitters.com.stockup.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by webskitters on 12/13/2016.
 */

public class RatingSubmitResponse {
    @SerializedName("order_id")
    @Expose
    private String orderId;
    @SerializedName("customer_id")
    @Expose
    private String customerId;
    @SerializedName("arrival")
    @Expose
    private String arrival;
    @SerializedName("professionalism")
    @Expose
    private String professionalism;
    @SerializedName("packing")
    @Expose
    private String packing;
    @SerializedName("other")
    @Expose
    private String other;
    @SerializedName("comment")
    @Expose
    private String comment;
    @SerializedName("success_msg")
    @Expose
    private String successMsg;

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
     * The arrival
     */
    public String getArrival() {
        return arrival;
    }

    /**
     *
     * @param arrival
     * The arrival
     */
    public void setArrival(String arrival) {
        this.arrival = arrival;
    }

    /**
     *
     * @return
     * The professionalism
     */
    public String getProfessionalism() {
        return professionalism;
    }

    /**
     *
     * @param professionalism
     * The professionalism
     */
    public void setProfessionalism(String professionalism) {
        this.professionalism = professionalism;
    }

    /**
     *
     * @return
     * The packing
     */
    public String getPacking() {
        return packing;
    }

    /**
     *
     * @param packing
     * The packing
     */
    public void setPacking(String packing) {
        this.packing = packing;
    }

    /**
     *
     * @return
     * The other
     */
    public String getOther() {
        return other;
    }

    /**
     *
     * @param other
     * The other
     */
    public void setOther(String other) {
        this.other = other;
    }

    /**
     *
     * @return
     * The comment
     */
    public String getComment() {
        return comment;
    }

    /**
     *
     * @param comment
     * The comment
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     *
     * @return
     * The successMsg
     */
    public String getSuccessMsg() {
        return successMsg;
    }

    /**
     *
     * @param successMsg
     * The success_msg
     */
    public void setSuccessMsg(String successMsg) {
        this.successMsg = successMsg;
    }
}
