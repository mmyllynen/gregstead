package com.lowdragmc.lowdraglib.client.shader;
import com.lowdragmc.lowdraglib.client.shader.management.*;
import com.lowdragmc.lowdraglib.client.shader.uniform.UniformCache;
import com.lowdragmc.lowdraglib.client.scene.NativeSceneTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import java.util.*;
import java.util.function.Consumer;
/** Original screen vertex and fragment calculations, composed through native GUI extraction. */
public final class NativeScreenEffects {
 private static final Map<Shader,NativeShaderTextureProgram> programs=new IdentityHashMap<>();
 private static Shader vertex;

 static{Shaders.addReloadListener(()->{programs.values().forEach(NativeShaderTextureProgram::close);programs.clear();vertex=null;});}
 private NativeScreenEffects(){}
 public static void draw(GuiGraphicsExtractor graphics,Shader fragment,Consumer<UniformCache> callback){var mc=Minecraft.getInstance();var window=mc.getWindow();if(vertex==null)try{vertex=Shader.loadSource(Shader.ShaderType.VERTEX,net.minecraft.resources.Identifier.parse("ldlib:shaders/screen.vsh"));}catch(java.io.IOException failure){throw new IllegalStateException("Original screen vertex missing",failure);}var program=programs.computeIfAbsent(fragment,shader->new NativeShaderTextureProgram(shader.source,vertex.source,false,null));var p=graphics.pose();var pose=new org.joml.Matrix4f().m00(p.m00()).m01(p.m01()).m10(p.m10()).m11(p.m11()).m30(p.m20()).m31(p.m21());float guiWidth=window.getGuiScaledWidth(),guiHeight=window.getGuiScaledHeight();try(var target=new NativeSceneTarget(window.getWidth(),window.getHeight())){com.mojang.blaze3d.systems.RenderSystem.getDevice().createCommandEncoder().clearColorTexture(target.get().getColorTexture(),new org.joml.Vector4f(0,0,0,0));program.render(target.get(),-1,cache->{cache.glUniform1F("GuiScale",(float)window.getGuiScale());cache.glUniform2F("ScreenSize",window.getWidth(),window.getHeight());cache.glUniformMatrix4F("PoseStack",pose);cache.glUniformMatrix4F("ProjMat",new org.joml.Matrix4f().setOrtho2D(0,guiWidth,guiHeight,0));callback.accept(cache);},Map.of());graphics.pose().pushMatrix();try{graphics.pose().identity();target.draw(graphics,0,0,guiWidth,guiHeight);}finally{graphics.pose().popMatrix();}}}
}
