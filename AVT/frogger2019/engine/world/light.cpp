#include <cstring>
#include <string>
#include <GL/glew.h>
#include "light.h"
#include "engine.h"


Light::Light(bool isEnabled, bool isLocal, bool isSpot, LightVec4 ambient, LightVec4 color, LightVec4 position){
    this->isEnabled = isEnabled;
    this->isLocal = isLocal;
    this->isSpot = isSpot;
    this->ambient[0] = ambient.x;
    this->ambient[1] = ambient.y;
    this->ambient[2] = ambient.z;
    this->ambient[3] = ambient.a;
    this->color[0] = color.x;
    this->color[1] = color.y;
    this->color[2] = color.z;
    this->color[3] = color.a;
    this->position[0] = position.x;
    this->position[1] = position.y;
    this->position[2] = position.z;
    this->position[3] = position.a;
}

void Light::render(int i) {
    GLint loc;
    std::string target;
    target = "Lights[].isEnabled";
    loc = glGetUniformLocation(shader.getProgramIndex(), target.insert(7, std::to_string(i)).c_str());
    glUniform1i(loc, this->isEnabled);
    target = "Lights[].isLocal";
    loc = glGetUniformLocation(shader.getProgramIndex(), target.insert(7, std::to_string(i)).c_str());
    glUniform1i(loc, this->isLocal);
    target = "Lights[].isSpot";
    loc = glGetUniformLocation(shader.getProgramIndex(), target.insert(7, std::to_string(i)).c_str());
    glUniform1i(loc, this->isSpot);
    target = "Lights[].ambient";
    loc = glGetUniformLocation(shader.getProgramIndex(), target.insert(7, std::to_string(i)).c_str());
    glUniform4fv(loc, 1, this->ambient);
    target = "Lights[].color";
    loc = glGetUniformLocation(shader.getProgramIndex(), target.insert(7, std::to_string(i)).c_str());
    glUniform4fv(loc, 1, this->color);
    target = "Lights[].position";
    float res[4];
    multMatrixPoint(VIEW, this->position, res);
    loc = glGetUniformLocation(shader.getProgramIndex(), target.insert(7, std::to_string(i)).c_str());
    glUniform4fv(loc, 1, res);
}

DirectionalLight::DirectionalLight(bool isEnabled, LightVec4 ambient, LightVec4 color, LightVec4 position)
    : Light(isEnabled, false, false, ambient,  color, position) {

}

void DirectionalLight::render(int i) {
    Light::render(i);
}

PointLight::PointLight(bool isEnabled, LightVec4 ambient, LightVec4 color, LightVec4 position, LightVec3 attenuation)
    : Light(isEnabled, true, false, ambient, color, position) {
        this->cAttenuation = attenuation.x;
        this->lAttenuation = attenuation.y;
        this->qAttenuation = attenuation.z;
}

void PointLight::render(int i) {
    GLint loc;
    std::string target;
    Light::render(i);
    target = "Lights[].isLocal";
    loc = glGetUniformLocation(shader.getProgramIndex(), target.insert(7, std::to_string(i)).c_str());
    glUniform1i(loc, this->isLocal);
    target = "Lights[].isSpot";
    loc = glGetUniformLocation(shader.getProgramIndex(), target.insert(7, std::to_string(i)).c_str());
    glUniform1i(loc, this->isSpot);
    target = "Lights[].constantAttenuation";
    loc = glGetUniformLocation(shader.getProgramIndex(), target.insert(7, std::to_string(i)).c_str());
    glUniform1f(loc, this->cAttenuation);
    target = "Lights[].linearAttenuation";
    loc = glGetUniformLocation(shader.getProgramIndex(), target.insert(7, std::to_string(i)).c_str());
    glUniform1f(loc, this->lAttenuation);
    target = "Lights[].quadraticAttenuation";
    loc = glGetUniformLocation(shader.getProgramIndex(), target.insert(7, std::to_string(i)).c_str());
    glUniform1f(loc, this->qAttenuation);
}

SpotLight::SpotLight(bool isEnabled, LightVec4 ambient, LightVec4 color, LightVec4 position, LightVec3 attenuation, LightVec4 coneDir, float cosCutoff, float spotExponent)
    : Light(isEnabled, true, true, ambient, color, position) {
        this->cAttenuation = attenuation.x;
        this->lAttenuation = attenuation.y;
        this->qAttenuation = attenuation.z;

        this->coneDir[0] = coneDir.x;
        this->coneDir[1] = coneDir.y;
        this->coneDir[2] = coneDir.z;
        this->coneDir[3] = coneDir.a;

        this->cosCutoff = cosCutoff;
        this->spotExponent = spotExponent;
}


void SpotLight::render(int i) {
    GLint loc;
    std::string target;
    Light::render(i);
    target = "Lights[].isLocal";
    loc = glGetUniformLocation(shader.getProgramIndex(), target.insert(7, std::to_string(i)).c_str());
    glUniform1i(loc, this->isLocal);
    target = "Lights[].isSpot";
    loc = glGetUniformLocation(shader.getProgramIndex(), target.insert(7, std::to_string(i)).c_str());
    glUniform1i(loc, this->isSpot);
    target = "Lights[].constantAttenuation";
    loc = glGetUniformLocation(shader.getProgramIndex(), target.insert(7, std::to_string(i)).c_str());
    glUniform1f(loc, this->cAttenuation);
    target = "Lights[].linearAttenuation";
    loc = glGetUniformLocation(shader.getProgramIndex(), target.insert(7, std::to_string(i)).c_str());
    glUniform1f(loc, this->lAttenuation);
    target = "Lights[].quadraticAttenuation";
    loc = glGetUniformLocation(shader.getProgramIndex(), target.insert(7, std::to_string(i)).c_str());
    glUniform1f(loc, this->qAttenuation);
    target = "Lights[].coneDirection";
    float res[4];
    multMatrixPoint(VIEW, this->coneDir, res);
    loc = glGetUniformLocation(shader.getProgramIndex(), target.insert(7, std::to_string(i)).c_str());
    glUniform4fv(loc, 1, res);
    target = "Lights[].spotCosCutoff";
    loc = glGetUniformLocation(shader.getProgramIndex(), target.insert(7, std::to_string(i)).c_str());
    glUniform1f(loc, this->cosCutoff);
    target = "Lights[].spotExponent";
    loc = glGetUniformLocation(shader.getProgramIndex(), target.insert(7, std::to_string(i)).c_str());
    glUniform1f(loc, this->spotExponent);
}