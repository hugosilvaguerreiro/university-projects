#ifndef __FROGH__
#define __FROGH__

#include "scene_object.h"
#include "player.h"
#include "light.h"

enum FrogDirection {F_UP, F_DOWN, F_LEFT, F_RIGHT};

class FrogPlayer;

class Frog : public  SceneCompositeObject {
    public:
        FrogPlayer *player;
        Camera *cam = nullptr;
        SpotLight *spotLight = nullptr;
        int nrLives = 5;
        int nrPoints = 0;
        int nrRow = 0;
        Position initialPosition;

        void onColision(SceneObject* object) override;
        void preColision(SceneObject* object) override;
        void afterColisions() override;
        Frog(Position pos);
        
};

class FrogPlayer : public Player {
    private: 
        Camera *cam;
        SpotLight *spotLight;
    public:
        Frog *frog;
        FrogPlayer(Frog *frog);
        void set_cam(Camera *cam) override;
        Camera *get_cam();

        void set_light(SpotLight *light);
        void set_light_dir(float x, float y, float z);
        SpotLight *get_light();
        int getLifes() override;
        void setLifes(int lifes) override;
        SceneObject * getObject() override;
        void moveObjectToPosition(Position pos) override;
        void move_left() override;
        void move_right() override;
        void move_front() override;
        void move_back() override;
};
void create_leg(Frog *frog, Position pos, float scale_x, float scale_y, float scale_z);

#endif