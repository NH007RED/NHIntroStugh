# NH Intro Studio v4 (Deep Unified Logo In/Out Engine + 20 Streaming Text Intro Templates)

Package: `com.nh.introstudio` · minSdk 26 · targetSdk 34 · Kotlin 1.9.24 · Jetpack Compose + Material 3 · MVVM

Offline Android app featuring **40 Broadcast-Grade Procedural Templates** across **2 Dedicated Studios**:
1. **Logo Animation AI Studio (Templates 1–20)**: 20 distinct AI Logo Animation choreographies upgraded with the **Deep Unified IN/IDLE/OUT Compositing Engine** (`InOutStyle`) so the logo, typography, and scene lighting look fused into one inseparable broadcast render.
2. **Streaming & Cinema Text Intro Studio (Templates 21–40)**: 20 Hero Text Intro templates inspired by **Netflix, Amazon Prime Video, Disney+ Hotstar, Marvel, Apple TV+, HBO, IMAX, Dolby Atmos, Universal, Columbia, JioCinema, Zee5, A24, Hulu, Paramount, Warner, Peacock, YouTube Originals, Cyberpunk, and Epic Trailers**.

## What's New in v4
### 1. Deep Unified Logo In/Idle/Out Engine (`IntroRenderer.kt` & `Effects.kt`)
- **Unified Camera Dolly & 3D Parallax**: The entire scene (background, logo, text, and foreground light motes) moves together inside a unified camera transform (`0.96x -> 1.03x` dolly push-in).
- **Sub-Frame Velocity Motion Blur**: Fast entrances and spins draw translucent sub-frame velocity echoes along the motion trajectory.
- **Unified Foreground Light-Wrap & Specular Sheen (`drawUnifiedForegroundLightWrap`)**: A diagonal specular light sweep and ambient color bridge pass *across the top* of both the logo and the brand text simultaneously, binding them into the same lighting environment.
- **5 Physical IN/OUT Transitions (`InOutStyle`)**:
  1. `HYPER_ZOOM_THROUGH` — Camera accelerates *through* the logo and text on exit (`scale 1x -> 4.6x`) with 40 radial warp streaks and anamorphic flash.
  2. `PARTICLE_VORTEX_DISSOLVE` — Logo and text disintegrate into 160 swirling glowing embers and stardust particles carried away by a wind vortex.
  3. `PRISM_CHROMATIC_WARP` — Chromatic RGB prism split on entrance and horizontal photon-line implosion on exit.
  4. `LIQUID_ELASTIC_MORPH` — Elastic anticipation swell (`1.14x`) followed by a rapid vacuum implosion (`0.0x`) and dual shockwave rings.
  5. `LASER_LIGHT_SWEEP` — A vertical high-energy laser light-curtain sweeps across the lockup, vaporizing the logo and text in its wake.

### 2. Dedicated Streaming & Cinema Text Intro Studio (`TextIntroScreen.kt`, Templates 21–40)
Accessible from the new **Text Intros** tab in the bottom navigation bar and the Home screen:
- **21. Crimson N-Ribbon Prism** (*Netflix style*: 3D crimson initial/wordmark zooming into 64 vertical prismatic light-spectrum bars with double bass boom)
- **22. Prime Cyan Smile Arc** (*Amazon Prime Video style*: Kinetic pop-in letters with a sweeping electric-cyan smile arrow underneath)
- **23. Hotstar Shooting Star Arc** (*Disney+ Hotstar style*: Parabolic gold-and-cyan shooting star raining stardust over a metallic wordmark)
- **24. Hero Flipbook Monolith** (*Marvel style*: Rapid storyboard action lines flipping inside giant impact letters that lock into a crimson monolith)
- **25. Apple Glass Refraction** (*Apple TV+ style*: Frosted liquid-glass caustic rings pulling optical focus into a crisp wordmark)
- **26. HBO 3D Chrome Spotlight** (*HBO style*: Analog static resolving into a 12-layer 3D chrome-extruded wordmark with sweeping overhead spotlight)
- **27. IMAX Dual Laser Beams** (*IMAX style*: Twin cobalt-blue volumetric projector cones cutting through cinema haze)
- **28. Dolby Sound-Sphere Gold** (*Dolby Atmos style*: Concentric champagne-gold acoustic sound-spheres)
- **29. Universal Planetary Orbit** (*Universal style*: Glowing planetary horizon sunrise with wide-tracked orbiting letters)
- **30. Torch Light Beam Pillar** (*Columbia/Sony style*: Central pillar of warm golden god-rays and starburst flash)
- **31. JioCinema Magenta Wave** (*JioCinema style*: Hot-pink and violet neon stadium waves)
- **32. Aurora Silk Ribbon Text** (*Zee5 style*: Multi-color aurora silk waves weaving through sinusoidal typography)
- **33. A24 Editorial Split-Cut** (*A24 style*: Indie cinema geometric frame cuts and diagonal shear typography)
- **34. Hulu Emerald Sonic Pulse** (*Hulu style*: Concentric `#1CE783` emerald sonic wave frames)
- **35. Paramount 22-Star Halo** (*Paramount style*: 22 golden-white stars swooping into a mountain halo arch)
- **36. Warner Golden Monolith** (*Warner style*: Rotating 3D golden shield watermark unfolding into a golden studio title)
- **37. Peacock 6-Color Bloom** (*Peacock style*: 6 translucent jewel-toned light petals fanning out behind the wordmark)
- **38. Red Play-Triangle Split** (*YouTube Originals style*: Crimson play-triangle splitting open horizontally around the channel title)
- **39. Cyberpunk Kinetic Slash** (*Cyberpunk style*: High-voltage `#FCEE09` yellow & `#00F0FF` cyan diagonal razor-cut title)
- **40. Molten Titanium Trailer** (*Epic Trailer style*: 12-layer brushed-titanium letters with glowing molten-orange fissures and rising embers)
