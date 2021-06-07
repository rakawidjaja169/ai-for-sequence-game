import java.util.ArrayList;
import java.util.List;

public class  AIGroup5 extends AI {

    private GameModel gameModel;

    //There are the final heuristic-factored variables/multipliers

    private static final int def_attackVal = 10;
    private static final int def_defendVal = 15;
    private static final int normalVal = 10;
    private static final int rangeVal = 25;
    private static final int centreVal = 5000;
    private static final double multiplier = 2;
    private static final double fieldMin = 25;
    private static final double fieldPlus = 50;

    private static final int outOfRange = -1;

    private double attackVal, defendVal, bestVal;
    private int idx, bestIndex;

    private final int range = 2;

    public AIGroup5(int id, int team, GameModel gameModel){

        super(id, team, gameModel);

        this.gameModel = gameModel;

        attackVal = def_attackVal; //attackVal is to block other player's sequence.
        defendVal = def_defendVal; //defendVal is to defend the AI and get the most sequence for itself. 

        resetAI();
    }

    public void resetAI(){ //to reset important variables 
        setTwinCardEnabled(false);
        idx = 0;
        bestVal = 0;
        bestIndex = 0;
    }

    @Override
    public int discardCard() {
        //always discard dead cards
        System.out.println("Discard!");
        List<Card> deadCards = getDeadCards(getGameModel().getSequenceBoard());
        if(deadCards == null)
            return 0;

        int ttlDiscardedCards = deadCards.size();
        for(Card eachCard : deadCards){
            getGameModel().setSelectedCard(eachCard);
            getGameModel().discardSelectedCard();
        }
        return ttlDiscardedCards;
    }

    @Override
    public Card evaluateHand() {

        ArrayList<Double> heuristicOfJack = new ArrayList<>();
        ArrayList<Double> heuristicOfCard = new ArrayList<>();
        ArrayList<Integer> cardJackIndex = new ArrayList<>();
        ArrayList<Integer> cardIndex = new ArrayList<>();
        List<Card> cardDeck = getCards();
        List<Card> checkedJacks = new ArrayList<>();
        List<Card> checkedCards = new ArrayList<>();
        List<Card> twinCards = getTwinCards();
        Card selectedCard = getCards().get(0); //gets first card in the deck to begin with

        resetAI();

        for (Card c: cardDeck) { //cycle through each card in the card stack
            double val = 0;
            if (c.isOneEyedJack() || c.isTwoEyedJack()) { //Since it's the priority, jack cards are checked first.
                if (c.isOneEyedJack()) {
                    attackVal = def_attackVal;
                    System.out.println("one-eyed\n");
                    val = getJackBestVal(c); //val = raw, unpolished heuristic value. getJackBestVal = to check which is the best cell to position the Jack based on index.
                    if (val >= 4) {
                        attackVal = def_attackVal + (multiplier*def_attackVal); //attackVal+ for one-eyed.
                    }
                    
                    val += getFieldScanValue(idx, c)*normalVal; //scan the field around the cell and returns the heuristic value
                    val += rangeHeuristic(idx)*rangeVal; //scan the range AI population around the cell (except the cell itself)
                    val += centreVal/centreStrength(idx); //the closer the cell is to the middle (4,4), the better heuristic value

                }

                else { //check for two-eyed jack
                    defendVal = def_defendVal;
                    System.out.println("two-eyed\n");
                    val = getJackBestVal(c); 
                    if (val >= 4) {
                        defendVal = def_defendVal + (multiplier*def_defendVal); //defendVal+ for one-eyed.
                    }
                    
                    val += getFieldScanValue(idx, c)*normalVal;
                    val += rangeHeuristic(idx)*rangeVal;
                    val += centreVal/centreStrength(idx);

                }

                heuristicOfJack.add(val); //Jacks have their own special heuristic list.
                checkedJacks.add(c); //Jacks have their own special card list after checking.
                cardJackIndex.add(idx); //Jack have their own special Index list.

            }
            else {
                //This is for normal cards, they go to a separate list.
                for (Integer idx : gameModel.sequenceBoard.getAvailableCellsIdx(c)) {
                    val += getFieldScanValue(idx, c)*normalVal;
                    val += rangeHeuristic(idx)*rangeVal;
                    val += centreVal/centreStrength(idx);
                    System.out.print(idx+"\n");
                    heuristicOfCard.add(val); //normal Cards have their own heuristic list.
                    checkedCards.add(c); //normal Cards have their own checked card list.
                    cardIndex.add(idx); //normal Cards have their own Index list.
                    System.out.println("Heuristic Value = " + val + "\n");
                    
                }
            }
        }
        System.out.println("Checked Jacks = "+checkedJacks.size()+"\n");
        System.out.println("Checked Normal Cards = "+checkedCards.size()+"\n");
        //This is where the card evaluation begins!
        
        if (!heuristicOfJack.isEmpty()) //Jacks get played first if have.
        {
            System.out.println("There is a jack\n");
            for (int i = 0; i < checkedJacks.size(); i++) {
                if (heuristicOfJack.get(i) > bestVal) {
                    bestVal = heuristicOfJack.get(i);
                    selectedCard = checkedJacks.get(i);
                    bestIndex = cardJackIndex.get(i);
                }
            }
            setSelectedIdxOneDim(bestIndex);
            System.out.println("Success in evaluating Jacks!\n");
        }
        else if (twinCards != null) {
            int val = 0; //twin cards gets played as the second Priority.
            for (Card twin : twinCards) { //in the case of multiple twins, we want the one with the best heuristic.
                if(gameModel.sequenceBoard.getAvailableCellsIdx(twin).size() == 2 ) { //If there are two cells that can be both occupied by the first twin cards in the twinCard deck
                    System.out.println("Here goes one twin!\n");
                    for (Integer i : gameModel.sequenceBoard.getAvailableCellsIdx(twin)) {
                        System.out.println("Here goes one index!\n");
                        val += rangeHeuristic(i)*rangeVal;
                        val += getFieldScanValue(i, twin)*normalVal;
                        
                    }
                    if (val > bestVal) {
                        bestVal = val;
                        selectedCard = twin;
                        setTwinCardEnabled(true);
                    }
                }
            }
        }
        //Rest are normal cards.
        else {
            System.out.println("Normal Cards checking time!\n");
            for (int i = 0; i < checkedCards.size(); i++) {
                if (heuristicOfCard.get(i) > bestVal) {
                    bestVal = heuristicOfCard.get(i);
                    bestIndex = cardIndex.get(i);
                    selectedCard = checkedCards.get(i);
                }
            }
            setSelectedIdxOneDim(bestIndex);
            System.out.println("Success in evaluating card deck!\n");
        }   
        return selectedCard;
    }//end of evaluateHand()

