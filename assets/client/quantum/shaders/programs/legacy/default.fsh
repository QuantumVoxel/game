#line 1
#ifdef GL_ES
#define LOWP lowp
#define MED mediump
#define HIGH highp
precision mediump float;
#else
#define MED
#define LOWP
#define HIGH
#endif

#if defined(specularTextureFlag) || defined(specularColorFlag)
#define specularFlag
#endif

#ifdef normalFlag
varying vec3 v_normal;
#endif//normalFlag

#if defined(colorFlag)
varying vec4 v_color;
#endif

#ifdef blendedFlag
varying float v_opacity;
#ifdef alphaTestFlag
varying float v_alphaTest;
#endif//alphaTestFlag
#endif//blendedFlag

#if defined(diffuseTextureFlag) || defined(specularTextureFlag) || defined(emissiveTextureFlag)
#define textureFlag
#endif

#ifdef diffuseTextureFlag
varying MED vec2 v_diffuseUV;
#endif

#ifdef specularTextureFlag
varying MED vec2 v_specularUV;
#endif

#ifdef emissiveTextureFlag
varying MED vec2 v_emissiveUV;
#endif

#ifdef diffuseColorFlag
uniform vec4 u_diffuseColor;
#endif

#ifdef diffuseTextureFlag
uniform sampler2D u_diffuseTexture;
#endif

#ifdef specularColorFlag
uniform vec4 u_specularColor;
#endif

#ifdef specularTextureFlag
uniform sampler2D u_specularTexture;
#endif

#ifdef normalTextureFlag
uniform sampler2D u_normalTexture;
#endif

#ifdef emissiveColorFlag
uniform vec4 u_emissiveColor;
#endif

#ifdef emissiveTextureFlag
uniform sampler2D u_emissiveTexture;
#endif

#ifdef lightingFlag
varying vec3 v_lightDiffuse;

#if    defined(ambientLightFlag) || defined(ambientCubemapFlag) || defined(sphericalHarmonicsFlag)
#define ambientFlag
#endif//ambientFlag

#ifdef specularFlag
varying vec3 v_lightSpecular;
#endif//specularFlag

#ifdef shadowMapFlag
uniform sampler2D u_shadowTexture;
uniform float u_shadowPCFOffset;
varying vec3 v_shadowMapUv;
#define separateAmbientFlag

float getShadowness(vec2 offset)
{
    const vec4 bitShifts = vec4(1.0, 1.0 / 255.0, 1.0 / 65025.0, 1.0 / 16581375.0);
    return step(v_shadowMapUv.z, dot(texture2D(u_shadowTexture, v_shadowMapUv.xy + offset), bitShifts));//+(1.0/255.0));
}

float getShadow()
{
    return (//getShadowness(vec2(0,0)) +
    getShadowness(vec2(u_shadowPCFOffset, u_shadowPCFOffset)) +
    getShadowness(vec2(-u_shadowPCFOffset, u_shadowPCFOffset)) +
    getShadowness(vec2(u_shadowPCFOffset, -u_shadowPCFOffset)) +
    getShadowness(vec2(-u_shadowPCFOffset, -u_shadowPCFOffset))) * 0.25;
}
#endif//shadowMapFlag

#if defined(ambientFlag) && defined(separateAmbientFlag)
varying vec3 v_ambientLight;
#endif//separateAmbientFlag

#if numDirectionalLights > 0
struct DirectionalLight
{
    vec3 color;
    vec3 direction;
};
uniform DirectionalLight u_dirLights[numDirectionalLights];
#endif// numDirectionalLights

#if numPointLights > 0
struct PointLight
{
    vec3 color;
    vec3 position;
};
uniform PointLight u_pointLights[numPointLights];
#endif// numPointLights

#endif//lightingFlag

#ifdef fogFlag
uniform vec4 u_fogColor;
varying float v_fog;
#endif// fogFlag

uniform vec3 u_cameraDirection;

struct SHC {
    vec3 L00, L1m1, L10, L11, L2m2, L2m1, L20, L21, L22;
};

