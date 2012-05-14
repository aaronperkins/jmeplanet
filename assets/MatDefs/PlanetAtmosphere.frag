uniform vec4 g_LightDirection;
uniform vec4 g_LightPosition;

uniform float m_Shininess;
uniform float m_PlanetRadius;
uniform float m_AtmosphereRadius;
uniform float m_AtmosphereDensity;

varying vec3 vNormal;
varying vec4 positionObjectSpace;
varying vec2 texCoord;
varying vec3 AmbientSum;
varying vec4 DiffuseSum;
varying vec3 SpecularSum;
varying vec3 vViewDir;
varying vec4 vLightDir;
varying vec3 lightVec;

#ifdef LOGARITHIMIC_DEPTH_BUFFER
uniform vec2 g_FrustumNearFar;
varying vec4 positionProjectionSpace;
#endif

float tangDot(in vec3 v1, in vec3 v2){
    float d = dot(v1,v2);
    return d;
}

float lightComputeDiffuse(in vec3 norm, in vec3 lightdir, in vec3 viewdir){
        float NdotL = max(0.0, dot(norm, lightdir));
        float NdotV = max(0.0, dot(norm, viewdir));
        return NdotL * pow(max(NdotL * NdotV, 0.1), -1.0) * 0.5;
}

float lightComputeSpecular(in vec3 norm, in vec3 viewdir, in vec3 lightdir, in float shiny){
       // Standard Phong
       vec3 R = reflect(-lightdir, norm);
       return pow(max(tangDot(R, viewdir), 0.0), shiny);
}


vec2 computeLighting(in vec3 wvNorm, in vec3 wvViewDir, in vec3 wvLightDir){
    float diffuseFactor = lightComputeDiffuse(wvNorm, wvLightDir, wvViewDir);
    float specularFactor = lightComputeSpecular(wvNorm, wvViewDir, wvLightDir, m_Shininess);
    //float att = vLightDir.w;
    float att = clamp(1.0 - g_LightPosition.w * length(lightVec), 0.0, 1.0);
    specularFactor *= diffuseFactor;
    return vec2(diffuseFactor, specularFactor) * vec2(att);
}

void main() {
    vec4 lightDir = vLightDir;
    lightDir.xyz = normalize(lightDir.xyz);
    vec3 viewDir = normalize(vViewDir);
    vec2 light = computeLighting(vNormal, viewDir, lightDir.xyz);

    vec4 SpecularSum2 = vec4(SpecularSum, 1.0);

    gl_FragColor.rgb =  AmbientSum  +
                           DiffuseSum.rgb  * vec3(light.x) +
                           SpecularSum2.rgb * vec3(light.y);
    gl_FragColor.a = max(max(gl_FragColor.r, gl_FragColor.b), gl_FragColor.g);

    #ifdef LOGARITHIMIC_DEPTH_BUFFER
        const float C = 1.0;
        const float offset = 1.0;
        gl_FragDepth = (log(C * positionProjectionSpace.z + offset) / log(C * g_FrustumNearFar.y + offset));
    #endif
}