import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.lang.model.util.ElementScanner6;

public class AIGroup12 extends AI {
    public AIGroup12(int id, int team, GameModel gameModel) {
        super(id, team, gameModel);
        uselessCellIdx = new ArrayList<>();
    }

    private int totalSum(int heuristic, int team, int best) {
        // check the total sum (heuristic)
        if (heuristic == 5 * team) {
            best = 10;
        } else if (heuristic == 4 * team) {
            best = 8;
        } else if (heuristic == 3 * team) {
            best = 5;
        }

        return best;
    }

    //Checker method checks wild cell, sequence, and sum all the value
    private int checker(int column, int row, int coordX, int coordY, int heuristic, int temp, int team, boolean isSequence, int direction) {
        Cell[][] cells = getGameModel().getSequenceBoard().getCells();

        switch (direction) {
            //Checking 5 cells next to the target with direction
            //1: Check horizontally rightwards
            //2: Check vertically downwards
            //3: Check diagonally from top right to bottom left
            //4: Check diagonally from top left to bottom right
            case 1:
                for (int i = 0; i <= 4; i++) {
                    if (cells[column + i][coordY].isWildCell())
                        temp = team;

                    if (cells[column + i][coordY].isSequence()) {
                        if (isSequence)
                            break;
                        isSequence = true;
                    }

                    if (cells[column + i][coordY].getMark() == team)
                        heuristic += cells[column + i][coordY].getMark();
                    if (column + i == coordX)
                        heuristic += team;
                }
                heuristic += temp;
                break;

            case 2:
                for (int i = 0; i <= 4; i++) {
                    if (cells[coordX][row + i].isWildCell())
                        temp = team;

                    if (cells[coordX][row + i].isSequence()) {
                        if (isSequence)
                            break;
                        isSequence = true;
                    }

                    if (cells[coordX][row + i].getMark() == team)
                        heuristic += cells[coordX][row + i].getMark();
                    if (row + i == coordY)
                        heuristic += team;
                }
                heuristic += temp;
                break;

            case 3:
                for (int i = 0; i <= 4; i++) {
                    if (cells[column - i][row + i].isWildCell())
                        temp = team;

                    if (cells[column - i][row + i].isSequence()) {
                        if (isSequence)
                            break;
                        isSequence = true;
                    }

                    if (cells[column - i][row + i].getMark() == team)
                        heuristic += cells[column - i][row + i].getMark();
                    if (row + i == coordY && column - i == coordX)
                        heuristic += team;
                }
                heuristic += temp;
                break;

            case 4:
                for (int i = 0; i <= 4; i++) {
                    if (cells[column + i][row + i].isWildCell())
                        temp = team;

                    if (cells[column + i][row + i].isSequence()) {
                        if (isSequence)
                            break;
                        isSequence = true;
                    }

                    if (cells[column + i][row + i].getMark() == team)
                        heuristic += cells[column + i][row + i].getMark();
                    if (row + i == coordY && column + i == coordX)
                        heuristic += team;
                }
                heuristic += temp;
                break;
        }
        return heuristic;
    }

    //Play the twin cards
    private Card playTwinCards(List<Card> twinCards, Card selectedCard) {
        if (twinCards != null) {
            if (getGameModel().getSequenceBoard().getAvailableCellsIdx(twinCards.get(0)).size() == 2) {
                selectedCard = twinCards.get(0);
                setTwinCardEnabled(true);
            }
        }
        return selectedCard;
    }

    @Override
    public int discardCard() {
        // always discard dead cards
        List<Card> deadCards = getDeadCards(getGameModel().getSequenceBoard());
        if (deadCards == null)
            return 0;

        // If the player has jack one eyed then stop the discard
        for(Card c: getCards()){
            if (c.isOneEyedJack()){
                return 0;
            }
        }

        int totalDeadCards = deadCards.size();
        for (Card eachCard : deadCards) {
            getGameModel().setSelectedCard(eachCard);
            getGameModel().discardSelectedCard();
        }
        return totalDeadCards;
    }

