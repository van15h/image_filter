# image_filter

## Parallelization strategy

Thread pool with nthreads (1, 2, 4, 8, 16). 1 - is almost the same as sequential code. Other numbers of threads produce a very good speedup in computations.

The main idea is to split the hole work into tasks. The splitting itself is done recursively: each task produces two more tasks with the work divided between them equally (divided by 2).

threshold – number of pixel rows, that will be in every task. Was chosen as 5 (almost same results for 10), because the tasks should be as small as possible, but at the same time not to make much overhead in splitting tasks and then managing them.

The number of generated tasks is proportional threshold.
