package me.unariginal.novaraids.utils;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import me.unariginal.novaraids.NovaRaids;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class EntityDiscardScheduler {
    public static final long BOSS_DISPLAY_DELAY = 60L;
    public static final long BOSS_CLONE_DELAY = 20L;
    public static final long DEFAULT_RAID_DELAY = 40L;

    private static final Map<UUID, ScheduledDiscard> scheduled = new HashMap<>();

    private record ScheduledDiscard(PokemonEntity entity, long executeTick) {}

    public static long delayFor(PokemonEntity entity) {
        NbtCompound data = entity.getPokemon().getPersistentData();
        if (data.contains("boss_display") && data.getBoolean("boss_display")) {
            return BOSS_DISPLAY_DELAY;
        }
        if (data.contains("boss_clone") && data.getBoolean("boss_clone")) {
            return BOSS_CLONE_DELAY;
        }
        return DEFAULT_RAID_DELAY;
    }

    public static void schedule(PokemonEntity entity, long delayTicks) {
        if (entity == null || entity.isRemoved()) {
            return;
        }
        long executeTick = NovaRaids.INSTANCE.server().getOverworld().getTime() + delayTicks;
        scheduled.put(entity.getUuid(), new ScheduledDiscard(entity, executeTick));
    }

    public static void schedule(PokemonEntity entity) {
        schedule(entity, delayFor(entity));
    }

    public static void tick() {
        if (scheduled.isEmpty()) {
            return;
        }

        long currentTick = NovaRaids.INSTANCE.server().getOverworld().getTime();
        Iterator<Map.Entry<UUID, ScheduledDiscard>> iterator = scheduled.entrySet().iterator();
        while (iterator.hasNext()) {
            ScheduledDiscard discard = iterator.next().getValue();
            if (currentTick >= discard.executeTick()) {
                forceDiscard(discard.entity());
                iterator.remove();
            }
        }
    }

    public static void forceDiscard(PokemonEntity entity) {
        if (entity == null || entity.isRemoved()) {
            return;
        }

        int chunkX = (int) Math.floor(entity.getPos().getX() / 16);
        int chunkZ = (int) Math.floor(entity.getPos().getZ() / 16);
        ServerWorld world = resolveWorld(entity);

        NovaRaids.INSTANCE.server().execute(() -> {
            if (entity.isRemoved()) {
                return;
            }
            world.setChunkForced(chunkX, chunkZ, true);
            if (!entity.isRemoved()) {
                entity.discard();
            }
            world.setChunkForced(chunkX, chunkZ, false);
        });
    }

    private static ServerWorld resolveWorld(PokemonEntity entity) {
        for (ServerWorld world : NovaRaids.INSTANCE.server().getWorlds()) {
            if (world.getRegistryKey().equals(entity.getWorld().getRegistryKey())) {
                return world;
            }
        }
        return NovaRaids.INSTANCE.server().getOverworld();
    }
}
