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

    @Inject(method = "sync", at = @At("HEAD"), cancellable = true, require = 0)
    private static void renderflow$onLwjglxSync(int fps, CallbackInfo ci) {
        if (ModConfig.enableFramePacing) {
            FramePacer.sync(fps);
            ci.cancel();
        }
    }
}
