package rrampage.wasp.gfx;

import processing.core.PApplet;

/**
 * Adapted from <a href="https://processing.org/examples/gameoflife.html">...</a>
 */
public class GameOfLifePureProcessing extends PApplet {
    // Size of cells
    int cellSize = 5;

    // How likely for a cell to be alive at start (in percentage)
    float probabilityOfAliveAtStart = 50;

    // Variables for timer
    int interval = 100;
    int lastRecordedTime = 0;

    // Colors for active/inactive cells
    int alive = color(0, 200, 0);
    int dead = color(0);

    // Array of cells
    byte[][] cells;
    // Buffer to record the state of the cells and use this
    // while changing the others in the interations
    byte[][] cellsBuffer;

    // Pause
    boolean pause = false;

    public void settings() {
        size(640, 360, P2D);
        noSmooth();
    }

    public void setup() {

        // Instantiate arrays
        cells = new byte[width / cellSize][height / cellSize];
        cellsBuffer = new byte[width / cellSize][height / cellSize];

        // This stroke will draw the background grid
        stroke(48);

        // Initialization of cells
        for (int x = 0; x < width / cellSize; x++) {
            for (int y = 0; y < height / cellSize; y++) {
                float state = random(100);
                cells[x][y] = (byte) ((state > probabilityOfAliveAtStart) ? 0 : 1);
            }
        }
        // Fill in black in case cells don't cover all the windows
        background(0);
    }

    public void draw() {

        //Draw grid
        for (int x = 0; x < width / cellSize; x++) {
            for (int y = 0; y < height / cellSize; y++) {
                if (cells[x][y] == 1) {
                    fill(alive); // If alive
                } else {
                    fill(dead); // If dead
                }
                rect(x * cellSize, y * cellSize, cellSize, cellSize);
            }
        }
        // Iterate if timer ticks
        if (millis() - lastRecordedTime > interval) {
            if (!pause) {
                iteration();
                lastRecordedTime = millis();
            }
        }

        // Create  new cells manually on pause
        if (pause && mousePressed) {
            // Map and avoid out of bound errors
            int xCellOver = (int) (map(mouseX, 0, width, 0, (float) width / cellSize));
            xCellOver = constrain(xCellOver, 0, width / cellSize - 1);
            int yCellOver = (int) (map(mouseY, 0, height, 0, (float) height / cellSize));
            yCellOver = constrain(yCellOver, 0, height / cellSize - 1);

            // Check against cells in buffer
            if (cellsBuffer[xCellOver][yCellOver] == 1) { // Cell is alive
                cells[xCellOver][yCellOver] = 0; // Kill
                fill(dead); // Fill with kill color
            } else { // Cell is dead
                cells[xCellOver][yCellOver] = 1; // Make alive
                fill(alive); // Fill alive color
            }
        } else if (pause) { // And then save to buffer once mouse goes up
            // Save cells to buffer (so we opeate with one array keeping the other intact)
            for (int x = 0; x < width / cellSize; x++) {
                if (height / cellSize >= 0)
                    System.arraycopy(cells[x], 0, cellsBuffer[x], 0, height / cellSize);
            }
        }
    }

    void iteration() { // When the clock ticks
        // Save cells to buffer (so we opeate with one array keeping the other intact)
        for (int x = 0; x < width / cellSize; x++) {
            if (height / cellSize >= 0) System.arraycopy(cells[x], 0, cellsBuffer[x], 0, height / cellSize);
        }

        // Visit each cell:
        for (int x = 0; x < width / cellSize; x++) {
            for (int y = 0; y < height / cellSize; y++) {
                // And visit all the neighbours of each cell
                int neighbours = 0; // We'll count the neighbours
                for (int xx = x - 1; xx <= x + 1; xx++) {
                    for (int yy = y - 1; yy <= y + 1; yy++) {
                        if (((xx >= 0) && (xx < width / cellSize)) && ((yy >= 0) && (yy < height / cellSize))) { // Make sure you are not out of bounds
                            if (!((xx == x) && (yy == y))) { // Make sure to to check against self
                                if (cellsBuffer[xx][yy] == 1) {
                                    neighbours++; // Check alive neighbours and count them
                                }
                            } // End of if
                        } // End of if
                    } // End of yy loop
                } //End of xx loop
                // We've checked the neigbours: apply rules!
                if (cellsBuffer[x][y] == 1) { // The cell is alive: kill it if necessary
                    if (neighbours < 2 || neighbours > 3) {
                        cells[x][y] = 0; // Die unless it has 2 or 3 neighbours
                    }
                } else { // The cell is dead: make it live if necessary
                    if (neighbours == 3) {
                        cells[x][y] = 1; // Only if it has 3 neighbours
                    }
                } // End of if
            } // End of y loop
        } // End of x loop
    }

    public void keyPressed() {
        if (key == 'r' || key == 'R') {
            // Restart: reinitialization of cells
            for (int x = 0; x < width / cellSize; x++) {
                for (int y = 0; y < height / cellSize; y++) {
                    float state = random(100);
                    cells[x][y] = (byte) ((state > probabilityOfAliveAtStart) ? 0 : 1);
                }
            }
        }
        if (key == ' ') { // On/off of pause
            System.out.println("PAUSE");
            pause = !pause;
        }
        if (key == 'c' || key == 'C') { // Clear all
            System.out.println("CLEAR");
            for (int x = 0; x < width / cellSize; x++) {
                for (int y = 0; y < height / cellSize; y++) {
                    cells[x][y] = 0; // Save all to zero
                }
            }
        }
    }

    public static void main(String[] args) {
        PApplet.main(GameOfLifePureProcessing.class.getName());
    }
}
