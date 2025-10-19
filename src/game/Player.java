package game;

/** Represents a player in the match
 *  @author Pau
 *  @version 1.0
 */
public class Player {
    private String name;
    private int color;
    private int type;
    private int position;

    /** Creates a new player
     *  @param name     Player's name
     *  @param type     Player type (computer or human)
     *  @param position Position on the board: horizontal or vertical */
    public Player(String name, int type, int position){
        this.name = name;
        this.type = type;
        this.position = position;
        if(position == 1) color = 1;
        else color = 2;
    }

    /** Creates a new player
     *  @param type     Player type (computer or human)
     *  @param position Position on the board: horizontal or vertical */
    public Player(int type, int position){
        this.type = type;
        this.position = position;
        if(position == 1){
            name = "vertical player";
            color = 1;
        }
        else{
            name = "horizontal player";
            color = 2;
        }
    }

    /** Returns the player's name
     *  @return A string with the player's name */
    public String getName(){
        return name;
    }

    /** Returns the player's color
     *  @return An integer representing the player's color */
    public int getColor(){
        return color;
    }

    /** Returns the player's type
     *  @return An integer representing the player's type */
    public int getType(){
        return type;
    }

    /** Returns whether the player is a computer
     *  @return True if the player is controlled by the system */
    public boolean isComputer(){
        return type == 1;
    }

    /** Returns the player's position
     *  @return An integer representing the player's position */
    public int getPosition(){
        return position;
    }
}
