package heuristics;
import java.util.*;

/** Represents a route between two cells
 *
 *  @author Pau
 *  @version 1.0
 */
class Route implements Cloneable{
    private ArrayList <Path> paths;      // List of paths composing the route
    private Path minimum;                // Maintains a pointer to the minimum path
    private static int maximumPaths = 20; // Maximum number of paths allowed per route

    /** Creates a new Route instance */
    public Route(){
        paths = new ArrayList();
        minimum = null;
    }

    /** Overrides the clone method and adapts it to Route */
    public Object clone(){
        Route o = null;
        try{
           o = (Route) super.clone();
        }
        catch(CloneNotSupportedException e){
            System.err.println("Not cloneable");
        }
        o.paths = (ArrayList) o.paths.clone();
        return o;
    }

    /** Removes the given path from the current route
     *  @param path Path to remove from the route */
    public void remove(Path path){
        paths.remove(path);
    }

    /** Adds the referenced path to the current route
     *  @param path Path to add to the route
     *  @return Returns true if the path was added, false otherwise */
    public boolean add(Path path){
        Iterator i = paths.iterator();
        Path c = null;

        /* If the route has reached the path threshold or has a direct path,
         * the insertion is cancelled and false is returned */
        if((this.getLength() >= maximumPaths) || (hasDirectPath())) return false;

        // If an equal path already exists in the route, the insertion is cancelled
        while(i.hasNext()){
            c = (Path)i.next();
            if(path.equals(c)) return false;
        }

        paths.add(path);

        // Updates the minimum path if necessary
        if(minimum == null) minimum = path;
        else if(minimum.getLength() > path.getLength()) minimum = path;

        return true;
    }

    /** Returns true if there is a direct path in the route
     *  @return True if the route has a direct path */
    public boolean hasDirectPath(){
        Iterator d = paths.iterator();

        while(d.hasNext())
            if(((Path)d.next()).isDirect()) return true;
        return false;
    }

    /** Returns the minimum path of the route
     *  @return A reference to the minimum path or null if there are no paths in the route */
    public Path getMinimumPath(){
        return minimum;
    }

    /** Returns a copy route of the current one deleting the path passed
     *  as parameter
     *  @param path The path to be removed in the route copy
     *  @return     The resulting route cloned from the current one*/
    public Route cloneWithoutPath(Path path){
        Route newRoute = new Route();
        Iterator il = paths.iterator();

        while(il.hasNext())
           newRoute.add((Path) il.next());
        newRoute.remove(path);

        return newRoute;
    }

    /** Returns the route length
     *  @return The number of paths in the route */
    public int getLength(){
        return(paths.size());
    }

    /** Returns if the route is empty
      * @return True if the route is empty, false otherwise */
    public boolean isEmpty(){
        return paths.isEmpty();
    }

    /** Returns an iterator of the paths
     *  @return Iterator object over Paths */
    public Iterator getIterator(){
        return paths.iterator();
    }

    /** ... */
    public String toString(){
        return "" + paths + "";
    }
}
