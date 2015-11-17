/******************************************************************************
 EditHints OmegaT plugin - Plugin for OmegaT (htpp://www.omegat.org) to provide
                           edit hints on the translation proposals by a
                           translation memory by using machine translation to
                           detect the parts of the proposal to be edited and
                           those to keep untouched. The method used here is
                           described by Esplà-Gomis, Sánchez-Martínez, and
                           Forcada in "Using machine translation in
                           computer-aided translation to suggest the target-side
                           words to change" (XIII Machine Translation Summit, p
                           172-179, Xiamen, Xina, 2011).

 Copyright (C) 2013-2014 Universitat d'Alacant [www.ua.es]

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat.plugins.edithints;

import es.ua.dlsi.recommendation.GeometricRecommender;
import es.ua.dlsi.segmentation.Segment;
import es.ua.dlsi.segmentation.SubSegment;
import es.ua.dlsi.segmentation.Word;
import es.ua.dlsi.translationmemory.SegmentDictionary;
import es.ua.dlsi.translationmemory.TranslationUnit;
import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.events.IApplicationEventListener;
import org.omegat.core.matching.NearString;
import org.omegat.filters2.html2.FilterVisitor;
import org.omegat.gui.exttrans.IMachineTranslationEdithints;
import org.omegat.gui.matches.MatchesTextArea;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.Token;
import org.omegat.util.gui.Styles;

/**
 * Class that manages the coloring on the matcher. This class contains a set of
 * control variables and methods for performing the coloring on the matcher text
 * area. This class interacts with the <code>EditHintsMarker</code> to use the
 * coloring information found in the translation memory on the inserted
 * translation units.
 * @author Miquel Esplà Gomis [mespla@dlsi.ua.es]
 */
public class MatcherColoring {
    /** List of words marked as red (to be edited). */
    protected Set<Token> red_words;

    /** List of words marked as green (to be edited). */
    protected Set<Token> green_words;

    /** Last match found (if the index is negative means NULL). */
    private static int former_match=-2;

    /** The marker of the plugin. */
    private EditHintsMarker marker;
    
    /**
     * Class constructor.
     * @param menu Menu manager of the plugin.
     */
    public MatcherColoring(EditHintsMarker marker){
        this.marker=marker;
        green_words=new HashSet<Token>();
        red_words=new HashSet<Token>();
        
        // When the application is started up, the DocumentListener for the
        // matching text area is created
        CoreEvents.registerApplicationEventListener(new IApplicationEventListener(){
            public void onApplicationStartup() {
                
                MatchesTextArea matcher=(MatchesTextArea)Core.getMatcher();
                matcher.getDocument().addDocumentListener(new DocumentListener(){
                    //When a change is registered, if the active match changed,
                    //the recommendations are re-computed
                    public void changedUpdate(DocumentEvent e) {
                        int activeMatch=getActiveMatchIndex();
                        if(former_match!=activeMatch){
                            former_match=activeMatch;
                            ApplyRecommendations();
                        }
                    }

                    public void insertUpdate(DocumentEvent e) {
                        former_match=getActiveMatchIndex();
                    }

                    public void removeUpdate(DocumentEvent e) {
                        former_match=getActiveMatchIndex();
                    }
                });
            }

            public void onApplicationShutdown() {
            }
        });
    }

    /**
     * Method that returs the list of words marked in red.
     * @return List of words marked in red.
     */
    public Set<Token> getRedWords() {
        return red_words;
    }

    /**
     * Method that returs the list of words marked in green.
     * @return List of words marked in green.
     */
    public Set<Token> getGreenWords() {
        return green_words;
    }
    
    /**
     * Method that clears the list of green and red words for the current match.
     */
    public void clear(){
        green_words.clear();
        red_words.clear();
    }
    
