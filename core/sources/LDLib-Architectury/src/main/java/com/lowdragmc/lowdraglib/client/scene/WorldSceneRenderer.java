package com.lowdragmc.lowdraglib.client.scene;

import com.lowdragmc.lowdraglib.client.shader.management.ShaderManager;
import com.lowdragmc.lowdraglib.math.Position;
import com.lowdragmc.lowdraglib.math.PositionedRect;
import com.lowdragmc.lowdraglib.math.Size;
import com.lowdragmc.lowdraglib.utils.virtuallevel.DummyWorld;
import com.lowdragmc.lowdraglib.utils.virtuallevel.TrackedDummyWorld;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.world.level.block.RenderShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.model.data.ModelData;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;



/**
 * @author KilaBash
 * @date 2022/05/25
 * @implNote render a scene, through VBO compilation scene, greatly optimize rendering performance.
 */
@SuppressWarnings("ALL")
@OnlyIn(Dist.CLIENT)
public abstract class WorldSceneRenderer {
    enum CacheState {
        UNUSED,
        NEED,
        COMPILING,
        COMPILED
    }

    public final Level world;
    public final Map<Collection<BlockPos>, ISceneBlockRenderHook> renderedBlocksMap;
    private record CachedBlock(BlockPos pos,ISceneBlockRenderHook hook) {}
    private final NativeSceneGeometryCache<CachedBlock> nativeBlockCache=new NativeSceneGeometryCache<>();
    protected boolean useCache;
    @Getter @Setter
    protected boolean endBatchLast = false;// if true, endBatch will be called after all rendering
    protected boolean ortho;
    protected AtomicReference<CacheState> cacheState;
    protected int maxProgress;
    protected int progress;
    protected ParticleManager particleManager;
    private NativeSceneCamera nativeParticleCamera;
    private NativeSceneCameraScope nativeCameraScope;
    private final NativeSceneBuffers sceneBuffers=new NativeSceneBuffers();
    public NativeSceneBuffers sceneBuffers(){return sceneBuffers;}
    protected NativeSceneViewCamera camera;
    protected CameraEntity cameraEntity;
    private Consumer<WorldSceneRenderer> beforeRender;
    private Consumer<WorldSceneRenderer> afterRender;
    @Setter @Nullable
    private BiConsumer<NativeSceneBuffers, Float> beforeBatchEnd;
    private Consumer<BlockHitResult> onLookingAt;
    @Setter @Nullable
    private ISceneEntityRenderHook sceneEntityRenderHook;
    protected int clearColor;
    @Getter
    private Vector3f lastHit;
    @Getter
    private BlockHitResult lastTraceResult;
    private Set<BlockPos> blocked;
    private Vector3f eyePos = new Vector3f(0, 0, 10f);
    private Vector3f lookAt = new Vector3f(0, 0, 0);
    private Vector3f worldUp = new Vector3f(0, 1, 0);
    @Getter
    private float fov = 60f;
    private float minX, maxX, minY, maxY, minZ, maxZ;

    public WorldSceneRenderer(Level world) {
        this.world = world;
        this.camera = new NativeSceneViewCamera();
        this.cameraEntity = new CameraEntity(world);
        setCameraLookAt(eyePos,lookAt,worldUp);
        renderedBlocksMap = new LinkedHashMap<>();
        cacheState = new AtomicReference<>(CacheState.UNUSED);
    }

    public WorldSceneRenderer setParticleManager(ParticleManager particleManager) {
        if (particleManager == null) {
            this.particleManager = null;
            return this;
        }
        this.particleManager = particleManager;
        if (this.world instanceof DummyWorld dummyWorld) {
            dummyWorld.setParticleManager(particleManager);
        }
        this.particleManager.setLevel(world);
        return this;
    }

    public ParticleManager getParticleManager() {
        return particleManager;
    }

    public WorldSceneRenderer useCacheBuffer(boolean useCache) {
        if(this.useCache==useCache || !Minecraft.getInstance().isSameThread())return this;
        deleteCacheBuffer();this.useCache=useCache;
        if(useCache)cacheState.set(CacheState.NEED);
        return this;
    }

    public WorldSceneRenderer useOrtho(boolean ortho) { this.ortho=ortho;return this; }

    public WorldSceneRenderer deleteCacheBuffer() {
        nativeBlockCache.clear();useCache=false;progress=0;maxProgress=0;
        cacheState.set(CacheState.UNUSED);return this;
    }

