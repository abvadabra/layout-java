package io.github.layout;

import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static io.github.layout.LayoutBoxFlags.*;
import static io.github.layout.LayoutFlags.*;

@SuppressWarnings("unused")
public final class Layout {

    public static final int LAY_INVALID_ID = -1;

    /**
     * these bits, starting at bit 16, can be safely assigned by the
     * application, e.g. as item types, other event types, drop targets, etc.
     * this is not yet exposed via API functions, you'll need to get/set these
     * by directly accessing item pointers.
     * (In reality we have more free bits than this, TODO)
     * TODO fix int/unsigned size mismatch (clang issues warning for this),
     * should be all bits as 1 instead of INT_MAX
     */
    public static final int LAY_USERMASK = 0x7fff0000;

    /**
     * a special mask passed to lay_find_item() (currently does not exist, was
     * not ported from oui)
     */
    public static final int LAY_ANY = 0x7fffffff;

    // region extra item flags

    /**
     * bit 0-2
     */
    public static final int LAY_ITEM_BOX_MODEL_MASK = 0x000007;
    /**
     * bit 0-4
     */
    public static final int LAY_ITEM_BOX_MASK       = 0x00001F;
    /**
     * bit 5-9
     */
    public static final int LAY_ITEM_LAYOUT_MASK = 0x0003E0;
    /**
     * item has been inserted (bit 10)
     */
    public static final int LAY_ITEM_INSERTED   = 0x400;
    /**
     * horizontal size has been explicitly set (bit 11)
     */
    public static final int LAY_ITEM_HFIXED      = 0x800;
    /**
     * vertical size has been explicitly set (bit 12)
     */
    public static final int LAY_ITEM_VFIXED      = 0x1000;
    /**
     * bit 11-12
     */
    public static final int LAY_ITEM_FIXED_MASK  = LAY_ITEM_HFIXED | LAY_ITEM_VFIXED;

    /**
     * which flag bits will be compared
     */
    public static final int LAY_ITEM_COMPARE_MASK = LAY_ITEM_BOX_MODEL_MASK
            | (LAY_ITEM_LAYOUT_MASK & ~LAY_BREAK)
            | LAY_USERMASK;

    // endregion

    static @NotNull LayoutItem layGetItem(@NotNull LayoutContext ctx, int id) {
        assert id >= 0 && id <= ctx.count;
        return ctx.items[id];
    }

    /**
     * Reserve enough heap memory to contain `count` items without needing to
     * reallocate. The initial lay_init_context() call does not allocate any heap
     * memory, so if you init a context and then call this once with a large enough
     * number for the number of items you'll create, there will not be any further
     * reallocations.
     */
    public static void layReserveItemsCapacity(@NotNull LayoutContext ctx, int count) {
        if(count >= ctx.capacity) {
            int prevCapacity = ctx.capacity;

            ctx.capacity = count;
            ctx.items = Arrays.copyOf(ctx.items, ctx.capacity);
            ctx.rects = Arrays.copyOf(ctx.rects, ctx.capacity);

            for(int i = prevCapacity; i < ctx.capacity; i++) {
                ctx.items[i] = new LayoutItem();
                ctx.rects[i] = new LayoutContext.LayoutRect();
            }
        }
    }

    /**
     * Clears all of the items in a context, setting its count to 0. Use this when
     * you want to re-declare your layout starting from the root item. This does not
     * free any memory or perform allocations. It's safe to use the context again
     * after calling this. You should probably use this instead of init/destroy if
     * you are recalculating your layouts in a loop.
     */
    public static void layResetContext(@NotNull LayoutContext ctx) {
        ctx.count = 0;
    }

    /**
     * Performs the layout calculations, starting at the root item (id 0). After
     * calling this, you can use lay_get_rect() to query for an item's calculated
     * rectangle. If you use procedures such as lay_append() or lay_insert() after
     * calling this, your calculated data may become invalid if a reallocation
     * occurs.
     * You should prefer to recreate your items starting from the root instead of
     * doing fine-grained updates to the existing context.
     * However, it's safe to use lay_set_size on an item, and then re-run
     * lay_run_context. This might be useful if you are doing a resizing animation
     * on items in a layout without any contents changing.
     */
    public static void layRunContext(@NotNull LayoutContext ctx) {
        if(ctx.count > 0) {
            layRunItem(ctx, 0);
        }
    }

