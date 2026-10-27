package com.lowdragmc.lowdraglib.client.scene;

import com.lowdragmc.lowdraglib.client.shader.LDLibRenderTypes;
import net.minecraft.resources.Identifier;

/** Original down-face translation/up-face GUI rotation expressed directly in world XZ coordinates. */
public final class NativeSceneGround {
    private NativeSceneGround() {}
    public static void draw(NativeSceneBuffers buffers,int minX,int maxX,int minY,int minZ,int maxZ) {
        draw(buffers,Identifier.parse("ldlib:textures/gui/darkened_slot.png"),
                (minX+maxX)/2f+0.5f,minY,(minZ+maxZ)/2f+0.5f,maxX-minX+3,maxZ-minZ+3);
    }
    public static void draw(NativeSceneBuffers buffers,Identifier texture,float centerX,float y,float centerZ,float width,float height) {
        var output=buffers.getBuffer(LDLibRenderTypes.texturedPlane(texture));
        float x0=centerX-width/2,x1=centerX+width/2,z0=centerZ-height/2,z1=centerZ+height/2;
        output.addVertex(x0,y,z0).setColor(-1).setUv(0,0);
        output.addVertex(x0,y,z1).setColor(-1).setUv(0,1);
        output.addVertex(x1,y,z1).setColor(-1).setUv(1,1);
        output.addVertex(x1,y,z0).setColor(-1).setUv(1,0);
    }
}
