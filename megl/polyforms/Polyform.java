package megl.polyforms;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Stack;
import java.util.Date;
import java.util.Scanner;
import java.io.PrintWriter;
import java.io.File;


/**
Contains static methods useful for testing polyforms and some test code.
*/
public class Polyform{
    /**
    The tessellation that an instance of Polyform is in.
    */
    Tessellation plane;
    
    /**
    The number of intended animal tiles in this Polyform.
    */
    int intendedTiles;
    
    /**
    The set of tiles that compose the animal that this polyform represents.
    */
    HashSet<Tile> animal;

    /**
    Creates the structure needed to use a polyform.
    */
    public Polyform(Tessellation plane, int intendedTiles){
        if(intendedTiles < 1)
            throw new IllegalArgumentException("can't have polyform with no tiles");
        this.plane = plane;
        this.intendedTiles = intendedTiles;
        this.animal = new HashSet<Tile>();
    }
    
    /**
    Creates the structure needed to use a polyform.
    */
    public Polyform(Tessellation plane){
        this(plane, 1);
    }
    
    /**
    Creates a polyform that stores only its set of animal tiles.
    */
    public Polyform(HashSet<Tile> animal){
        this.animal = animal;
    }

    /**
    Returns true if and only if every Tile in the tessellation with Type ANIMAL 
    can be reached from the tile with Type animal that has lowest index.
    @param tiles the Tiles composing the tessellation
    @return true if and only if the polyform is connected, including if the polyform is empty
    */
    public static boolean isConnected(ArrayList<Tile> tiles){
        int expected = 0;
        Tile startPoint = tiles.get(0);
        for(int i = tiles.size()-1; i >= 0; i--)
            if(tiles.get(i).type == Type.ANIMAL){
                expected++;
                startPoint = tiles.get(i);
            }
        if(startPoint.type != Type.ANIMAL)
            return true;
            
        HashSet<Tile> visited = new HashSet<Tile>(expected);
        Stack<Tile> unvisited = new Stack<Tile>();
        unvisited.push(startPoint);
        Tile tile;
        while(!unvisited.empty()){
            tile = unvisited.pop();
            if(visited.contains(tile))
                continue;
            visited.add(tile);
            for(int i = 0; i < tile.adjacent.length; i++)
                if(tile.adjacent[i] != null && tile.adjacent[i].type == Type.ANIMAL)
                    unvisited.push(tile.adjacent[i]);
        }
        
        return expected == visited.size();
    }
    
    /**
    Returns true if and only if every Tile in the tessellation with Type ANIMAL 
    can be reached from the tile with Type animal that has lowest index.
    @param plane the tessellation that is being searched
    @param animal the tiles in the tessellation that are being checked for connectivity
    @return true if and only if the polyform is connected, including if the polyform is empty
     no longer throws IllegalArgumentException if the tessellation has no Tiles with Type ANIMAL
    */
    public static boolean isConnected(Tessellation plane, HashSet<Tile> animal){
        Type[] preState = new Type[plane.tessellation.size()];
        Tile temp;
        for(int i = 0; i < preState.length; i++){
            temp = plane.tessellation.get(i);
            preState[i] = temp.type;
            if(animal.contains(temp))
                temp.type = Type.ANIMAL;
        }
        
        boolean isConnected = isConnected(plane.tessellation);
        
        for(int i = 0; i < preState.length; i++){
            plane.tessellation.get(i).type = preState[i];
        }
        
        return isConnected;
    }
    
