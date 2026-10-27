#version 330
#extension GL_ARB_separate_shader_objects : require

layout(location=2) flat in float iTime;

layout(location=0) in vec2 texCoord0;
layout(location=1) in vec4 vertexColor;

layout(location=0) out vec4 fragColor;

void main() {
    //get the colour
    vec2 uv = texCoord0.xy;
    uv = uv * 0.5 + 0.25;
    uv = uv * 0.5 + 0.25;
    vec3 horColour = vec3(0.25, 0.25, 0.25);
    float mode = 0.;
    float xCol = abs(mod(-iTime + uv.x, 4.0) - 2.0);
    horColour = vertexColor.rgb;

    //main beam
    uv = (2.0 * uv) - 1.0;
    float beamWidth = abs(1.5 / (30.0 * uv.y)) * 1. / (2. * xCol + 2.) ;
    vec3 horBeam = vec3(beamWidth);

    fragColor = vec4((horBeam * horColour), length(horBeam) * vertexColor.a);
}
