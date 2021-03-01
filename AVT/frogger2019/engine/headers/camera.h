#ifndef __CAMERAH__
#define __CAMERAH__


enum CameraType { PERSPECTIVE, ORTHO };

class Camera {
    public:
        CameraType type;
        int w, h;
        float camX, camY, camZ;
        float targetX = 0.f, targetY = 0.f, targetZ = 0.f;
        float upX = 0.f, upY = 1.f, upZ = 0.f;
        int currentMode = 0;

        Camera(CameraType type, int w, int h, float camX = 0.f, float camY = 0.f, float camZ = 20.f);
        int getMode();
        void setMode(int mode);
        void setOrtho();
        void setPerspective();
        void resizeWindow(int w, int h);
        void lookat();
        void setPos(float x, float y, float z);  
        void setTarget(float x, float y, float z); 
        void setUp(float x, float y, float z);
};

#endif