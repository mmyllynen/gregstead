package com.lowdragmc.lowdraglib.client.shader.management;

import com.google.common.base.Charsets;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.apache.commons.io.IOUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL43;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.Objects;

public class Shader {
    private static final org.slf4j.Logger LOGGER = LogUtils.getLogger();
    public final ShaderType shaderType;
    public final String source;
    private int shaderId;
    private boolean isCompiled;
    private boolean deleted;

    public Shader(ShaderType type, String source) {
        this.shaderType = type;
        this.source = source;
    }

    public void attachShader(int programId) {
        if (deleted) throw new IllegalStateException("Shader has been deleted");
        if (!isCompiled) compileShader();
        GL20.glAttachShader(programId, this.shaderId);
    }

    public void deleteShader() {
        if (deleted) return;
        if (shaderId != 0) GL20.glDeleteShader(this.shaderId);
        shaderId = 0;
        deleted = true;
        isCompiled = false;
    }

    public Shader compileShader() {
        if (deleted) throw new IllegalStateException("Shader has been deleted");
        if (!this.isCompiled) {
            if (!(Minecraft.getInstance().gameRenderer.mainRenderTarget().getColorTexture()
                    instanceof com.mojang.renderpearl.backend.opengl.GlTexture)) {
                throw new UnsupportedOperationException("Raw OpenGL shader compilation requires OpenGL; use a native render pipeline");
            }
            if (shaderId == 0) {
                shaderId = shaderType.createShader();
                if (shaderId == 0) throw new IllegalStateException("GL Shader Allocation Fail!");
            }
            Shader.setShaderSourceHackForAmd(this.shaderId, this.source);
            GL20.glCompileShader(this.shaderId);
            if (GL20.glGetShaderi(this.shaderId, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
                int maxLength = GL20.glGetShaderi(this.shaderId, GL20.GL_INFO_LOG_LENGTH);
                String error = String.format("Unable to compile %s shader object:\n%s", this.shaderType.name(), GL20.glGetShaderInfoLog(this.shaderId, maxLength));
                LOGGER.error(error);
                throw new IllegalStateException(error);
            }
            this.isCompiled = true;
        }
        return this;
    }

    // Evil hack for AMD
    private static void setShaderSourceHackForAmd(int shaderId, String source) {
        // Load shader source as bytes
        byte[] bs = source.getBytes(Charsets.UTF_8);
        ByteBuffer byteBuffer = MemoryUtil.memAlloc(bs.length + 1);
        byteBuffer.put(bs);
        byteBuffer.put((byte)0);
        byteBuffer.flip();

        try {
            MemoryStack memoryStack = MemoryStack.stackPush();
            try {
                // Use unsafe shader load method for loading because safe version fails on AMD GPUs.
                PointerBuffer pointerBuffer = memoryStack.mallocPointer(1);
                pointerBuffer.put(byteBuffer);
                GL20C.nglShaderSource(shaderId, 1, pointerBuffer.address0(), 0L);
            } catch (Throwable err1) {
                if (memoryStack != null) {
                    try {
                        memoryStack.close();
                    } catch (Throwable err2) {
                        err1.addSuppressed(err2);
                    }
                }
                throw err1;
            }

            if (memoryStack != null) {
                memoryStack.close();
            }
        } finally {
            MemoryUtil.memFree(byteBuffer);
        }
    }

    public static Shader loadShader(ShaderType type, String rawShader) {
        Shader shader = new Shader(type, rawShader);
        try { return shader.compileShader(); }
        catch (RuntimeException | Error failure) { shader.deleteShader(); throw failure; }
    }

    public static Shader loadShader(ShaderType type, Identifier resourceLocation) throws IOException {
        Shader shader = loadSource(type, resourceLocation);
        try { return shader.compileShader(); }
        catch (RuntimeException | Error failure) { shader.deleteShader(); throw failure; }
    }

    /** Resource descriptors are backend-neutral; GPU compilation belongs to the rendering owner. */
    public static Shader loadSource(ShaderType type, Identifier resourceLocation) throws IOException {
        var maybeResource = Minecraft.getInstance().getResourceManager().getResource(resourceLocation);
        if (maybeResource.isPresent()) {
            var resource = maybeResource.get();
            InputStream stream = resource.open();
            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String line;
            while((line = reader.readLine()) != null) {
                stringBuilder.append(line).append('\n');
            }
            stream.close();
            IOUtils.closeQuietly(stream);
            return new Shader(type, stringBuilder.toString());
        }
        throw new IOException("found no resource with ID " + resourceLocation);
    }

    public enum ShaderType {
        VERTEX("vertex", ".vsh", GL20.GL_VERTEX_SHADER, GL20::glCreateShader),
        FRAGMENT("fragment", ".fsh", GL20.GL_FRAGMENT_SHADER, GL20::glCreateShader),
        COMPUTE("compute", ".comp", 0x91B9, id -> GL43.glCreateShader(GL43.GL_COMPUTE_SHADER));

        public final String shaderName;
        public final String shaderExtension;
        public final int shaderMode;
        public final Int2IntFunction shaderCreator;

        ShaderType(String shaderNameIn, String shaderExtensionIn, int shaderModeIn, Int2IntFunction shaderCreatorIn) {
            this.shaderName = shaderNameIn;
            this.shaderExtension = shaderExtensionIn;
            this.shaderMode = shaderModeIn;
            this.shaderCreator = shaderCreatorIn;
        }

        public int createShader() {
            return shaderCreator.get(this.shaderMode);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) return true;
        if (obj instanceof Shader shader) {
            return Objects.equals(shader.source, this.source) && shader.shaderType == this.shaderType;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(shaderType, source);
    }
}
