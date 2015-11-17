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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import org.omegat.core.Core;
import org.omegat.gui.editor.EditorController;
import org.omegat.gui.editor.SegmentBuilder;
import org.omegat.gui.editor.mark.Mark;

/**
 * Event listener that manages the changes in the edition text area. This class
 * implements a <code>DocumentListener</code> to control the 
 * edition text area.
 * @author Miquel Esplà Gomis [mespla@dlsi.ua.es]
 */
public class TextAreaDocumentListener implements DocumentListener{
    
    /** Edit hints marker object. */
    EditHintsMarker marker;
    
    /**
     * Method that returns the <code>SegmentBuilder</code> corresponding tho the
     * active entry in the edition text area.
     * @return Returns the <code>SegmentBuilder</code> corresponding tho the
     * active entry in the edition text area.
     */
    public SegmentBuilder getDisplayedSegmentBuilder(){
    
        Field displayedEntryIndexField=null;
        try{
            displayedEntryIndexField= EditorController.class.getDeclaredField("displayedEntryIndex");
            displayedEntryIndexField.setAccessible(true);
        }
        catch(NoSuchFieldException nsfe){
            nsfe.printStackTrace(System.err);
            System.exit(-1);
        }
        
        Field m_docSegListField=null;
        try{
            m_docSegListField= EditorController.class.getDeclaredField("m_docSegList");
            m_docSegListField.setAccessible(true);
        }
        catch(NoSuchFieldException nsfe){
            nsfe.printStackTrace(System.err);
            System.exit(-1);
        }        

        try{
            if(displayedEntryIndexField!=null && m_docSegListField!=null){
                int displayedEntryIndex = (Integer)displayedEntryIndexField.get(
                        (EditorController)Core.getEditor());
                SegmentBuilder[] m_docSegList = (SegmentBuilder[])
                        m_docSegListField.get((EditorController)Core.getEditor());
                return m_docSegList[displayedEntryIndex];
            }
        }
        catch(IllegalAccessException iae){
            iae.printStackTrace(System.err);
            System.exit(-1);
        }
        return null;
    }
    
    /**
     * Constructor of the class.
     * @param marker Edit hints marker object.
     */
    public TextAreaDocumentListener(EditHintsMarker marker){
        this.marker=marker;
    }
    
    public void changedUpdate(DocumentEvent e) {
    }

    /**
     * Method launched when any text is inserted in the edition text area.
     * @param e event
     */
    public void insertUpdate(DocumentEvent e) {
        try {
            //If the text is inserted in the current entry, it is registered
            if(Core.getEditor().getCurrentEntry()==marker.getLastEntry())
                marker.setTextInserted(e.getDocument().getText(e.getOffset(), e.getLength()));
            //The previous marks are updated to fix their placement
            List<Mark> newmarks=new ArrayList<Mark>();
            int offset= getDisplayedSegmentBuilder().getStartPosition()+
                    Core.getEditor().getCurrentEntry().getSrcText().length();

            for(Mark m: marker.getMarks()){
                //If the text is inserted after the word, nothing is changed
                if(m.endOffset>(e.getOffset()-1)-offset){
                    //If the text is added in the middle of a word, the mark on
                    //it is removed, since it is not the one which was marked
                    //any more; if it is added before the begginging of the word
                    //the mark is displaced
                    if(m.startOffset>=(e.getOffset()-1)-offset){
                        int newend=m.endOffset+e.getLength();
                        int newstart=m.startOffset+e.getLength();
                        Mark newmark=new Mark(Mark.ENTRY_PART.TRANSLATION, newstart, newend);
                        newmark.painter=m.painter;
                        newmarks.add(newmark);
                    }
                }
                else{
                    newmarks.add(m);
                }
            }
            marker.setMarks(newmarks);
        } catch (BadLocationException ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * Method launched when any text is deleted in the edition text area.
     * @param e event
     */
    public void removeUpdate(DocumentEvent e) {
        marker.setTextInserted("");
        List<Mark> newmarks=new ArrayList<Mark>();
        int offset= getDisplayedSegmentBuilder().getStartPosition()+
                Core.getEditor().getCurrentEntry().getSrcText().length();
        
        //The previous marks are updated to fix their placement
        for(Mark m: marker.getMarks()){
            //If the text is deleted is all after the word, nothing is changed
            if(m.endOffset>(e.getOffset()-offset)-e.getLength()){
                //If the part of (or the whole) the word is deleted, the mark on
                //it is removed, since it is not the one which was marked any
                //more; if all the text removed is at the left of the word, the
                //mark is displaced
                if(e.getOffset()-offset<=m.startOffset){
                    int newend=m.endOffset-e.getLength();
                    int newstart=m.startOffset-e.getLength();
                    Mark newmark=new Mark(Mark.ENTRY_PART.TRANSLATION, newstart, newend);
                    newmark.painter=m.painter;
                    newmarks.add(newmark);
                }
            }
            else{
                newmarks.add(m);
            }
        }
        marker.setMarks(newmarks);
    }
}
