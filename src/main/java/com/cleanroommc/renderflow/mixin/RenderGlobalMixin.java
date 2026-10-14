package com.cleanroommc.renderflow.mixin;

import com.cleanroommc.renderflow.culling.OcclusionCullingEngine;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RenderGlobal.class)
public abstract class RenderGlobalMixin {

    @WrapOperation(
            method = "renderEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/RenderManager;shouldRender(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/renderer/culling/ICamera;DDD)Z"
            ),
            require = 0
    )
    private boolean hizculling$wrapShouldRender(RenderManager instance, Entity entity, ICamera camera, double camX, double camY, double camZ, Operation<Boolean> original) {
        if (!original.call(instance, entity, camera, camX, camY, camZ)) {
            return false;
        }
        return !OcclusionCullingEngine.isEntityOccluded(entity, camX, camY, camZ);
    }

    @WrapOperation(
            method = "renderEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/culling/ICamera;isBoundingBoxInFrustum(Lnet/minecraft/util/math/AxisAlignedBB;)Z",
                    ordinal = 0
            ),
            require = 0
    )
    private boolean hizculling$wrapTileEntityFrustum0(ICamera camera, AxisAlignedBB bb, Operation<Boolean> original) {
        if (!original.call(camera, bb)) {
            return false;
        }
        Minecraft mc = Minecraft.getMinecraft();
        Entity viewer = mc.getRenderViewEntity();
        if (viewer == null) {
            return true;
        }
        float pt = mc.getRenderPartialTicks();
        double camX = viewer.prevPosX + (viewer.posX - viewer.prevPosX) * (double) pt;
        double camY = viewer.prevPosY + (viewer.posY - viewer.prevPosY) * (double) pt + viewer.getEyeHeight();
        double camZ = viewer.prevPosZ + (viewer.posZ - viewer.prevPosZ) * (double) pt;

        return !OcclusionCullingEngine.isBoundingBoxOccluded(bb, camX, camY, camZ);
    }

    @WrapOperation(
            method = "renderEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/culling/ICamera;isBoundingBoxInFrustum(Lnet/minecraft/util/math/AxisAlignedBB;)Z",
                    ordinal = 1
            ),
            require = 0
    )
    private boolean hizculling$wrapTileEntityFrustum1(ICamera camera, AxisAlignedBB bb, Operation<Boolean> original) {
        if (!original.call(camera, bb)) {
            return false;
        }
        Minecraft mc = Minecraft.getMinecraft();
        Entity viewer = mc.getRenderViewEntity();
        if (viewer == null) {
            return true;
        }
        float pt = mc.getRenderPartialTicks();
        double camX = viewer.prevPosX + (viewer.posX - viewer.prevPosX) * (double) pt;
        double camY = viewer.prevPosY + (viewer.posY - viewer.prevPosY) * (double) pt + viewer.getEyeHeight();
        double camZ = viewer.prevPosZ + (viewer.posZ - viewer.prevPosZ) * (double) pt;

        return !OcclusionCullingEngine.isBoundingBoxOccluded(bb, camX, camY, camZ);
    }
}
