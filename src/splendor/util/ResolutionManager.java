package splendor.util;

import java.awt.*;

public class ResolutionManager {
    private final int referenceWidth = 1920;
    private final int referenceHeight = 1080;
    private int currentWidth;
    private int currentHeight;
    private float widthRatio;
    private float heightRatio;

    public ResolutionManager(int currentWidth, int currentHeight) {
        this.currentWidth = currentWidth;
        this.currentHeight = currentHeight;
        this.widthRatio = (float)currentWidth / referenceWidth;
        this.heightRatio = (float)currentHeight / referenceHeight;
    }

    public int scaleX(int x) {
        return (int)(x * widthRatio);
    }

    public int scaleY(int y) {
        return (int)(y * heightRatio);
    }

    public int scaleFontSize(int fontSize) {
        return (int)(fontSize * Math.min(widthRatio, heightRatio));
    }

    public Font scaleFont(Font font) {
        return font.deriveFont((float)scaleFontSize(font.getSize()));
    }

    // Pour les dimensions qui doivent s'adapter dans les deux axes
    public int scaleSize(int size) {
        return (int)(size * (widthRatio + heightRatio) / 2);
    }
}
