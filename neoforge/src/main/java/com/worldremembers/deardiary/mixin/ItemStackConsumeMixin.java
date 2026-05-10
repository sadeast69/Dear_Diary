package com.worldremembers.deardiary.mixin;

import com.worldremembers.deardiary.compat.neoforge.NeoForgeWaystonesCompat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class ItemStackConsumeMixin {
    @Inject(method = "consume(ILnet/minecraft/world/entity/LivingEntity;)V", at = @At("HEAD"))
    private void dearDiary$beforeConsume(int amount, LivingEntity entity, CallbackInfo callbackInfo) {
        if (amount > 0 && entity instanceof ServerPlayer player && !player.isCreative()) {
            NeoForgeWaystonesCompat.onItemConsumed(player, (ItemStack) (Object) this, amount);
        }
    }
}
