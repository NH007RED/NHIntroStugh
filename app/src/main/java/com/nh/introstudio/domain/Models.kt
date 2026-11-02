package com.nh.introstudio.domain

import org.json.JSONObject
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sqrt

enum class AspectRatio(val label: String, val sub: String, val rw: Int, val rh: Int) {
    R16_9("16:9", "YouTube", 16, 9),
    R9_16("9:16", "Reels / Shorts", 9, 16),
    R1_1("1:1", "Square", 1, 1),
    R4_5("4:5", "Social Media", 4, 5),
    R19_9("19:9", "Mobile", 19, 9)
}

enum class Quality(val label: String, val shortLabel: String, val refW: Int, val refH: Int) {
    HD("720p HD", "720p", 1280, 720),
    FHD("1080p Full HD", "1080p", 1920, 1080),
    QHD("1440p QHD", "1440p", 2560, 1440),
    UHD("2160p 4K Ultra HD", "4K UHD", 3840, 2160)
}

enum class Codec(val label: String, val mime: String) {
    H264("H.264 (AVC)", "video/avc"),
    H265("H.265 (HEVC)", "video/hevc")
}

enum class BgMode(val label: String, val desc: String) {
    AI_AUTO("Template Backdrop", "Template studio or cinematic background"),
    SOLID("Solid Color", "Clean solid color backdrop"),
    TRANSPARENT("Transparent", "Alpha overlay mode for layering"),
    CHROMA_GREEN("Chroma Key", "Green screen for Premiere / CapCut")
}

enum class LayoutMode(val label: String) {
    AUTO("Template Default"),
    HORIZONTAL("Horizontal [Logo | Text]"),
    CENTERED("Centered [Logo / Text]"),
    TEXT_HERO("Cinema Text Hero")
}

/**
 * Deeply integrated, broadcast-grade IN + IDLE + OUT transition physics so the logo,
 * typography, lighting, motion blur, and camera movement look fused into one seamless render.
 */
enum class InOutStyle(val label: String) {
    HYPER_ZOOM_THROUGH("Hyper-Zoom & Camera Fly-Through"),
    PARTICLE_VORTEX_DISSOLVE("Vortex Form & Particle Dissolve"),
    PRISM_CHROMATIC_WARP("Anamorphic Prism Split In/Out"),
    LIQUID_ELASTIC_MORPH("Liquid Glass Morph & Vacuum Implode"),
    LASER_LIGHT_SWEEP("Volumetric Light-Wipe & Vaporize")
}

enum class MascotType(val label: String) {
    BIRD("Emerald Songbird"),
    FALCON("Soaring Falcon"),
    BUTTERFLY("Monarch Butterfly"),
    ORIGAMI_PLANE("Origami Plane"),
    HUMMINGBIRD("Hummingbird"),
    PHOENIX("Fire Phoenix"),
    OWL("Academy Owl"),
    BEE("Golden Bee"),
    DRAGONFLY("Zen Dragonfly"),
    ROCKET("Mini Rocket")
}

enum class EmblemType(val label: String) {
    UNIVERSITY_CREST("University Shield Crest"),
    CROWN_SHIELD("Royal Crown Shield"),
    HEX_CUBE("Isometric Hex Cube"),
    WINGED_SHIELD("Hero Winged Shield"),
    DIAMOND_CREST("Faceted Diamond Star"),
    FLAME_SHIELD("Phoenix Flame Crest"),
    TORCH_SHIELD("Academy Compass Crest"),
    HONEYCOMB_CREST("Honeycomb Hex Badge"),
    LOTUS_CREST("Zen Lotus Seal"),
    ORBIT_SHIELD("Quantum Orbit Ring")
}

