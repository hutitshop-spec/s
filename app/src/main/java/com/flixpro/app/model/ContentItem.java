package com.flixpro.app.model;

import java.io.Serializable;

public class ContentItem implements Serializable {
    public String id = "";
    public String title = "";
    public String image = "";
    public String type = "movie";
    public boolean premium;

    public ContentItem() {}
    public ContentItem(String id, String title, String image, String type, boolean premium) {
        this.id = id == null ? "" : id;
        this.title = title == null ? "" : title;
        this.image = image == null ? "" : image;
        this.type = type == null ? "movie" : type;
        this.premium = premium;
    }
}
