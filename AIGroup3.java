import java.util.ArrayList;
import java.util.List;

public class AIGroup3 extends AI {
    public int[][] map;

    public AIGroup3(int id, int team, GameModel gameModel){
        super(id, team, gameModel);
        uselessCellIdx = new ArrayList<>();
        map = new int[10][10];
        resetMap();
    }

    //used to clear the map to the beginning of the game
    public void resetMap (){
        for (int x = 0; x<10; x++) {
            for (int y = 0; y < 10; y++) {
                map[x][y] = 0;
            }
        }
    }

    @Override
    public Sequence chooseSequence(List<Sequence> consecutiveSeqs) {
        //choose base on closest to edge
        int choice = 0;
        //get x&y value of farthest point and compare to get direction
        int n = consecutiveSeqs.get(0).getCellIdxAt(0);
        int m = consecutiveSeqs.get(consecutiveSeqs.size()-1).getCellIdxAt(4);
        int x1 = getGameModel().getSequenceBoard().getTwoDimCellIdxX(n), x2 = getGameModel().getSequenceBoard().getTwoDimCellIdxX(m), y1 = getGameModel().getSequenceBoard().getTwoDimCellIdxY(n), y2 = getGameModel().getSequenceBoard().getTwoDimCellIdxY(m);
        int dx = x2 - x1;
        int dy = y2 - y1;

        //choose for vertical
        if (dx == 0) {
            if (y1 < 9 - y2) {
                choice = 0;
            }
            else choice = consecutiveSeqs.size()-1;
        }
        //choose for horizontal and diagonal
        else {
            if (x1 < 9 - x2) {
                choice = 0;
            }
            else choice = consecutiveSeqs.size()-1;
        }
        return consecutiveSeqs.get(choice);
    }

