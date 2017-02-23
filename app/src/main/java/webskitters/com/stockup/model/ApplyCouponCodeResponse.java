package webskitters.com.stockup.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by webskitters on 11/11/2016.
 */

public class ApplyCouponCodeResponse {

    @SerializedName("success_msg")
    @Expose
    private String successMsg;
    @SerializedName("total_discount")
    @Expose
    private Double totalDiscount;

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

    /**
     *
     * @return
     * The totalDiscount
     */
    public Double getTotalDiscount() {
        return totalDiscount;
    }

    /**
     *
     * @param totalDiscount
     * The total_discount
     */
    public void setTotalDiscount(Double totalDiscount) {
        this.totalDiscount = totalDiscount;
    }
}
