/**
 * Knowledge base of user selections for the sandwich. 
 * These knowledges are referenced by the rest of the scripts to determine what option or prompt to offer to the user.
 * The knowledge base is initialised as 'nil' as the script checks for 'nil' to determine if the user has made a selection in that category.
 *
 * Parameter 1 - Type or category of the selection
 * Parameter 2 - User-selected value
 * Parameter 3 - Cost of the selected value (for price calculation)
 */
selected(meal, nil, 0).
selected(bread_type, nil, 0).
selected(bread_size, nil, 0).
selected(fillings, nil, 0).
selected(cheese_toppings, nil, 0).
selected(vegetables, nil, 0).
selected(sauces, nil, 0).
selected(topups, nil, 0).
selected(sides, nil, 0).
selected(drink, nil, 0).

/**
 * Knowledge base of computations based on user selections or user selections for other miscellaneous stuff. 
 * These knowledges are referenced by the rest of the scripts to determine what option or prompt to offer to the user.
 *
 * totalCost (cost)          - The total cost of the user's sandwich computated based on his/her selections.
 * paymentMethod(method)    - The payment method selected by the user. Initialised as 'nil' as the script checks for 'nil' to determine if the user has made a selection.
 * repeat(bool)             - Sets to 1 (instructed by the program) if the program is being repeated (user chooses to place a new order at the end). Affects certain dialogues.
 */
totalCost(0).
paymentMethod(nil).
repeat(0).

/**
 * Knowledge base of facts for user to choose from for the sandwich.
 * 
 * Parameter 1 - List of facts.
 * Parameter 2 - Cost of each fact (in a list) or for all facts (just an integer).
 */
meals([normal, value, healthy, veggie, vegan], 0).
breadTypes([italian, hearty_italian, honey_oat, oregano, multigrain, flatbread], 0).
breadSizes([six_inch, foot_long], [0, 4]).
meats([chicken_bacon, chicken_ham, chicken_teriyaki, cold_cut_trio, italian_b_m_t, meatball_marinara, roasted_beef, roasted_chicken_breast, steak, tuna, turkey, subway_club], [6, 6, 6, 6, 6, 6, 7, 6, 7, 5, 6, 8]).
meatsHealthy([cold_cut_trio, roasted_beef, roasted_chicken_breast], [6, 7, 6]).
meatsV([veggie_delite, veggie_patty], [7, 8]).
cheeseToppings([none, normal_cheese, processed_cheddar, monterey_cheddar], [0, 0, 1, 2]).
vegetables([none, cucumbers, green_bell_peppers, lettuce, red_onions, tomatoes, black_olives, jalapenos, pickles], 0).
sauce([none, barbeque, chili_sauce, chipotle_southwest, honey_mustard, mayonnaise, mustard, sweet_onion, tomato_sauce], 0).
sauceHealthy([none, honey_mustard, mustard, sweet_onion], 0).
topups([none, double_fillings, extra_cheese, more_vegetables], [0, 3, 1, 0]).
topupsVegan([none, more_vegetables], 0).
topupsVeggie([none, extra_cheese, more_vegetables], [0, 1, 0]).
sides([chips, cookies, energy_bar, fruit_crisps, yogurt], 0).
sidesVegan([chips, cookies, energy_bar, fruit_crisps], 0).
sidesHealthy([energy_bar, fruit_crisps, yogurt], 0).
drinks([fountain_drinks, mineral_water, pulpy_orange, japanese_green_tea, coffee, tea], 0).
drinksHealthy([mineral_water, pulpy_orange, japanese_green_tea, coffee, tea], 0).
drinksVegan([fountain_drinks, mineral_water, pulpy_orange, japanese_green_tea], 0).

/**
 * Knowledge base for miscellaneous facts.
 *
 * paymentOptions(choices)  - Payment options for user to choose from.
 * selectionRows(rowList)   - For the program to determine the maximum number of rows to display for a given category. It is a one-to-one matching with defined displayIds (0 to 10).
 *                            It is separated from the knowledge base of facts to prevent redundancies (there may be multiple bases for one category, ex: sauces with sauces() and saucesHealthy()).
 */
paymentOptions([credit_card, paylah, cash]).
selectionRows([1, 1, 1, 2, 1, 2, 2, 1, 1, 1, 1]).

/**
 * Functions for the program to extract certain facts or information from the knowledge base.
 *
 * done(bool, order_list)   - If bool is true, returns the user's sandwich order based on his/her selections.
 * getCosts(Costs)          - Gets the total cost for EACH SELECTED CATEGORY. May be more elegant but works for now.
 */
