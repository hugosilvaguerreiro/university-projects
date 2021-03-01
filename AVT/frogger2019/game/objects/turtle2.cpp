#include "turtle.h"
#include "game.h"
#include "turtle_model.h"
#include "turtle_modelMeshes.h"
#include <iostream>

Turtle2::Turtle2(BasicMaterial mat, Position position) :  SceneCompositeObject(position) {
    int v_index = 0;
    int i_index = 0;
    for (int i = 0; i < TURTLE_MODEL; i++) {
        float amb[] = {};
        float diffuse[] = {};
        BasicMaterial material = mat;

      
        
          
            
        SceneObject *obj;
        if (i == 3) {
             obj = new SceneObject(turtleBodyMat2, {position.x, position.y, position.z });
            obj->setTexture(Texture(DEFAULT, TURTLE_BODY_DETAILED));    
        }
        else if (i == 2) {
            obj = new SceneObject(tortoiseShelMat , {position.x, position.y , position.z });
            obj->setTexture(Texture(DEFAULT, TURTLE_SHELL_DETAILED));    
        } else {
            obj = new SceneObject(material, {position.x, position.y, position.z });
            obj->setTexture(Texture(NO_TEXTURE, TURTLE_SHELL_DETAILED));
        }
        
        createTeaPot(obj->getMesh(), v_index, i_index, custom_vertices_turtle, custom_uvs_turtle, custom_normals_turtle, custom_indices_turtle, &meshVector_TURTLE_MODEL[i]);
        
        this->addObject(obj);
        v_index += meshVector_TURTLE_MODEL[i].n_vertices * 4;
		i_index += meshVector_TURTLE_MODEL[i].n_indices;
    } 
    if(position.x > 0){
        this->velocity = -2;
    }
    else{
        this->velocity = 2;
    }

    this->initialPosition = position;
    this->setPosition(position);
    this->pushRotation({90.f, 0.f ,0.f ,1.f});
    this->setBoundingBox({1.5, 1.8, {position.x - 0.75f, position.y + 0.75f}, TURTLE});
}

void Turtle2::setVelocity(float velocity) {
    this->velocity = velocity;

}

void Turtle2::move(float seconds) {
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
    box->position.x = position.x - 0.75f;
    box->position.y = position.y + 0.75f;
    box->position.z = position.z;
    
      
      
}

void Turtle2::updateComponentsPositions(Position position){
    this->setPosition(position);
}

std::vector<SceneObject*> Turtle2::getComponents(){
    return this->objects;
}
