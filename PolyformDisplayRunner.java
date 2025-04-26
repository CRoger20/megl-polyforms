import megl.polyforms.PolyformDisplayer;
/**
A simple runner for the PolyformDisplayer.
See megl.polyforms.PolyformDisplayer for more information.
*/
public class PolyformDisplayRunner {
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