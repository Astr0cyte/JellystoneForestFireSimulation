// T is the type of tree this cell can hold. The bound "extends Tree"
// guarantees whatever is stored here has all of Tree's methods.
public class Cell<T extends Tree> {

    private int x;
    private int y;
    private T tree;
    private double windExposure;

    Cell(int x, int y) {
        this.x = x;
        this.y = y;
        this.tree = null;
    }
    
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int newX) {
        x = newX;
    }

    public void setY(int newY) {
        y = newY;
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