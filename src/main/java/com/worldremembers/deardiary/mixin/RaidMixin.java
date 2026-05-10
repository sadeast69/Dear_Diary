package com.worldremembers.deardiary.mixin;

import com.worldremembers.deardiary.event.VanillaDiaryHardHooks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.village.raid.Raid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Raid.class)
public abstract class RaidMixin {
    @Inject(method = "start", at = @At("RETURN"))
    private void dearDiary$afterRaidStarted(ServerPlayerEntity player, CallbackInfoReturnable<Boolean> callbackInfo) {
        if (callbackInfo.getReturnValueZ()) {
            VanillaDiaryHardHooks.onRaidStarted(player);
        }
    }
}
