/*
 *  Copyright 2023 The original authors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package dev.morling.onebrc;

import jdk.internal.util.ArraysSupport;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Math.min;
import static java.nio.file.Files.lines;

public class CalculateAverage_alexeyshurygin {

    private static final String FILE = "./measurements.txt";

    private static void readCharFile(String filename) throws IOException {
        try (var channel = FileChannel.open(Path.of(filename))) {
            final var size = channel.size();
            var chars = new char[1024 * 1024];
            long used = 0;
            while (used != size) {
                var thisSize = min(size - used, MAX_VALUE);
                var buf = channel.map(FileChannel.MapMode.READ_ONLY, used, thisSize).asCharBuffer();
                for (int i = 0; i < buf.remaining();) {
                    if (buf.remaining() >= chars.length) {
                        buf.get(chars, 0, chars.length);
                        i += chars.length;
                    }
                    else {
                        var chars1 = new char[buf.remaining()];
                        buf.get(chars1, 0, chars1.length);
                        i += chars1.length;
                    }
                }
                used += thisSize;
            }
        }
    }

    private static void readBBFile(String filename) throws IOException {
        try (var channel = FileChannel.open(Path.of(filename))) {
            final var size = channel.size();
            var bytes = new byte[1024 * 1024];
            long used = 0;
            while (used != size) {
                var thisSize = min(size - used, MAX_VALUE);
                var buf = channel.map(FileChannel.MapMode.READ_ONLY, used, thisSize);
                for (int i = 0; i < buf.remaining();) {
                    if (buf.remaining() >= bytes.length) {
                        buf.get(bytes, 0, bytes.length);
                        i += bytes.length;
                    }
                    else {
                        var bytes1 = new byte[buf.remaining()];
                        buf.get(bytes1, 0, bytes1.length);
                        i += bytes1.length;
                    }
                }
                used += thisSize;
            }
        }
    }

    private static void readFRFile(String filename) throws IOException {
        try (var io = new FileReader(filename, StandardCharsets.UTF_8)) {
            var chars = new char[1024 * 1024];
            int total = 0;
            for (int i = 0; i >= 0; i = io.read(chars)) {
                total += i;
            }
        }
    }

    private static void readFileSimple(String filename) throws IOException {
        lines(Paths.get(filename))
                .forEach(s -> {
                });
    }

    static class ArrayWrapper {

        final byte[] a;
        int length;

        public ArrayWrapper(byte[] a) {
            this.a = a;
        }

        void setLength(int length) {
            this.length = length;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            final ArrayWrapper that = (ArrayWrapper) o;
            return Arrays.equals(a, 0, length, that.a, 0, that.length);
        }

        @Override
        public int hashCode() {
            return switch (length) {
                case 0 -> 1;
                case 1 -> 31 + (int) a[0];
                default -> ArraysSupport.vectorizedHashCode(a, 0, length, 1, ArraysSupport.T_BYTE);
            };
        }
    }

    private static void readIOFile(String filename) throws IOException {
        Map<ArrayWrapper, Double> mean = new HashMap<>();
        Map<ArrayWrapper, Integer> min = new HashMap<>();
        Map<ArrayWrapper, Integer> max = new HashMap<>();
        Map<ArrayWrapper, Integer> count = new HashMap<>();
        try (var is = new FileInputStream(filename)) {
            int neg = 1;
            int l = 0;
            int temp = 0;
            int i = 0;
            int nameLength = 0;
            byte[] buf = new byte[4 * 1024];
            int read;
            byte[] nameBuf = new byte[100];
            boolean pastSemi = false;
            var key = new ArrayWrapper(nameBuf);
            while ((read = is.read(buf)) != -1) {
                for (int pos = 0; pos < read; pos++) {
                    int b = buf[pos];
                    switch (b) {
                        case ';' -> {
                            temp = 0;
                            pastSemi = true;
                        }
                        case '-' -> neg = -1;
                        case '\n' -> {
                            if (nameLength > 0) {
                                temp *= neg;
                                key.setLength(nameLength);
                                final var c = count.merge(key, 1, Integer::sum);
                                mean.merge(key, (double) temp, Double::sum);
                                min.merge(key, temp, Integer::min);
                                max.merge(key, temp, Integer::max);
                                if (c == 1) {
                                    nameBuf = new byte[100];
                                    key = new ArrayWrapper(nameBuf);
                                }
                                nameLength = 0;
                            }
                            pastSemi = false;
                            l++;
                            neg = 1;
                        }
                        // case '\r' -> l++;
                        // case '.' -> {
                        // }
                        default -> {
                            if (!pastSemi) {
                                nameBuf[nameLength++] = (byte) b;
                            }
                            else {
                                temp = temp * 10 + b - '0';
                            }
                        }
                    }
                    i++;
                }
                // System.out.println("Lines:" + l + ", keys:" + count.size());
            }
        }
        printResults(min, max, mean, count);
    }

    private static void printResults(Map<ArrayWrapper, Integer> min, Map<ArrayWrapper, Integer> max, Map<ArrayWrapper, Double> mean, Map<ArrayWrapper, Integer> count) {
        final SortedMap<String, ArrayWrapper> sorted = min.keySet().stream()
                .collect(Collectors.toMap(k -> new String(k.a, 0, k.length, Charset.forName("UTF-8")), k -> k, (a, b) -> b, TreeMap::new));
        System.out.print("{");
        final String last = sorted.lastKey();
        sorted.forEach((s, k) -> {
            System.out.print(s);
            System.out.print("=");
            System.out.print((double) min.get(k) / 10);
            System.out.print("/");
            System.out.print((double) Math.round(mean.get(k) / count.get(k)) / 10);
            System.out.print("/");
            System.out.print((double) max.get(k) / 10);
            System.out.print("/");
            if (last != s)
                System.out.print(", ");
        });
        System.out.println("}");
    }

    public static void main(String[] args) throws IOException {
        long s = System.currentTimeMillis();
        // readFileSimple(FILE);
        // readCharFile(FILE);
        // readBBFile(FILE);
        readIOFile(FILE);
        // readFRFile(FILE);
        long e = System.currentTimeMillis();
        System.out.println("Time: " + (e - s) + " ms");
    }
}
