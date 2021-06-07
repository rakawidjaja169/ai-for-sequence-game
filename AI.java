import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * an abstract class used as a template for your own implementation of AI. Extend this class and complete it with
 * your own strategy. Feel free to add more variables and private (or even public) methods to your classes, but make
 * sure that the methods provided here are the ones you ultimately use to determine the AI's moves.
 *
 * For example, you can consider complicated strategy that takes into account used cards to consider which cell to fill
 * first (ones whose pair is still empty) or which cell to discard using one-eyed jack (ones whose pair is filled).
 * However, all these considerations must eventually be used in method {@code evaluateHand()} to choose which card
 * to be played by this AI in this turn.
 *
 * The three most important methods that should be used are {@code chooseSequence(...)}, {@code evaluateHand()},
 * and {@code discardCard()}. These three are used in {@code FrmMain} (method {@code nextTurn()}) to determine the AIs'
 * movements.
 */
public abstract class AI extends Player {
    private GameModel gameModel;
    public List<Integer> uselessCellIdx;
    private boolean isTwinCardEnabled;
    private int selectedIdxOneDim;

    public boolean isTwinCardEnabled() { return isTwinCardEnabled; }
    public int getSelectedIdxOneDim(){ return selectedIdxOneDim; }
    public void setSelectedIdxOneDim(int idxOneDim) {selectedIdxOneDim = idxOneDim; }
    public void setTwinCardEnabled(boolean isEnabled){isTwinCardEnabled = isEnabled;}

    public AI(int id, int team, GameModel gameModel){
        super(id, team);
        this.gameModel = gameModel;
        uselessCellIdx = new ArrayList<>();
    }

    /**
     * choose a sequence from a list of consecutive (candidate) sequences. The default is to just return the
     * first candidate sequence in the list (no consideration at all)
     * @param consecutiveSeqs list of consecutive (candidate) sequences in the same direction that were made in 1 turn
     * @return the sequence to be formed from the list of consecutive sequences.
     */
    public Sequence chooseSequence(List<Sequence> consecutiveSeqs){
        return consecutiveSeqs.get(0);
    }

    public abstract Card evaluateHand();
    /*{
     *//*
     * CALCULATE HEURISTIC VALUES
     * (GET bestIdx)
     * (Store the position where the card will be played)
     *//*

        //check best heuristic value (bestIdx)
        //get the card cards[bestIdx]
        //get the position to play the card (bestOneDimLoc)
        //setSelectedIdx(bestOneDimLoc)
        //setTwinCardEnabled(mostly false, but may be true)
        //return card[bestIdx];
    }*/

    /**
     * a method to decide which card(s) to be played after evaluating the heuristic value of all cards in hand.
     * A player can play twin cards simultaneously if possible. This situation is indicated by having a list of Integer
     * with size 2 for the Value part in the returned Map object.
     * @return a Map with the card to be played as the Key and the index (or indices) in 1D format
     * to put the card as the Value. The Map can only have 1 Key-Value pair, but the Value (indices) may consists of
     * 1 (in normal situation) or 2 indices (in case of a twin card)
     */
    /*public abstract HashMap<Card, List<Integer>> evaluateHand();
    {
        Card selectedCard = null;
        Integer selectedIdx = null;
        List<Integer> indices = new ArrayList<>();
        //strategy
        //...
        //selectedCard = getCards().get(selectedIdx);
        //indices.add(selectedIdx);
        //

        HashMap<Card, List<Integer>> move = new HashMap<>();
        move.put(selectedCard, selectedIdx);
        return move;
    }*/

    /**
     * a method to decide whether the AI has a waste card to be discarded, and whether to discard that waste card.
     * Fill this method with your strategy to decide when a waste card should be discarded or kept. The default value
     * is to return 0, which means that the AI never discard a waste card (no AI)
     * @return the number of cards discarded in one turn by the AI (0 indicates no card discarded)
     */
    public int discardCard(){
        return 0;
    }

    /**
     * determine a heuristic value for a specific cell on the board.
     * Fill this method with your strategy in determining how worthy it is to place a card on a given cell. The
     * default value is to return 1, which means that the AI is indifferent to cell's position (all cells are considered
     * the same in determining which move to be made)
     * @param idxRow the row's index of a cell on the board
     * @param idxCol the column's index of a cell on the board
     * @return a heuristic value for the specified cell on the board (default 1, means indifferent)
     */
    public double evaluateCellPosition(int idxRow, int idxCol, boolean isOneEyedJack){ return 1d; }

    /**
     * determine a heuristic value for playing a specific card.
     * Fill this method with your strategy in determining how worthy it is to play a given card at a given position.
     * This method will probably be used in method {@code evaluateHand()} to evaluate each card in the AI's hand.
     * The default value is one, which means that all cards are considered the same (indifferent to which card
     * to be played)
     * @param card the card to be evaluated
     * @param idxRow the row index where the card will be played at
     * @param idxCol the column index where the card will be played at
     * @return a heuristic value for playing the specified card at the specified position
     */
    public double evaluateCard(Card card, int idxRow, int idxCol){
        return 1d;
    }


    public GameModel getGameModel(){
        return gameModel;
    }
    public List<Integer> getUselessCellIdx(){
        return uselessCellIdx;
    }
}
