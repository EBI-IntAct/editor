/**
 * Copyright 2011 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.editor.config.property;

import org.springframework.stereotype.Component;

/**
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
@Component
public class CharPropertyConverter implements PropertyConverter<Character> {

    public CharPropertyConverter() {
    }

    @Override
    public Character convertFromString(String str) {
        if (str == null) return null;

        return str.charAt( 0 );
    }

    @Override
    public String convertToString(Character obj) {
        if (obj == null) return null;

        return obj.toString();
    }

    @Override
    public Class<Character> getObjectType() {
        return Character.class;
    }
}
