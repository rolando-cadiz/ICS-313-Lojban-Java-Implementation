# ICS-313-Lojban-Java-Implementation

## Classes

---

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
  Check if tok
