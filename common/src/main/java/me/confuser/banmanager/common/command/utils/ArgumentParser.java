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

import me.confuser.banmanager.common.command.abstraction.CommandException;

/**
 * Utility class to help process arguments, and throw checked exceptions if the arguments are invalid.
 */
public class ArgumentParser {

    public abstract static class ArgumentException extends CommandException {}
    public static class DetailedUsageException extends ArgumentException {}
    public static class InvalidServerWorldException extends ArgumentException {}
    public static class PastDateException extends ArgumentException {}

    public static class InvalidDateException extends ArgumentException {
        private final String invalidDate;

        public InvalidDateException(String invalidDate) {
            this.invalidDate = invalidDate;
        }

        public String getInvalidDate() {
            return this.invalidDate;
        }
    }

    public static class InvalidPriorityException extends ArgumentException {
        private final String invalidPriority;

        public InvalidPriorityException(String invalidPriority) {
            this.invalidPriority = invalidPriority;
        }

        public String getInvalidPriority() {
            return this.invalidPriority;
        }
    }

}
