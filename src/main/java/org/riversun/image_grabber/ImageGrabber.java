/*  'image-grabber' Java library for image manipulation with ease of use 
 *
 *  Copyright (c) 2006- Tom Misawa, riversun.org@gmail.com
 *  
 *  Permission is hereby granted, free of charge, to any person obtaining a
 *  copy of this software and associated documentation files (the "Software"),
 *  to deal in the Software without restriction, including without limitation
 *  the rights to use, copy, modify, merge, publish, distribute, sublicense,
 *  and/or sell copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following conditions:
 *  
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *  
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 *  DEALINGS IN THE SOFTWARE.
 *  
 */
package org.riversun.image_grabber;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.riversun.image_grabber.ImageGrabberModWarper.WarpResult;

/**
 * The class for building and manipulating and transforming an image<br>
 * 
 * @author Tom Misawa (riversun.org@gmail.com)
 */
public class ImageGrabber {

    BufferedImage mImage;

    public ImageGrabber(File imageFile) {
        mImage = loadImage(imageFile);
    }

    public ImageGrabber(URL imageUrl) {
        mImage = loadImage(imageUrl);
    }

    public ImageGrabber(ImageGrabber imageGrabber) {
        mImage = imageGrabber.mImage;
    }

    public ImageGrabber(BufferedImage image) {
        mImage = image;
    }

    /**
     * Create ImageGrabber from resource on the class path
     * 
     * @param resourceName
     */
    public ImageGrabber(String resourceName) {
        this(ClassLoader.getSystemResource(resourceName));
    }

    public BufferedImage getImage() {
        return mImage;
    }

    /**
     * Create ImageGrabber initialized by size specified
     * 
     * @param width
     * @param height
     */
    public ImageGrabber(int width, int height) {
        mImage = createImage(width, height);
    }

    /**
     * Get the width of the image
     * 
     * @return
     */
    public int getWidth() {
        return mImage.getWidth();
    }

    /**
     * Get the heightof the image
     * 
     * @return
     */
    public int getHeight() {
        return mImage.getHeight();
    }

    /**
     * TO clear(fill by transparent) current image
     */
    public ImageGrabber clear() {

        final Graphics2D g2d = (Graphics2D) mImage.getGraphics();

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.dispose();

        return ImageGrabber.this;
    }

    /**
     * To clear(fill by transparent) specified rect
     * 
     * @param cutSize
     * @return
     * @throws Exception
     */
    public ImageGrabber clearRect(Rectangle rect) throws Exception {

        Graphics2D g2d = (Graphics2D) mImage.getGraphics();

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
        g2d.fillRect(rect.x, rect.y, rect.width, rect.height);
        g2d.dispose();

        return ImageGrabber.this;
    }

    /**
     * Put image on current image
     * 
     * @param image
     * @return
     */
    public ImageGrabber put(ImageGrabber image) {
        Graphics2D g2d = (Graphics2D) mImage.getGraphics();
        g2d.drawImage(image.mImage, 0, 0, null);
        g2d.dispose();
        return ImageGrabber.this;
    }

    /**
     * Put image on current image with specified position
     * 
     * @param image
     * @param left
     * @param top
     * @return
     */
    public ImageGrabber put(ImageGrabber image, int left, int top) {
        Graphics2D g2d = (Graphics2D) mImage.getGraphics();
        g2d.drawImage(image.mImage, left, top, null);
        g2d.dispose();
        return ImageGrabber.this;
    }

    /**
     * Grid for determining where to place the image
     * 
     */
    public static class GridPosition {

        int gridSizeX;
        int gridSizeY;
        int imagePosX;
        int imagePosY;

        int marginLeft;
        int marginTop;
        int marginRight;
        int marginBottom;

        /**
         * Size of grid
         * 
         * @param gridSizeX
         * @param gridSizeY
         */
        public GridPosition(int gridSizeX, int gridSizeY) {
            if (gridSizeX <= 0 || gridSizeY <= 0) {
                throw new RuntimeException("tableSizeXCount and tableSizeYCount must be >1 ");
            }
            this.gridSizeX = gridSizeX;
            this.gridSizeY = gridSizeY;

        }

        /**
         * To determine whether this image is placed anywhere in the grid by
         * coordinations.<br>
         * 
         * @param positionIndexX
         * @param positionIndexY
         * @return
         */
        public GridPosition setPosition(int positionIndexX, int positionIndexY) {
            this.imagePosX = positionIndexX;
            this.imagePosY = positionIndexY;
            return GridPosition.this;
        }

