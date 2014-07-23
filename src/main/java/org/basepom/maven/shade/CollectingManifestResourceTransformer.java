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

    // Configuration
    @SuppressFBWarnings("UWF_UNWRITTEN_FIELD")
    private String mainClass;

    @SuppressFBWarnings("UWF_UNWRITTEN_FIELD")
    private Map<String, String> manifestEntries;

    private boolean collectSections = false;

    // Fields
    private Manifest manifest;

    @Override
    public boolean canTransformResource( String resource )
    {
        if ( JarFile.MANIFEST_NAME.equalsIgnoreCase( resource ) )
        {
            return true;
        }

        return false;
    }

    @Override
    public void processResource( String resource, InputStream is, List<Relocator> relocators )
        throws IOException
    {
        // We just want to take the first manifest we come across as that's our project's manifest. This is the behavior
        // now which is situational at best. Right now there is no context passed in with the processing so we cannot
        // tell what artifact is being processed.
        Manifest loadedManifest = new Manifest( is );

        if ( manifest == null )
        {
            manifest = loadedManifest;
        }

        if ( collectSections && loadedManifest != manifest ) {
            Map<String, Attributes> sections = manifest.getEntries();

            for ( Map.Entry<String, Attributes> manifestSection : loadedManifest.getEntries().entrySet() ) {
                // Find the section that matches this entry or create a new one.
                Attributes sectionAttributes = sections.get( manifestSection.getKey() );
                if ( sectionAttributes == null ) {
                    sectionAttributes = new Attributes();
                    sections.put( manifestSection.getKey(), sectionAttributes );
                }

                // Loop over all attributes in the section.
                for( Map.Entry<Object, Object> attributeEntry : manifestSection.getValue().entrySet() ) {
                    sectionAttributes.put( attributeEntry.getKey(), attributeEntry.getValue() );
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
    public void modifyOutputStream( JarOutputStream jos )
        throws IOException
    {
        // If we didn't find a manifest, then let's create one.
        if ( manifest == null )
        {
            manifest = new Manifest();
        }

        Attributes attributes = manifest.getMainAttributes();

        if ( mainClass != null )
        {
            attributes.put( Attributes.Name.MAIN_CLASS, mainClass );
        }

        if ( manifestEntries != null )
        {
            for ( Map.Entry<String, String> entry : manifestEntries.entrySet() )
            {
                attributes.put( new Attributes.Name( entry.getKey() ), entry.getValue() );
            }
        }

        jos.putNextEntry( new JarEntry( JarFile.MANIFEST_NAME ) );
        manifest.write( jos );
    }
}
