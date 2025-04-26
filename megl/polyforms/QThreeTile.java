package megl.polyforms;

/**
An extension of Tile for Tessellations with q = 3.
Because every Tile connects to the previous ring once or twice, adjacent[] and wasPrimary have different meanings.
wasPrimary is now true if and only if the Tile connects to the previous ring exactly once (and false if it connects twice).
adjacent[]'s conventions are as follows:
    Primary Tiles are mostly unchanged. adjacent[0] is the tile from the previous ring, 
        adjacent[2 through p-4] are their primary tiles, adjacent[p-3] is "their" secondary tile, 
        adjacent[p-2] is "their neighbor's" secondary Tile.
    Secondary Tiles' adjacent[0] is now the Tile from the previous ring with lower ringPos, except for 
        the last secondary Tile of the ring, which has the Tile from the previous ring with higher ringPos.
        This makes them "belong" to the Tile they were generated from consistently. For example, the 
        first secondary Tile belongs to the first primary Tile of the previous ring, the second to the second, the last to the last.
    Secondary Tiles' adjacent[2 through p-5] are their primary tiles, adjacent[p-4] is "their" secondary tile, 
        adjacent[p-3] is "their neighbor's" secondary tile, adjacent[p-2] is the other Tile from the previous ring.
    In both cases, adjacent[1] is the next Tile in the same ring, and adjacent[p-1] is the previous Tile in the same ring.
*/

// TODO: test every method in this class

public class QThreeTile extends Tile {
    /**
    Same as Tile.
    @param p the number of Tiles that are adjacent to this Tile
    @param type whether this Tile is part of a HOLE, ANIMAL, or EMPTY
    @param ringNum which ring of the tessellation this Tile is in
    @param ringPos where in its ring this Tile is
    @param wasPrimary true if and only if this Tile is adjacent to the previous ring
    */
    public QThreeTile(int p, Type type, int ringNum, int ringPos, boolean wasPrimary){
        super(p, type, ringNum, ringPos, wasPrimary);
    }
    
    /**
    Same as Tile.
    @param ringNum which ring of the tesselation this Tile is in
    @param ringPos where in its ring this Tile is
    */
    public QThreeTile(int ringNum, int ringPos){
        super(ringNum, ringPos);
    }
    
    /**
    Returns the Tile in the same ring with next lowest index, or if none exist, the tile in the same ring with highest ringPos.
    @return the previous tile in the same ring
    */
    @Override
    public Tile getPreviousInRing(){
        return adjacent[adjacent.length-1];
    }
    
    /**
    Returns the Tile from the previous ring that this Tile "belongs" to, see class level documentation for details.
    @return the adjacent tile in the previous ring that this Tile "belongs" to
    */
    @Override
    public Tile getPreviousRing(){
        return adjacent[0];
    }
    
    /**
    Returns the Tile from the previous ring that this Tile does not "belong" to, see class level documentation for details.
    @return the adjacent tile in the prevous ring that this Tile does not "belong" to
    */
    public Tile getPreviousRingOther(){
        if(wasPrimary)
            throw new IllegalStateException("This tile connects to the previous ring only once.");
        else
            return adjacent[adjacent.length-2];
    }
    
    /**
    Returns every Tile in the next ring that is adjacent to this one, or an array of nulls if this is in the last ring.
    The first tile in the array will be this tile's neighbor's secondary tile, the last will be this tile's secondary tile.
    Note that this means the tiles will always be in ascending ringPos order except when this is the first tile in its ring,
        which will cause the first to be the last tile of its ring
    @return all adjacent tiles in the next ring
    */
    @Override
    public Tile[] getNextRing(){
        Tile[] nextRing = new QThreeTile[(wasPrimary ? adjacent.length-3 : adjacent.length-4)];
        nextRing[0] = getSecondaryOther();
        nextRing[nextRing.length-1] = getSecondary();
        for(int i = 1; i < nextRing.length-1; i++){
            nextRing[i] = adjacent[i+1];
        }
        return nextRing;
    }
    
    /**
    Returns the secondary Tile in the next ring that "belongs" to this one, see class level documentation for details.
    @return the secondary Tile in the next ring that "belongs" to this one, or null if it doesn't exist
    */
    public Tile getSecondary(){
        if(wasPrimary)
            return adjacent[adjacent.length-3];
        else
            return adjacent[adjacent.length-4];
    }
    
    /**
    Returns the adjacent secondary Tile in the next ring that does not "belong" to this one, see class level documentation for details.
    @return the adjacent secondary Tile in the next ring that does not "belong" to this one, or null if it doesn't exist
    */
    public Tile getSecondaryOther(){
        if(wasPrimary)
            return adjacent[adjacent.length-2];
        else
            return adjacent[adjacent.length-3];
    }
}