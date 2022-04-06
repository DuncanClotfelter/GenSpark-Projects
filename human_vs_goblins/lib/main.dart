
import 'package:flutter/material.dart';
import 'dart:async';
import 'dart:typed_data';
import 'package:flutter/services.dart';
import 'dart:math';
import "dart:collection";

void main() => runApp(const MyApp());

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Hooman Vs Gooblins',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: const MyHomePage(title: 'Hooman vs Gooblins', key: Key("yes"),),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({required Key key, required this.title}) : super(key: key);
  final String title;

  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _ImageData {
  final String name;
  final int width;
  final int height;
  final Image img;
  final double ratio;

  get path {return "resources/$name.png";}

  _ImageData(this.name, this.width, this.height, Size windowSize) :
        img = Image(image: ResizeImage(AssetImage('resources/$name.png'),
            width: (windowSize.height / height * width).toInt(),
            height: windowSize.height.toInt(),
            allowUpscaling: true)),
        ratio = windowSize.height / height * width;

  _ImageData.xResize(this.name, width, height, this.ratio, Size windowSize) :
        width = (width * ratio).toInt(),
        height = (height * ratio).toInt(),
        img = Image(image: ResizeImage(AssetImage('resources/$name.png'),
            width: (width * ratio).toInt(),
            height: (height * ratio).toInt(),
            allowUpscaling: true));

  _ImageData.noResize(this.name, this.width, this.height) :
        img = Image(image: AssetImage('resources/$name.png')),
        ratio = 1;
}

class _Tile {
  Point pt;
  _ImageData img;

  _Tile(this.pt, this.img);

  @override
  toString() {
    return pt.toString();
  }
}

enum Action {ATTACKING, WALKING, IDLING, DYING, FALLING, JUMPING}
enum Button {STOP, LEFT, UP, RIGHT, DOWN, ATTACK}

class _Frame {
  final Action _action;
  final int _stage;

  const _Frame(this._action, this._stage);

  _Frame nextFrame() {
    return _Frame(_action, _stage+1);
  }
}

abstract class _Entity {
  bool facingRight;
  Point pos = const Point(0, 0);
  _Frame curFrame = const _Frame(Action.IDLING, 0);

  _Entity(this.facingRight);

  void setAction(Action toSet) {
    if(curFrame._action != toSet) {
      curFrame = _Frame(toSet, 0);
    } else {
      curFrame = curFrame.nextFrame();
    }
  }

  void move(double x, double y) {
    if(x != 0) {facingRight = x > 0;}
    pos = Point(pos.x + x, pos.y + y);
  }

  Widget widget(double adjX, double adjY) =>
      Positioned(
          top: adjY - (getModelSheet().height / getModelSheetUnitSize().y * (getActionPoint().y + 1)) - pos.y,
          left: adjX + (-getModelSheet().width / getModelSheetUnitSize().x) * getActionPoint().x,
          child: ClipRect(
            child: getModelSheet().img,
            clipper: _SubRect(getActionPoint(), getModelSheetUnitSize().x.toInt(), getModelSheetUnitSize().y.toInt()),
          )
      )
  ;

  _ImageData getModelSheet();
  Point getModelSheetUnitSize();
  Point getActionPoint();
}

class _Goblin extends _Entity {
  static final _ImageData _modelSheet = _ImageData.noResize("goblin.peasant", 320, 256);

  _Goblin() : super(false);

  @override _ImageData getModelSheet() {return _modelSheet;}
  @override Point getModelSheetUnitSize() {return const Point(10, 4);}

  @override
  Point getActionPoint() {
    switch(curFrame._action) {
      case Action.DYING:
        return const Point(1, 1);
      case Action.WALKING:
        return Point(9 - curFrame._stage % 6, 0);
      case Action.ATTACKING:
        return Point(9 - curFrame._stage % 5, 2);
      default: //Idle
        return const Point(9, 0);
    }
  }
}

class _Player extends _Entity {
  static final _ImageData _modelSheet = _ImageData.noResize("advnt_full", 320, 640);

  _Player() : super(true);

  @override _ImageData getModelSheet() {return _modelSheet;}
  @override Point getModelSheetUnitSize() {return const Point(10, 10);}

  @override
  Point getActionPoint() {
    switch(curFrame._action) {
      case Action.WALKING:
        return Point(1 + curFrame._stage % 6, 0);
      case Action.ATTACKING:
        return Point(7 + curFrame._stage % 2, 2);
      case Action.JUMPING:
        return Point(7 + curFrame._stage % 3, 1);
      case Action.FALLING:
        return const Point(1, 5);
      default: //Idle
        return const Point(0, 0);
    }
  }
}

class _SubRect extends CustomClipper<Rect> {
  final Point _pt;
  final int partsX;
  final int partsY;

  _SubRect(this._pt, this.partsX, this.partsY);

