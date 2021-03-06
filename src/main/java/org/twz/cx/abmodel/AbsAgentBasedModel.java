package org.twz.cx.abmodel;

import org.json.JSONException;
import org.json.JSONObject;
import org.twz.cx.abmodel.behaviour.AbsBehaviour;
import org.twz.cx.abmodel.behaviour.ActiveBehaviour;
import org.twz.cx.abmodel.network.AbsNetwork;
import org.twz.cx.element.Event;
import org.twz.cx.element.ModelAtom;
import org.twz.cx.element.Request;
import org.twz.cx.mcore.AbsObserver;
import org.twz.cx.mcore.IY0;
import org.twz.cx.mcore.LeafModel;
import org.twz.dag.Parameters;
import org.twz.exception.IncompleteConditionException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by TimeWz on 09/07/2018.
 */
public abstract class AbsAgentBasedModel<Ta extends AbsAgent> extends LeafModel {

    private Population<Ta> Population;
    protected Map<String, AbsBehaviour> Behaviours;

    public <Tm extends AbsAgentBasedModel> AbsAgentBasedModel(String name, Parameters parameters,
                                                              Population<Ta> pop, AbsObserver<Tm> obs, IY0 protoY0) {
        super(name, parameters, obs, protoY0);
        this.Population = pop;
        Behaviours = new LinkedHashMap<>();
    }

    public <Tm extends AbsAgentBasedModel> AbsAgentBasedModel(String name, Map<String, Double> parameters,
                                                              Population<Ta> pop, AbsObserver<Tm> obs, IY0 protoY0) {
        this(name, new Parameters(name, null, parameters, 0), pop, obs, protoY0);
    }

    public void addBehaviour(AbsBehaviour be) {
        Behaviours.put(be.getName(), be);
        Scheduler.addAtom(be);
    }

    public void addNetwork(AbsNetwork net) {
        this.Population.addNetwork(net);
    }

    protected void makeAgents(int n, double ti, Map<String, Object> attributes) throws JSONException {
        List<Ta> ags = this.Population.addAgents(n, attributes);
        for (Ta ag: ags) {
            for (AbsBehaviour be: Behaviours.values()) {
                be.register(ag, ti);
            }
            Scheduler.addAtom(ag);
        }
    }

    public org.twz.cx.abmodel.Population<Ta> getPopulation() {
        return Population;
    }

    @Override
    public ModelAtom getAtom(String atom) {
        if (Behaviours.containsKey(atom)) {
            return Behaviours.get(atom);
        }

        try {
            return Population.get(atom);
        } catch (NullPointerException e){
            return null;
        }
    }

    @Override
    public void preset(double ti) {

        for (AbsBehaviour be : Behaviours.values()) {
            try {
                be.initialise(ti, this);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        for (Ta ag : this.Population.getAgents().values()) {
            try {
                ag.initialise(ti, this);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        super.preset(ti);
    }


    @Override
    public void reset(double ti) {
        Behaviours.values().forEach(be->be.reset(ti, this));
        this.Population.getAgents().values().forEach(ag -> ag.reset(ti, this));
        super.reset(ti);
    }

    protected List<AbsBehaviour> checkEnter(Ta ag) {
        return Behaviours.values().stream().filter(be -> be.checkEnterChange(ag)).collect(Collectors.toList());
    }

    protected void impulseEnter(List<AbsBehaviour> bes, Ta ag, double ti) {
        for (AbsBehaviour be : bes) {
            try {
                be.impulseEnter(this, ag, ti);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    protected List<AbsBehaviour> checkExit(Ta ag) {
        return Behaviours.values().stream().filter(be -> be.checkExitChange(ag)).collect(Collectors.toList());
    }

    protected void impulseExit(List<AbsBehaviour> bes, Ta ag, double ti) {
        for (AbsBehaviour be : bes) {
            try {
                be.impulseExit(this, ag, ti);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    protected List<Boolean> checkPreChange(Ta ag) {
        return Behaviours.values().stream().map(be -> be.checkPreChange(ag)).collect(Collectors.toList());
    }

    protected List<Boolean> checkPostChange(Ta ag) {
        return Behaviours.values().stream().map(be -> be.checkPostChange(ag)).collect(Collectors.toList());
    }

    protected List<AbsBehaviour> checkChange(List<Boolean> pre, List<Boolean> post) {
        List<AbsBehaviour> be_all = new ArrayList<>(Behaviours.values()), bes = new ArrayList<>();
        AbsBehaviour be;

        for (int i = 0; i < be_all.size(); i++) {
            be = be_all.get(i);
            if (be.checkChange(pre.get(i), post.get(i))) {
                bes.add(be);
            }
        }
        return bes;
    }

    protected void impulseChange(List<AbsBehaviour> bes, Ta ag, double ti) {
        for (AbsBehaviour be : bes) {
            try {
                be.impulseChange(this, ag, ti);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Ta> birth(int n, double ti, Map<String, Object> attributes) throws JSONException {
        List<Ta> ags = this.Population.addAgents(n, attributes);
        List<AbsBehaviour> bes;
        JSONObject js = new JSONObject();
        int nBirth = 0;
        for (Ta ag : ags) {
            Behaviours.values().forEach(be->be.register(ag, ti));
            bes = checkEnter(ag);
            ag.initialise(ti, this);
            impulseEnter(bes, ag, ti);
            Scheduler.addAndScheduleAtom(ag);
            nBirth ++;
        }
        if (nBirth > 0) {
            try {
                js.put("n", nBirth);
                js.put("attributes", attributes);
                disclose("add agents by " + n, "*", js);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return ags;
    }

    public void kill(String id, double ti) {
        try {
            Ta ag = Population.get(id);
            List<AbsBehaviour> bes = checkExit(ag);
            Scheduler.removeAtom(ag);
            Population.removeAgent(id);
            impulseExit(bes, ag, ti);
            disclose("remove agent " + id, "*");
        } catch (NullPointerException ignored) {

        }
    }

    @Override
    public void doRequest(Request req) throws JSONException {
        String nod = req.Who;
        Event todo = req.Todo;
        double time = req.getTime();
        if (Behaviours.containsKey(nod)) {
            ActiveBehaviour be = (ActiveBehaviour) Behaviours.get(nod);
            be.approveEvent(todo);
            be.operate(this);
        } else {
            try {
                Ta ag = this.Population.get(nod);
                ag.approveEvent(todo);
                List<Boolean> pre = checkPreChange(ag);
                record(ag, todo.getValue(), time);
                ag.executeEvent();
                ag.dropNext();
                List<Boolean> post = checkPostChange(ag);
                List<AbsBehaviour> bes = checkChange(pre, post);
                impulseChange(bes, ag, time);
                ag.updateTo(time);
            } catch (NullPointerException ignored) {

            } catch (IncompleteConditionException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void shock(double time, String action, JSONObject value) {
        try {
            AbsBehaviour be = Behaviours.get(action);
            be.shock(time, this, action, value);
        } catch (NullPointerException | JSONException e) {
            e.printStackTrace();
        }
    }

    protected abstract void record(AbsAgent ag, Object todo, double time);

    public long size() {
        return Population.count();
    }
}
