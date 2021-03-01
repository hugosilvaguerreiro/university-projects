#ifndef __PLAYERH__
#define __PLAYERH__

#include "scene_object.h"

class Player {
    public:
        SceneObject *obj;   
        int points;
        int lifes;
        Position initialPosition;
        Player(SceneObject *player_obj);
        virtual void set_cam(Camera *cam) = 0;
        virtual Camera *get_cam() = 0;
        virtual int getPoints();
        virtual SceneObject* getObject();
        virtual void moveObjectToPosition(Position pos);
        virtual void setPoints(int points);
        virtual int getLifes();
        virtual void setLifes(int lives);
        virtual void move_left();
        virtual void move_right();
        virtual void move_front();
        virtual void move_back();
        Position getInitialPosition();
};

#endif