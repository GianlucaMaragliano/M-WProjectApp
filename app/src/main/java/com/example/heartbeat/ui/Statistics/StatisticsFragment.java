package com.example.heartbeat.ui.Statistics;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.heartbeat.HeartBeatOpenHelper;
import com.example.heartbeat.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatisticsFragment extends Fragment {

    private ImageButton calendarButton;
    private Date selectedDate;
    private TextView textViewWorkoutOfWeek;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_statistics, container, false);

        selectedDate = new Date();

        calendarButton = rootView.findViewById(R.id.calendar_button);
        calendarButton.setOnClickListener(v -> showDatePicker());

        // Initialize the TextView and display the current week's range
        textViewWorkoutOfWeek = rootView.findViewById(R.id.textViewWorkoutOfWeek);
        displayCurrentWeekRange();

        return rootView;
    }

    private void displayCurrentWeekRange() {
        // Create a Calendar object for the selected date
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(selectedDate);

        // Calculate the start of the range (7 days before today)
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        Date startDate = calendar.getTime();

        // Format dates
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String weekStartDateStr = dateFormat.format(startDate);
        String weekEndDateStr = dateFormat.format(selectedDate);

        // Update the TextView
        textViewWorkoutOfWeek.setText("Workout of the week:\n" + weekStartDateStr + " - " + weekEndDateStr);
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(selectedDate); // Start with the currently selected date
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar newSelectedDate = Calendar.getInstance();
                    newSelectedDate.set(selectedYear, selectedMonth, selectedDay);
                    selectedDate = newSelectedDate.getTime(); // Update the selected date
                    loadWeekWorkoutHistory(); // Reload workout history for the new date
                },
                year, month, day);
        datePickerDialog.show();
    }

    private void loadWeekWorkoutHistory() {
        HeartBeatOpenHelper dbHelper = new HeartBeatOpenHelper(getContext());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateStr = dateFormat.format(selectedDate);
        // compute seven days before the selected date
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(selectedDate);
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        String weekStartDateStr = dateFormat.format(calendar.getTime());
        textViewWorkoutOfWeek = getView().findViewById(R.id.textViewWorkoutOfWeek);
        textViewWorkoutOfWeek.setText("Workout of the week:\n" + weekStartDateStr + " - " + dateStr);

//        String mostPlayedSong = dbHelper.getMostPlayedSong(weekStartDateStr, dateStr);
//        TextView textViewMostPlayedSong = getView().findViewById(R.id.textViewMostPlayedSong);
//        textViewMostPlayedSong.setText("Most played song: " + mostPlayedSong);

    }
}