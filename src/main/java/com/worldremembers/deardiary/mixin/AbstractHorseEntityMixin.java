package com.worldremembers.deardiary.mixin;

import com.worldremembers.deardiary.event.VanillaDiaryHardHooks;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractHorseEntity.class)
public abstract class AbstractHorseEntityMixin {
    @Inject(method = "bondWithPlayer", at = @At("RETURN"))
    private void dearDiary$afterHorseBonded(PlayerEntity player, CallbackInfoReturnable<Boolean> callbackInfo) {
        if (callbackInfo.getReturnValueZ() && player instanceof ServerPlayerEntity serverPlayer) {
            VanillaDiaryHardHooks.onHorseTamed(serverPlayer);
        }
    }
}
