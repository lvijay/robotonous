package com.lvijay.robotonous.test;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class MockClipboard extends Clipboard {
    private final MockRobot robot;

    public MockClipboard(MockRobot robot) {
        super("mock");

        this.robot = robot;
    }

    @Override
    public synchronized void setContents(Transferable contents, ClipboardOwner owner) {
        try {
            if (contents instanceof StringSelection s) {
                DataFlavor[] flavors = s.getTransferDataFlavors();
                Object data = s.getTransferData(DataFlavor.selectBestTextFlavor(flavors));

                if (data instanceof String ss) {
                    robot.addEvent(MockRobot.Event.PASTE, ss);
                    return;
                }
            }

            throw new IllegalArgumentException("Cannot support class " + contents.getClass());
        } catch (UnsupportedFlavorException | IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