    /**
     * Like lay_run_context(), this procedure will run layout calculations --
     * however, it lets you specify which item you want to start from.
     * lay_run_context() always starts with item 0, the first item, as the root.
     * Running the layout calculations from a specific item is useful if you want
     * need to iteratively re-run parts of your layout hierarchy, or if you are only
     * interested in updating certain subsets of it. Be careful when using this --
     * it's easy to generated bad output if the parent items haven't yet had their
     * output rectangles calculated, or if they've been invalidated (e.g. due to
     * re-allocation).
     */
    public static void layRunItem(@NotNull LayoutContext ctx, int item) {
        layCalcSize(ctx, item, 0);
        layArrange(ctx, item, 0);
        layCalcSize(ctx, item, 1);
        layArrange(ctx, item, 1);
    }

    /**
     * Performing a layout on items where wrapping is enabled in the parent
     * container can cause flags to be modified during the calculations. If you plan
     * to call lay_run_context or lay_run_item multiple times without calling
     * lay_reset, and if you have a container that uses wrapping, and if the width
     * or height of the container may have changed, you should call
     * lay_clear_item_break on all of the children of a container before calling
     * lay_run_context or lay_run_item again. If you don't, the layout calculations
     * may perform unnecessary wrapping.
     * This requirement may be changed in the future.
     * Calling this will also reset any manually-specified breaking. You will need
     * to set the manual breaking again, or simply not call this on any items that
     * you know you wanted to break manually.
     * If you clear your context every time you calculate your layout, or if you
     * don't use wrapping, you don't need to call this.
     */
    public static void layClearItemBreak(@NotNull LayoutContext ctx, int item) {
        LayoutItem pitem = layGetItem(ctx, item);
        pitem.flags = pitem.flags & ~(LAY_BREAK);
    }

    /**
     * Returns the number of items that have been created in a context.
     */
    public static int layItemsCount(@NotNull LayoutContext ctx) {
        return ctx.count;
    }

    /**
     * Returns the number of items the context can hold without performing a
     * reallocation.
     */
    public static int layItemsCapacity(@NotNull LayoutContext ctx) {
        return ctx.capacity;
    }

    /**
     * Create a new item, which can just be thought of as a rectangle. Returns the
     * id (handle) used to identify the item.
     */
    public static int layItem(@NotNull LayoutContext ctx) {
        int idx = ctx.count++;
        if(idx >= ctx.capacity) {
            layReserveItemsCapacity(ctx, ctx.capacity < 1 ? 32 : (ctx.capacity * 4));
        }

        LayoutItem item = layGetItem(ctx, idx);
        _clearItem(item);
        item.firstChild = LAY_INVALID_ID;
        item.nextSibling = LAY_INVALID_ID;
        _clearRect(ctx.rects[idx]);
        return idx;
    }

    static void layAppendByPtr(@NotNull LayoutItem pearlier, int later, @NotNull LayoutItem plater) {
        plater.nextSibling = pearlier.nextSibling;
        plater.flags |= LAY_ITEM_INSERTED;
        pearlier.nextSibling = later;
    }

    public static int layLastChild(@NotNull LayoutContext ctx, int parent) {
        LayoutItem pparent = layGetItem(ctx, parent);
        int child = pparent.firstChild;
        if(child == LAY_INVALID_ID) return LAY_INVALID_ID;
        LayoutItem pchild = layGetItem(ctx, child);
        int result = child;
        for(;;){
            int next = pchild.nextSibling;
            if(next == LAY_INVALID_ID) break;
            result = next;
            pchild = layGetItem(ctx, next);
        }
        return result;
    }