    private boolean hitsEdge(int x, int y) {
        return (x <= outOfRange || y <= outOfRange || x >= SequenceBoard.BOARD_WIDTH || y >= SequenceBoard.BOARD_HEIGHT);
    }

    private int getJackBestVal(Card c) { //best position is determined by the longest sequence the AI can find in the board.
        int tempJackVal = 0, bestJackVal = 0;

        for (Integer i : gameModel.sequenceBoard.getAvailableCellsIdx(c)) { //cycles through all available cells that the Jack can play
            if (c.isOneEyedJack()) {
                tempJackVal = findLongestSequence(i%SequenceBoard.BOARD_WIDTH, i/SequenceBoard.BOARD_HEIGHT,
                getTeam(), 1); //1 - one-eyed Jack type
            } //because one-eyed Jacks attack only other players by discarding, so the own team is in the parameter
            else {
                tempJackVal = findLongestSequence(i%SequenceBoard.BOARD_WIDTH, i/SequenceBoard.BOARD_HEIGHT,
                ((getTeam()%2)+1), 2); //2 - two-eyed Jack type
            } //two-eyed Jacks are defenders, they populate the AIs longest sequence, so the opposing team is in the parameter
            if (tempJackVal > bestJackVal) { // setting the best Jack value (longest Sequence found on the board)
                bestJackVal = tempJackVal;
                idx = i;
            }
        }
        return bestJackVal;
    }

    private int rangeHeuristic(int idx){ //to add more heuristic value weight on AI range crowdedness (except the cell itself)
        int rh_val = 0;
        int x = idx%SequenceBoard.BOARD_WIDTH;
        int y = idx/SequenceBoard.BOARD_HEIGHT;
        int aiMarker = getPlayerMarker(x, y);
        for (int i = y-range; i <= y+range; ++i) {
            for (int j = x-range; j <= x+range; ++j) { //checks the range from standpoint of the corresponding cel
                if (!hitsEdge(i, j)) {
                    if (gameModel.sequenceBoard.getCellAt(i, j).isFilled() && i != y && j != x && getPlayerMarker(i, j) == aiMarker) { //check if Filled and is AI marker or not; except for the current cell
                        rh_val += rangeVal;
                    }
                }
            }
        }
        return rh_val;
    }

