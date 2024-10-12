package com.example.webtoonmania;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class RecAdapter extends RecyclerView.Adapter<RecAdapter.AnimeViewHolder> {
    private List<AnimeModel> animeList;
        //Constructor call
    public RecAdapter(List<AnimeModel> animeList) {
        this.animeList = animeList;
    }

    @Override
    public AnimeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.toon_details, parent, false);
        return new AnimeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AnimeViewHolder holder, int position) {
        AnimeModel anime = animeList.get(position);
        holder.titleTextView.setText(anime.title);
        // Use a library like Glide or Picasso to load the image
        Glide.with(holder.imageView.getContext()).load(anime.imageUrl).into(holder.imageView);
        holder.descriptionTextView.setText(anime.description);
    }

    @Override
    public int getItemCount() {
        return animeList.size();
    }

    public void updateData(List<AnimeModel> newAnimeList) {
        animeList.clear();
        animeList.addAll(newAnimeList);
        notifyDataSetChanged();
    }

    static class AnimeViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        ImageView imageView;
        TextView descriptionTextView;

        AnimeViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.Title);
            imageView = itemView.findViewById(R.id.image);
            descriptionTextView = itemView.findViewById(R.id.des);
        }
    }
}
