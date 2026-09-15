package com.flixpro.app.model;

import java.util.ArrayList;
import java.util.List;

public class Section {
    public String title = "";
    public final List<ContentItem> items = new ArrayList<>();
    public Section() {}
    public Section(String title) { this.title = title; }
}
