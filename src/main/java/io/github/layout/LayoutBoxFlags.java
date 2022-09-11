package io.github.layout;

/**
 *  Container flags to pass to setContainer()
 */
public final class LayoutBoxFlags {

    // flex-direction (bit 0+1)

    /**
     * left to right
     */
    public static final int LAY_ROW = 0x002;
    /**
     * top to bottom
     */
    public static final int LAY_COLUMN = 0x003;

    // model (bit 1)

    /**
     * free layout
     */
    public static final int LAY_LAYOUT = 0x000;
    /**
     * flex model
     */
    public static final int LAY_FLEX = 0x002;

    // flex-wrap (bit 2)

    /**
     * single-line
     */
    public static final int LAY_NOWRAP = 0x000;
    /**
     * multi-line, wrap left to right
     */
    public static final int LAY_WRAP = 0x004;


    /**
     * justify-content (start, end, center, space-between)
     * at start of row/column
     */
    public static final int LAY_START = 0x008;
    /**
     * at center of row/column
     */
    public static final int LAY_MIDDLE = 0x000;
    /**
     * at end of row/column
     */
    public static final int LAY_END = 0x010;
    /**
     * insert spacing to stretch across whole row/column
     */
    public static final int LAY_JUSTIFY = 0x018;

    // align-items
    // can be implemented by putting a flex container in a layout container,
    // then using LAY_TOP, LAY_BOTTOM, LAY_VFILL, LAY_VCENTER, etc.
    // FILL is equivalent to stretch/grow

    // align-content (start, end, center, stretch)
    // can be implemented by putting a flex container in a layout container,
    // then using LAY_TOP, LAY_BOTTOM, LAY_VFILL, LAY_VCENTER, etc.
    // FILL is equivalent to stretch; space-between is not supported.
}
