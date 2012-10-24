package org.elfinder.servlets.commands;

import com.mortennobel.imagescaling.ResampleOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.elfinder.servlets.ConnectorException;
import org.elfinder.servlets.FileActionEnum;

/**
 * @author l.ciocci ASK srl
 * @date 23 ott. 2012
 * @version $Id$
 * @license BSD
 * @description: this class implmentes image resize,crop and rotate function 
 */
public class ResizeCommand extends AbstractCommandOverride {

    private static Logger logger = Logger.getLogger(ResizeCommand.class);

    @Override
    public void execute() throws ConnectorException {
        File dirCurrent = getExistingDir(getParam("current"), FileActionEnum.WRITE);
        if (dirCurrent != null) {
            String targethash = (String) getParamObject("target");
            File fileTarget = getExistingFile(targethash, dirCurrent, FileActionEnum.WRITE);
            String mode = (String) getParamObject("mode");
            if (mode.equals("resize")) {
                int with = Integer.parseInt((String) getParamObject("width"));
                int height = Integer.parseInt((String) getParamObject("height"));
                ResampleOp resampleOp = new ResampleOp(with, height);
                BufferedImage origImage;
                try {
                    origImage = ImageIO.read(fileTarget);
                    String ext = FilenameUtils.getExtension(fileTarget.getName());
                    BufferedImage rescaledImage = resampleOp.filter(origImage, null);
                    ImageIO.write(rescaledImage, ext, fileTarget);
                } catch (IOException rescaleEx) {
                    java.util.logging.Logger.getLogger(ResizeCommand.class.getName()).log(Level.SEVERE, null, rescaleEx);
                }
            } else if (mode.equals("crop")) {
                int width = Integer.parseInt((String) getParamObject("width"));
                int height = Integer.parseInt((String) getParamObject("height"));
                int x = Integer.parseInt((String) getParamObject("x"));
                int y = Integer.parseInt((String) getParamObject("y"));
                BufferedImage origImage;
                try {
                    origImage = ImageIO.read(fileTarget);
                    String ext = FilenameUtils.getExtension(fileTarget.getName());
                    BufferedImage cropImage = origImage.getSubimage(x, y, width, height);
                    ImageIO.write(cropImage, ext, fileTarget);
                } catch (IOException cropEx) {
                    java.util.logging.Logger.getLogger(ResizeCommand.class.getName()).log(Level.SEVERE, null, cropEx);
                }
            } else if (mode.equals("rotate")) {
                BufferedImage origImage;
                int degree = Integer.parseInt((String) getParamObject("degree"));
                System.out.println("Degree: " + degree);
                try {
                    origImage = ImageIO.read(fileTarget);
                    String ext = FilenameUtils.getExtension(fileTarget.getName());
                    /* TODO: check affinetransfor for correct feature about rotation 
                     now rotation working only 90 degrees by 90 degrees*/

                    /* Some trouble with AffineTransform
                     AffineTransform tx = new AffineTransform();                                                                
                     tx.rotate(Math.toRadians(degree), origImage.getWidth()/2, origImage.getHeight()/2);
                     AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);                                
                     BufferedImage rotateImage = op.filter(origImage, null);                                
                     */

                    ImageIO.write(rotateCw(origImage), ext, fileTarget);
                } catch (IOException cropEx) {
                    java.util.logging.Logger.getLogger(ResizeCommand.class.getName()).log(Level.SEVERE, null, cropEx);
                }
            }
            _content(dirCurrent, true);
        }
    }

    /**
     * Rotation function: this could be better!!! Rotation point by point not so
     * fast but work well.
     *
     * @param BufferedImage
     */
    private BufferedImage rotateCw(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        BufferedImage newImage = new BufferedImage(height, width, img.getType());

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                newImage.setRGB(height - 1 - j, i, img.getRGB(i, j));
            }
        }
        return newImage;
    }
}
