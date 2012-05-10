uniform vec3 m_region1;
uniform vec3 m_region2;
uniform vec3 m_region3;
uniform vec3 m_region4;

uniform sampler2D m_region1ColorMap;
uniform sampler2D m_region2ColorMap;
uniform sampler2D m_region3ColorMap;
uniform sampler2D m_region4ColorMap;

uniform vec3 m_patchCenter;
uniform float m_planetRadius;

varying vec3 vNormal;
varying vec4 position;
varying vec2 texCoord;

varying vec3 AmbientSum;
varying vec4 DiffuseSum;

uniform vec4 g_LightDirection;
varying vec3 vViewDir;
varying vec4 vLightDir;
varying vec3 lightVec;

float getWeight(float value, float vMin, float vMax) {
    float weight;
    float range = vMax - vMin;
    weight = (range - abs(value - vMax)) / range;
    weight = max(0.0, weight);

    return weight;
}

vec4 generateTerrainColor(float height) {
    
    vec4 region1Color = 0.25 * texture2D(m_region1ColorMap, texCoord)
            + 0.25 * texture2D(m_region1ColorMap, (1.0 / 8.0) * texCoord)
            + 0.25 * texture2D(m_region1ColorMap, (1.0 / (8.0*8.0)) * texCoord)
            + 0.25 * texture2D(m_region1ColorMap, (1.0 / (8.0*8.0*8.0)) * texCoord);
    vec4 region2Color = 0.25 * texture2D(m_region2ColorMap, texCoord)
            + 0.25 * texture2D(m_region2ColorMap, (1.0 / 8.0) * texCoord)
            + 0.25 * texture2D(m_region2ColorMap, (1.0 / (8.0*8.0)) * texCoord)
            + 0.25 * texture2D(m_region2ColorMap, (1.0 / (8.0*8.0*8.0)) * texCoord);
    vec4 region3Color = 0.25 * texture2D(m_region3ColorMap, texCoord)
            + 0.25 * texture2D(m_region3ColorMap, (1.0 / 8.0) * texCoord)
            + 0.25 * texture2D(m_region3ColorMap, (1.0 / (8.0*8.0)) * texCoord)
            + 0.25 * texture2D(m_region3ColorMap, (1.0 / (8.0*8.0*8.0)) * texCoord);
    vec4 region4Color = 0.25 * texture2D(m_region4ColorMap, texCoord)
            + 0.25 * texture2D(m_region4ColorMap, (1.0 / 8.0) * texCoord)
            + 0.25 * texture2D(m_region4ColorMap, (1.0 / (8.0*8.0)) * texCoord)
            + 0.25 * texture2D(m_region4ColorMap, (1.0 / (8.0*8.0*8.0)) * texCoord);

    vec4 color;
    color = vec4(0,0,0,1);

    //float slope = degrees(acos(dot(normalize(m_patchCenter + position), n_normal)));

    float m_regionMin = 0.0;
    float m_regionMax = 0.0;
    float m_regionWeight = 0.0;

    // Terrain m_region 1.
    m_regionMin = m_region1.x;
    m_regionMax = m_region1.y;
    // Use region1's texture as the base
    if (height <= m_regionMax)
        color = region1Color;
    m_regionWeight = getWeight(height, m_regionMin, m_regionMax);
    color = mix(color, region1Color, m_regionWeight);

    // Terrain m_region 2.
    m_regionMin = m_region2.x;
    m_regionMax = m_region2.y;
    m_regionWeight = getWeight(height, m_regionMin, m_regionMax);
    color = mix(color, region2Color, m_regionWeight);

    // Terrain m_region 3.
    m_regionMin = m_region3.x;
    m_regionMax = m_region3.y;
    m_regionWeight = getWeight(height, m_regionMin, m_regionMax);
    color = mix(color, region3Color, m_regionWeight);

    // Terrain m_region 4.
    m_regionMin = m_region4.x;
    m_regionMax = m_region4.y;
    m_regionWeight = getWeight(height, m_regionMin, m_regionMax);
    color = mix(color, region4Color, m_regionWeight);

    return (color);
}

float lightComputeDiffuse(in vec3 norm, in vec3 lightdir, in vec3 viewdir){
    #ifdef MINNAERT
        float NdotL = max(0.0, dot(norm, lightdir));
        float NdotV = max(0.0, dot(norm, viewdir));
        return NdotL * pow(max(NdotL * NdotV, 0.1), -1.0) * 0.5;
    #else
        return max(0.0, dot(norm, lightdir));
    #endif
}

vec2 computeLighting(in vec3 wvNorm, in vec3 wvViewDir, in vec3 wvLightDir){
    float diffuseFactor = lightComputeDiffuse(wvNorm, wvLightDir, wvViewDir);
    #ifdef HQ_ATTENUATION
        float att = clamp(1.0 - g_LightPosition.w * length(lightVec), 0.0, 1.0);
    #else
        float att = vLightDir.w;
    #endif

    return vec2(diffuseFactor) * vec2(att);
}

void main() {
    // Compute height of position from surface of planet
    float height = length(vec4(m_patchCenter, 1.0) + position) - m_planetRadius;

    vec4 color = generateTerrainColor(height);

    vec4 lightDir = vLightDir;
    lightDir.xyz = normalize(lightDir.xyz);
    vec3 viewDir = normalize(vViewDir);

    vec2 light = computeLighting(vNormal, viewDir, lightDir.xyz);

    gl_FragColor.rgb =  AmbientSum * color.rgb + DiffuseSum.rgb * color.rgb * vec3(light.x);
}