  @override
  Rect getClip(Size size) {
    return Rect.fromLTWH(
        size.width / partsX * _pt.x,
        size.height / partsY * _pt.y,
        size.width / partsX,
        size.height / partsY
    );
  }

  @override
  bool shouldReclip(CustomClipper<Rect> oldClipper) =>
      !(oldClipper is _SubRect && oldClipper._pt == _pt);
}

class _MyHomePageState extends State<MyHomePage> {
  static const platform = MethodChannel('com.example.humans_vs_goblins');
  static Map<String, _ImageData> images = {};
  static int imagesLoaded = 0;
  late final Size windowSize;
  static final _Player p = _Player();
  static const int framesPerPlatform = 6;
  static Map<int, _Entity> entities = {0 : p};
  bool hasWon = false, hasLost = false;

  @override
  void initState() {
    super.initState();
    resizeImages();
  }

  @override
  void didChangeDependencies() async {
    super.didChangeDependencies();
    images.forEach((key, value) {
      precacheImage(value.img.image, context).then(increaseLoadCount);
    });
    platform.setMethodCallHandler(javaMethodCallHandler);
    loadTiles(await platform.invokeMethod('getTiles'));
    Timer.periodic(const Duration(milliseconds: 40), (timer) {
      if (_moveQueue.isEmpty) {return;}
      _moveQueue.first.call();
      _moveQueue.removeFirst();
    });
  }

  _Entity getEntFromChar(int id, int char) {
    print("Creating new entity! $id $char");
    switch(char) {
      /*case 71:*/ default: return _Goblin();
    }
  }

  Future<dynamic> javaMethodCallHandler(MethodCall methodCall) async {
    switch (methodCall.method) {
      case 'notifyWin':
        setState(() {
          hasWon = true;
        });
        break;
      case 'notifyLoss':
        setState(() {
          hasLost = true;
        });
        break;
      case 'updateEntities':
        Int32List data = methodCall.arguments;
        print(data);
        for (int i = 0; i < framesPerPlatform; i++) {
          _moveQueue.add(() => {
              setState(() {
                for(int i = 0; i+4 < data.length; i+=5) {
                  if(entities[data[i]] == null) {
                    entities[data[i]] = getEntFromChar(data[i], data[i+1]);
                    entities[data[i]]!.pos = Point(images['mid']!.width * data[i+3], data[i+4] * images['mid']!.height);
                  }
                  _Entity e = entities[data[i]]!;
                  e.setAction(Action.values[data[i+2]]);

                  var xDiff = (data[i+3] * images['mid']!.width - e.pos.x);
                  var xDir = xDiff > 0 ? 1 : (xDiff == 0 ? 0 : -1);
                  var yDiff = (data[i+4] * images['mid']!.height - e.pos.y);
                  var yDir = yDiff > 0 ? 1 : (yDiff == 0 ? 0 : -1);
                  e.move(xDir * (images['mid']!.width / framesPerPlatform),
                      yDir * (images['mid']!.height / framesPerPlatform));
                }
              })
            });
        }
        return null;
      default:
        throw MissingPluginException('notImplemented');
    }
  }

  FutureOr<dynamic> increaseLoadCount(void a) async {setState(() {imagesLoaded++;});}

  final _moveQueue = Queue<Function>();

  //ImageStreamListener i = ImageStreamListener((ImageInfo image, bool synchronousCall) { increaseCodeLount(image, synchronousCall); });

  /*static Image? bg;
  static void increaseCodeLount(ImageInfo info, bool sync) {
    bg = info.image as Image?;
  }*/

  Future<void> resizeImages() async {
    //ResizeImage img = ResizeImage(const AssetImage('resources/back.png'));
    //img.resolve(ImageConfiguration.empty).addListener(ImageStreamListener((ImageInfo image, bool synchronousCall) { increaseCodeLount(image, synchronousCall); }));

    windowSize = MediaQueryData.fromWindow(WidgetsBinding.instance!.window).size;
    images = {
      "bg0": _ImageData("back", 144, 240, windowSize),
      "bg1": _ImageData("far", 176, 240, windowSize),
      "tree": _ImageData("middle", 192, 240, windowSize),
      "left": _ImageData.xResize("left", 16, 48, 2, windowSize),
      "mid": _ImageData.xResize("mid", 16, 48, 2, windowSize),
      "right": _ImageData.xResize("right", 16, 48, 2, windowSize),
      "float": _ImageData.xResize("float", 16, 16, 2, windowSize),
    };
  }

