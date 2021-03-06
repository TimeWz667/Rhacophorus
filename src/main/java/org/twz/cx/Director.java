package org.twz.cx;

import org.json.JSONException;
import org.twz.cx.abmodel.statespace.StSpABMBlueprint;
import org.twz.cx.ebmodel.ODEEBMBlueprint;
import org.twz.cx.mcore.AbsSimModel;
import org.twz.cx.mcore.IModelBlueprint;
import org.twz.cx.mcore.IY0;
import org.twz.cx.mcore.LeafY0;
import org.twz.dag.BayesNet;
import org.twz.dag.Parameters;
import org.twz.dag.NodeSet;
import org.twz.datafunction.AbsDataFunction;
import org.twz.datafunction.DataCentre;
import org.twz.exception.ValidationException;
import org.twz.statespace.AbsStateSpace;
import org.twz.statespace.StateSpaceFactory;
import org.twz.statespace.IStateSpaceBlueprint;
import org.twz.statespace.ctbn.CTBNBlueprint;
import org.twz.statespace.ctmc.CTMCBlueprint;
import org.twz.cx.multimodel.ModelLayout;
import org.json.JSONObject;
import org.twz.exception.ScriptException;
import org.twz.io.IO;
import org.twz.util.LogFormatter;
import org.twz.util.ILogable;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.*;

/**
 *
 * Created by TimeWz on 2017/6/16.
 */
public class Director implements ILogable {
    private DataCentre DC;
    private Map<String, BayesNet> BNs;
    private Map<String, IStateSpaceBlueprint> DCores;
    private Map<String, IModelBlueprint> MCores;
    private Map<String, ModelLayout> Layouts;
    private Logger Log;

    public Director() {
        DC = new DataCentre();
        BNs = new HashMap<>();
        DCores = new HashMap<>();
        MCores = new HashMap<>();
        Layouts = new HashMap<>();
        Log = Logger.getLogger("Main");
        Log.addHandler(new ConsoleHandler());
    }

    public void addDataFunction(AbsDataFunction df) {
        DC.put(df);
    }

    private void addBayesNet(BayesNet bn) {
        if (BNs.putIfAbsent(bn.getName(), bn) != null) {
            Log.info("New BayesNet " + bn.getName() + " added");
        }
    }

    public void readBayesNet(String script) {
        try {
            addBayesNet(BayesNet.buildFromScript(script));
        } catch (ScriptException e) {
            error("Invalidated script");
        }
    }

    public void readBayesNet(JSONObject js) {
        try {
            addBayesNet(new BayesNet(js));
        } catch (ScriptException | JSONException e) {
            error("Invalidated script");
        }
    }

    public void loadBayesNet(String path) throws JSONException {
        if (path.endsWith(".json")) {
            readBayesNet(IO.loadJSON(path));
        } else {
            readBayesNet(IO.loadText(path));
        }
    }

    public BayesNet createBayesNet(String name) {
        assert !BNs.containsKey(name);
        BayesNet BN = new BayesNet(name);
        addBayesNet(BN);
        return BN;
    }

    public void listBayesNets() {
        System.out.println(BNs.keySet());
    }

    public BayesNet getBayesNet(String name) {
        BayesNet bn = BNs.get(name);
        bn.bindDataCentre(DC);
        return bn;
    }

    public void joinBayesNets(String main, String sub, String newName) {
        assert BNs.containsKey(main);
        assert BNs.containsKey(sub);
        addBayesNet(BayesNet.merge(newName, BNs.get(main), BNs.get(sub)));
    }

    public void removeBayesNet(String name) {
        if (!BNs.containsKey(name)) {
            Log.info("BayesNet " + name + "was not defined; Nothing removed");
            return;
        }
        BNs.remove(name);
    }

    private void addStateSpace(IStateSpaceBlueprint dc) {
        if(DCores.putIfAbsent(dc.getName(), dc)!=null) {
            Log.info("New state space model " + dc.getName() + " added");
        }
    }

    public void loadStateSpace(String path) throws JSONException {
        if (path.endsWith(".json")) {
            readStateSpace(IO.loadJSON(path));
        } else {
            readStateSpace(IO.loadText(path));
        }
    }

    public void readStateSpace(String script) {
        try {
            addStateSpace(StateSpaceFactory.createFromScripts(script));
        } catch (ScriptException | JSONException e) {
            warning("Invalidated script");
        }
    }

    public void readStateSpace(JSONObject js) {
        try {
            addStateSpace(StateSpaceFactory.createFromJSON(js));
        } catch (JSONException e) {
            warning("Invalidated format");
        }
    }

    public void listStateSpace() {
        System.out.println(DCores.keySet().toString());
    }

    public IStateSpaceBlueprint getStateSpace(String name) {
        return DCores.get(name);
    }

    public IStateSpaceBlueprint createStateSpace(String name, String type) {
        assert !DCores.containsKey(name);
        IStateSpaceBlueprint ss;
        switch (type) {
            case "CTBN":
                ss = new CTBNBlueprint(name);
                break;
            case "CTMC":
                ss = new CTMCBlueprint(name);
                break;
            default:
                warning("Unknown type of state space");
                return null;
        }

        addStateSpace(ss);
        return ss;
    }

