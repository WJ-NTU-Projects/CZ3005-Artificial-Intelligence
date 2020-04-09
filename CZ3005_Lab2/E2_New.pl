offspring(charles, 1).
offspring(ann, 2).
offspring(andrew, 3).
offspring(edward, 4).

succession_compute(Sorted):-findall(Order:Name, offspring(Name, Order), Unsorted), sort(1, @<, Unsorted, Sorted).
succession(List):-succession_compute(A), findall(Name, (member(Element, A), with_output_to(atom(Atom), write(Element)), atomic_list_concat(Splitted, :, Atom), nth0(1, Splitted, Name)), List).