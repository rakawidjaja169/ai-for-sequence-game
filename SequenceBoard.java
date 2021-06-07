import java.io.File;
import java.io.IOException;
import java.util.*;

public class SequenceBoard {
    public static final Card WILD_CARD = new Card(Card.Code.X);
    public static final int BOARD_WIDTH = 10;
    public static final int BOARD_HEIGHT = 10;

    private Cell[][] cells;                         /** the board containing 10 x 10 cells.
                                                        The first dimension specifies the column (x coordinate),
                                                        while the second one specifies the row (y coordinate)*/
    private Map<Integer, List<Integer>> cellsNM;    /** map a card to all cells "containing" that card */
    private List<Integer> filledCellsIdx;           /** list of cells that has been filled, used when playing One-Eyed Jack */
    private List<Integer> emptyCellsIdx;            /** list of cells that is empty, used when playing Two-Eyed Jack */
    private int sequenceTeam1;
    private int sequenceTeam2;

    private SequenceBoard(List<Card> allCards){
        //initialize all empty collections
        cellsNM = new HashMap<Integer, List<Integer>>();
        emptyCellsIdx = new ArrayList<Integer>();
        filledCellsIdx = new ArrayList<Integer>();

        cells = new Cell[BOARD_WIDTH][BOARD_HEIGHT];
        for(int idxRow=0; idxRow<cells.length; idxRow++){
            for(int idxCol=0; idxCol<cells[idxRow].length; idxCol++) {
                //create the cell
                cells[idxCol][idxRow] = new Cell( null);

                //add the new cell to the list of empty cells
                emptyCellsIdx.add(getOneDimCellIdx(idxCol, idxRow));
            }
        }

        //maps each card (its code) to a list of cells
        for(Card card : allCards){
            if(!cellsNM.containsKey(card.getId()) && !card.isOneEyedJack() && !card.isTwoEyedJack()) {
                cellsNM.put(card.getId(), new ArrayList<Integer>());
            }
        }
        //include wild cells into this map
        cellsNM.put(Card.Code.X.ordinal(), new ArrayList<Integer>());
    }

    /**
     * create a new sequence board and maps each card in {@code allCards} to each cell in this board. The second
     * parameter
     * @param allCards all cards to be played in this game
     * @param boardStructureFileName a file name that contains the structure of the board, which is the mapping between
     *                               each card and each cell. If this parameter is null, then the board will be
     *                               initialized randomly
     * @throws IOException if the file specified by the given file name {@code boardStructureFileName} cannot be found
     */
    public SequenceBoard(List<Card> allCards, String boardStructureFileName) throws IOException {
        this(allCards);
        if(boardStructureFileName == null || boardStructureFileName.trim().equals(""))
            initializeRandom(allCards);
        else
            initializeFromFile(allCards, boardStructureFileName);
    }

    /**
     * get all available cells on which the given part can be put.
     * A cell is available if it is empty with the same card as the given one.
     * @param card the card that want to be put on the board.
     * @return a shallow copy of the list of {@code Cell} to put the card. Note that the returned list may be
     *         of size 0 (an empty list) if there is no cell on the board to put the given card.
     */
    public List<Integer> getAvailableCellsIdx(Card card) {
        //return all cells filled if the card is a one-eyed Jack
        if(card.isOneEyedJack()){
            //remove cells that are already part of a sequence
            ArrayList<Integer> returnedList = new ArrayList<Integer>(filledCellsIdx);
            int idx = 0;
            while(idx<returnedList.size()){
                if(getCellAt(returnedList.get(idx)).isSequence())
                    returnedList.remove(idx);
                else
                    idx++;
            }
            return returnedList;
        }

        //return all cells unfilled if the card is a one-eyed Jack
        else if(card.isTwoEyedJack())
            return new ArrayList<Integer>(emptyCellsIdx);
        //return cells that corresponds to the given card
        else
        {
            //create a shallow copy of the list containing all cells correspond to the given card
            List<Integer> availableCellsIdx = new ArrayList<Integer>(2);

            //for all indices correspond to the given card
            List<Integer> tempList = cellsNM.get(card.getId());
            for(Integer eachIdx : tempList){
                //only include cell that is empty
                if(!getCellAt(eachIdx).isFilled()){
                    availableCellsIdx.add(eachIdx);
                }
            }
            return availableCellsIdx;
        }
    }

