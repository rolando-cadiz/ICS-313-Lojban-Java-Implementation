# ICS-313-Lojban-Java-Implementation
A tiny interpreter for a Lojban-flavored predicate calculus.
It tokenizes, validates, and evaluates a sequence of i-statements with a small set of built-ins, basic facts, variables, and lists.

# Some features not implemented:
### fatci doesn’t store facts
- prints > fatci => true but does not add a fact.
### cmavo rules not implemented
- Only facts are supported (… lo steni as the third argument).
- A non-empty third argument prints > cmavo (rules not implemented) => false.

# How it works...
### User enters a Lojban program as one or more i-statements (possibly across multiple lines).

### Tokenizer (Tokenize.tokenize) runs:

- Splits the input into tokens (words, empty-space markers, newline markers).

- Produces a clean token list for the parser to consume.

### Validation (validateTokensOrThrow) checks:

- Characters are allowed (letters, digits, periods, whitespace).

- Word forms match expected Lojban structures.

- Names used as arguments are preceded by lo.

### Evaluation begins (evaluateProgram):

- Reads one statement at a time, starting with i.

- Detects if se is present to swap the first two arguments.

- Parses values (numbers, variables, names, or list literals).

### Predicate execution:

- If the predicate is built-in (fatci, sumji, vujni, dunli, steni, steko, cmavo):

- Executes the appropriate logic (math, list operations, equality, facts).

- If the predicate is user-defined (from cmavo facts):

- Attempts to match the arguments to a stored fact.

### Variable binding:

- Local bindings for the current statement are stored in a temporary environment.

- If the statement succeeds, bindings are added to the global environment (GLOBAL) for future statements.

### After all statements finish:

- The interpreter prints the values of all variables that appeared in the final statement only.

### Values are formatted as:

- Numbers → 42

- Names → .brook.

- Lists → (1 2 3)
## Files
## Lojban.java
- The main driver code
## Tokenize.java
- Separate file to tokenize user input into a string list

## Classes

---

### **Tokenize Class**
A lightweight tokenizer that converts raw input text into a list of tokens to be consumed by the `Lojban` parser.  
It preserves statement boundaries, tracks whitespace, and marks newlines explicitly to allow multi-statement parsing.

#### **Methods**

- `public static List<String> tokenize(String input)`
  - Converts the raw input string into a list of tokens.
  - **Key behaviors:**
    - Normalizes newlines (`\r\n`, `\r` → `\n`).
    - Emits a literal `"\\n"` token for every newline encountered so the parser can treat line breaks as valid separators.
    - Collapses consecutive spaces/tabs into individual empty-string tokens (`""`) to represent whitespace where needed.
    - Ignores leading whitespace immediately after a newline to allow clean formatting of multi-line `i`-statements.
    - Flushes accumulated characters into a token when a delimiter (space, tab, newline) is encountered.


### **Lojban Class**
Main interpreter that validates, parses, evaluates, and executes Lojban-like predicate calculus statements.

#### **Methods**

- `static String encNum(long n)`  
  Encode an integer as `N:<n>`.

- `static String encName(String s)`  
  Encode a name constant as `S:<s>`.

- `static String encVar(String s)`  
  Encode a variable identifier as `VAR:<s>`.

- `static String encEmpty()`  
  Encode the empty list as `L:[]`.

- `static boolean isNumEnc(String e)`  
  Check if encoded value is a number.

- `static boolean isNameEnc(String e)`  
  Check if encoded value is a name.

- `static boolean isVarEnc(String e)`  
  Check if encoded value is a variable.

- `static boolean isListEnc(String e)`  
  Check if encoded value is a list.

- `static long decNum(String e)`  
  Decode numeric encoding back into a `long`.

- `static String varName(String e)`  
  Extract variable name from a `VAR:<name>` encoding.

- `static String resolve(String enc, Env env)`  
  Resolve a value if it is a variable: first check local `Env`, then fallback to `GLOBAL`.

- `static boolean bindOrCheck(String targetEnc, String valueEnc, Env env)`  
  Bind a variable or check for equality between encoded values.

- `static boolean isNameToken(String t)`  
  Check if token matches `.name.` form.

- `static String stripDots(String t)`  
  Remove leading and trailing dots from a `.name.` token.

- `static String normId(String s)`  
  Normalize identifier to lowercase.

- `static boolean isLojVowel(char c)`  
  Check if character is a Lojban vowel.

- `static boolean isLojCons(char c)`  
  Check if character is a Lojban consonant.

- `static boolean isShortWord(String s)`  
  Check if token is a Lojban short word (CV form).

- `static boolean isGismu(String s)`  
  Check if token matches a Lojban predicate word (CVCCV or CCVCV).

- `static void validateTokensOrThrow(List<String> t)`  
  Validate the token list against allowed Lojban structure and token rules.

- `static boolean isNumeric(String s)`  
  Check valid integer formatting (no leading zeros except for 0).

- `static boolean isBlank(String t)`  
  Check if token is blank or newline placeholder.

- `static EncodedAndPos parseAnyValue(Ts ts)`  
  Parse next value token: variable, constant, list literal, number, or empty list.

- `static EncodedAndPos parseListLiteral(Ts ts)`  
  Parse a list literal built using chained `lo steko ... lo steni` structure.

- `static String showValue(String enc)`  
  Convert an encoded value into human-readable form for output.

- `static String showListInner(String listEnc)`  
  Render the inside of a list encoding into readable format.

- `static List<String> splitTopLevel(String s)`  
  Split a comma-separated list into parts while respecting nested list structure.

- `static void evaluateProgram(List<String> tokens)`  
  Execute all statements: perform unification, apply built-ins, handle predicate lookups, update `GLOBAL`, and print final bound variables.

- `public static void main(String[] args)`  
  Program entry point. Reads input, validates, tokenizes, evaluates, and prints results.

---

### **Env Class**
Represents a per-statement environment for variable bindings before merging into `GLOBAL`.

#### **Methods**

- `boolean bind(String var, String enc)`  
  Bind a variable to a value if not already bound; ensures consistency with previous local or global bindings.

- `String get(String var)`  
  Retrieve a local binding for a variable.

---

### **Ts Class**
Token stream cursor for parsing, allowing controlled traversal and lookahead.

#### **Methods**

- `boolean has()`  
  Check if more non-blank tokens exist.

- `String peek()`  
  Look ahead at next token without consuming it.

- `String next()`  
  Consume and return the next token; throws on end of input.

- `int pos()`  
  Return current index in token list.

- `void setPos(int p)`  
  Set cursor position (used for minimal backtracking).

---

### **EncodedAndPos Class**
A data holder storing an encoded value and the token index it was parsed at.

#### **Methods**

- **Constructor:** `EncodedAndPos(String enc, int pos)`  
  Store an encoded value with the parser position.


