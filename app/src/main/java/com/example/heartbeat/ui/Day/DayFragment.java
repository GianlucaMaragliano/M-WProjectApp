package com.example.heartbeat.ui.Day;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Column;
import com.anychart.enums.Anchor;
import com.anychart.enums.HoverMode;
import com.anychart.enums.Position;
import com.anychart.enums.TooltipPositionMode;
import com.example.heartbeat.HeartBeatOpenHelper;
import com.example.heartbeat.databinding.FragmentDayBinding;
import com.example.heartbeat.R;

public class DayFragment extends Fragment {

    public int todaySteps = 0;
    TextView numStepsTextView;
    AnyChartView anyChartView;

    Date cDate = new Date();
    String current_time = new SimpleDateFormat("yyyy-MM-dd").format(cDate);

    public Map<String, Integer> stepsByDay = null;

    private FragmentDayBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentDayBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Create column chart
        anyChartView = root.findViewById(R.id.dayBarChart);
        Log.d("ANYCHARTVIEW", String.valueOf(anyChartView));
        anyChartView.setProgressBar(root.findViewById(R.id.loadingBar));

        Cartesian cartesian = createColumnChart();
        anyChartView.setBackgroundColor("#00000000");
        anyChartView.setChart(cartesian);


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public Cartesian createColumnChart(){
        //***** Read data from SQLiteDatabase *********/
        stepsByDay = HeartBeatOpenHelper.loadStepsByDay(getContext(), current_time);

        Map<String, Integer> graph_map = new TreeMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        // Initialize the graph_map with the last 7 days
        for (int i = 0; i < 7; i++) {
            String date = dateFormat.format(new Date(cDate.getTime() - i * 24 * 3600 * 1000));
            // If steps are recorded for this date, use that value; otherwise, use 0
            graph_map.put(date, stepsByDay.getOrDefault(date, 0));
        }

        //***** Create column chart using AnyChart library *********/
        Cartesian cartesian = AnyChart.column();

        List<DataEntry> data = new ArrayList<>();
        for (Map.Entry<String,Integer> entry : graph_map.entrySet())
            data.add(new ValueDataEntry(String.valueOf(entry.getKey()), entry.getValue()));

        Column column = cartesian.column(data);

        //***** Modify the UI of the chart *********/
        column.fill("#1EB980");
        column.stroke("#1EB980");

        column.tooltip()
                .titleFormat("At day: {%X}")
                .format("{%Value} Steps")
                .anchor(Anchor.RIGHT_BOTTOM);

        column.tooltip()
                .position(Position.RIGHT_TOP)
                .offsetX(0d)
                .offsetY(5);


        // Modifying properties of cartesian
        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);
        cartesian.interactivity().hoverMode(HoverMode.BY_X);
        cartesian.yScale().minimum(0);


        cartesian.background().fill("#00000000");
        cartesian.yAxis(0).title("Number of Steps");
        cartesian.xAxis(0).title("Day");
        cartesian.animation(true);


        return cartesian;
    }
}