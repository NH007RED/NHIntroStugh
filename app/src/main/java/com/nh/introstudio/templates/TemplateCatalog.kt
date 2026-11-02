package com.nh.introstudio.templates

import com.nh.introstudio.domain.*
import com.nh.introstudio.domain.EmblemType.*
import com.nh.introstudio.domain.InOutStyle.*
import com.nh.introstudio.domain.LogoStyle.*
import com.nh.introstudio.domain.MascotType.*
import com.nh.introstudio.domain.SfxType.*
import com.nh.introstudio.domain.TextStyle.*

private fun c(rgb: Int): Int = 0xFF000000.toInt() or rgb
private fun s(t: SfxType, at: Float, g: Float = 1f) = SfxEvent(t, at, g)

/**
 * 40 Broadcast-Grade Templates in NH Intro Studio v4:
 * - Templates 1..20: 20 Distinct AI Logo Animation Templates with Deep Unified IN/IDLE/OUT Fusion
 *   (sub-frame velocity motion blur, unified SRC_ATOP specular light sweep across logo & text,
 *    volumetric light-wrap bloom, 3D camera dolly, and seamless outro transitions).
 * - Templates 21..40: 20 Streaming & Cinema Hero Text Intro Templates inspired by
 *   Netflix, Amazon Prime Video, Disney+ Hotstar, Marvel, Apple TV+, HBO, IMAX, Dolby, Universal,
 *   Sony/Columbia, JioCinema, Zee5, A24, Hulu, Paramount, Warner, Peacock, YouTube Originals, Cyberpunk, and Epic Trailers!
 */
object TemplateCatalog {

