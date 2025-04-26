package megl.polyforms;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JFileChooser;
import javax.swing.JCheckBox;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.border.Border;

import java.awt.BorderLayout;
import java.awt.Polygon;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.FlowLayout;

import java.io.File;

import java.util.Scanner;
import java.util.HashSet;
import java.util.ArrayList;

//import java.io.File;

public class PolyformDisplayer extends JFrame implements ActionListener {
    /**
    The p of the tessellation being displayed.
    */
    int p;

    /**
    The q of the tessellation being displayed.
    */
    int q;
    
    /**
    The ring of the tessellation that is generated.
    */
    int rings;
    
    /**
    The polyform being displayed.
    */
    Polyform polyform;
    
    /**
    The tessellation being displayed.
    */
    Tessellation tessellation;
    
    /**
    The tile used to orient the display.
    */
    Tile centerTile;
    
    /**
    The tile whose neighbors will be highlighted
    */
    Tile highlightCenter;
    
    /**
    The border used by tiles.
    Might be outdated? I think I replaced this with a class that extends JButton. 
    */
    final Border blackBorder = BorderFactory.createLineBorder(Color.BLACK);
    
    /**
    The border used for the tile that is the center of the highlight.
    */
    //final Border purpleishBorder = BorderFactory.createLineBorder(new Color(140, 50, 255), 5);
    final Border turquoiseBorder = BorderFactory.createLineBorder(new Color(0, 164, 164), 5);
    
    
    //The following JComponents are stored to make it easier to update or retrieve information from them.
    /**
    The labels used to display what tile is currently used to orient the display.
    */
    JLabel[] currentCenterText = new JLabel[3];
    
    /**
    The labels used to display what tile is currently used to orient the highlight.
    */
    JLabel[] currentHighlightText = new JLabel[3];
    
    /**
    The label used to display information about the polyform and tessellation.
    */
    JLabel topPanelText;
    
    /**
    The label used to diplay the list of tiles adjacent to the current highlight center
    */
    JLabel highlightAdjacentText;
    
    /**
    The labels used to display how many holes the polyform has and where they are, and whether the polyform is connected.
    [0] is holes, [1] is connectivity.
    */
    JLabel[] polyformStats = new JLabel[2];
    
    /**
    The text field used to open a new tessellation.
    */
    JTextField tessellator;
    
    /**
    The text field used to toggle tiles in the tessellation as part of the polyform.
    */
    JTextField tileToggler;
    
    /**
    The text field used to teleport center and highlight to tiles.
    */
    JTextField tileTeleporter;
    
    /**
    The panel containing all the tiles being displayed.
    */
    JPanel centerPanel;
    
    /**
    An array containing each row of tiles being displayed.
    */
    JPanel[] rows;
    
    /**
    If true, indicates that tile should be merged into mega tiles when appropriate.
    */
    boolean usingMegaTiles = true;
    
    /**
    Creates a display for a polyform with no tiles in the {4,5} tessellation.
    */
    public PolyformDisplayer(){
        this(4, 5, 5, null);
    }

    /**
    Creates a display for a polyform with no tiles in the specified tessellation.
    @param p the p of the tessellation
    @param q the q of the tessellation
    */    
    public PolyformDisplayer(int p, int q){
        this(p, q, 5, null);
    }
    
    /**
    Creates a display for a polyform with no tiles in the specified tessellation.
    @param p the p of the tessellation
    @param q the q of the tessellation
    @param rings the number of the highest ring in the tessellation
    */
    public PolyformDisplayer(int p, int q, int rings){
        this(p, q, rings, null);
    }
    
    /**
    Creates a display for the polyform specified by the given file in the {p,q} tessellation.
    @param p the p of the tessellation
    @param q the q of the tessellation
    @param rings the number of the highest ring in the tessellation
    @param fileName the path to a file that contains the data necessary for making a polyform.
    */
    public PolyformDisplayer(int p, int q, int rings, String fileName){
        super("Polyform Displayer");
        this.p = p;
        this.q = q;
        this.rings = rings;
        tessellation = new Tessellation(p, q, rings);
        centerTile = tessellation.getTile(0, 0);
        highlightCenter = centerTile;
        Polyform tempP = Polyform.makePolyform(fileName);
        this.polyform = new Polyform(tessellation);
        Tile temp;
        for(Tile t : tempP.animal){
            temp = tessellation.getTile(t.ringNum, t.ringPos);
            if(temp != null)
                polyform.animal.add(temp);
        }
        initializeDisplay();
    }
    
