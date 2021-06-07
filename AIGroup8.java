import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class AIGroup8 extends AI {

	public AIGroup8(int id, int team, GameModel gameModel){
        super(id, team, gameModel);
        uselessCellIdx = new ArrayList<>();
    }

    @Override
    public int discardCard() {
        List<Card> deadCards = getDeadCards(getGameModel().getSequenceBoard());
        if(deadCards == null)
            return 0;

        int DiscardedCards = deadCards.size();
        for(Card eachCard : deadCards){
            getGameModel().setSelectedCard(eachCard);
            getGameModel().discardSelectedCard();
        }
        return DiscardedCards;
    }

    //Note
    //Score will combining all the heuristic score 
    @Override 
    public Card evaluateHand() {
        ArrayList<Integer> CardPosi = new ArrayList<>();
        ArrayList<Integer> TotalHeu = new ArrayList<>();
        ArrayList<Card> AvailCard = new ArrayList<>();
        List<Card> HandCard = this.getCards();
        Card selectedCard = null;
        Integer Score;
        Integer onHand, onBoard;
        setTwinCardEnabled(false);
        //Heuristic For Board
        int Heuristic[][] = {{0 , 5, 5, 5, 5, 5, 5, 5, 5, 0},
				            {5 , 4, 4, 4, 4, 4, 4, 4, 4, 5},
				            {5 , 4, 3, 3, 3, 3, 3, 3, 4, 5},
				            {5 , 4, 3, 2, 2, 2, 2, 3, 4, 5},
			            	{5 , 4, 3, 2, 1, 1, 2, 3, 4, 5},
				            {5 , 4, 3, 2, 1, 1, 2, 3, 4, 5},
				            {5 , 4, 3, 2, 2, 2, 2, 3, 4, 5},
				            {5 , 4, 3, 3, 3, 3, 3, 3, 4, 5},
				            {5 , 4, 4, 4, 4, 4, 4, 4, 4, 5},
				            {0 , 5, 5, 5, 5, 5, 5, 5, 5, 0}};


        //Always play twin cards
        List<Card> twinCards = getTwinCards();
        if(twinCards != null){
            if(getGameModel().getSequenceBoard().getAvailableCellsIdx(twinCards.get(0)).size() == 2 ) {
                selectedCard = twinCards.get(0);
                setTwinCardEnabled(true);
                return selectedCard;
            }
        }

        // Try to check cards in hand
        for (Card i : HandCard) {
			List<Integer> each = getGameModel().getSequenceBoard().getAvailableCellsIdx(i);
			for(Integer j : each) {
				int x = j%SequenceBoard.BOARD_WIDTH;
				int y = j%SequenceBoard.BOARD_HEIGHT;
                int TwoEyed = 100;
                int Block = 1000;

                //If there is Two eyed jack
                if (i.isTwoEyedJack()) {			
                        Score = Heuristic[x][y] + TwoEyed + PotentialSequence(x,y);
                        TotalHeu.add(Score);
				        System.out.print(Score);
				        AvailCard.add(i);
				        CardPosi.add(j);
                }

                //If there is One eyed jack
                else if (i.isOneEyedJack()) {
                    if (EnemyHeuristic(x,y) == 2){
                        Score = EnemyHeuristic(x,y) + Block;
                        TotalHeu.add(Score);
				        System.out.print(Score);
				        AvailCard.add(i);
				        CardPosi.add(j);
                    }
                }

                //Else if theres no two/one eyed jack but enemy has potential sequence
                // For Defense
                else if (EnemyHeuristic(x,y) == 4) {
                    Score = EnemyHeuristic(x,y) + Block;
                    TotalHeu.add(Score);
				    System.out.print(Score);
				    AvailCard.add(i);
				    CardPosi.add(j);
                }

                //Else if theres no two/one eyed jack and nothing to defense
                //Try to build sequence
                else
                {
                    Score = Heuristic[x][y] + PotentialSequence(x,y);
                    TotalHeu.add(Score);
				    System.out.print(Score);
				    AvailCard.add(i);
				    CardPosi.add(j);
                }
			}
		}
        onHand = Collections.max(TotalHeu);
        onBoard = TotalHeu.indexOf(Collections.max(TotalHeu));
        selectedCard = AvailCard.get(onBoard);
        setSelectedIdxOneDim(CardPosi.get(onBoard));
        System.out.println();
        return selectedCard;
    }

    //Check The potential sequence for our AI Team
    public int PotentialSequence(int x, int y) {
        //prepare the variable
        int HeuVal = 0;
        int temp;
        boolean isSequence = false;
        Cell[][] cells = getGameModel().getSequenceBoard().getCells();
        
        //check column (LINE_EAST)
        for(int incCol=-4; incCol<=0; incCol++){
            isSequence = false;
            temp = 0;
            int idxCol = x+incCol;

            //if out of bound (leftmost column), then next iteration
            if(idxCol < 0)
                continue;
            //if out of bound (rightmost column), then break (do not need to check next column)
            if(idxCol + 4 >= SequenceBoard.BOARD_WIDTH)
                break;

            //check the next 5 cells to the east
            for(int n=0; n<=4; n++){
                //if there is a wild cell
                if(cells[idxCol + n][y].isWildCell()){
                    temp += 5;
                }

                //if there is a sequence AND this is not the first encounter (more than 1 cell are already a sequence)
                if(cells[idxCol + n][y].isSequence()){
                    if(isSequence)
                        break;
                    isSequence = true;
                    temp += 1000;
                }

            }
            HeuVal += temp; 
        }

        //check row (LINE_SOUTH)
        for(int incRow=-4; incRow<=0; incRow++){
            isSequence = false;
            temp = 0;
            int idxRow = y+incRow;

            //if out of bound (leftmost column), then next iteration
            if(idxRow < 0)
                continue;
            //if out of bound (rightmost column), then break (do not need to check next column)
            if(idxRow + 4 >= SequenceBoard.BOARD_HEIGHT)
                break;

            //check the next 5 cells to the east
            for(int n=0; n<=4; n++){
                //if there is a wild cell
                if(cells[x][idxRow+n].isWildCell()){
                    temp += 5;
                }

                //if there is a sequence AND this is not the first encounter (more than 1 cell are already a sequence)
                if(cells[x][idxRow+n].isSequence()){
                    if(isSequence)
                        break;
                    isSequence = true;
                    temp += 1000;
                }
            }
            HeuVal += temp;
        }

        //check row (LINE_SOUTH_WEST)
        for(int inc=4; inc>=0; inc--){
            isSequence = false;
            temp = 0;
            int idxCol = x+inc;
            int idxRow = y-inc;

            //if out of bound (leftmost column), then next iteration
            if(idxRow < 0 || idxCol >= SequenceBoard.BOARD_WIDTH)
                continue;
            //if out of bound (rightmost column), then break (do not need to check next column)
            if(idxRow + 4 >= SequenceBoard.BOARD_HEIGHT || idxCol - 4 < 0)
                break;

            //check the next 5 cells to the east
            for(int n=0; n<=4; n++){
                //if there is a wild cell
                if(cells[idxCol - n][idxRow + n].isWildCell()){
                    temp += 5;
                }

                //if there is a sequence AND this is not the first encounter (more than 1 cell are already a sequence)
                if(cells[idxCol - n][idxRow + n].isSequence()){
                    if(isSequence)
                        break;
                    isSequence = true;
                    temp += 1000;
                }
            }
            HeuVal += temp;
        }

        //check row (LINE_SOUTH_EAST)
        for(int inc=-4; inc<=0; inc++){
            isSequence = false;
            temp = 0;
            int idxCol = x+inc;
            int idxRow = y+inc;

            //if out of bound (leftmost column), then next iteration
            if(idxRow < 0 || idxCol < 0 )
                continue;
            //if out of bound (rightmost column), then break (do not need to check next column)
            if(idxRow + 4 >= SequenceBoard.BOARD_HEIGHT || idxCol + 4 < SequenceBoard.BOARD_WIDTH)
                break;

            //check the next 5 cells to the east
            for(int n=0; n<=4; n++){
                //if there is a wild cell
                if(cells[idxCol+n][idxRow+n].isWildCell()){
                    temp += 5;
                }

                //if there is a sequence AND this is not the first encounter (more than 1 cell are already a sequence)
                if(cells[idxCol+n][idxRow+n].isSequence()){
                    if(isSequence)
                        break;
                    isSequence = true;
                    temp += 1000;
                }
            }
            HeuVal += temp;
        }
        return HeuVal;
    }

//Check if there is potential sequence from the enemy
 public int EnemyHeuristic(int x, int y) {
        //prepare the variable
        int EnemyVal = 0;
        int Enemytemp;
        boolean isSequence = false;
        Cell[][] cells = getGameModel().getSequenceBoard().getCells();
        int Team, Enemy;
        Team = this.getTeam();

        //Check Team or Enemy AI Number
        if (Team == Player.TEAM_1){
            Enemy = Player.TEAM_2;
        }
        else
        {
            Enemy = Player.TEAM_1;
        }
        
        //check column (LINE_EAST)
        for(int incCol=-4; incCol<=0; incCol++){
            isSequence = false;
            Enemytemp = 0;
            int idxCol = x+incCol;

            //if out of bound (leftmost column), then next iteration
            if(idxCol < 0)
                continue;
            //if out of bound (rightmost column), then break (do not need to check next column)
            if(idxCol + 4 >= SequenceBoard.BOARD_WIDTH)
                break;

            //check the next 5 cells to the east
            for(int n=0; n<=4; n++){

                //if there is a sequence AND this is not the first encounter (more than 1 cell are already a sequence)
                if(cells[idxCol + n][y].isSequence()){
                    if(isSequence)
                        break;
                    isSequence = true;
                }
                if(cells[idxCol + n][y].getMark() == Enemy){
                    Enemytemp += 1;
                }
            }
            EnemyVal += Enemytemp;
        }

        //check row (LINE_SOUTH)
        for(int incRow=-4; incRow<=0; incRow++){
            isSequence = false;
            Enemytemp = 0;
            int idxRow = y+incRow;

            //if out of bound (leftmost column), then next iteration
            if(idxRow < 0)
                continue;
            //if out of bound (rightmost column), then break (do not need to check next column)
            if(idxRow + 4 >= SequenceBoard.BOARD_HEIGHT)
                break;

            //check the next 5 cells to the east
            for(int n=0; n<=4; n++){

                //if there is a sequence AND this is not the first encounter (more than 1 cell are already a sequence)
                if(cells[x][idxRow+n].isSequence()){
                    if(isSequence)
                        break;
                    isSequence = true;

                }
                if(cells[x][idxRow+n].getMark() == Enemy){
                    Enemytemp += 1;
                }
            }
            EnemyVal += Enemytemp;
        }

        //check row (LINE_SOUTH_WEST)
        for(int inc=4; inc>=0; inc--){
            isSequence = false;
            Enemytemp = 0;
            int idxCol = x+inc;
            int idxRow = y-inc;

            //if out of bound (leftmost column), then next iteration
            if(idxRow < 0 || idxCol >= SequenceBoard.BOARD_WIDTH)
                continue;
            //if out of bound (rightmost column), then break (do not need to check next column)
            if(idxRow + 4 >= SequenceBoard.BOARD_HEIGHT || idxCol - 4 < 0)
                break;

            //check the next 5 cells to the east
            for(int n=0; n<=4; n++){

                //if there is a sequence AND this is not the first encounter (more than 1 cell are already a sequence)
                if(cells[idxCol - n][idxRow + n].isSequence()){
                    if(isSequence)
                        break;
                    isSequence = true;
                }
                if(cells[idxCol - n][idxRow + n].getMark() == Enemy){
                    Enemytemp += 1;
                }
            }
            EnemyVal += Enemytemp;
        }

        //check row (LINE_SOUTH_EAST)
        for(int inc=-4; inc<=0; inc++){
            isSequence = false;
            Enemytemp = 0;
            int idxCol = x+inc;
            int idxRow = y+inc;

            //if out of bound (leftmost column), then next iteration
            if(idxRow < 0 || idxCol < 0 )
                continue;
            //if out of bound (rightmost column), then break (do not need to check next column)
            if(idxRow + 4 >= SequenceBoard.BOARD_HEIGHT || idxCol + 4 < SequenceBoard.BOARD_WIDTH)
                break;

            //check the next 5 cells to the east
            for(int n=0; n<=4; n++){

                //if there is a sequence AND this is not the first encounter (more than 1 cell are already a sequence)
                if(cells[idxCol+n][idxRow+n].isSequence()){
                    if(isSequence)
                        break;
                    isSequence = true;
                }
                //sum the mark (value) of each cell)
                if(cells[idxCol+n][idxRow+n].getMark() == Enemy){
                    Enemytemp += 1;
                }
            }
            EnemyVal += Enemytemp;
        }
        return EnemyVal;
    }
}