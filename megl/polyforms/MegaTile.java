package megl.polyforms;

import java.util.TreeSet;

/**
A class representing an arbitrary number of Tiles.
If regular Tile methods are used, behaves like the lowest or highest Tile added to it.
Designed for use in PolyformDisplayer, but not necessarily limited to it.
*/
public class MegaTile extends Tile {
    /**
    Contains every tile that is part of the MegaTile.
    */
    TreeSet<Tile> tiles;
    
    /**
    If set to true, indicates that this MegaTile is intended to represent a grouping of tiles
    in one ring that wraps around from the highest ringPos to the lowest at some point.
    Intended to help with PolyformDisplayer TilePanel naming.
    */
    boolean bigToSmall = false;
    
    /**
    Creates a MegaTile without any Tiles in it.
    Intended to be used by immediately having tiles added to it, before any are accessed.
    */
    public MegaTile(){
        super(-1, -1);
        tiles = new TreeSet<>();
    }
    
    /**
    Creates a MegaTile with one Tile in it.
    */
    public MegaTile(Tile tile){
        super(tile.getRingNum(), tile.getRingPos());
        tiles = new TreeSet<>();
        if(tile == null)
            return;
        tiles.add(tile);
        wasPrimary = tile.wasPrimary;
    }
    
    /**
    Creates a MegaTile with all the tiles in the ring between first and last. 
    Will include tiles in the increasing ringPos direction from first until last is reached.
    */
    public MegaTile(Tile first, Tile last){
        this(first);
        if(first.getRingNum() != last.getRingNum())
            return;
        Tile next = first.getNextInRing();
        while(!next.equals(last)){
            tiles.add(next);
            next = next.getNextInRing();
        }
    }
    
    /**
    Returns the Type of the lowest Tile in the MegaTile, or EMPTY if none exist.
    @return the Type of the lowest Tile
    */
    @Override
    public Type getType(){
        if(tiles.isEmpty())
            return Type.EMPTY;
        return tiles.first().getType();
    }
    
    /**
    Returns the ringNum of the lowest tile in the MegaTile, or -1 if none exist.
    @return the ringNum of the lowest Tile
    */
    @Override
    public int getRingNum(){
        if(tiles.isEmpty())
            return -1;
        return tiles.first().getRingNum();
    }
    
    /**
    Returns the ringPos of the lowest tile in the MegaTile, or -1 if none exist.
    @return the ringPos of the lowest Tile
    */
    @Override
    public int getRingPos(){
        if(tiles.isEmpty())
            return -1;
        return tiles.first().getRingPos();
    }
    
    /**
    Returns the primary status of the lowest tile in the MegaTile, or false if none exist.
    @return the primary status of the lowest Tile
    */
    @Override
    public boolean getWasPrimary(){
        if(tiles.isEmpty())
            return false;
        return tiles.first().getWasPrimary();
    }
    
    /**
    Returns the nextInRing of the highest Tile in the MegaTile, or null if none exist.
    @return the nextInRing of the highest Tile
    */
    @Override
    public Tile getNextInRing(){
        if(tiles.isEmpty())
            return null;
        return tiles.last().getNextInRing();
    }
    
    /**
    Returns the previousInRing of the lowest Tile in the MegaTile, or null if none exist.
    @return the previousInRing of the lowest Tile
    */
    @Override
    public Tile getPreviousInRing(){
        if(tiles.isEmpty())
            return null;
        return tiles.first().getPreviousInRing();
    }
    
    /**
    Returns the previousRing of the lowest Tile in the MegaTile, or null if none exist.
    @return the previousRing of the lowest Tile
    */
    @Override
    public Tile getPreviousRing(){
        if(tiles.isEmpty())
            return null;
        return tiles.first().getPreviousRing();
    }
    
    /**
    Returns the nextRing of the highest Tile in the MegaTile, or null if none exist.
    @return the nextRing of the highest Tile in the MegaTile, or null if none exist.
    */
    @Override
    public Tile[] getNextRing(){
        if(tiles.isEmpty())
            return null;
        return tiles.last().getNextRing();
    }
    
