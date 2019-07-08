import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.IIOException;

import javax.imageio.ImageIO;

public class TestImageFilter {

  public static void main(String[] args) throws Exception {

    BufferedImage image = null;
    String srcFileName = null;
    try {
      srcFileName = args[0];
      File srcFile = new File(srcFileName);
      image = ImageIO.read(srcFile);
    } catch (ArrayIndexOutOfBoundsException e) {
      System.out.println("Usage: java TestAll <image-file>");
      System.exit(1);
    } catch (IIOException e) {
      System.out.println("Error reading image file " + srcFileName + " !");
      System.exit(1);
    }

    System.out.println("Source image: " + srcFileName);

    int w = image.getWidth();
    int h = image.getHeight();
    System.out.println("Image size is " + w + "x" + h);
    System.out.println();

    int[] src = image.getRGB(0, 0, w, h, null, 0, w);
    int[] dst = new int[src.length];

    System.out.println("Starting sequential image filter.");

    long startTime = System.currentTimeMillis();
    ImageFilter filter0 = new ImageFilter(src, dst, w, h);
    filter0.apply();
    long endTime = System.currentTimeMillis();

    long tSequential = endTime - startTime;
    System.out.println("Sequential image filter took " + tSequential + " milliseconds.");

    BufferedImage dstImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    dstImage.setRGB(0, 0, w, h, dst, 0, w);

    String dstName = "Filtered" + srcFileName;
    File dstFile = new File(dstName);
    ImageIO.write(dstImage, "jpg", dstFile);

    System.out.println("Output image: " + dstName);

    //************ ex3 *************
    System.out.println();
    System.out.println("Available processors: " + Runtime.getRuntime().availableProcessors());
    System.out.println();

    int[] parallelDst = new int[1];
    for (int i = 1; i <= Runtime.getRuntime().availableProcessors(); i *= 2) {
      // new arrays
      int[] parallelSrc = image.getRGB(0, 0, w, h, null, 0, w);
      parallelDst = new int[parallelSrc.length];
      System.out.println("Starting parallel image filter using " + i + " threads.");

      // apply parallel filter
      long startTimeParallel = System.currentTimeMillis();
      ParallelFJImageFilter filterParallel = new ParallelFJImageFilter(parallelSrc, parallelDst, w,
          h);
      filterParallel.apply(i);
      long endTimeParallel = System.currentTimeMillis();
      // time of execution
      long tParallel = endTimeParallel - startTimeParallel;
      System.out.println(
          "Parallel image filter took " + tParallel + " milliseconds using " + i + " threads.");

      // verify image
      boolean identical = true;
      for (int j = 0; j < dst.length; j++) {
        if (dst[j] != parallelDst[j]) {
          identical = false;
          break;
        }
      }
      if (identical && dst.length == parallelDst.length) {
        System.out.println("Output image verified successfully!");
      } else {
        System.out.println("Error: images are not identical!");
      }

      // speedup
      float speedup = (float) tSequential / (float) tParallel;
      if (i == 16) {
        System.out.println("Speedup: " + speedup);
      } else if (speedup >= 0.7) {
        System.out.println("Speedup: " + speedup + " ok (>= 0.7)");
      } else {
        System.out.println("Error: too low speedup!");
      }
      System.out.println();
    }

    // save the image
    BufferedImage parallelDstImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    parallelDstImage.setRGB(0, 0, w, h, parallelDst, 0, w);

    String dstNameParallel = "ParallelFiltered" + srcFileName;
    File dstFileParallel = new File(dstNameParallel);
    ImageIO.write(parallelDstImage, "jpg", dstFileParallel);

    System.out.println("Output image (parallel filter): " + dstNameParallel);

  }
}
