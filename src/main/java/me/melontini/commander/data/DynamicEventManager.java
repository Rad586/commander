package me.melontini.commander.data;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import lombok.experimental.Accessors;
import me.melontini.commander.command.ConditionedCommand;
import me.melontini.commander.data.types.EventTypes;
import me.melontini.commander.event.EventType;
import me.melontini.commander.util.DataType;
import me.melontini.dark_matter.api.data.loading.ReloaderType;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.*;
import java.util.stream.Collectors;

@Accessors(fluent = true)
public class DynamicEventManager extends JsonDataLoader implements IdentifiableResourceReloadListener {

    public static final ReloaderType<DynamicEventManager> RELOADER = ReloaderType.create(new Identifier("commander:events"));
    public static final DataType<List<ConditionedCommand>> DEFAULT = new DataType<>();

    final Map<EventType, Object> customData = new IdentityHashMap<>();

    public DynamicEventManager() {
        super(new Gson(), RELOADER.identifier().toString().replace(":", "/"));
    }

    public static <T> T getData(MinecraftServer server, EventType type) {
        return getData(server, type, null);
    }

    public static <T> T getData(MinecraftServer server, EventType type, DataType<T> key) {
        return server.dm$getReloader(DynamicEventManager.RELOADER).getData(type, key);
    }

    public <T> T getData(EventType type, DataType<T> key) {
        return (T) this.customData.get(type);
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> parsed, ResourceManager manager, Profiler profiler) {
        Maps.transformValues(parsed, input -> Subscription.CODEC.parse(JsonOps.INSTANCE, input).getOrThrow(false, string -> {
            throw  new JsonParseException(string);
        })).values().stream().flatMap(Collection::stream).collect(Collectors.groupingBy(Subscription::type)).forEach((eventType, subscriptions) -> {
            var finalizer = eventType.context().get(EventType.FINALIZER);
            if (finalizer.isPresent()) {
                this.customData.put(eventType, finalizer.get().apply(subscriptions));
                return;
            }
            this.customData.put(eventType, subscriptions.stream().flatMap(s -> s.list().stream()).toList());
        });

        Sets.difference(EventTypes.types(), customData.keySet()).forEach(type -> {
            var finalizer = type.context().get(EventType.FINALIZER);
            if (finalizer.isPresent()) {
                this.customData.put(type, finalizer.get().apply(Collections.emptyList()));
                return;
            }
            this.customData.put(type, Collections.emptyList());
        });
    }

    @Override
    public Identifier getFabricId() {
        return RELOADER.identifier();
    }
}
