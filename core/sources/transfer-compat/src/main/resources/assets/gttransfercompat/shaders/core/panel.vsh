#version 330
#extension GL_ARB_separate_shader_objects : require
layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat; mat4 TextureMat; vec4 ColorModulator; vec3 ModelOffset;
};
layout(std140) uniform Projection { mat4 ProjMat; };
layout(location = 0) in vec3 Position;
layout(location = 1) in vec2 UV0;
layout(location = 2) in float PanelAspect;
layout(location = 0) out vec2 panelUV;
layout(location = 1) flat out float aspect;
void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    panelUV = UV0; aspect = PanelAspect;
}
