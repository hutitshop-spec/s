package com.flixpro.app.model;

import java.util.ArrayList;
import java.util.List;

public class MediaDetails {
    public String id = "";
    public String type = "movie";
    public String title = "";
    public String image = "";
    public String description = "";
    public String language = "";
    public String rating = "";
    public String contentRating = "";
    public String duration = "";
    public String releaseDate = "";
    public String videoUrl = "";
    public String videoType = "";
    public String quality480 = "";
    public String quality720 = "";
    public String quality1080 = "";
    public boolean premium;
    public boolean upcoming;
    public final List<Season> seasons = new ArrayList<>();
    public final List<ContentItem> related = new ArrayList<>();
}
