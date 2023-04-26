package fr.snapgames.demo.core.entity;

/**
 * List of possible Entity type. stadard ones can be RECTANGLE, ELLIPSE or
 * IMAGE.
 *
 * @author Frédéric Delorme
 * @since 1.0.0
 */
public enum EntityType {
    /**
     * this entity having a NONE type may not be displayed.
     */
    NONE,
    /**
     * Entity drawn as a simple dot
     */
    DOT,
    /**
     * Entity drawn as a line between (position) to (position+velocity)
     */
    LINE,
    /**
     * Entity drawn as a Rectangle as position of size (width x height)
     */
    RECTANGLE,
    /**
     * Entity drawn as an Eclipse as position of size (r1=width x r2=height)
     */
    ELLIPSE,
    /**
     * Entity drawn as Image as position of size (width x height)
     */
    IMAGE;
}
