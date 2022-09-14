package io.github.layout;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

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
    public void simpleGrowFactors() {
        int root = ctx.item();
        int childA = ctx.item();
        int childB = ctx.item();
        int childC = ctx.item();

        ctx.setSize(root, 100, 10);
        ctx.setContain(root, LAY_ROW | LAY_START);

        ctx.setBehave(childA, LAY_FILL);
        ctx.setBehave(childB, LAY_FILL);
        ctx.setBehave(childC, LAY_FILL);

        ctx.setGrow(childA, 2);
        ctx.setGrow(childB, 1);
        // grow factor for childC not set because by default it should be interpreted as 1

        ctx.insert(root, childA);
        ctx.insert(root, childB);
        ctx.insert(root, childC);

        ctx.runContext();

        assertVec4Equals(ctx.getRect(root, new float[4]), 0, 0, 100, 10);

        assertVec4Equals(ctx.getRect(childA, new float[4]), 0, 0, 50, 10);
        assertVec4Equals(ctx.getRect(childB, new float[4]), 50, 0, 25, 10);
        assertVec4Equals(ctx.getRect(childC, new float[4]), 75, 0, 25, 10);
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

    @Test
    public void simpleMargins1() {
        int root = ctx.item();
        int childA = ctx.item();
        int childB = ctx.item();
        int childC = ctx.item();

        ctx.setContain(root, LAY_COLUMN);
        ctx.setBehave(childA, LAY_HFILL);
        ctx.setBehave(childB, LAY_FILL);
        ctx.setBehave(childC, LAY_HFILL);

        ctx.setSize(root, 100, 90);

        ctx.setMargins(childA, 3, 5, 7, 10);
        ctx.setSize(childA, 0, (30 - (5 + 10)));
        ctx.setSize(childC, 0, 30);

        ctx.insert(root, childA);
        ctx.insert(root, childB);
        ctx.insert(root, childC);

        ctx.runContext();

        // Querying for the margins we set should give us the same value,
        assertVec4Equals(ctx.getMarginsLTRB(childA, new float[4]), 3, 5, 7, 10);

        // The resulting calculated rects should match these values.
        assertVec4Equals(ctx.getRect(childA, new float[4]), 3, 5, 90, (5 + 10));
        assertVec4Equals(ctx.getRect(childB, new float[4]), 0, 30, 100, 30);
        assertVec4Equals(ctx.getRect(childC, new float[4]), 0, 60, 100, 30);
    }

    @Test
    public void nestedBoxes1() {
        final int numRows = 5;
        // one of the rows is "fake" and will have 0 units tall height
        final int numRowsWithHeight = numRows - 1;

        int root = ctx.item();
        // main_child is a column that contains rows, and those rows
        // will contain columns.
        int mainChild = ctx.item();
        ctx.setSize(root, 70,
                // 10 units extra size above and below for main_child
                // margin
                (numRowsWithHeight * 10 + 2 * 10));
        ctx.setMargins(mainChild, 10, 10, 10, 10);
        ctx.setContain(mainChild, LAY_COLUMN);
        ctx.insert(root, mainChild);
        ctx.setBehave(mainChild, LAY_FILL);

        int[] rows = new int[numRows];
        // auto-filling columns-in-row, each one should end up being
        // 10 units wide
        rows[0] = ctx.item();
        ctx.setContain(rows[0], LAY_ROW);
        ctx.setBehave(rows[0], LAY_FILL);
        int[] cols1 = new int[5];
        // hmm so both the row and its child columns need to be set to
        // fill? which means main_child also needs to be set to fill?
        for (int i = 0; i < 5; i++) {
            int col = ctx.item();
            // fill empty space
            ctx.setBehave(col, LAY_FILL);
            ctx.insert(rows[0], col);
            cols1[i] = col;
        }

        rows[1] = ctx.item();
        ctx.setContain(rows[1], LAY_ROW);
        ctx.setBehave(rows[1], LAY_FILL);
        int[] cols2 = new int[5];
        for (int i = 0; i < 5; i++) {
            int col = ctx.item();
            // fixed-size horizontally, fill vertically
            ctx.setSize(col, 10, 0);
            ctx.setBehave(col, LAY_VFILL);
            ctx.insert(rows[1], col);
            cols2[i] = col;
        }

        // these columns have an inner item which sizes them
        rows[2] = ctx.item();
        ctx.setContain(rows[2], LAY_ROW);
        ctx.setBehave(rows[1], LAY_FILL);
        int[] cols3 = new int[2];
        for (int i = 0; i < 2; i++) {
            int col = ctx.item();
            int innerSizer = ctx.item();
            // only the second one will have height
            ctx.setSize(innerSizer, 25, i * 10);
            // align to bottom, only should make a difference for
            // first item
            ctx.setBehave(col, LAY_BOTTOM);
            ctx.insert(col, innerSizer);
            ctx.insert(rows[2], col);
            cols3[i] = col;
        }

        // row 4 should end up being 0 units tall after layout
        rows[3] = ctx.item();
        ctx.setContain(rows[3], LAY_ROW);
        ctx.setBehave(rows[3], LAY_HFILL);
        int[] cols4 = new int[99];
        for (int i = 0; i < 99; i++) {
            int col = ctx.item();
            ctx.insert(rows[3], col);
            cols4[i] = col;
        }

        // row 5 should be 10 pixels tall after layout, and each of
        // its columns should be 1 pixel wide
        rows[4] = ctx.item();
        ctx.setContain(rows[4], LAY_ROW);
        ctx.setBehave(rows[4], LAY_FILL);
        int[] cols5 = new int[50];
        for (int i = 0; i < 50; i++) {
            int col = ctx.item();
            ctx.setBehave(col, LAY_FILL);
            ctx.insert(rows[4], col);
            cols5[i] = col;
        }

        for (int i = 0; i < numRows; i++) {
            ctx.insert(mainChild, rows[i]);
        }

        // Repeat the run and tests multiple times to make sure we get the expected
        // results each time. The original version of oui would overwrite its input
        // state (intentionally) with the output state, so the context's input data
        // (margins, size) had to be "rebuilt" by the client code by doing a reset
        // and then filling it back up for each run. 'lay' does not have that
        // restriction.
        //
        // This is one of the more complex tests, so it's a good
        // choice for testing multiple runs of the same context.
        for(int runN = 0; runN < 5; runN++) {
            System.out.println("Iteration #" + runN);
            ctx.runContext();

            assertVec4Equals(ctx.getRect(mainChild, new float[4]), 10, 10, 50, 40);
            // These rows should all be 10 units in height
            assertVec4Equals(ctx.getRect(rows[0], new float[4]), 10, 10, 50, 10);
            assertVec4Equals(ctx.getRect(rows[1], new float[4]), 10, 20, 50, 10);
            assertVec4Equals(ctx.getRect(rows[2], new float[4]), 10, 30, 50, 10);
            // this row should have 0 height
            assertVec4Equals(ctx.getRect(rows[3], new float[4]), 10, 40, 50, 0);
            assertVec4Equals(ctx.getRect(rows[4], new float[4]), 10, 40, 50, 10);

            for (int i = 0; i < 5; i++) {
                float[] r = ctx.getRect(cols1[i], new float[4]);
                // each of these should be 10 units wide, and stacked
                // horizontally
                assertVec4Equals(r, 10 + 10 * i, 10, 10, 10);
            }

            // the cols in the second row are similar to first row
            for (int i = 0; i < 5; i++) {
                float[] r = ctx.getRect(cols2[i], new float[4]);
                assertVec4Equals(r, 10 + 10 * i, 20, 10, 10);
            }

            // leftmost (first of two items), aligned to bottom of row, 0
            // units tall
            assertVec4Equals(ctx.getRect(cols3[0], new float[4]), 10, 40, 25, 0);
            // rightmost (second of two items), same height as row, which
            // is 10 units tall
            assertVec4Equals(ctx.getRect(cols3[1], new float[4]), 35, 30, 25, 10);

            // these should all have size 0 and be in the middle of the
            // row
            for (int i = 0; i < 99; i++) {
                float[] r = ctx.getRect(cols4[i], new float[4]);
                assertVec4Equals(r, 25 + 10, 40, 0, 0);
            }

            // these should all be 1 unit wide and 10 units tall
            for (int i = 0; i < 50; i++) {
                float[] r = ctx.getRect(cols5[i], new float[4]);
                assertVec4Equals(r, 10 + i, 40, 1, 10);
            }
        }
    }

    @Test
    public void deepNest1() {
        int root = ctx.item();

        final int numItems = 500;

        int parent = root;
        for (int i = 0; i < numItems; i++) {
            int item = ctx.item();
            ctx.insert(parent, item);
            parent = item;
        }

        ctx.setSize(parent, 77, 99);

        ctx.runContext();

        assertVec4Equals(ctx.getRect(root, new float[4]), 0, 0, 77, 99);
    }

    @Test
    public void manyChildren1() {
        int numItems = 20000;

        int root = ctx.item();
        ctx.setSize(root, 1, 0);
        ctx.setContain(root, LAY_COLUMN);

        int prev = ctx.item();
        ctx.setSize(prev, 1, 1);
        ctx.insert(root, prev);
        for(int i = 0; i < numItems - 1; i++) {
            int item = ctx.item();
            ctx.setSize(item, 1, 1);
            ctx.append(prev, item);
            prev = item;
        }

        ctx.runContext();

        // with each child item being 1 unit tall, the total height should be num_items
        assertVec4Equals(ctx.getRect(root, new float[4]), 0, 0, 1, numItems);
    }

    @Test
    public void anchorRightMargin1() {
        int root = ctx.item();
        ctx.setSize(root, 100, 100);

        int child = ctx.item();
        ctx.setSize(child, 50, 50);
        ctx.setMargins(child, 5, 5, 0, 0);
        ctx.setBehave(child, LAY_BOTTOM | LAY_RIGHT);

        ctx.insert(root, child);

        ctx.runContext();

        assertVec4Equals(ctx.getRect(child, new float[4]), 50, 50, 50, 50);
    }

    @Test
    public void anchorRightMargin2() {
        int root = ctx.item();
        ctx.setSize(root, 100, 100);

        int child = ctx.item();
        ctx.setSize(child, 50, 50);
        ctx.setMargins(child, 5, 5, 10, 10);
        ctx.setBehave(child, LAY_BOTTOM | LAY_RIGHT);

        ctx.insert(root, child);

        ctx.runContext();

        assertVec4Equals(ctx.getRect(child, new float[4]), 40, 40, 50, 50);
    }

    private static void assertVec4Equals(float[] rect, float x, float y, float z, float w) {
        assertArrayEquals(new float[] { x, y, z, w }, rect);
    }
}
