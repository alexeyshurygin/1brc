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

import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Math.min;

public class CalculateAverage_alexeyshurygin {

    private static final String FILE = "./measurements_short.txt";

    private static void readFile(String filename) throws IOException {
        try (FileChannel channel = FileChannel.open(Path.of(filename))) {
            final long size = channel.size();
            char[] chars = new char[1024 * 1024];
            long used = 0;
            while (used != size) {
                long thisSize = min(size - used, MAX_VALUE);
                CharBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, thisSize).asCharBuffer();
                // buffer.slice()
                // CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
                // CharBuffer charBuffer = decoder.decode(buffer);
                for (int i = 0; i < buffer.remaining(); i++) {
                    buffer.get(chars, 0, min(buffer.remaining(), chars.length));
                }
                used += thisSize;
            }
        }
    }

    private static void readFileSimple(String filename) throws IOException {
        Files.lines(Paths.get(filename))
            .forEach(s -> {
            });
    }

    public static void main(String[] args) throws IOException {
        long s = System.currentTimeMillis();
//        readFileSimple(FILE);
        readFile(FILE);
        long e = System.currentTimeMillis();
        System.out.println("Time: " + (e - s) + " ms");
    }
}
