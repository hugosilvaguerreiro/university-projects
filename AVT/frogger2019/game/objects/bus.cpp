#include "bus.h"
#include <iostream>
#include "game.h"

Bus::Bus(BasicMaterial mat, Position position) :  SceneObject(mat, position) {
    std::vector<SceneObject*> component;
    SceneCube *body1;
    SceneCube *body2;
     if(position.x > 0){
        this->velocity = -2;
        body1 = new SceneCube(busMat, { position.x + 0.85,  position.y + 0.5,  position.z + 0.5});
        body2 = new SceneCube(busMat, { position.x + 0.85,  position.y +0.5,  position.z + 0.75}); 
        body1->setScale(2, 1, 0.75);
        body2->setScale(1.75, 1, 0.75);       
        body1->setBoundingBox({3.75,1, { position.x - 1.87, position.y + 0.5 }, CAR});
        body1->setRotation(180.0f,0.f,0.f,1.f);
        body2->setRotation(180.0f,0.f,0.f,1.f);
    }
    else{
        this->velocity = 2;
        body1 = new SceneCube(busMat, { position.x -1,  position.y -0.5,  position.z + 0.5});
        body2 = new SceneCube(busMat, { position.x - 1,  position.y -0.5,  position.z + 0.75});
        body1->setScale(2, 1, 0.75);
        body1->setBoundingBox({3.75,1, { position.x - 1.87, position.y + 0.5 }, CAR});
        body2->setScale(1.75, 1, 0.75);
    }

    SceneTorus *wheel1 = new SceneTorus(wheelMat, { position.x -0.75, position.y + 0.5, position.z + 0.5}, 0.15, 0.35, 60.0, 10.0);
    SceneTorus *wheel2 = new SceneTorus(wheelMat, { position.x + 0.75, position.y + 0.5, position.z + 0.5}, 0.15, 0.35, 60.0, 10.0);
    SceneTorus *wheel3 = new SceneTorus(wheelMat, { position.x - 0.75,  position.y -0.5,  position.z + 0.5}, 0.15, 0.35, 60.0, 10.0);
    SceneTorus *wheel4 = new SceneTorus(wheelMat, { position.x + 0.75,  position.y - 0.5,  position.z +0.5}, 0.15, 0.35, 60.0, 10.0);
    
    component.push_back(wheel1);
    component.push_back(wheel2);
    component.push_back(wheel3);
    component.push_back(wheel4);
    component.push_back(body1);
    component.push_back(body2);
    this->components = component;
    this->initialPosition = position;
    this->wait = false;
}

void Bus::setVelocity(float velocity) {
    this->velocity = velocity;
    this->components.at(4)->setVelocity(velocity);
}

void Bus::move(float seconds) {
    float lastX = this->getPosition()->x;
    float x = this->getPosition()->x + this->velocity*seconds;
     if(x > 20 || x < -20){
        this->setPosition(this->initialPosition);
        updateComponentsPositions(this->initialPosition);
        return;   
    }
    if(!this->wait){
        this->setPosition({x , this->getPosition()->y, this->getPosition()->z});
        float diff = x - lastX;
        for(int i=0;i< this->components.size(); i++){
            Position finalPosition = {this->components[i]->getPosition()->x + diff , this->components[i]->getPosition()->y, this->components[i]->getPosition()->z};
            this->components[i]->setPosition(finalPosition);
        }
        this->components.at(4)->getBoundingBox()->position.x = this->getPosition()->x - 1.87;
        this->components.at(4)->getBoundingBox()->position.y = this->getPosition()->y + 0.5;
    }
}

void Bus::updateComponentsPositions(Position position){
    this->components[0]->setPosition({ position.x -0.75, position.y + 0.5, position.z + 0.5});
    this->components[1]->setPosition({ position.x + 0.75, position.y + 0.5, position.z + 0.5});
    this->components[2]->setPosition({ position.x - 0.75,  position.y -0.5,  position.z + 0.5});
    this->components[3]->setPosition({ position.x + 0.75,  position.y - 0.5,  position.z +0.5});
    if(this->velocity < 0){
        this->components[4]->setPosition({ position.x + 0.85,  position.y + 0.5,  position.z + 0.5});
        this->components[5]->setPosition( { position.x + 0.85,  position.y +0.5,  position.z + 0.75}); 
    }
    else{
        this->components[4]->setPosition({ position.x -1,  position.y -0.5,  position.z + 0.5});
        this->components[5]->setPosition({ position.x - 1,  position.y -0.5,  position.z + 0.75});
        }
}

std::vector<SceneObject*> Bus::getComponents(){
    return this->components;
}

Bus::~Bus(){
    for(int i=0; i<this->components.size();i++){
        delete(this->components[i]);
    }
}