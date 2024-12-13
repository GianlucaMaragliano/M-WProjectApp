package com.example.heartbeat.ui.WorkoutHistory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.heartbeat.R;

import java.util.List;
import java.util.Map;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.ViewHolder> {

    private List<Map<String, String>> songsList;

    public SongsAdapter(List<Map<String, String>> songsList) {
        this.songsList = songsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, String> song = songsList.get(position);
        holder.songTitleTextView.setText(song.get("title")); // Set the song title
        holder.artistTextView.setText(song.get("artist"));   // Set the artist name
    }

    @Override
    public int getItemCount() {
        return songsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView songTitleTextView;
        TextView artistTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            songTitleTextView = itemView.findViewById(R.id.textViewSongTitle);
            artistTextView = itemView.findViewById(R.id.textViewArtist);
        }
    }
}