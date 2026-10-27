#version 330
#extension GL_ARB_separate_shader_objects : require
uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
layout(location=0) in vec2 fluidUV;
layout(location=1) in vec2 maskUV;
layout(location=2) in vec4 tint;
layout(location=0) out vec4 fragColor;
layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    mat4 TextureMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
};
void main() {
    if (any(lessThan(maskUV, vec2(0.0))) || any(greaterThan(maskUV, vec2(1.0)))) discard;
    vec4 color = texture(Sampler0, fluidUV) * tint * ColorModulator;
#ifndef SQUARE_MASK
    color.a *= texture(Sampler1, maskUV).a;
#endif
    if (color.a <= 0.0) discard;
    fragColor = color;
}
