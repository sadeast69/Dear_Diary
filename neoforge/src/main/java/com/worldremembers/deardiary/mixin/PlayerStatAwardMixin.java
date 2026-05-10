package com.worldremembers.deardiary.mixin;

import com.worldremembers.deardiary.compat.neoforge.NeoForgeWaystonesCompat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerStatAwardMixin {
    @Inject(method = "awardStat(Lnet/minecraft/resources/ResourceLocation;)V", at = @At("TAIL"))
    private void dearDiary$afterAwardStat(ResourceLocation statId, CallbackInfo callbackInfo) {
        if ((Object) this instanceof ServerPlayer player) {
            NeoForgeWaystonesCompat.onCustomStatAwarded(player, statId);
        }
    }
}