    private int centreStrength (int idx){ //The more centre the placement, the better heuristic value the card yields.
        int x = idx%SequenceBoard.BOARD_WIDTH;
        int y = idx/SequenceBoard.BOARD_HEIGHT;
        int x_strength, y_strength;

        if (x > 4) {x_strength = x-4;}
        else if (x <= 4) {x_strength = 4-x;}
        if (y > 4) {y_strength = y-4;}
        else if (y <= 4) {y_strength = 4-y;}

        x_strength =+ 1;
        y_strength =+ 1;

        return (x_strength*y_strength); //the lesser the value, the better.

    }

    private int findLongestSequence(int x, int y, int ignoredTeamNumber, int jackType){ //Checks the longest sequence in that movement pattern.
        
        ArrayList<Boolean> marker = new ArrayList<>();
        ArrayList<Integer> counter = new ArrayList<>();
        ArrayList<Integer> crowdCounter = new ArrayList<>();
        int counterTracker0 = 0; //counterTrackers keep track of the longest sequence during a 5-Cell scan.
        int counterTracker1 = 0; //counterTracker makes sequences like xxoxx counted as 4, and xooox counted as 2.
        int counterTracker2 = 0; //after a 5-Cell scan, counterTracker will reset.
        int counterTracker3 = 0;   
        
        for (int i=0; i<4; i++) {
            marker.add(false); //Marker represents the different movement patterns, and whether out of Range is achieved. 
            counter.add(0); //Counter keeps the longest sequence in that movement pattern. 
            crowdCounter.add(0); //crowdCounter represents the crowdedness of enemies (or AI, depending on the type of jack) in the corresponding movement pattern. 
            // ^ crowdCounter factors into the bestSequence for Jack, if there are two or more Counters with the same sequence size.
             
            //{false, false, false, false}
            //{0, 0, 0, 0}
            //{0, 0, 0, 0}
        }

        //One-eyed Jack == ignoredTeamNumber is AI themselves, crowdCounter will count AI markers.
        //Two-eyed Jack == ignoredTeamNumber is opposing team, crowdCounter will count opposing team markers.
        

        for (int i = -4; i <= 0; i++) { //Quadrant scanning
            for (int j = 0; j < 5; j++) {
                if (!marker.get(0)) { //(LeftEdge to RightEdge - Horiz)
                    if (!hitsEdge(x+i+j, y)) {
                        if (getPlayerMarker(x+i+j, y) == ignoredTeamNumber) {
                            crowdCounter.set(0, crowdCounter.get(0)+1); //adds ignored team crowdedness of the corresponding movement pattern.
                        }
                        else {
                            counterTracker0++; //adds sequence count
                        }
                        if (j == 4) { //iteration is already on cell number 5
                            if (counterTracker0 > counter.get(0)) { //if currently tracked sequence counter is larger previously kept longest sequence
                                counter.set(0, counterTracker0); //then replace
                            }
                            counterTracker0 = 0; //reset
                        }
                    }
                    else { //edge of board is hit
                        if (counterTracker0 > counter.get(0)) { 
                            counter.set(0, counterTracker0); 
                        }
                        marker.set(0, true); //disables traversing in that movement pattern for the following iterations.
                    }

                }
                if (!marker.get(1)) { //(TopEdge to BottomEdge - Verti)
                    if (!hitsEdge(x, y+i+j)) {
                        if (getPlayerMarker(x, y+i+j) == ignoredTeamNumber) {
                            crowdCounter.set(1, crowdCounter.get(1)+1);
                        }
                        else {
                            counterTracker1++;
                        }
                        if (j == 4) {
                            if (counterTracker1 > counter.get(1)) {
                                counter.set(0, counterTracker1);
                            }
                            counterTracker1 = 0;
                        }
                    }
                    else {
                        if (counterTracker1 > counter.get(1)) {
                            counter.set(1, counterTracker1);
                        }
                        marker.set(1, true);
                    }
                }
                if (!marker.get(2)) { //(TopLeftEdge to BottomRightEdge - Diag)
                    if (!hitsEdge(x+i+j, y+i+j)) {
                        if (getPlayerMarker(x, y+i+j) == ignoredTeamNumber) {
                            crowdCounter.set(2, crowdCounter.get(2)+1);
                        }
                        else {
                            counterTracker2++;
                        }
                        if (j == 4) {
                            if (counterTracker2 > counter.get(2)) {
                                counter.set(0, counterTracker2);
                            }
                            counterTracker2 = 0;    
                        }
                    }
                    else {
                        if (counterTracker2 > counter.get(2)) {
                            counter.set(2, counterTracker2);
                        }
                        marker.set(2, true);
                    }
                }
                if (!marker.get(3)) { //(BottomLeftEdge to TopRightEdge - Diag)
                    if (!hitsEdge(x+i+j, y-i-j)) {
                        if (getPlayerMarker(x+i+j, y-i-j) == ignoredTeamNumber) {
                            crowdCounter.set(3, crowdCounter.get(3)+1);
                        }
                        else {
                            counterTracker3++;
                        }
                        if (j == 4) {
                            if (counterTracker3 > counter.get(3)) {
                                counter.set(0, counterTracker3);
                            }
                            counterTracker3 = 0;
                        }
                    }
                    else {
                        if (counterTracker3 > counter.get(3)) {
                            counter.set(3, counterTracker3);
                        }
                        marker.set(3, true);
                    }
                }
            }
        }
        int bestCrowdCount = crowdCounter.get(0);
        int bestValLongSeq = counter.get(0);
        for (int i=0; i<counter.size(); i++){
            if (jackType == 1) //1 eyed
            {
                if (counter.get(i) > bestValLongSeq && crowdCounter.get(i) >= bestCrowdCount) { //the longer the enemySequence, and the more AI crowdedness, the better. Better isolation (enemy cancelling and ally population).
                    bestValLongSeq = counter.get(i);
                    bestCrowdCount = crowdCounter.get(i);
                }
            }
            else { //jackType == 2 eyed
                if (counter.get(i) > bestValLongSeq && crowdCounter.get(i) <= bestCrowdCount) { //the longer the AISequence, and the more the enemy crowdedness, the better. Better isolation (enemy cancelling and ally population).
                    bestValLongSeq = counter.get(i);
                    bestCrowdCount = crowdCounter.get(i);
                }
            }

        }

        return bestValLongSeq;
    }

    

