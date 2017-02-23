package webskitters.com.stockup.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by webskitters on 10/5/2016.
 */
public class GetCartListResponse {


    @SerializedName("quote_id")
    @Expose
    private String quoteId;
    @SerializedName("items")
    @Expose
    private List<CartData> items = new ArrayList<CartData>();

    /**
     *
     * @return
     * The items
     */
    public List<CartData> getItems() {
        return items;
    }

    /**
     *
     * @param items
     * The items
     */
    public void setItems(List<CartData> items) {
        this.items = items;
    }

    /**
     *
     * @return
     * The quoteId
     */
    public String getQuoteId() {
        return quoteId;
    }

    /**
     *
     * @param quoteId
     * The quote_id
     */
    public void setQuoteId(String quoteId) {
        this.quoteId = quoteId;
    }
}
