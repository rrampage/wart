package rrampage.wasp.gfx;

import rrampage.wasp.data.Module;
import rrampage.wasp.instructions.ConstInstruction;
import rrampage.wasp.parser.WasmParser;
import rrampage.wasp.vm.Machine;
import rrampage.wasp.vm.MachineVisitor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static rrampage.wasp.utils.ConversionUtils.constOf;

public class CanvasVisitorTest {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                createAndShowGUI();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void createAndShowGUI() throws Exception {
        Module module = WasmParser.fromFile("/Users/rr/Code/wart/examples/testsuite/fac.0.wasm").parseModule();
        System.out.println("Created GUI on EDT? "+
                SwingUtilities.isEventDispatchThread());
        JFrame f = new JFrame("Swing Paint Demo");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        var m = new MyPanel();
        f.add(m);
        f.pack();
        var g = m.getGraphics();
        f.setVisible(true);
        var visitor = MachineVisitor.VisitorBuilder.of().preFunction((fun) -> {
            System.out.println("ZZZZZZ: " + fun.name());
            g.setColor(Color.GREEN);
            System.out.println(g);
            g.clearRect(0, 0,250,200);
            g.drawString(fun.name(), 10, 50);
            //f.paintComponents(g);
            m.myUpdate();
            f.update(g);
        }).build();
        Machine machine = module.instantiate(null, visitor);
        machine.invoke("fac-iter", ConstInstruction.of(constOf(25L)));
    }
}

class MyPanel extends JPanel {

    private int squareX = 50;
    private int squareY = 50;
    private int squareW = 20;
    private int squareH = 20;

    public MyPanel() {

        setBorder(BorderFactory.createLineBorder(Color.black));

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                moveSquare(e.getX(),e.getY());
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
                moveSquare(e.getX(),e.getY());
            }
        });
    }

    private void moveSquare(int x, int y) {
        int OFFSET = 1;
        if ((squareX!=x) || (squareY!=y)) {
            repaint(squareX,squareY,squareW+OFFSET,squareH+OFFSET);
            squareX=x;
            squareY=y;
            repaint(squareX,squareY,squareW+OFFSET,squareH+OFFSET);
        }
    }


    public Dimension getPreferredSize() {
        return new Dimension(250,200);
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawString("This is my custom Panel!",10,20);
        g.setColor(Color.RED);
        g.fillRect(squareX,squareY,squareW,squareH);
        g.setColor(Color.BLACK);
        g.drawRect(squareX,squareY,squareW,squareH);
    }
     /*
        MachineVisitor -> function -> draw rectangle of some color

      */

    public Graphics getGraphics() {
        return super.getGraphics();
    }

    public void myUpdate() {
        System.out.println("Hiiii");
        super.update(getGraphics());
    }
}