        /**
         * Set margin pixels around the image
         * 
         * @param margin
         * @return
         */
        public GridPosition setMargin(int margin) {
            this.marginLeft = margin;
            this.marginTop = margin;
            this.marginRight = margin;
            this.marginBottom = margin;
            return GridPosition.this;
        }

        /**
         * Set left-margin
         * 
         * @param marginLeft
         * @return
         */
        public GridPosition setMarginLeft(int marginLeft) {
            this.marginLeft = marginLeft;
            return GridPosition.this;
        }

        /**
         * Set top-margin
         * 
         * @param marginTop
         * @return
         */
        public GridPosition setMarginTop(int marginTop) {
            this.marginTop = marginTop;
            return GridPosition.this;
        }

        /**
         * Set right-margin
         * 
         * @param marginRight
         * @return
         */
        public GridPosition setMarginRight(int marginRight) {
            this.marginRight = marginRight;
            return GridPosition.this;
        }

        /**
         * set bottom-margin
         * 
         * @param marginBottom
         * @return
         */
        public GridPosition setMarginBottom(int marginBottom) {
            this.marginBottom = marginBottom;
            return GridPosition.this;
        }

    }

    /**
     * Put image on current image with grid position
     * 
     * @param image
     * @param gridPosition
     * @return
     */
    public ImageGrabber put(ImageGrabber image, GridPosition gridPosition) {

        int marginLeft = gridPosition.marginLeft;
        int marginRight = gridPosition.marginRight;
        int marginTop = gridPosition.marginTop;
        int marginBottom = gridPosition.marginBottom;

        int objXCount = gridPosition.gridSizeX;
        int objYCount = gridPosition.gridSizeY;
        int yIdx = gridPosition.imagePosY;
        int xIdx = gridPosition.imagePosX;

        int marginWidthBeweenObjects = 0;

        if (objXCount == 1) {
            marginWidthBeweenObjects = ((this.getWidth() - marginLeft - marginRight) - image.getWidth() * objXCount) / 2;
            marginLeft += marginWidthBeweenObjects;
        }
        else if (objXCount > 1) {
            marginWidthBeweenObjects = ((this.getWidth() - marginLeft - marginRight) - image.getWidth() * objXCount) / (objXCount - 1);
        }

        int marginHeightBeweenObjects = 0;
        if (objYCount == 1) {
            marginHeightBeweenObjects = ((this.getHeight() - marginTop - marginBottom) - image.getHeight() * objYCount) / 2;
            marginTop += marginHeightBeweenObjects;

        }
        else if (objYCount > 1) {
            marginHeightBeweenObjects = ((this.getHeight() - marginTop - marginBottom) - image.getHeight() * objYCount) / (objYCount - 1);
        }

        put(image, EAnchor.LEFT_TOP,
            marginLeft + marginWidthBeweenObjects * xIdx + image.getWidth() * xIdx,
            marginTop + marginHeightBeweenObjects * yIdx + image.getHeight() * yIdx);

        return ImageGrabber.this;

    }

    /**
     * 
     * Anchor point of image to put
     *
     */
    public enum EAnchor {
        LEFT_TOP,
        CENTER_TOP,
        RIGHT_TOP,
        LEFT_CENTER,
        CENTER,
        RIGHT_CENTER,
        LEFT_BOTTOM,
        CENTER_BOTTOM,
        RIGHT_BOTTOM,
    }

    /**
     * Put image on current image with specified position
     * 
     * @param image
     * @param imageAnchor
     *            anchor of dest image grabber
     * @param x
     * @param y
     * @return
     */
    public ImageGrabber put(ImageGrabber image, EAnchor imageAnchor, int x, int y) {

        int left = 0;
        int top = 0;

        switch (imageAnchor) {
        case LEFT_TOP:
            left = x;
            top = y;
            break;
        case CENTER_TOP:
            left = x - image.getWidth() / 2;
            top = y;
            break;
        case RIGHT_TOP:
            left = x - image.getWidth();
            top = y;
            break;
        case LEFT_CENTER:
            left = x;
            top = y - image.getHeight() / 2;
            break;
        case CENTER:
            left = x - image.getWidth() / 2;
            top = y - image.getHeight() / 2;
            break;
        case RIGHT_CENTER:
            left = x - image.getWidth();
            top = y - image.getHeight() / 2;
            break;
        case LEFT_BOTTOM:
            left = x;
            top = y - image.getHeight();
            break;
        case CENTER_BOTTOM:
            left = x - image.getWidth() / 2;
            top = y - image.getHeight();
            break;
        case RIGHT_BOTTOM:
            left = x - image.getWidth();
            top = y - image.getHeight();
            break;
        default:
            left = x;
            top = y;
            break;
        }

        put(image, left, top);

        return ImageGrabber.this;
    }

