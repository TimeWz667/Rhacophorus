package hgm.abmodel;

import dcore.AbsDCore;
import dcore.Transition;
import hgm.abmodel.behaviour.AbsBehaviour;
import mcore.*;
import org.json.JSONObject;

import java.util.*;


/**
 *
 * Created by TimeWz on 2017/6/16.
 */
public class AgentBasedModel extends LeafModel {
    private final Population Agents;
    private AbsDCore DCore;
    private Map<String, AbsBehaviour> Behaviours;

    public AgentBasedModel(String name, AbsDCore dc, mcore.Meta meta, String prefix) {
        super(name, new ObserverABM(), meta);
        Agents = new Population(dc, prefix);
        Behaviours = new LinkedHashMap<>();
        DCore = dc;
    }

    public void addObsState(String state) {
        ((ObserverABM) getObserver()).addObsState(DCore.getState(state));
    }

    public void addObsTransition(String transition) {
        ((ObserverABM) getObserver()).addObsTransition(DCore.getTransition(transition));
    }

    public void addObsBehaviour(String behaviour) {
        ((ObserverABM) getObserver()).addObsBehaviour(Behaviours.get(behaviour));
    }

    public AbsBehaviour getBehaviours(String be) {
        return Behaviours.get(be);
    }

    public Population getPopulation() {
        return Agents;
    }

    @Override
    public void clear() {

    }

    @Override
    public void reset(double ti) {

    }

    @Override
    public void readY0(Y0<Double> y0, double ti) {

    }

    @Override
    public void listen(String src_m, String src_v, String tar_p) {

    }

    @Override
    public void listen(Collection<String> src_m, String src_v, String tar_p) {

    }

    @Override
    public boolean impulseForeign(AbsSimModel fore, double ti) {
        return false;
    }

    @Override
    public void findNext() {

    }

    @Override
    public void doRequest(Request req) {
        String nod = req.getNode();
        Event evt = req.getEvent();
        double time = req.getTime();

        if (Behaviours.containsKey(nod)) {
            AbsBehaviour be = Behaviours.get(nod);
            be.exec(this, evt);
        } else {
            Agent ag = Agents.get(nod);
            Transition tr = (Transition) evt.getValue();
            ((ObserverABM) getObserver()).record(ag, tr, time);
            // check transition self.check_tr(ag, tr)
            ag.exec(evt);
            // impulse transition self.impulse_tr(bes, ag, time)
            ag.update(time);
        }
    }

    @Override
    public JSONObject toJSON() {
        return null;
    }
}
