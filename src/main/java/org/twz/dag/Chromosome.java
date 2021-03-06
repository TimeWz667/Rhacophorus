package org.twz.dag;

import org.json.JSONException;
import org.json.JSONObject;
import org.twz.IParameters;
import org.twz.io.AdapterJSONObject;
import org.twz.io.IO;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * Created by TimeWz on 2017/4/21.
 */

public class Chromosome implements AdapterJSONObject, IParameters {
    public static Chromosome nullChromosome = new Chromosome() {
        @Override
        public void put(String s, double d) {

        }
    };

    private double LogPriorProb, LogLikelihood;
    private Map<String, Double> Locus;

    public Chromosome(Map<String, Double> locus, double pp) {
        try {
            Locus = new HashMap<>(locus);
        } catch (NullPointerException e) {
            Locus = new HashMap<>();
        }

        LogPriorProb = pp;
        LogLikelihood = Double.NaN;
    }

    public Chromosome(Map<String, Double> locus) {
        this(locus, Double.NaN);
    }

    public Chromosome() {
        this(new HashMap<>(), Double.NaN);
    }

    public Map<String, Double> getLocus() {
        return Locus;
    }

    public double getDouble(String s) {
        return Locus.getOrDefault(s, Double.NaN);
    }

    public void put(String s, double d) {
        Locus.put(s, d);
        LogLikelihood = Double.NaN;
        LogPriorProb = Double.NaN;
    }

    public void impulse(String k, double v) {
        if (has(k)) {
            Locus.put(k, v);
        }
        resetProbability();
    }

    public void impulse(Map<String, Double> values) {
        for (Map.Entry<String, Double> entry : values.entrySet()) {
            if (has(entry.getKey())) {
                Locus.put(entry.getKey(), entry.getValue());
            }
        }
        resetProbability();
    }

    public boolean has(String s) {
        return Locus.containsKey(s);
    }

    public double getLogPriorProb() {
        return LogPriorProb;
    }

    public void setLogPriorProb(double pp) {
        LogPriorProb = pp;
    }

    public void addLogPriorProb(double p) {
        LogPriorProb += p;
    }

    public double getLogLikelihood() {
        return LogLikelihood;
    }

    public void setLogLikelihood(double logLikelihood) {
        LogLikelihood = logLikelihood;
    }

    public double getLogPosterior() {
        return LogLikelihood + LogPriorProb;
    }

    public boolean isPriorEvaluated() {
        return !Double.isNaN(LogPriorProb);
    }

    public boolean isLikelihoodEvaluated() {
        return !Double.isNaN(LogLikelihood);
    }

    public boolean isEvaluated() {
        return isLikelihoodEvaluated() & isPriorEvaluated();
    }

    public void resetProbability() {
        LogLikelihood = Double.NaN;
        LogPriorProb = Double.NaN;
    }

    public int getSize() {
        return Locus.size();
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject js = new JSONObject();
        js.put("Values", Locus);

        if (isLikelihoodEvaluated()) {
            js.put("LogLikelihood", LogLikelihood);
        }
        if (isPriorEvaluated()) {
            js.put("LogPrior", LogPriorProb);
        }
        if (isEvaluated()) {
            js.put("LogPosterior", getLogPosterior());
        }
        return js;
    }

    public String toString() {
        String sb = "{";
        sb += Locus.entrySet().stream()
                .map(e -> e.getKey() + ": " + IO.doubleFormat(e.getValue()))
                .collect(Collectors.joining(", "));

        if (isPriorEvaluated()) {
            sb += ", LogPrior:" + IO.doubleFormat(LogPriorProb);
        }
        if (isLikelihoodEvaluated()) {
            sb += ", LogLikelihood:" + IO.doubleFormat(LogLikelihood);
        }
        if (isEvaluated()) {
            sb += ", LogPosterior:" + IO.doubleFormat(getLogPosterior());
        }
        sb += "}";
        return sb;
    }

    @Override
    public Chromosome clone() {
        Chromosome chromosome;
        try {
            chromosome = (Chromosome) super.clone();
        } catch (CloneNotSupportedException e) {
            chromosome = new Chromosome();
            chromosome.Locus.putAll(Locus);
        }
        if (isLikelihoodEvaluated()) {
            chromosome.setLogLikelihood(getLogLikelihood());
        }
        if (isPriorEvaluated()) {
            chromosome.setLogPriorProb(getLogPriorProb());
        }
        return chromosome;
    }

    @Override
    public Map<String, Double> toData() {
        Map<String, Double> vs = new LinkedHashMap<>(Locus);
        vs.put("LogLikelihood", LogLikelihood);
        vs.put("LogPrior", LogPriorProb);
        vs.put("LogPosterior", getLogPosterior());
        return vs;
    }
}
