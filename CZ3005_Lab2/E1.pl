company(sumSum).
company(appy).
competitor(sumSum, appy).
smartPhoneTechnology(galacticaS3).
developed(sumSum, galacticaS3).
boss(stevey).
rival(X):-competitor(X, appy); competitor(appy, X).
stole(stevey, galacticaS3).
business(X):-smartPhoneTechnology(X).
unethical(Name):-boss(Name), stole(Name, Stolen), business(Stolen), developed(Developer, Stolen), rival(Developer).
