package fr.wolf.ide.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import javax.swing.JTextArea;

public class WolfConsole extends PrintStream
{
    private JTextArea outComponent;

    public WolfConsole(JTextArea out) throws FileNotFoundException
    {
        super(new File("C:\\ttt.txt"));
        new File("C:\\ttt.txt").delete();
        this.outComponent = out;
    }

    @Override
    public void write(byte[] buf, int off, int len)
    {
        addToConsole(new String(buf, off, len));
    }

    @Override
    public void write(int b)
    {
        addToConsole(new String(new char[] {(char)b}));
    }

    @Override
    public void println(String x)
    {
        addToConsole(x);
    }

    private void addToConsole(String s)
    {
        this.outComponent.setText(this.outComponent.getText() + "\n" + s);
    }
}