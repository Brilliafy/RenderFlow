package com.cleanroommc.renderflow.mixin;

import com.cleanroommc.renderflow.config.ModConfig;
import com.cleanroommc.renderflow.pacer.FramePacer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "org.lwjglx.opengl.Sync", remap = false)
public abstract class SyncMixin {

    @Inject(method = "sync", at = @At("HEAD"), cancellable = true)
    private static void consistentframes$onLwjglxSync(int fps, CallbackInfo ci) {
        if (ModConfig.FRAME_PACING.enableFramePacing) {
            FramePacer.sync(fps);
            ci.cancel();
        }
    }
}
