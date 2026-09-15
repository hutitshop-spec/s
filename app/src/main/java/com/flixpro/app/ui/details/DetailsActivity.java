package com.flixpro.app.ui.details;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.flixpro.app.R;
import com.flixpro.app.api.ApiClient;
import com.flixpro.app.api.Parser;
import com.flixpro.app.model.Episode;
import com.flixpro.app.model.MediaDetails;
import com.flixpro.app.model.Season;
import com.flixpro.app.ui.PinGate;
import com.flixpro.app.ui.adapter.ContentAdapter;
import com.flixpro.app.ui.adapter.EpisodeAdapter;
import com.flixpro.app.ui.player.PlayerActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DetailsActivity extends AppCompatActivity {
    private String id = "";
    private String type = "movie";
    private MediaDetails details;
    private ProgressBar progress;
    private TextView title;
    private TextView meta;
    private TextView description;
    private TextView episodesTitle;
    private TextView relatedTitle;
    private android.widget.ImageView backdrop;
    private MaterialButton play;
    private ChipGroup seasons;
    private RecyclerView episodesList;
    private RecyclerView relatedList;
    private EpisodeAdapter episodeAdapter;
    private ContentAdapter relatedAdapter;
    private List<Episode> currentEpisodes = new ArrayList<>();

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_details);

        id = safe(getIntent().getStringExtra("id"));
        type = safe(getIntent().getStringExtra("type"));
        if (type.isEmpty()) type = "movie";

        progress = findViewById(R.id.progress);
        title = findViewById(R.id.title);
        meta = findViewById(R.id.meta);
        description = findViewById(R.id.description);
        episodesTitle = findViewById(R.id.episodesTitle);
        relatedTitle = findViewById(R.id.relatedTitle);
        backdrop = findViewById(R.id.backdrop);
        play = findViewById(R.id.play);
        seasons = findViewById(R.id.seasons);
        episodesList = findViewById(R.id.episodes);
        relatedList = findViewById(R.id.related);

        ImageButton back = findViewById(R.id.back);
        back.setOnClickListener(v -> finish());

        title.setText(safe(getIntent().getStringExtra("title")));
        String image = safe(getIntent().getStringExtra("image"));
        if (!image.isEmpty()) Glide.with(this).load(image).centerCrop().placeholder(R.drawable.ic_flix_pro).into(backdrop);

        episodeAdapter = new EpisodeAdapter(this::playEpisode);
        episodesList.setLayoutManager(new LinearLayoutManager(this));
        episodesList.setAdapter(episodeAdapter);
        episodesList.setNestedScrollingEnabled(false);

        relatedAdapter = new ContentAdapter(item -> DetailsLauncher.open(this, item));
        relatedList.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        relatedList.setAdapter(relatedAdapter);
        relatedList.setNestedScrollingEnabled(false);

        play.setEnabled(false);
        play.setOnClickListener(v -> playPrimary());
        loadDetails();
    }

    private void loadDetails() {
        progress.setVisibility(View.VISIBLE);
        JSONObject payload = new JSONObject();
        String endpoint;
        try {
            if ("show".equals(type)) {
                endpoint = "show_details";
                payload.put("show_id", id);
            } else if ("sport".equals(type)) {
                endpoint = "sports_details";
                payload.put("sport_id", id);
                payload.put("user_id", "");
            } else if ("tv".equals(type)) {
                endpoint = "livetv_details";
                payload.put("tv_id", id);
                payload.put("user_id", "");
            } else {
                type = "movie";
                endpoint = "movies_details";
                payload.put("movie_id", id);
                payload.put("user_id", "");
            }
        } catch (Exception e) {
            endpoint = "movies_details";
        }

        ApiClient.post(endpoint, payload, new ApiClient.Callback() {
            @Override public void onSuccess(JSONObject root) {
                MediaDetails parsed = Parser.details(root, type);
                runOnUiThread(() -> bind(parsed));
            }
            @Override public void onError(Exception error) {
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    Toast.makeText(DetailsActivity.this, R.string.something_wrong, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void bind(MediaDetails d) {
        progress.setVisibility(View.GONE);
        if (d == null) {
            Toast.makeText(this, R.string.no_content, Toast.LENGTH_LONG).show();
            return;
        }
        details = d;
        if (!safe(d.title).isEmpty()) title.setText(d.title);
        if (!safe(d.image).isEmpty()) Glide.with(this).load(d.image).centerCrop().placeholder(R.drawable.ic_flix_pro).into(backdrop);
        description.setText(safe(d.description).isEmpty() ? getString(R.string.no_description) : d.description);
        meta.setText(buildMeta(d));

        relatedAdapter.submit(d.related);
        relatedTitle.setVisibility(d.related.isEmpty() ? View.GONE : View.VISIBLE);
        relatedList.setVisibility(d.related.isEmpty() ? View.GONE : View.VISIBLE);

        if ("show".equals(type)) {
            bindShow(d);
        } else {
            episodesTitle.setVisibility(View.GONE);
            seasons.setVisibility(View.GONE);
            episodesList.setVisibility(View.GONE);
            play.setText(d.upcoming ? R.string.coming_soon : R.string.play);
            play.setEnabled(!d.upcoming && !Parser.bestVideo(d.videoUrl, d.quality480, d.quality720, d.quality1080).isEmpty());
        }
    }

    private void bindShow(MediaDetails d) {
        play.setText(R.string.play_first_episode);
        play.setEnabled(false);
        episodesTitle.setVisibility(View.VISIBLE);
        seasons.setVisibility(View.VISIBLE);
        episodesList.setVisibility(View.VISIBLE);
        seasons.removeAllViews();

        if (d.seasons.isEmpty()) {
            episodesTitle.setText(R.string.no_episodes);
            return;
        }
        for (int i = 0; i < d.seasons.size(); i++) {
            Season season = d.seasons.get(i);
            Chip chip = new Chip(this);
            chip.setId(View.generateViewId());
            chip.setText(safe(season.name).isEmpty() ? getString(R.string.season_number, i + 1) : season.name);
            chip.setCheckable(true);
            chip.setChecked(i == 0);
            final int index = i;
            chip.setOnClickListener(v -> loadEpisodes(d.seasons.get(index)));
            seasons.addView(chip);
        }
        loadEpisodes(d.seasons.get(0));
    }

    private void loadEpisodes(Season season) {
        if (season == null || safe(season.id).isEmpty()) return;
        JSONObject payload = new JSONObject();
        try {
            payload.put("season_id", season.id);
            payload.put("user_id", "");
        } catch (Exception ignored) {}
        ApiClient.post("episodes", payload, new ApiClient.Callback() {
            @Override public void onSuccess(JSONObject root) {
                List<Episode> data = Parser.episodes(root);
                runOnUiThread(() -> {
                    currentEpisodes = data == null ? new ArrayList<>() : data;
                    episodeAdapter.submit(currentEpisodes);
                    play.setEnabled(!currentEpisodes.isEmpty());
                    episodesTitle.setText(currentEpisodes.isEmpty() ? R.string.no_episodes : R.string.episodes);
                });
            }
            @Override public void onError(Exception error) {
                runOnUiThread(() -> {
                    currentEpisodes = new ArrayList<>();
                    episodeAdapter.submit(currentEpisodes);
                    play.setEnabled(false);
                });
            }
        });
    }

    private void playPrimary() {
        if (details == null) return;
        if ("show".equals(type)) {
            if (!currentEpisodes.isEmpty()) playEpisode(currentEpisodes.get(0));
            return;
        }
        String url = Parser.bestVideo(details.videoUrl, details.quality480, details.quality720, details.quality1080);
        if (url.isEmpty()) {
            Toast.makeText(this, R.string.video_unavailable, Toast.LENGTH_SHORT).show();
            return;
        }
        PinGate.require(this, () -> PlayerActivity.open(this, url, details.title));
    }

    private void playEpisode(Episode e) {
        if (e == null) return;
        String url = Parser.bestVideo(e.url, e.q480, e.q720, e.q1080);
        if (url.isEmpty()) {
            Toast.makeText(this, R.string.video_unavailable, Toast.LENGTH_SHORT).show();
            return;
        }
        PinGate.require(this, () -> PlayerActivity.open(this, url, e.title));
    }

    private String buildMeta(MediaDetails d) {
        List<String> parts = new ArrayList<>();
        addPart(parts, d.language);
        if (!safe(d.rating).isEmpty()) addPart(parts, "IMDb " + d.rating);
        addPart(parts, d.contentRating);
        addPart(parts, d.duration);
        addPart(parts, d.releaseDate);
        if (d.premium) addPart(parts, "PRO");
        if (d.upcoming) addPart(parts, getString(R.string.coming_soon));
        return android.text.TextUtils.join("  •  ", parts);
    }

    private void addPart(List<String> parts, String value) {
        String v = safe(value);
        if (!v.isEmpty() && !"0".equals(v) && !"null".equalsIgnoreCase(v)) parts.add(v);
    }

    private static String safe(String s) { return s == null ? "" : s.trim(); }

    @Override protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("id", id);
        outState.putString("type", type);
        super.onSaveInstanceState(outState);
    }
}
