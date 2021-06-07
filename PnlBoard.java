import javax.swing.JPanel;
import java.awt.*;
import java.util.*;
import java.util.List;

public class PnlBoard extends JPanel{
    FrmMain frmMain;
    LblCell[][] lblCells;
    SequenceBoard sequenceBoard;

    public PnlBoard(FrmMain frmMain, SequenceBoard mainBoard){
        super();
        this.frmMain = frmMain;         //reference to main container (main frame)
        this.sequenceBoard = mainBoard;

        //calculate the height of each cell in this board, while the width will be adjusted inside the cell
        int cellHeight, cellWidth;
        cellHeight = frmMain.getHeight()/10;
        cellWidth = 0;

        //initialize all cells in this board
        Map<Integer, List<Integer>> tempCellsNM = mainBoard.getCellsNM();
        List<Integer> tempCellIdx;
        setLayout(new GridLayout(10, 10));
        lblCells = new LblCell[10][10];

        //iterate through all cards
        for(Integer eachCardId : tempCellsNM.keySet()){

            //for each cell's index that contains this card
            tempCellIdx = tempCellsNM.get(eachCardId);
            for(int i=0; i<tempCellIdx.size(); i++){
                //initialize the cell
                int x = mainBoard.getTwoDimCellIdxX(tempCellIdx.get(i));
                int y = mainBoard.getTwoDimCellIdxY(tempCellIdx.get(i));
                lblCells[x][y] = new LblCell(frmMain, cellHeight,
                        x, y, false, mainBoard.getCellAt(tempCellIdx.get(i)));
            }
        }
        //dereference the temporary variables
        tempCellsNM = null;
        tempCellIdx=null;

        //set the properties of this panel
        setSize(lblCells[0][0].getWidth()*lblCells[0].length, cellHeight*lblCells.length);
        for(int idxRow=0; idxRow<lblCells.length; idxRow++){
            for(int idxCol=0; idxCol<lblCells[0].length; idxCol++){
                add(lblCells[idxCol][idxRow]);
            }
        }
        setBorder(FrmMain.BORDER_PANEL);
        setVisible(true);
    }

    /**
     * Reset the panel based on the mapping and cells in the board model. Board does not need to be updated since
     * the board model is not reinitialized, but instead reset and repopulated. Therefore, the reference will refer to
     * the updated model.
     * The panel will update all the cells' label contained based on the new board model.
     */
    public void resetBoard(){
        //update all cells based on the board in game model
        Map<Integer, List<Integer>> tempCellsNM = sequenceBoard.getCellsNM();
        SequenceBoard tempBoard = sequenceBoard;
        List<Integer> tempIdx;
        //iterate through all cards
        for(Integer eachCardId : tempCellsNM.keySet()){
            //for each cell that contains this card
            tempIdx = tempCellsNM.get(eachCardId);
            for(int i=0; i<tempIdx.size(); i++){
                //update the cell
                lblCells[tempBoard.getTwoDimCellIdxX(tempIdx.get(i))]
                        [tempBoard.getTwoDimCellIdxY(tempIdx.get(i))].setCell(tempBoard.getCellAt(tempIdx.get(i)));
            }
        }
        //dereference the temporary variables
        tempCellsNM = null;
        tempBoard = null;
        tempIdx = null;

        repaint();
        revalidate();
    }

    public void setCandidateSequence(int x, int y, boolean isCandidate){
        lblCells[x][y].setSequenceCandidate(isCandidate);
        repaint();
        revalidate();
    }

    public void setHighlightedCell(int x, int y, boolean isHighlighted){
        lblCells[x][y].setClickable(isHighlighted);
        lblCells[x][y].setSelected(isHighlighted);
        repaint();
        revalidate();
    }
}
