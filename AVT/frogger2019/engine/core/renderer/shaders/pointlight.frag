#version 330

uniform sampler2D texmap;
uniform sampler2D texmap1;
uniform sampler2D texmap2;
uniform samplerCube cubeMap;

uniform int texMode;
uniform bool shadowMode;
uniform bool fogActivated;

struct LightProperties {
	bool isEnabled;
	bool isLocal;
	bool isSpot;
	vec4 ambient;
	vec4 color;
	vec4 position;
	vec3 halfVector;
	vec4 coneDirection;
	float spotCosCutoff;
	float spotExponent;
	float constantAttenuation;
	float linearAttenuation;
	float quadraticAttenuation;
};

struct Materials {
	vec4 diffuse;
	vec4 ambient;
	vec4 specular;
	vec4 emissive;
	float shininess;
	int texCount;
};

// the set of lights to apply, per invocation of this shader
const int MaxLights = 9;
uniform LightProperties Lights[MaxLights];
uniform Materials mat;
uniform float Shininess;
uniform float Strength;

in Data {
	vec3 Normal;
	vec3 Eye;
	vec4 Position;
	vec2 tex_coord;
    //vec3 skyboxTexCoord;
} DataIn;

out vec4 FragColor;


void main() {
    float fogFactor = 0;
    float dist = abs(DataIn.Position.z);
    const vec3 fogColor = vec3(0.5, 0.5, 0.5);
    int fogSelector = 2;
    const float FogDensity = 0.15;
    
    if(shadowMode) {
        FragColor = vec4(0.3, 0.3, 0.3, 1.0);  //constant color
        //vec3 rgb = vec3(FragColor);
        if(fogActivated) {
            vec3 rgb2 = vec3(FragColor);
            if(fogSelector == 0)//linear fog
            {
                // 20 - fog starts; 80 - fog ends
                fogFactor = (30 - dist)/(30 - (-30));
                fogFactor = clamp( fogFactor, 0.0, 1.0 );

                //if you inverse color in glsl mix function you have to
                //put 1.0 - fogFactor
                rgb2 = mix(fogColor, rgb2, fogFactor);
            }
            else if( fogSelector == 1)// exponential fog
            {
                fogFactor = 1.0 /exp(dist * FogDensity);
                fogFactor = clamp( fogFactor, 0.0, 1.0 );

                // mix function fogColor⋅(1−fogFactor) + rgb⋅fogFactor
                rgb2 = mix(fogColor, rgb2, fogFactor);
            }
            else if( fogSelector == 2)
            {
                fogFactor = 3.0 /exp( (dist * FogDensity)* (dist * FogDensity));
                fogFactor = clamp( fogFactor, 0.0, 1.0 );
                rgb2 = mix(fogColor, rgb2, fogFactor);
            }
            FragColor = vec4(rgb2, FragColor.a);
        }
    }
    else {
        vec3 scatteredLight = vec3(0.0); // or, to a global ambient light
        vec3 reflectedLight = vec3(0.0);
        vec4 texel, texel1;
        vec3 normal;
        
        // loop over all the lights
        float spotCos;
        for (int light = 0; light < MaxLights; ++light) {
            if (! Lights[light].isEnabled)
                continue;
            vec3 halfVector;
            vec3 lightDirection = vec3(Lights[light].position);
            float attenuation = 1.0;

            if (Lights[light].isLocal) {
                lightDirection = lightDirection - vec3(DataIn.Position);
                float lightDistance = length(lightDirection);
                //lightDirection = lightDirection / lightDistance;
                attenuation = 1.0 / (Lights[light].constantAttenuation + Lights[light].linearAttenuation * lightDistance + Lights[light].quadraticAttenuation * lightDistance * lightDistance);
                if (Lights[light].isSpot) {
                    float spotCos = dot(lightDirection / lightDistance, normalize(vec3(-Lights[light].coneDirection)));
                    if (spotCos < Lights[light].spotCosCutoff){
                        attenuation = 0.0;
                    }
                    else{
                        attenuation *= pow(spotCos,Lights[light].spotExponent);
                    }
                }
                halfVector = normalize(lightDirection + DataIn.Eye);
            } 
            else {
                lightDirection = normalize(lightDirection);
                halfVector = normalize((lightDirection + DataIn.Eye) /2);
            }

            normal = DataIn.Normal;

            if(texMode == 5) { 
                normal = texture(texmap1, DataIn.tex_coord).rgb;
                normal = normalize(normal * 2.0 - 1.0); 
            }

            float diffuse = max(0.0, dot(normal, lightDirection));
            float specular = max(0.0, dot(normal, halfVector));
            if (diffuse == 0.0)
                specular = 0.0;
            else
                specular = pow(specular, mat.shininess);

            // Accumulate all the lights effects
            scatteredLight += Lights[light].ambient.rgb * mat.ambient.rgb * attenuation + Lights[light].color.rgb * diffuse * mat.diffuse.rgb * attenuation;
            reflectedLight += Lights[light].color.rgb * mat.specular.rgb * specular * attenuation;
        }

        vec3 rgb = min(mat.emissive.rgb + scatteredLight + reflectedLight, vec3(1.0));
        //FragColor = vec4(rgb,mat.diffuse.a);

        if (texMode == 0) { //No texture
            FragColor = vec4(rgb,mat.diffuse.a);
        } else if (texMode == 1 || texMode == 5) { //default or bump
            texel = texture(texmap, DataIn.tex_coord);
            FragColor = vec4(rgb,mat.diffuse.a) * texel;
        } else if( texMode == 2) { //multitexturing
            texel = texture(texmap, DataIn.tex_coord);
            texel1 = texture(texmap1, DataIn.tex_coord);
            FragColor = vec4(rgb,mat.diffuse.a) * texel * texel1;
        } else if(texMode == 3) { //no light
            FragColor = texture(texmap, DataIn.tex_coord);
        }else if(texMode == 4) { //particles
            texel = texture(texmap, DataIn.tex_coord);
		    if((texel.a == 0.0)  || (mat.diffuse.a == 0.0) ) 
                discard;
		    else
			    FragColor = mat.diffuse * texel;
		} else {
            FragColor = vec4(rgb,mat.diffuse.a);
        }
    
        if(fogActivated) { 
            //vec3 rgb = vec3(FragColor);
            vec3 rgb2 = vec3(FragColor);
            if(fogSelector == 0)//linear fog
            {
                // 20 - fog starts; 80 - fog ends
                fogFactor = (30 - dist)/(30 - (-30));
                fogFactor = clamp( fogFactor, 0.0, 1.0 );

                //if you inverse color in glsl mix function you have to
                //put 1.0 - fogFactor
                rgb2 = mix(fogColor, rgb2, fogFactor);
            }
            else if( fogSelector == 1)// exponential fog
            {
                fogFactor = 1.0 /exp(dist * FogDensity);
                fogFactor = clamp( fogFactor, 0.0, 1.0 );

                // mix function fogColor⋅(1−fogFactor) + rgb⋅fogFactor
                rgb2 = mix(fogColor, rgb2, fogFactor);
            }
            else if( fogSelector == 2)
            {
                fogFactor = 3.0 /exp( (dist * FogDensity)* (dist * FogDensity));
                fogFactor = clamp( fogFactor, 0.0, 1.0 );
                rgb2 = mix(fogColor, rgb2, fogFactor);
            }
            FragColor = vec4(rgb2, FragColor.a);
        }  
	}
}