    /**
     * Inserts an item into another item, forming a parent - child relationship. An
     * item can contain any number of child items. Items inserted into a parent are
     * put at the end of the ordering, after any existing siblings.
     */
    public static void layInsert(@NotNull LayoutContext ctx, int parent, int child) {
        assert child != 0; // Must not be root item
        assert parent != child; // Must not be root item
        LayoutItem pparent = layGetItem(ctx, parent);
        LayoutItem pchild = layGetItem(ctx, child);
        assert (pchild.flags & LAY_ITEM_INSERTED) == 0;
        // Parent has no existing children, make inserted item the first child.
        if(pparent.firstChild == LAY_INVALID_ID) {
            pparent.firstChild = child;
            pchild.flags |= LAY_ITEM_INSERTED;
        } else {
            // Parent has existing items, iterate to find the last child and append the
            // inserted item after it.
            int next = pparent.firstChild;
            LayoutItem pnext = layGetItem(ctx, next);
            for(;;) {
                next = pnext.nextSibling;
                if(next == LAY_INVALID_ID) break;
                pnext = layGetItem(ctx, next);
            }
            layAppendByPtr(pnext, child, pchild);
        }

    }

    /**
     * lay_append inserts an item as a sibling after another item. This allows
     * inserting an item into the middle of an existing list of items within a
     * parent. It's also more efficient than repeatedly using lay_insert(ctx,
     * parent, new_child) in a loop to create a list of items in a parent, because
     * it does not need to traverse the parent's children each time. So if you're
     * creating a long list of children inside of a parent, you might prefer to use
     * this after using lay_insert to insert the first child.
     */
    public static void layAppend(@NotNull LayoutContext ctx, int earlier, int later) {
        assert later != 0; // Must not be root item
        assert earlier != later; // Must not be same item id
        LayoutItem pearlier = layGetItem(ctx, earlier);
        LayoutItem plater = layGetItem(ctx, later);
        layAppendByPtr(pearlier, later, plater);
    }


    /**
     * Like lay_insert, but puts the new item as the first child in a parent instead
     * of as the last.
     */
    public static void layPush(@NotNull LayoutContext ctx, int parent, int newChild) {
        assert newChild != 0; // Must not be root item
        assert parent != newChild; // Must not be same item id
        LayoutItem pparent = layGetItem(ctx, parent);
        int oldChild = pparent.firstChild;
        LayoutItem pchild = layGetItem(ctx, newChild);
        assert (pchild.flags & LAY_ITEM_INSERTED) == 0;
        pparent.firstChild = newChild;
        pchild.flags |= LAY_ITEM_INSERTED;
        pchild.nextSibling = oldChild;
    }

    /**
     * Sets the size of an item
     */
    public static void laySetSize(@NotNull LayoutContext ctx, int item, float width, float height) {
        LayoutItem pitem = layGetItem(ctx, item);
        pitem.sizeX = width;
        pitem.sizeY = height;
        int flags = pitem.flags;
        if(width == 0) {
            flags &= ~(LAY_ITEM_HFIXED);
        } else {
            flags |= LAY_ITEM_HFIXED;
        }
        if(height == 0) {
            flags &= ~(LAY_ITEM_VFIXED);
        } else {
            flags |= LAY_ITEM_VFIXED;
        }
        pitem.flags = flags;
    }

    /**
     * Gets the horizontal size of an item that was set with setSize
     */
    public static float layGetSizeX(@NotNull LayoutContext ctx, int item) {
        return layGetItem(ctx, item).sizeX;
    }

    /**
     * Gets the vertical size of an item that was set with setSize
     */
    public static float layGetSizeY(@NotNull LayoutContext ctx, int item) {
        return layGetItem(ctx, item).sizeY;
    }

    /**
     * Gets the size of an item that was set with setSize. Output will be written to provided array,
     * which should have size of at least 2 elements
     */
    public static float[] layGetSizeXY(@NotNull LayoutContext ctx, int item, float[] dst) {
        assert dst.length >= 2;
        LayoutItem pitem = layGetItem(ctx, item);
        dst[0] = pitem.sizeX;
        dst[1] = pitem.sizeY;
        return dst;
    }

