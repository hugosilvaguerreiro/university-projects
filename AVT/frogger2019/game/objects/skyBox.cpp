#include "skybox.h"
#include "game.h"
#include "sky_box.h"
#include "sky_boxMeshes.h"
#include <iostream>

SkyBox::SkyBox(BasicMaterial mat, Position position) :  SceneCompositeObject(position) {

    int v_index = 0;
    int i_index = 0;
    for (int i = 0; i < SKY_BOX; i++) {
        float amb[] = {};
        float diffuse[] = {};
         
         
        BasicMaterial material = mat;
        
         
                        
        SceneObject *obj = new SceneObject(material, {position.x+1.f, position.y+10.6f, position.z+0.2f});
        obj->setTexture(Texture(DEFAULT, SKY));
        obj->pushRotation({90.f, 1.f, 0.f, 0.f});
        obj->pushRotation({90.f, 0.f, 1.f, 0.f});
         
         
        createTeaPot(obj->getMesh(), v_index, i_index, custom_vertices_s, custom_uvs_s, custom_normals_s, custom_indices_s, &meshVector_SKY_BOX[i]);
         
        this->addObject(obj);
        v_index += meshVector_SKY_BOX[i].n_vertices * 4;
		i_index += meshVector_SKY_BOX[i].n_indices;
        this->mesh = obj->getMesh();
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

void SkyBox::setVelocity(float velocity) {
    this->velocity = velocity;

}

void SkyBox::move(float seconds) {
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

void SkyBox::updateComponentsPositions(Position position){
    this->setPosition(position);


    return;

}

std::vector<SceneObject*> SkyBox::getComponents(){
    return this->objects;
}
