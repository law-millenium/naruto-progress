package com.lawmillenium.intellij.plugins.narutoprogress.model;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.ImmutableMap;
import com.intellij.openapi.util.text.StringUtil;

public enum Shinobi {
    NARUTO("naruto", -16, 0, 32, ShinobiGroup.KONOHA), //
    NARUTO_KYUBI("narutoKyubi", -16, -2, 32, ShinobiGroup.KONOHA), //
    NARUTO_SEXY_JUTSU("narutoSexyJutsu", -16, -0, 32, ShinobiGroup.KONOHA), //
    SASUKE("sasuke", -16, -0, 32, ShinobiGroup.KONOHA), //
    SASUKE_CURSED_SEAL("sasukeCursedSeal", -16, -0, 32, ShinobiGroup.KONOHA), //
    SAKURA("sakura", -16, 0, 32, ShinobiGroup.KONOHA), //
    DRUNK_ROCK_LEE("drunkRockLee", -16, -2, 32, ShinobiGroup.KONOHA), //
    TENTEN("tenten", -16, -2, 32, ShinobiGroup.KONOHA), //
    SHIKAMARU("shikamaru", -16, 0, 32, ShinobiGroup.KONOHA), //
    CHOJI("choji", -16, 0, 32, ShinobiGroup.KONOHA), //
    SHISUI("shisui", -16, 0, 32, ShinobiGroup.KONOHA), //
    KAKASHI("kakashi", -16, 0, 32, ShinobiGroup.KONOHA), //
    KAKASHI_ANBU("kakashiAnbu", -16, 0, 32, ShinobiGroup.KONOHA), //
    KURENAI("kurenai", -16, -3, 32, ShinobiGroup.KONOHA), //
    TOBIRAMA("tobirama", -16, 0, 32, ShinobiGroup.KONOHA), //
    MADARA("madara", -16, 0, 32, ShinobiGroup.KONOHA), //
    YUGITO("yugito", -16, 0, 32, ShinobiGroup.KUMO), //
    TOBI("tobi", -16, 0, 32, ShinobiGroup.AKATSUKI), //
    ITACHI("itachi", -16, -1, 32, ShinobiGroup.AKATSUKI), //
    MISSINGNO("missingNo.", -20, 0, 35, true, null);

    public static final Map<String, Shinobi> DEFAULT_SHINOBIS = Arrays.stream(values()).filter(shinobis -> !shinobis.secret).collect(
        ImmutableMap.toImmutableMap(Shinobi::getName, Function.identity(), (u, v) -> {
            throw new IllegalStateException(String.format("Duplicate shinobi name %s", u));
        }));

    private final String name;
    private final int xShift;
    private final int yShift;
    private final int height;
    private final boolean secret;
    private final ShinobiGroup shinobiGroup;

    Shinobi(final String name, final int xShift, final int yShift, final int height, final ShinobiGroup shinobiGroup) {
        this(name, xShift, yShift, height, false, shinobiGroup);
    }

    Shinobi(final String name, final int xShift, final int yShift, final int height, final boolean secret, final ShinobiGroup shinobiGroup) {
        this.xShift = xShift;
        this.yShift = yShift;
        this.height = height;
        this.name = name;
        this.secret = secret;
        this.shinobiGroup = shinobiGroup;
    }

    public static Shinobi getByName(final String name) {
        return DEFAULT_SHINOBIS.get(name);
    }

    public int getXShift() {
        return xShift;
    }

    public int getYShift() {
        return yShift;
    }

    public int getHeight() {
        return height;
    }

    public ShinobiGroup getShinobiGroup() {
        return shinobiGroup;
    }

    public String getName() {
        return name;
    }

    public String getNameWithNumber() {
        return StringUtil.capitalizeWords(name, true);
    }

    @Override
    public String toString() {
        return getNameWithNumber();
    }
}