    /**
     * Set the flags on an item which determines how it behaves as a child inside of
     * a parent item. For example, setting LAY_VFILL will make an item try to fill
     * up all available vertical space inside of its parent.
     */
    public static void laySetBehave(@NotNull LayoutContext ctx, int item, @MagicConstant(flagsFromClass = LayoutFlags.class) int flags) {
        //noinspection MagicConstant
        assert (flags & LAY_ITEM_LAYOUT_MASK) == flags;
        LayoutItem pitem = layGetItem(ctx, item);
        pitem.flags = (pitem.flags & ~LAY_ITEM_LAYOUT_MASK) | flags;
    }

    /**
     * Set the flags on an item which determines how it behaves as a parent. For
     * example, setting LAY_COLUMN will make an item behave as if it were a column
     * -- it will lay out its children vertically.
     */
    public static void laySetContain(@NotNull LayoutContext ctx, int item, @MagicConstant(flagsFromClass = LayoutBoxFlags.class) int flags) {
        //noinspection MagicConstant
        assert (flags & LAY_ITEM_BOX_MASK) == flags;
        LayoutItem pitem = layGetItem(ctx, item);
        pitem.flags = (pitem.flags & ~LAY_ITEM_BOX_MASK) | flags;
    }

    /**
     * Get the margins that were set by lay_set_margins. The _ltrb version writes
     * the output values to the specified addresses instead of returning the values
     * in a lay_vec4.
     * l: left, t: top, r: right, b: bottom
     */
    public static void laySetMargins(@NotNull LayoutContext ctx, int item, float left, float top, float right, float bottom) {
        LayoutItem pitem = layGetItem(ctx, item);
        pitem.marginLeft = left;
        pitem.marginTop = top;
        pitem.marginRight = right;
        pitem.marginBottom = bottom;
    }

    /**
     * Get the margins that were set by setMargins. Output will be written to provided array,
     * which should have size of at least 4 elements
     */
    public static float[] layGetMarginsLTRB(@NotNull LayoutContext ctx, int item, float[] dst) {
        assert dst.length >= 4;
        LayoutItem pitem = layGetItem(ctx, item);
        dst[0] = pitem.marginLeft;
        dst[1] = pitem.marginTop;
        dst[2] = pitem.marginRight;
        dst[3] = pitem.marginBottom;
        return dst;
    }

    /**
     * Get the id of first child of an item, if any. Returns LAY_INVALID_ID if there
     * is no child.
     */
    public static int layFirstChild(@NotNull LayoutContext ctx, int id) {
        return layGetItem(ctx, id).firstChild;
    }

    /**
     * Get the id of the next sibling of an item, if any. Returns LAY_INVALID_ID if
     * there is no next sibling.
     */
    public static int layNextSibling(@NotNull LayoutContext ctx, int id) {
        return layGetItem(ctx, id).nextSibling;
    }

    public static float layGetRectX(@NotNull LayoutContext ctx, int id) {
        assert id >= 0 && id < ctx.count;
        return ctx.rects[id].x;
    }

    public static float layGetRectY(@NotNull LayoutContext ctx, int id) {
        assert id >= 0 && id < ctx.count;
        return ctx.rects[id].y;
    }

    public static float layGetRectWidth(@NotNull LayoutContext ctx, int id) {
        assert id >= 0 && id < ctx.count;
        return ctx.rects[id].w;
    }

    public static float layGetRectHeight(@NotNull LayoutContext ctx, int id) {
        assert id >= 0 && id < ctx.count;
        return ctx.rects[id].h;
    }

    /**
     * Returns the calculated rectangle of an item. This is only valid after calling
     * lay_run_context and before any other reallocation occurs. Otherwise, the
     * result will be undefined. Output will be written to given array which should have a size of at least 4 elements.
     * The array components are:
     * 0: x starting position, 1: y starting position
     * 2: width, 3: height
     */
    public static float[] layGetRect(@NotNull LayoutContext ctx, int id, float[] dst) {
        assert id >= 0 && id < ctx.count;
        assert dst.length >= 4;
        LayoutContext.LayoutRect rect = ctx.rects[id];
        dst[0] = rect.x;
        dst[1] = rect.y;
        dst[2] = rect.w;
        dst[3] = rect.h;
        return dst;
    }

