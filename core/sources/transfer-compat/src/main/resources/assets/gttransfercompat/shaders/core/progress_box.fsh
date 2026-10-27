#version 330
#extension GL_ARB_separate_shader_objects : require
layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat; mat4 TextureMat; vec4 ColorModulator; vec3 ModelOffset;
};
layout(location = 0) in vec4 vertexColor;
layout(location = 1) in vec2 localPosition;
layout(location = 2) flat in vec2 boxSize;
layout(location = 3) flat in vec4 radii;
layout(location = 4) flat in float blur;
layout(location = 5) flat in vec4 secondColor;
layout(location = 6) flat in float progress;
layout(location = 0) out vec4 fragColor;
float sdRoundedBox(vec2 p, vec2 b, vec4 r) {
    r.xy = p.x > 0.0 ? r.xy : r.zw;
    r.x = p.y > 0.0 ? r.x : r.y;
    vec2 q = abs(p) - b + r.x;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r.x;
}
void main() {
    float distance = sdRoundedBox(localPosition, boxSize, radii);
    // Equivalent to upstream smoothstep(Blur, 0, distance), without reversed-edge GLSL undefined behavior.
    float coverage = 1.0 - smoothstep(0.0, blur, distance);
    float vertexPercent = (localPosition.x + boxSize.x + 2.0) / (2.0 * boxSize.x);
    float transition = smoothstep(progress - 0.005, progress + 0.005, vertexPercent);
    fragColor = mix(vertexColor, secondColor, transition) * coverage * ColorModulator;
    if (fragColor.a == 0.0) discard;
}