    val all: List<TemplateSpec> = listOf(
        // =========================================================================================================
        // SECTION 1: 20 AI LOGO ANIMATION TEMPLATES (1..20) WITH DEEP UNIFIED IN/IDLE/OUT FUSION
        // =========================================================================================================
        TemplateSpec(
            1, "Bird & Vortex Crest", "Mascot AI",
            "Staircase letters drop in, an emerald songbird perches on 'L', hops left into a swirling vortex, and flashes into a shield crest with unified light wrap.",
            c(0x00B179), c(0x7DD5B4), c(0xFFFFFF), c(0xFCFDFB), c(0xF4F8F6), c(0x013929),
            CASCADE_STAIRCASE, MASCOT_VORTEX_MORPH, PARTICLE_VORTEX_DISSOLVE, BIRD, UNIVERSITY_CREST,
            defaultHorizontal = true, revealAt = 2.4f, vignette = 0.04f, defaultMusic = "logo_ai_signature",
            sfx = listOf(s(BLIP, 0.15f, .35f), s(BLIP, 0.35f, .35f), s(WHOOSH, 1.4f, .45f), s(SWEEP, 2.5f, .45f), s(WHOOSH, 5.2f, .65f), s(SPLASH, 5.85f, .65f), s(CINEMATIC_HIT, 6.5f, .8f), s(SHIMMER, 6.75f, .65f)),
            isLogoAiFeatured = true, isTextIntro = false, isLightPaper = true
        ),
        TemplateSpec(
            2, "Paint Splash Burst", "Liquid AI",
            "Three glossy paint drops collide at the center, splashing into organic ink blobs and ripples with unified liquid-glass reflections and vacuum implode outro.",
            c(0xFF2D55), c(0x00E5FF), c(0xFFE600), c(0xFAF9FC), c(0xF1EEF7), c(0x181028),
            SLIDE_UP, SPLASH_DROP_BURST, LIQUID_ELASTIC_MORPH, HUMMINGBIRD, DIAMOND_CREST,
            defaultHorizontal = false, revealAt = 0.85f, vignette = 0.06f, defaultMusic = "logo_ai_minimal",
            sfx = listOf(s(WHOOSH, 0.1f, .6f), s(SPLASH, 0.8f, .95f), s(CINEMATIC_HIT, 0.9f, .7f), s(SHIMMER, 1.4f, .5f)),
            isLogoAiFeatured = true, isTextIntro = false, isLightPaper = true
        ),
        TemplateSpec(
            3, "3D Gyro Coin Spin", "3D Motion",
            "Triple 3D gyroscope rings orbit a metallic stage while your logo rotates in on a 3D Y-axis with 6-layer depth extrusion and hyper-zoom camera fly-through.",
            c(0x6366F1), c(0x38BDF8), c(0xE0E7FF), c(0x0A0D1A), c(0x03050A), c(0xFFFFFF),
            SHINE, GYRO_SPIN_3D, HYPER_ZOOM_THROUGH, FALCON, CROWN_SHIELD,
            defaultHorizontal = false, revealAt = 0.75f, vignette = 0.45f, defaultMusic = "logo_ai_minimal",
            sfx = listOf(s(SWEEP, 0.05f, .7f), s(WHOOSH, 0.55f, .8f), s(CINEMATIC_HIT, 1.25f, .9f), s(SHIMMER, 1.65f, .6f)),
            isLogoAiFeatured = true, isTextIntro = false, isLightPaper = false
        ),
        TemplateSpec(
            4, "Origami Facet Unfold", "Geometric",
            "Geometric triangular origami facets unfold step-by-step from the center into an isometric hex emblem with unified laser light-wipe.",
            c(0x0284C7), c(0x38BDF8), c(0xFFFFFF), c(0xF8FAFC), c(0xEFF6FF), c(0x0F172A),
            SPLIT_REVEAL, ORIGAMI_FOLD, LASER_LIGHT_SWEEP, ORIGAMI_PLANE, HEX_CUBE,
            defaultHorizontal = true, revealAt = 0.65f, vignette = 0.05f, defaultMusic = "logo_ai_minimal",
            sfx = listOf(s(BLIP, 0.15f, .5f), s(BLIP, 0.40f, .5f), s(WHOOSH, 0.70f, .7f), s(CINEMATIC_HIT, 1.35f, .85f)),
            isLogoAiFeatured = true, isTextIntro = false, isLightPaper = true
        ),
        TemplateSpec(
            5, "Digital Glitch Snap", "Cyberpunk",
            "High-speed RGB chromatic aberration splits, CRT scanlines and horizontal displacement slices snap your logo into focus and prism-warp out.",
            c(0x00FFE1), c(0xFF0055), c(0xFFFFFF), c(0x060812), c(0x010205), c(0xFFFFFF),
            GLITCH, CYBER_GLITCH_SNAP, PRISM_CHROMATIC_WARP, ROCKET, HEX_CUBE,
            defaultHorizontal = true, revealAt = 0.70f, vignette = 0.50f, defaultMusic = "neon_drive",
            sfx = listOf(s(GLITCH_SFX, 0.15f, .9f), s(ZAP, 0.65f, .8f), s(CINEMATIC_HIT, 1.05f, .9f)),
            isLogoAiFeatured = true, isTextIntro = false, isLightPaper = false
        ),
        TemplateSpec(
            6, "Particle Burst Vortex", "Particles",
            "240 glowing multi-color stardust particles spiral inward along 4 galaxy arms, coalescing into your logo and dissolving back into stardust on exit.",
            c(0xA855F7), c(0xEC4899), c(0x38BDF8), c(0x080417), c(0x020107), c(0xFFFFFF),
            FADE_SCALE, GALAXY_PARTICLE_CONVERGE, PARTICLE_VORTEX_DISSOLVE, BUTTERFLY, ORBIT_SHIELD,
            defaultHorizontal = false, revealAt = 1.25f, vignette = 0.50f, defaultMusic = "deep_space",
            sfx = listOf(s(RISER, 0f, .75f), s(BOOM, 1.25f, .85f), s(SHIMMER, 1.45f, .75f)),
            isLogoAiFeatured = true, isTextIntro = false, isLightPaper = false
        ),
        TemplateSpec(
            7, "Laser Outline Trace", "Technology",
            "Dual precision laser beams shoot from the top corners, tracing your emblem perimeter with glowing sparks and vaporizing on exit.",
            c(0x00E5FF), c(0x8B5CF6), c(0xFFFFFF), c(0x040914), c(0x000000), c(0xFFFFFF),
            SIGNATURE, LASER_PERIMETER_TRACE, LASER_LIGHT_SWEEP, FALCON, DIAMOND_CREST,
            defaultHorizontal = false, revealAt = 1.15f, vignette = 0.50f, defaultMusic = "neon_drive",
            sfx = listOf(s(ZAP, 0.1f, .65f), s(SWEEP, 0.35f, .7f), s(CINEMATIC_HIT, 1.20f, .9f), s(SHIMMER, 1.55f, .5f)),
            isLogoAiFeatured = true, isTextIntro = false, isLightPaper = false
        ),
        TemplateSpec(
            8, "Fire Flame Sweep", "Action",
            "A roaring diagonal wall of clean flames and swirling ember trails sweeps across the screen, forging your glowing logo and dissolving into embers.",
            c(0xFF5500), c(0xFFC700), c(0xFFF5CC), c(0x160500), c(0x040100), c(0xFFFFFF),
            ZOOM_BLUR, FLAME_WALL_WIPE, PARTICLE_VORTEX_DISSOLVE, PHOENIX, FLAME_SHIELD,
            defaultHorizontal = true, revealAt = 0.85f, vignette = 0.55f, defaultMusic = "cinematic_pulse",
            sfx = listOf(s(WHOOSH, 0.2f, .8f), s(FIRE, 0.45f, .95f), s(BOOM, 0.95f, .85f)),
            isLogoAiFeatured = true, isTextIntro = false, isLightPaper = false
        ),
        TemplateSpec(
            9, "Minimal Split Shutter", "Minimal",
            "Swiss-style diagonal shutter planes slide apart while a center split-line opens to reveal your logo and typography in opposing masks.",
            c(0x0F172A), c(0x2563EB), c(0x93C5FD), c(0xFAFBFD), c(0xF1F5F9), c(0x0F172A),
            SPLIT_REVEAL, SHUTTER_SPLIT_SLIDE, LASER_LIGHT_SWEEP, BIRD, UNIVERSITY_CREST,
            defaultHorizontal = true, revealAt = 0.65f, vignette = 0.04f, defaultMusic = "logo_ai_minimal",
            sfx = listOf(s(WHOOSH, 0.1f, .65f), s(CINEMATIC_HIT, 0.75f, .85f), s(SHIMMER, 1.3f, .45f)),
            isLogoAiFeatured = true, isTextIntro = false, isLightPaper = true
        ),
        TemplateSpec(
            10, "Neon Tube Ignition", "Cyberpunk",
            "Concentric cyan and magenta neon tubes trace around your logo, flickering like real glass neon before flying through the camera.",
            c(0x00F5D4), c(0xFF2A85), c(0xFFFFFF), c(0x070312), c(0x020106), c(0xFFFFFF),
            SHINE, NEON_TUBE_IGNITE, PRISM_CHROMATIC_WARP, DRAGONFLY, LOTUS_CREST,
            defaultHorizontal = false, revealAt = 0.80f, vignette = 0.55f, defaultMusic = "neon_drive",
            sfx = listOf(s(ZAP, 0.15f, .6f), s(ZAP, 0.45f, .75f), s(CHORD, 0.85f, .75f), s(SHIMMER, 1.4f, .5f)),
            isLogoAiFeatured = true, isTextIntro = false, isLightPaper = false
        ),
        TemplateSpec(
            11, "Kinetic Bouncing Ball", "Kinetic",
            "A playful glowing sphere bounces across each letter of your brand name—popping them up in a wave—then lands on the left and morphs into your logo.",
            c(0xEA580C), c(0xFBBF24), c(0xFFFFFF), c(0xFFFDF9), c(0xFEF3C7), c(0x1C1917),
            BOUNCE_WAVE, BOUNCING_BALL_MORPH, LIQUID_ELASTIC_MORPH, BEE, HONEYCOMB_CREST,
            defaultHorizontal = true, revealAt = 1.65f, vignette = 0.04f, defaultMusic = "logo_ai_signature",
            sfx = listOf(s(BLIP, 0.25f, .6f), s(BLIP, 0.55f, .6f), s(BLIP, 0.85f, .6f), s(BLIP, 1.15f, .6f), s(SPLASH, 1.70f, .85f), s(CINEMATIC_HIT, 1.95f, .85f)),
            isLogoAiFeatured = true, isTextIntro = false, isLightPaper = true
        ),
        TemplateSpec(
            12, "Royal Gold Ribbon", "Luxury",
            "Flowing golden silk calligraphy ribbons spiral across deep velvet, drawing an ornamental frame and crown emblem with drifting gold dust.",
            c(0xFFD27A), c(0xD97706), c(0xFFFFFF), c(0x140C1C), c(0x050208), c(0xFFF5D6),
            SIGNATURE, ROYAL_RIBBON_CROWN, PARTICLE_VORTEX_DISSOLVE, FALCON, CROWN_SHIELD,
            defaultHorizontal = false, revealAt = 1.10f, vignette = 0.55f, defaultMusic = "royal_strings",
            sfx = listOf(s(WHOOSH, 0.25f, .65f), s(CHORD, 1.15f, .8f), s(SHIMMER, 1.65f, .65f)),
            isLogoAiFeatured = true, isTextIntro = false, isLightPaper = false
        ),
        TemplateSpec(
            13, "Liquid Metaball Morph", "Liquid AI",
            "Six glossy liquid mercury streams orbit inward from all directions, colliding and fusing at the center into your smooth brand emblem.",
            c(0x0D9488), c(0x2DD4BF), c(0x99F6E4), c(0xF2FBF9), c(0xE6F7F3), c(0x0F2926),
            SPREAD, LIQUID_METABALL_FUSE, LIQUID_ELASTIC_MORPH, DRAGONFLY, LOTUS_CREST,
            defaultHorizontal = true, revealAt = 1.05f, vignette = 0.05f, defaultMusic = "logo_ai_minimal",
            sfx = listOf(s(SWEEP, 0.1f, .65f), s(SPLASH, 1.0f, .85f), s(CHORD, 1.25f, .7f)),
            isLogoAiFeatured = true, isTextIntro = false, isLightPaper = true
        ),
        TemplateSpec(
            14, "3D Cube Matrix", "3D Motion",
            "An isometric grid of 3D hexagonal cubes rises in a wave, locking together at the center to form your 3D brand block and title.",
            c(0x4F46E5), c(0x818CF8), c(0xC7D2FE), c(0x0B0F1E), c(0x04060C), c(0xFFFFFF),
            TYPEWRITER, ISOMETRIC_CUBE_RISE, HYPER_ZOOM_THROUGH, ORIGAMI_PLANE, HEX_CUBE,
            defaultHorizontal = true, revealAt = 0.95f, vignette = 0.45f, defaultMusic = "logo_ai_minimal",
            sfx = listOf(s(BLIP, 0.15f, .5f), s(BLIP, 0.35f, .5f), s(BLIP, 0.55f, .5f), s(CINEMATIC_HIT, 1.05f, .9f)),
            isLogoAiFeatured = true, isTextIntro = false, isLightPaper = false
        ),
        TemplateSpec(
            15, "Lightning Shockwave", "Action",
            "Forked high-voltage lightning bolts strike the center, triggering a thunder flash and dual plasma shockwaves that reveal your logo.",
            c(0x38BDF8), c(0xA855F7), c(0xFFFFFF), c(0x040918), c(0x010206), c(0xFFFFFF),
            ZOOM_BLUR, LIGHTNING_SHOCKWAVE, PRISM_CHROMATIC_WARP, FALCON, WINGED_SHIELD,
            defaultHorizontal = false, revealAt = 0.90f, vignette = 0.60f, defaultMusic = "cinematic_pulse",
            sfx = listOf(s(WHOOSH, 0.1f, .6f), s(ZAP, 0.35f, .9f), s(ZAP, 0.75f, .9f), s(BOOM, 0.90f, 1.0f)),
            isLogoAiFeatured = true, isTextIntro = false, isLightPaper = false
        ),
        TemplateSpec(
            16, "Zen Ink Brush Enso", "Creative",
            "An expressive calligraphic ink brush sweeps a circular Enso stroke over warm rice paper with ink splatters, unveiling your seal and title.",
            c(0x1E293B), c(0xE11D48), c(0x94A3B8), c(0xFAF7F2), c(0xEFECE4), c(0x0F172A),
            SIGNATURE, ZEN_INK_BRUSH, PARTICLE_VORTEX_DISSOLVE, BIRD, LOTUS_CREST,
            defaultHorizontal = true, revealAt = 0.95f, vignette = 0.06f, defaultMusic = "royal_strings",
            sfx = listOf(s(WHOOSH, 0.15f, .8f), s(SPLASH, 0.65f, .6f), s(CINEMATIC_HIT, 1.05f, .8f)),
            isLogoAiFeatured = true, isTextIntro = false, isLightPaper = true
        ),
        TemplateSpec(
            17, "Quantum Portal Warp", "Space",
            "A spinning interdimensional portal ring opens with tangential fiery sparks as your logo warps through from deep space.",
            c(0xF97316), c(0x38BDF8), c(0xFFFFFF), c(0x070510), c(0x010103), c(0xFFFFFF),
            ZOOM_BLUR, QUANTUM_PORTAL_WARP, HYPER_ZOOM_THROUGH, ROCKET, ORBIT_SHIELD,
            defaultHorizontal = false, revealAt = 1.10f, vignette = 0.55f, defaultMusic = "deep_space",
            sfx = listOf(s(RISER, 0f, .8f), s(SWEEP, 0.5f, .7f), s(BOOM, 1.15f, .9f), s(SHIMMER, 1.5f, .6f)),
            isLogoAiFeatured = true, isTextIntro = false, isLightPaper = false
        ),
        TemplateSpec(
            18, "Equalizer Beat Drop", "Music & Media",
            "Circular and horizontal audio spectrum bars pulse to the rhythm around a glowing bass core that drops into your brand mark.",
            c(0xEC4899), c(0x8B5CF6), c(0x22D3EE), c(0x090414), c(0x020106), c(0xFFFFFF),
            SLIDE_UP, AUDIO_EQUALIZER_DROP, LIQUID_ELASTIC_MORPH, HUMMINGBIRD, HONEYCOMB_CREST,
            defaultHorizontal = true, revealAt = 0.85f, vignette = 0.50f, defaultMusic = "neon_drive",
            sfx = listOf(s(BLIP, 0.2f, .5f), s(RISER, 0.1f, .7f), s(BOOM, 0.85f, .95f), s(CHORD, 1.0f, .7f)),
            isLogoAiFeatured = true, isTextIntro = false, isLightPaper = false
        ),
        TemplateSpec(
            19, "Blueprint Wireframe Scan", "Architecture",
            "Drafting compass arcs, degree ticks and wireframe construction lines sketch your emblem first, then a glowing scan-bar fills it in full color.",
            c(0x2563EB), c(0x06B6D4), c(0xFFFFFF), c(0xF4F8FD), c(0xE5EFFA), c(0x0C2D57),
            TYPEWRITER, BLUEPRINT_WIREFRAME_SCAN, LASER_LIGHT_SWEEP, OWL, TORCH_SHIELD,
            defaultHorizontal = true, revealAt = 0.80f, vignette = 0.05f, defaultMusic = "logo_ai_minimal",
            sfx = listOf(s(BLIP, 0.1f, .5f), s(BLIP, 0.35f, .5f), s(SWEEP, 0.75f, .8f), s(CINEMATIC_HIT, 1.35f, .85f)),
            isLogoAiFeatured = true, isTextIntro = false, isLightPaper = true
        ),
        TemplateSpec(
            20, "4K Anamorphic Flare", "Cinematic",
            "Widescreen anamorphic blue-gold lens streaks and volumetric floating dust illuminate your rising emblem with a 4-point star flare.",
            c(0x3B82F6), c(0xFBBF24), c(0xFFFFFF), c(0x050914), c(0x010205), c(0xFFFFFF),
            SPREAD, ANAMORPHIC_STAR_RISE, HYPER_ZOOM_THROUGH, FALCON, UNIVERSITY_CREST,
            defaultHorizontal = false, revealAt = 0.95f, vignette = 0.55f, defaultMusic = "cinematic_pulse",
            sfx = listOf(s(RISER, 0f, .75f), s(CINEMATIC_HIT, 0.95f, .95f), s(CHORD, 1.15f, .8f), s(SHIMMER, 1.6f, .6f)),
            isLogoAiFeatured = true, isTextIntro = false, isLightPaper = false
        ),

        // =========================================================================================================
        // SECTION 2: 20 STREAMING & CINEMA HERO TEXT INTRO TEMPLATES (21..40)
        // Inspired by Netflix, Amazon Prime Video, Disney+ Hotstar, Marvel, Apple TV+, HBO, IMAX, Dolby, etc.
        // =========================================================================================================

        // 21 — Netflix "N" Ribbon & Prism Light Spectrum
        TemplateSpec(
            21, "Crimson N-Ribbon Prism", "Streaming Text",
            "Netflix-style 3D crimson initial ribbon unfolds into your bold red wordmark, then accelerates into dozens of prismatic light-spectrum bars with a double bass boom.",
            c(0xE50914), c(0xFF5A5F), c(0x00E5FF), c(0x0A0102), c(0x000000), c(0xE50914),
            STREAM_NETFLIX_PRISM, ANAMORPHIC_STAR_RISE, HYPER_ZOOM_THROUGH, FALCON, UNIVERSITY_CREST,
            defaultHorizontal = false, revealAt = 0.45f, vignette = 0.65f, defaultMusic = "cinematic_pulse",
            sfx = listOf(s(CINEMATIC_HIT, 0.25f, 1.0f), s(BOOM, 0.55f, 1.0f), s(RISER, 1.8f, .85f), s(WHOOSH, 3.2f, .95f), s(SHIMMER, 3.5f, .6f)),
            isLogoAiFeatured = false, isTextIntro = true, isLightPaper = false
        ),

        // 22 — Prime Video Cyan Smile Sweep
        TemplateSpec(
            22, "Prime Cyan Smile Arc", "Streaming Text",
            "Prime-style crisp geometric letters snap into view over midnight blue while a glowing electric-cyan smile arrow sweeps from first to last letter.",
            c(0x00A8E1), c(0x38BDF8), c(0xFFFFFF), c(0x000814), c(0x000205), c(0xFFFFFF),
            STREAM_PRIME_SMILE, SHUTTER_SPLIT_SLIDE, LASER_LIGHT_SWEEP, BIRD, DIAMOND_CREST,
            defaultHorizontal = false, revealAt = 0.40f, vignette = 0.50f, defaultMusic = "logo_ai_minimal",
            sfx = listOf(s(WHOOSH, 0.2f, .7f), s(SWEEP, 0.9f, .85f), s(CHORD, 1.35f, .8f), s(SHIMMER, 1.8f, .65f)),
            isLogoAiFeatured = false, isTextIntro = true, isLightPaper = false
        ),

        // 23 — Hotstar / Disney+ Shooting Star Constellation Arc
        TemplateSpec(
            23, "Hotstar Shooting Star Arc", "Streaming Text",
            "A radiant gold-and-cyan shooting star arcs overhead across a royal twilight sky, raining stardust over your metallic wordmark and igniting a 4-point star flare.",
            c(0x38BDF8), c(0xFBBF24), c(0xFFFFFF), c(0x04081C), c(0x01030A), c(0xFFFFFF),
            STREAM_HOTSTAR_ARC, ANAMORPHIC_STAR_RISE, PARTICLE_VORTEX_DISSOLVE, BIRD, DIAMOND_CREST,
            defaultHorizontal = false, revealAt = 0.50f, vignette = 0.55f, defaultMusic = "royal_strings",
            sfx = listOf(s(SWEEP, 0.15f, .75f), s(SHIMMER, 1.1f, .85f), s(CHORD, 1.45f, .85f), s(CINEMATIC_HIT, 1.6f, .75f)),
            isLogoAiFeatured = false, isTextIntro = true, isLightPaper = false
        ),

        // 24 — Marvel Flipbook Monolith
        TemplateSpec(
            24, "Hero Flipbook Monolith", "Streaming Text",
            "Rapid storyboard panels flip at high speed inside giant impact letters as the camera pulls back to lock your bold white title inside a glowing crimson block.",
            c(0xED1D24), c(0xFBBF24), c(0xFFFFFF), c(0x1A0304), c(0x050000), c(0xFFFFFF),
            STREAM_MARVEL_FLIP, GYRO_SPIN_3D, HYPER_ZOOM_THROUGH, FALCON, WINGED_SHIELD,
            defaultHorizontal = false, revealAt = 0.35f, vignette = 0.60f, defaultMusic = "cinematic_pulse",
            sfx = listOf(s(BLIP, 0.1f, .5f), s(BLIP, 0.25f, .5f), s(BLIP, 0.4f, .5f), s(RISER, 0.3f, .8f), s(BOOM, 1.35f, 1.0f)),
            isLogoAiFeatured = false, isTextIntro = true, isLightPaper = false
        ),

        // 25 — Apple TV+ Liquid Glass Refraction & Focus
        TemplateSpec(
            25, "Apple Glass Refraction", "Streaming Text",
            "Frosted liquid-glass caustic light rings refract through an obsidian studio, smoothly pulling focus from soft optical bloom into a razor-sharp wordmark.",
            c(0xA5B4FC), c(0x38BDF8), c(0xFFFFFF), c(0x050508), c(0x000000), c(0xFFFFFF),
            STREAM_APPLE_GLASS, LIQUID_METABALL_FUSE, PRISM_CHROMATIC_WARP, BIRD, ORBIT_SHIELD,
            defaultHorizontal = false, revealAt = 0.50f, vignette = 0.55f, defaultMusic = "deep_space",
            sfx = listOf(s(RISER, 0f, .65f), s(CINEMATIC_HIT, 1.1f, .9f), s(CHORD, 1.25f, .85f)),
            isLogoAiFeatured = false, isTextIntro = true, isLightPaper = false
        ),

        // 26 — HBO 3D Chrome Extrusion & Overhead Spotlight
        TemplateSpec(
            26, "HBO 3D Chrome Spotlight", "Streaming Text",
            "Analog static micro-sparks resolve into a monolithic 3D chrome-extruded title as an overhead studio spotlight sweeps across the bevels, casting dynamic shadows.",
            c(0xD1D5DB), c(0x60A5FA), c(0xFFFFFF), c(0x090A0F), c(0x000000), c(0xF9FAFB),
            STREAM_HBO_CHROME, GYRO_SPIN_3D, LASER_LIGHT_SWEEP, FALCON, CROWN_SHIELD,
            defaultHorizontal = false, revealAt = 0.45f, vignette = 0.60f, defaultMusic = "royal_strings",
            sfx = listOf(s(ZAP, 0.1f, .55f), s(SWEEP, 0.5f, .7f), s(CHORD, 1.2f, .9f), s(SHIMMER, 1.8f, .6f)),
            isLogoAiFeatured = false, isTextIntro = true, isLightPaper = false
        ),

        // 27 — IMAX Dual Laser Projector Countdown
        TemplateSpec(
            27, "IMAX Dual Laser Beams", "Streaming Text",
            "Sweeping cobalt-blue dual laser projector cones cut through cinema haze, illuminating monumental wide-tracked typography with sub-bass resonance.",
            c(0x0072CE), c(0x38BDF8), c(0xFFFFFF), c(0x020814), c(0x000000), c(0xFFFFFF),
            STREAM_IMAX_LASER, LASER_PERIMETER_TRACE, HYPER_ZOOM_THROUGH, FALCON, DIAMOND_CREST,
            defaultHorizontal = false, revealAt = 0.50f, vignette = 0.60f, defaultMusic = "deep_space",
            sfx = listOf(s(RISER, 0f, .85f), s(BOOM, 1.1f, 1.0f), s(SWEEP, 1.4f, .7f)),
            isLogoAiFeatured = false, isTextIntro = true, isLightPaper = false
        ),

        // 28 — Dolby Atmos Acoustic Sound-Sphere
        TemplateSpec(
            28, "Dolby Sound-Sphere Gold", "Streaming Text",
            "Concentric 3D acoustic sound-spheres ripple outward in champagne gold, coalescing into twin symmetrically framed emblems and cinema typography.",
            c(0xF59E0B), c(0xFDE68A), c(0xFFFFFF), c(0x0A0804), c(0x000000), c(0xFEF3C7),
            STREAM_DOLBY_SPHERE, AUDIO_EQUALIZER_DROP, LIQUID_ELASTIC_MORPH, BEE, HONEYCOMB_CREST,
            defaultHorizontal = false, revealAt = 0.55f, vignette = 0.60f, defaultMusic = "cinematic_pulse",
            sfx = listOf(s(SWEEP, 0.1f, .7f), s(BOOM, 1.0f, .95f), s(CHORD, 1.15f, .85f), s(SHIMMER, 1.6f, .6f)),
            isLogoAiFeatured = false, isTextIntro = true, isLightPaper = false
        ),

        // 29 — Universal Planetary Horizon Orbit
        TemplateSpec(
            29, "Universal Planetary Orbit", "Streaming Text",
            "Your brand title arcs and orbits across a glowing golden-blue planetary horizon line with volumetric sunrise flare and majestic strings.",
            c(0x38BDF8), c(0xFBBF24), c(0xFFFFFF), c(0x03091A), c(0x000104), c(0xFFFFFF),
            STREAM_ORBIT_TEXT, QUANTUM_PORTAL_WARP, HYPER_ZOOM_THROUGH, ROCKET, ORBIT_SHIELD,
            defaultHorizontal = false, revealAt = 0.50f, vignette = 0.55f, defaultMusic = "royal_strings",
            sfx = listOf(s(RISER, 0f, .75f), s(CHORD, 1.2f, .9f), s(BOOM, 1.3f, .8f), s(SHIMMER, 1.9f, .65f)),
            isLogoAiFeatured = false, isTextIntro = true, isLightPaper = false
        ),

        // 30 — Sony / Columbia Torch Light Pillar
        TemplateSpec(
            30, "Torch Light Beam Pillar", "Streaming Text",
            "A blinding central pillar of warm golden-white god-rays parts atmospheric clouds as monumental cinema letters emerge from the light.",
            c(0xFBBF24), c(0xFEF08A), c(0xFFFFFF), c(0x0C0904), c(0x020100), c(0xFFFFFF),
            STREAM_BEAM_PILLAR, ANAMORPHIC_STAR_RISE, LASER_LIGHT_SWEEP,PHOENIX, TORCH_SHIELD,
            defaultHorizontal = false, revealAt = 0.50f, vignette = 0.55f, defaultMusic = "royal_strings",
            sfx = listOf(s(SWEEP, 0f, .7f), s(CHORD, 1.1f, .9f), s(SHIMMER, 1.6f, .7f)),
            isLogoAiFeatured = false, isTextIntro = true, isLightPaper = false
        ),

        // 31 — JioCinema Pink-Violet Stadium Wave
        TemplateSpec(
            31, "JioCinema Magenta Wave", "Streaming Text",
            "Vibrant hot-pink and violet neon stadium waves erupt with sparkling light showers behind bold kinetic streaming typography.",
            c(0xD9027D), c(0x8B5CF6), c(0x38BDF8), c(0x120214), c(0x040006), c(0xFFFFFF),
            STREAM_STADIUM_WAVE, SPLASH_DROP_BURST, PARTICLE_VORTEX_DISSOLVE, BUTTERFLY, CROWN_SHIELD,
            defaultHorizontal = false, revealAt = 0.45f, vignette = 0.50f, defaultMusic = "neon_drive",
            sfx = listOf(s(WHOOSH, 0.15f, .75f), s(CINEMATIC_HIT, 0.9f, .9f), s(SHIMMER, 1.3f, .75f)),
            isLogoAiFeatured = false, isTextIntro = true, isLightPaper = false
        ),

        // 32 — Zee5 Aurora Silk Ribbon Text
        TemplateSpec(
            32, "Aurora Silk Ribbon Text", "Streaming Text",
            "Flowing multi-color aurora silk waves weave through and behind each letter of your brand name, leaving a luminous gradient trail.",
            c(0xEC4899), c(0x06B6D4), c(0xFBBF24), c(0x090416), c(0x020106), c(0xFFFFFF),
            STREAM_AURORA_SILK, ROYAL_RIBBON_CROWN, LIQUID_ELASTIC_MORPH, DRAGONFLY, LOTUS_CREST,
            defaultHorizontal = false, revealAt = 0.45f, vignette = 0.50f, defaultMusic = "logo_ai_minimal",
            sfx = listOf(s(SWEEP, 0.1f, .7f), s(CHORD, 1.0f, .8f), s(SHIMMER, 1.5f, .65f)),
            isLogoAiFeatured = false, isTextIntro = true, isLightPaper = false
        ),

        // 33 — A24 Minimalist Split-Frame Editorial
        TemplateSpec(
            33, "A24 Editorial Split-Cut", "Streaming Text",
            "Indie cinema editorial title card with rhythmic geometric frame cuts, warm film grain, and high-contrast typographic lockups.",
            c(0xF8FAFC), c(0xF43F5E), c(0xFBBF24), c(0x0E0E10), c(0x050506), c(0xFFFFFF),
            STREAM_A24_EDITORIAL, SHUTTER_SPLIT_SLIDE, LASER_LIGHT_SWEEP, BIRD, UNIVERSITY_CREST,
            defaultHorizontal = false, revealAt = 0.40f, vignette = 0.45f, defaultMusic = "logo_ai_minimal",
            sfx = listOf(s(BLIP, 0.2f, .6f), s(BLIP, 0.5f, .6f), s(CINEMATIC_HIT, 0.9f, .85f)),
            isLogoAiFeatured = false, isTextIntro = true, isLightPaper = false
        ),

        // 34 — Hulu Emerald-Lime Sonic Pulse
        TemplateSpec(
            34, "Hulu Emerald Sonic Pulse", "Streaming Text",
            "Electric emerald-lime acoustic wave frames expand and contract around ultra-crisp modern typography with a snappy synth bass pop.",
            c(0x1CE783), c(0x34D399), c(0xFFFFFF), c(0x04120B), c(0x010503), c(0x1CE783),
            STREAM_GREEN_SONIC, NEON_TUBE_IGNITE, LIQUID_ELASTIC_MORPH, HUMMINGBIRD, HEX_CUBE,
            defaultHorizontal = false, revealAt = 0.45f, vignette = 0.55f, defaultMusic = "logo_ai_minimal",
            sfx = listOf(s(WHOOSH, 0.2f, .7f), s(CINEMATIC_HIT, 0.85f, .9f), s(SHIMMER, 1.2f, .5f)),
            isLogoAiFeatured = false, isTextIntro = true, isLightPaper = false
        ),

        // 35 — Paramount 22-Star Mountain Halo Arc
        TemplateSpec(
            35, "Paramount 22-Star Halo", "Streaming Text",
            "22 glowing five-pointed stars swoop in an elliptical arc from the foreground to form a majestic halo ring above your brand title.",
            c(0x60A5FA), c(0xFDE68A), c(0xFFFFFF), c(0x050C1E), c(0x010308), c(0xFFFFFF),
            STREAM_STAR_RING, GALAXY_PARTICLE_CONVERGE, PARTICLE_VORTEX_DISSOLVE, FALCON, CROWN_SHIELD,
            defaultHorizontal = false, revealAt = 0.55f, vignette = 0.55f, defaultMusic = "royal_strings",
            sfx = listOf(s(SWEEP, 0.1f, .7f), s(SHIMMER, 0.9f, .8f), s(CHORD, 1.3f, .85f)),
            isLogoAiFeatured = false, isTextIntro = true, isLightPaper = false
        ),

        // 36 — Warner 3D Golden Shield Monolith
        TemplateSpec(
            36, "Warner Golden Monolith", "Streaming Text",
            "A gleaming 3D golden shield monolith rotates through golden clouds and unfolds directly into your wide-screen studio wordmark.",
            c(0xF59E0B), c(0x3B82F6), c(0xFFFFFF), c(0x061026), c(0x01040C), c(0xFEF3C7),
            STREAM_GOLD_MONOLITH, GYRO_SPIN_3D, HYPER_ZOOM_THROUGH, FALCON, UNIVERSITY_CREST,
            defaultHorizontal = false, revealAt = 0.50f, vignette = 0.55f, defaultMusic = "royal_strings",
            sfx = listOf(s(WHOOSH, 0.2f, .7f), s(CHORD, 1.15f, .85f), s(SHIMMER, 1.65f, .65f)),
            isLogoAiFeatured = false, isTextIntro = true, isLightPaper = false
        ),

        // 37 — Peacock 6-Petal Prism Color Bloom
        TemplateSpec(
            37, "Peacock 6-Color Bloom", "Streaming Text",
            "Six translucent jewel-toned light petals (gold, orange, crimson, violet, blue, emerald) fan out and fold into your vibrant brand title.",
            c(0xEC4899), c(0x3B82F6), c(0xFBBF24), c(0x070610), c(0x010103), c(0xFFFFFF),
            STREAM_COLOR_BLOOM, SPLASH_DROP_BURST, PRISM_CHROMATIC_WARP, BUTTERFLY, LOTUS_CREST,
            defaultHorizontal = false, revealAt = 0.45f, vignette = 0.50f, defaultMusic = "logo_ai_minimal",
            sfx = listOf(s(SWEEP, 0.15f, .7f), s(CINEMATIC_HIT, 0.95f, .85f), s(SHIMMER, 1.3f, .65f)),
            isLogoAiFeatured = false, isTextIntro = true, isLightPaper = false
        ),

        // 38 — YouTube Originals Red Play-Prism Split
        TemplateSpec(
            38, "Red Play-Triangle Split", "Streaming Text",
            "A glowing crimson play-triangle splits open horizontally with kinetic precision to unveil your bold channel title, then snaps shut on exit.",
            c(0xFF0033), c(0xFF5A5F), c(0xFFFFFF), c(0x0E0507), c(0x030101), c(0xFFFFFF),
            STREAM_RED_PLAY, SHUTTER_SPLIT_SLIDE, LASER_LIGHT_SWEEP, ORIGAMI_PLANE, HEX_CUBE,
            defaultHorizontal = false, revealAt = 0.40f, vignette = 0.50f, defaultMusic = "logo_ai_minimal",
            sfx = listOf(s(WHOOSH, 0.15f, .75f), s(CINEMATIC_HIT, 0.75f, .9f), s(BLIP, 1.2f, .5f)),
            isLogoAiFeatured = false, isTextIntro = true, isLightPaper = false
        ),

        // 39 — Cyberpunk Diagonal Kinetic Slash
        TemplateSpec(
            39, "Cyberpunk Kinetic Slash", "Streaming Text",
            "High-voltage cyber-yellow and cyan diagonal razor cuts slice your brand title across HUD telemetry grids with glitch-cut transitions.",
            c(0xFCEE09), c(0x00F0FF), c(0xFF003C), c(0x08090F), c(0x020205), c(0xFCEE09),
            STREAM_CYBER_CUT, CYBER_GLITCH_SNAP, PRISM_CHROMATIC_WARP, ROCKET, HEX_CUBE,
            defaultHorizontal = false, revealAt = 0.40f, vignette = 0.55f, defaultMusic = "neon_drive",
            sfx = listOf(s(GLITCH_SFX, 0.15f, .9f), s(ZAP, 0.55f, .85f), s(BOOM, 0.9f, .95f)),
            isLogoAiFeatured = false, isTextIntro = true, isLightPaper = false
        ),

        // 40 — Epic Trailer Molten Titanium Forge
        TemplateSpec(
            40, "Molten Titanium Trailer", "Streaming Text",
            "Superheated molten orange fissures glow inside monumental brushed-titanium letters before cooling into chrome with rising embers and a trailer braaam.",
            c(0xFF5500), c(0x94A3B8), c(0xFDE047), c(0x0D0604), c(0x020100), c(0xF1F5F9),
            STREAM_TITANIUM_FORGE, FLAME_WALL_WIPE, HYPER_ZOOM_THROUGH, PHOENIX, FLAME_SHIELD,
            defaultHorizontal = false, revealAt = 0.50f, vignette = 0.65f, defaultMusic = "cinematic_pulse",
            sfx = listOf(s(RISER, 0f, .85f), s(FIRE, 0.4f, .9f), s(BOOM, 1.1f, 1.0f), s(CHORD, 1.25f, .8f)),
            isLogoAiFeatured = false, isTextIntro = true, isLightPaper = false
        )
    )


