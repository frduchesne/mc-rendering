#version 450

layout (location=0) in vec3 position;
layout (location=1) in vec2 texCoord;
layout (location=2) in vec3 vertexNormal;
layout (location=5) in mat4 modelInstancedMatrix;

out vec2 outTexCoord;

uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;

void main()
{

    vec4 initPos = vec4(position, 1.0);
    vec4 initNormal = vec4(vertexNormal, 0.0);
    mat4 modelViewMatrix =  viewMatrix * modelInstancedMatrix;
    vec4 mvPos = modelViewMatrix * initPos;
    gl_Position = projectionMatrix * mvPos;

    outTexCoord = texCoord;

}
