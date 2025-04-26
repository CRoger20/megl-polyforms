package megl.polyforms;

import java.util.ArrayList;
import java.util.Stack;
import java.util.HashSet;

/**
A Class representing some number of rings of tiles in the {p,q} tessellation.
It does not ensure that the given values correspond to the hyperbolic plane, 
but it should only be relied upon for hyperbolic tessellations.
*/
public class Tessellation {
    /**
    Specifies how many tiles share an edge with any given tile. 
    Some will be null if and only if the tile is on the edge of the tessellation.
    */
    int p;
    
    /**
    Specifies how many Tiles have an edge that connects to any given vertex.
    If the vertex is on the edge of the tessellation, some of these tiles will be null.
    */
    int q;
    
    /**
    Specifies how many rings of tiles are in the tesselation. Does not include the center tile (ring 0).
    Ring(0) is one tile, Ring(1) is all tiles that share a vertex with Ring(0), 
    and Ring(n) is all tiles that share a vertex with a tile in Ring(n-1) but not Ring(n-2).
    */
    int rings;
    
    /**
    Contains how many tiles are in each ring.
    ringSizes[0] is always 1, but all greater indicies depend on p and q.
    */
    int[] ringSizes;
    
    /**
    The list of tiles in increasing order. All tiles with a given ringNum are before all tiles with a higher ringNum,
    and all Tiles with the same ringNum are in ascending ringPos order.
    */
    ArrayList<Tile> tessellation;
    
    /**
    Specifies the parameters of the tessellation. Requires p and q to be greater than 3 due to edge cases, and at least a second ring.
    @param p the number of Tiles that share an edge with any given Tile
    @param q the number of Tiles that share any given vertex
    @param rings the number of rings that will be generated
    */
    public Tessellation(int p, int q, int rings){
        if(p < 3 || q < 3 || rings < 1)
            throw new IllegalArgumentException("This case is not covered. Required: p >= 3, q => 3, rings > 0.");
        this.p = p;
        this.q = q;
        this.rings = rings;
        this.ringSizes = new int[rings+1];
        ringSizes[0] = 1;
        ringSizes[1] = p*q -(2*p); // p + p*(q-3)
        tessellation = makeTessellation();
    }
    
    /**
    Creates a list of tiles that represent a {p,q} tessellation. Tiles will have Type EMPTY and all correct adjacencies.
    Will only create tiles within the specified number of rings.
    @return a list, in order, of every Tile in the tessellation, up to the specified ring.
    */
    public ArrayList<Tile> makeTessellation(){
        ArrayList<Tile> tiles = new ArrayList<Tile>();
        ArrayList<Tile> currentRing = new ArrayList<Tile>();
        
        Tile previous = new Tile(p, Type.EMPTY, 0, 0, true); // the tile in ring 0
        tiles.add(previous);
        
        makeRingOne(tiles, currentRing, previous); // ring 1
        
        ArrayList<Tile> previousRing;
        for(int ring = 2; ring <= rings; ring++){ // rings >= 2
            previousRing = currentRing;
            currentRing = new ArrayList<Tile>();
            ringSizes[ring] = makeNextRing(tiles, currentRing, previousRing, ring);
        }
        return tiles;
    }
    
    /**
    Helper for makeTessellation(). Creates the second ring, ring 1.
    @param tiles the list of tiles in the tessellation
    @param currentRing the list of tiles that are in ring 1
    @param previous the tile in ring 0
    */
    private void makeRingOne(ArrayList<Tile> tiles, ArrayList<Tile> currentRing, Tile previous){
        Tile current = null;
        for(int i = 0; i < p; i++){
            current = new Tile(p, Type.EMPTY, 1, i*(q-2), true); // new primary tile
            previous.adjacent[i] = current; // link from previous ring to this tile
            current.adjacent[0] = previous; // link from this tile to previous ring
            currentRing.add(current);
            tiles.add(current);
            if(i != 0 ){ // link between this tile and last secondary tile of previous primary tile
                           // if q=3, links between this tile and the previous primary tile
                current.adjacent[p-1] = currentRing.get(i*(q-2)-1); 
                current.adjacent[p-1].adjacent[1] = current;
            }
            for(int j = 1; j <= q-3; j++){ // make secondary tiles between this and next primary
                current = new Tile(p, Type.EMPTY, 1, i*(q-2)+j, false); // new secondary tile
                current.adjacent[0] = currentRing.get(i*(q-2)+j-1); // link to previous tile in ring
                current.adjacent[0].adjacent[1] = current; // link from previous tile in ring to this tile
                currentRing.add(current);
                tiles.add(current);
                if(i == p-1 && j == q-3){ // last tile of this ring
                    current.adjacent[1] = currentRing.get(0); // link to first tile of this ring
                    current.adjacent[1].adjacent[p-1] = current; // link from first tile of this ring to last tile of this ring
                }
            }
            
        }
        
        if(q == 3){ // link between first and last primary tile of ring when q=3
            if(current == null) throw new IllegalStateException("Should never happen, only initialized to null to stop compile error");
            current.adjacent[1] = currentRing.get(0);
            current.adjacent[1].adjacent[p-1] = current;
        }            
    }
    
