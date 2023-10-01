package hoursofza.enums;

import net.dv8tion.jda.api.entities.emoji.Emoji;

public enum ReactionEnum {
    THUMBS_UP("\uD83D\uDC4D"),
    GEAR("⚙"),
    CHECK_MARK("✅"),
    ENVELOPE("✉️")
    ;

    private final String unicode;
    ReactionEnum(String unicode) {
        this.unicode = unicode;
    }

    public Emoji getEmoji() {
        return Emoji.fromUnicode(this.unicode);
    }
}
