#ifndef __BUSH__
#define __BUSH__

#include "scene_object.h"

class Bus : public SceneObject {
    private:
        std::vector<SceneObject*> components;
    public:
        Bus(BasicMaterial mat, Position position);
        void move(float seconds);
        std::vector<SceneObject*> getComponents();
        void updateComponentsPositions(Position position);
        void setVelocity(float velocity) override;
        ~Bus();
};

#endif