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
import com.flixpro.app.model.Episode;

import java.util.ArrayList;
import java.util.List;

public class EpisodeAdapter extends RecyclerView.Adapter<EpisodeAdapter.Holder> {
    public interface Listener { void onClick(Episode episode); }
    private final List<Episode> data = new ArrayList<>();
    private final Listener listener;
    public EpisodeAdapter(Listener l){ listener=l; }
    public void submit(List<Episode> e){ data.clear(); if(e!=null)data.addAll(e); notifyDataSetChanged(); }
    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup p,int t){ return new Holder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_episode,p,false)); }
    @Override public void onBindViewHolder(@NonNull Holder h,int pos){
        Episode e=data.get(pos); h.title.setText(e.title); h.meta.setText((e.duration==null?"":e.duration) + (e.date==null||e.date.trim().isEmpty()?"":"  •  "+e.date));
        Glide.with(h.itemView).load(e.image).centerCrop().placeholder(R.drawable.ic_flix_pro).error(R.drawable.ic_flix_pro).into(h.image); h.itemView.setOnClickListener(v->listener.onClick(e));
    }
    @Override public int getItemCount(){return data.size();}
    static class Holder extends RecyclerView.ViewHolder{ImageView image;TextView title,meta;Holder(View v){super(v);image=v.findViewById(R.id.image);title=v.findViewById(R.id.title);meta=v.findViewById(R.id.meta);}}
}
