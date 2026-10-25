# RenderFlow

Client-side rendering optimization for Minecraft 1.12.2 on Forge and Cleanroom.

Ever noticed that even when your FPS counter shows a steady 60 or 144 FPS, the game still feels slightly jittery when moving the camera or traveling into new chunks? That usually comes down to frame pacing jitter in LWJGL and chunk mesh upload spikes on the main render thread. RenderFlow addresses both.

### What it does

**Accurate Frame Pacing**
Vanilla limits frame rate using LWJGL's `Display.sync()`, which relies on `Thread.sleep()`. Operating system sleep timers are imprecise (often off by 1 to 2 milliseconds), which causes frames to finish slightly too early or too late. RenderFlow replaces this with a hybrid sleep-spin timer: it parks the thread for the bulk of the wait, then spin-waits the final 1.5ms with `Thread.onSpinWait()`. If a frame runs late, it re-anchors the timer to prevent drift.

**No Thread.yield()**
Minecraft calls `Thread.yield()` every frame inside `runGameLoop()`. On modern multi-core operating systems, yielding the CPU can cause the OS scheduler to hand CPU time away from the render thread to background tasks at bad moments. RenderFlow removes this call.

**Chunk Upload Time Slicing**
When chunk compilation threads finish meshing chunks, those meshes are uploaded to the GPU on the main render thread. In vanilla, all finished chunks are uploaded in a single frame with no budget, causing noticeable frame drops while exploring. RenderFlow limits chunk upload time to a configurable budget (default: 1.5ms per frame) and defers the rest of the queue to following frames.

### Commands

RenderFlow is completely client-side. The command works in singleplayer and on any multiplayer server:

- `/rf status` : Shows current pacer and upload budget settings
- `/rf pacing` : Toggles the custom frame pacer
- `/rf yield` : Toggles thread yield elimination
- `/rf slicing` : Toggles chunk upload time slicing
- `/rf budget <ms>` : Sets the chunk upload budget in milliseconds (default: 1.5)
- `/rf reload` : Reloads the config from disk

You can also use `/renderflow` instead of `/rf`.

### Configuration

Config file: `config/renderflow.cfg`

- `Enable Frame Pacing` (default: true): Use the custom hybrid frame pacer instead of `Display.sync`.
- `Eliminate Thread Yield` (default: true): Skip the per-frame `Thread.yield()` call.
- `Enable Chunk Upload Time Slicing` (default: true): Spread chunk mesh uploads across frames.
- `Chunk Upload Budget (ms)` (default: 1.5): Max upload time per frame (range: 0.2 to 10.0 ms).

### Compatibility

- **OptiFine**: Fully compatible.
- **Nothirium / Valkyrie**: Compatible. If another mod replaces chunk rendering, chunk upload slicing safely defers.
- **Entity Culling**: Highly recommended. Entity Culling skips rendering hidden entities while RenderFlow handles frame pacing and chunk upload spikes.
- **Cleanroom / LWJGL3ify**: Native support via `org.lwjglx.opengl.Sync`.
- **Servers**: Client-only. Does nothing if installed on a dedicated server.

### Requirements

- Minecraft 1.12.2
- Forge (14.23.5.2847+) or Cleanroom (0.6.x+)
- MixinBooter 10.2 or newer
