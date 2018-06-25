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
    mat4 shadowViewProjTexMatrices[4];
};

uniform sampler2D texture_sampler;
uniform sampler2DShadow shadowMap;

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

#define NUM_SAMPLES 16

const vec2 filterKernel[NUM_SAMPLES] =
{
  vec2(-0.94201624, -0.39906216),
  vec2(0.94558609, -0.76890725),
  vec2(-0.094184101, -0.92938870),
  vec2(0.34495938, 0.29387760),
  vec2(-0.91588581, 0.45771432),
  vec2(-0.81544232, -0.87912464),
  vec2(-0.38277543, 0.27676845),
  vec2(0.97484398, 0.75648379),
  vec2(0.44323325, -0.97511554),
  vec2(0.53742981, -0.47373420),
  vec2(-0.26496911, -0.41893023),
  vec2(0.79197514, 0.19090188),
  vec2(-0.24188840, 0.99706507),
  vec2(-0.81409955, 0.91437590),
  vec2(0.19984126, 0.78641367),
  vec2(0.14383161, -0.14100790)
};

#define FILTER_RADIUS 1.5

const vec3 faceVectors[4] =
{
  vec3(0.0, -0.57735026, 0.81649661),
  vec3(0.0, -0.57735026, -0.81649661),
  vec3(-0.81649661, 0.57735026, 0.0),
  vec3(0.81649661, 0.57735026, 0.0)
};

uint GetFaceIndex(in vec3 dir)
{
  mat4x3 faceMatrix;
  faceMatrix[0] = faceVectors[0];
  faceMatrix[1] = faceVectors[1];
  faceMatrix[2] = faceVectors[2];
  faceMatrix[3] = faceVectors[3];
  vec4 dotProducts = dir*faceMatrix;
  float maximum = max (max(dotProducts.x, dotProducts.y), max(dotProducts.z, dotProducts.w));
  uint index;
  if(maximum == dotProducts.x)
    index = 0;
  else if(maximum == dotProducts.y)
    index = 1;
  else if(maximum == dotProducts.z)
    index = 2;
  else
    index = 3;
  return index;
}

float calcShadow(vec4 position, PointLight pointLight)
{
  vec3 lightVec = pointLight.mPosition -position.xyz;
  vec3 lightVecN = normalize(lightVec);
  uint index = GetFaceIndex(-lightVecN);
  vec4 result = pointLight.shadowViewProjTexMatrices[index]*position;
  result.xyz /= result.w;
  result.xyz = (result.xyz*0.5)+0.5;
  float shadowTerm = 0.0;
  vec2 invShadowMapSize = vec2(1.0f/1024.0f, 1.0f/1024.0f);
  const vec2 filterRadius = invShadowMapSize.xy*FILTER_RADIUS;
  for(uint i=0; i<NUM_SAMPLES; i++)
  {
    vec3 texCoords;
    texCoords.xy = result.xy+(filterKernel[i]*filterRadius);
    texCoords.z = result.z;
    shadowTerm +=  texture(shadowMap, texCoords);
  }
  shadowTerm /= NUM_SAMPLES;
  return shadowTerm;
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
            float shadow = calcShadow(vec4(mVertexPos.xyz, 1), pointLights[i]);
            diffuseSpecularComp += calcPointLight(pointLights[i], mvVertexPos, currNomal) * shadow;
        }
    }

    fragColor = clamp(ambientC * vec4(ambientLight, 1) + diffuseSpecularComp, 0, 1);
}
