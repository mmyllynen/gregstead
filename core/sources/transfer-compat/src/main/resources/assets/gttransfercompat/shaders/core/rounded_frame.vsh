#version 330
#extension GL_ARB_separate_shader_objects : require
layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat; mat4 TextureMat; vec4 ColorModulator; vec3 ModelOffset;
};
layout(std140) uniform Projection { mat4 ProjMat; };
layout(location = 0) in vec3 Position;
layout(location = 1) in vec4 Color;
layout(location = 2) in vec2 UV0;
layout(location = 3) in vec2 ShapeSize;
layout(location = 4) in vec4 ShapeRadii;
layout(location = 5) in vec4 FrameRadii;
layout(location = 6) in float ShapeThickness;
layout(location = 7) in float ShapeBlur;
layout(location = 0) out vec4 vertexColor;
layout(location = 1) out vec2 localPosition;
layout(location = 2) flat out vec2 boxSize;
layout(location = 3) flat out vec4 radii;
layout(location = 4) flat out float blur;
layout(location = 5) flat out vec4 frameRadii;
layout(location = 6) flat out float thickness;
void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vertexColor = Color; localPosition = UV0; boxSize = ShapeSize; radii = ShapeRadii; blur = ShapeBlur; frameRadii = FrameRadii; thickness = ShapeThickness;
}
