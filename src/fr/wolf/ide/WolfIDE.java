package fr.wolf.ide;

import java.awt.Window;

import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

public class WolfIDE extends SingleFrameApplication
{
    public static void main(String[] args)
    {
        launch(WolfIDE.class, args);
    }

    @Override
    protected void startup()
    {
        show(new WolfFrame(this));
        System.setProperty("java.home", "C:\\Program Files\\Java\\jdk1.7.0_67");
    }

    @Override
    protected void configureWindow(Window window)
    {

    }

    public static WolfIDE getApplication()
    {
        return Application.getInstance(WolfIDE.class);
    }
}