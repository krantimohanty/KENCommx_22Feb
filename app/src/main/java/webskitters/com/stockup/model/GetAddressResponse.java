package webskitters.com.stockup.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by android on 10/6/2016.
 */
public class GetAddressResponse {
    @SerializedName("customer_data")
    @Expose
    private GetAddress customerData;

    /**
     *
     * @return
     * The customerData
     */
    public GetAddress getCustomerData() {
        return customerData;
    }

    /**
     *
     * @param customerData
     * The customer_data
     */
    public void setCustomerData(GetAddress customerData) {
        this.customerData = customerData;
    }
}
