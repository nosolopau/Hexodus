package heuristics;

/**
 * Heuristics Factory. If a multi-processor system is available,
 * it uses it to create a specific MultiThread type object.
 *
 * @author Pau
 * @version 1.0
 */
public class Factory {

    /** Creates the Factory instance */
    public Factory() {
    }

    /** Based on the number of available processors on the machine,
     *  returns a reference to a single-thread or multi-thread heuristic */
    public Heuristic newHeuristic(int dim, int level, boolean swap) {
        if(Runtime.getRuntime().availableProcessors() < 2){
            return new SingleThread(dim, level, swap);
        }
        else
            return new MultiThread(dim, level, swap);
    }
}
