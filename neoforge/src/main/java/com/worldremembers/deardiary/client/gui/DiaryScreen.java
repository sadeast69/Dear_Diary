package com.worldremembers.deardiary.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.worldremembers.deardiary.client.ClientDiaryCache;
import com.worldremembers.deardiary.client.DearDiaryClientNetworking;
import com.worldremembers.deardiary.client.config.DearDiaryClientConfig;
import com.worldremembers.deardiary.client.config.DearDiaryClientConfig.DiaryListSortMode;
import com.worldremembers.deardiary.data.DiaryEntry;
import com.worldremembers.deardiary.data.DiaryEntryMarkers;
import com.worldremembers.deardiary.data.DiaryImportance;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class DiaryScreen extends Screen implements ClientDiaryCache.Listener {
    private static final boolean DEBUG_LAYOUT = false;
    private static final ResourceLocation BOOK_TEXTURE = ResourceLocation.fromNamespaceAndPath("dear_diary", "textures/gui/diary_book.png");
    private static final int BOOK_TEXTURE_WIDTH = 1449;
    private static final int BOOK_TEXTURE_HEIGHT = 1086;
    private static final float BOOK_ASPECT_RATIO = BOOK_TEXTURE_WIDTH / (float) BOOK_TEXTURE_HEIGHT;
    private static final float BOOK_SCALE_MULTIPLIER = 1.0F;
    private static final int RIGHT_PAGE_TOP_DECORATIVE_LINE_Y = 226;
    private static final int RIGHT_PAGE_BOTTOM_DECORATIVE_LINE_Y = 417;
    private static final int DIM_COLOR = 0x77000000;
    private static final int JOURNAL_COLOR = 0xFFF0D3A0;
    private static final int JOURNAL_SHADOW = 0x88311B0E;
    private static final int JOURNAL_SOFT_SHADOW = 0x4428140A;
    private static final int PAGE_COLOR = 0xFFFFE7B6;
    private static final int PAGE_CLEAR_COLOR = 0xFFFFEDC8;
    private static final int PAGE_SHADE = 0xFFECCB92;
    private static final int PAGE_EDGE_SHADE = 0xFFE0B977;
    private static final int PAGE_EDGE = 0xFFB2783C;
    private static final int PAGE_LINE = 0xFFF4D6A0;
    private static final int FOLD_DARK = 0xFF8F5D31;
    private static final int FOLD_LIGHT = 0xFFE7C58B;
    private static final int ROW_HOVER_COLOR = 0xFFF5D99F;
    private static final int ROW_SELECTED_COLOR = 0xFFE6BF77;
    private static final int TAB_SELECTED_COLOR = 0xFFE2BD79;
    private static final int TAB_HOVER_COLOR = 0xFFF1D398;
    private static final int TAB_IDLE_COLOR = 0xFFFFE4AE;
    private static final int ACTION_COLOR = 0xFFFFE8BC;
    private static final int ACTION_HOVER_COLOR = 0xFFF4D393;
    private static final int ACTION_DANGER_COLOR = 0xFFE8C09A;
    private static final int GOLD_COLOR = 0xFFE0A92F;
    private static final int TEXT_COLOR = 0xFF241204;
    private static final int MUTED_TEXT_COLOR = 0xFF68401F;
    private static final int ENTRY_ROW_HEIGHT = 36;
    private static final int ENTRY_ROW_BODY_HEIGHT = 32;
    private static final int ENTRY_LIST_TITLE_Y_OFFSET = 5;
    private static final int ENTRY_LIST_META_Y_OFFSET = 20;
    private static final int DETAIL_TITLE_LINE_HEIGHT = 12;
    private static final int COMPACT_TAB_PADDING = 4;
    private static final int REGULAR_TAB_PADDING = 12;
    private static final int PAGE_PADDING = 13;
    private static final int HEADER_NEW_WIDTH = 86;
    private static final int HEADER_CLOSE_WIDTH = 56;
    private static final int DETAIL_EDIT_WIDTH = 58;
    private static final int DETAIL_DELETE_WIDTH = 64;
    private static final int DETAIL_FAVORITE_WIDTH = 92;
    private static final int DETAIL_SHARE_WIDTH = 64;
    private static final int COMPOSE_SAVE_WIDTH = 76;
    private static final int COMPOSE_CANCEL_WIDTH = 64;
    private static final int MAX_TITLE_LENGTH = 80;
    private static final int MAX_TEXT_LENGTH = 2000;
    private static final int MAX_SEARCH_LENGTH = 80;
    private static final int FIELD_COLOR = 0xEFFFF0CD;
    private static final int FIELD_BORDER = 0xFFC08B50;
    private static final int ERROR_COLOR = 0xFF8D2117;

    private UUID selectedEntryId;
    private DiaryFilter filter = DiaryFilter.ALL;
    private DiaryListSortMode sortMode = DearDiaryClientConfig.get().diaryListSortMode();
    private int page;
    private int detailScroll;
    private int detailMaxScroll;
    private boolean requestedSnapshot;
    private boolean listening;
    private boolean selectNewestAfterSnapshot;
    private RightPageMode rightPageMode = RightPageMode.VIEW;
    private ComposeEntryMode composeEntryMode = ComposeEntryMode.ENTRY;
    private boolean composeAttachCoordinates = true;
    private String searchQuery = "";
    private String composeTitleDraft = "";
    private String composeTextDraft = "";
    private DiaryTitleFieldWidget searchField;
    private DiaryTitleFieldWidget composeTitleField;
    private DiaryTextAreaWidget composeTextArea;
    private Component composeErrorText;

    public DiaryScreen(UUID selectedEntryId) {
        this(selectedEntryId, false, false);
    }

    public DiaryScreen(UUID selectedEntryId, boolean selectNewestAfterSnapshot) {
        this(selectedEntryId, selectNewestAfterSnapshot, false);
    }

    public DiaryScreen(UUID selectedEntryId, boolean selectNewestAfterSnapshot, boolean composeMode) {
        super(Component.translatable("screen.dear_diary.title"));
        this.selectedEntryId = selectedEntryId;
        this.selectNewestAfterSnapshot = selectNewestAfterSnapshot;
        this.rightPageMode = composeMode ? RightPageMode.COMPOSE : RightPageMode.VIEW;
    }

    private enum RightPageMode {
        VIEW,
        COMPOSE,
        EDIT
    }

    private enum ComposeEntryMode {
        ENTRY,
        CHAPTER
    }

    private boolean isComposeMode() {
        return rightPageMode == RightPageMode.COMPOSE;
    }

    private boolean isEditMode() {
        return rightPageMode == RightPageMode.EDIT;
    }

    private boolean isFormMode() {
        return rightPageMode == RightPageMode.COMPOSE || rightPageMode == RightPageMode.EDIT;
    }

    private boolean isComposingChapter() {
        return isComposeMode() && composeEntryMode == ComposeEntryMode.CHAPTER;
    }

    @Override
    protected void init() {
        startListening();
        if (!requestedSnapshot) {
            DearDiaryClientNetworking.requestDiarySnapshot();
            requestedSnapshot = true;
        }

        JournalLayout layout = JournalLayout.create(width, height, font);
        int visibleRows = visibleRows(layout);
        List<DiaryEntry> entries = filteredEntries();
        if (rightPageMode == RightPageMode.VIEW) {
            ensureSelection(entries);
        }
        if (rightPageMode == RightPageMode.EDIT && selectedEntry().isEmpty()) {
            rightPageMode = RightPageMode.VIEW;
            ensureSelection(entries);
        }
        page = clamp(page, 0, Math.max(0, totalPages(entries, visibleRows) - 1));
        addSearchWidget(layout);
        if (isFormMode()) {
            addFormWidgets(layout);
        }
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        JournalLayout layout = JournalLayout.create(width, height, font);
        List<DiaryEntry> entries = filteredEntries();
        int visibleRows = visibleRows(layout);
        int totalPages = totalPages(entries, visibleRows);

        context.fill(0, 0, width, height, DIM_COLOR);
        context.pose().pushPose();
        renderJournal(context, layout);
        renderHeader(context, layout, mouseX, mouseY);
        renderFilterTabs(context, layout, mouseX, mouseY);
        renderSearchField(context, layout, mouseX, mouseY);
        renderEntryList(context, layout, entries, visibleRows, mouseX, mouseY);
        renderPager(context, layout, entries, totalPages, mouseX, mouseY);
        if (isFormMode()) {
            renderFormPage(context, layout, mouseX, mouseY);
        } else {
            renderSelectedEntry(context, layout, entries, mouseX, mouseY);
        }
        renderDebugLayout(context, layout);
        context.pose().popPose();
        renderChildrenWithoutBackground(context, mouseX, mouseY, delta);

        renderHoveredTooltip(context, layout, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            JournalLayout layout = JournalLayout.create(width, height, font);
            if (handleHeaderClick(layout, mouseX, mouseY)) {
                return true;
            }

            if (handleSearchClearClick(layout, mouseX, mouseY)) {
                return true;
            }

            if (isFormMode()) {
                if (handleFormClick(layout, mouseX, mouseY)) {
                    return true;
                }
                return super.mouseClicked(mouseX, mouseY, button);
            }

            if (rightPageMode == RightPageMode.VIEW && handleDetailActionClick(layout, mouseX, mouseY)) {
                return true;
            }

            if (rightPageMode == RightPageMode.VIEW && handleCoordinateCopyClick(layout, mouseX, mouseY)) {
                return true;
            }

            if (handlePagerClick(layout, mouseX, mouseY)) {
                return true;
            }

            if (handleSortClick(layout, mouseX, mouseY)) {
                return true;
            }

            Optional<DiaryFilter> clickedFilter = filterAt(layout, mouseX, mouseY);
            if (clickedFilter.isPresent()) {
                syncComposeDraft();
                filter = clickedFilter.get();
                page = 0;
                selectedEntryId = null;
                detailScroll = 0;
                rebuildWidgets();
                return true;
            }

            Optional<DiaryEntry> clickedEntry = entryAt(layout, mouseX, mouseY);
            if (clickedEntry.isPresent()) {
                syncComposeDraft();
                selectedEntryId = clickedEntry.get().getId();
                detailScroll = 0;
                rebuildWidgets();
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean handled = super.keyPressed(keyCode, scanCode, modifiers);
        return syncSearchQueryFromField() || handled;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        boolean handled = super.charTyped(chr, modifiers);
        return syncSearchQueryFromField() || handled;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        JournalLayout layout = JournalLayout.create(width, height, font);
        if (rightPageMode == RightPageMode.VIEW
                && layout.rightViewTextBounds.contains(mouseX, mouseY)
                && detailMaxScroll > 0) {
            int direction = verticalAmount > 0 ? -1 : 1;
            detailScroll = clamp(detailScroll + direction, 0, detailMaxScroll);
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void removed() {
        stopListening();
        super.removed();
    }

    @Override
    public void onDiaryCacheUpdated() {
        Minecraft minecraftClient = Minecraft.getInstance();
        minecraftClient.execute(() -> {
            if (minecraftClient.screen == this) {
                syncComposeDraft();
                if (selectNewestAfterSnapshot) {
                    selectedEntryId = null;
                    page = 0;
                    detailScroll = 0;
                    selectNewestAfterSnapshot = false;
                }
                rebuildWidgets();
            }
        });
    }

    private void renderJournal(GuiGraphics context, JournalLayout layout) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        context.blit(
                BOOK_TEXTURE,
                layout.bookX,
                layout.bookY,
                layout.bookWidth,
                layout.bookHeight,
                0.0F,
                0.0F,
                BOOK_TEXTURE_WIDTH,
                BOOK_TEXTURE_HEIGHT,
                BOOK_TEXTURE_WIDTH,
                BOOK_TEXTURE_HEIGHT
        );
    }

    private void renderHeader(GuiGraphics context, JournalLayout layout, int mouseX, int mouseY) {
        drawSoftButton(
                context,
                layout.newButtonX,
                layout.headerButtonY,
                layout.newButtonWidth,
                layout.headerButtonHeight,
                newEntryButtonText(layout),
                isInside(mouseX, mouseY, layout.newButtonX, layout.headerButtonY, layout.newButtonWidth, layout.headerButtonHeight),
                true,
                false
        );
        drawSoftButton(
                context,
                layout.closeButtonX,
                layout.headerButtonY,
                layout.closeButtonWidth,
                layout.headerButtonHeight,
                Component.translatable("screen.dear_diary.close"),
                isInside(mouseX, mouseY, layout.closeButtonX, layout.headerButtonY, layout.closeButtonWidth, layout.headerButtonHeight),
                true,
                false
        );
    }

    private void renderFilterTabs(GuiGraphics context, JournalLayout layout, int mouseX, int mouseY) {
        int x = layout.leftTabsBounds.x();
        for (DiaryFilter value : DiaryFilter.values()) {
            if (value == DiaryFilter.FAVORITES) {
                int sortWidth = sortButtonWidth(layout);
                renderSortControl(context, layout, mouseX, mouseY, x, sortWidth);
                x += sortWidth + layout.tabGap;
            }

            Component label = filterTabText(value, layout);
            int tabWidth = filterTabWidth(value, label, layout);
            boolean selected = filter == value;
            boolean hovered = isInside(mouseX, mouseY, x, layout.filterY, tabWidth, layout.tabHeight);
            int fillColor = selected ? TAB_SELECTED_COLOR : hovered ? TAB_HOVER_COLOR : TAB_IDLE_COLOR;
            context.fill(x, layout.filterY, x + tabWidth, layout.filterY + layout.tabHeight, fillColor);
            context.renderOutline(x, layout.filterY, tabWidth, layout.tabHeight, selected ? PAGE_EDGE : 0xFFBE8750);
            if (value == DiaryFilter.FAVORITES) {
                drawFavoriteStarIcon(
                        context,
                        x + tabWidth / 2,
                        layout.filterY + layout.tabHeight / 2,
                        selected ? GOLD_COLOR : MUTED_TEXT_COLOR
                );
            } else {
                drawCenteredTrimmed(context, label, x + tabWidth / 2, tabLabelY(layout), tabWidth - tabTextInset(layout), selected ? TEXT_COLOR : MUTED_TEXT_COLOR);
            }
            x += tabWidth + layout.tabGap;
        }
    }

    private void renderSortControl(GuiGraphics context, JournalLayout layout, int mouseX, int mouseY, int x, int width) {
        boolean hovered = isInside(mouseX, mouseY, x, layout.filterY, width, layout.tabHeight);
        context.fill(x, layout.filterY, x + width, layout.filterY + layout.tabHeight, hovered ? TAB_HOVER_COLOR : TAB_IDLE_COLOR);
        context.renderOutline(x, layout.filterY, width, layout.tabHeight, PAGE_EDGE);
        drawCenteredTrimmed(
                context,
                sortModeText(),
                x + width / 2,
                tabLabelY(layout),
                width - tabTextInset(layout),
                hovered ? TEXT_COLOR : MUTED_TEXT_COLOR
        );
    }

    private void renderSearchField(GuiGraphics context, JournalLayout layout, int mouseX, int mouseY) {
        drawFieldBackground(
                context,
                layout.leftSearchBounds.x(),
                layout.leftSearchBounds.y(),
                layout.leftSearchBounds.width(),
                layout.leftSearchBounds.height()
        );
        if (!hasSearchText()) {
            return;
        }

        Rect clearBounds = layout.leftSearchClearBounds;
        boolean hovered = clearBounds.contains(mouseX, mouseY);
        if (hovered) {
            context.fill(clearBounds.x(), clearBounds.y(), clearBounds.right(), clearBounds.bottom(), ACTION_HOVER_COLOR);
            context.renderOutline(clearBounds.x(), clearBounds.y(), clearBounds.width(), clearBounds.height(), PAGE_EDGE);
        }
        String clearText = "x";
        context.drawString(
                font,
                clearText,
                clearBounds.centerX() - font.width(clearText) / 2,
                clearBounds.y() + Math.max(1, (clearBounds.height() - font.lineHeight) / 2),
                MUTED_TEXT_COLOR,
                false
        );
    }

    private void renderEntryList(GuiGraphics context, JournalLayout layout, List<DiaryEntry> entries, int visibleRows, int mouseX, int mouseY) {
        int listY = layout.listY;
        if (entries.isEmpty()) {
            Component emptyText = emptyEntryListText();
            drawWrapped(context, emptyText, layout.leftContentX, listY, layout.leftContentWidth, MUTED_TEXT_COLOR);
            return;
        }

        int start = page * visibleRows;
        int end = Math.min(entries.size(), start + visibleRows);
        for (int index = start; index < end; index++) {
            DiaryEntry entry = entries.get(index);
            int rowY = listY + (index - start) * ENTRY_ROW_HEIGHT;
            boolean selected = entry.getId().equals(selectedEntryId);
            boolean hovered = isInside(mouseX, mouseY, layout.leftContentX, rowY, layout.leftContentWidth, ENTRY_ROW_BODY_HEIGHT);
            if (selected || hovered) {
                context.fill(layout.leftContentX, rowY, layout.leftContentRight, rowY + ENTRY_ROW_BODY_HEIGHT, selected ? ROW_SELECTED_COLOR : ROW_HOVER_COLOR);
                context.renderOutline(layout.leftContentX, rowY, layout.leftContentWidth, ENTRY_ROW_BODY_HEIGHT, selected ? PAGE_EDGE : 0xFFCB955C);
            } else {
                context.fill(layout.leftContentX + 5, rowY + ENTRY_ROW_BODY_HEIGHT - 1, layout.leftContentRight - 5, rowY + ENTRY_ROW_BODY_HEIGHT, PAGE_LINE);
            }

            if (DiaryEntryMarkers.isChapterEntry(entry)) {
                renderChapterEntryRow(context, layout, entry, rowY);
                continue;
            }

            int titleX = layout.leftContentX + 18;
            if (entry.isFavorite()) {
                drawFavoriteStarIcon(context, titleX + 5, rowY + ENTRY_LIST_TITLE_Y_OFFSET + 5, GOLD_COLOR);
                titleX += 12;
            }

            context.drawString(
                    font,
                    ellipsize(safeTitle(entry), layout.leftContentRight - titleX - 6),
                    titleX,
                    rowY + ENTRY_LIST_TITLE_Y_OFFSET,
                    TEXT_COLOR,
                    false
            );

            String detail = DiaryUiText.formatShortDate(entry) + " \u2022 " + entryKindLabel(entry, layout).getString();
            context.drawString(
                    font,
                    ellipsize(detail, layout.leftContentRight - titleX - 6),
                    titleX,
                    rowY + ENTRY_LIST_META_Y_OFFSET,
                    MUTED_TEXT_COLOR,
                    false
            );
        }
    }

    private void renderChapterEntryRow(GuiGraphics context, JournalLayout layout, DiaryEntry entry, int rowY) {
        int centerX = layout.leftContentX + layout.leftContentWidth / 2;
        int lineY = rowY + 13;
        String title = safeTitle(entry);
        int titleWidth = Math.min(font.width(title), Math.max(1, layout.leftContentWidth - 30));
        int leftLineEnd = Math.max(layout.leftContentX + 8, centerX - titleWidth / 2 - 7);
        int rightLineStart = Math.min(layout.leftContentRight - 8, centerX + titleWidth / 2 + 7);
        context.fill(layout.leftContentX + 8, lineY, leftLineEnd, lineY + 1, PAGE_EDGE);
        context.fill(rightLineStart, lineY, layout.leftContentRight - 8, lineY + 1, PAGE_EDGE);
        drawCenteredTrimmed(context, Component.literal(title), centerX, rowY + ENTRY_LIST_TITLE_Y_OFFSET, layout.leftContentWidth - 16, TEXT_COLOR);
        drawCenteredTrimmed(
                context,
                Component.translatable("screen.dear_diary.chapter.label"),
                centerX,
                rowY + ENTRY_LIST_META_Y_OFFSET,
                layout.leftContentWidth - 16,
                MUTED_TEXT_COLOR
        );
    }

    private void renderPager(GuiGraphics context, JournalLayout layout, List<DiaryEntry> entries, int totalPages, int mouseX, int mouseY) {
        if (entries.isEmpty() || totalPages <= 1) {
            return;
        }

        boolean previousEnabled = page > 0;
        boolean nextEnabled = page + 1 < totalPages;
        drawSoftButton(
                context,
                layout.leftContentX,
                layout.pagerY,
                layout.pagerButtonWidth,
                layout.footerButtonHeight,
                Component.translatable("screen.dear_diary.previous_page"),
                previousEnabled && isInside(mouseX, mouseY, layout.leftContentX, layout.pagerY, layout.pagerButtonWidth, layout.footerButtonHeight),
                previousEnabled,
                false
        );
        drawCentered(context, Component.literal((page + 1) + "/" + totalPages), layout.leftPageX + layout.leftPageWidth / 2, layout.pagerY + 5, MUTED_TEXT_COLOR);
        drawSoftButton(
                context,
                layout.leftContentRight - layout.pagerButtonWidth,
                layout.pagerY,
                layout.pagerButtonWidth,
                layout.footerButtonHeight,
                Component.translatable("screen.dear_diary.next_page"),
                nextEnabled && isInside(mouseX, mouseY, layout.leftContentRight - layout.pagerButtonWidth, layout.pagerY, layout.pagerButtonWidth, layout.footerButtonHeight),
                nextEnabled,
                false
        );
    }

    private void renderFormPage(GuiGraphics context, JournalLayout layout, int mouseX, int mouseY) {
        int x = layout.rightInnerBounds.x();
        int contentWidth = layout.rightInnerBounds.width();
        Rect coordinateBounds = layout.rightComposeCoordinateBounds;
        String titleKey = isEditMode() && selectedEntryIsChapter()
                ? "screen.dear_diary.chapter.edit"
                : isEditMode() ? "screen.dear_diary.edit_entry" : "screen.dear_diary.new_entry";
        drawText(context, ellipsizedText(titleKey, contentWidth), x, layout.rightComposeHeaderBounds.y(), TEXT_COLOR);
        renderComposeModeSwitch(context, layout, mouseX, mouseY);
        drawText(context, ellipsizedText("screen.dear_diary.field.title", contentWidth), x, layout.composeTitleFieldY - 12, MUTED_TEXT_COLOR);
        drawText(context, ellipsizedText("screen.dear_diary.field.text", contentWidth), x, layout.composeTextAreaY - 12, MUTED_TEXT_COLOR);

        drawFieldBackground(
                context,
                layout.rightComposeTitleFieldBounds.x(),
                layout.rightComposeTitleFieldBounds.y(),
                layout.rightComposeTitleFieldBounds.width(),
                layout.rightComposeTitleFieldBounds.height()
        );

        if (isComposeMode()) {
            boolean hovered = coordinateBounds.contains(mouseX, mouseY);
            context.fill(layout.composeCheckboxX, layout.composeCheckboxY, layout.composeCheckboxX + 12, layout.composeCheckboxY + 12, hovered ? ACTION_HOVER_COLOR : FIELD_COLOR);
            context.renderOutline(layout.composeCheckboxX, layout.composeCheckboxY, 12, 12, PAGE_EDGE);
            if (composeAttachCoordinates) {
                context.drawString(font, "\u2713", layout.composeCheckboxX + 3, layout.composeCheckboxY + 1, TEXT_COLOR, false);
            }
            Component coordinateLabel = Component.translatable("screen.dear_diary.attach_coordinates.short");
            context.drawString(
                    font,
                    ellipsize(coordinateLabel.getString(), contentWidth - 20),
                    layout.composeCheckboxX + 17,
                    layout.composeCheckboxY + 2,
                    TEXT_COLOR,
                    false
            );
            if (composeAttachCoordinates) {
                context.drawString(
                        font,
                        ellipsize(DiaryUiText.currentLocation(minecraft), contentWidth - 17),
                        layout.composeCheckboxX + 17,
                        layout.composeCheckboxY + 16,
                        MUTED_TEXT_COLOR,
                        false
                );
            }
        }

        if (composeErrorText != null) {
            drawWrapped(context, composeErrorText, x, layout.composeActionY - 14, contentWidth, ERROR_COLOR);
        }

        drawSoftButton(
                context,
                layout.composeCancelX,
                layout.composeActionY,
                layout.composeCancelWidth,
                layout.footerButtonHeight,
                cancelButtonText(layout),
                isInside(mouseX, mouseY, layout.composeCancelX, layout.composeActionY, layout.composeCancelWidth, layout.footerButtonHeight),
                true,
                false
        );
        drawSoftButton(
                context,
                layout.composeSaveX,
                layout.composeActionY,
                layout.composeSaveWidth,
                layout.footerButtonHeight,
                saveButtonText(layout),
                isInside(mouseX, mouseY, layout.composeSaveX, layout.composeActionY, layout.composeSaveWidth, layout.footerButtonHeight),
                true,
                false
        );
    }

    private void renderComposeModeSwitch(GuiGraphics context, JournalLayout layout, int mouseX, int mouseY) {
        if (!isComposeMode()) {
            return;
        }

        renderComposeModeButton(
                context,
                composeModeEntryBounds(layout),
                Component.translatable("screen.dear_diary.compose.mode.entry"),
                composeEntryMode == ComposeEntryMode.ENTRY,
                mouseX,
                mouseY
        );
        renderComposeModeButton(
                context,
                composeModeChapterBounds(layout),
                Component.translatable("screen.dear_diary.compose.mode.chapter"),
                composeEntryMode == ComposeEntryMode.CHAPTER,
                mouseX,
                mouseY
        );
    }

    private void renderComposeModeButton(GuiGraphics context, Rect bounds, Component label, boolean selected, int mouseX, int mouseY) {
        boolean hovered = bounds.contains(mouseX, mouseY);
        int fillColor = selected ? TAB_SELECTED_COLOR : hovered ? TAB_HOVER_COLOR : TAB_IDLE_COLOR;
        context.fill(bounds.x(), bounds.y(), bounds.right(), bounds.bottom(), fillColor);
        context.renderOutline(bounds.x(), bounds.y(), bounds.width(), bounds.height(), selected ? PAGE_EDGE : 0xFFBE8750);
        drawCenteredTrimmed(
                context,
                label,
                bounds.centerX(),
                bounds.y() + Math.max(1, (bounds.height() - font.lineHeight) / 2),
                bounds.width() - 6,
                selected ? TEXT_COLOR : MUTED_TEXT_COLOR
        );
    }

    private void renderSelectedEntry(GuiGraphics context, JournalLayout layout, List<DiaryEntry> visibleEntries, int mouseX, int mouseY) {
        Optional<DiaryEntry> selected = selectedEntry();
        int x = layout.rightInnerBounds.x();
        int contentWidth = layout.rightInnerBounds.width();

        if (selected.isEmpty()) {
            Component text = visibleEntries.isEmpty() && currentDiaryIsEmpty()
                    ? Component.translatable("screen.dear_diary.create_first_entry")
                    : Component.translatable("screen.dear_diary.no_entry_selected");
            drawWrapped(context, text, x, layout.rightViewHeaderBounds.y(), contentWidth, MUTED_TEXT_COLOR);
            detailMaxScroll = 0;
            detailScroll = 0;
            return;
        }

        DiaryEntry entry = selected.get();
        if (DiaryEntryMarkers.isChapterEntry(entry)) {
            renderSelectedChapter(context, layout, entry, mouseX, mouseY);
            return;
        }

        int lineY = layout.rightViewHeaderBounds.y();
        String titleText = safeTitle(entry);
        List<FormattedCharSequence> titleLines = font.split(Component.literal(titleText), contentWidth);
        int maxTitleLines = Math.min(Math.max(1, layout.rightViewHeaderBounds.height() / DETAIL_TITLE_LINE_HEIGHT), titleLines.size());
        for (int index = 0; index < maxTitleLines; index++) {
            context.drawString(font, titleLines.get(index), x, lineY, TEXT_COLOR, false);
            lineY += DETAIL_TITLE_LINE_HEIGHT;
        }

        lineY = layout.rightViewMetaBounds.y();
        context.drawString(font, DiaryUiText.formatDate(entry), x, lineY, MUTED_TEXT_COLOR, false);
        lineY += 11;

        String metadata = entryKindLabel(entry, layout).getString()
                + " \u2022 " + categoryLabel(entry, layout).getString()
                + " \u2022 " + importanceLabel(entry, layout).getString();
        context.drawString(font, ellipsize(metadata, contentWidth), x, lineY, MUTED_TEXT_COLOR, false);
        lineY += 11;

        String detailLine = entry.hasLocation() ? DiaryUiText.location(entry) : "";
        context.drawString(font, ellipsize(detailLine, contentWidth), x, lineY, MUTED_TEXT_COLOR, false);

        lineY = layout.rightViewTextBounds.y();
        int textHeight = Math.max(1, layout.rightViewTextBounds.bottom() - lineY);
        int visibleTextLines = Math.max(1, textHeight / 10);
        List<FormattedCharSequence> lines = font.split(Component.literal(safeText(entry)), contentWidth);
        detailMaxScroll = Math.max(0, lines.size() - visibleTextLines);
        detailScroll = clamp(detailScroll, 0, detailMaxScroll);

        int endLine = Math.min(lines.size(), detailScroll + visibleTextLines);
        for (int index = detailScroll; index < endLine; index++) {
            context.drawString(font, lines.get(index), x, lineY, TEXT_COLOR, false);
            lineY += 10;
        }

        if (detailMaxScroll > 0) {
            renderDetailScrollMarker(context, layout);
        }

        renderDetailActions(context, layout, entry, mouseX, mouseY);
    }

    private void renderSelectedChapter(GuiGraphics context, JournalLayout layout, DiaryEntry entry, int mouseX, int mouseY) {
        int x = layout.rightInnerBounds.x();
        int contentWidth = layout.rightInnerBounds.width();
        int titleY = Math.max(layout.rightViewHeaderBounds.y(), layout.rightViewMetaBounds.y() - 44);
        int labelY = Math.max(layout.rightPageBounds.y() + 8, titleY - 11);
        context.drawString(font, Component.translatable("screen.dear_diary.chapter.label"), x, labelY, MUTED_TEXT_COLOR, false);
        context.drawString(font, ellipsize(safeTitle(entry), contentWidth), x, titleY, TEXT_COLOR, false);

        int lineY = Math.max(titleY + DETAIL_TITLE_LINE_HEIGHT + 18, layout.rightViewMetaBounds.y() - 8);
        context.drawString(font, DiaryUiText.formatDate(entry), x, lineY, MUTED_TEXT_COLOR, false);
        lineY += 11;
        if (entry.hasLocation()) {
            context.drawString(font, ellipsize(DiaryUiText.location(entry), contentWidth), x, lineY, MUTED_TEXT_COLOR, false);
        }

        lineY = layout.rightViewTextBounds.y();
        int textHeight = Math.max(1, layout.rightViewTextBounds.bottom() - lineY);
        int visibleTextLines = Math.max(1, textHeight / 10);
        List<FormattedCharSequence> lines = font.split(Component.literal(safeText(entry)), contentWidth);
        detailMaxScroll = Math.max(0, lines.size() - visibleTextLines);
        detailScroll = clamp(detailScroll, 0, detailMaxScroll);

        int endLine = Math.min(lines.size(), detailScroll + visibleTextLines);
        for (int index = detailScroll; index < endLine; index++) {
            context.drawString(font, lines.get(index), x, lineY, TEXT_COLOR, false);
            lineY += 10;
        }

        if (detailMaxScroll > 0) {
            renderDetailScrollMarker(context, layout);
        }

        renderDetailActions(context, layout, entry, mouseX, mouseY);
    }

    private void renderDetailScrollMarker(GuiGraphics context, JournalLayout layout) {
        int trackX = layout.rightViewTextBounds.right() - 3;
        int trackTop = layout.rightViewTextBounds.y() + 4;
        int trackBottom = layout.rightViewTextBounds.bottom() - 4;
        int trackHeight = Math.max(8, trackBottom - trackTop);
        int thumbHeight = Math.max(6, trackHeight / Math.max(3, detailMaxScroll + 2));
        int maxThumbTravel = Math.max(1, trackHeight - thumbHeight);
        int thumbY = trackTop + Math.round(maxThumbTravel * (detailScroll / (float) Math.max(1, detailMaxScroll)));

        context.fill(trackX, trackTop, trackX + 1, trackBottom, 0x55966A38);
        context.fill(trackX - 1, thumbY, trackX + 2, thumbY + thumbHeight, 0xAA8E5D2D);
    }

    private void renderDetailActions(GuiGraphics context, JournalLayout layout, DiaryEntry entry, int mouseX, int mouseY) {
        int x = layout.detailActionX;
        int y = layout.detailActionY;
        int width = layout.detailButtonWidth;
        int height = layout.footerButtonHeight;
        int gap = layout.detailButtonGap;
        drawSoftButton(
                context,
                x,
                y,
                width,
                height,
                Component.translatable("screen.dear_diary.edit"),
                entry.isEditable() && isInside(mouseX, mouseY, x, y, width, height),
                entry.isEditable(),
                false
        );
        drawSoftButton(
                context,
                x + width + gap,
                y,
                width,
                height,
                deleteButtonText(layout),
                isInside(mouseX, mouseY, x + width + gap, y, width, height),
                true,
                true
        );
        if (DiaryEntryMarkers.isChapterEntry(entry)) {
            return;
        }

        drawFavoriteIconButton(
                context,
                x,
                y + height + gap,
                width,
                height,
                entry.isFavorite(),
                isInside(mouseX, mouseY, x, y + height + gap, width, height),
                true
        );
        drawSoftButton(
                context,
                x + width + gap,
                y + height + gap,
                width,
                height,
                shareButtonText(layout),
                entry.isShareable() && isInside(mouseX, mouseY, x + width + gap, y + height + gap, width, height),
                entry.isShareable(),
                false
        );
    }

    private void drawSoftButton(GuiGraphics context, int x, int y, int width, int height, Component text, boolean hovered, boolean enabled, boolean danger) {
        int fillColor = !enabled ? 0xFFE5CCA0 : hovered ? ACTION_HOVER_COLOR : danger ? ACTION_DANGER_COLOR : ACTION_COLOR;
        int borderColor = !enabled ? 0xFFBCA77F : danger ? 0xFF9F593C : PAGE_EDGE;
        int textColor = !enabled ? 0xFF927B58 : danger ? 0xFF5F2114 : TEXT_COLOR;
        context.fill(x, y, x + width, y + height, fillColor);
        context.renderOutline(x, y, width, height, borderColor);
        int textY = y + Math.max(1, (height - font.lineHeight) / 2);
        drawCenteredTrimmed(context, text, x + width / 2, textY, Math.max(1, width - 8), textColor);
    }

    private void drawFavoriteIconButton(GuiGraphics context, int x, int y, int width, int height, boolean active, boolean hovered, boolean enabled) {
        int fillColor = !enabled ? 0xFFE5CCA0 : active ? 0xFFFFE8BC : hovered ? ACTION_HOVER_COLOR : ACTION_COLOR;
        int borderColor = !enabled ? 0xFFBCA77F : active ? GOLD_COLOR : PAGE_EDGE;
        int iconColor = !enabled ? 0xFF927B58 : active ? GOLD_COLOR : MUTED_TEXT_COLOR;
        context.fill(x, y, x + width, y + height, fillColor);
        context.renderOutline(x, y, width, height, borderColor);
        drawFavoriteStarIcon(context, x + width / 2, y + height / 2, iconColor);
    }

    private void drawFavoriteStarIcon(GuiGraphics context, int centerX, int centerY, int color) {
        String star = "\u2605";
        context.drawString(
                font,
                star,
                centerX - font.width(star) / 2,
                centerY - font.lineHeight / 2,
                color,
                false
        );
    }

    private void drawFieldBackground(GuiGraphics context, int x, int y, int width, int height) {
        context.fill(x, y, x + width, y + height, FIELD_COLOR);
        context.renderOutline(x, y, width, height, FIELD_BORDER);
    }

    private void renderDebugLayout(GuiGraphics context, JournalLayout layout) {
        if (!DEBUG_LAYOUT) {
            return;
        }

        drawDebugRect(context, layout.bookBounds, 0xFFFF3030);
        drawDebugRect(context, layout.headerBounds, 0xFFFFFFFF);
        drawDebugRect(context, layout.spineBounds, 0xFFFF3030);
        drawDebugRect(context, layout.leftPageBounds, 0xFF30FF30);
        drawDebugRect(context, layout.rightPageBounds, 0xFF30FF30);
        drawDebugRect(context, layout.leftInnerBounds, 0xFF30A0FF);
        drawDebugRect(context, layout.rightInnerBounds, 0xFF30A0FF);
        drawDebugRect(context, layout.leftTabsBounds, 0xFFFFFF30);
        drawDebugRect(context, layout.leftSearchBounds, 0xFFA0FF30);
        drawDebugRect(context, layout.leftListBounds, 0xFFFFFF30);
        drawDebugRect(context, layout.leftFooterBounds, 0xFFFFFF30);
        drawDebugRect(context, layout.rightViewHeaderBounds, 0xFFFF80FF);
        drawDebugRect(context, layout.rightViewMetaBounds, 0xFFFF80FF);
        drawDebugRect(context, layout.rightViewTextBounds, 0xFFFF80FF);
        drawDebugRect(context, layout.rightViewFooterBounds, 0xFFFFA030);
        drawDebugRect(context, layout.rightComposeHeaderBounds, 0xFF00FFFF);
        drawDebugRect(context, layout.rightComposeModeBounds, 0xFF00FFFF);
        drawDebugRect(context, layout.rightComposeTitleFieldBounds, 0xFF00FFFF);
        drawDebugRect(context, layout.rightComposeTextFieldBounds, 0xFF00FFFF);
        drawDebugRect(context, layout.rightComposeCoordinateBounds, 0xFF00FFFF);
        drawDebugRect(context, layout.rightComposeFooterBounds, 0xFF00FFFF);
        drawDebugRect(context, layout.topTitleBounds, 0xFFFFFFFF);
        drawDebugRect(context, layout.topGlobalButtonsBounds, 0xFFFFFFFF);
    }

    private void drawDebugRect(GuiGraphics context, Rect bounds, int color) {
        context.renderOutline(bounds.x(), bounds.y(), bounds.width(), bounds.height(), color);
    }

    private void drawCornerMarks(GuiGraphics context, int left, int top, int right, int bottom) {
        int length = 9;
        context.fill(left + 4, top + 4, left + 4 + length, top + 5, PAGE_EDGE);
        context.fill(left + 4, top + 4, left + 5, top + 4 + length, PAGE_EDGE);
        context.fill(right - 4 - length, top + 4, right - 4, top + 5, PAGE_EDGE);
        context.fill(right - 5, top + 4, right - 4, top + 4 + length, PAGE_EDGE);
        context.fill(left + 4, bottom - 5, left + 4 + length, bottom - 4, PAGE_EDGE);
        context.fill(left + 4, bottom - 4 - length, left + 5, bottom - 4, PAGE_EDGE);
        context.fill(right - 4 - length, bottom - 5, right - 4, bottom - 4, PAGE_EDGE);
        context.fill(right - 5, bottom - 4 - length, right - 4, bottom - 4, PAGE_EDGE);
    }

    private void drawWrapped(GuiGraphics context, Component text, int x, int y, int width, int color) {
        int lineY = y;
        for (FormattedCharSequence line : font.split(text, width)) {
            context.drawString(font, line, x, lineY, color, false);
            lineY += 10;
        }
    }

    private void drawText(GuiGraphics context, Component text, int x, int y, int color) {
        context.drawString(font, text, x, y, color, false);
    }

    private void drawCentered(GuiGraphics context, Component text, int centerX, int y, int color) {
        context.drawString(font, text, centerX - font.width(text) / 2, y, color, false);
    }

    private void drawCenteredTrimmed(GuiGraphics context, Component text, int centerX, int y, int maxWidth, int color) {
        String value = ellipsize(text.getString(), maxWidth);
        context.drawString(font, value, centerX - font.width(value) / 2, y, color, false);
    }

    private String ellipsize(String value, int maxWidth) {
        if (value == null || value.isEmpty() || maxWidth <= 0) {
            return "";
        }
        if (font.width(value) <= maxWidth) {
            return value;
        }

        String ellipsis = "...";
        int ellipsisWidth = font.width(ellipsis);
        if (maxWidth <= ellipsisWidth) {
            return font.plainSubstrByWidth(ellipsis, maxWidth);
        }
        return font.plainSubstrByWidth(value, maxWidth - ellipsisWidth) + ellipsis;
    }

    private void renderChildrenWithoutBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
        for (var child : children()) {
            if (child instanceof Renderable renderable) {
                renderable.render(context, mouseX, mouseY, delta);
            }
        }
    }

    private void renderHoveredTooltip(GuiGraphics context, JournalLayout layout, int mouseX, int mouseY) {
        if (hasSearchText() && layout.leftSearchClearBounds.contains(mouseX, mouseY)) {
            context.renderTooltip(font, Component.translatable("screen.dear_diary.search.clear"), mouseX, mouseY);
            return;
        }

        if (sortButtonBounds(layout).contains(mouseX, mouseY)) {
            context.renderTooltip(font, sortModeTooltipText(), mouseX, mouseY);
            return;
        }

        Optional<DiaryFilter> hoveredFilter = filterAt(layout, mouseX, mouseY);
        if (hoveredFilter.filter(value -> value == DiaryFilter.FAVORITES).isPresent()) {
            context.renderTooltip(font, hoveredFilter.get().text(), mouseX, mouseY);
            return;
        }

        Optional<DiaryEntry> selected = selectedEntry();
        if (selected.isPresent()
                && rightPageMode == RightPageMode.VIEW
                && coordinateLineBounds(layout, selected.get()).filter(bounds -> bounds.contains(mouseX, mouseY)).isPresent()) {
            context.renderTooltip(font, Component.translatable("screen.dear_diary.location.copy_coordinates"), mouseX, mouseY);
            return;
        }

        if (selected.isPresent() && rightPageMode == RightPageMode.VIEW && !DiaryEntryMarkers.isChapterEntry(selected.get())) {
            DiaryEntry entry = selected.get();
            int x = layout.detailActionX;
            int y = layout.detailActionY + layout.footerButtonHeight + layout.detailButtonGap;
            if (isInside(mouseX, mouseY, x, y, layout.detailButtonWidth, layout.footerButtonHeight)) {
                String key = entry.isFavorite()
                        ? "screen.dear_diary.favorite_remove"
                        : "screen.dear_diary.favorite_add";
                context.renderTooltip(font, Component.translatable(key), mouseX, mouseY);
            }
        }
    }

    private boolean handleHeaderClick(JournalLayout layout, double mouseX, double mouseY) {
        if (isInside(mouseX, mouseY, layout.newButtonX, layout.headerButtonY, layout.newButtonWidth, layout.headerButtonHeight)) {
            enterComposeMode();
            return true;
        }

        if (isInside(mouseX, mouseY, layout.closeButtonX, layout.headerButtonY, layout.closeButtonWidth, layout.headerButtonHeight)) {
            minecraft.setScreen(null);
            return true;
        }

        return false;
    }

    private boolean handleFormClick(JournalLayout layout, double mouseX, double mouseY) {
        if (isComposeMode() && handleComposeModeClick(layout, mouseX, mouseY)) {
            return true;
        }

        if (isComposeMode() && layout.rightComposeCoordinateBounds.contains(mouseX, mouseY)) {
            composeAttachCoordinates = !composeAttachCoordinates;
            return true;
        }

        if (isInside(mouseX, mouseY, layout.composeCancelX, layout.composeActionY, layout.composeCancelWidth, layout.footerButtonHeight)) {
            cancelFormMode();
            return true;
        }

        if (isInside(mouseX, mouseY, layout.composeSaveX, layout.composeActionY, layout.composeSaveWidth, layout.footerButtonHeight)) {
            saveFormEntry();
            return true;
        }

        return false;
    }

    private boolean handleComposeModeClick(JournalLayout layout, double mouseX, double mouseY) {
        if (composeModeEntryBounds(layout).contains(mouseX, mouseY)) {
            switchComposeEntryMode(ComposeEntryMode.ENTRY);
            return true;
        }

        if (composeModeChapterBounds(layout).contains(mouseX, mouseY)) {
            switchComposeEntryMode(ComposeEntryMode.CHAPTER);
            return true;
        }

        return false;
    }

    private void switchComposeEntryMode(ComposeEntryMode mode) {
        if (composeEntryMode == mode) {
            return;
        }

        syncComposeDraft();
        composeEntryMode = mode;
        composeErrorText = null;
        rebuildWidgets();
    }

    private boolean handleDetailActionClick(JournalLayout layout, double mouseX, double mouseY) {
        Optional<DiaryEntry> selected = selectedEntry();
        if (selected.isEmpty()) {
            return false;
        }

        DiaryEntry entry = selected.get();
        int x = layout.detailActionX;
        int y = layout.detailActionY;
        int width = layout.detailButtonWidth;
        int height = layout.footerButtonHeight;
        int gap = layout.detailButtonGap;
        if (entry.isEditable() && isInside(mouseX, mouseY, x, y, width, height)) {
            enterEditMode(entry);
            return true;
        }

        if (isInside(mouseX, mouseY, x + width + gap, y, width, height)) {
            confirmDelete(entry);
            return true;
        }

        if (DiaryEntryMarkers.isChapterEntry(entry)) {
            return false;
        }

        if (isInside(mouseX, mouseY, x, y + height + gap, width, height)) {
            DearDiaryClientNetworking.setFavorite(entry.getId(), !entry.isFavorite());
            return true;
        }

        if (entry.isShareable() && isInside(mouseX, mouseY, x + width + gap, y + height + gap, width, height)) {
            confirmShare(entry);
            return true;
        }

        return false;
    }

    private boolean handleCoordinateCopyClick(JournalLayout layout, double mouseX, double mouseY) {
        Optional<DiaryEntry> selected = selectedEntry();
        if (selected.isEmpty()) {
            return false;
        }

        DiaryEntry entry = selected.get();
        if (coordinateLineBounds(layout, entry).filter(bounds -> bounds.contains(mouseX, mouseY)).isEmpty()) {
            return false;
        }

        if (minecraft != null && minecraft.keyboardHandler != null) {
            minecraft.keyboardHandler.setClipboard(coordinateClipboardText(entry));
        }
        return true;
    }

    private boolean handleSearchClearClick(JournalLayout layout, double mouseX, double mouseY) {
        if (!hasSearchText() || !layout.leftSearchClearBounds.contains(mouseX, mouseY)) {
            return false;
        }

        searchQuery = "";
        if (searchField != null) {
            searchField.setText("");
        }
        page = 0;
        detailScroll = 0;
        if (rightPageMode == RightPageMode.VIEW) {
            ensureSelection(filteredEntries());
        }
        return true;
    }

    private boolean handlePagerClick(JournalLayout layout, double mouseX, double mouseY) {
        List<DiaryEntry> entries = filteredEntries();
        int totalPages = totalPages(entries, visibleRows(layout));
        if (entries.isEmpty() || totalPages <= 1) {
            return false;
        }

        if (page > 0 && isInside(mouseX, mouseY, layout.leftContentX, layout.pagerY, layout.pagerButtonWidth, layout.footerButtonHeight)) {
            syncComposeDraft();
            page = Math.max(0, page - 1);
            rebuildWidgets();
            return true;
        }

        if (page + 1 < totalPages && isInside(mouseX, mouseY, layout.leftContentRight - layout.pagerButtonWidth, layout.pagerY, layout.pagerButtonWidth, layout.footerButtonHeight)) {
            syncComposeDraft();
            page = Math.min(totalPages - 1, page + 1);
            rebuildWidgets();
            return true;
        }

        return false;
    }

    private boolean handleSortClick(JournalLayout layout, double mouseX, double mouseY) {
        if (!sortButtonBounds(layout).contains(mouseX, mouseY)) {
            return false;
        }

        syncComposeDraft();
        sortMode = sortMode.next();
        DearDiaryClientConfig.get().saveDiaryListSortMode(sortMode);
        List<DiaryEntry> entries = filteredEntries();
        ensureSelection(entries);
        movePageToSelected(entries, visibleRows(layout));
        rebuildWidgets();
        return true;
    }

    private Optional<DiaryFilter> filterAt(JournalLayout layout, double mouseX, double mouseY) {
        int x = layout.leftTabsBounds.x();
        for (DiaryFilter value : DiaryFilter.values()) {
            if (value == DiaryFilter.FAVORITES) {
                x += sortButtonWidth(layout) + layout.tabGap;
            }

            int tabWidth = filterTabWidth(value, filterTabText(value, layout), layout);
            if (isInside(mouseX, mouseY, x, layout.filterY, tabWidth, layout.tabHeight)) {
                return Optional.of(value);
            }
            x += tabWidth + layout.tabGap;
        }

        return Optional.empty();
    }

    private Component filterTabText(DiaryFilter value, JournalLayout layout) {
        if (!layout.compactLayout) {
            return value.tabText();
        }

        return switch (value) {
            case ALL -> Component.translatable("screen.dear_diary.filter.all.short");
            case MANUAL -> Component.translatable("screen.dear_diary.filter.manual.short");
            case AUTOMATIC -> Component.translatable("screen.dear_diary.filter.automatic.short");
            case FAVORITES -> value.tabText();
        };
    }

    private int filterTabWidth(DiaryFilter value, Component label, JournalLayout layout) {
        if (value == DiaryFilter.FAVORITES) {
            return layout.compactLayout ? 22 : 24;
        }

        int desired = font.width(label) + (layout.compactLayout ? COMPACT_TAB_PADDING : REGULAR_TAB_PADDING);
        if (layout.compactLayout) {
            return switch (value) {
                case ALL -> clamp(desired, 20, 22);
                case MANUAL -> clamp(desired, 28, 32);
                case AUTOMATIC -> clamp(desired, 26, 28);
                case FAVORITES -> 22;
            };
        }

        return clamp(desired, 32, 58);
    }

    private Rect sortButtonBounds(JournalLayout layout) {
        int x = layout.leftTabsBounds.x();
        for (DiaryFilter value : DiaryFilter.values()) {
            if (value == DiaryFilter.FAVORITES) {
                break;
            }
            x += filterTabWidth(value, filterTabText(value, layout), layout) + layout.tabGap;
        }

        return new Rect(x, layout.filterY, sortButtonWidth(layout), layout.tabHeight);
    }

    private int sortButtonWidth(JournalLayout layout) {
        int padding = layout.compactLayout ? COMPACT_TAB_PADDING : REGULAR_TAB_PADDING;
        int minimum = layout.compactLayout ? 28 : 38;
        int maximum = layout.compactLayout ? 34 : 58;
        return clamp(Math.max(
                font.width(Component.translatable("screen.dear_diary.sort.date.short")) + padding,
                font.width(Component.translatable("screen.dear_diary.sort.importance.short")) + padding
        ), minimum, maximum);
    }

    private int tabLabelY(JournalLayout layout) {
        return layout.filterY + Math.max(1, (layout.tabHeight - font.lineHeight) / 2);
    }

    private int tabTextInset(JournalLayout layout) {
        return layout.compactLayout ? 4 : 6;
    }

    private Optional<DiaryEntry> entryAt(JournalLayout layout, double mouseX, double mouseY) {
        if (!isInside(mouseX, mouseY, layout.leftContentX, layout.listY, layout.leftContentWidth, layout.listHeight)) {
            return Optional.empty();
        }

        List<DiaryEntry> entries = filteredEntries();
        int visibleRows = visibleRows(layout);
        int relativeY = (int) mouseY - layout.listY;
        int row = relativeY / ENTRY_ROW_HEIGHT;
        if (row < 0 || row >= visibleRows) {
            return Optional.empty();
        }

        int index = page * visibleRows + row;
        if (index < 0 || index >= entries.size()) {
            return Optional.empty();
        }

        return Optional.of(entries.get(index));
    }

    private Optional<Rect> coordinateLineBounds(JournalLayout layout, DiaryEntry entry) {
        if (entry == null || !entry.hasLocation()) {
            return Optional.empty();
        }

        int x = layout.rightInnerBounds.x();
        int contentWidth = layout.rightInnerBounds.width();
        int y;
        if (DiaryEntryMarkers.isChapterEntry(entry)) {
            int titleY = Math.max(layout.rightViewHeaderBounds.y(), layout.rightViewMetaBounds.y() - 44);
            int dateY = Math.max(titleY + DETAIL_TITLE_LINE_HEIGHT + 18, layout.rightViewMetaBounds.y() - 8);
            y = dateY + 11;
        } else {
            y = layout.rightViewMetaBounds.y() + 22;
        }

        String visibleText = ellipsize(DiaryUiText.location(entry), contentWidth);
        int textWidth = Math.min(contentWidth, font.width(visibleText));
        return Optional.of(new Rect(x - 1, y - 1, Math.max(1, textWidth + 2), font.lineHeight + 2));
    }

    private static String coordinateClipboardText(DiaryEntry entry) {
        return entry.getX() + " " + entry.getY() + " " + entry.getZ();
    }

    private List<DiaryEntry> filteredEntries() {
        String normalizedQuery = normalizedSearchQuery();
        List<DiaryEntry> entries = new ArrayList<>(
                ClientDiaryCache.currentDiary()
                        .map(diary -> diary.entriesView().stream()
                                .filter(filter::matches)
                                .filter(entry -> matchesSearch(entry, normalizedQuery))
                                .toList())
                        .orElse(List.of())
        );
        entries.sort(entryComparator());
        return entries;
    }

    private boolean matchesSearch(DiaryEntry entry, String normalizedQuery) {
        if (normalizedQuery.isEmpty()) {
            return true;
        }

        return containsSearchText(safeTitle(entry), normalizedQuery)
                || containsSearchText(safeText(entry), normalizedQuery);
    }

    private boolean containsSearchText(String value, String normalizedQuery) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(normalizedQuery);
    }

    private String normalizedSearchQuery() {
        return searchQuery == null ? "" : searchQuery.trim().toLowerCase(Locale.ROOT);
    }

    private boolean hasSearchText() {
        return searchQuery != null && !searchQuery.isEmpty();
    }

    private boolean syncSearchQueryFromField() {
        if (searchField == null) {
            return false;
        }

        String value = searchField.getText();
        if (value == null) {
            value = "";
        }
        if (value.equals(searchQuery)) {
            return false;
        }

        searchQuery = value;
        page = 0;
        detailScroll = 0;
        if (rightPageMode == RightPageMode.VIEW) {
            ensureSelection(filteredEntries());
        }
        return true;
    }

    private Component emptyEntryListText() {
        if (ClientDiaryCache.currentDiary().isEmpty()) {
            return Component.translatable("screen.dear_diary.loading");
        }
        if (currentDiaryIsEmpty()) {
            return Component.translatable("screen.dear_diary.empty");
        }
        if (!normalizedSearchQuery().isEmpty()) {
            return Component.translatable("screen.dear_diary.search.no_results");
        }
        return Component.translatable("screen.dear_diary.empty");
    }

    private Comparator<DiaryEntry> entryComparator() {
        Comparator<DiaryEntry> dateDesc = Comparator.comparing(DiaryEntry::getCreatedAt).reversed();
        return switch (sortMode) {
            case DATE_DESC -> dateDesc;
            case IMPORTANCE_DESC -> Comparator
                    .comparingInt((DiaryEntry entry) -> importanceRank(entry.getImportance()))
                    .reversed()
                    .thenComparing(dateDesc);
        };
    }

    private int importanceRank(DiaryImportance importance) {
        if (importance == null) {
            return 2;
        }

        return switch (importance) {
            case LEGENDARY -> 4;
            case MAJOR -> 3;
            case NORMAL -> 2;
            case MINOR -> 1;
        };
    }

    private Component sortModeText() {
        return Component.translatable(sortMode == DiaryListSortMode.DATE_DESC
                ? "screen.dear_diary.sort.date.short"
                : "screen.dear_diary.sort.importance.short");
    }

    private Component sortModeTooltipText() {
        return Component.translatable(sortMode == DiaryListSortMode.DATE_DESC
                ? "screen.dear_diary.sort.date.tooltip"
                : "screen.dear_diary.sort.importance.tooltip");
    }

    private Optional<DiaryEntry> selectedEntry() {
        if (selectedEntryId == null) {
            return Optional.empty();
        }

        return ClientDiaryCache.currentDiary()
                .flatMap(diary -> diary.findEntry(selectedEntryId));
    }

    private void ensureSelection(List<DiaryEntry> entries) {
        boolean selectedStillVisible = selectedEntryId != null && entries.stream().anyMatch(entry -> entry.getId().equals(selectedEntryId));
        if (entries.isEmpty()) {
            selectedEntryId = null;
            detailScroll = 0;
            page = 0;
            return;
        }

        if (!selectedStillVisible) {
            selectedEntryId = entries.get(0).getId();
            detailScroll = 0;
            page = 0;
        }
    }

    private void movePageToSelected(List<DiaryEntry> entries, int visibleRows) {
        if (selectedEntryId == null || entries.isEmpty()) {
            page = 0;
            return;
        }

        for (int index = 0; index < entries.size(); index++) {
            if (entries.get(index).getId().equals(selectedEntryId)) {
                page = clamp(index / Math.max(1, visibleRows), 0, Math.max(0, totalPages(entries, visibleRows) - 1));
                return;
            }
        }

        page = 0;
    }

    private boolean currentDiaryIsEmpty() {
        return ClientDiaryCache.currentDiary().map(diary -> diary.entriesView().isEmpty()).orElse(false);
    }

    private int visibleRows(JournalLayout layout) {
        return Math.max(1, layout.listHeight / ENTRY_ROW_HEIGHT);
    }

    private int totalPages(List<DiaryEntry> entries, int visibleRows) {
        return Math.max(1, (int) Math.ceil(entries.size() / (double) visibleRows));
    }

    private Component newEntryButtonText(JournalLayout layout) {
        return Component.translatable(layout.compactLayout
                ? "screen.dear_diary.new_entry.short"
                : "screen.dear_diary.new_entry");
    }

    private Component deleteButtonText(JournalLayout layout) {
        return Component.translatable(layout.compactLayout
                ? "screen.dear_diary.delete.short"
                : "screen.dear_diary.delete");
    }

    private Component shareButtonText(JournalLayout layout) {
        return Component.translatable(layout.compactLayout
                ? "screen.dear_diary.share.short"
                : "screen.dear_diary.share");
    }

    private Component saveButtonText(JournalLayout layout) {
        if (isComposingChapter()) {
            return Component.translatable("screen.dear_diary.create_chapter");
        }

        return Component.translatable(layout.compactLayout
                ? "screen.dear_diary.save.short"
                : "screen.dear_diary.save");
    }

    private Component cancelButtonText(JournalLayout layout) {
        return Component.translatable(layout.compactLayout
                ? "screen.dear_diary.cancel.short"
                : "screen.dear_diary.cancel");
    }

    private Component ellipsizedText(String translationKey, int maxWidth) {
        return Component.literal(ellipsize(Component.translatable(translationKey).getString(), maxWidth));
    }

    private Rect composeModeEntryBounds(JournalLayout layout) {
        int gap = Math.max(1, layout.tabGap);
        int width = Math.max(1, (layout.rightComposeModeBounds.width() - gap) / 2);
        return new Rect(layout.rightComposeModeBounds.x(), layout.rightComposeModeBounds.y(), width, layout.rightComposeModeBounds.height());
    }

    private Rect composeModeChapterBounds(JournalLayout layout) {
        Rect entryBounds = composeModeEntryBounds(layout);
        int x = entryBounds.right() + Math.max(1, layout.tabGap);
        int width = Math.max(1, layout.rightComposeModeBounds.right() - x);
        return new Rect(x, layout.rightComposeModeBounds.y(), width, layout.rightComposeModeBounds.height());
    }

    private boolean selectedEntryIsChapter() {
        return selectedEntry().filter(DiaryEntryMarkers::isChapterEntry).isPresent();
    }

    private Component entryKindLabel(DiaryEntry entry, JournalLayout layout) {
        return layout.compactLayout ? DiaryUiText.entryKindShort(entry) : DiaryUiText.entryKind(entry);
    }

    private Component categoryLabel(DiaryEntry entry, JournalLayout layout) {
        return layout.compactLayout ? DiaryUiText.categoryShort(entry) : DiaryUiText.category(entry);
    }

    private Component importanceLabel(DiaryEntry entry, JournalLayout layout) {
        return layout.compactLayout ? DiaryUiText.importanceShort(entry) : DiaryUiText.importance(entry);
    }

    private String safeTitle(DiaryEntry entry) {
        String titleText = entry.getResolvedTitle();
        return titleText == null || titleText.isBlank() ? Component.translatable("screen.dear_diary.untitled").getString() : titleText;
    }

    private String safeText(DiaryEntry entry) {
        String text = entry.getResolvedText();
        return text == null ? "" : text;
    }

    private void confirmDelete(DiaryEntry entry) {
        minecraft.setScreen(new ConfirmScreen(
                confirmed -> {
                    if (confirmed) {
                        DearDiaryClientNetworking.deleteEntry(entry.getId());
                        minecraft.setScreen(new DiaryScreen(null));
                    } else {
                        minecraft.setScreen(this);
                    }
                },
                Component.translatable("screen.dear_diary.confirm.delete.title"),
                Component.translatable("screen.dear_diary.confirm.delete.body"),
                Component.translatable("screen.dear_diary.delete"),
                Component.translatable("screen.dear_diary.cancel")
        ));
    }

    private void confirmShare(DiaryEntry entry) {
        minecraft.setScreen(new ConfirmScreen(
                confirmed -> {
                    if (confirmed) {
                        DearDiaryClientNetworking.shareEntry(entry.getId());
                    }
                    minecraft.setScreen(this);
                },
                Component.translatable("screen.dear_diary.confirm.share.title"),
                Component.translatable("screen.dear_diary.confirm.share.body"),
                Component.translatable("screen.dear_diary.share"),
                Component.translatable("screen.dear_diary.cancel")
        ));
    }

    private void addSearchWidget(JournalLayout layout) {
        searchField = new DiaryTitleFieldWidget(
                font,
                layout.leftSearchTextBounds.x(),
                layout.leftSearchTextBounds.y(),
                layout.leftSearchTextBounds.width(),
                layout.leftSearchTextBounds.height(),
                Component.translatable("screen.dear_diary.search.placeholder")
        );
        searchField.setMaxLength(MAX_SEARCH_LENGTH);
        searchField.setPlaceholder(Component.translatable("screen.dear_diary.search.placeholder"));
        searchField.setText(searchQuery);
        addRenderableWidget(searchField);
    }

    private void addFormWidgets(JournalLayout layout) {
        Rect textFieldBounds = formTextFieldBounds(layout);
        composeTitleField = new DiaryTitleFieldWidget(
                font,
                layout.rightComposeTitleFieldBounds.x() + 6,
                layout.rightComposeTitleFieldBounds.y() + 5,
                layout.rightComposeTitleFieldBounds.width() - 12,
                14,
                Component.translatable("screen.dear_diary.field.title")
        );
        composeTitleField.setMaxLength(MAX_TITLE_LENGTH);
        composeTitleField.setPlaceholder(isComposingChapter()
                ? Component.translatable("screen.dear_diary.chapter.title_placeholder")
                : Component.empty());
        composeTitleField.setText(composeTitleDraft);
        addRenderableWidget(composeTitleField);

        composeTextArea = new DiaryTextAreaWidget(
                font,
                textFieldBounds.x(),
                textFieldBounds.y(),
                textFieldBounds.width(),
                textFieldBounds.height(),
                Component.translatable("screen.dear_diary.field.text")
        );
        composeTextArea.setMaxLength(MAX_TEXT_LENGTH);
        composeTextArea.setPlaceholder(isComposingChapter()
                ? Component.translatable("screen.dear_diary.chapter.text_placeholder")
                : Component.translatable("screen.dear_diary.field.text.placeholder"));
        composeTextArea.setText(composeTextDraft);
        addRenderableWidget(composeTextArea);
    }

    private Rect formTextFieldBounds(JournalLayout layout) {
        if (!isEditMode()) {
            return layout.rightComposeTextFieldBounds;
        }

        return new Rect(
                layout.rightComposeTextFieldBounds.x(),
                layout.rightComposeTextFieldBounds.y(),
                layout.rightComposeTextFieldBounds.width(),
                Math.max(24, layout.rightComposeFooterBounds.y() - layout.rightComposeTextFieldBounds.y() - 5)
        );
    }

    private void enterComposeMode() {
        rightPageMode = RightPageMode.COMPOSE;
        composeEntryMode = ComposeEntryMode.ENTRY;
        composeAttachCoordinates = true;
        composeTitleDraft = "";
        composeTextDraft = "";
        composeErrorText = null;
        detailScroll = 0;
        rebuildWidgets();
    }

    private void enterEditMode(DiaryEntry entry) {
        if (!entry.isEditable()) {
            composeErrorText = Component.translatable("screen.dear_diary.error.not_editable");
            return;
        }

        selectedEntryId = entry.getId();
        rightPageMode = RightPageMode.EDIT;
        composeTitleDraft = entry.getResolvedTitle() == null ? "" : entry.getResolvedTitle();
        composeTextDraft = entry.getResolvedText() == null ? "" : entry.getResolvedText();
        composeErrorText = null;
        detailScroll = 0;
        rebuildWidgets();
    }

    private void cancelFormMode() {
        rightPageMode = RightPageMode.VIEW;
        composeTitleDraft = "";
        composeTextDraft = "";
        composeErrorText = null;
        rebuildWidgets();
    }

    private void saveFormEntry() {
        syncComposeDraft();
        boolean editingChapter = isEditMode() && selectedEntryIsChapter();
        if (isComposingChapter()) {
            if (composeTitleDraft == null || composeTitleDraft.isBlank()) {
                composeErrorText = Component.translatable("commands.dear_diary.chapter.empty_title");
                return;
            }
            if (composeTitleDraft.strip().length() > MAX_TITLE_LENGTH) {
                composeErrorText = Component.translatable("commands.dear_diary.chapter.title_too_long");
                return;
            }

            DearDiaryClientNetworking.createChapterEntry(composeTitleDraft, composeTextDraft, composeAttachCoordinates);
            rightPageMode = RightPageMode.VIEW;
            composeEntryMode = ComposeEntryMode.ENTRY;
            composeTitleDraft = "";
            composeTextDraft = "";
            composeErrorText = null;
            selectedEntryId = null;
            page = 0;
            detailScroll = 0;
            selectNewestAfterSnapshot = true;
            rebuildWidgets();
            return;
        }

        if (!editingChapter && (composeTextDraft == null || composeTextDraft.isBlank())) {
            composeErrorText = Component.translatable("screen.dear_diary.error.text_empty");
            return;
        }
        if (editingChapter && (composeTitleDraft == null || composeTitleDraft.isBlank())) {
            composeErrorText = Component.translatable("commands.dear_diary.chapter.empty_title");
            return;
        }

        if (isEditMode()) {
            Optional<DiaryEntry> selected = selectedEntry();
            if (selected.isEmpty() || !selected.get().isEditable()) {
                composeErrorText = Component.translatable("screen.dear_diary.error.not_editable");
                return;
            }

            selectedEntryId = selected.get().getId();
            DearDiaryClientNetworking.editEntry(selected.get().getId(), composeTitleDraft, composeTextDraft);
            rightPageMode = RightPageMode.VIEW;
            composeTitleDraft = "";
            composeTextDraft = "";
            composeErrorText = null;
            detailScroll = 0;
            rebuildWidgets();
            return;
        }

        DearDiaryClientNetworking.createManualEntry(composeTitleDraft, composeTextDraft, composeAttachCoordinates);
        rightPageMode = RightPageMode.VIEW;
        composeTitleDraft = "";
        composeTextDraft = "";
        composeErrorText = null;
        selectedEntryId = null;
        page = 0;
        detailScroll = 0;
        selectNewestAfterSnapshot = true;
        rebuildWidgets();
    }

    private void syncComposeDraft() {
        if (composeTitleField != null) {
            composeTitleDraft = composeTitleField.getText();
        }
        if (composeTextArea != null) {
            composeTextDraft = composeTextArea.getText();
        }
    }

    private void startListening() {
        if (!listening) {
            ClientDiaryCache.addListener(this);
            listening = true;
        }
    }

    private void stopListening() {
        if (listening) {
            ClientDiaryCache.removeListener(this);
            listening = false;
        }
    }

    private boolean isInside(double mouseX, double mouseY, int x, int y, int rectWidth, int rectHeight) {
        return mouseX >= x && mouseX < x + rectWidth && mouseY >= y && mouseY < y + rectHeight;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private record Rect(int x, int y, int width, int height) {
        private int right() {
            return x + width;
        }

        private int bottom() {
            return y + height;
        }

        private int centerX() {
            return x + width / 2;
        }

        private boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX < right() && mouseY >= y && mouseY < bottom();
        }

        private Rect inset(int left, int top, int right, int bottom) {
            int newX = x + left;
            int newY = y + top;
            int newWidth = Math.max(1, width - left - right);
            int newHeight = Math.max(1, height - top - bottom);
            return new Rect(newX, newY, newWidth, newHeight);
        }
    }

    private record JournalLayout(
            Rect bookBounds,
            Rect headerBounds,
            Rect spineBounds,
            Rect leftPageBounds,
            Rect rightPageBounds,
            Rect leftInnerBounds,
            Rect rightInnerBounds,
            Rect leftTabsBounds,
            Rect leftSearchBounds,
            Rect leftSearchTextBounds,
            Rect leftSearchClearBounds,
            Rect leftListBounds,
            Rect leftFooterBounds,
            Rect rightViewHeaderBounds,
            Rect rightViewMetaBounds,
            Rect rightViewTextBounds,
            Rect rightViewFooterBounds,
            Rect rightComposeHeaderBounds,
            Rect rightComposeModeBounds,
            Rect rightComposeTitleFieldBounds,
            Rect rightComposeTextFieldBounds,
            Rect rightComposeCoordinateBounds,
            Rect rightComposeFooterBounds,
            Rect leftContentBounds,
            Rect rightContentBounds,
            Rect leftFilterTabsBounds,
            Rect leftEntryListBounds,
            Rect rightViewTitleBounds,
            Rect rightActionButtonsBounds,
            Rect rightComposeButtonsBounds,
            Rect topTitleBounds,
            Rect topGlobalButtonsBounds,
            int bookX,
            int bookY,
            int bookWidth,
            int bookHeight,
            float scale,
            boolean compactLayout,
            int centerX,
            int titleY,
            int titleMaxWidth,
            int leftPageX,
            int leftPageY,
            int leftPageWidth,
            int rightPageX,
            int rightPageY,
            int rightPageWidth,
            int pageHeight,
            int leftContentX,
            int leftContentRight,
            int leftContentWidth,
            int rightContentX,
            int rightContentY,
            int rightContentWidth,
            int filterY,
            int searchY,
            int listY,
            int listHeight,
            int pagerY,
            int headerButtonY,
            int headerButtonHeight,
            int newButtonX,
            int newButtonWidth,
            int closeButtonX,
            int closeButtonWidth,
            int tabHeight,
            int tabGap,
            int footerButtonHeight,
            int pagerButtonWidth,
            int detailActionX,
            int detailActionY,
            int detailButtonWidth,
            int detailButtonGap,
            int composeTitleY,
            int composeTitleFieldX,
            int composeTitleFieldY,
            int composeTextAreaX,
            int composeTextAreaY,
            int composeTextAreaHeight,
            int composeFieldWidth,
            int composeCheckboxX,
            int composeCheckboxY,
            int composeActionY,
            int composeCancelX,
            int composeCancelWidth,
            int composeSaveX,
            int composeSaveWidth
    ) {
        private static JournalLayout create(int screenWidth, int screenHeight, Font font) {
            int maxWidth = Math.max(260, Math.round(screenWidth * 0.94F));
            int maxHeight = Math.max(220, Math.round(screenHeight * 0.90F));
            int minHeight = Math.min(maxHeight, Math.max(196, Math.round(screenHeight * 0.68F)));
            int targetHeight = Math.round(screenHeight * 0.86F * BOOK_SCALE_MULTIPLIER);
            int bookHeight = clampValue(targetHeight, minHeight, maxHeight);
            int bookWidth = Math.round(bookHeight * BOOK_ASPECT_RATIO);
            if (bookWidth > maxWidth) {
                bookWidth = maxWidth;
                bookHeight = Math.max(196, Math.round(bookWidth / BOOK_ASPECT_RATIO));
            }
            float scale = bookHeight / (float) BOOK_TEXTURE_HEIGHT;
            int bookX = (screenWidth - bookWidth) / 2;
            int bookY = (screenHeight - bookHeight) / 2;
            boolean compactLayout = bookWidth < 900 || screenHeight <= 620;

            Rect bookBounds = new Rect(bookX, bookY, bookWidth, bookHeight);
            Rect headerBounds = scaledRect(bookX, bookY, scale, 154, 78, 1108, 38);
            Rect spineBounds = scaledRect(bookX, bookY, scale, 666, 112, 122, 860);
            Rect leftPageBounds = scaledRect(bookX, bookY, scale, 118, 140, 548, 790);
            Rect rightPageBounds = scaledRect(bookX, bookY, scale, 784, 140, 535, 790);

            Rect leftInnerBounds = leftPageBounds.inset(scaled(scale, 32), scaled(scale, 42), scaled(scale, 38), scaled(scale, 68));
            Rect rightInnerBounds = rightPageBounds.inset(scaled(scale, 36), scaled(scale, 104), scaled(scale, 40), scaled(scale, 70));

            int sectionGap = compactLayout ? Math.max(4, scaled(scale, 6)) : Math.max(5, scaled(scale, 8));
            int smallGap = compactLayout ? Math.max(3, scaled(scale, 4)) : Math.max(4, scaled(scale, 6));
            int tabHeight = compactLayout ? 15 : 18;
            int tabGap = compactLayout ? 1 : Math.max(2, scaled(scale, 4));
            int footerButtonHeight = compactLayout ? 15 : 18;
            int searchHeight = compactLayout ? 15 : 18;

            Rect leftTabsBounds = new Rect(leftInnerBounds.x(), leftInnerBounds.y(), leftInnerBounds.width(), tabHeight);
            Rect leftFooterBounds = new Rect(leftInnerBounds.x(), leftInnerBounds.bottom() - footerButtonHeight, leftInnerBounds.width(), footerButtonHeight);
            Rect leftSearchBounds = new Rect(
                    leftInnerBounds.x(),
                    leftTabsBounds.bottom() + smallGap,
                    leftInnerBounds.width(),
                    searchHeight
            );
            Rect leftSearchClearBounds = new Rect(
                    leftSearchBounds.right() - searchHeight,
                    leftSearchBounds.y(),
                    searchHeight,
                    searchHeight
            );
            Rect leftSearchTextBounds = new Rect(
                    leftSearchBounds.x() + 5,
                    leftSearchBounds.y() + 1,
                    Math.max(12, leftSearchBounds.width() - searchHeight - 10),
                    Math.max(10, searchHeight - 2)
            );
            int listY = leftSearchBounds.bottom() + sectionGap;
            Rect leftListBounds = new Rect(
                    leftInnerBounds.x(),
                    listY,
                    leftInnerBounds.width(),
                    Math.max(ENTRY_ROW_HEIGHT, leftFooterBounds.y() - listY - sectionGap)
            );

            int detailButtonGap = compactLayout ? Math.max(3, scaled(scale, 4)) : Math.max(5, scaled(scale, 7));
            int viewFooterHeight = footerButtonHeight * 2 + detailButtonGap;
            Rect rightViewFooterBounds = new Rect(
                    rightInnerBounds.x(),
                    rightInnerBounds.bottom() - viewFooterHeight,
                    rightInnerBounds.width(),
                    viewFooterHeight
            );
            int topDecorativeLineY = sy(bookY, scale, RIGHT_PAGE_TOP_DECORATIVE_LINE_Y);
            int bottomDecorativeLineY = sy(bookY, scale, RIGHT_PAGE_BOTTOM_DECORATIVE_LINE_Y);
            int titleTop = sy(bookY, scale, compactLayout ? 188 : 184);
            int titleBottom = topDecorativeLineY - Math.max(3, scaled(scale, compactLayout ? 12 : 16));
            Rect rightViewHeaderBounds = new Rect(
                    rightInnerBounds.x(),
                    titleTop,
                    rightInnerBounds.width(),
                    Math.max(12, titleBottom - titleTop)
            );
            int metadataTop = topDecorativeLineY + Math.max(5, scaled(scale, compactLayout ? 26 : 30));
            int metadataBottom = bottomDecorativeLineY - Math.max(5, scaled(scale, compactLayout ? 22 : 28));
            Rect rightViewMetaBounds = new Rect(
                    rightInnerBounds.x(),
                    metadataTop,
                    rightInnerBounds.width(),
                    Math.max(32, metadataBottom - metadataTop)
            );
            int rightViewTextTop = bottomDecorativeLineY + Math.max(8, scaled(scale, compactLayout ? 24 : 30));
            Rect rightViewTextBounds = new Rect(
                    rightInnerBounds.x(),
                    rightViewTextTop,
                    rightInnerBounds.width(),
                    Math.max(40, rightViewFooterBounds.y() - rightViewTextTop - sectionGap)
            );

            Rect rightComposeFooterBounds = new Rect(
                    rightInnerBounds.x(),
                    rightInnerBounds.bottom() - footerButtonHeight,
                    rightInnerBounds.width(),
                    footerButtonHeight
            );
            Rect rightComposeHeaderBounds = new Rect(
                    rightInnerBounds.x(),
                    titleTop,
                    rightInnerBounds.width(),
                    Math.max(12, titleBottom - titleTop)
            );
            int composeModeY = topDecorativeLineY + Math.max(5, scaled(scale, compactLayout ? 8 : 10));
            int composeModeWidth = Math.min(rightInnerBounds.width(), compactLayout ? 112 : 142);
            Rect rightComposeModeBounds = new Rect(
                    rightInnerBounds.x(),
                    composeModeY,
                    composeModeWidth,
                    compactLayout ? 15 : 18
            );
            int composeTitleFieldY = rightComposeModeBounds.bottom() + Math.max(18, scaled(scale, compactLayout ? 20 : 24));
            Rect rightComposeTitleFieldBounds = new Rect(rightInnerBounds.x(), composeTitleFieldY, rightInnerBounds.width(), compactLayout ? 22 : 24);
            Rect rightComposeCoordinateBounds = new Rect(
                    rightInnerBounds.x(),
                    rightComposeFooterBounds.y() - (compactLayout ? 34 : 42) - sectionGap,
                    rightInnerBounds.width(),
                    compactLayout ? 34 : 42
            );
            int preferredComposeTextY = bottomDecorativeLineY + Math.max(18, scaled(scale, compactLayout ? 34 : 42));
            int earliestComposeTextY = rightComposeTitleFieldBounds.bottom() + Math.max(18, scaled(scale, compactLayout ? 24 : 30));
            int latestComposeTextY = rightComposeCoordinateBounds.y() - sectionGap - 36;
            int composeTextY = Math.max(earliestComposeTextY, Math.min(preferredComposeTextY, latestComposeTextY));
            int composeTextHeight = Math.max(24, rightComposeCoordinateBounds.y() - composeTextY - sectionGap);
            Rect rightComposeTextFieldBounds = new Rect(
                    rightInnerBounds.x(),
                    composeTextY,
                    rightInnerBounds.width(),
                    composeTextHeight
            );

            int headerButtonHeight = compactLayout ? 14 : 16;
            int headerGap = compactLayout ? Math.max(3, scaled(scale, 4)) : Math.max(4, scaled(scale, 6));
            Component closeText = Component.translatable("screen.dear_diary.close");
            Component newEntryText = Component.translatable(compactLayout
                    ? "screen.dear_diary.new_entry.short"
                    : "screen.dear_diary.new_entry");
            int closeButtonWidth = buttonWidth(font, closeText, compactLayout ? 44 : 52, compactLayout ? 66 : 82, compactLayout ? 8 : 12);
            int newButtonWidth = buttonWidth(font, newEntryText, compactLayout ? 54 : 70, compactLayout ? 82 : 112, compactLayout ? 8 : 12);
            int headerButtonsWidth = newButtonWidth + closeButtonWidth + headerGap;
            if (headerButtonsWidth > headerBounds.width() / 2) {
                int sharedWidth = Math.max(compactLayout ? 44 : 52, (headerBounds.width() / 2 - headerGap) / 2);
                newButtonWidth = sharedWidth;
                closeButtonWidth = sharedWidth;
                headerButtonsWidth = newButtonWidth + closeButtonWidth + headerGap;
            }

            int headerButtonY = headerBounds.y() + Math.max(0, (headerBounds.height() - headerButtonHeight) / 2);
            Rect topGlobalButtonsBounds = new Rect(
                    headerBounds.right() - headerButtonsWidth,
                    headerButtonY,
                    headerButtonsWidth,
                    headerButtonHeight
            );
            Rect topTitleBounds = new Rect(
                    headerBounds.x(),
                    headerBounds.y() + Math.max(0, (headerBounds.height() - 10) / 2),
                    Math.max(24, topGlobalButtonsBounds.x() - headerBounds.x() - headerGap * 2),
                    12
            );

            int centerX = topTitleBounds.centerX();
            int titleY = topTitleBounds.y();
            int titleMaxWidth = topTitleBounds.width();
            int newButtonX = topGlobalButtonsBounds.x();
            int closeButtonX = newButtonX + newButtonWidth + headerGap;

            int pagerButtonWidth = Math.min(compactLayout ? 56 : 66, Math.max(compactLayout ? 44 : 52, (leftFooterBounds.width() - 50) / 2));
            int detailButtonWidth = Math.max(compactLayout ? 34 : 50, Math.min(compactLayout ? 58 : 92, (rightViewFooterBounds.width() - detailButtonGap) / 2));
            int detailActionX = rightViewFooterBounds.x() + Math.max(0, (rightViewFooterBounds.width() - detailButtonWidth * 2 - detailButtonGap) / 2);

            Component saveText = Component.translatable(compactLayout ? "screen.dear_diary.save.short" : "screen.dear_diary.save");
            Component createChapterText = Component.translatable("screen.dear_diary.create_chapter");
            Component cancelText = Component.translatable(compactLayout ? "screen.dear_diary.cancel.short" : "screen.dear_diary.cancel");
            int composeSaveWidth = Math.max(
                    buttonWidth(font, saveText, compactLayout ? 54 : COMPOSE_SAVE_WIDTH, compactLayout ? 78 : 112, compactLayout ? 12 : 20),
                    buttonWidth(font, createChapterText, compactLayout ? 70 : COMPOSE_SAVE_WIDTH, compactLayout ? 96 : 120, compactLayout ? 12 : 20)
            );
            int composeCancelWidth = buttonWidth(font, cancelText, compactLayout ? 54 : COMPOSE_CANCEL_WIDTH, compactLayout ? 82 : 100, compactLayout ? 12 : 20);
            int composeTotalWidth = composeCancelWidth + composeSaveWidth + headerGap;
            if (composeTotalWidth > rightComposeFooterBounds.width()) {
                int sharedWidth = Math.max(compactLayout ? 44 : 48, (rightComposeFooterBounds.width() - headerGap) / 2);
                composeCancelWidth = sharedWidth;
                composeSaveWidth = sharedWidth;
                composeTotalWidth = composeCancelWidth + composeSaveWidth + headerGap;
            }
            int composeSaveX = rightComposeFooterBounds.right() - composeSaveWidth;
            int composeCancelX = composeSaveX - headerGap - composeCancelWidth;

            return new JournalLayout(
                    bookBounds,
                    headerBounds,
                    spineBounds,
                    leftPageBounds,
                    rightPageBounds,
                    leftInnerBounds,
                    rightInnerBounds,
                    leftTabsBounds,
                    leftSearchBounds,
                    leftSearchTextBounds,
                    leftSearchClearBounds,
                    leftListBounds,
                    leftFooterBounds,
                    rightViewHeaderBounds,
                    rightViewMetaBounds,
                    rightViewTextBounds,
                    rightViewFooterBounds,
                    rightComposeHeaderBounds,
                    rightComposeModeBounds,
                    rightComposeTitleFieldBounds,
                    rightComposeTextFieldBounds,
                    rightComposeCoordinateBounds,
                    rightComposeFooterBounds,
                    leftInnerBounds,
                    rightInnerBounds,
                    leftTabsBounds,
                    leftListBounds,
                    rightViewHeaderBounds,
                    rightViewFooterBounds,
                    rightComposeFooterBounds,
                    topTitleBounds,
                    topGlobalButtonsBounds,
                    bookX,
                    bookY,
                    bookWidth,
                    bookHeight,
                    scale,
                    compactLayout,
                    centerX,
                    titleY,
                    titleMaxWidth,
                    leftPageBounds.x(),
                    leftPageBounds.y(),
                    leftPageBounds.width(),
                    rightPageBounds.x(),
                    rightPageBounds.y(),
                    rightPageBounds.width(),
                    Math.max(120, leftPageBounds.height()),
                    leftInnerBounds.x(),
                    leftInnerBounds.right(),
                    Math.max(80, leftInnerBounds.width()),
                    rightInnerBounds.x(),
                    rightInnerBounds.y(),
                    Math.max(80, rightInnerBounds.width()),
                    leftTabsBounds.y(),
                    leftSearchBounds.y(),
                    leftListBounds.y(),
                    Math.max(ENTRY_ROW_HEIGHT, leftListBounds.height()),
                    leftFooterBounds.y(),
                    headerButtonY,
                    headerButtonHeight,
                    newButtonX,
                    newButtonWidth,
                    closeButtonX,
                    closeButtonWidth,
                    tabHeight,
                    tabGap,
                    footerButtonHeight,
                    pagerButtonWidth,
                    detailActionX,
                    rightViewFooterBounds.y(),
                    detailButtonWidth,
                    detailButtonGap,
                    rightComposeHeaderBounds.y(),
                    rightComposeTitleFieldBounds.x(),
                    rightComposeTitleFieldBounds.y(),
                    rightComposeTextFieldBounds.x(),
                    rightComposeTextFieldBounds.y(),
                    rightComposeTextFieldBounds.height(),
                    Math.max(80, rightComposeTitleFieldBounds.width()),
                    rightComposeCoordinateBounds.x(),
                    rightComposeCoordinateBounds.y(),
                    rightComposeFooterBounds.y(),
                    composeCancelX,
                    composeCancelWidth,
                    composeSaveX,
                    composeSaveWidth
            );
        }

        private static int buttonWidth(Font font, Component text, int minWidth, int maxWidth, int padding) {
            return Math.max(minWidth, Math.min(maxWidth, font.width(text) + padding));
        }

        private static int clampValue(int value, int min, int max) {
            return Math.max(min, Math.min(max, value));
        }

        private static int scaled(float scale, int textureValue) {
            return Math.max(1, Math.round(textureValue * scale));
        }

        private static int sy(int bookY, float scale, int textureY) {
            return bookY + Math.round(textureY * scale);
        }

        private static Rect scaledRect(int bookX, int bookY, float scale, int textureX, int textureY, int textureWidth, int textureHeight) {
            return new Rect(
                    bookX + Math.round(textureX * scale),
                    bookY + Math.round(textureY * scale),
                    Math.max(1, Math.round(textureWidth * scale)),
                    Math.max(1, Math.round(textureHeight * scale))
            );
        }
    }
}
