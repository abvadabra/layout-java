package org.layout;

import org.intellij.lang.annotations.MagicConstant;

@SuppressWarnings("unused")
public final class LayoutContext {

    Layout.LayoutItem[] items = new Layout.LayoutItem[0];
    LayoutRect[] rects = new LayoutRect[0];
    int capacity;
    int count;

    public LayoutContext() {}

    /**
     * @see org.layout.Layout#layReserveItemsCapacity
     */
    public void reserveItemsCapacity(int count) {
        Layout.layReserveItemsCapacity(this, count);
    }

    /**
     * @see org.layout.Layout#layResetContext)
     */
    public void resetContext() {
        Layout.layResetContext(this);
    }

    /**
     * @see org.layout.Layout#layRunContext
     */
    public void runContext() {
        Layout.layRunContext(this);
    }

    /**
     * @see org.layout.Layout#layRunItem
     */
    public void runItem(int item) {
        Layout.layRunItem(this, item);
    }

    /**
     * @see org.layout.Layout#layClearItemBreak}
     */
    public void clearItemBreak(int item) {
        Layout.layClearItemBreak(this, item);
    }

    /**
     * @see org.layout.Layout#layItemsCount}
     */
    public int itemsCount() {
        return Layout.layItemsCount(this);
    }

    /**
     * @see org.layout.Layout#layItemsCapacity
     */
    public int itemsCapacity() {
        return Layout.layItemsCapacity(this);
    }

    /**
     * @see org.layout.Layout#layItem
     */
    public int item() {
        return Layout.layItem(this);
    }

    /**
     * @see org.layout.Layout#layLastChild
     */
    public int lastChild(int parent) {
        return Layout.layLastChild(this, parent);
    }

    /**
     * @see org.layout.Layout#layInsert
     */
    public void insert(int parent, int child) {
        Layout.layInsert(this, parent, child);
    }

    /**
     * @see org.layout.Layout#layAppend
     */
    public void append(int earlier, int later) {
        Layout.layAppend(this, earlier, later);
    }

    /**
     * @see org.layout.Layout#layPush
     */
    public void push(int parent, int newChild) {
        Layout.layPush(this, parent, newChild);
    }

    /**
     * @see org.layout.Layout#laySetSize
     */
    public void setSize(int item, float width, float height) {
        Layout.laySetSize(this, item, width, height);
    }

    /**
     * @see org.layout.Layout#layGetSizeX
     */
    public float getSizeX(int item) {
        return Layout.layGetSizeX(this, item);
    }

    /**
     * @see org.layout.Layout#layGetSizeY
     */
    public float getSizeY(int item) {
        return Layout.layGetSizeY(this, item);
    }

    /**
     * @see org.layout.Layout#layGetSizeXY
     */
    public float[] getSizeXY(int item, float[] dst) {
        return Layout.layGetSizeXY(this, item, dst);
    }

    /**
     * @see org.layout.Layout#laySetBehave
     */
    public void setBehave(int item, @MagicConstant(flagsFromClass = LayoutFlags.class) int flags) {
        Layout.laySetBehave(this, item, flags);
    }

    /**
     * @see org.layout.Layout#laySetContain
     */
    public void setContain(int item, @MagicConstant(flagsFromClass = LayoutBoxFlags.class) int flags) {
        Layout.laySetContain(this, item, flags);
    }

    /**
     * @see org.layout.Layout#laySetMargins
     */
    public void setMargins(int item, float left, float top, float right, float bottom) {
        Layout.laySetMargins(this, item, left, top, right, bottom);
    }

    /**
     * @see org.layout.Layout#layGetMarginsLTRB
     */
    public float[] getMarginsLTRB(int item, float[] dst) {
        return Layout.layGetMarginsLTRB(this, item, dst);
    }

    /**
     * @see org.layout.Layout#layFirstChild
     */
    public int firstChild(int id) {
        return Layout.layFirstChild(this, id);
    }

    /**
     * @see org.layout.Layout#layNextSibling
     */
    public int nextSibling(int id) {
        return Layout.layNextSibling(this, id);
    }

    /**
     * @see org.layout.Layout#layGetRectX
     */
    public float getRectX(int id) {
        return Layout.layGetRectX(this, id);
    }

    /**
     * @see org.layout.Layout#layGetRectY
     */
    public float getRectY(int id) {
        return Layout.layGetRectY(this, id);
    }

    /**
     * @see org.layout.Layout#layGetRectWidth
     */
    public float getRectWidth(int id) {
        return Layout.layGetRectWidth(this, id);
    }

    /**
     * @see org.layout.Layout#layGetRectHeight
     */
    public float getRectHeight(int id) {
        return Layout.layGetRectHeight(this, id);
    }

    /**
     * @see org.layout.Layout#layGetRect
     */
    public float[] getRect(int id, float[] dst) {
        return Layout.layGetRect(this, id, dst);
    }

    /**
     * @see org.layout.Layout#layCalcSize
     */
    public void calcSize(int item, int dim) {
        Layout.layCalcSize(this, item, dim);
    }

    /**
     * @see org.layout.Layout#layArrange
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
