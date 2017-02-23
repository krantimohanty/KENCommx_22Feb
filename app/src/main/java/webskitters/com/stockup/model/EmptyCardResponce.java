package webskitters.com.stockup.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by android on 10/18/2016.
 */

public class EmptyCardResponce {

    @SerializedName("suc_msg")
    @Expose
    private String sucMsg;

    /**
     *
     * @return
     * The sucMsg
     */
    public String getSucMsg() {
        return sucMsg;
    }

    /**
     *
     * @param sucMsg
     * The suc_msg
     */
    public void setSucMsg(String sucMsg) {
        this.sucMsg = sucMsg;
    }
}
