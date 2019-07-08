import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class ParallelFJImageFilter {

  private int[] src;
  private int[] dst;
  private int width;
  private int height;

  private final int NRSTEPS = 100;

  private final int THRESHOLD = 5;

  public ParallelFJImageFilter(int[] src, int[] dst, int w, int h) {
    this.src = src;
    this.dst = dst;
    width = w;
    height = h;
  }

  public void apply(int nthreads) {
    // create thread pool
    ForkJoinPool pool = new ForkJoinPool(nthreads);
    // 100 filter applies
    for (int steps = 0; steps < NRSTEPS; steps++) {
      // start recursive task creation
      pool.invoke(new JoinForkWorker(1, height - 1));
      // swap arrays
      int[] help;
      help = src;
      src = dst;
      dst = help;
    }
    pool.shutdown();
  }

  class JoinForkWorker extends RecursiveAction {

    private int start;
    private int end;

    public JoinForkWorker(int start, int end) {
      this.start = start;
      this.end = end;
    }

    @Override
    protected void compute() {
      // when to stop recursion
      if (end - start <= THRESHOLD) {
        computeDirectly();
        return;
      }
      // splitting task into two
      invokeAll(new JoinForkWorker(start, start + (end - start) / 2),
          new JoinForkWorker(start + (end - start) / 2, end));
    }

    private void computeDirectly() {
      int index, pixel;

      // start/end of rows to filter changed
      for (int i = start; i < end; i++) {
        for (int j = 1; j < width - 1; j++) {
          float rt = 0, gt = 0, bt = 0;
          for (int k = i - 1; k <= i + 1; k++) {
            index = k * width + j - 1;
            pixel = src[index];
            rt += (float) ((pixel & 0x00ff0000) >> 16);
            gt += (float) ((pixel & 0x0000ff00) >> 8);
            bt += (float) ((pixel & 0x000000ff));

            index = k * width + j;
            pixel = src[index];
            rt += (float) ((pixel & 0x00ff0000) >> 16);
            gt += (float) ((pixel & 0x0000ff00) >> 8);
            bt += (float) ((pixel & 0x000000ff));

            index = k * width + j + 1;
            pixel = src[index];
            rt += (float) ((pixel & 0x00ff0000) >> 16);
            gt += (float) ((pixel & 0x0000ff00) >> 8);
            bt += (float) ((pixel & 0x000000ff));
          }
          // Re-assemble destination pixel.
          index = i * width + j;
          int dpixel =
              (0xff000000) | (((int) rt / 9) << 16) | (((int) gt / 9) << 8) | (((int) bt / 9));
          dst[index] = dpixel;
        }
      }
    }
  }
}
