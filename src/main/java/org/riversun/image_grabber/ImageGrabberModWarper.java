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

import java.awt.Point;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.BufferedImage;
import java.awt.image.renderable.ParameterBlock;

import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.PerspectiveTransform;
import javax.media.jai.PlanarImage;
import javax.media.jai.WarpPerspective;

/**
 * Warper module for ImageGrabber using JAI <br>
 * <br>
 * <br>
 * This additional module requires JAI lib<br>
 * <br>
 * http://download.java.net/media/jai/builds/release/1_1_3/<br>
 * 
 * You can download native lib of JAI, you can get more higher speed of
 * calculating,<br>
 * but you can run image-grabber without native lib with slower speed.<br>
 * 
 * @author Tom Misawa (riversun.org@gmail.com)
 */
public class ImageGrabberModWarper {

    /**
     * Construct rectangle image from cropped area that was specified by
     * supplied points by WARP operation.<br>
     * 
     * @param bufImage
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
     * @param destRectWidth
     * @param destRectHeight
     * @return
     */
    WarpResult execWarpToRect(BufferedImage bufImage, Point p0, Point p1, Point p2, Point p3, int destRectWidth, int destRectHeight) {

        final WarpResult result = new WarpResult();

        final Point[] srcImageEdgePoints = new Point[] { p0, p1, p2, p3 };

        final Point[] destSizePoints = new Point[4];
        destSizePoints[0] = new Point(0, 0);
        destSizePoints[1] = new Point(destRectWidth, 0);
        destSizePoints[2] = new Point(destRectWidth, destRectHeight);
        destSizePoints[3] = new Point(0, destRectHeight);

        final PerspectiveTransform perspectiveTransform = getWarpTransform(srcImageEdgePoints, destSizePoints);

        final Point offset = new Point(0, 0);

        {
            final Point[] bufImagePoints = new Point[4];
            bufImagePoints[0] = new Point(0, 0);
            bufImagePoints[1] = new Point(bufImage.getWidth(), 0);
            bufImagePoints[2] = new Point(bufImage.getWidth(), bufImage.getHeight());
            bufImagePoints[3] = new Point(0, bufImage.getHeight());

            final Point bufImageTopLeft = getUpperLeftPointOf(bufImage, bufImagePoints, perspectiveTransform);

            offset.x = -bufImageTopLeft.x;
            offset.y = -bufImageTopLeft.y;
        }

        final BufferedImage warpedBufferedImage = warpImage(bufImage, perspectiveTransform);

        result.offset = offset;
        result.bufImage = warpedBufferedImage;

        return result;

    }

    /**
     * 
     * Warp Result
     *
     */
    static class WarpResult {
        /**
         * warped image
         */
        public BufferedImage bufImage;

        /**
         * warped image offset for fitting parent image (always parent image
         * means background image)
         */
        public Point offset;
    }

    /**
     * Execute the operation of WARP from rectangle image
     * 
     * @param bufImage
     * 
     * @param p0
     *            The point of the corner no.0(upper,left)
     * @param p1
     *            The point of the corner no.1(upper,right)
     * @param p2
     *            The point of the corner no.2(bottom,right)
     * @param p3
     *            The point of the corner no.3(bottomleft)
     * @return
     */
    WarpResult execWarpFromRect(BufferedImage bufImage, Point p0, Point p1, Point p2, Point p3) {

        final WarpResult result = new WarpResult();

        final Point[] srcImageEdgePoints = getCornerPointsOfImage(bufImage);

        final Point[] moveToEdgePointsOnBackgroundImage = new Point[4];

        moveToEdgePointsOnBackgroundImage[0] = p0;
        moveToEdgePointsOnBackgroundImage[1] = p1;
        moveToEdgePointsOnBackgroundImage[2] = p2;
        moveToEdgePointsOnBackgroundImage[3] = p3;

        final PerspectiveTransform perspectiveTransform = getWarpTransform(srcImageEdgePoints, moveToEdgePointsOnBackgroundImage);

        final Point topLeft = getUpperLeftPointOf(bufImage, srcImageEdgePoints, perspectiveTransform);
        final BufferedImage warpedBufferedImage = warpImage(bufImage, perspectiveTransform);

        result.offset = topLeft;
        result.bufImage = warpedBufferedImage;

        return result;

    }

