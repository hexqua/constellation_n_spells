package jp.aquafactory.constellationandspells.spell.stargazing;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.util.GsonHelper;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public record StargazingChart(List<Star> stars, List<Edge> edges) {
    // リソースパックのファイル列挙順や追加ファイルで、同じ seed の選択結果を変えない。
    public static final List<String> IDS = List.of("lyre", "crown", "serpent", "kite", "compass", "phoenix");

    public StargazingChart {
        stars = List.copyOf(stars);
        edges = List.copyOf(edges);
    }

    public static String idForSeed(long seed) { return IDS.get(Math.floorMod(Long.hashCode(seed), IDS.size())); }

    public static StargazingChart parse(JsonElement json) {
        var root = GsonHelper.convertToJsonObject(json, "chart");
        var starJson = GsonHelper.getAsJsonArray(root, "stars");
        var edgeJson = GsonHelper.getAsJsonArray(root, "edges");
        if (starJson.size() < 3 || starJson.size() > 64 || edgeJson.isEmpty() || edgeJson.size() > 128) {
            throw new JsonParseException("Chart must contain 3 to 64 stars and 1 to 128 edges");
        }
        var stars = new ArrayList<Star>();
        for (var element : starJson) {
            var star = GsonHelper.convertToJsonObject(element, "star");
            float x = GsonHelper.getAsFloat(star, "x"), z = GsonHelper.getAsFloat(star, "z");
            float size = GsonHelper.getAsFloat(star, "size", 1), brightness = GsonHelper.getAsFloat(star, "brightness", 1);
            if (!Float.isFinite(x) || !Float.isFinite(z) || Math.abs(x) > 0.9f || Math.abs(z) > 0.9f
                    || !Float.isFinite(size) || size < 0.5f || size > 2 || !Float.isFinite(brightness) || brightness <= 0 || brightness > 1.5f) {
                throw new JsonParseException("Star coordinates or appearance are outside chart bounds");
            }
            stars.add(new Star(x, z, size, brightness));
        }
        var edges = new ArrayList<Edge>();
        var unique = new HashSet<Integer>();
        for (var element : edgeJson) {
            var pair = GsonHelper.convertToJsonArray(element, "edge");
            if (pair.size() != 2) throw new JsonParseException("Edge must contain two star indices");
            int a = pair.get(0).getAsInt(), b = pair.get(1).getAsInt();
            if (a < 0 || b < 0 || a >= stars.size() || b >= stars.size() || a == b) throw new JsonParseException("Invalid edge endpoint");
            var first = stars.get(a);
            var second = stars.get(b);
            if (Math.hypot(first.x - second.x, first.z - second.z) < 0.02
                    || !unique.add(Math.min(a, b) * 64 + Math.max(a, b))) throw new JsonParseException("Duplicate or zero-length edge");
            edges.add(new Edge(a, b));
        }
        var visited = new HashSet<Integer>();
        var pending = new ArrayDeque<Integer>();
        pending.add(0);
        while (!pending.isEmpty()) {
            int current = pending.remove();
            if (!visited.add(current)) continue;
            for (var edge : edges) {
                if (edge.a == current) pending.add(edge.b);
                if (edge.b == current) pending.add(edge.a);
            }
        }
        if (visited.size() != stars.size()) throw new JsonParseException("Chart must form one connected constellation");
        return new StargazingChart(stars, edges);
    }

    public record Star(float x, float z, float size, float brightness) {}
    public record Edge(int a, int b) {}
}
