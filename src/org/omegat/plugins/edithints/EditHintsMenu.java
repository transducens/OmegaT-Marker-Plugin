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

import org.omegat.plugins.edithints.machinetranslation.ApertiumTranslateEdithints;
import org.omegat.plugins.edithints.machinetranslation.MicrosoftTranslateEdithints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.events.IApplicationEventListener;
import org.omegat.gui.exttrans.IMachineTranslationEdithints;
import org.omegat.plugins.edithints.machinetranslation.Google2TranslateEdithints;

/**
 * Class that manages the menu of the plugin. This class contains all the menu
 * objects and actions to take when the user interacts with it.
 * @author Miquel Esplà Gomis [mespla@dlsi.ua.es]
 */
public class EditHintsMenu {
    
    /** Main menu for choosing the edit hints options. */
    private final JMenu edithintsmenu;

    /** Option for activating the geometric recommender. */
    final JCheckBoxMenuItem geometricRecommendingMenuItem;

    /** Sub-menu for choosing the machine translation sources. */
    final JMenu translatorsmenu;

    /** Option for activating Apertium. */
    final JCheckBoxMenuItem apertiumOption;
    
    /** Option for activating Microsoft translator. */
    final JCheckBoxMenuItem microsoftOption;

    /** Option for activating Google. */
    final JCheckBoxMenuItem googleOption;

    /** Variable for controlling the recommending method which is active. */
    private static int recommendingEnabled=0;
    
    /** List of machine translation available systems. */
    private Map<String,IMachineTranslationEdithints> machinetranslators;
    
    /** Marker of the pluging. */
    EditHintsMarker marker;
    
    /**
     * Method that returns the value of the variable
     * <code>recomendingEnabled</code>. This method indicates if the recommender
     * has been activated and which recommender is chosen.
     * @return Value of the variable <code>recomendingEnabled</code>.
     */
    public static int getRecommendingEnabled(){
        return recommendingEnabled;
    }
    
    /**
     * Method that returns the list of machine translation systems available for
     * recommending. Method that returns the list of machine translation systems
     * available for recommending
     * @return Returns the list of machine translation systems available for
     * recommending
     */
    public Set<IMachineTranslationEdithints> GetMachineTranslatorsForEditHints(){
        if(machinetranslators==null){
            machinetranslators = new HashMap<String,IMachineTranslationEdithints>();
        }
        return new HashSet<IMachineTranslationEdithints>(machinetranslators.values());
    }

    /**
     * Constructor of the class, which initialises the control variables in the
     * class and menus.
     * @param match_coloring Object that controls the coloring in the matcher.
     */
    public EditHintsMenu(EditHintsMarker marker) {
        this.marker=marker;
        
        machinetranslators=new HashMap<String,IMachineTranslationEdithints>();
        edithintsmenu=new JMenu("Edit hints");

        geometricRecommendingMenuItem = new JCheckBoxMenuItem("Simple");
        geometricRecommendingMenuItem.setEnabled(false);
        geometricRecommendingMenuItem.addActionListener(geometricRecommendingMenuItemActionListener);
        geometricRecommendingMenuItem.setSelected(false);
        
        apertiumOption = new JCheckBoxMenuItem("Apertium");
        apertiumOption.addActionListener(apmtListener);
        apertiumOption.setSelected(false);
        microsoftOption = new JCheckBoxMenuItem("Microsoft Translator");
        microsoftOption.addActionListener(mimtListener);
        microsoftOption.setSelected(false);
        googleOption = new JCheckBoxMenuItem("Google");
        googleOption.addActionListener(gomtListener);
        googleOption.setSelected(false);
        
        translatorsmenu=new JMenu("Translation options");
        translatorsmenu.add(apertiumOption);
        translatorsmenu.add(microsoftOption);
        translatorsmenu.add(googleOption);
        
        edithintsmenu.add(geometricRecommendingMenuItem);
        edithintsmenu.add(translatorsmenu);
        
        CoreEvents.registerApplicationEventListener(new IApplicationEventListener(){
            public void onApplicationStartup() {
                Core.getMainWindow().getMainMenu().getOptionsMenu().add(edithintsmenu);
            }

            public void onApplicationShutdown() {
            }
        });
    }

    /**
     * Listener which acts when the option of the geometric recommender is chosen.
     */
    protected ActionListener geometricRecommendingMenuItemActionListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            if(geometricRecommendingMenuItem.isSelected()){
                recommendingEnabled = 1;
                marker.getMatcherColoring().ApplyRecommendations();
            }
            else{
                recommendingEnabled=0;
                marker.getMatcherColoring().clear();
                marker.getMatcherColoring().Unrecomend();
            }
        }
    };
    
    /** Listener which acts when Apertium machine translation option is chosen. */
    protected ActionListener apmtListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            ApertiumTranslateEdithints amt=new ApertiumTranslateEdithints();
            if(apertiumOption.isSelected()){
                geometricRecommendingMenuItem.setEnabled(true);
                if(!machinetranslators.containsKey(amt.getName()))
                    machinetranslators.put(amt.getName(), amt);
            }
            else{
                if(machinetranslators.containsKey(amt.getName()))
                    machinetranslators.remove(amt.getName());
                if(machinetranslators.isEmpty()){
                    geometricRecommendingMenuItem.setSelected(false);
                    geometricRecommendingMenuItem.setEnabled(false);
                    marker.getMatcherColoring().clear();
                    marker.getMatcherColoring().Unrecomend();
                }
            }
        }
    };
    
    /** Listener which acts when Apertium machine translation option is chosen. */
    protected ActionListener gomtListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            Google2TranslateEdithints gmt=new Google2TranslateEdithints();
            if(googleOption.isSelected()){
                geometricRecommendingMenuItem.setEnabled(true);
                if(!machinetranslators.containsKey(gmt.getName()))
                    machinetranslators.put(gmt.getName(), gmt);
            }
            else{
                if(machinetranslators.containsKey(gmt.getName()))
                    machinetranslators.remove(gmt.getName());
                if(machinetranslators.isEmpty()){
                    geometricRecommendingMenuItem.setSelected(false);
                    geometricRecommendingMenuItem.setEnabled(false);
                    marker.getMatcherColoring().clear();
                    marker.getMatcherColoring().Unrecomend();
                }
            }
        }
    };

    /** Listener which acts when Microsoft machine translation option is chosen. */
    protected ActionListener mimtListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            MicrosoftTranslateEdithints mmt=new MicrosoftTranslateEdithints();
            if(microsoftOption.isSelected()){
                geometricRecommendingMenuItem.setEnabled(true);
                if(!machinetranslators.containsKey(mmt.getName()))
                    machinetranslators.put(mmt.getName(), mmt);
            }
            else{
                if(machinetranslators.containsKey(mmt.getName()))
                    machinetranslators.remove(mmt.getName());
                if(machinetranslators.isEmpty()){
                    geometricRecommendingMenuItem.setSelected(false);
                    geometricRecommendingMenuItem.setEnabled(false);
                    marker.getMatcherColoring().clear();
                    marker.getMatcherColoring().Unrecomend();
                }
            }
        }
    };
}
