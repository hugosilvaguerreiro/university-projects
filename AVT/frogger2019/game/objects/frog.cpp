#include "frog.h"
#include "scene_object.h"
#include "game.h"
#include <iostream>
#include "engine.h"
#include "froggy.h"
#include "froggyMeshes.h"

bool COLIDE_RIVER =false;
bool COLIDE_PAVEWALK =false;
bool COLIDE_TURTLE =false;
bool COLIDE_TRUNK =false;
bool COLIDE_CAR =false;


#define TOP_BOUND 12
#define BOTTOM_BOUND -11
#define LEFT_BOUND -15
#define RIGHT_BOUND 15

 
BasicMaterial frogMaterial = {{0.4f, 0.5f, 0.4f, 1.0f}, {0.4f, 0.5f, 0.4f, 1.0f}, {0.04f, 0.7f, 0.04f, 1.0f}, {0.0f, 0.0f, 0.0f, 1.0f}, .078125f, 0};
BasicMaterial frogBodyMaterial = {{0.1f, 0.35f, 0.1f, 1.0f}, {0.1f, 0.35f, 0.1f, 1.0f}, {0.45f, 0.55f, 0.45f, 1.0f}, {0.0f, 0.0f, 0.0f, 1.0f}, .25f, 0};

float body_width = 0.25f;
float body_height = 0.5f;
float sub_leg_height = body_height*0.2f;
float sub_leg_width = body_width*0.4f;
bool ONPAVEWALK = false;

void move_scene_object(SceneObject *obj, int direction);
void move_frog_element(Frog *frog, int direction);
Frog::Frog(Position pos) : SceneCompositeObject(pos) {
    this->initialPosition.x = pos.x;
    this->initialPosition.y = pos.y;
    this->initialPosition.z = pos.z;
     
     
     
    
    int v_index = 0;
    int i_index = 0;
    for (int i = 0; i < FROGGY; i++) {
        SceneObject *obj = new SceneObject(grassMat, {position.x + 1.f, position.y + 10.6f, position.z + 0.2f});
        obj->setTexture(Texture(NO_TEXTURE, FROG_TEX));
        obj->setRotation(90.f, 1, 0, 0);
         
        createTeaPot(obj->getMesh(), v_index, i_index, custom_vertices, custom_uvs, custom_normals, custom_indices,&meshVector[i]);
        obj->setScale(0.1,0.1,0.1);
        this->addObject(obj);
        v_index += meshVector[i].n_vertices * 4;
		i_index += meshVector[i].n_indices;
    } 
    
    this->setBoundingBox({body_width, body_height, {-body_height/2 + pos.x, body_width/2.f + pos.y}, FROG});
}
void move_frog_to_position(Frog *frog, Position pos);
int total = 0;

void Frog::preColision(SceneObject *otherObject) {
    switch(otherObject->getBoundingBox()->objectType) {
        case RIVER:
            COLIDE_RIVER = true;
            break;
        case TRUNK:
             
            COLIDE_TRUNK = true;
            Moviment::setOnTrunk(true, otherObject);
            break;
        case CAR:
            COLIDE_CAR = true;
        break;
        case TURTLE:
             
            COLIDE_TURTLE = true;
            Moviment::setOnTurtle(true, otherObject);
            break;
    }
}

void Frog::afterColisions() {
     
    COLIDE_RIVER =false;
    COLIDE_PAVEWALK =false;
    COLIDE_TURTLE =false;
    COLIDE_TRUNK =false;
    COLIDE_CAR =false;
}