    @Override
    public int discardCard() {
        //always discard dead cards
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
        //maxValue: current maximum value
        //maxIdx: index of maximum value
        double maxValue = 0;
        int maxIdx = 0;
        Card selectedCard = null;
        setTwinCardEnabled(false);

        updateMap();

        //Card value check
        for (int n = 0; n < cards.size(); n++) {
            int temp = 0;
            //One-Eyed Jack check
            if (getCards().get(n).isOneEyedJack()) {
                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 10; j++) {
                        if (map[i][j] <= -1500) {
                            temp = -map[i][j];
                            if (temp > maxValue) {
                                selectedCard = getCards().get(n);
                                maxIdx = getGameModel().getSequenceBoard().getOneDimCellIdx(i, j);
                            }
                        }
                    }
                }
            }
            //Two-Eyed Jack Check
            else if (getCards().get(n).isTwoEyedJack()) {
                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 10; j++) {
                        if (map[i][j] >= 1000) {
                            temp = map[i][j];
                            if (temp > maxValue) {
                                selectedCard = getCards().get(n);
                                maxIdx = getGameModel().getSequenceBoard().getOneDimCellIdx(i, j);
                            }
                        }
                    }
                }
            }
            //Normal card check
            else {
                List<Integer> idx = getGameModel().getSequenceBoard().getAvailableCellsIdx(getCards().get(n));
                //basic outline: check all the cards. check the value and get max. if same pick first card with the value.
                //problem: need to check the how the idx works
                for (int i : idx) {
                    temp = map[getGameModel().getSequenceBoard().getTwoDimCellIdxX(i)][getGameModel().getSequenceBoard().getTwoDimCellIdxY(i)];
                    if (temp >= maxValue) {
                        maxValue = temp;
                        maxIdx = i;
                        selectedCard = getCards().get(n);
                    }
                }
            }
        }

        //Twin Card check
        List<Card> twinCards = getTwinCards();
        if(twinCards != null) {
            if (getGameModel().getSequenceBoard().getAvailableCellsIdx(twinCards.get(0)).size() == 2) {

                List<Integer> idx = getGameModel().getSequenceBoard().getAvailableCellsIdx(twinCards.get(0));
                int temp = 0;
                for (int i : idx) {
                    temp += map[getGameModel().getSequenceBoard().getTwoDimCellIdxX(i)][getGameModel().getSequenceBoard().getTwoDimCellIdxY(i)];
                }
                if (temp >= maxValue) {
                    selectedCard = twinCards.get(0);
                    setTwinCardEnabled(true);
                    return selectedCard;
                }
            }
        }

        setSelectedIdxOneDim(maxIdx);
        return selectedCard;
    }

    public int calculateHeuristic(int x, int y) {
        //x1 ~ x6: variables
        //calc: count mark for each team
        //free: count empty mark. will be used to check if calc value will be added to x1 or x2
        //pot: count consecutive mark near the cell for potential sequence. used to calculate x3 and x5
        //stat: keep the team mark for each direction. 0 is unassigned, 1 is AI team, 2 is opponent
        //isSeq: check if overlap with other sequence
        //potStat: check if it is still consecutive mark near cell or not
        //stop: used to stop checking the direction
        int x1 = 0, x2 = 0, x4 = 1, x5 = 0, x6 = 1;
        double x3 = 0;
        int horCalcX1 = 0, verCalcX1 = 0, diagTopLeftCalcX1 = 0, diagBottomLeftCalcX1 = 0, horCalcX2 = 0, verCalcX2 = 0, diagTopLeftCalcX2 = 0, diagBottomLeftCalcX2 = 0;
        int horFreeX1 = 0, verFreeX1 = 0, diagTopLeftFreeX1 = 0, diagBottomLeftFreeX1 = 0, horFreeX2 = 0, verFreeX2 = 0, diagTopLeftFreeX2 = 0, diagBottomLeftFreeX2 = 0;
        int horPotX1 = 0, verPotX1 = 0, diagTopLeftPotX1 = 0, diagBottomLeftPotX1 = 0, horPotX2 = 0, verPotX2 = 0, diagTopLeftPotX2 = 0, diagBottomLeftPotX2 = 0;
        int leftStat = 0, rightStat = 0, upStat = 0, downStat = 0, upLeftStat = 0, downLeftStat = 0, upRightStat = 0, downRightStat = 0;
        boolean leftIsSeq = false, rightIsSeq = false, upIsSeq = false, downIsSeq = false, upLeftIsSeq = false, downLeftIsSeq = false, upRightIsSeq = false, downRightIsSeq = false;
        boolean leftPotStat = false, rightPotStat = false, upPotStat = false, downPotStat = false, upLeftPotStat = false, downLeftPotStat= false, upRightPotStat = false, downRightPotStat = false;
        boolean leftStop = false, rightStop = false, upStop = false, downStop = false, upLeftStop = false, downLeftStop = false, upRightStop = false, downRightStop = false;
        Cell[][] cells = getGameModel().getSequenceBoard().getCells();

        //mark check
        for (int n = 1; n <= 4; n++) {
            //left check
            if (!leftStop) {
                //check out of bounds
                if (x-n >= 0) {
                    //empty cell check
                    if (cells[x-n][y].getMark() == Cell.MARK_EMPTY) {
                        //check based on the closest team mark on that direction
                        if (leftStat == 0) {
                            horFreeX1++;
                            horFreeX2++;
                        }
                        else if (leftStat == 1) {
                            horFreeX1++;
                        }
                        else if (leftStat == 2) {
                            horFreeX2++;
                        }
                        leftPotStat = false;
                    }
                    //wild cell check
                    else if (cells[x-n][y].isWildCell()) {
                        if (leftStat == 0) {
                            horCalcX1++;
                            horCalcX2++;
                            if (n == 1) {
                                horPotX1++;
                                horPotX2++;
                            }
                        }
                        else if (leftStat == 1) {
                            horCalcX1++;
                            if (leftPotStat) {
                                horPotX1++;
                            }
                        }
                        else if (leftStat == 2) {
                            horCalcX2++;
                            if (leftPotStat) {
                                horPotX2++;
                            }
                        }
                        leftStop = true;
                    }
                    //Player mark check
                    else if (cells[x-n][y].getMark() == this.getTeam()) {
                        //if the first mark found, will change Stat to their team
                        if (leftStat == 0) {
                            leftStat = 1;
                            horCalcX1++;
                            if (cells[x-n][y].isSequence()) {
                                leftIsSeq = true;
                            }
                            if (n == 1) {
                                horPotX1++;
                                leftPotStat = true;
                            }
                        }
                        else if (leftStat == 1) {
                            //check for sequence overlap
                            if (leftIsSeq) {
                                leftStop = true;
                            }
                            else {
                                horCalcX1++;
                                if (cells[x-n][y].isSequence()) {
                                    leftIsSeq = true;
                                }
                                if (leftPotStat) {
                                    horPotX1++;
                                }
                            }
                        }
                        //if the other team mark is found, stop checking the direction
                        else if (leftStat == 2) {
                            leftStop = true;
                        }
                    }
                    //Opponent mark check. must be the last check. may not need the if conditionals
                    else if (cells[x-n][y].getMark() != this.getTeam()) {
                        if (leftStat == 0) {
                            leftStat = 2;
                            horCalcX2++;
                            if (cells[x-n][y].isSequence()) {
                                leftIsSeq = true;
                            }
                            if (n == 1) {
                                horPotX2++;
                                leftPotStat = true;

                            }
                        }
                        else if (leftStat == 1) {
                            leftStop = true;
                        }
                        else if (leftStat == 2) {
                            if (leftIsSeq) {
                                leftStop = true;
                            }
                            else {
                                horCalcX2++;
                                if (cells[x-n][y].isSequence()) {
                                    leftIsSeq = true;
                                }
                                if (leftPotStat) {
                                    horPotX2++;
                                }
                            }
                        }
                    }
                }
                else leftStop = true;
            }
            //right check
            if (!rightStop) {
                if (x+n <= 9) {
                    if (cells[x+n][y].getMark() == Cell.MARK_EMPTY) {
                        if (rightStat == 0) {
                            horFreeX1++;
                            horFreeX2++;
                        }
                        else if (rightStat == 1) {
                            horFreeX1++;
                        }
                        else if (rightStat == 2) {
                            horFreeX2++;
                        }
                        rightPotStat = false;
                    }
                    else if (cells[x+n][y].isWildCell()) {
                        if (rightStat == 0) {
                            horCalcX1++;
                            horCalcX2++;
                            if (n == 1) {
                                horPotX1++;
                                horPotX2++;
                            }
                        }
                        else if (rightStat == 1) {
                            horCalcX1++;
                            if (rightPotStat) {
                                horPotX1++;
                            }
                        }
                        else if (rightStat == 2) {
                            horCalcX2++;
                            if (rightPotStat) {
                                horPotX2++;
                            }
                        }
                        rightStop = true;
                    }
                    else if (cells[x+n][y].getMark() == this.getTeam()) {
                        if (rightStat == 0) {
                            rightStat = 1;
                            horCalcX1++;
                            if (cells[x+n][y].isSequence()) {
                                rightIsSeq = true;
                            }
                            if (n == 1) {
                                horPotX1++;
                                rightPotStat = true;
                            }
                        }
                        else if (rightStat == 1) {
                            if (rightIsSeq) {
                                rightStop = true;
                            }
                            else {
                                horCalcX1++;
                                if (cells[x+n][y].isSequence()) {
                                    rightIsSeq = true;
                                }
                                if (rightPotStat) {
                                    horPotX1++;
                                }
                            }
                        }
                        else if (rightStat == 2) {
                            rightStop = true;
                        }
                    }
                    else if (cells[x+n][y].getMark() != this.getTeam()) {
                        if (rightStat == 0) {
                            rightStat = 2;
                            horCalcX2++;
                            if (cells[x+n][y].isSequence()) {
                                rightIsSeq = true;
                            }
                            if (n == 1) {
                                horPotX2++;
                                rightPotStat = true;
                            }
                        }
                        else if (rightStat == 1) {
                            rightStop = true;
                        }
                        else if (rightStat == 2) {
                            if (rightIsSeq) {
                                rightStop = true;
                            }
                            else {
                                horCalcX2++;
                                if (cells[x+n][y].isSequence()) {
                                    rightIsSeq = true;
                                }
                                if (rightPotStat) {
                                    horPotX2++;
                                }
                            }
                        }
                    }
                }
                else rightStop = true;
            }
            //down check
            if (!downStop) {
                if (y+n <= 9) {
                    if (cells[x][y+n].getMark() == Cell.MARK_EMPTY) {
                        if (downStat == 0) {
                            verFreeX1++;
                            verFreeX2++;
                        }
                        else if (downStat == 1) {
                            verFreeX1++;
                        }
                        else if (downStat == 2) {
                            verFreeX2++;
                        }
                        downPotStat = false;
                    }
                    else if (cells[x][y+n].isWildCell()) {
                        if (downStat == 0) {
                            verCalcX1++;
                            verCalcX2++;
                            if (n == 1) {
                                verPotX1++;
                                verPotX2++;
                            }
                        }
                        else if (downStat == 1) {
                            verCalcX1++;
                            if(downPotStat) {
                                verPotX1++;
                            }
                        }
                        else if (downStat == 2) {
                            verCalcX2++;
                            if (downPotStat) {
                                verPotX2++;
                            }
                        }
                        downStop = true;
                    }
                    else if (cells[x][y+n].getMark() == this.getTeam()) {
                        if (downStat == 0) {
                            downStat = 1;
                            verCalcX1++;
                            if (cells[x][y+n].isSequence()) {
                                downIsSeq = true;
                            }
                            if (n == 1) {
                                verPotX1++;
                                downPotStat = true;
                            }
                        }
                        else if (downStat == 1) {
                            if (downIsSeq) {
                                downStop = true;
                            }
                            else {
                                verCalcX1++;
                                if (cells[x][y+n].isSequence()) {
                                    downIsSeq = true;
                                }
                                if (downPotStat) {
                                    verPotX1++;
                                }
                            }
                        }
                        else if (downStat == 2) {
                            downStop = true;
                        }
                    }
                    else if (cells[x][y+n].getMark() != this.getTeam()) {
                        if (downStat == 0) {
                            downStat = 2;
                            verCalcX2++;
                            if (cells[x][y+n].isSequence()) {
                                downIsSeq = true;
                            }
                            if (n == 1) {
                                verPotX2++;
                                downPotStat = true;
                            }
                        }
                        else if (downStat == 1) {
                            downStop = true;
                        }
                        else if (downStat == 2) {
                            if (downIsSeq) {
                                downStop = true;
                            }
                            else {
                                verCalcX2++;
                                if (cells[x][y+n].isSequence()) {
                                    downIsSeq = true;
                                }
                                if (downPotStat) {
                                    verPotX2++;
                                }
                            }
                        }
                    }
                }
                else downStop = true;
            }
            //up check
            if (!upStop) {
                if (y-n >= 0) {
                    if (cells[x][y-n].getMark() == Cell.MARK_EMPTY) {
                        if (upStat == 0) {
                            verFreeX1++;
                            verFreeX2++;
                        }
                        else if (upStat == 1) {
                            verFreeX1++;
                        }
                        else if (upStat == 2) {
                            verFreeX2++;
                        }
                        upPotStat = false;
                    }
                    else if (cells[x][y-n].isWildCell()) {
                        if (upStat == 0) {
                            verCalcX1++;
                            verCalcX2++;
                            if (n == 1) {
                                verPotX1++;
                                verPotX2++;
                            }
                        }
                        else if (upStat == 1) {
                            verCalcX1++;
                            if (upPotStat) {
                                verPotX1++;
                            }
                        }
                        else if (upStat == 2) {
                            verCalcX2++;
                            if (upPotStat) {
                                verPotX2++;
                            }
                        }
                        upStop = true;
                    }
                    else if (cells[x][y-n].getMark() == this.getTeam()) {
                        if (upStat == 0) {
                            upStat = 1;
                            verCalcX1++;
                            if (cells[x][y-n].isSequence()) {
                                upIsSeq = true;
                            }
                            if (n == 1) {
                                verPotX1++;
                                upPotStat = true;
                            }
                        }
                        else if (upStat == 1) {
                            if (upIsSeq) {
                                upStop = true;
                            }
                            else {
                                verCalcX1++;
                                if (cells[x][y-n].isSequence()) {
                                    upIsSeq = true;
                                }
                                if (upPotStat) {
                                    verPotX1++;
                                }
                            }
                        }
                        else if (upStat == 2) {
                            upStop = true;
                        }
                    }
                    else if (cells[x][y-n].getMark() != this.getTeam()) {
                        if (upStat == 0) {
                            upStat = 2;
                            verCalcX2++;
                            if (cells[x][y-n].isSequence()) {
                                upIsSeq = true;
                            }
                            if (n == 1) {
                                verPotX2++;
                                upPotStat = true;
                            }
                        }
                        else if (upStat == 1) {
                            upStop = true;
                        }
                        else if (upStat == 2) {
                            if (upIsSeq) {
                                upStop = true;
                            }
                            else {
                                verCalcX2++;
                                if (cells[x][y-n].isSequence()) {
                                    upIsSeq = true;
                                }
                                if (upPotStat) {
                                    verPotX2++;
                                }
                            }
                        }
                    }
                }
                else upStop = true;
            }
            //downLeft check
            if (!downLeftStop) {
                if (x-n >= 0 && y+n <= 9) {
                    if (cells[x-n][y+n].getMark() == Cell.MARK_EMPTY) {
                        if (downLeftStat == 0) {
                            diagBottomLeftFreeX1++;
                            diagBottomLeftFreeX2++;
                        }
                        else if (downLeftStat == 1) {
                            diagBottomLeftFreeX1++;
                        }
                        else if (downLeftStat == 2) {
                            diagBottomLeftFreeX2++;
                        }
                        downLeftPotStat = false;
                    }
                    else if (cells[x-n][y+n].isWildCell()) {
                        if (downLeftStat == 0) {
                            diagBottomLeftCalcX1++;
                            diagBottomLeftCalcX2++;
                            if (n == 1) {
                                diagBottomLeftPotX1++;
                                diagBottomLeftPotX2++;
                            }
                        }
                        else if (downLeftStat == 1) {
                            diagBottomLeftCalcX1++;
                            if (downLeftPotStat) {
                                diagBottomLeftPotX1++;
                            }
                        }
                        else if (downLeftStat == 2) {
                            diagBottomLeftCalcX2++;
                            if (downLeftPotStat) {
                                diagBottomLeftPotX2++;
                            }
                        }
                        downLeftStop = true;
                    }
                    else if (cells[x-n][y+n].getMark() == this.getTeam()) {
                        if (downLeftStat == 0) {
                            downLeftStat = 1;
                            diagBottomLeftCalcX1++;
                            if (cells[x-n][y+n].isSequence()) {
                                downLeftIsSeq = true;
                            }
                            if (n == 1) {
                                diagBottomLeftPotX1++;
                                downLeftPotStat = true;
                            }
                        }
                        else if (downLeftStat == 1) {
                            if (downLeftIsSeq) {
                                downLeftStop = true;
                            }
                            else {
                                diagBottomLeftCalcX1++;
                                if (cells[x-n][y+n].isSequence()) {
                                    downLeftIsSeq = true;
                                }
                                if (downLeftPotStat) {
                                    diagBottomLeftPotX1++;
                                }
                            }
                        }
                        else if (downLeftStat == 2) {
                            downLeftStop = true;
                        }
                    }
                    else if (cells[x-n][y+n].getMark() != this.getTeam()) {
                        if (downLeftStat == 0) {
                            downLeftStat = 2;
                            diagBottomLeftCalcX2++;
                            if (cells[x-n][y+n].isSequence()) {
                                downLeftIsSeq = true;
                            }
                            if (n == 1) {
                                diagBottomLeftPotX2++;
                                downLeftPotStat = true;
                            }
                        }
                        else if (downLeftStat == 1) {
                            downLeftStop = true;
                        }
                        else if (downLeftStat == 2) {
                            if (downLeftIsSeq) {
                                downLeftStop = true;
                            }
                            else {
                                diagBottomLeftCalcX2++;
                                if (cells[x-n][y+n].isSequence()) {
                                    downLeftIsSeq = true;
                                }
                                if (downLeftPotStat) {
                                    diagBottomLeftPotX2++;
                                }
                            }
                        }
                    }
                }
                else downLeftStop = true;
            }
            //upRight check
            if (!upRightStop) {
                if (x+n <= 9 && y-n >= 0) {
                    if (cells[x+n][y-n].getMark() == Cell.MARK_EMPTY) {
                        if (upRightStat == 0) {
                            diagBottomLeftFreeX1++;
                            diagBottomLeftFreeX2++;
                        }
                        else if (upRightStat == 1) {
                            diagBottomLeftFreeX1++;
                        }
                        else if (upRightStat == 2) {
                            diagBottomLeftFreeX2++;
                        }
                        upRightPotStat = false;
                    }
                    else if (cells[x+n][y-n].isWildCell()) {
                        if (upRightStat == 0) {
                            diagBottomLeftCalcX1++;
                            diagBottomLeftCalcX2++;
                            if (n == 1) {
                                diagBottomLeftPotX1++;
                                diagBottomLeftPotX2++;
                            }
                        }
                        else if (upRightStat == 1) {
                            diagBottomLeftCalcX1++;
                            if (upRightPotStat) {
                                diagBottomLeftPotX1++;
                            }
                        }
                        else if (upRightStat == 2) {
                            diagBottomLeftCalcX2++;
                            if (upRightPotStat) {
                                diagBottomLeftPotX2++;
                            }
                        }
                        upRightStop = true;
                    }
                    else if (cells[x+n][y-n].getMark() == this.getTeam()) {
                        if (upRightStat == 0) {
                            upRightStat = 1;
                            diagBottomLeftCalcX1++;
                            if (cells[x+n][y-n].isSequence()) {
                                upRightIsSeq = true;
                            }
                            if (n == 1) {
                                diagBottomLeftPotX1++;
                                upRightPotStat = true;
                            }
                        }
                        else if (upRightStat == 1) {
                            if (upRightIsSeq) {
                                upRightStop = true;
                            }
                            else {
                                diagBottomLeftCalcX1++;
                                if (cells[x+n][y-n].isSequence()) {
                                    upRightIsSeq = true;
                                }
                                if (upRightPotStat) {
                                    diagBottomLeftPotX1++;
                                }
                            }
                        }
                        else if (upRightStat == 2) {
                            upRightStop = true;
                        }
                    }
                    else if (cells[x+n][y-n].getMark() != this.getTeam()) {
                        if (upRightStat == 0) {
                            upRightStat = 2;
                            diagBottomLeftCalcX2++;
                            if (cells[x+n][y-n].isSequence()) {
                                upRightIsSeq = true;
                            }
                            if (n == 1) {
                                diagBottomLeftPotX2++;
                                upRightPotStat = true;
                            }
                        }
                        else if (upRightStat == 1) {
                            upRightStop = true;
                        }
                        else if (upRightStat == 2) {
                            if (upRightIsSeq) {
                                upRightStop = true;
                            }
                            else {
                                diagBottomLeftCalcX2++;
                                if (cells[x+n][y-n].isSequence()) {
                                    upRightIsSeq = true;
                                }
                                if (upRightPotStat) {
                                    diagBottomLeftPotX2++;
                                }
                            }
                        }
                    }
                }
                else upRightStop = true;
            }
            //downRight check
            if (!downRightStop) {
                if (x+n <= 9 && y+n <= 9) {
                    if (cells[x+n][y+n].getMark() == Cell.MARK_EMPTY) {
                        if (downRightStat == 0) {
                            diagTopLeftFreeX1++;
                            diagTopLeftFreeX2++;
                        }
                        else if (downRightStat == 1) {
                            diagTopLeftFreeX1++;
                        }
                        else if (downRightStat == 2) {
                            diagTopLeftFreeX2++;
                        }
                        downRightPotStat = false;
                    }
                    else if (cells[x+n][y+n].isWildCell()) {
                        if (downRightStat == 0) {
                            diagTopLeftCalcX1++;
                            diagTopLeftCalcX2++;
                            if (n == 1) {
                                diagTopLeftPotX1++;
                                diagTopLeftPotX2++;
                            }
                        }
                        else if (downRightStat == 1) {
                            diagTopLeftCalcX1++;
                            if (downRightPotStat) {
                                diagTopLeftPotX1++;
                            }
                        }
                        else if (downRightStat == 2) {
                            diagTopLeftCalcX2++;
                            if (downRightPotStat) {
                                diagTopLeftPotX2++;
                            }
                        }
                        downRightStop = true;
                    }
                    else if (cells[x+n][y+n].getMark() == this.getTeam()) {
                        if (downRightStat == 0) {
                            downRightStat = 1;
                            diagTopLeftCalcX1++;
                            if (cells[x+n][y+n].isSequence()) {
                                downRightIsSeq = true;
                            }
                            if (n == 1) {
                                diagTopLeftPotX1++;
                                downRightPotStat = true;
                            }
                        }
                        else if (downRightStat == 1) {
                            if (downRightIsSeq) {
                                downRightStop = true;
                            }
                            else {
                                diagTopLeftCalcX1++;
                                if (cells[x+n][y+n].isSequence()) {
                                    downRightIsSeq = true;
                                }
                                if (downRightPotStat) {
                                    diagTopLeftPotX1++;
                                }
                            }
                        }
                        else if (downRightStat == 2) {
                            downRightStop = true;
                        }
                    }
                    else if (cells[x+n][y+n].getMark() != this.getTeam()) {
                        if (downRightStat == 0) {
                            downRightStat = 2;
                            diagTopLeftCalcX2++;
                            if (cells[x+n][y+n].isSequence()) {
                                downRightIsSeq = true;
                            }
                            if (n == 1) {
                                diagTopLeftPotX2++;
                                downRightPotStat = true;
                            }
                        }
                        else if (downRightStat == 1) {
                            downRightStop = true;
                        }
                        else if (downRightStat == 2) {
                            if (downRightIsSeq) {
                                downRightStop = true;
                            }
                            else {
                                diagTopLeftCalcX2++;
                                if (cells[x+n][y+n].isSequence()) {
                                    downRightIsSeq = true;
                                }
                                if (downRightPotStat) {
                                    diagTopLeftPotX2++;
                                }
                            }
                        }
                    }
                }
                else downRightStop = true;
            }
            //upLeft check
            if (!upLeftStop) {
                if (x-n >= 0 && y-n >= 0) {
                    if (cells[x-n][y-n].getMark() == Cell.MARK_EMPTY) {
                        if (upLeftStat == 0) {
                            diagTopLeftFreeX1++;
                            diagTopLeftFreeX2++;
                        }
                        else if (upLeftStat == 1) {
                            diagTopLeftFreeX1++;
                        }
                        else if (upLeftStat == 2) {
                            diagTopLeftFreeX2++;
                        }
                        upLeftPotStat = false;
                    }
                    else if (cells[x-n][y-n].isWildCell()) {
                        if (upLeftStat == 0) {
                            diagTopLeftCalcX1++;
                            diagTopLeftCalcX2++;
                            if (n == 1) {
                                diagTopLeftPotX1++;
                                diagTopLeftPotX2++;
                            }
                        }
                        else if (upLeftStat == 1) {
                            diagTopLeftCalcX1++;
                            if (upLeftPotStat) {
                                diagTopLeftPotX1++;
                            }
                        }
                        else if (upLeftStat == 2) {
                            diagTopLeftCalcX2++;
                            if (upLeftPotStat) {
                                diagTopLeftPotX2++;
                            }
                        }
                        upLeftStop = true;
                    }
                    else if (cells[x-n][y-n].getMark() == this.getTeam()) {
                        if (upLeftStat == 0) {
                            upLeftStat = 1;
                            diagTopLeftCalcX1++;
                            if (cells[x-n][y-n].isSequence()) {
                                upLeftIsSeq = true;
                            }
                            if (n == 1) {
                                diagTopLeftPotX1++;
                                upLeftPotStat = true;
                            }
                        }
                        else if (upLeftStat == 1) {
                            if (upLeftIsSeq) {
                                upLeftStop = true;
                            }
                            else {
                                diagTopLeftCalcX1++;
                                if (cells[x-n][y-n].isSequence()) {
                                    upLeftIsSeq = true;
                                }
                                if (upLeftPotStat) {
                                    diagTopLeftPotX1++;
                                }
                            }
                        }
                        else if (upLeftStat == 2) {
                            upLeftStop = true;
                        }
                    }
                    else if (cells[x-n][y-n].getMark() != this.getTeam()) {
                        if (upLeftStat == 0) {
                            upLeftStat = 2;
                            diagTopLeftCalcX2++;
                            if (cells[x-n][y-n].isSequence()) {
                                upLeftIsSeq = true;
                            }
                            if (n == 1) {
                                diagTopLeftPotX2++;
                                upLeftPotStat = true;
                            }
                        }
                        else if (upLeftStat == 1) {
                            upLeftStop = true;
                        }
                        else if (upLeftStat == 2) {
                            if (upLeftIsSeq) {
                                upLeftStop = true;
                            }
                            else {
                                diagTopLeftCalcX2++;
                                if (cells[x-n][y-n].isSequence()) {
                                    upLeftIsSeq = true;
                                }
                                if (upLeftPotStat) {
                                    diagTopLeftPotX2++;
                                }
                            }
                        }
                    }
                }
                else upLeftStop = true;
            }
        }

        //x1 and x2 calculation
        if (horCalcX1+horFreeX1 >= 4) {
            x1 =+ horCalcX1;
        }
        if (verCalcX1+verFreeX1 >= 4) {
            x1 =+ verCalcX1;
        }
        if (diagTopLeftCalcX1+diagTopLeftFreeX1 >= 4) {
            x1 =+ diagTopLeftCalcX1;
        }
        if (diagBottomLeftCalcX1+diagBottomLeftFreeX1 >= 4) {
            x1 =+ diagBottomLeftCalcX1;
        }

        if (horCalcX2+horFreeX2 >= 4) {
            x2 =+ horCalcX2;
        }
        if (verCalcX2+verFreeX2 >= 4) {
            x2 =+ verCalcX2;
        }
        if (diagTopLeftCalcX2+diagTopLeftFreeX2 >= 4) {
            x2 =+ diagTopLeftCalcX2;
        }
        if (diagBottomLeftCalcX2+diagBottomLeftFreeX2 >= 4) {
            x2 =+ diagBottomLeftCalcX2;
        }

        //x3 and x5 calculation
        if (horPotX1 >= 4 || verPotX1 >= 4 || diagTopLeftPotX1 >= 4 || diagBottomLeftPotX1 >= 4) {
            x3 = 1;
            //calculation to check if it's open ended or not. only check if there's 4 consecutive team mark to that direction
            //then check if the 5th position from the cell is empty
            if (horPotX1 >= 4) {
                if (x+5 <= 9 && rightPotStat && !rightStop) {
                    if (cells[x+5][y].getMark() == Cell.MARK_EMPTY) {
                        x3 = 1.2;
                    }
                }
                if (x-5 >= 0 && leftPotStat && !leftStop) {
                    if (cells[x-5][y].getMark() == Cell.MARK_EMPTY) {
                        x3 = 1.2;
                    }
                }
            }
            if (verPotX1 >= 4) {
                if (y+5 <= 9 && downPotStat && !downStop) {
                    if (cells[x][y+5].getMark() == Cell.MARK_EMPTY) {
                        x3 = 1.2;
                    }
                }
                if (y-5 >= 0 && upPotStat && !upStop) {
                    if (cells[x][y-5].getMark() == Cell.MARK_EMPTY) {
                        x3 = 1.2;
                    }
                }
            }
            if (diagBottomLeftPotX1 >= 4) {
                if (x-5 >= 0 && y+5 <= 9 && downLeftPotStat && !downLeftStop) {
                    if (cells[x-5][y+5].getMark() == Cell.MARK_EMPTY) {
                        x3 = 1.2;
                    }
                }
                if (x+5 <= 9 && y-5 >=0 && upRightPotStat && !upRightStop) {
                    if (cells[x+5][y-5].getMark() == Cell.MARK_EMPTY) {
                        x3 = 1.2;
                    }
                }
            }
            if (diagTopLeftPotX1 >= 4) {
                if (x+5 <= 9 && y+5 <= 9 && downRightPotStat && !downRightStop) {
                    if (cells[x+5][y+5].getMark() == Cell.MARK_EMPTY) {
                        x3 = 1.2;
                    }
                }
                if (x-5 >= 0 && y-5 >=0 && upLeftPotStat && !upLeftStop) {
                    if (cells[x-5][y-5].getMark() == Cell.MARK_EMPTY) {
                        x3 = 1.2;
                    }
                }
            }
        }
        if (horPotX2 >= 4 || verPotX2 >= 4 || diagTopLeftPotX2 >= 4 || diagBottomLeftPotX2 >= 4) {
            x5 = 1;
        }

        //x4 and x6 check
        if (getGameModel().getSequenceBoard().getNSequence(this.getTeam()) == 1) {
            x4 = 2;
        }
        if (this.getTeam() == Player.TEAM_1) {
            if (getGameModel().getSequenceBoard().getNSequence(Player.TEAM_2) == 1) {
                x6 = 2;
            }
        }
        else {
            if (getGameModel().getSequenceBoard().getNSequence(Player.TEAM_1) == 1) {
                x6 = 2;
            }
        }

        return (x1*10+x2*10+(int)(x3*1500)*x4+(x5*1000)*x6);
    }

    //update the heat map. only calculate empty cell and opponent cell, the rest is 0
    public void updateMap() {
        Cell[][] cells = getGameModel().getSequenceBoard().getCells();
        for (int x = 0; x <= 9; x++) {
            for (int y = 0; y <= 9; y++) {
                if (cells[x][y].getMark() == Cell.MARK_EMPTY){
                    map[x][y] = calculateHeuristic(x, y);
                }
                else if (cells[x][y].getMark() != this.getTeam() && !cells[x][y].isSequence() && !cells[x][y].isWildCell()) {
                    map[x][y] = -calculateHeuristic(x, y);
                }
                else map[x][y] = 0;

            }
        }
        /*for (int y = 0; y <= 9; y++) {
            for (int x = 0; x <= 9; x++) {
                System.out.print(map[x][y] + " | ");
            }
            System.out.println();
        }*/
    }
}
