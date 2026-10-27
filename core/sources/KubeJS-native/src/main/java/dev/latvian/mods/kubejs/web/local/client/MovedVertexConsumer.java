package dev.latvian.mods.kubejs.web.local.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

public record MovedVertexConsumer(VertexConsumer parent, PoseStack.Pose pose) implements VertexConsumer {
	@Override
	public VertexConsumer addVertex(float x, float y, float z) {
		parent.addVertex(pose.pose(), x, y, z);
        return this;
	}

	@Override
	public VertexConsumer setColor(int red, int green, int blue, int alpha) {
		parent.setColor(red, green, blue, alpha);
        return this;
	}

	@Override
	public VertexConsumer setColor(int i) {
		parent.setColor(i);
        return this;
	}

	@Override
	public VertexConsumer setUv(float u, float v) {
		parent.setUv(u, v);
        return this;
	}

	@Override
	public VertexConsumer setUv1(int u, int v) {
		parent.setUv1(u, v);
        return this;
	}

	@Override
	public VertexConsumer setUv2(int u, int v) {
		parent.setUv2(u, v);
        return this;
	}

    @Override
    public VertexConsumer setUv3(float u, float v) {
        parent.setUv3(u, v);
        return this;
    }

	@Override
	public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
		parent.setNormal(pose, normalX, normalY, normalZ);
        return this;
	}

	@Override
	public VertexConsumer setLineWidth(float v) {
		parent.setLineWidth(v);
        return this;
	}
}
