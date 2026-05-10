package com.worldremembers.deardiary.event;

import com.worldremembers.deardiary.DearDiaryMod;
import com.worldremembers.deardiary.api.AutomaticEntryRequest;
import com.worldremembers.deardiary.data.DiaryCategory;
import com.worldremembers.deardiary.data.DiaryImportance;
import java.util.List;
import java.util.Random;

public final class OriginEntryFactory {
    public static final String EVENT_TYPE = "dear_diary:origin";

    private static final List<DiaryTextVariant> VARIANTS = List.of(
            new DiaryTextVariant(
                    "storm",
                    "entry.dear_diary.origin.storm.title",
                    "entry.dear_diary.origin.storm.text",
                    "The first surviving page",
                    "I remember wind, water, and the stubborn weight of this diary under my arm. Whatever brought me here, the page survived with me."
            ),
            new DiaryTextVariant(
                    "plan",
                    "entry.dear_diary.origin.plan.title",
                    "entry.dear_diary.origin.plan.text",
                    "A modest plan",
                    "I am alive. This is not a complete plan, but it is a better beginning than most plans I have written."
            ),
            new DiaryTextVariant(
                    "tree",
                    "entry.dear_diary.origin.tree.title",
                    "entry.dear_diary.origin.tree.text",
                    "Begin with wood",
                    "If I had a grand purpose, I have misplaced it. I will begin with a tree; many practical stories do."
            ),
            new DiaryTextVariant(
                    "map",
                    "entry.dear_diary.origin.map.title",
                    "entry.dear_diary.origin.map.text",
                    "An unhelpful map",
                    "The map in my pocket is either outdated or optimistic. The diary is more honest: it starts exactly where I am standing."
            ),
            new DiaryTextVariant(
                    "quiet",
                    "entry.dear_diary.origin.quiet.title",
                    "entry.dear_diary.origin.quiet.text",
                    "Where the quiet starts",
                    "This place is too quiet for somewhere new. Either the world is holding its breath, or I arrived before the story was ready."
            ),
            new DiaryTextVariant(
                    "dawn",
                    "entry.dear_diary.origin.dawn.title",
                    "entry.dear_diary.origin.dawn.text",
                    "A page before the road",
                    "I do not know what this place wants from me yet. For now, I have a page, two hands, and enough daylight to begin."
            ),
            new DiaryTextVariant(
                    "small",
                    "entry.dear_diary.origin.small.title",
                    "entry.dear_diary.origin.small.text",
                    "Small beginnings",
                    "No grand voice announced my arrival. The world simply opened in front of me, and I suppose I should do the same."
            ),
            new DiaryTextVariant(
                    "mark",
                    "entry.dear_diary.origin.mark.title",
                    "entry.dear_diary.origin.mark.text",
                    "The first mark",
                    "I marked this page so tomorrow can find me. That feels like a beginning, even if the path refuses to introduce itself."
            ),
            new DiaryTextVariant(
                    "blank",
                    "entry.dear_diary.origin.blank.title",
                    "entry.dear_diary.origin.blank.text",
                    "A blank page",
                    "The first page is still clean, which feels almost rude in a world this untidy. I suppose I should fix that."
            ),
            new DiaryTextVariant(
                    "unknown",
                    "entry.dear_diary.origin.unknown.title",
                    "entry.dear_diary.origin.unknown.text",
                    "The unknown road",
                    "I do not know where this road begins or where it expects me to go. For now, I will write down the part beneath my feet."
            ),
            new DiaryTextVariant(
                    "alive",
                    "entry.dear_diary.origin.alive.title",
                    "entry.dear_diary.origin.alive.text",
                    "Still alive",
                    "Good news: I am alive. Bad news: that appears to be the entire plan so far."
            ),
            new DiaryTextVariant(
                    "north",
                    "entry.dear_diary.origin.north.title",
                    "entry.dear_diary.origin.north.text",
                    "No north yet",
                    "The sky offers directions with great confidence and very little explanation. I will choose one soon enough."
            ),
            new DiaryTextVariant(
                    "pocket",
                    "entry.dear_diary.origin.pocket.title",
                    "entry.dear_diary.origin.pocket.text",
                    "In my pocket",
                    "I found this diary with me, or it found me first. Either way, it seems polite to give it something worth keeping."
            ),
            new DiaryTextVariant(
                    "threshold",
                    "entry.dear_diary.origin.threshold.title",
                    "entry.dear_diary.origin.threshold.text",
                    "At the threshold",
                    "There is a whole world on the other side of this first breath. I am trying not to look too impressed."
            ),
            new DiaryTextVariant(
                    "weather",
                    "entry.dear_diary.origin.weather.title",
                    "entry.dear_diary.origin.weather.text",
                    "Weather enough",
                    "The weather has not introduced itself kindly, but it has introduced itself. That may have to count as welcome."
            ),
            new DiaryTextVariant(
                    "questions",
                    "entry.dear_diary.origin.questions.title",
                    "entry.dear_diary.origin.questions.text",
                    "Too many questions",
                    "I arrived with more questions than supplies. Since questions are hard to eat, I should begin with the supplies."
            ),
            new DiaryTextVariant(
                    "camp",
                    "entry.dear_diary.origin.camp.title",
                    "entry.dear_diary.origin.camp.text",
                    "Before the first camp",
                    "No campfire yet, no roof, no sensible plan. Only this page, which is at least honest about being empty."
            ),
            new DiaryTextVariant(
                    "stone",
                    "entry.dear_diary.origin.stone.title",
                    "entry.dear_diary.origin.stone.text",
                    "Stone underfoot",
                    "The ground feels real enough. That is comforting, though it refuses to explain anything else."
            ),
            new DiaryTextVariant(
                    "footprints",
                    "entry.dear_diary.origin.footprints.title",
                    "entry.dear_diary.origin.footprints.text",
                    "First footprints",
                    "My footprints are the only proof I have that I belong here even a little. I will try to leave better proof tomorrow."
            ),
            new DiaryTextVariant(
                    "again",
                    "entry.dear_diary.origin.again.title",
                    "entry.dear_diary.origin.again.text",
                    "Begin again",
                    "Something ended before this page. I do not know what it was, so I will begin from here and call that mercy."
            ),
            new DiaryTextVariant(
                    "compass",
                    "entry.dear_diary.origin.compass.title",
                    "entry.dear_diary.origin.compass.text",
                    "No compass needed yet",
                    "A compass would be useful. Lacking one, I will follow curiosity and blame it later if necessary."
            ),
            new DiaryTextVariant(
                    "shelter",
                    "entry.dear_diary.origin.shelter.title",
                    "entry.dear_diary.origin.shelter.text",
                    "Before shelter",
                    "This world is large, and I am currently very easy to misplace. Shelter seems like a sensible first argument."
            ),
            new DiaryTextVariant(
                    "morning",
                    "entry.dear_diary.origin.morning.title",
                    "entry.dear_diary.origin.morning.text",
                    "Morning inventory",
                    "I counted what I have: a body, a page, and a suspicious amount of empty space around me. It will have to do."
            ),
            new DiaryTextVariant(
                    "edge",
                    "entry.dear_diary.origin.edge.title",
                    "entry.dear_diary.origin.edge.text",
                    "At the edge",
                    "The world begins at my boots and runs farther than I can guess. I should probably move before it changes its mind."
            ),
            new DiaryTextVariant(
                    "thread",
                    "entry.dear_diary.origin.thread.title",
                    "entry.dear_diary.origin.thread.text",
                    "A loose thread",
                    "This page feels like a loose thread. If I pull carefully, perhaps a story will come with it."
            ),
            new DiaryTextVariant(
                    "stillness",
                    "entry.dear_diary.origin.stillness.title",
                    "entry.dear_diary.origin.stillness.text",
                    "A very quiet start",
                    "The quiet here is not empty. It is waiting to see what sort of noise I make."
            ),
            new DiaryTextVariant(
                    "first_step",
                    "entry.dear_diary.origin.first_step.title",
                    "entry.dear_diary.origin.first_step.text",
                    "The first step",
                    "The first step did not answer any of my questions. It did, however, prove that I can take another."
            ),
            new DiaryTextVariant(
                    "no_return",
                    "entry.dear_diary.origin.no_return.title",
                    "entry.dear_diary.origin.no_return.text",
                    "No clear way back",
                    "If there is a way back, it is hiding well. I will look forward instead; at least forward is visible."
            ),
            new DiaryTextVariant(
                    "stranger",
                    "entry.dear_diary.origin.stranger.title",
                    "entry.dear_diary.origin.stranger.text",
                    "A stranger here",
                    "I am a stranger here, but the world has not objected yet. I will take that as permission to start."
            ),
            new DiaryTextVariant(
                    "open_sky",
                    "entry.dear_diary.origin.open_sky.title",
                    "entry.dear_diary.origin.open_sky.text",
                    "Under open sky",
                    "The sky is too wide for someone with so few answers. Still, it is bright enough to write by."
            )
    );

    private OriginEntryFactory() {
    }

    public static AutomaticEntryRequest create(Random random) {
        DiaryTextVariant variant = VARIANTS.get(random.nextInt(VARIANTS.size()));
        return AutomaticEntryRequest.builder(EVENT_TYPE, DearDiaryMod.MOD_ID)
                .category(DiaryCategory.BEGINNING)
                .importance(DiaryImportance.NORMAL)
                .titleKey(variant.titleKey())
                .textKey(variant.textKey())
                .resolvedTitle(variant.fallbackTitle())
                .resolvedText(variant.fallbackText())
                .icon("minecraft:writable_book")
                .includeLocation(true)
                .shareable(true)
                .build();
    }

    public static List<DiaryTextVariant> variants() {
        return VARIANTS;
    }
}
