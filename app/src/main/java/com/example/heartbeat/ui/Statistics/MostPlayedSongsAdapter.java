package com.example.heartbeat.ui.Statistics;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.heartbeat.HeartBeatOpenHelper;
import com.example.heartbeat.R;

import java.util.List;
import java.util.Map;

public class MostPlayedSongsAdapter extends RecyclerView.Adapter<MostPlayedSongsAdapter.SongViewHolder> {

    private final List<Map<String, String>> songs;
    private HeartBeatOpenHelper dbHelper;

    public MostPlayedSongsAdapter(List<Map<String, String>> songs) {
        this.songs = songs;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_most_played_song, parent, false);

        dbHelper = new HeartBeatOpenHelper(parent.getContext());
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Map<String, String> song = songs.get(position);
        Map<String, String> songObj = dbHelper.getSongById(song.get("songId"));
        holder.songId.setText(songObj.get("title") + " - " + songObj.get("artist"));
        holder.playCount.setText("Played " + song.get("count") + " times");
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    static class SongViewHolder extends RecyclerView.ViewHolder {
        TextView songId;
        TextView playCount;

        SongViewHolder(@NonNull View itemView) {
            super(itemView);
            songId = itemView.findViewById(R.id.textViewSongId);
            playCount = itemView.findViewById(R.id.textViewPlayCount);
        }
    }
}
