public class Cell {
    public static final int MARK_EMPTY = -1;
    public static final int MARK_BOTH_TEAM = 0;

    private boolean isSequence;
    private int mark;
    private Card card;

    /**
     * create a cell at the given (x, y) coordinate on the board, with (0,0) on the top-left corner
     * @param card the card contained in this cell
     */
    public Cell(Card card){
        isSequence = false;
        mark = MARK_EMPTY;
        setCard(card);
    }

    /**
     * fill the cell with the given player's (or team's) mark to indicate this cell has been occupied. This operation
     * will only succeed if this cell has been associated with a card.
     * @param player the player who fill this cell.
     * @return true if the operation is successful, which happens only if this cell has been associated with a card,
     * false otherwise
     */
    public boolean fill(Player player){
        if(card == null || isWildCell()){
            return false;
        }
        else
        {
            mark = player.getTeam();
            return true;
        }

    }

    /**
     * empty the cell
     */
    public void empty(){ mark = MARK_EMPTY; }

    /**
     * set this cell to be a wild cell (that can be used by both teams)
     */
    private void setWildCell(){
        mark = MARK_BOTH_TEAM;
    }

    /**
     * set the card associated with this cell. Give null value to reset this cell (and mark it as empty).
     * @param card the card to be associated to this cell. If null is given, the cell will be reset and marked as empty
     */
    public void setCard(Card card){
        this.card = card;
        if(card == null){
            empty();
            setSequence(false);
        }
        else if(this.card.equals(SequenceBoard.WILD_CARD)){
            setWildCell();
        }
    }

    /**
     * get the card associated with this cell.
     * @return a reference to the card associated with this cell.
     */
    public Card getCard(){ return card;}

    /**
     * determine whether this cell is a wild cell, that is the cell on the four corners of the board
     * which can be used to form a line by both teams.
     * @return true if this cell is a wild cell, false otherwise.
     */
    public boolean isWildCell(){ return (mark == MARK_BOTH_TEAM); }

    /**
     * get whether this cell is already filled previously
     * @return true if this cell is already filled, false otherwise
     */
    public boolean isFilled()
    {
        return mark != MARK_EMPTY;
    }

    /**
     * set whether this cell is a part of a sequence on the board
     * @param isSequence if true then this cell is a part of a sequence, if false then it is not.
     */
    public void setSequence(boolean isSequence){ this.isSequence = isSequence;}

    /**
     * determine whether this cell is a part of a sequence on the board.
     * @return true if this cell is a part of a sequence on the board, false otherwise.
     */
    public boolean isSequence() { return isSequence; }

    /**
     * get the mark of this cell, which indicate the state of this cell (empty, wild cell, filled by player from team 1
     * or from team 2).
     * @return an integer indicating the cell's state, with the value of {@code MARK_EMPTY} if the cell is empty,
     * {@code MARK_BOTH_TEAM if the cell is a wild cell, {@code Player.TEAM_1} if the cell has been filled by player
     * from team 1, and {@code Player.TEAM_2} if the cell has been filled by player from team 2.
     */
    public int getMark() { return mark; }

    /**
     * return the code of the card contained in this cell.
     * @return the code of the card (of type {@code Card.Code} contained in this cell.
     */
    public Card.Code getCardCode() {return card.getCode(); }

}
