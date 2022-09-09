package org.layout;

public final class LayoutContext {

    Layout.LayoutItem[] items = new Layout.LayoutItem[0];
    LayoutRect[] rects = new LayoutRect[0];
    int capacity;
    int count;

    public LayoutContext() {}

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
