package tools

import org.jpl7.Atom
import org.jpl7.Query
import org.jpl7.Term
import org.jpl7.Variable

class Prolog {
    companion object {
        var fallback: String = ""
        private var consulted: Boolean = false

        fun consult(file: String) {
            val success: Boolean = Query("consult", arrayOf<Term>(Atom("prolog\\${file}"))).hasSolution()
            consulted = success
            if (!success) println("Database consult failed.")
        }

        fun query(queryTerm: String, vararg variables: String): Array<Array<String>> {
            val termList = ArrayList<Term>()
            for (variable in variables) termList.add(Atom(variable))
            return query(queryTerm, termList.toTypedArray())
        }

        fun query(queryTerm: String, vararg variables: Variable): Array<Array<String>> {
            val termList = ArrayList<Term>()
            for (variable in variables) termList.add(variable)
            return query(queryTerm, termList.toTypedArray())
        }

        private fun query(queryTerm: String, terms: Array<Term>): Array<Array<String>> {
            if (!consulted) {
                if (fallback.isBlank()) return arrayOf()
                consult(fallback)
            }

            val query = Query(queryTerm, terms)
            val provable: Boolean = query.hasSolution()

            if (!provable) {
                println(provable); return arrayOf()
            }

            val solution = query.oneSolution()

            if (solution.isEmpty()) {
                println(provable); return arrayOf()
            }

            val stringList = ArrayList<Array<String>>()

            for (key in solution.keys) {
                val term: Term = solution[key] ?: continue
                val solutionList = ArrayList<String>()

                if (term.args().isEmpty()) {
                    solutionList.add("$term")
                    stringList.add(solutionList.toTypedArray())
                    continue
                }

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

        fun updateSelection(responseType: String, selection: String, cost: Int) {
            updateDatabase("selected($responseType, nil, 0).", "selected($responseType, ${selection}, ${cost}).")
        }

        fun updateTotalCost(cost: Int) {
            updateDatabase("totalCost(", "totalCost(${cost}).")
        }

        fun updatePaymentMethod(method: String) {
            updateDatabase("paymentMethod(nil).", "paymentMethod(${method}).")
        }

        fun updateRepeated(repeat: Int) {
            updateDatabase("repeat(0).", "repeat(${repeat}).")
        }

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