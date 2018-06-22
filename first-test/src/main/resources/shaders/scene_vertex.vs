#version 450

layout (location=0) in vec3 position;
layout (location=1) in vec2 texCoord;
layout (location=2) in vec3 vertexNormal;
layout (location=5) in mat4 modelInstancedMatrix;
layout (location=9) in vec2 texOffset;
layout (location=10) in float selectedInstanced;

out vec2 outTexCoord;
out vec3 mvVertexNormal;
out vec3 mvVertexPos;
out vec3 mVertexPos;
out mat4 outModelViewMatrix;
out float outSelected;

uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;
uniform int numCols;
uniform int numRows;

void main()
{
    outSelected = selectedInstanced;
    
    vec4 initPos = vec4(position, 1.0);
    vec4 initNormal = vec4(vertexNormal, 0.0);
    mat4 modelViewMatrix =  viewMatrix * modelInstancedMatrix;
    vec4 mvPos = modelViewMatrix * initPos;
    gl_Position = projectionMatrix * mvPos;

    // Support for texture atlas, update texture coordinates
    float x = (texCoord.x / numCols + texOffset.x);
    float y = (texCoord.y / numRows + texOffset.y);
    outTexCoord = vec2(x, y);

    mvVertexNormal = normalize(modelViewMatrix * initNormal).xyz;
    mvVertexPos = mvPos.xyz;
    mVertexPos = (modelInstancedMatrix * initPos).xyz;
    outModelViewMatrix = modelViewMatrix;
}
