package webskitters.com.stockup.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by webskitters on 11/4/2016.
 */

public class CardDetailsResponse {
    @SerializedName("token")
    @Expose
    private List<CardDetails> token = new ArrayList<CardDetails>();

    /**
     *
     * @return
     * The token
     */
    public List<CardDetails> getToken() {
        return token;
    }

    /**
     *
     * @param token
     * The token
     */
    public void setToken(List<CardDetails> token) {
        this.token = token;
    }
}
