package com.lawmillenium.intellij.plugins.narutoprogress;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.Nullable;

public class NarutoProgressChangenotesDialog extends DialogWrapper {

    private static final String CHANGENOTES_URL = "https://raw.githubusercontent.com/law-millenium/naruto-progress/master/changenotes.html";

    public NarutoProgressChangenotesDialog(@Nullable final Project project) {
        super(project);
        setTitle("Naruto Progress Changenotes");
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        String text;
        try (InputStream inputStream = new URL(CHANGENOTES_URL).openStream()) {
            text = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ioException) {
            text = "Changelog not found";
        }

        final JBLabel label = new JBLabel(text);
        label.setCopyable(true);
        return new JBScrollPane(label, JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JBScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }
}
