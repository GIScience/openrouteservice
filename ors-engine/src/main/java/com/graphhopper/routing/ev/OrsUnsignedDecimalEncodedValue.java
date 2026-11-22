package com.graphhopper.routing.ev;

import com.graphhopper.storage.IntsRef;

/**
 * This class holds a decimal value and stores it as an unsigned integer value via a conversion factor and a maximum number
 * of bits. Unlike GraphHopper's {@link UnsignedDecimalEncodedValue}, the stored 0 is reserved for the default value and
 * every other value is stored shifted by one, so 0.0 stays distinguishable from the default.
 */
public final class OrsUnsignedDecimalEncodedValue extends UnsignedIntEncodedValue implements DecimalEncodedValue {
    private final double factor;
    private final double defaultValue;

    public OrsUnsignedDecimalEncodedValue(String name, int bits, double factor, boolean storeTwoDirections) {
        this(name, bits, factor, -1, storeTwoDirections);
    }

    /**
     * @param name               the key to identify this EncodedValue
     * @param bits               the bits that should be reserved for the storage
     * @param factor             the precision factor, i.e. store = (int) Math.round(value / factor)
     * @param defaultValue       the value that should be returned if the stored value is 0.
     * @param storeTwoDirections true if forward and backward direction of the edge should get two independent values.
     */
    public OrsUnsignedDecimalEncodedValue(String name, int bits, double factor, double defaultValue, boolean storeTwoDirections) {
        super(name, bits, storeTwoDirections);
        this.factor = factor;
        this.defaultValue = defaultValue;
    }

    private int toInt(double val) {
        return (int) Math.round(val / factor);
    }

    @Override
    public void setDecimal(boolean reverse, IntsRef ints, double value) {
        if (!isInitialized())
            throw new IllegalStateException("Call init before usage for EncodedValue " + toString());
        if (value == defaultValue) {
            super.setInt(reverse, ints, 0);
            return;
        }
        if (value > (maxValue - 1) * factor)
            throw new IllegalArgumentException(getName() + " value " + value + " too large for encoding. maxValue:" + getMaxDecimal());
        else if (value < 0)
            throw new IllegalArgumentException("Negative value for " + getName() + " not allowed! " + value);
        else if (Double.isNaN(value))
            throw new IllegalArgumentException("NaN value for " + getName() + " not allowed!");

        super.setInt(reverse, ints, toInt(value) + 1);
    }

    @Override
    public double getDecimal(boolean reverse, IntsRef ref) {
        int value = getInt(reverse, ref);
        return value == 0 ? defaultValue : (value - 1)  * factor;
    }

    @Override
    public double getMaxDecimal() {
        return (maxValue - 1) * factor;
    }

    @SuppressWarnings("java:S1206")
    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        OrsUnsignedDecimalEncodedValue that = (OrsUnsignedDecimalEncodedValue) o;
        return Double.compare(that.factor, factor) == 0;
    }

    @Override
    public int getVersion() {
        return 31 * (31 * super.getVersion() + staticHashCode(factor)) + staticHashCode(defaultValue);
    }
}
