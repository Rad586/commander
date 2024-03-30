package me.melontini.commander.builtin.events;

import me.melontini.commander.event.EventType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.entity.Entity;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import static me.melontini.commander.Commander.id;
import static me.melontini.commander.util.EventExecutors.runVoid;

public class ServerLifecycle {

    public static final EventType SERVER_STARTED = EventType.builder().build(id("server/started"));
    public static final EventType SERVER_STOPPING = EventType.builder().build(id("server/stopping"));

    public static final EventType WORLD_LOAD = EventType.builder().build(id("world/load"));
    public static final EventType WORLD_UNLOAD = EventType.builder().build(id("world/unload"));

    public static final EventType CHUNK_LOAD = EventType.builder().build(id("chunk/load"));
    public static final EventType CHUNK_UNLOAD = EventType.builder().build(id("chunk/unload"));

    public static final EventType ENTITY_LOAD = EventType.builder().build(id("entity/load"));
    public static final EventType ENTITY_UNLOAD = EventType.builder().build(id("entity/unload"));

    public static void init() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> runVoid(SERVER_STARTED, server.getOverworld(), () -> forWorld(server.getOverworld())));
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> runVoid(SERVER_STOPPING, server.getOverworld(), () -> forWorld(server.getOverworld())));

        ServerWorldEvents.LOAD.register((server, world) -> runVoid(WORLD_LOAD, world, () -> forWorld(world)));
        ServerWorldEvents.UNLOAD.register((server, world) -> runVoid(WORLD_UNLOAD, world, () -> forWorld(world)));

        ServerChunkEvents.CHUNK_LOAD.register((world, chunk) -> runVoid(CHUNK_LOAD, world, () -> forWorld(world, Vec3d.of(chunk.getPos().getStartPos()))));
        ServerChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> runVoid(CHUNK_UNLOAD, world, () -> forWorld(world, Vec3d.of(chunk.getPos().getStartPos()))));

        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> runVoid(ENTITY_LOAD, world, () -> forEntity(world, entity)));
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> runVoid(ENTITY_UNLOAD, world, () -> forEntity(world, entity)));
    }

    private static LootContext forEntity(ServerWorld world, Entity entity) {
        LootContextParameterSet.Builder builder = new LootContextParameterSet.Builder(world)
                .add(LootContextParameters.ORIGIN, Vec3d.ZERO)
                .add(LootContextParameters.THIS_ENTITY, entity);
        return new LootContext.Builder(builder.build(LootContextTypes.COMMAND)).build(null);
    }

    private static LootContext forWorld(ServerWorld world, Vec3d origin) {
        LootContextParameterSet.Builder builder = new LootContextParameterSet.Builder(world).add(LootContextParameters.ORIGIN, origin);
        return new LootContext.Builder(builder.build(LootContextTypes.COMMAND)).build(null);
    }

    private static LootContext forWorld(ServerWorld world) {
        LootContextParameterSet.Builder builder = new LootContextParameterSet.Builder(world).add(LootContextParameters.ORIGIN, Vec3d.ZERO);
        return new LootContext.Builder(builder.build(LootContextTypes.COMMAND)).build(null);
    }
}
