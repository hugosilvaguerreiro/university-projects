#include "trunk.h"
#include <iostream>
#include "game.h"


Trunk::Trunk(BasicMaterial mat, Position position) :  SceneCompositeObject(position) {
    SceneCylinder *body = new SceneCylinder(mat, position, 3, 1, 10);
    body->setTexture(Texture(DEFAULT, WOOD));
    body->pushRotation({90.f, 0.f, 0.f, 1.f});
    body->pushRotation({10.f, 1.f,0.f, 0.f});
    body->velocity = 2.f;
    body->setBoundingBox({3, 2, {position.x-1.5f, position.y+1.f}, TRUNK});

    this-> addObject(body);
     
    this->velocity = 2.f;
         

}

void Trunk::setVelocity(float velocity) {
    this->velocity = velocity;
    this->objects.at(0)->setVelocity(velocity);
}

void Trunk::move(float seconds) {
    float lastX = this->getPosition()->x;
    float x = this->getPosition()->x + this->velocity*seconds;
    Position finalPosition = {x , this->getPosition()->y, this->getPosition()->z};
    if(x > 20){
        this->setPosition( {-20 , this->getPosition()->y, this->getPosition()->z});
        return;   
    }
    if(!this->wait){
        this->setPosition(finalPosition);
        float diff = x - lastX;
        
        this->objects.at(0)->setPosition({this->getPosition()->x + diff, this->getPosition()->y , this->getPosition()->z});
        this->objects.at(0)->getBoundingBox()->position.x = this->getPosition()->x-1.5f;
        this->objects.at(0)->getBoundingBox()->position.y = this->getPosition()->y+1.f;
        std::vector<Rotation> *rotations = this->objects.at(0)->getRotations();
        Rotation lastRotation = rotations->at(1);
        rotations->at(1) = {lastRotation.angle + 10.f, 0.f, 1.f, 0.f };
          
          
          
          
          
    }
}

SceneObject* Trunk::getComponent(){
    return this->objects.at(0);
}

void Trunk::updateComponentsPositions(Position position){
    this->objects.at(0)->setPosition(position);
}