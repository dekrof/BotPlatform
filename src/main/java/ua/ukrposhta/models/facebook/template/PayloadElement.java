package ua.ukrposhta.models.facebook.template;

import com.google.gson.annotations.SerializedName;


public class PayloadElement {
    private String title;
    @SerializedName("image_url")
    private String imageUrl;
    @SerializedName("item_url")
    private String itemUrl;

    public String getTitle() {
        return title;
    }

    public PayloadElement setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public PayloadElement setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }

    public String getItemUrl() {
        return itemUrl;
    }

    public PayloadElement setItemUrl(String itemUrl) {
        this.itemUrl = itemUrl;
        return this;
    }
}
