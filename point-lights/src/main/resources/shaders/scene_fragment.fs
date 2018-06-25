#version 450

const int MAX_POINT_LIGHTS = 5;

in vec2 outTexCoord;
in vec3 mvVertexNormal;
in vec3 mvVertexPos;
in vec3 mVertexPos;
in mat4 outModelViewMatrix;

out vec4 fragColor;

struct Attenuation
{
    float constant;
    float linear;
    float exponent;
};

struct PointLight
{
    vec3 colour;
    vec3 mvPosition;
    vec3 mPosition;
    float intensity;
    Attenuation att;
};

uniform sampler2D texture_sampler;

uniform vec3 ambientLight;
uniform float specularPower;
uniform PointLight pointLights[MAX_POINT_LIGHTS];

vec4 ambientC;
vec4 diffuseC;
vec4 speculrC;

vec4 calcLightColour(vec3 light_colour, float light_intensity, vec3 position, vec3 to_light_dir, vec3 normal)
{
    vec4 diffuseColour = vec4(0, 0, 0, 0);
    vec4 specColour = vec4(0, 0, 0, 0);

    // Diffuse Light
    float diffuseFactor = max(dot(normal, to_light_dir), 0.0);
    diffuseColour = diffuseC * vec4(light_colour, 1.0) * light_intensity * diffuseFactor;

    // Specular Light
    vec3 camera_direction = normalize(-position);
    vec3 from_light_dir = -to_light_dir;
    vec3 reflected_light = normalize(reflect(from_light_dir , normal));
    float specularFactor = max( dot(camera_direction, reflected_light), 0.0);
    specularFactor = pow(specularFactor, specularPower);
    specColour = speculrC * light_intensity  * specularFactor * vec4(light_colour, 1.0);

    return (diffuseColour + specColour);
}

vec4 calcPointLight(PointLight light, vec3 position, vec3 normal)
{
    vec3 light_direction = light.mvPosition - position;
    vec3 to_light_dir  = normalize(light_direction);
    vec4 light_colour = calcLightColour(light.colour, light.intensity, position, to_light_dir, normal);

    // Apply Attenuation
    float distance = length(light_direction);
    float attenuationInv = light.att.constant + light.att.linear * distance +
        light.att.exponent * distance * distance;
    return light_colour / attenuationInv;
}

void main()
{
    ambientC = texture(texture_sampler, outTexCoord);
    diffuseC = ambientC;
    speculrC = ambientC;

    vec3 currNomal = mvVertexNormal;

    vec4 diffuseSpecularComp = vec4(0.0f);

    for (int i=0; i<MAX_POINT_LIGHTS; i++)
    {
        if ( pointLights[i].intensity > 0 )
        {
            diffuseSpecularComp += calcPointLight(pointLights[i], mvVertexPos, currNomal);
        }
    }

    fragColor = clamp(ambientC * vec4(ambientLight, 1) + diffuseSpecularComp, 0, 1);
}
