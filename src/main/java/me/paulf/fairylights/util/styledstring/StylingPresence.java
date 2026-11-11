package me.paulf.fairylights.util.styledstring;

public record StylingPresence(boolean hasColor, boolean hasObfuscated, boolean hasBold, boolean hasStrikethrough,
                              boolean hasUnderline, boolean hasItalic) {
    public static final StylingPresence ALL = new StylingPresence(true, true, true, true, true, true);
}
