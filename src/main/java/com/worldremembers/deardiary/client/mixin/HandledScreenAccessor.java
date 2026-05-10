package com.worldremembers.deardiary.client.mixin;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HandledScreen.class)
public interface HandledScreenAccessor {
    @Accessor("x")
    int dearDiary$getX();

    @Accessor("y")
    int dearDiary$getY();

    @Accessor("backgroundWidth")
    int dearDiary$getBackgroundWidth();

    @Accessor("backgroundHeight")
    int dearDiary$getBackgroundHeight();
}
