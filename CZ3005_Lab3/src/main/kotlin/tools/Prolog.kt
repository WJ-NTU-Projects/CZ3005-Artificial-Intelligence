package tools

import org.jpl7.Atom
import org.jpl7.Query
import org.jpl7.Term
import org.jpl7.Variable

/**
 * Interface with Prolog engine via JPL.
 */
class Prolog {
    companion object {
        var fallback: String = ""
        private var consulted: Boolean = false

        /**
         * Sets a prolog script file to be consulted from.
         */
        fun consult(file: String) {
            val success: Boolean = Query("consult", arrayOf<Term>(Atom("prolog\\${file}"))).hasSolution()
            consulted = success
            if (!success) println("Database consult failed.")
        }

        /**
         * Sends a query to the prolog engine.
         * @param queryTerm - The query command or function in the prolog script.
         * @param variables - Variables in the command or function, can be infinitely extended as it is a vararg.
         * @return An array of an array of strings. Returns only the first solution of the query. First array is the solution for each variable. Second array is an array as the solution may be a list.
         */
        fun query(queryTerm: String, vararg variables: Variable): Array<Array<String>> {
            val termList = ArrayList<Term>()
            for (variable in variables) termList.add(variable)
            return query(queryTerm, termList.toTypedArray())
        }

        /**
         * Sends a query to the prolog engine with a fixed terms array.
         * @param queryTerm - The query command or function in the prolog script.
         * @param terms - Terms representing variables in the command or function. Fixed length.
         * @return An array of an array of strings. Returns only the first solution of the query. First array is the solution for each variable. Second array is an array as the solution may be a list.
         */
        private fun query(queryTerm: String, terms: Array<Term>): Array<Array<String>> {
            // If consult() was never called, consult using fallback script. If fallback script is never set, then return nothing.
            if (!consulted) {
                if (fallback.isBlank()) return arrayOf()
                consult(fallback)
            }

            // First check if query has solution.
            // If false or has no solution (true/false evaluation), prints the result in the console and return nothing.
            val query = Query(queryTerm, terms)
            val provable: Boolean = query.hasSolution()

            if (!provable) {
                println(provable); return arrayOf()
            }

            val solution = query.oneSolution()

            if (solution.isEmpty()) {
                println(provable); return arrayOf()
            }

            // Collates the solution(s) into an array.
            val stringList = ArrayList<Array<String>>()

            for (key in solution.keys) {
                val term: Term = solution[key] ?: continue
                val solutionList = ArrayList<String>()

                if (term.args().isEmpty()) {
                    solutionList.add("$term")
                    stringList.add(solutionList.toTypedArray())
                    continue
                }

                // If it is a normal list, the response string is prefixed with ['|'].
                // If a template is set, the response string is prefixed with ['template_divider'] where template_divider is the character that separates two terms.
                // Currently checks up to three levels deep (hard-code cause we know how the rules are defined, for simplicity reasons).
                var divider: Char = "$term"[2]
                var args = if (divider == '|') term.toTermArray() else term.args()

                for (t in args) {
                    if (t.args().isEmpty()) {
                        solutionList.add("$t"); continue
                    }

                    divider = "$t"[2]
                    args = if (divider == '|') t.toTermArray() else t.args()
                    if (divider != '|') divider = "$t"[1]
                    var s2 = ""

                    for (t2 in args) {
                        if (t2.args().isEmpty()) {
                            s2 += "$t2"
                            s2 += "$divider "
                            continue
                        }

                        divider = "$t2"[2]
                        args = if (divider == '|') t2.toTermArray() else t2.args()
                        var s3 = ""

                        for (t3 in args) {
                            if (s3.isNotBlank()) s3 += ", "
                            s3 += "$t3"
                        }

                        s2 += s3
                        s2 += "$divider "
                    }

                    s2 = s2.trim().dropLast(1)
                    solutionList.add(s2)
                }


                stringList.add(solutionList.toTypedArray())
            }

            return stringList.toTypedArray()
        }

        /**
         * Updates the selection knowledge base in the Prolog script.
         * @param responseType The stage to modify the selection for.
         * @param selection Selection STRING to be set. Lists should start with '[' and end with ']'.
         * @param cost Cost of the selections for the stage.
         */
        fun updateSelection(responseType: String, selection: String, cost: Int) {
            updateDatabase("selected($responseType, nil, 0).", "selected($responseType, ${selection}, ${cost}).")
        }

        /**
         * Updates the total cost of all selections in the Prolog script.
         * @param cost Cost of to be set.
         */
        fun updateTotalCost(cost: Int) {
            updateDatabase("totalCost(", "totalCost(${cost}).")
        }

        /**
         * Updates the payment method selected in the Prolog script.
         * @param method Payment method STRING to be set.
         */
        fun updatePaymentMethod(method: String) {
            updateDatabase("paymentMethod(nil).", "paymentMethod(${method}).")
        }

        /**
         * Updates the 'repeat' state of the program in the Prolog script, basically if the program is repeating. (affects certain dialogues).
         * @param repeat 1 or 0 -> Represents true or false that the program is in repeat state.
         */
        fun updateRepeated(repeat: Int) {
            updateDatabase("repeat(0).", "repeat(${repeat}).")
        }

        /**
         * Modifies the Prolog KB by physically modifying the Prolog script (the temp file, not the original).
         * @param lineToFind The line to search for to update.
         * @param lineToUpdate The content to REPLACE the found line.
         */
        private fun updateDatabase(lineToFind: String, lineToUpdate: String) {
            val fileContent: Array<String> = File.readPrologFile("subway_temp.pl").toTypedArray()

            loop@
            for ((lineIndex, line) in fileContent.withIndex()) {
                if (!line.contains(lineToFind)) continue@loop
                fileContent[lineIndex] = lineToUpdate
                break@loop
            }

            var s = ""

            for (line in fileContent) {
                if (s.isNotBlank()) s += "\n"
                s += line
            }

            File.replacePrologFileContent("subway_temp.pl", s)
        }
    }
}