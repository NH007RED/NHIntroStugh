package com.nh.introstudio.exporter

import android.graphics.Bitmap
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLExt
import android.opengl.EGLSurface
import android.opengl.GLES20
import android.opengl.GLUtils
import android.view.Surface
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class TextureLimitException(val max: Int) : RuntimeException("GL_MAX_TEXTURE_SIZE=$max")

/** Hardware accelerated bridge: uploads each rendered Bitmap as an OpenGL ES texture and presents it to the MediaCodec input surface with an exact timestamp. */
class EncoderGl(surface: Surface, private val width: Int, private val height: Int) {
    private var dpy: EGLDisplay = EGL14.EGL_NO_DISPLAY
    private var ctx: EGLContext = EGL14.EGL_NO_CONTEXT
    private var surf: EGLSurface = EGL14.EGL_NO_SURFACE
    private var program = 0
    private var tex = 0
    private var texReady = false
    private val quad: FloatBuffer = ByteBuffer.allocateDirect(16 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply {
        put(floatArrayOf(-1f, -1f, 0f, 1f, 1f, -1f, 1f, 1f, -1f, 1f, 0f, 0f, 1f, 1f, 1f, 0f)); position(0)
    }

    init {
        try {
            dpy = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
            val ver = IntArray(2)
            if (dpy == EGL14.EGL_NO_DISPLAY || !EGL14.eglInitialize(dpy, ver, 0, ver, 1)) throw RuntimeException("EGL unavailable")
            val attrs = intArrayOf(
                EGL14.EGL_RED_SIZE, 8, EGL14.EGL_GREEN_SIZE, 8, EGL14.EGL_BLUE_SIZE, 8, EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT, EGLExt.EGL_RECORDABLE_ANDROID, 1, EGL14.EGL_NONE
            )
            val cfgs = arrayOfNulls<EGLConfig>(1)
            val n = IntArray(1)
            if (!EGL14.eglChooseConfig(dpy, attrs, 0, cfgs, 0, 1, n, 0) || n[0] == 0) throw RuntimeException("No EGL config")
            ctx = EGL14.eglCreateContext(dpy, cfgs[0], EGL14.EGL_NO_CONTEXT, intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE), 0)
            surf = EGL14.eglCreateWindowSurface(dpy, cfgs[0], surface, intArrayOf(EGL14.EGL_NONE), 0)
            if (!EGL14.eglMakeCurrent(dpy, surf, surf, ctx)) throw RuntimeException("eglMakeCurrent failed")

            val maxTex = IntArray(1)
            GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, maxTex, 0)
            if (maxTex[0] in 1 until maxOf(width, height)) throw TextureLimitException(maxTex[0])

            program = buildProgram(
                "attribute vec4 aPos; attribute vec2 aTex; varying vec2 vTex; void main(){ gl_Position=aPos; vTex=aTex; }",
                "precision mediump float; uniform sampler2D uTex; varying vec2 vTex; void main(){ gl_FragColor=texture2D(uTex,vTex); }"
            )
            val t = IntArray(1)
            GLES20.glGenTextures(1, t, 0)
            tex = t[0]
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        } catch (e: Throwable) {
            release()
            throw e
        }
    }

    fun draw(bitmap: Bitmap, ptsNanos: Long) {
        GLES20.glViewport(0, 0, width, height)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex)
        if (!texReady) {
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
            texReady = true
        } else {
            GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, bitmap)
        }
        GLES20.glUseProgram(program)
        val aPos = GLES20.glGetAttribLocation(program, "aPos")
        val aTex = GLES20.glGetAttribLocation(program, "aTex")
        quad.position(0)
        GLES20.glVertexAttribPointer(aPos, 2, GLES20.GL_FLOAT, false, 16, quad)
        GLES20.glEnableVertexAttribArray(aPos)
        quad.position(2)
        GLES20.glVertexAttribPointer(aTex, 2, GLES20.GL_FLOAT, false, 16, quad)
        GLES20.glEnableVertexAttribArray(aTex)
        GLES20.glUniform1i(GLES20.glGetUniformLocation(program, "uTex"), 0)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        EGLExt.eglPresentationTimeANDROID(dpy, surf, ptsNanos)
        EGL14.eglSwapBuffers(dpy, surf)
    }

    fun release() {
        try {
            if (dpy != EGL14.EGL_NO_DISPLAY) {
                EGL14.eglMakeCurrent(dpy, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
                if (surf != EGL14.EGL_NO_SURFACE) EGL14.eglDestroySurface(dpy, surf)
                if (ctx != EGL14.EGL_NO_CONTEXT) EGL14.eglDestroyContext(dpy, ctx)
                EGL14.eglReleaseThread()
                EGL14.eglTerminate(dpy)
            }
        } catch (_: Throwable) {
        }
        dpy = EGL14.EGL_NO_DISPLAY; ctx = EGL14.EGL_NO_CONTEXT; surf = EGL14.EGL_NO_SURFACE
    }

    private fun buildProgram(vs: String, fs: String): Int {
        val v = compile(GLES20.GL_VERTEX_SHADER, vs)
        val f = compile(GLES20.GL_FRAGMENT_SHADER, fs)
        val p = GLES20.glCreateProgram()
        GLES20.glAttachShader(p, v); GLES20.glAttachShader(p, f)
        GLES20.glLinkProgram(p)
        val ok = IntArray(1)
        GLES20.glGetProgramiv(p, GLES20.GL_LINK_STATUS, ok, 0)
        if (ok[0] == 0) throw RuntimeException("GL link failed: " + GLES20.glGetProgramInfoLog(p))
        return p
    }

    private fun compile(type: Int, src: String): Int {
        val s = GLES20.glCreateShader(type)
        GLES20.glShaderSource(s, src)
        GLES20.glCompileShader(s)
        val ok = IntArray(1)
        GLES20.glGetShaderiv(s, GLES20.GL_COMPILE_STATUS, ok, 0)
        if (ok[0] == 0) throw RuntimeException("GL shader failed: " + GLES20.glGetShaderInfoLog(s))
        return s
    }

    companion object {
        @Volatile private var cachedMax = -1

        /** Largest texture side this GPU supports (cached). Used by the compatibility check. */
        fun maxTextureSize(): Int {
            if (cachedMax > 0) return cachedMax
            var dpy: EGLDisplay = EGL14.EGL_NO_DISPLAY
            var ctx: EGLContext = EGL14.EGL_NO_CONTEXT
            var surf: EGLSurface = EGL14.EGL_NO_SURFACE
            try {
                dpy = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
                val ver = IntArray(2)
                EGL14.eglInitialize(dpy, ver, 0, ver, 1)
                val attrs = intArrayOf(EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT, EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT, EGL14.EGL_NONE)
                val cfgs = arrayOfNulls<EGLConfig>(1)
                val n = IntArray(1)
                EGL14.eglChooseConfig(dpy, attrs, 0, cfgs, 0, 1, n, 0)
                ctx = EGL14.eglCreateContext(dpy, cfgs[0], EGL14.EGL_NO_CONTEXT, intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE), 0)
                surf = EGL14.eglCreatePbufferSurface(dpy, cfgs[0], intArrayOf(EGL14.EGL_WIDTH, 1, EGL14.EGL_HEIGHT, 1, EGL14.EGL_NONE), 0)
                EGL14.eglMakeCurrent(dpy, surf, surf, ctx)
                val m = IntArray(1)
                GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, m, 0)
                cachedMax = if (m[0] > 0) m[0] else 4096
            } catch (e: Throwable) {
                cachedMax = 4096
            } finally {
                try {
                    if (dpy != EGL14.EGL_NO_DISPLAY) {
                        EGL14.eglMakeCurrent(dpy, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
                        if (surf != EGL14.EGL_NO_SURFACE) EGL14.eglDestroySurface(dpy, surf)
                        if (ctx != EGL14.EGL_NO_CONTEXT) EGL14.eglDestroyContext(dpy, ctx)
                        EGL14.eglReleaseThread()
                        EGL14.eglTerminate(dpy)
                    }
                } catch (_: Throwable) {
                }
            }
            return cachedMax
        }
    }
}
