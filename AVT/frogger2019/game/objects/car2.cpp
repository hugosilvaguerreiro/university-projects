#include "car.h"
#include "game.h"
#include "racing_car.h"
#include "racing_carMeshes.h"
#include <iostream>

Car2::Car2(BasicMaterial mat, Position position) :  SceneCompositeObject(position) {
     
    int v_index = 0;
    int i_index = 0;
    for (int i = 0; i < RACING_CAR; i++) {
        float amb[] = {};
        float diffuse[] = {};
         
         
        BasicMaterial material;
        if (i >= 3 && i < 5) {
            material = mat;
            
        } else {
            material = {{meshVector_RACING_CAR[i].ka[0],meshVector_RACING_CAR[i].ka[1],meshVector_RACING_CAR[i].ka[2],meshVector_RACING_CAR[i].ka[3]}, 
                        {meshVector_RACING_CAR[i].kd[0],meshVector_RACING_CAR[i].kd[1],meshVector_RACING_CAR[i].kd[2],meshVector_RACING_CAR[i].kd[3]}, 
                        {meshVector_RACING_CAR[i].ks[0],meshVector_RACING_CAR[i].ks[1],meshVector_RACING_CAR[i].ks[2],meshVector_RACING_CAR[i].ks[3]},
                            { 0.0f, 0.0f, 0.0f, 1.0f }, 1, 0};
        }
        
         
                        
        SceneObject *obj = new SceneObject(material, {position.x+1.f, position.y+10.6f, position.z+0.2f});
        obj->setTexture(Texture(NO_TEXTURE, FROG_TEX));
        obj->pushRotation({90.f, 1.f, 0.f, 0.f});
        obj->pushRotation({90.f, 0.f, 1.f, 0.f});
         
         
        createTeaPot(obj->getMesh(), v_index, i_index, custom_vertices_raci, custom_uvs_raci, custom_normals_raci, custom_indices_raci, &meshVector_RACING_CAR[i]);
         
        this->addObject(obj);
        v_index += meshVector_RACING_CAR[i].n_vertices * 4;
		i_index += meshVector_RACING_CAR[i].n_indices;
    } 
    if(position.x > 0){
        this->velocity = -5;
    }
    else{
        this->velocity = 5;
    }
    this->initialPosition = position;
    this->setBoundingBox({3, 1, { position.x - 1.5f, position.y + 0.5f }, CAR});
}

void Car2::setVelocity(float velocity) {
    this->velocity = velocity;

}

void Car2::move(float seconds) {
    float lastX = this->getPosition()->x;
    float x = this->getPosition()->x + this->velocity*seconds;
    Position finalPosition = {x , this->getPosition()->y, this->getPosition()->z};
    if(x > 20 || x <-20){
        this->setPosition(this->initialPosition);
         
        return;   
    }
    this->setPosition(finalPosition);
    float diff = x - lastX;
    for(int i=0;i< this->components.size(); i++){
        Position finalPosition = {this->components[i]->getPosition()->x + diff , this->components[i]->getPosition()->y, this->components[i]->getPosition()->z};
        this->components[i]->setPosition(finalPosition);
    }
    BoundingBox *box = this->getBoundingBox();
    box->position.x = position.x-1.5f;
    box->position.y = position.y+0.5f;
    box->position.z = position.z;
     
     
}

void Car2::updateComponentsPositions(Position position){
    this->setPosition(position);


    return;

}

std::vector<SceneObject*> Car2::getComponents(){
    return this->objects;
}