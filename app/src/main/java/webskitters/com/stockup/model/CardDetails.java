package webskitters.com.stockup.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by webskitters on 11/4/2016.
 */

public class CardDetails {
    @SerializedName("token_id")
    @Expose
    private String tokenId;
    @SerializedName("customer_id")
    @Expose
    private String customerId;
    @SerializedName("token")
    @Expose
    private String token;
    @SerializedName("expire")
    @Expose
    private String expire;
    @SerializedName("card_last_four")
    @Expose
    private String cardLastFour;
    @SerializedName("card_brand")
    @Expose
    private String cardBrand;
    @SerializedName("card_nickname")
    @Expose
    private String cardNickname;
    @SerializedName("account_number_count_of_digits")
    @Expose
    private String accountNumberCountOfDigits;
    @SerializedName("contact_ip")
    @Expose
    private String contactIp;
    @SerializedName("last_used")
    @Expose
    private String lastUsed;

    /**
     *
     * @return
     * The tokenId
     */
    public String getTokenId() {
        return tokenId;
    }

    /**
     *
     * @param tokenId
     * The token_id
     */
    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    /**
     *
     * @return
     * The customerId
     */
    public String getCustomerId() {
        return customerId;
    }

    /**
     *
     * @param customerId
     * The customer_id
     */
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    /**
     *
     * @return
     * The token
     */
    public String getToken() {
        return token;
    }

    /**
     *
     * @param token
     * The token
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     *
     * @return
     * The expire
     */
    public String getExpire() {
        return expire;
    }

    /**
     *
     * @param expire
     * The expire
     */
    public void setExpire(String expire) {
        this.expire = expire;
    }

    /**
     *
     * @return
     * The cardLastFour
     */
    public String getCardLastFour() {
        return cardLastFour;
    }

    /**
     *
     * @param cardLastFour
     * The card_last_four
     */
    public void setCardLastFour(String cardLastFour) {
        this.cardLastFour = cardLastFour;
    }

    /**
     *
     * @return
     * The cardBrand
     */
    public String getCardBrand() {
        return cardBrand;
    }

    /**
     *
     * @param cardBrand
     * The card_brand
     */
    public void setCardBrand(String cardBrand) {
        this.cardBrand = cardBrand;
    }

    /**
     *
     * @return
     * The cardNickname
     */
    public String getCardNickname() {
        return cardNickname;
    }

    /**
     *
     * @param cardNickname
     * The card_nickname
     */
    public void setCardNickname(String cardNickname) {
        this.cardNickname = cardNickname;
    }

    /**
     *
     * @return
     * The accountNumberCountOfDigits
     */
    public String getAccountNumberCountOfDigits() {
        return accountNumberCountOfDigits;
    }

    /**
     *
     * @param accountNumberCountOfDigits
     * The account_number_count_of_digits
     */
    public void setAccountNumberCountOfDigits(String accountNumberCountOfDigits) {
        this.accountNumberCountOfDigits = accountNumberCountOfDigits;
    }

    /**
     *
     * @return
     * The contactIp
     */
    public String getContactIp() {
        return contactIp;
    }

    /**
     *
     * @param contactIp
     * The contact_ip
     */
    public void setContactIp(String contactIp) {
        this.contactIp = contactIp;
    }

    /**
     *
     * @return
     * The lastUsed
     */
    public String getLastUsed() {
        return lastUsed;
    }

    /**
     *
     * @param lastUsed
     * The last_used
     */
    public void setLastUsed(String lastUsed) {
        this.lastUsed = lastUsed;
    }
}