    /**
     * Put image on current image with specified position
     * 
     * @param bufImage
     * @param left
     * @param top
     * @return
     */
    public ImageGrabber put(BufferedImage bufImage, int left, int top) {
        Graphics2D g2d = (Graphics2D) mImage.getGraphics();
        g2d.drawImage(bufImage, left, top, null);
        g2d.dispose();
        return ImageGrabber.this;
    }

    /**
     * Put image in the center of the current image
     * 
     * @param image
     * @return
     */
    public ImageGrabber putInCenter(ImageGrabber image) {
        Graphics2D g2d = (Graphics2D) mImage.getGraphics();
        g2d.drawImage(image.mImage, getWidth() / 2 - image.getWidth() / 2, getHeight() / 2 - image.getHeight() / 2, null);
        g2d.dispose();
        return ImageGrabber.this;
    }

    /**
     * returns color with alpha
     * 
     * @param color
     * @param alpha
     * @return
     */
    public Color getColorWithAlpha(int color, int alpha) {
        return new Color(
            (color >> 16) & 0xff,// R
            (color >> 8) & 0xff,// G
            (color) & 0xff,// B
            alpha// alpha
        );

    }

    /**
     * Crop a shape consisting of four points
     * 
     * @param p0
     *            The point of the corner no.0(upper,left)
     * @param p1
     *            The point of the corner no.1(upper,right)
     * @param p2
     *            The point of the corner no.2(bottom,right)
     * @param p3
     *            The point of the corner no.3(bottomleft)
     * 
     * @param refCropInfo
     *            you can set the reference of Crop operation additional info if
     *            you need.
     * @return
     */
    public ImageGrabber crop(Point p0, Point p1, Point p2, Point p3, CropInfo refCropInfo) {
        return crop(p0, p1, p2, p3, ECropPolicy.FIT_SIZE, refCropInfo);
    }

    /**
     * Crop a shape consisting of four points
     * 
     * @param p0
     *            The point of the corner no.0(upper,left)
     * @param p1
     *            The point of the corner no.1(upper,right)
     * @param p2
     *            The point of the corner no.2(bottom,right)
     * @param p3
     *            The point of the corner no.3(bottomleft)
     * @param policy
     *            crop operation policy
     * @param refCropInfo
     *            you can set the reference of Crop operation additional info if
     *            you need.
     * @return
     */
    public ImageGrabber crop(Point p0, Point p1, Point p2, Point p3, ECropPolicy policy, CropInfo refCropInfo) {

        final BufferedImage tempBufImage = createImage(getWidth(), getHeight());

        Graphics2D g2d = (Graphics2D) tempBufImage.getGraphics();
        GeneralPath path = new GeneralPath();
        path.moveTo(p0.x, p0.y);
        path.lineTo(p1.x, p1.y);
        path.lineTo(p2.x, p2.y);
        path.lineTo(p3.x, p3.y);
        path.lineTo(p0.x, p0.y);
        g2d.clip(path);
        g2d.drawImage(mImage, 0, 0, null);

        mImage = tempBufImage;

        g2d.dispose();

        if (ECropPolicy.FIT_SIZE == policy) {

            final Point upperLeftCoord = getUpperLeftCoord(new Point[] { p0, p1, p2, p3 });
            final Point bottomRightCoord = getBottomRightCoord(new Point[] { p0, p1, p2, p3 });

            if (refCropInfo != null) {

                final int offsetX = upperLeftCoord.x;
                final int offsetY = upperLeftCoord.y;

                refCropInfo.p0 = new Point(p0.x - offsetX, p0.y - offsetY);
                refCropInfo.p1 = new Point(p1.x - offsetX, p1.y - offsetY);
                refCropInfo.p2 = new Point(p2.x - offsetX, p2.y - offsetY);
                refCropInfo.p3 = new Point(p3.x - offsetX, p3.y - offsetY);
            }

            cropRect(upperLeftCoord, bottomRightCoord);
        }
        return ImageGrabber.this;
    }

