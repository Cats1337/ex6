package io.github.cats1337.battlekingdom.utils;

import java.util.List;

public interface ITabCompleterHelper {
    static List<String> tabComplete(String arg, List<String> subCommands) {
        return subCommands.stream().filter(x -> x.toLowerCase().startsWith(arg.toLowerCase())).toList();
    }
}