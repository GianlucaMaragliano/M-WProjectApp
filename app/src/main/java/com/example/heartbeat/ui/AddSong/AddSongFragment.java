package com.example.heartbeat.ui.AddSong;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.heartbeat.HeartBeatOpenHelper;
import com.example.heartbeat.databinding.FragmentAddSongBinding;

import java.util.List;
import java.util.Map;

public class AddSongFragment extends Fragment {

    private static final int PICK_AUDIO_FILE = 1; // Request code for file picker
    private FragmentAddSongBinding binding;
    private String audioFilePath = null; // Store selected audio file path

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentAddSongBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            audioFilePath = uri.toString();
                            binding.selectedAudioPath.setText(audioFilePath); // Update the displayed file path
                        }
                    }
                });

        // Handle Select Audio button
        binding.buttonSelectAudio.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("audio/*");
            filePickerLauncher.launch(intent);
        });

        // Handle Add Song button
        binding.buttonAddSong.setOnClickListener(v -> {
            addSong();
        });

        return root;
    }

    private void addSong() {
        String title = binding.inputTitle.getText().toString().trim();
        String artist = binding.inputArtist.getText().toString().trim();
        String genre = binding.inputGenre.getText().toString().trim();
        String bpmText = binding.inputBpm.getText().toString().trim();

        HeartBeatOpenHelper dbHelper = new HeartBeatOpenHelper(getContext());


        List<Map<String, String>> songs = dbHelper.getAllSongs();
        for (Map<String, String> song : songs) {
            Log.d("Song", "Title: " + song.get("title") + ", BPM: " + song.get("bpm"));
        }

        List<Map<String, String>> songs_120_bpm = dbHelper.getSongsByBpmRange(100, 130);
        for (Map<String, String> song : songs_120_bpm) {
            Log.d("Song 100-130 BPM", "Title: " + song.get("title") + ", BPM: " + song.get("bpm"));
        }

        // Validate inputs
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(artist) || TextUtils.isEmpty(genre) ||
                TextUtils.isEmpty(bpmText) || audioFilePath == null) {
            Toast.makeText(getContext(), "Please fill in all fields and select an audio file.", Toast.LENGTH_SHORT).show();
            return;
        }

        int bpm;
        try {
            bpm = Integer.parseInt(bpmText);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "BPM must be a valid number.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add song to database
        dbHelper.insertSong(title, artist, genre, bpm, audioFilePath);

        Toast.makeText(getContext(), "Song added successfully!", Toast.LENGTH_SHORT).show();

        // Reset form fields
        binding.inputTitle.setText("");
        binding.inputArtist.setText("");
        binding.inputGenre.setText("");
        binding.inputBpm.setText("");
        binding.selectedAudioPath.setText("No file selected");
        audioFilePath = null;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