    /**
     * fill the cell at the given indices by the mark from the given player.
     * @param idxRow the row index (y-coordinate) of the cell to be emptied
     * @param idxCol the col index (x-coordinate) of the cell to be emptied
     * @param player the player whose mark will fill this cell
     */
    public List<List<Sequence>> fillCell(int idxRow, int idxCol, Player player) {
        //file the cell at the given position
        cells[idxCol][idxRow].fill(player);

        //update the collections of empty and filled cells
        for(int i=0; i<emptyCellsIdx.size(); i++){
            if(emptyCellsIdx.get(i) == getOneDimCellIdx(idxCol, idxRow)){
                emptyCellsIdx.remove(i);
                break;
            }
        }
        filledCellsIdx.add(getOneDimCellIdx(idxCol, idxRow));

        //check sequence
        return findSequence(idxCol, idxRow, player);
    }

    /**
     * find all candidate sequences that can be made if the given player plays a card at the specified index.
     * @param x the column index where the card will be played at
     * @param y the row index where the card will be played at
     * @param player the player who plays the card
     * @return a list of list of sequences. The inner list contains all candidate (possible) sequences that can be made
     * in one direction, while the outer list always has a size of 4 to contain the candidate sequences for each of the
     * 4 directions.
     */
    public List<List<Sequence>> findSequence(int x, int y, Player player){
        //prepare the variable
        int h, temp;
        boolean isSequence = false;
        List<List<Sequence>> seqs = new ArrayList<List<Sequence>>(4);

        //check column (LINE_EAST)
        seqs.add(new ArrayList<Sequence>());
        for(int incCol=-4; incCol<=0; incCol++){
            isSequence = false;
            h = temp = 0;
            int idxCol = x+incCol;

            //if out of bound (leftmost column), then next iteration
            if(idxCol < 0)
                continue;
            //if out of bound (rightmost column), then break (do not need to check next column)
            if(idxCol + 4 >= BOARD_WIDTH)
                break;

            //check the next 5 cells to the east
            for(int n=0; n<=4; n++){
                //if there is a wild cell
                if(cells[idxCol + n][y].isWildCell())
                    temp = player.getTeam();

                //if there is a sequence AND this is not the first encounter (more than 1 cell are already a sequence)
                if(cells[idxCol + n][y].isSequence()){
                    if(isSequence)
                        break;
                    isSequence = true;
                }

                //sum the mark (value) of each cell)
                h += cells[idxCol + n][y].getMark();
            }
            h += temp;

            //check the total sum (h)
            if(h == 5*player.getTeam()){
                //create a sequence
                seqs.get(seqs.size()-1).add(createNSequence(player, idxCol, y, Sequence.Direction.EAST));
            }
        }

        //check row (LINE_SOUTH)
        seqs.add(new ArrayList<Sequence>());
        for(int incRow=-4; incRow<=0; incRow++){
            isSequence = false;
            h = temp = 0;
            int idxRow = y+incRow;

            //if out of bound (top row), then next iteration
            if(idxRow < 0)
                continue;
            //if out of bound (bottom row), then break (do not need to check next column)
            if(idxRow + 4 >= BOARD_HEIGHT)
                break;

            //check the next 5 cells to the south
            for(int n=0; n<=4; n++){
                //if there is a wild cell
                if(cells[x][idxRow+n].isWildCell())
                    temp = player.getTeam();

                //if there is a sequence AND this is not the first encounter (more than 1 cell are already a sequence)
                if(cells[x][idxRow+n].isSequence()){
                    if(isSequence)
                        break;
                    isSequence = true;
                }

                //sum the mark (value) of each cell
                h += cells[x][idxRow+n].getMark();
            }
            h += temp;

            //check the total sum (h)
            if(h == 5*player.getTeam()){
                //create a sequence
                seqs.get(seqs.size()-1).add(createNSequence(player, x, idxRow, Sequence.Direction.SOUTH));

            }
        }

        //check diagonal left  (LINE_SOUTH_WEST)
        seqs.add(new ArrayList<Sequence>());
        for(int inc=4; inc>=0; inc--){
            isSequence = false;
            h = temp = 0;
            int idxCol = x+inc;
            int idxRow = y-inc;

            //if out of bound (starting position), then next iteration
            if(idxRow < 0 || idxCol >= BOARD_WIDTH)
                continue;
            //if out of bound (end position), then break (do not need to check next column)
            if(idxRow + 4 >= BOARD_HEIGHT || idxCol - 4 < 0)
                break;

            //check the next 5 cells to the south west
            for(int n=0; n<=4; n++){
                //if there is a wild cell
                if(cells[idxCol-n][idxRow+n].isWildCell())
                    temp = player.getTeam();

                //if there is a sequence AND this is not the first encounter (more than 1 cell are already a sequence)
                if(cells[idxCol-n][idxRow+n].isSequence()){
                    if(isSequence)
                        break;
                    isSequence = true;
                }

                //sum the mark (value) of each cell
                h += cells[idxCol-n][idxRow+n].getMark();
            }
            h += temp;

            //check the total sum (h)
            if(h == 5*player.getTeam()){
                //create a sequence
                seqs.get(seqs.size()-1).add(createNSequence(player, idxCol, idxRow, Sequence.Direction.SOUTH_WEST));
            }
        }

        //check diagonal right (LINE_SOUTH_EAST)
        seqs.add(new ArrayList<Sequence>());
        for(int inc=-4; inc<=0; inc++){
            isSequence = false;
            h = temp = 0;
            int idxCol = x+inc;
            int idxRow = y+inc;

            //if out of bound (starting position), then next iteration
            if(idxRow < 0 || idxCol < 0)
                continue;
            //if out of bound (end position), then break (do not need to check next column)
            if(idxRow + 4 >= BOARD_HEIGHT || idxCol + 4 >= BOARD_WIDTH)
                break;

            //check the next 5 cells to the south east
            for(int n=0; n<=4; n++) {
                //if there is a wild cell
                if(cells[idxCol+n][idxRow+n].isWildCell())
                    temp = player.getTeam();

                //if there is a sequence AND this is not the first encounter (more than 1 cell are already a sequence)
                if(cells[idxCol+n][idxRow+n].isSequence()){
                    if(isSequence)
                        break;
                    isSequence = true;
                }

                //sum the mark (value) of each cell
                h += cells[idxCol+n][idxRow+n].getMark();
            }
            h += temp;

            //check the total sum (h)
            if(h == 5*player.getTeam()){
                //create a sequence
                seqs.get(seqs.size()-1).add(createNSequence(player, idxCol, idxRow, Sequence.Direction.SOUTH_EAST));
            }
        }
        return seqs;
    }

