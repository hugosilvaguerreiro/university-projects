#ifndef __SCENEOBJECTH__
#define __SCENEOBJECTH__

//#include "common.h"
#include "basic_geometry.h"
#include "customModel.h"
#include "material.h"
#include "textures.h"

struct Position {
    float x, y, z;
};

struct Scale {
    float x, y, z;
};

struct Rotation {
    float angle, x, y, z;
};

struct BoundingBox {
    float length, width ; //length -> x, width -> y; height -> z  
    Position position;
    int objectType;
};

class SceneObject {
    protected:
        MyMesh *mesh;
        Scale scale;
        Rotation rotation;
        std::vector<Rotation> rotations;
        Position position;
        BoundingBox boundingBox;
        Texture texture;
        bool wait;
        Position initialPosition;        
    public:
        bool castShadow = true;
        bool reflect = true;
        float velocity;
        SceneObject(Position position, MyMesh *mesh);
        SceneObject(BasicMaterial mat, Position position);
        MyMesh *getMesh();
        Position *getPosition();
        Scale *getScale();
        Rotation *getRotation();
        std::vector<Rotation> *getRotations();
        void pushRotation(Rotation rotation);
        void popRotation();
        BoundingBox *getBoundingBox();
        void setTexture(Texture texture);
        Texture * getTexture();
        void setScale(float x, float y, float z);
        void setPosition(Position position);
        void setBoundingBox(BoundingBox box);
        virtual void setVelocity(float velocity);
        virtual void onColision(SceneObject* object);
        virtual void preColision(SceneObject* object);
        virtual void afterColisions();
        void setRotation(float angle, float x, float y, float z);
        virtual void move(float seconds){};
        bool isToWait();
        void hasToWait();
        void canContinue();
        void reset(float velocity);
        virtual void updateComponentsPositions(Position position);
};

class SceneCube : public SceneObject {
    public:
        SceneCube(BasicMaterial mat,Position position);
        void setScale(float x, float y, float z);
        
};

class SceneQuad : public SceneObject {
    public:
        SceneQuad(BasicMaterial mat,Position position, float size_x, float size_y);
};

class SceneSphere : public SceneObject {
    public:
        SceneSphere(BasicMaterial mat,Position position, float radius, int divisions);
};

class SceneCone : public SceneObject {
    public:
        SceneCone(BasicMaterial mat,Position position, float height, float baseRadius, int sides);
};

class ScenePawn : public SceneObject {
    public:
        ScenePawn(BasicMaterial mat,Position position);
};

class SceneTorus : public SceneObject {
    public:
        SceneTorus(BasicMaterial mat,Position position, float innerRadius, float outerRadius, int rings, int sides);
};

class SceneCylinder : public SceneObject {
    public:
        SceneCylinder(BasicMaterial mat,Position position, float height, float radius, int sides);
};


class SceneCompositeObject : public SceneObject {
    public:
        std::vector<SceneObject*> objects;
        SceneCompositeObject(Position position);
        void addObject(SceneObject *obj);
};

class SceneModel : public SceneCompositeObject {
    public:
        ModelDetails *model;
        SceneModel(BasicMaterial mat,Position position);
        //void setScale(float x, float y, float z);
};


#endif