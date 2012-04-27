uniform vec3 m_region1;
uniform vec3 m_region2;
uniform vec3 m_region3;
uniform vec3 m_region4;

uniform sampler2D m_region1ColorMap;
uniform sampler2D m_region2ColorMap;
uniform sampler2D m_region3ColorMap;
uniform sampler2D m_region4ColorMap;

uniform vec4 m_baseColor;

uniform vec3 m_patchCenter;
uniform float m_planetRadius;

varying vec3 normal;
varying vec4 position;
varying vec2 texCoord1;

float getWeight(float value, float vMin, float vMax) {

    float weight;
    float range = vMax - vMin;
    weight = (range - abs(value - vMax)) / range;
    weight = max(0.0, weight);

    return weight;
}

vec4 GenerateTerrainColor() {
    
    vec4 region1Color = 0.25 * texture2D(m_region1ColorMap, texCoord1)
            + 0.25 * texture2D(m_region1ColorMap, (1.0 / 8.0) * texCoord1)
            + 0.25 * texture2D(m_region1ColorMap, (1.0 / (8.0*8.0)) * texCoord1)
            + 0.25 * texture2D(m_region1ColorMap, (1.0 / (8.0*8.0*8.0)) * texCoord1);
    vec4 region2Color = 0.25 * texture2D(m_region2ColorMap, texCoord1)
            + 0.25 * texture2D(m_region2ColorMap, (1.0 / 8.0) * texCoord1)
            + 0.25 * texture2D(m_region2ColorMap, (1.0 / (8.0*8.0)) * texCoord1)
            + 0.25 * texture2D(m_region2ColorMap, (1.0 / (8.0*8.0*8.0)) * texCoord1);
    vec4 region3Color = 0.25 * texture2D(m_region3ColorMap, texCoord1)
            + 0.25 * texture2D(m_region3ColorMap, (1.0 / 8.0) * texCoord1)
            + 0.25 * texture2D(m_region3ColorMap, (1.0 / (8.0*8.0)) * texCoord1)
            + 0.25 * texture2D(m_region3ColorMap, (1.0 / (8.0*8.0*8.0)) * texCoord1);
    vec4 region4Color = 0.25 * texture2D(m_region4ColorMap, texCoord1)
            + 0.25 * texture2D(m_region4ColorMap, (1.0 / 8.0) * texCoord1)
            + 0.25 * texture2D(m_region4ColorMap, (1.0 / (8.0*8.0)) * texCoord1)
            + 0.25 * texture2D(m_region4ColorMap, (1.0 / (8.0*8.0*8.0)) * texCoord1);

    vec4 color;
    color = vec4(0,0,0,1);

    float height = length(m_patchCenter + position) - m_planetRadius;
    //float slope = degrees(acos(dot(normalize(m_patchCenter + position), n_normal)));

    if (height <= m_region1.y)
        color = m_baseColor;

    float m_regionMin = 0.0;
    float m_regionMax = 0.0;
    float m_regionWeight = 0.0;

    // Terrain m_region 1.
    m_regionMin = m_region1.x;
    m_regionMax = m_region1.y;
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

void main() {
    vec4 color = GenerateTerrainColor();
    gl_FragColor = color;
}
