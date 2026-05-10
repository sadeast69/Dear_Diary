package com.worldremembers.deardiary.mixin;

import com.worldremembers.deardiary.event.VanillaDiaryHardHooks;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerAdvancementTracker.class)
public abstract class PlayerAdvancementTrackerMixin {
    @Shadow
    private ServerPlayerEntity owner;

    @Inject(method = "grantCriterion", at = @At("RETURN"))
    private void dearDiary$afterCriterionGranted(
            AdvancementEntry advancement,
            String criterionName,
            CallbackInfoReturnable<Boolean> callbackInfo
    ) {
        VanillaDiaryHardHooks.onAdvancementCriterionGranted(owner, advancement, callbackInfo.getReturnValueZ());
    }
}