    public WorldSceneRenderer needCompileCache() { cacheState.set(CacheState.NEED);return this; }

    public WorldSceneRenderer setBeforeWorldRender(Consumer<WorldSceneRenderer> callback) {
        this.beforeRender = callback;
        return this;
    }

    public WorldSceneRenderer setAfterWorldRender(Consumer<WorldSceneRenderer> callback) {
        this.afterRender = callback;
        return this;
    }

    public WorldSceneRenderer addRenderedBlocks(Collection<BlockPos> blocks, ISceneBlockRenderHook renderHook) {
        if (blocks != null) {
            this.renderedBlocksMap.put(blocks, renderHook);
            if(useCache)needCompileCache();
        }
        return this;
    }

    public WorldSceneRenderer setBlocked(Set<BlockPos> blocked) {
        this.blocked = blocked;
        if(useCache)needCompileCache();
        return this;
    }

    public WorldSceneRenderer setOnLookingAt(Consumer<BlockHitResult> onLookingAt) {
        this.onLookingAt = onLookingAt;
        return this;
    }

    public boolean isUseCache() {
        return useCache;
    }

    public void setClearColor(int clearColor) {
        this.clearColor = clearColor;
    }

    public void render(@Nonnull net.minecraft.client.gui.GuiGraphicsExtractor graphics,float x,float y,float width,float height,int mouseX,int mouseY) {
        var m=graphics.pose();var pose=new PoseStack();
        pose.mulPose(new Matrix4f(m.m00(),m.m01(),0,0,m.m10(),m.m11(),0,0,0,0,1,0,m.m20(),m.m21(),0,1));
        render(pose,x,y,width,height,mouseX,mouseY);
    }

    public void render(@Nonnull PoseStack poseStack, float x, float y, float width, float height, int mouseX, int mouseY) {
        // do not render if the minecraft is reloading
        if (Minecraft.getInstance().gui.overlay() instanceof LoadingOverlay) {
            return;
        }
        // setupCamera
        var pose = poseStack.last().pose();
        Vector4f pos = new Vector4f(x, y, 0, 1.0F);
        pos = pose.transform(pos);
        Vector4f size = new Vector4f(x + width, y + height, 0, 1.0F);
        size = pose.transform(size);
        x = pos.x();
        y = pos.y();
        width = size.x() - x;
        height = size.y() - y;
        PositionedRect viewport = getPositionedRect((int) x, (int) y, (int) width, (int) height);
        var topLeft = poseStack.last().pose().transformPosition(new Vector3f(0.0f, 0.0f, 0.0f));
        PositionedRect mouse = getPositionedRect((int) (mouseX + topLeft.x), (int) (mouseY + topLeft.y), 0, 0);
        mouseX = mouse.position.x;
        mouseY = mouse.position.y;
        setupCamera(viewport);
        try {
            // render TrackedDummyWorld
            drawWorld();
            // check lookingAt
            this.lastTraceResult = null;
            this.lastHit = unProject(mouseX, mouseY);
            if (onLookingAt != null && mouseX > viewport.position.x && mouseX < viewport.position.x + viewport.size.width
                    && mouseY > viewport.position.y && mouseY < viewport.position.y + viewport.size.height) {
                BlockHitResult result = rayTrace(lastHit);
                if (result != null) {
                    this.lastTraceResult = null;
                    this.lastTraceResult = result;
                    onLookingAt.accept(result);
                }
            }
        } finally { resetCamera(); }
    }

    public Vector3f getEyePos() {
        return eyePos;
    }

    public Vector3f getLookAt() {
        return lookAt;
    }

    public Vector3f getWorldUp() {
        return worldUp;
    }

    public void setCameraLookAt(Vector3f eyePos, Vector3f lookAt, Vector3f worldUp) {
        this.eyePos = eyePos;
        this.lookAt = lookAt;
        this.worldUp = worldUp;
        if (camera != null) {
            Vector3f xzProduct = new Vector3f(lookAt.x() - eyePos.x(), 0, lookAt.z() - eyePos.z());
            double angleYaw = Math.toDegrees(xzProduct.angle(new Vector3f(0, 0, 1)));
            if (xzProduct.angle(new Vector3f(1, 0, 0)) < Math.PI / 2) {
                angleYaw = -angleYaw;
            }
            double anglePitch = Math.toDegrees(new Vector3f(lookAt).sub(new Vector3f(eyePos)).angle(new Vector3f(0, 1, 0))) - 90;
            cameraEntity.setPos(eyePos.x(), eyePos.y() - cameraEntity.getEyeHeight(), eyePos.z());
            cameraEntity.xo = cameraEntity.getX();
            cameraEntity.yo = cameraEntity.getY();
            cameraEntity.zo = cameraEntity.getZ();
            cameraEntity.setYRot((float) angleYaw);
            cameraEntity.setXRot((float) anglePitch);
            cameraEntity.yRotO = cameraEntity.getYRot();
            cameraEntity.xRotO = cameraEntity.getXRot();
        }
    }

