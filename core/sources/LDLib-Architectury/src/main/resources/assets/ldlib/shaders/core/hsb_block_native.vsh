#version 330
#extension GL_ARB_separate_shader_objects : require

layout(location = 0) in vec3 Position;
layout(location = 1) in vec4 HSB_ALPHA;

layout(location = 0) out vec4 hsb_alpha;

layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    mat4 TextureMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
};
layout(std140) uniform Projection {
    mat4 ProjMat;
};

void main() {
//    gl_Position = vec4(Postion.x * 2 -1, -(Postion.y * 2 - 1), 1.0, 1.0);
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    hsb_alpha = vec4(HSB_ALPHA.r / 360.0,HSB_ALPHA.gba);
}
