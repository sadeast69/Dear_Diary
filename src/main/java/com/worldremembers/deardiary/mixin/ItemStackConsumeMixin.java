package com.worldremembers.deardiary.mixin;

import com.worldremembers.deardiary.compat.fabric.FabricWaystonesCompat;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class ItemStackConsumeMixin {
    @Inject(method = "decrementUnlessCreative(ILnet/minecraft/entity/LivingEntity;)V", at = @At("HEAD"))
    private void dearDiary$beforeDecrementUnlessCreative(int amount, LivingEntity entity, CallbackInfo callbackInfo) {
        if (amount > 0 && entity instanceof ServerPlayerEntity player && !player.isCreative()) {
            FabricWaystonesCompat.onItemConsumed(player, (ItemStack) (Object) this, amount);
        }
    }
}
