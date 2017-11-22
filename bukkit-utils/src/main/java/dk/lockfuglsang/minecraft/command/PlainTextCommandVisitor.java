package dk.lockfuglsang.minecraft.command;

import dk.lockfuglsang.minecraft.util.FormatUtil;

import java.io.PrintStream;
import java.util.List;

import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

/**
 * Simple visitor for generating plain-text documentation of an Command-hierarchy.
 */
public class PlainTextCommandVisitor extends RowCommandVisitor implements DocumentWriter {
    private final int linewidth;

    public PlainTextCommandVisitor(int linewidth) {
        this.linewidth = linewidth;
    }

    public void writeTo(PrintStream out) {
        int[] colWidths = new int[3];
        for (Row row : getRows()) {
            if (row != null) {
                if (row.getCommand().length() > colWidths[0]) {
                    colWidths[0] = row.getCommand().length();
                }
                if (row.getPermission().length() > colWidths[1]) {
                    colWidths[1] = row.getPermission().length();
                }
                if (row.getDescription().length() > colWidths[2]) {
                    colWidths[2] = row.getDescription().length();
                }
            }
        }
        colWidths[0]++; // make room for the '/'
        if (colWidths[0] + colWidths[1] + colWidths[2] > linewidth && colWidths[0] + colWidths[1] < linewidth) {
            // truncate description column
            colWidths[2] = linewidth - colWidths[0] - colWidths[1];
        }
        String rowFormat = "";
        String separator = "";
        for (int i = 0; i < colWidths.length; i++) {
            if (i != 0) {
                rowFormat += " | ";
                separator += "-+-";
            }
            rowFormat += "%-" + colWidths[i] + "s";
            separator += String.format("%" + colWidths[i] + "s", "").replaceAll(" ", "-");
        }
        out.println(String.format(rowFormat, tr("Command"), tr("Permission"), tr("Description")));
        for (Row row : getRows()) {
            if (row == null) {
                out.println(separator);
            } else {
                String cmd = row.getCommand().isEmpty()  ? "" : "/" + row.getCommand();
                String description = row.getDescription();
                List<String> strings = FormatUtil.wordWrapStrict(description, colWidths[2]);
                out.println(String.format(rowFormat, cmd, row.getPermission(), strings.size() > 0 ? strings.get(0) : ""));
                for (int i = 1; i < strings.size(); i++) {
                    out.println(String.format(rowFormat, "", "", strings.get(i)));
                }
            }
        }
    }

}