    /**
     * Crop rectangle area
     * 
     * @param rect
     * @return
     */
    public ImageGrabber cropRect(Rectangle rect) {

        mImage = mImage.getSubimage(rect.x, rect.y, rect.width, rect.height);
        return ImageGrabber.this;
    }

    /**
     * Crop rectangle area specified by top-left and bottom-right
     * 
     * @param topLeft
     * @param bottomRight
     */
    public ImageGrabber cropRect(Point topLeft, Point bottomRight) {

        mImage = mImage.getSubimage(topLeft.x, topLeft.y, (bottomRight.x - topLeft.x), (bottomRight.y - topLeft.y));
        return ImageGrabber.this;
    }

    /**
     * Crop oval area
     * 
     * @param centerX
     * @param centerY
     * @param radius
     * @param policy
     * @return
     */
    public ImageGrabber cropOval(double centerX, double centerY, double radius, ECropPolicy policy) {

        final BufferedImage tempBufImage = createImage(getWidth(), getHeight());

        final Graphics2D g2d = (Graphics2D) tempBufImage.getGraphics();

        final Ellipse2D ellipse = new Ellipse2D.Double();

        double cropAreaWidth = radius * 2;
        double cropAreaHeight = radius * 2;
        double cropAreaLeft = centerX - cropAreaWidth / 2;
        double cropAreaTop = centerY - cropAreaHeight / 2;

        ellipse.setFrame(cropAreaLeft, cropAreaTop, cropAreaWidth, cropAreaHeight);

        g2d.setClip(ellipse);

        g2d.drawImage(mImage, 0, 0, null);

        mImage = tempBufImage;

        g2d.dispose();

        if (ECropPolicy.FIT_SIZE == policy) {

            cropRect(new Rectangle((int) cropAreaLeft, (int) cropAreaTop, (int) cropAreaWidth, (int) cropAreaHeight));
        }
        return ImageGrabber.this;
    }

    /**
     * returns upper-left point from points<br>
     * 
     * @param points
     * @return
     */
    private Point getUpperLeftCoord(Point[] points) {

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        for (int i = 0; i < points.length; i++) {

            final int x = points[i].x;
            final int y = points[i].y;

            if (minX > x) {
                minX = x;
            }
            if (minY > y) {
                minY = y;
            }
        }
        return new Point(minX, minY);
    }

    /**
     * returns bottom,right coordination from points<br>
     * 
     * @param points
     * @return
     */
    private Point getBottomRightCoord(Point[] points) {
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (int i = 0; i < points.length; i++) {
            int x = points[i].x;
            int y = points[i].y;
            if (maxX < x) {
                maxX = x;
            }
            if (maxY < y) {
                maxY = y;
            }
        }

        return new Point(maxX, maxY);
    }

    /**
     * Execute warp to rectangle from crop area<br>
     * Make the area that was cut out by four corner points to a rectangle
     * 
     * @param p0
     *            The point of the corner no.0<br>
     *            (upper-left of the crop area )
     * @param p1
     *            The point of the corner no.1<br>
     *            (upper-right of the crop area)
     * @param p2
     *            The point of the corner no.2<br>
     *            (bottom-right of the crop area)
     * @param p3
     *            The point of the corner no.3<br>
     *            (bottom-left of the crop area)
     * 
     * @param destRectWidth
     * @param destRectHeight
     * @return
     */
    public ImageGrabber cropWarp(Point p0, Point p1, Point p2, Point p3, int destRectWidth, int destRectHeight) {

        final CropInfo cropInfo = new CropInfo();
        crop(p0, p1, p2, p3, ECropPolicy.FIT_SIZE, cropInfo);

        // KEY POINT:the 'CLONE()' is needed to avoid the bug of JAI.
        BufferedImage srcBufImage = clone().getImage();

        final ImageGrabberModWarper modWarper = new ImageGrabberModWarper();
        final WarpResult warpResult = modWarper.execWarpToRect(srcBufImage, cropInfo.p0, cropInfo.p1, cropInfo.p2, cropInfo.p3, destRectWidth, destRectHeight);
        mImage = warpResult.bufImage;
        Point offset = warpResult.offset;

        cropRect(new Point(offset.x, offset.y), new Point(offset.x + destRectWidth, offset.y + destRectHeight));
        return ImageGrabber.this;
    }