    static float layCalcOverlayedSize(@NotNull LayoutContext ctx, int item, int dim) {
        final int wdim = dim + 2;
        LayoutItem pitem = layGetItem(ctx, item);
        float needSize = 0;
        int child = pitem.firstChild;
        while (child != LAY_INVALID_ID) {
            LayoutItem pchild = layGetItem(ctx, child);
            LayoutContext.LayoutRect rect = ctx.rects[child];
            // width = start margin + calculated margin + end margin
            float childSize = rect.get(dim) + rect.get(2 + dim) + pchild.margins(wdim);
            needSize = Math.max(needSize, childSize);
            child = pchild.nextSibling;
        }
        return needSize;
    }

    static float layCalcStackedSize(@NotNull LayoutContext ctx, int item, int dim) {
        final int wdim = dim + 2;
        LayoutItem pitem = layGetItem(ctx, item);
        float needSize = 0;
        int child = pitem.firstChild;
        while (child != LAY_INVALID_ID) {
            LayoutItem pchild = layGetItem(ctx, child);
            LayoutContext.LayoutRect rect = ctx.rects[child];
            needSize += rect.get(dim) + rect.get(2 + dim) + pchild.margins(wdim);
            child = pchild.nextSibling;
        }
        return needSize;
    }

    static float layCalcWrappedOverlayedSize(@NotNull LayoutContext ctx, int item, int dim) {
        final int wdim = dim + 2;
        LayoutItem pitem = layGetItem(ctx, item);
        float needSize = 0F;
        float needSize2 = 0F;
        int child = pitem.firstChild;
        while (child != LAY_INVALID_ID) {
            LayoutItem pchild = layGetItem(ctx, child);
            LayoutContext.LayoutRect rect = ctx.rects[child];
            if((pchild.flags & LAY_BREAK) > 0) {
                needSize2 += needSize;
                needSize = 0;
            }
            float childSize = rect.get(dim) + rect.get(2 + dim) + pchild.margins(wdim);
            needSize = Math.max(needSize, childSize);
            child = pchild.nextSibling;
        }
        return needSize2 + needSize;
    }

    static float layCalcWrappedStackedSize(@NotNull LayoutContext ctx, int item, int dim) {
        final int wdim = dim + 2;
        LayoutItem pitem = layGetItem(ctx, item);
        float needSize = 0F;
        float needSize2 = 0F;
        int child = pitem.firstChild;
        while (child != LAY_INVALID_ID) {
            LayoutItem pchild = layGetItem(ctx, child);
            LayoutContext.LayoutRect rect = ctx.rects[child];
            if((pchild.flags & LAY_BREAK) > 0) {
                needSize2 = Math.max(needSize2, needSize);
                needSize = 0F;
            }
            needSize2 += rect.get(dim) + rect.get(2 + dim) + pchild.margins(wdim);
            child = pchild.nextSibling;
        }
        return Math.max(needSize2, needSize);
    }

    public static void layCalcSize(@NotNull LayoutContext ctx, int item, int dim) {
        LayoutItem pitem = layGetItem(ctx, item);

        int child = pitem.firstChild;
        while (child != LAY_INVALID_ID) {
            layCalcSize(ctx, child, dim);
            LayoutItem pchild = layGetItem(ctx, child);
            child = pchild.nextSibling;
        }

        // Set the mutable rect output data to the starting input data
        ctx.rects[item].set(dim, pitem.margins(dim));

        // If we have an explicit input size, just set our output size (which other
        // calcSize and arrange procedures will us) to it.
        if(pitem.size(dim) != 0) {
            ctx.rects[item].set(2 + dim, pitem.size(dim));
            return;
        }

        // Calculate our size based on children items. Note that we've already
        // called calcSize on our children at this point.
        float calSize;
        switch (pitem.flags & LAY_ITEM_BOX_MODEL_MASK) {
            case LAY_LAYOUT | LAY_WRAP:
                // flex model
                if (dim > 0) {
                    calSize = layCalcStackedSize(ctx, item, 1);
                } else {
                    calSize = layCalcOverlayedSize(ctx, item, 0);
                }
                break;
            case LAY_ROW | LAY_WRAP:
                // flex model
                if(dim == 0) { // direction
                    calSize = layCalcWrappedStackedSize(ctx, item, 0);
                } else {
                    calSize = layCalcWrappedOverlayedSize(ctx, item, 1);
                }
                break;
            case LAY_COLUMN:
            case LAY_ROW:
                // flex model
                if((pitem.flags & 1) == dim) { // direction
                    calSize = layCalcStackedSize(ctx, item, dim);
                } else {
                    calSize = layCalcOverlayedSize(ctx, item, dim);
                }
                break;
            default:
                // layout model
                calSize = layCalcOverlayedSize(ctx, item, dim);
                break;
        }

        // Set our output data size. Will be used by parent calc_size procedures.,
        // and by arrange procedures.
        ctx.rects[item].set(2 + dim, calSize);
    }

