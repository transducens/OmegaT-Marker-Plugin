package org.omegat.gui.exttrans;

import org.omegat.util.Language;

/**
 * Class that extends <code>IMachineTranslation</code>. Class that extends the 
 * <code>IMachineTranslation</code> class by adding the abstract method for
 * translating HTML text instead of plain text
 * @author Miquel Espl� Gomis [mespla@dlsi.ua.es]
 */
public interface IMachineTranslationEdithints extends IMachineTranslation {
    String getHTMLTranslation(Language sLang, Language tLang, String text) throws Exception;
}
