#ifndef __CUSTOMMODELH__
#define __CUSTOMMODELH__


#include<stdio.h>
#include<iostream>
#include<vector>
#include "basic_geometry.h"
struct ModelDetails {
    float *vertexPoints;
    int *faceIndexes;
    float *vertexNormals;
    float *textureIndexes;
    int nrVertexPoints;
    int nrFaceIndexes;
    int nrIndexes;
    int nrVertexNormals;
    int nrTextureIndexes;
};


void loadModel(char *fname, ModelDetails *myModel);
void createTeaPot( MyMesh *mesh , int v_index, int i_index,float *custom_vertices, float *custom_uvs, 
                    float *custom_normals, int *custom_indices, customMesh *custom_mesh );
void createModel(MyMesh *obj_mesh, ModelDetails* myModel);
void loadModel();

#endif