    static void layArrangeStacked(@NotNull LayoutContext ctx, int item, int dim, boolean wrap) {
        final int wdim = dim + 2;
        LayoutItem pitem = layGetItem(ctx, item);

        final int itemFlags = pitem.flags;
        LayoutContext.LayoutRect rect = ctx.rects[item];
        float space = rect.get(2 + dim);

        float maxX2 = rect.get(dim) + space;

        int startChild = pitem.firstChild;
        while (startChild != LAY_INVALID_ID) {
            float used = 0F;
            int count = 0; // count of fillers
            int squeezedCount = 0; // count of squeezable elements
            int total = 0;
            boolean hardbreak = false;

            // first pass: count items that need to be expanded,
            // and the space that is used
            int child = startChild;
            int endChild = LAY_INVALID_ID;
            while (child != LAY_INVALID_ID) {
                LayoutItem pchild = layGetItem(ctx, child);
                final int childFlags = pchild.flags;
                final int flags = (childFlags & LAY_ITEM_LAYOUT_MASK) >> dim;
                final int fflags = (childFlags & LAY_ITEM_FIXED_MASK) >> dim;
                LayoutContext.LayoutRect childRect = ctx.rects[child];
                float extend = used;
                if((flags & LAY_HFILL) == LAY_HFILL) {
                    ++count;
                    extend += childRect.get(dim) + pchild.margins(wdim);
                } else {
                    if((fflags & LAY_ITEM_HFIXED) != LAY_ITEM_HFIXED) {
                        ++squeezedCount;
                    }
                    extend += childRect.get(dim) + childRect.get(2 + dim) + pchild.margins(wdim);
                }
                // wrap on end of line or manual flag
                if(wrap && (total > 0 && ((extend > space) || (childFlags & LAY_BREAK) > 0))) {
                    endChild = child;
                    hardbreak = (childFlags & LAY_BREAK) == LAY_BREAK;
                    // add marker for subsequent queries
                    pchild.flags = childFlags | LAY_BREAK;
                    break;
                } else {
                    used = extend;
                    child = pchild.nextSibling;
                }
                ++total;
            }

            float extraSpace = space - used;
            float filler = 0F;
            float spacer = 0F;
            float extraMargin = 0F;
            float eater = 0F;

            if (extraSpace > 0) {
                if(count > 0) {
                    filler = extraSpace / (float)count;
                } else if(total > 0) {
                    switch (itemFlags & LAY_JUSTIFY) {
                        case LAY_JUSTIFY:
                            // justify when not wrapping or not in last line,
                            // or not manually breaking
                            if(!wrap || ((endChild != LAY_INVALID_ID) && !hardbreak)) {
                                spacer = extraSpace / (float)(total - 1);
                            }
                            break;
                        case LAY_START:
                            break;
                        case LAY_END:
                            extraMargin = extraSpace;
                            break;
                        default:
                            extraMargin = extraSpace / 2.0F;
                            break;
                    }
                }
            } else if(!wrap && (squeezedCount > 0)) {
                eater = extraSpace / (float)squeezedCount;
            }

            // distribute width among items
            float x = rect.get(dim);
            float x1;
            // second pass: distribute and rescale
            child = startChild;
            while (child != endChild) {
                float ix0, ix1;
                LayoutItem pchild = layGetItem(ctx, child);
                final int childFlags = pchild.flags;
                final int flags = (childFlags & LAY_ITEM_LAYOUT_MASK) >> dim;
                final int fflags = (childFlags & LAY_ITEM_FIXED_MASK) >> dim;
                LayoutContext.LayoutRect childRect = ctx.rects[child];

                x += childRect.get(dim) + extraMargin;
                if((flags & LAY_HFILL) == LAY_HFILL) { // grow
                    x1 = x + filler;
                } else if((fflags & LAY_ITEM_HFIXED) == LAY_ITEM_HFIXED) {
                    x1 = x + childRect.get(2 + dim);
                } else { // squeeze
                    x1 = x + Math.max(0.0F, childRect.get(2 + dim) + eater);
                }

                ix0 = x;
                if(wrap) {
                    ix1 = Math.min(maxX2 - pchild.margins(wdim), x1);
                } else {
                    ix1 = x1;
                }

                childRect.set(dim, ix0); // pos
                childRect.set(dim + 2, ix1 - ix0); // size
                x = x1 + pchild.margins(wdim);
                child = pchild.nextSibling;
                extraMargin = spacer;
            }

            startChild = endChild;
        }
    }

