package com.worldremembers.deardiary.mixin;

import com.worldremembers.deardiary.compat.fabric.FabricFarmersDelightCompat;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class ItemFinishUsingMixin {
    @Inject(method = "finishUsing", at = @At("HEAD"))
    private void dearDiary$beforeFinishUsing(
            ItemStack stack,
            World world,
            LivingEntity user,
            CallbackInfoReturnable<ItemStack> callbackInfo
    ) {
        if (!world.isClient() && user instanceof ServerPlayerEntity player) {
            FabricFarmersDelightCompat.onFinishedUsingItem(player, stack);
        }
    }
}
