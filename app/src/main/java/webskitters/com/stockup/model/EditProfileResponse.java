package webskitters.com.stockup.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by android on 10/1/2016.
 */
public class EditProfileResponse {
    @SerializedName("customer_data")
    @Expose
    private EditProfileCustomerData customerData;
    @SerializedName("success")
    @Expose
    private String success;

    /**
     *
     * @return
     * The customerData
     */
    public EditProfileCustomerData getCustomerData() {
        return customerData;
    }

    /**
     *
     * @param customerData
     * The customer_data
     */
    public void setCustomerData(EditProfileCustomerData customerData) {
        this.customerData = customerData;
    }

    /**
     *
     * @return
     * The success
     */
    public String getSuccess() {
        return success;
    }

    /**
     *
     * @param success
     * The success
     */
    public void setSuccess(String success) {
        this.success = success;
    }


}
