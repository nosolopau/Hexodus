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
    private boolean anyNew = true;       // True if the route may contain a new path
                                         // (conservative: may be stale-true, never stale-false)
    private boolean hasDirect = false;   // Cached: true if the route contains a direct path

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
        if(path.isDirect()){
            // Recompute the cached flag; another direct path may remain
            hasDirect = false;
            for(int i = 0; i < paths.size(); i++)
                if(paths.get(i).isDirect()){ hasDirect = true; break; }
        }
    }

    /** Clears this route and fills it with the paths of the source route,
     *  sharing the Path objects (same semantics as clone). Used to recycle
     *  Route objects from the scratch pool.
     *  @param src Route whose contents are copied into this one */
    void resetFrom(Route src){
        paths.clear();
        paths.addAll(src.paths);
        minimum = src.minimum;
        anyNew = src.anyNew;
        hasDirect = src.hasDirect;
    }

    /** Adds the referenced path to the current route
     *  @param path Path to add to the route
     *  @return Returns true if the path was added, false otherwise */
    public boolean add(Path path){
        Iterator i = paths.iterator();
        Path c = null;

        /* If the route has reached the path threshold or has a direct path,
         * the insertion is cancelled and false is returned */
        if((this.getLength() >= OptConfig.maxPathsPerRoute) || (hasDirectPath())) return false;

        // If an equal path already exists in the route, the insertion is cancelled
        while(i.hasNext()){
            c = (Path)i.next();
            if(path.equals(c)) return false;
        }

        paths.add(path);
        if(path.isNew()) anyNew = true;
        if(path.isDirect()) hasDirect = true;

        // Updates the minimum path if necessary
        if(minimum == null) minimum = path;
        else if(minimum.getLength() > path.getLength()) minimum = path;

        return true;
    }

    /** Returns whether this route may contain a new path (see refreshAnyNew)
     *  @return True if a new path may be present */
    boolean hasAnyNew(){
        return anyNew;
    }

    /** Recomputes the new-path flag exactly by scanning the paths. Called
     *  at the start of each H-search generation; between refreshes the flag
     *  only transitions to true (on insert), never to false, keeping it
     *  conservative. */
    void refreshAnyNew(){
        for(int i = 0; i < paths.size(); i++){
            if(paths.get(i).isNew()){
                anyNew = true;
                return;
            }
        }
        anyNew = false;
    }

    /** Returns true if there is a direct path in the route
     *  @return True if the route has a direct path */
    public boolean hasDirectPath(){
        if(OptConfig.USE_LEAN_OR) return hasDirect;

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

        if(OptConfig.USE_LEAN_OR){
            /* This route is already de-duplicated and under the cap, so the
             * add() dedup scan per path is pure waste: copy the list
             * directly, skipping the excluded path (identity comparison,
             * matching what ArrayList.remove did for Path, which does not
             * override equals(Object)). The minimum is maintained with the
             * same strictly-shorter rule add() applies. */
            for(int i = 0; i < paths.size(); i++){
                Path p = paths.get(i);
                if(p == path) continue;
                newRoute.paths.add(p);
                if(p.isDirect()) newRoute.hasDirect = true;
                if(newRoute.minimum == null) newRoute.minimum = p;
                else if(newRoute.minimum.getLength() > p.getLength()) newRoute.minimum = p;
            }
            return newRoute;
        }

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
