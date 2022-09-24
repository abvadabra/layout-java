package io.github.layout;

import org.intellij.lang.annotations.MagicConstant;

@SuppressWarnings("unused")
public final class LayoutContext {

    Layout.LayoutItem[] items = new Layout.LayoutItem[0];
    LayoutRect[] rects = new LayoutRect[0];
    int capacity;
    int count;

    public LayoutContext() {}

    /**
     * @see Layout#layReserveItemsCapacity
     */
    public void reserveItemsCapacity(int count) {
        Layout.layReserveItemsCapacity(this, count);
    }

    /**
     * @see Layout#layResetContext
     */
    public void resetContext() {
        Layout.layResetContext(this);
    }

    /**
     * @see Layout#layRunContext
     */
    public void runContext() {
        Layout.layRunContext(this);
    }

    /**
     * @see Layout#layRunItem
     */
    public void runItem(int item) {
        Layout.layRunItem(this, item);
    }

    /**
     * @see Layout#layClearItemBreak
     */
    public void clearItemBreak(int item) {
        Layout.layClearItemBreak(this, item);
    }

    /**
     * @see Layout#layItemsCount
     */
    public int itemsCount() {
        return Layout.layItemsCount(this);
    }

    /**
     * @see Layout#layItemsCapacity
     */
    public int itemsCapacity() {
        return Layout.layItemsCapacity(this);
    }

    /**
     * @see Layout#layItem
     */
    public int item() {
        return Layout.layItem(this);
    }

    /**
     * @see Layout#layLastChild
     */
    public int lastChild(int parent) {
        return Layout.layLastChild(this, parent);
    }

    /**
     * @see Layout#layInsert
     */
    public void insert(int parent, int child) {
        Layout.layInsert(this, parent, child);
    }

    /**
     * @see Layout#layAppend
     */
    public void append(int earlier, int later) {
        Layout.layAppend(this, earlier, later);
    }

    /**
     * @see Layout#layPush
     */
    public void push(int parent, int newChild) {
        Layout.layPush(this, parent, newChild);
    }

    /**
     * @see Layout#laySetGrow
     */
    public void setGrow(int item, float grow) {
        Layout.laySetGrow(this, item, grow);
    }

    /**
     * @see Layout#layGetGrow
     */
    public float getGrow(int item) {
        return Layout.layGetGrow(this, item);
    }

    /**
     * @see Layout#laySetSize
     */
    public void setSize(int item, float width, float height) {
        Layout.laySetSize(this, item, width, height);
    }

    /**
     * @see Layout#layGetSizeX
     */
    public float getSizeX(int item) {
        return Layout.layGetSizeX(this, item);
    }

    /**
     * @see Layout#layGetSizeY
     */
    public float getSizeY(int item) {
        return Layout.layGetSizeY(this, item);
    }

    /**
     * @see Layout#layGetSizeXY
     */
    public float[] getSizeXY(int item, float[] dst) {
        return Layout.layGetSizeXY(this, item, dst);
    }

    /**
     * @see Layout#laySetBehave
     */
    public void setBehave(int item, @MagicConstant(flagsFromClass = LayoutFlags.class) int flags) {
        Layout.laySetBehave(this, item, flags);
    }

    /**
     * @see Layout#laySetContain
     */
    public void setContain(int item, @MagicConstant(flagsFromClass = LayoutBoxFlags.class) int flags) {
        Layout.laySetContain(this, item, flags);
    }

    /**
     * @see Layout#laySetMargins
     */
    public void setMargins(int item, float left, float top, float right, float bottom) {
        Layout.laySetMargins(this, item, left, top, right, bottom);
    }

    /**
     * @see Layout#layGetMarginsLTRB
     */
    public float[] getMarginsLTRB(int item, float[] dst) {
        return Layout.layGetMarginsLTRB(this, item, dst);
    }

    /**
     * @see Layout#layFirstChild
     */
    public int firstChild(int id) {
        return Layout.layFirstChild(this, id);
    }

    /**
     * @see Layout#layNextSibling
     */
    public int nextSibling(int id) {
        return Layout.layNextSibling(this, id);
    }

    /**
     * @see Layout#layGetFlags
     */
    public int getFlags(int id) {
        return Layout.layGetFlags(this, id);
    }

    /**
     * @see Layout#layGetRectX
     */
    public float getRectX(int id) {
        return Layout.layGetRectX(this, id);
    }

    /**
     * @see Layout#layGetRectY
     */
    public float getRectY(int id) {
        return Layout.layGetRectY(this, id);
    }

    /**
     * @see Layout#layGetRectWidth
     */
    public float getRectWidth(int id) {
        return Layout.layGetRectWidth(this, id);
    }

    /**
     * @see Layout#layGetRectHeight
     */
    public float getRectHeight(int id) {
        return Layout.layGetRectHeight(this, id);
    }

    /**
     * @see Layout#layGetRect
     */
    public float[] getRect(int id, float[] dst) {
        return Layout.layGetRect(this, id, dst);
    }

    /**
     * @see Layout#layCalcSize
     */
    public void calcSize(int item, int dim) {
        Layout.layCalcSize(this, item, dim);
    }

    /**
     * @see Layout#layArrange
     */
    public void arrange(int item, int dim) {
        Layout.layArrange(this, item, dim);
    }

    public static class LayoutRect {
        public float x,y,w,h;

        public float get(int i) {
            switch (i) {
                case 0: return x;
                case 1: return y;
                case 2: return w;
                case 3: return h;
                default: throw new IllegalArgumentException("Invalid index while reading layout rect component, should be [0;3], given: " + i);
            }
        }

        public void set(int i, float value) {
            switch (i) {
                case 0: x = value; break;
                case 1: y = value; break;
                case 2: w = value; break;
                case 3: h = value; break;
                default: throw new IllegalArgumentException("Invalid index while setting layout rect component, should be [0;3], given: " + i);
            }
        }
    }

}