    @Override
    public Card evaluateHand() {
        Card selectedCard = null;

        int tempValue = 0;
        int tempValue2 = 0;
        int bestValue = 1;

        List[] cellIdx = new List[getCards().size()];
        List<Card> twinCards = getTwinCards();

        setTwinCardEnabled(false);

        //Check if there is Twin Cards
        selectedCard = playTwinCards(twinCards, selectedCard);
        if(selectedCard != null) {
            return selectedCard;
        }


        for (int i = 0; i < getCards().size(); i++) {
            cellIdx[i] = getGameModel().getSequenceBoard().getAvailableCellsIdx(getCards().get(i));

            for (int j = 0; j < cellIdx[i].size(); j++) {
                int coordX = SequenceBoard.getTwoDimCellIdxX((Integer) cellIdx[i].get(j));
                int coordY = SequenceBoard.getTwoDimCellIdxY((Integer) cellIdx[i].get(j));
                int enemyCount = this.getTeam() % 2 + 1;

                if (enemyCount == 0) {
                    enemyCount = 2;
                }

                //One Eyed Jack
                if (getCards().get(i).isOneEyedJack()) {
                    tempValue = findHeuristic(coordX, coordY, enemyCount);
                }
                //Two Eyed Jack
                if (getCards().get(i).isTwoEyedJack()) {
                    tempValue2 = findHeuristic(coordX, coordY, this.getTeam());
                }
                if (tempValue2 > tempValue) {
                    tempValue = tempValue2;
                }
                if (tempValue > bestValue) {
                    bestValue = tempValue;
                    selectedCard = getCards().get(i);
                    setSelectedIdxOneDim((Integer) cellIdx[i].get(j));
                } else if (tempValue == bestValue && bestValue > 1) {
                    if (selectedCard.isTwoEyedJack()) {
                        selectedCard = getCards().get(i);
                        setSelectedIdxOneDim((Integer) cellIdx[i].get(j));
                    }
                }
            }
        }
        if (bestValue == 1) {
            for (int i = 0; i < getCards().size(); i++) {
                cellIdx[i] = getGameModel().getSequenceBoard().getAvailableCellsIdx(getCards().get(i));
                for (int j = 0; j < cellIdx[i].size(); j++) {
                    int coordX = SequenceBoard.getTwoDimCellIdxX((Integer) cellIdx[i].get(j));
                    int coordY = SequenceBoard.getTwoDimCellIdxY((Integer) cellIdx[i].get(j));
                    if (coordX >=3 && coordX <= 6 && coordY >=3 && coordY <= 6) {
                        selectedCard = getCards().get(i);
                        setSelectedIdxOneDim((Integer) cellIdx[i].get(j));
                        return selectedCard;
                    } else if (coordX == 0 || coordX == 9 || coordY == 0 || coordY == 9) {
                        selectedCard = getCards().get(i);
                        setSelectedIdxOneDim((Integer) cellIdx[i].get(j));
                        return selectedCard;
                    }
                }
            }
            for (int i = 0; i < getCards().size(); i++) {
                selectedCard = getCards().get(i);
                if (!selectedCard.isOneEyedJack() && !selectedCard.isTwoEyedJack()) {
                    setSelectedIdxOneDim(getGameModel().getSequenceBoard().getAvailableCellsIdx(getCards().get(i)).get(0));
                    return selectedCard;
                }
            }
        }
        return selectedCard;
    }

    public int findHeuristic(int coordX, int coordY, int team) {
        int heuristic, temp, best = 1;
        boolean isSequence = false;

        //Check horizontally
        for (int i = -4; i <= 0; i++) {
            isSequence = false;
            heuristic = temp = 0;
            int column = coordX + i;

            //Check Border
            if (column < 0)
                continue;
            if (column + 4 >= SequenceBoard.BOARD_WIDTH)
                break;

            heuristic = checker(column, 0, coordX, coordY, heuristic, temp, team, isSequence, 1);

            best = totalSum(heuristic, team, best);
        }

        //Check vertically
        for (int i = -4; i <= 0; i++) {
            isSequence = false;
            heuristic = temp = 0;
            int row = coordY + i;

            //Check Border
            if (row < 0)
                continue;
            if (row + 4 >= SequenceBoard.BOARD_HEIGHT)
                break;

            heuristic = checker(0, row, coordX, coordY, heuristic, temp, team, isSequence, 2);

            best = totalSum(heuristic, team, best);
        }

        //Check diagonally from top right to bottom left
        for (int i = 4; i >= 0; i--) {
            isSequence = false;
            heuristic = temp = 0;
            int column = coordX + i;
            int row = coordY - i;

            //Check Border
            if (row < 0 || column >= SequenceBoard.BOARD_WIDTH)
                continue;
            if (row + 4 >= SequenceBoard.BOARD_HEIGHT || column - 4 < 0)
                break;

            heuristic = checker(column, row, coordX, coordY, heuristic, temp, team, isSequence, 3);

            best = totalSum(heuristic, team, best);
        }

        //Check diagonally from top left to bottom right
        for (int i = -4; i <= 0; i++) {
            isSequence = false;
            heuristic = temp = 0;
            int column = coordX + i;
            int row = coordY + i;

            //Check Border
            if (row < 0 || column < 0)
                continue;
            if (row + 4 >= SequenceBoard.BOARD_HEIGHT || column + 4 >= SequenceBoard.BOARD_WIDTH)
                break;

            heuristic = checker(column, row, coordX, coordY, heuristic, temp, team, isSequence, 4);

            best = totalSum(heuristic, team, best);
        }
        return best;
    }
}
