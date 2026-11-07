package jp.aquafactory.constellationandspells.spell.stargazing.client;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import jp.aquafactory.constellationandspells.ConstellationnSpells;
import jp.aquafactory.constellationandspells.spell.stargazing.StargazingChart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

public final class StargazingCharts extends SimpleJsonResourceReloadListener {
    public static final StargazingCharts INSTANCE = new StargazingCharts();
    private static final Logger LOGGER = LogUtils.getLogger();
    private Map<String, StargazingChart> charts = Map.of();

    private StargazingCharts() { super(new Gson(), "stargazing"); }

    public @Nullable StargazingChart forSeed(long seed) { return charts.get(StargazingChart.idForSeed(seed)); }

    @Override
    protected void apply(@NotNull Map<ResourceLocation, JsonElement> resources, @NotNull ResourceManager manager, @NotNull ProfilerFiller profiler) {
        var loaded = new HashMap<String, StargazingChart>();
        for (var id : StargazingChart.IDS) {
            var location = ResourceLocation.fromNamespaceAndPath(ConstellationnSpells.MODID, id);
            try {
                var json = resources.get(location);
                if (json == null) throw new IllegalArgumentException("Missing chart resource");
                loaded.put(id, StargazingChart.parse(json));
            } catch (RuntimeException exception) {
                LOGGER.error("Unable to load Stargazing chart {}", location, exception);
                // 編集途中の不正なパックで、表示中の星座まで失わないよう直前の有効な定義を残す。
                if (charts.containsKey(id)) loaded.put(id, charts.get(id));
            }
        }
        charts = Map.copyOf(loaded);
    }
}
