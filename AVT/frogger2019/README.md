```
frogger2019/
├── engine - This is where everything related with low level rendering stuff goes
│   ├── controlers.cpp - All user interaction should be defined here.
│   ├── core
│   │   ├── engine.cpp - Main class, responsible for bringing all the pieces together
│   │   ├── engine_utils.cpp
│   │   └── renderer
│   │       ├── renderer.cpp - Responsible for all rendering
│   │       └── shaders
│   │           ├── pointlight.frag
│   │           └── pointlight.vert
│   ├── geometry - professor stuff
│   │   ├── basic_geometry.cpp
│   │   ├── basic_geometry.h
│   │   └── cube.h
│   ├── headers - All the headers that are defined by us, put them here
│   │   ├── common.h
│   │   ├── controlers.h
│   │   ├── engine.h
│   │   ├── engine_utils.h
│   │   ├── renderer.h
│   │   ├── scene.h
│   │   └── scene_object.h
│   ├── lib - professor stuff
│   │   ├── AVTmathLib.cpp
│   │   ├── AVTmathLib.h
│   │   ├── VertexAttrDef.h
│   │   ├── VSShaderLib.cpp
│   │   └── VSShaderLib.h
│   └── world - This is where we should define the main classes for objects, scenes, cameras, etc ...
│       ├── scene.cpp
│       └── scene_object.cpp
├── game - This is where we will define our main application that will make use of the classes defined in the engine
│   └── objects
├── main.cpp
└── Makefile
```