package webskitters.com.stockup.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by android on 10/5/2016.
 */
public class AutoSearchKeyResponse {
    @SerializedName("product_data")
    @Expose
    private List<AutoSearchKey> productData = new ArrayList<AutoSearchKey>();

    /**
     *
     * @return
     * The productData
     */
    public List<AutoSearchKey> getProductData() {
        return productData;
    }

    /**
     *
     * @param productData
     * The product_data
     */
    public void setProductData(List<AutoSearchKey> productData) {
        this.productData = productData;
    }
}
