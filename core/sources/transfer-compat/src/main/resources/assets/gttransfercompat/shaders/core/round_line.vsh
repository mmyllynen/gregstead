#version 330
#extension GL_ARB_separate_shader_objects : require
layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat; mat4 TextureMat; vec4 ColorModulator; vec3 ModelOffset;
};
layout(std140) uniform Projection { mat4 ProjMat; };
layout(location = 0) in vec3 Position;
layout(location = 1) in vec4 Color;
layout(location = 2) in vec2 UV0;
layout(location = 3) in vec2 Segment;
layout(location = 4) in float LineRadius;
layout(location = 5) in vec4 SecondColor;
layout(location = 0) out vec4 vertexColor;
layout(location = 1) out vec2 localPosition;
layout(location = 2) flat out float radius;
layout(location = 3) flat out vec2 segment;
layout(location = 4) flat out vec4 secondColor;
void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vertexColor = Color; localPosition = UV0; radius = LineRadius; segment = Segment; secondColor = SecondColor;
}
