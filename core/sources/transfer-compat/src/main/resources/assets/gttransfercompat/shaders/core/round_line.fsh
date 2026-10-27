#version 330
#extension GL_ARB_separate_shader_objects : require
layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat; mat4 TextureMat; vec4 ColorModulator; vec3 ModelOffset;
};
layout(location = 0) in vec4 vertexColor;
layout(location = 1) in vec2 localPosition;
layout(location = 2) flat in float radius;
layout(location = 3) flat in vec2 segment;
layout(location = 4) flat in vec4 secondColor;
layout(location = 0) out vec4 fragColor;
void main() {
    float lengthSquared = dot(segment, segment);
    // Equal endpoints are a circle in the first color; avoid upstream's undefined zero division.
    float axial = lengthSquared > 0.0 ? dot(localPosition, segment) / lengthSquared : 0.0;
    float distance = length(localPosition - segment * clamp(axial, 0.0, 1.0));
    float coverage = 1.0 - smoothstep(radius, radius + 2.0, distance);
    fragColor = mix(vertexColor, secondColor, axial) * coverage * ColorModulator;
    if (fragColor.a == 0.0) discard;
}
