package fr.snapgames.demo.demo01;

import fr.snapgames.demo.core.Game;
import fr.snapgames.demo.core.scenes.SceneManager;
import fr.snapgames.demo.core.system.SystemManager;
import fr.snapgames.demo.demo01.scenes.PlayScene;
import fr.snapgames.demo.demo01.scenes.TitleScene;

import java.util.Map;

/**
 * A demonstration implemntation of a Game with its own Scenes instances.
 * 
 * @author Frédéric Delorme
 * @since 1.0.3
 */
public class Demo01 extends Game {

    /**
     * Create the Game demonstration instance with the required parameters.
     * 
     * @param args                  arguments from comman line.
     * @param pathToConfigPropsFile path to the configuration file.
     */
    public Demo01(String[] args, String pathToConfigPropsFile) {
        super(args, pathToConfigPropsFile);
    }

    /**
     * Prepare the Game scenes.
     * 
     * @param context a contextetual map to output metadata to create step.
     */
    @Override
    protected void prepare(Map<String, Object> context) {
        SceneManager scm = (SceneManager) SystemManager.get("SceneManager");
        // add required Scenes
        scm.add(new TitleScene(this));
        scm.add(new PlayScene(this));
    }

    /**
     * Create the first scene.
     * 
     * @param context a contextetual map to input metadata from prepare step.
     */
    @Override
    protected void create(Map<String, Object> context) {
        SceneManager scm = (SceneManager) SystemManager.get("SceneManager");
        // activate the preferred start scene.
        scm.activate("play");
    }

    /**
     * The entry moint for demonstration game execution.
     * 
     * @param args
     */
    public static void main(String[] args) {
        Demo01 demo01 = new Demo01(args, "/demo01.properties");
        demo01.run();
    }
}
