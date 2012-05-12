uniform vec4 g_LightDirection;

uniform float m_PlanetRadius;
uniform float m_HeightScale;
uniform float m_AtmosphereRadius;
uniform float m_AtmosphereDensity;

varying vec3 vNormal;
varying vec4 positionObjectSpace;
varying vec2 texCoord;
varying vec3 AmbientSum;
varying vec4 DiffuseSum;
varying vec3 vViewDir;
varying vec4 vLightDir;
varying vec3 lightVec;

float lightComputeDiffuse(in vec3 norm, in vec3 lightdir, in vec3 viewdir){
    return max(0.0, dot(norm, lightdir));
}

vec2 computeLighting(in vec3 wvNorm, in vec3 wvViewDir, in vec3 wvLightDir){
    float diffuseFactor = lightComputeDiffuse(wvNorm, wvLightDir, wvViewDir);
    float att = vLightDir.w;
    return vec2(diffuseFactor) * vec2(att);
}

void main() {
    vec4 color = vec4(1,0.5,0.5,1);

    vec4 lightDir = vLightDir;
    lightDir.xyz = normalize(lightDir.xyz);
    vec3 viewDir = normalize(vViewDir);
    vec2 light = computeLighting(vNormal, viewDir, lightDir.xyz);

    gl_FragColor.rgb =  AmbientSum * color.rgb + DiffuseSum.rgb * color.rgb * vec3(light.x);
}