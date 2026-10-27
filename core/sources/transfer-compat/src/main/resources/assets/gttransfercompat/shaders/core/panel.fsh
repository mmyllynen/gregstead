#version 330
#extension GL_ARB_separate_shader_objects : require
layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat; mat4 TextureMat; vec4 ColorModulator; vec3 ModelOffset;
};
layout(location = 0) in vec2 panelUV;
layout(location = 1) flat in float aspect;
layout(location = 0) out vec4 fragColor;
void main() {
    vec2 cell = fract(vec2(panelUV.x * aspect, panelUV.y) * 5.0);
    vec4 background = vec4(vec3(20.0 / 255.0), 0.95);
    vec4 square = vec4(vec3(40.0 / 255.0), 0.95);
    fragColor = abs(cell.x) < 0.1 && abs(cell.y) < 0.1 ? square : background;
    fragColor *= ColorModulator;
}
