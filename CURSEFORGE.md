<div align="center">

# ⚡ FrameFlow ⚡
### **Nanosecond-Precision Monotonic Frame Pacing & Stutter Elimination for Minecraft 1.12.2**

**Client-Side Only | Forge 1.12.2 & Cleanroom | Requires MixinBooter 10.2+**

---

</div>

Have you ever looked at your in-game FPS counter showing a steady 60, 144, or 240 FPS, yet the game **still felt choppy, jittery, or micro-stuttery**—especially when turning the camera or traveling into new chunks?

That stutter isn't your GPU struggling. It's caused by **legacy flaws in Minecraft 1.12.2's render loop and frame limiter**. 

**FrameFlow** completely eliminates these hitches by fixing how Minecraft paces frames and uploads chunk geometry to your graphics card.

---

### 🌟 What Does FrameFlow Do?

#### 1. ⏱️ Monotonic Hybrid Frame Pacer (Eliminates Frame Jitter)
* **The Problem:** In Minecraft 1.12.2, capping your FPS uses LWJGL 2's ancient `Display.sync()` function. It relies on standard OS thread sleeping (`Thread.sleep()`), which has a 1 to 3 millisecond margin of error on modern Windows and Linux. Some frames finish too early, others too late—creating erratic frametime wobbles that cause noticeable visual stutter even at high FPS.
* **The Fix:** FrameFlow replaces the frame limiter with a high-precision, two-stage monotonic pacer:
  * **Coarse Stage:** Uses `LockSupport.parkNanos()` down to the last 2.0 ms.
  * **Fine Stage:** Microsecond-precision active spin-wait using `Thread.onSpinWait()` for the final 1.5 ms.
  * **Anti-Drift Re-Anchoring:** If a frame takes longer than intended (e.g. during a GC pause), FrameFlow dynamically resets its target forward so you never experience lagging frame queues.

#### 2. 🚫 Thread Yield Elimination (Stops Random Micro-Freezes)
* **The Problem:** On every frame pass, Minecraft calls `Thread.yield()`. On modern multi-core operating systems, this needlessly tells the OS scheduler: *"I'm done with my turn, give my CPU core to someone else."* The OS often de-schedules Minecraft for background tasks, causing sudden 5–15 ms micro-stutters.
* **The Fix:** FrameFlow safely bypasses this call, keeping the CPU core dedicated to rendering your game smoothly.

#### 3. 📦 Chunk Mesh Upload Time-Slicing (Stops World Travel Spikes)
* **The Problem:** When background threads finish building chunk geometry, all of those meshes must be uploaded to OpenGL on the main render thread. In vanilla, there is no time limit: if 15 chunks finish at the same time, the game halts and uploads all 15 at once—causing an immediate 30–60 ms freeze while exploring.
* **The Fix:** FrameFlow enforces a strict, configurable time slice (default **1.5 ms per frame**). Any remaining chunk uploads are cleanly spread across subsequent frames, completely eliminating chunk-loading freezes.

---

### 🎮 In-Game Commands

FrameFlow is **100% client-side** and includes a client-registered command that works in singleplayer AND on any remote multiplayer server without needing server permissions or plugins!

**Command Aliases:** `/frameflow` or `/ff`

* `/ff status` — Displays the live status of Frame Pacing, Yield Elimination, and Chunk Slicing.
* `/ff pacing` — Toggles the monotonic frame pacer on/off.
* `/ff yield` — Toggles thread yield elimination on/off.
* `/ff slicing` — Toggles chunk upload time-slicing on/off.
* `/ff budget <ms>` — Dynamically sets chunk upload budget (e.g. `/ff budget 2.0`).
* `/ff reload` — Reloads settings from `config/frameflow.cfg` instantly.

---

### ⚙️ Configuration (`config/frameflow.cfg`)

All options can be edited via the config file or in real-time using in-game commands:

| Setting | Default | Description |
|---|:---:|---|
| **Enable Frame Pacing** | `true` | Replaces `Display.sync` with nanosecond-precision hybrid pacing. |
| **Eliminate Thread Yield** | `true` | Eliminates per-frame `Thread.yield()` to keep CPU core affinity. |
| **Enable Chunk Upload Time Slicing** | `true` | Limits chunk mesh upload duration on the render thread. |
| **Chunk Upload Budget (ms)** | `1.5` | Max milliseconds allowed per frame for chunk uploads (0.2 – 10.0 ms). |

---

### 🧩 Compatibility & Recommended Companions

FrameFlow is built with safe `@WrapOperation` fallbacks (`require = 0`) to guarantee compatibility across the 1.12.2 mod ecosystem:

* **OptiFine:** Fully compatible.
* **Nothirium / Valkyrie:** Fully compatible. (If Nothirium replaces chunk rendering, FrameFlow safely defers without crashes).
* **Cleanroom / LWJGL3ify:** Native support with hooks into `org.lwjglx.opengl.Sync`.
* **CensoredASM / VintageFix / FoamFix:** Fully compatible.
* **Entity Culling:** **Highly Recommended!** Entity Culling optimizes what entities get drawn; FrameFlow optimizes *when* and *how smoothly* frames and chunk meshes are delivered. They make an ideal pair.
* **Dedicated Servers:** Client-only mod. It does nothing on a dedicated server and is completely safe to leave in client modpacks.

---

### 📥 Requirements & Installation

1. **Minecraft 1.12.2** running **Minecraft Forge** (14.23.5.2847+) or **Cleanroom** (0.6.x+).
2. **MixinBooter** (10.2 or higher, 11.14+ recommended).
3. Place `frameflow-<version>.jar` into your `.minecraft/mods` directory and enjoy buttery-smooth frametimes!
