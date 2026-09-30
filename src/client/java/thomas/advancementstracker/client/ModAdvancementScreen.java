package thomas.advancementstracker.client;

import net.minecraft.advancements.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class ModAdvancementScreen extends Screen {

    private enum Status { DONE, IN_PROGRESS, NONE }

    private enum StatusFilter {
        ALL("Status: All"),
        COMPLETED("Status: Completed"),
        IN_PROGRESS("Status: In progress"),
        NOT_STARTED("Status: Not started");

        final String label;
        StatusFilter(String label) { this.label = label; }
        StatusFilter next() { return values()[(this.ordinal() + 1) % values().length]; }
    }

    private static final Map<String, String> CRITERION_OVERRIDES = new HashMap<>();
    static {
        // CRITERION_OVERRIDES.put("husbandry_obtain_wandering_trader", "Trade with a Wandering Trader");
    }

    private static StatusFilter lastStatusFilter = StatusFilter.ALL;
    private static int lastSelection = 0;
    private static @Nullable AdvancementHolder lastSelectedCategory = null;
    private static @Nullable AdvancementHolder lastSelectedAdvancement = null;
    private static int lastListScrollOffset = 0;
    private static int lastDetailScrollOffset = 0;
    private static int lastTabsScrollOffset = 0;
    private static String lastSearchQuery = "";

    private static final int FRAME_COLOR = 0xFFC6C6C6;
    private static final int WELL_COLOR = 0xFF262626;
    private static final int TITLE_COLOR = 0xFF404040;
    private static final int ROW_COLOR = 0xFF3D3D3D;
    private static final int ROW_SELECTED_COLOR = 0xFF4A4A6E;
    private static final int TAB_COLOR = 0xFF6E6E6E;
    private static final int TAB_SELECTED_COLOR = 0xFFE2E2E2;
    private static final int TAB_SELECTED_TEXT_COLOR = 0xFF404040;
    private static final int DONE_COLOR = 0xFF55FF55;
    private static final int PROGRESS_COLOR = 0xFFFFFF55;
    private static final int NONE_COLOR = 0xFFD0D0D0;
    private static final int MUTED_COLOR = 0xFFAAAAAA;
    private static final int SLOT_BG_COLOR = 0xFF171717;
    private static final int FAVORITE_COLOR = 0xFFFFD700;
    private static final int FAVORITE_EMPTY_COLOR = 0xFF555555;

    private static final int FRAME_HI = 0xFFFFFFFF;
    private static final int FRAME_LO = 0xFF555555;
    private static final int WELL_HI = 0xFFFFFFFF;
    private static final int WELL_LO = 0xFF373737;
    private static final int PLATE_HI = 0xFF6E6E6E;
    private static final int PLATE_LO = 0xFF1C1C1C;
    private static final int PROGRESS_TRACK_RING = 0xFF3A3A3A;

    private static final int BUTTON_ROW_HEIGHT = 26;
    private static final int SEARCH_ROW_HEIGHT = 24;
    private static final int HEADER_HEIGHT = BUTTON_ROW_HEIGHT + SEARCH_ROW_HEIGHT;
    private static final int ROW_HEIGHT = 22;
    private static final int TAB_HEIGHT = 30;
    private static final int ICON_SIZE = 16;
    private static final int STAR_SIZE = 10;
    private static final int DETAIL_STAR_SIZE = 12;
    private static final int TABS_WIDTH = 150;
    private static final int LIST_WIDTH_PERCENT = 40;
    private static final int PANEL_MAX_WIDTH = 640;
    private static final int PANEL_MAX_HEIGHT = 400;
    private static final int PANEL_MARGIN = 20;
    private static final int LINE_HEIGHT = 11;
    private static final int PROGRESS_BAR_HEIGHT = 14;
    private static final int WELL_TOP_PADDING = 2;
    private static final int TEXT_PADDING_RIGHT = 6;

    private static final long MARQUEE_PAUSE_MS = 800;
    private static final long MARQUEE_SCROLL_MS = 2500;

    private final List<AdvancementNode> allAdvancements = new ArrayList<>();
    private final Map<AdvancementNode, AdvancementProgress> progressByNode = new LinkedHashMap<>();
    private final Map<AdvancementHolder, AdvancementNode> rootsByHolder = new LinkedHashMap<>();
    private final List<AdvancementHolder> categories = new ArrayList<>();

    private AdvancementNode selectedNode;
    private int listScrollOffset;
    private int detailScrollOffset;
    private int tabsScrollOffset;
    private StatusFilter statusFilter;
    private int selection = 0;

    private EditBox searchBox;

    private record CriteriaLine(FormattedCharSequence text, int color, int indent) { }

    private static boolean lastMissingOnly = false;

    private static final int SCROLLBAR_WIDTH = 6;
    private static final int TOGGLE_HEIGHT = 14;

    private boolean missingOnly;
    private int toggleX, toggleY, toggleW, toggleH;

    private int panelX, panelY, panelWidth, panelHeight;
    private int tabsX, tabsY, tabsWidth, tabsBottom;
    private int listX, listY, listWidth, listBottom;
    private int detailX;
    private int detailWidth;

    public ModAdvancementScreen(Component title) {
        super(title);
        this.statusFilter = lastStatusFilter;
        this.listScrollOffset = lastListScrollOffset;
        this.detailScrollOffset = lastDetailScrollOffset;
        this.tabsScrollOffset = lastTabsScrollOffset;
        this.missingOnly = lastMissingOnly;
    }

    @Override
    protected void init() {
        this.panelWidth = Math.min(PANEL_MAX_WIDTH, this.width - PANEL_MARGIN * 2);
        this.panelHeight = Math.min(PANEL_MAX_HEIGHT, this.height - PANEL_MARGIN * 2);
        this.panelX = (this.width - this.panelWidth) / 2;
        this.panelY = (this.height - this.panelHeight) / 2;

        this.tabsX = this.panelX;
        this.tabsY = this.panelY + HEADER_HEIGHT;
        this.tabsWidth = TABS_WIDTH;
        this.tabsBottom = this.panelY + this.panelHeight;

        int remainingWidth = this.panelWidth - this.tabsWidth - 2;
        this.listWidth = remainingWidth * LIST_WIDTH_PERCENT / 100;
        this.listX = this.tabsX + this.tabsWidth + 2;
        this.listY = this.tabsY;
        this.listBottom = this.tabsBottom;
        this.detailX = this.listX + this.listWidth + 2;
        this.detailWidth = this.panelX + this.panelWidth - this.detailX;

        int buttonWidth = 140;

        Button statusButton = Button.builder(
                Component.literal(this.statusFilter.label),
                (btn) -> {
                    this.statusFilter = this.statusFilter.next();
                    btn.setMessage(Component.literal(this.statusFilter.label));
                    this.listScrollOffset = 0;
                }
        ).bounds(this.panelX + 4, this.panelY + 3, buttonWidth, 20).build();
        this.addRenderableWidget(statusButton);

        Button backToVanillaButton = Button.builder(
                Component.literal("Vanilla screen"),
                (_) -> {
                    assert this.minecraft.player != null;
                    this.minecraft.gui.setScreen(
                            new AdvancementsScreen(this.minecraft.player.connection.getAdvancements())
                    );
                }
        ).bounds(this.panelX + this.panelWidth - buttonWidth - 4, this.panelY + 3, buttonWidth, 20).build();
        this.addRenderableWidget(backToVanillaButton);

        this.searchBox = new EditBox(
                this.font,
                this.panelX + 4,
                this.panelY + BUTTON_ROW_HEIGHT + 2,
                this.panelWidth - 8,
                20,
                Component.literal("Search")
        );
        this.searchBox.setValue(lastSearchQuery);
        this.searchBox.setResponder(_ -> this.listScrollOffset = 0);
        this.searchBox.setHint(Component.literal("Search advancements..."));
        this.addRenderableWidget(this.searchBox);

        if (this.allAdvancements.isEmpty()) {
            assert this.minecraft.player != null;

            var clientAdvancements = this.minecraft.player.connection.getAdvancements();
            var tree = clientAdvancements.tree();
            Map<AdvancementHolder, AdvancementProgress> progressMap = clientAdvancements.progress();

            List<AdvancementNode> inTreeOrder = new ArrayList<>();
            for (AdvancementNode root : tree.roots()) {
                inTreeOrder.add(root);
            }
            for (AdvancementNode task : tree.tasks()) {
                inTreeOrder.add(task);
            }

            for (AdvancementNode node : inTreeOrder) {
                if (node.advancement().display().isPresent()) {
                    this.allAdvancements.add(node);
                    AdvancementProgress progress = progressMap.get(node.holder());
                    if (progress != null) {
                        this.progressByNode.put(node, progress);
                    }

                    AdvancementNode root = node.root();
                    this.rootsByHolder.putIfAbsent(root.holder(), root);
                }
            }

            this.categories.addAll(this.rootsByHolder.keySet());
            this.categories.sort(Comparator.comparing(this::rootTitle));

            if (lastSelection == 1) {
                this.selection = 1;
            } else if (lastSelectedCategory != null) {
                int foundIndex = this.categories.indexOf(lastSelectedCategory);
                this.selection = foundIndex >= 0 ? foundIndex + 2 : 0;
            } else {
                this.selection = 0;
            }

            if (lastSelectedAdvancement != null) {
                for (AdvancementNode node : this.allAdvancements) {
                    if (node.holder().equals(lastSelectedAdvancement)) {
                        this.selectedNode = node;
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void removed() {
        lastStatusFilter = this.statusFilter;
        lastSelection = this.selection;
        lastSelectedCategory = this.selection >= 2 ? this.categories.get(this.selection - 2) : null;
        lastSelectedAdvancement = this.selectedNode == null ? null : this.selectedNode.holder();
        lastListScrollOffset = this.listScrollOffset;
        lastDetailScrollOffset = this.detailScrollOffset;
        lastTabsScrollOffset = this.tabsScrollOffset;
        lastSearchQuery = this.searchBox.getValue();
        lastMissingOnly = this.missingOnly;
    }

    private String rootTitle(AdvancementHolder rootHolder) {
        AdvancementNode root = this.rootsByHolder.get(rootHolder);
        return root.advancement().display().map(d -> d.title().getString()).orElse("Unknown");
    }

    private boolean isFavorite(AdvancementHolder holder) {
        return FavoritesStore.contains(holder.id().toString()); // GUESS: 90% holder.id()
    }

    private void toggleFavorite(AdvancementHolder holder) {
        FavoritesStore.toggle(holder.id().toString()); // GUESS: 90% holder.id()
    }

    private Set<String> remainingOf(AdvancementNode node) {
        AdvancementProgress progress = this.progressByNode.get(node);
        if (progress == null) {
            // No progress known yet: nothing is done
            return new HashSet<>(node.advancement().requirements().names());
        }
        Set<String> remaining = new HashSet<>();
        progress.getRemainingCriteria().forEach(remaining::add);
        return remaining;
    }

    private List<List<String>> groupsOf(AdvancementNode node) {
        // Each inner list is one group: one of its criteria is enough (OR). A group of size 1 is a normal criterion.
        return node.advancement().requirements().requirements();
    }

    private boolean isGroupDone(List<String> group, Set<String> remaining) {
        for (String criterion : group) {
            if (!remaining.contains(criterion)) return true;
        }
        return false;
    }

    private Status statusOf(AdvancementNode node) {
        AdvancementProgress progress = this.progressByNode.get(node);
        if (progress == null) return Status.NONE;
        if (progress.isDone()) return Status.DONE;
        return progressCounts(node)[0] > 0 ? Status.IN_PROGRESS : Status.NONE;
    }

    private int[] progressCounts(AdvancementNode node) {
        List<List<String>> groups = groupsOf(node);
        AdvancementProgress progress = this.progressByNode.get(node);
        if (progress != null && progress.isDone()) return new int[]{groups.size(), groups.size()};

        Set<String> remaining = remainingOf(node);
        int done = 0;
        for (List<String> group : groups) {
            if (isGroupDone(group, remaining)) done++;
        }
        return new int[]{done, groups.size()};
    }

    private int colorFor(Status status) {
        return switch (status) {
            case DONE -> DONE_COLOR;
            case IN_PROGRESS -> PROGRESS_COLOR;
            case NONE -> NONE_COLOR;
        };
    }

    /** tabIndex: 0 = All, 1 = Favorites, 2+ = categories.get(tabIndex - 2) */
    private int[] tabCounts(int tabIndex) {
        int done = 0, total = 0;
        for (AdvancementNode node : this.allAdvancements) {
            boolean matches = switch (tabIndex) {
                case 0 -> true;
                case 1 -> isFavorite(node.holder());
                default -> node.root().holder().equals(this.categories.get(tabIndex - 2));
            };
            if (!matches) continue;
            total++;
            if (statusOf(node) == Status.DONE) done++;
        }
        return new int[]{done, total};
    }

    private List<AdvancementNode> visibleAdvancements() {
        String query = this.searchBox == null ? "" : this.searchBox.getValue().toLowerCase(Locale.ROOT);
        List<AdvancementNode> filtered = new ArrayList<>();

        for (AdvancementNode node : this.allAdvancements) {
            Status status = statusOf(node);
            boolean statusMatches = switch (this.statusFilter) {
                case ALL -> true;
                case COMPLETED -> status == Status.DONE;
                case IN_PROGRESS -> status == Status.IN_PROGRESS;
                case NOT_STARTED -> status == Status.NONE;
            };

            boolean tabMatches = switch (this.selection) {
                case 0 -> true;
                case 1 -> isFavorite(node.holder());
                default -> node.root().holder().equals(this.categories.get(this.selection - 2));
            };

            boolean searchMatches = query.isEmpty()
                    || node.advancement().display().get().title().getString().toLowerCase(Locale.ROOT).contains(query);

            if (statusMatches && tabMatches && searchMatches) filtered.add(node);
        }
        return filtered;
    }

    private String humanizeCriterion(String raw) {
        String override = CRITERION_OVERRIDES.get(raw);
        if (override != null) return override;

        String cleaned = raw.contains(":") ? raw.substring(raw.indexOf(':') + 1) : raw;
        cleaned = cleaned.replace('_', ' ').replace('/', ' ').trim();
        if (cleaned.isEmpty()) return raw;

        StringBuilder sb = new StringBuilder();
        for (String word : cleaned.split(" ")) {
            if (word.isEmpty()) continue;
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return sb.toString();
    }

    private void drawBevelPanel(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int baseColor, int highlight, int shadow, boolean sunken) {
        graphics.fill(x, y, x + w, y + h, baseColor);
        graphics.outline(x, y, w, h, 0xFF000000);

        int topLeft = sunken ? shadow : highlight;
        int bottomRight = sunken ? highlight : shadow;
        int t = 2;

        graphics.fill(x + 1, y + 1, x + w - 1, y + 1 + t, topLeft);
        graphics.fill(x + 1, y + 1, x + 1 + t, y + h - 1, topLeft);
//        graphics.fill(x + 1, y + h - 1 - t, x + w - 1, y + h - 1, bottomRight);
//        graphics.fill(x + w - 1 - t, y + 1, x + w - 1, y + h - 1, bottomRight);
    }

    private void drawIconSlot(GuiGraphicsExtractor graphics, int x, int y, net.minecraft.world.item.ItemStack item, int statusColor) {
        graphics.fill(x - 2, y - 2, x + ICON_SIZE + 2, y + ICON_SIZE + 2, SLOT_BG_COLOR);
        graphics.outline(x - 2, y - 2, ICON_SIZE + 4, ICON_SIZE + 4, statusColor);
        graphics.item(item, x, y);
    }

    private void drawFavoriteStar(GuiGraphicsExtractor graphics, int x, int y, int size, boolean isFavorite) {
        graphics.fill(x, y, x + size, y + size, isFavorite ? FAVORITE_COLOR : 0xFF222222);
        graphics.outline(x, y, size, size, isFavorite ? 0xFF806000 : FAVORITE_EMPTY_COLOR);
    }

    private void drawProgressBar(GuiGraphicsExtractor graphics, int x, int y, int width, int done, int total, Status status) {
        graphics.fill(x, y, x + width, y + PROGRESS_BAR_HEIGHT, 0xFF151515);
        graphics.outline(x, y, width, PROGRESS_BAR_HEIGHT, 0xFF000000);
        graphics.outline(x + 1, y + 1, width - 2, PROGRESS_BAR_HEIGHT - 2, PROGRESS_TRACK_RING);

        float fraction = total == 0 ? 0f : (float) done / total;
        int fillWidth = Math.max(0, (int) ((width - 6) * fraction));
        if (fillWidth > 0) {
            graphics.fill(x + 3, y + 3, x + 3 + fillWidth, y + PROGRESS_BAR_HEIGHT - 3, colorFor(status));
        }

        String label = done + "/" + total;
        int labelY = y + (PROGRESS_BAR_HEIGHT - this.font.lineHeight) / 2;
        graphics.text(this.font, label, x + width - this.font.width(label) - 4, labelY, 0xFFFFFFFF, true);
    }

    private String typeLabel(AdvancementType type) {
        String raw = type.name();
        return raw.substring(0, 1).toUpperCase() + raw.substring(1).toLowerCase().replace('_', ' ');
    }

    private void drawMarqueeText(GuiGraphicsExtractor graphics, FormattedCharSequence text, int x, int y, int maxWidth, int color, boolean dropShadow) {
        int textWidth = this.font.width(text);
        if (textWidth <= maxWidth) {
            graphics.text(this.font, text, x, y, color, dropShadow);
            return;
        }

        int overflow = textWidth - maxWidth;
        long cycleLength = 2 * (MARQUEE_PAUSE_MS + MARQUEE_SCROLL_MS);
        long t = System.currentTimeMillis() % cycleLength;

        float progress;
        if (t < MARQUEE_PAUSE_MS) {
            progress = 0f;
        } else if (t < MARQUEE_PAUSE_MS + MARQUEE_SCROLL_MS) {
            progress = (t - MARQUEE_PAUSE_MS) / (float) MARQUEE_SCROLL_MS;
        } else if (t < 2 * MARQUEE_PAUSE_MS + MARQUEE_SCROLL_MS) {
            progress = 1f;
        } else {
            progress = 1f - (t - 2 * MARQUEE_PAUSE_MS - MARQUEE_SCROLL_MS) / (float) MARQUEE_SCROLL_MS;
        }

        int scrollX = (int) (overflow * progress);
        graphics.enableScissor(x, y - 1, x + maxWidth, y + this.font.lineHeight + 1);
        graphics.text(this.font, text, x - scrollX, y, color, dropShadow);
        graphics.disableScissor();
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int panelRight = this.panelX + this.panelWidth;

        drawBevelPanel(graphics, this.panelX, this.panelY, this.panelWidth, this.panelHeight, FRAME_COLOR, FRAME_HI, FRAME_LO, false);

        super.extractRenderState(graphics, mouseX, mouseY, delta);

        graphics.text(this.font, this.getTitle(), this.panelX + 8, this.panelY + (BUTTON_ROW_HEIGHT - this.font.lineHeight) / 2, TITLE_COLOR, false);

        drawBevelPanel(graphics, this.tabsX, this.tabsY, this.tabsWidth, this.tabsBottom - this.tabsY, WELL_COLOR, WELL_HI, WELL_LO, true);
        drawBevelPanel(graphics, this.listX, this.listY, this.listWidth, this.listBottom - this.listY, WELL_COLOR, WELL_HI, WELL_LO, true);
        drawBevelPanel(graphics, this.detailX, this.listY, panelRight - this.detailX, this.listBottom - this.listY, WELL_COLOR, WELL_HI, WELL_LO, true);

        renderTabs(graphics);
        renderList(graphics);

        if (this.selectedNode != null) {
            renderDetailPanel(graphics, panelRight);
        } else {
            graphics.text(this.font, Component.literal("Select an advancement on the left."), this.detailX + 8, this.listY + 8, MUTED_COLOR, false);
        }

        graphics.outline(this.panelX, this.panelY, this.panelWidth, this.panelHeight, 0xFF000000);
    }

    private void renderTabs(GuiGraphicsExtractor graphics) {
        int contentTop = this.tabsY + WELL_TOP_PADDING;

        graphics.enableScissor(this.tabsX, this.tabsY, this.tabsX + this.tabsWidth, this.tabsBottom);
        int y = contentTop - this.tabsScrollOffset;

        y = renderOneTab(graphics, y, 0, "All advancements", null);
        y = renderOneTab(graphics, y, 1, "Favorites", null);
        for (int i = 0; i < this.categories.size(); i++) {
            AdvancementHolder category = this.categories.get(i);
            y = renderOneTab(graphics, y, i + 2, rootTitle(category), category);
        }
        graphics.disableScissor();
    }

    private int renderOneTab(GuiGraphicsExtractor graphics, int y, int tabIndex, String name, @Nullable AdvancementHolder categoryOrNull) {
        if (y + TAB_HEIGHT >= this.tabsY && y <= this.tabsBottom) {
            boolean isSelected = this.selection == tabIndex;
            drawBevelPanel(graphics, this.tabsX + 3, y, this.tabsWidth - 6, TAB_HEIGHT - 3,
                    isSelected ? TAB_SELECTED_COLOR : TAB_COLOR, PLATE_HI, PLATE_LO, false);

            int[] counts = tabCounts(tabIndex);
            int textColor = isSelected ? TAB_SELECTED_TEXT_COLOR : 0xFFFFFFFF;

            if (categoryOrNull != null) {
                AdvancementNode root = this.rootsByHolder.get(categoryOrNull);
                DisplayInfo display = root.advancement().display().get();
                graphics.item(display.icon().create(), this.tabsX + 8, y + (TAB_HEIGHT - 3 - ICON_SIZE) / 2);
            }

            int textX = this.tabsX + 8 + (categoryOrNull != null ? ICON_SIZE + 6 : 0);
            int availableWidth = this.tabsWidth - (textX - this.tabsX) - TEXT_PADDING_RIGHT;

            drawMarqueeText(graphics, Component.literal(name).getVisualOrderText(), textX, y + 4, availableWidth, textColor, false);
            String countText = counts[0] + "/" + counts[1];
            graphics.text(this.font, countText, textX, y + 4 + LINE_HEIGHT, textColor, false);
        }
        return y + TAB_HEIGHT;
    }

    private void renderList(GuiGraphicsExtractor graphics) {
        List<AdvancementNode> visible = visibleAdvancements();
        int contentTop = this.listY + WELL_TOP_PADDING;

        graphics.enableScissor(this.listX, this.listY, this.listX + this.listWidth, this.listBottom);
        int y = contentTop - this.listScrollOffset;
        for (AdvancementNode node : visible) {
            if (y + ROW_HEIGHT >= this.listY && y <= this.listBottom) {
                DisplayInfo display = node.advancement().display().get();
                Status status = statusOf(node);
                boolean isSelected = node == this.selectedNode;

                drawBevelPanel(graphics, this.listX + 2, y, this.listWidth - 4, ROW_HEIGHT - 2,
                        isSelected ? ROW_SELECTED_COLOR : ROW_COLOR, PLATE_HI, PLATE_LO, false);

                int starX = this.listX + 6;
                int starY = y + (ROW_HEIGHT - 2 - STAR_SIZE) / 2;
                drawFavoriteStar(graphics, starX, starY, STAR_SIZE, isFavorite(node.holder()));

                int iconX = starX + STAR_SIZE + 6;
                int iconY = y + (ROW_HEIGHT - 2 - ICON_SIZE) / 2;
                drawIconSlot(graphics, iconX, iconY, display.icon().create(), colorFor(status));

                int textX = iconX + ICON_SIZE + 10;
                int availableWidth = this.listX + this.listWidth - textX - TEXT_PADDING_RIGHT;
                drawMarqueeText(graphics, display.title().getVisualOrderText(), textX, y + 6, availableWidth, colorFor(status), false);
            }
            y += ROW_HEIGHT;
        }
        graphics.disableScissor();
    }

    private void renderDetailPanel(GuiGraphicsExtractor graphics, int panelRight) {
        DisplayInfo display = this.selectedNode.advancement().display().get();
        Status status = statusOf(this.selectedNode);
        int[] counts = progressCounts(this.selectedNode);

        int starX = this.detailX + 8;
        int starY = this.listY + WELL_TOP_PADDING + 5;
        drawFavoriteStar(graphics, starX, starY + (ICON_SIZE - DETAIL_STAR_SIZE) / 2, DETAIL_STAR_SIZE,
                isFavorite(this.selectedNode.holder()));

        int iconX = starX + DETAIL_STAR_SIZE + 8;
        drawIconSlot(graphics, iconX, starY, display.icon().create(), colorFor(status));

        int titleX = iconX + ICON_SIZE + 10;
        int titleAvailableWidth = panelRight - titleX - 8;
        drawMarqueeText(graphics, display.title().getVisualOrderText(), titleX, starY + 2, titleAvailableWidth, colorFor(status), true);

        int barY = starY + ICON_SIZE + 10;
        drawProgressBar(graphics, this.detailX + 8, barY, this.detailWidth - 16, counts[0], counts[1], status);

        int wrapWidth = panelRight - this.detailX - 16;
        int contentBottom = this.listBottom - 2;
        int y = barY + PROGRESS_BAR_HEIGHT + 6;

        // --- Fixed part: description + type ---
        for (FormattedCharSequence line : this.font.split(display.description(), wrapWidth)) {
            graphics.text(this.font, line, this.detailX + 8, y, MUTED_COLOR, false);
            y += LINE_HEIGHT;
        }
        graphics.text(this.font, "Type: " + typeLabel(display.type()), this.detailX + 8, y, TITLE_COLOR, false);
        y += LINE_HEIGHT + 4;

        // --- Fixed part: criteria header + "missing only" toggle ---
        graphics.text(this.font, "Criteria (" + counts[0] + "/" + counts[1] + "):",
                this.detailX + 8, y + (TOGGLE_HEIGHT - this.font.lineHeight) / 2, TITLE_COLOR, false);

        String toggleLabel = (this.missingOnly ? "[x] " : "[ ] ") + "Missing only";
        this.toggleW = this.font.width(toggleLabel) + 12;
        this.toggleH = TOGGLE_HEIGHT;
        this.toggleX = panelRight - 8 - this.toggleW;
        this.toggleY = y;
        drawBevelPanel(graphics, this.toggleX, this.toggleY, this.toggleW, this.toggleH,
                this.missingOnly ? TAB_SELECTED_COLOR : TAB_COLOR, PLATE_HI, PLATE_LO, false);
        graphics.text(this.font, toggleLabel, this.toggleX + 6,
                this.toggleY + (this.toggleH - this.font.lineHeight + 4) / 2,
                this.missingOnly ? TAB_SELECTED_TEXT_COLOR : 0xFFFFFFFF, false);
        y += TOGGLE_HEIGHT + 4;

        // --- Scrolling part: only the criteria ---
        int areaTop = y;
        int areaHeight = Math.max(0, contentBottom - areaTop);
        int textWidth = wrapWidth - SCROLLBAR_WIDTH - 4;

        List<CriteriaLine> lines = buildCriteriaLines(this.selectedNode, textWidth);
        int contentHeight = lines.size() * LINE_HEIGHT;
        int maxScroll = Math.max(0, contentHeight - areaHeight);
        this.detailScrollOffset = Math.clamp(this.detailScrollOffset, 0, maxScroll);

        graphics.enableScissor(this.detailX, areaTop, panelRight, contentBottom);
        int lineY = areaTop - this.detailScrollOffset;
        for (CriteriaLine line : lines) {
            if (lineY + LINE_HEIGHT >= areaTop && lineY <= contentBottom) {
                graphics.text(this.font, line.text(), this.detailX + 8 + line.indent(), lineY, line.color(), false);
            }
            lineY += LINE_HEIGHT;
        }
        graphics.disableScissor();

        // --- Scrollbar (only when there is something to scroll) ---
        if (maxScroll > 0) {
            int trackX = panelRight - SCROLLBAR_WIDTH - 4;
            graphics.fill(trackX, areaTop, trackX + SCROLLBAR_WIDTH, areaTop + areaHeight, SLOT_BG_COLOR);
            int thumbHeight = Math.max(16, areaHeight * areaHeight / contentHeight);
            int thumbY = areaTop + (int) ((long) (areaHeight - thumbHeight) * this.detailScrollOffset / maxScroll);
            graphics.fill(trackX, thumbY, trackX + SCROLLBAR_WIDTH, thumbY + thumbHeight, FRAME_COLOR);
            graphics.outline(trackX, thumbY, SCROLLBAR_WIDTH, thumbHeight, 0xFF000000);
        }
    }

    private List<CriteriaLine> buildCriteriaLines(AdvancementNode node, int width) {
        List<CriteriaLine> lines = new ArrayList<>();
        Set<String> remaining = remainingOf(node);

        for (List<String> group : groupsOf(node)) {
            boolean groupDone = isGroupDone(group, remaining);
            if (this.missingOnly && groupDone) continue;

            if (group.size() == 1) {
                // Normal criterion (AND)
                String criterion = group.getFirst();
                addWrapped(lines, (groupDone ? "[x] " : "[ ] ") + humanizeCriterion(criterion),
                        width, 0, groupDone ? DONE_COLOR : NONE_COLOR);
            } else {
                // OR group: only ONE of these is needed
                addWrapped(lines, (groupDone ? "[x] " : "[ ] ") + "Any one of:",
                        width, 0, groupDone ? DONE_COLOR : NONE_COLOR);
                for (String criterion : group) {
                    boolean optionDone = !remaining.contains(criterion);
                    int color = optionDone ? DONE_COLOR : (groupDone ? MUTED_COLOR : NONE_COLOR);
                    addWrapped(lines, "- " + humanizeCriterion(criterion), width - 10, 10, color);
                }
            }
        }

        if (lines.isEmpty()) {
            addWrapped(lines, this.missingOnly ? "Nothing missing!" : "No criteria.", width, 0, MUTED_COLOR);
        }
        return lines;
    }

    private void addWrapped(List<CriteriaLine> lines, String text, int width, int indent, int color) {
        for (FormattedCharSequence part : this.font.split(Component.literal(text), width)) {
            lines.add(new CriteriaLine(part, color, indent));
        }
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();

        if (this.selectedNode != null
                && mouseX >= this.toggleX && mouseX < this.toggleX + this.toggleW
                && mouseY >= this.toggleY && mouseY < this.toggleY + this.toggleH) {
            this.missingOnly = !this.missingOnly;
            this.detailScrollOffset = 0;
            return true;
        }

        if (mouseX >= this.tabsX && mouseX < this.tabsX + this.tabsWidth && mouseY >= this.tabsY) {
            int index = (int) ((mouseY - this.tabsY - WELL_TOP_PADDING + this.tabsScrollOffset) / TAB_HEIGHT);
            int totalTabs = this.categories.size() + 2;
            if (index >= 0 && index < totalTabs) {
                this.selection = index;
                this.listScrollOffset = 0;
                return true;
            }
        }

        if (mouseX >= this.listX && mouseX < this.listX + this.listWidth && mouseY >= this.listY) {
            int index = (int) ((mouseY - this.listY - WELL_TOP_PADDING + this.listScrollOffset) / ROW_HEIGHT);
            List<AdvancementNode> visible = visibleAdvancements();
            if (index >= 0 && index < visible.size()) {
                AdvancementNode node = visible.get(index);

                int starX = this.listX + 6;
                if (mouseX >= starX && mouseX < starX + STAR_SIZE) {
                    toggleFavorite(node.holder());
                } else {
                    this.selectedNode = node;
                    this.detailScrollOffset = 0;
                }
                return true;
            }
        }

        if (this.selectedNode != null) {
            int starX = this.detailX + 8;
            int starY = this.listY + WELL_TOP_PADDING + 5 + (ICON_SIZE - DETAIL_STAR_SIZE) / 2;
            if (mouseX >= starX && mouseX < starX + DETAIL_STAR_SIZE && mouseY >= starY && mouseY < starY + DETAIL_STAR_SIZE) {
                toggleFavorite(this.selectedNode.holder());
                return true;
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        boolean overTabs = mouseX >= this.tabsX && mouseX < this.tabsX + this.tabsWidth
                && mouseY >= this.tabsY && mouseY < this.tabsBottom;
        boolean overList = mouseX >= this.listX && mouseX < this.listX + this.listWidth
                && mouseY >= this.listY && mouseY < this.listBottom;
        boolean overDetail = mouseX >= this.detailX && mouseY >= this.listY && mouseY < this.listBottom;

        if (overTabs) {
            this.tabsScrollOffset -= (int) (scrollY * TAB_HEIGHT);
            int totalTabs = this.categories.size() + 2;
            int maxScroll = Math.max(0, totalTabs * TAB_HEIGHT - (this.tabsBottom - this.tabsY));
            this.tabsScrollOffset = Math.clamp(this.tabsScrollOffset, 0, maxScroll);
            return true;
        } else if (overList) {
            this.listScrollOffset -= (int) (scrollY * ROW_HEIGHT);
            int maxScroll = Math.max(0, visibleAdvancements().size() * ROW_HEIGHT - (this.listBottom - this.listY));
            this.listScrollOffset = Math.clamp(this.listScrollOffset, 0, maxScroll);
            return true;
        } else if (overDetail && this.selectedNode != null) {
            this.detailScrollOffset -= (int) (scrollY * LINE_HEIGHT * 3);
            if (this.detailScrollOffset < 0) this.detailScrollOffset = 0;
            return true;
        }
        return false;
    }
}