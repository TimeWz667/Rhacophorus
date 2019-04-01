package org.twz.prob;

/**
 *
 * Created by TimeWz on 2017/4/17.
 */
public interface IWalkable extends IDistribution {
    double getUpper();
    double getLower();
    double getMean();
    double getStd();
}
