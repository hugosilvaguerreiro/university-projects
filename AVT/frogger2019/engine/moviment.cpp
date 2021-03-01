#include <iostream>
#include <vector>

#include "moviment.h"
#include "scene_object.h"
#include "trunk.h"
#include "turtle.h"
#include "engine.h"

#include <GL/glew.h>
#include <GL/freeglut.h>
#include "player.h"

static bool altered = false;
float DIFFICULTY_LEVEL = 0.1;
int DIFFICULTY_TICKS = 100;
int CURRENT_TICKS = 10;

std::chrono::high_resolution_clock::time_point Moviment::lastMoment;
std::vector<SceneObject*> Moviment::objects = std::vector<SceneObject*>();
Player *OBJ_PLAYER;
bool ON_TRUNK=false;
bool ON_TURTLE=false;
SceneObject* current_trunk;
SceneObject* current_turtle;

bool MOVIMENT_PAUSED = false;

void Moviment::putPlayer(Player* player) {
    OBJ_PLAYER = player;
}

void Moviment::setOnTrunk(bool val, SceneObject *trunk) {
    ON_TRUNK = val;
    current_trunk = trunk;
}

void Moviment::pause() {
    MOVIMENT_PAUSED = true;
    glutTimerFunc(0, Moviment::moveObjects, 0);  
}

void Moviment::resume() {
    MOVIMENT_PAUSED = false;
    glutTimerFunc(100, Moviment::moveObjects, 0);
    lastMoment = std::chrono::high_resolution_clock::now();
}

void Moviment::restart() {
    CURRENT_TICKS = 0;
    for( int i = 0; i < objects.size(); i++){
            if(objects[i]->velocity < 0)
                objects[i]->reset(-2);
            else
                objects[i]->reset(2);
    }
    lastMoment = std::chrono::high_resolution_clock::now();
}

void Moviment::setOnTrunk(bool val) {
    ON_TRUNK = val;
}

bool Moviment::isOnTrunk() {
    return ON_TRUNK;
}

void Moviment::setOnTurtle(bool val, SceneObject *turtle) {
    ON_TURTLE = val;
    current_turtle = turtle;
}


void Moviment::setOnTurtle(bool val) {
    ON_TURTLE = val;
}

bool Moviment::isOnTurtle() {
    return ON_TURTLE;
}


void Moviment::putObject(SceneObject *object) {
    objects.push_back(object);
}

std::vector<SceneObject*> Moviment::getObjects() {
    return objects;
}

void Moviment::checkMovementColision(){
    int i = 0;
    BoundingBox *bound1;
    BoundingBox *bound2;
    int limit = 7;
    for(; i < Moviment::objects.size()-1; i++){
        for(int j=i+1; j < Moviment::objects.size(); j++){
            bound1 = Moviment::objects[i]->getBoundingBox();
            bound2 = Moviment::objects[j]->getBoundingBox();
            Trunk* trunk1 = dynamic_cast<Trunk*>(Moviment::objects[i]);
            Trunk* trunk2 = dynamic_cast<Trunk*>(Moviment::objects[j]);
            if(trunk1 && trunk2){ 
                
                limit = 10;
            }
            if(!trunk1 && trunk2 || trunk1 && !trunk2){
                continue;
            }
            if( bound2->position.y == bound1->position.y){
                if( bound2->position.x - bound1->position.x < limit){
                    std::cout << "INDEX" << i << std::endl;
                     
                    return;
                }
            }
        } 
    }
    Moviment::objects[i]->canContinue();
}

void Moviment::moveObjects(int value) {

    if(MOVIMENT_PAUSED)
        return;

    if (CURRENT_TICKS > DIFFICULTY_TICKS) {  
        for( int i = 0; i< objects.size(); i++){
            if(objects[i]->velocity < 0)
                objects[i]->setVelocity(objects[i]->velocity - 1);
            else
                objects[i]->setVelocity(objects[i]->velocity + 1);
        }
        CURRENT_TICKS = 0;
    }
    CURRENT_TICKS += 1;

    if(!altered) {
        lastMoment = std::chrono::high_resolution_clock::now();
        altered = true;
    }
    std::chrono::high_resolution_clock::time_point currentMoment = std::chrono::high_resolution_clock::now();
    float elapsedSecs = std::chrono::duration_cast<std::chrono::duration<float>>(currentMoment - lastMoment).count();
    if(objects.size()>0) {
        for( int i = 0; i< objects.size(); i++){
             
                objects[i]->move(elapsedSecs);
             
        }
        checkMovementColision();
    }
    
    lastMoment = currentMoment;

    if(ON_TRUNK) {
        if(current_trunk != nullptr) {
            Position *currentPos = OBJ_PLAYER->getObject()->getPosition();
            Position initialPos = OBJ_PLAYER->getInitialPosition();
            OBJ_PLAYER->moveObjectToPosition({currentPos->x + current_trunk->velocity * elapsedSecs , currentPos->y, initialPos.z + 0.75f});
            if(currentPos->x + current_trunk->velocity * elapsedSecs > 20){
                ON_TRUNK = false;
                current_trunk = nullptr;
                int lifes = OBJ_PLAYER->getLifes();
                OBJ_PLAYER->setLifes(lifes - 1);
                OBJ_PLAYER->getObject()->setPosition(initialPos);
            }
        }
    }
    if(ON_TURTLE) {
        if(current_turtle != nullptr) {
            Position *currentPos = OBJ_PLAYER->getObject()->getPosition();
            Position initialPos = OBJ_PLAYER->getInitialPosition();
             
            OBJ_PLAYER->moveObjectToPosition({currentPos->x + current_turtle->velocity * elapsedSecs , currentPos->y, initialPos.z + 0.4f});
        }
    }    

    glutTimerFunc(100, Moviment::moveObjects, 0);  
}

void Moviment::reset(){
    objects = {};
}