    /**
    Creates the structure for displaying polyforms. Defaults to the viewpoint of (0,0).
    */
    private void initializeDisplay(){
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        makeTopPanel();
        makeBottomPanel();
        makeLeftPanel();
        makeCenterPanel();
        
        pack();
        setVisible(true);
    }
    
    /**
    Creates the top panel, saying the dimensions of the polyform being displayed.
    */
    private void makeTopPanel(){
        JPanel topPanel = new JPanel();
        topPanelText = new JLabel();
        updateTopPanelText();
        topPanel.add(topPanelText);
        add(topPanel, BorderLayout.NORTH);
    }
    
    /**
    Creates the bottom panel, containing buttons for shifting the view window and a reminder of the current position.
    */
    private void makeBottomPanel(){
        JPanel bottomWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        
        addNavigationButtons(bottomWrapper);
        addCenteringButtons(bottomWrapper);
        addTeleportField(bottomWrapper);
        
        add(bottomWrapper, BorderLayout.SOUTH);
    }
    
    /**
    Adds buttons for shifting the view by one tile at a time.
    @param container the container that the buttons will be added to
    */
    private void addNavigationButtons(JPanel container){
        JPanel[] bottomPanels = new JPanel[3];
        JPanel textPanel;

        for(int i = 2; i >= 0; i--){
            bottomPanels[i] = new JPanel(new BorderLayout(10, 5));

            //the buttons use unicode arrow symbols
            JButton goUp = new JButton("\u2191");
            JButton goDown = new JButton("\u2193");
            JButton goLeft = new JButton("\u2190");
            JButton goRight = new JButton("\u2192");
        
            goUp.addActionListener(this);
            goUp.setActionCommand(String.format("01%d", i));
            goDown.addActionListener(this);
            goDown.setActionCommand(String.format("02%d", i));
            goLeft.addActionListener(this);
            goLeft.setActionCommand(String.format("03%d", i));
            goRight.addActionListener(this);
            goRight.setActionCommand(String.format("04%d", i));
        
            bottomPanels[i].add(goUp, BorderLayout.NORTH);
            bottomPanels[i].add(goDown, BorderLayout.SOUTH);
            bottomPanels[i].add(goLeft, BorderLayout.WEST);
            bottomPanels[i].add(goRight, BorderLayout.EAST);
            
            currentCenterText[i] = new JLabel("Current Center: (0, 0)");
            currentHighlightText[i] = new JLabel("Highlight Center: (0,0)");
            textPanel = new JPanel();
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            if(i != 1)
                textPanel.add(currentCenterText[i]);
            if(i != 0)
                textPanel.add(currentHighlightText[i]);
            bottomPanels[i].add(textPanel, BorderLayout.CENTER);
        }
        
        container.add(bottomPanels[0]);
        container.add(bottomPanels[2]);
        container.add(bottomPanels[1]);
    }
    
    /**
    Adds buttons to bring the center and highlight to each other.
    @param container the container that the buttons will be added to
    */
    private void addCenteringButtons(JPanel container){
        JPanel centerer = new JPanel();
        centerer.setLayout(new BoxLayout(centerer, BoxLayout.Y_AXIS));
        
        JButton centerToHighlight = new JButton("Move Center to Highlight");
        JButton highlightToCenter = new JButton("Move Highlight to Center");
        centerToHighlight.addActionListener(this);
        centerToHighlight.setActionCommand("050");
        highlightToCenter.addActionListener(this);
        highlightToCenter.setActionCommand("051");
        
        centerer.add(centerToHighlight);
        centerer.add(highlightToCenter);
        container.add(centerer);
    }
    
    /**
    Adds a text field for teleporting to a tile
    */
    private void addTeleportField(JPanel container){
        JPanel teleportWrapper = new JPanel();
        teleportWrapper.setLayout(new BoxLayout(teleportWrapper, BoxLayout.Y_AXIS));
        
        teleportWrapper.add(new JLabel("<html><body>Enter ringNum,ringPos,ringNum,ringPos<br>to teleport center and highlight</body></html>"));
        
        tileTeleporter = new JTextField();
        tileTeleporter.setMaximumSize(new Dimension(450, 25));
        tileTeleporter.addActionListener(this);
        tileTeleporter.setActionCommand("3");
        
        teleportWrapper.add(tileTeleporter);
        container.add(teleportWrapper);
    }
    
