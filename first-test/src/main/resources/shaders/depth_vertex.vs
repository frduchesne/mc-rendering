#version 450

layout (location=0) in vec3 position;
layout (location=5) in mat4 modelInstancedMatrix;

uniform mat4 projectionMatrix;

out gl_PerVertex
{
  vec4 gl_Position;
};

void main()
{
    gl_Position = modelInstancedMatrix * vec4(position, 1.0);
}
