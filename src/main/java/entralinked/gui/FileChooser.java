package entralinked.gui;

import java.awt.Component;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

public class FileChooser {
    
    private static final JFileChooser fileChooser = new JFileChooser(".");
    
    public static void showFileOpenDialog(Component parent, Consumer<FileSelection> handler) {
        showFileOpenDialog(parent, Collections.emptyList(), handler);
    }
    
    public static void showFileOpenDialog(Component parent, FileFilter fileFilter, Consumer<FileSelection> handler) {
        showFileOpenDialog(parent, List.of(fileFilter), handler);
    }
    
    public static void showFileOpenDialog(Component parent, List<FileFilter> fileFilters, Consumer<FileSelection> handler) {
        showDialog(parent, fileFilters, fileChooser::showOpenDialog, handler);
    }
    
    private static void showDialog(Component parent, List<FileFilter> fileFilters, Function<Component, Integer> dialogFunction, Consumer<FileSelection> handler) {
        FileFilter currentFilter = fileChooser.getFileFilter();
        fileChooser.resetChoosableFileFilters();
        fileChooser.setAcceptAllFileFilterUsed(fileFilters.isEmpty());
        fileFilters.forEach(fileChooser::addChoosableFileFilter);
        
        if(fileFilters.contains(currentFilter)) {
            fileChooser.setFileFilter(currentFilter);
        }
        
        if(dialogFunction.apply(parent) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            handler.accept(new FileSelection(file, fileChooser.getFileFilter(), getFileExtension(file)));
        }
    }
    
    public static String getFileExtension(File file) {
        String name = file.getName();
        int index = name.lastIndexOf('.');
        
        if(index == -1 || index + 1 == name.length()) {
            return null;
        }
        
        return name.substring(index + 1).toLowerCase();
    }
}
