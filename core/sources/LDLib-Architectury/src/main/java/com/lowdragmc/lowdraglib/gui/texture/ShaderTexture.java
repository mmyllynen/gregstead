package com.lowdragmc.lowdraglib.gui.texture;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.client.shader.Shaders;
import com.lowdragmc.lowdraglib.client.shader.management.Shader;
import com.lowdragmc.lowdraglib.client.shader.management.ShaderManager;
import com.lowdragmc.lowdraglib.client.shader.management.ShaderProgram;
import com.lowdragmc.lowdraglib.client.shader.uniform.UniformCache;
import com.lowdragmc.lowdraglib.editor.annotation.*;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.registry.annotation.LDLRegisterClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@LDLRegisterClient(name = "shader_texture", registry = "ldlib:gui_texture")
public class ShaderTexture extends TransformTexture {
    private static final Map<Identifier, ShaderTexture> CACHE = new HashMap<>();

    @Configurable(name = "ldlib.gui.editor.name.resource", tips = "ldlib.gui.editor.tips.shader_location")
    public Identifier location;

    @OnlyIn(Dist.CLIENT)
    private ShaderProgram program;
    private com.lowdragmc.lowdraglib.client.shader.management.NativeShaderTextureProgram nativeProgram;

    @OnlyIn(Dist.CLIENT)
    private Shader shader;

    @Configurable(tips = "ldlib.gui.editor.tips.shader_resolution")
    @NumberRange(range = {1, 3})
    private float resolution = 2;

    @Configurable
    @NumberColor
    private int color = -1;

    private Consumer<UniformCache> uniformCache;
    private final Map<String, Object> pendingTextures = new java.util.LinkedHashMap<>();

    private final boolean isRaw;

    private ShaderTexture(boolean isRaw) {
        this.isRaw = isRaw;
    }

    public ShaderTexture() {
        this(false);
        this.location = Identifier.fromNamespaceAndPath(LDLib.MOD_ID, "fbm");
        if (LDLib.isRemote() && ShaderManager.allowedShader()) {
            Shader shader = Shaders.load(Shader.ShaderType.FRAGMENT, location);
            if (shader == null) return;
            this.shader = shader;
        }
    }

    public static void clearCache() {
        CACHE.values().forEach(ShaderTexture::dispose);
        CACHE.clear();
    }

    public void dispose() {
        if (isRaw && shader != null) {
            shader.deleteShader();
        }
        if (program != null) {
            program.delete();
        }
        if (nativeProgram != null) nativeProgram.close();
        nativeProgram = null;
        shader = null;
        program = null;
        pendingTextures.clear();
    }

    @Override
    public ShaderTexture setColor(int color) {
        this.color = color;
        return this;
    }

    @ConfigSetter(field = "location")
    public void updateShader(Identifier location) {
        if (LDLib.isRemote() && ShaderManager.allowedShader()) {
            this.location = location;
            dispose();
            Shader shader = Shaders.load(Shader.ShaderType.FRAGMENT, location);
            if (shader == null) return;
            this.shader = shader;
        }
    }

    public void updateRawShader(String rawShader) {
        if (LDLib.isRemote() && ShaderManager.allowedShader()) {
            dispose();
            shader = new Shader(Shader.ShaderType.FRAGMENT, rawShader);
        }
    }

    public String getRawShader() {
        if (LDLib.isRemote() && ShaderManager.allowedShader() && shader != null) {
            return shader.source;
        }
        return "";
    }

    @OnlyIn(Dist.CLIENT)
    private ShaderTexture(Shader shader, boolean isRaw) {
        this.isRaw = isRaw;
        if (shader == null) return;
        this.shader = shader;
    }

    public static ShaderTexture createShader(Identifier location) {
        if (CACHE.containsKey(location) && CACHE.get(location).shader != null) {
            return CACHE.get(location);
        }
        ShaderTexture texture;
        if (LDLib.isRemote() && ShaderManager.allowedShader()) {
            Shader shader = Shaders.load(Shader.ShaderType.FRAGMENT, location);
            texture = new ShaderTexture(shader, false);
            CACHE.put(location, texture);
        } else {
            texture = new ShaderTexture(false);
        }
        texture.location = location;
        return texture;
    }

