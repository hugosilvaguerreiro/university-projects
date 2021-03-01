#include "car.h"
#include "game.h"
#include <iostream>

Car::Car(BasicMaterial mat, Position position) :  SceneObject(mat, position) {
    std::vector<SceneObject*> component;
    SceneTorus *wheel1 = new SceneTorus(wheelMat, {position.x -0.75f, position.y + 0.5f, position.z + 0.5f}, 0.15f, 0.35f, 60.0f, 5.f);
    SceneTorus *wheel2 = new SceneTorus(wheelMat, {position.x + 0.75f, position.y + 0.5f, position.z + 0.5f}, 0.15f, 0.35f, 60.0f, 5.f);
    SceneTorus *wheel3 = new SceneTorus(wheelMat, { position.x - 0.75f,  position.y -0.5f,  position.z + 0.5f}, 0.15f, 0.35f, 60.0f, 5.f);
    SceneTorus *wheel4 = new SceneTorus(wheelMat, { position.x + 0.75f,  position.y - 0.5f,  position.z +0.5f}, 0.15f, 0.35f, 60.0f, 5.f);
    SceneCube *body1 = new SceneCube(carMat, { position.x -1.f,  position.y -0.5f,  position.z + 0.5f});
    SceneCube *body2 = new SceneCube(carMat, { position.x -0.5f, position.y -0.5f,  position.z + 0.75f});
    body1->setScale(2, 1, 0.5f);
    body2->setScale(1, 1, 0.5f);
    body1->setBoundingBox({3,1, { position.x - 1.5f, position.y + 0.5f }, CAR});
    component.push_back(wheel1);
    component.push_back(wheel2);
    component.push_back(wheel3);
    component.push_back(wheel4);
    component.push_back(body1);
    component.push_back(body2);
    this->components = component;
    if(position.x > 0){
        this->velocity = -2;
    }
    
    else{
        this->velocity = 2;
    }
    this->initialPosition = position;
}

void Car::setVelocity(float velocity) {
    this->velocity = velocity;
    this->components.at(4)->setVelocity(velocity);

}

void Car::move(float seconds) {
    float lastX = this->getPosition()->x;
    float x = this->getPosition()->x + this->velocity*seconds;
    Position finalPosition = {x , this->getPosition()->y, this->getPosition()->z};
    if(x > 20 || x <-20){
        this->setPosition(this->initialPosition);
        updateComponentsPositions(this->initialPosition);
        return;   
    }
    this->setPosition(finalPosition);
    float diff = x - lastX;
    for(int i=0;i< this->components.size(); i++){
        Position finalPosition = {this->components[i]->getPosition()->x + diff , this->components[i]->getPosition()->y, this->components[i]->getPosition()->z};
        this->components[i]->setPosition(finalPosition);
    }
    this->components.at(4)->getBoundingBox()->position.x = this->getPosition()->x - 1.5f;
    this->components.at(4)->getBoundingBox()->position.y = this->getPosition()->y + 0.5f;

}

void Car::updateComponentsPositions(Position position){
    this->components[0]->setPosition({ position.x -0.75f, position.y + 0.5f, position.z + 0.5f});
    this->components[1]->setPosition({ position.x + 0.75f, position.y + 0.5f, position.z + 0.5f});
    this->components[2]->setPosition({ position.x - 0.75f,  position.y -0.5f,  position.z + 0.5f});
    this->components[3]->setPosition({ position.x + 0.75f,  position.y - 0.5f,  position.z +0.5f});
    this->components[4]->setPosition({ position.x -1.f,  position.y -0.5f,  position.z + 0.5f});
    this->components[5]->setPosition({ position.x -0.5f, position.y -0.5f,  position.z + 0.75f});
}

std::vector<SceneObject*> Car::getComponents(){
    return this->components;
}

Car::~Car(){
    for(int i=0; i<this->components.size();i++){
        delete(this->components[i]);
    }
}