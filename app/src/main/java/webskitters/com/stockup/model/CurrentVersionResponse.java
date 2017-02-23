package webskitters.com.stockup.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by webskitters on 12/9/2016.
 */

public class CurrentVersionResponse {
    @SerializedName("version")
    @Expose
    private String version;
    @SerializedName("msg")
    @Expose
    private String msg;

    /**
     *
     * @return
     * The version
     */
    public String getVersion() {
        return version;
    }

    /**
     *
     * @param version
     * The version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     *
     * @return
     * The msg
     */
    public String getMsg() {
        return msg;
    }

    /**
     *
     * @param msg
     * The msg
     */
    public void setMsg(String msg) {
        this.msg = msg;
    }
}
