package com.flixpro.app.ui.player;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.flixpro.app.R;
import com.google.android.material.snackbar.Snackbar;

import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import java.util.Locale;

public class PlayerActivity extends AppCompatActivity {
    private PlayerView playerView;
    private ExoPlayer player;
    private String url = "";
    private long position;

    public static void open(android.content.Context context, String url, String title) {
        Intent i = new Intent(context, PlayerActivity.class);
        i.putExtra("url", url == null ? "" : url);
        i.putExtra("title", title == null ? "" : title);
        context.startActivity(i);
    }

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_player);
        playerView = findViewById(R.id.playerView);
        findViewById(R.id.back).setOnClickListener(v -> finish());
        url = getIntent().getStringExtra("url");
        if (url == null) url = "";
        if (url.trim().isEmpty()) {
            Toast.makeText(this, R.string.video_unavailable, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (isExternalProvider(url)) {
            try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); }
            catch (Exception e) { Toast.makeText(this, R.string.video_unavailable, Toast.LENGTH_LONG).show(); }
            finish();
            return;
        }
        if (state != null) position = state.getLong("position", 0);
        hideSystemUi();
    }

    private boolean isExternalProvider(String value) {
        String u = value.toLowerCase(Locale.US);
        return u.contains("youtube.com") || u.contains("youtu.be") || u.contains("vimeo.com");
    }

    private void initializePlayer() {
        if (player != null || url.trim().isEmpty()) return;
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        player.addListener(new Player.Listener() {
            @Override public void onPlayerError(@NonNull PlaybackException error) {
                Snackbar.make(playerView, R.string.playback_error, Snackbar.LENGTH_LONG).show();
            }
        });
        player.setMediaItem(MediaItem.fromUri(url));
        player.prepare();
        player.seekTo(position);
        player.setPlayWhenReady(true);
    }

    private void releasePlayer() {
        if (player != null) {
            position = player.getCurrentPosition();
            player.release();
            player = null;
        }
    }

    private void hideSystemUi() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    @Override protected void onStart() { super.onStart(); initializePlayer(); }
    @Override protected void onStop() { releasePlayer(); super.onStop(); }
    @Override protected void onSaveInstanceState(@NonNull Bundle out) {
        out.putLong("position", player == null ? position : player.getCurrentPosition());
        super.onSaveInstanceState(out);
    }
}
