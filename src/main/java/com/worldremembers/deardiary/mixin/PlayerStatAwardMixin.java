package com.worldremembers.deardiary.mixin;

import com.worldremembers.deardiary.compat.fabric.FabricWaystonesCompat;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerStatAwardMixin {
    @Inject(method = "incrementStat(Lnet/minecraft/util/Identifier;)V", at = @At("TAIL"))
    private void dearDiary$afterIncrementStat(Identifier statId, CallbackInfo callbackInfo) {
        if ((Object) this instanceof ServerPlayerEntity player) {
            FabricWaystonesCompat.onCustomStatAwarded(player, statId);
        }
    }
}
