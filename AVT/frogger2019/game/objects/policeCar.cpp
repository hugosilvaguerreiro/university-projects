#include "car.h"
#include "game.h"
#include "police_car.h"
#include "police_carMeshes.h"
#include <iostream>

PoliceCar::PoliceCar(BasicMaterial mat, Position position, bool looking_right) :  SceneCompositeObject(position) {

    int v_index = 0;
    int i_index = 0;
    for (int i = 0; i < POLICE_CAR; i++) {
        float amb[] = {};
        float diffuse[] = {};
         
         
        BasicMaterial material;
        if (i >= 3 && i < 5) {
            material = mat;
            
        } else {
            material = {{meshVector_POLICE_CAR[i].ka[0],meshVector_POLICE_CAR[i].ka[1],meshVector_POLICE_CAR[i].ka[2],meshVector_POLICE_CAR[i].ka[3]}, 
                        {meshVector_POLICE_CAR[i].kd[0],meshVector_POLICE_CAR[i].kd[1],meshVector_POLICE_CAR[i].kd[2],meshVector_POLICE_CAR[i].kd[3]}, 
                        {meshVector_POLICE_CAR[i].ks[0],meshVector_POLICE_CAR[i].ks[1],meshVector_POLICE_CAR[i].ks[2],meshVector_POLICE_CAR[i].ks[3]},
                            { 0.0f, 0.0f, 0.0f, 1.0f }, 1, 0};
        }
        
         
                        
        SceneObject *obj = new SceneObject(material, {position.x+1.f, position.y+10.6f, position.z+0.2f});
        obj->setTexture(Texture(DEFAULT, POLICE_CAR_TEX));
         
         
         
         
        createTeaPot(obj->getMesh(), v_index, i_index, custom_vertices_poli, custom_uvs_poli, custom_normals_poli, custom_indices_poli, &meshVector_POLICE_CAR[i]);
         
        this->addObject(obj);
        v_index += meshVector_POLICE_CAR[i].n_vertices * 4;
		i_index += meshVector_POLICE_CAR[i].n_indices;
    } 
    if(position.x > 0){
        this->velocity = -5;
    }
    else{
        this->velocity = 5;
    }
    this->direction = looking_right;
    if(looking_right) {
        this->initialPosition = {position.x+6.f, position.y-1.25f, position.z+0.5f};
        this->setPosition(this->initialPosition);
        this->pushRotation({90, 0,0,1});
        this->setBoundingBox({4.3, 1.1, { position.x - 0.4f, position.y }, CAR});
        
    } else {
        this->initialPosition = {position.x-6.f, position.y, position.z+0.5f};
        this->setPosition(this->initialPosition);
        this->pushRotation({270, 0,0,1});
        this->setBoundingBox({4.3, 1.1, { position.x - 1.5f, position.y + 0.5f }, CAR});
    }


}

void PoliceCar::setVelocity(float velocity) {
    this->velocity = velocity;

}

void PoliceCar::move(float seconds) {
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

void PoliceCar::updateComponentsPositions(Position position){
    this->setPosition(position);


    return;

}

std::vector<SceneObject*> PoliceCar::getComponents(){
    return this->objects;
}