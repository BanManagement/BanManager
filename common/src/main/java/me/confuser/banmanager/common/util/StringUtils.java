package me.confuser.banmanager.common.util;

import java.util.ArrayList;
import java.util.List;

public class StringUtils {

  // From apache commons lang3
  /**
   * <p>Joins the elements of the provided array into a single String
   * containing the provided list of elements.</p>
   *
   * <p>No delimiter is added before or after the list.
   * Null objects or empty strings within the array are represented by
   * empty strings.</p>
   *
   * <pre>
   * StringUtils.join(null, *)               = null
   * StringUtils.join([], *)                 = ""
   * StringUtils.join([null], *)             = ""
   * StringUtils.join(["a", "b", "c"], ';')  = "a;b;c"
   * StringUtils.join(["a", "b", "c"], null) = "abc"
   * StringUtils.join([null, "", "a"], ';')  = ";;a"
   * </pre>
   *
   * @param array  the array of values to join together, may be null
   * @param separator  the separator character to use
   * @param startIndex the first index to start joining from.  It is
   * an error to pass in a start index past the end of the array
   * @param endIndex the index to stop joining from (exclusive). It is
   * an error to pass in an end index past the end of the array
   * @return the joined String, {@code null} if null array input
   */
  public static String join(final Object[] array, String separator, final int startIndex, final int endIndex) {
    if (array == null) {
      return null;
    }
    if (separator == null) {
      separator = "";
    }

    // endIndex - startIndex > 0:   Len = NofStrings *(len(firstString) + len(separator))
    //           (Assuming that all Strings are roughly equally long)
    final int noOfItems = endIndex - startIndex;
    if (noOfItems <= 0) {
      return "";
    }

    final StringBuilder buf = new StringBuilder(noOfItems * 16);;

    for (int i = startIndex; i < endIndex; i++) {
      if (i > startIndex) {
        buf.append(separator);
      }
      if (array[i] != null) {
        buf.append(array[i]);
      }
    }

    return buf.toString();
  }

  /**
   * <p>Searches a String for substrings delimited by a start and end tag,
   * returning all matching substrings in an array.</p>
   *
   * <p>A {@code null} input String returns {@code null}.
   * A {@code null} open/close returns {@code null} (no match).
   * An empty ("") open/close returns {@code null} (no match).</p>
   *
   * <pre>
   * StringUtils.substringsBetween("[a][b][c]", "[", "]") = ["a","b","c"]
   * StringUtils.substringsBetween(null, *, *)            = null
   * StringUtils.substringsBetween(*, null, *)            = null
   * StringUtils.substringsBetween(*, *, null)            = null
   * StringUtils.substringsBetween("", "[", "]")          = []
   * </pre>
   *
   * @param str  the String containing the substrings, null returns null, empty returns empty
   * @param open  the String identifying the start of the substring, empty returns null
   * @param close  the String identifying the end of the substring, empty returns null
   * @return a String Array of substrings, or {@code null} if no match
   */
  public static String[] substringsBetween(final String str, final String open, final String close) {
    if (str == null || open == null || open.length() == 0 || close == null || close.length() == 0) {
      return null;
    }

    final int strLen = str.length();

    if (strLen == 0) {
      return new String[0];
    }

    final int closeLen = close.length();
    final int openLen = open.length();
    final List<String> list = new ArrayList<>();
    int pos = 0;

    while (pos < strLen - closeLen) {
      int start = str.indexOf(open, pos);
      if (start < 0) {
        break;
      }
      start += openLen;
      final int end = str.indexOf(close, start);
      if (end < 0) {
        break;
      }
      list.add(str.substring(start, end));
      pos = end + closeLen;
    }

    if (list.isEmpty()) {
      return null;
    }

    return list.toArray(new String[0]);
  }
}
