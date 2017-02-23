package webskitters.com.stockup.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SubCategoryList {

    @SerializedName("cat_id")
    @Expose
    private String catId;
    @SerializedName("cat_name")
    @Expose
    private String catName;
    @SerializedName("cat_img_url")
    @Expose
    private String catImgUrl;

    /**
     *
     * @return
     * The catId
     */
    public String getCatId() {
        return catId;
    }

    /**
     *
     * @param catId
     * The cat_id
     */
    public void setCatId(String catId) {
        this.catId = catId;
    }

    /**
     *
     * @return
     * The catName
     */
    public String getCatName() {
        return catName;
    }

    /**
     *
     * @param catName
     * The cat_name
     */
    public void setCatName(String catName) {
        this.catName = catName;
    }

    /**
     *
     * @return
     * The catImgUrl
     */
    public String getCatImgUrl() {
        return catImgUrl;
    }

    /**
     *
     * @param catImgUrl
     * The cat_img_url
     */
    public void setCatImgUrl(String catImgUrl) {
        this.catImgUrl = catImgUrl;
    }

}
