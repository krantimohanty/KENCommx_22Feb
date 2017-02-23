package webskitters.com.stockup.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by android on 10/5/2016.
 */
public class CompleteOrderListResponce {

    @SerializedName("order_data")
    @Expose
    private List<CompleteOrderDetails> orderData = null;
    /**
     *
     * @return
     * The orderData
     */
    public List<CompleteOrderDetails> getOrderData() {
        return orderData;
    }

    /**
     *
     * @param orderData
     * The order_data
     */
    public void setOrderData(List<CompleteOrderDetails> orderData) {
        this.orderData = orderData;
    }

}
