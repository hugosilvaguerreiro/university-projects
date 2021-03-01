#ifndef __TGAH__
#define __TGAH__

#define TGA_RGB		2
#define TGA_A		3
#define TGA_RLE		10

#include <vector>
#include <string>

typedef GLushort WORD;
typedef GLubyte byte;

typedef struct tImageTGA
{
	int channels;
	int size_x;	
	int size_y;				
	unsigned char *data;
} tImageTGA;


void TGA_Texture(unsigned int *textureArray, char *strFileName, int ID);
void loadCubemap(unsigned int *textureArray, int ID, std::vector<std::string> faces);
tImageTGA *Load_TGA(char *filename);


#endif

// www.morrowland.com
// apron@morrowland.com