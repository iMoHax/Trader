package ru.trader.model.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.model.SystemModel;

import java.util.ArrayList;
import java.util.List;

public class PositionComputer {
    private final static Logger LOG = LoggerFactory.getLogger(PositionComputer.class);

    private final static double MAX_ERROR = 1E-10;
    private final static double MAX_VALUE = 1000;
    private final static double MIN_VALUE = -1000;

    private final List<SystemModel> marks = new ArrayList<>(6);
    private final List<Double> distances = new ArrayList<>(6);

    public PositionComputer() {
    }

    public void addLandMark(SystemModel system, double distance){
        marks.add(system);
        distances.add(distance);
    }


    public Coordinates compute(){
        return compute(MIN_VALUE, MIN_VALUE, MIN_VALUE);
    }

    public Coordinates compute(final double x, final double y, final double z){
        Coordinates res = new Coordinates(Double.NaN, Double.NaN, Double.NaN);
        Coordinates current = new Coordinates(x, y, z);
        double xmin = MIN_VALUE, xmax = MAX_VALUE;
        double ymin = MIN_VALUE, ymax = MAX_VALUE;
        double zmin = MIN_VALUE, zmax = MAX_VALUE;
        double step=100, mineps = Double.NaN;

        while (true){
            double delta = 0;
            for (int i = 0; i < marks.size(); i++) {
                delta +=  Math.abs(marks.get(i).getDistance(current.x, current.y, current.z) - distances.get(i));
            }
            LOG.trace("coordinates = {}, delta = {}", current, delta);
            if (delta < MAX_ERROR) {
                res = current;
                LOG.debug("Coordinates found {}", res);
                break;
            }
            if (Double.isNaN(mineps) || delta < mineps){
                mineps = delta;
                res = new Coordinates(current);
            }

            current.x += step;
            if (current.x >= xmax){
                current.x = xmin;
                current.y += step;
                if (current.y >= ymax){
                    current.y = ymin;
                    current.z += step;
                    if (current.z >= zmax){
                        if (step < MAX_ERROR){
                            LOG.debug("Coordinates not found, last {}, delta {}", res, mineps);
                            break;
                        } else {
                            xmin = res.x - step*2; xmax = res.x + step*2; current.x = xmin;
                            ymin = res.y - step*2; ymax = res.y + step*2; current.y = ymin;
                            zmin = res.z - step*2; zmax = res.z + step*2; current.z = zmin;
                            step = step/2;
                            LOG.trace("Change step to {}", step);
                            mineps = Double.NaN;
                        }
                    }
                }
            }

        }

        return res;
    }


    public class Coordinates {
        private double x;
        private double y;
        private double z;

        public Coordinates(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Coordinates(Coordinates other) {
            this.x = other.x;
            this.y = other.y;
            this.z = other.z;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double getZ() {
            return z;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("{");
            sb.append("x=").append(x);
            sb.append(", y=").append(y);
            sb.append(", z=").append(z);
            sb.append('}');
            return sb.toString();
        }
    }
}
