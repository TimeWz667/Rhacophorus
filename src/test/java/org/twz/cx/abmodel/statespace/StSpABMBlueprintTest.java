package org.twz.cx.abmodel.statespace;

import org.junit.Before;
import org.junit.Test;
import org.twz.cx.Director;
import org.twz.cx.mcore.Simulator;
import org.twz.dag.ParameterCore;
import org.twz.statespace.AbsStateSpace;

import java.util.HashMap;
import java.util.Map;

public class StSpABMBlueprintTest {
    private Director Da;
    private StSpABMBlueprint Bp;
    private StSpY0 Y0;

    @Before
    public void setUp() {
        Da = new Director();
        Da.loadBayesNet("src/test/resources/script/pCloseSIR.txt");
        Da.loadStateSpace("src/test/resources/script/CloseSIR.txt");

        Bp = new StSpABMBlueprint("SIR");
        Bp.setAgent("Ag", "agent", "CloseSIR");

        Map<String, Object> args = new HashMap<>();

        args.put("s_src", "Inf");
        args.put("t_tar", "Infect");
        Bp.addBehaviour("FOI", "FDShock", args);
        Bp.setObservations(new String[]{"Sus", "Inf", "Rec"}, new String[]{"Infect"}, new String[]{"FOI"});

        Y0 = new StSpY0();
        Y0.append(950, "Sus");
        Y0.append(50, "Inf");
    }

    @Test
    public void simulationPcDc() {
        Map<String, Object> args = new HashMap<>();

        ParameterCore PC = Da.getBayesNet("pCloseSIR")
                .toSimulationCore(Bp.getParameterHierarchy(Da.getStateSpace("CloseSIR")), true)
                .generate("Test");
        AbsStateSpace DC = Da.generateDCore("CloseSIR", PC.genPrototype("agent"));

        args.put("pc", PC);
        args.put("dc", DC);

        run(args);
    }

    @Test
    public void simulationDaBN() {
        Map<String, Object> args = new HashMap<>();

        args.put("bn", "pCloseSIR");
        args.put("da", Da);

        run(args);
    }

    public void run(Map<String, Object> args) {
        StSpABModel Model = Bp.generate("Test", args);

        Simulator Simu = new Simulator(Model);
        Simu.addLogPath("FDShock.txt");

        Simu.simulate(Y0, 0, 10, 1);
        Model.getObserver().getObservations().print();
    }
}