    /**
    Creates the left panel, containing things for controlling the polyform being represented.
    */
    private void makeLeftPanel(){
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        
        addTessellationSelector(leftPanel);
        addFileButtons(leftPanel);
        addToggleField(leftPanel);
        addStatsPanel(leftPanel);
        addHighlightAdjacentText(leftPanel);
        addMegaTileToggle(leftPanel);
        
        add(leftPanel, BorderLayout.WEST);
    }
    
    /**
    Adds a label and text field for opening a new Tessellation to the specified container.
    @param container the container the text field will be added to
    */
    private void addTessellationSelector(JPanel container){
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        
        wrapper.add(new JLabel("<html><body>Enter p, q, rings to open<br>a fresh tessellation.</body></html>"));
        
        tessellator = new JTextField();
        tessellator.setMaximumSize(new Dimension(300, 25));
        tessellator.addActionListener(this);
        tessellator.setActionCommand("5");
        
        wrapper.add(tessellator);
        container.add(wrapper);
    }
    
    /**
    Adds buttons for file selection to the specified container.
    @param container the container that buttons wil be added to
    */
    private void addFileButtons(JPanel container){
        JButton fileChooserButton = new JButton("Open a polyform file");
        fileChooserButton.setMaximumSize(new Dimension(300,25));
        fileChooserButton.setMinimumSize(new Dimension(300,25));
        fileChooserButton.addActionListener(this);
        fileChooserButton.setActionCommand("20");
        container.add(fileChooserButton);
        
        fileChooserButton = new JButton("Save current polyform");
        fileChooserButton.setMaximumSize(new Dimension(300,25));
        fileChooserButton.setMinimumSize(new Dimension(300,25));
        fileChooserButton.addActionListener(this);
        fileChooserButton.setActionCommand("21");
        container.add(fileChooserButton);
    }
    
    /**
    Adds a label and text field for tie toggling to the specified container.
    @param container the container that the components will be added to
    */
    private void addToggleField(JPanel container){
        JPanel toggleWrapper = new JPanel();
        toggleWrapper.setLayout(new BoxLayout(toggleWrapper, BoxLayout.Y_AXIS));
        
        toggleWrapper.add(new JLabel("<html><body>Enter ringNum,ringPos to<br>toggle a tile</body></html>"));
        
        tileToggler = new JTextField();
        tileToggler.setMaximumSize(new Dimension(300, 25));
        tileToggler.addActionListener(this);
        tileToggler.setActionCommand("1");
        
        toggleWrapper.add(tileToggler);
        container.add(toggleWrapper);
    }

    /**
    Adds a button for recalculating stats about the polyform and a display for those stats.
    These stats include hole numbers and positions. 
    @param container the container the button and display will be added to.
    */    
    private void addStatsPanel(JPanel container){
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

        polyformStats[0] = new JLabel();
        polyformStats[1] = new JLabel();
        recalcPolyformStats();
        wrapper.add(polyformStats[0]);
        wrapper.add(polyformStats[1]);
        
        JButton temp = new JButton("Recalculate Stats");
        temp.setMaximumSize(new Dimension(300, 25));
        temp.addActionListener(this);
        temp.setActionCommand("4");
        wrapper.add(temp);
        
        container.add(wrapper);
    }
    
    /**
    Adds a list of tiles adjacent to the current highlight center to the given container.
    This text can be updated by the updateHighlightAdjacentText or by accessing the highlightAdjacentText JLabel.
    @param container the container the text will be placed in
    */
    private void addHighlightAdjacentText(JPanel container){
        highlightAdjacentText = new JLabel();
        updateHighlightAdjacentText();
        container.add(highlightAdjacentText);
    }
    
    /**
    Adds a button for toggling the usage of mega tiles in the display
    */
    private void addMegaTileToggle(JPanel container){
        JCheckBox temp = new JCheckBox("Enable Tile Merging", true);
        temp.setActionCommand("6");
        temp.addActionListener(this);
        container.add(temp);
    }
    
    /**
    Creates and fills the center panel with rows of tiles.
    */
    private void makeCenterPanel(){
        centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        makeRows(centerPanel);
        add(centerPanel);
    }
    