void Frog::onColision(SceneObject *otherObject) {
    switch (otherObject->getBoundingBox()->objectType) {
    case FROG:
         
        break;
    case CAR:   
        this->nrLives -= 1;
        if(this->nrLives <= 0) {
            this->nrLives = 0;
            ENGINE_GAME_OVER = true;
            Engine::togglePause();
        }
        move_frog_to_position(this, this->initialPosition);

        break;
    case RIVER:
        if(!COLIDE_TRUNK && !COLIDE_TURTLE) {
             
            if(Moviment::isOnTrunk())
                Moviment::setOnTrunk(false, nullptr);
            if(Moviment::isOnTurtle())
                Moviment::setOnTurtle(false, nullptr);
            if(ONPAVEWALK) {
                ONPAVEWALK = false;
                 
            }
            this->nrLives -= 1;
            if(this->nrLives <= 0) {
                this->nrLives = 0;
                ENGINE_GAME_OVER = true;
                Engine::togglePause();
            }
             
            move_frog_to_position(this, this->initialPosition);
        }

        break;
    case PAVEWALK:
         
        if(Moviment::isOnTrunk())
            Moviment::setOnTrunk(false, nullptr);
        if(Moviment::isOnTurtle())
            Moviment::setOnTurtle(false, nullptr);
         
         
        if(!ONPAVEWALK) {
            ONPAVEWALK = true;
             
        }
    break;
    case TRUNK:
         
        if(!Moviment::isOnTrunk()) {
            Moviment::setOnTrunk(true, otherObject);
        }
        if(Moviment::isOnTurtle()) {
            Moviment::setOnTurtle(false, nullptr);
        }
    break;
    case TURTLE:
        if(!Moviment::isOnTurtle()) {
            Moviment::setOnTurtle(true, otherObject);
        }
        if(Moviment::isOnTrunk()) {
            Moviment::setOnTrunk(false, nullptr);
        }
    break;
    default:
        if(ONPAVEWALK) {
            ONPAVEWALK = false;
             
        }
        break;
    }
}

FrogPlayer::FrogPlayer(Frog *frog) : Player(frog) {
    this->cam = nullptr;
    this->spotLight = nullptr;
    this->frog = frog;
    this->frog->player = this;
}

SceneObject* FrogPlayer::getObject() {
    return this->frog;
}
int FrogPlayer::getLifes() {
    return this->frog->nrLives;
}
void FrogPlayer::setLifes(int nrlifes) {
    this->frog->nrLives = nrlifes;
}

void create_leg(Frog *frog, Position pos, float scale_x, float scale_y, float scale_z) {
    SceneCube *leftSubLeg = new SceneCube(frogMaterial, pos);
    leftSubLeg->setScale(scale_x, scale_y, 0.5);
    leftSubLeg->setScale(scale_x, scale_y, scale_z);
    frog->addObject(leftSubLeg);
}

void move_scene_object_to_position(SceneObject *obj, float x, float y, float z) {
    obj->getPosition()->x = x;
    obj->getPosition()->y = y;
    obj->getPosition()->z = z;
}

void move_frog_to_position(Frog *frog, Position pos) {
    frog->nrRow = 0;
     
    frog->setPosition({pos.x, pos.y, pos.z});

    BoundingBox *box = frog->getBoundingBox();
    box->position.x = frog->getPosition()->x - body_height/2.f;
    box->position.y = frog->getPosition()->y + body_width/2.f;
    frog->setBoundingBox(*box);
    frog->player->set_cam(frog->player->get_cam());
    frog->player->set_light(frog->player->get_light());
    frog->player->set_light_dir(0.f, 1.f, 0.f);


    /*std::cout << "##############\n";
    std::cout << pos.x << " "  << pos.y << " " << pos.z << "\n" ;
    Position pos2 = frog->getBoundingBox()->position;
    std::cout << pos2.x << " "  << pos2.y << " " << pos2.z << "\n" ;
    std::cout << "##############\n";*/
}
#define JUMP_SIZE 1
void move_scene_object(SceneObject *obj, int direction) {
    switch(direction){
        case 0:
            obj->getPosition()->x = obj->getPosition()->x - JUMP_SIZE > LEFT_BOUND ? obj->getPosition()->x - JUMP_SIZE : obj->getPosition()->x;
            obj->setRotation(270.f, 0.f, 0.f , -1.f);
        break;
        case 1:
            obj->getPosition()->x = obj->getPosition()->x + JUMP_SIZE < RIGHT_BOUND ? obj->getPosition()->x +JUMP_SIZE : obj->getPosition()->x;
            obj->setRotation(90.f, 0.f, 0.f , -1.f);
        break;
        case 2:
            obj->getPosition()->y = obj->getPosition()->y + JUMP_SIZE < TOP_BOUND ? obj->getPosition()->y + JUMP_SIZE : obj->getPosition()->y;
            obj->setRotation(0.f, 0.f, 0.f , -1.f);
        break;
        case 3:
            obj->getPosition()->y = obj->getPosition()->y - JUMP_SIZE > BOTTOM_BOUND ? obj->getPosition()->y - JUMP_SIZE : obj->getPosition()->y;
            obj->setRotation(180.f, 0.f, 0.f , -1.f);
        break;
        case 4:  
            obj->getPosition()->z += 1.15;
        break;
        case 5:  
            obj->getPosition()->z -= 1.15;
        break;
    }
}

