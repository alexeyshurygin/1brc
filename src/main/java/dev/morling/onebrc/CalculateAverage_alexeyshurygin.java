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

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Math.min;
import static java.nio.file.Files.lines;

public class CalculateAverage_alexeyshurygin {

    private static final String FILE = "./measurements_short.txt";

    private static void readCharFile(String filename) throws IOException {
        try (var channel = FileChannel.open(Path.of(filename))) {
            final var size = channel.size();
            var chars = new char[1024 * 1024];
            long used = 0;
            while (used != size) {
                var thisSize = min(size - used, MAX_VALUE);
                var buf = channel.map(FileChannel.MapMode.READ_ONLY, used, thisSize).asCharBuffer();
                for (int i = 0; i < buf.remaining(); ) {
                    if (buf.remaining() >= chars.length) {
                        buf.get(chars, 0, chars.length);
                        i += chars.length;
                    } else {
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
                for (int i = 0; i < buf.remaining(); ) {
                    if (buf.remaining() >= bytes.length) {
                        buf.get(bytes, 0, bytes.length);
                        i += bytes.length;
                    } else {
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

    private static void readIOFile(String filename) throws IOException {
        Map<byte[], Double> mean = new HashMap<>();
        Map<byte[], Integer> min = new HashMap<>();
        Map<byte[], Integer> max = new HashMap<>();
        Map<byte[], Integer> count = new HashMap<>();
        try (var is = new FileInputStream(filename)) {
            int neg = 1;
            int l = 0;
            int temp = 0;
            int i = 0;
            int bi = 0;
            byte[] buf = new byte[1024 * 1024];
            int read;
            byte[] nameBuf = new byte[100];
            boolean pastSemi = false;
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
                            //todo do stuff here
                            temp *= neg;
                            final byte[] key = Arrays.copyOf(nameBuf, bi);
                            mean.merge(key, (double) temp, Double::sum);
                            min.merge(key, temp, Integer::min);
                            max.merge(key, temp, Integer::max);
                            count.merge(key, 1, Integer::sum);
                            bi = 0;
                            pastSemi = false;
                            l++;
                            neg = 1;
                        }
//                    case '\r' -> l++;
//                    case '.' -> {
//                    }
                        default -> {
                            if (pastSemi) {
                                temp = temp * 10 + b - '0';
                            } else {
                                nameBuf[bi++] = (byte) b;
                            }
                        }
                    }
                    i++;
                }
            }
        }
        printResults(min, max, mean, count);
    }

    private static void printResults(Map<byte[], Integer> min, Map<byte[], Integer> max, Map<byte[], Double> mean, Map<byte[], Integer> count) {
        System.out.print("{");
        min.keySet().stream().sorted().forEach(
            k -> {
                System.out.print(k);
                System.out.print("=");
                System.out.print((((double) min.get(k))) / 10);
                System.out.print("/");
                System.out.print(((mean.get(k) / count.get(k))) / 10);
                System.out.print("/");
                System.out.print((((double) max.get(k))) / 10);
            }
        );
        System.out.print("}");
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
