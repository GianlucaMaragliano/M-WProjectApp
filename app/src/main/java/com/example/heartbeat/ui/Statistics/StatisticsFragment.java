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
        // Inflate the fragment's layout
        return inflater.inflate(R.layout.fragment_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views after the fragment's view is created
        selectedDate = new Date(); // Default selected date is the current date

        calendarButton = view.findViewById(R.id.calendar_button);
        calendarButton.setOnClickListener(v -> showDatePicker());

        // Initialize the TextView and display the current week's range
        textViewWorkoutOfWeek = view.findViewById(R.id.textViewWorkoutOfWeek);
        displayCurrentWeekRange();

        // Call loadWeekWorkoutHistory to load the history for the current date (or selected date)
        loadWeekWorkoutHistory();
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

        String songId = dbHelper.getMostPlayedSong(weekStartDateStr, dateStr);
        Map<String, String> song = dbHelper.getSongById(songId);
        if (song != null) {
            TextView textViewMostPlayedSong = getView().findViewById(R.id.textViewMostPlayedSong);
            textViewMostPlayedSong.setText("Most played song: " + song.get("title") + " by " + song.get("artist"));
        }

        int totalNumberOfWorkouts = dbHelper.getWorkoutCount(weekStartDateStr, dateStr);
        TextView textViewTotalNumberOfWorkouts = getView().findViewById(R.id.textViewTotalNumberOfWorkouts);
        textViewTotalNumberOfWorkouts.setText("Total number of workouts: " + totalNumberOfWorkouts);

        String totalWeekDistance = dbHelper.getWeekTotalDistance(weekStartDateStr, dateStr);
        TextView textViewTotalWeekDistance = getView().findViewById(R.id.textViewTotalWeekDistance);
        textViewTotalWeekDistance.setText("Total distance covered: " + totalWeekDistance + " m");

        String avgHeartRate = dbHelper.getWeekAvgHeartRate(weekStartDateStr, dateStr);
        // convert to int
        avgHeartRate = avgHeartRate.split("\\.")[0];
        TextView textViewAvgHeartRate = getView().findViewById(R.id.textViewAvgHeartRate);
        textViewAvgHeartRate.setText("Average heart rate: " + avgHeartRate + " bpm");

        String usualWorkoutTime = dbHelper.getMostUsualTimeRange(weekStartDateStr, dateStr);
        TextView textViewUsualWorkoutTime = getView().findViewById(R.id.textViewUsualWorkoutTime);
        textViewUsualWorkoutTime.setText("Most usual workout time: " + usualWorkoutTime);

        String totalTimeWorkoutSeconds = dbHelper.getTotalWorkoutTime(weekStartDateStr, dateStr);
        // convert into hours and minutes, given is in seconds
        int totalTimeWorkout = Integer.parseInt(totalTimeWorkoutSeconds) / 60;
        TextView textViewTotalTimeWorkout = getView().findViewById(R.id.textViewTotalTimeWorkout);
        textViewTotalTimeWorkout.setText("Total time spent working out: " + totalTimeWorkout + " minutes");

    }
}