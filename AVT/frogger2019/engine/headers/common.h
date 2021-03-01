#ifndef __COMMONH__
#define __COMMONH__


// Use Very Simple Libs
#include "VSShaderLib.h"
#include "AVTmathLib.h"
#include "basic_geometry.h"
#include "camera.h"

#define CAPTION "Frogger"
extern int WindowHandle;
extern int WinX, WinY;

extern unsigned int FrameCount;

extern VSShaderLib shader;

extern struct MyMesh mesh[4];
extern int objId; //id of the object mesh - to be used as index of mesh: mesh[objID] means the current mesh


//External array storage defined in AVTmathLib.cpp
/// The storage for matrices
extern float mMatrix[COUNT_MATRICES][16];
extern float mCompMatrix[COUNT_COMPUTED_MATRICES][16];

/// The normal matrix
extern float mNormal3x3[9];

extern GLint pvm_uniformId;
extern GLint vm_uniformId;
extern GLint normal_uniformId;
extern GLint lPos_uniformId;

// Camera Position
extern Camera Cam;
extern Camera OldCam;
// extern float camX, camY, camZ;

// Mouse Tracking Variables
extern int startX, startY, tracking;

// Camera Spherical Coordinates
extern float alpha, beta;
extern float r;

// Frame counting and FPS computation
extern long myTime,timebase,frame;
extern char s[32];
extern float lightPos[4];

#endif