    public void setCameraLookAt(Vector3f lookAt, double radius, double rotationPitch, double rotationYaw) {
        Vector3f vecX = new Vector3f((float) Math.cos(rotationPitch), (float) 0, (float) Math.sin(rotationPitch));
        Vector3f vecY = new Vector3f(0, (float) (Math.tan(rotationYaw) * vecX.length()), 0);
        Vector3f pos = new Vector3f(vecX).add(vecY).normalize().mul((float) radius);
        setCameraLookAt(pos.add(lookAt.x(), lookAt.y(), lookAt.z()), lookAt, worldUp);
    }

    public void setFov(float fov) {
        this.fov = fov;
    }

    public void setCameraOrtho(float x, float y, float z) {
        this.minX = -x;
        this.maxX = x;
        this.minY = -y;
        this.maxY = y;
        this.minZ = -z;
        this.maxZ = z;
    }

    public void setCameraOrtho(float minX, float maxX, float minY, float maxY, float minZ, float maxZ) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.minZ = minZ;
        this.maxZ = maxZ;
    }

    public PositionedRect getPositionedRect(int x, int y, int width, int height) {
        return PositionedRect.of(Position.of(x, y), Size.of(width, height));
    }

    public PositionedRect getPositionRectRevert(int windowX, int windowY, int windowWidth, int windowHeight) {
        return PositionedRect.of(Position.of(windowX, windowY), Size.of(windowWidth, windowHeight));
    }

    protected com.mojang.blaze3d.pipeline.RenderTarget particleRenderTarget() {
        return Minecraft.getInstance().gameRenderer.mainRenderTarget();
    }

    protected void setupCamera(PositionedRect viewport) {
        int x = viewport.getPosition().x;
        int y = viewport.getPosition().y;
        int width = viewport.getSize().width;
        int height = viewport.getSize().height;

        if(nativeCameraScope!=null)throw new IllegalStateException("Scene camera already active");
        nativeParticleCamera=NativeSceneCamera.create(ortho,fov,minX,maxX,minY,maxY,minZ,maxZ,
                eyePos,lookAt,worldUp,x,y,width,height);

        nativeCameraScope=new NativeSceneCameraScope(nativeParticleCamera);
        try {
            clearView(x,y,width,height);
            Minecraft mc=Minecraft.getInstance();

            if (camera != null) {
                camera.configure(world, cameraEntity, new Vec3(eyePos.x(), eyePos.y(), eyePos.z()), cameraEntity.getYRot(), cameraEntity.getXRot(), fov, width, height);
            }
            ShaderManager.getInstance().setViewPort(viewport);

        } catch(RuntimeException|Error error) {
            nativeCameraScope.close();nativeCameraScope=null;
            ShaderManager.getInstance().clearViewPort();
            throw error;
        }
    }

    protected void clearView(int x, int y, int width, int height) {
        NativeSceneFeaturePass.clear(particleRenderTarget(),new org.joml.Vector4f(
                ((clearColor>>>16)&255)/255f,((clearColor>>>8)&255)/255f,
                (clearColor&255)/255f,((clearColor>>>24)&255)/255f),
                new com.mojang.renderpearl.api.commands.RenderPass.RenderArea(x,y,width,height));
    }

    protected void resetCamera() {
        try {
            RenderSystem.getDevice().createCommandEncoder().clearDepthTexture(particleRenderTarget().getDepthTexture(),0);
        } finally {
            if(nativeCameraScope!=null) { nativeCameraScope.close();nativeCameraScope=null; }
            ShaderManager.getInstance().clearViewPort();
        }
    }

    protected void drawWorld() {
        sceneBuffers.clear();
        if (beforeRender != null) {
            beforeRender.accept(this);
        }

        Minecraft mc = Minecraft.getInstance();

        float particleTicks = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        var buffers = sceneBuffers;
        var sceneSubmits=new net.minecraft.client.renderer.SubmitNodeStorage();
        if (useCache) {
            renderCacheBuffer(particleTicks,sceneSubmits);
        } else {
            var geometry=new ArrayList<NativeSceneBlockGeometry>();
            renderedBlocksMap.forEach((positions,hook)-> {
                for(var pos:positions)if(blocked==null || !blocked.contains(pos))geometry.add(extractBlockGeometry(pos,hook,particleTicks));
            });
            submitSceneGeometry(geometry,sceneSubmits);
            renderedBlocksMap.forEach((positions,hook)->renderTESR(positions,hook,particleTicks,sceneSubmits));
        }

        if(world instanceof TrackedDummyWorld level)renderEntities(level,sceneEntityRenderHook,particleTicks,sceneSubmits);
        if(beforeBatchEnd!=null)beforeBatchEnd.accept(buffers,particleTicks);
        buffers.submit(sceneSubmits);
        try(var renderer=new NativeSceneFeaturePass()) {
            renderer.render(sceneSubmits,particleRenderTarget(),nativeParticleCamera.projection(),nativeParticleCamera.type(),
                    nativeParticleCamera.view(),nativeParticleCamera.area());
        }

        if (particleManager != null) {
            @Nonnull PoseStack poseStack = new PoseStack();
            poseStack.setIdentity();
            poseStack.translate(camera.position().x, camera.position().y, camera.position().z);
            particleManager.render(poseStack, camera, particleTicks, particleRenderTarget(), nativeParticleCamera);
        }

        if (afterRender != null) {
            afterRender.accept(this);
            var overlays=new net.minecraft.client.renderer.SubmitNodeStorage();sceneBuffers.submit(overlays);
            try(var renderer=new NativeSceneFeaturePass()) {
                renderer.render(overlays,particleRenderTarget(),nativeParticleCamera.projection(),nativeParticleCamera.type(),
                        nativeParticleCamera.view(),nativeParticleCamera.area());
            }
        }
    }

    public boolean isCompiling() {
        return cacheState.get() == CacheState.COMPILING;
    }

    public double getCompileProgress() {
        if (cacheState.get() == CacheState.COMPILING && maxProgress > 0) {
            return progress * 1. / maxProgress;
        }
        return 0;
    }

    private void submitSceneGeometry(List<NativeSceneBlockGeometry> geometry,net.minecraft.client.renderer.SubmitNodeCollector submits) {
        var pose=new PoseStack();
        for(var block:geometry)block.submit(pose,submits);
    }

    private void renderCacheBuffer(float particleTicks,net.minecraft.client.renderer.SubmitNodeCollector sceneSubmits) {
        if(cacheState.get()==CacheState.NEED) {
            var requests=new ArrayList<CachedBlock>();
            renderedBlocksMap.forEach((positions,hook)-> {
                for(var pos:positions)if(blocked==null || !blocked.contains(pos))requests.add(new CachedBlock(pos.immutable(),hook));
            });
            nativeBlockCache.rebuild(requests,entry->extractBlockGeometry(entry.pos(),entry.hook(),0));
            cacheState.set(CacheState.COMPILING);
        }
        nativeBlockCache.advance(8);
        progress=nativeBlockCache.completed();maxProgress=nativeBlockCache.total();
        cacheState.set(nativeBlockCache.compiling()?CacheState.COMPILING:CacheState.COMPILED);
        submitSceneGeometry(nativeBlockCache.snapshot(),sceneSubmits);
        // Animated block entities retain their live extraction independently of cached block geometry.
        renderedBlocksMap.forEach((positions,hook)->renderTESR(positions,hook,particleTicks,sceneSubmits));
    }

    protected NativeSceneBlockGeometry extractBlockGeometry(BlockPos pos, @Nullable ISceneBlockRenderHook hook, float partialTicks) {
        var state=world.getBlockState(pos);
        var geometry=NativeSceneBlockGeometry.extract(com.lowdragmc.lowdraglib.utils.virtuallevel.NativeLevelRenderView.of(world),pos,state,
                Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state),true,true,
                (layer,output)-> {
                    var wrapper=new VertexConsumerWrapper(output);
                    if(hook!=null) {
                        hook.apply(false,NativeSceneBlockGeometry.renderType(layer));
                        hook.applyVertexConsumerWrapper(world,pos,state,wrapper,NativeSceneBlockGeometry.renderType(layer),partialTicks);
                    }
                    return wrapper;
                });
        return hook==null?geometry:geometry.withRenderTypes(hook::renderType);
    }

    private void renderTESR(Collection<BlockPos> positions,@Nullable ISceneBlockRenderHook hook,float partialTicks,net.minecraft.client.renderer.SubmitNodeCollector sceneSubmits) {
        if(hook!=null)hook.apply(true,net.minecraft.client.renderer.rendertype.RenderTypes.translucentMovingBlock());
        var entities=new ArrayList<BlockEntity>();
        for(var pos:positions) {
            var entity=world.getBlockEntity(pos);
            if(entity!=null)entities.add(entity);
        }
        var snapshot=NativeSceneBlockEntities.extract(entities,partialTicks,
                new Vec3(eyePos.x(),eyePos.y(),eyePos.z()),hook==null?null:
                (entity,pose)->hook.applyBESR(world,entity.getBlockPos(),entity,pose,partialTicks));
        var cameraState=new net.minecraft.client.renderer.state.level.CameraRenderState();
        cameraState.pos=new Vec3(eyePos.x(),eyePos.y(),eyePos.z());
        if(camera!=null)cameraState.orientation.set(camera.rotation());
        snapshot.submit(sceneSubmits,cameraState);
    }

    private void renderEntities(TrackedDummyWorld level,@Nullable ISceneEntityRenderHook hook,float partialTicks,
            net.minecraft.client.renderer.SubmitNodeCollector sceneSubmits) {
        var snapshot=NativeSceneEntities.extract(level.getAllEntities(),camera,partialTicks,
                hook==null?null:(entity,pose)->hook.applyEntity(world,entity,pose,partialTicks));
        var cameraState=new net.minecraft.client.renderer.state.level.CameraRenderState();
        cameraState.pos=new Vec3(eyePos.x(),eyePos.y(),eyePos.z());
        if(camera!=null)cameraState.orientation.set(camera.rotation());
        snapshot.submit(sceneSubmits,cameraState);
    }

    public BlockHitResult rayTrace(Vector3f hitPos) {
        Vec3 startPos = new Vec3(this.eyePos.x(), this.eyePos.y(), this.eyePos.z());
        if (ortho) {
            startPos = startPos.add(new Vec3(startPos.x - lookAt.x(), startPos.y - lookAt.y(), startPos.z - lookAt.z()).multiply(500, 500, 500));
        }
        hitPos = hitPos.mul(2, new Vector3f()); // Double view range to ensure pos can be seen.
        Vec3 endPos = new Vec3((hitPos.x() - startPos.x), (hitPos.y() - startPos.y), (hitPos.z() - startPos.z));
        try {
            return this.world.clip(new ClipContext(startPos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, cameraEntity));
        } catch (Exception e) {
            return null;
        }
    }

    public Vector3f project(Vector3f pos) {
        return nativeParticleCamera.project(pos);
    }

    public Vector3f unProject(int mouseX,int mouseY) { return unProject(mouseX,mouseY,true); }

    public Vector3f unProject(int mouseX,int mouseY,boolean checkDepth) {
        float depth=checkDepth?NativeSceneDepth.read(particleRenderTarget(),mouseX,mouseY):0.001f;
        return nativeParticleCamera.unproject(mouseX,mouseY,depth);
    }

    /***
     * For better performance, You'd better handle the event {@link #setOnLookingAt(Consumer)} or {@link #getLastTraceResult()}
     * @param mouseX xPos in Texture
     * @param mouseY yPos in Texture
     * @return RayTraceResult Hit
     */
    protected BlockHitResult screenPos2BlockPosFace(int mouseX, int mouseY, int x, int y, int width, int height) {
        // render a frame
        setupCamera(getPositionedRect(x, y, width, height));

        try {
            drawWorld();
            Vector3f hitPos=this.lastHit==null?unProject(mouseX,mouseY):this.lastHit;
            return rayTrace(hitPos);
        } finally { resetCamera(); }
    }

    /***
     * For better performance, You'd better do project in {@link #setAfterWorldRender(Consumer)}
     * @param pos BlockPos
     * @param depth should pass Depth Test
     * @return x, y, z
     */
    protected Vector3f blockPos2ScreenPos(BlockPos pos, boolean depth, int x, int y, int width, int height) {
        // render a frame
        setupCamera(getPositionedRect(x, y, width, height));

        try {
            drawWorld();
            return project(new Vector3f(pos.getX()+0.5f,pos.getY()+0.5f,pos.getZ()+0.5f));
        } finally { resetCamera(); }
    }

    public static class VertexConsumerWrapper extends NativeSceneVertexConsumer {
        public VertexConsumerWrapper(VertexConsumer builder) { super(builder); }
    }
}