    /**
    Returns an arraylist containing hashsets representing holes. Each element in the arraylist is a distinct hole.
    @param tiles the Tiles composing the Tessellation
    @return the collection of holes in the polyform
    */
    public static ArrayList<HashSet<Tile>> getHoles(ArrayList<Tile> tiles){
        ArrayList<HashSet<Tile>> holes = new ArrayList<HashSet<Tile>>();
        Stack<Tile> unvisited;
        HashSet<Tile> currentHole;
        Tile tile;
        boolean alreadyFound;
        boolean falseHole;
        
        for(int i = tiles.size()-1; i >= 0; i--){
            tile = tiles.get(i);
            alreadyFound = false;
            if(tile.type == Type.HOLE || tile.type == Type.EMPTY){
                for(int j = 0; j < holes.size(); j++){
                    if(holes.get(j).contains(tile)){
                        alreadyFound = true;
                        break;
                    }
                }
                if(!alreadyFound){
                    falseHole = false;
                    currentHole = new HashSet<Tile>();
                    unvisited = new Stack<Tile>();
                    unvisited.push(tile);
                    while(!unvisited.empty() && !falseHole){
                        tile = unvisited.pop();
                        if(currentHole.contains(tile))
                            continue;
                        currentHole.add(tile);
                        for(int j = 0; j < tile.adjacent.length; j++){
                            if(tile.adjacent[j] == null){
                                falseHole = true;
                                break;
                            }
                            if(tile.adjacent[j].type == Type.HOLE || tile.adjacent[j].type == Type.EMPTY)
                                unvisited.push(tile.adjacent[j]);
                        }
                    }
                    if(!falseHole)
                        holes.add(currentHole);
                }
            }
        }
        
        return holes;
    }
    
    /**
    Returns an arraylist containing hashsets representing holes. Each element in the arraylist is a distinct hole.
    @param plane the tessellation that is being examined
    @param animal the Tiles composing the tessellation
    @return the collection of holes in the polyform
    */
    @SuppressWarnings("unchecked")
    public static ArrayList<HashSet<Tile>> getHoles(Tessellation plane, HashSet<Tile> animal){
        if(animal == null)
            return null;
        Type[] preState = new Type[plane.tessellation.size()];
        Tile temp;
        for(int i = 0; i < preState.length; i++){
            temp = plane.tessellation.get(i);
            preState[i] = temp.type;
            if(animal.contains(temp))
                temp.type = Type.ANIMAL;
            else
                temp.type = Type.EMPTY;
        }
        
        ArrayList<HashSet<Tile>> holes = getHoles(plane.tessellation);
        
        for(int i = 0; i < preState.length; i++){
            plane.tessellation.get(i).type = preState[i];
        }
        
        return holes;
    }

    
    /**
    Counts how many unique holes are in the given polyform. A hole is only counted if each Tile that is part of it
    is surrounded entirely by Tiles that have Type HOLE or ANIMAL. If any Tiles are EMPTY or null, they are excluded.
    @param tiles the Tiles composing the tessellation
    @return the number of holes in the polyform
    */
    public static int numHoles(ArrayList<Tile> tiles){
        return getHoles(tiles).size();
    }
    
    /**
    Counts how many unique holes are in the given polyform. A hole is only counted if each Tile that is part of it
    is surrounded entirely by Tiles that have Type HOLE or ANIMAL. If any Tiles are EMPTY or null, they are excluded.
    @param plane the tessellation that is being examined
    @param animal the Tiles composing the tessellation
    @return the number of holes in the polyform
    */
    @SuppressWarnings("unchecked")
    public static int numHoles(Tessellation plane, HashSet<Tile> animal){
        if(animal == null)
            return 0;
        Type[] preState = new Type[plane.tessellation.size()];
        Tile temp;
        for(int i = 0; i < preState.length; i++){
            temp = plane.tessellation.get(i);
            preState[i] = temp.type;
            if(animal.contains(temp))
                temp.type = Type.ANIMAL;
            else
                temp.type = Type.EMPTY;
        }
        
        int holes = numHoles(plane.tessellation);
        
        for(int i = 0; i < preState.length; i++){
            plane.tessellation.get(i).type = preState[i];
        }
        
        return holes;
    }
    
    
        
