package me.melontini.commander.event.builtin;

import lombok.experimental.UtilityClass;
import me.melontini.commander.data.types.EventTypes;
import me.melontini.commander.event.EventType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import static me.melontini.commander.Commander.id;
import static me.melontini.commander.event.builtin.BuiltInEvents.runVoid;

@UtilityClass
public class ServerTick {

    public static final EventType START_TICK = EventTypes.register(id("server_tick/start"), EventType.builder().build());
    public static final EventType END_TICK = EventTypes.register(id("server_tick/end"), EventType.builder().build());
    public static final EventType START_WORLD_TICK = EventTypes.register(id("world_tick/start"), EventType.builder().build());
    public static final EventType END_WORLD_TICK = EventTypes.register(id("world_tick/end"), EventType.builder().build());

    static void init() {
        ServerTickEvents.START_SERVER_TICK.register(server -> runVoid(START_TICK, server.getOverworld(), () -> forWorld(server.getOverworld())));
        ServerTickEvents.END_SERVER_TICK.register(server -> runVoid(END_TICK, server.getOverworld(), () -> forWorld(server.getOverworld())));

        ServerTickEvents.START_WORLD_TICK.register((world) -> runVoid(START_WORLD_TICK, world, () -> forWorld(world)));
        ServerTickEvents.END_WORLD_TICK.register((world) -> runVoid(END_WORLD_TICK, world, () -> forWorld(world)));
    }

    private static LootContext forWorld(ServerWorld world) {
        LootContextParameterSet.Builder builder = new LootContextParameterSet.Builder(world).add(LootContextParameters.ORIGIN, Vec3d.ZERO);
        return new LootContext.Builder(builder.build(LootContextTypes.COMMAND)).build(null);
    }
}
