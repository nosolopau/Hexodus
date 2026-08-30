package heuristics;

import java.util.*;

/**
 *  Represents a path composed of cells.
 *
 *  Two internal representations are supported: the original ArrayList of
 *  Cells, and (when OptConfig.USE_BITPATH is active) a 128-bit mask (two
 *  long words) indexed by cell id, which turns contains/intersection/
 *  union/equals into a couple of bitwise operations. The mask
 *  representation supports boards up to 11x11 (121 squares + 4 borders =
 *  125 ids) and does not support iterating the cells of a path.
 *
 *  @author Pau
 *  @version 1.0
 */
public class Path {
    private ArrayList <Cell> list;      // An ArrayList of Cells in the path (null in bitmask mode)
    private long maskLo;                // Bits for cell ids 0-63 (bitmask mode)
    private long maskHi;                // Bits for cell ids 64-127 (bitmask mode)
    private boolean direct;             // Indicates if the path is direct type []
    private boolean newPath;            // Indicates if the path is new or not

    /** Creates a new Path */
    public Path() {
        if(!OptConfig.USE_BITPATH)
            list = new ArrayList();
        maskLo = 0L;
        maskHi = 0L;
        direct = false;     // Initially defined as NOT direct path
        newPath = true;     // Marked as new
    }

    /** Adds a cell to the path if it doesn't exist
     *  @param cell Cell to add to the path */
    public void add(Cell cell){
        if(OptConfig.USE_BITPATH){
            int id = cell.getId();
            if(id < 64)
                maskLo |= (1L << id);
            else if(id < 128)
                maskHi |= (1L << (id - 64));
            else // Fail loudly instead of silently colliding bits
                throw new IllegalStateException("USE_BITPATH supports boards up to 11x11 (cell id " + id + ")");
            return;
        }
        if(!list.contains(cell))
            list.add(cell);
    }

    /** Returns the current value of the New property
     *  @return True if the path is new, false otherwise*/
    public boolean isNew(){
        return newPath;
    }

    /** Sets a new value for the new attribute
     *  @param newValue The new value for the new attribute */
    public void changeNew(boolean newValue){
        newPath = newValue;
    }

    /** Returns the path length based on the number of cells
     *  @return Path length (number of cells it contains) */
    public int getLength(){
        if(OptConfig.USE_BITPATH)
            return Long.bitCount(maskLo) + Long.bitCount(maskHi);
        return list.size();
    }

    /** Returns a value to establish whether a path contains a cell or not
     *  @param target Target cell
     *  @return True if cell 'target' is in the path, false otherwise */
    public boolean contains(Cell target){
        if(OptConfig.USE_BITPATH){
            int id = target.getId();
            if(id < 64)
                return (maskLo & (1L << id)) != 0;
            return (maskHi & (1L << (id - 64))) != 0;
        }
        return list.contains(target);
    }

    /** Returns true if the intersection of the current path with the given one
     *  is empty
     *  @param other Path to compare with the current one
     *  @return True if the intersection of both is null, false otherwise */
    public boolean hasEmptyIntersection(Path other){
        if(OptConfig.USE_BITPATH)
            return ((maskLo & other.maskLo) == 0) && ((maskHi & other.maskHi) == 0);

        Iterator i1 = list.iterator();

        while(i1.hasNext())
            if(other.contains((Cell) i1.next())) return false;
        return true;
    }

    /** Executes the intersection operation between two paths
     *
     *  @param other Path to calculate intersection with the current one
     *  @return A path result of the intersection between the current and
     *  received by arguments. */
    public Path intersection(Path other){
        Path newPath = new Path();

        if(OptConfig.USE_BITPATH){
            newPath.maskLo = maskLo & other.maskLo;
            newPath.maskHi = maskHi & other.maskHi;
            return newPath;
        }

        Cell square = null;

        Iterator <Cell> i1 = list.iterator();
        while(i1.hasNext()){
            square = i1.next();
            if(other.contains(square)) newPath.add(square);
        }
        return newPath;
    }