    /**
     * empty the cell at the given indices.
     * @param idxRow the row index (y-coordinate) of the cell to be emptied
     * @param idxCol the col index (x-coordinate) of the cell to be emptied
     */
    public void emptyCell(int idxRow, int idxCol){
        //empty the cell at the given index
        cells[idxCol][idxRow].empty();

        //update the collections of empty and filled cells
        for(int i=0; i<filledCellsIdx.size(); i++){
            if(filledCellsIdx.get(i) == getOneDimCellIdx(idxCol, idxRow)){
                filledCellsIdx.remove(i);
                break;
            }
        }
        filledCellsIdx.remove(cells[idxCol][idxRow]);

        emptyCellsIdx.add(getOneDimCellIdx(idxCol, idxRow));
    }

    /**
     * get the cell at the specified index, where the index is given in one dimensional form. The index can be
     * calculated to given the two dimensional counterpart using division and modulo.
     * @param idx the index of the cell in one dimensional form. See {@code getOneDimCellIdx} to calculate the one
     *            dimensional form from its two dimensional counterpart.
     * @return the cell at the given index.
     */
    public Cell getCellAt(int idx){
        int x, y;
        y = getTwoDimCellIdxY(idx);
        x = getTwoDimCellIdxX(idx);
        return cells[x][y];
    }

    /**
     * get the cell at the specified column and row.
     * @param idxRow the row index of the cell
     * @param idxCol the column index of the cell
     * @return a reference to the cell at the specified column and row
     */
    public Cell getCellAt(int idxRow, int idxCol){
        return cells[idxCol][idxRow];
    }

