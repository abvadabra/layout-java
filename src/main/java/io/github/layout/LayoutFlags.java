package io.github.layout;

/**
 * Child layout flags to pass to lay_set_behave()
 */
public final class LayoutFlags {

    // attachments (bit 5-8)
    // fully valid when parent uses LAY_LAYOUT model
    // partially valid when in LAY_FLEX model

    /**
     * Anchor to left item or left side of parent
     */
    public static final int LAY_LEFT = 0x020;
    /**
     * Anchor to top item or top side of parent
     */
    public static final int LAY_TOP = 0x040;
    /**
     * Anchor to right item or right side of parent
     */
    public static final int LAY_RIGHT = 0x080;
    /**
     * Anchor to bottom item or bottom side of parent
     */
    public static final int LAY_BOTTOM = 0x100;
    /**
     * Anchor to both left and right item or parent borders
     */
    public static final int LAY_HFILL = 0x0a0;
    /**
     * Anchor to both top and bottom item or parent borders
     */
    public static final int LAY_VFILL = 0x140;
    /**
     * Size which was set for the item will be treated as minimal width
     */
    public static final int LAY_HGROW = 0x4000;
    /**
     * Size which was set for the item will be treated as minimal height
     */
    public static final int LAY_VGROW = 0x8000;
    /**
     * Aenter horizontally, with left margin as offset
     */
    public static final int LAY_HCENTER = 0x000;
    /**
     * Center vertically, with top margin as offset
     */
    public static final int LAY_VCENTER = 0x000;
    /**
     * Center in both directions, with left/top margin as offset
     */
    public static final int LAY_CENTER = 0x000;
    /**
     * Anchor to all four directions
     */
    public static final int LAY_FILL = 0x1e0;
    /**
     * When in a wrapping container, put this element on a new line. Wrapping
     * layout code auto-inserts LAY_BREAK flags as needed. See GitHub issues for
     * TODO related to this.
     * Drawing routines can read this via item pointers as needed after
     * performing layout calculations.
     */
    public static final int LAY_BREAK = 0x200;
}
