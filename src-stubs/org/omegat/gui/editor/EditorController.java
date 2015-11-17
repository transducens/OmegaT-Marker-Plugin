/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.omegat.gui.editor;

import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.main.MainWindow;

/**
 *
 * @author miquel
 */
public class EditorController implements IEditor{

    protected SegmentBuilder[] m_docSegList;
    protected int displayedEntryIndex;

    public EditorController(final MainWindow mainWindow) {}

    public SourceTextEntry getCurrentEntry() {return null;}

    public void remarkOneMarker(final String markerClassName) {}

    public String getCurrentTranslation() {return null;}
}