    static void layArrangeOverlay(@NotNull LayoutContext ctx, int item, int dim) {
        final int wdim = dim + 2;
        LayoutItem pitem = layGetItem(ctx, item);
        LayoutContext.LayoutRect rect = ctx.rects[item];
        float offset = rect.get(dim);
        float space = rect.get(2 + dim);

        int child = pitem.firstChild;
        while (child != LAY_INVALID_ID) {
            LayoutItem pchild = layGetItem(ctx, child);
            final int bFlags = (pchild.flags & LAY_ITEM_LAYOUT_MASK) >> dim;
            LayoutContext.LayoutRect childRect = ctx.rects[child];

            switch (bFlags & LAY_HFILL) {
                case LAY_HCENTER:
                    float centerSize = childRect.get(dim) + (space - childRect.get(2 + dim)) / 2F - pchild.margins(wdim);
                    childRect.set(dim, centerSize);
                    break;
                case LAY_RIGHT:
                    float rightSize = childRect.get(dim) + space - childRect.get(2 + dim) - pchild.margins(dim) - pchild.margins(wdim);
                    childRect.set(dim, rightSize);
                    break;
                case LAY_HFILL:
                    float fillSize = Math.max(0, space - childRect.get(dim) - pchild.margins(wdim));
                    childRect.set(2 + dim, fillSize);
                    break;
                default:
                    break;
            }

            childRect.set(dim, childRect.get(dim) + offset);
            child = pchild.nextSibling;
        }
    }

    static void layArrangeOverlaySqueezedRange(@NotNull LayoutContext ctx, int dim, int startItem, int endItem, float offset, float space) {
        int wdim = dim + 2;
        int item = startItem;
        while (item != endItem) {
            LayoutItem pitem = layGetItem(ctx, item);
            final int bFlags = (pitem.flags & LAY_ITEM_LAYOUT_MASK) >> dim;
            LayoutContext.LayoutRect rect = ctx.rects[item];
            float minSize = Math.max(0, space - rect.get(dim) - pitem.margins(wdim));
            switch (bFlags & LAY_HFILL) {
                case LAY_HCENTER:
                    rect.set(2 + dim, Math.min(rect.get(2 + dim), minSize));
                    rect.set(dim, rect.get(dim) + (space - rect.get(2 + dim)) / 2F - pitem.margins(wdim));
                    break;
                case LAY_RIGHT:
                    rect.set(2 + dim, Math.min(rect.get(2 + dim), minSize));
                    rect.set(dim, space - rect.get(2 + dim) - pitem.margins(wdim));
                    break;
                case LAY_HFILL:
                    rect.set(2 + dim, minSize);
                    break;
                default:
                    rect.set(2 + dim, Math.min(rect.get(2 + dim), minSize));
                    break;
            }

            rect.set(dim, rect.get(dim) + offset);
            item = pitem.nextSibling;
        }
    }

