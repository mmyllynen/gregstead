package com.lowdragmc.lowdraglib.client.shader.management;

import com.lowdragmc.lowdraglib.client.shader.uniform.UniformCache;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.pipeline.*;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import java.nio.*;
import java.util.*;
import java.util.regex.*;
import java.util.function.Consumer;

/** Runs the original GUI fragment calculation through RenderPearl, without OpenGL calls. */
public final class NativeShaderTextureProgram implements AutoCloseable {
    private record Field(String type,int offset,int flag) {}
    private final Map<String,Field> fields=new LinkedHashMap<>();
    private final List<String> samplers=new ArrayList<>();
    private final ByteBuffer data;
    private final UniformCache uniforms;
    private final CompiledRenderPipeline compiled;
    private final GpuBuffer buffer;
    private static final String VERTEX="""
            #version 450
            layout(location=0) out vec4 vertexColor;
            layout(location=1) out vec2 texCoord;
            void main(){
                vec2 p=vec2((gl_VertexIndex<<1)&2,gl_VertexIndex&2);
                gl_Position=vec4(p*2.0-1.0,0.0,1.0);
                texCoord=vec2(p.x,1.0-p.y);
                vertexColor=LDLGuiColor;
            }
            """;
    public NativeShaderTextureProgram(String original) { this(original, true, null); }
    public NativeShaderTextureProgram(String original, boolean guiUv, BlendFunction blend) {this(original,null,guiUv,blend);}
    public NativeShaderTextureProgram(String original,String customVertex,boolean guiUv,BlendFunction blend) {
        String source=(original+(customVertex==null?"":"\n//LDL_VERTEX_SPLIT\n"+customVertex)).replaceAll("(?m)^\\s*#version[^\\n]*","");
        var pattern=Pattern.compile("\\buniform\\s+(\\w+)\\s+(\\w+)\\s*(?:=\\s*([^;]+))?;");
        var matcher=pattern.matcher(source);var body=new StringBuffer();var members=new StringBuilder("layout(offset=0) vec4 LDLGuiColor;\n");var macros=new StringBuilder();int offset=16;
        while(matcher.find()) {
            String type=matcher.group(1),name=matcher.group(2),initial=matcher.group(3);
            if(type.equals("sampler2D")){samplers.add(name);matcher.appendReplacement(body,Matcher.quoteReplacement("uniform sampler2D "+name+";"));continue;}
            if(!Set.of("float","int","bool","vec2","vec3","vec4","ivec2","ivec3","ivec4","mat2","mat4").contains(type))throw new IllegalArgumentException("Unsupported GUI uniform type "+type);
            if(fields.containsKey(name)){if(!fields.get(name).type().equals(type))throw new IllegalArgumentException("Conflicting shader uniform "+name);matcher.appendReplacement(body,"");continue;}
            int bytes=type.equals("mat4")?64:type.equals("mat2")?32:16;
            int flag=initial==null?-1:offset+bytes;
            fields.put(name,new Field(type,offset,flag));members.append("layout(offset=").append(offset).append(") ").append(type).append(" LDLu_").append(name).append(";\n");
            if(flag>=0)members.append("layout(offset=").append(flag).append(") int LDLhas_").append(name).append(";\n");
            macros.append("#define ").append(name).append(initial==null?" LDLu_":" (LDLhas_").append(name);
            if(initial!=null)macros.append(" != 0 ? LDLu_").append(name).append(" : (").append(initial).append("))");
            macros.append('\n');offset+=bytes+(flag>=0?16:0);matcher.appendReplacement(body,"");
        }
        matcher.appendTail(body);data=ByteBuffer.allocateDirect(offset).order(ByteOrder.nativeOrder());uniforms=new CapturedUniforms();
        String block="layout(std140) uniform LDLGuiUniforms {\n"+members+"};\n";
        String[] parts=body.toString().split("//LDL_VERTEX_SPLIT\\n",2);
        String fragment="#version 450\n"+block+macros+parts[0];
        fragment=fragment.replaceAll("(?m)\\bin\\s+vec4\\s+vertexColor\\s*;","layout(location=0) in vec4 vertexColor;")
                .replaceAll("(?m)\\bin\\s+vec2\\s+texCoord\\s*;","layout(location=1) in vec2 texCoord;")
                .replaceAll("(?m)\\bout\\s+vec4\\s+(\\w+)\\s*;","layout(location=0) out vec4 $1;");
        String vertex=VERTEX.replace("#version 450","#version 450\n"+block);
        if (!guiUv) vertex=vertex.replace("texCoord=vec2(p.x,1.0-p.y);", "texCoord=p;");
        if(customVertex!=null) {
            vertex="#version 450\n"+block+macros+parts[1];
            vertex=vertex.replaceAll("\\bin\\s+vec3\\s+Position\\s*;", "\n#define Position vec3(vec2((gl_VertexIndex<<1)&2,gl_VertexIndex&2)*2.0-1.0,0.0)\n");
        }
        for(var entry:Map.of("screenPos",2,"guiPos",3,"uv",4).entrySet()) {
            fragment=fragment.replaceAll("\\bin\\s+vec2\\s+"+entry.getKey()+"\\s*;", "layout(location="+entry.getValue()+") in vec2 "+entry.getKey()+";");
            vertex=vertex.replaceAll("\\bout\\s+vec2\\s+"+entry.getKey()+"\\s*;", "layout(location="+entry.getValue()+") out vec2 "+entry.getKey()+";");
        }
        var layout=BindGroupLayout.builder().withUniform("LDLGuiUniforms",UniformType.UNIFORM_BUFFER);
        samplers.forEach(name->layout.withUniform(name,UniformType.COMBINED_IMAGE_SAMPLER));
        var id=Identifier.fromNamespaceAndPath("ldlib","runtime/gui_"+Integer.toUnsignedString(original.hashCode()));
        var pipeline=RenderPipeline.builder().withLocation(id).withVertexShader(id).withFragmentShader(id)
                .withBindGroupLayout(layout.build()).withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
                .withCull(false).withColorTargetState(blend==null?ColorTargetState.DEFAULT:new ColorTargetState(blend)).build();
        final String f=fragment,v=vertex;
        var shaderSource=new ShaderSource(){public String getShader(Identifier key,ShaderType type){return type==ShaderType.VERTEX?v:f;}public CachedIncludeSource getInclude(Identifier key){throw new IllegalArgumentException("GUI shader include must be expanded before compilation: "+key);}public void close(){}};
        compiled=RenderSystem.getDevice().compilePipeline(pipeline,shaderSource,Runnable::run).join().finishCompile();
        if(compiled==null)throw new IllegalStateException("Native GUI shader compilation failed");
        buffer=RenderSystem.getDevice().createBuffer(()->"LDLib GUI shader uniforms",GpuBuffer.USAGE_UNIFORM|GpuBuffer.USAGE_COPY_DST,offset);
    }
    public void render(RenderTarget target,int color,Consumer<UniformCache> callback,Map<String,Object> textures) {
        for(int i=0;i<4;i++)data.putFloat(i*4,((color>>>(i==0?16:i==1?8:i==2?0:24))&255)/255f);
        callback.accept(uniforms);var encoder=RenderSystem.getDevice().createCommandEncoder();data.position(0);data.limit(data.capacity());encoder.writeToBuffer(buffer.slice(),data);
        var textureViews=new LinkedHashMap<String,com.mojang.renderpearl.api.textures.GpuTextureView>();
        for(var name:samplers) {
            var resource=textures.get(name);
            if(resource instanceof com.mojang.renderpearl.api.textures.GpuTextureView view) textureViews.put(name,view);
            else if(resource instanceof Identifier location) textureViews.put(name,Minecraft.getInstance().getTextureManager().getTexture(location).getTextureView());
            else throw new IllegalArgumentException("Native sampler requires texture resource or view: "+name);
        }
        var sampler=RenderSystem.getSamplerCache().getClampToEdge(com.mojang.renderpearl.api.textures.FilterMode.LINEAR);
        try(var pass=encoder.createRenderPass(()->"LDLib custom GUI fragment",target.getColorTextureView(),Optional.empty())) {
            pass.setPipeline(compiled);pass.setUniform("LDLGuiUniforms",buffer);
            textureViews.forEach((name,view)->pass.setUniform(name,view,sampler));
            pass.draw(3,1,0,0);
        }
    }
    private void floats(String name,float... values){var field=fields.get(name);if(field==null)return;for(int i=0;i<values.length;i++)data.putFloat(field.offset()+i*4,values[i]);if(field.flag()>=0)data.putInt(field.flag(),1);}
    private void ints(String name,int... values){var field=fields.get(name);if(field==null)return;for(int i=0;i<values.length;i++)data.putInt(field.offset()+i*4,values[i]);if(field.flag()>=0)data.putInt(field.flag(),1);}
    private void matrix(String name,boolean transpose,FloatBuffer value,int n){var field=fields.get(name);if(field==null)return;for(int column=0;column<n;column++)for(int row=0;row<n;row++)data.putFloat(field.offset()+column*16+row*4,value.get(value.position()+(transpose?row*n+column:column*n+row)));if(field.flag()>=0)data.putInt(field.flag(),1);}
    private final class CapturedUniforms extends UniformCache {
        CapturedUniforms(){super(0);}
        @Override public void glUniform1F(String n,float a){floats(n,a);}
        @Override public void glUniform2F(String n,float a,float b){floats(n,a,b);}
        @Override public void glUniform3F(String n,float a,float b,float c){floats(n,a,b,c);}
        @Override public void glUniform4F(String n,float a,float b,float c,float d){floats(n,a,b,c,d);}
        @Override public void glUniform1I(String n,int a){ints(n,a);}
        @Override public void glUniform2I(String n,int a,int b){ints(n,a,b);}
        @Override public void glUniform3I(String n,int a,int b,int c){ints(n,a,b,c);}
        @Override public void glUniform4I(String n,int a,int b,int c,int d){ints(n,a,b,c,d);}
        @Override public void glUniformBoolean(String n,boolean a){ints(n,a?1:0);}
        @Override public void glUniformMatrix2(String n,boolean t,FloatBuffer m){matrix(n,t,m,2);}
        @Override public void glUniformMatrix4(String n,boolean t,FloatBuffer m){matrix(n,t,m,4);}
        @Override public void glUniformMatrix(String n,java.util.function.IntConsumer ignored,boolean t,FloatBuffer m){var f=fields.get(n);if(f!=null)matrix(n,t,m,f.type().equals("mat2")?2:4);}
        @Override public void glUniformMatrix4F(String n,org.joml.Matrix4f m){var b=ByteBuffer.allocateDirect(64).order(ByteOrder.nativeOrder()).asFloatBuffer();m.get(b);matrix(n,false,b,4);}
    }
    public UniformCache uniformCache(){return uniforms;}
    @Override public void close(){buffer.close();compiled.close();}
}