/** 20 distinct LogoAnimationAI logo reveal choreographies — plus seamless IN/IDLE/OUT fusion! */
enum class LogoStyle(val label: String) {
    MASCOT_VORTEX_MORPH("1. Bird Perch → Vortex → Crest"),
    SPLASH_DROP_BURST("2. Liquid Paint Drop & Splash"),
    GYRO_SPIN_3D("3. 3D Gyro Coin Flip & Extrusion"),
    ORIGAMI_FOLD("4. Origami Facet Unfold"),
    CYBER_GLITCH_SNAP("5. Cyber RGB Glitch Snap"),
    GALAXY_PARTICLE_CONVERGE("6. Galaxy Vortex Convergence"),
    LASER_PERIMETER_TRACE("7. Dual Laser Outline Trace"),
    FLAME_WALL_WIPE("8. Sweeping Flame Wall Forge"),
    SHUTTER_SPLIT_SLIDE("9. Minimal Split Shutter Slide"),
    NEON_TUBE_IGNITE("10. Electric Neon Tube Ignite"),
    BOUNCING_BALL_MORPH("11. Bouncing Ball Letter-Pop"),
    ROYAL_RIBBON_CROWN("12. Golden Ribbon & Crown Draw"),
    LIQUID_METABALL_FUSE("13. Liquid Metaball Fusion"),
    ISOMETRIC_CUBE_RISE("14. Isometric 3D Cube Matrix"),
    LIGHTNING_SHOCKWAVE("15. Lightning Strike Shockwave"),
    ZEN_INK_BRUSH("16. Zen Sumi-e Ink Brush Enso"),
    QUANTUM_PORTAL_WARP("17. Quantum Portal Ring Warp"),
    AUDIO_EQUALIZER_DROP("18. Equalizer Spectrum Beat Drop"),
    BLUEPRINT_WIREFRAME_SCAN("19. Blueprint Sketch & Scanner"),
    ANAMORPHIC_STAR_RISE("20. 4K Anamorphic Star Flare"),
    POP("Elastic Pop"),
    SPIN("2D Spin"),
    DROP("Bounce Drop"),
    RISE("Smooth Rise")
}

/**
 * 31 Typography Animation Engines:
 * - 11 Logo Lockup Text Styles
 * - 20 Streaming & Cinema Hero Text Intro Styles (Netflix, Prime, Hotstar, Marvel, Apple TV+, HBO, IMAX, Dolby, etc.)
 */
enum class TextStyle(val label: String) {
    // 11 Logo Lockup Text Styles
    CASCADE_STAIRCASE("Staircase Letter Drop"),
    BOUNCE_WAVE("Kinetic Ball Bounce"),
    SLIDE_UP("Masked Slide Up"),
    SPLIT_REVEAL("Center Split Reveal"),
    FADE_SCALE("Fade & Scale"),
    TYPEWRITER("Digital Typewriter"),
    GLITCH("RGB Chromatic Glitch"),
    ZOOM_BLUR("Depth Zoom Blur"),
    SPREAD("Cinematic Letter Spread"),
    SHINE("Metallic Light Sweep"),
    SIGNATURE("Laser / Ink Draw"),

    // 20 Streaming & Cinema Hero Text Intro Styles (Templates 21..40)
    STREAM_NETFLIX_PRISM("Netflix Ribbon & Prism Spectrum"),
    STREAM_PRIME_SMILE("Prime Video Cyan Smile Sweep"),
    STREAM_HOTSTAR_ARC("Hotstar Shooting Star Arc"),
    STREAM_MARVEL_FLIP("Marvel Flipbook Monolith"),
    STREAM_APPLE_GLASS("Apple TV+ Liquid Glass Focus"),
    STREAM_HBO_CHROME("HBO 3D Chrome Spotlight"),
    STREAM_IMAX_LASER("IMAX Dual Laser Projectors"),
    STREAM_DOLBY_SPHERE("Dolby Atmos Sound-Sphere"),
    STREAM_ORBIT_TEXT("Universal Planetary Orbit"),
    STREAM_BEAM_PILLAR("Columbia Torch Light Pillar"),
    STREAM_STADIUM_WAVE("JioCinema Neon Stadium Wave"),
    STREAM_AURORA_SILK("Zee5 Aurora Silk Ribbon"),
    STREAM_A24_EDITORIAL("A24 Split-Frame Editorial"),
    STREAM_GREEN_SONIC("Hulu Emerald Sonic Pulse"),
    STREAM_STAR_RING("Paramount 22-Star Halo Arc"),
    STREAM_GOLD_MONOLITH("Warner 3D Golden Shield"),
    STREAM_COLOR_BLOOM("Peacock 6-Petal Prism Bloom"),
    STREAM_RED_PLAY("YouTube Originals Red Prism"),
    STREAM_CYBER_CUT("Cyberpunk Diagonal Slash"),
    STREAM_TITANIUM_FORGE("Epic Trailer Molten Titanium")
}

