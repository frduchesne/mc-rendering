#version 450

out vec4 fragColor;

in vec4 projPosition;

float near = 0.01;
float far  = 100.0;

float LinearizeDepth(float depth)
{
    float z = depth * 2.0 - 1.0; // back to NDC
    return (2.0 * near * far) / (far + near - z * (far - near));
}

void main()
{
    //float depth = LinearizeDepth(gl_FragCoord.z); // divide by far for demonstration
    fragColor = vec4(gl_FragCoord.z);
}
