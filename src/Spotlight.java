
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/**
 *
 * @author yaitloutou
 */
public class Spotlight {

    private static final String USER_PROFILE = System.getenv("USERPROFILE");
    private static final String SPOTLIGHT_IMAGES_LOCATION = USER_PROFILE + "/AppData/Local/Packages/Microsoft.Windows.ContentDeliveryManager_cw5n1h2txyewy/LocalState/Assets";
    private static final Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();
    private static final String DEST = USER_PROFILE + "/Pictures/Spotlight";

    private static final int MIN_FILE_SIZE = 200;//Kb

    public static void main(String[] args) {
        System.out.println("SCREEN_SIZE : " + SCREEN_SIZE.toString().substring(18));
        System.out.println("SPOTLIGHT_IMAGES_DEFAUL_LOCATION : " + SPOTLIGHT_IMAGES_LOCATION);
        System.out.println("SPOTLIGHT_IMAGES_NEW_LOCATION : " + DEST);
        try {
            Spotlight s = new Spotlight();
            s.make();
        } catch (IOException ex) {
            Logger.getLogger(Spotlight.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static boolean isPanoramic(Dimension d) {
        return d.getWidth() > d.getHeight();
    }

    private void make() throws IOException {
        // Creating Spotlight directory
        File dest = new File(DEST);
        if (Files.notExists(dest.toPath())) {
            dest.mkdir();
            System.out.println("SPOTLIGHT_IMAGES_NEW_LOCATION directory created");
        } else if (Files.exists(dest.toPath())) {
            System.out.println("SPOTLIGHT_IMAGES_NEW_LOCATION already exists");
            long count = Files.list(Paths.get(dest.getAbsolutePath())).count();
            System.out.printf("and it contains %d file%s %n",count,plural(count));
        }

        // Copy image files with the same dimmension as SCREEN_SIZE to Spotlight directory
        File[] files = new File(SPOTLIGHT_IMAGES_LOCATION).listFiles();
        int existentImages = 0, copiedImage = 0;
        for (File f : files) {

            if (f.isFile() && f.length() / 1024 > MIN_FILE_SIZE) {
                Dimension d = getImageDimension(f);
                if (d != null && ((isPanoramic(d)) == (isPanoramic(SCREEN_SIZE)))
                        && d.getHeight() >= SCREEN_SIZE.height) {
                    ++existentImages;
                    try {
                        if (copyImage(f)) {
                            ++copiedImage;
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(Spotlight.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        System.out.println(existentImages + " image" + plural(existentImages) + " found ");
        System.out.println(copiedImage + " image" + plural(copiedImage) + " copied to " + DEST);
    }

    boolean copyImage(File source) throws IOException {
        File dest = new File(USER_PROFILE + "/Pictures/Spotlight/" + source.getName() + ".JPEG");
        if (Files.notExists(dest.toPath())) {
            Files.copy(source.toPath(), dest.toPath());
            return true;
        } 
        return false;
    }

    Dimension getImageDimension(Object resourceFile) {
        try (ImageInputStream in = ImageIO.createImageInputStream(
                resourceFile)) {
            final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                try {
                    reader.setInput(in);
                    return new Dimension(reader.getWidth(0), reader.getHeight(0));
                } finally {
                    reader.dispose();
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Spotlight.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    String plural(long n) {
        return (n > 1) ? "s" : "";
    }

}
