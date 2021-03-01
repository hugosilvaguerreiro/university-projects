#ifndef __LIGHTH__
#define __LIGHTH__

typedef struct LightVec3 {
	float x, y, z;
} LightVec3;

typedef struct LightVec4 {
	float x, y, z, a;
} LightVec4;

class Light {
public:
	bool isEnabled;
	bool isLocal;
	bool isSpot;
	float ambient[4];
	float color[4];
	float position[4];

    Light(bool isEnabled, bool isLocal, bool isSpot, LightVec4 ambient, LightVec4 color, LightVec4 position);
	virtual void render(int index);
};

class DirectionalLight : public Light {
public:
    DirectionalLight(bool isEnabled,LightVec4 ambient, LightVec4 color, LightVec4 position);
	virtual void render(int index) override;
};

class PointLight : public Light {
public:
	float cAttenuation;
	float lAttenuation;
	float qAttenuation;

    PointLight(bool isEnabled, LightVec4 ambient, LightVec4 color, LightVec4 position, LightVec3 attenuation);
	virtual void render(int index) override;
};


class SpotLight : public Light {
public:
	float cAttenuation;
	float lAttenuation;
	float qAttenuation;
	float coneDir[4];
	float cosCutoff;
	float spotExponent;

    SpotLight(bool isEnabled, LightVec4 ambient, LightVec4 color, LightVec4 position, LightVec3 attenuation, LightVec4 coneDir, float cosCutoff, float spotExponent);
	virtual void render(int index) override;
};



#endif