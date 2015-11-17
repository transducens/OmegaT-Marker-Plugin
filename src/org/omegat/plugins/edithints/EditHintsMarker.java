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

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter.HighlightPainter;
import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.editor.Document3;
import org.omegat.gui.editor.EditorController;
import org.omegat.gui.editor.EditorTextArea3;
import org.omegat.gui.editor.mark.IMarker;
import org.omegat.gui.editor.mark.Mark;
import org.omegat.util.Token;

/**
 * Marker that colours the text in the text area when a proposal from the
 * translation memory is accepted.
 * @author Miquel Esplà Gomis [mespla@dlsi.ua.es]
 */
public class EditHintsMarker implements IMarker{
    
    /** Painter for words in green. */
    protected static final HighlightPainter GPAINTER = new DefaultHighlighter.DefaultHighlightPainter(Color.green);
    /** Painter for words in red. */
    protected static final HighlightPainter RPAINTER = new DefaultHighlighter.DefaultHighlightPainter(Color.red);

    /** List of current marks for the active entry. */
    private List<Mark> marks;
    
    /** Object that controls the actions on the matcher for coloring proposals in this text box. */
    private MatcherColoring matcher_coloring;
    
    /** Object that initialises and controls the menu of the EditHints utility. */
    private EditHintsMenu menu;
    
    /** Field that contains the last entry being displayed. */
    private SourceTextEntry last_entry=null;
    
    /** Field that contains the last text inserted. */
    private String text_inserted;
    
    /**
     * Method that returns the text last text inserted in the text box.
     * Method that returns the text last text inserted in the text box.
     * @return Returns the value of the variable <code>text_inserted</code>
     */
    public String getTextInserted() {
        return text_inserted;
    }

    /**
     * Method that sets the text last text inserted in the text box.
     * Method that sets the text last text inserted in the text box.
     * 
     * @param text_inserted Test inserted in the text box
     */
    public void setTextInserted(String text_inserted) {
        this.text_inserted = text_inserted;
    }
    
    /**
     * Method that sets the last entry being displayed.
     * Method that sets the last entry being displayed.
     * @param new_last_entry Entry which have been displayed.
     */
    public void setLastEntry(SourceTextEntry new_last_entry) {
        this.last_entry = new_last_entry;
    }

    /**
     * Method that returns the last entry being displayed.
     * Method that returns the last entry being displayed.
     * @return Returns the last entry being displayed.
     */
    public SourceTextEntry getLastEntry() {
        return last_entry;
    }
    
    /** 
     * Method that returns the list of marks for the text from the translation
     * memory.
     * Method that returns the list of marks for the text from the translation
     * memory.
     * @return Returns the current list of marks.
     */
    public List<Mark> getMarks() {
        return marks;
    }
    
    /**
     * Method that sets a new list of marks for the marker.
     * Method that sets a new list of marks to replace the current one in the marker.
     * @param new_marks New list of marks for the marker.
     */
    public void setMarks(List<Mark> new_marks){
        marks=new_marks;
    }
    
    /**
     * Method that clears the list of marks in the marker.
     */
    public void clearMarks(){
        marks.clear();
    }
    
    public MatcherColoring getMatcherColoring(){
        return this.matcher_coloring;
    }
    
    public EditHintsMenu getMenu(){
        return this.menu;
    }
    
    /** 
     * Constructor of the marker.
     */
    public EditHintsMarker() {
        matcher_coloring=new MatcherColoring(this);
        menu=new EditHintsMenu(this);
        marks=new LinkedList<Mark>();

        CoreEvents.registerEntryEventListener(new EntryChangedEventListener(
                matcher_coloring, this));
    }
    
    /**
     * Retisters the marker class in the list of markers of OmegaT.
     */
    public static void loadPlugins() {
        Core.registerMarkerClass(EditHintsMarker.class);
    }
    
