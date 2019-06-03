/*
 * This file is part of LuckPerms, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package me.confuser.banmanager.common.command.utils;

import java.util.Collection;
import java.util.List;

public final class MessageUtils {
    private MessageUtils() {}

    public static String toCommaSep(Collection<String> strings) {
        if (strings.isEmpty()) {
            return "&bNone";
        }

        StringBuilder sb = new StringBuilder();
        strings.forEach(s -> sb.append("&3").append(s).append("&7, "));
        return sb.delete(sb.length() - 2, sb.length()).toString();
    }

    public static String listToArrowSep(Collection<String> strings, String highlight) {
        if (strings.isEmpty()) {
            return "&bNone";
        }

        StringBuilder sb = new StringBuilder();
        strings.forEach(s -> sb.append(s.equalsIgnoreCase(highlight) ? "&b" : "&3").append(s).append("&7 ---> "));
        return sb.delete(sb.length() - 6, sb.length()).toString();
    }

    public static String listToArrowSep(Collection<String> strings, String highlightFirst, String highlightSecond, boolean reversed) {
        if (strings.isEmpty()) {
            return "&6None";
        }

        StringBuilder sb = new StringBuilder();
        for (String s : strings) {
            if (s.equalsIgnoreCase(highlightFirst)) {
                sb.append("&b").append(s).append("&4");
            } else if (s.equalsIgnoreCase(highlightSecond)) {
                sb.append("&b").append(s).append("&7");
            } else {
                sb.append("&3").append(s).append("&7");
            }

            sb.append(reversed ? " <--- " : " ---> ");
        }
        return sb.delete(sb.length() - 6, sb.length()).toString();
    }

    public static String listToArrowSep(List<String> strings) {
        if (strings.isEmpty()) {
            return "&6None";
        }

        StringBuilder sb = new StringBuilder();
        strings.forEach(s -> sb.append("&3").append(s).append("&b ---> "));
        return sb.delete(sb.length() - 6, sb.length()).toString();
    }

}