    private double getFieldScanValue(int idx, Card c) { //to scan horizontally and vertically, for the population of enemy marked cells. This factors in the heuristic value.
        
        double val = 0;
        
        int temp_x = idx%SequenceBoard.BOARD_WIDTH;
        int temp_y = idx%SequenceBoard.BOARD_HEIGHT;
        int y = temp_y;
        int x = temp_x;
        int marker = getPlayerMarker(temp_x, temp_y); //set the current cell's marker.
        for (x = temp_x+1; x <= temp_x+range; x++) { //(Movement = Right, Horiz)
            if (!hitsEdge(x, y)) {
                if (getPlayerMarker(x, y) == 0) { //if marker at x,y is empty, adds heuristic value, we want to play aggressive defense.
                    val += fieldPlus;
                }
                if (marker != getPlayerMarker(x, y)) { //if marker at iterated cell is not ally's, heuristic value deducts.
                    val -= fieldMin;
                }
                else if (marker == getPlayerMarker(x, y)) { //if marker variable is the same as other cell markers, heuristic value+. 
                    if (c.isOneEyedJack()) { //added boost if the card is a jack type
                        val += attackVal;
                    }
                    else if (c.isTwoEyedJack()) { //added boost if the card is a jack type
                        val += defendVal;
                    }
                    val += fieldPlus;
                }
                else { 
                    break;
                }
            }
        }
        
        for (x = temp_x-1; x >= temp_x-range; x--) { //(Movement = Left, Horiz)
            if (!hitsEdge(x, y)) {
                if (getPlayerMarker(x, y) == 0) { //if marker at x,y is empty, adds heuristic value, we want to play aggressive defense.
                    val += fieldPlus;
                }
                
                if (marker != getPlayerMarker(x, y)) { //if marker at iterated cell is not ally's, heuristic value deducts.
                    val -= fieldMin;
                }
                else if (marker == getPlayerMarker(x, y)) { //if marker variable is the same as other cell markers, heuristic value+. 
                    if (c.isOneEyedJack()) {
                        val += attackVal;
                    }
                    else if (c.isTwoEyedJack()) {
                        val += defendVal;
                    }
                    val += fieldPlus;
                }
                else {
                    break;
                }
            }
        }
        
        for (y = temp_y+1; y <= temp_y+range; y++) { //(Movement = Down, Verti)
            if (!hitsEdge(x, y)) {
                if (getPlayerMarker(x, y) == 0) { //if marker at x,y is empty, adds heuristic value, we want to play aggressive defense.
                    val += fieldPlus;
                }
                
                if (marker != getPlayerMarker(x, y)) { //if marker at iterated cell is not ally's, heuristic value deducts.
                    val -= fieldMin;
                }
                else if (marker == getPlayerMarker(x, y)) { //if marker variable is the same as other cell markers, heuristic value+. 
                    if (c.isOneEyedJack()) {
                        val += attackVal;
                    }
                    else if (c.isTwoEyedJack()) {
                        val += defendVal;
                    }
                    val += fieldPlus;
                }
                else {
                    break;
                }
            }
        }
        
        for (y = temp_y-1; y >= temp_y+range; y--) { //(Movement = Up, Verti)
            if (!hitsEdge(x, y)) {
                if (getPlayerMarker(x, y) == 0) { //if marker at x,y is empty, adds heuristic value, we want to play aggressive defense.
                    val += fieldPlus;
                }
                if (marker != getPlayerMarker(x, y)) { //if marker at iterated cell is not ally's, heuristic value deducts.
                    val -= fieldMin;
                }
                else if (marker == getPlayerMarker(x, y)) { //if marker variable is the same as other cell markers, heuristic value+. 
                    if (c.isOneEyedJack()) {
                        val += attackVal;
                    }
                    else if (c.isTwoEyedJack()) {
                        val += defendVal;
                    }
                    val += fieldPlus;
                }
                else {
                    break;
                }
            }
        }

        //Below is for Diagonal Movement
        y = temp_y;
        x = temp_x;
        for (int i = 1; i <= range; i++) { 
            //Diagonal Left-Up
            int newX = x-i;
            int newY = y-i;
            if (!hitsEdge(newX, newY)) {
                if (getPlayerMarker(newX, newY) == 0) { //if marker at x,y is empty, adds heuristic value, we want to play aggressive defense.
                    val += fieldPlus;
                }
                if (marker != getPlayerMarker(x, y)) { //if marker at iterated cell is not ally's, heuristic value deducts.
                    val -= fieldMin;
                }
                else if (marker == getPlayerMarker(x, y)) { //if marker variable is the same as other cell markers, heuristic value+. 
                    if (c.isOneEyedJack()) {
                        val += attackVal;
                    }
                    else if (c.isTwoEyedJack()) {
                        val += defendVal;
                    }
                    val += fieldPlus;
                }
                else {
                    break;
                }
            }
            //Diagonal Left-Down
            newX = x-i;
            newY = y+i;
            if (!hitsEdge(newX, newY)) {
                if (getPlayerMarker(newX, newY) == 0) { //if marker at x,y is empty, adds heuristic value, we want to play aggressive defense.
                    val += fieldPlus;
                }
                if (marker != getPlayerMarker(x, y)) { //if marker at iterated cell is not ally's, heuristic value deducts.
                    val -= fieldMin;
                }
                else if (marker == getPlayerMarker(x, y)) { //if marker variable is the same as other cell markers, heuristic value+. 
                    if (c.isOneEyedJack()) {
                        val += attackVal;
                    }
                    else if (c.isTwoEyedJack()) {
                        val += defendVal;
                    }
                    val += fieldPlus;
                }
                else {
                    break;
                }
            }
            //Diagonal Right-Down
            newX = x+i;
            newY = y+i;
            if (!hitsEdge(newX, newY)) {
                if (getPlayerMarker(newX, newY) == 0) { //if marker at x,y is empty, adds heuristic value, we want to play aggressive defense.
                    val += fieldPlus;
                }
                if (marker != getPlayerMarker(x, y)) { //if marker at iterated cell is not ally's, heuristic value deducts.
                    val -= fieldMin;
                }
                else if (marker == getPlayerMarker(x, y)) { //if marker variable is the same as other cell markers, heuristic value+. 
                    if (c.isOneEyedJack()) {
                        val += attackVal;
                    }
                    else if (c.isTwoEyedJack()) {
                        val += defendVal;
                    }
                    val += fieldPlus;
                }
                else {
                    break;
                }
            }
            //Diagonal Right-Up
            newX = x+i;
            newY = y-i;
            if (!hitsEdge(newX, newY)) {
                if (getPlayerMarker(newX, newY) == 0) { //if marker at x,y is empty, adds heuristic value, we want to play aggressive defense.
                    val += fieldPlus;
                }
                if (marker != getPlayerMarker(x, y)) { //if marker at iterated cell is not ally's, heuristic value deducts.
                    val -= fieldMin;
                }
                else if (marker == getPlayerMarker(x, y)) { //if marker variable is the same as other cell markers, heuristic value+. 
                    if (c.isOneEyedJack()) {
                        val += attackVal;
                    }
                    else if (c.isTwoEyedJack()) {
                        val += defendVal;
                    }
                    val += fieldPlus;
                }
                else {
                    break;
                }
            }
        }
    
        return val;
    
    }

    private int getPlayerMarker(int x, int y) { //get player marker
        return gameModel.sequenceBoard.getCellAt(x, y).getMark();
    }
}