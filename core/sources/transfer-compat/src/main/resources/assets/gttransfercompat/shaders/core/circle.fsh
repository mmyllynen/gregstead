#version 330
#extension GL_ARB_separate_shader_objects : require
layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat; mat4 TextureMat; vec4 ColorModulator; vec3 ModelOffset;
};
layout(location = 0) in vec4 vertexColor;
layout(location = 1) in vec2 localPosition;
layout(location = 2) flat in float radius;
layout(location = 0) out vec4 fragColor;
void main() {
    float coverage = 1.0 - smoothstep(radius, radius + 1.0, length(localPosition));
    fragColor = vertexColor * coverage * ColorModulator;
    if (fragColor.a == 0.0) discard;
}
