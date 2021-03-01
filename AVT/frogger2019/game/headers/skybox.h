#ifndef __SKYBOXH__
#define __SKYBOXH__

#include "scene_object.h"

class SkyBox : public SceneCompositeObject {
    private:
        std::vector<SceneObject*> components;
        //float velocity;
        Position initialPosition;
    public:
        SkyBox(BasicMaterial mat, Position position);
        void move(float seconds);
        std::vector<SceneObject*> getComponents();
        void updateComponentsPositions(Position position);
        void setVelocity(float velocity) override;
};



#endif