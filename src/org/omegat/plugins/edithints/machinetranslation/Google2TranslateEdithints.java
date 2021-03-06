/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik, Didier Briel
               2011 Briac Pilpre, Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 **************************************************************************/

package org.omegat.plugins.edithints.machinetranslation;

import org.omegat.core.machinetranslators.*;
import org.omegat.gui.exttrans.IMachineTranslationEdithints;
import org.omegat.util.Language;
import org.omegat.util.Preferences;

/**
 * Support of Google Translate API v.2 machine translation.
 * https://code.google.com/apis/language/translate/v2/getting_started.html
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 * @author Briac Pilpre
 */
public class Google2TranslateEdithints extends Google2Translate implements IMachineTranslationEdithints {
    
    @Override
    protected String getPreferenceName() {
        return Preferences.ALLOW_GOOGLE2_TRANSLATE;
    }

    public String getHTMLTranslation(Language sLang, Language tLang, String text) throws Exception {
        return super.translate(sLang, tLang, text);
    }
}
