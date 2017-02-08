package heigit.ors.util;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.util.GeometricShapeFactory;

public class FrechetDistance {

        static double delta = 0.01;
        public double[][] a, b, c, d;
        Point2D[] pl1;
        Point2D[] pl2;
        int pLength;
        int qLength;
        static GeometricShapeFactory gsf = new GeometricShapeFactory();
        static GeometryFactory gf = new GeometryFactory();

        /**
         * P and Q are two polylines
         * 
         * @param P
         * @param Q
         */
        public FrechetDistance(Point2D[] P, Point2D[] Q) {
                pl1 = P;
                pl2 = Q;
                pLength = P.length;
                qLength = Q.length;
                int p = P.length;
                int q = Q.length;
                a = new double[p][q];
                b = new double[p][q];
                c = new double[p][q];
                d = new double[p][q];
        }

        /**
         * 
         * @param P
         * @param Q
         * @param epsilon
         * @return true if the Frechet distance is <= epsilon
         */
        public boolean isFrechet(double epsilon) {
                // check first pair of segments
                if (Line2D.ptSegDist(pl1[0].getX(), pl1[0].getY(), pl1[1].getX(),
                                pl1[1].getY(), pl2[0].getX(), pl2[0].getY()) > epsilon
                                && Line2D.ptSegDist(pl1[0].getX(), pl1[0].getY(),
                                                pl1[1].getX(), pl1[1].getY(), pl2[1].getX(),
                                                pl2[1].getY()) > epsilon) {
                        return false;
                }
                if (Line2D.ptSegDist(pl2[0].getX(), pl2[0].getY(), pl2[1].getX(),
                                pl2[1].getY(), pl1[0].getX(), pl1[0].getY()) > epsilon
                                && Line2D.ptSegDist(pl2[0].getX(), pl2[0].getY(),
                                                pl2[1].getX(), pl1[2].getY(), pl1[1].getX(),
                                                pl1[1].getY()) > epsilon) {
                        return false;
                }

                // check last pair of segments
                if (Line2D.ptSegDist(pl1[pLength - 2].getX(), pl1[pLength - 2].getY(),
                                pl1[pLength - 1].getX(), pl1[pLength - 1].getY(),
                                pl2[qLength - 1].getX(), pl2[qLength - 1].getY()) > epsilon
                                && Line2D.ptSegDist(pl1[pLength - 2].getX(),
                                                pl1[pLength - 2].getY(), pl1[pLength - 1].getX(),
                                                pl1[pLength - 1].getY(), pl2[qLength - 2].getX(),
                                                pl2[qLength - 2].getY()) > epsilon) {
                        return false;
                }
                if (Line2D.ptSegDist(pl2[qLength - 2].getX(), pl2[qLength - 2].getY(),
                                pl2[qLength - 1].getX(), pl2[qLength - 1].getY(),
                                pl1[pLength - 2].getX(), pl1[pLength - 2].getY()) > epsilon
                                && Line2D.ptSegDist(pl2[qLength - 2].getX(),
                                                pl2[qLength - 2].getY(), pl2[qLength - 1].getX(),
                                                pl2[qLength - 1].getY(), pl1[pLength - 1].getX(),
                                                pl1[pLength - 1].getY()) > epsilon) {
                        return false;
                }

                LineString tempLsQ;
                LineString tempLsP;
                Coordinate p1, p2, q1, q2;
                Polygon tempCircle;
                Geometry tempGeom;

                for (int i = 0; i < pl1.length - 1; i++) {
                        for (int j = 0; j < pl2.length - 1; j++) {

                                p1 = new Coordinate(pl1[i].getX(), pl1[i].getY());
                                p2 = new Coordinate(pl1[i + 1].getX(), pl1[i + 1].getY());
                                q1 = new Coordinate(pl2[j].getX(), pl2[j].getY());
                                q2 = new Coordinate(pl2[j + 1].getX(), pl2[j + 1].getY());
                                
                                if (Line2D.ptSegDist(pl2[j].getX(), pl2[j].getY(),
                                                pl2[j + 1].getX(), pl2[j + 1].getY(), pl1[i].getX(),
                                                pl1[i].getY()) > epsilon) {
                                        a[i][j] = b[i][j] = -1;
                                } else {
                                        // make line string out of j's two end points
                                        
                                        tempLsQ = gf.createLineString(new Coordinate[] { q1, q2 });

                                        // make circle with i's first end point
                                        
                                        gsf.setCentre(p1);
                                        gsf.setSize(2 * epsilon);
                                        tempCircle = gsf.createCircle();

                                        if (tempCircle.contains(tempLsQ)) {
                                                a[i][j] = 0;
                                                b[i][j] = 1;
                                        } else {
                                                // collapse the circle and the line
                                                tempGeom = tempCircle.intersection(tempLsQ);
                                                int numCoords = tempGeom.getCoordinates().length;

                                                if (numCoords == 2) {
                                                        // 2 points
                                                        Coordinate[] intersections = tempGeom
                                                                        .getCoordinates();
                                                        a[i][j] = getProportion(intersections[0], tempLsQ);
                                                        b[i][j] = getProportion(intersections[1], tempLsQ);
                                                } else if (numCoords == 1) {
                                                        // 1 point
                                                        Coordinate intersection = tempGeom.getCoordinate();
                                                        if (p1.distance(q1) < p1
                                                                        .distance(q2)) {
                                                                a[i][j] = 0;
                                                                b[i][j] = getProportion(intersection, tempLsQ);
                                                        } else {
                                                                a[i][j] = getProportion(intersection, tempLsQ);
                                                                b[i][j] = 1;
                                                        }
                                                }
                                        }
                                }
                                
                                // fill up c_ij and d_ij
                                double val1 = Line2D.ptSegDist(pl1[i].getX(), pl1[i].getY(),
                                                pl1[i + 1].getX(), pl1[i + 1].getY(), pl2[j].getX(),
                                                pl2[j].getY()) ;
                                
                                if (val1 > epsilon) {
                                        c[i][j] = d[i][j] = -1;
                                }else{
                                        tempLsP = gf.createLineString(new Coordinate[] { p1, p2 });
                                        gsf.setCentre(q1);
                                        gsf.setSize(2 * epsilon+delta);
                                        tempCircle = gsf.createCircle();
                                        if (tempCircle.contains(tempLsP)) {
                                                c[i][j] = 0;
                                                d[i][j] = 1;
                                        } else {
                                                // collapse the circle and the line
                                                tempGeom = tempCircle.intersection(tempLsP);
                
                                                int numCoords = tempGeom.getCoordinates().length;
                                                if (numCoords == 1) {
                                                        //1 point
                                                        Coordinate intersect = tempGeom.getCoordinate();
                                                        if (q1.distance(p1) < q1.distance(p2)) {
                                                                c[i][j] = 0;
                                                                d[i][j] = getProportion(intersect, tempLsP);
                                                        } else {
                                                                c[i][j] = getProportion(intersect, tempLsP);
                                                                d[i][j] = 1;
                                                        }
                                                } else {
                                                        // 2 points
//                                                      System.out.println("i, j, eps: "+i+","+j+", "+epsilon);
//                                                      System.out.println("tempCircle center:"+tempCircle.getCentroid());
//                                                      System.out.println("tempCircle extent: "+tempCircle.toText());
//                                                      System.out.println(tempGeom.toString());
                                                        Coordinate[] intersections = ((LineString) tempGeom)
                                                                        .getCoordinates();
                                                        c[i][j] = getProportion(intersections[0], tempLsP);
                                                        d[i][j] = getProportion(intersections[1], tempLsP);
                                                }
                                        }
                                }
                        }
                }

                // determine B^R_i,1
                boolean flag = true;
                for (int i = 0; i < pl1.length; i++) {
                        if (flag && c[i][0] == -1 && d[i][0] == -1) {
                                flag = false;
                        } else if (!flag) {
                                c[i][0] = d[i][0] = -1;
                        }
                }

                flag = true;
                // determine L^R_1,j
                for (int j = 1; j < pl2.length; j++) {
                        if (flag && a[0][j] == -1 && b[0][j] == -1) {
                                flag = false;
                        } else if (!flag) {
                                a[0][j] = b[0][j] = -1;
                        }
                }

                // TODO: the complicated loop to compute L^R_(i+1),j and B^R_i,(j+1)
                boolean retVal = true;

                // cannot enter the upper right cell
                if (a[pLength - 1][qLength - 1] == -1
                                && b[pLength - 1][qLength - 1] == -1
                                && c[pLength - 1][qLength - 1] == -1
                                && d[pLength - 1][qLength - 1] == -1) {
                        retVal = false;
                }
                return retVal;
        }

