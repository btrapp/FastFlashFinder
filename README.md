# FashFlashFinder

In semiconductor manufacturing it is common to have a rectangular flash (sometimes called reticle) with many rectangular dies on them.

For some dense products there can easily be ~ 100 flashes and thousands of dies per flash printed on a single wafer.

Later we often have to align wafer level X/Y coordinates (for example from a defect scan) with the printed flash/die information in order to calculate which flash and which (if any) die a X/Y location falls on.
 
A brute-force approach is very inefficient as you may have to check every die for each x/y location.

A slightly faster flash-then-die approach is to first check for a flash bounding box match, and then only check the dies on that flash for matches.

This is an even better (sweep-line) approach, which takes advantage of:
 * Flashes are traditionally logically spaced in X and Y with no gaps, so calculating the matching flash is fast and easy.
 * Within a flash, dies may not be regularly spaced (for example, multi-part-wafers), but the die layout per flash is consistent
  
For dies on a reticle, a sweep-line approach can be used to build a fast TreeMap to know for each event (chip start/end) in X and then Y which die entries are relevant for this coordinate range.

https://ersantana.com/coding/algorithms/sweep_line_algorithms

Also tried a simple X-Range approach, where we store dies keyed by the die's X-Range.

When testing a worst-case wafer example with 700,000 dies (about 8,000 per Flash)
Running all 3 over multiple iterations, timings worked out to:
* flash-then-die took ~3 seconds
* ScanLine took ~0.2 seconds
* XRange took ~0.02 seconds!

This is sorta surprising but nice since XRange is a simpler implementation