SHC groove = SHC(
vec3(0.3783264, 0.4260425, 0.4504587),
vec3(0.2887813, 0.3586803, 0.4147053),
vec3(0.0379030, 0.0295216, 0.0098567),
vec3(-0.1033028, -0.1031690, -0.0884924),
vec3(-0.0621750, -0.0554432, -0.0396779),
vec3(0.0077820, -0.0148312, -0.0471301),
vec3(-0.0935561, -0.1254260, -0.1525629),
vec3(-0.0572703, -0.0502192, -0.0363410),
vec3(0.0203348, -0.0044201, -0.0452180)
);

SHC beach = SHC(
vec3(0.6841148, 0.6929004, 0.7069543),
vec3(0.3173355, 0.3694407, 0.4406839),
vec3(-0.1747193, -0.1737154, -0.1657420),
vec3(-0.4496467, -0.4155184, -0.3416573),
vec3(-0.1690202, -0.1703022, -0.1525870),
vec3(-0.0837808, -0.0940454, -0.1027518),
vec3(-0.0319670, -0.0214051, -0.0147691),
vec3(0.1641816, 0.1377558, 0.1010403),
vec3(0.3697189, 0.3097930, 0.2029923)
);

SHC tomb = SHC(
vec3(1.0351604, 0.7603549, 0.7074635),
vec3(0.4442150, 0.3430402, 0.3403777),
vec3(-0.2247797, -0.1828517, -0.1705181),
vec3(0.7110400, 0.5423169, 0.5587956),
vec3(0.6430452, 0.4971454, 0.5156357),
vec3(-0.1150112, -0.0936603, -0.0839287),
vec3(-0.3742487, -0.2755962, -0.2875017),
vec3(-0.1694954, -0.1343096, -0.1335315),
vec3(0.5515260, 0.4222179, 0.4162488)
);

vec3 sh_light(vec3 normal, SHC l) {
    float x = normal.x;
    float y = normal.y;
    float z = normal.z;

    const float C1 = 0.429043;
    const float C2 = 0.511664;
    const float C3 = 0.743125;
    const float C4 = 0.886227;
    const float C5 = 0.247708;

    return (
    C1 * l.L22 * (x * x - y * y) +
    C3 * l.L20 * z * z +
    C4 * l.L00 -
    C5 * l.L20 +
    2.0 * C1 * l.L2m2 * x * y +
    2.0 * C1 * l.L21 * x * z +
    2.0 * C1 * l.L2m1 * y * z +
    2.0 * C2 * l.L11 * x +
    2.0 * C2 * l.L1m1 * y +
    2.0 * C2 * l.L10 * z
    );
}

vec3 gamma(vec3 color) {
    return pow(color, vec3(1.0 / 2.0));
}

#if defined(lightingFlag)
#if numDirectionalLights > 0
vec3 computeDirectionalLight(DirectionalLight light, vec3 normal, vec3 viewDir, vec3 fragmentPos) {
    // Directional light is assumed to be infinitely far away, so we treat the light's direction as constant
    vec3 lightDir = normalize(-light.direction);

    // Calculate diffuse shading using Lambert's cosine law
    float diff = max(dot(normal, lightDir), 0.0);

    // Simulate shadow based on the angle to the light
    float shadow = diff < 0.5 ? 0.2 : 1.0;  // Simple threshold to simulate soft shadows

    // Specular shading (using Blinn-Phong model)
    vec3 halfDir = normalize(lightDir + viewDir);
    float spec = pow(max(dot(normal, halfDir), 0.0), 16.0); // Glossy highlight

    // Calculate final color with shadowed lighting
    vec3 diffuse = light.color * diff * shadow;
    vec3 specular = light.color * spec * shadow;

    return diffuse + specular;
}

#endif

#if numPointLights > 0

vec3 computePointLight(PointLight light, vec3 normal, vec3 viewDir, vec3 fragmentPos) {
    vec3 lightDir = normalize(light.position - fragmentPos);
    float dist = length(light.position - fragmentPos);

    // Inverse square law for attenuation
    float attenuation = 1.0 / (1.0 + 0.09 * dist + 0.032 * (dist * dist)); // Physically accurate falloff

    // Diffuse lighting
    float diff = max(dot(normal, lightDir), 0.0);

    // Specular lighting (Blinn-Phong)
    vec3 halfDir = normalize(lightDir + viewDir);
    float spec = pow(max(dot(normal, halfDir), 0.0), 32.0);

    // Final lighting based on attenuation
    vec3 diffuse = light.color * diff * attenuation;
    vec3 specular = light.color * spec * attenuation;

    return diffuse + specular;
}

