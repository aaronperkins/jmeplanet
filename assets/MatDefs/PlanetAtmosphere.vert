uniform mat4 g_WorldViewProjectionMatrix;
uniform mat4 g_WorldMatrix;
uniform mat4 g_ViewMatrixInverse;
uniform vec4 g_LightPosition;
uniform vec3 g_CameraPosition;
uniform vec2 g_FrustumNearFar;

uniform float m_AtmosphereRadius;
uniform float m_SurfaceRadius;
uniform float m_StretchAmount;

varying vec4 oPosition;
varying vec2 oUV;
varying float oAlpha;
varying vec3 oCamToPos;
varying vec3 oLightDir;

attribute vec3 inPosition;

void main() { 
    
    vec4 Po = vec4(inPosition,1);
    vec4 Pw = g_WorldMatrix * vec4(inPosition,1);
    vec3 position = Pw.xyz;
    vec4 camPos = vec4(g_CameraPosition.xyz,1);
    //vec4 camPos = vec4(g_ViewMatrixInverse[3].xyz,1);
    
    oPosition = g_WorldViewProjectionMatrix * Po;
  
    float radius = length(position);
    float radius2 = radius * radius;
    float camHeight = length(camPos.xyz);
    vec3 camToPos = position - camPos.xyz;
    float farDist = length(camToPos);
  
    vec3 lightDir = normalize(-g_LightPosition.xyz);
    vec3 normal = normalize(position);
  
    vec3 rayDir = camToPos / farDist;
    float camHeight2 = camHeight * camHeight;
  
    // Calculate the closest intersection of the ray with the outer atmosphere
    float B = 2.0 * dot(camPos.xyz, rayDir);
    float C = camHeight2 - radius2;
    float det = max(0.0, B*B - 4.0 * C);
    float nearDist = 0.5 * (-B - sqrt(det));
    vec3 nearPos = camPos.xyz + (rayDir * nearDist);
    vec3 nearNormal = normalize(nearPos);
 
    // get dot products we need
    float lc = dot(lightDir, camPos / camHeight);
    float ln = dot(lightDir, normal);
    float lnn = dot(lightDir, nearNormal);
 
    // get distance to surface horizon
    float altitude = camHeight - m_SurfaceRadius;
    float horizonDist = sqrt((altitude*altitude) + (2.0 * m_SurfaceRadius * altitude));
    float maxDot = horizonDist / camHeight;
  
    // get distance to atmosphere horizon - use max(0,...) because we can go into the atmosphere
    altitude = max(0,camHeight - m_AtmosphereRadius);
    horizonDist = sqrt((altitude*altitude) + (2.0 * m_AtmosphereRadius * altitude));
  
    // without this, the shift between inside and outside atmosphere is  jarring
    float tweakAmount = 0.1;
    float minDot = max(tweakAmount,horizonDist / camHeight);
  
    // scale minDot from 0 to -1 as we enter the atmosphere
    float minDot2 = ((camHeight - m_SurfaceRadius) * (1.0 / (m_AtmosphereRadius  - m_SurfaceRadius))) - (1.0 - tweakAmount);
    minDot = min(minDot, minDot2);
   
    // get dot product of the vertex we're looking out
    float posDot = dot(camToPos / farDist,-camPos.xyz / camHeight) - minDot;
  
    // calculate the height from surface in range 0..1
    float height = posDot * (1.0 / (maxDot - minDot));
  
    // push the horizon back based on artistic taste
    ln = max(0,ln + m_StretchAmount);
    lnn = max(0,lnn + m_StretchAmount);
 
    // the front color is the sum of the near and far normals
    float brightness = clamp(ln + (lnn * lc), 0.0, 1.0);
  
    // use "saturate(lc + 1.0 + StretchAmt)" to make more of the sunset side color be used when behind the planet
    oUV.x = brightness * clamp(lc + 1.0 + m_StretchAmount, 0.0, 1.0);
    oUV.y = height;
  
    // as the camera gets lower in the atmosphere artificially increase the height
    // so that the alpha value gets raised and multiply the increase amount
    // by the dot product of the light and the vertex normal so that
    // vertices closer to the sun are less transparent than vertices far from the sun.
    height -= min(0.0,minDot2 + (ln * minDot2));
    oAlpha = height * brightness;
  
    // normalised camera to position ray
    oCamToPos = -rayDir;
  
    oLightDir = normalize(g_LightPosition.xyz - position.xyz);

    // Vertex transformation 
    gl_Position = oPosition; 

    C = 1.0;
    gl_Position.z = (2*log(C*gl_Position.z + 1) / log(C*g_FrustumNearFar.y + 1) - 1) * gl_Position.w;

}
