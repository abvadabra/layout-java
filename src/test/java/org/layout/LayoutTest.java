package org.layout;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.layout.Layout.*;
import static org.layout.LayoutBoxFlags.*;
import static org.layout.LayoutFlags.*;

public class LayoutTest {

    private LayoutContext ctx;

    @BeforeEach
    public void setup() {
        ctx = new LayoutContext();
    }

    @Test
    public void simpleFill() {
        int root = layItem(ctx);
        int child = layItem(ctx);

        laySetSize(ctx, root, 30, 40);
        laySetBehave(ctx, child, LAY_FILL);
        layInsert(ctx, root, child);

        layRunContext(ctx);

        float[] rootR = layGetRect(ctx, root, new float[4]);
        float[] childR = layGetRect(ctx, child, new float[4]);

        assertTrue(rootR[0] == 0 && rootR[1] == 0);
        assertTrue(rootR[2] == 30 && rootR[3] == 40);

        assertTrue(childR[0] == 0 && childR[1] == 0);
        assertTrue(childR[2] == 30 && childR[3] == 40);

        // Test to make sure size os ok
        float[] rootSize = layGetSizeXY(ctx, root, new float[2]);
        // Make sure x/y versions give the same values
        float rootSizeX = layGetSizeX(ctx, root);
        float rootSizeY = layGetSizeY(ctx, root);
        assertTrue(rootSize[0] == 30 && rootSize[1] == 40);
        assertTrue(rootSize[0] == rootSizeX && rootSize[1] == rootSizeY);
    }

    @Test
    public void reserveCapacity() {
        layReserveItemsCapacity(ctx, 512);
        assertTrue(layItemsCapacity(ctx) >= 512);

        // Run some simple stuff like above, just to make it's still working

        int root = layItem(ctx);
        int child = layItem(ctx);

        laySetSize(ctx, root, 30, 40);
        laySetBehave(ctx, child, LAY_FILL);
        layInsert(ctx, root, child);

        layRunContext(ctx);

        float[] rootR = layGetRect(ctx, root, new float[4]);
        float[] childR = layGetRect(ctx, child, new float[4]);

        assertTrue(rootR[0] == 0 && rootR[1] == 0);
        assertTrue(rootR[2] == 30 && rootR[3] == 40);

        assertTrue(childR[0] == 0 && childR[1] == 0);
        assertTrue(childR[2] == 30 && childR[3] == 40);

        assertTrue(layItemsCapacity(ctx) >= 512);
    }

    @Test
    public void multipleUninserted() {
        int root = layItem(ctx);
        int child1 = layItem(ctx);
        int child2 = layItem(ctx);

        laySetSize(ctx, root, 155, 177);
        laySetSize(ctx, child2, 1, 1);

        layRunContext(ctx);

        float[] rootR = layGetRect(ctx, root, new float[4]);
        float[] child1R = layGetRect(ctx, child1, new float[4]);
        float[] child2R = layGetRect(ctx, child2, new float[4]);

        assertVec4Equals(rootR, 0, 0, 155, 177);
        // This uninserted child should obviously be zero:
        assertVec4Equals(child1R, 0, 0, 0, 0);
        // You might expect this to pass for this child:
        //
        // LTEST_VEC4EQ(child2_r, 0, 0, 1, 1);
        //
        // But it won't, because items not inserted into the root will not have
        // their output rect set. (Hmm, is this a good API design idea?)
        //
        // Instead, it will be zero:
        assertVec4Equals(child2R, 0, 0, 0, 0);
    }

    @Test
    public void columnEvenFill() {
        int root = layItem(ctx);
        int childA = layItem(ctx);
        int childB = layItem(ctx);
        int childC = layItem(ctx);

        laySetSize(ctx, root, 50, 60);
        laySetContain(ctx, root, LAY_COLUMN);
        laySetBehave(ctx, childA, LAY_FILL);
        laySetBehave(ctx, childB, LAY_FILL);
        laySetBehave(ctx, childC, LAY_FILL);
        laySetSize(ctx, childA, 0, 0);
        laySetSize(ctx, childB, 0, 0);
        laySetSize(ctx, childC, 0, 0);
        layInsert(ctx, root, childA);
        layInsert(ctx, root, childB);
        layInsert(ctx, root, childC);

        layRunContext(ctx);

        assertVec4Equals(layGetRect(ctx, root, new float[4]), 0, 0, 50, 60);
        assertVec4Equals(layGetRect(ctx, childA, new float[4]), 0, 0, 50, 20);
        assertVec4Equals(layGetRect(ctx, childB, new float[4]), 0, 20, 50, 20);
        assertVec4Equals(layGetRect(ctx, childC, new float[4]), 0, 40, 50, 20);
    }

    @Test
    public void rowEvenFill() {
        int root = layItem(ctx);
        int childA = layItem(ctx);
        int childB = layItem(ctx);
        int childC = layItem(ctx);

        laySetSize(ctx, root, 90, 3);
        laySetContain(ctx, root, LAY_ROW);
        laySetBehave(ctx, childA, LAY_HFILL | LAY_TOP);
        laySetBehave(ctx, childB, LAY_HFILL | LAY_VCENTER);
        laySetBehave(ctx, childC, LAY_HFILL | LAY_BOTTOM);
        laySetSize(ctx, childA, 0, 1);
        laySetSize(ctx, childB, 0, 1);
        laySetSize(ctx, childC, 0, 1);
        layInsert(ctx, root, childA);
        layInsert(ctx, root, childB);
        layInsert(ctx, root, childC);

        layRunContext(ctx);

        assertVec4Equals(layGetRect(ctx, root, new float[4]), 0, 0, 90, 3);
        assertVec4Equals(layGetRect(ctx, childA, new float[4]), 0, 0, 30, 1);
        assertVec4Equals(layGetRect(ctx, childB, new float[4]), 30, 1, 30, 1);
        assertVec4Equals(layGetRect(ctx, childC, new float[4]), 60, 2, 30, 1);
    }

    @Test
    public void fixedAndFill() {
        int root = layItem(ctx);
        int fixedA = layItem(ctx);
        int fixedB = layItem(ctx);
        int filler = layItem(ctx);

        laySetContain(ctx, root, LAY_COLUMN);

        laySetSize(ctx, root, 50, 60);
        laySetSize(ctx, fixedA, 50, 15);
        laySetSize(ctx, fixedB, 50, 15);
        laySetBehave(ctx, filler, LAY_FILL);

        layInsert(ctx, root, fixedA);
        layInsert(ctx, root, filler);
        layInsert(ctx, root, fixedB);

        layRunContext(ctx);

        assertVec4Equals(layGetRect(ctx, root, new float[4]), 0, 0, 50, 60);
        assertVec4Equals(layGetRect(ctx, fixedA, new float[4]), 0, 0, 50, 15);
        assertVec4Equals(layGetRect(ctx, filler, new float[4]), 0, 15, 50, 30);
        assertVec4Equals(layGetRect(ctx, fixedB, new float[4]), 0, 45, 50, 15);
    }

    private static void assertVec4Equals(float[] rect, float x, float y, float z, float w) {
        assertTrue(rect.length >= 4);
        assertTrue(rect[0] == x && rect[1] == y && rect[2] == z && rect[3] == w);
    }
}
