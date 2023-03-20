# README

A simple `Game`  class project will all inside.

## build

```bash
build.sh 
```

## run

```bash
java -jar target/simpleclass.jar
```

Available CLI arguments & configuration file parameters:

| Configuration          | Argument   | Type      | Decription                                           |
|:-----------------------|:-----------|:----------|:-----------------------------------------------------|
| game.size              | size       | Dimension | define window dimension: `(width)x(height)`          |
| game.screen.resolution | resolution | Dimension | Define Screen resolution `(width)x(height)`          |
| game.physic.play.area  | playarea   | Dimension | Define the play ground dimension: `(width)x(height)` |
| game.physic.gravity    | gravity    | Dimension | Define the play ground dimension: `(width)x(height)` |
| game.title             | title      | String    | define the title for the game window                 |
| game.debug             | debug      | Integer   | set the debug information level (0 to 5)             |

Any argument can be activated by using the command line option :

```bash
$> java -jar target/simpleclass.jar game.title=ThisIsMyWindow
```



McG.