    /**
    Two polyforms are equal if their sets of animal tiles are equal.
    @param other the other Polyform
    @return true if and only if the polyforms have the same animal tiles.
    */
    @Override
    public boolean equals(Object other){
        if(other instanceof Polyform){
            Polyform p = (Polyform) other;
            if(this.animal.size() != p.animal.size())
                return false;
            Object[] tiles = this.animal.toArray();
            Tile temp;
            for(int i = 0; i < tiles.length; i++){
                temp = (Tile) tiles[i];
                if(!p.animal.contains(temp))
                    return false;
            }
            return true;
        }
        return false;
    }
    
    @Override
    public int hashCode(){
        int code = 0;
        Tile[] tiles = new Tile[animal.size()];
        tiles = animal.toArray(tiles);
        
        for(int i = 0; i < tiles.length; i++){
            code += Integer.parseInt(String.format("%d%d", tiles[i].ringNum, tiles[i].ringPos));
        }
        
        return code;
    }
    
    /**
    Returns the number of tiles that are part of the polyform.
    @return the number of tiles that are in the polyform
    */
    public int size(){
        return (animal == null ? 0 : animal.size());
    }
    
    /**
    Creates a Polyform represented by the given file. The file should be formatted as one tile per line, 
    each line containing only "ringNum ringPos" as integers.
    @param the name of the file that will be read to make a polyform. If null, an empty polyform will be made.
    @return the polyform represented by the given file
    @throws IllegalArgumentException if the fileName cannot make a valid polyform unless the file fails to create
    */
    public static Polyform makePolyform(String fileName){
        if(fileName == null)
            return new Polyform(new HashSet<Tile>());
        
        File file = null;
        try{
            file = new File(fileName);
        }
        catch(Exception e){
            return new Polyform(new HashSet<Tile>());
        }
        return makePolyform(file);
    }
    
    /**
    Creates a Polyform represented by the given file. The file should be formatted as one tile per line, 
    each line containing only "ringNum ringPos" as integers.
    @param the file that will be read to make a polyform.
    @return the polyform represented by the given file, or an empty polyform if the file is invalid.
    */
    public static Polyform makePolyform(File file){
        HashSet<Tile> tiles = new HashSet<>();
        try{
            Scanner scanner = new Scanner(file);
            int ringNum;
            int ringPos;
            while(scanner.hasNext()){
                ringNum = scanner.nextInt();
                ringPos = scanner.nextInt();
                tiles.add(new Tile(ringNum, ringPos));
            }
            scanner.close();
        }
        catch(Exception e){
            ;
        }
        return new Polyform(tiles);
    }
    
    /**
    Saves the polyform to the given file if and only if it does not already exist.
    The format of the file will have p q rings on the first line, and ringNum ringPos of a tile on every future line.
    @param file the file that the polyform will be saved to
    @param tessellation the tessellation that the polyform exists in. Needed for p, q, rings.
    */
    public boolean saveToFile(File file, Tessellation tessellation){
        if(file == null || tessellation == null)
            return false;
        PrintWriter writer = null;
        try{
            if(file.exists() || !file.createNewFile()){
                return false;
            }
            writer = new PrintWriter(file);
            writer.printf("%d %d %d\n", tessellation.p, tessellation.q, tessellation.rings);
            for(Tile tile : animal){
                writer.printf("%d %d\n", tile.ringNum, tile.ringPos);
            }
            writer.flush();
            writer.close();
        }
        catch(Exception e){
            if(writer != null)
                writer.println("error");
                writer.flush();
                writer.close();
            return false;
        }
        return true;
    }
    
    /**
    Accessor for the hashset.
    @return the set of tiles in the polyform.
    */
    public HashSet<Tile> getAnimal(){
        return animal;
    }
    
    /**
    Functions identically to HashSet.contains()
    Note that only one Tile in a MegaTile needs to be in the polyform for this to be considered true.
    @param tile the tile that will be checked for presence in the polyform
    @return true if and only if the given Tile is in the polyform
    */
    public boolean contains(Tile tile){
        if(tile instanceof MegaTile mega){
            for(Tile t : mega.getAll())
                if(animal.contains(t))
                    return true;
            return false;
        }
        else return animal.contains(tile);
    }
}