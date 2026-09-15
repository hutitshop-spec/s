package com.flixpro.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.flixpro.app.R;
import com.flixpro.app.model.ContentItem;

import java.util.ArrayList;
import java.util.List;

public class HeroAdapter extends RecyclerView.Adapter<HeroAdapter.Holder> {
    public interface Listener { void onClick(ContentItem item); }
    private final List<ContentItem> items = new ArrayList<>();
    private final Listener listener;
    public HeroAdapter(Listener l) { listener = l; }
    public void submit(List<ContentItem> data) { items.clear(); if (data != null) items.addAll(data); notifyDataSetChanged(); }
    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup p, int t) { return new Holder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_hero, p, false)); }
    @Override public void onBindViewHolder(@NonNull Holder h, int pos) {
        ContentItem i = items.get(pos);
        h.title.setText(i.title == null || i.title.trim().isEmpty() ? "Flix Pro" : i.title);
        h.badge.setText(i.premium ? "PRO • VIEW" : "VIEW");
        Glide.with(h.itemView).load(i.image).centerCrop().placeholder(R.drawable.ic_flix_pro).error(R.drawable.ic_flix_pro).into(h.image);
        h.itemView.setOnClickListener(v -> listener.onClick(i));
    }
    @Override public int getItemCount() { return items.size(); }
    static class Holder extends RecyclerView.ViewHolder {
        ImageView image; TextView title,badge;
        Holder(View v){ super(v); image=v.findViewById(R.id.image);title=v.findViewById(R.id.title);badge=v.findViewById(R.id.badge); }
    }
}
