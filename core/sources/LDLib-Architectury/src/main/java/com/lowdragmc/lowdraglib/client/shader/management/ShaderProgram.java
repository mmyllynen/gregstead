package com.lowdragmc.lowdraglib.client.shader.management;

import com.lowdragmc.lowdraglib.client.shader.uniform.IUniformCallback;
import com.lowdragmc.lowdraglib.client.shader.uniform.UniformCache;
import com.mojang.renderpearl.backend.opengl.GlStateManager;
import com.mojang.renderpearl.backend.opengl.GlTexture;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ShaderProgram {

	public final int programId;
	public final Set<Shader> shaders;
	public final UniformCache uniformCache;
	public final LinkedHashMap<String, Integer> samplers;
	private boolean unLinked;
    private boolean bound, deleted;
    private int previousProgram, previousActive;
    private final Map<Integer,Integer> previousTextures = new LinkedHashMap<>();
	private IUniformCallback globalUniform;

    private NativeShaderTextureProgram nativeProgram;
    private final Map<String,Object> nativeTextures=new LinkedHashMap<>();
    private ShaderProgram(Shader fragment) {
        programId=0;shaders=new ReferenceOpenHashSet<>();shaders.add(fragment);samplers=new LinkedHashMap<>();
        nativeProgram=new NativeShaderTextureProgram(fragment.source,false,null);
        uniformCache=nativeProgram.uniformCache();
    }
    static ShaderProgram nativeFramebuffer(Shader fragment){return new ShaderProgram(fragment);}
    void renderNative(com.mojang.blaze3d.pipeline.RenderTarget target,IUniformCallback callback){
        if(deleted)throw new IllegalStateException("Shader program has been deleted");
        nativeProgram.render(target,-1,cache->{if(globalUniform!=null)globalUniform.apply(cache);if(callback!=null)callback.apply(cache);},nativeTextures);
    }
    public void bindTexture(String samplerName,com.mojang.renderpearl.api.textures.GpuTextureView view){
        if(nativeProgram==null)throw new IllegalStateException("GPU texture view requires native program");
        if(view==null)nativeTextures.remove(samplerName);else nativeTextures.put(samplerName,view);
    }
	public ShaderProgram() {
        if (!(Minecraft.getInstance().gameRenderer.mainRenderTarget().getColorTexture() instanceof GlTexture))
            throw new UnsupportedOperationException("Raw shader program requires OpenGL; native custom shader rendering is required");
		this.programId = GL20.glCreateProgram();
		this.shaders = new ReferenceOpenHashSet<>();
		this.samplers = new LinkedHashMap<>();
		if (this.programId == 0) {
			throw new IllegalStateException("Unable to create ShaderProgram.");
		}
		this.uniformCache = new UniformCache(this.programId);
	}

	public ShaderProgram attach(Shader loader) {
        if(nativeProgram!=null)throw new IllegalStateException("Native framebuffer program already owns its vertex and fragment stages");
		if (loader == null) return this;
		if (this.shaders.contains(loader)) {
			throw new IllegalStateException(String.format("Unable to attach Shader as it is already attached:\n%s", loader.source));
		}
		loader.attachShader(programId);
		this.shaders.add(loader);
		this.unLinked = true;
		return this;
	}

	public void use(IUniformCallback callback) {
        this.use();
        try { callback.apply(uniformCache); }
        catch (RuntimeException | Error failure) { release(); throw failure; }
	}

	public void setGlobalUniform(IUniformCallback globalUniform) {
		this.globalUniform = globalUniform;
	}

	public void bindTexture(String samplerName, int textureId) {
        if(nativeProgram!=null)throw new IllegalArgumentException("Raw OpenGL texture IDs cannot identify a native GPU texture; bind a resource or view");
		if (textureId > 0) {
			samplers.put(samplerName, textureId);
		} else {
			samplers.remove(samplerName);
		}
	}

	public void bindTexture(String samplerName, Identifier resourceLocation) {
        if(nativeProgram!=null){if(resourceLocation==null)nativeTextures.remove(samplerName);else nativeTextures.put(samplerName,resourceLocation);return;}
		if (resourceLocation == null) {
			bindTexture(samplerName, 0);
			return;
		}
		AbstractTexture abstracttexture = Minecraft.getInstance().getTextureManager().getTexture(resourceLocation);
        if (!(abstracttexture.getTexture() instanceof GlTexture texture)) {
            throw new UnsupportedOperationException("Raw shader program requires the OpenGL backend");
        }
        bindTexture(samplerName, texture.glId());
	}

	public void linkProgram() {
        if(nativeProgram!=null)return;
		if (unLinked) {
			this.uniformCache.invalidate();
			GL20.glLinkProgram(programId);
			if (GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
				throw new RuntimeException(String.format("ShaderProgram validation has failed!\n%s", GL20.glGetProgramInfoLog(programId, GL20.glGetProgrami(programId, 35716))));
			}
			this.unLinked = false;
		}
	}

    public void use() {
        if(nativeProgram!=null){if(deleted)throw new IllegalStateException("Shader program has been deleted");if(globalUniform!=null)globalUniform.apply(uniformCache);return;}
        if (deleted) throw new IllegalStateException("Shader program has been deleted");
        if (unLinked) linkProgram();
        if (!bound) {
            previousProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
            previousActive = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
            bound = true;
        }
        try {
            GlStateManager._glUseProgram(programId);
            int unit = 0;
            for (var entry : samplers.entrySet()) {
                GlStateManager._activeTexture(GL13.GL_TEXTURE0 + unit);
                if (!previousTextures.containsKey(unit)) previousTextures.put(unit, GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D));
                GlStateManager._bindTexture(entry.getValue());
                uniformCache.glUniform1I(entry.getKey(), unit++);
            }
            if (globalUniform != null) globalUniform.apply(uniformCache);
        } catch (RuntimeException | Error failure) { release(); throw failure; }
    }

    public void release() {
        if (!bound) return;
        for (var entry : previousTextures.entrySet()) {
            GlStateManager._activeTexture(GL13.GL_TEXTURE0 + entry.getKey());
            GlStateManager._bindTexture(entry.getValue());
        }
        GlStateManager._activeTexture(previousActive);
        GlStateManager._glUseProgram(previousProgram);
        previousTextures.clear();
        bound = false;
    }

    public void delete() {
        if(nativeProgram!=null){if(!deleted){nativeProgram.close();nativeTextures.clear();deleted=true;}return;}
        if (deleted) return;
        release();
        if (GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM) == programId) GlStateManager._glUseProgram(0);
        GL20.glDeleteProgram(programId);
        deleted = true;
    }

}