    /** Executes the union operation between two paths
     *
     *  @param other Path to calculate union with the current one
     *  @return A path result of the union of the current and received by
     *  arguments. */
    public Path union(Path other){
        Cell square = null;
        Path newPath = new Path();

        if (other.isDirect() && isDirect()){
            newPath.makeDirect();
            return newPath;
        }

        if(OptConfig.USE_BITPATH){
            newPath.maskLo = maskLo | other.maskLo;
            newPath.maskHi = maskHi | other.maskHi;
            return newPath;
        }

        Iterator i1 = list.iterator();
        while(i1.hasNext()){
            newPath.add((Cell) i1.next());
        }
        Iterator i2 = other.getIterator();
        while(i2.hasNext()){
            square = (Cell) i2.next();
            if(!newPath.contains(square)) newPath.add(square);
        }
        return newPath;
    }

    /** Executes the union operation between two paths
     *
     *  @param other Path to calculate union with the current one
     *  @param cell Cell to be intercalated in the union
     *  @return A path result of the union of the current and received by
     *  arguments, intercalating cell c in the union */
    public Path union(Path other, Cell cell){
        Path newPath = new Path();

        if(OptConfig.USE_BITPATH){
            newPath.maskLo = maskLo | other.maskLo;
            newPath.maskHi = maskHi | other.maskHi;
            newPath.add(cell);
            return newPath;
        }

        Iterator <Cell> i1 = list.iterator();
        while(i1.hasNext()){
            newPath.add(i1.next());
        }
        newPath.add(cell);

        Iterator <Cell> i2 = other.getIterator();
        Cell square = null;
        while(i2.hasNext()){
            square = i2.next();
            if(!newPath.contains(square)) newPath.add(square);
        }

        return newPath;
    }

    /** Returns if the current path is empty. If the path is direct it is
     *  considered empty
     *  @return True if the path is empty or direct. False otherwise. */
    public boolean isEmpty(){
        if(OptConfig.USE_BITPATH)
            return (maskLo | maskHi) == 0L;
        return list.isEmpty();
    }

    /** Returns the ids of the cells making up this path, in either
     *  representation. The interface uses this to show which empty cells a
     *  virtual connection depends on.
     *  @return Cell ids in the path */
    public java.util.List<Integer> cellIds(){
        java.util.List<Integer> ids = new java.util.ArrayList<Integer>();
        if(OptConfig.USE_BITPATH){
            for(int i = 0; i < 64; i++){
                if((maskLo & (1L << i)) != 0) ids.add(Integer.valueOf(i));
                if((maskHi & (1L << i)) != 0) ids.add(Integer.valueOf(64 + i));
            }
        }
        else if(list != null){
            for(int i = 0; i < list.size(); i++) ids.add(Integer.valueOf(list.get(i).getId()));
        }
        return ids;
    }

    /** Returns a path iterator. Not supported in bitmask mode (returns an
     *  empty iterator), where paths are not enumerable.
     *  @return An Iterator object over the Cell list
     */
    public Iterator getIterator(){
        if(list == null)
            return Collections.emptyList().iterator();
        return list.iterator();
    }

    /** Marks the direct attribute as true */
    public void makeDirect(){
        if(list != null)
            list.clear();
        maskLo = 0L;
        maskHi = 0L;
        direct = true;
    }

    /** Returns whether the path is direct or not
     *  @return True if the path is direct, false otherwise */
    public boolean isDirect(){
        return direct;
    }

    /** Returns true if the current path and the received one are equal.
     *  A path is considered equal to another if it contains the same
     *  cells regardless of order. If the paths are direct, they are considered equal.
     *
     *  @param other A target path to compare with the current one
     *  @return True if the paths are equal, false otherwise */
    public boolean equals(Path other){
        if(this.direct && other.direct) return true;

        if(OptConfig.USE_BITPATH)
            return (maskLo == other.maskLo) && (maskHi == other.maskHi);

        if(other.getLength() != this.getLength()) return false;

        Iterator <Cell> i1 = list.iterator();
        while(i1.hasNext()){
            Cell c1 = i1.next();
            if(!other.contains(c1)) return false;
        }
        return true;
    }

    /** ... */
    public String toString(){
        if(list == null) return "[mask:" + Long.toBinaryString(maskHi) + ":" + Long.toBinaryString(maskLo) + "]";
        if(list.isEmpty()) return "[]";
        Iterator e = list.iterator();
        String s = new String();
        s = "[";
        while(e.hasNext())
            s = s + e.next();
        s = s + "]";
        return s;
    }
}
