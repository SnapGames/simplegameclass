/**
 * This <code>fr.snapgames.demo.core</code> package define all the 2D platform game framework and utilities to build a simple 2D game.
 * <p>
 * Base on the {@link fr.snapgames.demo.core.Game} class, your game must extend the {@link fr.snapgames.demo.core.Game} class
 * and override the prepare(context) and create(context) methods.
 * </p>
 *
 * <ul>
 *     <li><code>prepare()</code> where you will preload all the required resourceSystem and prepared some objects,
 *     like preloading {@link fr.snapgames.demo.core.gfx.animation.Animations},</li>
 *     <li><code>create()</code> here a instantiated all the required {@link fr.snapgames.demo.core.entity.Entity},
 *     {@link fr.snapgames.demo.core.entity.Camera}, {@link fr.snapgames.demo.core.entity.Particle}, etc...
 *     to set your game scene.</li>
 * </ul>
 *
 * <pre>
 * <code>
 * public class MyGame extends Game{
 *     public myGame(String[] args){
 *         super(args);
 *     }
 *     public void prepare(Map&lt;String,Object&gt; context){
 *         //...
 *     }
 *     public void create(Map&lt;String,Object&gt; context){
 *         //...
 *     }
 *     public static void main(String[]args){
 *         MyGame g = new MyGame(args);
 *         g.run();
 *     }
 *  }
 * </code>
 * </pre>
 */
package fr.snapgames.demo.core;
