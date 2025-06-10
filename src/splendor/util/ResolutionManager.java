package splendor.util;

import java.awt.*;
import java.util.Objects;

public class ResolutionManager {
    private final int referenceWidth = 1920;
    private final int referenceHeight = 1080;
    private int currentWidth;
    private int currentHeight;
    private float widthRatio;
    private float heightRatio;

    public ResolutionManager(int currentWidth, int currentHeight) {
        if (currentWidth <= 0 || currentHeight <= 0) {
            throw new IllegalArgumentException("Width and height must be positive");
        }

        this.currentWidth = currentWidth;
        this.currentHeight = currentHeight;
        this.widthRatio = (float) currentWidth / referenceWidth;
        this.heightRatio = (float) currentHeight / referenceHeight;
    }

    public int scaleX(int x) {
        if (x < 0) {
            throw new IllegalArgumentException("Scale X value must not be negative");
        }
        return (int) (x * widthRatio);
    }

    public int scaleY(int y) {
        if (y < 0) {
            throw new IllegalArgumentException("Scale Y value must not be negative");
        }
        return (int) (y * heightRatio);
    }

    public int scaleFontSize(int fontSize) {
        if (fontSize < 0) {
            throw new IllegalArgumentException("Font size must not be negative");
        }
        return (int) (fontSize * Math.min(widthRatio, heightRatio));
    }

    public Font scaleFont(Font font) {
        Objects.requireNonNull(font, "Font cannot be null");
        return font.deriveFont((float) scaleFontSize(font.getSize()));
    }

    public int scaleSize(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Size to scale must not be negative");
        }
        return (int) (size * (widthRatio + heightRatio) / 2);
    }
}