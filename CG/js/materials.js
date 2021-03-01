var WIREFRAME = false;



var tableTexture = new THREE.TextureLoader().load( './textures/towel.png' );
tableTexture.wrapS = THREE.RepeatWrapping;
tableTexture.wrapT = THREE.RepeatWrapping;
tableTexture.repeat.set( 18,10 );

var clothTexture = new THREE.TextureLoader().load( './textures/cloth.png' );
clothTexture.wrapS = THREE.RepeatWrapping;
clothTexture.wrapT = THREE.RepeatWrapping;
clothTexture.repeat.set(360 ,200 );

var deadTexture = new THREE.TextureLoader().load( './textures/dead.png' );
deadTexture.wrapS = THREE.RepeatWrapping;
deadTexture.wrapT = THREE.RepeatWrapping;
deadTexture.flipY = false;

var pauseTexture = new THREE.TextureLoader().load( './textures/pause.png' );
pauseTexture.wrapS = THREE.RepeatWrapping;
pauseTexture.wrapT = THREE.RepeatWrapping;
pauseTexture.flipY = false;


var orangeTexture = new THREE.TextureLoader().load( './textures/orange.jpg' );
orangeTexture.wrapS = THREE.RepeatWrapping;
orangeTexture.wrapT = THREE.RepeatWrapping;
orangeTexture.repeat.set(4,4);

var cheerioTexture = new THREE.TextureLoader().load('./textures/cheerio_bump.png');
cheerioTexture.wrapS = THREE.RepeatWrapping;
cheerioTexture.wrapT = THREE.RepeatWrapping;
cheerioTexture.repeat.set(2.5,1);

//var TABLE_MATERIAL = [new THREE.MeshPhongMaterial({map: texture,shininess: 10}), new THREE.MeshLambertMaterial( {map: texture} )];
var TABLE_MATERIAL = [new THREE.MeshPhongMaterial({color: 0xAAAAAA,shininess: 2, map:tableTexture, bumpMap: clothTexture}), new THREE.MeshLambertMaterial( {color: 0xAAAAAA, map:tableTexture} )];

var BUTTER_MATERIAL = [new THREE.MeshPhongMaterial({color: 0xFFFF00,specular: 0x020202,shininess: 50}), new THREE.MeshLambertMaterial( {color: 0xFFFF00})];

var ORANGE_BODY_MATERIAL =  [new THREE.MeshPhongMaterial({color: 0xF69D03,specular: 0x020202,shininess: 200, map: orangeTexture}), new THREE.MeshLambertMaterial( {color: 0xF69D03, map: orangeTexture})];
var ORANGE_LEAF_MATERIAL =  [new THREE.MeshPhongMaterial({color: 0x236e2b,specular: 0x020202,shininess: 50}), new THREE.MeshLambertMaterial( {color: 0x236e2b})];
var ORANGE_CONE_MATERIAL =  [new THREE.MeshPhongMaterial({color: 0x144a1a,specular: 0x020202,shininess: 10}), new THREE.MeshLambertMaterial( {color: 0x144a1a})];

var CHEERIO_MATERIALS = [[ new THREE.MeshPhongMaterial({color: 0x8B5555,specular: 0x020202,shininess: 20, bumpMap: cheerioTexture}), new THREE.MeshLambertMaterial({color: 0x8B5555}) ],
						 [ new THREE.MeshPhongMaterial({color: 0x94A35E,specular: 0x020202,shininess: 20, bumpMap: cheerioTexture}), new THREE.MeshLambertMaterial({color: 0x94A35E}) ],
						 [ new THREE.MeshPhongMaterial({color: 0xD98056,specular: 0x020202,shininess: 20, bumpMap: cheerioTexture}), new THREE.MeshLambertMaterial({color: 0xD98056}) ],
						 [ new THREE.MeshPhongMaterial({color: 0xDB5742,specular: 0x020202,shininess: 20, bumpMap: cheerioTexture}), new THREE.MeshLambertMaterial({color: 0xDB5742}) ],
						 [ new THREE.MeshPhongMaterial({color: 0xE45640,specular: 0x020202,shininess: 20, bumpMap: cheerioTexture}), new THREE.MeshLambertMaterial({color: 0xE45640}) ]];

var CAR_DOME_MATERIAL = [new THREE.MeshPhongMaterial({color: 0x25272b,specular: 0x020202,shininess: 1000, transparent:true, opacity:0.8}), new THREE.MeshLambertMaterial( {color: 0x25272b, transparent:true, opacity:0.8}), new THREE.MeshBasicMaterial({color: 0x25272b})];
var CAR_RIMS_MATERIAL = [new THREE.MeshPhongMaterial({color: 0xc0c0c0,specular: 0x020202,shininess: 230}), new THREE.MeshLambertMaterial( {color: 0xc0c0c0}), new THREE.MeshBasicMaterial({color: 0xc0c0c0})];
var CAR_HUB_MATERIAL = [new THREE.MeshPhongMaterial({color: 0x808080,specular: 0x020202,shininess: 230}), new THREE.MeshLambertMaterial( {color: 0x808080}), new THREE.MeshBasicMaterial({color: 0x808080})];
var CAR_WHEEL_MATERIAL = [new THREE.MeshPhongMaterial({color: 0x202020,specular: 0x010101,shininess: 10}), new THREE.MeshLambertMaterial( {color: 0x202020}), new THREE.MeshBasicMaterial({color: 0x202020})];
var CAR_BODY_MATERIAL = [new THREE.MeshPhongMaterial({color: 0xaa0000,specular: 0x020202,shininess: 100}), new THREE.MeshLambertMaterial( {color: 0xaa0000}), new THREE.MeshBasicMaterial({color: 0xaa0000})];
var CAR_SPOILSUP_MATERIAL = [new THREE.MeshPhongMaterial({color: 0xaa0000,specular: 0xaa0000,shininess: 100}), new THREE.MeshLambertMaterial( {color: 0xaa0000}), new THREE.MeshBasicMaterial({color: 0xaa0000})];
var CAR_SPOILTOP_MATERIAL = [new THREE.MeshPhongMaterial({color: 0x25272b,specular: 0x020202,shininess: 100}), new THREE.MeshLambertMaterial( {color: 0x25272b}), new THREE.MeshBasicMaterial({color: 0x25272b})];
var CAR_ORNAMENT_MATERIAL = [new THREE.MeshPhongMaterial({color: 0xaaaaaa,specular: 0x020202,shininess: 800}), new THREE.MeshLambertMaterial( {color: 0xaaaaaa}), new THREE.MeshBasicMaterial({color: 0xaaaaaa})];
var CAR_HEADLIGHT_MATERIAL = [new THREE.MeshPhongMaterial({color: 0x25272b,specular: 0x020202,shininess: 1000, transparent:true, opacity:0.4}), new THREE.MeshLambertMaterial( {color: 0x25272b, transparent:true, opacity:0.4}), new THREE.MeshBasicMaterial({color: 0x25272b})];

var CANDLE_MATERIAL = [new THREE.MeshPhongMaterial({color: 0xaa0000,specular: 0x020202,shininess: 180}), new THREE.MeshLambertMaterial( {color: 0xaa0000})];
var CANDLE_BASE_MATERIAL = [new THREE.MeshPhongMaterial({color: 0xaaaaaa,specular: 0x020202,shininess: 230}), new THREE.MeshLambertMaterial( {color: 0xaaaaaa})];