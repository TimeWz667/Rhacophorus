package org.twz.fit;

import org.json.JSONObject;
import org.twz.dag.BayesianModel;
import org.twz.dag.Gene;
import org.twz.util.ILogable;
import org.twz.util.CxFormatter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.*;

public abstract class AbsFitter implements ILogable {
    protected Map<String, Object> Options;
    private Logger Log;

    public AbsFitter() {
        Options = new HashMap<>();
        Log = null;
    }

    public void onLog(Logger log) {
        Log = log;
    }

    public void onLog() {
        if (Log == null) {
            Log = Logger.getLogger(this.getClass().getSimpleName());
            Log.setUseParentHandlers(false);
            Log.setLevel(Level.INFO);
            Handler handler = new ConsoleHandler();
            handler.setFormatter(new CxFormatter());
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

    public synchronized void setOption(String key, Object value) {
        Options.replace(key, value);
    }

    public Object getOption(String key) {
        return Options.get(key);
    }

    public double getOptionDouble(String key) {
        Object obj = getOption(key);
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        } else {
            return (double) obj;
        }
    }

    public int getOptionInteger(String key) {
        Object obj = getOption(key);
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        } else {
            return (int) obj;
        }
    }

    public String getOptionString(String key) {
        return getOption(key).toString();
    }

    public void printOptions() {
        System.out.println("Fitter: " + getClass().getSimpleName());
        Options.forEach((k, v) -> System.out.println(k + ": " + v.toString()));
    }

    public abstract List<Gene> fit(BayesianModel bm);

    public List<Gene> fit(BayesianModel bm, Map<String, Object> opt) {
        opt.forEach(this::setOption);
        return fit(bm);
    }

    public abstract List<Gene> update(BayesianModel bm);

    public List<Gene> update(BayesianModel bm, Map<String, Object> opt) {
        opt.forEach(this::setOption);
        return update(bm);
    }

    void appendPriorUntil(BayesianModel bm, int n, List<Gene> prior) {
        while(prior.size() < n) {
            Gene gene = bm.samplePrior();
            if (!gene.isPriorEvaluated()) bm.evaluateLogPrior(gene);
            if (!gene.isLikelihoodEvaluated()) bm.evaluateLogLikelihood(gene);
            if (Double.isInfinite(gene.getLogLikelihood())) continue;
            prior.add(gene);
        }
    }

    public abstract JSONObject getGoodnessOfFit(BayesianModel bm);
}
