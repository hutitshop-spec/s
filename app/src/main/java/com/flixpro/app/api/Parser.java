package com.flixpro.app.api;

import com.flixpro.app.model.ContentItem;
import com.flixpro.app.model.Episode;
import com.flixpro.app.model.MediaDetails;
import com.flixpro.app.model.Season;
import com.flixpro.app.model.Section;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class Parser {
    private Parser() {}

    public static List<ContentItem> sliders(JSONObject app) {
        List<ContentItem> out = new ArrayList<>();
        JSONArray a = app == null ? null : app.optJSONArray("slider");
        if (a == null) return out;
        for (int i = 0; i < a.length(); i++) {
            JSONObject o = a.optJSONObject(i); if (o == null) continue;
            out.add(new ContentItem(
                    o.optString("slider_post_id"),
                    o.optString("slider_title"),
                    ApiClient.normalizeImage(o.optString("slider_image")),
                    normalizeType(o.optString("slider_type")),
                    "Paid".equalsIgnoreCase(o.optString("video_access"))));
        }
        return out;
    }

    public static List<Section> homeSections(JSONObject app) {
        List<Section> sections = new ArrayList<>();
        if (app == null) return sections;
        addSpecialSection(sections, "Recently Watched", app.optJSONArray("recently_watched"), "recent");
        addSpecialSection(sections, "Upcoming Movies", app.optJSONArray("upcoming_movies"), "movie");
        addSpecialSection(sections, "Upcoming Series", app.optJSONArray("upcoming_series"), "show");

        JSONArray hs = app.optJSONArray("home_sections");
        if (hs != null) {
            for (int i = 0; i < hs.length(); i++) {
                JSONObject h = hs.optJSONObject(i); if (h == null) continue;
                Section s = new Section(h.optString("home_title", "Featured"));
                String homeType = normalizeType(h.optString("home_type"));
                JSONArray c = h.optJSONArray("home_content");
                if (c == null) continue;
                for (int j = 0; j < c.length(); j++) {
                    JSONObject o = c.optJSONObject(j); if (o == null) continue;
                    String type = detectType(o, homeType);
                    s.items.add(new ContentItem(
                            first(o, "video_id", "movie_id", "show_id", "sport_id", "tv_id"),
                            first(o, "video_title", "movie_title", "show_title", "sport_title", "tv_title"),
                            ApiClient.normalizeImage(first(o, "video_image", "movie_poster", "show_poster", "sport_image", "sport_poster", "tv_logo", "tv_image", "video_thumb_image")),
                            type,
                            "Paid".equalsIgnoreCase(first(o, "video_access", "movie_access", "show_access", "sport_access", "tv_access"))));
                }
                if (!s.items.isEmpty()) sections.add(s);
            }
        }
        return sections;
    }

    private static void addSpecialSection(List<Section> sections, String title, JSONArray a, String typeHint) {
        if (a == null || a.length() == 0) return;
        Section s = new Section(title);
        for (int i = 0; i < a.length(); i++) {
            JSONObject o = a.optJSONObject(i); if (o == null) continue;
            String fallback = typeHint.equals("recent") ? normalizeType(o.optString("video_type")) : typeHint;
            String type = detectType(o, fallback);
            s.items.add(new ContentItem(
                    first(o, "video_id", "movie_id", "show_id", "sport_id", "tv_id"),
                    first(o, "video_title", "movie_title", "show_title", "sport_title", "tv_title", "episode_title"),
                    ApiClient.normalizeImage(first(o, "video_thumb_image", "movie_poster", "show_poster", "sport_image", "sport_poster", "tv_logo", "tv_image", "video_image")),
                    type,
                    "Paid".equalsIgnoreCase(first(o, "video_access", "movie_access", "show_access", "sport_access", "tv_access"))));
        }
        if (!s.items.isEmpty()) sections.add(s);
    }

    public static List<ContentItem> catalog(JSONObject root, String type) {
        List<ContentItem> out = new ArrayList<>();
        JSONArray a = root.optJSONArray("VIDEO_STREAMING_APP");
        if (a == null) return out;
        for (int i = 0; i < a.length(); i++) {
            JSONObject o = a.optJSONObject(i); if (o == null) continue;
            boolean show = "show".equals(type);
            out.add(new ContentItem(
                    o.optString(show ? "show_id" : "movie_id"),
                    o.optString(show ? "show_title" : "movie_title"),
                    ApiClient.normalizeImage(o.optString(show ? "show_poster" : "movie_poster")),
                    type,
                    "Paid".equalsIgnoreCase(o.optString(show ? "show_access" : "movie_access"))));
        }
        return out;
    }

    public static List<ContentItem> search(JSONObject root) {
        List<ContentItem> out = new ArrayList<>();
        JSONObject app = root.optJSONObject("VIDEO_STREAMING_APP");
        if (app == null) return out;
        JSONArray movies = app.optJSONArray("movies");
        if (movies != null) for (int i = 0; i < movies.length(); i++) {
            JSONObject o = movies.optJSONObject(i); if (o == null) continue;
            out.add(new ContentItem(o.optString("movie_id"), o.optString("movie_title"), ApiClient.normalizeImage(o.optString("movie_poster")), "movie", "Paid".equalsIgnoreCase(o.optString("movie_access"))));
        }
        JSONArray shows = app.optJSONArray("shows");
        if (shows != null) for (int i = 0; i < shows.length(); i++) {
            JSONObject o = shows.optJSONObject(i); if (o == null) continue;
            out.add(new ContentItem(o.optString("show_id"), o.optString("show_title"), ApiClient.normalizeImage(o.optString("show_poster")), "show", "Paid".equalsIgnoreCase(o.optString("show_access"))));
        }
        JSONArray sports = app.optJSONArray("sports");
        if (sports != null) for (int i = 0; i < sports.length(); i++) {
            JSONObject o = sports.optJSONObject(i); if (o == null) continue;
            out.add(new ContentItem(o.optString("sport_id"), o.optString("sport_title"), ApiClient.normalizeImage(first(o, "sport_image", "sport_poster")), "sport", "Paid".equalsIgnoreCase(o.optString("sport_access"))));
        }
        JSONArray tv = app.optJSONArray("live_tv");
        if (tv != null) for (int i = 0; i < tv.length(); i++) {
            JSONObject o = tv.optJSONObject(i); if (o == null) continue;
            out.add(new ContentItem(o.optString("tv_id"), o.optString("tv_title"), ApiClient.normalizeImage(first(o, "tv_logo", "tv_image")), "tv", "Paid".equalsIgnoreCase(o.optString("tv_access"))));
        }
        return out;
    }

    public static MediaDetails details(JSONObject root, String type) {
        JSONObject app = root.optJSONObject("VIDEO_STREAMING_APP");
        if (app == null) return null;
        MediaDetails d = new MediaDetails();
        d.type = type;
        if ("show".equals(type)) {
            d.id = app.optString("show_id");
            d.title = first(app, "show_name", "show_title");
            d.image = ApiClient.normalizeImage(first(app, "show_poster", "show_image"));
            d.description = first(app, "show_info", "description");
            d.language = first(app, "show_lang", "language_name");
            d.rating = app.optString("imdb_rating");
            d.contentRating = app.optString("content_rating");
            d.upcoming = boolish(app, "upcoming");
            JSONArray seasons = app.optJSONArray("season_list");
            if (seasons != null) for (int i = 0; i < seasons.length(); i++) {
                JSONObject o = seasons.optJSONObject(i); if (o == null) continue;
                Season season = new Season();
                season.id = o.optString("season_id");
                season.name = o.optString("season_name");
                season.poster = ApiClient.normalizeImage(o.optString("season_poster"));
                season.trailer = safeNull(o.optString("trailer_url"));
                d.seasons.add(season);
            }
            addRelated(d.related, app.optJSONArray("related_shows"), "show");
        } else if ("sport".equals(type)) {
            d.id = first(app, "sport_id", "sports_id");
            d.title = first(app, "sport_title", "sports_title", "title");
            d.image = ApiClient.normalizeImage(first(app, "sport_image", "sport_poster", "sports_image"));
            d.description = first(app, "description", "sport_info", "sports_info");
            d.language = first(app, "language_name", "sport_lang");
            d.rating = app.optString("imdb_rating");
            d.contentRating = app.optString("content_rating");
            d.duration = first(app, "sport_duration", "duration");
            d.releaseDate = first(app, "release_date", "date");
            d.videoUrl = safeNull(first(app, "video_url", "sport_url"));
            d.videoType = first(app, "video_type", "sport_url_type");
            d.quality480 = safeNull(app.optString("video_url_480"));
            d.quality720 = safeNull(app.optString("video_url_720"));
            d.quality1080 = safeNull(app.optString("video_url_1080"));
            d.premium = "Paid".equalsIgnoreCase(first(app, "sport_access", "video_access"));
        } else if ("tv".equals(type)) {
            d.id = first(app, "tv_id", "live_tv_id");
            d.title = first(app, "tv_title", "live_tv_title", "title");
            d.image = ApiClient.normalizeImage(first(app, "tv_logo", "tv_image", "live_tv_image"));
            d.description = first(app, "description", "tv_info");
            d.language = first(app, "language_name", "tv_lang");
            d.videoUrl = safeNull(first(app, "tv_url", "video_url", "stream_url"));
            d.videoType = first(app, "tv_url_type", "video_type");
            d.premium = "Paid".equalsIgnoreCase(first(app, "tv_access", "video_access"));
        } else {
            d.type = "movie";
            d.id = app.optString("movie_id");
            d.title = app.optString("movie_title");
            d.image = ApiClient.normalizeImage(first(app, "movie_image", "movie_poster"));
            d.description = app.optString("description");
            d.language = app.optString("language_name");
            d.rating = app.optString("imdb_rating");
            d.contentRating = app.optString("content_rating");
            d.duration = app.optString("movie_duration");
            d.releaseDate = app.optString("release_date");
            d.videoUrl = safeNull(app.optString("video_url"));
            d.videoType = app.optString("video_type");
            d.quality480 = safeNull(app.optString("video_url_480"));
            d.quality720 = safeNull(app.optString("video_url_720"));
            d.quality1080 = safeNull(app.optString("video_url_1080"));
            d.premium = "Paid".equalsIgnoreCase(app.optString("movie_access"));
            d.upcoming = boolish(app, "upcoming");
            addRelated(d.related, app.optJSONArray("related_movies"), "movie");
        }
        return d;
    }

    private static boolean boolish(JSONObject o, String key) {
        Object value = o.opt(key);
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof Number) return ((Number) value).intValue() != 0;
        String s = safeNull(String.valueOf(value));
        return "1".equals(s) || "true".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s);
    }

    private static void addRelated(List<ContentItem> out, JSONArray a, String type) {
        if (a == null) return;
        boolean show = "show".equals(type);
        for (int i = 0; i < a.length(); i++) {
            JSONObject o = a.optJSONObject(i); if (o == null) continue;
            out.add(new ContentItem(
                    first(o, show ? "show_id" : "movie_id", "video_id"),
                    first(o, show ? "show_title" : "movie_title", "video_title"),
                    ApiClient.normalizeImage(first(o, show ? "show_poster" : "movie_poster", "video_image")),
                    type,
                    "Paid".equalsIgnoreCase(first(o, show ? "show_access" : "movie_access", "video_access"))));
        }
    }

    public static List<Episode> episodes(JSONObject root) {
        List<Episode> out = new ArrayList<>();
        JSONArray a = root.optJSONArray("VIDEO_STREAMING_APP");
        if (a == null) return out;
        for (int i = 0; i < a.length(); i++) {
            JSONObject o = a.optJSONObject(i); if (o == null) continue;
            Episode e = new Episode();
            e.id = o.optString("episode_id");
            e.title = o.optString("episode_title");
            e.image = ApiClient.normalizeImage(o.optString("episode_image"));
            e.url = safeNull(o.optString("video_url"));
            e.type = o.optString("video_type");
            e.duration = o.optString("duration");
            e.date = o.optString("release_date");
            e.description = o.optString("description");
            e.q480 = safeNull(o.optString("video_url_480"));
            e.q720 = safeNull(o.optString("video_url_720"));
            e.q1080 = safeNull(o.optString("video_url_1080"));
            e.premium = "Paid".equalsIgnoreCase(o.optString("video_access"));
            out.add(e);
        }
        return out;
    }

    public static String bestVideo(String original, String q480, String q720, String q1080) {
        if (!safeNull(q1080).isEmpty()) return q1080;
        if (!safeNull(q720).isEmpty()) return q720;
        if (!safeNull(q480).isEmpty()) return q480;
        return safeNull(original);
    }

    private static String first(JSONObject o, String... keys) {
        for (String key : keys) {
            String v = safeNull(o.optString(key));
            if (!v.isEmpty()) return v;
        }
        return "";
    }

    private static String safeNull(String s) {
        if (s == null || "null".equalsIgnoreCase(s)) return "";
        return s.trim();
    }



    private static String detectType(JSONObject o, String fallback) {
        if (!safeNull(o.optString("show_id")).isEmpty()) return "show";
        if (!safeNull(o.optString("movie_id")).isEmpty()) return "movie";
        if (!safeNull(o.optString("sport_id")).isEmpty()) return "sport";
        if (!safeNull(o.optString("tv_id")).isEmpty() || !safeNull(o.optString("live_tv_id")).isEmpty()) return "tv";
        String f = normalizeType(fallback);
        if (isContentType(f)) return f;
        return normalizeType(o.optString("video_type"));
    }

    private static boolean isContentType(String type) {
        return "movie".equals(type) || "show".equals(type) || "sport".equals(type) || "tv".equals(type);
    }

    private static String normalizeType(String type) {
        if (type == null) return "movie";
        String t = type.toLowerCase(Locale.US);
        if (t.contains("show") || t.contains("series")) return "show";
        if (t.contains("movie") || t.contains("film")) return "movie";
        if (t.contains("sport")) return "sport";
        if (t.contains("live") || t.equals("tv") || t.contains("television")) return "tv";
        return t.isEmpty() ? "movie" : t;
    }
}
