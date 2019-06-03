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

package me.confuser.banmanager.common.util;

import com.google.common.collect.Range;

import java.util.function.Predicate;

/**
 * A collection of predicate utilities used mostly in command classes
 */
public final class Predicates {
    private Predicates() {}

    private static final Predicate FALSE = new Predicate() {
        @Override public boolean test(Object o) { return false; }
        @Override public Predicate and(Predicate other) { return this; }
        @Override public Predicate or(Predicate other) { return other; }
        @Override public Predicate negate() { return TRUE; }
    };
    private static final Predicate TRUE = new Predicate() {
        @Override public boolean test(Object o) { return true; }
        @Override public Predicate and(Predicate other) { return other; }
        @Override public Predicate or(Predicate other) { return this; }
        @Override public Predicate negate() { return FALSE; }
    };

    public static <T> Predicate<T> alwaysFalse() {
        //noinspection unchecked
        return FALSE;
    }

    public static <T> Predicate<T> alwaysTrue() {
        //noinspection unchecked
        return TRUE;
    }

    public static Predicate<Integer> notInRange(int start, int end) {
        Range<Integer> range = Range.closed(start, end);
        return value -> !range.contains(value);
    }

    public static Predicate<Integer> inRange(int start, int end) {
        Range<Integer> range = Range.closed(start, end);
        return range::contains;
    }

    public static <T> Predicate<T> not(T t) {
        return obj -> !t.equals(obj);
    }

    public static <T> Predicate<T> is(T t) {
        return t::equals;
    }

}