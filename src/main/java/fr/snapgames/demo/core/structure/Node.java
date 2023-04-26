package fr.snapgames.demo.core.structure;

import java.util.List;

/**
 * A Node interface to define child/prent hierarchy structure.
 * <p>
 * Here is an implementation with T=Entity
 *
 * <pre>
 *     Object&lt;Entity&gt;
 *     |__ Child1&lt;Entity&gt;
 *     |__ Child2&lt;tEntity&gt;
 * </pre>
 *
 * @param <T> the object type to be hierarchically organized.
 * @author Frédéric Delorme
 * @since 1.0.0
 */
public interface Node<T> {

    String getName();

    long getId();

    T setParent(T p);

    T getParent();

    T addChild(T c);

    List<T> getChild();
}
