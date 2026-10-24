# FrameFlow

![FrameFlow](logo.png)

Client-side frame pacing, thread yield elimination, and chunk mesh upload time-slicing for Minecraft 1.12.2 on Cleanroom or Minecraft Forge.

This is a **client-only** mod. It does nothing on a dedicated server.

## Overview

FrameFlow targets the two most common causes of client micro-stutter and uneven frametimes in Minecraft 1.12.2:

1. **Inaccurate frame pacing**: Vanilla's `Display.sync` relies on coarse millisecond sleeping that overshoots and jitters when an FPS limit is set.
2. **Chunk mesh upload bursts**: When multiple chunk compilation tasks finish at once, uploading their meshes to OpenGL on the render thread can cause 20–50 ms lag spikes.

## Features

### 1. Monotonic Hybrid Frame Pacer
Vanilla limits the frame rate with LWJGL 2's `Display.sync`, which uses a coarse sleep loop that frequently misses frame deadlines. FrameFlow replaces this with a two-phase monotonic hybrid pacer:
- **Phase 1 (Park Sleep)**: Uses high-precision `LockSupport.parkNanos` while more than 2 ms remain before the deadline.
- **Phase 2 (Spin-Wait)**: High-resolution spin-wait using `Thread.onSpinWait()` for the final 1.5 ms.
- **Target Re-anchoring**: Whenever a frame runs late or stalls, the pacer re-anchors its schedule to prevent drift.

On Cleanroom / LWJGL3ify, FrameFlow hooks `org.lwjglx.opengl.Sync` to provide the same jitter-free pacing.

### 2. Thread.yield() Elimination
Vanilla calls `Thread.yield()` in the render loop on every frame. On modern preemptive OS schedulers, this can cause the operating system to prematurely de-schedule the Minecraft render thread, creating random micro-stutters. FrameFlow removes this call by default.

### 3. Chunk Mesh Upload Time-Slicing
Chunk geometry compiled by background threads is uploaded to the GPU on the main render thread. In vanilla, an upload burst directly freezes the frame. FrameFlow monitors render-thread upload time and limits it to a configurable budget (default: 1.5 ms per frame), cleanly deferring the remaining upload queue across subsequent frames.

## Requirements

- Minecraft 1.12.2
- Minecraft Forge 14.23.5.x or Cleanroom 0.6.x
- MixinBooter 10.0 or newer (MixinExtras is bundled with MixinBooter 11.14+)
- Java 8 or newer

## Installation

Place `frameflow-<version>.jar` into your `.minecraft/mods` directory.

## Configuration

Configuration file: `config/frameflow.cfg`

| Option | Default | Description |
|---|---|---|
| `Enable Frame Pacing` | `true` | Use the monotonic hybrid frame pacer instead of `Display.sync`. |
| `Eliminate Thread Yield` | `true` | Remove the per-frame `Thread.yield()` call to prevent OS de-scheduling. |
| `Enable Chunk Upload Time Slicing` | `true` | Enforce a render-thread time slice on chunk VBO uploads. |
| `Chunk Upload Budget (ms)` | `1.5` | Maximum milliseconds per frame allowed for uploading compiled chunk meshes (range: 0.2 – 10.0 ms). |

## In-Game Commands

Available in singleplayer and on any multiplayer server:
- `/frameflow status` (or `/ff status`) — displays current pacing, yield, and chunk budget settings.
- `/frameflow pacing` — toggles the frame pacer on or off.
- `/frameflow yield` — toggles thread yield elimination on or off.
- `/frameflow slicing` — toggles chunk upload time-slicing on or off.
- `/frameflow budget <ms>` — dynamically sets the chunk upload time budget (0.2 – 10.0 ms).
- `/frameflow reload` — reloads configuration from disk.

## Compatibility

- **OptiFine**: Fully compatible. Mixin injections include fallback guards.
- **Nothirium / Valkyrie**: Compatible. If another mod replaces `ChunkRenderDispatcher`, time-slicing safely yields.
- **Entity Culling**: Compatible and recommended for occlusion culling (as FrameFlow focuses strictly on frame pacing and upload budgeting).

## License

GPL-3.0. Copyright (C) 2026 Brilliafy. See the [LICENSE](LICENSE) file.
