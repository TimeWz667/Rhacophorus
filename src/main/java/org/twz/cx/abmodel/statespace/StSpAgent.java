package org.twz.cx.abmodel.statespace;

import org.json.JSONObject;
import org.twz.cx.abmodel.AbsAgent;
import org.twz.cx.abmodel.statespace.modifier.AbsModifier;
import org.twz.cx.abmodel.statespace.modifier.ModifierSet;
import org.twz.cx.element.Event;
import org.twz.cx.mcore.AbsSimModel;
import org.twz.dag.Chromosome;
import org.twz.exception.IncompleteConditionException;
import org.twz.statespace.AbsStateSpace;
import org.twz.statespace.State;
import org.twz.statespace.Transition;

import java.util.*;
import java.util.stream.Collectors;

public class StSpAgent extends AbsAgent {
    private State State;
    private Map<Transition, Double> Transitions;
    private ModifierSet Mods;

    public StSpAgent(String name, Chromosome pars, org.twz.statespace.State state) {
        super(name, pars);
        State = state;
        Transitions = new HashMap<>();
        Mods = new ModifierSet();
    }

    public org.twz.statespace.State getState() {
        return State;
    }

    @Override
    public Object get(String key) {
        if (key.equals("State")) {
            return State;
        } else {
            return super.get(key);
        }
    }

    @Override
    protected Event findNext() {
        if (Transitions.isEmpty()) {
            return Event.NullEvent;
        }
        Transition tr = null;
        double time = Double.POSITIVE_INFINITY;

        for (Map.Entry<Transition, Double> ent: Transitions.entrySet()) {
            if (ent.getValue() < time) {
                tr = ent.getKey();
                time = ent.getValue();
            }
        }
        if (Double.isInfinite(time)) return Event.NullEvent;
        return new Event(tr, time);
    }

    @Override
    public void updateTo(double ti) {
        Transitions = Transitions.entrySet().stream()
                .filter(e-> !Double.isNaN(e.getValue()))
                .filter(e-> e.getValue() > ti)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));


        List<Transition> new_trs = State.getNextTransitions();
        Set<Transition> add = new HashSet<>(new_trs);

        add.removeAll(Transitions.keySet());

        Transitions = Transitions.entrySet().stream()
                .filter(e-> new_trs.contains(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        double tte;
        for (Transition tr: add) {
            tte = tr.rand((org.twz.dag.Parameters) getParameters());
            for (AbsModifier mod: Mods.on(tr)) {
                tte = mod.modify(tte);
            }
            Transitions.put(tr, tte + ti);
        }
        dropNext();
    }

    @Override
    public void executeEvent() {
        Event next = getNext();
        if (!next.isCancelled()) {
            State = State.exec((Transition) next.getValue());
        }
    }

    @Override
    public void initialise(double ti, AbsSimModel model) {
        Transitions.clear();
        updateTo(ti);
    }

    @Override
    public void reset(double ti, AbsSimModel model) {
        Transitions.clear();
        updateTo(ti);
    }

    @Override
    public void shock(double ti, AbsSimModel model, String action, JSONObject value) {
        AbsModifier mod = Mods.get(action);
        if (mod.update(value)) {
            modify(action, ti);
        }
    }

    public void shockTransitions(Set<Transition> transitions, double ti) {
        if (transitions.isEmpty()) return;
        for (Transition transition : transitions) {
            Transitions.replace(transition, Double.NaN);
        }
        updateTo(ti);
    }

    public void shockTransitions(Transition transition, double ti) {
        Transitions.replace(transition, Double.NaN);
        updateTo(ti);
    }

    public void addMod(AbsModifier mod) {
        Mods.put(mod.getName(), mod);
    }


    public void modify(String m, double ti) {
        AbsModifier mod = Mods.get(m);
        Transition tr = mod.getTarget();
        if (Transitions.containsKey(tr)) {
            double tte = tr.rand((org.twz.dag.Parameters) getParameters());
            tte = Mods.on(tr).stream().reduce(tte, (sum, p) -> p.modify(sum), (sum1, sum2) -> sum2);
            Transitions.put(tr, tte + ti);
            dropNext();
        }
    }

    public boolean isa(State st) {
        return State.isa(st);
    }

    public boolean isa(String k, Object v) {
        if (k.equals("State")) {
            State st = (State) v;
            return isa(st);
        } else {
            return super.isa(k, v);
        }
    }

    public StSpAgent deepcopy(AbsStateSpace dc_new, List<String> tr_ch) {
        StSpAgent ag = new StSpAgent(getName(), Parameters, dc_new.getState(State.getName()));
        for (Map.Entry<Transition, Double> ent: Transitions.entrySet()) {
            if (!tr_ch.contains(ent.getKey().getName())) {
                ag.Transitions.put(dc_new.getTransition(ent.getKey().getName()), ent.getValue());
            }
        }
        return ag;
    }

    @Override
    public JSONObject toJSON() {
        return null;
    }
}