    /**
    Helper for makeTessellation(). Creates the next ring of tiles after at least two rings have been made.
    @param tiles the Tiles of the tessellation
    @param currentRing the Tiles in the ring being created
    @param previousRing the Tiles that are in the previous ring
    @ring the number of the ring being generated
    @return the number of tiles that were generated for this ring
    */
    private int makeNextRing(ArrayList<Tile> tiles, ArrayList<Tile> currentRing, ArrayList<Tile> previousRing, int ring){
        if(p == 3)
            return pThreeTessellation(tiles, currentRing, previousRing, ring);
        if(q == 3)
            return qThreeTessellation(tiles, currentRing, previousRing, ring);

        Tile current = null, previous;
        int ringPos = 0;
        for(int n = 0; n < previousRing.size(); n++){
            previous = previousRing.get(n);
            for(int i = 2; i < p; i++){
                if(i == p-1 && previous.wasPrimary)
                    break; // this tile is accounted for by link to previous ring
                current = new Tile(p, Type.EMPTY, ring, ringPos, true); // new primary tile
                previous.adjacent[i] = current; // link from previous ring to this tile
                current.adjacent[0] = previous; // link to this tile from previous ring
                if(n != 0 || i != 2){ // link the last secondary tile of the previous primary tile in this ring if it exists yet
                    current.adjacent[p-1] = currentRing.get(ringPos-1);
                    currentRing.get(ringPos-1).adjacent[1] = current;
                }
                currentRing.add(current);
                tiles.add(current);
                ringPos++;
                for(int j = 1; j <= q-3; j++){ // make this primary tile's secondary tiles
                    if(j == q-3 && ((previous.wasPrimary && i == p-2) || (!previous.wasPrimary && i == p-1)))
                        break; //this tile will be accounted for by the next primary tile of the next tile
                    current = new Tile(p, Type.EMPTY, ring, ringPos, false); // new secondary tile
                    current.adjacent[0] = currentRing.get(ringPos-1); // link to previous tile in ring
                    current.adjacent[0].adjacent[1] = current; // link from previous tile in ring to this tile
                    currentRing.add(current);
                    tiles.add(current);
                    ringPos++;
                }
            }
        }
        current.adjacent[1] = currentRing.get(0); // link to first tile of ring from last
        current.adjacent[1].adjacent[p-1] = current; // link from first tile of this ring to lst tile of this ring
        return ringPos;
    }
    
    /**
    Helper for makeNextRing(). Creates the next ring in cases where p = 3.
    @param tiles the Tiles of the tessellation
    @param currentRing the Tiles in the ring being created
    @param previousRing the Tiles that are in the previous ring
    @ring the number of the ring being generated
    @return the number of tiles that were generated for this ring
    */
    private int pThreeTessellation(ArrayList<Tile> tiles, ArrayList<Tile> currentRing, ArrayList<Tile> previousRing, int ring){
        Tile current = null;
        Tile previous;
        int ringPos = 0;
        for(int n = 0; n < previousRing.size(); n++){
            previous = previousRing.get(n);
            if(previous.wasPrimary)
                continue; // it already has 3 adjacent tiles
            
            current = new Tile(p, Type.EMPTY, ring, ringPos, true); // new primary tile
            previous.adjacent[2] = current; // link from previous ring to this tile
            current.adjacent[0] = previous; // link to this tile from previous ring

            if(ringPos != 0){ // link the last secondary tile of the previous primary tile in this ring if it exists yet
                current.adjacent[2] = currentRing.get(ringPos-1);
                currentRing.get(ringPos-1).adjacent[1] = current;
            }
            
            currentRing.add(current);
            tiles.add(current);
            ringPos++;
            
            for(int i = 0; i < q-4; i++){
                if(i == q-5 && previousRing.get(n).getNextInRing().wasPrimary)
                    break; // this tile is actually the next primary tile
                
                current = new Tile(p, Type.EMPTY, ring, ringPos, false); // new secondary Tile
                current.adjacent[0] = currentRing.get(ringPos-1); // link to previous tile in ring
                current.adjacent[0].adjacent[1] = current; // link from previous tile in ring to this tile
                
                currentRing.add(current);
                tiles.add(current);
                ringPos++;

            }
        }
        
        if(current == null) throw new IllegalStateException("Should never happen, only initialized to null to stop compile error");
        current.adjacent[1] = currentRing.get(0); //link first and last tiles of the ring
        currentRing.get(0).adjacent[2] = current;
        
        return ringPos;
    }
    
