package org.twz.cx.abmodel.statespace.modifier;

import org.twz.statespace.Transition;

/**
 *
 * Created by timewz on 30/09/17.
 */
public abstract class AbsModifier implements Cloneable {
    private final String Name;
    private final Transition Target;

    public AbsModifier(String name, Transition target) {
        Name = name;
        Target = target;
    }

    public String getName() {
        return Name;
    }

    public abstract double getValue();

    public Transition getTarget() {
        return Target;
    }

    public abstract double modify(double tte);

    public abstract boolean update(Object value);

    public abstract AbsModifier clone();


}
