package webskitters.com.stockup.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by webskitters on 11/25/2016.
 */

public class DeviceIdInstalResponse {
    @SerializedName("success_message")
    @Expose
    private String successMessage;

    /**
     *
     * @return
     * The successMessage
     */
    public String getSuccessMessage() {
        return successMessage;
    }

    /**
     *
     * @param successMessage
     * The success_message
     */
    public void setSuccessMessage(String successMessage) {
        this.successMessage = successMessage;
    }
}