    /**
     * Execute warp<br>
     * 
     * @param p0
     *            The point of the corner no.0<br>
     *            (upper-left of the source rectangle)
     * @param p1
     *            The point of the corner no.1<br>
     *            (upper-right of the source rectangle)
     * @param p2
     *            The point of the corner no.2<br>
     *            (bottom-right of the source rectangle)
     * @param p3
     *            The point of the corner no.3<br>
     *            (bottom-left of the source rectangle)
     */
    public ImageGrabber warp(Point p0, Point p1, Point p2, Point p3) {
        return warp(p0, p1, p2, p3, EWarpPolicy.RESIZE_AND_MOVE_TO_CORRECT_POSITION, null);
    }

    /**
     * Execute warp<br>
     * 
     * @param p0
     *            The point of the corner no.0<br>
     *            (upper-left of the source rectangle)
     * @param p1
     *            The point of the corner no.1<br>
     *            (upper-right of the source rectangle)
     * @param p2
     *            The point of the corner no.2<br>
     *            (bottom-right of the source rectangle)
     * @param p3
     *            The point of the corner no.3<br>
     *            (bottom-left of the source rectangle)
     * 
     * @param warpPolicy
     *            warp operation policy
     * 
     * @param refWarpInfo
     *            you can set the reference of Warp operation additional info if
     *            you need.
     * @return
     */
    public ImageGrabber warp(Point p0, Point p1, Point p2, Point p3, EWarpPolicy warpPolicy, WarpInfo refWarpInfo) {

        final ImageGrabberModWarper warper = new ImageGrabberModWarper();

        WarpResult warpResult = warper.execWarpFromRect(mImage, p0, p1, p2, p3);
        BufferedImage warpedImage = warpResult.bufImage;

        if (refWarpInfo != null) {
            refWarpInfo.offsetX = warpResult.offset.x;
            refWarpInfo.offsetY = warpResult.offset.y;
            refWarpInfo.warpedSizeWidth = warpedImage.getWidth();
            refWarpInfo.warpedSizeHeight = warpedImage.getHeight();

        }

        switch (warpPolicy) {
        case RESIZE_AND_MOVE_TO_CORRECT_POSITION: {

            // The image size is automatically changed corresponding to the
            // warped
            // image size
            final int newSizeWidth = warpedImage.getWidth() + warpResult.offset.x;
            final int newSizeHeight = warpedImage.getHeight() + warpResult.offset.y;

            mImage = createImage(newSizeWidth, newSizeHeight);

            put(warpedImage, warpResult.offset.x, warpResult.offset.y);
        }
            break;
        case RESIZE_AND_FIT: {

            // The image size is automatically changed corresponding to the
            // warped
            // image size
            final int newSizeWidth = warpedImage.getWidth();
            final int newSizeHeight = warpedImage.getHeight();

            mImage = createImage(newSizeWidth, newSizeHeight);

            put(warpedImage, 0, 0);
        }
            break;
        case DONT_RESIZE: {
            put(warpedImage, 0, 0);
        }
            break;

        }

        return ImageGrabber.this;
    }

    /**
     * Fill with pattern<br>
     * (like tiling paint)
     * 
     * @param image
     *            pattern image
     * @return
     */
    public ImageGrabber fill(ImageGrabber image) {
        Graphics2D g2d = (Graphics2D) mImage.getGraphics();

        Rectangle2D.Double rec =
                new Rectangle2D.Double(0, 0, image.getWidth(), image.getHeight());
        g2d.setPaint(new TexturePaint(image.getImage(), rec));
        g2d.fill(new Rectangle2D.Double(0, 0, getWidth(), getHeight()));
        g2d.dispose();

        return ImageGrabber.this;
    }

    /**
     * Fill with color
     * 
     * @param color
     * @return
     */
    public ImageGrabber fill(int color) {
        Graphics2D g2d = (Graphics2D) mImage.getGraphics();

        g2d.setColor(getColorWithAlpha(color, 255));
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.dispose();
        return ImageGrabber.this;
    }