        private double getProportion(Coordinate coord, LineString ls) {
                // coord is a point in line ls
                Coordinate[] ends = ls.getCoordinates();
                return coord.distance(ends[0]) / ls.getLength();
        }

        public Double[] computeCriticalValues() {
                ArrayList<Double> list = new ArrayList<Double>();

                // distances between starting and ending points
                list.add(pl1[0].distance(pl2[0]));
                list.add(pl1[pLength - 1].distance(pl2[qLength - 1]));

                // distances between vertices of one curve and edges of the other curve
                for (int i = 0; i < pLength; i++) {
                        for (int j = 0; j < qLength - 1; j++) {
                                double d = Line2D.ptSegDist(pl2[j].getX(), pl2[j].getY(),
                                                pl2[j + 1].getX(), pl2[j + 1].getY(), pl1[i].getX(),
                                                pl1[i].getY());
                                list.add(d);
                        }
                }

                for (int j = 0; j < qLength; j++) {
                        for (int i = 0; i < pLength - 1; i++) {
                                double d = Line2D.ptSegDist(pl1[i].getX(), pl1[i].getY(),
                                                pl1[i + 1].getX(), pl1[i + 1].getY(), pl2[j].getX(),
                                                pl2[j].getY());
                                list.add(d);
                        }
                }

                // convert into coordinate array
                Coordinate[] pCurve = new Coordinate[pl1.length];
                Coordinate[] qCurve = new Coordinate[pl2.length];
                for (int i = 0; i < pLength; i++) {
                        pCurve[i] = new Coordinate(pl1[i].getX(), pl1[i].getY());
                }
                for (int i = 0; i < qLength; i++) {
                        qCurve[i] = new Coordinate(pl2[i].getX(), pl2[i].getY());
                }
                // common distance of two vertices of one curve to the intersection
                // point of their bisector with some edge of the other

                LineString ls;
                LineString temp;
                LineSegment lseg;
                Coordinate c1, midPoint, c2;
                Coordinate intersect = null;
                for (int i = 0; i < pLength - 2; i++) {
                        for (int j = i + 2; j < pLength; j++) {
                                // comp seg between i and j
                                // comp bisector and intersection point with q
                                // comp the distance
                                // ls = new LineString(pCurve[i], pCurve[j]);
                                lseg = new LineSegment(pCurve[i], pCurve[j]);
                                midPoint = lseg.midPoint();
                                double origSlope = getSlope(pCurve[i].x, pCurve[i].y,
                                                pCurve[j].x, pCurve[j].y);
                                double bisectSlope = 0;
                                if (origSlope != Double.MAX_VALUE) {
                                        if (origSlope == 0) {
                                                bisectSlope = Double.MAX_VALUE;
                                        } else {
                                                bisectSlope = -1 / origSlope;
                                        }
                                }

                                // linear func: y-midPoint.y = bisectSlope*(x-midpoint.x)
                                double step = qCurve[0].distance(midPoint);
                                c1 = new Coordinate(midPoint.x - step, bisectSlope * (-step)
                                                + midPoint.y);
                                c2 = new Coordinate(midPoint.x + step, bisectSlope * step
                                                + midPoint.y);
                                // (c1, midPoint, c2) is the bisector linestring of the
                                // linesegment(i,j)
                                ls = gf.createLineString(new Coordinate[] { c1, midPoint, c2 });
                                temp = gf.createLineString(qCurve);
                                if (ls.intersects(temp)) {
                                        intersect = ls.intersection(temp).getCoordinate();
                                }
                                if (intersect != null) {
                                        list.add(intersect.distance(pCurve[i]));
                                }
                        }
                }

                for (int i = 0; i < qLength - 2; i++) {
                        for (int j = i + 2; j < qLength; j++) {
                                lseg = new LineSegment(qCurve[i], qCurve[j]);
                                midPoint = lseg.midPoint();
                                double origSlope = getSlope(qCurve[i].x, qCurve[i].y,
                                                qCurve[j].x, qCurve[j].y);
                                double bisectSlope = 0;
                                if (origSlope != Double.MAX_VALUE) {
                                        if (origSlope == 0) {
                                                bisectSlope = Double.MAX_VALUE;
                                        } else {
                                                bisectSlope = -1 / origSlope;
                                        }
                                }
                                // linear func: y-midPoint.y = bisectSlope*(x-midpoint.x)
                                double step = pCurve[0].distance(midPoint);
                                if (bisectSlope == Double.MAX_VALUE) {
                                        // vertical line
                                        c1 = new Coordinate(midPoint.x, midPoint.y - step);
                                        c2 = new Coordinate(midPoint.x, midPoint.y + step);
                                } else {
                                        c1 = new Coordinate(midPoint.x - step, bisectSlope
                                                        * (-step) + midPoint.y);
                                        c2 = new Coordinate(midPoint.x + step, bisectSlope * step
                                                        + midPoint.y);
                                }
                                ls = gf.createLineString(new Coordinate[] { c1, midPoint, c2 });
                                temp = gf.createLineString(pCurve);

                                // System.out.println(ls.intersects(temp));
                                // System.out.println(ls);
                                if (ls.intersects(temp)) {
                                        // System.out.println(ls.intersection(temp));
                                        intersect = ls.intersection(temp).getCoordinate();

                                }
                                if (intersect != null) {
                                        list.add(intersect.distance(qCurve[i]));
                                }
                        }
                }
                return list.toArray(new Double[list.size()]);
        }

        public double computeFrechetDistance() {
                Double[] cv = computeCriticalValues();
                Arrays.sort(cv);

                int index = binarySearch(cv);
                return cv[index];
        }

        private double getSlope(double x1, double y1, double x2, double y2) {
                if ((x2 - x1) == 0)
                        return Double.MAX_VALUE;
                else
                        return (y2 - y1) / (x2 - x1);

        }

        /**
         * Performs the standard binary search using two comparisons per level.
         * 
         * @return index where item is found, or NOT_FOUND.
         */
        public int binarySearch(Double[] a) {
                int low = 0;
                int high = a.length - 1;
                int mid = 0;

                while (low <= high) {
                        mid = (low + high) / 2;
                        if (!isFrechet(a[mid])) {
                                low = mid + 1;
                        } else {
                                high = mid - 1;
                        }
                }
                return mid;
        }

}