    val logoAiTemplates: List<TemplateSpec> get() = all.filter { !it.isTextIntro }
    val textIntroTemplates: List<TemplateSpec> get() = all.filter { it.isTextIntro }

    fun byId(id: Int): TemplateSpec = all.firstOrNull { it.id == id } ?: all.first()

    /** Fresh configuration for a template, automatically configuring Logo AI vs Streaming Text Intro defaults. */
    fun defaultConfig(id: Int): BrandConfig {
        val spec = byId(id)
        val track = BUILTIN_MUSIC.firstOrNull { it.id == spec.defaultMusic } ?: BUILTIN_MUSIC.first()
        val isText = spec.isTextIntro
        val dur = if (spec.id == 1) 8f else 6f
        return BrandConfig(
            templateId = spec.id,
            brandName = if (isText) "NETSTREAM" else "Logoipsum",
            tagline = if (isText) "ORIGINALS" else "University",
            showLogo = !isText,
            layoutMode = if (isText) LayoutMode.TEXT_HERO else LayoutMode.AUTO,
            durationSec = dur,
            bgSolidColor = spec.bg1,
            colorPrimary = spec.c1,
            colorSecondary = spec.c2,
            colorText = spec.textColor,
            musicSource = "builtin:${track.id}",
            musicName = track.title
        )
    }
}
