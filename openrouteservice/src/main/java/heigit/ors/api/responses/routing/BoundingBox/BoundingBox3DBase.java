package heigit.ors.api.responses.routing.BoundingBox;

import com.graphhopper.util.shapes.BBox;

public class BoundingBox3DBase extends BoundingBoxBase {
    protected double minEle;
    protected double maxEle;

    public BoundingBox3DBase(BBox bounding) {
        super(bounding);
        this.maxEle = bounding.maxEle;
        this.minEle = bounding.minEle;
    }

    public double getMinEle() {
        return minEle;
    }

    public double getMaxEle() {
        return maxEle;
    }
}
