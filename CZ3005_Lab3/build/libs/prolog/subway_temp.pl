meals([normal, value, healthy, veggie, vegan], 0).
breadTypes([italian, hearty_italian, honey_oat, oregano, multigrain, flatbread], 0).
breadSizes([six_inch, foot_long], [0, 4]).
meats([chicken_bacon, chicken_ham, chicken_teriyaki, cold_cut_trio, italian_b_m_t, meatball_marinara, roast_beef, roasted_chicken_breast, steak, tuna, turkey], [6, 6, 6, 6, 6, 6, 7, 6, 7, 5, 6]).
meatsV([veggie_delite, veggie_patty], [7, 8]).
cheeseToppings([none, normal_cheese, processed_cheddar, monterey_cheddar], [0, 0, 1, 2]).
vegetables([none, cucumbers, green_bell_peppers, lettuce, red_onions, tomatoes, black_olives, jalapenos, pickles], 0).

sauce([none, barbeque, chili_sauce, chipotle_southwest, honey_mustard, mayonnaise, mustard, ranch, sweet_onion, tomato_sauce], 0).
sauceHealthy([none, honey_mustard, mustard, sweet_onion], 0).
topups([none, double_meat, egg_mayo_scoop, extra_cheese, more_vegetables], [0, 3, 1, 1, 0]).
topupsVegan([none, more_vegetables], 0).
topupsVeggie([none, extra_cheese, more_vegetables], [0, 1, 0]).
sides([chips, cookies, energy_bar, hashbrown, fruit_crisps, yogurt], 0).
sidesVegan([chips, cookies, energy_bar, hashbrown, fruit_crisps], 0).
sidesHealthy([energy_bar, fruit_crisps, yogurt], 0).
drinks([fountain_drinks, mineral_water, pulpy_orange, japanese_green_tea, coffee, tea], 0).
drinksHealthy([mineral_water, pulpy_orange, japanese_green_tea, coffee, tea], 0).
done(Boolean, Order):- Boolean == 1 -> findall(X:Y, selected(X, Y, _), Order).
getCosts(Costs):- findall(Z, selected(_, _, Z), Costs).

ask(Type, List, Count, Question, Cost):- selected(Type, nil, _), Type == meal -> (meals(List, Cost), Count = 1, Question = "Welcome to Subway Eat Trash!\nWhat type of meal would you like?").
ask(Type, List, Count, Question, Cost):- selected(Type, nil, _), Type == bread_type -> (breadTypes(List, Cost), Count = 1, Question = "What type of bread would you like?").
ask(Type, List, Count, Question, Cost):- selected(Type, nil, _), Type == bread_size -> (breadSizes(List, Cost), Count = 1, Question = "Would you like a six-inch bread, or a foot-long bread?").
ask(Type, List, Count, Question, Cost):- selected(Type, nil, _), Type == meat -> (not(selected(meal, veggie, _)), not(selected(meal, vegan, _)), meats(List, Cost), Count = 1, Question = "It's time to select the hero of the sandwich: the meat!").
ask(Type, List, Count, Question, Cost):- selected(Type, nil, _), Type == meat -> (meatsV(List, Cost), Count = 1, Question = "It's time to select the hero of the sandwich: the main filling!").
ask(Type, List, Count, Question, Cost):- selected(Type, nil, _), Type == cheese_toppings -> (not(selected(meal, vegan, _)), cheeseToppings(List, Cost), Count = 99, Question = "Any cheese topping to go on top of the meat?").
ask(Type, List, Count, Question, Cost):- selected(Type, nil, _), Type == vegetables -> (vegetables(List, Cost), Count = 5, Question = "Any vegetables for you?").
ask(Type, List, Count, Question, Cost):- selected(Type, nil, _), Type == sauce -> (selected(meal, Meal, _), (Meal == healthy -> sauceHealthy(List, Cost); sauce(List, Cost)), Count = 2, Question = "Any sauce for you?").
ask(Type, List, Count, Question, Cost):- selected(Type, nil, _), Type == topups -> (selected(meal, Meal, _), (Meal == vegan -> topupsVegan(List, Cost); Meal == veggie -> topupsVeggie(List, Cost); topups(List, Cost)), Count = 99, Question = "Would you like any additional fillings?").
ask(Type, List, Count, Question, Cost):- selected(Type, nil, _), Type == sides -> (selected(meal, Meal, _), (Meal == healthy -> sidesHealthy(List, Cost); Meal == vegan -> sidesVegan(List, Cost); sides(List, Cost)), Count = 1, Question = "What side would you like for your meal?").
ask(Type, List, Count, Question, Cost):- selected(Type, nil, _), Type == drink -> (selected(meal, Meal, _), (Meal == healthy -> drinksHealthy(List, Cost); drinks(List, Cost)), Count = 1, Question = "And finally... your choice of beverage?").
ask(Type, List, Count, Question, Cost):- (done(1, List), Type = final, Count = 0, Question = "Thank you! Please enjoy your meal and we hope to see you again!", Cost = 0).

selected(meal, normal, 0).
selected(bread_type, oregano, 0).
selected(bread_size, six_inch, 0).
selected(meat, turkey, 6).
selected(cheese_toppings, processed_cheddar, 1).
selected(vegetables, nil, 0).
selected(sauce, nil, 0).
selected(topups, nil, 0).
selected(sides, nil, 0).
selected(drink, nil, 0).