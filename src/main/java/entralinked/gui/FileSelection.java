package entralinked.gui;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public record FileSelection(File file, FileFilter filter, String extension) {}
