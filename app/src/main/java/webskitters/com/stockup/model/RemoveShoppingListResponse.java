package webskitters.com.stockup.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by android on 10/5/2016.
 */
public class RemoveShoppingListResponse {
    @SerializedName("success_msg")
    @Expose
    private String successMsg;

    /**
     * @return The successMsg
     */
    public String getSuccessMsg() {
        return successMsg;
    }

    /**
     * @param successMsg The success_msg
     */
    public void setSuccessMsg(String successMsg) {
        this.successMsg = successMsg;
    }

}
