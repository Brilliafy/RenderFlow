package com.cleanroommc.renderflow.mixin;

import com.cleanroommc.renderflow.config.ModConfig;
import com.cleanroommc.renderflow.pacer.FramePacer;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Inject(method = "runGameLoop", at = @At("HEAD"))
    private void consistentframes$onFrameStart(CallbackInfo ci) {
        FramePacer.onFrameStart();
    }

    @WrapOperation(
            method = "runGameLoop",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/opengl/Display;sync(I)V",
                    remap = false
            )
    )
    private void consistentframes$wrapDisplaySync(int fps, Operation<Void> original) {
        if (ModConfig.FRAME_PACING.enableFramePacing) {
            FramePacer.sync(fps);
        } else {
            original.call(fps);
        }
    }

    @WrapOperation(
            method = "runGameLoop",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/Thread;yield()V",
                    remap = false
            )
    )
    private void consistentframes$wrapThreadYield(Operation<Void> original) {
        if (!ModConfig.FRAME_PACING.eliminateThreadYield) {
            original.call();
        }
        // When eliminateThreadYield is true, skip Thread.yield() to prevent OS scheduler descheduling jitter
    }
}
