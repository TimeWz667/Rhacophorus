package org.twz.factory;


import org.json.JSONException;
import org.twz.cx.element.Ticker.AbsTicker;
import org.twz.cx.element.Ticker.AppointmentTicker;
import org.twz.cx.element.Ticker.TickerFactory;
import org.json.JSONObject;
import org.junit.Test;

/**
 *
 * Created by TimeWz on 2017/10/13.
 */
public class TickerFactoryTest {


    @Test
    public void createScheduleTicker() throws JSONException {
        AbsTicker tick = TickerFactory.create(new JSONObject("{'Type': 'Schedule', 'Args': {'ts':[1,3,6], 't':0}}"));
        System.out.println("Ticker:");
        System.out.println(tick);

        double ti=0;
        tick.initialise(ti);
        while (ti < 10) {
            tick.update(ti);
            ti = tick.getNext();
            System.out.println(String.format("At: %.2f", ti));
        }
        System.out.println(tick);

    }

    @Test
    public void createStepTicker() throws JSONException {
        AbsTicker tick = TickerFactory.create(new JSONObject("{'Type': 'Step', 'Args': {'dt':0.7}}"));
        System.out.println("Ticker:");
        System.out.println(tick);

        double ti=0;
        tick.initialise(ti);
        while (ti < 10) {
            tick.update(ti);
            ti = tick.getNext();
            System.out.println(String.format("At: %.2f", ti));
        }
        System.out.println(tick);

    }

    @Test
    public void createAppointmentTicker() throws JSONException {
        AbsTicker tick = TickerFactory.create(new JSONObject("{'Type': 'Appointment', 'Args': {'queue':[]}}"));
        ((AppointmentTicker) tick).makeAnAppointment(1);
        System.out.println("Ticker:");
        System.out.println(tick);

        double ti=0;
        tick.initialise(ti);
        while (ti < 10) {
            tick.update(ti);
            ti = tick.getNext();
            ((AppointmentTicker) tick).makeAnAppointment(ti+2.4);
            System.out.println(String.format("At: %.2f", ti));
        }
        System.out.println(tick);

    }

}