    /**
     * Fill color on non-alpha-area
     * 
     * @param color
     * @return
     */
    public ImageGrabber fillOnNoAlphaArea(int color) {

        Graphics2D g2d = (Graphics2D) mImage.getGraphics();

        g2d.setColor(getColorWithAlpha(color, 255));

        int height = mImage.getHeight();
        int width = mImage.getWidth();

        for (int y = 0; y < height; y++) {

            for (int x = 0; x < width; x++) {
                int argb = mImage.getRGB(x, y);

                int alpha = (argb >> 24) & 0xff;
                // int rr = (argb >> 16) & 0xff; // red
                // int gg = (argb >> 8) & 0xff; // green
                // int bb = (argb) & 0xff; // blue

                if (alpha == 0) {
                    // - Part Of full transparency
                } else if (alpha < 255) {
                    // - Not entirely transparency, but the portion where the
                    // transparent is activated.
                    g2d.setColor(getColorWithAlpha(color, alpha));
                    g2d.drawLine(x, y, x, y);
                } else {
                    // - Part there is a color
                    g2d.drawLine(x, y, x, y);
                }
            }
        }
        g2d.dispose();
        return ImageGrabber.this;
    }

    /**
     * Fill force color of non-full-transparency-area
     * 
     * @param color
     * @return
     */
    public ImageGrabber fillForceOnNoAlphaArea(int color) {

        Graphics2D g2d = (Graphics2D) mImage.getGraphics();

        g2d.setColor(getColorWithAlpha(color, 255));

        int height = mImage.getHeight();
        int width = mImage.getWidth();

        for (int y = 0; y < height; y++) {

            for (int x = 0; x < width; x++) {
                int argb = mImage.getRGB(x, y);

                int alpha = (argb >> 24) & 0xff;
                // int rr = (argb >> 16) & 0xff; // red
                // int gg = (argb >> 8) & 0xff; // green
                // int bb = (argb) & 0xff; // blue

                if (alpha == 0) {
                    // - Part Of full transparency
                } else if (alpha < 255) {
                    // - Not entirely transparency, but the portion where the
                    // transparent is activated.
                    g2d.setColor(getColorWithAlpha(color, 255));
                    g2d.drawLine(x, y, x, y);
                } else {
                    // - Part there is a color
                    g2d.drawLine(x, y, x, y);
                }
            }
        }
        g2d.dispose();
        return ImageGrabber.this;
    }

    /**
     * Rotate image without changing the canvas size
     * 
     * @param angleDegree
     * @return
     */
    public ImageGrabber rotate(double angleDegree) {
        final BufferedImage newBufImage = createImage(getWidth(), getHeight());

        Graphics2D g2d = (Graphics2D) newBufImage.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);

        // g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
        // RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        AffineTransform transform = new AffineTransform();

        transform.rotate(Math.PI * angleDegree / 180d, getWidth() / 2, getHeight() / 2);

        g2d.drawImage(mImage, transform, null);
        g2d.dispose();

        mImage = newBufImage;