done(Boolean, Order):- Boolean == 1 -> (findall(X:Y, selected(X, Y, _), Order)).
getCosts(Costs):- findall(Z, selected(_, _, Z), Costs).

/**
 * Primary query for the program.
 * Based on the knowledge base of user selections, one of these queries will evaluate to true and be returned to the program.
 * The queries are ordered based on the display order flow of the program.
 * The queries are split up otherwise the singular query will be extremely long. 
 * Each query is 'responsible' for a certain category (separation of responsibilities). It helps with the isolation of issues when modifying a query for a specific category.
 * Some facts or variables are defined here (ex: Parameter 3) to prevent redundancies (there may be multiple bases for one category, ex: sauces with sauces() and saucesHealthy()).
 *
 * Parameter 1 - Type or category for logging of the user's selection.
 * Parameter 2 - List of user-selectable options to be returned to the program for display.
 * Parameter 3 - Maximum number of selections the user can make for the current category.
 * Parameter 4 - Prompt or question to be displayed by the program to guide the user (with voice).
 * Parameter 5 - Cost for the selection options in the current category.
 * Parameter 6 - Display ID or order for the program.
 */
ask(Type, List, 1, Prompt, Cost, 0)   :- selected(Type, nil, _), Type == meal -> (meals(List, Cost), repeat(A), (A == 0 -> Prompt = "Welcome to Subway!\n\nWhat type of meal would you like?"; Prompt = "Alright! What type of meal would you like for your new order?")).
ask(Type, List, 1, Prompt, Cost, 1)   :- selected(Type, nil, _), Type == bread_type -> (breadTypes(List, Cost), Prompt = "What type of bread would you like?").
ask(Type, List, 1, Prompt, Cost, 2)   :- selected(Type, nil, _), Type == bread_size -> (breadSizes(List, Cost), Prompt = "Would you like a six-inch bread, or a foot-long bread?").
ask(Type, List, 1, Prompt, Cost, 3)   :- selected(Type, nil, _), Type == fillings -> (not(selected(meal, veggie, _)), not(selected(meal, vegan, _)), (selected(meal, Meal, _), (Meal == healthy -> meatsHealthy(List, Cost); meats(List, Cost))), Prompt = "It's time to select the hero of the sandwich: the meat fillings!").
ask(Type, List, 1, Prompt, Cost, 3)   :- selected(Type, nil, _), Type == fillings -> (meatsV(List, Cost), Prompt = "It's time to select the hero of the sandwich: the main fillings!").
ask(Type, List, 99, Prompt, Cost, 4)  :- selected(Type, nil, _), Type == cheese_toppings -> (not(selected(meal, vegan, _)), cheeseToppings(List, Cost), Prompt = "Any cheese topping to go on top of the fillings?").
ask(Type, List, 6, Prompt, Cost, 5)   :- selected(Type, nil, _), Type == vegetables -> (vegetables(List, Cost), Prompt = "Any vegetables for you?").
ask(Type, List, 3, Prompt, Cost, 6)   :- selected(Type, nil, _), Type == sauces -> (selected(meal, Meal, _), (Meal == healthy -> sauceHealthy(List, Cost); sauce(List, Cost)), Prompt = "Any sauce for you?").
ask(Type, List, 99, Prompt, Cost, 7)  :- selected(Type, nil, _), Type == topups -> (not(selected(meal, value, _)), selected(meal, Meal, _), (Meal == vegan -> topupsVegan(List, Cost); Meal == veggie -> topupsVeggie(List, Cost); topups(List, Cost)), Prompt = "Would you like any additional fillings?").
ask(Type, List, 1, Prompt, Cost, 8)   :- selected(Type, nil, _), Type == sides -> (selected(meal, Meal, _), (Meal == healthy -> sidesHealthy(List, Cost); Meal == vegan -> sidesVegan(List, Cost); sides(List, Cost)), Prompt = "What side would you like for your meal?").
ask(Type, List, 1, Prompt, Cost, 9)   :- selected(Type, nil, _), Type == drink -> (selected(meal, Meal, _), (Meal == healthy -> drinksHealthy(List, Cost); Meal == vegan -> drinksVegan(List, Cost); drinks(List, Cost)), Prompt = "And finally... your choice of beverage?").
ask(Type, List, 0, Prompt, 0, 0)      :- totalCost(0), done(1, List), Type = final, Prompt = "Here's your order summary. The total amount would be".
ask(payment, List, 1, Prompt, 0, 10)  :- paymentMethod(nil), paymentOptions(List), Prompt = "How would you like to make payment?".
ask(end, [good_bye], 1, Prompt, 0, 0)         :- Prompt = "Thank you. Your payment has been processed successfully.\n\nPlease enjoy your meal and we hope to see you again!".