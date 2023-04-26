# Main class and user input

Building a game can be a game itself; or a nightmare. Depending on the tools, the team, the skills, … a lot of thing can
interact badly with the process of creating a game. And this is exactly what we won't talk about!:)

Here I will talk about creating a A-B-AB game with the only weapons provided by the nice Java JDK 19:) And no, I won't
use JavaFX (beuarrk:)) I'll use the good old AWT and Swing (yes, I am maybe a 50 years old man…).

Anyway let's try it through a series of posts to design some code.

## Creating a simple class

The basic Main class will support the famous "triptic" of the game loop: manage input, update objects, and draw
everything on screen!
But do you know why this is the basic default pattern for a game ?

## A bit of video game history

Why the game loop exists ? This is a very good question and the reason why is a historically based answer. Everything
starts from the first ever video game: PONG.

![Pong, where all begin !](https://cdn-images-1.medium.com/max/800/0*ySNC72GHeT19Nq3N "wikipedia PONG screenshot")

_figure 1 - Pong where all begins (ref:[https://fr.wikipedia.org/wiki/Pong](https://fr.wikipedia.org/wiki/Pong))_

The original Pong video game from wikipediaAt this very beginning time, the processor to execute tasks is a very a slow
on, almost some hundreds of Khz as CPU frequency. To understand the scale we are talking about, current processor are
running at 2 to 4 GHz!
So processor are very slow, each cycle of CPU is a precious one. So every line of code is very optimized and clearly
dedicated to some precise tasks.

And another element must be taken in account: the display process. At this time, screen where not flat one with a bunch
of LCD, but CRT ones. CRT display screen are based on ionic flow started from a cathode (electronic gun) and moving to
the anode (the screen grid) to excite fluorescent layer in the intern face of the glass bulb.

And swiping the all surface of the screen has a time cost: to display 25 frame per seconds, we need 16ms to swipe a
frame.
A CRT tube with its ions gun!The CRT Tube is nothing more than a big bubble light. (3) the cathode emits ions (1) and
(2) are anodes, deflecting ion ray to screen, lighting a fluorescent dot.

![A CRT diagram with ions gun and anodes deflectors](illustrations/figure-crt.jpg "A CRT diagram with ions gun and anodes deflectors (c) myself with my own hands !")

_figure 2 - A CRT diagram with ions gun and anodes deflectors_

This is the available time for the CPU to prepare next image!

So capturing input, moving things and displaying things must be done in 16ms. And loop again for the next frame.

So the game process is a LOOP. that's why we talk about a Main Loop:

![The basic Main loop explained with a pencil: the method to keep a fixed frame rate !](illustrations/figure-game-loop.jpg "the basic Main loop explained with a pencil: the method to keep a fixed frame rate ! (c) myself with my own hands !")

_figure 3 - The basic Main loop explained with a pencil: the method to keep a fixed frame rate !_

There is also some advanced version of the Main Loop, where multiple update can be performed between each rendering
phase, the timer is around the update methods only:

![The advanced method to keep a fixed update rate](illustrations/figure-game-loop-fixed.jpg "The advanced method to keep a fixed update rate (c) myself with my own hands !")

_figure 4 - The advanced method to keep a fixed update rate_

I can only invite you to read the fantastic book from Robert Nystrom for details about the Main loop.

> **Note:** diagram are largely inspired by the Robert Nystrom book, thanks to him to had shared his own knowledge!

Anyway, I need to implement my own. As a good diagram is better than word:

![A good diagram explaining the Main class and its usage](http://www.plantuml.com/plantuml/png/NOqngiCm341tdq9_-nrwWGmbMyyXOj4ARDdO4YazVULG2l4IulUafxKhDhMSmfy-AHFKX2mX-mUkDxXZfWM4zZ3-VeI5bJ7nc_ulN-FgM5hWCTui7fQDJYMNpUISsXgXKaYbL31HJa0lrW0m7QocVWi0au8KXOhMAJgO9gr6xnsZ977kD6VKt4vyHnxviN7YaNijVUHMTvRJ1m00 "figure 5 - A good diagram explaining the Main class and its usage")

_figure 5 - A good diagram explaining the Main class and its usage_

## The Main

And as any tutorial - guide - how-to won't exist without code; here it is!

```java
public class Main {
    public Main(String[] args) {
        initialize(args);
    }

    //... need to implement some initialization process
    public void run() {
        create();
        loop();
        dispose();
    }

    public void create() {
        // create resourceSystem for the game.
    }

    public void loop() {
        while (!isExit()) {
            input();
            update();
            render();
            waitUntilNextFrame();
        }
    }

    public void dispose() {
        // will created free resourceSystem
    }

    public static void game(String[] args) {
        Main game = new Main(args);
        game.run();
    }
}
```

So, what do you think of my (too?) basic class ? nothing fancy, nothing "bling bling". only useful and mandatory.

So yes, as you maybe now, the gameloop is the heart of any old school game. and even some Big game engine keep running
such old loop.
Let's dive into some details.

The Loop is certainly the most important one, so let get some code:

```java
public class Main {
    //...
    public void loop() {
        int frames = 0;
        int fps = getTargetFps();
        double internalTime = 0;
        double previousTime = System.currentTimeMillis();
        double currentTime = System.currentTimeMillis();
        double elapsed = currentTime - previousTime;
        create(this);
        while (!isExitRequested()) {
            currentTime = System.currentTimeMillis();
            input(this);
            elapsed = currentTime - previousTime;
            if (!isPaused()) {
                update(this, elapsed);
            }
            render(this, fps);
            frames += 1;
            internalTime += elapsed;
            if (internalTime > 1000.0) {
                fps = frames;
                frames = 0;
                internalTime = 0;
            }
            waitUntilNextFrame(elapsed);
            previousTime = currentTime;
        }
    }

    default void waitUntilNextFrame(double elapsed) {
        try {
            double timeFrame = 1000.0 / getTargetFps();
            int wait = (int) (elapsed < timeFrame ? timeFrame - elapsed : 1);
            Thread.sleep(wait);
        } catch (InterruptedException ie) {
            System.err.println("error while trying to wait for sometime");
        }
    }}
    //...
}
```

So to maintain a sustainable frequency for that loop, I use the default current traditional FPS: 60 frames per second.

So starting with that , defining the waiting time is a simple calculus.

```java
int timeFrame=1000/60;
        int wait=timeFrame-elapsed;
```

and the rest of the loop process is simple calls:

```java
elapsed=currentTime-previousTime;
        input();
        update(elapsed);
        render();
```

- input() will manage all the player's device input like keyboard, mouse or joystick,
- update(elapsed) will compute all the moves and animations for all the game objects,
- render() will draw the resulting objects and display that on screen.

And when you execute this incredible class… nothing will happen.
BUT… we now have the framework of our future game. I'm not joking, this is a fact ;)
