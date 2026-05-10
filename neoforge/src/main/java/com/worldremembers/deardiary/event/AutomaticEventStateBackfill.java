package com.worldremembers.deardiary.event;

import com.worldremembers.deardiary.data.AutomaticEventState;
import com.worldremembers.deardiary.data.DiaryEntry;
import com.worldremembers.deardiary.data.DiaryEntryKind;
import com.worldremembers.deardiary.data.PlayerDiary;

public final class AutomaticEventStateBackfill {
    private AutomaticEventStateBackfill() {
    }

    public static boolean reconcile(PlayerDiary diary) {
        AutomaticEventState state = diary.automaticEventState();
        boolean changed = false;
        for (DiaryEntry entry : diary.entriesView()) {
            if (entry.getEntryKind() != DiaryEntryKind.AUTOMATIC) {
                continue;
            }

            String eventType = entry.getEventType();
            if (OriginEntryFactory.EVENT_TYPE.equals(eventType)) {
                changed |= state.markTriggeredEvent(OriginEntryFactory.EVENT_TYPE);
                continue;
            }

            AutomaticEventDefinition definition = DearDiaryEventRegistry.getDefinition(eventType).orElse(null);
            if (definition != null && definition.triggerPolicy() == TriggerPolicy.ONCE_PER_PLAYER) {
                changed |= state.markTriggeredEvent(definition.eventId());
            }
        }
        return changed;
    }
}
