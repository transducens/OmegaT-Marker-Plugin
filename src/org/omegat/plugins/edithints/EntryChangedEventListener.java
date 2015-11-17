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

import javax.swing.event.DocumentListener;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.events.IEntryEventListener;

/**
 * Class that manages the activation of an entry. This class controls the
 * activateion of an entry in the text area where entries are shown. When a new
 * entry is activated, the variables controling the actions on the editino text
 * area and the matchig text area are restarted and the
 * <code>TextAreaDocumentListener</code> is reset.
 * @author Miquel Esplà Gomis [mespla@dlsi.ua.es]
 */
public class EntryChangedEventListener implements IEntryEventListener{
    
    /** Object that controls the coloring on the matching text area. */
    private MatcherColoring match_coloring;
    
    /** Edit hints marker object. */
    private EditHintsMarker marker;
    
    /** Document listener for managing the actions on the edition text area. */
    private DocumentListener insertion_listener;
    
    /**
     * Class builder.
     * @param match_coloring <code>MatcherColoring</code> used by the marker.
     * @param marker <code>EditHintsMarker<code> object using this listener.
     */
    public EntryChangedEventListener(MatcherColoring match_coloring, EditHintsMarker marker){
        this.match_coloring=match_coloring;
        this.marker=marker;
        this.insertion_listener=new TextAreaDocumentListener(marker);
    }
    
    @Override
    public void onNewFile(String activeFileName) {}

    /**
     * Method launched when an entry is activated.
     * @param newEntry Entry which has been activated.
     */
    @Override
    public void onEntryActivated(SourceTextEntry newEntry) {
        match_coloring.clear();
        marker.setTextInserted("");
        marker.clearMarks();
        marker.setLastEntry(newEntry);
        EditHintsMarker.getEditorTextArea().getOmDocument().removeDocumentListener(insertion_listener);
        EditHintsMarker.getEditorTextArea().getOmDocument().addDocumentListener(insertion_listener);

    }
}
