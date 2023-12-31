package com.example.trailblazer;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import com.example.trailblazer.databinding.GoalsFragmentBinding;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class GoalsFragment extends Fragment {
    GoalsFragmentViewModel viewModel;
    GoalsFragmentBinding binding;
    Button btnAddGoal;
    Database database;

    public GoalsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.goals_fragment, container, false);
        viewModel = new ViewModelProvider(this).get(GoalsFragmentViewModel.class);
        btnAddGoal = binding.btnCreateGoal;

        database = DatabaseManager.getInstance(requireContext());

        // Bind the ViewModel to the layout
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);

        setupGoalsList();

        return binding.getRoot();
    }

    private void setupGoalsList() {
        viewModel.setGoalsList(getGoalsFromDatabase().getValue());

        // Observe changes in the LiveData and update the adapter
        viewModel.getGoalsList().observe(getViewLifecycleOwner(), goals -> {
            // Filter out completed goals
            List<Goal> incompleteGoals = filterCompletedGoals(goals);

            GoalsAdapter adapter = new GoalsAdapter(incompleteGoals);
            binding.recyclerView.setAdapter(adapter);
        });

        btnAddGoal.setOnClickListener(v -> showAddGoalDialog());
    }

    private List<Goal> filterCompletedGoals(List<Goal> goals) {
        List<Goal> incompleteGoals = new ArrayList<>();

        if (goals != null) { // Check if goals is not null
            for (Goal goal : goals) {
                if (!goal.isComplete) {
                    incompleteGoals.add(goal);
                }
            }
        }

        return incompleteGoals;
    }

    private LiveData<List<Goal>> getGoalsFromDatabase() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        MutableLiveData<List<Goal>> goalList = new MutableLiveData<>();

        executor.execute(() -> {
            List<Goal> goals = database.goalDao().loadGoals();
            handler.post(() -> {
                goalList.postValue(goals); // Update LiveData on the main thread
            });
        });

        return goalList;
    }

    private void showAddGoalDialog() {
        final int[] metricType = {0};
        final int[] timeframeType = {0};
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_goal, null);
        builder.setView(dialogView);

        ArrayAdapter<CharSequence> timeframeAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.timeframe_types,
                android.R.layout.simple_spinner_item
        );
        timeframeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<CharSequence> unitAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.metric_units,
                android.R.layout.simple_spinner_item
        );
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner timeframeSpinner = dialogView.findViewById(R.id.spinnerTimeframeType); // Replace with your actual Spinner ID
        Spinner metricSpinner = dialogView.findViewById(R.id.spinnerTargetUnits); // Replace with your actual Spinner ID
        EditText editTextNumOfTimeframes = dialogView.findViewById(R.id.editTextTimeframe);
        EditText editTextTarget = dialogView.findViewById(R.id.editTextTarget);

        timeframeSpinner.setAdapter(timeframeAdapter);
        metricSpinner.setAdapter(unitAdapter);

        timeframeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                timeframeType[0] = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Handle the case where nothing is selected
            }
        });

        metricSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                metricType[0] = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Handle the case where nothing is selected
            }
        });

        builder.setTitle("Add New Goal");

        // Set up the positive button (Add)
        builder.setPositiveButton("Add", (dialog, which) -> {
            int numOfTimeframes = Integer.parseInt(editTextNumOfTimeframes.getText().toString());
            double target = Double.parseDouble(editTextTarget.getText().toString());

            Date currentDate = new Date();
            // Define the desired date format
            Goal newGoal = new Goal(0,metricType[0],numOfTimeframes,timeframeType[0],0,target,currentDate);

            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(() -> {
                database.goalDao().addNewGoal(newGoal);
                List<Goal> updatedGoalsList = database.goalDao().loadGoals();
                handler.post(() -> {
                    viewModel.setGoalsList(updatedGoalsList);
                });
            });
        });

        // Set up the negative button (Cancel)
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        });

        // Show the dialog
        builder.create().show();
    }
}
