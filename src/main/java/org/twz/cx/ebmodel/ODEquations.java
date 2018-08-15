package org.twz.cx.ebmodel;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.apache.commons.math3.ode.nonstiff.DormandPrince853Integrator;
import org.json.JSONObject;

import org.twz.cx.mcore.AbsSimModel;
import org.twz.dag.Gene;

import java.util.Map;


public class ODEquations extends AbsEquations implements FirstOrderDifferentialEquations {

    private FirstOrderIntegrator Integrator;
    private ODEFunction Function;

    public ODEquations(String name, ODEFunction fn, String[] y_names, double dt, Gene parameters) {
        super(name, y_names, parameters, dt);
        Function = fn;
        Integrator = new DormandPrince853Integrator(1.0e-8, 10.0, 1.0e-10, 1.0e-10);
    }

    public ODEquations(String name, ODEFunction fn, String[] y_names, double dt, Map<String, Double> parameters) {
        super(name, y_names, parameters, dt);
        Function = fn;
        Integrator = new DormandPrince853Integrator(1.0e-8, 10.0, 1.0e-10, 1.0e-10);
    }

    public ODEquations(String name, ODEFunction fn, String[] y_names, double dt) {
        super(name, y_names, dt);
        Function = fn;
        Integrator = new DormandPrince853Integrator(1.0e-8, 10.0, 1.0e-10, 1.0e-10);
    }


    @Override
    protected void goTo(double t0, double[] y0, double t1, double[] y1) {
        Integrator.integrate(this, t0, y0, t1, y1);
    }

    @Override
    public void computeDerivatives(double t, double[] y0, double[] y1) throws MaxCountExceededException, DimensionMismatchException {
        Function.call(t, y0, y1, getParameters(), Attributes);
    }

    @Override
    public void shock(double ti, AbsSimModel source, String action, JSONObject value) {
        EquationBasedModel model = (EquationBasedModel) source;
        int n;
        double v0, v1;
        String y;
        JSONObject js;
        switch (action) {
            case "impulse":
                y = value.getString("k");
                v0 = getY(y);
                v1 = value.getDouble("v");
                put(y, v1);
                js = new JSONObject();
                js.put("k", y);
                js.put("v0", v0);
                js.put("v1", v1);
                model.disclose(String.format("change %s from %.4f to %.4f", y, v1, v0), getName(), js);
                break;

            case "add":
                y = (String) value.get("y");
                n = value.has("n")? value.getInt("n"): 1;
                v0 = getY(y);
                v1 = v0 + n;
                setY(y, v1);
                js = new JSONObject();
                js.put("y", y);
                js.put("n", n);
                js.put("v0", v0);
                js.put("v1", v1);
                model.disclose(String.format("add %s by %d", y, n), getName(), js);
                break;

            case "del":
                y = (String) value.get("y");
                n = value.has("n")? value.getInt("n"): 1;
                v0 = getY(y);
                n = Math.min(n, (int) Math.floor(v0));
                v1 = v0 - n;
                setY(y, v1);

                js = new JSONObject();
                js.put("y", y);
                js.put("n", n);
                js.put("v0", v0);
                js.put("v1", v1);
                model.disclose(String.format("del %s by %d", y, n), getName(), js);
        }
    }
}
