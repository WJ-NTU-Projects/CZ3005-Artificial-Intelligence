offspring(charles, male, 1).
offspring(ann, female, 2).
offspring(andrew, male, 3).
offspring(edward, male, 4).

mergelist([], L, L).
mergelist([H|T], L, [H|R]):-mergelist(T, L, R).
succession_compute(List):-findall(Order:Name, offspring(Name, male, Order), UnsortedM), sort(1, @<, UnsortedM, Males), findall(Order:Name, offspring(Name, female, Order), UnsortedF), sort(1, @<, UnsortedF, Females), mergelist(Males, Females, List).
succession(List):-succession_compute(A), findall(Name, (member(Element, A), with_output_to(atom(Atom), write(Element)), atomic_list_concat(Splitted, :, Atom), nth0(1, Splitted, Name)), List).