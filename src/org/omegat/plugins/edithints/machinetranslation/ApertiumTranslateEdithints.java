/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.omegat.plugins.edithints.machinetranslation;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import org.omegat.core.machinetranslators.ApertiumTranslate;
import org.omegat.gui.exttrans.IMachineTranslationEdithints;
import org.omegat.util.Language;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;
import org.omegat.util.WikiGet;

/**
 *
 * @author miquel
 */
public class ApertiumTranslateEdithints extends ApertiumTranslate implements IMachineTranslationEdithints{
    
    @Override
    protected String getPreferenceName() {
        return Preferences.ALLOW_APERTIUM_TRANSLATE;
    }
    
    public String getHTMLTranslation(Language sLang, Language tLang, String text) throws Exception {

        Method apertiumCode = ApertiumTranslate.class.getDeclaredMethod("apertiumCode", Language.class);
        apertiumCode.setAccessible(true);
        
        String trText = text;

        String sourceLang = (String) apertiumCode.invoke(this,sLang);
        String targetLang = (String) apertiumCode.invoke(this,tLang);

        String url2 = GT_URL2.replace("#sourceLang#", sourceLang).replace("#targetLang#", targetLang);
        String url = GT_URL + URLEncoder.encode(trText, "UTF-8") + url2 + "&format=html&markUnknown=no";

        String v;
        try {
            v = WikiGet.getURL(url);
        } catch (IOException e) {
            return e.getLocalizedMessage();
        }
        while (true) {
            Matcher m = RE_UNICODE.matcher(v);
            if (!m.find()) {
                break;
            }
            String g = m.group();
            char c = (char) Integer.parseInt(m.group(1), 16);
            v = v.replace(g, Character.toString(c));
        }
        v = v.replace("&quot;", "&#34;");
        v = v.replace("&nbsp;", "&#160;");
        v = v.replace("&amp;", "&#38;");
        v = v.replace("\\/", "/");
        v = v.replace("\\\"", "\"");
        
        while (true) {
            Matcher m = RE_HTML.matcher(v);
            if (!m.find()) {
                break;
            }
            String g = m.group();
            char c = (char) Integer.parseInt(m.group(1));
            v = v.replace(g, Character.toString(c));
        }

        int beg = v.indexOf(MARK_BEG) + MARK_BEG.length();
        int end = v.indexOf(MARK_END, beg);
        if (end < 0) {
            //no translation found. e.g. {"responseData":{"translatedText":null},"responseDetails":"Not supported pair","responseStatus":451}
            Matcher m = RE_DETAILS.matcher(v);
            if (!m.find()) {
                return "";
            }
            String details = m.group(1);
            String code = "";
            m = RE_STATUS.matcher(v);
            if (m.find()) {
                code = m.group(1);
            }
            return StaticUtils.format(OStrings.getString("APERTIUM_ERROR"), code, details);
        }
        String tr = v.substring(beg, end - 2); // Remove \n
        return tr;
    }
}
