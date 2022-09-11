package io.github.layout.demo;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.app.Application;
import imgui.app.Configuration;
import imgui.flag.ImGuiCond;
import imgui.type.ImInt;
import io.github.layout.Layout;
import io.github.layout.LayoutContext;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.Nullable;
import io.github.layout.LayoutBoxFlags;
import io.github.layout.LayoutFlags;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main extends Application {

    private final LayoutContext ctx = new LayoutContext();
    private Component root;

    private final ImInt sidebarItems = new ImInt(15);
    private final ImInt contentItems = new ImInt(60);
    private final ImInt footerItems = new ImInt(9);


    @Override
    protected void configure(Configuration config) {
        config.setTitle("Layout Demo");
    }

    @Override
    public void process() {

        ImGui.setNextWindowSize(600, 500, ImGuiCond.Appearing);
        ImGui.begin("Layout Example");
        float windowWidth = ImGui.getWindowWidth();
        float windowHeight = ImGui.getWindowHeight();

        // It is not necessary to setup layout every frame. It can only be done on window resize, for example.
        setupLayout(windowWidth, windowHeight);

        float ox = ImGui.getWindowPosX();
        float oy = ImGui.getWindowPosY();

        draw(ox, oy, root);
        ImGui.end();

        ImGui.setNextWindowSize(300, 600, ImGuiCond.Appearing);
        ImGui.begin("Settings");

        ImGui.inputInt("Sidebar Items", sidebarItems);
        ImGui.inputInt("Content Items", contentItems);
        ImGui.inputInt("Footer Items", footerItems);

        ImGui.end();
    }

    private void draw(float ox, float oy, Component component) {
        float[] rect = Layout.layGetRect(ctx, component.layoutId, new float[4]);

        // imgui expects abgr, so we have to convert colors
        int argb = component.color;
        int abgr = argb & 0xFF000000
                | ((argb & 0XFF) << 16 )
                | argb & 0x0000FF00
                | ((argb >> 16) & 0xFF);
        ImDrawList drawList = ImGui.getWindowDrawList();

        float minX = ox + rect[0];
        float minY = oy + rect[1];
        float maxX = ox + rect[0] + rect[2];
        float maxY = oy + rect[1] + rect[3];
        drawList.addRectFilled(minX, minY, maxX, maxY, abgr);

        drawList.pushClipRect(minX, minY, maxX, maxY);
        for (Component child : component.children) {
            draw(ox, oy, child);
        }
        drawList.popClipRect();
    }

    private void setupLayout(float width, float height) {
        Component sidebar;
        Component contentContainer;
        Component footer;
        this.root = new Component()
                .setColor(0xFF2B2B2B)
                .setContain(LayoutBoxFlags.LAY_COLUMN)
                .setSize(width - 60, height - 60)
                .setMargins(30, 30, 30, 30)
                .addComponent(new Component()
                        .setColor(0)
                        .setBehave(LayoutFlags.LAY_HFILL | LayoutFlags.LAY_VFILL)
                        .setContain(LayoutBoxFlags.LAY_ROW)
                        .addComponent(new Component()
                                .setColor(0xFFB1772F)
                                .setMargins(10, 10, 10, 10)
                                .setSize(200, 0)
                                .setBehave(LayoutFlags.LAY_VFILL)
                                .addComponent(sidebar = new Component()
                                        .setColor(0)
                                        .setBehave(LayoutFlags.LAY_VFILL | LayoutFlags.LAY_HFILL)
                                        .setMargins(10, 20, 10, 20)
                                        .setContain(LayoutBoxFlags.LAY_START | LayoutBoxFlags.LAY_COLUMN | LayoutBoxFlags.LAY_JUSTIFY)))
                        .addComponent(new Component()
                                .setColor(0xFF49544A)
                                .setMargins(0, 10, 10, 10)
                                .setBehave(LayoutFlags.LAY_HFILL | LayoutFlags.LAY_VFILL)
                                .addComponent(contentContainer = new Component()
                                        .setColor(0)
                                        .setBehave(LayoutFlags.LAY_HFILL | LayoutFlags.LAY_VFILL)
                                        .setContain(LayoutBoxFlags.LAY_ROW | LayoutBoxFlags.LAY_WRAP | LayoutBoxFlags.LAY_START)
                                        .setMargins(20, 20, 20, 20)))
                )
                .addComponent(footer = new Component()
                        .setColor(0xFFBC5A2B)
                        .setSize(0, 80)
                        .setBehave(LayoutFlags.LAY_HFILL)
                        .setContain(LayoutBoxFlags.LAY_ROW));

        Random rand = new Random(0xCAFEBABE);
        for (int i = 0; i < sidebarItems.get(); i++) {
            sidebar.addComponent(new Component()
                    .setBehave(LayoutFlags.LAY_LEFT)
                    .setColor(0xFF5191A6)
                    .setSize(90 + rand.nextInt(60), 15)
                    .setMargins(0, 10, 0, 10));
        }

        rand.setSeed(0xCAFEBABE);
        for (int i = 0; i < contentItems.get(); i++) {
            contentContainer.addComponent(new Component()
                    .setColor(0xFFC75450)
                    .setMargins(10, 10, 10, 10)
                    .setBehave(i == 15 ? LayoutFlags.LAY_BREAK : 0)
                    .setSize(30 + rand.nextInt(30), 30));
        }

        rand.setSeed(0xCAFEBABE);
        for (int i = 0; i < footerItems.get(); i++) {
            footer.addComponent(new Component()
                    .setColor(0xFF87939A)
                    .setSize(30, 20 + rand.nextInt(20))
                    .setMargins(0, 0, 10, 0));
        }

        Layout.layResetContext(ctx);
        this.root.submitLayout(ctx);
        Layout.layRunContext(ctx);

    }

    public static void main(String[] args) {
        launch(new Main());
    }

    /**
     * Example wrapper around layout items
     */
    private static class Component {
        public @Nullable Component parent = null;
        public List<Component> children = new ArrayList<>();
        private int layoutId;

        private int containFlags = 0;
        private int behaveFlags = 0;
        private float width, height;
        private float ml, mt, mr, mb;

        public int color = -1;

        public Component() {}

        void submitLayout(LayoutContext ctx) {
            this.layoutId = Layout.layItem(ctx);
            Layout.laySetSize(ctx, this.layoutId, width, height);
            Layout.laySetMargins(ctx, this.layoutId, ml, mt, mr, mb);
            Layout.laySetContain(ctx, this.layoutId, containFlags);
            Layout.laySetBehave(ctx, this.layoutId, behaveFlags);

            if (this.parent != null) {
                Layout.layInsert(ctx, this.parent.layoutId, this.layoutId);
            }

            for (Component child : this.children) {
                child.submitLayout(ctx);
            }
        }

        public Component setColor(int color) {
            this.color = color;
            return this;
        }

        public Component setBehave(@MagicConstant(flagsFromClass = LayoutFlags.class) int flags) {
            this.behaveFlags = flags;
            return this;
        }

        public Component setContain(@MagicConstant(flagsFromClass = LayoutBoxFlags.class) int flags) {
            this.containFlags = flags;
            return this;
        }

        public Component setSize(float width, float height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Component setMargins(float left, float top, float right, float bottom) {
            this.ml = left;
            this.mt = top;
            this.mr = right;
            this.mb = bottom;
            return this;
        }

        public Component addComponent(Component component) {
            children.add(component);
            component.parent = this;
            return this;
        }
    }
}
