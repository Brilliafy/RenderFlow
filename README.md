# Render Flow

![RenderFlow](logo.png)

Client rendering optimization for Minecraft 1.12.2 on Cleanroom or Minecraft Forge.

This is a client-side mod. It does nothing on a dedicated server.

## Overview

Render Flow targets two common sources of client stutter: an imprecise frame limiter, and rendering
work that the player cannot actually see.

- A monotonic frame pacer replaces the vanilla `Display.sync` limiter.
- Chunk mesh uploads are spread over frames with a time budget.
- Offscreen entities and tile entities are skipped using a software occlusion test.

## Frame pacing

Vanilla limits the frame rate with `Display.sync`, which uses a coarse sleep loop and can overshoot or
undershoot the target. Render Flow replaces it with a hybrid pacer that parks the thread for the bulk
of the wait and then spin waits the last part with `LockSupport` and `Thread.onSpinWait`. The result is
a more even frame interval. The pacer re-anchors its target whenever a frame runs late, so it does not
drift or stall after a slow frame.

The mod can also remove the per-frame `Thread.yield()` call in the render loop. On most systems that
call is unnecessary and can hand the CPU away at a bad moment.

## Chunk upload time slicing

Compiled chunk meshes are uploaded to the GPU on the render thread. When many chunks finish compiling
at once, that upload burst shows up as a frame spike. Render Flow tracks how long the current frame has
spent uploading and defers the rest of the upload queue to the next frame once the budget is used. The
budget is configurable and defaults to 1.5 ms.

## Occlusion culling

Entities and tile entities behind solid geometry are skipped. The test casts a few rays from the
camera to the entity or block's bounding box and culls the object only when every ray hits an opaque
block first. Unloaded chunks are treated as not occluding, so the culler never hides something it
cannot see through. Boss entities and entities close to the camera are always kept.

Occlusion culling is the most situational part of the mod. In sparse terrain the raycasts can cost
more than they save, so it is toggleable and can be limited by distance.

## Requirements

- Minecraft 1.12.2
- Minecraft Forge 14.23.5.x or Cleanroom 0.6.x
- MixinBooter 10 or newer (MixinExtras is provided by MixinBooter 11.14 and newer)
- Java 8

## Installation

Put `renderflow-<version>.jar` into your `mods` folder. It is client only, so there is no reason to
install it on a server.

## Configuration

File: `config/renderflow.cfg`

### Frame Pacing

| Option | Default | Description |
|---|---|---|
| Enable Frame Pacing | true | Use the custom frame pacer instead of `Display.sync`. |
| Eliminate Thread Yield | true | Remove the per-frame `Thread.yield()` call. |
| Enable Chunk Upload Time Slicing | true | Spread chunk mesh uploads over frames. |
| Chunk Upload Budget (ms) | 1.5 | Maximum upload time per frame. |
| Enable Smart Animations | true | Throttle offscreen animated textures. |

### Occlusion Culling

| Option | Default | Description |
|---|---|---|
| Enable Mod | true | Turns occlusion culling on or off. |
| Cull Entities | true | Cull living entities and mobs. |
| Cull Tile Entities | true | Cull chests, spawners and other tile entities. |
| Min Cull Distance | 3.0 | Objects closer than this are never culled. |
| Max Raytrace Distance | 80.0 | Maximum distance used by the occlusion rays. |
| Exempt Bosses | true | Never cull bosses, dragons and mobs with health bars. |

## Commands

- `/renderflow stats` shows the culling counters and pacer state.
- `/renderflow clear` resets the counters.
- `/renderflow toggle` enables or disables occlusion culling.
- `/renderflow reload` reloads the config.

## Notes and limitations

- Occlusion culling can be a net loss in very open terrain. If you see no benefit, or notice objects
  popping in, turn it off or reduce `Max Raytrace Distance`.
- Frame pacing only applies when the game is frame limited. With the limit set to unlimited and vsync
  on, the game uses vsync instead.

## Credits

Written by Brilliafy.

## License

GPL-3.0. Copyright (C) 2026 Brilliafy. See the LICENSE file.
