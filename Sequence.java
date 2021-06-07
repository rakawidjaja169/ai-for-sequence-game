import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class Sequence {
    //List of constants used to define direction of the line
    public static enum Direction{EAST, SOUTH_EAST, SOUTH, SOUTH_WEST};
    public static final int LENGTH = 5;
    /*public static final int EAST = 0;
    public static final int SOUTH_EAST = 1;
    public static final int SOUTH = 2;
    public static final int SOUTH_WEST = 3;*/

    private Direction dir;
    private int[] cellsIdx;

    public Sequence(int[] cellsIdx, Direction dir){
        //initialize
        this.cellsIdx = new int[LENGTH];

        //populate the cells index, then sort it
        for(int i=0; i<cellsIdx.length; i++){
            this.cellsIdx[i] = cellsIdx[i];
        }
        Arrays.sort(cellsIdx);
    }

    /**
     * get the total number of intersection (total number of same cells) between this sequence and the given one.
     * @param otherSeq the other sequence that may intersect with this one.
     * @return the total number of intersection (total number of same cells) between this sequence and the given one.
     */
    public int getTtlIntersection(Sequence otherSeq){
        int ttlIntersection = 0;

        //compare the array of cells index
        int idxThis, idxOther;
        idxThis = idxOther = 0;
        while(idxThis < LENGTH && idxOther < LENGTH){
            if(cellsIdx[idxThis] == otherSeq.cellsIdx[idxOther])
                ttlIntersection++;
            else if(cellsIdx[idxThis] < otherSeq.cellsIdx[idxOther])
                idxThis++;
            else if(cellsIdx[idxThis] > otherSeq.cellsIdx[idxOther])
                idxOther++;
        }
        return ttlIntersection;
    }

    /**
     * get the array of cells' index (in one dimensional form) contained in this sequence
     * @return a reference to the array of cells' index (in one dimensional form) contained in this sequence
     */
    public int[] getCellsIdx(){
        return cellsIdx;
    }

    /** get the cell's index stored at the specified index {@code idx}
     * @return if {@code idx} between 0 and 4 (inclusive), return the cell's index stored at the given index,
     * else return -1.
     */
    public int getCellIdxAt(int idx) {
        if (idx < LENGTH)
            return cellsIdx[idx];
        else
            return -1;
    }

    /**
     * determine whether the given index is the first cell's index contained in this sequence
     * @param idx the index to be checked whether it is the first cell's index in this sequence
     * @return true if {@code idx} is the first cell's index in this sequence, false otherwise
     */
    public boolean isFirstIdx(int idx){
        return cellsIdx[0] == idx;
    }
}