    /**
     * Method that calls the protected method <code>isEditMode</code> from
     * <code>Document3</code>.
     * Method that calls the protected method <code>isEditMode</code> in the
     * object <code>Document3</code>, placed inside the text area where entries
     * are shown. Since this method is not public, the call is performed by
     * means of introspection. Ideally, this way of calling the method should be
     * changed.
     * @param doc Document3 object from which the method will be called.
     * @return Returns the exit of the method <code>isEditMode</code> from <code>doc</code>
     */
    public static boolean isEditMode(Document3 doc){
        
        try {
            Method privateMethod = Document3.class.getDeclaredMethod("isEditMode", null);
        
            privateMethod.setAccessible(true);

            Boolean returnValue = (Boolean)
                    privateMethod.invoke(doc, null);
            if(returnValue==null)
                return false;
            else{
                boolean value=returnValue;
                return value;
            }
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace(System.err);
            System.exit(-1);
        } catch (SecurityException ex) {
            ex.printStackTrace(System.err);
            System.exit(-1);
        } catch (IllegalAccessException ex) {
            ex.printStackTrace(System.err);
            System.exit(-1);
        } catch (InvocationTargetException ex) {
            ex.printStackTrace(System.err);
            System.exit(-1);
        }
        return false;
    }

    /**
     * Method that returns the marks for a given entry.
     * Method that returnst the list of marks for each entry in the text area.
     * @param ste Entry for which the marks are requiered.
     * @param sourceText Sourece text in the entry.
     * @param translationText Translation in the entry.
     * @param isActive This is <code>true</code> if the ste for which the marks
     * will be obtained is the one which is active.
     * @return Returns the marks for a given entry.
     * @throws Exception 
     */
    @Override
    public List<Mark> getMarksForEntry(SourceTextEntry ste, String sourceText,
            String translationText, boolean isActive) throws Exception {
        
        //If the text area is not under edition, this means that a new entry is
        //being activated and, therefore, the marks must be reset
        if(!isEditMode(getEditorTextArea().getOmDocument())){
            marks.clear();
            return marks;
        }
        //If no recommendation is made on thye active match (or no match was
        //found) 
        if(matcher_coloring.getRedWords().isEmpty() && matcher_coloring.getGreenWords().isEmpty())
            return marks;
        
        //The marks are obtained only if: a) the ste matches the active one
        //b) it is active, c) there is at least one match, and d) the text
        //inserted matches text of the active match
        if(ste==last_entry && isActive && Core.getMatcher().getActiveMatch()!=null
                && Core.getEditor().getCurrentTranslation()!=null &&
                text_inserted.equals(Core.getMatcher().getActiveMatch().translation)){
            if(matcher_coloring.getRedWords()!=null){
                for(Token tok: matcher_coloring.getRedWords()){
                    Mark m=new Mark(Mark.ENTRY_PART.TRANSLATION, tok.getOffset(), tok.getOffset()+tok.getLength());
                    m.painter=RPAINTER;
                    marks.add(m);
                }
            }

            if(matcher_coloring.getGreenWords()!=null){
                for(Token tok: matcher_coloring.getGreenWords()){
                    Mark m=new Mark(Mark.ENTRY_PART.TRANSLATION, tok.getOffset(), tok.getOffset()+tok.getLength());
                    m.painter=GPAINTER;
                    marks.add(m);
                }
            }
        }
        return marks;
    }
    
    
    /**
     * Method that returns the EditorTextArea3 object from <code>Core</code>.
     * This method uses introspection to acces the private EditorTextArea3
     * object in <code>Core</code> and return it. This should be idealy accessed
     * in a different way (without introspection) but it is the only possibility
     * by now.
     * @return Returns the EditorTextArea3 object from <code>Core</code>
     */
    public static EditorTextArea3 getEditorTextArea(){
        EditorController controller=(EditorController)Core.getEditor();

        //Getting the field
        Field editor=null;
        EditorTextArea3 tarea=null;
        try{
            editor= EditorController.class.getDeclaredField("editor");
        }
        catch(NoSuchFieldException nsfe){
            nsfe.printStackTrace(System.err);
            System.exit(-1);
        }
        //Setting it accessible
        editor.setAccessible(true);
        try{
            tarea=(EditorTextArea3)editor.get(controller);
        }
        catch(IllegalAccessException iae){
            iae.printStackTrace(System.err);
            System.exit(-1);
        }
        //Returning the object
        return tarea;
    }
}
