package me.paulf.fairylights.server.jingle;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.paulf.fairylights.FairyLights;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class JingleManager extends SimpleJsonResourceReloadListener<Jingle> implements IdentifiableResourceReloadListener {
    public static final JingleManager INSTANCE = new JingleManager();

    private Object2ObjectMap<String, JingleLibrary> libraries = Object2ObjectMaps.emptyMap();

    public JingleManager() {
        super(Jingle.CODEC, FileToIdConverter.json("jingles"));
    }

    public JingleLibrary get(final String library) {
        return this.libraries.getOrDefault(library, JingleLibrary.empty());
    }

    @Override
    protected void apply(Map<Identifier, Jingle> elements, ResourceManager manager, ProfilerFiller profiler) {
        final Object2ObjectMap<String, JingleLibrary.Builder> builders = new Object2ObjectOpenHashMap<>();
        elements.forEach((file, jingle) -> {
            final String path = file.getPath();
            final int sl = path.indexOf('/');
            final String library = path.substring(0, Math.max(0, sl));
            final Identifier name = Identifier.fromNamespaceAndPath(file.getNamespace(), path.substring(sl + 1));
            builders.computeIfAbsent(library, _ -> new JingleLibrary.Builder()).add(name, jingle);
        });
        final Object2ObjectMap<String, JingleLibrary> libraries = new Object2ObjectOpenHashMap<>(builders.size());
        Object2ObjectMaps.fastForEach(builders, e -> libraries.put(e.getKey(), e.getValue().build()));
        this.libraries = libraries;
    }

    @Override
    public Identifier getFabricId() {
        return FairyLights.id("jingles");
    }
}
