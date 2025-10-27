import java.util.Scanner;
import java.util.List;

public class Lojban {
    static final java.util.Map<String, java.util.List<java.util.List<String>>> FACTS = new java.util.HashMap<>();
    static final java.util.Map<String,String> GLOBAL = new java.util.HashMap<>();

    static String encNum(long n)         { return "N:" + n; }
    static String encName(String s)      { return "S:" + s; }
    static String encVar(String s)       { return "VAR:" + s; }
    static String encEmpty()             { return "L:[]"; }

    static boolean isNumEnc(String e)    { return e != null && e.startsWith("N:"); }
    static boolean isNameEnc(String e)   { return e != null && e.startsWith("S:"); }
    static boolean isVarEnc(String e)    { return e != null && e.startsWith("VAR:"); }
    static boolean isListEnc(String e)   { return e != null && (e.equals("L:[]") || e.startsWith("L:[")); }

    static long    decNum(String e)      { return Long.parseLong(e.substring(2)); }
    static String  varName(String e)     { return e.substring(4); }

    static class Env {
        final java.util.Map<String,String> m = new java.util.HashMap<>();
        boolean bind(String var, String enc){
            String oldLocal = m.get(var);
            if (oldLocal != null) return oldLocal.equals(enc);
            String oldGlobal = GLOBAL.get(var);
            if (oldGlobal != null) return oldGlobal.equals(enc);
            m.put(var, enc);
            return true;
        }
        String get(String var){ return m.get(var); }
    }

    static String resolve(String enc, Env env){
        if (enc == null) return null;
        if (isVarEnc(enc)) {
            String v = env.get(varName(enc));
            if (v != null) return v;
            return GLOBAL.get(varName(enc)); 
        }
        return enc;
    }
    static boolean bindOrCheck(String targetEnc, String valueEnc, Env env){
        if (isVarEnc(targetEnc)) return env.bind(varName(targetEnc), valueEnc);
        return targetEnc.equals(valueEnc);
    }

    // token helpers
    static boolean isNameToken(String t){ return t.startsWith(".") && t.endsWith("."); }
    static String  stripDots(String t)  { return t.substring(1, t.length()-1); }
    static String normId(String s){ return s.toLowerCase(); }
    static boolean isLojVowel(char c){ return "aeiouy".indexOf(c) >= 0; }
    static boolean isLojCons(char c){ return "bcdfgjklmnprstvxz".indexOf(c) >= 0; }
    static boolean isShortWord(String s){
        if (s.length()!=2) return false;
        char c0 = Character.toLowerCase(s.charAt(0));
        char c1 = Character.toLowerCase(s.charAt(1));
        return isLojCons(c0) && isLojVowel(c1);
    }
    static boolean isGismu(String s){
        if (s.length()!=5) return false;
        String p="";
        for (int i=0;i<5;i++){
            char ch = Character.toLowerCase(s.charAt(i));
            if (isLojCons(ch)) p+="C";
            else if (isLojVowel(ch)) p+="V";
            else return false;
        }
        return p.equals("CVCCV") || p.equals("CCVCV");
    }
        static void validateTokensOrThrow(List<String> t){
        for (int i=0;i<t.size(); i++){
            String w = t.get(i);
            if (isBlank(w)) continue;
            if (w.equals("i") || w.equals("lo") || w.equals("se")) continue;
            if (w.equals("steni") || w.equals("steko") || w.equals("cmavo") ||
                w.equals("fatci") || w.equals("sumji") || w.equals("vujni") || w.equals("dunli")) {
                continue;
            }
            if (w.matches("0|(-?[1-9]\\d*)")) continue;               
            if (isNameToken(w)) continue;                               
            if (!isGismu(w) && !isShortWord(w)) {
            }
            if (isNameToken(w)) {
                String prev = (i>0) ? t.get(i-1) : "";
                if (!"lo".equals(prev)) {
                    throw new RuntimeException("Name must be preceded by 'lo': " + w);
                }
            }
        }
    }
    static boolean isNumeric(String s) {
        return s.matches("0|(-?[1-9]\\d*)");
    }
    static boolean isBlank(String t) { return t.equals("") || t.equals("\\n"); }