    /**
    Makes and adds the rows of tiles to the specified JPanel.
    @param container the component that rows will be added to
    */
    private void makeRows(JPanel container){
        rows = new JPanel[5];
        JPanel row;
        
        Tile[][] temp = getRowBounds();
        Tile[] firsts = temp[0];
        Tile[] lasts = temp[1];
        for(int i = 4; i >= 0; i--){
            rows[i] = new JPanel();
            //rows[i].setLayout(new BoxLayout(rows[i], BoxLayout.X_AXIS));
            rows[i].setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));
            populateRow(rows[i], firsts[i], lasts[i]);
            container.add(rows[i]);
        }
    }
    
    /**
    Computes where the edges of the rings will be.
    @returns an array containing two arrays (in order): the start points of the rows, the end points of the rows
    */
    private Tile[][] getRowBounds(){
        if(p == 3)
            return getRowBoundsAlt();
        Tile[] firsts = new Tile[5];
        Tile[] lasts = new Tile[5];
        
        firsts[2] = findNextPrimary(centerTile.getPreviousInRing(), false); // calculates firsts and lasts for center row
        lasts[2] = findNextPrimary(centerTile.getNextInRing(), true);
        
        Tile[] temp;
        for(int i = 1; i <= 2; i++){ // calculates firsts and lasts for rows i removed from the center row
            if(centerTile.ringNum + i <= rings){
                temp = lasts[2+(i-1)].getNextRing();
                firsts[2+i] = findNextPrimary(firsts[2+(i-1)].getNextRing()[0].getPreviousInRing(), false);
                lasts[2+i] = findNextPrimary(temp[temp.length-1].getNextInRing(), true);
            }
            else {
                firsts[2+i] = null;
                lasts[2+i] = null;
            }
            
            if(centerTile.ringNum - i >= 0){
                firsts[2-i] = firsts[2-(i-1)];
                while(!firsts[2-i].wasPrimary)
                    firsts[2-i] = firsts[2-i].getPreviousInRing();
                firsts[2-i] = findNextPrimary(firsts[2-i].getPreviousRing(), false);
                
                lasts[2-i] = lasts[2-(i-1)];
                while(!lasts[2-i].wasPrimary)
                    lasts[2-1] = lasts[2-i].getPreviousInRing();
                lasts[2-i] = findNextPrimary(lasts[2-i].getPreviousRing(), true);
            }
            else {
                firsts[2-i] = null;
                lasts[2-i] = null;
            }
        }
        return new Tile[][]{firsts, lasts};
    }
    
    /**
    Computes where the edges of the rings will be. Used when p = 3 to prevent a crash.
    @returns two arrays (in order): the start points of the rows, the end points of the end points of the rows
    */
    private Tile[][] getRowBoundsAlt(){
        Tile[] firsts = new Tile[5];
        Tile[] lasts = new Tile[5];
        
        firsts[2] = findNextPrimary(centerTile.getPreviousInRing(), false); // calculates firsts and lasts for center row
        lasts[2] = findNextPrimary(centerTile.getNextInRing(), true);
        
        Tile[] temp;
        for(int i = 1; i <= 2; i++){ // calculates firsts and lasts for rows i removed from the center row
            if(centerTile.ringNum + i <= rings){
                temp = lasts[2+(i-1)].getNextInRing().getNextRing();
                firsts[2+i] = findNextPrimary(firsts[2+(i-1)].getPreviousInRing().getNextRing()[0], false);
                lasts[2+i] = findNextPrimary(temp[temp.length-1].getNextInRing(), true);
            }
            else {
                firsts[2+i] = null;
                lasts[2+i] = null;
            }
            
            if(centerTile.ringNum - i >= 0){
                firsts[2-i] = firsts[2-(i-1)];
                while(!firsts[2-i].wasPrimary)
                    firsts[2-i] = firsts[2-i].getPreviousInRing();
                firsts[2-i] = findNextPrimary(firsts[2-i].getPreviousRing(), false);
                
                lasts[2-i] = lasts[2-(i-1)];
                while(!lasts[2-i].wasPrimary)
                    lasts[2-1] = lasts[2-i].getPreviousInRing();
                lasts[2-i] = findNextPrimary(lasts[2-i].getPreviousRing(), true);
            }
            else {
                firsts[2-i] = null;
                lasts[2-i] = null;
            }
        }
        return new Tile[][]{firsts, lasts};
    }
    
    /**
    Redraws the center to match the backend state.
    */
    private void redrawCenter(){
        centerPanel.removeAll();
        makeRows(centerPanel);
        revalidate();
        repaint();
    }
    
    /**
    Finds the next primary tile in a ring from the given start tile. Searches next tiles if given true, previous tiles otherwise.
    @param start the start point for searching
    @param searchForward true if it should check next tiles, false if it should check previous
    @return the first primary tile in the specified direction.
    */
    private Tile findNextPrimary(Tile start, boolean searchForward){
        if(searchForward)
            while(!start.wasPrimary)
                start = start.getNextInRing();
        else
            while(!start.wasPrimary)
                start = start.getPreviousInRing();
        return start;
    }
    
    /**
    Adds tiles to the given row until all tiles from first to last have been added.
    @param row the row that will be filled
    @param first the first tile that will be added
    @param last the last tile that will be added
    */
    private void populateRow(JPanel row, Tile first, Tile last){
        if(first == null || last == null)
            return;
        Tile temp = first;
        boolean pastLast = false;
        while(!first.equals(last)){
            if(usingMegaTiles && !(first.equals(centerTile) || first.equals(highlightCenter) || highlightCenter.isNeighbor(first))){
                for(temp = first.getNextInRing(); true; temp = temp.getNextInRing()){ //finds bounds for a MegaTile
                    if(temp.type != first.type || temp.equals(centerTile) || temp.equals(highlightCenter) || highlightCenter.isNeighbor(temp)){ //only permits tiles of the same type, not center, not highlighted
                        temp = temp.getPreviousInRing(); //backs up when it goes too far
                        break;
                    }
                    if(temp.equals(last)){
                        pastLast = true;
                        break;
                    }
                }
            }
            else
                temp = first;
            temp = groupTiles(first, temp);
            row.add(makeTilePanel(temp));
            if(pastLast)
                return;
            if(temp instanceof MegaTile mega)
                if(mega.bigToSmall) //wrapped around pos0
                    first = mega.getExtremities()[0].getNextInRing();
                else
                    first = mega.getNextInRing();
            else
                first = temp.getNextInRing();
        }
        row.add(makeTilePanel(last));
    }
    
    /**
    Creates a TilePanel representing the specified tile. Will have a red/gray background if it is/isn't part of the polyform.
    Will have a blue/black border if it is/isn't selected.
    @param tile the Tile represented by the panel
    @return a TilePanel representing the specified tile
    */
    private TilePanel makeTilePanel(Tile tile){
        boolean isHighlighted = tile.is(highlightCenter.getRingNum(), highlightCenter.getRingPos()) || tile.isNeighbor(highlightCenter);
        TilePanel temp = new TilePanel(tile, polyform.contains(tile), isHighlighted);
        if(tile.equals(highlightCenter))
            temp.setBorder(turquoiseBorder/*purpleishBorder*/);
        return temp;
    }
    
    /**
    Changes which tile is the basis of the display.
    @param c1 the control character identifying which direction the Tiles are being shifted. 1: up, 2: down, 3: left, 4: right
    @param c2 the control character identifying what is being shifted. 0: the view, 1: the highlight, 2: both
    */
    private void shiftTiles(char c1, char c2){
        Tile newViewCenter = null;
        Tile newHighlightCenter = null;
        switch(c1){
            case '1': //shifting up
                if((c2 == '0' || c2 == '2') && centerTile.ringNum != rings){
                    if(p > 3 || !centerTile.wasPrimary || centerTile.ringNum == 0)
                        newViewCenter = centerTile.getNextRing()[0];
                }
                if((c2 == '1' || c2 == '2') && highlightCenter.ringNum != rings){
                    if(p > 3 || !centerTile.wasPrimary || centerTile.ringNum == 0)
                        newHighlightCenter = highlightCenter.getNextRing()[0];
                }
                break;
            case '2': //shifting down
                if((c2 == '0' || c2 == '2') && centerTile.wasPrimary && centerTile.ringNum != 0)
                    newViewCenter = centerTile.getPreviousRing();
                if((c2 == '1' || c2 == '2') && highlightCenter.wasPrimary && highlightCenter.ringNum != 0)
                    newHighlightCenter = highlightCenter.getPreviousRing();
                break;
            case '3': //shifting left
                if(c2 == '0' || c2 == '2')
                    newViewCenter = centerTile.getPreviousInRing();
                if(c2 == '1' || c2 == '2')
                    newHighlightCenter = highlightCenter.getPreviousInRing();
                break;
            case '4': //shifting right
                if(c2 == '0' || c2 == '2')
                    newViewCenter = centerTile.getNextInRing();
                if(c2 == '1' || c2 == '2')
                    newHighlightCenter = highlightCenter.getNextInRing();
                break;
            case '5': //shifting to bring together center and highlight
                if(c2 == '0')
                    newViewCenter = highlightCenter;
                else if(c2 == '1')
                    newHighlightCenter = centerTile;
                break;
            default:
                return;
        }
        teleport(newViewCenter, newHighlightCenter);
    }
    
    /**
    Teleports the view and highlight based on the text in the teleporter text field.
    */
    private void teleportFromTextField(){
        String text = tileTeleporter.getText();
        tileTeleporter.setText("");
        teleport(text);
    }
    
    /**
    Moves the center and highlight to the specified tiles.
    The string should be formatted "rn1,rp1,rn2,rp2" where rn is short for ringNum, rp is short for ringPos,
    and tile 1 is where the new view center will be, tile 2 is where the new highlight center will be.
    To not change one of these, just include commas without their numbers.
    @param tile the coordinates of the tile that will be teleported to
    */
    private void teleport(String tile){
        String[] targets = tile.split("\\D+"); //non-digit one or more times, greedy
        if(targets.length < 2)
            return;
        Tile newViewCenter = Tile.parseString(targets[0] + "," + targets[1]);
        Tile newHighlightCenter = null;
        if(targets.length >= 4)
            newHighlightCenter = Tile.parseString(targets[2] + "," + targets[3]);
        teleport(tessellation.getTile(newViewCenter), tessellation.getTile(newHighlightCenter));
    }
    
    /**
    Moves the center and highlight to the specified tiles.
    If either Tile is null, it will not be updated.
    @param newViewCenter the tile the view will be centered on
    @param newHighlightCenter the tile the highlight will be centered on
    */
    private void teleport(Tile newViewCenter, Tile newHighlightCenter){
        if(newViewCenter == null && newHighlightCenter == null)
            return;
        if(newViewCenter != null)
            centerTile = newViewCenter;
        if(newHighlightCenter != null) {
            highlightCenter = newHighlightCenter;
            updateHighlightAdjacentText();
        }
        updateCenteringText();
        redrawCenter();
    }
    
    /**
    Adds or removes the specified tile from the polyform. If the tile is likely to be onscreen, redraws the tiles.
    @param tile the tile that will be toggled
    */
    private void toggleTile(Tile tile){
        if(tile == null){
            return;
        }
        if(polyform.animal.contains(tile)){
            polyform.animal.remove(tile);
            tile.type = megl.polyforms.Type.EMPTY;
        }                                       //Note: these need the fully qualified name because of java.awt.Window.Type
        else {
            polyform.animal.add(tile);
            tile.type = megl.polyforms.Type.ANIMAL;
        }
        updateTopPanelText();
        if(centerTile.ringNum - 2 <= tile.ringNum || tile.ringNum <= centerTile.ringNum + 2) //these are the rings displayed in the original version, may need to be updated
            redrawCenter();
        else { //these are done in redrawCenter() but the topPanelText needs it too
            revalidate();
            repaint();
        }
    }
    
    /**
    Calls toggleTile(Tile) with the tile given as a String, or does nothing if the Tile is invalid.
    @param tile a string representation of the tile that will be toggled
    */
    private void toggleTile(String tile){
        Tile temp = Tile.parseString(tile);
        if(temp != null)
            toggleTile(tessellation.getTile(temp));
    }
    
    /**
    Processes the text from the text field, toggles the corresponding tile, and clears the field.
    */
    private void toggleTileFromTextField(){
        String text = tileToggler.getText();
        tileToggler.setText("");
        toggleTile(text);
    }
    
    /**
    Opens a dialogue to select a polyform file, then replaces the polyform if a valid file is selected.
    @param mode controls which action will be performed. 0: open polyform, 1: save polyform
    */
    private void choosePolyformFile(char mode){
        JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
        if(mode == '0'){ //open
            if(fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
                replacePolyformAndTessellation(fileChooser.getSelectedFile());
            }
        }
        else if(mode == '1'){ //save
            if(fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION){
                if(!polyform.saveToFile(fileChooser.getSelectedFile(), tessellation)){
                    System.err.println("saving to existing file not permitted");
                }
            }
        }
    }
    
    /**
    Replaces the current polyform and tessellation with the one represented by the file.
    If the file is invalid, nothing happens.
    Valid files are structured with the first line containing "p q rings" for the tessellaton, 
    and all future lines containing ringNum ringPos of a tile that is included.
    */
    private void replacePolyformAndTessellation(File file){
        Tessellation newTess = null;
        Polyform newPoly = null;
        HashSet<Tile> tiles = new HashSet<>();
        try{
            Scanner scanner = new Scanner(file);
            int p = scanner.nextInt();
            int q = scanner.nextInt();
            int rings = scanner.nextInt();
            newTess = new Tessellation(p,q,rings);
            int ringNum;
            int ringPos;
            Tile temp;
            while(scanner.hasNext()){
                ringNum = scanner.nextInt();
                ringPos = scanner.nextInt();
                temp = newTess.getTile(ringNum, ringPos);
                if(temp != null){
                    temp.type = megl.polyforms.Type.ANIMAL;
                    tiles.add(temp);
                }
            }
            scanner.close();
            newPoly = new Polyform(tiles);
        }
        catch(Exception e){
            System.err.println("Invalid file: " + file);
            return;
        }
        tessellation = newTess;
        this.p = tessellation.p;
        this.q = tessellation.q;
        this.rings = tessellation.rings;
        this.centerTile = tessellation.getTile(0,0);
        this.highlightCenter = centerTile;
        polyform = newPoly;
        updateTopPanelText();
        updateCenteringText();
        recalcPolyformStats();
        redrawCenter();
    }
    
    /**
    Replaces the current tessellation with the one specified by p, q, rings.
    This will occur even if the arguments represent the same tessellation.
    Does nothing if given invalid values.
    @param p the p of the new tessellation
    @param q the q of the new tessellation
    @param rings the number of rings that will be generated for the new tessellation
    */
    private void setTessellation(int p, int q, int rings){
        if(p < 3 || q < 3 || rings < 1)
            return;
        tessellation = new Tessellation(p, q, rings);
        polyform = new Polyform(tessellation);
        this.p = p;
        this.q = q;
        this.rings = rings;
        updateTopPanelText();
        recalcPolyformStats();
        teleport(tessellation.getTile(0,0), tessellation.getTile(0,0));
    }
    
    /**
    Replaces the current tessellation with the one specified by the string in the tessellator text field.
    If the field does not have a valid input, nothing will happen.
    */
    private void setTessellation(){
        String[] pieces = tessellator.getText().split("\\D+"); // non-digit one or more times, greedy
        tessellator.setText("");
        
        int newP = -1;
        int newQ = -1;
        int newRings = -1;
        for(String s : pieces){
            if(s.matches("\\A\\d{1,9}?\\z")) // just an integer 0 <= x < 1 billion
                if(newP >= 0){
                    if(newQ >= 0){
                        newRings = Integer.parseInt(s);
                        break;
                    }
                    else
                        newQ = Integer.parseInt(s);
                }
                else
                    newP = Integer.parseInt(s);
        }
        if(newP < 0 || newQ < 0 || newRings < 0)
            return;
        setTessellation(newP, newQ, newRings);
    }
    
    private void toggleMegaTileUsage(){
        usingMegaTiles = !usingMegaTiles;
        redrawCenter();
    }
    
    /**
    Directs all button presses to their proper function.
    If the first character is 0, the event is a view shift button.
    If the first character is 1, the event is toggling a tile
    If the first character is 2, the event is choosing a polyform file
    */
    public void actionPerformed(ActionEvent e){
        String cmd = e.getActionCommand();
        switch(cmd.charAt(0)){
            case '0':
                shiftTiles(cmd.charAt(1), cmd.charAt(2));
                break;
            case '1':
                toggleTileFromTextField();
                break;
            case '2':
                choosePolyformFile(cmd.charAt(1));
                break;
            case '3':
                teleportFromTextField();
                break;
            case '4':
                recalcPolyformStats();
                revalidate();
                repaint();
                break;
            case '5':
                setTessellation();                
                break;
            case '6':
                toggleMegaTileUsage();
                break;
            default:
                System.err.println("invalid action happened");    
        }
    }
    
    /**
    Updates the text of the top panel to reflect the current polyform and tessellation.
    */
    private void updateTopPanelText(){
        topPanelText.setText(String.format("Currently displaying a polyform in {%d,%d} with %d tiles.", 
                                                    p, q, (polyform == null ? 0 : polyform.size())));
        revalidate();
        repaint();
    }
    
    /**
    Updates the list of tiles that are adjacent to the current highlight center
    */
    private void updateHighlightAdjacentText(){
        String text = String.format("<html><body>Tiles adjacent to (%d,%d):<br>", highlightCenter.ringNum, highlightCenter.ringPos);
        String temp = null;
        int lineLength = 0;
        Tile t = null;
        for(int i = 0; i < highlightCenter.adjacent.length; i++){
            t = highlightCenter.adjacent[i];
            if(t == null)
                continue;
            temp = t.toString();
            lineLength += temp.length() + (i + 1 == highlightCenter.adjacent.length ? 0 : 2); //accounts for comma and space
            text += temp + (i + 1 == highlightCenter.adjacent.length ? "" : ", ");
            if(lineLength > 50){
                text += "<br>";
                lineLength = 0;
            }
        }
        text += "</body></html>";
        highlightAdjacentText.setText(text);
    }
    
    /**
    Recomputes assorted stats for the polyform and updates their text appropriately.
    */
    private void recalcPolyformStats(){
        updateHoleStatus();
        updateConnectivityStatus();
    }
    
    /**
    Recomputes where holes are in the polyform and updates the list of them.
    */
    private void updateHoleStatus(){
        ArrayList<HashSet<Tile>> holes = Polyform.getHoles(tessellation, polyform.animal);
        String text = String.format("<html><body>The polyform contains %d holes:<br>", holes.size());
        String temp = null;
        int lineLength = 0;
        int i = 0;
        for(HashSet<Tile> hole : holes){
            temp = hole.toString();
            lineLength += temp.length() + (++i == holes.size() ? 0 : 2);//accounts for comma and space
            text += temp + (i == holes.size() ? "" : "; ");
            if(lineLength > 50){
                text += "<br>";
                lineLength = 0;
            }
        }
        text += "</body></html>";
        polyformStats[0].setText(text);
    }
    
    /**
    Recomputes whether or not the polyform is connected.
    */
    private void updateConnectivityStatus(){
        if(Polyform.isConnected(tessellation, polyform.animal))
            polyformStats[1].setText("Connected: true");
        else
            polyformStats[1].setText("Connected: false");
    }
    
    /**
    Updates the text of the bottom panel to reflect the current view and highlight centers.
    */
    private void updateCenteringText(){
        String temp = String.format("Current Center: (%d, %d)", centerTile.ringNum, centerTile.ringPos);
        for(int i = 0; i < 3; i++){
            currentCenterText[i].setText(temp);
        }
        temp = String.format("Highlight Center: (%d, %d)", highlightCenter.ringNum, highlightCenter.ringPos);
        for(int i = 0; i < 3; i++){
            currentHighlightText[i].setText(temp);
        }
        revalidate();
        repaint();
    }
    
    /**
    Returns a Tile or MegaTile representing every tile from start to end, inclusive.
    If end < start, it will return a representation of the complement of groupTiles(start, end).
    Returns null if start and end are not in the same ring.
    @param start the lowest tile that will be represented
    @param end the highest tile that will be represented
    @returns a Tile or Megatile representing every tile in the given range, or null if invalid.
    */
    private Tile groupTiles(Tile start, Tile end){
        if(start.getRingNum() != end.getRingNum())
            return null;
        if(start.equals(end))
            return start;

        MegaTile ret = new MegaTile(); 
        if(start.getRingPos() > end.getRingPos())
            ret.bigToSmall = true;        
        while(!start.equals(end)){
            ret.addTile(start);
            start = start.getNextInRing();
        }
        ret.addTile(end);
        return ret;
    }
    
    /**
    Creates a display.
    @param args the p, q, and filename of the polyform respectively, or nothing.
    */
    public static void main(String[] args){
        switch(args.length){
            case 4:
                new PolyformDisplayer(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]), args[3]);
                break;
            case 3:
                new PolyformDisplayer(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
                break;
            case 2:
                break;
            default:
                new PolyformDisplayer();
                break;
        }
    }
}

