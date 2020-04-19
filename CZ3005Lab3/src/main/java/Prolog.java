import org.jpl7.Atom;
import org.jpl7.Query;
import org.jpl7.Term;
import org.jpl7.Variable;

import java.util.ArrayList;
import java.util.Map;

public class Prolog {
    private Prolog() {

    }

    /**
     * SETS A FILE TO BE CONSULTED BY PROLOG.
     * @param file FILE TO BE CONSULTED
     */
    public static void consult(String file) {
        String filePath = "prolog\\" + file;
        Query q = new Query("consult", new Term[] {new Atom(filePath)});
        q.hasSolution();
    }

    /**
     * SENDS A QUERY TO PROLOG WITH SOLUTIONS EXPECTED.
     * @param q QUERY COMMAND
     * @param variables VARIABLES IN THE QUERY
     * @return 2D ARRAY CONSISTING OF SOLUTIONS FOR EACH VARIABLE. ONLY THE FIRST SOLUTION OF THE QUERY IS RETURNED.
     */
    public static String[][] query(String q, Variable ...variables) {
        Query query = new Query(q, variables);

        if (!query.hasSolution()) {
            System.out.println("No solution available.");
            return new String[][] {};
        }

        Map<String, Term> solution = query.oneSolution();

        // IF QUERY RESOLVES TO TRUE/FALSE WITH NO SOLUTION, DISPLAY RESULT IN THE CONSOLE.
        // FOR DEBUGGING ONLY.
        if (solution.isEmpty()) {
            System.out.println("Solution resolves to true.");
            return new String[][] {};
        }

        ArrayList<String[]> ret = new ArrayList<>();

        // THE SOLUTION RETURNED NEEDS TO BE PARSED.
        // PARSES UP TO THREE NESTED TERM ARRAYS DEEP
        for (String key : solution.keySet()) {
            Term term = solution.get(key);
            ArrayList<String> list = new ArrayList<>();

            if (term.args().length == 0) {
                list.add(term.toString());
            } else {
                // IF TERM IS A LIST, THE TERM STRING IS PREFIXED WITH ['|']
                // IF TERM HAS A TEMPLATE SET, THE TERM STRING IS PREFIXED WITH ['X'] WHERE X IS THE DIVIDER OF THE TEMPLATE
                char divider = term.toString().charAt(2);
                Term[] arguments = (divider == '|') ? term.toTermArray() : term.args();

                for (Term t : arguments) {
                    if (t.args().length == 0) {
                        list.add(t.toString());
                    } else {
                        divider = t.toString().charAt(2);
                        arguments = (divider == '|') ? t.toTermArray() : t.args();

                        if (divider != '|') {
                            divider = t.toString().charAt(1);
                        }

                        StringBuilder stringBuilder = new StringBuilder();

                        for (Term t2 : arguments) {
                            if (t2.args().length == 0) {
                                stringBuilder.append(t2.toString());
                            } else {
                                System.out.println(t.toString());
                                arguments = (t2.toString().charAt(2) == '|') ? t2.toTermArray() : t2.args();
                                StringBuilder stringBuilder1 = new StringBuilder();

                                for (Term t3 : arguments) {
                                    if (stringBuilder1.length() > 0) {
                                        stringBuilder1.append(", ");
                                    }

                                    stringBuilder1.append(t3.toString());
                                }

                                stringBuilder.append(stringBuilder1.toString());
                            }

                            stringBuilder.append(divider);
                        }

                        String str = stringBuilder.toString().trim();
                        str = str.substring(0, str.length() - 1);
                        list.add(str);
                    }
                }
            }

            ret.add(list.toArray(new String[0]));
        }

        return ret.toArray(new String[0][0]);
    }
}