  loadTiles(Int32List receivedTiles) {
    int prevGroundTile = -1;
    for(int i = 0; i < receivedTiles.length; i+= 2) {
      int x = receivedTiles[i];
      int y = receivedTiles[i+1];

      Point p = Point(x * images['mid']!.width, (5 * images['mid']!.ratio) + windowSize.height - ((y+1) * images['mid']!.height));
      if(y != 0) {
        tiles.add(_Tile(p, images['float']!));
        continue;
      }

      bool hasGroundLeft = prevGroundTile == x-1;
      bool hasGroundRight = receivedTiles.length > i+3 && receivedTiles[i+2] == x+1 && receivedTiles[i+3] == 0;
      if(hasGroundLeft && !hasGroundRight) {
        tiles.add(_Tile(p, images['right']!));
      } else if(!hasGroundLeft && hasGroundRight) {
        tiles.add(_Tile(p, images['left']!));
      } else {
        tiles.add(_Tile(p, images['mid']!));
      }

      if(y == 0) {
        prevGroundTile = x;
      }
    }
  }

  List<_Tile> tiles = [];

  Widget _buildImage() {
    if (imagesLoaded >= images.length && !hasWon && !hasLost) {
      return Stack(//First item on bottom, last item on top
        children: <Widget>[
          for(double i = 0; i < windowSize.width + p.pos.x; i += images['bg0']!.width)
            Positioned(
                left: i - p.pos.x,
                top: 0,
                child: images['bg0']!.img
            )
          ,
          for(double i = 0; i < windowSize.width + p.pos.x; i += images['bg1']!.width)
            Positioned(
                left: i - p.pos.x,
                top: 0,
                child: images['bg1']!.img
            )
          ,
          for(double i = 0; i < windowSize.width + p.pos.x; i += images['tree']!.width)
            Positioned(
                left: i - p.pos.x,
                top: 0,
                child: images['tree']!.img
            )
          ,
          for(_Tile t in tiles)
            Positioned(
                left: t.pt.x.toDouble() - p.pos.x,
                top: t.pt.y.toDouble(),
                child: t.img.img
            )
          ,
          for(_Entity e in entities.values)
            e.widget(e.pos.x - p.pos.x.toDouble(),
              (5 * images['mid']!.ratio) + windowSize.height - images['mid']!.height),
          _getButtons()
        ],
      );
    } else if(imagesLoaded < images.length) {
      return const Center(child: Text('Loading'));
    } else if(hasWon) {
      return const Center(child: Text('YOU HAVE SUCCEEDED', style: TextStyle(color: Colors.green),));
    } else  {
      return const Center(child: Text('FAILURE', style: TextStyle(color: Colors.red),));
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: _buildImage(),
    );
  }

  Point dirVector = const Point(0, 0);
  void press(Button pressed) {
    switch(pressed) {
      case Button.STOP: dirVector = const Point(0, 0); break;
      /*Toggle buttons
      case Button.UP: dirVector = Point(dirVector.x, dirVector.y == 1 ? 0 : 1); break;
      case Button.RIGHT: dirVector = Point(dirVector.x == 1 ? 0 : 1, dirVector.y); break;
      case Button.LEFT: dirVector = Point(dirVector.x == -1 ? 0 : -1, dirVector.y); break;*/
      case Button.UP: dirVector = Point(dirVector.x, 1); break;
      case Button.RIGHT: dirVector = const Point(1, 0); break;
      case Button.LEFT: dirVector = const Point(-1, 0); break;
    }
    print("Button pressed: ${[dirVector.x, dirVector.y]}");
    platform.invokeMethod('setPlayerDir', [dirVector.x, dirVector.y]);
  }

  Color getColor(Button toShow) {
    switch(toShow) {
      case Button.STOP:
        return dirVector == const Point(0, 0) ? Colors.green : Colors.red;
      /*case Button.LEFT:
        return dirVector.x < 0 ? Colors.green : Colors.blue;
      case Button.RIGHT:
        return dirVector.x > 0 ? Colors.green : Colors.blue;
      case Button.UP:
        return dirVector.y > 0 ? Colors.green : Colors.blue;*/
      default:
        return Colors.blue;
    }
  }

  Stack _getButtons() {
    if (hasWon || hasLost) {
      return Stack();
    }
    return Stack(
      children: [
        Positioned(
          bottom: 20,
          right: 20,
          child: FloatingActionButton(
              backgroundColor: getColor(Button.RIGHT),
              onPressed: () => press(Button.RIGHT),
              child: const Icon(Icons.keyboard_double_arrow_right_sharp)
          ),
        ),
        Positioned(
          bottom: 20,
          right: 80,
          child: FloatingActionButton(
              backgroundColor: getColor(Button.UP),
              onPressed: () => press(Button.UP),
              child: const Icon(Icons.keyboard_double_arrow_up_sharp)
          ),
        ),
        /*Positioned(
          bottom: 20,
          right: 140,
          child: FloatingActionButton(
              backgroundColor: getColor(Button.LEFT),
              onPressed: () => press(Button.LEFT),
              child: const Icon(Icons.keyboard_double_arrow_left_sharp)
          ),
        ),
        Positioned(
          bottom: 20,
          right: 230,
          child: FloatingActionButton(
              backgroundColor: getColor(Button.STOP),
              onPressed: () => press(Button.STOP),
              child: const Icon(Icons.stop_rounded)
          ),
        ),*/
      ],
    );
  }
}