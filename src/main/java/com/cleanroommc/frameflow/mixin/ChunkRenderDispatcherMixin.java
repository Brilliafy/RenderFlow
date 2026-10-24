package com.cleanroommc.frameflow.mixin;

import com.cleanroommc.frameflow.pacer.FramePacer;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Queue;

@Mixin(ChunkRenderDispatcher.class)
public abstract class ChunkRenderDispatcherMixin {

    @Shadow
    @Final
    private Queue<?> queueChunkUploads;

    @Inject(method = "runChunkUploads", at = @At("HEAD"), require = 0)
    private void frameflow$onStartChunkUploads(long finishTimeNano, CallbackInfoReturnable<Boolean> cir) {
        FramePacer.startChunkUploadBatch();
    }

    @WrapOperation(
            method = "runChunkUploads",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Queue;isEmpty()Z",
                    ordinal = 0
            ),
            require = 0
    )
    private boolean frameflow$timeSliceChunkUploads(Queue<?> queue, Operation<Boolean> original) {
        // If time budget for this frame is exhausted, pretend the upload queue is empty to cleanly defer to next frame
        if (queue == this.queueChunkUploads && FramePacer.isChunkUploadBudgetExhausted()) {
            return true;
        }
        return original.call(queue);
    }
}
