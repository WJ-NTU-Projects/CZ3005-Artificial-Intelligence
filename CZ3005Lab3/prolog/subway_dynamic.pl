meal([normal,veggie,healthy,vegan,value]).
size([foot_long,six_inch]).
breads([italian,hearty_italian,multi_grain,honey_oat,parmesan_oregano,flat_bread]).
meat([chicken,tuna,beef,turkey]).
veggies([lettuce,tomato,onion,capsicum,olive,pickle,cucumber,jalapeno,none]).
sauce([mayonnaise,mustard,honey_mustard,chipotle_southwest,sweet_onion,ranch,bbq,chilli,ketchup,none]).
sauce_healthy([mustard,honey_mustard,sweet_onion,none]).
topup([extra_cheese,double_meat,add_diced_mushrooms,add_tuna,add_bacon,none]).
topup_vegan([add_diced_mushrooms,none]).
topup_veggie([extra_cheese,add_diced_mushrooms,none]).
sides([chips,cookies,fruit_crisps,yogurt]).
sides_healthy([fruit_crisps,yogurt]).
drinks([soda,mineral_water,orange_juice,green_tea]).
drinks_healthy([mineral_water,orange_juice]).

:-dynamic selected/2.

selected(meal,nil).
selected(size,nil).
selected(breads,nil).
selected(meat,nil).
selected(veggies,nil).
selected(sauce,nil).
selected(topup,nil).
selected(sides,nil).
selected(drinks,nil).

display_meal(X):-meal(X).
display_size(X):-size(X).
display_breads(X):-breads(X).
display_meat(X):-meat(X).
display_veggies(X):-veggies(X).
display_sauce(X):-selected(meal,Y),Y==healthy->sauce_healthy(X);sauce(X).
display_topup(X):-selected(meal,Y),Y==vegan->topup_vegan(X);Y==veggie->topup_veggie(X);topup(X).
display_sides(X):-selected(meal,Y),Y==healthy->sides_healthy(X);sides(X).
display_drinks(X):-selected(meal,Y),Y==healthy->drinks_healthy(X);drinks(X).
done(Z,Final):-Z==1->findall(X:Y,selected(X,Y),Final).

ask(X,Y,Z):-(selected(X,nil),X==meal->(display_meal(Y)),Z=1).
ask(X,Y,Z):-(selected(X,nil),X==size->(display_size(Y)),Z=1).
ask(X,Y,Z):-(selected(X,nil),X==breads->(display_breads(Y)),Z=1).
ask(X,Y,Z):-(selected(X,nil),X==meat->(not(selected(meal,veggie)),not(selected(meal,vegan)),display_meat(Y)),Z=1).
ask(X,Y,Z):-(selected(X,nil),X==veggies->(display_veggies(Y)),Z=9).
ask(X,Y,Z):-(selected(X,nil),X==sauce->(display_sauce(Y)),Z=2).
ask(X,Y,Z):-(selected(X,nil),X==topup->(display_topup(Y)),Z=6).
ask(X,Y,Z):-(selected(X,nil),X==sides->(display_sides(Y)),Z=1).
ask(X,Y,Z):-(selected(X,nil),X==drinks->(display_drinks(Y)),Z=1).
ask(X,Y,Z):-done(1,Y),X=final,Z=0.