    /**
     * get total number of sequences that have been made on the board by the given player's team.
     * @param player the player who wants to know the total number of sequences made by its team.
     * @return the total number of sequences that have been made on the board by the given player's team.
     */
    public int getNSequence(Player player){
        return getNSequence(player.getTeam());
    }
    /**
     * get total number of sequences that have been made on the board by the given team.
     * @param team an integer indicating the team whose total sequence is needed.
     * @return the total number of sequences that have been made on the board by given team.
     */
    public int getNSequence(int team){
        if(team == Player.TEAM_1)
            return sequenceTeam1;
        else
            return sequenceTeam2;
    }
    /**
     *
     * @param player
     * @param idxCol
     * @param idxRow
     * @param direction
     */
    public Sequence createNSequence(Player player, int idxCol, int idxRow, Sequence.Direction direction){
        System.out.print(player.getTeam());
        int[] cellsIdx = new int[Sequence.LENGTH];
        for(int n=0; n<=4; n++){
            if(direction == Sequence.Direction.EAST){
                cellsIdx[n] = getOneDimCellIdx(idxCol+n, idxRow);
                //System.out.print(" (" + (idxCol+n) + ", " + (idxRow) + ") ");
            }
            if(direction == Sequence.Direction.SOUTH){
                cellsIdx[n] = getOneDimCellIdx(idxCol, idxRow+n);
                //System.out.print(" (" + (idxCol) + ", " + (idxRow+n) + ") ");
            }
            if(direction == Sequence.Direction.SOUTH_WEST){
                cellsIdx[n] = getOneDimCellIdx(idxCol-n, idxRow+n);
                //System.out.print(" (" + (idxCol-n) + ", " + (idxRow+n) + ") ");
            }
            if(direction == Sequence.Direction.SOUTH_EAST){
                cellsIdx[n] = getOneDimCellIdx(idxCol+n, idxRow+n);
                //System.out.print(" (" + (idxCol+n) + ", " + (idxRow+n) + ") ");
            }
        }
        //ystem.out.println();
        return new Sequence(cellsIdx, direction);
    }
    /**
     *
     * @param player
     * @param seq
     */
    public void setNSequence(Player player, Sequence seq, boolean isSequence){
        //mark all cells in the sequence
        int[] cellsIdx = seq.getCellsIdx();
        for(int i=0; i<cellsIdx.length; i++){
            getCellAt(cellsIdx[i]).setSequence(isSequence);

            //remove all cells in this sequence from array of filled cells
            filledCellsIdx.remove(Integer.valueOf(cellsIdx[i]));
        }
        seq = null;

        //update the number of sequence formed
        if(player.getTeam() == Player.TEAM_1){
            sequenceTeam1++;
        }
        else{
            sequenceTeam2++;
        }
    }

    /**
     * get the index of the cell in one dimensional format
     * @param x the x-coordinate of the cell in two dimensional format
     * @param y the y-coordinate of the cell in two dimensional format
     * @return the index of the cell in one dimensional format (from 0-99)
     */
    public static int getOneDimCellIdx(int x, int y){
        return (y*BOARD_WIDTH + x);
    }

    /**
     * get the x coordinate (column index) of the given index (in one dimensional form)
     * by first translating the given index into two dimensional form
     * @param idx the index in one dimensional form to be translated into two dimensional form
     * @return the x coordinate (column index) of the given index in two dimensional form
     */
    public static int getTwoDimCellIdxX(int idx){
       return idx%BOARD_WIDTH;
    }

    /**
     * get the y coordinate (row index) of the given index (in one dimensional form)
     * by first translating the given index into two dimensional form
     * @param idx the index in one dimensional form to be translated into two dimensional form
     * @return the y coordinate (row index) of the given index in two dimensional form
     */
    public static int getTwoDimCellIdxY(int idx){
        return idx/BOARD_WIDTH;
    }

    /**
     * get the mapping between cards and cells used in this board.
     * @return a reference to the map between cards and cells in this object.
     */
    public Map<Integer, List<Integer>> getCellsNM(){ return cellsNM; }

    /**
     * initialize the board's alignment by reading it from a file. See "SequenceBoard1.txt" for example on how to
     * create the file.
     * @param allCards all cards that will be placed on this board (still contains Jack)
     * @param fileName the path to the file containing the board's alignment.
     */
    public void initializeFromFile(List<Card> allCards, String fileName) throws IOException{
        //open the given file containing the cards arrangement for a sequence board
        File fileBoard = new File(fileName);
        Scanner scanner = new Scanner(fileBoard);

        //read the board's alignments
        int idxRow=0, idxCol=0;
        int boardHeight = cells.length;
        int boardWidth = cells[idxRow].length;
        while(scanner.hasNext()){
            //read the card's code
            String code = scanner.next();

            //initialize the cell based on the code read
            if(code.equals(Card.Code.X.name())){
                //set the cell to be a wild cell, then add it to the map
                cells[idxCol][idxRow].setCard(WILD_CARD);
                cellsNM.get(Card.Code.X.ordinal()).add(getOneDimCellIdx(idxCol, idxRow));
            }
            else {
                //not a wild cell, update the mapping between code and cell
                cellsNM.get(Card.Code.valueOf(code).ordinal()).add(getOneDimCellIdx(idxCol,idxRow));
            }

            //update the indexing to change column and row
            idxCol++;
            if(idxCol == boardWidth){
                idxCol = 0;
                idxRow++;
            }
        }

        //associate each cell with a card
        List<Integer> tempIdx;
        //for each card
        for(Card eachCard : allCards){
            //if not Jack
            if(!eachCard.isOneEyedJack() && !eachCard.isTwoEyedJack()) {
                tempIdx = cellsNM.get(eachCard.getId());
                //for each cell that should be associated with this card
                for(Integer eachIdx : tempIdx){
                    getCellAt(eachIdx).setCard(eachCard);
                }
            }
        }
    }

