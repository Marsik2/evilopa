package dev.evelinevisuals;

import java.util.Set;
import java.util.TreeSet;

final class Friends {
    static final Set<String> SET = new TreeSet<>();
    static boolean is(String name) { return SET.contains(name.toLowerCase()); }
}
