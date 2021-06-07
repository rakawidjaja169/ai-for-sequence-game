import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;

public class LblCell extends LblCard{
    private static final ImageIcon IMG_WILD_CARD = new ImageIcon("./Resource/X.png");
    private static final Ellipse2D.Float MARK = new Ellipse2D.Float(0,0,0,0);
    private static final Color CLR_CANDIDATE = Color.YELLOW;

    private int x, y;
    private Cell cell;
    private boolean isSequenceCandidate;

    //@param cardCode the code for the card that will be displayed in this label.
    public LblCell(FrmMain frmMain,  int height,
                   int x, int y, boolean isClickable,  Cell cell){
        super(frmMain, height, isClickable, cell.getCard(), 3,true);
        this.x = x;
        this.y = y;
        setCell(cell);
        repaint();
        revalidate();
    }

    /**
     * set the cell associated to this label to the given cell. This method should be memory safe since all cells
     * are reused.
     * @param cell the cell this label is associated with
     */
    public void setCell(Cell cell){
        this.cell = cell;
        setCard(cell.getCard());
    }

    /**
     * set whether this label is a part of sequence candidate or not.
     * @param isSequenceCandidate if true then this label is a part of sequence candidate, if false then it is not.
     */
    public void setSequenceCandidate(boolean isSequenceCandidate){
        this.isSequenceCandidate = isSequenceCandidate;
    }

    /**
     * determine whether this label is a part of sequence candidate.
     * @return true if this label is a part of sequence candidate, false otherwise
     */
    public boolean isSequenceCandidate(){ return isSequenceCandidate; }

    @Override
    public void reset(){
        super.reset();
        setSequenceCandidate(false);
    }

    @Override
    public void mouseEntered(MouseEvent e){
        //change the border and the cursor for button-like effect
        if(isClickable()) {
            setBorder(LblCard.BORDER_ENTERED);
            setCursor(LblCard.HAND_CURSOR);
            if(isSequenceCandidate()){
                frmMain.markCandidateSequence(SequenceBoard.getOneDimCellIdx(x, y), true);
            }
        }
    }

    @Override
    public void mouseExited(MouseEvent e){
        //remove the border and change back the cursor
        if(isClickable()) {
            if(isSelected()){
                setBorder(BORDER_SELECTED);
                if(isSequenceCandidate()){
                    frmMain.markCandidateSequence(SequenceBoard.getOneDimCellIdx(x, y), false);
                }
            }
            else{
                setBorder(BORDER_DEFAULT);
            }

            setCursor(ARROW_CURSOR);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e){
        if(isClickable()) {
            if(!isSequenceCandidate()) {
                frmMain.playSelectedCardAt(x, y);
            }
            else {
                frmMain.chooseSequences(SequenceBoard.getOneDimCellIdx(x, y));
            }
            setCursor(LblCard.ARROW_CURSOR);
        }
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);

        //if this cell has been filled already, draw the mark
        if(cell.getMark() > 0) {
            //determine the team color
            if (cell.getMark() == Player.TEAM_1)
                g.setColor(PnlPlayer.COLOR_1);
            else if (cell.getMark() == Player.TEAM_2)
                g.setColor(PnlPlayer.COLOR_2);

            //draw the mark
            Graphics2D g2d = (Graphics2D)g;
            MARK.setFrame((int) (getWidth()/2 - getWidth()/4),
                    (int) (getHeight()/2 - getWidth()/4),
                    (int) (getWidth()/2),
                    (int) (getWidth()/2));
            g2d.fill(MARK);

            //draw a sign if this cell is part of a sequence candidate
            if(isSequenceCandidate()){
                g2d.setPaint(CLR_CANDIDATE);
                AlphaComposite acomp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
                g2d.setComposite(acomp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }

            //draw the mark if the cell is a part of a sequence
            if(cell.isSequence())
            {
                if(cell.getMark() == Player.TEAM_1)
                    g2d.setPaint(PnlPlayer.COLOR_1);
                if(cell.getMark() == Player.TEAM_2)
                    g2d.setPaint(PnlPlayer.COLOR_2);
                AlphaComposite acomp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
                g2d.setComposite(acomp);
                g2d.fillRect(0, 0, getWidth(), getHeight());

            }
        }
    }

}