    /**
     * initialize the board's alignment randomly while maintaining the four wild cells's position on the four corners
     * of the board.
     * @param allCards a list of all cards to be mapped to the board's cells
     */
    public void initializeRandom(List<Card> allCards){
        //create a copy of all the cards, then shuffle it
        List<Card> tempCards = new ArrayList<Card>(allCards);
        Collections.shuffle(tempCards, new Random(System.currentTimeMillis()));

        //create the board
        //set the four corners of the board to wild cells, then add them to the map
        cells[0][0].setCard(WILD_CARD);
        cells[0][cells[0].length-1].setCard(WILD_CARD);
        cells[cells.length-1][0].setCard(WILD_CARD);
        cells[cells.length-1][cells[cells.length-1].length-1].setCard(WILD_CARD);
        cellsNM.get(Card.Code.X.ordinal()).add(getOneDimCellIdx(0,0));
        cellsNM.get(Card.Code.X.ordinal()).add(getOneDimCellIdx(cells[0].length-1,0));
        cellsNM.get(Card.Code.X.ordinal()).add(getOneDimCellIdx(0,cells.length-1));
        cellsNM.get(Card.Code.X.ordinal()).add(getOneDimCellIdx(cells[cells.length-1].length-1,cells.length-1));

        //for all cards, randomize its position (mapping)
        int idxRow = 0, idxCol = 1;
        int boardWidth = cells[idxRow].length;
        for(int i=0; i<tempCards.size(); i++){
            //if jacks
            if(tempCards.get(i).isTwoEyedJack() || tempCards.get(i).isOneEyedJack())
                continue;

            //initialize the cell based on the card's code
            cells[idxCol][idxRow].setCard(tempCards.get(i));
            cellsNM.get(tempCards.get(i).getCode().ordinal()).add(getOneDimCellIdx(idxCol, idxRow));

            //update the indexing of column and row
            idxCol++;
            if(idxCol == boardWidth){
                idxCol = 0;
                idxRow++;
            }

            //if the cell contains wild card
            if(cells[idxCol][idxRow].isWildCell()) {
                idxCol++;
                if(idxCol == boardWidth){
                    idxCol = 0;
                    idxRow++;
                }
            }
        }

        //clean the variables
        tempCards.clear();
    }

    /**
     * reset all collections in this board to initial condition:
     * 1. all lists will be cleared.
     * 2. the map will retain its keys, and the associated values (which is a list) will be cleared.
     * 3. the array of cells will be reset to empty condition.
     */
    public void reset(){
        //clear the sequence
        sequenceTeam1 = sequenceTeam2 = 0;

        //clear all collections of cells except the map
        emptyCellsIdx.clear();
        filledCellsIdx.clear();

        //reset the map between cell and card by clearing the list of all keys without removing the keys
        Set<Integer> cellKeys = cellsNM.keySet();
        for(Integer key : cellKeys){
            cellsNM.get(key).clear();
        }
        cellKeys = null;

        //reset all the cells to empty condition, then add it to
        for(int i=0; i<cells.length; i++){
            for(int ii=0; ii<cells[i].length; ii++){
                if(!cells[i][ii].isWildCell()){
                    //reset the cells (by removing the card and thus emptying it)
                    cells[i][ii].setCard(null);

                    //add the empty cell to the list
                    emptyCellsIdx.add(getOneDimCellIdx(i, ii));
                }
            }
        }
    }

    /**
     * determine whether any of the team has won by forming two sequences
     * @return Player.TEAM_1 if team 1 has won, Player.TEAM_2 if team 2 has won, or -1 if there is no winner yet
     */
    public int getWinner()
    {
        if(sequenceTeam1  >= 2)
            return Player.TEAM_1;
        else if(sequenceTeam2 >= 2)
            return Player.TEAM_2;
        else
            return -1;
    }


    public Cell[][] getCells(){
        return cells;
    }


}
