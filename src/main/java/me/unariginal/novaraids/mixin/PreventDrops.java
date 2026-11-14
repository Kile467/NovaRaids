package me.unariginal.novaraids.mixin;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import me.unariginal.novaraids.utils.EntityDiscardScheduler;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class PreventDrops {
    @Inject(at = {@At("HEAD")}, method = {"onDeath"})
    public void onDeath(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof PokemonEntity pokemonEntity) {
            Pokemon pokemon = pokemonEntity.getPokemon();
            if (!pokemon.isPlayerOwned()) {
                if (pokemon.getPersistentData().contains("raid_entity")) {
                    if (pokemon.getPersistentData().getBoolean("raid_entity")) {
                        pokemon.removeHeldItem();
                    }
                }
            }
        }
    }

    @Inject(at = {@At("RETURN")}, method = {"onDeath"})
    public void scheduleDiscard(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof PokemonEntity pokemonEntity) {
            Pokemon pokemon = pokemonEntity.getPokemon();
            if (!pokemon.isPlayerOwned()) {
                if (pokemon.getPersistentData().contains("raid_entity")) {
                    if (pokemon.getPersistentData().getBoolean("raid_entity")) {
                        EntityDiscardScheduler.schedule(pokemonEntity);
                    }
                }
            }
        }
    }

    @Inject(at = {@At("HEAD")}, method = {"dropXp"}, cancellable = true)
    public void onDropXp(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof PokemonEntity pokemonEntity) {
            Pokemon pokemon = pokemonEntity.getPokemon();
            if (!pokemon.isPlayerOwned()) {
                if (pokemon.getPersistentData().contains("raid_entity")) {
                    if (pokemon.getPersistentData().getBoolean("raid_entity")) {
                        ci.cancel();
                    }
                }
            }
        }
    }

    @Inject(at = {@At("HEAD")}, method = {"drop"}, cancellable = true)
    public void onDrop(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof PokemonEntity pokemonEntity) {
            Pokemon pokemon = pokemonEntity.getPokemon();
            if (!pokemon.isPlayerOwned()) {
                if (pokemon.getPersistentData().contains("raid_entity")) {
                    if (pokemon.getPersistentData().getBoolean("raid_entity")) {
                        ci.cancel();
                    }
                }
            }
        }
    }
}
