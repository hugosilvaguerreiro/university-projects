#ifndef __CARH__
#define __CARH__

#include "scene_object.h"

class Car : public SceneObject {
    private:
        std::vector<SceneObject*> components;
    public:
        Car(BasicMaterial mat, Position position);
        void move(float seconds);
        std::vector<SceneObject*> getComponents();
        void updateComponentsPositions(Position position);
        void setVelocity(float velocity) override;
        ~Car();
};


class Car2 : public SceneCompositeObject {
    private:
        std::vector<SceneObject*> components;
    public:
        Car2(BasicMaterial mat, Position position);
        void move(float seconds);
        std::vector<SceneObject*> getComponents();
        void updateComponentsPositions(Position position);
        void setVelocity(float velocity) override;
};

class CartoonCar : public SceneCompositeObject {
    private:
        std::vector<SceneObject*> components;
    public:
        CartoonCar(BasicMaterial mat, Position position);
        void move(float seconds);
        std::vector<SceneObject*> getComponents();
        void updateComponentsPositions(Position position);
        void setVelocity(float velocity) override;
};


class PoliceCar : public SceneCompositeObject {
    private:
        std::vector<SceneObject*> components;
        bool direction;
    public:
        PoliceCar(BasicMaterial mat, Position position, bool looking_right);
        void move(float seconds);
        std::vector<SceneObject*> getComponents();
        void updateComponentsPositions(Position position);
        void setVelocity(float velocity) override;
};


#endif