    static float layArrangeWrappedOverlaySqueezed(@NotNull LayoutContext ctx, int item, int dim) {
        final int wdim = dim + 2;
        LayoutItem pitem = layGetItem(ctx, item);
        float offset = ctx.rects[item].get(dim);
        float needSize = 0F;
        int child = pitem.firstChild;
        int startChild = child;
        while (child != LAY_INVALID_ID) {
            LayoutItem pchild = layGetItem(ctx, child);
            if((pchild.flags & LAY_BREAK) > 0) {
                layArrangeOverlaySqueezedRange(ctx, dim, startChild, child, offset, needSize);
                offset += needSize;
                startChild = child;
                needSize = 0F;
            }
            LayoutContext.LayoutRect rect = ctx.rects[child];
            float childSize = rect.get(dim) + rect.get(2 + dim) + pchild.margins(wdim);
            needSize = Math.max(needSize, childSize);
            child = pchild.nextSibling;
        }
        layArrangeOverlaySqueezedRange(ctx, dim, startChild, LAY_INVALID_ID, offset, needSize);
        offset += needSize;
        return offset;
    }

    public static void layArrange(@NotNull LayoutContext ctx, int item, int dim) {
        LayoutItem pitem = layGetItem(ctx, item);

        final int flags = pitem.flags;

        switch (flags & LAY_ITEM_BOX_MODEL_MASK) {
            case LAY_COLUMN | LAY_WRAP:
                if(dim != 0) {
                    layArrangeStacked(ctx, item, 1, true);
                    float offset = layArrangeWrappedOverlaySqueezed(ctx, item, 0);
                    ctx.rects[item].set(2, offset - ctx.rects[item].get(0));
                }
                break;
            case LAY_ROW | LAY_WRAP:
                if(dim == 0) {
                    layArrangeStacked(ctx, item, 0, true);
                } else {
                    // discard return value
                    layArrangeWrappedOverlaySqueezed(ctx, item, 1);
                }
                break;
            case LAY_COLUMN:
            case LAY_ROW:
                if((flags & 1) == dim) {
                    layArrangeStacked(ctx, item, dim, false);
                } else {
                    LayoutContext.LayoutRect rect = ctx.rects[item];
                    layArrangeOverlaySqueezedRange(ctx, dim, pitem.firstChild, LAY_INVALID_ID, rect.get(dim), rect.get(2 + dim));
                }
                break;
            default:
                layArrangeOverlay(ctx, item, dim);
                break;
        }
        int child = pitem.firstChild;
        while (child != LAY_INVALID_ID) {
            layArrange(ctx, child, dim);
            LayoutItem pchild = layGetItem(ctx, child);
            child = pchild.nextSibling;
        }

    }

    private static void _clearItem(@NotNull LayoutItem item) {
        item.flags = 0;
        item.firstChild = 0;
        item.nextSibling = 0;
        item.marginLeft = 0.0F;
        item.marginTop = 0.0F;
        item.marginRight = 0.0F;
        item.marginBottom = 0.0F;
        item.sizeX = 0.0F;
        item.sizeY = 0.0F;
    }

    private static void _clearRect(@NotNull LayoutContext.LayoutRect rect) {
        rect.x = rect.y = rect.w = rect.h = 0.0F;
    }


    public static final class LayoutItem {

        int flags;
        int firstChild;
        int nextSibling;
        float marginLeft, marginTop, marginRight, marginBottom;
        float sizeX, sizeY;

        LayoutItem() {}

        public float margins(int i) {
            switch (i) {
                case 0: return marginLeft;
                case 1: return marginTop;
                case 2: return marginRight;
                case 3: return marginBottom;
                default: throw new IllegalArgumentException("Invalid index for accessing layout item margin component, should be [0;3], given: " + i);
            }
        }

        public float size(int i) {
            switch (i) {
                case 0: return sizeX;
                case 1: return sizeY;
                default: throw new IllegalArgumentException("Invalid index for accessing layout item size component, should be [0;1], given: " + i);
            }
        }
    }
}
