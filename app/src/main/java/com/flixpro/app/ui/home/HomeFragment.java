package com.flixpro.app.ui.home;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.flixpro.app.R;
import com.flixpro.app.api.ApiClient;
import com.flixpro.app.api.Parser;
import com.flixpro.app.model.ContentItem;
import com.flixpro.app.model.Section;
import com.flixpro.app.ui.adapter.ContentAdapter;
import com.flixpro.app.ui.adapter.HeroAdapter;
import com.flixpro.app.ui.details.DetailsLauncher;

import org.json.JSONObject;

import java.util.List;

public class HomeFragment extends Fragment {
    private SwipeRefreshLayout swipe;
    private ViewPager2 heroPager;
    private LinearLayout sectionContainer;
    private HeroAdapter heroAdapter;
    private final Handler heroHandler = new Handler(Looper.getMainLooper());
    private final Runnable heroAdvance = new Runnable() {
        @Override public void run() {
            if (heroPager != null && heroAdapter != null && heroAdapter.getItemCount() > 1) {
                int next = (heroPager.getCurrentItem() + 1) % heroAdapter.getItemCount();
                heroPager.setCurrentItem(next, true);
                heroHandler.postDelayed(this, 5500);
            }
        }
    };

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        swipe = root.findViewById(R.id.swipe);
        heroPager = root.findViewById(R.id.heroPager);
        sectionContainer = root.findViewById(R.id.sectionContainer);

        heroAdapter = new HeroAdapter(item -> DetailsLauncher.open(requireContext(), item));
        heroPager.setAdapter(heroAdapter);
        heroPager.setOffscreenPageLimit(2);
        heroPager.setPageTransformer((page, position) -> {
            float distance = Math.min(1f, Math.abs(position));
            float scale = 1f - distance * 0.06f;
            page.setScaleY(scale);
            page.setAlpha(0.78f + (1f - distance) * 0.22f);
        });
        heroPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override public void onPageSelected(int position) {
                heroHandler.removeCallbacks(heroAdvance);
                heroHandler.postDelayed(heroAdvance, 5500);
            }
        });

        swipe.setColorSchemeResources(R.color.accent);
        swipe.setOnRefreshListener(this::loadHome);
        swipe.post(() -> {
            swipe.setRefreshing(true);
            loadHome();
        });
        return root;
    }

    private void loadHome() {
        JSONObject payload = new JSONObject();
        try { payload.put("user_id", ""); } catch (Exception ignored) {}
        ApiClient.post("home", payload, new ApiClient.Callback() {
            @Override public void onSuccess(JSONObject root) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> bindHome(root));
            }
            @Override public void onError(Exception error) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    swipe.setRefreshing(false);
                    Toast.makeText(requireContext(), R.string.something_wrong, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void bindHome(JSONObject root) {
        swipe.setRefreshing(false);
        JSONObject app = ApiClient.appObject(root);
        List<ContentItem> sliders = Parser.sliders(app);
        heroAdapter.submit(sliders);
        heroPager.setVisibility(sliders.isEmpty() ? View.GONE : View.VISIBLE);
        heroHandler.removeCallbacks(heroAdvance);
        if (sliders.size() > 1) heroHandler.postDelayed(heroAdvance, 5500);

        sectionContainer.removeAllViews();
        List<Section> sections = Parser.homeSections(app);
        for (Section section : sections) addSection(section);
        if (sections.isEmpty() && sliders.isEmpty()) {
            TextView empty = new TextView(requireContext());
            empty.setText(R.string.no_content);
            empty.setTextColor(getResources().getColor(R.color.text_secondary, requireContext().getTheme()));
            empty.setTextSize(15);
            int p = dp(24);
            empty.setPadding(p, p, p, p);
            sectionContainer.addView(empty);
        }
    }

    private void addSection(Section section) {
        View block = getLayoutInflater().inflate(R.layout.view_section, sectionContainer, false);
        TextView title = block.findViewById(R.id.sectionTitle);
        RecyclerView list = block.findViewById(R.id.sectionList);
        title.setText(section.title == null || section.title.trim().isEmpty() ? "Featured" : section.title);
        list.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
        list.setHasFixedSize(true);
        ContentAdapter adapter = new ContentAdapter(item -> DetailsLauncher.open(requireContext(), item));
        adapter.submit(section.items);
        list.setAdapter(adapter);
        sectionContainer.addView(block);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    @Override public void onResume() {
        super.onResume();
        heroHandler.removeCallbacks(heroAdvance);
        heroHandler.postDelayed(heroAdvance, 5500);
    }

    @Override public void onPause() {
        heroHandler.removeCallbacks(heroAdvance);
        super.onPause();
    }

    @Override public void onDestroyView() {
        heroHandler.removeCallbacksAndMessages(null);
        heroPager = null;
        sectionContainer = null;
        swipe = null;
        super.onDestroyView();
    }
}
