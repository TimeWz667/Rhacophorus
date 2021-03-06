package org.twz.cx.ebmodel;

import org.junit.Before;
import org.junit.Test;
import org.twz.cx.mcore.Simulator;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by TimeWz on 14/08/2018.
 */
public class ODEquationsTest {

    private EquationBasedModel EBM;

    @Before
    public void setUp() {
        Map<String, Double> pars = new HashMap<>();
        pars.put("beta", 1.5);
        pars.put("gamma", 0.5);

        ODEFunction Fn = (t, y0, y1, parameters, attributes) -> {
            double beta = parameters.getDouble("beta"), gamma = parameters.getDouble("gamma");
            double n = y0[0] + y0[1] + y0[2];

            y1[0] = - beta * y0[0] * y0[1] / n;
            y1[1] = beta * y0[0] * y0[1] / n - gamma * y0[1];
            y1[2] = gamma * y0[1];
        };

        ODEquations Eq = new ODEquations("SIR", Fn, new String[]{"S", "I", "R"}, 1, pars);
        EBM = new EquationBasedModel("SIR", Eq, pars);
        EBM.addObservingStock("S");
        EBM.addObservingStock("I");
        EBM.addObservingStock("R");

        EBM.addObservingStockFunction((tab, ti, ys, pc, x) -> {
            double beta = pc.getDouble("beta");
            double n = ys[0] + ys[1] + ys[2];
            tab.put("FOI", beta * ys[0] * ys[1] / n);
        });
    }

    @Test
    public void simulation() throws Exception {
        Simulator Simu = new Simulator(EBM);
        Simu.onLog("log/EBM.txt");
        EBMY0 Y0 = new EBMY0();
        Y0.append("{'y': 'S', 'n': 990}");
        Y0.append("{'y': 'I', 'n': 10}");

        Simu.simulate(Y0, 0, 15, 1);
        EBM.getObserver().getObservations().println();
    }

}