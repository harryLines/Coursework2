package com.example.trailblazer;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.trailblazer.databinding.GoalsFragmentBinding;

import java.util.List;

public class GoalsFragment extends Fragment {
    GoalsFragmentViewModel viewModel;
    GoalsFragmentBinding binding;
    Button btnAddGoal;

    public GoalsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.goals_fragment, container, false);

        viewModel = new ViewModelProvider(this).get(GoalsFragmentViewModel.class);

        // Bind the ViewModel to the layout
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);

        // Observe changes in the LiveData and update the adapter
        viewModel.getGoalsList().observe(getViewLifecycleOwner(), new Observer<List<Goal>>() {
            @Override
            public void onChanged(List<Goal> goals) {
                GoalsAdapter adapter = new GoalsAdapter(goals);
                binding.recyclerView.setAdapter(adapter);
            }
        });

        btnAddGoal.setOnClickListener(v -> {
            showAddGoalDialog();
        });


        return binding.getRoot();
    }

    private void showAddGoalDialog() {
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
        Spinner unitSpinner = dialogView.findViewById(R.id.spinnerTargetUnits); // Replace with your actual Spinner ID

        timeframeSpinner.setAdapter(timeframeAdapter);
        unitSpinner.setAdapter(unitAdapter);
        builder.setTitle("Add New Goal");

        // Set up the positive button (Add)
        builder.setPositiveButton("Add", (dialog, which) -> {
            //String locationName = etLocationName.getText().toString().trim();
        });

        // Set up the negative button (Cancel)
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        });

        // Show the dialog
        builder.create().show();
    }
}
