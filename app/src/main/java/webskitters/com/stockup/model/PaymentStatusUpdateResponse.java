package webskitters.com.stockup.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by webskitters on 11/14/2016.
 */

public class PaymentStatusUpdateResponse {

    @SerializedName("is_success")
    @Expose
    private String isSuccess;

    /**
     *
     * @return
     * The isSuccess
     */
    public String getIsSuccess() {
        return isSuccess;
    }

    /**
     *
     * @param isSuccess
     * The is_success
     */
    public void setIsSuccess(String isSuccess) {
        this.isSuccess = isSuccess;
    }
}
