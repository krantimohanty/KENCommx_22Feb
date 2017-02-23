package webskitters.com.stockup.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by webskitters on 12/13/2016.
 */

public class RatingSubmitRequest {
    @SerializedName("data")
    @Expose
    private RatingSubmitResponse data;
    @SerializedName("status")
    @Expose
    private Integer status;
    @SerializedName("error_msg")
    @Expose
    private String errorMsg;
    @SerializedName("success")
    @Expose
    private Integer success;

    /**
     *
     * @return
     * The data
     */
    public RatingSubmitResponse getData() {
        return data;
    }

    /**
     *
     * @param data
     * The data
     */
    public void setData(RatingSubmitResponse data) {
        this.data = data;
    }

    /**
     *
     * @return
     * The status
     */
    public Integer getStatus() {
        return status;
    }

    /**
     *
     * @param status
     * The status
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     *
     * @return
     * The errorMsg
     */
    public String getErrorMsg() {
        return errorMsg;
    }

    /**
     *
     * @param errorMsg
     * The error_msg
     */
    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    /**
     *
     * @return
     * The success
     */
    public Integer getSuccess() {
        return success;
    }

    /**
     *
     * @param success
     * The success
     */
    public void setSuccess(Integer success) {
        this.success = success;
    }
}
