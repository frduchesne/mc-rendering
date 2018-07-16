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

vec3 ambientC;
vec3 diffuseC;
vec3 speculrC;

const float gamma = 2.2;

vec3 calcLightColour(vec3 light_colour, float light_intensity, vec3 position, vec3 to_light_dir, vec3 normal)
{
    vec3 diffuseColour = vec3(0);
    vec3 specColour = vec3(0);

    // Diffuse Light
    float diffuseFactor = max(dot(normal, to_light_dir), 0.0);
    diffuseColour = diffuseC * light_colour * light_intensity * diffuseFactor;

    // Specular Light
    vec3 camera_direction = normalize(-position);
    vec3 from_light_dir = -to_light_dir;
    vec3 reflected_light = normalize(reflect(from_light_dir , normal));
    float specularFactor = max( dot(camera_direction, reflected_light), 0.0);
    specularFactor = pow(specularFactor, specularPower);
    specColour = speculrC * light_intensity  * specularFactor * light_colour;

    return (diffuseColour + specColour);
}

vec3 calcPointLight(PointLight light, vec3 position, vec3 normal)
{
    vec3 light_direction = light.mvPosition - position;
    vec3 to_light_dir  = normalize(light_direction);
    vec3 light_colour = calcLightColour(light.colour, light.intensity, position, to_light_dir, normal);

    // Apply Attenuation
    float distance = length(light_direction);
    float attenuationInv = light.att.constant + light.att.linear * distance +
        light.att.exponent * distance * distance;
    return light_colour / attenuationInv;
}

const float A = 0.25;		
const float B = 0.29;		
const float C = 0.10;			
const float D = 0.2;		
const float E = 0.03;
const float F = 0.35;

vec3 uncharted2Intermediate(vec3 color) {
	return ((color*(A*color+C*B)+D*E)/(color*(A*color+B)+D*F))-E/F;
}

vec3 uncharted2(vec3 color) {
	vec3 curr = uncharted2Intermediate(color*4.7);
	return pow(curr/uncharted2Intermediate(vec3(15.0)),vec3(1.0/gamma));
}

vec3 reinhard(in vec3 color) {
  color = color / (1.0 + color);
  return pow(color, vec3(1 / gamma));
}

vec3 burgess(in vec3 color) {
  vec3 maxColor = max(vec3(0.0), color - 0.004);
  vec3 retColor = (maxColor * (6.2 * maxColor + 0.05)) / (maxColor * (6.2 * maxColor + 2.3) + 0.06);
  return pow(retColor, vec3(1 / gamma));
}

void main()
{
    vec4 texColor = texture(texture_sampler, outTexCoord);
    if (texColor.a == 0) {
    	discard;
    }
    float alpha = texColor.a;

    // to gamma linear space
    ambientC = pow(texColor.rgb, vec3(gamma));

    diffuseC = ambientC;
    speculrC = ambientC;

    vec3 currNomal = mvVertexNormal;

    vec3 diffuseSpecularComp = vec3(0.0f);

    for (int i=0; i<MAX_POINT_LIGHTS; i++)
    {
        if ( pointLights[i].intensity > 0 )
        {
            diffuseSpecularComp += calcPointLight(pointLights[i], mvVertexPos, currNomal);
        }
    }

    vec3 color = ambientC * ambientLight + diffuseSpecularComp;
	
	// only gama correction
	//color = pow(color, vec3(1 / gamma));
	
	// reinhard
	//color = reinhard(color);
    
    // burgess
    //color = burgess(color);
    
    // uncharted2
    color = uncharted2(color);
    
    fragColor = vec4(color, alpha);
}