enum class SfxType {
    WHOOSH, BOOM, SHIMMER, ZAP, BLIP, CHORD, FIRE, SWEEP, RISER,
    SPLASH, GLITCH_SFX, CINEMATIC_HIT
}

data class SfxEvent(val type: SfxType, val at: Float, val gain: Float = 1f)

data class TemplateSpec(
    val id: Int,
    val name: String,
    val category: String,
    val description: String,
    val c1: Int,
    val c2: Int,
    val c3: Int,
    val bg1: Int,
    val bg2: Int,
    val textColor: Int,
    val textStyle: TextStyle,
    val logoStyle: LogoStyle,
    val inOutStyle: InOutStyle,
    val mascot: MascotType,
    val emblem: EmblemType,
    val defaultHorizontal: Boolean,
    val revealAt: Float,
    val vignette: Float,
    val defaultMusic: String,
    val sfx: List<SfxEvent>,
    val isLogoAiFeatured: Boolean = true,
    val isTextIntro: Boolean = false,
    val isLightPaper: Boolean = false
)

data class ExportSettings(
    val quality: Quality = Quality.FHD,
    val aspect: AspectRatio = AspectRatio.R16_9,
    val fps: Int = 30,
    val codec: Codec = Codec.H264,
    val bitrateMbps: Float = 0f
) {
    fun dimensions(): Pair<Int, Int> = dimensionsFor(quality, aspect)

    fun effectiveBitrate(): Int {
        if (bitrateMbps > 0f) return (bitrateMbps * 1_000_000).roundToInt()
        val (w, h) = dimensions()
        val bpp = if (codec == Codec.H265) 0.07 else 0.10
        return max(2_000_000.0, w.toDouble() * h * fps * bpp).toInt()
    }

    companion object {
        fun dimensionsFor(q: Quality, a: AspectRatio): Pair<Int, Int> {
            if (a == AspectRatio.R16_9) return q.refW to q.refH
            val area = q.refW.toDouble() * q.refH
            val h = sqrt(area * a.rh / a.rw)
            val w = h * a.rw / a.rh
            return align(w) to align(h)
        }
        private fun align(v: Double): Int = max(16, (v / 16.0).roundToInt() * 16)
    }
}