    static final class Ts {
        final List<String> t; int i = 0;
        Ts(List<String> t){ this.t = t; }

        boolean has(){
            int k = i; while (k < t.size() && isBlank(t.get(k))) k++;
            return k < t.size();
        }
        String peek(){
            int k = i; while (k < t.size() && isBlank(t.get(k))) k++;
            return k < t.size() ? t.get(k) : null;
        }
        String next(){
            int k = i; while (k < t.size() && isBlank(t.get(k))) k++;
            if (k >= t.size()) throw new RuntimeException("Unexpected end");
            i = k + 1;
            return t.get(k);
        }
        int pos(){ return i; }
        void setPos(int p){ i = p; }
    }
    static class EncodedAndPos { final String enc; final int pos; EncodedAndPos(String e, int p){ enc=e; pos=p; } }
    static EncodedAndPos parseAnyValue(Ts ts){
        String t = ts.next();

        if ("lo".equals(t)) {
            String t2 = ts.next();
            if ("steni".equals(t2)) return new EncodedAndPos(encEmpty(), ts.pos());
            if ("steko".equals(t2)) return parseListLiteral(ts);
            if (isNameToken(t2))    return new EncodedAndPos(encVar(normId(stripDots(t2))), ts.pos());
            if (isNumeric(t2))      return new EncodedAndPos(encNum(Long.parseLong(t2)), ts.pos());
            return new EncodedAndPos(encVar(normId(t2)), ts.pos()); // fallback var after lo (normalized)
        }

        if (isNumeric(t))      return new EncodedAndPos(encNum(Long.parseLong(t)), ts.pos());
        if (isNameToken(t))    return new EncodedAndPos(encName(normId(stripDots(t))), ts.pos());
        if ("steni".equals(t)) return new EncodedAndPos(encEmpty(), ts.pos());
        return new EncodedAndPos(encName(t), ts.pos()); // bare word as name literal
    }

    static EncodedAndPos parseListLiteral(Ts ts){
        java.util.ArrayList<String> elems = new java.util.ArrayList<>();
        while (true) {
            EncodedAndPos head = parseAnyValue(ts);
            elems.add(head.enc);

            String lo = ts.next();
            if (!"lo".equals(lo)) throw new RuntimeException("list expects 'lo'");
            String nxt = ts.next();
            if ("steni".equals(nxt)) {
                String enc = elems.isEmpty() ? encEmpty() : "L:[" + String.join(",", elems) + "]";
                return new EncodedAndPos(enc, ts.pos());
            }
            if (!"steko".equals(nxt)) throw new RuntimeException("list expects 'steko' or 'steni'");
        }
    }

    static String showValue(String enc){
        if (enc == null) return "(unbound)";
        if (isNumEnc(enc)) return enc.substring(2);
        if (isNameEnc(enc)) return "." + enc.substring(2) + ".";
        if (isListEnc(enc)) return "(" + showListInner(enc) + ")";
        if (isVarEnc(enc))  return "." + varName(enc) + "."; 
        return enc;
    }

    static String showListInner(String listEnc){
        if ("L:[]".equals(listEnc)) return "";
        String inside = listEnc.substring(3, listEnc.length()-1); 
        java.util.List<String> parts = splitTopLevel(inside);
        java.util.List<String> shown = new java.util.ArrayList<>();
        for (String p : parts) shown.add(showValue(p.trim()));
        return String.join(" ", shown);
    }

