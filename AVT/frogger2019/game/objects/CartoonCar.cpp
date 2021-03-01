#include "car.h"
#include "game.h"
#include "cartoon_car.h"
#include "cartoon_carMeshes.h"
#include <iostream>

CartoonCar::CartoonCar(BasicMaterial mat, Position position) :  SceneCompositeObject(position) {
    int v_index = 0;
    int i_index = 0;
    for (int i = 0; i < CARTOON_CAR; i++) {
        float amb[] = {};
        float diffuse[] = {};
        
         
         
        BasicMaterial material;
        if (i == 0 || i == 1) {
            v_index += meshVector_CARTOON_CAR[i].n_vertices * 4;
		    i_index += meshVector_CARTOON_CAR[i].n_indices;
            continue;
             
        } 
        else if (i == 1) {
            material = {{meshVector_CARTOON_CAR[i].ka[0],meshVector_CARTOON_CAR[i].ka[1],meshVector_CARTOON_CAR[i].ka[2],meshVector_CARTOON_CAR[i].ka[3]*0.3f}, 
                        {meshVector_CARTOON_CAR[i].kd[0],meshVector_CARTOON_CAR[i].kd[1],meshVector_CARTOON_CAR[i].kd[2],meshVector_CARTOON_CAR[i].kd[3]*0.3f}, 
                        {meshVector_CARTOON_CAR[i].ks[0],meshVector_CARTOON_CAR[i].ks[1],meshVector_CARTOON_CAR[i].ks[2],meshVector_CARTOON_CAR[i].ks[3]*0.3f},
                            { 0.0f, 0.0f, 0.0f, 1.0f }, 1, 0};;
        }
        else {
                material = mat;
        }
        
         
                        
        SceneObject *obj = new SceneObject(material, {position.x+1.f, position.y+10.6f, position.z+0.2f});
        if (i == 2) {
            obj->setTexture(Texture(DEFAULT, CARTOON_CAR_TEX));
        } else if(i==3) {
            obj->setTexture(Texture(DEFAULT, CARTOON_INTERIOR_TEX));
        }
        else {
            obj->setTexture(Texture(NO_TEXTURE, CARTOON_CAR_TEX));
        }
        

         
         
        createTeaPot(obj->getMesh(), v_index, i_index, custom_vertices_carto, custom_uvs_carto, custom_normals_carto, custom_indices_carto, &meshVector_CARTOON_CAR[i]);
         
        this->pushRotation({45,0,0,1});
        this->addObject(obj);
        v_index += meshVector_CARTOON_CAR[i].n_vertices * 4;
		i_index += meshVector_CARTOON_CAR[i].n_indices;
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

void CartoonCar::setVelocity(float velocity) {
    this->velocity = velocity;

}

void CartoonCar::move(float seconds) {
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

void CartoonCar::updateComponentsPositions(Position position){
    this->setPosition(position);


    return;
    /*this->components[0]->setPosition({ position.x -0.75, position.y + 0.5, position.z + 0.5});
    this->components[1]->setPosition({ position.x + 0.75, position.y + 0.5, position.z + 0.5});
    this->components[2]->setPosition({ position.x - 0.75,  position.y -0.5,  position.z + 0.5});
    this->components[3]->setPosition({ position.x + 0.75,  position.y - 0.5,  position.z +0.5});
    this->components[4]->setPosition({ position.x -1,  position.y -0.5,  position.z + 0.5});
    this->components[5]->setPosition({ position.x -0.5, position.y -0.5,  position.z + 0.75});*/
}

std::vector<SceneObject*> CartoonCar::getComponents(){
    return this->objects;
}