data class BrandConfig(
    val templateId: Int = 1,
    val brandName: String = "Logoipsum",
    val tagline: String = "University",
    val subtitle: String = "",
    val showLogo: Boolean = true,
    val logoPath: String? = null,
    val removeLogoBg: Boolean = false,
    val motionPrompt: String = "",
    val layoutMode: LayoutMode = LayoutMode.AUTO,
    val inOutStyleOverride: InOutStyle? = null,
    val bgMode: BgMode = BgMode.AI_AUTO,
    val bgSolidColor: Int = 0xFFFCFDFB.toInt(),
    val bgImagePath: String? = null,
    val bgVideoPath: String? = null,
    val useCustomColors: Boolean = false,
    val colorPrimary: Int = 0xFF00B179.toInt(),
    val colorSecondary: Int = 0xFF7DD5B4.toInt(),
    val colorText: Int = 0xFF013929.toInt(),
    val fontIndex: Int = 0,
    val fontScale: Float = 1f,
    val textX: Float = 0.5f,
    val textY: Float = 0.5f,
    val logoX: Float = 0.5f,
    val logoY: Float = 0.36f,
    val logoScale: Float = 1f,
    val logoRotation: Float = 0f,
    val logoStyleOverride: LogoStyle? = null,
    val textStyleOverride: TextStyle? = null,
    val mascotOverride: MascotType? = null,
    val emblemOverride: EmblemType? = null,
    val speed: Float = 1f,
    val durationSec: Float = 7f,
    val aspect: AspectRatio = AspectRatio.R16_9,
    val musicSource: String = "builtin:logo_ai_signature",
    val musicName: String = "Logo AI Signature",
    val musicVolume: Float = 0.85f,
    val musicStartSec: Float = 0f,
    val musicEndSec: Float = 0f,
    val sfxEnabled: Boolean = true,
    val sfxVolume: Float = 0.85f,
    val watermarkEnabled: Boolean = false
) {
    fun toJson(): String = JSONObject().apply {
        put("templateId", templateId); put("brandName", brandName); put("tagline", tagline)
        put("subtitle", subtitle); put("showLogo", showLogo)
        put("logoPath", logoPath ?: JSONObject.NULL)
        put("removeLogoBg", removeLogoBg)
        put("motionPrompt", motionPrompt)
        put("layoutMode", layoutMode.name)
        put("inOutStyleOverride", inOutStyleOverride?.name ?: JSONObject.NULL)
        put("bgMode", bgMode.name)
        put("bgSolidColor", bgSolidColor)
        put("bgImagePath", bgImagePath ?: JSONObject.NULL)
        put("bgVideoPath", bgVideoPath ?: JSONObject.NULL)
        put("useCustomColors", useCustomColors)
        put("colorPrimary", colorPrimary); put("colorSecondary", colorSecondary); put("colorText", colorText)
        put("fontIndex", fontIndex); put("fontScale", fontScale.toDouble())
        put("textX", textX.toDouble()); put("textY", textY.toDouble())
        put("logoX", logoX.toDouble()); put("logoY", logoY.toDouble())
        put("logoScale", logoScale.toDouble()); put("logoRotation", logoRotation.toDouble())
        put("logoStyleOverride", logoStyleOverride?.name ?: JSONObject.NULL)
        put("textStyleOverride", textStyleOverride?.name ?: JSONObject.NULL)
        put("mascotOverride", mascotOverride?.name ?: JSONObject.NULL)
        put("emblemOverride", emblemOverride?.name ?: JSONObject.NULL)
        put("speed", speed.toDouble()); put("durationSec", durationSec.toDouble())
        put("aspect", aspect.name)
        put("musicSource", musicSource); put("musicName", musicName)
        put("musicVolume", musicVolume.toDouble())
        put("musicStartSec", musicStartSec.toDouble()); put("musicEndSec", musicEndSec.toDouble())
        put("sfxEnabled", sfxEnabled); put("sfxVolume", sfxVolume.toDouble())
        put("watermarkEnabled", watermarkEnabled)
    }.toString()

    companion object {
        fun fromJson(json: String): BrandConfig {
            val d = BrandConfig()
            return try {
                val o = JSONObject(json)
                fun str(k: String): String? = if (o.isNull(k)) null else o.optString(k).takeIf { it.isNotEmpty() }
                BrandConfig(
                    templateId = o.optInt("templateId", d.templateId),
                    brandName = o.optString("brandName", d.brandName),
                    tagline = o.optString("tagline", d.tagline),
                    subtitle = o.optString("subtitle", d.subtitle),
                    showLogo = o.optBoolean("showLogo", d.showLogo),
                    logoPath = str("logoPath"),
                    removeLogoBg = o.optBoolean("removeLogoBg", d.removeLogoBg),
                    motionPrompt = o.optString("motionPrompt", d.motionPrompt),
                    layoutMode = runCatching { LayoutMode.valueOf(o.optString("layoutMode", d.layoutMode.name)) }.getOrDefault(d.layoutMode),
                    inOutStyleOverride = str("inOutStyleOverride")?.let { s -> runCatching { InOutStyle.valueOf(s) }.getOrNull() },
                    bgMode = runCatching { BgMode.valueOf(o.optString("bgMode", d.bgMode.name)) }.getOrDefault(d.bgMode),
                    bgSolidColor = o.optInt("bgSolidColor", d.bgSolidColor),
                    bgImagePath = str("bgImagePath"),
                    bgVideoPath = str("bgVideoPath"),
                    useCustomColors = o.optBoolean("useCustomColors", d.useCustomColors),
                    colorPrimary = o.optInt("colorPrimary", d.colorPrimary),
                    colorSecondary = o.optInt("colorSecondary", d.colorSecondary),
                    colorText = o.optInt("colorText", d.colorText),
                    fontIndex = o.optInt("fontIndex", d.fontIndex),
                    fontScale = o.optDouble("fontScale", d.fontScale.toDouble()).toFloat(),
                    textX = o.optDouble("textX", d.textX.toDouble()).toFloat(),
                    textY = o.optDouble("textY", d.textY.toDouble()).toFloat(),
                    logoX = o.optDouble("logoX", d.logoX.toDouble()).toFloat(),
                    logoY = o.optDouble("logoY", d.logoY.toDouble()).toFloat(),
                    logoScale = o.optDouble("logoScale", d.logoScale.toDouble()).toFloat(),
                    logoRotation = o.optDouble("logoRotation", d.logoRotation.toDouble()).toFloat(),
                    logoStyleOverride = str("logoStyleOverride")?.let { s -> runCatching { LogoStyle.valueOf(s) }.getOrNull() },
                    textStyleOverride = str("textStyleOverride")?.let { s -> runCatching { TextStyle.valueOf(s) }.getOrNull() },
                    mascotOverride = str("mascotOverride")?.let { s -> runCatching { MascotType.valueOf(s) }.getOrNull() },
                    emblemOverride = str("emblemOverride")?.let { s -> runCatching { EmblemType.valueOf(s) }.getOrNull() },
                    speed = o.optDouble("speed", d.speed.toDouble()).toFloat(),
                    durationSec = o.optDouble("durationSec", d.durationSec.toDouble()).toFloat(),
                    aspect = runCatching { AspectRatio.valueOf(o.optString("aspect", d.aspect.name)) }.getOrDefault(d.aspect),
                    musicSource = o.optString("musicSource", d.musicSource),
                    musicName = o.optString("musicName", d.musicName),
                    musicVolume = o.optDouble("musicVolume", d.musicVolume.toDouble()).toFloat(),
                    musicStartSec = o.optDouble("musicStartSec", d.musicStartSec.toDouble()).toFloat(),
                    musicEndSec = o.optDouble("musicEndSec", d.musicEndSec.toDouble()).toFloat(),
                    sfxEnabled = o.optBoolean("sfxEnabled", d.sfxEnabled),
                    sfxVolume = o.optDouble("sfxVolume", d.sfxVolume.toDouble()).toFloat(),
                    watermarkEnabled = o.optBoolean("watermarkEnabled", d.watermarkEnabled)
                )
            } catch (e: Exception) {
                d
            }
        }
    }
}

