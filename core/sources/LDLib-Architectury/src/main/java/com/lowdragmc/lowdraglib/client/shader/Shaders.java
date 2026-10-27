package com.lowdragmc.lowdraglib.client.shader;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.client.shader.management.Shader;
import com.lowdragmc.lowdraglib.gui.texture.ShaderTexture;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.resources.Identifier;
import org.lwjgl.opengl.GL;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@OnlyIn(Dist.CLIENT)
public class Shaders {
	private static final List<Runnable> reloadListeners = new ArrayList<>();
	public static Shader IMAGE_F;
	public static Shader IMAGE_V;
	public static Shader GUI_IMAGE_V;
	public static Shader SCREEN_V;
	public static Shader ROUND_F;
	public static Shader PANEL_BG_F;
	public static Shader ROUND_BOX_F;
	public static Shader PROGRESS_ROUND_BOX_F;
	public static Shader FRAME_ROUND_BOX_F;
	public static Shader ROUND_LINE_F;

	public static void init() {
		IMAGE_F = load(Shader.ShaderType.FRAGMENT, Identifier.fromNamespaceAndPath(LDLib.MOD_ID, "image"));
		IMAGE_V = load(Shader.ShaderType.VERTEX, Identifier.fromNamespaceAndPath(LDLib.MOD_ID, "image"));
		GUI_IMAGE_V = load(Shader.ShaderType.VERTEX, Identifier.fromNamespaceAndPath(LDLib.MOD_ID, "gui_image"));
		SCREEN_V = load(Shader.ShaderType.VERTEX, Identifier.fromNamespaceAndPath(LDLib.MOD_ID, "screen"));
		ROUND_F = load(Shader.ShaderType.FRAGMENT, Identifier.fromNamespaceAndPath(LDLib.MOD_ID, "round"));
		PANEL_BG_F = load(Shader.ShaderType.FRAGMENT, Identifier.fromNamespaceAndPath(LDLib.MOD_ID, "panel_bg"));
		ROUND_BOX_F = load(Shader.ShaderType.FRAGMENT, Identifier.fromNamespaceAndPath(LDLib.MOD_ID, "round_box"));
		PROGRESS_ROUND_BOX_F = load(Shader.ShaderType.FRAGMENT, Identifier.fromNamespaceAndPath(LDLib.MOD_ID, "progress_round_box"));
		FRAME_ROUND_BOX_F = load(Shader.ShaderType.FRAGMENT, Identifier.fromNamespaceAndPath(LDLib.MOD_ID, "frame_round_box"));
		ROUND_LINE_F = load(Shader.ShaderType.FRAGMENT, Identifier.fromNamespaceAndPath(LDLib.MOD_ID, "round_line"));
	}

	public static Map<Identifier, Shader> CACHE = new HashMap<>();

	public static void addReloadListener(Runnable runnable) {
		reloadListeners.add(runnable);
	}

	public static void reload() {
        var previouslyLoaded = new HashMap<>(CACHE);
		for (Shader shader : CACHE.values()) {
			if (shader != null) {
				shader.deleteShader();
			}
		}
		CACHE.clear();
		init();
        previouslyLoaded.forEach((key, previous) -> {
            if (previous != null && !CACHE.containsKey(key)) {
                try { CACHE.put(key, Shader.loadSource(previous.shaderType, key)); }
                catch (IOException failure) { LDLib.LOGGER.error("Failed to reload shader {}", key, failure); }
            }
        });
		DrawerHelper.init();
		ShaderTexture.clearCache();
		reloadListeners.forEach(Runnable::run);
	}

	public static Shader load(Shader.ShaderType shaderType, Identifier resourceLocation) {
		return CACHE.computeIfAbsent(Identifier.fromNamespaceAndPath(resourceLocation.getNamespace(), "shaders/" + resourceLocation.getPath() + shaderType.shaderExtension), key -> {
			try {
                Shader shader = Shader.loadSource(shaderType, key);
				LDLib.LOGGER.debug("load shader {} resource {} success", shaderType, resourceLocation);
				return shader;
			} catch (IOException e) {
				LDLib.LOGGER.error("load shader {} resource {} failed", shaderType, resourceLocation);
				LDLib.LOGGER.error("caused by ", e);
				return IMAGE_F;
			}
		});
	}

	public static boolean supportComputeShader() {
		return GL.getCapabilities().GL_ARB_compute_shader;
	}

	public static boolean supportSSBO() {
		return GL.getCapabilities().GL_ARB_shader_storage_buffer_object;
	}

}
