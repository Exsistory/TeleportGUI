package exsistory.project.teleportgui;

import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorizeHandler {
    private static final Pattern HEX_PATTERN = Pattern.compile("&(#\\w{6})");

    public static List<String> colorize(List<String> strings) {
        List<String> finalList = new ArrayList<>();
        for (String s : strings) finalList.add(colorize(s));
        return finalList;
    }

    public static String colorize(String message) {
        Matcher matcher = HEX_PATTERN.matcher(ChatColor.translateAlternateColorCodes('&', message));
        StringBuilder buffer = new StringBuilder();

        while (matcher.find()) {
            matcher.appendReplacement(buffer, ChatColor.of(matcher.group(1)).toString());
        }

        return matcher.appendTail(buffer).toString();
    }
}
