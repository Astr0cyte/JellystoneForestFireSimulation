// T is the type of tree this cell can hold. The bound "extends Tree"
// guarantees whatever is stored here has all of Tree's methods.
public class Cell<T extends Tree> {

    private final int row;
    private final int col;
    private T tree;
    private double windExposure;

    // True once a tree has burned down here - the ground stays scorched
    // for the rest of the simulation (drawn by ForestPanel).
    private boolean scorched;

    Cell(int row, int col) {
        this.row = row;
        this.col = col;
        this.tree = null;
    }
    
    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public void plantTree(T tree) {
        this.tree = tree;
    }

    public boolean hasTree() {
        if(this.tree == null) {
            return false;
        }
        return true;
    }

    public T getTree() {
        return this.tree;
    }

    public void removeTree() {
        this.tree = null;
    }

    public boolean isScorched() {
        return scorched;
    }

    public void markScorched() {
        this.scorched = true;
    }

    public boolean isEmpty() {
        if(!this.hasTree()) {
            return true;
        }
        return false;
    }

    public double getWindExposure() {
        return windExposure;
    }

    public void setWindExposure(double windExposure) {
        this.windExposure = windExposure;
    }

}