    private void addSimModel(IModelBlueprint mc) {
        MCores.putIfAbsent(mc.getName(), mc);
    }

    public void loadSimModel(String path) throws JSONException {
        restoreSimModel(IO.loadJSON(path));
    }

    public void restoreSimModel(JSONObject js) {
        //try {
        //    addDCore(name, MCoreBuilder.buildFromJSON(js));
        //} catch (ScriptException e) {
        //    e.printStackTrace();
        //}
        // todo
    }

    public void listSimModels() {
        System.out.println(MCores.keySet().toString());
    }

    public IModelBlueprint getSimModel(String name) {
        return MCores.get(name);
    }

    public IModelBlueprint createSimModel(String name, String type) {
        assert !MCores.containsKey(name);
        IModelBlueprint mbp;
        switch (type) {
            case "StSpABM":
                mbp = new StSpABMBlueprint(name);
                break;
            case "ODEEBM":
                mbp = new ODEEBMBlueprint(name);
                break;
            default:
                warning("Unknown type of simulation model");
                return null;
        }

        addSimModel(mbp);
        return mbp;
    }

    private void addModelLayout(ModelLayout ml) {
        Layouts.putIfAbsent(ml.getName(), ml);
    }

    public ModelLayout createModelLayout(String name) {
        assert !Layouts.containsKey(name);
        ModelLayout layout = new ModelLayout(name);
        addModelLayout(layout);
        return layout;
    }

    public NodeSet getParameterHierarchy(String name) {
        if (Layouts.containsKey(name)) {
            return Layouts.get(name).getParameterHierarchy(this);
        } else if (MCores.containsKey(name)) {
            return MCores.get(name).getParameterHierarchy(this);
        } else {
            return new NodeSet(name, new String[0]);
        }
    }

    public Parameters generatePCore(String name, String bn) throws ValidationException {
        return getBayesNet(bn).toParameterModel().generate(name);
    }

    public AbsStateSpace generateDCore(String dc, String pc) throws ValidationException {
        return generateDCore(dc, generatePCore(dc, pc));
    }

    public AbsStateSpace generateDCore(String dc, Parameters pc) {
        IStateSpaceBlueprint bp = getStateSpace(dc);
        if (bp.isCompatible(pc)) {
            return bp.generateModel(pc);
        } else {
            warning("The parameter model is not compatible with the state-space");
        }
        return null;
    }

    public AbsSimModel generateMCore(String name, String type, String bn) throws ValidationException {
        Map<String, Object> args = new HashMap<>();
        args.put("bn", bn);
        args.put("da", this);
        return MCores.get(type).generate(name, args);
    }

    public AbsSimModel generateMCore(String name, String type, Parameters pc) throws ValidationException {
        Map<String, Object> args = new HashMap<>();
        args.put("pc", pc);
        args.put("da", this);
        return MCores.get(type).generate(name, args);
    }

    public AbsSimModel generateModel(String name, String type, String bn) throws ValidationException {
        if (Layouts.containsKey(type)) {
            ModelLayout layout = Layouts.get(type);
            NodeSet ns = layout.getParameterHierarchy(this);
            Parameters pc = getBayesNet(bn).toParameterModel(ns).generate(name);
            return layout.generate(name, this, pc);
        } else {
            return generateMCore(name, type, bn);
        }
    }

    public AbsSimModel generateModel(String name, String type, String bn, Map<String, Double> exo) throws ValidationException {
        if (Layouts.containsKey(type)) {
            ModelLayout layout = Layouts.get(type);
            NodeSet ns = layout.getParameterHierarchy(this);
            Parameters pc = getBayesNet(bn).toParameterModel(ns).generate(name, exo);
            return layout.generate(name, this, pc);
        } else {
            return generateMCore(name, type, bn);
        }
    }

    public AbsSimModel generateModel(String name, String type, Parameters pc) throws ValidationException {
        if (Layouts.containsKey(type)) {
            ModelLayout layout = Layouts.get(type);
            return layout.generate(name, this, pc);
        } else {
            return generateMCore(name, type, pc);
        }
    }

    public IY0 generateModelY0(String type) {
        if (Layouts.containsKey(type)) {
            return Layouts.get(type).getY0s();
        } else {
            return new LeafY0();
        }
    }

    public void onLog(Logger log) {
        Log = log;
    }

    public void onLog() {
        if (Log == null) {
            Log = Logger.getLogger(this.getClass().getSimpleName());
            Log.setLevel(Level.INFO);
            Handler handler = new ConsoleHandler();
            handler.setFormatter(new LogFormatter());
            Log.addHandler(handler);
        }
    }

    public void offLog() {
        Log = null;
    }

    public void info(String msg) {
        if (Log != null) Log.info(msg);
    }

    public void warning(String msg) {
        if (Log != null) Log.warning(msg);
    }

    public void error(String msg) {
        if (Log != null) Log.severe(msg);
    }
}
