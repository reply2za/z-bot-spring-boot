package hoursofza.enums;

import net.dv8tion.jda.api.entities.emoji.Emoji;

public enum Reactions {
    THUMBS_UP("\uD83D\uDC4D"),
    GEAR("⚙");

    private final String unicode;
    Reactions(String unicode) {
        this.unicode = unicode;
    }

    public Emoji getEmoji() {
        return Emoji.fromUnicode(this.unicode);
    }
}
