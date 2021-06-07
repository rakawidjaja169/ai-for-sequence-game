import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GameModel {
    List<Card> allCards;            /** a collection of all cards used in the entire game */
    List<Card> deck;                /** the deck of cards that players will draw from */
    List<Card> usedDeck;            /** the deck of cards that have been played */
    SequenceBoard sequenceBoard;    /** */
    List<Player> players;           /** */
    List<List<Sequence>> seqs;      /** */

    int idxCurrentPlayer;           /** the index of the current player who is in turn */
    Card selectedCard;              /** the card selected by current player to be played on the board */
    int ttlCards;                   /** total available cards */
    int ttlPlayerCards;             /** total cards in each player's hand */
    int scoreTeam1;                 /** the number of wins achieved by team 1*/
    int scoreTeam2;                 /** the number of wins achieved by team 2*/
    boolean isFinish;               /** the indicator whether this game has finished */

    public static final int MAX_PLAYER = 12;
    public static final int MIN_PLAYER = 2;
    public static final int MAX_CARD_PLAYER = 7;

    /**
     *
     * @param ttlPlayers total number of players in this game
     * @param boardArrangementFileName the file name containing the board's arrangement (
     * @throws Exception if the total number of players ({@code ttlPlayers}) is not an even number between 2-12
     * @throws java.io.IOException if the given file name ({@code boardArrangementFIleName}) is not found
     */
    public GameModel(int ttlPlayers, String boardArrangementFileName, boolean[] isAi) throws Exception{
        //chech the total players
        if( (ttlPlayers < MIN_PLAYER) && (ttlPlayers>MAX_PLAYER) && (ttlPlayers%2 != 0) )
            throw new Exception("Invalid total players. Need to be an even number between" +
                    MIN_PLAYER + " and " + MAX_PLAYER + ".");

        //initialize the main card deck
        ttlCards = 104;
        initializeDeck();

        //initialize the game board
        sequenceBoard = new SequenceBoard(allCards, boardArrangementFileName);
        seqs = new ArrayList<List<Sequence>>(1);

        //set the game up and initialize the game board (including the players)
        scoreTeam1 = scoreTeam2 = 0;
        newGame(ttlPlayers, boardArrangementFileName, isAi);
    }

    /**
     * play the selected twin cards simultaneously at the given indices (both available cells).
     * @param indices list of available cell indices
     * @return true if the operation is successful (both cards are placed on board), false otherwise
     */
    public boolean playSelectedTwinCard(List<Integer> indices){
        if(selectedCard != null && getCurrentPlayer().isTwinCard(selectedCard) && !isConsecutiveSequences()
                && !isFinished() && !selectedCard.isOneEyedJack() && !selectedCard.isTwoEyedJack()){

            //update the board
            int x = SequenceBoard.getTwoDimCellIdxX(indices.get(0));
            int y = SequenceBoard.getTwoDimCellIdxY(indices.get(0));
            seqs.addAll(sequenceBoard.fillCell(y, x, players.get(idxCurrentPlayer)));
            x = SequenceBoard.getTwoDimCellIdxX(indices.get(1));
            y = SequenceBoard.getTwoDimCellIdxY(indices.get(1));
            seqs.addAll(sequenceBoard.fillCell(y, x, players.get(idxCurrentPlayer)));

            //check sequences formed. After this section, only consecutive sequences are left
            int idx = 0;
            while(idx < seqs.size()){
                //if there is only one sequence, mark it
                if(seqs.get(idx).size() == 1) {
                    getSequenceBoard().setNSequence(getCurrentPlayer(), seqs.get(idx).get(0), true);
                    seqs.get(idx).clear();
                    seqs.remove(idx);
                }
                //if the list is empty (no sequence at all)
                else if(seqs.get(idx).size() == 0){
                    seqs.remove(idx);
                }
                //multiple consecutive sequences, leave it alone
                else {
                    idx++;
                }
            }

            //remove the card from the player's hands
            players.get(idxCurrentPlayer).removeCard(selectedCard);
            players.get(idxCurrentPlayer).removeCard(selectedCard);

            //add a card to the current player
            if (deck.size() > 0) {
                players.get(idxCurrentPlayer).addCard(deck.remove(0));
            }
            if (deck.size() > 0) {
                players.get(idxCurrentPlayer).addCard(deck.remove(0));
            }

            //update the deck
            usedDeck.add(selectedCard);
            usedDeck.add(selectedCard);

            //remove the selected card
            selectedCard = null;

            return true;
        }
        return false;
    }

    /**
     * play the selected card at the given x (column) and y (row) position.
     * @param x the x coordinate (column index) where the card is to be played.
     * @param y the y coordinate (row index) where the card is to be played.
     * @return true if the operation is successful (the card is placed on board), false otherwise
     */
    public boolean playSelectedCardAt(int x, int y){
        if(selectedCard != null && !isConsecutiveSequences() && !isFinished()){
            //update the board
            if(selectedCard.isOneEyedJack())
                sequenceBoard.emptyCell(y, x);
            else {
                seqs.addAll(sequenceBoard.fillCell(y, x, players.get(idxCurrentPlayer)));
            }

            //check sequences formed. After this section, only consecutive sequences are left
            int idx = 0;
            while(idx < seqs.size()){
                //if there is only one sequence, mark it
                if(seqs.get(idx).size() == 1) {
                    getSequenceBoard().setNSequence(getCurrentPlayer(), seqs.get(idx).get(0), true);
                    seqs.get(idx).clear();
                    seqs.remove(idx);
                }
                //if the list is empty (no sequence at all)
                else if(seqs.get(idx).size() == 0){
                    seqs.remove(idx);
                }
                //multiple consecutive sequences, leave it alone
                else {
                    idx++;
                }
            }

            //remove the card from the player's hands
            players.get(idxCurrentPlayer).removeCard(selectedCard);

            //add a card to the current player
            if (deck.size() > 0)
                players.get(idxCurrentPlayer).addCard(deck.remove(0));

            //update the deck
            usedDeck.add(selectedCard);

            //remove the selected card
            selectedCard = null;

            return true;
        }
        return false;
    }

    /**
     * change the turn to the next player
     * @return true if this operation is successful (if it is allowed to change turn), false otherwise
     */
    public boolean nextTurn(){
        if(!isConsecutiveSequences() && !isFinished())
        {
            //check whether the finish condition has been met
            int winner = sequenceBoard.getWinner();
            if(winner >= 0){
                if(winner == Player.TEAM_1)
                    scoreTeam1++;
                else if(winner == Player.TEAM_2)
                    scoreTeam2++;
                finish();
                return false;
            }

            //change players turn if the game is not finished to player who still has cards
            int idxLastMovingPlayer = idxCurrentPlayer;
            boolean hasMove = false;
            do{
                //increment the index
                idxCurrentPlayer++;
                if (idxCurrentPlayer == players.size())
                    idxCurrentPlayer = 0;

                //check whether player still has cards
                if(getCurrentPlayer().getCards().size() > 0)
                    hasMove = true;
                else
                    hasMove = false;

                //check whether one complete loop has been done (back to the last moving player)
                if(idxCurrentPlayer == idxLastMovingPlayer){
                    finish();
                    return false;
                }
            }while(!hasMove);
            return true;
        }
        return false;
    }

    /**
     * discard the selected card to the used deck, then change it to a new card from the deck
     * @return true if this operation is successful, false otherwise
     */
    public boolean discardSelectedCard(){
        if(!isConsecutiveSequences() && !isFinished()) {
            //remove the card to the used deck
            getCurrentPlayer().getCards().remove(selectedCard);
            usedDeck.add(selectedCard);
            selectedCard = null;

            //add a card to the current player
            if (deck.size() > 0)
                getCurrentPlayer().addCard(deck.remove(0));
            return true;
        }
        return false;
    }

    /**
     * determine whether there are consecutive sequences that need to be resolved by current player.
     * @return true if there are consecutive sequences that need to be resolved, false otherwise
     */
    public boolean isConsecutiveSequences(){
        return (seqs.size() > 0);
    }

    /**
     * set the card currently selected by the current player.
     * @param card the card selected by the current player
     * @return the list of all cells where the selected card can be put on (size 0 means there is no place to put the
     * selected card) if this operation is successful, null if this operation is not successful.
     */
    public List<Integer> setSelectedCard(Card card) {
        if(!isConsecutiveSequences() && !isFinished()){
            //set the selected card
            selectedCard = card;

            //get all available cells
            List<Integer> availableCellsIdx = sequenceBoard.getAvailableCellsIdx(selectedCard);
            return availableCellsIdx;
        }
        return null;
    }

    /**
     * resolve the set of sequence candidate by choosing which sequence to be formed among the sequence candidates
     * (sequence candidates are consecutive / adjacent sequences created from playing a single card, so player
     * must choose which sequence to be made).
     * @param chosenSeq the sequence to be formed among all candidates.
     * @param resolvedSeqs the set of sequence candidates where the chosen sequence belong to.
     * @return true if this operation is successful, false otherwise.
     */
    public boolean resolveConsecutiveSequences(Sequence chosenSeq, List<Sequence> resolvedSeqs){
        if(seqs.contains(resolvedSeqs)){
            //mark the chosen sequence
            getSequenceBoard().setNSequence(getCurrentPlayer(), chosenSeq, true);
            chosenSeq = null;

            //remove the consecutive sequences from the list, then clear it
            this.seqs.remove(resolvedSeqs);
            resolvedSeqs.clear();
            resolvedSeqs = null;

            return true;
        }
        return false;
    }

    /**
     * get the list of consecutive sequences (in one direction only) formed in this turn
     * after the call for {@code playSelectedCardAt} method. There might be at max four lists of consecutive sequences
     * (for each direction), and this method gets the list for a particular direction, specifically the one at index 0.
     * @return if there are consecutive sequences formed in this turn, return the list of those sequences (in one
     * particular direction), else return null.
     */
    public List<Sequence> getCurrentConsecutiveSequences(){
        return seqs.get(0);
    }

    /**
     * get the reference to the list of consecutive sequences formed in this turn after the call for
     * {@code playSelectedCardAt} method.
     * @return a reference to the list of consecutive sequences formed in this turn.
     */
    public List<List<Sequence>> getAllConsecutiveSequences(){
        return seqs;
    }

    /**
     * set the game to finish state that indicates a team has formed two sequences in this round.
     */
    public void finish(){ isFinish = true; }
    /**
     * determine whether this game has finished or not (whether any team manages to form 2 sequences or not).
     * @return true if the game has finished, false otherwise.
     */
    public boolean isFinished(){ return isFinish; }
    /**
     * get the card currently selected by the current player.
     * @return a reference to the card selected by the current player.
     */
    public Card getSelectedCard(){ return selectedCard;}
    /**
     * get the array of all players in this game.
     * @return a reference to the array containing all players in this game.
     */
    public List<Player> getPlayers() {return players;}
    /**
     * get the current player.
     * @return a reference to the current player.
     */
    public Player getCurrentPlayer() {return players.get(idxCurrentPlayer);}
    /**
     * get the board of this game
     * @return a reference to the game's board used in this object
     */
    public SequenceBoard getSequenceBoard(){ return sequenceBoard; }
    /**
     * get the deck of cards used in this game, which is the deck of cards where players draw new cards from.
     * @return a reference to the deck of cards used.
     */
    public List<Card> getDeck(){ return deck;}
    /**
     * get the deck of used cards, which is the deck of cards that has been played by players.
     * @return a reference to the deck of used cards.
     */
    public List<Card> getUsedDeck(){ return usedDeck; }
    /**
     * get the score (the total wins in this game) for team 1
     * @return the score (the total wins) for team 1
     */
    public int getScoreTeam1() {return scoreTeam1;}
    /**
     * get the score (the total wins in this game) for team 2
     * @return the score (the total wins) for team 2
     */
    public int getScoreTeam2() {return scoreTeam2;}

    /**
     * determine whether the current player is AI or not
     * @return true if current player is an AI, false otherwise
     */
    public boolean isCurrentPlayerAi(){
        return getCurrentPlayer() instanceof  AI;
    }

    /**
     * method to create a new model for a new game with the specified total players and a new board arrangement. This
     * method will first clear all collections while keeping the basic objects that can be reused. The method then
     * set all basic objects to their initial state, then repopulate all collections.
     * @param ttlPlayers total number of players in this game.
     * @param boardArrangementFileName a file name containing the board's arrangement.
     * @throws IOException when the given file name for board's arrangement cannot be found.
     */
    public void newGame(int ttlPlayers, String boardArrangementFileName, boolean[] isAi) throws IOException {
        //set the properties of this model
        selectedCard = null;
        idxCurrentPlayer = 0;
        isFinish = false;

        //clear the players
        if(players != null) {
            for (Player eachPlayer : players) {
                eachPlayer.getCards().clear();
            }
            players.clear();
            players = null;
        }

        //(re)initialize the players
        initializePlayers(ttlPlayers, isAi);

        //reset the deck (and redistribute the cards)
        newDeck();

        //reset the board
        sequenceBoard.reset();
        if(boardArrangementFileName == null || boardArrangementFileName.trim().equals("")){
            sequenceBoard.initializeRandom(allCards);
        }
        else{
            sequenceBoard.initializeFromFile(allCards, boardArrangementFileName);
        }
    }

    /**
     * reset all decks to be played in the game. This will clear the deck of cards that has been played,
     * repopulate and reshuffle the deck of cards, then distribute the new deck to the players
     */
    public void newDeck(){
        //reset the deck to its initial state, then shuffle the deck to be played
        deck.clear();
        usedDeck.clear();
        deck.addAll(allCards);
        /*deck.add(new Card(Card.Code.SK));
        deck.add(new Card(Card.Code.SK));
        deck.add(new Card(Card.Code.SK));
        deck.add(new Card(Card.Code.SK));
        deck.add(new Card(Card.Code.SK));
        deck.add(new Card(Card.Code.SK));
        deck.add(new Card(Card.Code.SK));
        deck.add(new Card(Card.Code.SK));
        deck.add(new Card(Card.Code.SK));
        deck.add(new Card(Card.Code.SK));
        deck.add(new Card(Card.Code.SK));
        deck.add(new Card(Card.Code.SK));
        deck.add(new Card(Card.Code.SK));
        deck.add(new Card(Card.Code.SK));
        deck.add(new Card(Card.Code.SJ));
        deck.add(new Card(Card.Code.SJ));
        deck.add(new Card(Card.Code.SJ));
        deck.add(new Card(Card.Code.SJ));
        deck.add(new Card(Card.Code.CJ)); deck.add(new Card(Card.Code.CJ));
        deck.add(new Card(Card.Code.CJ)); deck.add(new Card(Card.Code.CJ));
        deck.add(new Card(Card.Code.CJ)); deck.add(new Card(Card.Code.CJ));
        deck.add(new Card(Card.Code.CJ)); deck.add(new Card(Card.Code.CJ));
        deck.add(new Card(Card.Code.CJ)); deck.add(new Card(Card.Code.CJ));
        deck.add(new Card(Card.Code.CJ)); deck.add(new Card(Card.Code.CJ));
        deck.add(new Card(Card.Code.CJ)); deck.add(new Card(Card.Code.CJ));
        deck.add(new Card(Card.Code.CJ)); deck.add(new Card(Card.Code.CJ));
        deck.add(new Card(Card.Code.CJ)); deck.add(new Card(Card.Code.CJ));
        deck.add(new Card(Card.Code.CJ)); deck.add(new Card(Card.Code.CJ));
        deck.add(new Card(Card.Code.CJ)); deck.add(new Card(Card.Code.CJ));
        deck.add(new Card(Card.Code.CJ)); deck.add(new Card(Card.Code.CJ));
        deck.add(new Card(Card.Code.CJ)); deck.add(new Card(Card.Code.CJ));
        deck.add(new Card(Card.Code.CJ)); deck.add(new Card(Card.Code.CJ));
        deck.add(new Card(Card.Code.CJ)); deck.add(new Card(Card.Code.CJ));
        deck.add(new Card(Card.Code.CJ)); deck.add(new Card(Card.Code.CJ));
        deck.add(new Card(Card.Code.CJ)); deck.add(new Card(Card.Code.CJ));
        deck.add(new Card(Card.Code.CJ)); deck.add(new Card(Card.Code.CJ));
        deck.add(new Card(Card.Code.CJ)); deck.add(new Card(Card.Code.CJ));
        deck.add(new Card(Card.Code.CJ)); deck.add(new Card(Card.Code.CJ));
        deck.add(new Card(Card.Code.CJ)); deck.add(new Card(Card.Code.CJ));
        deck.add(new Card(Card.Code.CJ)); deck.add(new Card(Card.Code.CJ));
        deck.add(new Card(Card.Code.CJ)); deck.add(new Card(Card.Code.CJ));
        deck.add(new Card(Card.Code.CJ)); deck.add(new Card(Card.Code.CJ));
        deck.add(new Card(Card.Code.CJ)); deck.add(new Card(Card.Code.CJ));*/
        Collections.shuffle(deck, new Random(System.currentTimeMillis()));

        //distribute the deck to each player according to ttlPlayerCards
        for(int i=0; i<ttlPlayerCards; i++){
            for(int ii=0; ii<players.size(); ii++){
                players.get(ii).addCard(deck.remove(0));
            }
        }
    }

    /**
     * private function to initialize the collections of cards used in this game.
     * This includes a collection of all 104 cards, a deck of cards where players are going to draw new cards from,
     * and a deck of cards where players are going to discard the cards they want to play.
     */
    private void initializeDeck(){
        //initialize all cards used in this game
        allCards = new ArrayList<Card>(ttlCards);

        //initialize each card twice to initialize all 104 cards
        //Initialize the Spade family
        allCards.add(new Card(Card.Code.SK)); allCards.add(new Card(Card.Code.SK));
        allCards.add(new Card(Card.Code.SQ)); allCards.add(new Card(Card.Code.SQ));
        allCards.add(new Card(Card.Code.SJ)); allCards.add(new Card(Card.Code.SJ));
        allCards.add(new Card(Card.Code.S10)); allCards.add(new Card(Card.Code.S10));
        allCards.add(new Card(Card.Code.S9)); allCards.add(new Card(Card.Code.S9));
        allCards.add(new Card(Card.Code.S8)); allCards.add(new Card(Card.Code.S8));
        allCards.add(new Card(Card.Code.S7)); allCards.add(new Card(Card.Code.S7));
        allCards.add(new Card(Card.Code.S6)); allCards.add(new Card(Card.Code.S6));
        allCards.add(new Card(Card.Code.S5)); allCards.add(new Card(Card.Code.S5));
        allCards.add(new Card(Card.Code.S4)); allCards.add(new Card(Card.Code.S4));
        allCards.add(new Card(Card.Code.S3)); allCards.add(new Card(Card.Code.S3));
        allCards.add(new Card(Card.Code.S2)); allCards.add(new Card(Card.Code.S2));
        allCards.add(new Card(Card.Code.SA)); allCards.add(new Card(Card.Code.SA));
        //Initialize the Heart family
        allCards.add(new Card(Card.Code.HK)); allCards.add(new Card(Card.Code.HK));
        allCards.add(new Card(Card.Code.HQ)); allCards.add(new Card(Card.Code.HQ));
        allCards.add(new Card(Card.Code.HJ)); allCards.add(new Card(Card.Code.HJ));
        allCards.add(new Card(Card.Code.H10)); allCards.add(new Card(Card.Code.H10));
        allCards.add(new Card(Card.Code.H9)); allCards.add(new Card(Card.Code.H9));
        allCards.add(new Card(Card.Code.H8)); allCards.add(new Card(Card.Code.H8));
        allCards.add(new Card(Card.Code.H7)); allCards.add(new Card(Card.Code.H7));
        allCards.add(new Card(Card.Code.H6)); allCards.add(new Card(Card.Code.H6));
        allCards.add(new Card(Card.Code.H5)); allCards.add(new Card(Card.Code.H5));
        allCards.add(new Card(Card.Code.H4)); allCards.add(new Card(Card.Code.H4));
        allCards.add(new Card(Card.Code.H3)); allCards.add(new Card(Card.Code.H3));
        allCards.add(new Card(Card.Code.H2)); allCards.add(new Card(Card.Code.H2));
        allCards.add(new Card(Card.Code.HA)); allCards.add(new Card(Card.Code.HA));
        //Initialize the Club family
        allCards.add(new Card(Card.Code.CK)); allCards.add(new Card(Card.Code.CK));
        allCards.add(new Card(Card.Code.CQ)); allCards.add(new Card(Card.Code.CQ));
        allCards.add(new Card(Card.Code.CJ)); allCards.add(new Card(Card.Code.CJ));
        allCards.add(new Card(Card.Code.C10)); allCards.add(new Card(Card.Code.C10));
        allCards.add(new Card(Card.Code.C9)); allCards.add(new Card(Card.Code.C9));
        allCards.add(new Card(Card.Code.C8)); allCards.add(new Card(Card.Code.C8));
        allCards.add(new Card(Card.Code.C7)); allCards.add(new Card(Card.Code.C7));
        allCards.add(new Card(Card.Code.C6)); allCards.add(new Card(Card.Code.C6));
        allCards.add(new Card(Card.Code.C5)); allCards.add(new Card(Card.Code.C5));
        allCards.add(new Card(Card.Code.C4)); allCards.add(new Card(Card.Code.C4));
        allCards.add(new Card(Card.Code.C3)); allCards.add(new Card(Card.Code.C3));
        allCards.add(new Card(Card.Code.C2)); allCards.add(new Card(Card.Code.C2));
        allCards.add(new Card(Card.Code.CA)); allCards.add(new Card(Card.Code.CA));
        //Initialize the Diamond family
        allCards.add(new Card(Card.Code.DK)); allCards.add(new Card(Card.Code.DK));
        allCards.add(new Card(Card.Code.DQ)); allCards.add(new Card(Card.Code.DQ));
        allCards.add(new Card(Card.Code.DJ)); allCards.add(new Card(Card.Code.DJ));
        allCards.add(new Card(Card.Code.D10)); allCards.add(new Card(Card.Code.D10));
        allCards.add(new Card(Card.Code.D9)); allCards.add(new Card(Card.Code.D9));
        allCards.add(new Card(Card.Code.D8)); allCards.add(new Card(Card.Code.D8));
        allCards.add(new Card(Card.Code.D7)); allCards.add(new Card(Card.Code.D7));
        allCards.add(new Card(Card.Code.D6)); allCards.add(new Card(Card.Code.D6));
        allCards.add(new Card(Card.Code.D5)); allCards.add(new Card(Card.Code.D5));
        allCards.add(new Card(Card.Code.D4)); allCards.add(new Card(Card.Code.D4));
        allCards.add(new Card(Card.Code.D3)); allCards.add(new Card(Card.Code.D3));
        allCards.add(new Card(Card.Code.D2)); allCards.add(new Card(Card.Code.D2));
        allCards.add(new Card(Card.Code.DA)); allCards.add(new Card(Card.Code.DA));

        //initialize the deck of cards to be played in this game by shallow copying all cards
        deck = new ArrayList<Card>(allCards);
        usedDeck = new ArrayList<Card>(ttlCards);
    }

    /**
     * private function to initialize all players based on the given total players. The function then calculates the
     * total number of cards for each player based on the total number of players, and also distributes the cards.
     * @param ttlPlayers the total number of players in this game.
     */
    private void initializePlayers(int ttlPlayers, boolean[] isAi){
        //initialize players and set 1st player to be current player
        idxCurrentPlayer = 0;
        players = new ArrayList<Player>(ttlPlayers);
        for(int i=0; i<ttlPlayers; i++){
            if(isAi[i]){
                if(i%2 == 0){
                    players.add(new AIGroup12(i+1, Player.TEAM_1, this));
                }else{
                    players.add(new AIGroup8(i+1, Player.TEAM_2, this));
                }
            }
            else{
                if(i%2 == 0){
                    players.add(new Player(i+1, Player.TEAM_1));
                }else{
                    players.add(new Player(i+1, Player.TEAM_2));
                }
            }

        }

        //determine the total number of cards per player
        if(ttlPlayers == 2)
            ttlPlayerCards = MAX_CARD_PLAYER;
        else if(ttlPlayers == 4)
            ttlPlayerCards = MAX_CARD_PLAYER-1;
        else if(ttlPlayers == 6)
            ttlPlayerCards = MAX_CARD_PLAYER-2;
        else if(ttlPlayers == 8)
            ttlPlayerCards = MAX_CARD_PLAYER-3;
        else
            ttlPlayerCards = MAX_CARD_PLAYER-4;
    }
}
