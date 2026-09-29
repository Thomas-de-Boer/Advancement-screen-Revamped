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

    // Session-only "remember where I was" state - resets on full game restart, not on screen close.
    private static StatusFilter lastStatusFilter = StatusFilter.ALL;
    private static int lastSelection = 0; // 0 = All, 1 = Favorites, 2+ = category
    private static @Nullable AdvancementHolder lastSelectedCategory = null;
    private static @Nullable AdvancementHolder lastSelectedAdvancement = null;
    private static int lastListScrollOffset = 0;
    private static int lastDetailScrollOffset = 0;
    private static int lastTabsScrollOffset = 0;
    private static String lastSearchQuery = "";

    // Favorites are their own persistent set (session-only, same caveat as above).
    private static final Set<AdvancementHolder> favoriteAdvancements = new HashSet<>();

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
    private static final int WELL_TOP_PADDING = 3;
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
    private int selection = 0; // 0 = All, 1 = Favorites, 2+ = categories.get(selection - 2)

    private EditBox searchBox; // GUESS: 55% - constructor signature, see note below the code

    private int panelX, panelY, panelWidth, panelHeight;
    private int tabsX, tabsY, tabsWidth, tabsBottom;
    private int listX, listY, listWidth, listBottom;
    private int detailX, detailWidth, detailContentY;

    public ModAdvancementScreen(Component title) {
        super(title);
        this.statusFilter = lastStatusFilter;
        this.listScrollOffset = lastListScrollOffset;
        this.detailScrollOffset = lastDetailScrollOffset;
        this.tabsScrollOffset = lastTabsScrollOffset;
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
        this.detailContentY = this.listY + 64;

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

        // Search box, second header row. Constructor signature is a guess - see note below.
        this.searchBox = new EditBox(
                this.font,
                this.panelX + 4,
                this.panelY + BUTTON_ROW_HEIGHT + 2,
                this.panelWidth - 8,
                20,
                Component.literal("Search")
        );
        this.searchBox.setValue(lastSearchQuery); // GUESS: 60% - setValue(String) method name
        this.searchBox.setResponder(_ -> this.listScrollOffset = 0); // GUESS: 45% - setResponder(Consumer<String>) may not exist; see fallback note
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

            // Restore previous selection (All / Favorites / a specific category).
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
        lastSearchQuery = this.searchBox.getValue(); // GUESS: 60% - getValue() method name
    }

    private String rootTitle(AdvancementHolder rootHolder) {
        AdvancementNode root = this.rootsByHolder.get(rootHolder);
        return root.advancement().display().map(d -> d.title().getString()).orElse("Unknown");
    }

    private void toggleFavorite(AdvancementHolder holder) {
        if (!favoriteAdvancements.remove(holder)) {
            favoriteAdvancements.add(holder);
        }
    }

    private Status statusOf(AdvancementNode node) {
        AdvancementProgress progress = this.progressByNode.get(node);
        if (progress == null) return Status.NONE;
        if (progress.isDone()) return Status.DONE;

        int total = node.advancement().requirements().names().size();
        Set<String> remaining = new HashSet<>();
        progress.getRemainingCriteria().forEach(remaining::add);
        int done = total - remaining.size();

        return done > 0 ? Status.IN_PROGRESS : Status.NONE;
    }

    private int[] progressCounts(AdvancementNode node) {
        int total = node.advancement().requirements().names().size();
        AdvancementProgress progress = this.progressByNode.get(node);
        if (progress == null) return new int[]{0, total};
        if (progress.isDone()) return new int[]{total, total};
        Set<String> remaining = new HashSet<>();
        progress.getRemainingCriteria().forEach(remaining::add);
        return new int[]{total - remaining.size(), total};
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
                case 1 -> favoriteAdvancements.contains(node.holder());
                default -> node.root().holder().equals(this.categories.get(tabIndex - 2));
            };
            if (!matches) continue;
            total++;
            if (statusOf(node) == Status.DONE) done++;
        }
        return new int[]{done, total};
    }

    private List<AdvancementNode> visibleAdvancements() {
        String query = this.searchBox == null ? "" : this.searchBox.getValue().toLowerCase(Locale.ROOT); // GUESS: 60% - getValue()
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
                case 1 -> favoriteAdvancements.contains(node.holder());
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
        graphics.fill(x + 1, y + h - 1 - t, x + w - 1, y + h - 1, bottomRight);
        graphics.fill(x + w - 1 - t, y + 1, x + w - 1, y + h - 1, bottomRight);
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

        super.extractRenderState(graphics, mouseX, mouseY, delta); // draws buttons + search box

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
                drawFavoriteStar(graphics, starX, starY, STAR_SIZE, favoriteAdvancements.contains(node.holder()));

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
                favoriteAdvancements.contains(this.selectedNode.holder()));

        int iconX = starX + DETAIL_STAR_SIZE + 8;
        drawIconSlot(graphics, iconX, starY, display.icon().create(), colorFor(status));

        int titleX = iconX + ICON_SIZE + 10;
        int titleAvailableWidth = panelRight - titleX - 8;
        drawMarqueeText(graphics, display.title().getVisualOrderText(), titleX, starY + 2, titleAvailableWidth, colorFor(status), true);

        int barY = starY + ICON_SIZE + 10;
        drawProgressBar(graphics, this.detailX + 8, barY, this.detailWidth - 16, counts[0], counts[1], status);

        int wrapWidth = panelRight - this.detailX - 16;
        int contentBottom = this.listBottom;

        List<FormattedCharSequence> lines = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        for (FormattedCharSequence line : this.font.split(display.description(), wrapWidth)) {
            lines.add(line);
            colors.add(MUTED_COLOR);
        }

        lines.add(Component.literal("Type: " + typeLabel(display.type())).getVisualOrderText());
        colors.add(TITLE_COLOR);

        List<String> allCriteria = new ArrayList<>(this.selectedNode.advancement().requirements().names());
        AdvancementProgress progress = this.progressByNode.get(this.selectedNode);
        Set<String> remaining = new HashSet<>();
        if (progress != null) progress.getRemainingCriteria().forEach(remaining::add);

        lines.add(Component.literal("").getVisualOrderText());
        colors.add(MUTED_COLOR);
        lines.add(Component.literal("Criteria (" + counts[0] + "/" + counts[1] + "):").getVisualOrderText());
        colors.add(TITLE_COLOR);

        for (String criterion : allCriteria) {
            boolean isDone = !remaining.contains(criterion);
            String prefix = isDone ? "[x] " : "[ ] ";
            for (FormattedCharSequence line : this.font.split(Component.literal(prefix + humanizeCriterion(criterion)), wrapWidth)) {
                lines.add(line);
                colors.add(isDone ? DONE_COLOR : NONE_COLOR);
            }
        }

        int maxScroll = Math.max(0, lines.size() * LINE_HEIGHT - (contentBottom - this.detailContentY));
        this.detailScrollOffset = Math.clamp(this.detailScrollOffset, 0, maxScroll);

        graphics.enableScissor(this.detailX, this.detailContentY, panelRight, contentBottom);
        int y = this.detailContentY - this.detailScrollOffset;
        for (int i = 0; i < lines.size(); i++) {
            if (y + LINE_HEIGHT >= this.detailContentY && y <= contentBottom) {
                graphics.text(this.font, lines.get(i), this.detailX + 8, y, colors.get(i), false);
            }
            y += LINE_HEIGHT;
        }
        graphics.disableScissor();
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();

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