void move_frog_element(Frog *frog, int direction) {
    /*
        direction: 
            0 - left
            1 - right
            2 - front
            3 - back
    */
   std::cout << frog->getPosition()->x << " " << frog->getPosition()->y  << "\n";
   move_scene_object(frog, direction);
   BoundingBox *box = frog->getBoundingBox();
   box->position.x = frog->getPosition()->x-body_height/2;
   box->position.y = frog->getPosition()->y+body_width/2;

}


void FrogPlayer::moveObjectToPosition(Position pos) {
    move_frog_to_position(this->frog, pos);
}

void FrogPlayer::move_left() {
    Frog *frog = ((Frog*)this->obj);
    move_frog_element(frog, 0);
    set_cam(this->cam);
    set_light(this->spotLight);
    set_light_dir(-1.f, 0.f, 0.f);
}

void FrogPlayer::move_right() {
    Frog *frog = ((Frog*)this->obj);
    move_frog_element(frog, 1);
    set_cam(this->cam);
    set_light(this->spotLight);
    set_light_dir(1.f, 0.f, 0.f);

}

void FrogPlayer::move_front() {
    
    Frog *frog = ((Frog*)this->obj);
    float oldy = this->obj->getPosition()->y;
    move_frog_element(frog, 2);
    set_cam(this->cam);
    set_light(this->spotLight);
    set_light_dir(0.f, 1.f, 0.f);
     
    if(this->obj->getPosition()->y > oldy) {
        this->setPoints(std::max(this->getPoints(), ++this->frog->nrRow));
    }
    if(this->frog->nrRow % 22 == 0) {
        move_frog_to_position(this->frog, this->frog->initialPosition);
    }

    /*std::cout << "##############\n";
    Position pos2 = frog->getBoundingBox()->position;
    std::cout << pos2.x << " "  << pos2.y << " " << pos2.z << "\n" ;
    std::cout << "##############\n";*/
}

void FrogPlayer::move_back() {
    Frog *frog = ((Frog*)this->obj);
    float oldy = this->obj->getPosition()->y;
    move_frog_element(frog, 3);
    set_cam(this->cam);
    set_light(this->spotLight);
    set_light_dir(0.f, -1.f, 0.f);
    if(this->obj->getPosition()->y < oldy) {
        this->frog->nrRow--;
    }
}


void FrogPlayer::set_cam(Camera *cam) {
    Frog *frog = ((Frog*)this->obj);
    this->cam = cam;
    if(this->cam != nullptr) {
        cam->setPos(frog->getPosition()->x, frog->getPosition()->y-5.f, 3.f);
        cam->setTarget(frog->getPosition()->x, frog->getPosition()->y, frog->getPosition()->z);
        cam->setUp(0.f, 0.f, 1.f);
        frog->cam = cam;
        return;
    }   
    frog->cam = cam;   
}

Camera *FrogPlayer::get_cam() {
    return this->cam;
}


void FrogPlayer::set_light(SpotLight *light) {
    this->spotLight = light;
    if(this->spotLight != nullptr) {
        Frog *frog = ((Frog*)this->obj);
        this->spotLight->position[0] = frog->getPosition()->x;
        this->spotLight->position[1] = frog->getPosition()->y;
        this->spotLight->position[2] = frog->getPosition()->z;
        frog->spotLight = light;
    }
}

void FrogPlayer::set_light_dir(float x, float y, float z) {
    if(this->spotLight != nullptr) {
        Frog *frog = ((Frog*)this->obj);
        this->spotLight->coneDir[0] = 90.f * x;
        this->spotLight->coneDir[1] = 90.f * y;
        this->spotLight->coneDir[2] = 90.f * z;
    }
}

SpotLight *FrogPlayer::get_light() {
    return this->spotLight;
}