package webskitters.com.stockup.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by webskitters on 10/14/2016.
 */

public class CartTotalResponse {
    @SerializedName("total_qty")
    @Expose
    private Integer totalQty;
    @SerializedName("total_price")
    @Expose
    private String totalPrice;
    @SerializedName("currency")
    @Expose
    private String currency;

    /**
     *
     * @return
     * The totalQty
     */
    public Integer getTotalQty() {
        return totalQty;
    }

    /**
     *
     * @param totalQty
     * The total_qty
     */
    public void setTotalQty(Integer totalQty) {
        this.totalQty = totalQty;
    }

    /**
     *
     * @return
     * The totalPrice
     */
    public String getTotalPrice() {
        return totalPrice;
    }

    /**
     *
     * @param totalPrice
     * The total_price
     */
    public void setTotalPrice(String totalPrice) {
        this.totalPrice = totalPrice;
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
}