    /**
     * Method that applies recommendations for the current match in the matching
     * text area.
     */
    public void ApplyRecommendations(){
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                //Before starting, lists of words (green and red) are reset
                clear();
        
                //Checking if there is any match
                NearString match = Core.getMatcher().getActiveMatch();
                if(match!=null && EditHintsMenu.getRecommendingEnabled()>0){
                    int activeMatchIdx = getActiveMatchIndex();
                    //Tokenising the source segment from the translation unit
                    ITokenizer tokenizer = Core.getProject().getSourceTokenizer();
                    if (tokenizer == null) {
                        return;
                    }
                    Token[] tokens = tokenizer.tokenizeAllExactly(match.source);

                    List<Word> words;
                    Segment sourceseg;
                    words=new LinkedList<Word>();
                    for(int i=0;i<tokens.length;i++){
                        String word=match.source.substring(tokens[i].getOffset(),
                                tokens[i].getOffset()+tokens[i].getLength());
                        //System.out.println(word);
                        if(!word.matches("\\s")){
                            words.add(new Word(word));
                        }
                    }
                    sourceseg=new Segment(words);
                    words.clear();
                    
                    //Tokenising the target segment from the translation unit
                    tokens = Core.getProject().getTargetTokenizer().tokenizeAllExactly(
                            match.translation);
                    for(int i=0;i<tokens.length;i++){
                        String word=match.translation.substring(tokens[i].getOffset(),
                                tokens[i].getOffset()+tokens[i].getLength());
                        if(!word.matches("\\s")){
                            words.add(new Word(word));
                        }
                    }
                    Segment targetseg=new Segment(words);

                    //Obtaining the evidence and the recommendations
                    TranslationUnit tu=new TranslationUnit(sourceseg, targetseg);
                    int[] result=null;
                    if(EditHintsMenu.getRecommendingEnabled()==1){
                        SegmentDictionary sd=ObtainEvidence(sourceseg, targetseg);

                        tu.CollectEvidences(sd, 3, false);
                        result=GeometricRecommender.MakeRecommendation(new Segment(
                                Core.getEditor().getCurrentEntry().getSrcText()), tu, 3,
                                false, null, null, 0.5);
                    }

                    //Colouring the words on the text area
                    if(result!=null){
                        List<Integer> delimiters;

                        Field delimitersField=null;
                        try{
                            delimitersField= MatchesTextArea.class.getDeclaredField("delimiters");
                            delimitersField.setAccessible(true);
                        }
                        catch(NoSuchFieldException nsfe){
                            nsfe.printStackTrace(System.err);
                            System.exit(-1);
                        }
                        try{
                            if(delimitersField!=null){
                                delimiters=(List<Integer>)delimitersField.get(
                                        ((MatchesTextArea)Core.getMatcher()));

                                int targetstart = delimiters.get(activeMatchIdx) +
                                        match.source.length()+1;
                                
                                for (int i = 0, j=0; i < tokens.length; i++) {
                                    Token token = tokens[i];
                                    int tokstart = targetstart + 3 + token.getOffset();
                                    int tokend = targetstart + 3 + token.getOffset() +
                                            token.getLength();
                                    if(!match.translation.substring(token.getOffset(),
                                            token.getOffset() + token.getLength()).matches("\\s")){
                                        ((MatchesTextArea)Core.getMatcher()).select(tokstart, tokend);
                                        if (result[j]==-1) {
                                            red_words.add(token);
                                            ((MatchesTextArea)Core.getMatcher()).setCharacterAttributes(
                                                    Styles.createAttributeSet(Color.red, null,
                                                    null, null), false);
                                        }
                                        else if (result[j]==1){
                                            green_words.add(token);
                                            ((MatchesTextArea)Core.getMatcher()).setCharacterAttributes(
                                                    Styles.createAttributeSet(Color.green, null,
                                                    null, null), false);
                                        }
                                        j++;
                                    }
                                }
                            }
                        }
                        catch(IllegalAccessException iae){
                            iae.printStackTrace(System.err);
                            System.exit(-1);
                        }
                    }
                }
            }
        });
    }
    
    /**
     * Method that resets the format of the text in the matching text area. This
     * method removes the colouring from the matching text area.
     */
    public void Unrecomend(){
        SwingUtilities.invokeLater(new Runnable()
        {
            //Dirty trick to repaint the original colours in the matcher: changing
            //the value of the active match with introspection and setting back
            //the true active match
            @Override
            public void run()
            {
                //
                Field actMatch=null;
                int activeMatch=-1;
                try{
                    actMatch= MatchesTextArea.class.getDeclaredField("activeMatch");
                }
                catch(NoSuchFieldException nsfe){
                    nsfe.printStackTrace(System.err);
                    System.exit(-1);
                }
                actMatch.setAccessible(true);
                try{
                    activeMatch=(Integer)actMatch.get(Core.getMatcher());
                    actMatch.set(Core.getMatcher(), -1);
                    Core.getMatcher().setActiveMatch(activeMatch);
                }
                catch(IllegalAccessException iae){
                    iae.printStackTrace(System.err);
                    System.exit(-1);
                }
                Core.getEditor().remarkOneMarker(EditHintsMarker.class.getName());
            }
        });
    }
    
    /**
     * Method that returns the index of the active match  from
     * <code>MatchesTextArea</code>.
     * This method uses introspection to acces the private MatchesTextArea
     * object in <code>Core</code> and returns the index of the active match.
     * This should be idealy accessed in a different way (without introspection)
     * but this is the only possibility by now.
     * @return Returns the index of the active match  from
     * <code>MatchesTextArea</code>.
     */
    public static int getActiveMatchIndex(){
        Field actMatch=null;
        int activeMatch=-1;
        try{
            actMatch= MatchesTextArea.class.getDeclaredField("activeMatch");
        }
        catch(NoSuchFieldException nsfe){
            nsfe.printStackTrace(System.err);
            System.exit(-1);
        }
        actMatch.setAccessible(true);
        try{
            activeMatch=(Integer)actMatch.get(Core.getMatcher());
        }
        catch(IllegalAccessException iae){
            iae.printStackTrace(System.err);
            System.exit(-1);
        }
        return activeMatch;
    }
    
    /**
     * Method that obtains a sub-segment pairs list by splitting two segments
     * and machine-translating them.
     * @param sourceseg Source segment
     * @param targetseg Target segment
     * @return Returns an <code>SegmentDictionary</code> object containing a
     * list of pairs of sub-segments which are mutual translations.
     */
    public SegmentDictionary ObtainEvidence(Segment sourceseg, Segment targetseg){
        
        Language source = Core.getProject().getProjectProperties().getSourceLanguage();
        Language target = Core.getProject().getProjectProperties().getTargetLanguage();
        
        List<SubSegment> subsegmentss=sourceseg.AllSubSegmentsInSentence(3);
        List<SubSegment> subsegmentst=targetseg.AllSubSegmentsInSentence(3);

        SegmentDictionary sd=new SegmentDictionary();
        for (IMachineTranslationEdithints mt : 
                marker.getMenu().GetMachineTranslatorsForEditHints()) {
            try {

                //Using paragraph tags to to sepparate the sub-segments to translate
                StringBuilder sb=new StringBuilder("<html>");
                for(SubSegment sub: subsegmentss){
                    sb.append("<p>");
                    sb.append(sub.toString());
                    sb.append("</p>");
                }
                sb.append("</html>");
                String trans=mt.getHTMLTranslation(source, target, sb.toString());
                if(trans!=null){
                    if(trans.matches("&.*;")){
                        FilterVisitor fv=new FilterVisitor(null, null, null);
                        Method entitiesToChars = FilterVisitor.class.
                                getDeclaredMethod("entitiesToChars");
                        entitiesToChars.setAccessible(true);
                        trans=(String) entitiesToChars.invoke(fv,trans);//new FilterVisitor(null, null, null).entitiesToChars(trans);
                    }
                    String[] splitten=trans.substring(0, trans.length()).replace(
                            "<html><p>", "").replace("</p></html>", "").split("</p><p>");
                    if(splitten.length!=subsegmentss.size()){
                        System.err.println("Error: sub-segments not correctly"
                                + "translated for word keeping recomendation:");
                        for(int i=0;i<splitten.length;i++){
                            System.err.println(splitten[i]+" -> "+
                                    subsegmentss.get(i));
                        }
                    }
                    else{
                        for(int i=0;i<subsegmentss.size();i++){
                            sd.AddSegmentPair(subsegmentss.get(i),
                                    new Segment(splitten[i].trim()));
                        }
                    }
                }

                sb=new StringBuilder("<html>");
                for(SubSegment sub: subsegmentst){
                    sb.append("<p>");
                    sb.append(sub.toString());
                    sb.append("</p>");
                }
                sb.append("</html>");
                trans=mt.getHTMLTranslation(target, source, sb.toString());
                if(trans!=null){
                    if(trans.matches(".*&.*;.*")){
                        FilterVisitor fv=new FilterVisitor(null, null, null);
                        Method entitiesToChars = FilterVisitor.class.
                                getDeclaredMethod("entitiesToChars");
                        entitiesToChars.setAccessible(true);
                        trans=(String) entitiesToChars.invoke(fv,trans);
                    }
                    String[] splitten=trans.substring(0, trans.length()).
                            replace("<html><p>", "").replace("</p></html>",
                            "").split("</p><p>");
                    if(splitten.length!=subsegmentst.size()){
                        System.err.println("Error: sub-segments not correctly"
                                + "translated for word keeping recomendation:");
                        for(int i=0;i<splitten.length;i++){
                            System.err.println(splitten[i]+" -> " +
                                    subsegmentst.get(i));
                        }
                    }
                    else{
                        for(int i=0;i<subsegmentst.size();i++){
                            sd.AddSegmentPair(new Segment(splitten[i].trim()),
                                    subsegmentst.get(i));
                        }
                    }
                }
            } catch (Exception ex) {
                Log.log(ex);
            }
        }
        return sd;
    }
}