    /**
     * Comuputes the supplied warp transform and construct the image
     * 
     * @param bufImage
     * @param perspectiveTransform
     * @return
     */
    private BufferedImage warpImage(BufferedImage bufImage, PerspectiveTransform perspectiveTransform)

    {

        PerspectiveTransform inverseTransform = null;

        try {
            inverseTransform = perspectiveTransform.createInverse();
        } catch (NoninvertibleTransformException e1) {
            e1.printStackTrace();
            return null;
        } catch (CloneNotSupportedException e1) {
            e1.printStackTrace();
            return null;
        }

        final WarpPerspective warpPerspective = new WarpPerspective(inverseTransform);

        if (warpPerspective == null) {
            return null;
        }

        final ParameterBlock parameterBlock = new ParameterBlock();

        final PlanarImage planarImage = PlanarImage.wrapRenderedImage(bufImage);

        parameterBlock.addSource(planarImage);
        parameterBlock.add(warpPerspective);

        if (false) {
            // BINLINER:higher speed, low quality
            parameterBlock.add(Interpolation.getInstance(Interpolation.INTERP_BILINEAR));
        }
        if (true) {
            // BICUBIC:lower speed, high quality
            parameterBlock.add(Interpolation.getInstance(Interpolation.INTERP_BICUBIC)); //
        }

        // DO OPERATIONS[begin]
        final String OP_NAME = "warp";
        final PlanarImage warpedPlanarImage = JAI.create(OP_NAME, parameterBlock);
        // DO OPERATIONS[end]

        final BufferedImage wapedBufferedImage = warpedPlanarImage.getAsBufferedImage();

        return wapedBufferedImage;
    }

    /**
     * Returns corner points of the image
     * 
     * @param bufImage
     * @return
     */
    private Point[] getCornerPointsOfImage(BufferedImage bufImage) {

        final int srcWidth = bufImage.getWidth();
        final int srcHeight = bufImage.getHeight();
        final Point[] srcImageEdgePoints = new Point[4];

        srcImageEdgePoints[0] = new Point(0, 0);
        srcImageEdgePoints[1] = new Point(srcWidth, 0);
        srcImageEdgePoints[2] = new Point(srcWidth, srcHeight);
        srcImageEdgePoints[3] = new Point(0, srcHeight);

        return srcImageEdgePoints;

    }

    /**
     * Get upper-left point of warped object
     * 
     * @param boundedIm
     * @param points
     * @param persTF
     * @return
     */
    private Point getUpperLeftPointOf(BufferedImage boundedIm, Point[] points, PerspectiveTransform persTF)
    {
        final int numOfPoints = 4;

        final Point[] transformedPoints = new Point[numOfPoints];

        for (int i = 0; i < numOfPoints; i++) {
            transformedPoints[i] = new Point();
            persTF.transform(points[i], transformedPoints[i]);
        }

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;

        for (int i = 0; i < points.length; i++) {
            if (transformedPoints[i].x < minX) {
                minX = transformedPoints[i].x;
            }
            if (points[i].y < minY) {
                minY = transformedPoints[i].y;
            }
        }

        return new Point(minX, minY);
    }

    /**
     * Create PerspectiveTransform from coords
     * 
     * @param srcImageEdgePoints
     * @param moveToEdgePoints
     * @return
     */
    private PerspectiveTransform getWarpTransform(Point[] srcImageEdgePoints, Point[] moveToEdgePoints) {
        final PerspectiveTransform perspectiveTransform = PerspectiveTransform.getQuadToQuad(

            // Source rectangle vertex coordinates placed clockwise from the
            // upper left corner of the rectangle ordered
            srcImageEdgePoints[0].x,
            srcImageEdgePoints[0].y,
            srcImageEdgePoints[1].x,
            srcImageEdgePoints[1].y,
            srcImageEdgePoints[2].x,
            srcImageEdgePoints[2].y,
            srcImageEdgePoints[3].x,
            srcImageEdgePoints[3].y,

            // Dest vertex coordinates placed clockwise from the upper left
            // corner of the rectangle ordered
            moveToEdgePoints[0].x,
            moveToEdgePoints[0].y,
            moveToEdgePoints[1].x,
            moveToEdgePoints[1].y,
            moveToEdgePoints[2].x,
            moveToEdgePoints[2].y,
            moveToEdgePoints[3].x,
            moveToEdgePoints[3].y
            );

        return perspectiveTransform;
    }
}