    /**
    Helper for makeNextRing(). Creates the next ring in cases where q = 3.
    @param tiles the Tiles of the tessellation
    @param currentRing the Tiles in the ring being created
    @param previousRing the Tiles that are in the previous ring
    @ring the number of the ring being generated
    @return the number of tiles that were generated for this ring
    */
    private int qThreeTessellation(ArrayList<Tile> tiles, ArrayList<Tile> currentRing, ArrayList<Tile> previousRing, int ring){
        Tile current = null;
        Tile previous;
        int ringPos = 0;
        for(int n = 0; n < previousRing.size(); n++){
            previous = previousRing.get(n);
            
            if(n != 0){ // link between most recently created secondary tile and other tile in previous ring
                previous.adjacent[(previous.wasPrimary? p-2 : p-3)] = current;
                current.adjacent[p-2] = previous;
            }
            
            for(int i = 2; i < (previous.wasPrimary ? p-3 : p-4); i++){
                current = new QThreeTile(p, Type.EMPTY, ring, ringPos, true); // new primary tile
                previous.adjacent[i] = current; // link from previous ring to this tile
                current.adjacent[0] = previous; // link to this tile from previous ring
                
                if(ringPos != 0){
                    current.adjacent[p-1] = currentRing.get(ringPos-1); // link to previous tile in ring
                    current.adjacent[p-1].adjacent[1] = current; // link from previous tile in ring
                }
                
                currentRing.add(current);
                tiles.add(current);
                ringPos++;
            }
            
            current = new QThreeTile(p, Type.EMPTY, ring, ringPos, false); // new secondary tile
            previous.adjacent[(previous.wasPrimary ? p-3 : p-4)] = current; // link from previous ring
            current.adjacent[0] = previous; // link to previous ring
            
            current.adjacent[p-1] = currentRing.get(ringPos-1); // link to previous tile in ring
            current.adjacent[p-1].adjacent[1] = current; // link from previous tile in ring
            
            currentRing.add(current);
            tiles.add(current);
            ringPos++;
        }
        
        if(current == null) throw new IllegalStateException("Should never happen, only initialized to null to stop compile error");
        
        current.adjacent[1] = currentRing.get(0); // link to first tile
        current.adjacent[1].adjacent[p-1] = current; // link from first tile
        
        previousRing.get(0).adjacent[(previousRing.get(0).wasPrimary? p-2 : p-3)] = current;
        current.adjacent[p-2] = previousRing.get(0);
        
        return ringPos;
    }
    
    /**
    Returns how many tiles are in the specified ring, or -1 if its out of bounds.
    @param ring the ring whose size will be returned
    @return the number of tiles in the ring or -1 if the given ring was not made 
    */
    public int sizeOfRing(int ring){
        if(ring < 0 || ring >= ringSizes.length)
            return -1;
        return ringSizes[ring];
    }
    
    /**
    Returns the tile at the given coordinate, or null if its out of bounds.
    @param ringNum the ring of the tile that will be returned
    @param ringPos the position of the tile that will be returned
    @return the tile at the given coordinate
    */
    public Tile getTile(int ringNum, int ringPos){
        if(ringNum >= 0 && ringNum <= rings)
            if(ringPos >= 0 && ringPos < ringSizes[ringNum])
                return tessellation.get((new Tile(ringNum, ringPos)).getAbsoluteIndex(ringSizes));
        return null;
    }
    
    /**
    Returns the tile in this tessellation that matches the given tile's coordinates.
    If no such tile exists, returns null.
    @param the tile that matches the one that will be returned
    @returns a tile that matches the one that will be returned
    */
    public Tile getTile(Tile tile){
        if(tile == null)
            return null;
        return getTile(tile.ringNum, tile.ringPos);
    }
    
    /**
    Standard accessor for the list of tiles.
    @return the array list of tiles.
    */
    public ArrayList<Tile> getTessellation(){
        return this.tessellation;
    }
}