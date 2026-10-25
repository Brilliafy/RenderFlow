package com.cleanroommc.renderflow.mixin;

import com.cleanroommc.renderflow.config.ModConfig;
import com.cleanroommc.renderflow.pacer.FramePacer;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Inject(method = "runGameLoop", at = @At("HEAD"))
    private void renderflow$onFrameStart(CallbackInfo ci) {
        FramePacer.onFrameStart();
    }

    @WrapOperation(
            method = "runGameLoop",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/opengl/Display;sync(I)V",
                    remap = false
            ),
            require = 0
    )
    private void renderflow$wrapDisplaySync(int fps, Operation<Void> original) {
        if (ModConfig.enableFramePacing) {
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
            ),
            require = 0
    )
    private void renderflow$wrapThreadYield(Operation<Void> original) {
        if (!ModConfig.eliminateThreadYield) {
            original.call();
        }
    }
}