data class FontOption(val label: String, val family: String, val bold: Boolean, val italic: Boolean = false)

val FONT_OPTIONS = listOf(
    FontOption("Modern Sans", "sans-serif", true),
    FontOption("Condensed", "sans-serif-condensed", true),
    FontOption("Black Impact", "sans-serif-black", false),
    FontOption("Light Elegant", "sans-serif-light", false),
    FontOption("Classic Serif", "serif", true),
    FontOption("Mono Code", "monospace", true),
    FontOption("Cursive", "cursive", false),
    FontOption("Casual", "casual", true)
)

data class MusicTrack(val id: String, val title: String, val file: String)

val BUILTIN_MUSIC = listOf(
    MusicTrack("logo_ai_signature", "Logo AI Signature", "audio/logo_ai_signature.wav"),
    MusicTrack("logo_ai_minimal", "Logo AI Minimal", "audio/logo_ai_minimal.wav"),
    MusicTrack("cinematic_pulse", "Cinematic Pulse", "audio/cinematic_pulse.wav"),
    MusicTrack("neon_drive", "Neon Drive", "audio/neon_drive.wav"),
    MusicTrack("royal_strings", "Royal Strings", "audio/royal_strings.wav"),
    MusicTrack("deep_space", "Deep Space", "audio/deep_space.wav")
)
