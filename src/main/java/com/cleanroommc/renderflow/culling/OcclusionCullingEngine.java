package com.cleanroommc.renderflow.culling;

import com.cleanroommc.renderflow.config.ModConfig;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.concurrent.atomic.AtomicLong;

public class OcclusionCullingEngine {

    public static final AtomicLong ENTITIES_CULLED = new AtomicLong(0);
    public static final AtomicLong ENTITIES_RENDERED = new AtomicLong(0);
    public static final AtomicLong TILE_ENTITIES_CULLED = new AtomicLong(0);
    public static final AtomicLong TILE_ENTITIES_RENDERED = new AtomicLong(0);

    public static boolean isEntityOccluded(Entity entity, double camX, double camY, double camZ) {
        if (!ModConfig.OCCLUSION_CULLING.enabled || !ModConfig.OCCLUSION_CULLING.cullEntities) {
            ENTITIES_RENDERED.incrementAndGet();
            return false;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (entity == mc.player || entity == mc.getRenderViewEntity()) {
            ENTITIES_RENDERED.incrementAndGet();
            return false;
        }

        if (entity.isRidingOrBeingRiddenBy(mc.player)) {
            ENTITIES_RENDERED.incrementAndGet();
            return false;
        }

        if (entity.isGlowing()) {
            ENTITIES_RENDERED.incrementAndGet();
            return false;
        }

        if (ModConfig.OCCLUSION_CULLING.exemptBosses && isBoss(entity)) {
            ENTITIES_RENDERED.incrementAndGet();
            return false;
        }

        double dx = entity.posX - camX;
        double dy = (entity.posY + entity.height * 0.5D) - camY;
        double dz = entity.posZ - camZ;
        double distSq = dx * dx + dy * dy + dz * dz;

        double minDist = ModConfig.OCCLUSION_CULLING.minCullDistance;
        if (distSq < minDist * minDist) {
            ENTITIES_RENDERED.incrementAndGet();
            return false;
        }

        double maxDist = ModConfig.OCCLUSION_CULLING.maxRaytraceDistance;
        if (distSq > maxDist * maxDist) {
            ENTITIES_CULLED.incrementAndGet();
            return true;
        }

        World world = mc.world;
        if (world == null) {
            ENTITIES_RENDERED.incrementAndGet();
            return false;
        }

        AxisAlignedBB bb = entity.getEntityBoundingBox();
        if (bb == null) {
            bb = entity.getRenderBoundingBox();
        }
        if (bb == null) {
            ENTITIES_RENDERED.incrementAndGet();
            return false;
        }

        double centerY = (bb.minY + bb.maxY) * 0.5D;
        double topY = bb.maxY - 0.05D;
        double botY = bb.minY + 0.15D;

        // Test center ray
        if (!isRayBlocked(world, camX, camY, camZ, entity.posX, centerY, entity.posZ)) {
            ENTITIES_RENDERED.incrementAndGet();
            return false;
        }

        // Test top ray
        if (!isRayBlocked(world, camX, camY, camZ, entity.posX, topY, entity.posZ)) {
            ENTITIES_RENDERED.incrementAndGet();
            return false;
        }

        // Test bottom ray
        if (!isRayBlocked(world, camX, camY, camZ, entity.posX, botY, entity.posZ)) {
            ENTITIES_RENDERED.incrementAndGet();
            return false;
        }

        // All rays blocked by solid opaque geometry -> occluded!
        ENTITIES_CULLED.incrementAndGet();
        return true;
    }

    public static boolean isBoundingBoxOccluded(AxisAlignedBB bb, double camX, double camY, double camZ) {
        if (!ModConfig.OCCLUSION_CULLING.enabled || !ModConfig.OCCLUSION_CULLING.cullTileEntities) {
            TILE_ENTITIES_RENDERED.incrementAndGet();
            return false;
        }

        double centerX = (bb.minX + bb.maxX) * 0.5D;
        double centerY = (bb.minY + bb.maxY) * 0.5D;
        double centerZ = (bb.minZ + bb.maxZ) * 0.5D;

        double dx = centerX - camX;
        double dy = centerY - camY;
        double dz = centerZ - camZ;
        double distSq = dx * dx + dy * dy + dz * dz;

        double minDist = ModConfig.OCCLUSION_CULLING.minCullDistance;
        if (distSq < minDist * minDist) {
            TILE_ENTITIES_RENDERED.incrementAndGet();
            return false;
        }

        double maxDist = ModConfig.OCCLUSION_CULLING.maxRaytraceDistance;
        if (distSq > maxDist * maxDist) {
            TILE_ENTITIES_CULLED.incrementAndGet();
            return true;
        }

        Minecraft mc = Minecraft.getMinecraft();
        World world = mc.world;
        if (world == null) {
            TILE_ENTITIES_RENDERED.incrementAndGet();
            return false;
        }

        double topY = bb.maxY - 0.05D;
        double botY = bb.minY + 0.1D;

        if (!isRayBlocked(world, camX, camY, camZ, centerX, centerY, centerZ)) {
            TILE_ENTITIES_RENDERED.incrementAndGet();
            return false;
        }

        if (!isRayBlocked(world, camX, camY, camZ, centerX, topY, centerZ)) {
            TILE_ENTITIES_RENDERED.incrementAndGet();
            return false;
        }

        if (!isRayBlocked(world, camX, camY, camZ, centerX, botY, centerZ)) {
            TILE_ENTITIES_RENDERED.incrementAndGet();
            return false;
        }

        TILE_ENTITIES_CULLED.incrementAndGet();
        return true;
    }

    public static boolean isTileEntityOccluded(TileEntity te, double camX, double camY, double camZ) {
        if (!ModConfig.OCCLUSION_CULLING.enabled || !ModConfig.OCCLUSION_CULLING.cullTileEntities) {
            TILE_ENTITIES_RENDERED.incrementAndGet();
            return false;
        }

        AxisAlignedBB bb = te.getRenderBoundingBox();
        if (bb == null) {
            BlockPos p = te.getPos();
            bb = new AxisAlignedBB(p.getX(), p.getY(), p.getZ(), p.getX() + 1.0D, p.getY() + 1.0D, p.getZ() + 1.0D);
        }

        return isBoundingBoxOccluded(bb, camX, camY, camZ);
    }

    public static boolean isRayBlocked(World world, double x0, double y0, double z0, double x1, double y1, double z1) {
        int ix = MathHelper.floor(x0);
        int iy = MathHelper.floor(y0);
        int iz = MathHelper.floor(z0);

        int endX = MathHelper.floor(x1);
        int endY = MathHelper.floor(y1);
        int endZ = MathHelper.floor(z1);

        if (ix == endX && iy == endY && iz == endZ) {
            return false;
        }

        double dx = x1 - x0;
        double dy = y1 - y0;
        double dz = z1 - z0;

        int stepX = dx > 0.0D ? 1 : (dx < 0.0D ? -1 : 0);
        int stepY = dy > 0.0D ? 1 : (dy < 0.0D ? -1 : 0);
        int stepZ = dz > 0.0D ? 1 : (dz < 0.0D ? -1 : 0);

        double invDx = dx != 0.0D ? 1.0D / dx : Double.POSITIVE_INFINITY;
        double invDy = dy != 0.0D ? 1.0D / dy : Double.POSITIVE_INFINITY;
        double invDz = dz != 0.0D ? 1.0D / dz : Double.POSITIVE_INFINITY;

        double tMaxX = stepX > 0 ? (ix + 1.0D - x0) * invDx : (stepX < 0 ? (ix - x0) * invDx : Double.POSITIVE_INFINITY);
        double tMaxY = stepY > 0 ? (iy + 1.0D - y0) * invDy : (stepY < 0 ? (iy - y0) * invDy : Double.POSITIVE_INFINITY);
        double tMaxZ = stepZ > 0 ? (iz + 1.0D - z0) * invDz : (stepZ < 0 ? (iz - z0) * invDz : Double.POSITIVE_INFINITY);

        double tDeltaX = stepX != 0 ? Math.abs(invDx) : Double.POSITIVE_INFINITY;
        double tDeltaY = stepY != 0 ? Math.abs(invDy) : Double.POSITIVE_INFINITY;
        double tDeltaZ = stepZ != 0 ? Math.abs(invDz) : Double.POSITIVE_INFINITY;

        Chunk currentChunk = null;
        int currentChunkX = Integer.MIN_VALUE;
        int currentChunkZ = Integer.MIN_VALUE;
        int maxSteps = 100;

        while (maxSteps-- > 0) {
            if (tMaxX < tMaxY) {
                if (tMaxX < tMaxZ) {
                    ix += stepX;
                    tMaxX += tDeltaX;
                } else {
                    iz += stepZ;
                    tMaxZ += tDeltaZ;
                }
            } else {
                if (tMaxY < tMaxZ) {
                    iy += stepY;
                    tMaxY += tDeltaY;
                } else {
                    iz += stepZ;
                    tMaxZ += tDeltaZ;
                }
            }

            if (ix == endX && iy == endY && iz == endZ) {
                return false;
            }

            if (iy < 0 || iy >= 256) {
                continue;
            }

            int cx = ix >> 4;
            int cz = iz >> 4;
            if (cx != currentChunkX || cz != currentChunkZ || currentChunk == null) {
                currentChunkX = cx;
                currentChunkZ = cz;
                currentChunk = world.getChunkProvider().getLoadedChunk(cx, cz);
            }

            if (currentChunk == null || !currentChunk.isLoaded()) {
                return false;
            }

            IBlockState state = currentChunk.getBlockState(ix, iy, iz);
            if (state.isOpaqueCube()) {
                return true;
            }
        }

        return false;
    }

    public static boolean isBoss(Entity entity) {
        if (entity instanceof EntityDragon || entity instanceof EntityWither || entity instanceof MultiPartEntityPart) {
            return true;
        }

        if (entity instanceof EntityLivingBase && !((EntityLivingBase) entity).isNonBoss()) {
            return true;
        }

        String className = entity.getClass().getName().toLowerCase();
        if (className.contains("boss") || className.contains("dragon") || className.contains("wither") ||
                className.contains("wroughtnaut") || className.contains("zeus") || className.contains("drstalker")) {
            return true;
        }

        if (entity.hasCustomName()) {
            String name = entity.getCustomNameTag().toLowerCase();
            if (name.contains("boss") || name.contains("ferocious") || name.contains("wroughtnaut") ||
                    name.contains("zeus") || name.contains("drstalker")) {
                return true;
            }
        }

        return false;
    }

    public static void clearStats() {
        ENTITIES_CULLED.set(0);
        ENTITIES_RENDERED.set(0);
        TILE_ENTITIES_CULLED.set(0);
        TILE_ENTITIES_RENDERED.set(0);
    }
}
