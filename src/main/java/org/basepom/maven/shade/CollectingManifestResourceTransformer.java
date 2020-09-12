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

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.apache.maven.plugins.shade.relocation.Relocator;
import org.apache.maven.plugins.shade.resource.ManifestResourceTransformer;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Extends {@link ManifestResourceTransformer} to collect the additional sections
 * in the jar manifests. This keeps the build information from the internal jars in the
 * shaded jars. Submitted back to Maven as http://jira.codehaus.org/browse/MSHADE-165.
 */
public final class CollectingManifestResourceTransformer
        extends ManifestResourceTransformer
{
    private final List<String> defaultAttributes = Arrays.asList("Export-Package",
            "Import-Package",
            "Provide-Capability",
            "Require-Capability");

    private String mainClass;

    private Map<String, Object> manifestEntries = Collections.emptyMap();

    private boolean collectSections = false;

    private List<String> additionalAttributes = Collections.emptyList();

    // The root manifest
    private Manifest rootManifest = null;

    private long time = Long.MIN_VALUE;

    /**
     * If set, the transformer will collect all sections from jar manifests
     * and adds them to the main manifest.
     */
    public void setCollectSections(boolean collectSections) {
        this.collectSections = collectSections;
    }

    @Override
    public void setMainClass( String mainClass )
    {
        this.mainClass = mainClass;
    }

    @Override
    public void setManifestEntries(Map<String, Object> manifestEntries)
    {
        if (manifestEntries != null) {
            this.manifestEntries = manifestEntries;
        } else {
            this.manifestEntries = Collections.emptyMap();
        }
    }

    @Override
    public void setAdditionalAttributes(List<String> additionalAttributes)
    {
        if (additionalAttributes != null) {
            this.additionalAttributes = additionalAttributes;
        } else {
            this.additionalAttributes = Collections.emptyList();
        }
    }

    @Override
    public boolean canTransformResource(String resource)
    {
        if (JarFile.MANIFEST_NAME.equalsIgnoreCase(resource)) {
            return true;
        }

        return false;
    }

    @Override
    public void processResource(String resource, InputStream is, List<Relocator> relocators, long time)
            throws IOException
    {
        Manifest loadedManifest = new Manifest(is);

        // Relocate the just loaded manifest
        if (relocators != null && !relocators.isEmpty()) {
            final Attributes attributes = loadedManifest.getMainAttributes();

            for (final String attribute : defaultAttributes) {
                final String attributeValue = attributes.getValue(attribute);
                if (attributeValue != null) {
                    String newValue = relocate(attributeValue, relocators);
                    attributes.putValue(attribute, newValue);
                }
            }

            for (final String attribute : additionalAttributes) {
                final String attributeValue = attributes.getValue(attribute);
                if (attributeValue != null) {
                    String newValue = relocate(attributeValue, relocators);
                    attributes.putValue(attribute, newValue);
                }
            }
        }

        // We just want to take the first manifest we come across as that's our project's manifest. This is the behavior
        // now which is situational at best. Right now there is no context passed in with the processing so we cannot
        // tell what artifact is being processed.
        if (rootManifest == null) {
            rootManifest = loadedManifest;

            if (time > this.time) {
                this.time = time;
            }
        }

        // collect additional sections into the root manifest
        if (collectSections && loadedManifest != rootManifest) {
            Map<String, Attributes> existingRootSections = rootManifest.getEntries();

            // loop through the sections of the just loaded manifest
            for (Map.Entry<String, Attributes> manifestSection : loadedManifest.getEntries().entrySet()) {
                // Find the section that matches this entry or create a new one.
                Attributes existingRootSectionAttributes = existingRootSections.get(manifestSection.getKey());

                // create a new section if it does not already exist
                if (existingRootSectionAttributes == null) {
                    existingRootSectionAttributes = new Attributes();
                    existingRootSections.put(manifestSection.getKey(), existingRootSectionAttributes);
                }

                // Add all attributes from that section to the manifest
                for (Map.Entry<Object, Object> attributeEntry : manifestSection.getValue().entrySet()) {
                    existingRootSectionAttributes.put(attributeEntry.getKey(), attributeEntry.getValue());
                }
            }
        }
    }

    @Override
    public boolean hasTransformedResource()
    {
        return true;
    }

    @Override
    public void modifyOutputStream(JarOutputStream jos)
            throws IOException
    {
        // If we didn't find a manifest, then let's create one.
        if (rootManifest == null) {
            rootManifest = new Manifest();
        }

        Attributes attributes = rootManifest.getMainAttributes();

        if (mainClass != null && !mainClass.isEmpty()) {
            attributes.put(Attributes.Name.MAIN_CLASS, mainClass);
        }

        if (manifestEntries != null) {
            for (Map.Entry<String, Object> entry : manifestEntries.entrySet()) {
                attributes.put(new Attributes.Name(entry.getKey()), entry.getValue());
            }
        }

        jos.putNextEntry(new JarEntry(JarFile.MANIFEST_NAME));
        rootManifest.write(jos);
    }

    private String relocate(String originalValue, List<Relocator> relocators)
    {
        String newValue = originalValue;
        for (Relocator relocator : relocators) {
            String value;
            do {
                value = newValue;
                newValue = relocator.relocateClass(value);
            }
            while (!value.equals(newValue));
        }
        return newValue;
    }
}
