package com.lowdragmc.lowdraglib.client.scene;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.commands.RenderPass;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** Scene projection follows the native reversed near/far convention and device clip range. */
public final class NativeSceneCamera {
    private final Matrix4f projection, view;
    private final boolean zeroToOne;
    private final ProjectionType type;
    private final RenderPass.RenderArea area;
    private NativeSceneCamera(Matrix4f projection, Matrix4f view, ProjectionType type, RenderPass.RenderArea area) {
        zeroToOne=RenderSystem.getDevice().getDeviceInfo().isZZeroToOne();
        this.projection=new Matrix4f(projection);this.view=new Matrix4f(view);this.type=type;this.area=area;
    }
    public static NativeSceneCamera create(boolean ortho,float fov,float minX,float maxX,float minY,float maxY,
            float near,float far,Vector3f eye,Vector3f target,Vector3f up,int x,int y,int width,int height) {
        if(width<=0 || height<=0)throw new IllegalArgumentException("Scene viewport must be positive");
        float aspect=width/(float)height;
        boolean zeroToOne=RenderSystem.getDevice().getDeviceInfo().isZZeroToOne();
        var projection=ortho ? new Matrix4f().setOrtho(minX,maxX,minY/aspect,maxY/aspect,far,near,zeroToOne)
                : new Matrix4f().setPerspective((float)Math.toRadians(fov),aspect,10000,0.1f,zeroToOne);
        return new NativeSceneCamera(projection,new Matrix4f().setLookAt(eye,target,up),
                ortho?ProjectionType.ORTHOGRAPHIC:ProjectionType.PERSPECTIVE,new RenderPass.RenderArea(x,y,width,height));
    }
    public Vector3f project(Vector3f point) {
        var ndc=new Matrix4f(projection).mul(view).transformProject(new Vector3f(point));
        return new Vector3f(area.x()+(ndc.x+1)*area.width()/2,
                area.y()+(ndc.y+1)*area.height()/2,zeroToOne?ndc.z:(ndc.z+1)/2);
    }
    public Vector3f unproject(float x,float y,float depth) {
        var ndc=new Vector3f(2*(x-area.x())/area.width()-1,2*(y-area.y())/area.height()-1,
                zeroToOne?depth:2*depth-1);
        return new Matrix4f(projection).mul(view).invert().transformProject(ndc);
    }
    public Matrix4f projection(){return new Matrix4f(projection);}
    public Matrix4f view(){return new Matrix4f(view);}
    public ProjectionType type(){return type;}
    public RenderPass.RenderArea area(){return area;}
}
