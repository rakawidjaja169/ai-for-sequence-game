import java.awt.Dimension;

public class ImageScaler {
    /**
     * calculate the scale needed to resize the original size {@code oriSize} to the intended size {@code trgtSize}.
     * This method is used to calculate scale for one dimension only.
     * @param oriSize the original size (in one dimension)
     * @param trgtSize the intended size (in one dimension)
     * @return the scale factor to resize from {@code oriSize} to {@code trgtSize}.
     */
    public static double getScale(int oriSize, int trgtSize) {
        double scale = 1;
        scale = (double) trgtSize / (double) oriSize;
        return scale;
    }

    /**
     * calculate the scale needed to resize the original dimension {@code ori} to the intended dimension {@code trgt}.
     * This method is used to calculate scale in two dimensions while keeping aspect ratio.
     * @param ori the original dimension
     * @param trgt the intended dimension
     * @return the scale factor to resize both dimensions from {@code ori} to {@code trgt} while keeping aspect ratio.
     */
    public static double getScale(Dimension ori, Dimension trgt) {
        double scale = 1;
        if (ori != null && trgt != null) {
            double dScaleWidth = getScale(ori.width, trgt.width);
            double dScaleHeight = getScale(ori.height, trgt.height);
            scale = Math.min(dScaleHeight, dScaleWidth);
        }
        return scale;
    }
}