#endif

vec3 computeLighting(vec3 normal, vec3 viewDir, vec3 fragmentPos) {
    vec3 totalLight = vec3(0.0);

    // Apply directional lights (shadowing simulated)
    #if numDirectionalLights > 0
    for (int i = 0; i < numDirectionalLights; i++) {
        totalLight += computeDirectionalLight(u_dirLights[i], normal, viewDir, fragmentPos);
    }
    #endif

    // Apply point lights (attenuation applied)
    #if numPointLights > 0
    for (int i = 0; i < numPointLights; i++) {
        totalLight += computePointLight(u_pointLights[i], normal, viewDir, fragmentPos);
    }
    #endif

    return totalLight;
}

#endif

void main() {
    #if defined(normalFlag)
    vec3 normal = v_normal;
    #else
    vec3 normal = vec3(0.0, 0.0, 0.0);
    #endif// normalFlag

    #if defined(diffuseTextureFlag) && defined(diffuseColorFlag) && defined(colorFlag)
    vec4 diffuse = texture2D(u_diffuseTexture, v_diffuseUV) * u_diffuseColor * v_color;
    #elif defined(diffuseTextureFlag) && defined(diffuseColorFlag)
    vec4 diffuse = texture2D(u_diffuseTexture, v_diffuseUV) * u_diffuseColor;
    #elif defined(diffuseTextureFlag) && defined(colorFlag)
    vec4 diffuse = texture2D(u_diffuseTexture, v_diffuseUV) * v_color;
    #elif defined(diffuseTextureFlag)
    vec4 diffuse = texture2D(u_diffuseTexture, v_diffuseUV);
    #elif defined(diffuseColorFlag) && defined(colorFlag)
    vec4 diffuse = u_diffuseColor * v_color;
    #elif defined(diffuseColorFlag)
    vec4 diffuse = u_diffuseColor;
    #elif defined(colorFlag)
    vec4 diffuse = v_color;
    #else
    vec4 diffuse = vec4(1.0);
    #endif

    #if defined(emissiveTextureFlag) && defined(emissiveColorFlag)
    vec4 emissive = texture2D(u_emissiveTexture, v_emissiveUV) * u_emissiveColor;
    #elif defined(emissiveTextureFlag)
    vec4 emissive = texture2D(u_emissiveTexture, v_emissiveUV);
    #elif defined(emissiveColorFlag)
    vec4 emissive = u_emissiveColor;
    #else
    vec4 emissive = vec4(0.0);
    #endif

    // Compute lighting
//    #if defined(normalFlag) && defined(lightingFlag)
//    vec3 lighting = computeLighting(normal, vec3(0.0, 0.0, 0.0), gl_FragCoord.xyz);
//    #else
//    vec3 lighting = vec3(1.0);
//    #endif

    // Apply shadow if enabled
    #ifdef shadowMapFlag
    vec3 lighting = vec3(getShadow());
    #else
    vec3 lighting = vec3(1.0, 0.0, 0.0);
    #endif

    #ifdef fogFlag
    gl_FragColor.rgb = mix(gl_FragColor.rgb, u_fogColor.rgb, v_fog) * gamma(sh_light(v_normal, groove)).r;
    #else
    gl_FragColor.rgb = gl_FragColor.rgb * gamma(sh_light(v_normal, groove)).r;
    #endif// end fogFlag

    gl_FragColor = (vec4(lighting, 1.0) / 2.0 + 0.5) * diffuse + emissive;

    #ifdef blendedFlag
    gl_FragColor.a = diffuse.a * v_opacity;
    #ifdef alphaTestFlag
    if (gl_FragColor.a <= v_alphaTest) {
        discard;
    }
    #endif
    #else
    gl_FragColor.a = 1.0;
    #endif

}
