package com.github.pozzoo.quickwaystones.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;

public class StringUtils {
    public static Component formatString(String string) {
        return MiniMessage.miniMessage().deserialize(string);
    }

    public static List<Component> formatStringList(List<String> strings) {
        List<Component> components = new ArrayList<>();

        for (String string : strings) {
            components.add(formatString(string));
        }

        return components;
    }
}
