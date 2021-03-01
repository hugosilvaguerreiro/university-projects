#ifndef __TRUNKH__
#define __TRUNKH__

#include "scene_object.h"

class Trunk : public SceneCompositeObject {
    private:
        SceneObject* component;
    public:
        Trunk(BasicMaterial mat, Position position);
        void move(float seconds);
        SceneObject* getComponent();
        void setVelocity(float velocity) override;
        void updateComponentsPositions(Position position);
};

#endif