package io.github.layout;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static io.github.layout.LayoutBoxFlags.*;
import static io.github.layout.LayoutFlags.*;

public class LayoutTest {

    private LayoutContext ctx;

    @BeforeEach
    public void setup() {
        ctx = new LayoutContext();
    }

    @Test
    public void simpleFill() {
        int root = Layout.layItem(ctx);
        int child = Layout.layItem(ctx);

        Layout.laySetSize(ctx, root, 30, 40);
        Layout.laySetBehave(ctx, child, LAY_FILL);
        Layout.layInsert(ctx, root, child);

        Layout.layRunContext(ctx);

        float[] rootR = Layout.layGetRect(ctx, root, new float[4]);
        float[] childR = Layout.layGetRect(ctx, child, new float[4]);

        assertTrue(rootR[0] == 0 && rootR[1] == 0);
        assertTrue(rootR[2] == 30 && rootR[3] == 40);

        assertTrue(childR[0] == 0 && childR[1] == 0);
        assertTrue(childR[2] == 30 && childR[3] == 40);

        // Test to make sure size os ok
        float[] rootSize = Layout.layGetSizeXY(ctx, root, new float[2]);
        // Make sure x/y versions give the same values
        float rootSizeX = Layout.layGetSizeX(ctx, root);
        float rootSizeY = Layout.layGetSizeY(ctx, root);
        assertTrue(rootSize[0] == 30 && rootSize[1] == 40);
        assertTrue(rootSize[0] == rootSizeX && rootSize[1] == rootSizeY);
    }

    @Test
    public void reserveCapacity() {
        Layout.layReserveItemsCapacity(ctx, 512);
        assertTrue(Layout.layItemsCapacity(ctx) >= 512);

        // Run some simple stuff like above, just to make it's still working

        int root = Layout.layItem(ctx);
        int child = Layout.layItem(ctx);

        Layout.laySetSize(ctx, root, 30, 40);
        Layout.laySetBehave(ctx, child, LAY_FILL);
        Layout.layInsert(ctx, root, child);

        Layout.layRunContext(ctx);

        float[] rootR = Layout.layGetRect(ctx, root, new float[4]);
        float[] childR = Layout.layGetRect(ctx, child, new float[4]);

        assertTrue(rootR[0] == 0 && rootR[1] == 0);
        assertTrue(rootR[2] == 30 && rootR[3] == 40);

        assertTrue(childR[0] == 0 && childR[1] == 0);
        assertTrue(childR[2] == 30 && childR[3] == 40);

        assertTrue(Layout.layItemsCapacity(ctx) >= 512);
    }

    @Test
    public void multipleUninserted() {
        int root = Layout.layItem(ctx);
        int child1 = Layout.layItem(ctx);
        int child2 = Layout.layItem(ctx);

        Layout.laySetSize(ctx, root, 155, 177);
        Layout.laySetSize(ctx, child2, 1, 1);

        Layout.layRunContext(ctx);

        float[] rootR = Layout.layGetRect(ctx, root, new float[4]);
        float[] child1R = Layout.layGetRect(ctx, child1, new float[4]);
        float[] child2R = Layout.layGetRect(ctx, child2, new float[4]);

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
        int root = Layout.layItem(ctx);
        int childA = Layout.layItem(ctx);
        int childB = Layout.layItem(ctx);
        int childC = Layout.layItem(ctx);

        Layout.laySetSize(ctx, root, 50, 60);
        Layout.laySetContain(ctx, root, LAY_COLUMN);
        Layout.laySetBehave(ctx, childA, LAY_FILL);
        Layout.laySetBehave(ctx, childB, LAY_FILL);
        Layout.laySetBehave(ctx, childC, LAY_FILL);
        Layout.laySetSize(ctx, childA, 0, 0);
        Layout.laySetSize(ctx, childB, 0, 0);
        Layout.laySetSize(ctx, childC, 0, 0);
        Layout.layInsert(ctx, root, childA);
        Layout.layInsert(ctx, root, childB);
        Layout.layInsert(ctx, root, childC);

        Layout.layRunContext(ctx);

        assertVec4Equals(Layout.layGetRect(ctx, root, new float[4]), 0, 0, 50, 60);
        assertVec4Equals(Layout.layGetRect(ctx, childA, new float[4]), 0, 0, 50, 20);
        assertVec4Equals(Layout.layGetRect(ctx, childB, new float[4]), 0, 20, 50, 20);
        assertVec4Equals(Layout.layGetRect(ctx, childC, new float[4]), 0, 40, 50, 20);
    }

    @Test
    public void rowEvenFill() {
        int root = Layout.layItem(ctx);
        int childA = Layout.layItem(ctx);
        int childB = Layout.layItem(ctx);
        int childC = Layout.layItem(ctx);

        Layout.laySetSize(ctx, root, 90, 3);
        Layout.laySetContain(ctx, root, LAY_ROW);
        Layout.laySetBehave(ctx, childA, LAY_HFILL | LAY_TOP);
        Layout.laySetBehave(ctx, childB, LAY_HFILL | LAY_VCENTER);
        Layout.laySetBehave(ctx, childC, LAY_HFILL | LAY_BOTTOM);
        Layout.laySetSize(ctx, childA, 0, 1);
        Layout.laySetSize(ctx, childB, 0, 1);
        Layout.laySetSize(ctx, childC, 0, 1);
        Layout.layInsert(ctx, root, childA);
        Layout.layInsert(ctx, root, childB);
        Layout.layInsert(ctx, root, childC);

        Layout.layRunContext(ctx);

        assertVec4Equals(Layout.layGetRect(ctx, root, new float[4]), 0, 0, 90, 3);
        assertVec4Equals(Layout.layGetRect(ctx, childA, new float[4]), 0, 0, 30, 1);
        assertVec4Equals(Layout.layGetRect(ctx, childB, new float[4]), 30, 1, 30, 1);
        assertVec4Equals(Layout.layGetRect(ctx, childC, new float[4]), 60, 2, 30, 1);
    }

    @Test
    public void fixedAndFill() {
        int root = Layout.layItem(ctx);
        int fixedA = Layout.layItem(ctx);
        int fixedB = Layout.layItem(ctx);
        int filler = Layout.layItem(ctx);

        Layout.laySetContain(ctx, root, LAY_COLUMN);

        Layout.laySetSize(ctx, root, 50, 60);
        Layout.laySetSize(ctx, fixedA, 50, 15);
        Layout.laySetSize(ctx, fixedB, 50, 15);
        Layout.laySetBehave(ctx, filler, LAY_FILL);

        Layout.layInsert(ctx, root, fixedA);
        Layout.layInsert(ctx, root, filler);
        Layout.layInsert(ctx, root, fixedB);

        Layout.layRunContext(ctx);

        assertVec4Equals(Layout.layGetRect(ctx, root, new float[4]), 0, 0, 50, 60);
        assertVec4Equals(Layout.layGetRect(ctx, fixedA, new float[4]), 0, 0, 50, 15);
        assertVec4Equals(Layout.layGetRect(ctx, filler, new float[4]), 0, 15, 50, 30);
        assertVec4Equals(Layout.layGetRect(ctx, fixedB, new float[4]), 0, 45, 50, 15);
    }

    private static void assertVec4Equals(float[] rect, float x, float y, float z, float w) {
        assertTrue(rect.length >= 4);
        assertTrue(rect[0] == x && rect[1] == y && rect[2] == z && rect[3] == w);
    }
}