/**
A class for the tiles that are displyed by the program.
*/
class TilePanel extends JButton {
    Tile tile;
    static final Border blackBorder = BorderFactory.createLineBorder(Color.BLACK);
    static final Border blueBorder = BorderFactory.createLineBorder(Color.BLUE, 4);
    
    public TilePanel(Tile tile){
        this(tile, false, false);
    }
    
    public TilePanel(Tile tile, boolean isAnimal, boolean isHighlighted){
        this.tile = tile;
        this.setBorder(isHighlighted ? blueBorder : blackBorder);
        this.setText(findTileText());
        this.setBackground(isAnimal ? Color.RED : Color.LIGHT_GRAY);
    }
    
    private String findTileText(){
        if(tile instanceof MegaTile mega){
                if(!mega.bigToSmall)
                    return String.format("(%d, %d) - (%d, %d)", mega.getFirst().getRingNum(), mega.getFirst().getRingPos(), mega.getLast().getRingNum(), mega.getLast().getRingPos());
                Tile[] extremes = mega.getExtremities();
                return String.format("(%d, %d) - (%d, %d)", extremes[1].getRingNum(), extremes[1].getRingPos(), extremes[0].getRingNum(), extremes[0].getRingPos());
        }
        else
            return String.format("(%d, %d)", tile.getRingNum(), tile.getRingPos());
    }
}