package megl.polyforms;
/**
A Class representing a tile in a tessellation. Contains methods useful for navigating between and identifying tiles.
*/
public class Tile implements Comparable<Tile> {
    /**
    Contains all Tiles that share an edge with this tile (has length p).
    If this tile is primary, adjacent[0] is the tile in the previous ring, and adjacent[p-1] is the previous tile in the same ring.
    If this tile is secondary, adjacent[0] is the previous tile in the same ring.
    In either case, adjacent[1] is the next tile in the same ring.
    The order of these tiles can be clockwise or counterclockwise, as long as you are consistent for every tile in the tessellation.
    */
    Tile[] adjacent;
    
    /**
    Indicates whether this Tile is part of a hole, animal, or neither.
    */
    Type type;
    
    /**
    Indicates which ring this tile is in, where 0 is the innermost ring.
    */
    int ringNum;
    
    /**
    Indicates where in its ring this tile is, where 0 is the first tile.
    The first Tile is always a primary Tile.
    */
    int ringPos;
    
    /**
    Indicates if this tile was created touching a tile in the previous ring (true) or not (false).
    If it is not primary, it is secondary.
    */
    boolean wasPrimary;
    
    /**
    Creates a new tile with all needed information except for adjacencies.
    @param p the number of Tiles that are adjacent to this Tile
    @param type whether this Tile is part of a HOLE, ANIMAL, or EMPTY
    @param ringNum which ring of the tessellation this Tile is in
    @param ringPos where in its ring this Tile is
    @param wasPrimary true if and only if this Tile is adjacent to the previous ring
    */
    public Tile(int p, Type type, int ringNum, int ringPos, boolean wasPrimary){
        this.adjacent = new Tile[p];
        this.type = type;
        this.ringNum = ringNum;
        this.ringPos = ringPos;
        this.wasPrimary = wasPrimary;
    }
    
    /**
    Creates a tile with the minimum needed information to uniquely identify it.
    Primarily useful for hashCode()
    @param ringNum which ring of the tesselation this Tile is in
    @param ringPos where in its ring this Tile is
    */
    public Tile(int ringNum, int ringPos){
        this.ringNum = ringNum;
        this.ringPos = ringPos;
    }
    
    /**
    Returns this tile's type
    @return this tile's type
    */
    public Type getType(){
        return type;
    }
    
    /**
    Returns this tile's ringNum
    @return this tile's ringNum
    */
    public int getRingNum(){
        return ringNum;
    }
    
    /**
    Returns this tile's ringPos
    @return this tile's ringPos
    */
    public int getRingPos(){
        return ringPos;
    }
    
    /**
    Returns true iff this tile was primary
    @return true iff this tile was primary
    */
    public boolean getWasPrimary(){
        return wasPrimary;
    }
    
    /**
    Returns the Tile in the same ring with next highest index, or if none exist, the tile in the same ring with ringPos 0.
    @return the next tile in the same ring
    */
    public Tile getNextInRing(){
        if(ringNum == 0)
            return this;

        return adjacent[1];
    }
    
    /**
    Returns the Tile in the same ring with next lowest index, or if none exist, the tile in the same ring with highest ringPos.
    @return the previous tile in the same ring
    */
    public Tile getPreviousInRing(){
        if(ringNum == 0)
            return this;

        if(wasPrimary)
            return adjacent[adjacent.length-1];
        else
            return adjacent[0];
    }
    
    /**
    Returns the tile in the previous ring that this tile is adjacent to, or throws an exception if none exist.
    @return the adjacent tile in the previous ring
    @throws IllegalStateException when the tile is not adjacent to the previous ring
    */
    public Tile getPreviousRing(){
        if(wasPrimary && ringNum != 0)
            return adjacent[0];
        else
            throw new IllegalStateException("this tile does not connect directly to previous ring");
    }
    
    /**
    Returns all adjacent tiles in the next ring, or an array of null if this Tile is in the last ring.
    @return all adjacent tiles in the next ring
    */
    public Tile[] getNextRing(){
        if(ringNum == 0){ // special case for (0,0), all adjacent are next ring
            Tile[] ret = new Tile[adjacent.length];
            for(int i = 0; i < ret.length; i++)
                ret[i] = adjacent[i];
            return ret;
        }
        
        int max;
        if(wasPrimary)
            max = adjacent.length-3;
        else
            max = adjacent.length-2;
            
        Tile[] nextRing = new Tile[max];
        
        for(int i = 0; i < nextRing.length; i++){
            nextRing[i] = adjacent[i+2];
        }
        
        return nextRing;
    }
    
