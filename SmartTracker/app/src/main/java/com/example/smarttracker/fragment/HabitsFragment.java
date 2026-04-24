package com.example.smarttracker.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.smarttracker.R;
import com.example.smarttracker.adapter.HabitAdapter;
import com.example.smarttracker.util.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

public class HabitsFragment extends Fragment implements HabitAdapter.OnHabitDeleteListener {

    private static final String BASE_URL = "http://10.0.2.2/smarttracker/";

    private RecyclerView recyclerHabits;
    private HabitAdapter habitAdapter;
    private ProgressBar progressHabits;
    private TextView tvEmpty;
    private RequestQueue queue;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_habits, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        queue = Volley.newRequestQueue(requireContext());
        sessionManager = new SessionManager(requireContext());

        recyclerHabits = (RecyclerView) view.findViewById(R.id.recyclerHabits);
        progressHabits = (ProgressBar) view.findViewById(R.id.progressHabits);
        tvEmpty = (TextView) view.findViewById(R.id.tvEmptyHabits);

        habitAdapter = new HabitAdapter(this);
        recyclerHabits.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerHabits.setAdapter(habitAdapter);

        loadHabits();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHabits();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) loadHabits();
    }

    private void loadHabits() {
        progressHabits.setVisibility(View.VISIBLE);
        int userId = sessionManager.getUserId();
        String url = BASE_URL + "gethabits.php?user_id=" + userId;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (!isAdded()) return;
                        progressHabits.setVisibility(View.GONE);
                        habitAdapter.setHabits(response);
                        tvEmpty.setVisibility(response.length() == 0 ? View.VISIBLE : View.GONE);
                        recyclerHabits.setVisibility(response.length() == 0 ? View.GONE : View.VISIBLE);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (isAdded()) {
                            progressHabits.setVisibility(View.GONE);
                            Toast.makeText(requireContext(),
                                    "Failed to load habits", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        queue.add(request);
    }

    public void showAddHabitDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_habit, null);

        final TextInputEditText etTitle = (TextInputEditText) dialogView.findViewById(R.id.etHabitTitle);
        final TextInputEditText etDescription = (TextInputEditText) dialogView.findViewById(R.id.etHabitDescription);
        final TextInputEditText etCategory = (TextInputEditText) dialogView.findViewById(R.id.etHabitCategory);
        final RadioGroup rgFrequency = (RadioGroup) dialogView.findViewById(R.id.rgFrequency);

        new AlertDialog.Builder(requireContext())
                .setTitle("New Habit")
                .setView(dialogView)
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String title = etTitle.getText().toString().trim();
                        if (title.isEmpty()) {
                            Toast.makeText(requireContext(),
                                    "Title is required", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        final String description = etDescription.getText().toString().trim();
                        final String category = etCategory.getText().toString().trim();
                        final String frequency = rgFrequency.getCheckedRadioButtonId() == R.id.rbWeekly
                                ? "WEEKLY" : "DAILY";

                        String url = BASE_URL + "addhabit.php";

                        StringRequest request = new StringRequest(Request.Method.POST, url,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        if (isAdded()) {
                                            Toast.makeText(requireContext(),
                                                    "Habit created!", Toast.LENGTH_SHORT).show();
                                            loadHabits();
                                        }
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        if (isAdded()) {
                                            Toast.makeText(requireContext(),
                                                    "Network error", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }) {
                            @Override
                            protected Map<String, String> getParams() {
                                Map<String, String> params = new HashMap<>();
                                params.put("user_id", String.valueOf(sessionManager.getUserId()));
                                params.put("title", title);
                                params.put("description", description);
                                params.put("category", category);
                                params.put("frequency", frequency);
                                return params;
                            }
                        };

                        queue.add(request);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDelete(int habitId, String title) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Habit")
                .setMessage("Remove \"" + title + "\"?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String url = BASE_URL + "deletehabit.php";

                        StringRequest request = new StringRequest(Request.Method.POST, url,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        if (isAdded()) loadHabits();
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        if (isAdded()) {
                                            Toast.makeText(requireContext(),
                                                    "Failed to delete", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }) {
                            @Override
                            protected Map<String, String> getParams() {
                                Map<String, String> params = new HashMap<>();
                                params.put("habit_id", String.valueOf(habitId));
                                params.put("user_id", String.valueOf(sessionManager.getUserId()));
                                return params;
                            }
                        };

                        queue.add(request);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
