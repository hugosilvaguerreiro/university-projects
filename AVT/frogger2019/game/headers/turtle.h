#ifndef __TURTLEH__
#define __TURTLEH__

#include "scene_object.h"

class Turtle : public SceneObject {
    private:
        std::vector<SceneObject*> components;
    public:
        Turtle(BasicMaterial mat, Position position);
        void move(float seconds);
        std::vector<SceneObject*> getComponents();
        void updateComponentsPositions(Position position);
        void setVelocity(float velocity) override;
        ~Turtle();
};


class Turtle2 : public SceneCompositeObject {
    private:
        std::vector<SceneObject*> components;
    public:
        Turtle2(BasicMaterial mat, Position position);
        void move(float seconds);
        std::vector<SceneObject*> getComponents();
        void updateComponentsPositions(Position position);
        void setVelocity(float velocity) override;
};
#endif