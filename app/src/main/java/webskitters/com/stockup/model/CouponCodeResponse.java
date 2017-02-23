package webskitters.com.stockup.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by android on 11/15/2016.
 */

public class CouponCodeResponse {

    @SerializedName("regist_coupon")
    @Expose
    private String registCoupon;
    @SerializedName("total_discount")
    @Expose
    private String totalDiscount;

    /**
     *
     * @return
     * The registCoupon
     */
    public String getRegistCoupon() {
        return registCoupon;
    }

    /**
     *
     * @param registCoupon
     * The regist_coupon
     */
    public void setRegistCoupon(String registCoupon) {
        this.registCoupon = registCoupon;
    }

    /**
     *
     * @return
     * The totalDiscount
     */
    public String getTotalDiscount() {
        return totalDiscount;
    }

    /**
     *
     * @param totalDiscount
     * The total_discount
     */
    public void setTotalDiscount(String totalDiscount) {
        this.totalDiscount = totalDiscount;
    }
}