    static java.util.List<String> splitTopLevel(String s){
        java.util.List<String> out = new java.util.ArrayList<>();
        if (s.isEmpty()) return out;
        int depth = 0, start = 0;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == '[') depth++;
            else if (ch == ']') depth--;
            else if (ch == ',' && depth == 0) {
                out.add(s.substring(start, i));
                start = i + 1;
            }
        }
        out.add(s.substring(start));
        return out;
    }

    static void evaluateProgram(List<String> tokens){
        Ts ts = new Ts(tokens);

        java.util.List<String> lastVars = new java.util.ArrayList<>();
        Env lastEnv = null;

        while (ts.has()) {
            String maybeI = ts.next();
            if (!"i".equals(maybeI)) continue;

            Env env = new Env();
            java.util.List<String> thisStmtVars = new java.util.ArrayList<>();

            boolean swap = false;
            if ("se".equals(ts.peek())) { ts.next(); swap = true; }
            // First argument
            EncodedAndPos a1 = parseAnyValue(ts);
            if (isVarEnc(a1.enc)) thisStmtVars.add(a1.enc);

            String predToken = ts.next();

            if (!swap && "se".equals(ts.peek())) { ts.next(); swap = true; }

            switch (predToken) {
                case "fatci": {
                    System.out.println("> fatci => true");
                    break;
                }
                case "steni": {
                    boolean ok = bindOrCheck(a1.enc, encEmpty(), env);
                    System.out.println("> steni => " + ok);
                    break;
                }
                case "dunli": {
                    EncodedAndPos b = parseAnyValue(ts);
                    if (isVarEnc(b.enc)) thisStmtVars.add(b.enc);
                    if (swap) { EncodedAndPos tmp = a1; a1 = b; b = tmp; }

                    String va = resolve(a1.enc, env), vb = resolve(b.enc, env);
                    boolean ok;
                    if (isVarEnc(a1.enc) && va == null && vb != null) ok = env.bind(varName(a1.enc), vb);
                    else if (isVarEnc(b.enc) && vb == null && va != null) ok = env.bind(varName(b.enc), va);
                    else ok = (va != null && va.equals(vb));
                    System.out.println("> dunli => " + ok);
                    break;
                }
                case "sumji": {
                    EncodedAndPos b = parseAnyValue(ts);
                    EncodedAndPos c = parseAnyValue(ts);
                    if (isVarEnc(b.enc)) thisStmtVars.add(b.enc);
                    if (isVarEnc(c.enc)) thisStmtVars.add(c.enc);

                    if (swap) { EncodedAndPos tmp = a1; a1 = b; b = tmp; } 

                    String vb = resolve(b.enc, env), vc = resolve(c.enc, env);
                    boolean ok = isNumEnc(vb) && isNumEnc(vc)
                            && bindOrCheck(a1.enc, encNum(decNum(vb) + decNum(vc)), env);
                    System.out.println("> sumji => " + ok);
                    break;
                }
                case "vujni": {
                    EncodedAndPos b = parseAnyValue(ts);
                    EncodedAndPos c = parseAnyValue(ts);
                    if (isVarEnc(b.enc)) thisStmtVars.add(b.enc);
                    if (isVarEnc(c.enc)) thisStmtVars.add(c.enc);

                    if (swap) { EncodedAndPos tmp = a1; a1 = b; b = tmp; }

                    String vb = resolve(b.enc, env), vc = resolve(c.enc, env);
                    boolean ok = isNumEnc(vb) && isNumEnc(vc)
                            && bindOrCheck(a1.enc, encNum(decNum(vb) - decNum(vc)), env);
                    System.out.println("> vujni => " + ok);
                    break;
                }
                case "steko": {
                    EncodedAndPos h = parseAnyValue(ts);
                    EncodedAndPos t2 = parseAnyValue(ts);
                    if (isVarEnc(h.enc)) thisStmtVars.add(h.enc);
                    if (isVarEnc(t2.enc)) thisStmtVars.add(t2.enc);

                    if (swap) { EncodedAndPos tmp = a1; a1 = h; h = tmp; } 

                    String vh = resolve(h.enc, env), vt = resolve(t2.enc, env);
                    boolean ok = isListEnc(vt);
                    String res = null;
                    if (ok) {
                        res = vt.equals("L:[]")
                            ? "L:[" + vh + "]"
                            : "L:[" + vh + "," + vt.substring(3, vt.length()-1) + "]";
                        ok = bindOrCheck(a1.enc, res, env);
                    }
                    System.out.println("> steko => " + ok);
                    break;
                }
                case "cmavo": {
                    String predName = isVarEnc(a1.enc) ? normId(varName(a1.enc))
                                    : (isNameEnc(a1.enc) ? normId(a1.enc.substring(2)) : null);
                    if (predName == null) { System.out.println("> cmavo => false (bad predicate name)"); break; }
                    EncodedAndPos params = parseAnyValue(ts);
                    EncodedAndPos body;
                    if (!ts.has() || "i".equals(ts.peek())) {
                        body = new EncodedAndPos(encEmpty(), ts.pos());
                    } else {
                        body = parseAnyValue(ts);
                    }

                    if (!isListEnc(params.enc)) { System.out.println("> cmavo => false (args must be list)"); break; }
                    String inside = params.enc.equals("L:[]") ? "" : params.enc.substring(3, params.enc.length()-1);
                    if (inside.isEmpty()) { System.out.println("> cmavo => false (needs at least one arg)"); break; }
                    java.util.List<String> parts = splitTopLevel(inside);
                    java.util.List<String> tuple = new java.util.ArrayList<>();
                    for (String p : parts) {
                        String q = p.trim();
                        if (isVarEnc(q)) q = encName(varName(q)); 
                        tuple.add(q);
                    }

                    if (isListEnc(body.enc) && body.enc.equals("L:[]")) {
                        FACTS.computeIfAbsent(predName, k -> new java.util.ArrayList<>()).add(tuple);
                        System.out.println("> cmavo (fact) => true");
                    } else {
                        System.out.println("> cmavo (rules not implemented) => false");
                    }
                    break;
                }
                default: {
        String userPred = predToken;
        if (userPred.startsWith(".") && userPred.endsWith(".")) userPred = userPred.substring(1, userPred.length()-1);
        userPred = normId(userPred); 

        java.util.List<EncodedAndPos> args = new java.util.ArrayList<>();
        args.add(a1);
        if (isVarEnc(a1.enc)) thisStmtVars.add(a1.enc);

        while (ts.has() && !"i".equals(ts.peek())) {
            EncodedAndPos ap = parseAnyValue(ts);
            args.add(ap);
            if (isVarEnc(ap.enc)) thisStmtVars.add(ap.enc);
        }

        if (swap && args.size() >= 2) {
            EncodedAndPos tmp = args.get(0);
            args.set(0, args.get(1));
            args.set(1, tmp);
        }

        java.util.List<java.util.List<String>> facts = FACTS.get(userPred);
        boolean ok = false;
        if (facts != null) {
            for (java.util.List<String> tup : facts) {
                if (tup.size() != args.size()) continue;
                Env e2 = new Env();
                boolean all = true;
                for (int k = 0; k < tup.size(); k++) {
                    if (!bindOrCheck(args.get(k).enc, tup.get(k), e2)) { all = false; break; }
                }
                if (all) { env = e2; ok = true; break; } 
            }
        }
        System.out.println("> " + userPred + " => " + ok);
        break;
                }
            }
           
            GLOBAL.putAll(env.m);
            lastVars = thisStmtVars;
            lastEnv  = env;
        }

        if (lastEnv != null && !lastVars.isEmpty()) {
            java.util.LinkedHashSet<String> seen = new java.util.LinkedHashSet<>(lastVars);
            for (String v : seen) {
                String bound = lastEnv.get(varName(v));
                if (bound != null) {
                    System.out.println(showValue(bound));
                }
            }
        }
    }
    public static void main(String[] args) {
        System.out.println("Enter your Lojban statement: ");
        Scanner scanner = new Scanner(System.in);
        String userInput = scanner.useDelimiter("\\A").next();

        if (!userInput.matches("^[A-Za-z0-9\\.\\s]+$")){
            System.out.println("Invalid character found");
            System.out.println("Only ASCII, digits 0-9, periods, and whitespaces allowed.");
        }
        if (!userInput.trim().matches("(?s)(i\\s+.+)+")){
            System.out.println("Invalid Lojban structure");
            System.out.println("Lojban statements must start with 'i' followed by at least one word.");
        }

        List<String> tokens = Tokenize.tokenize(userInput);
        validateTokensOrThrow(tokens);
        evaluateProgram(tokens);
        scanner.close();

    }
}