# README

A simple `Game`  class project will all inside.


## build

```bash
$> ./build.sh 
```

## run

```bash
$> java -jar target/simpleclass.jar
```

Available CLI arguments & configuration file parameters:

| Configuration   | Argument      | Type      | Decription                                           |
| :-------------- | :------------ | :-------- | :--------------------------------------------------- |
| game.size       | game,gs       | Dimension | define window dimension: `(width)x(height)`          |
| game.resoltuion | resoltuion, r | Dimension | Define Screen resolution `(width)x(height)`          |
| game.playarea   | playarea, gpa | Dimension | Define the play ground dimension: `(width)x(height)` |
| game.title      | title,t       | String    | define the title for the game window                 |
| game.debug      | debug,d       | Integer   | set the debug information level (0 to 5)             |

Any argument can be activated by using the command line option -D
```bash
$> java -jar target/simpleclass.jar -Dtitle=ThisIsMyWindow
```

McG.
