package com.flixpro.app.ui.catalog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.flixpro.app.R;
import com.flixpro.app.api.ApiClient;
import com.flixpro.app.api.Parser;
import com.flixpro.app.model.ContentItem;
import com.flixpro.app.model.Section;
import com.flixpro.app.ui.adapter.GridContentAdapter;
import com.flixpro.app.ui.details.DetailsLauncher;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CatalogFragment extends Fragment {
    private static final String ARG_TYPE = "type";
    private String type = "movie";
    private SwipeRefreshLayout swipe;
    private ProgressBar progress;
    private TextView empty;
    private GridContentAdapter adapter;

    public static CatalogFragment newInstance(String type) {
        CatalogFragment f = new CatalogFragment();
        Bundle b = new Bundle();
        b.putString(ARG_TYPE, type);
        f.setArguments(b);
        return f;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        View root = inflater.inflate(R.layout.fragment_catalog, container, false);
        if (getArguments() != null) type = getArguments().getString(ARG_TYPE, "movie");
        swipe = root.findViewById(R.id.swipe);
        progress = root.findViewById(R.id.progress);
        empty = root.findViewById(R.id.empty);
        RecyclerView grid = root.findViewById(R.id.grid);
        grid.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        adapter = new GridContentAdapter(item -> DetailsLauncher.open(requireContext(), item));
        grid.setAdapter(adapter);
        swipe.setColorSchemeResources(R.color.accent);
        swipe.setOnRefreshListener(this::loadCatalog);
        loadCatalog();
        return root;
    }

    private void loadCatalog() {
        progress.setVisibility(View.VISIBLE);
        empty.setVisibility(View.GONE);
        JSONObject payload = new JSONObject();
        try {
            payload.put("genre_id", "0");
            payload.put("filter", "new");
            payload.put("user_id", "");
        } catch (Exception ignored) {}
        String endpoint = "show".equals(type) ? "shows_by_genre" : "movies_by_genre";
        ApiClient.post(endpoint, payload, 1, new ApiClient.Callback() {
            @Override public void onSuccess(JSONObject root) {
                List<ContentItem> items = Parser.catalog(root, type);
                if (items.isEmpty()) loadFromHome();
                else show(items);
            }
            @Override public void onError(Exception error) { loadFromHome(); }
        });
    }

    private void loadFromHome() {
        JSONObject payload = new JSONObject();
        try { payload.put("user_id", ""); } catch (Exception ignored) {}
        ApiClient.post("home", payload, new ApiClient.Callback() {
            @Override public void onSuccess(JSONObject root) {
                List<ContentItem> items = new ArrayList<>();
                Set<String> seen = new HashSet<>();
                JSONObject app = ApiClient.appObject(root);
                for (ContentItem item : Parser.sliders(app)) addIfType(items, seen, item);
                for (Section section : Parser.homeSections(app)) {
                    for (ContentItem item : section.items) addIfType(items, seen, item);
                }
                show(items);
            }
            @Override public void onError(Exception error) { show(new ArrayList<>()); }
        });
    }

    private void addIfType(List<ContentItem> items, Set<String> seen, ContentItem item) {
        if (item == null || !type.equals(item.type)) return;
        String key = item.type + ":" + item.id;
        if (seen.add(key)) items.add(item);
    }

    private void show(List<ContentItem> items) {
        if (!isAdded()) return;
        requireActivity().runOnUiThread(() -> {
            progress.setVisibility(View.GONE);
            swipe.setRefreshing(false);
            adapter.submit(items);
            empty.setVisibility(items == null || items.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }
}