        return ImageGrabber.this;
    }

    /**
     * Change the resolution
     * 
     * @param newSizeWidth
     * @param newSizeHeight
     * @return
     */
    public ImageGrabber reresolution(final int newSizeWidth, final int newSizeHeight) {
        final BufferedImage newBufImage = createImage(newSizeWidth, newSizeHeight);

        Graphics2D g2d = (Graphics2D) newBufImage.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);

        // g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
        // RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        AffineTransform transform = new AffineTransform();

        double xScale = (double) newSizeWidth / (double) getWidth();
        double yScale = (double) newSizeHeight / (double) getHeight();

        transform.scale(xScale, yScale);

        g2d.drawImage(mImage, transform, null);
        g2d.dispose();

        mImage = newBufImage;
        return ImageGrabber.this;
    }

    /**
     * Scale image
     * 
     * @param xScale
     * @param yScale
     */
    public ImageGrabber scale(double xScale, double yScale) {

        final int newSizeWidth = (int) (xScale * getWidth());
        final int newSizeHeight = (int) (yScale * getHeight());

        return reresolution(newSizeWidth, newSizeHeight);

    }

    /**
     * Resize the current image grabber size<br>
     * Extend and apply the size of the current canvas <br>
     * If you want to change the resolution ,see {@see #scale(float, float)}
     * 
     * @param width
     * @param height
     * @return
     */
    public ImageGrabber resize(int width, int height) {

        ImageGrabber ig = new ImageGrabber(width, height);
        ig.putInCenter(this);

        mImage = ig.getImage();

        return ImageGrabber.this;
    }

    /**
     * returns Deep copy of ImageGrabber
     */
    public ImageGrabber clone() {
        return new ImageGrabber(copyBufImage(mImage));
    }

    private int mJpegQualityPercentage = 80;

    /**
     * Set quality percentage of jpeg.<br>
     * This value is used when saving in jpeg.
     * 
     * @param qualityPercentage
     * @return
     */
    public ImageGrabber setJpegQuality(int qualityPercentage) {
        mJpegQualityPercentage = qualityPercentage;
        return ImageGrabber.this;
    }

    /**
     * Save current image to file<br>
     * file name must be ends with .png or .jpg<br>
     * <br>
     * Only png or jpg is supported.<br>
     * 
     * @param file
     * @return
     */
    public boolean saveImageTo(File file) {
        try {
            save(mImage, file, mJpegQualityPercentage);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;

    }

    /**
     * Erase alpha-value form the color of pixels
     * 
     * @return
     */
    public ImageGrabber convertToRGBColor() {
        BufferedImage argbBufImage = mImage;
        final BufferedImage rgbBufImage = new BufferedImage(argbBufImage.getWidth(), argbBufImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        final Graphics2D g2d = rgbBufImage.createGraphics();
        g2d.drawImage(argbBufImage, 0, 0, null);
        g2d.dispose();
        mImage = rgbBufImage;
        return ImageGrabber.this;
    }

    /**
     * 
     * policy for crop operation
     * 
     */
    public enum ECropPolicy {
        DONT_CHANGE_SIZE, //
        FIT_SIZE, //
    }

    /**
     * crop operation additinal info<br>
     * 
     */
    public static class CropInfo {
        /**
         * crop result point 0(upper,left)
         */
        public Point p0;

        /**
         * crop result point 1(upper,right)
         */
        public Point p1;

        /**
         * 
         * crop result point 2(bottom,right)
         */
        public Point p2;

        /**
         * crop result point 3(bottom,left)
         */
        public Point p3;

        @Override
        public String toString() {
            return "CropInfo [p0=" + p0 + ", p1=" + p1 + ", p2=" + p2 + ", p3=" + p3 + "]";
        }

    }

    /**
     * policy for warp operation
     */
    public enum EWarpPolicy {
        RESIZE_AND_MOVE_TO_CORRECT_POSITION,
        RESIZE_AND_FIT,
        DONT_RESIZE,
    }

    /**
     * 
     * Warp operation additional info <br>
     * 
     */
    public static class WarpInfo {
        /**
         * offset x of warped image
         */
        public int offsetX;

        /**
         * offset y of warped image
         */
        public int offsetY;

        /**
         * width of fitted warped image
         */
        public int warpedSizeWidth;

        /**
         * height of fitted warped image
         */
        public int warpedSizeHeight;

    }

    /**
     * Show current image on the window
     * 
     * @param title
     *            windowTitle
     * @return
     */
    public ImageGrabber showPreviewWindow(String title) {
        showPreview(title, this, 1.0f);
        return ImageGrabber.this;
    }

    /**
     * Show current image on the window
     * 
     * @return
     */
    public ImageGrabber showPreviewWindow() {
        showPreview(null, this, 1.0f);
        return ImageGrabber.this;
    }

    /**
     * Show current image on the window with zoom ratio
     * 
     * @param ratio
     * @return
     */
    public ImageGrabber showPreviewWindow(float ratio) {
        showPreview(null, this, ratio);
        return ImageGrabber.this;
    }

    /**
     * Show current image on the window with title and zoom ratio
     * 
     * @param title
     * @param ratio
     * @return
     */
    public ImageGrabber showPreviewWindow(String title, float ratio) {
        showPreview(title, this, ratio);
        return ImageGrabber.this;
    }

    // STATIC METHODS[begin]///////////////////////////////////////

    /**
     * Show image on the swing window
     * 
     * @param windowTitle
     * @param imageGrabber
     */
    private static void showPreview(String windowTitle, ImageGrabber imageGrabber, float zoomRatio) {

        final JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        if (windowTitle != null) {
            frame.setTitle(windowTitle);
        }

        final Container content = frame.getContentPane();
        content.setLayout(new FlowLayout());
        content.add(new JLabel(new ImageIcon(imageGrabber.clone().scale(zoomRatio, zoomRatio).getImage())));
        frame.pack();

        // centering
        frame.setLocationRelativeTo(null);

        frame.setVisible(true);

        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

        int screenWidth = screen.width;

        // Place the window at the same distance[begin]///////////////
        sFrameList.add(frame);

        if (sFrameList.size() > 1) {

            final int numOfFrames = sFrameList.size();

            // calculate width of all frames
            int framesTotalWidth = 0;
            for (int i = 0; i < numOfFrames; i++) {
                framesTotalWidth += sFrameList.get(i).getWidth();
            }

            // place frames
            final int spacerWidthAlongFrames = (screenWidth - framesTotalWidth) / (numOfFrames + 1);
            int frameLeft = spacerWidthAlongFrames;

            for (int i = 0; i < numOfFrames; i++) {

                final JFrame targetFrame = sFrameList.get(i);
                final Point framePosition = targetFrame.getLocation();

                framePosition.x = frameLeft;

                // set position
                targetFrame.setLocation(framePosition);

                // proceed cursor for next turn
                frameLeft += targetFrame.getWidth() + spacerWidthAlongFrames;

            }// end for

        }// end if (sFrameList.size() > 1) {

        // Place the window at the same distance[begin]///////////////

    }

    private static List<JFrame> sFrameList = new ArrayList<JFrame>();

    /**
     * Create BufferedImage with specified size
     * 
     * @param width
     * @param height
     * @return
     */
    private static BufferedImage createImage(int width, int height) {
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    /**
     * Load image from URL
     * 
     * @param url
     * @return
     */
    private static BufferedImage loadImage(URL url) {
        BufferedImage readImage = null;

        try {
            readImage = ImageIO.read(url);
        } catch (Exception e) {
            e.printStackTrace();
            readImage = null;
        }
        return readImage;
    }

    /**
     * Load image from file
     * 
     * @param file
     * @return
     */
    private static BufferedImage loadImage(File file) {
        BufferedImage readImage = null;

        try {
            readImage = ImageIO.read(file);
        } catch (Exception e) {
            e.printStackTrace();
            readImage = null;
        }
        return readImage;
    }

    /**
     * Returns deep copy of buffered image
     * 
     * @param srcBufImage
     * @return
     */
    private static BufferedImage copyBufImage(BufferedImage srcBufImage) {

        BufferedImage destBufImage =
                new BufferedImage(srcBufImage.getWidth(), srcBufImage.getHeight(), srcBufImage.getType());

        Graphics g = destBufImage.getGraphics();
        g.drawImage(srcBufImage, 0, 0, null);
        g.dispose();

        return destBufImage;
    }

    /**
     * Save image to file<br>
     * supported format is PNG and JPEG<br>
     * 
     * @param bufImage
     * @param file
     * @param jpegQualityPercentage
     *            (use when save as JPEG)
     * @throws IOException
     */
    private static void save(BufferedImage bufImage, File file, int jpegQualityPercentage) throws IOException {

        if (file.getAbsolutePath().toUpperCase().endsWith(".PNG")) {
            ImageIO.write(bufImage, "PNG", file);
        }
        else if (file.getAbsolutePath().toUpperCase().endsWith(".JPG") || file.getAbsolutePath().toUpperCase().endsWith(".JPEG")) {
            saveJpeg(bufImage, file, (float) jpegQualityPercentage / 100f);
        }
        else {
            throw new RuntimeException("Error occured while saving the image.Not supported extension. only supported .png / .jpg ");
        }

    }

    /**
     * Save JPEG image with quality option
     * 
     * @param argbBufImage
     * @param f
     * @param quality
     * @throws IOException
     */
    private static void saveJpeg(BufferedImage argbBufImage, File f, float quality) throws IOException {

        // convert ARGB image into RGB image
        final BufferedImage rgbBufImage = new BufferedImage(argbBufImage.getWidth(), argbBufImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        final Graphics2D g2d = rgbBufImage.createGraphics();
        g2d.drawImage(argbBufImage, 0, 0, null);
        g2d.dispose();

        // save jpeg with quality
        final ImageWriter jpegWriter = ImageIO.getImageWritersByFormatName("jpg").next();
        final ImageWriteParam jpegWriteParam = jpegWriter.getDefaultWriteParam();
        jpegWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);

        if (quality > 1.0f) {
            quality = 1.0f;
        } else if (quality < 0) {
            quality = 0;
        }

        jpegWriteParam.setCompressionQuality(quality);

        final OutputStream os = new FileOutputStream(f);
        jpegWriter.setOutput(ImageIO.createImageOutputStream(os));

        final IIOImage jpegImage = new IIOImage(rgbBufImage, null, null);
        jpegWriter.write(null, jpegImage, jpegWriteParam);
        jpegWriter.dispose();
    }
    // STATIC METHODS[end]///////////////////////////////////////
}
