import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class LblCard extends JLabel implements MouseListener{

    private static final Color CLR_LBL_DEFAULT = new Color(238,238,238);
    private static final Color CLR_LBL_ENTERED =  Color.YELLOW;
    private static final Color CLR_LBL_CLICKED = Color.RED;
    private static final Color CLR_LBL_SELECTED = Color.GREEN;

    public static final LineBorder BORDER_DEFAULT =  new LineBorder(CLR_LBL_DEFAULT, 8);
    public static final LineBorder BORDER_ENTERED = new LineBorder(CLR_LBL_ENTERED, 8);
    public static final LineBorder BORDER_CLICKED = new LineBorder(CLR_LBL_CLICKED, 8);
    public static final LineBorder BORDER_SELECTED = new LineBorder(CLR_LBL_SELECTED, 8);
    public static final Cursor HAND_CURSOR = new Cursor(Cursor.HAND_CURSOR);
    public static final Cursor ARROW_CURSOR = new Cursor(Cursor.DEFAULT_CURSOR);

    protected FrmMain frmMain;
    private Card card;
    private boolean isClickable;
    private boolean isSelected;
    /**a boolean value indicating whether this card is facing up or down. */
    private boolean isOpen;
    private BufferedImage imgOpen;
    private BufferedImage imgClose;
    private int cardScale;



    private static final BufferedImage IMG_CLOSE;
    static {
        BufferedImage imgTemp = null;
        try {
            imgTemp = ImageIO.read(new File("./Resource/red_back.png"));
        }catch(IOException e){
            imgTemp = null;
            System.err.println("Image for the back of the card is not found");
            e.printStackTrace();
            System.exit(1);
        }
        IMG_CLOSE = imgTemp;
    }

    /**
     * create a new customized {@code JLabel} to display a card in a Sequence game. This label will be used to display
     * the deck of cards, the players' hand, and also as a parent class for the board's cells.
     * @param frmMain the {@code FrmMain} object that acts as the main GUI of the game.
     * @param lblHeight the height of this label, which then will be used to calculate this label's width while keeping
     *                  the aspect ratio of the card's image used
     * @param isClickable to determine whether users will be able to interact (by clicking) this label or not
     * @param card a reference to the card that will be displayed in this label.
     * @param isOpen determine whether this card is currently facing up or down.
     */
    public LblCard(FrmMain frmMain, int lblHeight, boolean isClickable,
                   Card card, boolean isOpen) {
        this(frmMain, lblHeight, isClickable, card, 2, isOpen);
    }

    /**
     * create a new customized {@code JLabel} to display a card in a Sequence game. This label will be used to display
     * the deck of cards, the players' hand, and also as a parent class for the board's cells.
     * @param frmMain the {@code FrmMain} object that acts as the main GUI of the game.
     * @param lblHeight the height of this label, which then will be used to calculate this label's width while keeping
     *                  the aspect ratio of the card's image used
     * @param isClickable to determine whether users will be able to interact (by clicking) this label or not
     * @param card a reference to the card that will be displayed in this label.
     * @param cardScale an integer number to determine the scale in drawing the card.
     * @param isOpen determine whether this card is currently facing up or down.
     */
    public LblCard(FrmMain frmMain, int lblHeight, boolean isClickable,
                   Card card, int cardScale, boolean isOpen) {
        //initialize object properties
        this.frmMain = frmMain;
        this.card = card;
        this.isClickable = isClickable;
        this.isOpen = isOpen;
        this.isSelected = false;
        this.cardScale = cardScale;
        setBorder(BORDER_DEFAULT);

        //calculate the width of this label based on the card's image loaded and the specified height,
        // then set the label's size
        double scale =  (double)lblHeight/IMG_CLOSE.getHeight();
        int lblWidth = (int)(IMG_CLOSE.getWidth()*scale);
        setSize(lblWidth, lblHeight);

        //set the card to be displayed
        setCard(card);

        //Control the interaction with users' mouse
        addMouseListener(this);
    }

    /**
     * set the card to be displayed on this label. This method will automatically resize the image
     * to fit the label's size.
     * @param card the card to be display on this label.
     */
    public void setCard(Card card){
        //set the card contained in this label
        this.card = card;

        //load the image of the card
        BufferedImage imgTemp = null;
        try {
            imgTemp = ImageIO.read(new File("./Resource/" + card.getName() + ".png"));
        }catch (IOException ex){
            System.err.println("Image not found for card \"" + card.getName() + "\"");
            ex.printStackTrace();
            System.exit(2);
        }

        //resize image by scaling imgTemp and then drawing it on imgOpen with the calculated new dimensions
        double scale = ImageScaler.getScale(new Dimension(imgTemp.getWidth(), imgTemp.getHeight()), getSize());
        int newW = (int)(imgTemp.getWidth() * scale);
        int newH = (int)(imgTemp.getHeight() * scale);
        newW = newW*cardScale; newH = newH*cardScale;

        //create a new image to store the resized image of the card
        imgOpen = new BufferedImage(newW, newH, imgTemp.getType());
        Graphics2D g2 = (Graphics2D) imgOpen.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(imgTemp, 0, 0, newW, newH, null);

        //resize the image for when the card is closed
        imgClose = new BufferedImage(newW, newH, imgTemp.getType());
        g2 = (Graphics2D) imgClose.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(IMG_CLOSE, 0, 0, newW, newH, null);

        //redraw the panel
        repaint();
        revalidate();
    }

    /**
     * set all reference variables to null to prevent memory leak
     */
    public void reset(){
        setClickable(false);
        setSelected(false);
        imgOpen = null;
        imgClose = null;
        card = null;
    }

    /**
     * select this label, which will change the border to show that this label has been selected.
     */
    public void select(){
        if(isSelected()){
            setSelected(false);
        }
        else{
            setSelected(true);
            frmMain.selectCard(card);
        }
    }

    /**
     * Determine whether this label is currently selected or not.
     * @return true if this label has been selected, false otherwise.
     */
    public boolean isSelected(){ return isSelected; }
    /**
     * Determine whether this label can be clicked or not
     * @return {@code true} if this label intended to be clicked by users, {@code false} otherwise
     */
    public boolean isClickable(){ return isClickable; }
    /**
     * Determine whether this card is currently facing up or facing down.
     * @return true if this card is currently facing up in the game, false otherwise
     */
    public boolean isOpen() { return isOpen; }
    /**
     * enable or disable this label to be clicked  depending on the value of parameter {@code isClickable}.
     * @param isClickable if true, enable this label to be clicked, otherwise disable this property
     */
    public void setClickable(boolean isClickable){ this.isClickable = isClickable; }

    /**
     * Set whether this label needs to be highlighted because a player just chose a card.
     * @param isSelected if true, this label will be highlighted, otherwise it there will be no highlight
     */
    public void setSelected(boolean isSelected){
        this.isSelected = isSelected;
        if(isSelected)
            setBorder(BORDER_SELECTED);
        else
            setBorder(BORDER_DEFAULT);
    }

    /** determine which side of the card is drawn in this label.*/
    public void setOpen(boolean isOpen){ this.isOpen = isOpen;}


    /**
     * get the image of this card when it is opened
     * @return a {@code BufferedImage} representing the front image of this card
     */
    public BufferedImage getImgOpen() { return imgOpen; }
    /**
     * get the image of this card when it is closed
     * @return a {@code BufferedImage} representing the back image of this card
     */
    public static BufferedImage getImgClose() { return IMG_CLOSE; }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        if(isClickable()) {
            //unselect the previous
            if (mouseEvent.getSource().getClass() == LblCard.class)
                ((PnlPlayer) getParent()).unselectLabel();
            //at this point, isSelected = false

            //select this label
            select();
        }
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        //change the border for button-like effect
        if(isClickable()) {
            setBorder(BORDER_CLICKED);
        }
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        if (isClickable()){
            //if the mouse is released inside this label
            if(contains(mouseEvent.getX(), mouseEvent.getY())) {
                setBorder(BORDER_ENTERED);
            }
            //if the mouse is clicked here, but released elsewhere
            else {
                setBorder(BORDER_DEFAULT);
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {
        //change the border and the cursor for button-like effect
        if(isClickable()) {
            setBorder(BORDER_ENTERED);
            setCursor(HAND_CURSOR);
        }
    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {
        //remove the border and change back the cursor
        if(isClickable()) {
            if(isSelected){
                setBorder(BORDER_SELECTED);
            }
            else{
                setBorder(BORDER_DEFAULT);
            }

            setCursor(ARROW_CURSOR);
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        //if there is a card to be drawn
        if (card != null){
            Graphics2D g2 = (Graphics2D) g;

            //determine which image to be drawn
            BufferedImage imgDrawn;
            if (isOpen)
                imgDrawn = imgOpen;
            else
                imgDrawn = imgClose;

            //draw the image
            int x = BORDER_DEFAULT.getBorderInsets(this).left;
            if (getWidth() - imgDrawn.getWidth() > 0)
                g2.drawImage(imgDrawn, null, (getWidth() - imgDrawn.getWidth()) / 2, 0);
            else
                g2.drawImage(imgDrawn, null, x, 0);
        }
    }
}
