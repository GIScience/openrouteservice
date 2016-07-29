/*
 * The Unified Mapping Platform (JUMP) is an extensible, interactive GUI
 * for visualizing and manipulating spatial features with geometry and attributes.
 *
 * Copyright (C) 2003 Vivid Solutions
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * For more information, contact:
 *
 * Vivid Solutions
 * Suite #1A
 * 2328 Government Street
 * Victoria BC  V8T 5G5
 * Canada
 *
 * (250)385-6040
 * www.vividsolutions.com
 */

package org.freeopenls.overlay;

import java.util.Iterator;
import java.util.List;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.precision.EnhancedPrecisionOp;
import com.vividsolutions.jump.feature.*;

/**
 * Class for "Overlay" two FeatureCollecion with/without an Index
 *
 * @author revised & modified by Pascal Neis, neis@geographie.uni-bonn.de
 * @version 1.0 2007-07-25
 * @version 1.1 2008-04-22
 */
public class OverlayEngine {
    private boolean splittingGeometryCollections = true;
    private boolean allowingPolygonsOnly = false;

    /**
     *  Creates a new OverlayEngine.
     */
    public OverlayEngine() {
    }

    /**
    *  Creates the overlay of the two datasets. The attributes from the datasets
    *  will be transferred as specified by the AttributeMapping.
    *
    *@param indexA  IndexFeatureCollection of A ; the first dataset involved in the overlay
    *@param b  the second dataset involved in the overlay
    *@return intersections of all pairs of input features
    */
    public FeatureCollection overlay(IndexedFeatureCollection indexA, FeatureCollection b) {

    	FeatureSchema featschema = new FeatureSchema();
    	featschema.addAttribute("EdgeID", AttributeType.INTEGER);
    	featschema.addAttribute("GEOMETRY", AttributeType.GEOMETRY);
    	
        FeatureDataset overlay = new FeatureDataset(featschema);
        List bFeatures = b.getFeatures();

        for (int i = 0; (i < bFeatures.size()); i++) {
            Feature bFeature = (Feature) bFeatures.get(i);

            for (Iterator j = indexA.query(bFeature.getGeometry().getEnvelopeInternal()).iterator(); j.hasNext();) {
                Feature aFeature = (Feature) j.next();
                addIntersection(aFeature, bFeature, overlay);
            }
        }

        return overlay;
    }

    private void addIntersection(Feature a, Feature b, FeatureCollection overlay) {
        if (!a.getGeometry().getEnvelope().intersects(b.getGeometry().getEnvelope()))
            return;

        Geometry intersection = EnhancedPrecisionOp.intersection(a.getGeometry(), b.getGeometry());

        if ((intersection == null) || intersection.isEmpty())
            return;

        addFeature(intersection, overlay, a, b);
    }

    protected void addFeature(Geometry intersection, FeatureCollection overlay, Feature a, Feature b) {
        if (splittingGeometryCollections && intersection instanceof GeometryCollection) {
            GeometryCollection gc = (GeometryCollection) intersection;

            for (int i = 0; i < gc.getNumGeometries(); i++) {
                addFeature(gc.getGeometryN(i), overlay, a, b);
            }

            return;
        }

        if (allowingPolygonsOnly && !(intersection instanceof Polygon || intersection instanceof MultiPolygon)) {
            return;
        }

        Feature feature = new BasicFeature(overlay.getFeatureSchema());
        feature.setAttribute("EdgeID", a.getAttribute("EdgeID"));
        feature.setGeometry(intersection);
        overlay.add(feature);
    }

    public void setSplittingGeometryCollections(boolean splittingGeometryCollections) {
        this.splittingGeometryCollections = splittingGeometryCollections;
    }

    public void setAllowingPolygonsOnly(boolean allowingPolygonsOnly) {
        this.allowingPolygonsOnly = allowingPolygonsOnly;
    }
}
