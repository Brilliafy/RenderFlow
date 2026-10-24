package com.cleanroommc.frameflow.pacer;

import com.cleanroommc.frameflow.config.ModConfig;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.concurrent.locks.LockSupport;

public class FramePacer {

    private static long nextFrameTargetNanos = 0L;
    private static int lastFps = -1;
    private static long frameIntervalNanos = 0L;

    private static long currentFrameStartNanos = 0L;
    private static long chunkUploadStartNanos = 0L;

    private static final MethodHandle ON_SPIN_WAIT_HANDLE;

    static {
        MethodHandle handle = null;
        try {
            Method m = Thread.class.getMethod("onSpinWait");
            handle = MethodHandles.lookup().unreflect(m);
        } catch (Throwable ignored) {
        }
        ON_SPIN_WAIT_HANDLE = handle;
    }

    public static void onFrameStart() {
        currentFrameStartNanos = System.nanoTime();
    }

    /**
     * High-precision hybrid monotonic sleep-spin frame pacer.
     * Replaces LWJGL 2's Display.sync() to eliminate frametime jitter.
     */
    public static void sync(int fps) {
        if (!ModConfig.enableFramePacing || fps <= 0) {
            return;
        }

        if (fps != lastFps) {
            lastFps = fps;
            frameIntervalNanos = 1_000_000_000L / fps;
            nextFrameTargetNanos = System.nanoTime() + frameIntervalNanos;
            return;
        }

        long now = System.nanoTime();
        long remaining = nextFrameTargetNanos - now;

        // If we are already past the target (a slow frame or a long stall), re-anchor the schedule
        // instead of incrementally advancing it. Otherwise the target drifts further into the past on
        // every frame and pacing silently disables itself until the next spike.
        if (remaining <= 0L) {
            nextFrameTargetNanos = now + frameIntervalNanos;
            return;
        }

        // Phase 1: High-precision park sleep for remaining time > 2ms
        while ((remaining = nextFrameTargetNanos - System.nanoTime()) > 2_000_000L) {
            long sleepNanos = remaining - 1_500_000L;
            LockSupport.parkNanos(sleepNanos);
        }

        // Phase 2: Monotonic microsecond-precision spin wait for final 1.5ms
        while (System.nanoTime() < nextFrameTargetNanos) {
            spinWait();
        }

        nextFrameTargetNanos += frameIntervalNanos;
    }

    private static void spinWait() {
        if (ON_SPIN_WAIT_HANDLE != null) {
            try {
                ON_SPIN_WAIT_HANDLE.invokeExact();
            } catch (Throwable ignored) {
            }
        }
    }

    /**
     * Chunk upload time budget tracker.
     */
    public static void startChunkUploadBatch() {
        chunkUploadStartNanos = System.nanoTime();
    }

    public static boolean isChunkUploadBudgetExhausted() {
        if (!ModConfig.enableChunkUploadTimeSlicing) {
            return false;
        }
        long elapsed = System.nanoTime() - chunkUploadStartNanos;
        return elapsed >= ModConfig.getChunkUploadBudgetNanos();
    }
}