    /**
    Computes the unique index of this tile in its tessellation.
    If given an invalid array, returns -1 or a wrong number.
    @param ringSizes the number of tiles in each ring of this tessellation. Only entries for lower rings are required
    @return a number unique to this tile for this tesselation
    */
    public int getAbsoluteIndex(int[] ringSizes){
        if(ringSizes.length < ringNum)
            return -1;
        int index = ringPos;
        for(int i = 0; i < ringNum; i++)
            index += ringSizes[i];
        return index;
    }
    
    /**
    Returns true if and only if the argument is a Tile with the same position as this one.
    @return true if and only if the tiles represent the same tile
    */
    @Override
    public boolean equals(Object other){
        if(other instanceof Tile){
            Tile t = (Tile) other;
            return this.ringNum == t.getRingNum() && this.ringPos == t.getRingPos();
        }
        return false;
    }
    
    @Override
    public int hashCode(){
        return String.format("%d%d", ringPos, ringNum).hashCode();
    }
    
    /**
    Creates a String with coordinates for this tile based on ring and position.
    @return a String of form (ringNum,ringPos)
    */
    @Override
    public String toString(){
        return String.format("(%d,%d)", ringNum, ringPos);
        //return String.format("ringNum: %d ringPos: %d", ringNum, ringPos);
    }
    
    /**
    Creates and returns Tile that represents this one's position in its tessellation, but doesn't reach others
    @return a Tile that is equal to this one
    */
    public Tile minimalClone(){
        return new Tile(this.ringPos, this.ringNum);
    }
    
    /**
    Creates and returns a shallow copy of the tile.
    @return a shallow copy of the tile
    */
    public Tile fullClone(){
        Tile temp = minimalClone();
        temp.adjacent = this.adjacent;
        temp.wasPrimary = this.wasPrimary;
        temp.type = this.type;
        return temp;
    }
    
    /**
    Compares with another tile. A tile is less than another tile if the other tile 
    has a higher ringNum or the same ringNum and a higher ringPos.
    @param o the other Tile
    @return -1 if this tile is less than the argument, 0 if they are equal, and 1 if this tile is greater than the argument.
    */
    @Override
    public int compareTo(Tile o){
        if(this.ringNum < o.ringNum)
            return -1;
        else if(this.ringNum > o.ringNum)
            return 1;
        else if(this.ringPos == o.ringPos)
            return 0;
        else return (this.ringPos < o.ringPos ? -1 : 1); 
    }
    
    /**
    Creates a tile with the coordinates represented in the String
    @param tile the String representation of the Tile
    @returns the Tile represented by the string, or null if it is invalid.
    */
    public static Tile parseString(String tile){
        String[] pieces = tile.split("\\D+");
        int ringNum = -1;
        int ringPos = -1;
        for(String s : pieces){
            if(s.matches("\\A\\d{1,9}?\\z")) // just an integer 0 <= x < 1 billion
                if(ringNum >= 0){
                    ringPos = Integer.parseInt(s);
                    break;
                }
                else
                    ringNum = Integer.parseInt(s);
        }
        if(ringNum < 0 || ringPos < 0)
            return null;
        return new Tile(ringNum, ringPos);
    }
    
    /**
    Returns a copy of the adjacent array.
    @return a shallow copy of the adjacent array.
    */
    public Tile[] getAdjacent(){
        Tile[] ret = new Tile[adjacent.length];
        for(int i = 0; i < adjacent.length; i++)
            ret[i] = adjacent[i];
        return ret;
    }
    
    /**
    Returns true if and only if this tile has the given coordinate.
    @param ringNum the ringNum that will be checked against
    @param ringPos the ringPos that will be checked against
    @return true iff this tile matches the given coordinates.
    */
    public boolean is(int ringNum, int ringPos){
        return this.ringNum == ringNum && this.ringPos == ringPos;
    }
    
    /**
    Returns true if and only if the given tile is adjacent to this one (shares an edge with).
    If given null, returns false.
    @param other the tile whose adjacency is checked
    @return true if and only if the tiles are neighbors
    */
    public boolean isNeighbor(Tile other){
        if(other == null)
            return false;
        for(Tile t : adjacent)
            if(other.equals(t))
                return true;
        return false;
    }
}