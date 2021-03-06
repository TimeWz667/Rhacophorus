package org.twz.dataframe.timeseries;

import org.twz.exception.TimeseriesException;

import java.util.ArrayList;
import java.util.List;

public class Series<T> extends ArrayList<T> {
    private String Name;

    public Series(String name, List<T> vs) {
        super(vs);
        Name = name;
    }

    public String getName() {
        return Name;
    }

    public String getDataType() {
        return get(0).getClass().getSimpleName();
    }

    public String getString(int i) {
        return get(i).toString();
    }

    public double getDouble(int i) throws TimeseriesException {
        Object e = get(i);
        if (e instanceof Number) {
            return ((Number) e).doubleValue();
        }

        try {
            return Double.parseDouble((String) e);
        } catch (Exception ex) {
            throw new TimeseriesException("This series cannot be transformed to double data");
        }
    }

    public int getInt(int i) throws TimeseriesException {
        Object e = get(i);
        if (e instanceof Number) {
            return ((Number) e).intValue();
        }

        try {
            return Integer.parseInt((String) e);
        } catch (Exception ex) {
            throw new TimeseriesException("This series cannot be transformed to double data");
        }
    }

    public T interpolate(double t, double t0, T v0, double t1, T v1) {
        return v1;
    }

}
