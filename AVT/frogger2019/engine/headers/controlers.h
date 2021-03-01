#ifndef __CONTROLERSH__
#define __CONTROLERSH__
#include "player.h"


class Controlers {
    
    public:
        static Player *player;
        static void setPlayer(Player * Player);
        static Player* getPlayer();
        static void processKeys(unsigned char key, int xx, int yy);
        static void processMouseMotion(int xx, int yy);
        static void mouseWheel(int wheel, int direction, int x, int y);
        static void processMouseButtons(int button, int state, int xx, int yy);
};

#endif