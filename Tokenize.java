import java.util.List;
import java.util.ArrayList;

public class Tokenize {
    public static List<String> tokenize(String input) {
        // newlines to \n
        input = input.replace("\r\n", "\n").replace("\r", "\n");

        List<String> out = new ArrayList<>();
        StringBuilder buf = new StringBuilder();
        boolean justSawNewline = true; // ignore spaces immediately after a newline

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c == '\n') {
                // flush word if any
                if (buf.length() > 0) {
                    out.add(buf.toString());
                    buf.setLength(0);
                }
                out.add("\\n");           // newline token as literal "\n"
                justSawNewline = true;    // next leading spaces should be ignored
            } else if (c == ' ' || c == '\t') {
                if (justSawNewline) {
                    // skip leading spaces/tabs right after a newline
                    continue;
                }
                // flush word if any
                if (buf.length() > 0) {
                    out.add(buf.toString());
                    buf.setLength(0);
                }
                // each space/tab becomes an empty-string token
                out.add("");
            } else {
                buf.append(c);
                justSawNewline = false;
            }
        }
        if (buf.length() > 0) out.add(buf.toString());
        return out;
    }}