#version 450

in vec2 outTexCoord;

out vec4 fragColor;

struct Material
{
    vec4 ambient;
    int hasTexture;
};

uniform sampler2D texture_sampler;

uniform vec3 ambientLight;
uniform Material material;

vec4 ambientC;

void setupColours(Material material, vec2 textCoord)
{
    if (material.hasTexture == 1)
    {
        ambientC = texture(texture_sampler, textCoord);
    }
    else
    {
        ambientC = material.ambient;
    }
}

void main()
{
    setupColours(material, outTexCoord);

    fragColor = clamp(ambientC * vec4(1), 0, 1);
}
