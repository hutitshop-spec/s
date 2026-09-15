package com.flixpro.app.ui.details;

import android.content.Context;
import android.content.Intent;
import com.flixpro.app.model.ContentItem;

public final class DetailsLauncher {
    private DetailsLauncher(){}
    public static void open(Context c, ContentItem item){
        Intent i=new Intent(c,DetailsActivity.class);
        i.putExtra("id",item.id); i.putExtra("type",item.type); i.putExtra("title",item.title); i.putExtra("image",item.image);
        c.startActivity(i);
    }
}