    /**
    Returns the absoluteIndex of the lowest Tile in the MegaTile, or -1 if none exist.
    @param ringSizes the number of tiles in each ring
    @return the absoluteIndex of the lowest Tile
    */
    @Override
    public int getAbsoluteIndex(int[] ringSizes){
        if(tiles.isEmpty())
            return -1;
        return tiles.first().getAbsoluteIndex(ringSizes);
    }
    
    @Override
    public boolean equals(Object other){
        if(other instanceof MegaTile t){
            return this.tiles == t.tiles;
        }
        else if(other instanceof Tile && this.tiles.size() == 1)
            return other.equals(this);
        return false;
        
    }
    
    @Override
    public int hashCode(){
        return tiles.hashCode();
    }
    
    @Override
    public String toString(){
        return tiles.toString();
    }
    
    @Override
    public Tile minimalClone(){
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Tile fullClone(){
        throw new UnsupportedOperationException();    
    }
    
    @Override
    public int compareTo(Tile o){
        throw new UnsupportedOperationException();
    }
    
    /*****************************************
    Above: Overridden Tile methods that certainly exit
    Below: Potential additions to the Tile methods
    *****************************************/
    
    /**
    Checks if the MegaTile contains any Tiles with the given coordinate.
    @param ringNum the ringNum that will be checked for
    @param ringPos the ringPos that will be checked for
    @return true if and only if at least one Tile in the MegaTile has the given coordinates
    */
    @Override
    public boolean is(int ringNum, int ringPos){
        for(Tile t : tiles)
            if(t.is(ringNum, ringPos))
                return true;
        return false;
    }
    
    /**
    Checks if the MegaTile contains any Tiles adjacent to the given one.
    @param other the Tile whose adjacency will be checked
    @return true if and only if the given Tile is adjacent to a tile in the mega tile
    */
    @Override
    public boolean isNeighbor(Tile other){
        for(Tile t : tiles)
            if(t.isNeighbor(other))
                return true;
        return false;
    }
    
    /*****************************************
    Above: Overrideden Tile methods
    Below: MegaTile exclusive methods
    *****************************************/
    
    /**
    Returns the first tile present in the MegaTile, or null if none exist.
    @return the lowest element in the MegaTile
    */
    public Tile getFirst(){
        if(tiles.size() == 0)
            return null;
        return tiles.first();
    }
    
    /**
    Returns the last tile present in the MegaTile, or null if none exist.
    @return the highest element in the MegaTile
    */
    public Tile getLast(){
        if(tiles.size() == 0)
            return null;
        return tiles.last();
    }
    
    /**
    Adds the given tile to the MegaTile.
    Functions the same as add(E e) in TreeSet.
    */
    public boolean addTile(Tile tile){
        return tiles.add(tile);
    }
    
    /**
    Returns an array containing every Tile that is part of the MegaTile.
    */
    public Tile[] getAll(){
        return tiles.toArray(new Tile[tiles.size()]);
    }
    
    /**
    numHoles?
    */
    
    /**
    Returns the number of tiles that are in the MegaTile, regardless of their Type.
    @return the size of the MegaTile.
    */
    public int numTiles(){
        return tiles.size();
    }
    
    /**
    Assumes the MegaTile represents one continuous link of tiles in one ring, then finds
    the two that are on opposite ends. Relies on bigToSmall being accurately set by the caller.
    @return an array with two tiles on opposite ends of a chain in one ring, or null if something invalid is found.
    */
    public Tile[] getExtremities(){
        Tile[] list = getAll();
        if(!bigToSmall)
            return new Tile[] {tiles.first(), tiles.last()};
        
        int ringPosA;
        int ringPosB;
        try{
            ringPosB = list[0].getRingPos();
            ringPosA = list[1].getRingPos();
            int i = 1;
            while(true){
                if(ringPosA != ringPosB +1){
                    return new Tile[] {list[i-1], list[i]};
                }
                i++;
                ringPosB = ringPosA;
                ringPosA = list[i].getRingPos();
            }
        }
        catch(Exception e){
            return null;
        }
    }
}