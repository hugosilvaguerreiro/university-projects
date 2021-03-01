#include "common.h"
#include "material.h"

#include <cstring>
#include "customModel.h"
#include "scene_object.h"



#define DEFAULT_OBJ_TYPE -1

SceneObject::SceneObject(Position position, MyMesh *mesh) {

    this->mesh = mesh;
    this->position = position;
    this->scale = {1.f,1.f,1.f};
    this->rotation = {0.f, 0.f, 0.f, 0.f};
    this->boundingBox = {0,0,DEFAULT_OBJ_TYPE};
    this->texture = Texture();
}

void SceneObject::setVelocity(float velocity) {
    this->velocity = velocity;
}

SceneObject::SceneObject(BasicMaterial mat, Position position) {
    this->rotations = std::vector<Rotation>();
    this->position = position;
    this->scale = {1.f,1.f,1.f};
    this->rotation = {0.f, 0.f, 0.f, 0.f};

    this->wait=false;

    MyMesh *mesh = new MyMesh();
    memcpy(mesh->mat.ambient, &mat.amb,4*sizeof(float));
	memcpy(mesh->mat.diffuse, &mat.diff,4*sizeof(float));
	memcpy(mesh->mat.specular, &mat.spec,4*sizeof(float));
	memcpy(mesh->mat.emissive, &mat.emissive,4*sizeof(float));
	mesh->mat.shininess = mat.shininess;
	mesh->mat.texCount = mat.texcount;
    this->mesh = mesh;
    this->boundingBox = {0,0,{position.x, position.y},DEFAULT_OBJ_TYPE};
    this->texture = Texture();
}

void SceneObject::setTexture(Texture texture) {
    this->texture = texture;
}

Texture *  SceneObject::getTexture() {
    return &this->texture;
}

std::vector<Rotation> *SceneObject::getRotations() {
    return &this->rotations;
}

void SceneObject::pushRotation(Rotation rotation) {
    this->rotations.push_back(rotation);
}

void SceneObject::popRotation() {
    this->rotations.pop_back();
}

void SceneObject::onColision(SceneObject* object) {
     
}
void SceneObject::preColision(SceneObject* object) {
     
}
void SceneObject::afterColisions() {
     
}
void SceneObject::setBoundingBox(BoundingBox box) {
    this->boundingBox = box;
}

BoundingBox *SceneObject::getBoundingBox() {
    return &this->boundingBox;
}

struct MyMesh *SceneObject::getMesh() {
    return this->mesh;
}

Position * SceneObject::getPosition() {
    return &this->position;
}
void SceneObject::setPosition(Position position) {
    this->position= position;
}

Rotation * SceneObject::getRotation() {
    return &this->rotation;
}

Scale * SceneObject::getScale() {
    return &this->scale;
}

void SceneObject::setRotation(float angle, float x, float y, float z) {
    this->rotation = {angle, x, y , z};
}

void SceneObject::setScale(float x, float y, float z) {
    this->scale = {x, y , z};
}


SceneModel::SceneModel(BasicMaterial mat, Position position) : SceneCompositeObject(position) {

}

SceneCube::SceneCube(BasicMaterial mat,Position position) : SceneObject(mat,position) {
    createCube(this->mesh);
    this->setBoundingBox({1, 1, {position.x-0.5f, position.y+0.5f}, DEFAULT_OBJ_TYPE });
}

void SceneCube::setScale(float x, float y, float z) {
    this->scale = {x, y , z};
    this->getBoundingBox()->length = x;
    this->getBoundingBox()->width = y;
    this->getBoundingBox()->position.x = this->getPosition()->x + x/2;
    this->getBoundingBox()->position.y = this->getPosition()->y + y/2;
}

SceneQuad::SceneQuad(BasicMaterial mat,Position position, float size_x, float size_y) : SceneObject(mat, position) {
    createQuad(this->mesh, size_x, size_y);
    this->setBoundingBox({size_x, size_y, {position.x-size_x/2, position.y+size_y/2}, DEFAULT_OBJ_TYPE });   
}

SceneSphere::SceneSphere(BasicMaterial mat,Position position, float radius, int divisions) : SceneObject(mat, position) {
    createSphere(this->mesh, radius, divisions);
}

ScenePawn::ScenePawn(BasicMaterial mat,Position position) :SceneObject(mat, position) {
    createPawn(this->mesh);
}

SceneCone::SceneCone(BasicMaterial mat,Position position, float height, float baseRadius, int sides) : SceneObject(mat,position){
    createCone(this->mesh, height, baseRadius, sides);
}

SceneTorus::SceneTorus(BasicMaterial mat,Position position, float innerRadius, float outerRadius, int rings, int sides) : SceneObject(mat, position){
    createTorus(this->mesh,innerRadius, outerRadius, rings, sides);
}

SceneCylinder::SceneCylinder(BasicMaterial mat,Position position, float height, float radius, int sides) : SceneObject(mat, position){
    createCylinder(this->mesh, height, radius, sides);
}


SceneCompositeObject::SceneCompositeObject(Position position) : SceneObject(position, nullptr) {
}

void SceneCompositeObject::addObject(SceneObject *obj){
    this->objects.push_back(obj);
}

bool SceneObject::isToWait(){
    return this->wait;
}

void SceneObject::hasToWait(){
    this->wait = true;
}

void SceneObject::canContinue(){
    this->wait = false;
}

void SceneObject::reset(float velocity){
    this->velocity = velocity;
    this->updateComponentsPositions(this->initialPosition);
}

void SceneObject::updateComponentsPositions(Position position){
    throw "Not implemented";
}