/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.basepom.maven.shade;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.maven.plugins.shade.relocation.Relocator;
import org.apache.maven.plugins.shade.relocation.SimpleRelocator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CollectingManifestResourceTransformerTest {

    @Test
    void testPatternRelocation() {
        Relocator relocator = new SimpleRelocator("^com\\.google\\.common", "prefab.shaded.com.google.common", null, null);

        Assertions.assertEquals("prefab.shaded.com.google.common.SomeClass",
                CollectingManifestResourceTransformer.relocate("com.google.common.SomeClass", Collections.singletonList(relocator)));
    }

    @Test
    void testAnchoredRelocation() {
        Relocator relocator = new SimpleRelocator("^com.google.common", "prefab.shaded.com.google.common", null, null);

        Assertions.assertEquals("prefab.shaded.com.google.common.SomeClass",
                CollectingManifestResourceTransformer.relocate("com.google.common.SomeClass", Collections.singletonList(relocator)));
    }

    @Test
    void testFloatingNonMatchingRelocation() {
        Relocator relocator = new SimpleRelocator("com.google.common", "prefab.shaded.cgm", null, null);

        Assertions.assertEquals("prefab.shaded.cgm.SomeClass",
                CollectingManifestResourceTransformer.relocate("com.google.common.SomeClass", Collections.singletonList(relocator)));
    }

    @Test
    void testFloatingMatchingRelocation() {
        Relocator relocator = new SimpleRelocator("com.google.common", "prefab.shaded.com.google.common", null, null);

        IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class,
                () -> CollectingManifestResourceTransformer.relocate("com.google.common.SomeClass", Collections.singletonList(relocator)));

        Assertions.assertEquals(
                "Detected loop when relocating prefab.shaded.com.google.common.SomeClass to prefab.shaded.prefab.shaded.com.google.common.SomeClass, "
                        + "prefix prefab.shaded. already applied!\nRelocation pattern (<pattern>) may be unanchored (not starting with '^')!",
                exception.getMessage());
    }

    @Test
    void testManifestEntryKeepsInputTime() throws IOException {
        long time = 1_767_225_600_000L; // 2026-01-01T00:00:00Z, a multiple of the two-second DOS resolution
        byte[] manifest = "Manifest-Version: 1.0\r\nX-Test: value\r\n\r\n".getBytes(StandardCharsets.UTF_8);

        CollectingManifestResourceTransformer transformer = new CollectingManifestResourceTransformer();
        transformer.processResource(JarFile.MANIFEST_NAME, new ByteArrayInputStream(manifest), Collections.emptyList(), time);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (JarOutputStream jos = new JarOutputStream(bytes)) {
            transformer.modifyOutputStream(jos);
        }

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            ZipEntry entry = zis.getNextEntry();
            Assertions.assertEquals(JarFile.MANIFEST_NAME, entry.getName());
            Assertions.assertEquals(time, entry.getTime());
        }
    }
}
