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

public class GridContentAdapter extends RecyclerView.Adapter<GridContentAdapter.Holder> {
    public interface Listener { void onClick(ContentItem item); }
    private final List<ContentItem> items = new ArrayList<>();
    private final Listener listener;

    public GridContentAdapter(Listener listener) { this.listener = listener; }

    public void submit(List<ContentItem> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grid_content, parent, false));
    }

    @Override public void onBindViewHolder(@NonNull Holder h, int position) {
        ContentItem item = items.get(position);
        h.title.setText(item.title == null || item.title.trim().isEmpty() ? "Flix Pro" : item.title);
        h.premium.setVisibility(item.premium ? View.VISIBLE : View.GONE);
        Glide.with(h.itemView)
                .load(item.image)
                .centerCrop()
                .placeholder(R.drawable.ic_flix_pro)
                .error(R.drawable.ic_flix_pro)
                .into(h.poster);
        h.itemView.setOnClickListener(v -> listener.onClick(item));
    }

    @Override public int getItemCount() { return items.size(); }

    static class Holder extends RecyclerView.ViewHolder {
        ImageView poster; TextView title, premium;
        Holder(View v) {
            super(v);
            poster = v.findViewById(R.id.poster);
            title = v.findViewById(R.id.title);
            premium = v.findViewById(R.id.premium);
        }
    }
}
