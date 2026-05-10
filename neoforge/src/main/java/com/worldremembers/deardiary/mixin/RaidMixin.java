package com.worldremembers.deardiary.mixin;

import com.worldremembers.deardiary.event.VanillaDiaryEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.raid.Raid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Raid.class)
public abstract class RaidMixin {
    @Inject(method = "absorbRaidOmen(Lnet/minecraft/server/level/ServerPlayer;)Z", at = @At("RETURN"))
    private void dearDiary$afterRaidOmenAbsorbed(ServerPlayer player, CallbackInfoReturnable<Boolean> callbackInfo) {
        if (Boolean.TRUE.equals(callbackInfo.getReturnValue())) {
            VanillaDiaryEvents.onRaidStarted(player);
        }
    }
}
