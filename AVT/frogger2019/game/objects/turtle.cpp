#include "turtle.h"
#include "game.h"
#include <iostream>

Turtle::Turtle(BasicMaterial mat, Position position) :  SceneObject(mat, position) {
    std::vector<SceneObject*> component;
    SceneCube *leg1 = new SceneCube(turtleBodyMat, { position.x - 0.75f, position.y + 0.4f, position.z});
    SceneCube *leg2 = new SceneCube(turtleBodyMat, { position.x + 0.25f, position.y + 0.4f, position.z});
    SceneCube *leg3 = new SceneCube(turtleBodyMat, { position.x - 0.75f, position.y - 0.6f, position.z});
    SceneCube *leg4 = new SceneCube(turtleBodyMat, { position.x + 0.25f, position.y - 0.6f, position.z});
    SceneSphere *head = new SceneSphere(turtleBodyMat, { position.x - 0.75f, position.y, position.z + 0.20f}, 0.20, 5);
    SceneSphere *shell = new SceneSphere(tortoiseShelMat, { position.x, position.y, - 0.3f}, 0.75, 4);
    
    shell->setBoundingBox({1.5, 1.8, {position.x - 0.75f, position.y + 0.75f}, TURTLE});
    shell->velocity = -2;
    this->velocity = -2;

    leg1->setScale(0.5, 0.15, 0.10);
    leg2->setScale(0.5, 0.15, 0.10);
    leg3->setScale(0.5, 0.15, 0.10);
    leg4->setScale(0.5, 0.15, 0.10);

    shell->setTexture(Texture(NO_TEXTURE, TURTLE_SHELL));

    component.push_back(leg1);
    component.push_back(leg2);
    component.push_back(leg3);
    component.push_back(leg4);
    component.push_back(head);
    component.push_back(shell);

    this->components = component;
   // this->initialPosition = position;
    
}

void Turtle::setVelocity(float velocity) {
    this->velocity = velocity;
    this->components.at(5)->setVelocity(velocity);
}

void Turtle::move(float seconds) {
    float lastX = this->getPosition()->x;
    float x = this->getPosition()->x + velocity*seconds;
    Position finalPosition = {x , this->getPosition()->y, this->getPosition()->z};
    if(x <-20){
        this->setPosition( {20 , this->getPosition()->y, this->getPosition()->z});
        updateComponentsPositions(position);
        return;   
    }
    if(!this->wait){
        this->setPosition(finalPosition);
        float diff = x - lastX;
        for(int i=0;i< this->components.size(); i++){
            Position finalPosition = {this->components[i]->getPosition()->x + diff , this->components[i]->getPosition()->y, this->components[i]->getPosition()->z};
            this->components[i]->setPosition(finalPosition);
        }
        this->components.at(5)->getBoundingBox()->position.x = this->getPosition()->x-0.75;
        this->components.at(5)->getBoundingBox()->position.y = this->getPosition()->y+0.75;
    }
}

void Turtle::updateComponentsPositions(Position position){
    this->components[0]->setPosition({ position.x - 0.75f, position.y + 0.4f, position.z});
    this->components[1]->setPosition({ position.x + 0.25f, position.y + 0.4f, position.z});
    this->components[2]->setPosition({ position.x - 0.75f, position.y - 0.6f, position.z});
    this->components[3]->setPosition({ position.x + 0.25f, position.y - 0.6f, position.z});
    this->components[4]->setPosition({ position.x - 0.75f, position.y, position.z + 0.20f});
    this->components[5]->setPosition({ position.x, position.y, -0.3f});
}

std::vector<SceneObject*> Turtle::getComponents(){
    return this->components;
}

Turtle::~Turtle(){
    for(int i=0; i<this->components.size();i++){
        delete(this->components[i]);
    }
}