    public static ShaderTexture createRawShader(String rawShader) {
        if (LDLib.isRemote() && ShaderManager.allowedShader()) {
            Shader shader = new Shader(Shader.ShaderType.FRAGMENT, rawShader);
            return new ShaderTexture(shader, true);
        } else {
            return new ShaderTexture(true);
        }
    }

    public ShaderTexture setUniformCache(Consumer<UniformCache> uniformCache) {
        this.uniformCache = uniformCache;
        return this;
    }

    public ShaderTexture setResolution(float resolution) {
        this.resolution = resolution;
        return this;
    }

    public float getResolution() {
        return resolution;
    }

    public void bindTexture(String samplerName, int id) {
        if (LDLib.isRemote() && ShaderManager.allowedShader()) {
            pendingTextures.put(samplerName, id);
            if (program != null) program.bindTexture(samplerName, id);
        }
    }

    public void bindTexture(String samplerName, Identifier location) {
        if (LDLib.isRemote() && ShaderManager.allowedShader()) {
            pendingTextures.put(samplerName, location);
            if (program != null) program.bindTexture(samplerName, location);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void drawInternal(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float x, float y, int width, int height) {
        if(width<=0 || height<=0)return;
        if (shader != null && !(Minecraft.getInstance().gameRenderer.mainRenderTarget().getColorTexture()
                instanceof com.mojang.renderpearl.backend.opengl.GlTexture)) {
            if (nativeProgram == null) nativeProgram = new com.lowdragmc.lowdraglib.client.shader.management.NativeShaderTextureProgram(shader.source);
            try (var target = new com.lowdragmc.lowdraglib.client.scene.NativeSceneTarget(
                    Math.max(1, (int) Math.ceil(width * resolution)), Math.max(1, (int) Math.ceil(height * resolution)))) {
                nativeProgram.render(target.get(), color, cache -> {
                    var mc = Minecraft.getInstance();
                    float time = mc.player != null ? (mc.player.tickCount + mc.getDeltaTracker().getGameTimeDeltaTicks()) / 20f : System.currentTimeMillis() / 1000f;
                    cache.glUniform2F("iResolution", width * resolution, height * resolution);
                    cache.glUniform2F("iMouse", Mth.clamp(mouseX-x,0,width)*resolution, Mth.clamp(mouseY-y,0,height)*resolution);
                    cache.glUniform1F("iTime", time);
                    if (uniformCache != null) uniformCache.accept(cache);
                }, pendingTextures);
                target.draw(graphics, x, y, width, height);
            }
            return;
        }

        if (program == null && shader != null) {
            program = new ShaderProgram();
            try {
                program.attach(Shaders.GUI_IMAGE_V).attach(shader);
                pendingTextures.forEach((name, texture) -> {
                    if (texture instanceof Integer id) program.bindTexture(name, id);
                    else program.bindTexture(name, (Identifier) texture);
                });
            } catch (RuntimeException | Error failure) { program.delete(); program = null; throw failure; }
        }
        if (program != null) {
            try(var target=new com.lowdragmc.lowdraglib.client.scene.NativeSceneTarget(
                    Math.max(1,(int)Math.ceil(width*resolution)),Math.max(1,(int)Math.ceil(height*resolution)))) {
                com.lowdragmc.lowdraglib.client.shader.management.NativeGuiShaderDraw.render(target.get(),program,color,cache->{
                    var mc=Minecraft.getInstance();
                    float time=mc.player!=null?(mc.player.tickCount+mc.getDeltaTracker().getGameTimeDeltaTicks())/20f:System.currentTimeMillis()/1000f;
                    cache.glUniformMatrix4F("ModelViewMat",new org.joml.Matrix4f());
                    cache.glUniformMatrix4F("ProjMat",new org.joml.Matrix4f());
                    cache.glUniform2F("iResolution",width*resolution,height*resolution);
                    cache.glUniform2F("iMouse",Mth.clamp(mouseX-x,0,width)*resolution,Mth.clamp(mouseY-y,0,height)*resolution);
                    cache.glUniform1F("iTime",time);
                    if(uniformCache!=null)uniformCache.accept(cache);
                });
                target.draw(graphics,x,y,width,height);
            } catch(Exception exception) { exception.printStackTrace();dispose(); }
        } else {
            DrawerHelper.drawText(graphics, "Error compiling shader", x + 2, y + 2, 1, 0xffff0000);
        }
    }
}
