package com.flixpro.app.ui.search;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.flixpro.app.R;
import com.flixpro.app.api.ApiClient;
import com.flixpro.app.api.Parser;
import com.flixpro.app.model.ContentItem;
import com.flixpro.app.ui.adapter.GridContentAdapter;
import com.flixpro.app.ui.details.DetailsLauncher;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {
    private EditText input;
    private ProgressBar progress;
    private GridContentAdapter adapter;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable pending;
    private int generation = 0;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        View root = inflater.inflate(R.layout.fragment_search, container, false);
        input = root.findViewById(R.id.searchInput);
        progress = root.findViewById(R.id.progress);
        RecyclerView results = root.findViewById(R.id.results);
        results.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        adapter = new GridContentAdapter(item -> DetailsLauncher.open(requireContext(), item));
        results.setAdapter(adapter);

        input.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                scheduleSearch(input.getText().toString(), 0);
                return true;
            }
            return false;
        });
        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int before, int count) {
                scheduleSearch(s == null ? "" : s.toString(), 450);
            }
            @Override public void afterTextChanged(Editable e) {}
        });
        return root;
    }

    private void scheduleSearch(String query, long delay) {
        if (pending != null) handler.removeCallbacks(pending);
        String q = query == null ? "" : query.trim();
        if (q.length() < 2) {
            generation++;
            progress.setVisibility(View.GONE);
            adapter.submit(new ArrayList<>());
            return;
        }
        pending = () -> search(q);
        handler.postDelayed(pending, delay);
    }

    private void search(String query) {
        final int requestId = ++generation;
        progress.setVisibility(View.VISIBLE);
        JSONObject payload = new JSONObject();
        try { payload.put("search_text", query); } catch (Exception ignored) {}
        ApiClient.post("search", payload, new ApiClient.Callback() {
            @Override public void onSuccess(JSONObject root) {
                List<ContentItem> data = Parser.search(root);
                if (!isAdded() || requestId != generation) return;
                requireActivity().runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    adapter.submit(data);
                });
            }
            @Override public void onError(Exception error) {
                if (!isAdded() || requestId != generation) return;
                requireActivity().runOnUiThread(() -> progress.setVisibility(View.GONE));
            }
        });
    }

    @Override public void onDestroyView() {
        if (pending != null) handler.removeCallbacks(pending);
        super.onDestroyView();
    }
}
