#include "scene.h"
#include "frog.h"
#include "engine.h"

Scene::Scene() {
    this->objects = new std::vector<SceneObject*>();
    this->lights = new std::vector<Light*>();
    this->reflectedPlanes = new std::vector<SceneObject*>();
    this->shadowPlanes = new std::vector<SceneObject*>();
}

void Scene::putObject(SceneObject *object) {
    if(Car *car = dynamic_cast<Car*>(object)){
        putObject(car);
    }
    else if(Bus *bus = dynamic_cast<Bus*>(object)){
        putObject(bus);
    }
    this->objects->push_back(object);
}

void Scene::putObject(SceneCompositeObject *object) {
    this->objects->push_back(object);
}

void Scene::putReflected(SceneObject *object) {
    this->reflectedPlanes->push_back(object);
}
void Scene::putShadow(SceneObject *object) {
    this->shadowPlanes->push_back(object);
}


void Scene::putObject(Car *car) {
    std::vector<SceneObject*> components = car->getComponents();
    for(int i=0; i < components.size(); i++){
        this->objects->push_back(components[i]);
    }
}

void Scene::putObject(Turtle *turtle) {
    std::vector<SceneObject*> components = turtle->getComponents();
    for(int i=0; i < components.size(); i++){
        this->objects->push_back(components[i]);
    }
}

void Scene::putObject(Bus *bus) {
    std::vector<SceneObject*> components = bus->getComponents();
    for(int i=0; i < components.size(); i++){
        this->objects->push_back(components[i]);
    }
}

void Scene::putLight(Light *light) {
    this->lights->push_back(light);
}

void Scene::putObject(Trunk *trunk) {
    SceneObject* component = trunk->getComponent();
    this->objects->push_back(component);
}

std::vector<SceneObject*> *Scene::getObjects() {
    return this->objects;
}

std::vector<Light*> *Scene::getLights() {
    return this->lights;
}

std::vector<SceneObject*> *Scene::getReflected() {
    return this->reflectedPlanes;
}

std::vector<SceneObject*> *Scene::getShadow() {
    return this->shadowPlanes;
}


void Scene::toggleDirectionalLights(){
    for(std::vector<Light*>::iterator it = this->lights->begin(); it != this->lights->end(); ++it) {
        Light * light = *it;
        if (DirectionalLight* t = dynamic_cast<DirectionalLight*>(light)) {
            t->isEnabled = !t->isEnabled;
        }
    }
}

void Scene::togglePointLights(){
    POINT_LIGHTS_ON = !POINT_LIGHTS_ON;
    Renderer::toggleLensFlare();
    for(std::vector<Light*>::iterator it = this->lights->begin(); it != this->lights->end(); ++it) {
        Light * light = *it;
        if (PointLight* t = dynamic_cast<PointLight*>(light)) {
            t->isEnabled = !t->isEnabled;
        }
    }

}

void Scene::toggleSpotLights() {

    for(std::vector<Light*>::iterator it = this->lights->begin(); it != this->lights->end(); ++it) {
        Light * light = *it;
        if (SpotLight* t = dynamic_cast<SpotLight*>(light)) {
            t->isEnabled = !t->isEnabled;
        }
    }

}

HUD::HUD(Player* player) : Scene(){
    this->player = player;
}

